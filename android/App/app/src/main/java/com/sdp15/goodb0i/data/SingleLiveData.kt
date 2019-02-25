package com.sdp15.goodb0i.data

/**
 * [MutableLiveData] implementation allowing each posted value to be read at most once
 */
class SingleLiveData<T> : CountedLiveData<T>(1)