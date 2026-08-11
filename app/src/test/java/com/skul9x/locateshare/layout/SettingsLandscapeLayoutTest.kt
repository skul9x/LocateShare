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

class SettingsLandscapeLayoutTest {

    private fun findLandscapeLayoutFile(): File {
        val possiblePaths = listOf(
            "app/src/main/res/layout-land/activity_settings.xml",
            "src/main/res/layout-land/activity_settings.xml",
            "../app/src/main/res/layout-land/activity_settings.xml"
        )
        for (path in possiblePaths) {
            val file = File(path)
            if (file.exists()) {
                return file
            }
        }
        val workspaceDir = File(".").canonicalFile
        return File(workspaceDir, "app/src/main/res/layout-land/activity_settings.xml")
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

    private fun findElementByTag(node: Node, tagName: String): Element? {
        if (node is Element && (node.tagName == tagName || node.tagName.endsWith(".$tagName") || node.tagName.endsWith(":$tagName"))) {
            return node
        }
        val children = node.childNodes
        for (i in 0 until children.length) {
            val found = findElementByTag(children.item(i), tagName)
            if (found != null) return found
        }
        return null
    }

    @Test
    fun testSettingsLandscapeLayoutFileExists() {
        val file = findLandscapeLayoutFile()
        assertTrue("activity_settings.xml must exist in app/src/main/res/layout-land/", file.exists() && file.isFile)
    }

    @Test
    fun testLandscapeRootOrHorizontalSplit() {
        val file = findLandscapeLayoutFile()
        val doc = parseXml(file)
        val root = doc.documentElement

        assertNotNull("Root element should exist", root)
        val orientationNs = root.getAttributeNS("http://schemas.android.com/apk/res/android", "orientation")
        val orientationSimple = root.getAttribute("android:orientation")
        val orientation = if (orientationNs.isNotEmpty()) orientationNs else orientationSimple

        assertTrue(
            "Landscape root must be horizontal LinearLayout or ConstraintLayout, but was: ${root.tagName} (orientation=$orientation)",
            orientation == "horizontal" || root.tagName.contains("ConstraintLayout")
        )
    }

    @Test
    fun testLandscapeRequiredViewIdsExist() {
        val file = findLandscapeLayoutFile()
        val doc = parseXml(file)
        val ids = mutableSetOf<String>()
        collectAllIds(doc.documentElement, ids)

        val requiredIds = listOf(
            "btnBack",
            "layoutSettingsLeft",
            "layoutSettingsRight",
            "tvStatus",
            "btnAddFavorite",
            "rvFavorites"
        )

        for (reqId in requiredIds) {
            assertTrue("Required view ID '@+id/$reqId' must exist in layout-land/activity_settings.xml. Found IDs: $ids", ids.contains(reqId))
        }
    }

    @Test
    fun testLandscapeRecyclerViewHasScrollbars() {
        val file = findLandscapeLayoutFile()
        val doc = parseXml(file)
        val rv = findElementByTag(doc.documentElement, "RecyclerView")

        assertNotNull("RecyclerView must exist in landscape layout", rv)
        val scrollbarsNs = rv?.getAttributeNS("http://schemas.android.com/apk/res/android", "scrollbars") ?: ""
        val scrollbarsSimple = rv?.getAttribute("android:scrollbars") ?: ""
        val scrollbars = if (scrollbarsNs.isNotEmpty()) scrollbarsNs else scrollbarsSimple

        assertEquals("RecyclerView in landscape must have android:scrollbars='vertical'", "vertical", scrollbars)
    }
}
