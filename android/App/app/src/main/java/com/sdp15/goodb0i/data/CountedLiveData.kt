package com.sdp15.goodb0i.data

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicInteger

open class CountedLiveData<T>(val count: Int) : MutableLiveData<T>() {

    private var currentCount = AtomicInteger(count)

    init {
        require(count > 0) { "count must be greater than 0" }
    }


    override fun postValue(value: T?) {
        currentCount.set(count)
        super.postValue(value)
    }

    @MainThread
    override fun setValue(value: T?) {
        currentCount.set(count)
        super.setValue(value)
    }

    @MainThread
    override fun observeForever(observer: Observer<in T>) {
        super.observeForever(Observer { data ->
            if (data == null) return@Observer
            observer.onChanged(data)
            if (currentCount.decrementAndGet() == 0) {
                value = null
            }
        })
    }

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer { data ->
            if (data == null) return@Observer
            observer.onChanged(data)
            if (currentCount.decrementAndGet() == 0) {
                value = null
            }
        })
    }

}