package com.mycompany.app.web

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookAddr
import com.mycompany.app.db.book.SavedAddress
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.ui.browser.ComposeOverlay
import org.json.JSONObject

/**
 * Saved addresses on web pages (2026-11-28). Part of OPUM's own manager: off with another app ([PassManager]), which
 * fills addresses through Android's autofill.
 *
 * - Filling, only when the user picks one: a page with an address form (at least three fields, two of them parts of
 *   an address: street, postcode, city, region, country) gets the bar "Fill in a saved address?", when it has
 *   loaded or when the user taps into one of its address fields.
 * - Saving: when such a form is sent (submit, Enter, a button), the page hands over what was typed and the bar asks
 *   "Save this address?"; one that is already saved is not offered, a changed one is offered as an update.
 *
 * Fields are recognised by their `autocomplete` names first, else by their name, id, placeholder and label in
 * several languages. Login, search, card and code fields are never taken.
 */
object AddressManager {
    val on: Boolean get() = PassManager.builtIn && ZtriSettings.addrSave.value
}

/** Field recognition shared by the scripts below: `kind(element)` and `scan()` (kind -> first such field). */
private const val ADDR_LIB = """
var OA_SKIP=/user.?name|login|passw|search|captcha|coupon|promo|voucher|gift|card.?(num|holder|code)|cc-?(num|csc|exp|name)|cvv|cvc|iban|otp|verif/;
var OA_AUTO={'name':'name','given-name':'given','family-name':'family','organization':'org','street-address':'street','address-line1':'street','address-line2':'line2','address-line3':'line2','postal-code':'postcode','address-level2':'city','address-level1':'region','country':'cc','country-name':'country','tel':'tel','tel-national':'tel','email':'email'};
var OA_RULES=[
['email',/e.?mail|courriel|correo/],
['tel',/phone|tel(e|_|-|\b)|mobile|handy|cell|telefon/],
['postcode',/zip|postal|post.?code|postcode|plz|postleitzahl|code.?postal|cod.?postal|c[oó]digo.?postal|\bcap\b|\bcp\b|pin.?code|郵便|우편|邮编|индекс/],
['city',/city|town|\bort\b|stadt|wohnort|ville|ciudad|citt[aà]|cidade|locali|municip|gemeinde|miasto|город|市区町村|城市/],
['region',/state|province|region|county|bundesland|kanton|provincia|estado|d[eé]partement|prefecture|wojew|област|省/],
['country',/country|\bland\b|pays|pa[ií]s|nazione|страна|国/],
['house',/house.?(no|num)|hausn|haus.?nr|street.?(no|num)|building.?num|n[uú]mero|\bnr\b/],
['line2',/address.?(line)?.?2|addr.?2|line.?2|adresszusatz|zusatz|compl[eé]ment|apartment|\bapt\b|suite|etage|floor/],
['street',/street|address|addr|stra(ss|ß)e|rue|calle|\bvia\b|rua|adresse|direcci|indirizzo|endere|ulica|улиц|住所|주소|地址/],
['org',/company|organi[sz]ation|firma|unternehmen|entreprise|soci[eé]t[eé]|empresa|azienda/],
['given',/first.?name|given|fname|vorname|pr[eé]nom|\bnombre\b|\bnome\b|imi[eę]|имя/],
['family',/last.?name|family|surname|lname|nachname|\bnom\b|apellido|cognome|sobrenome|nazwisko|фамили/],
['name',/full.?name|your.?name|\bname\b|contact.?name|recipient|empf[aä]nger|destinataire/]];
function kind(e){
var t=(e&&e.tagName||'').toLowerCase();if(t!=='input'&&t!=='select'&&t!=='textarea')return null;
var ty=(e.type||'').toLowerCase();if(t==='input'&&!/^(text|email|tel)?$/.test(ty))return null;
if(e.disabled||e.readOnly||e.offsetParent===null)return null;
var ac=(e.getAttribute('autocomplete')||'').toLowerCase().split(/\s+/).filter(function(w){return w&&!/^(section-|shipping|billing|home|work|mobile|fax|pager|on$|off$)/.test(w);}).pop()||'';
if(OA_AUTO[ac])return OA_AUTO[ac];
if(/^cc-|username|password|one-time|webauthn/.test(ac))return null;
var lab='';try{if(e.id){var l=document.querySelector('label[for="'+CSS.escape(e.id)+'"]');if(l)lab=l.textContent;}if(!lab&&e.closest){var p=e.closest('label');if(p)lab=p.textContent;}}catch(x){}
var k=[e.name,e.id,e.getAttribute('placeholder'),e.getAttribute('aria-label'),lab].join(' ').toLowerCase();
if(OA_SKIP.test(k))return null;
if(ty==='email')return 'email';if(ty==='tel')return 'tel';
for(var i=0;i<OA_RULES.length;i++){if(OA_RULES[i][1].test(k))return OA_RULES[i][0];}
return null;}
function scan(){var m={};var a=document.querySelectorAll('input,select,textarea');for(var i=0;i<a.length;i++){var k=kind(a[i]);if(k&&!m[k])m[k]=a[i];}return m;}
function isAddr(m){var p=['street','postcode','city','region'].filter(function(k){return m[k];}).length+((m.country||m.cc)?1:0);return p>=2&&Object.keys(m).length>=3;}
"""

/** "addr" when the page shows an address form, else "none". */
private const val ADDR_KIND_JS = "(function(){$ADDR_LIB return isAddr(scan())?'addr':'none';})()"

/**
 * Added once per page: when a form is sent, the address typed into it goes to the app (`android.onAddrResult`, the
 * keys of [SavedAddress.fields]); a tap into an address field asks for the fill bar once (`android.onAddrFocus`).
 */
private const val ADDR_WATCH_JS = "(function(){if(window.__opumAddrWatch)return;window.__opumAddrWatch=1;" + ADDR_LIB + """
function val(e){if(!e)return '';if(e.tagName.toLowerCase()==='select'){var o=e.options[e.selectedIndex];return o&&o.value?(o.text||'').trim():'';}return (e.value||'').trim();}
function code(e){if(!e||e.tagName.toLowerCase()!=='select')return '';var v=(e.value||'').trim();return /^[a-zA-Z]{2}$/.test(v)?v.toUpperCase():'';}
function grab(){var m=scan();if(!isAddr(m))return;var r={};
r.name=val(m.name)||[val(m.given),val(m.family)].filter(Boolean).join(' ');r.org=val(m.org);
var st=val(m.street).split(/\s*\n\s*/);r.street=[st[0],val(m.house)].filter(Boolean).join(' ');r.line2=val(m.line2)||st.slice(1).join(', ');
r.postcode=val(m.postcode);r.city=val(m.city);r.region=val(m.region);
var ce=m.country||m.cc;var cv=val(ce);r.cc=code(ce);if(!r.cc&&/^[a-zA-Z]{2}$/.test(cv))r.cc=cv.toUpperCase();r.country=(cv.length===2&&r.cc)?'':cv;
r.tel=val(m.tel);r.email=val(m.email);
if(['street','postcode','city','region'].filter(function(k){return r[k];}).length+((r.country||r.cc)?1:0)<2)return;
var s=JSON.stringify(r);if(s===window.__opumAddrLast)return;window.__opumAddrLast=s;android.onAddrResult(s);}
var go=function(){try{grab();}catch(x){}};
document.addEventListener('submit',go,true);
document.addEventListener('keyup',function(e){if(e.keyCode==13)go();},true);
document.addEventListener('click',function(e){var b=e.target&&e.target.closest&&e.target.closest('button,input[type=submit],input[type=image],[role=button]');if(b)go();},true);
document.addEventListener('focusin',function(e){if(window.__opumAddrAsked)return;try{var k=kind(e.target);if(k&&/^(street|house|line2|postcode|city|region|country|cc)$/.test(k)&&isAddr(scan())){window.__opumAddrAsked=1;android.onAddrFocus();}}catch(x){}},true);
})();"""

/** Fills address [d] (the keys of [SavedAddress.fields] plus streetOnly, house, given, family, cnames) into the page. */
private const val ADDR_FILL_FN = ADDR_LIB + """
function put(e,v){if(!e||v==null||v==='')return false;
if(e.tagName.toLowerCase()==='select'){var o=e.options,lv=String(v).trim().toLowerCase(),hit=-1,i;
for(i=0;i<o.length;i++){if((o[i].value||'').trim().toLowerCase()===lv||(o[i].text||'').trim().toLowerCase()===lv){hit=i;break;}}
if(hit<0&&lv.length>2)for(i=0;i<o.length;i++){if((o[i].text||'').trim().toLowerCase().indexOf(lv)===0){hit=i;break;}}
if(hit<0)return false;e.selectedIndex=hit;}
else{var pr=e.tagName.toLowerCase()==='textarea'?HTMLTextAreaElement.prototype:HTMLInputElement.prototype;Object.getOwnPropertyDescriptor(pr,'value').set.call(e,v);}
e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));return true;}
function fill(d){var m=scan();
if(m.name)put(m.name,d.name);if(m.given)put(m.given,d.given||d.name);if(m.family)put(m.family,d.family);
put(m.org,d.org);
if(m.house){put(m.street,d.streetOnly||d.street);put(m.house,d.house);}else put(m.street,d.street);
put(m.line2,d.line2);put(m.postcode,d.postcode);put(m.city,d.city);put(m.region,d.region);
var cs=[d.country,d.cc].concat(d.cnames||[]);
[m.country,m.cc].forEach(function(e){if(e)for(var i=0;i<cs.length;i++){if(cs[i]&&put(e,cs[i]))break;}});
put(m.tel,d.tel);put(m.email,d.email);}
"""

/** Languages whose country names a shop's country list may use (the app's languages and a few more). */
private val COUNTRY_LANGS = listOf("en", "de", "fr", "it", "es", "pt", "nl", "pl", "hu", "ru", "uk", "tr", "ar", "hi", "in", "th", "vi", "ja", "ko", "zh")

/** The two-letter code of [address]'s country: as saved, else found by its name in one of [COUNTRY_LANGS]. */
private fun countryCodeOf(address: SavedAddress): String {
    if (address.countryCode.isNotEmpty()) return address.countryCode
    val name = address.country.trim().lowercase()
    if (name.isEmpty()) return ""
    val langs = COUNTRY_LANGS.map { java.util.Locale(it) } + java.util.Locale.getDefault()
    return java.util.Locale.getISOCountries().firstOrNull { cc ->
        val country = java.util.Locale("", cc)
        langs.any { country.getDisplayCountry(it).lowercase() == name }
    } ?: ""
}

/**
 * Other ways a country list may name [code]: its name in [COUNTRY_LANGS] and the phone's language, and its
 * three-letter code (a German shop says "Schweiz", an English one "Switzerland", another "CHE").
 */
private fun countryNames(code: String): List<String> {
    if (code.length != 2) return emptyList()
    val country = java.util.Locale("", code)
    val names = (COUNTRY_LANGS.map { java.util.Locale(it) } + java.util.Locale.getDefault()).map { country.getDisplayCountry(it) }
    val three = try { country.isO3Country } catch (_: Exception) { "" }
    return (names + three).filter { it.isNotBlank() && it != code }.distinct()
}

/** Fills [address] into the page's address form, the way typing does. */
fun WebViewActivity.fillAddress(address: SavedAddress) {
    val (streetOnly, house) = SavedAddress.splitStreet(address.street)
    val (given, family) = SavedAddress.splitName(address.name)
    val code = countryCodeOf(address)
    val data = JSONObject(address.fields())
        .put("streetOnly", streetOnly).put("house", house).put("given", given).put("family", family)
        .put("cc", code).put("cnames", org.json.JSONArray(countryNames(code)))
    evalJs(currentPage, "(function(){$ADDR_FILL_FN fill($data);})();", true)
    val context = applicationContext
    runInBackground(Runnable { DbBookAddr.touch(context, address.id) })
}

/** After a page has loaded: watches its forms for a sent address, and offers the fill bar on an address form. */
fun WebViewActivity.checkAddressForm() {
    if (!AddressManager.on || onStartPage) return
    val page = currentPage ?: return
    evalJs(page, ADDR_WATCH_JS, true)
    offerAddressFill()
}

/** The fill bar, once per page, when the page shows an address form and addresses are saved. */
fun WebViewActivity.offerAddressFill() {
    if (!AddressManager.on) return
    val page = currentPage ?: return
    val url = dialogsState.pageUrl ?: return
    if (floatingState.addrOfferedUrl == url || floatingState.loginFillBar != null) return
    val context = applicationContext
    page.evalJsRaw(ADDR_KIND_JS) { result ->
        if (result != "\"addr\"" || currentPage !== page) return@evalJsRaw
        runInBackground(Runnable {
            val saved = DbBookAddr.all(context)
            if (saved.isEmpty()) return@Runnable
            runOnUiThread {
                if (currentPage !== page || dialogsState.pageUrl != url || floatingState.loginFillBar != null) return@runOnUiThread
                floatingState.addrOfferedUrl = url
                showFillBar(AddressFillBar(this, saved))
            }
        })
    }
}

/**
 * A form with an address was sent ([json] from the page): "Save this address?", or "Update the saved address?" when
 * it changes one that is saved. Nothing when the address is saved already.
 */
fun WebViewActivity.offerAddressSave(json: String?) {
    if (!AddressManager.on) return
    val found = DbBookAddr.fromJson(0, json) ?: return
    if (found.addressParts < 2) return
    val context = applicationContext
    runInBackground(Runnable {
        val saved = DbBookAddr.all(context)
        if (saved.any { found.coveredBy(it) }) return@Runnable
        val same = saved.firstOrNull { found.samePlace(it) }
        runOnUiThread { if (!isClosing) showFillBar(AddressSaveBar(this, found, same)) }
    })
}

/** Shows [bar] at the bottom of the page, in place of any fill bar shown before. */
internal fun WebViewActivity.showFillBar(bar: ComposeOverlay) {
    closeLoginFillBar()
    val overlays = floatingState.overlays ?: return
    floatingState.loginFillBar = bar
    val decorHeight = window?.decorView?.height ?: 0
    val params = com.mycompany.app.ui.browser.OverlayParams(-1, -2)
    params.gravity = android.view.Gravity.BOTTOM
    params.bottomMargin = (decorHeight - pageVisibleBottom().coerceAtMost(decorHeight)).coerceAtLeast(0) + com.mycompany.app.main.MainApp.dp8
    try { overlays.addCoordView(bar, params) } catch (_: Exception) { floatingState.loginFillBar = null }
}

/** The bar's frame: an icon, [content], and a close button; [below] opens under it. */
@Composable
private fun BarFrame(onClose: () -> Unit, below: @Composable () -> Unit = {}, content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.outline_home_black_24), null, tint = MaterialTheme.colorScheme.primary)
                content()
                IconButton(onClick = onClose) {
                    Icon(com.mycompany.app.ui.components.Sym.Close, stringResource(R.string.close))
                }
            }
            below()
        }
    }
}

@Composable
private fun BarText(title: String, line: String?, modifier: Modifier) {
    Column(modifier.padding(start = 12.dp)) {
        Text(title, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        if (!line.isNullOrBlank()) {
            Text(line, style = MaterialTheme.typography.labelMedium, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** "Fill in a saved address?": Fill fills the one address, or opens the list of several. Filling closes the bar. */
@android.annotation.SuppressLint("ViewConstructor")
class AddressFillBar(private val host: WebViewActivity, private val saved: List<SavedAddress>) : ComposeOverlay(host) {
    init {
        setPage({ com.mycompany.app.ui.state.AppState.darkUi }) { Bar() }
    }

    private fun fill(address: SavedAddress) {
        host.closeLoginFillBar()
        host.fillAddress(address)
    }

    @Composable
    private fun Bar() {
        var open by remember { mutableStateOf(false) }
        BarFrame(onClose = { host.closeLoginFillBar() }, below = {
            if (open) {
                for (address in saved) {
                    Column(Modifier.fillMaxWidth().clickable { fill(address) }.padding(start = 52.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
                        Text(address.title(), style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                        Text(address.summary(), style = MaterialTheme.typography.bodySmall, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }) {
            BarText(stringResource(R.string.addr_fill_title), if (saved.size == 1) saved[0].summary() else null, Modifier.weight(1f))
            TextButton(onClick = { if (saved.size == 1) fill(saved[0]) else open = !open }) {
                Text(stringResource(R.string.fill_login))
            }
        }
    }
}

/**
 * "Save this address?" (or "Update the saved address?" when it changes [same]). It stays over the page the form
 * leads to ([survive]: the next page start does not close it), then goes with the page after that.
 */
@android.annotation.SuppressLint("ViewConstructor")
class AddressSaveBar(private val host: WebViewActivity, private val found: SavedAddress, private val same: SavedAddress?) : ComposeOverlay(host) {
    var survive = 1

    init {
        setPage({ com.mycompany.app.ui.state.AppState.darkUi }) { Bar() }
    }

    private fun save() {
        host.closeLoginFillBar()
        val context = host.applicationContext
        val address = if (same != null) found.mergedInto(same) else found
        host.runInBackground(Runnable {
            val ok = DbBookAddr.save(context, address) > 0
            host.runOnUiThread { MainUtil.showToast(context, if (ok) R.string.save_success else R.string.fail) }
        })
    }

    @Composable
    private fun Bar() {
        BarFrame(onClose = { host.closeLoginFillBar() }) {
            BarText(
                stringResource(if (same != null) R.string.addr_update_title else R.string.addr_save_title),
                found.summary(), Modifier.weight(1f),
            )
            TextButton(onClick = { save() }) {
                Text(stringResource(if (same != null) R.string.pass_update_button else R.string.save))
            }
        }
    }
}
