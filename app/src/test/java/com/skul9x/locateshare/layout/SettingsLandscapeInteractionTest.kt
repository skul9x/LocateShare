package com.skul9x.locateshare.layout

import com.skul9x.locateshare.network.ApiService
import com.skul9x.locateshare.network.CurrentLocation
import com.skul9x.locateshare.network.FavoriteLocation
import com.skul9x.locateshare.network.InsertFavorite
import com.skul9x.locateshare.network.UpdateCurrentLocation
import com.skul9x.locateshare.network.UpdateFavorite
import com.skul9x.locateshare.network.UpdateStarred
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsLandscapeInteractionTest {

    private class FakeApiService : ApiService {
        val database = mutableListOf<FavoriteLocation>()
        private var nextId = 1L

        fun seed(items: List<FavoriteLocation>) {
            database.clear()
            database.addAll(items)
            nextId = (items.maxOfOrNull { it.id } ?: 0L) + 1L
        }

        override suspend fun getCurrentLocation(select: String, id: String): List<CurrentLocation> {
            return emptyList()
        }

        override suspend fun updateCurrentLocation(id: String, body: UpdateCurrentLocation): ResponseBody {
            return "{}".toResponseBody("application/json".toMediaTypeOrNull())
        }

        override suspend fun getFavorites(select: String, order: String): List<FavoriteLocation> {
            return database.sortedWith(
                compareByDescending<FavoriteLocation> { it.isStarred }
                    .thenByDescending { it.id }
            )
        }

        override suspend fun getStarredFavorite(select: String, isStarred: String, limit: Int): List<FavoriteLocation> {
            return database.filter { it.isStarred }.take(limit)
        }

        override suspend fun addFavorite(body: InsertFavorite): ResponseBody {
            val item = FavoriteLocation(
                id = nextId++,
                name = body.name,
                url = body.url,
                isStarred = body.isStarred,
                createdAt = "2026-08-11T12:00:00Z"
            )
            database.add(item)
            return "{}".toResponseBody("application/json".toMediaTypeOrNull())
        }

        override suspend fun updateFavorite(id: String, body: UpdateFavorite): ResponseBody {
            val rawId = id.replace("eq.", "").toLongOrNull() ?: return "{}".toResponseBody(null)
            val index = database.indexOfFirst { it.id == rawId }
            if (index != -1) {
                val current = database[index]
                val updated = current.copy(
                    name = body.name ?: current.name,
                    url = body.url ?: current.url,
                    isStarred = body.isStarred ?: current.isStarred
                )
                database[index] = updated
            }
            return "{}".toResponseBody("application/json".toMediaTypeOrNull())
        }

        override suspend fun unstarAll(isStarred: String, body: UpdateStarred): ResponseBody {
            for (i in database.indices) {
                if (database[i].isStarred) {
                    database[i] = database[i].copy(isStarred = false)
                }
            }
            return "{}".toResponseBody("application/json".toMediaTypeOrNull())
        }

        override suspend fun deleteFavorite(id: String): ResponseBody {
            val rawId = id.replace("eq.", "").toLongOrNull()
            database.removeAll { it.id == rawId }
            return "{}".toResponseBody("application/json".toMediaTypeOrNull())
        }
    }

    private lateinit var fakeApi: FakeApiService

    @Before
    fun setUp() {
        fakeApi = FakeApiService()
    }

    @Test
    fun testAddFavoriteAppendsToDatabase() = runBlocking {
        assertEquals("Database should start empty", 0, fakeApi.database.size)

        fakeApi.addFavorite(InsertFavorite(name = "Nhà riêng", url = "https://maps.google.com/?q=21.0285,105.8542"))
        fakeApi.addFavorite(InsertFavorite(name = "Công ty", url = "https://maps.google.com/?q=21.0300,105.8500"))

        val favorites = fakeApi.getFavorites()
        assertEquals(2, favorites.size)
        assertEquals("Nhà riêng", favorites.find { it.name == "Nhà riêng" }?.name)
        assertEquals("Công ty", favorites.find { it.name == "Công ty" }?.name)
    }

    @Test
    fun testStarringItemEnforcesSingleStarredInvariant() = runBlocking {
        // Seed 3 items with Item 1 starred
        fakeApi.seed(
            listOf(
                FavoriteLocation(id = 1L, name = "Nhà riêng", url = "https://maps.google.com/?q=1", isStarred = true),
                FavoriteLocation(id = 2L, name = "Công ty", url = "https://maps.google.com/?q=2", isStarred = false),
                FavoriteLocation(id = 3L, name = "Sân bay", url = "https://maps.google.com/?q=3", isStarred = false)
            )
        )

        // Verify initial state: exactly 1 item starred (Item 1)
        var starredItems = fakeApi.getFavorites().filter { it.isStarred }
        assertEquals(1, starredItems.size)
        assertEquals(1L, starredItems[0].id)

        // User clicks star on Item 2 ("Công ty")
        // SettingsActivity logic: unstarAll first, then updateFavorite(isStarred = true)
        fakeApi.unstarAll(body = UpdateStarred(isStarred = false))
        fakeApi.updateFavorite(id = "eq.2", body = UpdateFavorite(isStarred = true))

        val updatedFavorites = fakeApi.getFavorites()
        val currentStarred = updatedFavorites.filter { it.isStarred }

        assertEquals("Single-starred invariant: exactly 1 item must be starred", 1, currentStarred.size)
        assertEquals("Newly starred item must be Item 2 (Công ty)", 2L, currentStarred[0].id)
        assertEquals("Công ty", currentStarred[0].name)

        // Item 1 must now be unstarred
        val item1 = updatedFavorites.find { it.id == 1L }
        assertNotNull(item1)
        assertFalse("Previous starred item must be unstarred", item1!!.isStarred)
    }

    @Test
    fun testUnstarringItemLeavesNoStarred() = runBlocking {
        fakeApi.seed(
            listOf(
                FavoriteLocation(id = 1L, name = "Nhà", url = "https://maps.google.com/?q=1", isStarred = true)
            )
        )

        // User unstars Item 1
        fakeApi.updateFavorite(id = "eq.1", body = UpdateFavorite(isStarred = false))

        val favorites = fakeApi.getFavorites()
        val starred = favorites.filter { it.isStarred }
        assertEquals("No items should be starred after unstarring", 0, starred.size)
    }

    @Test
    fun testUpdateFavoriteNameAndUrl() = runBlocking {
        fakeApi.seed(
            listOf(
                FavoriteLocation(id = 1L, name = "Nhà cũ", url = "https://maps.google.com/?q=old", isStarred = true)
            )
        )

        fakeApi.updateFavorite(
            id = "eq.1",
            body = UpdateFavorite(name = "Nhà mới", url = "https://maps.google.com/?q=new")
        )

        val item = fakeApi.getFavorites().first()
        assertEquals("Nhà mới", item.name)
        assertEquals("https://maps.google.com/?q=new", item.url)
        assertTrue("Star status should be preserved on edit", item.isStarred)
    }

    @Test
    fun testDeleteFavoriteRemovesFromDatabase() = runBlocking {
        fakeApi.seed(
            listOf(
                FavoriteLocation(id = 1L, name = "Nhà", url = "url1"),
                FavoriteLocation(id = 2L, name = "Công ty", url = "url2")
            )
        )

        fakeApi.deleteFavorite(id = "eq.1")

        val remaining = fakeApi.getFavorites()
        assertEquals(1, remaining.size)
        assertEquals(2L, remaining[0].id)
        assertNull("Item 1 should no longer exist", remaining.find { it.id == 1L })
    }

    @Test
    fun testStarredFavoriteSelectionQuery() = runBlocking {
        fakeApi.seed(
            listOf(
                FavoriteLocation(id = 1L, name = "Nhà", url = "url1", isStarred = false),
                FavoriteLocation(id = 2L, name = "Sân bay", url = "url2", isStarred = true)
            )
        )

        val starred = fakeApi.getStarredFavorite()
        assertEquals(1, starred.size)
        assertEquals("Sân bay", starred[0].name)
    }
}
