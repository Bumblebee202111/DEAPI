package com.github.bumblebee202111.deapi.gzip

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream

internal object GzipUtils {
    /**
     * Decompresses a Gzipped byte array.
     * @throws java.util.zip.ZipException if the data is not in GZIP format.
     */
    fun decompress(data: ByteArray): ByteArray {
        if (data.isEmpty()) return ByteArray(0)
        ByteArrayInputStream(data).use { bis ->
            GZIPInputStream(bis).use { gzis ->
                ByteArrayOutputStream().use { bos ->
                    gzis.copyTo(bos)
                    return bos.toByteArray()
                }
            }
        }
    }
}