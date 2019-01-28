package com.sdp15.goodb0i.view.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.camerakit.CameraKitView
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.otaliastudios.cameraview.Audio
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.Gesture
import com.otaliastudios.cameraview.GestureAction
import com.sdp15.goodb0i.R
import kotlinx.android.synthetic.main.layout_scanner.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber




class ScannerFragment : Fragment() {

    private val vm: ScannerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindViewModel()
    }

    private fun bindViewModel() {
        vm.actions.observe(this, Observer {
            captureImage()
        })
    }

    private fun captureImage() {
        Timber.i("Capturing image")
        camera_view.captureSnapshot()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_scanner, container, false)
    }

    override fun onStart() {
        super.onStart()
        camera_view.addCameraListener(object: CameraListener() {

            override fun onPictureTaken(jpeg: ByteArray?) {
                super.onPictureTaken(jpeg)
                Timber.i("Taken")
                val bitmap = jpeg?.size?.let { BitmapFactory.decodeByteArray(jpeg, 0, it) }
                bitmap?.let {
                    vm.onImageCaptured(bitmap)
                }
            }
        })
        camera_view.audio = Audio.OFF
        camera_view.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER)
        camera_view.mapGesture(Gesture.LONG_TAP, GestureAction.CAPTURE)
    }

    override fun onResume() {
        super.onResume()
        camera_view.setLifecycleOwner(this)
        captureImage()
    }


    interface ScannerFragmentInteractor {

        fun onImageCaptured(image: Bitmap)

    }

}