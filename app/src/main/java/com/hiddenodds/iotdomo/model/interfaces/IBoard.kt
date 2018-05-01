package com.hiddenodds.iotdomo.model.interfaces

import io.reactivex.subjects.Subject

interface IBoard {
    var observableDevicesNames: Subject<List<String>>
    var observableMessage: Subject<String>
    fun initSDK()
    fun scanDevices()
    fun selectDevice(index: Int)
    fun connect()
    fun digitalWrite(index: Int)
    fun disconnect()
    fun permissionLocation(): Boolean
    fun ifSelectedScannedDevice(): Boolean
}