package com.skul9x.locateshare.layout

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

class PixelPerfectCarDashboardLayoutTest {

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
    fun testAllRequiredCarViewIdsExist() {
        val file = findResFile("layout-land/activity_car.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val requiredCarIds = listOf(
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

        for (id in requiredCarIds) {
            assertTrue(
                "layout-land/activity_car.xml must contain element with id '@+id/$id'. Found: ${idMap.keys}",
                idMap.containsKey(id)
            )
        }
    }

    @Test
    fun testHeroCardSurfaceAndCornerRadius() {
        val file = findResFile("layout-land/activity_car.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val heroCard = idMap["cardCurrentLocation"]
        assertNotNull("cardCurrentLocation must exist", heroCard)

        val cornerRadius = heroCard!!.getAttribute("app:cardCornerRadius")
        assertTrue(
            "cardCurrentLocation corner radius must be @dimen/car_card_radius_large or 20dp. Found: $cornerRadius",
            cornerRadius == "@dimen/car_card_radius_large" || cornerRadius == "20dp"
        )

        val bgColor = heroCard.getAttribute("app:cardBackgroundColor")
        assertTrue(
            "cardCurrentLocation background must be @color/car_card_surface_dark or #161922. Found: $bgColor",
            bgColor == "@color/car_card_surface_dark" || bgColor.equals("#161922", ignoreCase = true)
        )

        val strokeColor = heroCard.getAttribute("app:strokeColor")
        assertTrue(
            "cardCurrentLocation stroke color must be @color/car_card_stroke_dark or #252938. Found: $strokeColor",
            strokeColor == "@color/car_card_stroke_dark" || strokeColor.equals("#252938", ignoreCase = true)
        )
    }

    @Test
    fun testHeroOpenMapButton72dpHeightAndStyling() {
        val file = findResFile("layout-land/activity_car.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val btnOpenMap = idMap["btnOpenMap"]
        assertNotNull("btnOpenMap must exist in hero card", btnOpenMap)

        val height = btnOpenMap!!.getAttribute("android:layout_height")
        val minHeight = btnOpenMap.getAttribute("android:minHeight")

        assertTrue(
            "btnOpenMap height ($height) must reference @dimen/car_hero_button_height_large or be 72dp",
            height == "@dimen/car_hero_button_height_large" || height == "72dp"
        )

        assertTrue(
            "btnOpenMap minHeight ($minHeight) must be at least 72dp or @dimen/car_hero_button_height_large",
            minHeight == "@dimen/car_hero_button_height_large" || minHeight.replace("dp", "").toIntOrNull()?.let { it >= 72 } == true
        )

        val text = btnOpenMap.getAttribute("android:text")
        assertTrue("btnOpenMap text should be 'MỞ BẢN ĐỒ'", text.contains("MỞ BẢN ĐỒ"))

        val bgTint = btnOpenMap.getAttribute("app:backgroundTint")
        assertTrue(
            "btnOpenMap backgroundTint should be emerald green token. Found: $bgTint",
            bgTint == "@color/car_accent_emerald_bright" || bgTint == "@color/car_accent_emerald" || bgTint == "@color/car_accent_green"
        )
    }

    @Test
    fun testHeroReloadButton72dpSquareDimensions() {
        val file = findResFile("layout-land/activity_car.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val btnReload = idMap["btnReload"]
        assertNotNull("btnReload must exist in hero card", btnReload)

        val width = btnReload!!.getAttribute("android:layout_width")
        val height = btnReload.getAttribute("android:layout_height")
        val minHeight = btnReload.getAttribute("android:minHeight")

        assertTrue(
            "btnReload width ($width) must be @dimen/car_hero_button_reload_size or 72dp",
            width == "@dimen/car_hero_button_reload_size" || width == "72dp"
        )

        assertTrue(
            "btnReload height ($height) must be @dimen/car_hero_button_height_large or 72dp",
            height == "@dimen/car_hero_button_height_large" || height == "72dp"
        )

        assertTrue(
            "btnReload minHeight ($minHeight) must be at least 72dp",
            minHeight == "@dimen/car_hero_button_height_large" || minHeight.replace("dp", "").toIntOrNull()?.let { it >= 72 } == true
        )
    }

    @Test
    fun testDriverRailWidthAndButtonTileDimensions() {
        val file = findResFile("layout-land/activity_car.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val driverRail = idMap["layout_driver_rail"]
        assertNotNull("layout_driver_rail must exist", driverRail)

        val railWidth = driverRail!!.getAttribute("android:layout_width")
        assertTrue(
            "Driver rail width ($railWidth) must reference @dimen/car_rail_width or be 88dp",
            railWidth == "@dimen/car_rail_width" || railWidth == "88dp"
        )

        val railBg = driverRail.getAttribute("android:background")
        assertTrue(
            "Driver rail background ($railBg) must be @color/car_bg_obsidian or obsidian color",
            railBg == "@color/car_bg_obsidian" || railBg.equals("#101217", ignoreCase = true)
        )

        val railButtons = listOf("btnBack", "btnWifiSettings", "btnStarredQuick", "btnSettings")
        for (btnId in railButtons) {
            val btn = idMap[btnId]
            assertNotNull("$btnId must exist in driver rail", btn)

            val btnWidth = btn!!.getAttribute("android:layout_width")
            val btnHeight = btn.getAttribute("android:layout_height")
            val btnBg = btn.getAttribute("android:background")

            assertTrue(
                "$btnId width ($btnWidth) must be @dimen/car_rail_tile_size, @dimen/car_rail_icon_size, or >= 56dp",
                btnWidth == "@dimen/car_rail_tile_size" || btnWidth == "@dimen/car_rail_icon_size" ||
                        btnWidth.replace("dp", "").toIntOrNull()?.let { it >= 56 } == true
            )

            assertTrue(
                "$btnId height ($btnHeight) must be @dimen/car_rail_tile_size, @dimen/car_rail_icon_size, or >= 56dp",
                btnHeight == "@dimen/car_rail_tile_size" || btnHeight == "@dimen/car_rail_icon_size" ||
                        btnHeight.replace("dp", "").toIntOrNull()?.let { it >= 56 } == true
            )

            assertEquals(
                "$btnId background must be @drawable/bg_car_rail_tile",
                "@drawable/bg_car_rail_tile",
                btnBg
            )
        }
    }

    @Test
    fun testDedicatedSyncStatusContainerStructure() {
        val file = findResFile("layout-land/activity_car.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val ivSyncStatus = idMap["ivSyncStatus"]
        assertNotNull("ivSyncStatus must exist in driver rail", ivSyncStatus)

        val parentNode = ivSyncStatus!!.parentNode as? Element
        assertNotNull("ivSyncStatus must have a parent container", parentNode)

        val parentBg = parentNode!!.getAttribute("android:background")
        val parentWidth = parentNode.getAttribute("android:layout_width")
        val parentHeight = parentNode.getAttribute("android:layout_height")

        assertTrue(
            "Sync indicator container background must be @drawable/bg_car_rail_tile. Found: $parentBg",
            parentBg == "@drawable/bg_car_rail_tile"
        )
        assertTrue(
            "Sync indicator container width must be @dimen/car_rail_tile_size or >= 56dp. Found: $parentWidth",
            parentWidth == "@dimen/car_rail_tile_size" || parentWidth == "@dimen/car_rail_icon_size" ||
                    parentWidth.replace("dp", "").toIntOrNull()?.let { it >= 56 } == true
        )
        assertTrue(
            "Sync indicator container height must be @dimen/car_rail_tile_size or >= 56dp. Found: $parentHeight",
            parentHeight == "@dimen/car_rail_tile_size" || parentHeight == "@dimen/car_rail_icon_size" ||
                    parentHeight.replace("dp", "").toIntOrNull()?.let { it >= 56 } == true
        )
    }

    @Test
    fun testSyncPillBadgeStructureAndTokens() {
        val file = findResFile("layout-land/activity_car.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val tvSyncHeader = idMap["tvSyncHeader"]
        val tvSyncTime = idMap["tvSyncTime"]
        assertNotNull("tvSyncHeader must exist in sync pill", tvSyncHeader)
        assertNotNull("tvSyncTime must exist in sync pill", tvSyncTime)

        val parentNode = tvSyncHeader!!.parentNode as? Element
        assertNotNull("tvSyncHeader must have a parent pill container", parentNode)

        val badgeBg = parentNode!!.getAttribute("android:background")
        assertEquals(
            "Sync status badge background must be @drawable/bg_car_pill_badge",
            "@drawable/bg_car_pill_badge",
            badgeBg
        )

        val headerText = tvSyncHeader.getAttribute("android:text")
        assertTrue("tvSyncHeader text should contain 'ĐÃ ĐỒNG BỘ'", headerText.contains("ĐÃ ĐỒNG BỘ"))
    }
}
