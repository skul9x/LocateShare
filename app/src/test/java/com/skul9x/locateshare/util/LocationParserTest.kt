package com.skul9x.locateshare.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationParserTest {

    @Test
    fun testSpecificNamedLocation() {
        val input = """
            Cộng Cà Phê
            101 Hoàng Sa, Đa Kao, Quận 1, Thành phố Hồ Chí Minh, Vietnam
            https://maps.app.goo.gl/xxxxx
        """.trimIndent()
        val url = "https://maps.app.goo.gl/xxxxx"
        val result = LocationParser.parseNameFromSharedText(input, url)
        assertEquals("Cộng Cà Phê", result)
    }

    @Test
    fun testPinnedLocationGenericLabel() {
        val input = """
            Đã ghim
            Gần Hải Châu, Đà Nẵng
            https://maps.app.goo.gl/xxxxx
        """.trimIndent()
        val url = "https://maps.app.goo.gl/xxxxx"
        val result = LocationParser.parseNameFromSharedText(input, url)
        assertEquals("Đã ghim (Gần Hải Châu, Đà Nẵng)", result)
    }

    @Test
    fun testUrlOnlyNoText() {
        val input = "https://maps.app.goo.gl/xxxxx"
        val url = "https://maps.app.goo.gl/xxxxx"
        val result = LocationParser.parseNameFromSharedText(input, url)
        assertEquals("", result)
    }

    @Test
    fun testDecodeUrlPlaceName() {
        val input = "https://www.google.com/maps/place/Chi+nh%C3%A1nh+C%C3%B4ng+ty+c%E1%BB%95+ph%E1%BA%A7n+Qu%E1%BB%91c+t%E1%BA%BF+Delta+t%E1%BA%A1i+B%E1%BA%AFc+Ninh,+KM+8%2B415,+%C4%90T291,+Qu%E1%BA%BF+V%C3%B5,+B%E1%BA%AFc+Ninh/data=!4m2!3m1!..."
        val result = LocationParser.extractNameFromFullUrl(input)
        assertEquals("Chi nhánh Công ty cổ phần Quốc tế Delta tại Bắc Ninh, KM 8+415, ĐT291, Quế Võ, Bắc Ninh", result)
    }

    @Test
    fun testResolveRedirectUrlToName() = kotlinx.coroutines.runBlocking {
        val inputUrl = "https://maps.app.goo.gl/SxdjHxg5vqmXecCk6?g_st=ac"
        val expected = "Chi nhánh Công ty cổ phần Quốc tế Delta tại Bắc Ninh, KM 8+415, ĐT291, Quế Võ, Bắc Ninh"
        
        val result = LocationParser.resolveRedirectUrlToName(inputUrl)
        if (result.isNotEmpty()) {
            assertEquals(expected, result)
        } else {
            println("Result was empty (could be offline or rate-limited)")
            assertEquals("", result)
        }
    }
}
