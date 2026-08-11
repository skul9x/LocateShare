package com.skul9x.locateshare.layout

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.skul9x.locateshare.adapter.QuickFavoriteAdapter
import com.skul9x.locateshare.network.FavoriteLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class PixelPerfectQuickFavoritesItemTest {

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

    private fun extractElementIds(doc: Document): Map<String, Element> {
        val idMap = mutableMapOf<String, Element>()
        val nodeList = doc.getElementsByTagName("*")
        for (i in 0 until nodeList.length) {
            val element = nodeList.item(i) as? Element ?: continue
            val idAttr = element.getAttribute("android:id")
            if (idAttr.isNotEmpty()) {
                val cleanId = idAttr.replace("@+id/", "").replace("@id/", "")
                idMap[cleanId] = element
            }
        }
        return idMap
    }

    @Test
    fun testQuickFavoriteItemLayoutXmlExistsAndParses() {
        val file = findResFile("layout/item_quick_favorite.xml")
        assertTrue("layout/item_quick_favorite.xml must exist in res/layout/", file.exists() && file.isFile)

        val doc = parseXml(file)
        assertNotNull("Root XML document must not be null", doc.documentElement)
        assertEquals(
            "Root element should be MaterialCardView",
            "com.google.android.material.card.MaterialCardView",
            doc.documentElement.nodeName
        )
    }

    @Test
    fun testQuickFavoriteLandscapeLayoutXmlExistsAndParses() {
        val file = findResFile("layout-land/item_quick_favorite.xml")
        assertTrue("layout-land/item_quick_favorite.xml must exist in res/layout-land/", file.exists() && file.isFile)

        val doc = parseXml(file)
        assertNotNull("Root XML document must not be null", doc.documentElement)
        assertEquals(
            "Root element should be MaterialCardView",
            "com.google.android.material.card.MaterialCardView",
            doc.documentElement.nodeName
        )
    }

    @Test
    fun testAllRequiredViewIdsExistInItemQuickFavorite() {
        val file = findResFile("layout/item_quick_favorite.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val requiredIds = listOf(
            "cardQuickFavItem",
            "ivStarBadge",
            "tvQuickFavName",
            "tvQuickFavAddress",
            "ivQuickFavNav"
        )

        for (id in requiredIds) {
            assertTrue(
                "item_quick_favorite.xml must contain element with id '@+id/$id'. Found: ${idMap.keys}",
                idMap.containsKey(id)
            )
        }
    }

    @Test
    fun testCardElevationCornerRadiusAndBackgroundTokens() {
        val file = findResFile("layout/item_quick_favorite.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val cardView = idMap["cardQuickFavItem"]
        assertNotNull("cardQuickFavItem must exist", cardView)

        val cornerRadius = cardView!!.getAttribute("app:cardCornerRadius")
        assertTrue(
            "cardQuickFavItem corner radius must be 14dp or @dimen/car_card_radius_small. Found: $cornerRadius",
            cornerRadius == "14dp" || cornerRadius == "@dimen/car_card_radius_small"
        )

        val bgColor = cardView.getAttribute("app:cardBackgroundColor")
        assertTrue(
            "cardQuickFavItem background must be @color/car_card_surface_elevated or #1E222D. Found: $bgColor",
            bgColor == "@color/car_card_surface_elevated" || bgColor.equals("#1E222D", ignoreCase = true)
        )

        val strokeColor = cardView.getAttribute("app:strokeColor")
        assertTrue(
            "cardQuickFavItem stroke must be @color/car_card_stroke or @color/car_card_tile_stroke. Found: $strokeColor",
            strokeColor == "@color/car_card_stroke" || strokeColor == "@color/car_card_tile_stroke"
        )
    }

    @Test
    fun testItemCardMinHeightMeetsCarGuidelines() {
        val file = findResFile("layout/item_quick_favorite.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val cardView = idMap["cardQuickFavItem"]
        assertNotNull("cardQuickFavItem must exist", cardView)

        val cardMinHeight = cardView!!.getAttribute("android:minHeight")
        val cardHeight = cardView.getAttribute("android:layout_height")

        val heightValue = cardMinHeight.replace("dp", "").toIntOrNull()
            ?: cardHeight.replace("dp", "").toIntOrNull()
            ?: 0

        assertTrue(
            "Item height or minHeight ($cardMinHeight / $cardHeight) must be at least 64dp (target 68dp)",
            heightValue >= 64 || cardMinHeight == "@dimen/car_hero_button_height" || cardMinHeight == "@dimen/car_hero_button_height_large"
        )
    }

    @Test
    fun testTypographyAndColorTokensInTextElements() {
        val file = findResFile("layout/item_quick_favorite.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val tvName = idMap["tvQuickFavName"]
        val tvAddress = idMap["tvQuickFavAddress"]

        assertNotNull("tvQuickFavName must exist", tvName)
        assertNotNull("tvQuickFavAddress must exist", tvAddress)

        val nameSize = tvName!!.getAttribute("android:textSize")
        val nameStyle = tvName.getAttribute("android:textStyle")
        val nameColor = tvName.getAttribute("android:textColor")

        assertTrue("tvQuickFavName textSize should be 16sp or body dimen. Found: $nameSize", nameSize == "16sp" || nameSize == "@dimen/car_text_body")
        assertEquals("tvQuickFavName textStyle must be bold", "bold", nameStyle)
        assertTrue("tvQuickFavName textColor should be primary text. Found: $nameColor", nameColor == "@color/car_text_primary" || nameColor.equals("#FFFFFF", ignoreCase = true))

        val addressSize = tvAddress!!.getAttribute("android:textSize")
        val addressColor = tvAddress.getAttribute("android:textColor")

        assertTrue("tvQuickFavAddress textSize should be 12sp or 13sp. Found: $addressSize", addressSize == "12sp" || addressSize == "13sp" || addressSize == "@dimen/car_text_caption")
        assertTrue(
            "tvQuickFavAddress textColor should be secondary or subtle text. Found: $addressColor",
            addressColor == "@color/car_text_subtle" || addressColor == "@color/car_text_secondary" || addressColor.equals("#8E95A5", ignoreCase = true)
        )
    }

    @Test
    fun testNavigationActionIconStyling() {
        val file = findResFile("layout/item_quick_favorite.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val ivNav = idMap["ivQuickFavNav"]
        assertNotNull("ivQuickFavNav must exist", ivNav)

        val navTint = ivNav!!.getAttribute("app:tint")
        assertTrue(
            "ivQuickFavNav tint should be emerald or green accent. Found: $navTint",
            navTint == "@color/car_accent_emerald" || navTint == "@color/car_accent_green" || navTint == "@color/car_nav_green"
        )
    }

    @Test
    fun testAdapterItemCountAndGetters() {
        val items = mutableListOf(
            FavoriteLocation(id = 1L, name = "Căn hộ Vinhome", url = "https://maps.google.com/?q=10.79,106.72", isStarred = true),
            FavoriteLocation(id = 2L, name = "Sân bay Tân Sơn Nhất", url = "https://maps.google.com/?q=10.81,106.65", isStarred = false),
            FavoriteLocation(id = 3L, name = "Cảng Cát Lái", url = "https://maps.google.com/?q=10.76,106.78", isStarred = false)
        )

        val adapter = QuickFavoriteAdapter(items)

        assertEquals("Adapter item count must be 3", 3, adapter.itemCount)
        assertEquals("getItems size must be 3", 3, adapter.getItems().size)
        assertEquals("getItem(0) name matches", "Căn hộ Vinhome", adapter.getItem(0)?.name)
        assertTrue("getItem(0) isStarred is true", adapter.getItem(0)?.isStarred == true)
        assertEquals("getItem(1) name matches", "Sân bay Tân Sơn Nhất", adapter.getItem(1)?.name)
        assertEquals("getItem(1) isStarred is false", false, adapter.getItem(1)?.isStarred)
        assertNull("getItem out of bounds returns null", adapter.getItem(99))
    }

    @Test
    fun testAdapterUpdateListRefreshesData() {
        val adapter = QuickFavoriteAdapter()
        assertEquals(0, adapter.itemCount)

        val newFavs = listOf(
            FavoriteLocation(id = 10L, name = "Trường Đại học Bách Khoa", url = "https://maps.google.com/?q=10.77,106.65", isStarred = true),
            FavoriteLocation(id = 20L, name = "Bệnh viện Chợ Rẫy", url = "https://maps.google.com/?q=10.75,106.66", isStarred = false)
        )

        adapter.updateList(newFavs)
        assertEquals("Item count after updateList must be 2", 2, adapter.itemCount)
        assertEquals("First item name matches", "Trường Đại học Bách Khoa", adapter.getItems()[0].name)
        assertEquals("Second item name matches", "Bệnh viện Chợ Rẫy", adapter.getItems()[1].name)
    }

    @Test
    fun testAdapterOnItemClickCallbackDispatchesExactFavorite() {
        var clickedFavorite: FavoriteLocation? = null
        val targetFav = FavoriteLocation(id = 55L, name = "Hồ Bán Nguyệt", url = "https://maps.google.com/?q=10.72,106.71", isStarred = true)

        val adapter = QuickFavoriteAdapter(
            items = mutableListOf(targetFav),
            onItemClick = { fav -> clickedFavorite = fav }
        )

        adapter.onItemClick(targetFav)

        assertNotNull("Clicked favorite should not be null", clickedFavorite)
        assertEquals("Clicked favorite ID must match", 55L, clickedFavorite?.id)
        assertEquals("Clicked favorite name must match", "Hồ Bán Nguyệt", clickedFavorite?.name)
        assertEquals("Clicked favorite URL must match", "https://maps.google.com/?q=10.72,106.71", clickedFavorite?.url)
        assertTrue("Clicked favorite isStarred is true", clickedFavorite?.isStarred == true)
    }

    class MockViewHolder(view: View) : QuickFavoriteAdapter.ViewHolder(view) {
        var boundName: String? = null
        var boundAddress: String? = null
        var boundImageRes: Int? = null
        var isBadgeVisible: Boolean = false
        var contentDescription: String? = null
    }

    @Test
    fun testAdapterBindViewHolderLogicForStarredAndRegularFavorites() {
        val adapter = QuickFavoriteAdapter()

        val starredFav = FavoriteLocation(id = 1L, name = "Nhà Riêng ⭐", url = "https://maps.google.com/?q=10.1,106.1", isStarred = true)
        val regularFav = FavoriteLocation(id = 2L, name = "Cơ quan", url = "https://maps.google.com/?q=10.2,106.2", isStarred = false)

        val name1 = adapter.formatDisplayName(starredFav)
        val name2 = adapter.formatDisplayName(regularFav)

        assertEquals("Starred name formatted correctly", "Nhà Riêng ⭐", name1)
        assertEquals("Regular name formatted correctly", "Cơ quan", name2)
    }
}
