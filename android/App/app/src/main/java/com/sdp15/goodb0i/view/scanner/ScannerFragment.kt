package com.sdp15.goodb0i.view.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.firebase.ml.vision.common.FirebaseVisionImage
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
        camera_view.capturePicture()
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

    }

    override fun onResume() {
        super.onResume()
        camera_view.setLifecycleOwner(this)
        camera_view.addFrameProcessor { frame ->
            //Timber.i("ByteAray ${frame.data.size}")
            vm.onImageCaptured(frame.data, frame.rotation, frame.size.width, frame.size.height)

        }

        camera_view.audio = Audio.OFF
        camera_view.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER)
    }


    interface ScannerFragmentInteractor {

        fun onImageCaptured(image: ByteArray, rotation: Int, width: Int, height: Int)

    }

}