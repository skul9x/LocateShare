package com.skul9x.locateshare.layout

import android.content.Intent
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.skul9x.locateshare.CarActivity
import com.skul9x.locateshare.adapter.QuickFavoriteAdapter
import com.skul9x.locateshare.network.ApiService
import com.skul9x.locateshare.network.CurrentLocation
import com.skul9x.locateshare.network.FavoriteLocation
import com.skul9x.locateshare.network.InsertFavorite
import com.skul9x.locateshare.network.UpdateCurrentLocation
import com.skul9x.locateshare.network.UpdateFavorite
import com.skul9x.locateshare.network.UpdateStarred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CarDashboardComponentTest {

    private val testDispatcher = StandardTestDispatcher()

    class FakeApiService : ApiService {
        var currentLocationList: List<CurrentLocation> = emptyList()
        var favoritesList: List<FavoriteLocation> = emptyList()
        var starredList: List<FavoriteLocation> = emptyList()
        var shouldThrowError: Boolean = false

        override suspend fun getCurrentLocation(select: String, id: String): List<CurrentLocation> {
            if (shouldThrowError) throw RuntimeException("Supabase fetch failed")
            return currentLocationList
        }

        override suspend fun updateCurrentLocation(id: String, body: UpdateCurrentLocation): ResponseBody = throw NotImplementedError()

        override suspend fun getFavorites(select: String, order: String): List<FavoriteLocation> {
            if (shouldThrowError) throw RuntimeException("Supabase fetch failed")
            return favoritesList
        }

        override suspend fun getStarredFavorite(select: String, isStarred: String, limit: Int): List<FavoriteLocation> {
            if (shouldThrowError) throw RuntimeException("Supabase fetch failed")
            return starredList
        }

        override suspend fun addFavorite(body: InsertFavorite): ResponseBody = throw NotImplementedError()
        override suspend fun updateFavorite(id: String, body: UpdateFavorite): ResponseBody = throw NotImplementedError()
        override suspend fun unstarAll(isStarred: String, body: UpdateStarred): ResponseBody = throw NotImplementedError()
        override suspend fun deleteFavorite(id: String): ResponseBody = throw NotImplementedError()
    }

    class TestableCarActivity : CarActivity() {
        val openedUrls = mutableListOf<String>()
        val startedIntents = mutableListOf<Intent>()

        override fun openMap(url: String) {
            openedUrls.add(url)
        }

        override fun startActivity(intent: Intent?) {
            if (intent != null) {
                startedIntents.add(intent)
            }
        }
    }

    private lateinit var testActivity: TestableCarActivity
    private lateinit var fakeApi: FakeApiService

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) {
                runnable.run()
            }

            override fun postToMainThread(runnable: Runnable) {
                runnable.run()
            }

            override fun isMainThread(): Boolean = true
        })

        testActivity = TestableCarActivity()
        testActivity.coroutineScope = CoroutineScope(testDispatcher)
        testActivity.ioDispatcher = testDispatcher
        fakeApi = FakeApiService()
        testActivity.api = fakeApi
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

    @Test
    fun testQuickFavoriteAdapterBindsDataCorrectly() {
        val fav1 = FavoriteLocation(id = 1L, name = "Nhà riêng", url = "https://maps.google.com/?q=10.1,106.1", isStarred = true)
        val fav2 = FavoriteLocation(id = 2L, name = "Công ty FPT", url = "https://maps.google.com/?q=10.2,106.2", isStarred = false)

        val clickedItems = mutableListOf<FavoriteLocation>()
        val adapter = QuickFavoriteAdapter(
            items = mutableListOf(fav1, fav2),
            onItemClick = { item -> clickedItems.add(item) }
        )

        assertEquals("Adapter item count must be 2", 2, adapter.itemCount)
        assertEquals("Items list should contain 2 elements", 2, adapter.getItems().size)
        assertEquals("Item 1 name matches", "Nhà riêng", adapter.formatDisplayName(adapter.getItems()[0]))
        assertEquals("Item 2 name matches", "Công ty FPT", adapter.formatDisplayName(adapter.getItems()[1]))

        // Test item click triggers callback
        adapter.onItemClick(fav1)
        assertEquals("Clicked items should have 1 entry", 1, clickedItems.size)
        assertEquals("Clicked URL matches item 1", "https://maps.google.com/?q=10.1,106.1", clickedItems[0].url)
    }

    @Test
    fun testQuickFavoriteAdapterUpdateList() {
        val adapter = QuickFavoriteAdapter(items = mutableListOf())
        assertEquals(0, adapter.itemCount)

        val newFavs = listOf(
            FavoriteLocation(id = 10L, name = "Sân bay", url = "https://maps.google.com/?q=10.8,106.6", isStarred = true),
            FavoriteLocation(id = 20L, name = "Bệnh viện", url = "https://maps.google.com/?q=10.7,106.7", isStarred = false)
        )

        adapter.updateList(newFavs)
        assertEquals(2, adapter.itemCount)
        assertEquals("Sân bay", adapter.getItems()[0].name)
    }

    @Test
    fun testQuickFavoriteItemClickTriggersDirectMapIntent() = runTest {
        val targetFav = FavoriteLocation(id = 100L, name = "Vincom Center", url = "https://maps.google.com/?q=10.77,106.70", isStarred = true)
        fakeApi.favoritesList = listOf(targetFav)

        val adapter = QuickFavoriteAdapter(
            items = mutableListOf(targetFav),
            onItemClick = { fav ->
                testActivity.openMap(fav.url)
            }
        )

        adapter.onItemClick(targetFav)

        assertEquals("openMap should be called 1 time", 1, testActivity.openedUrls.size)
        assertEquals("Map intent URL must match target favorite URL", "https://maps.google.com/?q=10.77,106.70", testActivity.openedUrls[0])
    }

    @Test
    fun testHeroOpenMapButtonDispatchesLocationUrl() = runTest {
        val sampleLocation = CurrentLocation(
            id = 1,
            url = "https://maps.app.goo.gl/SampleTarget123",
            name = "Trạm dừng nghỉ Long Thành",
            updatedAt = "2026-08-11T12:00:00Z"
        )
        fakeApi.currentLocationList = listOf(sampleLocation)

        // Trigger fetch
        testActivity.fetchCurrentLocation(isManualReload = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify currentUrl is populated
        assertEquals("https://maps.app.goo.gl/SampleTarget123", testActivity.currentUrl)

        // Call openMap with current url
        testActivity.openMap(testActivity.currentUrl)

        assertEquals(1, testActivity.openedUrls.size)
        assertEquals("https://maps.app.goo.gl/SampleTarget123", testActivity.openedUrls[0])
    }

    @Test
    fun testDriverRailStarredQuickButtonDirectNavigation() = runTest {
        val starredFav = FavoriteLocation(
            id = 77L,
            name = "Nhà Riêng ⭐",
            url = "https://maps.google.com/?q=10.75,106.68",
            isStarred = true
        )
        fakeApi.starredList = listOf(starredFav)

        testActivity.openStarredFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("openStarredFavorite must directly invoke openMap with starred URL", 1, testActivity.openedUrls.size)
        assertEquals("https://maps.google.com/?q=10.75,106.68", testActivity.openedUrls[0])
    }

    @Test
    fun testFetchQuickFavoritesPopulatesAdapter() = runTest {
        val sampleFavs = listOf(
            FavoriteLocation(id = 1L, name = "Cà phê Sáng", url = "https://maps.google.com/?q=10.1,106.1", isStarred = false),
            FavoriteLocation(id = 2L, name = "Trụ sở Công ty", url = "https://maps.google.com/?q=10.2,106.2", isStarred = true)
        )
        fakeApi.favoritesList = sampleFavs

        val adapter = QuickFavoriteAdapter()
        testActivity.quickFavoriteAdapter = adapter

        testActivity.fetchQuickFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, adapter.itemCount)
        assertEquals("Cà phê Sáng", adapter.getItems()[0].name)
        assertEquals("Trụ sở Công ty", adapter.getItems()[1].name)
    }
}
