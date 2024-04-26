package skywolf46.devain.apicall

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicBoolean

object APICallLib {
    private val isInitialized = AtomicBoolean(false)

    fun init(client: HttpClient = HttpClient(CIO)) {
        if (isInitialized.getAndSet(true)) {
            return
        }
        loadKoinModules(module {
            single<HttpClient> { client }
        })
        println("APICallLib initialized.")
    }
}