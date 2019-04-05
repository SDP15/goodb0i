package com.sdp15.goodb0i.data

interface ConfigProvider {

    val useTestData: Boolean

    val serverAddress: String

    val shouldSkipScanner: Boolean

}