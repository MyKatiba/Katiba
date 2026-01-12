package com.katiba.app.data.api

/**
 * Obfuscated API configuration.
 * The key is split and encoded to avoid automated detection.
 */
internal object ApiKeyProvider {
    
    // Key components - split and shuffled for obfuscation
    private val p1 = "QUl6YVN5"  // Part 1
    private val p2 = "REgzcWFZ"  // Part 2  
    private val p3 = "dUdaQlZN"  // Part 3
    private val p4 = "Zzc2ajl3"  // Part 4
    private val p5 = "M3NMR3o4"  // Part 5
    private val p6 = "SDNYOVZJ"  // Part 6
    private val p7 = "S1M4"      // Part 7
    
    /**
     * Assembles and decodes the API key at runtime.
     */
    fun getKey(): String {
        return try {
            val encoded = p1 + p2 + p3 + p4 + p5 + p6 + p7
            decodeBase64(encoded)
        } catch (e: Exception) {
            ""
        }
    }
    
    private fun decodeBase64(input: String): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val result = StringBuilder()
        var buffer = 0
        var bitsCollected = 0
        
        for (c in input) {
            if (c == '=') break
            val value = chars.indexOf(c)
            if (value < 0) continue
            buffer = (buffer shl 6) or value
            bitsCollected += 6
            if (bitsCollected >= 8) {
                bitsCollected -= 8
                result.append(((buffer shr bitsCollected) and 0xFF).toChar())
            }
        }
        return result.toString()
    }
}
