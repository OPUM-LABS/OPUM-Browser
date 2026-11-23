package com.mycompany.app.main;

import com.mycompany.app.compress.CompressUtil;
import java.util.ArrayList;
import net.lingala.zip4j.model.ZipParameters;

/**
 * Helpers that need javac (a field set on a zip4j class). The bridge methods for the old prebuilt library jar's
 * `SettingListAdapter$SettingItem` calls had no callers left and were removed with `setting/` (2026-11-17).
 */
public final class JarBridge {
    private JarBridge() {
    }

    public static void zipListener(ZipParameters params, CompressUtil.CompressListener listener) {
        params.compressListener = listener;
    }
}
