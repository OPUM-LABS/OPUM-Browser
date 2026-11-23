package com.mycompany.app.web

import android.util.Log
import android.webkit.WebView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

/**
 * Web dark mode fix for pages that claim a dark theme they do not have (2026-11-20, owner's report on ankersolix.com).
 *
 * Dark web pages come from the WebView's algorithmic darkening, which Chromium skips on pages that declare dark support
 * (`color-scheme: dark`). Many sites set that declaration as soon as the browser prefers dark (theme scripts such as
 * next-themes put `style="color-scheme: dark"` and `data-theme="dark"` on `<html>`), but their own dark styles are
 * missing or unfinished, so the page shows black while loading and then turns white.
 *
 * This document-start script runs only while the page sees `prefers-color-scheme: dark` (web dark mode on). When the
 * root declares `dark` but the page background is light, it sets `color-scheme: light !important` on `<html>`, so the
 * WebView darkens the page as it does for any light page. Pages with a real dark theme (dark background) are left as
 * they are. It re-checks when the page changes the root's attributes (theme scripts, route changes) and after load.
 *
 * It also lightens dark logos on darkened pages (owner's report: Wikipedia's black wordmark was unreadable), since the
 * WebView's darkening never changes pictures; see [SCRIPT].
 */
object WebDarkFix {

    private const val TAG = "WebDarkFix"

    /**
     * The page script. Part 1: a background counts as light above relative luminance 0.4 (well above dark themes'
     * greys). Part 2 (dark logos): on pages the WebView darkens (no dark claim left), small pictures that are mostly
     * dark on a transparent background (logos, wordmarks, icons; not photos) get `invert(1) hue-rotate(180deg)`:
     * inline `<svg>` drawn only in dark colours (the WebView does not darken SVG paint; SVGs that draw through `<use>`
     * are skipped, their real colours are not visible from here), `<img>`, CSS background
     * pictures of elements without text (a filter would invert their text too) and those of
     * `::before` / `::after`. Pixels are read through a 40x40 canvas;
     * cross-site pictures only when their server allows it (CORS), otherwise they are left alone.
     */
    private const val SCRIPT = """(function(){
if(window.__opumDarkFix)return;window.__opumDarkFix=1;
var mq;try{mq=window.matchMedia('(prefers-color-scheme: dark)');}catch(e){return;}
function parse(c){var m=/rgba?\(([^)]+)\)/.exec(c||'');if(!m)return null;var p=m[1].split(',');
if(p.length>3&&parseFloat(p[3])<0.5)return null;return[parseFloat(p[0]),parseFloat(p[1]),parseFloat(p[2])];}
function lum(c){function f(v){v/=255;return v<=0.03928?v/12.92:Math.pow((v+0.055)/1.055,2.4);}
return 0.2126*f(c[0])+0.7152*f(c[1])+0.0722*f(c[2]);}
function bgOf(el){while(el&&el.nodeType===1){var c=parse(getComputedStyle(el).backgroundColor);if(c)return c;el=el.parentElement;}return null;}
function claimsDark(){var d=document.documentElement;return d&&(getComputedStyle(d).colorScheme||'').indexOf('dark')>=0;}
function check(){try{
if(!mq.matches)return;
var d=document.documentElement,b=document.body;if(!d||!b)return;
if(!claimsDark())return;
var c=parse(getComputedStyle(b).backgroundColor)||parse(getComputedStyle(d).backgroundColor);
if(!c&&window.innerWidth>0){c=bgOf(document.elementFromPoint(window.innerWidth/2,window.innerHeight/2));}
if(c&&lum(c)>0.4){d.style.setProperty('color-scheme','light','important');}
}catch(e){}}
var verdict={};
function analyse(url,done){
if(verdict[url]!==undefined){if(verdict[url]!==null)done(verdict[url]);else (verdict[url+'#w']=verdict[url+'#w']||[]).push(done);return;}
verdict[url]=null;var waiting=verdict[url+'#w']=[done];
function finish(v){verdict[url]=v;for(var i=0;i<waiting.length;i++){try{waiting[i](v);}catch(e){}}}
var img=new Image();
try{if(new URL(url,location.href).origin!==location.origin)img.crossOrigin='anonymous';}catch(e){finish(false);return;}
img.onload=function(){try{
var n=40,cv=document.createElement('canvas');cv.width=n;cv.height=n;var x=cv.getContext('2d');
x.drawImage(img,0,0,n,n);var px=x.getImageData(0,0,n,n).data;
var opaque=0,dark=0,total=n*n;
for(var i=0;i<px.length;i+=4){if(px[i+3]>=128){opaque++;if(lum([px[i],px[i+1],px[i+2]])<0.18)dark++;}}
finish(opaque>0&&opaque<=total*0.8&&dark>=opaque*0.6);
}catch(e){finish(false);}};
img.onerror=function(){finish(false);};
img.src=url;}
var sheet=null,rules={};
function invertPseudo(el,pseudo){
var id=el.getAttribute('data-opum-dk');if(!id){id=String(Math.random()).slice(2,10);el.setAttribute('data-opum-dk',id);}
var key=id+pseudo;if(rules[key])return;rules[key]=1;
if(!sheet){sheet=document.createElement('style');sheet.id='opum_dark_logos';(document.head||document.documentElement).appendChild(sheet);}
sheet.textContent+='[data-opum-dk="'+id+'"]'+pseudo+'{filter:invert(1) hue-rotate(180deg)!important}';}
function urlOf(bi){if(!bi||bi==='none'||bi.indexOf('gradient')>=0)return null;var m=/url\(["']?([^"')]+)["']?\)/.exec(bi);return m?m[1]:null;}
function small(w,h){return w>=8&&h>=8&&w<=600&&h<=250;}
function logos(){try{
if(!mq.matches||claimsDark())return;
var all=document.body?document.body.getElementsByTagName('*'):[];var max=Math.min(all.length,4000);
for(var i=0;i<max;i++){(function(el){
var r=el.getBoundingClientRect();
if(el.tagName==='svg'){
if(el.__opumDk||!small(r.width,r.height)||(el.parentElement&&el.parentElement.closest('svg')))return;el.__opumDk=1;
if(el.querySelector('use'))return;var shapes=el.querySelectorAll('path,rect,circle,ellipse,polygon,polyline,line,text');if(!shapes.length||shapes.length>300)return;
var paints=0,ok=true;
for(var k=0;k<shapes.length&&ok;k++){var sc=getComputedStyle(shapes[k]);[sc.fill,sc.stroke].forEach(function(v){
if(!ok||!v||v==='none')return;if(v.indexOf('url(')>=0){ok=false;return;}var pc=parse(v);if(!pc)return;paints++;if(lum(pc)>=0.18)ok=false;});}
if(ok&&paints>0)el.style.setProperty('filter','invert(1) hue-rotate(180deg)','important');return;}
if(el.tagName==='IMG'){
if(el.__opumDk||!el.complete||!small(r.width,r.height))return;el.__opumDk=1;
var src=el.currentSrc||el.src;if(!src||src.indexOf('data:')===0&&src.length>20000)return;
analyse(src,function(v){if(v)el.style.setProperty('filter','invert(1) hue-rotate(180deg)','important');});return;}
var cs=getComputedStyle(el);
if(!el.__opumDkBg&&small(r.width,r.height)&&!(el.textContent||'').trim()&&!el.querySelector('img,svg,video,canvas,input')){var u=urlOf(cs.backgroundImage);if(u){el.__opumDkBg=1;
analyse(u,function(v){if(v)el.style.setProperty('filter','invert(1) hue-rotate(180deg)','important');});}}
['::before','::after'].forEach(function(ps){
if(el['__opumDk'+ps])return;var pc=getComputedStyle(el,ps);if(!pc||pc.content==='none')return;
var u2=urlOf(pc.backgroundImage)||urlOf(pc.content);if(!u2)return;
if(!small(parseFloat(pc.width)||r.width,parseFloat(pc.height)||r.height))return;el['__opumDk'+ps]=1;
analyse(u2,function(v){if(v)invertPseudo(el,ps);});});
})(all[i]);}
}catch(e){}}
function watch(){try{
new MutationObserver(check).observe(document.documentElement,{attributes:true,attributeFilter:['style','class','data-theme','data-mode','data-color-scheme']});
}catch(e){}}
if(document.readyState==='loading'){document.addEventListener('DOMContentLoaded',function(){check();watch();logos();});}else{check();watch();logos();}
window.addEventListener('load',function(){check();logos();setTimeout(function(){check();logos();},500);setTimeout(function(){check();logos();},2500);});
try{mq.addEventListener('change',check);}catch(e){}
})();"""

    /** Registers the script on [view] (once per WebView, like the other document-start scripts). */
    @JvmStatic
    fun attach(view: WebView?) {
        if (view == null) return
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) return
        try {
            WebViewCompat.addDocumentStartJavaScript(view, SCRIPT, setOf("*"))
        } catch (e: Throwable) {
            Log.w(TAG, "document-start script not registered", e)
        }
    }
}
