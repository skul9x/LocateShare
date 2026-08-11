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

class AutomotiveThemeXmlTest {

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

    @Test
    fun testAutomotiveColorTokensExistAndFormat() {
        val file = findResFile("values/colors.xml")
        assertTrue("colors.xml must exist in res/values/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val colors = extractColors(doc)

        // Required tokens
        val requiredColorKeys = listOf(
            "car_bg_dark",
            "car_card_surface",
            "car_card_stroke",
            "car_accent_green",
            "car_starred_gold",
            "car_text_primary",
            "car_text_secondary"
        )

        for (key in requiredColorKeys) {
            assertTrue("colors.xml must define color '$key'. Found: ${colors.keys}", colors.containsKey(key))
        }

        // Verify specific color values match automotive specifications
        assertEquals("#0F1015", colors["car_bg_dark"]?.uppercase())
        assertEquals("#1B1D24", colors["car_card_surface"]?.uppercase())
        assertEquals("#2E3240", colors["car_card_stroke"]?.uppercase())
        assertEquals("#00E676", colors["car_accent_green"]?.uppercase())
        assertEquals("#FFD700", colors["car_starred_gold"]?.uppercase())
        assertEquals("#FFFFFF", colors["car_text_primary"]?.uppercase())
        assertEquals("#B0B3C6", colors["car_text_secondary"]?.uppercase())

        // Validate all colors have valid hex format (#RGB, #RRGGBB, #AARRGGBB)
        val hexRegex = Regex("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")
        for ((name, value) in colors) {
            assertTrue("Color '$name' value '$value' must be a valid hex color code", hexRegex.matches(value))
        }
    }

    @Test
    fun testAutomotiveLandscapeThemeDefinition() {
        val file = findResFile("values/themes.xml")
        assertTrue("themes.xml must exist in res/values/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val styleNodes = doc.getElementsByTagName("style")
        var carLandscapeStyle: Element? = null

        for (i in 0 until styleNodes.length) {
            val style = styleNodes.item(i) as? Element ?: continue
            if (style.getAttribute("name") == "Theme.LocateShare.CarLandscape") {
                carLandscapeStyle = style
                break
            }
        }

        assertNotNull("Theme.LocateShare.CarLandscape must be defined in themes.xml", carLandscapeStyle)

        val items = mutableMapOf<String, String>()
        val itemNodes = carLandscapeStyle!!.getElementsByTagName("item")
        for (i in 0 until itemNodes.length) {
            val item = itemNodes.item(i) as? Element ?: continue
            val name = item.getAttribute("name")
            val value = item.textContent.trim()
            if (name.isNotEmpty()) {
                items[name] = value
            }
        }

        assertEquals("@color/car_bg_dark", items["android:windowBackground"])
        assertEquals("true", items["android:windowNoTitle"])
        assertEquals("false", items["android:windowActionBar"])
        assertEquals("@color/car_text_primary", items["android:textColorPrimary"])
        assertEquals("@color/car_accent_green", items["android:colorPrimary"])
    }

    @Test
    fun testAutomotiveDrawablesExistAndValid() {
        val requiredDrawables = listOf(
            "drawable/bg_car_card.xml",
            "drawable/bg_car_hero_button.xml",
            "drawable/bg_car_rail_item.xml",
            "drawable/bg_car_badge.xml"
        )

        for (drawablePath in requiredDrawables) {
            val file = findResFile(drawablePath)
            assertTrue("Drawable file '$drawablePath' must exist", file.exists() && file.isFile)

            val doc = parseXml(file)
            assertNotNull("Root element of '$drawablePath' should not be null", doc.documentElement)
        }
    }
}
