package common

import com.google.gson.GsonBuilder
import io.ktor.application.Application
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.restassured.RestAssured
import io.restassured.mapper.ObjectMapperType
import io.restassured.response.ResponseBodyExtractionOptions
import io.restassured.specification.RequestSpecification
import module
import org.junit.jupiter.api.BeforeAll
import repository.exposedTypeAdapters
import java.util.concurrent.TimeUnit

open class ServerTest {

    protected fun RequestSpecification.When(): RequestSpecification {
        return this.`when`()
    }

    protected val gson = GsonBuilder().apply(exposedTypeAdapters()).create()

    protected inline fun <reified T> ResponseBodyExtractionOptions.fromJSON(): T {
        return gson.fromJson(this.jsonPath().prettify(), T::class.java)
    }

    protected inline fun <reified T> ResponseBodyExtractionOptions.to(): T {
        return  this.`as`(T::class.java, ObjectMapperType.GSON)
    }

    companion object {

        private var serverStarted = false

        private lateinit var server: ApplicationEngine

        @BeforeAll
        @JvmStatic
        fun startServer() {
            if(!serverStarted) {
                server = embeddedServer(Netty, 8081, module = Application::module)
                server.start()
                serverStarted = true
                RestAssured.baseURI = "http://localhost"
                RestAssured.port = 8081
                Runtime.getRuntime().addShutdownHook(Thread { server.stop(0, 0, TimeUnit.SECONDS) })
            }
        }
    }

}
