package hr.algebra.domace.infrastructure.serialization

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.properties.Properties

object Resources {
    val lazy = LazyResources
    val async = AsyncResources

    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> properties(path: String) = Properties.decodeFromResource<T>(path)

    inline fun <reified T> json(path: String) = Json.decodeFromResource<T>(path)
}

object LazyResources {
    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> properties(path: String) = lazy { Properties.decodeFromResource<T>(path) }

    inline fun <reified T> json(path: String) = lazy { Json.decodeFromResource<T>(path) }
}

object AsyncResources {
    @OptIn(ExperimentalSerializationApi::class)
    suspend inline fun <reified T> properties(path: String, dispatcher: CoroutineDispatcher = Dispatchers.IO) =
        withContext(dispatcher) {
            Properties.decodeFromResource<T>(path)
        }

    suspend inline fun <reified T> json(path: String, dispatcher: CoroutineDispatcher = Dispatchers.IO) =
        withContext(dispatcher) {
            Json.decodeFromResource<T>(path)
        }
}
