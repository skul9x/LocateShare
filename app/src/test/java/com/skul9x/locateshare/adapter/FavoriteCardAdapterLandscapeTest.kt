package com.skul9x.locateshare.adapter

import com.skul9x.locateshare.network.FavoriteLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteCardAdapterLandscapeTest {

    @Test
    fun testAdapterWithFourFavoritesAndStarredBadge() {
        val mockFavorites = listOf(
            FavoriteLocation(id = 1L, name = "Nhà riêng", url = "https://maps.google.com/?q=10.1,106.1", isStarred = true),
            FavoriteLocation(id = 2L, name = "Công ty", url = "https://maps.google.com/?q=10.2,106.2", isStarred = false),
            FavoriteLocation(id = 3L, name = "Sân bay", url = "https://maps.google.com/?q=10.3,106.3", isStarred = false),
            FavoriteLocation(id = 4L, name = "Bệnh viện", url = "https://maps.google.com/?q=10.4,106.4", isStarred = false)
        )

        val adapter = FavoriteCardAdapter(mockFavorites.toMutableList())

        // 1. Assert dataset size
        assertEquals("Adapter item count must be 4", 4, adapter.itemCount)

        // 2. Assert item 0 is starred with ⭐ badge
        val item0 = adapter.getItem(0)
        assertNotNull("Item 0 should not be null", item0)
        assertTrue("Item 0 must have isStarred = true", item0!!.isStarred)
        assertEquals("Item 0 formatted title must have ⭐ badge", "⭐ Nhà riêng", adapter.formatDisplayName(item0))

        // 3. Assert unstarred items retain clean names
        val item1 = adapter.getItem(1)
        assertNotNull(item1)
        assertEquals("Item 1 is unstarred", false, item1!!.isStarred)
        assertEquals("Item 1 formatted title should not have star", "Công ty", adapter.formatDisplayName(item1))

        val item2 = adapter.getItem(2)
        assertNotNull(item2)
        assertEquals("Item 2 is unstarred", false, item2!!.isStarred)
        assertEquals("Item 2 formatted title should not have star", "Sân bay", adapter.formatDisplayName(item2))

        val item3 = adapter.getItem(3)
        assertNotNull(item3)
        assertEquals("Item 3 is unstarred", false, item3!!.isStarred)
        assertEquals("Item 3 formatted title should not have star", "Bệnh viện", adapter.formatDisplayName(item3))
    }

    @Test
    fun testActionButtonClickDispatchesTargetUrl() {
        val mockFavorites = listOf(
            FavoriteLocation(id = 1L, name = "Nhà riêng", url = "https://maps.google.com/?q=10.1,106.1", isStarred = true),
            FavoriteLocation(id = 2L, name = "Công ty", url = "https://maps.google.com/?q=10.2,106.2", isStarred = false),
            FavoriteLocation(id = 3L, name = "Sân bay", url = "https://maps.google.com/?q=10.3,106.3", isStarred = false),
            FavoriteLocation(id = 4L, name = "Bệnh viện", url = "https://maps.google.com/?q=10.4,106.4", isStarred = false)
        )

        var dispatchedUrl: String? = null
        var clickedLocation: FavoriteLocation? = null

        val adapter = FavoriteCardAdapter(
            items = mockFavorites.toMutableList(),
            onItemClick = { location ->
                clickedLocation = location
            },
            onOpenMapClick = { location ->
                dispatchedUrl = location.url
            }
        )

        // Simulate action button click on item 2 (Sân bay)
        val targetItem = mockFavorites[2]
        adapter.onOpenMapClick(targetItem)

        assertEquals("Dispatched URL must match item 2 url", "https://maps.google.com/?q=10.3,106.3", dispatchedUrl)

        // Simulate whole card click on item 0 (Nhà riêng)
        adapter.onItemClick(mockFavorites[0])
        assertEquals("Clicked location ID must match item 0", 1L, clickedLocation?.id)
        assertEquals("Clicked location URL must match item 0", "https://maps.google.com/?q=10.1,106.1", clickedLocation?.url)
    }

    @Test
    fun testUpdateListAndGetItems() {
        val adapter = FavoriteCardAdapter()
        assertEquals(0, adapter.itemCount)

        val newFavorites = listOf(
            FavoriteLocation(id = 10L, name = "Điểm A", url = "https://maps.google.com/?q=1.0,2.0", isStarred = false),
            FavoriteLocation(id = 20L, name = "Điểm B", url = "https://maps.google.com/?q=3.0,4.0", isStarred = true)
        )

        adapter.updateList(newFavorites)
        assertEquals(2, adapter.itemCount)
        assertEquals("Điểm A", adapter.getItem(0)?.name)
        assertEquals("⭐ Điểm B", adapter.formatDisplayName(adapter.getItem(1)!!))
    }
}
