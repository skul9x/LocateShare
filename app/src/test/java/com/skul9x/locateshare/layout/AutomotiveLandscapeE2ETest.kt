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

class AutomotiveLandscapeE2ETest {

    private fun findFile(pathSuffix: String): File {
        val possiblePaths = listOf(
            pathSuffix,
            "app/$pathSuffix",
            "src/main/$pathSuffix",
            "app/src/main/$pathSuffix"
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
    fun testAllLandscapeLayoutFilesExist() {
        val files = listOf(
            findFile("res/layout-land/activity_car.xml"),
            findFile("res/layout-land/activity_settings.xml"),
            findFile("res/layout-land/dialog_favorites_card_popup.xml")
        )

        for (file in files) {
            assertTrue("Landscape file ${file.name} must exist", file.exists() && file.isFile)
        }
    }

    @Test
    fun testCarActivityLandscapeStructureAndIds() {
        val file = findFile("res/layout-land/activity_car.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        assertNotNull("Root element of activity_car.xml must exist", root)
        assertEquals("Root layout should be LinearLayout", "LinearLayout", root.tagName)

        val ids = mutableSetOf<String>()
        collectAllIds(root, ids)

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

        for (reqId in requiredCarIds) {
            assertTrue(
                "Landscape activity_car.xml must contain ID '$reqId'. Found: $ids",
                ids.contains(reqId)
            )
        }
    }

    @Test
    fun testSettingsActivityLandscapeStructureAndIds() {
        val file = findFile("res/layout-land/activity_settings.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        assertNotNull("Root element of landscape activity_settings.xml must exist", root)

        val ids = mutableSetOf<String>()
        collectAllIds(root, ids)

        val requiredSettingsIds = listOf(
            "btnBack",
            "layoutSettingsLeft",
            "layoutSettingsRight",
            "tvStatus",
            "ivStatusDot",
            "tvFavoritesCount",
            "tvStarredName",
            "tvStarredUrl",
            "tvVersionInfo",
            "btnAddFavorite",
            "rvFavorites"
        )

        for (reqId in requiredSettingsIds) {
            assertTrue(
                "Landscape activity_settings.xml must contain ID '$reqId'. Found: $ids",
                ids.contains(reqId)
            )
        }
    }

    @Test
    fun testFloatingFavoritesPopupLandscapeStructureAndIds() {
        val file = findFile("res/layout-land/dialog_favorites_card_popup.xml")
        val doc = parseXml(file)
        val root = doc.documentElement

        assertNotNull("Root element of dialog_favorites_card_popup.xml must exist", root)

        val ids = mutableSetOf<String>()
        collectAllIds(root, ids)

        val requiredPopupIds = listOf(
            "cardFavoritesDialogContainer",
            "tvPopupTitle",
            "btnClosePopup",
            "rvFavoritesPopup",
            "tvEmptyFavorites"
        )

        for (reqId in requiredPopupIds) {
            assertTrue(
                "Landscape dialog_favorites_card_popup.xml must contain ID '$reqId'. Found: $ids",
                ids.contains(reqId)
            )
        }
    }

    @Test
    fun testAndroidManifestAutomotiveMetadata() {
        val manifestFile = findFile("AndroidManifest.xml")
        assertTrue("AndroidManifest.xml must exist", manifestFile.exists())

        val doc = parseXml(manifestFile)
        val activities = doc.getElementsByTagName("activity")

        var carActivityFound = false
        var carHasDistractionOptimized = false
        var carConfigChangesValid = false

        var settingsActivityFound = false
        var settingsHasDistractionOptimized = false
        var settingsConfigChangesValid = false

        for (i in 0 until activities.length) {
            val act = activities.item(i) as? Element ?: continue
            val name = act.getAttributeNS("http://schemas.android.com/apk/res/android", "name")
                .ifEmpty { act.getAttribute("android:name") }

            val configChanges = act.getAttributeNS("http://schemas.android.com/apk/res/android", "configChanges")
                .ifEmpty { act.getAttribute("android:configChanges") }

            if (name == ".CarActivity" || name.endsWith(".CarActivity")) {
                carActivityFound = true
                carConfigChangesValid = configChanges.contains("orientation") && configChanges.contains("screenSize")

                val metaNodes = act.getElementsByTagName("meta-data")
                for (m in 0 until metaNodes.length) {
                    val meta = metaNodes.item(m) as? Element ?: continue
                    val metaName = meta.getAttributeNS("http://schemas.android.com/apk/res/android", "name")
                        .ifEmpty { meta.getAttribute("android:name") }
                    val metaVal = meta.getAttributeNS("http://schemas.android.com/apk/res/android", "value")
                        .ifEmpty { meta.getAttribute("android:value") }
                    if (metaName == "distractionOptimized" && metaVal == "true") {
                        carHasDistractionOptimized = true
                    }
                }
            }

            if (name == ".SettingsActivity" || name.endsWith(".SettingsActivity")) {
                settingsActivityFound = true
                settingsConfigChangesValid = configChanges.contains("orientation") && configChanges.contains("screenSize")

                val metaNodes = act.getElementsByTagName("meta-data")
                for (m in 0 until metaNodes.length) {
                    val meta = metaNodes.item(m) as? Element ?: continue
                    val metaName = meta.getAttributeNS("http://schemas.android.com/apk/res/android", "name")
                        .ifEmpty { meta.getAttribute("android:name") }
                    val metaVal = meta.getAttributeNS("http://schemas.android.com/apk/res/android", "value")
                        .ifEmpty { meta.getAttribute("android:value") }
                    if (metaName == "distractionOptimized" && metaVal == "true") {
                        settingsHasDistractionOptimized = true
                    }
                }
            }
        }

        assertTrue("CarActivity must be declared in AndroidManifest", carActivityFound)
        assertTrue("CarActivity must declare distractionOptimized='true'", carHasDistractionOptimized)
        assertTrue("CarActivity must declare automotive configChanges", carConfigChangesValid)

        assertTrue("SettingsActivity must be declared in AndroidManifest", settingsActivityFound)
        assertTrue("SettingsActivity must declare distractionOptimized='true'", settingsHasDistractionOptimized)
        assertTrue("SettingsActivity must declare automotive configChanges", settingsConfigChangesValid)
    }

    @Test
    fun testAutomotiveDesignTokensDeclared() {
        val dimensFile = findFile("res/values/dimens.xml")
        assertTrue("dimens.xml must exist", dimensFile.exists())

        val doc = parseXml(dimensFile)
        val dimens = doc.getElementsByTagName("dimen")
        val dimenNames = mutableSetOf<String>()

        for (i in 0 until dimens.length) {
            val el = dimens.item(i) as? Element ?: continue
            val name = el.getAttribute("name")
            if (name.isNotEmpty()) {
                dimenNames.add(name)
            }
        }

        val requiredDimens = listOf(
            "car_rail_width",
            "car_rail_icon_size",
            "car_card_corner_radius",
            "car_card_padding",
            "car_hero_button_height",
            "car_text_hero",
            "car_text_title",
            "car_text_subtitle",
            "car_text_body",
            "car_text_caption"
        )

        for (dim in requiredDimens) {
            assertTrue("Automotive dimens.xml must define token '$dim'", dimenNames.contains(dim))
        }
    }
}
