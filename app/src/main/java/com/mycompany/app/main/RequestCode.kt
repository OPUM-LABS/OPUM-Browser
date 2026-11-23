package com.mycompany.app.main

/**
 * Request codes for [MainActivity.launchForResult] / [ActivityHost.launchExternalForResult] and the matching
 * onLauncherResult branches. Each code gets its own result launcher, registered in onCreate with
 * launchForResult(null, code). Codes 20-25 and 30 are permission requests (requestPermissions), not launchers.
 */
object RequestCode {
    /** QuickAddActivity: a new quick-access item. */
    const val QUICK_ADD = 0

    /** MainSettingsActivity: settings that may need a page clean-up or new tabs. */
    const val MAIN_SETTINGS = 1

    /** Lock screen for a locked action: the video's touch lock, or the block-link dialog for the pending link. */
    const val UNLOCK = 2

    /** Lock screen, then secret mode on (also the tab overview's unlock). */
    const val UNLOCK_SECRET = 3

    /** DownloadListActivity: a downloaded file to open. */
    const val DOWNLOAD_LIST = 8

    /** Camera or a single picked file (ACTION_GET_CONTENT), also after the storage permission. */
    const val CAMERA_OR_FILE = 9

    /** Photo picker, several images. */
    const val PICK_IMAGES = 10

    /** Document picker, several files. */
    const val PICK_FILES = 11

    /** An image saved to a file: crop, wallpaper or start-page background. */
    const val IMAGE_FILE = 12

    /** ImageEditActivity: the edited image. */
    const val EDIT_IMAGE = 13

    /** Speech recognizer: a voice search. */
    const val VOICE_SEARCH = 17

    /** Folder or new document from the system picker (save folder, bookmark export). */
    const val SAVE_LOCATION = 19

    /** System screen for the picture-in-picture permission. */
    const val PIP_SETTINGS = 26

    /** System screen for installing unknown apps. */
    const val INSTALL_SETTINGS = 27

    /** CleanSettingsActivity: the ad-block and clean-up settings. */
    const val CLEAN_SETTINGS = 32

    /** QuickSettingsActivity: the quick-access layout. */
    const val QUICK_SETTINGS = 33

    /** A settings screen opened from a dialog; the result goes back to that dialog. */
    const val DIALOG_SETTINGS = 35

    /** Lock screen for a locked site. */
    const val UNLOCK_SITE = 36

    /** Lock screen before a launch URL opens. */
    const val UNLOCK_LAUNCH = 37

    /** Highest code with a launcher; launchForResult starts anything above it as a plain activity. */
    const val LAST_LAUNCHER = 37
}
