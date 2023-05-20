package com.riobener.sonicsoul.utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object HashUtils{
    fun hashStringWithSHA256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val encodedHash = digest.digest(input.toByteArray(StandardCharsets.UTF_8))

        // Convert the byte array to a hexadecimal string representation
        val hexString = StringBuilder()
        for (element in encodedHash) {
            val hex = Integer.toHexString(0xff and element.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}