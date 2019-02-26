package com.sdp15.goodb0i.view.navigation.scanner

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.otaliastudios.cameraview.Audio
import com.otaliastudios.cameraview.Gesture
import com.otaliastudios.cameraview.GestureAction
import com.sdp15.goodb0i.R
import kotlinx.android.synthetic.main.layout_scanner.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class ScannerFragment : Fragment() {

    private val vm: ScannerViewModel by viewModel()
    private lateinit var mp: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindViewModel()
    }

    private fun bindViewModel() {
        vm.reading.observe(this, Observer {
            mp = MediaPlayer.create(this.requireContext(), R.raw.pop_up)
            mp.start ()
            Toast.makeText(context, "Reading: ${it.value}", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_scanner, container, false)
    }

    override fun onResume() {
        super.onResume()
        camera_view.setLifecycleOwner(this)
        camera_view.clearFrameProcessors() //TODO: Check if this is necessary
        camera_view.addFrameProcessor { frame ->
            vm.onImageCaptured(frame.data, frame.rotation, frame.size.width, frame.size.height)
        }

        camera_view.audio = Audio.OFF
        camera_view.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER)
    }


    interface ScannerFragmentInteractor {

        fun onImageCaptured(ba: ByteArray, rotation: Int, width: Int, height: Int)

    }

}