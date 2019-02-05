package com.sdp15.goodb0i.data.bluetooth

import android.os.Handler
import android.os.Message
import java.lang.ref.WeakReference

class SafeHandler(messageHandler: MessageHandler) : Handler() {
    private val messageHandler = WeakReference(messageHandler)

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        messageHandler.get()?.handleMessage(msg)
    }

    interface MessageHandler {

        fun handleMessage(msg: Message)

    }

    //TODO: Will this be needed?
    class MergedMessageHandler(vararg initial: MessageHandler) : MessageHandler {

        private val handlers = mutableSetOf<WeakReference<MessageHandler>>()

        init {
            handlers.addAll(initial.map { WeakReference(it) })
        }

        fun addHandler(handler: MessageHandler) = handlers.add(WeakReference(handler))

        fun removeHandler(handler: MessageHandler) = handlers.removeAll { it.get() == handler }

        override fun handleMessage(msg: Message) {
            handlers.forEach { it.get()?.handleMessage(msg) }
            handlers.removeAll { it.get() == null }
        }
    }

}