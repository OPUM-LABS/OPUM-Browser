package com.mycompany.app.main

import ch.opumlabs.opumbrowser.R
import ch.opumlabs.opumbrowser.R as AppR
import android.os.Build
import java.nio.charset.Charset

@Suppress("deprecation")
object MainConst {
    @JvmField
    val moreIconsDark: IntArray
    @JvmField
    val engineOrderKo: IntArray
    @JvmField
    val engineNames: IntArray
    @JvmField
    val engineIcons: IntArray
    @JvmField
    val engineUrls: Array<String?>
    @JvmField
    val agentNames: Array<String?>
    @JvmField
    val knownSites: Array<String?>
    @JvmField
    val loginWords: Array<String?>
    @JvmField
    val defaultCharset: String?
    @JvmField
    val screenOffNames: IntArray
    @JvmField
    val screenOffInfos: IntArray
    @JvmField
    val scrollPosValues: IntArray
    @JvmField
    val scrollPosNames: IntArray
    @JvmField
    val colourKinds: IntArray
    @JvmField
    val videoSizeNames: IntArray
    @JvmField
    val cookieValues: IntArray
    @JvmField
    val cookieNames: IntArray
    @JvmField
    val shotValues: IntArray
    @JvmField
    val shotNames: IntArray
    @JvmField
    val secretUseValues: IntArray
    @JvmField
    val secretUseNames: IntArray
    @JvmField
    val filterValues: IntArray
    @JvmField
    val filterNames: IntArray
    @JvmField
    val lockValues: IntArray
    @JvmField
    val lockNames: IntArray
    @JvmField
    val localeList: Array<Array<String?>?>
    @JvmField
    val isApi28: Boolean
    @JvmField
    val iconColors: IntArray
    @JvmField
    val isApi33: Boolean
    @JvmField
    val folderIcons: IntArray
    @JvmField
    val isApi30: Boolean
    @JvmField
    val searchIcons: IntArray
    @JvmField
    val isApi30Insets: Boolean
    @JvmField
    val viewModeNames: IntArray
    @JvmField
    val placeholderWords: Array<String?>
    @JvmField
    val imageSizeNames: IntArray
    @JvmField
    val linkMenuValues: IntArray
    @JvmField
    val imageSizeInfos: IntArray
    @JvmField
    val linkMenuNames: IntArray
    @JvmField
    val linkMenuIconsBlack: IntArray
    @JvmField
    val linkMenuIconsDark: IntArray
    @JvmField
    val downFilterValues: IntArray
    @JvmField
    val imageMenuValues: IntArray
    @JvmField
    val downFilterNames: IntArray
    @JvmField
    val imageMenuNames: IntArray
    @JvmField
    val imageMenuIconsBlack: IntArray
    @JvmField
    val imageMenuIconsDark: IntArray
    @JvmField
    val presetColors: IntArray
    @JvmField
    val presetPositions: FloatArray
    @JvmField
    val gradientColors: IntArray

    /** The transparency (%) a button on the page starts with (2026-11-26; 0 before, with lavender as the colour). */
    const val BUTTON_ALPHA = 15

    /** The lavender that was the default button colour until 2026-11-26 (now slate, [gradientColors] 5). */
    const val OLD_LAVENDER = -5854742
    @JvmField
    val gradientColorsAlt: IntArray
    @JvmField
    val tabColors: IntArray
    @JvmField
    val tabColorsFill: IntArray
    @JvmField
    val headColors: IntArray
    @JvmField
    val backColors: IntArray
    @JvmField
    val mainMenuNames: IntArray
    @JvmField
    val mainMenuIconsBlack: IntArray
    @JvmField
    val mainMenuIconsDark: IntArray
    @JvmField
    val moreIconsBlack: IntArray

    init {
        val i2 = Build.VERSION.SDK_INT
        isApi28 = i2 >= 28
        isApi33 = i2 >= 33
        val z2 = i2 >= 30
        isApi30 = z2
        isApi30Insets = z2
        placeholderWords = arrayOf<String?>(
            "bg_white",
            "blank.",
            "favicon",
            "loading",
            "logo",
            "nopicture",
            "transparen"
        )
        linkMenuValues = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        val i3 = AppR.string.list_menu
        val i4 = AppR.string.preview
        val i5 = AppR.string.open_url
        val i6 = AppR.string.new_url
        val i7 = AppR.string.group_url
        val i8 = AppR.string.back_url
        val i9 = AppR.string.secret_tab
        val i10 = AppR.string.copy_url
        val i11 = AppR.string.share_url
        val i12 = AppR.string.copy_text
        val i13 = AppR.string.open_with
        val i14 = AppR.string.block_link
        val i15 = AppR.string.block_area
        linkMenuNames = intArrayOf(i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15)
        val i16 = AppR.drawable.outline_menu_black_24
        val i17 = AppR.drawable.outline_visibility_black_24
        val i18 = AppR.drawable.outline_open_in_new_black_24
        val i19 = AppR.drawable.outline_add_box_black_24
        val i20 = AppR.drawable.outline_library_add_black_24
        val i21 = AppR.drawable.outline_flip_to_back_black_24
        val i22 = AppR.drawable.outline_secret_mode_black_24
        val i23 = AppR.drawable.outline_link_black_24
        val i24 = AppR.drawable.outline_share_black_24
        val i25 = AppR.drawable.outline_content_copy_black_24
        val i26 = AppR.drawable.outline_apps_black_24 // "Open with" (was the move-arrows icon)
        val i27 = AppR.drawable.outline_block_black_24
        val i28 = AppR.drawable.outline_remove_selection_black_24
        linkMenuIconsBlack = intArrayOf(i16, i17, i18, i19, i20, i21, i22, i23, i24, i25, i26, i27, i28)
        val i29 = AppR.drawable.outline_menu_dark_24
        val i30 = AppR.drawable.outline_visibility_dark_24
        val i31 = AppR.drawable.outline_open_in_new_dark_24
        val i32 = AppR.drawable.outline_add_box_dark_24
        val i33 = AppR.drawable.outline_library_add_dark_24
        val i34 = AppR.drawable.outline_flip_to_back_dark_24
        val i35 = AppR.drawable.outline_secret_mode_dark_24
        val i36 = AppR.drawable.outline_link_dark_24
        val i37 = AppR.drawable.outline_share_dark_24
        val i38 = AppR.drawable.outline_content_copy_dark_24
        val i39 = AppR.drawable.outline_apps_dark_24
        val i40 = AppR.drawable.outline_block_dark_24
        val i41 = AppR.drawable.outline_remove_selection_dark_24
        linkMenuIconsDark = intArrayOf(i29, i30, i31, i32, i33, i34, i35, i36, i37, i38, i39, i40, i41)
        imageMenuValues = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        val i42 = AppR.string.preview_image
        val i43 = AppR.string.view_image
        val i44 = AppR.string.tab_image
        val i45 = AppR.string.down_image
        val i46 = AppR.string.downall_image
        imageMenuNames = intArrayOf(
            i3,
            i42,
            i43,
            i44,
            i45,
            i46,
            AppR.string.search_image,
            AppR.string.copy_img_url,
            AppR.string.share_img_url,
            AppR.string.share_image,
            AppR.string.set_wallpaper,
            AppR.string.block_a_image,
            i15
        )
        val i47 = AppR.drawable.outline_download_black_24
        val i48 = AppR.drawable.outline_cloud_download_black_24
        val i49 = AppR.drawable.outline_search_black_24
        val i50 = AppR.drawable.outline_send_black_24
        val i51 = AppR.drawable.outline_wallpaper_black_24
        val i52 = AppR.drawable.outline_hide_image_black_24
        imageMenuIconsBlack = intArrayOf(i16, i17, i18, i19, i47, i48, i49, i23, i24, i50, i51, i52, i28)
        val i53 = AppR.drawable.outline_download_dark_24
        val i54 = AppR.drawable.outline_cloud_download_dark_24
        val i55 = AppR.drawable.outline_search_dark_24
        val i56 = AppR.drawable.outline_send_dark_24
        val i57 = AppR.drawable.outline_wallpaper_dark_24
        val i58 = AppR.drawable.outline_hide_image_dark_24
        imageMenuIconsDark = intArrayOf(i29, i30, i31, i32, i53, i54, i55, i36, i37, i56, i57, i58, i41)
        presetColors = intArrayOf(-1, -65536, -33024, -256, -16711936, -16776961, -16777089, -16777216)
        presetPositions = floatArrayOf(0.0f, 0.1429f, 0.2858f, 0.4287f, 0.5716f, 0.7145f, 0.8574f, 1.0f)
        gradientColors = intArrayOf(
            -65536,
            -291840,
            -7461718,
            -4142541,
            -8604862,
            -12232092,
            -13022805,
            -16777216
        )
        gradientColorsAlt = intArrayOf(-65536, -291840, -7461718, -256, -4142541, -8604862, -5854742, -13022805)
        tabColors = intArrayOf(
            -65536,
            -291840,
            -7461718,
            -9920712,
            -14983648,
            -5854742,
            -13022805,
            -10395295,
            -12434878,
            -16777216
        )
        tabColorsFill = intArrayOf(
            -2818048,
            -37632,
            -11922292,
            -9920712,
            -14983648,
            -10720320,
            -13022805,
            -10395295,
            -12434878,
            -16777216
        )
        headColors = intArrayOf(
            -1754827,
            -2937041,
            -3790808,
            -4776932,
            -2614432,
            -4056997,
            -5434281,
            -7860657,
            -7461718,
            -8708190,
            -9823334,
            -11922292,
            -10603087,
            -11457112,
            -12245088,
            -13558894,
            -13022805,
            -13615201,
            -14142061,
            -15064194,
            -14776091,
            -15108398,
            -15374912,
            -15906911,
            -16540699,
            -16611119,
            -16615491,
            -16689253,
            -16732991,
            -16738393,
            -16743537,
            -16752540,
            -16742021,
            -16746133,
            -16750244,
            -16757440,
            -12345273,
            -13070788,
            -13730510,
            -14983648,
            -8604862,
            -9920712,
            -11171025,
            -13407970,
            -4142541,
            -5262293,
            -6382300,
            -8227049,
            -9614271,
            -10665929,
            -11652050,
            -12703965,
            -11243910,
            -12232092,
            -13154481,
            -14273992,
            -10395295,
            -12434878,
            -14606047,
            -16777216
        )
        backColors = intArrayOf(-1, -3026479, -7237231, -11447983, -14606047, -16777216)
        mainMenuNames = intArrayOf(
            AppR.string.not_used,
            AppR.string.address_bar,
            i3,
            AppR.string.history,
            AppR.string.down_list,
            i6,
            AppR.string.block_images,
            AppR.string.only_image,
            i46,
            AppR.string.capture,
            AppR.string.photo_editor,
            AppR.string.translate,
            i11,
            AppR.string.add_home_1,
            AppR.string.find_word,
            AppR.string.down_page,
            AppR.string.print_page,
            AppR.string.desk_mode,
            AppR.string.brightness,
            AppR.string.text_size,
            AppR.string.full_screen,
            AppR.string.secret_mode,
            AppR.string.night_mode,
            AppR.string.pdf,
            AppR.string.not_used,
            AppR.string.app_exit,
            AppR.string.prev_page,
            AppR.string.next_page,
            AppR.string.home_page,
            AppR.string.bookmark,
            AppR.string.tab_list,
            AppR.string.reader_mode,
            AppR.string.page_source,
            AppR.string.memo_title,
            AppR.string.not_used,
            AppR.string.refresh,
            AppR.string.prev_tab,
            AppR.string.next_tab,
            AppR.string.delete_tab,
            AppR.string.delete_all_tab,
            AppR.string.clear_data,
            AppR.string.not_used,
            AppR.string.user_agent,
            i10,
            AppR.string.search_url,
            AppR.string.add_page,
            AppR.string.onehand_mode,
            AppR.string.text_zoom,
            AppR.string.cookie,
            AppR.string.js_title,
            AppR.string.txt_viewer,
            AppR.string.address_menu,
            AppR.string.open_copied_url,
            AppR.string.voice_title,
            AppR.string.screen_filter,
            AppR.string.move_top,
            AppR.string.move_bot,
            AppR.string.scroll_up,
            AppR.string.scroll_down,
            AppR.string.delete_other_tab,
            AppR.string.tts_mode,
            AppR.string.gesture,
            AppR.string.ads_block,
            AppR.string.add_book,
            AppR.string.video_download,
            AppR.string.setting,
            AppR.string.dark_mode,
            i13,
            AppR.string.space_title,
            AppR.string.speak_page,
            AppR.string.tab_menu,
            i7,
            i8,
            i15,
            AppR.string.workspaces,
            // 75 this site (page info), 76 restart (2026-11-26, from the menu's header)
            AppR.string.this_site,
            AppR.string.mh_restart
        )
        val i59 = AppR.drawable.outline_border_clear_black_24
        val i60 = AppR.drawable.outline_history_black_24
        val i61 = AppR.drawable.outline_photo_library_black_24
        val i62 = AppR.drawable.outline_center_focus_weak_black_24
        val i63 = AppR.drawable.outline_palette_black_24
        val i64 = AppR.drawable.outline_translate_black_24
        val i65 = AppR.drawable.outline_add_to_home_screen_black_24
        val i66 = AppR.drawable.outline_find_in_page_black_24
        val i67 = AppR.drawable.outline_file_save_black_24
        val i68 = AppR.drawable.outline_print_black_24
        val i69 = AppR.drawable.outline_desktop_windows_black_24
        val i70 = AppR.drawable.outline_brightness_6_black_24
        val i71 = AppR.drawable.outline_text_fields_black_24
        val i72 = AppR.drawable.outline_fullscreen_black_24
        val i73 = AppR.drawable.outline_dark_mode_black_24
        val i74 = AppR.drawable.outline_local_library_black_24
        val i75 = AppR.drawable.outline_live_tv_black_24
        val i76 = AppR.drawable.outline_power_settings_new_black_24
        val i77 = AppR.drawable.outline_keyboard_arrow_left_black_24
        val i78 = AppR.drawable.outline_keyboard_arrow_right_black_24
        val i79 = AppR.drawable.outline_home_black_24
        val i80 = AppR.drawable.outline_kid_star_2_black_24
        val i81 = AppR.drawable.outline_check_box_outline_blank_black_24
        val i82 = AppR.drawable.outline_chrome_reader_mode_black_24
        val i83 = AppR.drawable.outline_description_black_24
        val i84 = AppR.drawable.outline_text_snippet_black_24
        val i85 = AppR.drawable.outline_qr_code_black_24
        val i86 = AppR.drawable.outline_refresh_black_24
        val i87 = AppR.drawable.outline_tab_move_2_black_24
        val i88 = AppR.drawable.outline_tab_move_black_24
        val i89 = AppR.drawable.outline_disabled_by_default_black_24
        val i90 = AppR.drawable.outline_tab_close_black_24
        val i91 = AppR.drawable.outline_delete_sweep_black_24
        val i92 = AppR.drawable.outline_vpn_key_black_24
        val i93 = AppR.drawable.outline_public_black_24
        val i94 = AppR.drawable.outline_playlist_add_black_24
        val i95 = AppR.drawable.outline_swipe_down_black_24
        val i96 = AppR.drawable.outline_zoom_in_black_24
        val i97 = AppR.drawable.outline_copyright_black_24
        val i98 = AppR.drawable.outline_javascript_black_24
        val i99 = AppR.drawable.outline_sticky_note_black_24
        val i100 = AppR.drawable.outline_article_black_24
        val i101 = AppR.drawable.outline_mic_black_24
        mainMenuIconsBlack = intArrayOf(
            i59,
            i16,
            i16,
            i60,
            i47,
            i19,
            i52,
            i61,
            i48,
            i62,
            i63,
            i64,
            i24,
            i65,
            i66,
            i67,
            i68,
            i69,
            i70,
            i71,
            i72,
            i22,
            i73,
            i74,
            i75,
            i76,
            i77,
            i78,
            i79,
            i80,
            i81,
            i82,
            i83,
            i84,
            i85,
            i86,
            i87,
            i88,
            i89,
            i90,
            i91,
            i92,
            i93,
            i23,
            i49,
            i94,
            i95,
            i96,
            i97,
            i98,
            i99,
            i100,
            i18,
            i101,
            AppR.drawable.outline_tonality_black_24,
            AppR.drawable.outline_vertical_align_top_black_24,
            AppR.drawable.outline_vertical_align_bottom_black_24,
            AppR.drawable.outline_keyboard_arrow_up_black_24,
            AppR.drawable.outline_keyboard_arrow_down_black_24,
            AppR.drawable.outline_tab_close_right_black_24,
            AppR.drawable.outline_text_to_speech_black_24,
            AppR.drawable.outline_gesture_black_24,
            AppR.drawable.outline_verified_user_black_24,
            AppR.drawable.baseline_star_add_black_24,
            AppR.drawable.outline_download_for_offline_black_24,
            AppR.drawable.outline_settings_black_24,
            AppR.drawable.outline_format_color_fill_black_24,
            i26,
            i59,
            AppR.drawable.outline_campaign_black_24,
            AppR.drawable.outline_filter_none_black_24,
            i20,
            i21,
            i28,
            AppR.drawable.outline_workspaces_black_24,
            AppR.drawable.outline_info_black_24,
            AppR.drawable.outline_restart_alt_black_24
        )
        val i102 = AppR.drawable.outline_border_clear_dark_24
        val i103 = AppR.drawable.outline_history_dark_24
        val i104 = AppR.drawable.outline_photo_library_dark_24
        val i105 = AppR.drawable.outline_center_focus_weak_dark_24
        val i106 = AppR.drawable.outline_palette_dark_24
        val i107 = AppR.drawable.outline_translate_dark_24
        val i108 = AppR.drawable.outline_add_to_home_screen_dark_24
        val i109 = AppR.drawable.outline_find_in_page_dark_24
        val i110 = AppR.drawable.outline_file_save_dark_24
        val i111 = AppR.drawable.outline_print_dark_24
        val i112 = AppR.drawable.outline_desktop_windows_dark_24
        val i113 = AppR.drawable.outline_brightness_6_dark_24
        val i114 = AppR.drawable.outline_text_fields_dark_24
        val i115 = AppR.drawable.outline_fullscreen_dark_24
        val i116 = AppR.drawable.outline_dark_mode_dark_24
        val i117 = AppR.drawable.outline_local_library_dark_24
        val i118 = AppR.drawable.outline_live_tv_dark_24
        val i119 = AppR.drawable.outline_power_settings_new_dark_24
        val i120 = AppR.drawable.outline_keyboard_arrow_left_dark_24
        val i121 = AppR.drawable.outline_keyboard_arrow_right_dark_24
        val i122 = AppR.drawable.outline_home_dark_24
        val i123 = AppR.drawable.outline_kid_star_2_dark_24
        val i124 = AppR.drawable.outline_check_box_outline_blank_dark_24
        val i125 = AppR.drawable.outline_chrome_reader_mode_dark_24
        val i126 = AppR.drawable.outline_description_dark_24
        val i127 = AppR.drawable.outline_text_snippet_dark_24
        val i128 = AppR.drawable.outline_qr_code_dark_24
        val i129 = AppR.drawable.outline_refresh_dark_24
        val i130 = AppR.drawable.outline_tab_move_2_dark_24
        val i131 = AppR.drawable.outline_tab_move_dark_24
        val i132 = AppR.drawable.outline_disabled_by_default_dark_24
        val i133 = AppR.drawable.outline_tab_close_dark_24
        val i134 = AppR.drawable.outline_delete_sweep_dark_24
        val i135 = AppR.drawable.outline_vpn_key_dark_24
        val i136 = AppR.drawable.outline_public_dark_24
        val i137 = AppR.drawable.outline_playlist_add_dark_24
        val i138 = AppR.drawable.outline_swipe_down_dark_24
        val i139 = AppR.drawable.outline_zoom_in_dark_24
        val i140 = AppR.drawable.outline_copyright_dark_24
        val i141 = AppR.drawable.outline_javascript_dark_24
        val i142 = AppR.drawable.outline_sticky_note_dark_24
        val i143 = AppR.drawable.outline_article_dark_24
        val i144 = AppR.drawable.outline_mic_dark_24
        mainMenuIconsDark = intArrayOf(
            i102,
            i29,
            i29,
            i103,
            i53,
            i32,
            i58,
            i104,
            i54,
            i105,
            i106,
            i107,
            i37,
            i108,
            i109,
            i110,
            i111,
            i112,
            i113,
            i114,
            i115,
            i35,
            i116,
            i117,
            i118,
            i119,
            i120,
            i121,
            i122,
            i123,
            i124,
            i125,
            i126,
            i127,
            i128,
            i129,
            i130,
            i131,
            i132,
            i133,
            i134,
            i135,
            i136,
            i36,
            i55,
            i137,
            i138,
            i139,
            i140,
            i141,
            i142,
            i143,
            i31,
            i144,
            AppR.drawable.outline_tonality_dark_24,
            AppR.drawable.outline_vertical_align_top_dark_24,
            AppR.drawable.outline_vertical_align_bottom_dark_24,
            AppR.drawable.outline_keyboard_arrow_up_dark_24,
            AppR.drawable.outline_keyboard_arrow_down_dark_24,
            AppR.drawable.outline_tab_close_right_dark_24,
            AppR.drawable.outline_text_to_speech_dark_24,
            AppR.drawable.outline_gesture_dark_24,
            AppR.drawable.outline_verified_user_dark_24,
            AppR.drawable.baseline_star_add_dark_24,
            AppR.drawable.outline_download_for_offline_dark_24,
            AppR.drawable.outline_settings_dark_24,
            AppR.drawable.outline_format_color_fill_dark_24,
            i39,
            i102,
            AppR.drawable.outline_campaign_dark_24,
            AppR.drawable.outline_filter_none_dark_24,
            i33,
            i34,
            i41,
            AppR.drawable.outline_workspaces_dark_24,
            AppR.drawable.outline_info_dark_24,
            AppR.drawable.outline_restart_alt_dark_24
        )
        moreIconsBlack = intArrayOf(AppR.drawable.outline_more_vert_black_24, i85, i101)
        moreIconsDark = intArrayOf(AppR.drawable.outline_more_vert_dark_24, i128, i144)
        engineOrderKo = intArrayOf(4, 5, 2, 3, 0, 1, 6, 7, 8, 9)
        engineNames = intArrayOf(
            AppR.string.baidu,
            AppR.string.bing,
            AppR.string.daum,
            AppR.string.duckduckgo,
            AppR.string.google,
            AppR.string.naver,
            AppR.string.spage,
            AppR.string.yahoo,
            AppR.string.yandex,
            AppR.string.youtube
        )
        engineIcons = intArrayOf(
            AppR.drawable.baseline_search_baidu,
            AppR.drawable.baseline_search_bing,
            AppR.drawable.baseline_search_daum,
            AppR.drawable.baseline_search_duckduckgo,
            AppR.drawable.baseline_search_google,
            AppR.drawable.baseline_search_naver,
            AppR.drawable.baseline_search_spage,
            AppR.drawable.baseline_search_yahoo,
            AppR.drawable.baseline_search_yandex,
            AppR.drawable.baseline_search_youtube
        )
        engineUrls = arrayOf<String?>(
            "https://www.baidu.com/s?wd=",
            "https://www.bing.com/search?q=",
            "https://search.daum.net/search?w=tot&DA=YZR&t__nil_searchbox=btn&sug=&sugo=&o=&q=",
            "https://duckduckgo.com/?q=",
            "https://www.google.com/search?q=",
            "https://search.naver.com/search.naver?where=nexearch&query=",
            "https://www.startpage.com/sp/search?query=",
            "https://search.yahoo.com/search;_ylt=AsNJlW0_HqFaPWRRY225.umbvZx4?p=",
            "https://www.yandex.com/search/?text=",
            "https://www.youtube.com/results?search_query="
        )
        agentNames = arrayOf<String?>("OPUM", "Android", "iPhone", "iPad")
        knownSites = arrayOf<String?>(
            "afreecatv",
            "baidu",
            "band.us",
            "bing",
            "daum",
            "duckduckgo",
            "facebook",
            "flipboard",
            "google",
            "instagram",
            "kakao",
            "line.me",
            "linkedin",
            "naver",
            "pinterest",
            "reddit",
            "tumblr",
            "twitch",
            "yahoo",
            "yandex",
            "youtube",
            "youtu.be"
        )
        loginWords = arrayOf<String?>("account", "auth", "login", "signin", "signup")
        defaultCharset = Charset.defaultCharset().displayName()
        screenOffNames = intArrayOf(AppR.string.system_name, AppR.string.screen_hour, AppR.string.screen_always)
        screenOffInfos = intArrayOf(AppR.string.screen_info_system, AppR.string.screen_info_hour, 0)
        scrollPosValues = intArrayOf(1, 3, 0)
        scrollPosNames = intArrayOf(
            AppR.string.not_show,
            AppR.string.show_left,
            AppR.string.show_center,
            AppR.string.show_right
        )
        colourKinds = intArrayOf(1, 2, 3, 0)
        videoSizeNames = intArrayOf(AppR.string.size_fit, AppR.string.full_screen, AppR.string.zoom_size)
        cookieValues = intArrayOf(0, 1, 2)
        val i145 = AppR.string.always_allow
        val i146 = AppR.string.always_block
        cookieNames = intArrayOf(i145, i146, AppR.string.block_in_secret)
        shotValues = intArrayOf(0, 1, 2)
        shotNames = intArrayOf(i145, i146, AppR.string.block_back)
        secretUseValues = intArrayOf(1, 2, 0)
        val i147 = AppR.string.not_used
        val i148 = AppR.string.always_use
        secretUseNames = intArrayOf(i147, i148, AppR.string.only_use_secret)
        filterValues = intArrayOf(1, 2, 0)
        filterNames = intArrayOf(i147, i148, AppR.string.only_night)
        lockValues = intArrayOf(1, 2, 3, 4, 0)
        lockNames = intArrayOf(
            i147,
            AppR.string.lock_pattern,
            AppR.string.lock_pin,
            AppR.string.password,
            AppR.string.finger_print
        )
        localeList = arrayOf<Array<String?>?>(
            arrayOf<String?>("", "", ""),
            arrayOf<String?>("cs", "CZ", "Čeština"),
            arrayOf<String?>("de", "DE", "Deutsch"),
            arrayOf<String?>("en", "US", "English"),
            arrayOf<String?>("es", "ES", "Español"),
            arrayOf<String?>("fr", "FR", "Français"),
            arrayOf<String?>("in", "ID", "Indonesia"),
            arrayOf<String?>("it", "IT", "Italiano"),
            arrayOf<String?>("hu", "HU", "Magyar"),
            arrayOf<String?>("pl", "PL", "Polska"),
            arrayOf<String?>("pt", "PT", "Português"),
            arrayOf<String?>("pt", "BR", "Português (Brasil)"),
            arrayOf<String?>("ru", "RU", "Русский"),
            arrayOf<String?>("ro", "RO", "România"),
            arrayOf<String?>("vi", "VN", "Tiếng Việt"),
            arrayOf<String?>("tr", "TR", "Türkiye"),
            arrayOf<String?>("uk", "UA", "Українська"),
            arrayOf<String?>("ar", "SA", "العربية"),
            arrayOf<String?>("fa", "IR", "فارسی"),
            arrayOf<String?>("hi", "IN", "हिन्दी"),
            arrayOf<String?>("sat", "IN", "ᱥᱟᱱᱛᱟᱲᱤ"),
            arrayOf<String?>("th", "TH", "ไทย"),
            arrayOf<String?>("zh", "CN", "中文 (简体)"),
            arrayOf<String?>("zh", "TW", "中文 (繁體)"),
            arrayOf<String?>("ja", "JP", "日本語"),
            arrayOf<String?>("ko", "KR", "한국어")
        )
        iconColors = intArrayOf(
            -65536,
            -278483,
            -2080517,
            -6543440,
            -10011977,
            -12627531,
            -16611119,
            -16738393,
            -16757440,
            -13730510,
            -11171025,
            -4142541,
            -7508381,
            -9079435,
            -10453621,
            -16777216
        )
        folderIcons = intArrayOf(
            AppR.drawable.baseline_folder_00_24,
            AppR.drawable.baseline_folder_01_24,
            AppR.drawable.baseline_folder_02_24,
            AppR.drawable.baseline_folder_03_24,
            AppR.drawable.baseline_folder_04_24,
            AppR.drawable.baseline_folder_05_24,
            AppR.drawable.baseline_folder_06_24,
            AppR.drawable.baseline_folder_07_24,
            AppR.drawable.baseline_folder_08_24,
            AppR.drawable.baseline_folder_09_24,
            AppR.drawable.baseline_folder_10_24,
            AppR.drawable.baseline_folder_11_24,
            AppR.drawable.baseline_folder_12_24,
            AppR.drawable.baseline_folder_13_24,
            AppR.drawable.baseline_folder_14_24,
            AppR.drawable.baseline_folder_black_24
        )
        searchIcons = intArrayOf(
            AppR.drawable.baseline_search_00_24,
            AppR.drawable.baseline_search_01_24,
            AppR.drawable.baseline_search_02_24,
            AppR.drawable.baseline_search_03_24,
            AppR.drawable.baseline_search_04_24,
            AppR.drawable.baseline_search_05_24,
            AppR.drawable.baseline_search_06_24,
            AppR.drawable.baseline_search_07_24,
            AppR.drawable.baseline_search_08_24,
            AppR.drawable.baseline_search_09_24,
            AppR.drawable.baseline_search_10_24,
            AppR.drawable.baseline_search_11_24,
            AppR.drawable.baseline_search_12_24,
            AppR.drawable.baseline_search_13_24,
            AppR.drawable.baseline_search_14_24,
            AppR.drawable.baseline_search_15_24
        )
        viewModeNames = intArrayOf(
            AppR.string.page_effect,
            AppR.string.page_scroll,
            AppR.string.list_hori,
            AppR.string.list_vert
        )
        imageSizeNames = intArrayOf(AppR.string.best_size, AppR.string.reduce_image)
        imageSizeInfos = intArrayOf(AppR.string.best_info, AppR.string.reduce_info)
        downFilterValues = intArrayOf(0, 8, 4, 5, 6, 7, 43)
        val i149 = AppR.string.all
        val i150 = AppR.string.page
        val i151 = AppR.string.image
        val i152 = AppR.string.video
        val i153 = AppR.string.audio
        downFilterNames = intArrayOf(i149, i150, i151, i152, i153, AppR.string.doc, AppR.string.others)
    }
}
