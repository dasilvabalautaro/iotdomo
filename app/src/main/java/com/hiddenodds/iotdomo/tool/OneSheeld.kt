package com.hiddenodds.iotdomo.tool

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.hiddenodds.iotdomo.App
import com.hiddenodds.iotdomo.R
import com.hiddenodds.iotdomo.model.interfaces.IBoard
import com.integreight.onesheeld.sdk.*
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class OneSheeld @Inject constructor(private val activity:
                                    AppCompatActivity,
                                    private val permissionUtils:
                                    PermissionUtils): IBoard,
        ActivityCompat.OnRequestPermissionsResultCallback{

    val LOCATION_PERMISSION_REQUEST_CODE = 1
    val TIME_OUT_SCAN = 20
    private val context = App.appComponent.context()
    private var oneSheeldManager: OneSheeldManager? = null
    private var selectedScannedDevice: OneSheeldDevice? = null
    private var oneSheeldScannedDevices: MutableList<OneSheeldDevice> =
            ArrayList()
    private var oneSheeldScannedDevicesNames: MutableList<String> =
            ArrayList()
    private var oneSheeldConnectedDevices: MutableList<OneSheeldDevice> =
            ArrayList()
    private var isBaudRateQueried: Boolean = false
    private var digitalWriteState: Boolean = false
    private var receivedStringBuilder = StringBuilder()
    private var pendingRenames: HashMap<String, String> = HashMap()
    private var message: String = ""
    override var observableDevicesNames: Subject<List<String>> =
            PublishSubject.create()
    override var observableMessage: Subject<String> =
            PublishSubject.create()

    init {
        observableDevicesNames
                .subscribe { oneSheeldScannedDevicesNames }
        observableMessage
                .subscribe { message }
    }

    private val scanningCallback = object : OneSheeldScanningCallback() {
        override fun onDeviceFind(device: OneSheeldDevice?) {
            if (device != null){
                oneSheeldScannedDevices.add(device)
                oneSheeldScannedDevicesNames.add(device.name)
            }

        }

        override fun onScanFinish(foundDevices: List<OneSheeldDevice>?) {
            if (foundDevices != null){
                observableDevicesNames.onNext(oneSheeldScannedDevicesNames)
            }else{
                message = "Devices not exists."
                observableMessage.onNext(message)
            }
        }
    }

    private val testingCallback = object : OneSheeldTestingCallback() {
        override fun onFirmwareTestResult(device: OneSheeldDevice?,
                                          isPassed: Boolean) {
            message = device!!.name + ": Firmware test result: " +
                    if (isPassed) "Correct" else "Failed"
            observableMessage.onNext(message)
        }

        override fun onLibraryTestResult(device: OneSheeldDevice?, isPassed: Boolean) {
            message = device!!.name + ": Library test result: " +
                    if (isPassed) "Correct" else "Failed"
            observableMessage.onNext(message)

        }

        override fun onFirmwareTestTimeOut(device: OneSheeldDevice?) {
            message = device!!.name + ": Error, firmware test timeout!"
            observableMessage.onNext(message)
        }

        override fun onLibraryTestTimeOut(device: OneSheeldDevice?) {
            message = device!!.name + ": Error, library test timeout!"
            observableMessage.onNext(message)
        }
    }

    private val baudRateQueryCallback = object :
            OneSheeldBaudRateQueryCallback() {
        override fun onBaudRateQueryResponse(device: OneSheeldDevice?,
                                             supportedBaudRate: SupportedBaudRate?) {
            if (isBaudRateQueried) {
                message = device!!.name +
                        if (supportedBaudRate != null) ": Current baud rate: " +
                                supportedBaudRate.baudRate else ": Device responded with an unsupported baud rate"
                observableMessage.onNext(message)
                isBaudRateQueried = false
            }
        }
    }

    private val dataCallback = object : OneSheeldDataCallback() {
        override fun onSerialDataReceive(device: OneSheeldDevice?, data: Int) {
            receivedStringBuilder.append(data.toChar())
            if (receivedStringBuilder.count() >= 1) {
                message = receivedStringBuilder.toString()
                observableMessage.onNext(message)
            }
        }
    }

    private val firmwareUpdateCallback = object : OneSheeldFirmwareUpdateCallback() {
        override fun onStart(device: OneSheeldDevice?) {
            message = "Starting.."
            observableMessage.onNext(message)
        }

        override fun onProgress(device: OneSheeldDevice?,
                                totalBytes: Int, sentBytes: Int) {
            message = (sentBytes.toFloat() / totalBytes * 100)
                    .toInt().toString() + "%"
            observableMessage.onNext(message)
        }

        override fun onSuccess(device: OneSheeldDevice?) {
            message = device!!.name + ": Firmware update succeeded!"
            observableMessage.onNext(message)
        }

        override fun onFailure(device: OneSheeldDevice?, isTimeOut: Boolean) {
            message = device!!.name + ": Firmware update failed!" +
                    if (isTimeOut) "Time-out occurred!" else ""
            observableMessage.onNext(message)
        }
    }

    private val renamingCallback = object : OneSheeldRenamingCallback() {

        override fun onRenamingAttemptTimeOut(device: OneSheeldDevice?) {
            message = device!!.name + ": Error, renaming attempt failed, retrying!"
            observableMessage.onNext(message)

        }

        override fun onAllRenamingAttemptsTimeOut(device: OneSheeldDevice?) {
            message = device!!.name + ": Error, all renaming attempts failed!"
            observableMessage.onNext(message)
            pendingRenames.remove(device.address)
        }

        override fun onRenamingRequestReceivedSuccessfully(device: OneSheeldDevice?) {
            message = device!!.name + ": Renaming request received successfully!"
            observableMessage.onNext(message)

        }
    }

    private val connectionCallback = object : OneSheeldConnectionCallback() {
        override fun onConnect(device: OneSheeldDevice?) {
            oneSheeldScannedDevices.remove(device)
            oneSheeldConnectedDevices.add(device!!)
            device.addTestingCallback(testingCallback)
            device.addRenamingCallback(renamingCallback)
            device.addDataCallback(dataCallback)
            device.addBaudRateQueryCallback(baudRateQueryCallback)
            device.addFirmwareUpdateCallback(firmwareUpdateCallback)
        }

        override fun onDisconnect(device: OneSheeldDevice?) {
            oneSheeldConnectedDevices.remove(device)

        }
    }

    override fun initSDK() {
        OneSheeldSdk.setDebugging(true)
        OneSheeldSdk.init(activity)
        oneSheeldManager = OneSheeldSdk.getManager()
        oneSheeldManager!!.connectionRetryCount = 1
        oneSheeldManager!!.setAutomaticConnectingRetriesForClassicConnections(true)
        oneSheeldManager!!.addScanningCallback(scanningCallback)
        oneSheeldManager!!.addConnectionCallback(connectionCallback)
        oneSheeldManager!!.addErrorCallback(errorCallback)

    }

    override fun scanDevices() {
        when {
            ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED -> permissionUtils
                    .requestPermission(activity,
                            LOCATION_PERMISSION_REQUEST_CODE,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
            else -> executeScan()
        }

    }

    override fun permissionLocation():Boolean{
        return permissionUtils
                .requestPermission(activity,
                        LOCATION_PERMISSION_REQUEST_CODE,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        when {
            requestCode != LOCATION_PERMISSION_REQUEST_CODE -> return
            permissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_COARSE_LOCATION) -> scanDevices()
            else -> activity.toast(context.getString(R.string.access_not_allowed))
        }
    }


    private fun executeScan(){
        oneSheeldManager!!.scanningTimeOut = TIME_OUT_SCAN
        oneSheeldManager!!.cancelScanning()
        oneSheeldScannedDevices.clear()
        oneSheeldManager!!.scan()
    }

    override fun selectDevice(index: Int) {
        if (oneSheeldScannedDevices.isNotEmpty()){
            selectedScannedDevice = oneSheeldScannedDevices[index]
        }
    }

    override fun ifSelectedScannedDevice(): Boolean {
        return (selectedScannedDevice != null)
    }

    override fun connect() {
        if (selectedScannedDevice != null){
            oneSheeldManager!!.cancelScanning()
            selectedScannedDevice!!.setBaudRate(SupportedBaudRate._115200)
            selectedScannedDevice!!.connect()
            println("Connected device.")
        }else{
            println("Device not selected.")
        }
    }

    override fun digitalWrite(index: Int) {
        if (selectedScannedDevice != null){
            digitalWriteState = !digitalWriteState
            selectedScannedDevice!!.digitalWrite(index, digitalWriteState)
        }
    }

    override fun disconnect() {
        if (oneSheeldManager != null){
            oneSheeldManager!!.cancelScanning()
            oneSheeldManager!!.disconnectAll()
        }
    }


    private val errorCallback = object : OneSheeldErrorCallback() {
        override fun onError(device: OneSheeldDevice?, error: OneSheeldError?) {
            message = "Error: " +
                    error!!.toString() +
                    if (device != null) " in " +
                            device.name else ""
            observableMessage.onNext(message)
        }
    }

    private fun Activity.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

}