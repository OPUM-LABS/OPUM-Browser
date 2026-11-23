package com.mycompany.app.main;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Callback for image loads of a {@link MainItem.ViewItem} (see {@code com.mycompany.app.glide.ThumbLoader}).
 * Called on the main thread; {@code reason} on failure is null when the cause is unknown.
 */
public interface ViewItemListener {
    default void onFailed(MainItem.ViewItem item, View view, Exception reason) {
    }

    default void onComplete(MainItem.ViewItem item, View view, Bitmap bitmap) {
    }

    /** The view was bound to another item before this load finished; the result was dropped. */
    default void onStale(View view) {
    }
}
