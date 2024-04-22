package hr.algebra.domace.infrastructure.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.serializer
import java.io.InputStream

private fun java.util.Properties.toStringMap(): Map<String, String> =
    buildMap {
        for (name in stringPropertyNames())
            put(name, getProperty(name))
    }

@OptIn(ExperimentalSerializationApi::class)
fun <T> Properties.decodeFromStream(deserializer: DeserializationStrategy<T>, stream: InputStream): T =
    decodeFromStringMap(
        deserializer,
        java.util.Properties()
            .apply {
                load(stream)
            }
            .toStringMap()
    )

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Properties.decodeFromStream(stream: InputStream): T =
    decodeFromStream(serializersModule.serializer<T>(), stream)

@OptIn(ExperimentalSerializationApi::class)
fun <T> Properties.decodeFromResource(deserializer: DeserializationStrategy<T>, path: String): T =
    decodeFromStream(
        deserializer,
        this::class.java.classLoader.getResourceAsStream(path)!!
    )

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Properties.decodeFromResource(path: String): T =
    decodeFromResource(serializersModule.serializer<T>(), path)
