package com.hiddenodds.iotdomo.presentation.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.hiddenodds.iotdomo.R
import com.hiddenodds.iotdomo.presentation.view.activity.EnrollActivity
import com.hiddenodds.iotdomo.presentation.view.activity.MainActivity
import com.hiddenodds.iotdomo.presentation.view.activity.RecognitionActivity
import com.hiddenodds.iotdomo.presentation.view.activity.TrainingActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject


class FaceFragment: AuthenticateFragment() {
    @BindView(R.id.tv_details)
    @JvmField var tvDetails: TextView? = null
    @BindView(R.id.pb_download)
    @JvmField var pbDownload: ProgressBar? = null
    @BindView(R.id.iv_image)
    @JvmField var ivImage: ImageView? = null
    @BindView(R.id.bt_detect)
    @JvmField var btDetect: Button? = null

    @OnClick(R.id.bt_detect)
    fun executeDetect(){
        val intent = Intent(context, EnrollActivity::class.java)
        startActivity(intent)
        (activity as MainActivity).finish()
    }

    @OnClick(R.id.bt_training)
    fun executeTraining(){
        val intent = Intent(context, TrainingActivity::class.java)
        startActivity(intent)
        (activity as MainActivity).finish()
    }
    @OnClick(R.id.bt_recognition)
    fun executeRecognition(){
        val intent = Intent(context, RecognitionActivity::class.java)
        startActivity(intent)
        (activity as MainActivity).finish()
    }
    /*fun executeDetect() =  runBlocking{

        val request = launch {
            if (bitmap != null){

                bitmap = faceRecognition.detectFace(bitmap!!)
                msgImage = if (bitmap != null){
                    "OK"
                }else{
                    "NOK"
                }
                observableImage.onNext(msgImage)
            }
        }

        request.join()
        if (bitmap != null){
            //digitalWrite()
            activity.runOnUiThread({
                flagAccess = true
                goLock(this@FaceFragment)
            })
        }

    }
*/

    private var disposable: CompositeDisposable = CompositeDisposable()
    private var bitmap: Bitmap? = null
    private var msgImage: String = ""
    private var observableImage: Subject<String> = PublishSubject.create()
    //val handler = Handler(Looper.getMainLooper())
    //handler.post { pbDownload!!.visibility = View.VISIBLE }

    init {
        observableImage
                .subscribe { msgImage }
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(com.hiddenodds.iotdomo.R.layout.view_face,
                container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view!!)
    }

    override fun onStart() {
        super.onStart()

        val msgImage = observableImage.map { m -> m }
        disposable.add(msgImage.observeOn(AndroidSchedulers.mainThread())
                .subscribe { m ->
                    kotlin.run {
                        pbDownload!!.visibility = View.INVISIBLE
                        if (m == "OK"){
                            val bMap = Bitmap.createScaledBitmap(this.bitmap,
                                    (this.bitmap!!.width*0.2).toInt(),
                                    (this.bitmap!!.height*0.2).toInt(), true)
                            ivImage!!.setImageBitmap(bMap)
                            /*if (faceRecognition.smile > 0.6f){
                                context.toast("You smile is: " + faceRecognition.smile.toString() +
                                        " Thank very much.")

                            }else{
                                context.toast("Smile please.")
                            }*/
                        }else{
                            context.toast("Face not exists.")
                        }

                    }
                })

        /*val message = hearMessage()
        disposable.add(message.observeOn(AndroidSchedulers.mainThread())
                .subscribe { e ->
                    tvDetails!!.text = e
                    if (!e.isEmpty()){
                        btDetect!!.isEnabled = true
                    }
                    pbDownload!!.visibility = View.INVISIBLE
                })*/

        val image = (activity as MainActivity).observableImage.map { i -> i }
        disposable.add(image.observeOn(Schedulers.newThread())
                .map { i ->
                    kotlin.run {
                        val img: Bitmap = (i.drawable as BitmapDrawable).bitmap
                        this.bitmap = img
                        return@map Bitmap.createScaledBitmap(img, (img.width*0.2).toInt(),
                                (img.height*0.2).toInt(), true)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { resize ->
                    kotlin.run {
                        ivImage!!.setImageBitmap(resize)
                        pbDownload!!.visibility = View.VISIBLE
                        //tvDetails!!.text = getString(R.string.upload_image)

                    }
                })
        //btDetect!!.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).enabledMenuItemImage(true)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).enabledMenuItemImage(false)
    }

    /*private fun hearMessage(): Observable<String> {
        return (activity as MainActivity).getResultTensor()
    }*/

    private fun Context.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}