package com.mycompany.app.subtitle

/**
 * One entry of a cue list: from [start] (ms) on, show [text]; a null [text] clears the subtitle.
 */
class SubtitleItem(
    @JvmField var start: Long,
    @JvmField var text: String?,
)
