package com.mycompany.app.web.filter

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * The JNI facade against the host build of `libopumadblock` (run `native/adblock-jni/build.sh` first; without the
 * library every test is skipped, so Gradle builds without Rust still pass).
 */
class UboEngineTest {
    private val rules = """
        ! Title: Test list
        ! Homepage: https://example.org/list
        ! Expires: 2 days
        ||ads.example.com^
        @@||ads.example.com/allowed/*
        /banner/*${'$'}image
        ||cdn.example.org/ad.js${'$'}script,redirect=noopjs
        example.com##.sponsored
        ##.ad-banner
        ###ad_top
        ||example.com^${'$'}removeparam=utm_source
    """.trimIndent()

    private var engine: UboEngine? = null

    @Before
    fun setUp() {
        assumeTrue("libopumadblock host build missing", UboEngine.loadLibrary())
        engine = UboEngine.create(listOf(rules, "news.test##.promo"))
        assertNotNull(engine)
    }

    @After
    fun tearDown() {
        engine?.close()
    }

    @Test
    fun networkVerdicts() {
        val e = engine!!
        assertEquals(UboEngine.Verdict.Block, e.check("https://ads.example.com/x.js", "https://news.test/", "script"))
        assertEquals(UboEngine.Verdict.Allow, e.check("https://ads.example.com/allowed/x.js", "https://news.test/", "script"))
        assertEquals(UboEngine.Verdict.Block, e.check("https://img.test/banner/1.png", "https://news.test/", "image"))
        assertEquals(UboEngine.Verdict.Allow, e.check("https://img.test/banner/1.png", "https://news.test/", "script"))
        assertEquals(
            UboEngine.Verdict.Rewrite("https://www.example.com/p?id=1"),
            e.check("https://www.example.com/p?utm_source=x&id=1", "https://www.example.com/", "xmlhttprequest"),
        )
        assertEquals(UboEngine.Verdict.Allow, e.check("not a url", "", ""))
    }

    @Test
    fun redirectResource() {
        val e = engine!!
        val json = """[{"name":"noopjs","aliases":["noop.js"],"kind":{"mime":"application/javascript"},"content":"KGZ1bmN0aW9uKCl7fSkoKTs="}]"""
        assertEquals(1, e.useResources(json))
        val v = e.check("https://cdn.example.org/ad.js", "https://news.test/", "script")
        assertTrue(v.toString(), v is UboEngine.Verdict.Redirect && v.dataUrl.startsWith("data:application/javascript"))
        assertEquals(-1, e.useResources("{nope"))
    }

    @Test
    fun cosmeticAndGeneric() {
        val e = engine!!
        val c = e.cosmetic("https://www.example.com/a")!!
        assertTrue(c.hideSelectors.toString(), ".sponsored" in c.hideSelectors)
        assertTrue(!c.genericHide)
        assertTrue(".promo" in e.cosmetic("https://news.test/")!!.hideSelectors)
        val generic = e.classIdSelectors(listOf("ad-banner", "content"), listOf("ad_top"), c.exceptions)
        assertTrue(generic.toString(), ".ad-banner" in generic && "#ad_top" in generic)
    }

    @Test
    fun serializeRoundTrip() {
        val data = engine!!.serialize()!!
        UboEngine.deserialize(data)!!.use { e2 ->
            assertEquals(UboEngine.Verdict.Block, e2.check("https://ads.example.com/x.js", "https://news.test/", "script"))
        }
        assertNull(UboEngine.deserialize(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun closedEngineAllows() {
        val e = engine!!
        e.close()
        assertTrue(e.isClosed)
        assertEquals(UboEngine.Verdict.Allow, e.check("https://ads.example.com/x.js", "https://news.test/", "script"))
        assertNull(e.cosmetic("https://www.example.com/"))
        e.close() // twice is fine
    }

    @Test
    fun builtInUboResources() {
        // The asset tools/ubo-resources/build.py makes from uBlock Origin (unit tests run in the app module dir).
        val json = java.io.File("src/main/assets/filter/ubo-resources.json").readText()
        val list = "example.com##+js(set, foo, true)\nexample.com##+js(json-prune, ads)\n" +
            "example.com##+js(trusted-set, bar, 1)\n||ads.example.com/a.js${'$'}script,redirect=noopjs"
        UboEngine.create(listOf(list), listOf(false))!!.use { e ->
            assertTrue(e.useResources(json) > 150)
            val script = e.cosmetic("https://example.com/")!!.script
            assertTrue(script.length.toString(), script.contains("function setConstant") && script.contains("function jsonPrune"))
            assertTrue("untrusted list must not run trusted-set", !script.contains("trustedSetConstant("))
            val v = e.check("https://ads.example.com/a.js", "https://example.com/", "script")
            assertTrue(v.toString(), v is UboEngine.Verdict.Redirect)
        }
        UboEngine.create(listOf(list), listOf(true))!!.use { e ->
            e.useResources(json)
            assertTrue(e.cosmetic("https://example.com/")!!.script.contains("trustedSetConstant("))
        }
    }

    @Test
    fun trackingParametersOnPageLoads() {
        val list = "\$removeparam=fbclid\n||news.example^\$removeparam=utm_source\n"
        UboEngine.create(listOf(list))!!.use { e ->
            assertEquals(
                UboEngine.Verdict.Rewrite("https://news.example/a?id=1"),
                e.check("https://news.example/a?utm_source=mail&id=1", "https://news.example/a?utm_source=mail&id=1", "document"),
            )
            assertEquals(
                UboEngine.Verdict.Rewrite("https://any.example/p"),
                e.check("https://any.example/p?fbclid=abc", "https://any.example/p?fbclid=abc", "document"),
            )
            // adblock-rust ignores $removeparam exceptions; KeepParamRules + a second engine handle them (below)
            assertEquals(UboEngine.Verdict.Allow, e.check("https://clean.example/?id=1", "https://clean.example/?id=1", "document"))
        }
    }

    @Test
    fun removeparamExceptionsViaKeepRules() {
        val list = "\$removeparam=fbclid\n@@||web.archive.org/*/http\$removeparam\n@@||safe.example^\$removeparam=fbclid,domain=safe.example\n"
        val (keepText, count) = KeepParamRules.extract(list)
        assertEquals(2, count)
        UboEngine.create(listOf(list))!!.use { e ->
            UboEngine.create(listOf(keepText))!!.use { keep ->
                val archived = "https://web.archive.org/web/2020/https://shop.example/?fbclid=1"
                assertTrue(e.check(archived, archived, "document") is UboEngine.Verdict.Rewrite) // engine alone strips it
                assertTrue("exception matches", keep.check(archived, archived, "other").blocks)
                val other = "https://news.example/?fbclid=1"
                assertFalse(keep.check(other, other, "other").blocks)
            }
        }
        assertEquals("||web.archive.org/*/http", KeepParamRules.toKeepRule("@@||web.archive.org/*/http\$removeparam"))
        assertEquals("||a.example^\$domain=b.example", KeepParamRules.toKeepRule("@@||a.example^\$removeparam=x,doc,domain=b.example"))
        assertNull(KeepParamRules.toKeepRule("@@\$removeparam"))
        assertNull(KeepParamRules.toKeepRule("||a.example^\$removeparam=x"))
    }

    @Test
    fun uboAndBraveResourcesTogether() {
        val parts = listOf("src/main/assets/filter/ubo-resources.json", "src/main/assets/filter/brave-resources.json")
            .map { java.io.File(it).readText().trim().removePrefix("[").removeSuffix("]") }
        val json = parts.joinToString(",", "[", "]")
        val list = "example.com##+js(brave-fix)\nexample.com##+js(set, foo, true)\n"
        UboEngine.create(listOf(list), listOf(true))!!.use { e ->
            assertTrue(e.useResources(json) >= 201 + 25)
            val script = e.cosmetic("https://example.com/")!!.script
            assertTrue("uBO scriptlet", script.contains("function setConstant"))
            assertTrue("Brave scriptlet resolved: " + script.length, script.length > 2000 && script.contains("brave", ignoreCase = true))
        }
    }

    @Test
    fun opumFixesAllParse() {
        val text = java.io.File("src/main/assets/filter/opum-fixes.txt").readText()
        val st = UboEngine.listStats(text, 10)!!
        assertEquals(st.rejectedSamples.joinToString("\n"), 0, st.rejected)
        assertTrue(st.rules > 120)
    }

    @Test
    fun listStats() {
        val st = UboEngine.listStats("! c\n||a.example^\nexample.com##.ad\n||x.com^${'$'}unknownoption123\n\n", 5)!!
        assertEquals(2, st.rules)
        assertEquals(1, st.rejected)
        assertTrue(st.rejectedSamples.single().startsWith("||x.com^"))
    }

    @Test
    fun validateMetadataVersion() {
        assertNull(UboEngine.validate("||ads.example.com^"))
        assertNull(UboEngine.validate("example.com##.ad"))
        assertNotNull(UboEngine.validate("||x.com^${'$'}unknownoption123"))
        val m = UboEngine.listMetadata(rules)!!
        assertEquals("Test list", m.title)
        assertEquals("https://example.org/list", m.homepage)
        assertEquals(48, m.expiresHours)
        assertTrue(UboEngine.version()!!.startsWith("opumadblock"))
    }
}
