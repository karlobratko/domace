package hr.algebra.domace.infrastructure.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer

@OptIn(ExperimentalSerializationApi::class)
fun <T> Json.decodeFromResource(deserializer: DeserializationStrategy<T>, path: String): T =
    decodeFromStream(
        deserializer,
        this::class.java.classLoader.getResourceAsStream(path)!!
    )

inline fun <reified T> Json.decodeFromResource(path: String): T =
    decodeFromResource(serializersModule.serializer<T>(), path)
