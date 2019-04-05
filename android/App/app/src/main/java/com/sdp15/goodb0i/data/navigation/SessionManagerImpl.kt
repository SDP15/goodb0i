package com.sdp15.goodb0i.data.navigation

import com.sdp15.goodb0i.data.navigation.sockets.SocketHandler
import com.sdp15.goodb0i.data.navigation.sockets.WebSocketShoppingSession
import com.sdp15.goodb0i.data.store.products.ProductLoader
import kotlin.reflect.KProperty

class SessionManagerImpl(
    private val productLoader: ProductLoader,
    private val socketHandler: SocketHandler<Message.IncomingMessage, Message.OutgoingMessage>
) : ShoppingSessionManager {

    private lateinit var session: ShoppingSession

    override fun startSession(): ShoppingSession {
        session = WebSocketShoppingSession(socketHandler, productLoader)
        return session
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): ShoppingSession {
        if (!::session.isInitialized) {
            throw IllegalStateException("startSession must be called before accessing session")
        }
        return session
    }

    override fun closeSession() {
        socketHandler.stop()
    }
}