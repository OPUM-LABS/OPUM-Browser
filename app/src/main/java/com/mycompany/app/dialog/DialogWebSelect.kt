package com.mycompany.app.dialog

import android.app.Activity
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.browser.ActionItem
import com.mycompany.app.ui.browser.ComposeActionDialog

/**
 * What to attach to a web page's file input: image / camera, video / camcorder, audio, or any file. [maskParam] is the mask of
 * what the page accepts (0 = all kinds; 2 = images, 4 = video, 8 = audio; "file" is always offered). Picking a row
 * reports its id and closes the sheet; cancelling (back or outside) reports `onCancel()` so the page's chooser is released.
 * Compose now (on [ComposeActionDialog]); [theme] only chose the old dialog theme.
 */
class DialogWebSelect(activity: Activity?, theme: Int, maskParam: Int, webSelectListener: WebSelectListener?) :
    ComposeActionDialog(activity, selectItems(maskParam), {}) {
    private var listener: WebSelectListener? = webSelectListener

    interface WebSelectListener {
        fun onPick(type: Int)
        fun onCancel()
    }

    override fun onItem(id: Int) {
        listener?.onPick(id)
        dismiss()
    }

    override fun cancel() {
        listener?.onCancel()
        dismiss()
    }

    override fun dismiss() {
        listener = null
        super.dismiss()
    }

    companion object {
        private fun selectItems(mask: Int): List<ActionItem> {
            val image = ActionItem(4, R.drawable.outline_image_black_24, R.drawable.outline_image_dark_24, R.string.image)
            val camera = ActionItem(10, R.drawable.outline_photo_camera_black_24, R.drawable.outline_photo_camera_dark_24, R.string.camera)
            val video = ActionItem(5, R.drawable.baseline_play_arrow_black_24, R.drawable.baseline_play_arrow_dark_24, R.string.video)
            val camcorder = ActionItem(11, R.drawable.outline_videocam_black_24, R.drawable.outline_videocam_dark_24, R.string.camcorder)
            val audio = ActionItem(6, R.drawable.baseline_music_note_black_24, R.drawable.baseline_music_note_dark_24, R.string.audio)
            val file = ActionItem(0, R.drawable.outline_draft_black_24, R.drawable.outline_draft_dark_24, R.string.file)
            if (mask == 0) return listOf(image, camera, video, camcorder, audio, file)
            val list = ArrayList<ActionItem>()
            if ((mask and 2) == 2) { list.add(image); list.add(camera) }
            if ((mask and 4) == 4) { list.add(video); list.add(camcorder) }
            if ((mask and 8) == 8) list.add(audio)
            list.add(file)
            return list
        }
    }
}
