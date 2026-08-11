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

class FloatingCardPopupXmlTest {

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
    fun testItemFavoriteCardXmlStructureAndIds() {
        val file = findResFile("layout/item_favorite_card.xml")
        assertTrue("item_favorite_card.xml must exist in res/layout/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull("Root element should exist", root)

        assertTrue(
            "Root element should be MaterialCardView or CardView, but was: ${root.tagName}",
            root.tagName.contains("CardView") || root.tagName.contains("MaterialCardView")
        )

        val ids = mutableSetOf<String>()
        collectAllIds(root, ids)

        val requiredIds = listOf("tvCardName", "tvCardAddress", "btnCardOpenMap")
        for (id in requiredIds) {
            assertTrue("Required view ID '$id' must exist in item_favorite_card.xml. Found: $ids", ids.contains(id))
        }

        val btnOpenMap = findElementByTagOrId(root, "Button", "btnCardOpenMap")
            ?: findElementByTagOrId(root, "MaterialButton", "btnCardOpenMap")
        assertNotNull("btnCardOpenMap button must exist in item_favorite_card.xml", btnOpenMap)

        val btnText = btnOpenMap?.getAttributeNS("http://schemas.android.com/apk/res/android", "text")
            ?: btnOpenMap?.getAttribute("android:text")
        assertEquals("Action button text must be 'MỞ BẢN ĐỒ'", "MỞ BẢN ĐỒ", btnText)
    }

    @Test
    fun testDialogFavoritesCardPopupXmlStructureAndScrollbar() {
        val file = findResFile("layout/dialog_favorites_card_popup.xml")
        assertTrue("dialog_favorites_card_popup.xml must exist in res/layout/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull("Root element should exist", root)

        val ids = mutableSetOf<String>()
        collectAllIds(root, ids)

        assertTrue("dialog_favorites_card_popup.xml must contain 'rvFavoritesPopup'", ids.contains("rvFavoritesPopup"))
        assertTrue("dialog_favorites_card_popup.xml must contain 'btnClosePopup'", ids.contains("btnClosePopup"))

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
    fun testScrollbarThumbDrawableXmlExists() {
        val file = findResFile("drawable/scrollbar_thumb_car.xml")
        assertTrue("scrollbar_thumb_car.xml must exist in res/drawable/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull("Root element should exist in scrollbar_thumb_car.xml", root)
        assertEquals("Root element should be 'shape'", "shape", root.tagName)
    }
}
