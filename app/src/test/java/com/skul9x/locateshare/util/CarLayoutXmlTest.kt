package com.skul9x.locateshare.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class CarLayoutXmlTest {

    @Test
    fun testCarLayoutXmlContainsWifiButtonAndFitsSystemWindows() {
        val xmlFile = File("src/main/res/layout/activity_car.xml")
        val altFile = File("app/src/main/res/layout/activity_car.xml")
        val targetFile = if (xmlFile.exists()) xmlFile else altFile

        assertTrue("Layout file activity_car.xml should exist", targetFile.exists())

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(targetFile)
        doc.documentElement.normalize()

        val rootElement = doc.documentElement
        val rootId = rootElement.getAttribute("android:id")
        val fitsSystemWindows = rootElement.getAttribute("android:fitsSystemWindows")

        assertTrue("Root element must have android:id='@+id/rootCarLayout'", rootId == "@+id/rootCarLayout")
        assertTrue("Root element must have android:fitsSystemWindows='true'", fitsSystemWindows == "true")

        val imageButtons = doc.getElementsByTagName("ImageButton")
        var foundWifiBtn = false
        var wifiSrcCorrect = false

        for (i in 0 until imageButtons.length) {
            val item = imageButtons.item(i)
            val id = item.attributes.getNamedItem("android:id")?.nodeValue
            if (id == "@+id/btnWifiSettings") {
                foundWifiBtn = true
                val src = item.attributes.getNamedItem("android:src")?.nodeValue
                if (src == "@drawable/ic_wifi") {
                    wifiSrcCorrect = true
                }
            }
        }

        assertTrue("btnWifiSettings ImageButton must be present in layout", foundWifiBtn)
        assertTrue("btnWifiSettings must have android:src='@drawable/ic_wifi'", wifiSrcCorrect)
    }
}
