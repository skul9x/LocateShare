package com.skul9x.locateshare.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class CarLandscapeLayoutXmlTest {

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
    fun testCarLandscapeLayoutXmlExistsAndParses() {
        val file = findResFile("layout-land/activity_car.xml")
        assertTrue("layout-land/activity_car.xml must exist in res/", file.exists() && file.isFile)

        val doc = parseXml(file)
        assertNotNull("Root XML document must not be null", doc.documentElement)
    }

    @Test
    fun testRequiredLandscapeElementIdsExist() {
        val file = findResFile("layout-land/activity_car.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val requiredIds = listOf(
            "rootCarLayout",
            "layout_driver_rail",
            "btnBack",
            "btnWifiSettings",
            "ivSyncStatus",
            "btnStarredQuick",
            "btnSettings",
            "cardCurrentLocation",
            "tvLocationName",
            "tvLocation",
            "btnOpenMap",
            "btnReload",
            "cardQuickFavorites",
            "rvQuickFavorites",
            "btnFavorites"
        )

        for (id in requiredIds) {
            assertTrue("layout-land/activity_car.xml must contain element with id '@+id/$id'. Found: ${idMap.keys}", idMap.containsKey(id))
        }
    }

    @Test
    fun testHeroOpenMapButtonDimensionsAndStyling() {
        val file = findResFile("layout-land/activity_car.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val btnOpenMap = idMap["btnOpenMap"]
        assertNotNull("btnOpenMap must exist", btnOpenMap)

        val height = btnOpenMap!!.getAttribute("android:layout_height")
        val minHeight = btnOpenMap.getAttribute("android:minHeight")

        // Should reference @dimen/car_hero_button_height or be >= 60dp/68dp
        val isHeightValid = height == "@dimen/car_hero_button_height" ||
                height.replace("dp", "").toIntOrNull()?.let { it >= 60 } == true ||
                minHeight.replace("dp", "").toIntOrNull()?.let { it >= 60 } == true

        assertTrue(
            "btnOpenMap height ($height) or minHeight ($minHeight) must be >= 60dp or reference @dimen/car_hero_button_height",
            isHeightValid
        )

        val text = btnOpenMap.getAttribute("android:text")
        assertTrue("btnOpenMap text should be 'MỞ BẢN ĐỒ'", text.contains("MỞ BẢN ĐỒ"))
    }

    @Test
    fun testDriverRailDimensionsAndTouchTargets() {
        val file = findResFile("layout-land/activity_car.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val driverRail = idMap["layout_driver_rail"]
        assertNotNull("layout_driver_rail must exist", driverRail)

        val railWidth = driverRail!!.getAttribute("android:layout_width")
        assertTrue(
            "Driver rail width ($railWidth) must reference @dimen/car_rail_width or be ~88dp",
            railWidth == "@dimen/car_rail_width" || railWidth == "88dp"
        )

        val railButtons = listOf("btnBack", "btnWifiSettings", "btnStarredQuick", "btnSettings")
        for (btnId in railButtons) {
            val btn = idMap[btnId]
            assertNotNull("$btnId must exist in driver rail", btn)
            val btnWidth = btn!!.getAttribute("android:layout_width")
            val btnHeight = btn.getAttribute("android:layout_height")
            assertTrue(
                "$btnId width ($btnWidth) must be >= 48dp or @dimen/car_rail_icon_size / @dimen/car_rail_tile_size",
                btnWidth == "@dimen/car_rail_icon_size" || btnWidth == "@dimen/car_rail_tile_size" || btnWidth.replace("dp", "").toIntOrNull()?.let { it >= 48 } == true
            )
            assertTrue(
                "$btnId height ($btnHeight) must be >= 48dp or @dimen/car_rail_icon_size / @dimen/car_rail_tile_size",
                btnHeight == "@dimen/car_rail_icon_size" || btnHeight == "@dimen/car_rail_tile_size" || btnHeight.replace("dp", "").toIntOrNull()?.let { it >= 48 } == true
            )
        }
    }

    @Test
    fun testKeepScreenOnEnabledInLandscape() {
        val file = findResFile("layout-land/activity_car.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        val keepScreenOn = root.getAttribute("android:keepScreenOn")
        assertEquals("Root layout must have android:keepScreenOn='true' for car mode", "true", keepScreenOn)
    }

    @Test
    fun testQuickFavoriteItemLayoutXmlExistsAndValid() {
        val file = findResFile("layout/item_quick_favorite.xml")
        assertTrue("layout/item_quick_favorite.xml must exist in res/layout/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val requiredQuickFavIds = listOf(
            "cardQuickFavItem",
            "tvQuickFavName",
            "tvQuickFavAddress",
            "ivStarBadge"
        )

        for (id in requiredQuickFavIds) {
            assertTrue("item_quick_favorite.xml must contain id '@+id/$id'. Found: ${idMap.keys}", idMap.containsKey(id))
        }
    }
}
