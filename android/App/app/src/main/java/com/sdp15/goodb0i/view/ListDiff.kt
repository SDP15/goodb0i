package com.sdp15.goodb0i.view

sealed class ListDiff<T>(val items: List<T>) {
    class All<T>(items: List<T>) : ListDiff<T>(items)
    class Add<T>(items: List<T>, val item: T) : ListDiff<T>(items)
    class Remove<T>(items: List<T>, val item: T) : ListDiff<T>(items)
    class Update<T>(items: List<T>, val item: T) : ListDiff<T>(items)
}