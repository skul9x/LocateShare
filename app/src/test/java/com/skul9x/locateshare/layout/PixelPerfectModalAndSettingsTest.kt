package com.skul9x.locateshare.layout

import com.skul9x.locateshare.adapter.FavoriteCardAdapter
import com.skul9x.locateshare.network.FavoriteLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * File-based unit test suite for Phase 04: Modal & Settings Polish.
 * Validates:
 * 1. Floating Favorites Modal (dialog_favorites_card_popup.xml & item_favorite_card.xml)
 * 2. Split-Screen Settings Activity (layout-land/activity_settings.xml)
 * 3. Automotive touch target safety, IDs, tokens, and master-detail weights.
 */
class PixelPerfectModalAndSettingsTest {

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

    private fun collectAllIds(node: Node, ids: MutableSet<String>) {
        if (node is Element) {
            val idAttr = node.getAttributeNS("http://schemas.android.com/apk/res/android", "id")
            val idSimple = node.getAttribute("android:id")
            val idValue = if (idAttr.isNotEmpty()) idAttr else idSimple
            if (idValue.isNotEmpty()) {
                val cleanId = idValue.substringAfter("@+id/").substringAfter("@id/")
                ids.add(cleanId)
            }
        }
        val children: NodeList = node.childNodes
        for (i in 0 until children.length) {
            collectAllIds(children.item(i), ids)
        }
    }

    private fun findElementByTagOrId(node: Node, tagName: String, idName: String? = null): Element? {
        if (node is Element) {
            val matchesTag = node.tagName == tagName || node.tagName.endsWith(".$tagName") || node.tagName.endsWith(":$tagName")
            if (matchesTag) {
                if (idName == null) return node
                val idAttr = node.getAttributeNS("http://schemas.android.com/apk/res/android", "id")
                val idSimple = node.getAttribute("android:id")
                val idValue = if (idAttr.isNotEmpty()) idAttr else idSimple
                if (idValue.endsWith("/$idName")) return node
            }
        }
        val children = node.childNodes
        for (i in 0 until children.length) {
            val found = findElementByTagOrId(children.item(i), tagName, idName)
            if (found != null) return found
        }
        return null
    }

    // ==========================================
    // 1. FLOATING FAVORITES MODAL TESTS
    // ==========================================

    @Test
    fun testLandscapeFavoritesPopupModalXmlExistsAndParses() {
        val file = findResFile("layout-land/dialog_favorites_card_popup.xml")
        assertTrue("layout-land/dialog_favorites_card_popup.xml must exist in res/layout-land/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull("Root XML document must not be null", root)
        assertTrue(
            "Root element should be MaterialCardView, but was: ${root.tagName}",
            root.tagName.contains("CardView") || root.tagName.contains("MaterialCardView")
        )
    }

    @Test
    fun testPortraitFavoritesPopupModalXmlExistsAndParses() {
        val file = findResFile("layout/dialog_favorites_card_popup.xml")
        assertTrue("layout/dialog_favorites_card_popup.xml must exist in res/layout/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull("Root XML document must not be null", root)
        assertTrue(
            "Root element should be MaterialCardView, but was: ${root.tagName}",
            root.tagName.contains("CardView") || root.tagName.contains("MaterialCardView")
        )
    }

    @Test
    fun testLandscapeModalRequiredViewIds() {
        val file = findResFile("layout-land/dialog_favorites_card_popup.xml")
        val doc = parseXml(file)
        val ids = mutableSetOf<String>()
        collectAllIds(doc.documentElement, ids)

        val requiredIds = listOf(
            "cardFavoritesDialogContainer",
            "tvPopupTitle",
            "btnClosePopup",
            "tvEmptyFavorites",
            "rvFavoritesPopup"
        )

        for (id in requiredIds) {
            assertTrue("Landscape modal dialog must contain ID '$id'. Found: $ids", ids.contains(id))
        }
    }

    @Test
    fun testLandscapeModalStylingAndTokens() {
        val file = findResFile("layout-land/dialog_favorites_card_popup.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        val bgColor = root.getAttributeNS("http://schemas.android.com/apk/res-auto", "cardBackgroundColor")
            .ifEmpty { root.getAttribute("app:cardBackgroundColor") }
        assertEquals("Dialog card background must be '#1E1E1E'", "#1E1E1E", bgColor)

        val cornerRadius = root.getAttributeNS("http://schemas.android.com/apk/res-auto", "cardCornerRadius")
            .ifEmpty { root.getAttribute("app:cardCornerRadius") }
        assertEquals("Dialog corner radius must be '16dp'", "16dp", cornerRadius)

        val strokeColor = root.getAttributeNS("http://schemas.android.com/apk/res-auto", "strokeColor")
            .ifEmpty { root.getAttribute("app:strokeColor") }
        assertEquals("Dialog stroke color must be '#2E3240'", "#2E3240", strokeColor)

        val strokeWidth = root.getAttributeNS("http://schemas.android.com/apk/res-auto", "strokeWidth")
            .ifEmpty { root.getAttribute("app:strokeWidth") }
        assertEquals("Dialog stroke width must be '1.5dp'", "1.5dp", strokeWidth)
    }

    @Test
    fun testLandscapeModalCloseButtonTouchTarget() {
        val file = findResFile("layout-land/dialog_favorites_card_popup.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        val btnClose = findElementByTagOrId(root, "ImageButton", "btnClosePopup")
        assertNotNull("Close button 'btnClosePopup' must exist", btnClose)

        val width = btnClose?.getAttributeNS("http://schemas.android.com/apk/res/android", "layout_width")
            ?.ifEmpty { null } ?: btnClose?.getAttribute("android:layout_width")
        val height = btnClose?.getAttributeNS("http://schemas.android.com/apk/res/android", "layout_height")
            ?.ifEmpty { null } ?: btnClose?.getAttribute("android:layout_height")

        val widthDp = width?.replace("dp", "")?.toIntOrNull() ?: 0
        val heightDp = height?.replace("dp", "")?.toIntOrNull() ?: 0

        assertTrue("Close button width ($widthDp dp) must be >= 56dp for touch safety", widthDp >= 56)
        assertTrue("Close button height ($heightDp dp) must be >= 56dp for touch safety", heightDp >= 56)
    }

    @Test
    fun testLandscapeModalRecyclerViewScrollbarConfig() {
        val file = findResFile("layout-land/dialog_favorites_card_popup.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        val rv = findElementByTagOrId(root, "RecyclerView", "rvFavoritesPopup")
        assertNotNull("RecyclerView rvFavoritesPopup must exist", rv)

        val scrollbars = rv?.getAttributeNS("http://schemas.android.com/apk/res/android", "scrollbars")?.ifEmpty { null }
            ?: rv?.getAttribute("android:scrollbars")
        assertEquals("rvFavoritesPopup must have vertical scrollbars", "vertical", scrollbars)

        val fadeScrollbars = rv?.getAttributeNS("http://schemas.android.com/apk/res/android", "fadeScrollbars")?.ifEmpty { null }
            ?: rv?.getAttribute("android:fadeScrollbars")
        assertEquals("rvFavoritesPopup must have fadeScrollbars='false'", "false", fadeScrollbars)

        val scrollbarThumb = rv?.getAttributeNS("http://schemas.android.com/apk/res/android", "scrollbarThumbVertical")?.ifEmpty { null }
            ?: rv?.getAttribute("android:scrollbarThumbVertical")
        assertEquals("rvFavoritesPopup must use '@drawable/scrollbar_thumb_car'", "@drawable/scrollbar_thumb_car", scrollbarThumb)
    }

    // ==========================================
    // 2. ITEM FAVORITE CARD (POPUP GRID ITEM)
    // ==========================================

    @Test
    fun testItemFavoriteCardXmlExistsAndParses() {
        val file = findResFile("layout/item_favorite_card.xml")
        assertTrue("layout/item_favorite_card.xml must exist in res/layout/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull("Root XML element must exist", root)
        assertTrue(
            "Root element should be MaterialCardView, but was: ${root.tagName}",
            root.tagName.contains("CardView") || root.tagName.contains("MaterialCardView")
        )
    }

    @Test
    fun testItemFavoriteCardViewIdsAndDesignTokens() {
        val file = findResFile("layout/item_favorite_card.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        val ids = mutableSetOf<String>()
        collectAllIds(root, ids)

        val requiredIds = listOf("cardFavoriteItem", "tvCardName", "tvCardAddress", "btnCardOpenMap")
        for (id in requiredIds) {
            assertTrue("item_favorite_card.xml must contain ID '$id'. Found: $ids", ids.contains(id))
        }

        val bgColor = root.getAttributeNS("http://schemas.android.com/apk/res-auto", "cardBackgroundColor")
            .ifEmpty { root.getAttribute("app:cardBackgroundColor") }
        assertEquals("Card background color should be '#1E222D' (elevated dark surface)", "#1E222D", bgColor)

        val cornerRadius = root.getAttributeNS("http://schemas.android.com/apk/res-auto", "cardCornerRadius")
            .ifEmpty { root.getAttribute("app:cardCornerRadius") }
        assertEquals("Card corner radius must be '16dp'", "16dp", cornerRadius)

        val strokeColor = root.getAttributeNS("http://schemas.android.com/apk/res-auto", "strokeColor")
            .ifEmpty { root.getAttribute("app:strokeColor") }
        assertEquals("Card stroke color must be '#2E3240'", "#2E3240", strokeColor)

        val strokeWidth = root.getAttributeNS("http://schemas.android.com/apk/res-auto", "strokeWidth")
            .ifEmpty { root.getAttribute("app:strokeWidth") }
        assertEquals("Card stroke width must be '1.5dp'", "1.5dp", strokeWidth)
    }

    @Test
    fun testItemFavoriteCardOpenMapButtonSpecification() {
        val file = findResFile("layout/item_favorite_card.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        val btnOpenMap = findElementByTagOrId(root, "MaterialButton", "btnCardOpenMap")
            ?: findElementByTagOrId(root, "Button", "btnCardOpenMap")
        assertNotNull("btnCardOpenMap button must exist in item_favorite_card.xml", btnOpenMap)

        val btnText = btnOpenMap?.getAttributeNS("http://schemas.android.com/apk/res/android", "text")
            ?: btnOpenMap?.getAttribute("android:text")
        assertEquals("Action button text must be 'MỞ BẢN ĐỒ'", "MỞ BẢN ĐỒ", btnText)

        val height = btnOpenMap?.getAttributeNS("http://schemas.android.com/apk/res/android", "layout_height")
            ?.ifEmpty { null } ?: btnOpenMap?.getAttribute("android:layout_height")
        val minHeight = btnOpenMap?.getAttributeNS("http://schemas.android.com/apk/res/android", "minHeight")
            ?.ifEmpty { null } ?: btnOpenMap?.getAttribute("android:minHeight")

        val heightDp = height?.replace("dp", "")?.toIntOrNull() ?: 0
        val minHeightDp = minHeight?.replace("dp", "")?.toIntOrNull() ?: 0

        assertTrue("btnCardOpenMap height ($heightDp dp) or minHeight ($minHeightDp dp) must be >= 56dp", heightDp >= 56 || minHeightDp >= 56)
    }

    // ==========================================
    // 3. SPLIT-SCREEN SETTINGS ACTIVITY TESTS
    // ==========================================

    @Test
    fun testLandscapeSettingsLayoutXmlExistsAndParses() {
        val file = findResFile("layout-land/activity_settings.xml")
        assertTrue("layout-land/activity_settings.xml must exist in res/layout-land/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull("Root XML document must not be null", root)
        assertEquals("Root element should be LinearLayout", "LinearLayout", root.tagName)
    }

    @Test
    fun testLandscapeSettingsMasterDetail3565Weights() {
        val file = findResFile("layout-land/activity_settings.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val leftPane = idMap["layoutSettingsLeft"]
        val rightPane = idMap["layoutSettingsRight"]

        assertNotNull("layoutSettingsLeft (Master pane) must exist", leftPane)
        assertNotNull("layoutSettingsRight (Detail pane) must exist", rightPane)

        val leftWeight = leftPane!!.getAttribute("android:layout_weight")
        val rightWeight = rightPane!!.getAttribute("android:layout_weight")

        assertEquals("Left master pane should have layout_weight='35'", "35", leftWeight)
        assertEquals("Right detail pane should have layout_weight='65'", "65", rightWeight)
    }

    @Test
    fun testLandscapeSettingsRequiredViewIds() {
        val file = findResFile("layout-land/activity_settings.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val requiredIds = listOf(
            "layoutSettingsLeft",
            "layoutSettingsRight",
            "btnBack",
            "tvStatus",
            "ivStatusDot",
            "tvCloudUrl",
            "cardConnectionStatus",
            "cardStarredLocation",
            "tvStarredName",
            "tvStarredUrl",
            "cardAppInfo",
            "tvVersionInfo",
            "tvFavoritesCount",
            "rvFavorites",
            "btnAddFavorite"
        )

        for (id in requiredIds) {
            assertTrue(
                "layout-land/activity_settings.xml must contain element with id '@+id/$id'. Found: ${idMap.keys}",
                idMap.containsKey(id)
            )
        }
    }

    @Test
    fun testLandscapeSettingsActionButtonsTouchSafety() {
        val file = findResFile("layout-land/activity_settings.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val btnBack = idMap["btnBack"]
        assertNotNull("btnBack must exist", btnBack)
        val backHeight = btnBack?.getAttribute("android:layout_height")?.replace("dp", "")?.toIntOrNull()
            ?: btnBack?.getAttribute("android:minHeight")?.replace("dp", "")?.toIntOrNull() ?: 0
        assertTrue("btnBack height ($backHeight dp) must be >= 56dp", backHeight >= 56)

        val btnAddFavorite = idMap["btnAddFavorite"]
        assertNotNull("btnAddFavorite must exist", btnAddFavorite)
        val addHeight = btnAddFavorite?.getAttribute("android:layout_height")?.replace("dp", "")?.toIntOrNull()
            ?: btnAddFavorite?.getAttribute("android:minHeight")?.replace("dp", "")?.toIntOrNull() ?: 0
        assertTrue("btnAddFavorite height ($addHeight dp) must be >= 56dp", addHeight >= 56)
    }

    @Test
    fun testLandscapeSettingsAutomotiveFlags() {
        val file = findResFile("layout-land/activity_settings.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        val keepScreenOn = root.getAttribute("android:keepScreenOn")
        assertEquals("Root layout must have android:keepScreenOn='true'", "true", keepScreenOn)

        val fitsSystemWindows = root.getAttribute("android:fitsSystemWindows")
        assertEquals("Root layout must have android:fitsSystemWindows='true'", "true", fitsSystemWindows)
    }

    // ==========================================
    // 4. FAVORITE ADAPTER & CRUD CONTRACTS
    // ==========================================

    @Test
    fun testFavoriteCardAdapterContract() {
        val items = mutableListOf(
            FavoriteLocation(id = 1L, name = "Nhà riêng", url = "https://maps.google.com/?q=10.1,106.1", isStarred = true),
            FavoriteLocation(id = 2L, name = "Công ty", url = "https://maps.google.com/?q=10.2,106.2", isStarred = false)
        )

        var clickedItem: FavoriteLocation? = null
        var openMapItem: FavoriteLocation? = null

        val adapter = FavoriteCardAdapter(
            items = items,
            onItemClick = { clickedItem = it },
            onOpenMapClick = { openMapItem = it }
        )

        assertEquals(2, adapter.itemCount)
        assertEquals("⭐ Nhà riêng", adapter.formatDisplayName(items[0]))
        assertEquals("Công ty", adapter.formatDisplayName(items[1]))

        adapter.onItemClick(items[0])
        assertEquals(1L, clickedItem?.id)

        adapter.onOpenMapClick(items[1])
        assertEquals(2L, openMapItem?.id)
    }
}
