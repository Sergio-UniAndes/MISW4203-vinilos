package com.misw4203.vinilos.feature.home.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoteHomeRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: RemoteHomeRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = RemoteHomeRepository(baseUrl = server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun observeItems_returnsParsedAlbums_onSuccess() = runTest {
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

        val items = repository.observeItems().first()

        assertEquals(2, items.size)
        assertEquals("1", items[0].id)
        assertEquals("Buscando America", items[0].title)
        assertEquals("Ruben Blades", items[0].artist)
        assertEquals(1984, items[0].year)
        assertEquals(1, items[0].tracks.size)
        assertEquals("Decisiones", items[0].tracks[0].name)
        assertEquals("Hector Lavoe", items[1].artist)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/albums", request.path)
    }

    @Test
    fun observeItems_returnsEmpty_on500() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val items = repository.observeItems().first()

        assertTrue(items.isEmpty())
    }

    @Test
    fun observeItems_returnsEmpty_on404() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val items = repository.observeItems().first()

        assertTrue(items.isEmpty())
    }

    @Test
    fun observeItems_returnsEmpty_onMalformedJson() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("not-json"))

        val items = repository.observeItems().first()

        assertTrue(items.isEmpty())
    }

    @Test
    fun observeItems_returnsEmpty_onEmptyArray() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val items = repository.observeItems().first()

        assertTrue(items.isEmpty())
    }

    @Test
    fun observeItems_skipsNonObjectArrayEntries() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[ "garbage", { "id": 1, "name": "Valid", "genre": "Rock" }, 42 ]""",
            ),
        )

        val items = repository.observeItems().first()

        assertEquals(1, items.size)
        assertEquals("Valid", items[0].title)
    }

    @Test
    fun observeItem_returnsAlbumDetail_onSuccess() = runTest {
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

        val item = repository.observeItem("7").first()

        assertEquals("7", item?.id)
        assertEquals("Solo", item?.title)
        assertEquals("Lila Downs", item?.artist)
        assertEquals(1, item?.tracks?.size)

        val request = server.takeRequest()
        assertEquals("/albums/7", request.path)
    }

    @Test
    fun observeItem_returnsNull_on404() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val item = repository.observeItem("999").first()

        assertNull(item)
    }

    @Test
    fun observeItem_returnsNull_onMalformedJson() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{ broken"))

        val item = repository.observeItem("7").first()

        assertNull(item)
    }

    @Test
    fun observeItem_handlesAlternateIdField() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{ "new_id_a": 42, "name": "Alt Id Album", "genre": "Rock" }""",
            ),
        )

        val item = repository.observeItem("42").first()

        assertEquals("42", item?.id)
        assertEquals("Alt Id Album", item?.title)
    }
}
