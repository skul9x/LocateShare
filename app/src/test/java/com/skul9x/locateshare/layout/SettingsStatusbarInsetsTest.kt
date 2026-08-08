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

class SettingsStatusbarInsetsTest {

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

    @Test
    fun testPortraitFitsSystemWindows() {
        val file = findLayoutFile(isLandscape = false)
        assertTrue("activity_settings.xml must exist in app/src/main/res/layout/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull("Root element should exist", root)

        val fitsSystemWindowsNs = root.getAttributeNS("http://schemas.android.com/apk/res/android", "fitsSystemWindows")
        val fitsSystemWindowsSimple = root.getAttribute("android:fitsSystemWindows")
        val fitsSystemWindows = if (fitsSystemWindowsNs.isNotEmpty()) fitsSystemWindowsNs else fitsSystemWindowsSimple

        assertEquals(
            "Portrait activity_settings.xml root element must have android:fitsSystemWindows='true'",
            "true",
            fitsSystemWindows
        )
    }

    @Test
    fun testLandscapeFitsSystemWindows() {
        val file = findLayoutFile(isLandscape = true)
        assertTrue("activity_settings.xml must exist in app/src/main/res/layout-land/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val root = doc.documentElement
        assertNotNull("Root element should exist", root)

        val fitsSystemWindowsNs = root.getAttributeNS("http://schemas.android.com/apk/res/android", "fitsSystemWindows")
        val fitsSystemWindowsSimple = root.getAttribute("android:fitsSystemWindows")
        val fitsSystemWindows = if (fitsSystemWindowsNs.isNotEmpty()) fitsSystemWindowsNs else fitsSystemWindowsSimple

        assertEquals(
            "Landscape activity_settings.xml root element must have android:fitsSystemWindows='true'",
            "true",
            fitsSystemWindows
        )
    }

    @Test
    fun testRequiredViewsPreserved() {
        val requiredIds = listOf(
            "btnBackSettings",
            "etFavName",
            "etFavUrl",
            "btnAddFavorite",
            "rvFavorites"
        )

        // Portrait
        val portraitFile = findLayoutFile(isLandscape = false)
        val portraitDoc = parseXml(portraitFile)
        val portraitIds = mutableSetOf<String>()
        collectAllIds(portraitDoc.documentElement, portraitIds)

        for (id in requiredIds) {
            assertTrue(
                "Required ID '$id' must exist in portrait activity_settings.xml. Found: $portraitIds",
                portraitIds.contains(id)
            )
        }

        // Landscape
        val landFile = findLayoutFile(isLandscape = true)
        val landDoc = parseXml(landFile)
        val landIds = mutableSetOf<String>()
        collectAllIds(landDoc.documentElement, landIds)

        for (id in requiredIds) {
            assertTrue(
                "Required ID '$id' must exist in landscape activity_settings.xml. Found: $landIds",
                landIds.contains(id)
            )
        }
    }
}
