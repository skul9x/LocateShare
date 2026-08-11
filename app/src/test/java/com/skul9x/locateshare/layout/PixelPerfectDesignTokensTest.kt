package com.skul9x.locateshare.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class PixelPerfectDesignTokensTest {

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

    private fun extractColors(doc: Document): Map<String, String> {
        val colors = mutableMapOf<String, String>()
        val colorNodes = doc.getElementsByTagName("color")
        for (i in 0 until colorNodes.length) {
            val node = colorNodes.item(i) as? Element ?: continue
            val name = node.getAttribute("name")
            val value = node.textContent.trim()
            if (name.isNotEmpty()) {
                colors[name] = value
            }
        }
        return colors
    }

    private fun extractDimens(doc: Document): Map<String, String> {
        val dimens = mutableMapOf<String, String>()
        val dimenNodes = doc.getElementsByTagName("dimen")
        for (i in 0 until dimenNodes.length) {
            val node = dimenNodes.item(i) as? Element ?: continue
            val name = node.getAttribute("name")
            val value = node.textContent.trim()
            if (name.isNotEmpty()) {
                dimens[name] = value
            }
        }
        return dimens
    }

    private fun parseNumericValue(valueStr: String): Double {
        val clean = valueStr.replace("dp", "").replace("sp", "").replace("px", "").trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    @Test
    fun testPixelPerfectColorTokensExistAndMatchPalette() {
        val file = findResFile("values/colors.xml")
        assertTrue("colors.xml must exist in res/values/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val colors = extractColors(doc)

        val requiredColorKeys = listOf(
            "car_bg_obsidian",
            "car_bg_obsidian_subtle",
            "car_card_surface_dark",
            "car_card_surface_elevated",
            "car_card_tile",
            "car_card_tile_stroke",
            "car_card_stroke_dark",
            "car_btn_reload_bg",
            "car_btn_reload_stroke",
            "car_accent_emerald",
            "car_accent_emerald_bright",
            "car_accent_emerald_dark",
            "car_accent_emerald_pressed",
            "car_pill_badge_bg",
            "car_pill_badge_stroke",
            "car_pill_badge_dot",
            "car_starred_gold",
            "car_accent_pin"
        )

        for (key in requiredColorKeys) {
            assertTrue("colors.xml must define color '$key'. Found: ${colors.keys}", colors.containsKey(key))
        }

        // Verify color values
        assertEquals("#101217", colors["car_bg_obsidian"]?.uppercase())
        assertEquals("#141720", colors["car_bg_obsidian_subtle"]?.uppercase())
        assertEquals("#161922", colors["car_card_surface_dark"]?.uppercase())
        assertEquals("#1C202B", colors["car_card_tile"]?.uppercase())
        assertEquals("#282D3C", colors["car_card_tile_stroke"]?.uppercase())
        assertEquals("#222632", colors["car_btn_reload_bg"]?.uppercase())
        assertEquals("#2E3444", colors["car_btn_reload_stroke"]?.uppercase())
        assertEquals("#00D26A", colors["car_accent_emerald"]?.uppercase())
        assertEquals("#00E676", colors["car_accent_emerald_bright"]?.uppercase())
        assertEquals("#132E22", colors["car_accent_emerald_dark"]?.uppercase())
        assertEquals("#132E22", colors["car_pill_badge_bg"]?.uppercase())
        assertEquals("#00E676", colors["car_pill_badge_stroke"]?.uppercase())
        assertEquals("#FFD700", colors["car_starred_gold"]?.uppercase())
        assertEquals("#4F75FF", colors["car_accent_pin"]?.uppercase())

        val hexRegex = Regex("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")
        for ((name, value) in colors) {
            assertTrue("Color '$name' value '$value' must be a valid hex color code", hexRegex.matches(value))
        }
    }

    @Test
    fun testPixelPerfectDimensionTokens() {
        val landFile = findResFile("values-land/dimens.xml")
        assertTrue("values-land/dimens.xml must exist in res/", landFile.exists() && landFile.isFile)

        val doc = parseXml(landFile)
        val dimens = extractDimens(doc)

        val requiredTokens = listOf(
            "car_hero_button_height_large",
            "car_hero_button_reload_size",
            "car_card_radius_large",
            "car_card_radius_medium",
            "car_card_radius_small",
            "car_card_radius_pill",
            "car_rail_tile_size",
            "car_rail_tile_radius",
            "car_badge_radius_pill",
            "car_pill_badge_padding_horizontal",
            "car_pill_badge_padding_vertical",
            "car_pill_badge_dot_size"
        )

        for (token in requiredTokens) {
            assertTrue("values-land/dimens.xml must define dimen '$token'", dimens.containsKey(token))
        }

        assertEquals(72.0, parseNumericValue(dimens["car_hero_button_height_large"] ?: "0"), 0.01)
        assertEquals(72.0, parseNumericValue(dimens["car_hero_button_reload_size"] ?: "0"), 0.01)
        assertEquals(20.0, parseNumericValue(dimens["car_card_radius_large"] ?: "0"), 0.01)
        assertEquals(16.0, parseNumericValue(dimens["car_card_radius_medium"] ?: "0"), 0.01)
        assertEquals(14.0, parseNumericValue(dimens["car_card_radius_small"] ?: "0"), 0.01)
        assertEquals(50.0, parseNumericValue(dimens["car_card_radius_pill"] ?: "0"), 0.01)
        assertEquals(56.0, parseNumericValue(dimens["car_rail_tile_size"] ?: "0"), 0.01)
        assertEquals(16.0, parseNumericValue(dimens["car_rail_tile_radius"] ?: "0"), 0.01)
        assertEquals(24.0, parseNumericValue(dimens["car_badge_radius_pill"] ?: "0"), 0.01)
    }

    @Test
    fun testPixelPerfectCustomDrawablesExistAndValid() {
        val requiredDrawables = listOf(
            "drawable/bg_car_rail_tile.xml",
            "drawable/bg_car_pill_badge.xml",
            "drawable/bg_car_btn_navigation.xml",
            "drawable/bg_car_btn_reload.xml"
        )

        for (drawablePath in requiredDrawables) {
            val file = findResFile(drawablePath)
            assertTrue("Drawable file '$drawablePath' must exist", file.exists() && file.isFile)

            val doc = parseXml(file)
            assertNotNull("Root element of '$drawablePath' should not be null", doc.documentElement)
            val rootTag = doc.documentElement.tagName
            assertTrue("Root of '$drawablePath' must be ripple or shape, found <$rootTag>",
                rootTag == "ripple" || rootTag == "shape")
        }
    }

    @Test
    fun testPillBadgeDrawableStructure() {
        val file = findResFile("drawable/bg_car_pill_badge.xml")
        val doc = parseXml(file)
        val root = doc.documentElement
        assertEquals("shape", root.tagName)
        assertEquals("rectangle", root.getAttribute("android:shape"))

        val solidNodes = root.getElementsByTagName("solid")
        assertTrue("bg_car_pill_badge.xml must contain <solid>", solidNodes.length > 0)

        val strokeNodes = root.getElementsByTagName("stroke")
        assertTrue("bg_car_pill_badge.xml must contain <stroke>", strokeNodes.length > 0)

        val cornersNodes = root.getElementsByTagName("corners")
        assertTrue("bg_car_pill_badge.xml must contain <corners>", cornersNodes.length > 0)
    }

    @Test
    fun testNavigationButtonDrawableStructure() {
        val file = findResFile("drawable/bg_car_btn_navigation.xml")
        val doc = parseXml(file)
        val root = doc.documentElement
        assertEquals("ripple", root.tagName)

        val gradientNodes = root.getElementsByTagName("gradient")
        assertTrue("bg_car_btn_navigation.xml should have gradient background", gradientNodes.length > 0)

        val cornersNodes = root.getElementsByTagName("corners")
        assertTrue("bg_car_btn_navigation.xml must define corners radius", cornersNodes.length > 0)
    }

    @Test
    fun testReloadButtonDrawableStructure() {
        val file = findResFile("drawable/bg_car_btn_reload.xml")
        val doc = parseXml(file)
        val root = doc.documentElement
        assertEquals("ripple", root.tagName)

        val strokeNodes = root.getElementsByTagName("stroke")
        assertTrue("bg_car_btn_reload.xml must define stroke border", strokeNodes.length > 0)
    }
}
