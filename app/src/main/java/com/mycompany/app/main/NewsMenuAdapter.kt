package com.mycompany.app.main

import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.ZtwoSettings

/**
 * Tables of the news topic menu (DialogNewsMenu, Compose since 2026-11-17): [labels] topic names, [icons] / [badges] the
 * icons, and [colors] circle colours for the current topic (ZtwoSettings.newsTopic.value). The RecyclerView
 * adapter part (rows with `MyCircleView`) is gone.
 */
class NewsMenuAdapter private constructor() {
    companion object {
        @JvmField
        val labels: IntArray = intArrayOf(
            R.string.news_topic_headline,
            R.string.news_topic_world,
            R.string.news_topic_nation,
            R.string.news_topic_business,
            R.string.news_topic_tech,
            R.string.news_topic_enter,
            R.string.news_topic_sports,
            R.string.news_topic_science,
            R.string.news_topic_health,
            R.string.search_url
        )
        @JvmField
        val icons: IntArray = intArrayOf(
            R.drawable.outline_newspaper_black_24,
            R.drawable.outline_public_black_24,
            R.drawable.outline_travel_explore_black_24,
            R.drawable.outline_monitoring_black_24,
            R.drawable.outline_rocket_launch_black_24,
            R.drawable.outline_movie_filter_black_24,
            R.drawable.outline_sports_handball_black_24,
            R.drawable.outline_biotech_black_24,
            R.drawable.outline_cardiology_black_24,
            R.drawable.outline_search_black_24
        )
        @JvmField
        val badges: IntArray = intArrayOf(
            R.drawable.outline_newspaper_white_24,
            R.drawable.outline_public_white_24,
            R.drawable.outline_travel_explore_white_24,
            R.drawable.outline_monitoring_white_24,
            R.drawable.outline_rocket_launch_white_24,
            R.drawable.outline_movie_filter_white_24,
            R.drawable.outline_sports_handball_white_24,
            R.drawable.outline_biotech_white_24,
            R.drawable.outline_cardiology_white_24,
            R.drawable.outline_search_white_24
        )
        @JvmField
        val colors: IntArray = intArrayOf(
            -65536,
            -3862174,
            -10354454,
            -14064897,
            -16729900,
            -16725933,
            -8227049,
            -37120,
            -2282496,
            -9079435
        )
    }
}
