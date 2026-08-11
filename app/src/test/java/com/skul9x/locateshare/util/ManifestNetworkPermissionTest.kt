package com.skul9x.locateshare.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class ManifestNetworkPermissionTest {

    @Test
    fun testManifestContainsAccessNetworkStatePermission() {
        val file1 = File("src/main/AndroidManifest.xml")
        val file2 = File("app/src/main/AndroidManifest.xml")
        val manifestFile = if (file1.exists()) file1 else file2

        assertTrue("AndroidManifest.xml should exist", manifestFile.exists())

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(manifestFile)
        doc.documentElement.normalize()

        val usesPermissions = doc.getElementsByTagName("uses-permission")
        var hasAccessNetworkState = false
        var hasInternet = false

        for (i in 0 until usesPermissions.length) {
            val item = usesPermissions.item(i)
            val name = item.attributes.getNamedItem("android:name")?.nodeValue
            if (name == "android.permission.ACCESS_NETWORK_STATE") {
                hasAccessNetworkState = true
            }
            if (name == "android.permission.INTERNET") {
                hasInternet = true
            }
        }

        assertTrue("AndroidManifest.xml must declare android.permission.ACCESS_NETWORK_STATE", hasAccessNetworkState)
        assertTrue("AndroidManifest.xml must declare android.permission.INTERNET", hasInternet)
    }
}
