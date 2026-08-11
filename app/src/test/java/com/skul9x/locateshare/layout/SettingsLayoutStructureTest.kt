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

class SettingsLayoutStructureTest {

    private fun findLayoutFile(isLandscape: Boolean): File {
        val pathSuffix = if (isLandscape) "res/layout-land/activity_settings.xml" else "res/layout/activity_settings.xml"
        val possiblePaths = listOf(
            "app/src/main/$pathSuffix",
            "src/main/$pathSuffix",
            "../app/src/main/$pathSuffix"
        )
        for (path in possiblePaths) {
            val file = File(path)
            if (file.exists()) {
                return file
            }
        }
        val workspaceDir = File(".").canonicalFile
        return File(workspaceDir, "app/src/main/$pathSuffix")
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
    fun testPortraitLayoutScrollAndFillViewport() {
        val file = findLayoutFile(isLandscape = false)
        assertTrue("activity_settings.xml must exist in app/src/main/res/layout/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull("Root element should exist", root)

        assertTrue(
            "Portrait root must be androidx.core.widget.NestedScrollView or ScrollView, but was: ${root.tagName}",
            root.tagName == "androidx.core.widget.NestedScrollView" || root.tagName.contains("ScrollView")
        )

        val fillViewportNs = root.getAttributeNS("http://schemas.android.com/apk/res/android", "fillViewport")
        val fillViewportSimple = root.getAttribute("android:fillViewport")
        val fillViewport = if (fillViewportNs.isNotEmpty()) fillViewportNs else fillViewportSimple
        assertEquals("Portrait layout must have android:fillViewport='true'", "true", fillViewport)

        val scrollbarsNs = root.getAttributeNS("http://schemas.android.com/apk/res/android", "scrollbars")
        val scrollbarsSimple = root.getAttribute("android:scrollbars")
        val scrollbars = if (scrollbarsNs.isNotEmpty()) scrollbarsNs else scrollbarsSimple
        assertEquals("Portrait layout root must have android:scrollbars='vertical'", "vertical", scrollbars)
    }

    @Test
    fun testLandscapeLayoutTwoColumnAndScrollbars() {
        val file = findLayoutFile(isLandscape = true)
        assertTrue("activity_settings.xml must exist in app/src/main/res/layout-land/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull("Root element should exist", root)

        val orientationNs = root.getAttributeNS("http://schemas.android.com/apk/res/android", "orientation")
        val orientationSimple = root.getAttribute("android:orientation")
        val orientation = if (orientationNs.isNotEmpty()) orientationNs else orientationSimple

        assertTrue(
            "Landscape root must be horizontal 2-column LinearLayout or ConstraintLayout, but was: ${root.tagName} (orientation=$orientation)",
            orientation == "horizontal" || root.tagName.contains("ConstraintLayout")
        )

        val rv = findElementByTagOrId(root, "RecyclerView", "rvFavorites")
        assertNotNull("RecyclerView rvFavorites must exist in landscape layout", rv)

        val rvScrollbarsNs = rv?.getAttributeNS("http://schemas.android.com/apk/res/android", "scrollbars") ?: ""
        val rvScrollbarsSimple = rv?.getAttribute("android:scrollbars") ?: ""
        val rvScrollbars = if (rvScrollbarsNs.isNotEmpty()) rvScrollbarsNs else rvScrollbarsSimple
        assertEquals("rvFavorites in landscape layout must have android:scrollbars='vertical'", "vertical", rvScrollbars)
    }

    @Test
    fun testRequiredViewIdsAcrossBothLayouts() {
        val portraitRequiredIds = listOf(
            "btnBackSettings",
            "etFavName",
            "etFavUrl",
            "btnAddFavorite",
            "rvFavorites"
        )

        val landscapeRequiredIds = listOf(
            "btnBack",
            "layoutSettingsLeft",
            "layoutSettingsRight",
            "btnAddFavorite",
            "rvFavorites"
        )

        // Check Portrait Layout IDs
        val portraitFile = findLayoutFile(isLandscape = false)
        val portraitDoc = parseXml(portraitFile)
        val portraitIds = mutableSetOf<String>()
        collectAllIds(portraitDoc.documentElement, portraitIds)

        for (id in portraitRequiredIds) {
            assertTrue(
                "Required ID '$id' must exist in portrait layout activity_settings.xml. Found: $portraitIds",
                portraitIds.contains(id)
            )
        }

        // Check Landscape Layout IDs
        val landFile = findLayoutFile(isLandscape = true)
        val landDoc = parseXml(landFile)
        val landIds = mutableSetOf<String>()
        collectAllIds(landDoc.documentElement, landIds)

        for (id in landscapeRequiredIds) {
            assertTrue(
                "Required ID '$id' must exist in landscape layout activity_settings.xml. Found: $landIds",
                landIds.contains(id)
            )
        }
    }
}
