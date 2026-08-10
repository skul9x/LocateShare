package com.skul9x.locateshare

import android.app.Dialog
import android.content.Context
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.skul9x.locateshare.adapter.FavoriteCardAdapter
import com.skul9x.locateshare.network.ApiService
import com.skul9x.locateshare.network.CurrentLocation
import com.skul9x.locateshare.network.FavoriteLocation
import com.skul9x.locateshare.network.InsertFavorite
import com.skul9x.locateshare.network.UpdateCurrentLocation
import com.skul9x.locateshare.network.UpdateFavorite
import com.skul9x.locateshare.network.UpdateStarred
import com.skul9x.locateshare.util.DoubleTapHandler
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

@OptIn(ExperimentalCoroutinesApi::class)
class CarActivityFloatingCardPopupTest {

    private val testDispatcher = StandardTestDispatcher()

    class FakeApiService : ApiService {
        var favoritesList: List<FavoriteLocation> = emptyList()
        var starredList: List<FavoriteLocation> = emptyList()
        var shouldThrowError: Boolean = false

        override suspend fun getCurrentLocation(select: String, id: String): List<CurrentLocation> = emptyList()
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

    class FakeDialog(context: Context) : Dialog(context) {
        var isShowingState = false
        var isDismissedState = false

        override fun show() {
            isShowingState = true
            isDismissedState = false
        }

        override fun dismiss() {
            isShowingState = false
            isDismissedState = true
        }

        override fun isShowing(): Boolean = isShowingState
    }

    class TestableCarActivity : CarActivity() {
        val openedUrls = mutableListOf<String>()
        var createdFakeDialog: FakeDialog? = null
        var lastCreatedAdapter: FavoriteCardAdapter? = null

        override fun openMap(url: String) {
            openedUrls.add(url)
        }

        override fun createFavoritesDialog(favorites: List<FavoriteLocation>): Dialog {
            val dialog = FakeDialog(this)
            val adapter = FavoriteCardAdapter(
                items = favorites.toMutableList(),
                onItemClick = { fav ->
                    dialog.dismiss()
                    openMap(fav.url)
                },
                onOpenMapClick = { fav ->
                    dialog.dismiss()
                    openMap(fav.url)
                }
            )
            lastCreatedAdapter = adapter
            createdFakeDialog = dialog
            return dialog
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
    fun testShowFavoritesPopupCreatesAndDisplaysCustomDialog() = runTest {
        val sampleFavorites = listOf(
            FavoriteLocation(id = 1L, name = "Nhà riêng", url = "https://maps.google.com/?q=10.1,106.1", isStarred = true),
            FavoriteLocation(id = 2L, name = "Công ty", url = "https://maps.google.com/?q=10.2,106.2", isStarred = false)
        )
        fakeApi.favoritesList = sampleFavorites

        testActivity.showFavoritesPopup()
        testDispatcher.scheduler.advanceUntilIdle()

        val dialog = testActivity.createdFakeDialog
        assertNotNull("Custom dialog should be created", dialog)
        assertTrue("Dialog must be displayed", dialog!!.isShowing)
        assertEquals("Dialog instance must be stored in currentFavoritesDialog", dialog, testActivity.currentFavoritesDialog)

        val adapter = testActivity.lastCreatedAdapter
        assertNotNull("RecyclerView adapter should be initialized", adapter)
        assertEquals("Adapter item count must match favorites list size", 2, adapter!!.itemCount)
        assertEquals("First favorite item url must match", "https://maps.google.com/?q=10.1,106.1", adapter.getItems()[0].url)
        assertEquals("First favorite name formatted with star", "⭐ Nhà riêng", adapter.formatDisplayName(adapter.getItems()[0]))
    }

    @Test
    fun testWindowDimmingAndBlurConstants() {
        assertEquals("Window dim amount must be 0.85f (85% dimming)", 0.85f, CarActivity.POPUP_DIM_AMOUNT, 0.001f)
        assertEquals("Window blur behind radius must be 60px", 60, CarActivity.POPUP_BLUR_BEHIND_RADIUS)
        assertEquals("Popup max width in dp must be 640", 640, CarActivity.POPUP_MAX_WIDTH_DP)
    }

    @Test
    fun testCalculatePopupWidthAcrossScreenDensities() {
        // MDPI landscape headunit (1024x600, density = 1.0)
        // 85% of 1024 = 870px, max 640dp * 1.0 = 640px -> 640px
        val mdpiWidth = CarActivity.calculatePopupWidth(widthPixels = 1024, density = 1.0f)
        assertEquals("Landscape 1024px headunit width capped at 640px", 640, mdpiWidth)

        // HDPI portrait screen (480x800, density = 1.5)
        // 85% of 480 = 408px, max 640dp * 1.5 = 960px -> 408px
        val hdpiWidth = CarActivity.calculatePopupWidth(widthPixels = 480, density = 1.5f)
        assertEquals("Portrait 480px width calculated as 85% (408px)", 408, hdpiWidth)

        // XHDPI widescreen headunit (1920x720, density = 2.0)
        // 85% of 1920 = 1632px, max 640dp * 2.0 = 1280px -> 1280px
        val xhdpiWidth = CarActivity.calculatePopupWidth(widthPixels = 1920, density = 2.0f)
        assertEquals("Widescreen 1920px headunit capped at 1280px", 1280, xhdpiWidth)
    }

    @Test
    fun testItemClickDismissesDialogAndDispatchesOpenMap() = runTest {
        val targetFav = FavoriteLocation(id = 5L, name = "Sân bay TSN", url = "https://maps.google.com/?q=10.81,106.66", isStarred = false)
        fakeApi.favoritesList = listOf(targetFav)

        testActivity.showFavoritesPopup()
        testDispatcher.scheduler.advanceUntilIdle()

        val dialog = testActivity.createdFakeDialog
        val adapter = testActivity.lastCreatedAdapter
        assertNotNull(dialog)
        assertNotNull(adapter)
        assertTrue(dialog!!.isShowing)

        // Simulate item tap
        adapter!!.onItemClick(targetFav)

        assertTrue("Dialog should be dismissed after item click", dialog.isDismissedState)
        assertEquals("openMap should be called with target URL", 1, testActivity.openedUrls.size)
        assertEquals("Target URL must match clicked item", "https://maps.google.com/?q=10.81,106.66", testActivity.openedUrls[0])
    }

    @Test
    fun testOpenMapButtonClickDismissesDialogAndDispatchesOpenMap() = runTest {
        val targetFav = FavoriteLocation(id = 8L, name = "Quán Cafe", url = "https://maps.google.com/?q=10.77,106.69", isStarred = true)
        fakeApi.favoritesList = listOf(targetFav)

        testActivity.showFavoritesPopup()
        testDispatcher.scheduler.advanceUntilIdle()

        val dialog = testActivity.createdFakeDialog
        val adapter = testActivity.lastCreatedAdapter
        assertNotNull(dialog)
        assertNotNull(adapter)

        // Simulate "MỞ BẢN ĐỒ" button click
        adapter!!.onOpenMapClick(targetFav)

        assertTrue("Dialog should be dismissed after open map click", dialog!!.isDismissedState)
        assertEquals("openMap should be called once", 1, testActivity.openedUrls.size)
        assertEquals("Target URL must match", "https://maps.google.com/?q=10.77,106.69", testActivity.openedUrls[0])
    }

    @Test
    fun testEmptyFavoritesDoesNotCreateDialog() = runTest {
        fakeApi.favoritesList = emptyList()

        testActivity.showFavoritesPopup()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull("Dialog must not be created when favorites list is empty", testActivity.createdFakeDialog)
        assertNull("currentFavoritesDialog should remain null", testActivity.currentFavoritesDialog)
    }

    @Test
    fun testApiErrorHandledGracefullyWithoutCrashing() = runTest {
        fakeApi.shouldThrowError = true

        testActivity.showFavoritesPopup()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull("Dialog must not be created on API error", testActivity.createdFakeDialog)
        assertNull("currentFavoritesDialog should remain null on API error", testActivity.currentFavoritesDialog)
    }

    @Test
    fun testHandleDestroyDismissesActiveDialog() = runTest {
        val sampleFavorites = listOf(
            FavoriteLocation(id = 1L, name = "Nhà riêng", url = "https://maps.google.com/?q=10.1,106.1", isStarred = true)
        )
        fakeApi.favoritesList = sampleFavorites

        testActivity.showFavoritesPopup()
        testDispatcher.scheduler.advanceUntilIdle()

        val dialog = testActivity.createdFakeDialog
        assertNotNull(dialog)
        assertTrue(dialog!!.isShowing)

        testActivity.handleDestroy()

        assertTrue("Dialog must be dismissed on activity destruction", dialog.isDismissedState)
        assertNull("currentFavoritesDialog reference should be cleared", testActivity.currentFavoritesDialog)
    }

    @Test
    fun testDoubleTapHandlerIntegrationTriggersFloatingPopup() = runTest {
        val sampleFavorites = listOf(
            FavoriteLocation(id = 1L, name = "Địa điểm 1", url = "https://maps.google.com/?q=10.0,106.0", isStarred = false)
        )
        fakeApi.favoritesList = sampleFavorites

        var currentTime = 1000L
        val scheduler = object : DoubleTapHandler.Scheduler {
            val tasks = mutableListOf<Runnable>()
            override fun postDelayed(runnable: Runnable, delayMillis: Long): Boolean {
                tasks.add(runnable)
                return true
            }
            override fun removeCallbacks(runnable: Runnable) {
                tasks.remove(runnable)
            }
        }

        val handler = testActivity.initFavoritesDoubleTapHandler(
            scheduler = scheduler,
            timeProvider = { currentTime }
        )

        // First click
        handler.processClick(currentTime)
        // Second click within 300ms window
        currentTime += 150L
        handler.processClick(currentTime)

        testDispatcher.scheduler.advanceUntilIdle()

        val dialog = testActivity.createdFakeDialog
        assertNotNull("Double tap should trigger showFavoritesPopup and create dialog", dialog)
        assertTrue("Dialog must be displayed", dialog!!.isShowing)
    }

    @Test
    fun testFloatingDialogThemeXmlAttributes() {
        val themeFiles = listOf(
            findResFile("values/themes.xml"),
            findResFile("values-night/themes.xml")
        )

        for (file in themeFiles) {
            assertTrue("Theme file ${file.name} must exist", file.exists())
            val doc = parseXml(file)
            val root = doc.documentElement

            val styles = root.getElementsByTagName("style")
            var floatingDialogStyle: Element? = null
            for (i in 0 until styles.length) {
                val elem = styles.item(i) as Element
                if (elem.getAttribute("name") == "Theme.LocateShare.FloatingDialog") {
                    floatingDialogStyle = elem
                    break
                }
            }

            assertNotNull("Theme.LocateShare.FloatingDialog must be declared in ${file.name}", floatingDialogStyle)

            val items = floatingDialogStyle!!.getElementsByTagName("item")
            val itemMap = mutableMapOf<String, String>()
            for (i in 0 until items.length) {
                val itemElem = items.item(i) as Element
                itemMap[itemElem.getAttribute("name")] = itemElem.textContent.trim()
            }

            assertEquals(
                "android:windowBackground in ${file.name} must be transparent",
                "@android:color/transparent",
                itemMap["android:windowBackground"]
            )
            assertEquals(
                "android:windowIsFloating in ${file.name} must be true",
                "true",
                itemMap["android:windowIsFloating"]
            )
            assertEquals(
                "android:windowNoTitle in ${file.name} must be true",
                "true",
                itemMap["android:windowNoTitle"]
            )
        }
    }

    private fun findResFile(subPath: String): File {
        val possiblePaths = listOf(
            "app/src/main/res/$subPath",
            "src/main/res/$subPath",
            "../app/src/main/res/$subPath"
        )
        for (path in possiblePaths) {
            val file = File(path)
            if (file.exists()) {
                return file
            }
        }
        val workspaceDir = File(".").canonicalFile
        return File(workspaceDir, "app/src/main/res/$subPath")
    }

    private fun parseXml(file: File): Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()
        return builder.parse(file)
    }
}
