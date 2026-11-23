package com.mycompany.app.dialog

/**
 * The old photo editor's text dialog is gone (the Compose editor has its own); only this callback type remains because
 * other legacy dialogs (icon editor, web text settings) still report a colour / text through it.
 */
class DialogEditorText private constructor() {
    interface EditorSetListener {
        fun onApply(color: Int, label: String?)
    }
}
