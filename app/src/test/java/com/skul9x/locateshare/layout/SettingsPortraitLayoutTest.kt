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

class SettingsPortraitLayoutTest {

    private fun findLayoutFile(): File {
        val possiblePaths = listOf(
            "app/src/main/res/layout/activity_settings.xml",
            "src/main/res/layout/activity_settings.xml",
            "../app/src/main/res/layout/activity_settings.xml"
        )
        for (path in possiblePaths) {
            val file = File(path)
            if (file.exists()) {
                return file
            }
        }
        val workspaceDir = File(".").canonicalFile
        val file = File(workspaceDir, "app/src/main/res/layout/activity_settings.xml")
        return file
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

    @Test
    fun testSettingsLayoutFileExists() {
        val file = findLayoutFile()
        assertTrue("activity_settings.xml must exist in app/src/main/res/layout/", file.exists() && file.isFile)
    }

    @Test
    fun testRootIsNestedScrollViewWithScrollbarsAndFillViewport() {
        val file = findLayoutFile()
        val doc = parseXml(file)
        val root = doc.documentElement

        assertNotNull("Root element should exist", root)
        assertTrue(
            "Root element must be androidx.core.widget.NestedScrollView or ScrollView, but was: ${root.tagName}",
            root.tagName == "androidx.core.widget.NestedScrollView" || root.tagName.contains("ScrollView")
        )

        val scrollbarsNs = root.getAttributeNS("http://schemas.android.com/apk/res/android", "scrollbars")
        val scrollbarsSimple = root.getAttribute("android:scrollbars")
        val scrollbars = if (scrollbarsNs.isNotEmpty()) scrollbarsNs else scrollbarsSimple
        assertEquals("android:scrollbars attribute must be 'vertical'", "vertical", scrollbars)

        val fillViewportNs = root.getAttributeNS("http://schemas.android.com/apk/res/android", "fillViewport")
        val fillViewportSimple = root.getAttribute("android:fillViewport")
        val fillViewport = if (fillViewportNs.isNotEmpty()) fillViewportNs else fillViewportSimple
        assertEquals("android:fillViewport attribute must be 'true'", "true", fillViewport)
    }

    @Test
    fun testRequiredViewIdsExist() {
        val file = findLayoutFile()
        val doc = parseXml(file)
        val ids = mutableSetOf<String>()
        collectAllIds(doc.documentElement, ids)

        val requiredIds = listOf(
            "btnBackSettings",
            "etFavName",
            "etFavUrl",
            "btnAddFavorite",
            "rvFavorites"
        )

        for (reqId in requiredIds) {
            assertTrue("Required view ID '@+id/$reqId' must exist in activity_settings.xml. Found IDs: $ids", ids.contains(reqId))
        }
    }

    @Test
    fun testSettingsActivityConfiguresNestedScrollingDisabledForRecyclerView() {
        val possiblePaths = listOf(
            "app/src/main/java/com/skul9x/locateshare/SettingsActivity.kt",
            "src/main/java/com/skul9x/locateshare/SettingsActivity.kt",
            "../app/src/main/java/com/skul9x/locateshare/SettingsActivity.kt"
        )
        val file = possiblePaths.map { File(it) }.firstOrNull { it.exists() }
            ?: File(".", "app/src/main/java/com/skul9x/locateshare/SettingsActivity.kt")

        assertTrue("SettingsActivity.kt must exist", file.exists())
        val content = file.readText()
        assertTrue(
            "SettingsActivity.kt must configure rvFavorites.isNestedScrollingEnabled = false",
            content.contains("rvFavorites.isNestedScrollingEnabled = false")
        )
    }
}
