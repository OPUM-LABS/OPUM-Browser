package com.mycompany.app.web

import android.content.Context
import android.webkit.WebView
import net.dankito.readability4j.Article
import org.json.JSONObject
import org.json.JSONTokener
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Fast reader-mode extraction (2026-11-21). Before, the reader copied the whole page (Wikipedia: ~1.9 million
 * characters) through the JavaScript bridge into the app, parsed it with Jsoup and scored it with Readability4J, which
 * took 5–16 s on the emulator. Now Mozilla's Readability.js (assets/reader, Apache 2.0, the original Readability4J was
 * ported from) runs inside the page's own WebView on a copy of the live document, and only the article comes back:
 * its HTML, title, byline, excerpt, language, plus the page's `<head>` meta and link tags (og:image, theme-color, …),
 * from which [Result.document] builds a small document the rest of the reader code reads as before. When the script
 * finds no article (or fails), the callers fall back to the old way. YouTube watch pages always use the old way (their
 * reader reads the page's scripts).
 */
object ReaderExtract {
    /** What Readability.js found. */
    class Result(val title: String?, val byline: String?, val excerpt: String?, val dir: String?, val lang: String?, val content: String, val head: String) {
        /** The article as a small document: the page's head tags and the article in the body. */
        fun document(pageUrl: String): Document {
            val langAttr = if (lang.isNullOrBlank()) "" else " lang=\"" + lang.replace("\"", "") + "\""
            return Jsoup.parse("<html$langAttr><head>$head</head><body>$content</body></html>", pageUrl)
        }

        /** The article for the reader code (which reads [Article.articleContent] and [Article.title]). */
        fun article(pageUrl: String, document: Document): Article {
            val article = Article(pageUrl)
            article.title = title
            article.byline = byline
            article.excerpt = excerpt
            article.dir = dir
            article.articleContent = document.body().children().firstOrNull() ?: document.body()
            return article
        }
    }

    @Volatile
    private var source: String? = null

    /** The script: Readability.js and the call, wrapped so nothing leaks into the page's globals. */
    fun script(context: Context): String? {
        source?.let { return it }
        val lib = try {
            context.assets.open("reader/Readability.js").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            return null
        }
        val js = "(function(){var module=undefined;\n" + lib + "\n" + RUNNER + "})()"
        source = js
        return js
    }

    /**
     * Runs on a copy of the document: the article as JSON, or null. The head tags are taken from the live document
     * (only meta / link / title: no scripts or styles, so the result stays small).
     */
    private const val RUNNER = """
try {
  var doc = document.cloneNode(true);
  // folded sections (mobile Wikipedia: every section after the intro) are text the page shows on a tap or a find;
  // Readability drops anything marked hidden, so unfold them in the copy
  var folded = doc.querySelectorAll('[hidden="until-found"]');
  for (var f = 0; f < folded.length; f++) folded[f].removeAttribute('hidden');
  var a = new Readability(doc, {charThreshold: 500, keepClasses: false}).parse();
  if (!a || !a.content) return null;
  var head = '';
  var tags = document.querySelectorAll('head meta, head link[rel~="icon"], head link[rel="canonical"], head title');
  for (var i = 0; i < tags.length; i++) head += tags[i].outerHTML;
  return JSON.stringify({title: a.title, byline: a.byline, excerpt: a.excerpt, dir: a.dir,
    lang: a.lang || document.documentElement.lang || '', content: a.content, head: head});
} catch (e) { return null; }
"""

    /** Whether the fast way applies to [pageUrl] (not on YouTube watch pages, whose reader reads the page's scripts). */
    fun applies(pageUrl: String?): Boolean =
        !pageUrl.isNullOrEmpty() && !com.mycompany.app.main.MainUtil.isYouTubeUrl(pageUrl)

    /**
     * Extracts the article of the page in [webView]; [done] gets the result, or null when there is none (then the
     * caller takes the old way). Main thread.
     */
    fun run(webView: WebView?, pageUrl: String?, done: (Result?) -> Unit) {
        val js = if (webView != null && applies(pageUrl)) script(webView.context) else null
        if (webView == null || js == null) { done(null); return }
        try {
            webView.evaluateJavascript(js) { value -> done(parse(value)) }
        } catch (_: Exception) {
            done(null)
        }
    }

    /** The value evaluateJavascript hands back (a JSON string literal holding the JSON object), or null. */
    fun parse(value: String?): Result? {
        if (value.isNullOrEmpty() || value == "null") return null
        return try {
            val text = JSONTokener(value).nextValue() as? String ?: return null
            val json = JSONObject(text)
            fun str(key: String): String? = if (json.isNull(key)) null else json.optString(key).ifBlank { null }
            val content = str("content") ?: return null
            Result(str("title"), str("byline"), str("excerpt"), str("dir"), str("lang"), content, str("head") ?: "")
        } catch (_: Exception) {
            null
        }
    }
}
