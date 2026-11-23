package com.mycompany.app.subtitle

/** A parsed cue before it is added to a cue list: shown from [start] until [end]. */
internal class Caption(
    var start: Time = Time(),
    var end: Time = Time(),
    var text: String = "",
)
