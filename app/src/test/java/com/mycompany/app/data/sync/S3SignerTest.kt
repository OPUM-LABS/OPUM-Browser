package com.mycompany.app.data.sync

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class S3SignerTest {

    /** The "GET Object" example of Amazon's Signature Version 4 documentation. */
    @Test
    fun matchesAmazonsExample() {
        val date = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse("20130524T000000Z")!!
        val s = S3Signer.sign(
            method = "GET",
            host = "examplebucket.s3.amazonaws.com",
            path = "/test.txt",
            query = "",
            payloadSha256 = S3Signer.EMPTY_SHA256,
            region = "us-east-1",
            accessKey = "AKIAIOSFODNN7EXAMPLE",
            secretKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
            now = date,
            extraHeaders = mapOf("range" to "bytes=0-9"),
        )
        assertEquals(
            "AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request," +
                "SignedHeaders=host;range;x-amz-content-sha256;x-amz-date," +
                "Signature=f0e8bdb87c964420e857bd35b5d6ed310bd44f0170aba48dd91039c6036bdb41",
            s.authorization,
        )
    }

    @Test
    fun encodesSegments() {
        assertEquals("my%20file%2B1.xbel", S3Signer.encodeSegment("my file+1.xbel"))
        assertEquals("a-b_c.d~e", S3Signer.encodeSegment("a-b_c.d~e"))
    }

    @Test
    fun regionFromHost() {
        assertEquals("eu-central-1", S3Transport.regionOf("s3.eu-central-1.amazonaws.com"))
        assertEquals("eu-central-003", S3Transport.regionOf("s3.eu-central-003.backblazeb2.com"))
        assertEquals("auto", S3Transport.regionOf("abc.r2.cloudflarestorage.com"))
        assertEquals("us-east-1", S3Transport.regionOf("minio.local"))
    }
}
