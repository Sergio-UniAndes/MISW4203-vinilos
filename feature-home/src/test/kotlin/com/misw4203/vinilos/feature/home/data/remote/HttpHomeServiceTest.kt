package com.misw4203.vinilos.feature.home.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HttpHomeServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: HttpHomeService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        service = HttpHomeService(baseUrl = server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getAlbums_returnsParsedDtos_onSuccess() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    [
                      {
                        "id": 1,
                        "name": "Buscando America",
                        "artist": "Ruben Blades",
                        "genre": "Salsa",
                        "releaseDate": "1984-08-01",
                        "recordLabel": "Elektra",
                        "tracks": [{ "id": 10, "name": "Decisiones", "duration": "5:00" }],
                        "performers": [{ "id": 9, "name": "Ruben Blades" }],
                        "comments": [{ "id": 2, "description": "Great", "rating": 5 }]
                      },
                      {
                        "id": 2,
                        "name": "Poeta del Pueblo",
                        "artist": "Hector Lavoe",
                        "genre": "Salsa"
                      }
                    ]
                    """.trimIndent(),
                ),
        )

        val albums = service.getAlbums()

        assertEquals(2, albums.size)
        assertEquals(1L, albums[0].id)
        assertEquals("Buscando America", albums[0].name)
        assertEquals("Ruben Blades", albums[0].artist)
        assertEquals("Elektra", albums[0].recordLabel)
        assertEquals(1, albums[0].tracks.size)
        assertEquals("Decisiones", albums[0].tracks[0].name)
        assertEquals("Hector Lavoe", albums[1].artist)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/albums", request.path)
    }

    @Test
    fun getAlbums_returnsEmpty_on500() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        assertTrue(service.getAlbums().isEmpty())
    }

    @Test
    fun getAlbums_returnsEmpty_on404() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        assertTrue(service.getAlbums().isEmpty())
    }

    @Test
    fun getAlbums_returnsEmpty_onMalformedJson() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("not-json"))

        assertTrue(service.getAlbums().isEmpty())
    }

    @Test
    fun getAlbums_returnsEmpty_onEmptyArray() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        assertTrue(service.getAlbums().isEmpty())
    }

    @Test
    fun getAlbums_skipsNonObjectArrayEntries() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[ "garbage", { "id": 1, "name": "Valid", "genre": "Rock" }, 42 ]""",
            ),
        )

        val albums = service.getAlbums()

        assertEquals(1, albums.size)
        assertEquals("Valid", albums[0].name)
    }

    @Test
    fun getAlbum_returnsParsedDto_onSuccess() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {
                  "id": 7,
                  "name": "Solo",
                  "artist": "Lila Downs",
                  "genre": "Folk",
                  "releaseDate": "2010-03-01",
                  "tracks": [{ "id": 1, "name": "Track A", "duration": "3:30" }]
                }
                """.trimIndent(),
            ),
        )

        val album = service.getAlbum("7")

        assertEquals(7L, album?.id)
        assertEquals("Solo", album?.name)
        assertEquals("Lila Downs", album?.artist)
        assertEquals(1, album?.tracks?.size)

        val request = server.takeRequest()
        assertEquals("/albums/7", request.path)
    }

    @Test
    fun getAlbum_returnsNull_on404() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        assertNull(service.getAlbum("999"))
    }

    @Test
    fun getAlbum_returnsNull_onMalformedJson() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{ broken"))

        assertNull(service.getAlbum("7"))
    }

    @Test
    fun getAlbum_handlesAlternateIdField() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{ "new_id_a": 42, "name": "Alt Id Album", "genre": "Rock" }""",
            ),
        )

        val album = service.getAlbum("42")

        assertEquals(42L, album?.id)
        assertEquals("Alt Id Album", album?.name)
    }
}
