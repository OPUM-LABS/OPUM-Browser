# App rules for R8 (release builds).
#
# Until 2026-11-21 every class of the app was kept whole (-keep class com.mycompany.** { *; }), so R8 could not drop
# any unused app code. Now the app code is shrunk and optimized like the libraries. Names are kept (no obfuscation),
# so crash reports stay readable and nothing that looks a class up by name breaks.
-dontobfuscate
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# JavaScript bridges (addJavascriptInterface): page scripts call these methods by name.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Native helpers (libmyutil: MainUtil, libopumadblock: UboEngine): JNI finds the methods by class and method name.
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Objects passed through intents and saved state.
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-dontwarn **

# JGit (bookmark sync over Git, 2026-11-28): its message texts are filled in by reflection, its transports register
# themselves, and it refers to desktop-only classes it never uses on Android
-keep class * extends org.eclipse.jgit.nls.TranslationBundle { *; }
-keep class org.eclipse.jgit.transport.Transport* { *; }
-dontwarn org.eclipse.jgit.**
-dontwarn org.slf4j.**
-dontwarn javax.management.**
-dontwarn org.ietf.jgss.**
-dontwarn com.jcraft.jsch.**
