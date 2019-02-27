package com.sdp15.goodb0i.view

/**
 * Model for a change in the contents of a [List]
 * @param items The current state of the list
 */
sealed class ListDiff<T>(val items: List<T>) : List<T> by items {
    class All<T>(items: List<T>) : ListDiff<T>(items)
    class Add<T>(items: List<T>, val added: T) : ListDiff<T>(items)
    class Remove<T>(items: List<T>, val removed: T) : ListDiff<T>(items)
    class Update<T>(items: List<T>, val updated: T) : ListDiff<T>(items)
    class Move<T>(items: List<T>, val moved: T, val from: Int, val to: Int) : ListDiff<T>(items)

    // Convert a diff into All, to change the entire dataset
    fun toAll() = All(items)

}