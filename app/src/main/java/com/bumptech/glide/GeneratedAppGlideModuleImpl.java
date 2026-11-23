package com.bumptech.glide;

import android.content.Context;
import androidx.annotation.NonNull;
import com.bumptech.glide.integration.webp.WebpGlideLibraryModule;
import com.mycompany.app.view.MyGlideModule;
import java.util.Collections;
import java.util.Set;

/**
 * What Glide's annotation processor would generate for {@link MyGlideModule} (the old bundle carried a generated copy):
 * Glide finds this class by name. Registers the animated-WebP decoders, then the app's own module (SVG).
 */
@SuppressWarnings("deprecation")
final class GeneratedAppGlideModuleImpl extends GeneratedAppGlideModule {
    private final MyGlideModule appGlideModule = new MyGlideModule();

    GeneratedAppGlideModuleImpl(Context context) {
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        appGlideModule.applyOptions(context, builder);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        new WebpGlideLibraryModule().registerComponents(context, glide, registry);
        appGlideModule.registerComponents(context, glide, registry);
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return appGlideModule.isManifestParsingEnabled();
    }

    @NonNull
    @Override
    public Set<Class<?>> getExcludedModuleClasses() {
        return Collections.emptySet();
    }
}
