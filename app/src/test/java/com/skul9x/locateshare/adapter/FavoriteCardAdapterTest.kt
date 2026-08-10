package com.skul9x.locateshare.adapter

import com.skul9x.locateshare.network.FavoriteLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteCardAdapterTest {

    @Test
    fun testItemCountMatchesInputList() {
        val testItems = mutableListOf(
            FavoriteLocation(id = 1L, name = "Nhà riêng", url = "https://maps.google.com/?q=10.1,106.1", isStarred = true),
            FavoriteLocation(id = 2L, name = "Công ty", url = "https://maps.google.com/?q=10.2,106.2", isStarred = false),
            FavoriteLocation(id = 3L, name = "Quê nhà", url = "https://maps.google.com/?q=10.3,106.3", isStarred = false)
        )

        val adapter = FavoriteCardAdapter(testItems)
        assertEquals("Initial itemCount should match testItems size", 3, adapter.itemCount)

        val updatedItems = listOf(
            FavoriteLocation(id = 1L, name = "Nhà riêng", url = "https://maps.google.com/?q=10.1,106.1", isStarred = true),
            FavoriteLocation(id = 4L, name = "Sân bay", url = "https://maps.google.com/?q=10.4,106.4", isStarred = false)
        )

        adapter.updateList(updatedItems)
        assertEquals("itemCount should be 2 after updateList", 2, adapter.itemCount)
        assertEquals("Updated list elements should match", updatedItems, adapter.getItems())
    }

    @Test
    fun testStarredFavoritePrependsStarBadge() {
        val adapter = FavoriteCardAdapter()

        val starredItem = FavoriteLocation(id = 1L, name = "Nhà riêng", url = "https://maps.google.com/?q=10.1,106.1", isStarred = true)
        val unstarredItem = FavoriteLocation(id = 2L, name = "Công ty", url = "https://maps.google.com/?q=10.2,106.2", isStarred = false)
        val alreadyStarredBadgeItem = FavoriteLocation(id = 3L, name = "⭐ Quê nhà", url = "https://maps.google.com/?q=10.3,106.3", isStarred = true)

        val starredTitle = adapter.formatDisplayName(starredItem)
        val unstarredTitle = adapter.formatDisplayName(unstarredItem)
        val alreadyBadgeTitle = adapter.formatDisplayName(alreadyStarredBadgeItem)

        assertEquals("Starred item should prepend star badge '⭐ '", "⭐ Nhà riêng", starredTitle)
        assertEquals("Unstarred item should retain plain name", "Công ty", unstarredTitle)
        assertEquals("Already starred item should not have duplicated star badge", "⭐ Quê nhà", alreadyBadgeTitle)
    }

    @Test
    fun testClickCallbacksDispatchCorrectPayload() {
        var clickedItem: FavoriteLocation? = null
        var openMapItem: FavoriteLocation? = null

        val targetItem = FavoriteLocation(id = 99L, name = "Quán Cafe", url = "https://maps.google.com/?q=10.9,106.9", isStarred = false)

        val adapter = FavoriteCardAdapter(
            items = mutableListOf(targetItem),
            onItemClick = { item -> clickedItem = item },
            onOpenMapClick = { item -> openMapItem = item }
        )

        // Invoke callbacks
        adapter.onItemClick(targetItem)
        adapter.onOpenMapClick(targetItem)

        assertNotNull("Clicked item should not be null", clickedItem)
        assertEquals("Clicked item ID should match", 99L, clickedItem?.id)
        assertEquals("Clicked item name should match", "Quán Cafe", clickedItem?.name)

        assertNotNull("Open map item should not be null", openMapItem)
        assertEquals("Open map item ID should match", 99L, openMapItem?.id)
        assertEquals("Open map item url should match", "https://maps.google.com/?q=10.9,106.9", openMapItem?.url)
    }

    @Test
    fun testEmptyListInitialState() {
        val adapter = FavoriteCardAdapter()
        assertEquals("Default constructor should initialize empty list", 0, adapter.itemCount)
        assertTrue("getItems should return empty list", adapter.getItems().isEmpty())
    }
}
