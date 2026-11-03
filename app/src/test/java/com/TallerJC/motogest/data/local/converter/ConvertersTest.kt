package com.TallerJC.motogest.data.local.converter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun testPhotosListSerializationAndDeserialization() {
        val originalPhotos = listOf(
            "/data/user/0/com.TallerJC.motogest/files/foto_frente.jpg",
            "/data/user/0/com.TallerJC.motogest/files/foto_lateral.jpg",
            "/data/user/0/com.TallerJC.motogest/files/foto_tablero.jpg"
        )
        val serialized = converters.fromStringList(originalPhotos)
        val deserialized = converters.toStringList(serialized)

        assertEquals(originalPhotos, deserialized)
        assertEquals(3, deserialized.size)
    }

    @Test
    fun testRolesListSerializationAndDeserialization() {
        val originalRoles = listOf("RECEPCIONISTA", "GERENTE")
        val serialized = converters.fromStringList(originalRoles)
        val deserialized = converters.toStringList(serialized)

        assertEquals(originalRoles, deserialized)
    }

    @Test
    fun testSingleItem() {
        val singleList = listOf("MECANICO")
        val serialized = converters.fromStringList(singleList)
        val deserialized = converters.toStringList(serialized)

        assertEquals(singleList, deserialized)
    }

    @Test
    fun testEmptyAndNullLists() {
        val emptySerialized = converters.fromStringList(emptyList())
        assertEquals("", emptySerialized)
        val emptyDeserialized = converters.toStringList(emptySerialized)
        assertTrue(emptyDeserialized.isEmpty())

        val nullSerialized = converters.fromStringList(null)
        assertEquals("", nullSerialized)
        val nullDeserialized = converters.toStringList(null)
        assertTrue(nullDeserialized.isEmpty())
    }
}
