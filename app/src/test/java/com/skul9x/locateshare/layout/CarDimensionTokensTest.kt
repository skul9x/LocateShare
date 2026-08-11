package com.skul9x.locateshare.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class CarDimensionTokensTest {

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
    fun testLandscapeDimensXmlExistsAndContainsAutomotiveTokens() {
        val file = findResFile("values-land/dimens.xml")
        assertTrue("values-land/dimens.xml must exist in res/", file.exists() && file.isFile)

        val doc = parseXml(file)
        val dimens = extractDimens(doc)

        val requiredTokens = listOf(
            "car_button_min_height",
            "car_hero_button_height",
            "car_rail_icon_size",
            "car_rail_width",
            "car_card_corner_radius",
            "car_text_hero",
            "car_text_title",
            "car_text_body"
        )

        for (token in requiredTokens) {
            assertTrue("values-land/dimens.xml must define dimen '$token'. Found: ${dimens.keys}", dimens.containsKey(token))
        }
    }

    @Test
    fun testDriverSafeTouchTargetThresholds() {
        val file = findResFile("values-land/dimens.xml")
        val doc = parseXml(file)
        val dimens = extractDimens(doc)

        // Automotive driver touch target minimums: button height >= 56dp, hero button >= 64dp, rail icon >= 48dp
        val buttonMinHeight = parseNumericValue(dimens["car_button_min_height"] ?: "0")
        assertTrue("car_button_min_height ($buttonMinHeight dp) must be >= 56dp for driver safety", buttonMinHeight >= 56.0)

        val heroButtonHeight = parseNumericValue(dimens["car_hero_button_height"] ?: "0")
        assertTrue("car_hero_button_height ($heroButtonHeight dp) must be >= 64dp for glanceable navigation button", heroButtonHeight >= 64.0)

        val railIconSize = parseNumericValue(dimens["car_rail_icon_size"] ?: "0")
        assertTrue("car_rail_icon_size ($railIconSize dp) must be >= 48dp for driver rail touch targets", railIconSize >= 48.0)
    }

    @Test
    fun testGlanceableTypographyThresholds() {
        val file = findResFile("values-land/dimens.xml")
        val doc = parseXml(file)
        val dimens = extractDimens(doc)

        // Typography minimum readability thresholds for in-car glanceability
        val textHero = parseNumericValue(dimens["car_text_hero"] ?: "0")
        assertTrue("car_text_hero ($textHero sp) must be >= 24sp for glanceability at distance", textHero >= 24.0)

        val textTitle = parseNumericValue(dimens["car_text_title"] ?: "0")
        assertTrue("car_text_title ($textTitle sp) must be >= 20sp for clear readability", textTitle >= 20.0)

        val textBody = parseNumericValue(dimens["car_text_body"] ?: "0")
        assertTrue("car_text_body ($textBody sp) must be >= 14sp", textBody >= 14.0)
    }

    @Test
    fun testValuesDimensDefaultTokensExist() {
        val file = findResFile("values/dimens.xml")
        assertTrue("values/dimens.xml should also exist as base fallback", file.exists() && file.isFile)

        val doc = parseXml(file)
        val dimens = extractDimens(doc)
        assertTrue(dimens.containsKey("car_button_min_height"))
        assertTrue(dimens.containsKey("car_hero_button_height"))
    }
}
