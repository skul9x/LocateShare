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

class FavoritesFloatingModalLandscapeTest {

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

    @Test
    fun testLandscapeDialogFavoritesCardPopupXmlExistsAndParses() {
        val file = findResFile("layout-land/dialog_favorites_card_popup.xml")
        assertTrue("layout-land/dialog_favorites_card_popup.xml must exist in res/layout-land/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull("Root element should exist", root)

        assertTrue(
            "Root element should be MaterialCardView or CardView, but was: ${root.tagName}",
            root.tagName.contains("CardView") || root.tagName.contains("MaterialCardView")
        )
    }

    @Test
    fun testLandscapeDialogCardViewStylingAndCorners() {
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
    }

    @Test
    fun testLandscapeDialogRequiredViewIds() {
        val file = findResFile("layout-land/dialog_favorites_card_popup.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        val ids = mutableSetOf<String>()
        collectAllIds(root, ids)

        val requiredIds = listOf(
            "rvFavoritesPopup",
            "btnClosePopup",
            "tvPopupTitle",
            "tvEmptyFavorites"
        )
        for (id in requiredIds) {
            assertTrue("Landscape dialog must contain ID '$id'. Found: $ids", ids.contains(id))
        }
    }

    @Test
    fun testLandscapeDialogCloseButtonTouchTarget() {
        val file = findResFile("layout-land/dialog_favorites_card_popup.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        val btnClose = findElementByTagOrId(root, "ImageButton", "btnClosePopup")
        assertNotNull("Close button 'btnClosePopup' must exist", btnClose)

        val width = btnClose?.getAttributeNS("http://schemas.android.com/apk/res/android", "layout_width")
            ?.ifEmpty { null } ?: btnClose?.getAttribute("android:layout_width")
        val height = btnClose?.getAttributeNS("http://schemas.android.com/apk/res/android", "layout_height")
            ?.ifEmpty { null } ?: btnClose?.getAttribute("android:layout_height")

        assertEquals("Close button width must be 56dp", "56dp", width)
        assertEquals("Close button height must be 56dp", "56dp", height)
    }

    @Test
    fun testLandscapeDialogRecyclerViewScrollbarConfig() {
        val file = findResFile("layout-land/dialog_favorites_card_popup.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        val rv = findElementByTagOrId(root, "RecyclerView", "rvFavoritesPopup")
        assertNotNull("RecyclerView rvFavoritesPopup must exist", rv)

        val scrollbars = rv?.getAttributeNS("http://schemas.android.com/apk/res/android", "scrollbars")?.ifEmpty { null }
            ?: rv?.getAttribute("android:scrollbars")
        assertEquals("rvFavoritesPopup must have android:scrollbars='vertical'", "vertical", scrollbars)

        val fadeScrollbars = rv?.getAttributeNS("http://schemas.android.com/apk/res/android", "fadeScrollbars")?.ifEmpty { null }
            ?: rv?.getAttribute("android:fadeScrollbars")
        assertEquals("rvFavoritesPopup must have android:fadeScrollbars='false'", "false", fadeScrollbars)

        val scrollbarSize = rv?.getAttributeNS("http://schemas.android.com/apk/res/android", "scrollbarSize")?.ifEmpty { null }
            ?: rv?.getAttribute("android:scrollbarSize")
        assertEquals("rvFavoritesPopup must have android:scrollbarSize='6dp'", "6dp", scrollbarSize)

        val thumb = rv?.getAttributeNS("http://schemas.android.com/apk/res/android", "scrollbarThumbVertical")?.ifEmpty { null }
            ?: rv?.getAttribute("android:scrollbarThumbVertical")
        assertEquals("rvFavoritesPopup must have android:scrollbarThumbVertical='@drawable/scrollbar_thumb_car'", "@drawable/scrollbar_thumb_car", thumb)
    }

    @Test
    fun testLandscapeDialogHeaderTitleText() {
        val file = findResFile("layout-land/dialog_favorites_card_popup.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        val tvTitle = findElementByTagOrId(root, "TextView", "tvPopupTitle")
        assertNotNull("tvPopupTitle must exist", tvTitle)

        val titleText = tvTitle?.getAttributeNS("http://schemas.android.com/apk/res/android", "text")?.ifEmpty { null }
            ?: tvTitle?.getAttribute("android:text")
        assertTrue("Dialog header title should contain 'ĐỊA ĐIỂM ƯA THÍCH'", titleText?.contains("ĐỊA ĐIỂM ƯA THÍCH") == true)
    }

    @Test
    fun testItemFavoriteCardOptimizedFor2ColumnGrid() {
        val file = findResFile("layout/item_favorite_card.xml")
        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull(root)

        val strokeColor = root.getAttributeNS("http://schemas.android.com/apk/res-auto", "strokeColor")
            .ifEmpty { root.getAttribute("app:strokeColor") }
        assertEquals("Card stroke color must be '#2E3240'", "#2E3240", strokeColor)

        val cornerRadius = root.getAttributeNS("http://schemas.android.com/apk/res-auto", "cardCornerRadius")
            .ifEmpty { root.getAttribute("app:cardCornerRadius") }
        assertEquals("Card corner radius must be '16dp'", "16dp", cornerRadius)

        val btnOpenMap = findElementByTagOrId(root, "MaterialButton", "btnCardOpenMap")
            ?: findElementByTagOrId(root, "Button", "btnCardOpenMap")
        assertNotNull(btnOpenMap)

        val btnHeight = btnOpenMap?.getAttributeNS("http://schemas.android.com/apk/res/android", "layout_height")?.ifEmpty { null }
            ?: btnOpenMap?.getAttribute("android:layout_height")
        assertEquals("Action button height must be 60dp", "60dp", btnHeight)
    }
}
