package com.sdp15.goodb0i.data.bluetooth

import com.sdp15.goodb0i.view.connection.BluetoothService

class ServiceRobotController : RobotController {

    private var service: BluetoothService? = null

    override fun forward() = write(RobotConstants.forward)

    private fun write(command: String, vararg args: String) {
        if (args.isEmpty()) {
            service?.write(command)
        } else {
            service?.write(command, args.joinToString("/"))
        }
    }
}