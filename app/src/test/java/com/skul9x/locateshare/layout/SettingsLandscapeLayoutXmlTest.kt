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

class SettingsLandscapeLayoutXmlTest {

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
    fun testSettingsLandscapeLayoutXmlExistsAndParses() {
        val file = findResFile("layout-land/activity_settings.xml")
        assertTrue("layout-land/activity_settings.xml must exist in res/", file.exists() && file.isFile)

        val doc = parseXml(file)
        assertNotNull("Root XML document must not be null", doc.documentElement)
    }

    @Test
    fun testRequiredLandscapeElementIdsExist() {
        val file = findResFile("layout-land/activity_settings.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val requiredIds = listOf(
            "layoutSettingsLeft",
            "layoutSettingsRight",
            "btnBack",
            "tvStatus",
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
    fun testAddFavoriteButtonDimensionsAndDriverSafety() {
        val file = findResFile("layout-land/activity_settings.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val btnAddFavorite = idMap["btnAddFavorite"]
        assertNotNull("btnAddFavorite must exist in landscape layout", btnAddFavorite)

        val height = btnAddFavorite!!.getAttribute("android:layout_height")
        val minHeight = btnAddFavorite.getAttribute("android:minHeight")

        val heightDp = height.replace("dp", "").toIntOrNull()
        val minHeightDp = minHeight.replace("dp", "").toIntOrNull()

        val isHeightValid = (heightDp != null && heightDp >= 56) ||
                (minHeightDp != null && minHeightDp >= 56) ||
                height.contains("hero_button") ||
                height.contains("button_min_height")

        assertTrue(
            "btnAddFavorite height ($height) or minHeight ($minHeight) must be >= 56dp for automotive touch safety",
            isHeightValid
        )
    }

    @Test
    fun testMasterDetailPaneWeights() {
        val file = findResFile("layout-land/activity_settings.xml")
        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val leftPane = idMap["layoutSettingsLeft"]
        val rightPane = idMap["layoutSettingsRight"]

        assertNotNull("layoutSettingsLeft must exist", leftPane)
        assertNotNull("layoutSettingsRight must exist", rightPane)

        val leftWeight = leftPane!!.getAttribute("android:layout_weight")
        val rightWeight = rightPane!!.getAttribute("android:layout_weight")

        assertEquals("Left pane should have layout_weight='35'", "35", leftWeight)
        assertEquals("Right pane should have layout_weight='65'", "65", rightWeight)
    }

    @Test
    fun testKeepScreenOnAndFitsSystemWindowsInLandscapeSettings() {
        val file = findResFile("layout-land/activity_settings.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        val keepScreenOn = root.getAttribute("android:keepScreenOn")
        assertEquals("Root layout must have android:keepScreenOn='true' for car mode", "true", keepScreenOn)

        val fitsSystemWindows = root.getAttribute("android:fitsSystemWindows")
        assertEquals("Root layout must have android:fitsSystemWindows='true'", "true", fitsSystemWindows)
    }

    @Test
    fun testEditDialogLayoutXmlExistsAndValid() {
        val file = findResFile("layout/dialog_edit_favorite.xml")
        assertTrue("layout/dialog_edit_favorite.xml must exist in res/layout/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val requiredDialogIds = listOf(
            "tvDialogTitle",
            "etEditName",
            "etEditUrl"
        )

        for (id in requiredDialogIds) {
            assertTrue(
                "dialog_edit_favorite.xml must contain id '@+id/$id'. Found: ${idMap.keys}",
                idMap.containsKey(id)
            )
        }

        val etEditName = idMap["etEditName"]
        val nameMinHeight = etEditName?.getAttribute("android:minHeight")?.replace("dp", "")?.toIntOrNull()
            ?: etEditName?.getAttribute("android:layout_height")?.replace("dp", "")?.toIntOrNull() ?: 0
        assertTrue("etEditName minHeight should be >= 48dp for in-car typing", nameMinHeight >= 48)
    }

    @Test
    fun testItemFavoriteTouchTargets() {
        val file = findResFile("layout/item_favorite.xml")
        assertTrue("layout/item_favorite.xml must exist", file.exists() && file.isFile)

        val doc = parseXml(file)
        val idMap = extractElementIds(doc)

        val requiredItemIds = listOf(
            "btnStar",
            "tvFavName",
            "tvFavUrl",
            "btnEdit",
            "btnDelete"
        )

        for (id in requiredItemIds) {
            assertTrue("item_favorite.xml must contain id '@+id/$id'", idMap.containsKey(id))
        }

        val btnStar = idMap["btnStar"]
        val starWidth = btnStar?.getAttribute("android:layout_width")?.replace("dp", "")?.toIntOrNull() ?: 0
        val starHeight = btnStar?.getAttribute("android:layout_height")?.replace("dp", "")?.toIntOrNull() ?: 0
        assertTrue("btnStar touch target width ($starWidth) must be >= 48dp", starWidth >= 48)
        assertTrue("btnStar touch target height ($starHeight) must be >= 48dp", starHeight >= 48)

        val btnEdit = idMap["btnEdit"]
        val editWidth = btnEdit?.getAttribute("android:layout_width")?.replace("dp", "")?.toIntOrNull() ?: 0
        assertTrue("btnEdit touch target width ($editWidth) must be >= 48dp", editWidth >= 48)

        val btnDelete = idMap["btnDelete"]
        val deleteWidth = btnDelete?.getAttribute("android:layout_width")?.replace("dp", "")?.toIntOrNull() ?: 0
        assertTrue("btnDelete touch target width ($deleteWidth) must be >= 48dp", deleteWidth >= 48)
    }
}
