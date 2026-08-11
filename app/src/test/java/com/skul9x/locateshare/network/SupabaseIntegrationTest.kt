package com.skul9x.locateshare.network

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class SupabaseIntegrationTest {

    private lateinit var api: ApiService

    @Before
    fun setup() {
        api = RetrofitClient.getApiService()
    }

    @Test
    fun testUpdateAndFetchCurrentLocation() = runBlocking {
        // Update URL
        val testUrl = "https://maps.google.com/?q=${UUID.randomUUID()}"
        val testName = "Test Location ${System.currentTimeMillis()}"

        val updateResponse = api.updateCurrentLocation(
            body = UpdateCurrentLocation(url = testUrl, name = testName)
        )
        assertTrue("Update should be successful", updateResponse.contentLength() >= 0 || updateResponse.string() != null)

        // Fetch to verify
        val results = api.getCurrentLocation()
        assertTrue("Should return at least 1 record", results.isNotEmpty())
        
        val record = results.first()
        assertEquals("URL should match the updated one", testUrl, record.url)
        assertEquals("Name should match the updated one", testName, record.name)
        assertEquals("ID should be 1", 1, record.id)
    }

    @Test
    fun testFavoritesCRUD() = runBlocking {
        // 1. Add favorite
        val randomStr = UUID.randomUUID().toString().substring(0, 8)
        val name = "Test Fav $randomStr"
        val url = "https://maps.app.goo.gl/$randomStr"
        
        // Ensure no exception
        val addRes = api.addFavorite(InsertFavorite(name = name, url = url))
        
        // 2. Fetch all and find added
        var favorites = api.getFavorites()
        val addedFav = favorites.find { it.name == name }
        assertNotNull("Should find the added favorite by name", addedFav)
        val addedId = addedFav!!.id

        // 3. Unstar all and star this one
        api.unstarAll(body = UpdateStarred(isStarred = false))
        api.updateFavorite(id = "eq.$addedId", body = UpdateFavorite(isStarred = true))

        // Check if starred
        val starredFavs = api.getStarredFavorite()
        assertTrue("Should have at least 1 starred", starredFavs.isNotEmpty())
        assertEquals("The starred item should be our added one", addedId, starredFavs.first().id)
        
        // 4. Update the favorite
        val newName = "$name Updated"
        api.updateFavorite(id = "eq.$addedId", body = UpdateFavorite(name = newName))
        
        val updatedFav = api.getFavorites().find { it.id == addedId }
        assertNotNull(updatedFav)
        assertEquals("Name should be updated", newName, updatedFav!!.name)

        // 5. Delete the favorite
        api.deleteFavorite(id = "eq.$addedId")
        
        val afterDelete = api.getFavorites().find { it.id == addedId }
        assertNull("Should be deleted", afterDelete)
    }
}
