package service.routing

interface RouteFinder {

    fun plan(code: Long): String

}