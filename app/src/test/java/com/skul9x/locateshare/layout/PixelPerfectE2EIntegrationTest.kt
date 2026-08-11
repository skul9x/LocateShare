package com.skul9x.locateshare.layout

import com.skul9x.locateshare.adapter.FavoriteCardAdapter
import com.skul9x.locateshare.adapter.QuickFavoriteAdapter
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
 * Master Pixel-Perfect End-to-End (E2E) Integration Test Suite.
 * Validates the complete automotive UI system across:
 * 1. Landscape Layout Hierarchy & IDs (Car, Settings, Dialogs, Items)
 * 2. Visual Design System & Token Integrity (Colors, Dimens, Drawables)
 * 3. Driver Touch Safety Targets (72dp Hero, 88dp Rail, >= 56dp Tiles)
 * 4. Android Automotive Distraction Optimization & Manifest Declarations
 * 5. Adapter Data Contracts & View Binding Logic
 */
class PixelPerfectE2EIntegrationTest {

    private fun findResFile(subPath: String): File {
        val possiblePaths = listOf(
            subPath,
            "app/$subPath",
            "src/main/$subPath",
            "app/src/main/$subPath",
            "app/src/main/res/$subPath",
            "src/main/res/$subPath",
            "../app/src/main/res/$subPath",
            "../app/src/main/$subPath"
        )
        for (path in possiblePaths) {
            val file = File(path)
            if (file.exists()) {
                return file
            }
        }
        val workspaceDir = File(".").canonicalFile
        val candidate = File(workspaceDir, "app/src/main/res/$subPath")
        if (candidate.exists()) return candidate
        return File(workspaceDir, "app/src/main/$subPath")
    }

    private fun parseXml(file: File): Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()
        return builder.parse(file)
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

    private fun extractColors(doc: Document): Map<String, String> {
        val colors = mutableMapOf<String, String>()
        val colorNodes = doc.getElementsByTagName("color")
        for (i in 0 until colorNodes.length) {
            val node = colorNodes.item(i) as? Element ?: continue
            val name = node.getAttribute("name")
            val value = node.textContent.trim()
            if (name.isNotEmpty()) {
                colors[name] = value
            }
        }
        return colors
    }

    private fun extractDimens(doc: Document): Map<String, String> {
        val dimens = mutableMapOf<String, String>()
        val dimenNodes = doc.getElementsByTagName("dimen")
        for (i in 0 until dimenNodes.length) {
            val node = dimenNodes.item(i) as? Element ?: continue
            val name = node.getAttribute("name")
            val value = node.textContent.trim()
            if (name.isNotEmpty()) {
                dimens[name] = value
            }
        }
        return dimens
    }

    private fun parseNumericValue(valueStr: String): Double {
        val clean = valueStr.replace("dp", "").replace("sp", "").replace("px", "").trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    // =========================================================================
    // 1. ALL LANDSCAPE XML FILES EXIST & PARSE PROPERLY
    // =========================================================================

    @Test
    fun testAllLandscapeLayoutFilesExistAndParse() {
        val requiredLandscapeFiles = listOf(
            "layout-land/activity_car.xml",
            "layout-land/activity_settings.xml",
            "layout-land/dialog_favorites_card_popup.xml",
            "layout-land/item_quick_favorite.xml",
            "layout/item_quick_favorite.xml",
            "layout/item_favorite_card.xml"
        )

        for (relPath in requiredLandscapeFiles) {
            val file = findResFile(relPath)
            assertTrue("Layout file '$relPath' must exist", file.exists() && file.isFile)
            val doc = parseXml(file)
            assertNotNull("Document element of '$relPath' must not be null", doc.documentElement)
        }
    }

    // =========================================================================
    // 2. DASHBOARD VIEW HIERARCHY & IDS (CarActivity)
    // =========================================================================

    @Test
    fun testCarDashboardCompleteLayoutAndIds() {
        val file = findResFile("layout-land/activity_car.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val requiredDashboardIds = listOf(
            "rootCarLayout",
            "layout_driver_rail",
            "btnBack",
            "btnWifiSettings",
            "ivSyncStatus",
            "btnStarredQuick",
            "btnSettings",
            "cardCurrentLocation",
            "tvSyncHeader",
            "tvSyncTime",
            "tvLocationName",
            "tvLocation",
            "btnOpenMap",
            "btnReload",
            "cardQuickFavorites",
            "tvFavoritesTitle",
            "btnFavorites",
            "rvQuickFavorites"
        )

        for (reqId in requiredDashboardIds) {
            assertTrue("Dashboard layout must contain ID '$reqId'", idMap.containsKey(reqId))
        }

        // Verify Driver Rail specifications
        val driverRail = idMap["layout_driver_rail"]
        assertNotNull("Driver rail layout element must exist", driverRail)
        val railWidth = driverRail!!.getAttribute("android:layout_width")
        assertTrue(
            "Driver rail width must be 88dp or @dimen/car_rail_width. Found: $railWidth",
            railWidth == "88dp" || railWidth == "@dimen/car_rail_width"
        )

        // Verify Hero Card specifications
        val heroCard = idMap["cardCurrentLocation"]
        assertNotNull("Hero card must exist", heroCard)
        val heroCornerRadius = heroCard!!.getAttribute("app:cardCornerRadius")
        assertTrue(
            "Hero card corner radius must be 20dp or @dimen/car_card_radius_large. Found: $heroCornerRadius",
            heroCornerRadius == "20dp" || heroCornerRadius == "@dimen/car_card_radius_large"
        )

        // Verify Open Map Button 72dp
        val btnOpenMap = idMap["btnOpenMap"]
        assertNotNull("Open Map button must exist", btnOpenMap)
        val btnHeight = btnOpenMap!!.getAttribute("android:layout_height")
        val btnMinHeight = btnOpenMap.getAttribute("android:minHeight")
        assertTrue(
            "Open Map button height ($btnHeight / $btnMinHeight) must be 72dp",
            btnHeight == "72dp" || btnHeight == "@dimen/car_hero_button_height_large" ||
                    btnMinHeight == "72dp" || btnMinHeight == "@dimen/car_hero_button_height_large"
        )

        // Verify Reload Button 72dp
        val btnReload = idMap["btnReload"]
        assertNotNull("Reload button must exist", btnReload)
        val reloadWidth = btnReload!!.getAttribute("android:layout_width")
        val reloadHeight = btnReload.getAttribute("android:layout_height")
        assertTrue(
            "Reload button width/height ($reloadWidth / $reloadHeight) must be 72dp",
            (reloadWidth == "72dp" || reloadWidth == "@dimen/car_hero_button_reload_size") &&
                    (reloadHeight == "72dp" || reloadHeight == "@dimen/car_hero_button_height_large")
        )
    }

    // =========================================================================
    // 3. QUICK FAVORITES & MODAL POPUP INTEGRATION
    // =========================================================================

    @Test
    fun testQuickFavoritesItemAndPopupModalHierarchy() {
        val itemFile = findResFile("layout/item_quick_favorite.xml")
        val itemDoc = parseXml(itemFile)
        val itemIds = extractElementIds(itemDoc)

        val requiredItemIds = listOf(
            "cardQuickFavItem",
            "ivStarBadge",
            "tvQuickFavName",
            "tvQuickFavAddress",
            "ivQuickFavNav"
        )
        for (id in requiredItemIds) {
            assertTrue("item_quick_favorite.xml must contain ID '$id'", itemIds.containsKey(id))
        }

        val popupFile = findResFile("layout-land/dialog_favorites_card_popup.xml")
        val popupDoc = parseXml(popupFile)
        val popupIds = extractElementIds(popupDoc)

        val requiredPopupIds = listOf(
            "cardFavoritesDialogContainer",
            "tvPopupTitle",
            "btnClosePopup",
            "rvFavoritesPopup",
            "tvEmptyFavorites"
        )
        for (id in requiredPopupIds) {
            assertTrue("dialog_favorites_card_popup.xml must contain ID '$id'", popupIds.containsKey(id))
        }
    }

    // =========================================================================
    // 4. SPLIT-SCREEN SETTINGS SPECIFICATION & WEIGHTS
    // =========================================================================

    @Test
    fun testSplitScreenSettingsMasterDetailStructure() {
        val file = findResFile("layout-land/activity_settings.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val leftPane = idMap["layoutSettingsLeft"]
        val rightPane = idMap["layoutSettingsRight"]

        assertNotNull("Master pane layoutSettingsLeft must exist", leftPane)
        assertNotNull("Detail pane layoutSettingsRight must exist", rightPane)

        assertEquals("Master pane weight must be 35", "35", leftPane!!.getAttribute("android:layout_weight"))
        assertEquals("Detail pane weight must be 65", "65", rightPane!!.getAttribute("android:layout_weight"))

        val requiredSettingsIds = listOf(
            "btnBack",
            "cardConnectionStatus",
            "tvStatus",
            "ivStatusDot",
            "tvCloudUrl",
            "cardStarredLocation",
            "tvStarredName",
            "tvStarredUrl",
            "cardAppInfo",
            "tvVersionInfo",
            "tvFavoritesCount",
            "rvFavorites",
            "btnAddFavorite"
        )
        for (id in requiredSettingsIds) {
            assertTrue("Settings layout must contain ID '$id'", idMap.containsKey(id))
        }
    }

    // =========================================================================
    // 5. COLOR PALETTE & DESIGN SYSTEM TOKENS
    // =========================================================================

    @Test
    fun testVisualDesignTokensAndPaletteAccuracy() {
        val colorFile = findResFile("values/colors.xml")
        val colorDoc = parseXml(colorFile)
        val colors = extractColors(colorDoc)

        // Verify Obsidian Backgrounds
        assertEquals("#101217", colors["car_bg_obsidian"]?.uppercase())
        assertEquals("#141720", colors["car_bg_obsidian_subtle"]?.uppercase())

        // Verify Dark Card Surfaces
        assertEquals("#161922", colors["car_card_surface_dark"]?.uppercase())
        assertEquals("#1C202B", colors["car_card_tile"]?.uppercase())

        // Verify Emerald Green Accents
        assertEquals("#00D26A", colors["car_accent_emerald"]?.uppercase())
        assertEquals("#00E676", colors["car_accent_emerald_bright"]?.uppercase())
        assertEquals("#132E22", colors["car_accent_emerald_dark"]?.uppercase())

        // Verify Star Gold & Pin Blue Accents
        assertEquals("#FFD700", colors["car_starred_gold"]?.uppercase())
        assertEquals("#4F75FF", colors["car_accent_pin"]?.uppercase())

        // Verify Dimension Tokens in values-land/dimens.xml
        val dimensFile = findResFile("values-land/dimens.xml")
        val dimensDoc = parseXml(dimensFile)
        val dimens = extractDimens(dimensDoc)

        assertEquals(72.0, parseNumericValue(dimens["car_hero_button_height_large"] ?: "0"), 0.01)
        assertEquals(72.0, parseNumericValue(dimens["car_hero_button_reload_size"] ?: "0"), 0.01)
        assertEquals(20.0, parseNumericValue(dimens["car_card_radius_large"] ?: "0"), 0.01)
        assertEquals(16.0, parseNumericValue(dimens["car_card_radius_medium"] ?: "0"), 0.01)
        assertEquals(14.0, parseNumericValue(dimens["car_card_radius_small"] ?: "0"), 0.01)
        assertEquals(56.0, parseNumericValue(dimens["car_rail_tile_size"] ?: "0"), 0.01)
    }

    // =========================================================================
    // 6. CUSTOM DRAWABLES STRUCTURE
    // =========================================================================

    @Test
    fun testCustomDrawablesIntegrity() {
        val drawables = listOf(
            "drawable/bg_car_rail_tile.xml",
            "drawable/bg_car_pill_badge.xml",
            "drawable/bg_car_btn_navigation.xml",
            "drawable/bg_car_btn_reload.xml"
        )

        for (drawablePath in drawables) {
            val file = findResFile(drawablePath)
            assertTrue("Drawable '$drawablePath' must exist", file.exists() && file.isFile)
            val doc = parseXml(file)
            val rootTag = doc.documentElement.tagName
            assertTrue("Root tag of '$drawablePath' should be ripple or shape", rootTag == "ripple" || rootTag == "shape")
        }
    }

    // =========================================================================
    // 7. ANDROID MANIFEST AUTOMOTIVE COMPLIANCE
    // =========================================================================

    @Test
    fun testAndroidManifestDistractionOptimizedAndConfigChanges() {
        val fileToUse = findResFile("AndroidManifest.xml")
        assertTrue("AndroidManifest.xml must exist", fileToUse.exists())

        val doc = parseXml(fileToUse)
        val activities = doc.getElementsByTagName("activity")

        var carFound = false
        var carDistractionOptimized = false
        var settingsFound = false
        var settingsDistractionOptimized = false

        for (i in 0 until activities.length) {
            val act = activities.item(i) as? Element ?: continue
            val name = act.getAttributeNS("http://schemas.android.com/apk/res/android", "name")
                .ifEmpty { act.getAttribute("android:name") }

            val metaNodes = act.getElementsByTagName("meta-data")
            var hasDO = false
            for (m in 0 until metaNodes.length) {
                val meta = metaNodes.item(m) as? Element ?: continue
                val metaName = meta.getAttributeNS("http://schemas.android.com/apk/res/android", "name")
                    .ifEmpty { meta.getAttribute("android:name") }
                val metaVal = meta.getAttributeNS("http://schemas.android.com/apk/res/android", "value")
                    .ifEmpty { meta.getAttribute("android:value") }
                if (metaName == "distractionOptimized" && metaVal == "true") {
                    hasDO = true
                }
            }

            if (name == ".CarActivity" || name.endsWith(".CarActivity")) {
                carFound = true
                carDistractionOptimized = hasDO
            }

            if (name == ".SettingsActivity" || name.endsWith(".SettingsActivity")) {
                settingsFound = true
                settingsDistractionOptimized = hasDO
            }
        }

        assertTrue("CarActivity must be declared in manifest", carFound)
        assertTrue("CarActivity must have distractionOptimized='true'", carDistractionOptimized)
        assertTrue("SettingsActivity must be declared in manifest", settingsFound)
        assertTrue("SettingsActivity must have distractionOptimized='true'", settingsDistractionOptimized)
    }

    // =========================================================================
    // 8. ADAPTER DATA CONTRACTS & INTERACTION LOGIC
    // =========================================================================

    @Test
    fun testQuickFavoritesAdapterDataFlowAndCallbacks() {
        val fav1 = FavoriteLocation(id = 101L, name = "Điểm Đến Ưu Tiên ⭐", url = "https://maps.google.com/?q=10.7,106.6", isStarred = true)
        val fav2 = FavoriteLocation(id = 102L, name = "Trạm Xăng", url = "https://maps.google.com/?q=10.8,106.7", isStarred = false)

        var clickedFav: FavoriteLocation? = null
        val adapter = QuickFavoriteAdapter(
            items = mutableListOf(fav1, fav2),
            onItemClick = { clickedFav = it }
        )

        assertEquals(2, adapter.itemCount)
        assertEquals("Điểm Đến Ưu Tiên ⭐", adapter.getItem(0)?.name)
        assertEquals(true, adapter.getItem(0)?.isStarred)

        adapter.onItemClick(fav1)
        assertEquals(101L, clickedFav?.id)

        adapter.updateList(listOf(fav2))
        assertEquals(1, adapter.itemCount)
        assertEquals("Trạm Xăng", adapter.getItem(0)?.name)
    }

    @Test
    fun testFavoriteCardAdapterDataFlowAndCallbacks() {
        val fav = FavoriteLocation(id = 201L, name = "Trụ sở chính", url = "https://maps.google.com/?q=10.75,106.65", isStarred = true)

        var clickedCard: FavoriteLocation? = null
        var openMapCard: FavoriteLocation? = null

        val cardAdapter = FavoriteCardAdapter(
            items = mutableListOf(fav),
            onItemClick = { clickedCard = it },
            onOpenMapClick = { openMapCard = it }
        )

        assertEquals(1, cardAdapter.itemCount)
        assertEquals("⭐ Trụ sở chính", cardAdapter.formatDisplayName(fav))

        cardAdapter.onItemClick(fav)
        assertEquals(201L, clickedCard?.id)

        cardAdapter.onOpenMapClick(fav)
        assertEquals(201L, openMapCard?.id)
    }
}
