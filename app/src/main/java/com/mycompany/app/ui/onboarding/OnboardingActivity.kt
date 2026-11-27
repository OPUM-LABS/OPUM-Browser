package com.mycompany.app.ui.onboarding

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import com.mycompany.app.ui.components.TickSwitch as Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogBackupLoad
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ColorWheelPicker
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.settings.TextAssetActivity
import com.mycompany.app.ui.settings.rememberBrowserTransfer
import com.mycompany.app.ui.theme.ThemeChoice
import java.util.Locale

/**
 * First-start setup wizard (2026-11-20, replaces the four-picture intro): Welcome, Theme, Address bar, Search engine,
 * Ads & trackers, Bring your data, Default browser, Notifications, Done (Tab overview after Address bar and Notifications
 * since 2026-11-23). A progress bar and "Step n of 10" sit on top; every step can be
 * skipped (Skip / Next), Back goes one step back, and "Skip setup" on the welcome page ends it at once. Choices apply
 * as they are made (the theme re-colours the wizard itself). Finishing or skipping stores "seen" (`SyncSettings.splash`
 * off), so it shows only once. Opened by the browser on first start ([open]).
 */
class OnboardingActivity : OpumComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        isOpen = true
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        if (isFinishing) isOpen = false
        super.onDestroy()
    }

    /** Ends the wizard for good (also when skipped). */
    private fun complete() {
        if (SyncSettings.splash.value) SyncSettings.edit(applicationContext) { set(SyncSettings.splash, false) }
        finish()
    }

    @Composable
    override fun Content() {
        var step by rememberSaveable { mutableIntStateOf(0) }
        val last = STEPS - 1
        fun next() { if (step < last) step++ else complete() }
        fun back() { if (step > 0) step-- }
        BackHandler { if (step > 0) back() else complete() }

        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Progress(step)
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        val dir = if (targetState > initialState) 1 else -1
                        (slideInHorizontally { it * dir / 4 } + fadeIn()) togetherWith (slideOutHorizontally { -it * dir / 4 } + fadeOut())
                    },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    label = "step",
                ) { page ->
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
                    ) {
                        when (page) {
                            0 -> Welcome()
                            1 -> ThemeStep()
                            2 -> BarStep()
                            3 -> OverviewStep()
                            4 -> SearchStep()
                            5 -> PrivacyStep()
                            6 -> DataStep()
                            7 -> DefaultStep()
                            8 -> NotifyStep()
                            else -> DoneStep()
                        }
                    }
                }
                // Back · Skip · Next
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (step > 0) TextButton(onClick = { back() }) { Text(stringResource(R.string.ob_back)) }
                    Spacer(Modifier.weight(1f))
                    when (step) {
                        0 -> TextButton(onClick = { complete() }) { Text(stringResource(R.string.ob_skip_setup)) }
                        last -> Unit
                        else -> TextButton(onClick = { next() }) { Text(stringResource(R.string.skip)) }
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { next() }) {
                        Text(
                            stringResource(
                                when (step) {
                                    0 -> R.string.start
                                    last -> R.string.ob_done_button
                                    else -> R.string.ob_next
                                },
                            ),
                        )
                    }
                }
            }
        }
    }

    /** Segmented progress bar and "Step n of [STEPS]". */
    @Composable
    private fun Progress(step: Int) {
        val label = stringResource(R.string.ob_step, step + 1, STEPS)
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp).semantics { contentDescription = label }) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(STEPS) { i ->
                    Box(
                        Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                            .background(if (i <= step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest),
                    )
                }
            }
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        }
    }

    @Composable
    private fun ColumnScope.Heading(title: Int, text: Int) {
        Text(stringResource(title), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            stringResource(text), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
    }

    @Composable
    private fun ColumnScope.Welcome() {
        val context = this@OnboardingActivity
        val icon = remember {
            try {
                ResourcesCompat.getDrawable(resources, R.mipmap.ic_launcher, theme)?.toBitmap(192, 192)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
        Spacer(Modifier.height(24.dp))
        if (icon != null) Image(icon, null, Modifier.size(96.dp).align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.ob_welcome_title), style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
        Text(
            stringResource(R.string.ob_welcome_text), style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        Spacer(Modifier.height(48.dp))
        // the terms notice of the old intro ("By pressing the Start button, you agree to …")
        val terms = stringResource(R.string.notice_tos)
        val privacy = stringResource(R.string.notice_privacy)
        val full = String.format(Locale.US, stringResource(R.string.notice), terms, privacy)
        val links = TextLinkStyles(SpanStyle(color = MaterialTheme.colorScheme.primary))
        val notice = buildAnnotatedString {
            append(full)
            val t = full.indexOf(terms)
            if (t >= 0) addLink(LinkAnnotation.Clickable("terms", links) { startActivity(TextAssetActivity.legalIntent(context, true)) }, t, t + terms.length)
            val p = full.indexOf(privacy)
            if (p >= 0) addLink(LinkAnnotation.Clickable("privacy", links) { startActivity(TextAssetActivity.legalIntent(context, false)) }, p, p + privacy.length)
        }
        Text(
            notice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
    }

    @Composable
    private fun ColumnScope.ThemeStep() {
        var tick by remember { mutableIntStateOf(0) }
        Heading(R.string.ob_theme_title, R.string.ob_theme_text)
        // the same two choices as Appearance > Theme: colours, and light / dark
        val color = "$tick".let { ThemeChoice.currentColor() }
        val mode = "$tick".let { ThemeChoice.currentMode() }
        ChoiceRow(R.string.color_title, listOf(R.string.system_name, R.string.theme_neutral, R.string.theme_own_color), color) {
            ThemeChoice.chooseColor(this@OnboardingActivity, it); tick++
        }
        ChoiceRow(R.string.theme_mode, listOf(R.string.system_name, R.string.theme_light, R.string.theme_dark), mode) {
            ThemeChoice.chooseMode(this@OnboardingActivity, it); tick++
        }
        if (color == ThemeChoice.COLOR_OWN) {
            ColorWheelPicker(
                color = "$tick".let { BrowserSettings.themeColor.value },
                onChange = { BrowserSettings.editLocal { set(BrowserSettings.themeColor, it) }; MainUtil.resetTextSizePref() },
                onCommit = { BrowserSettings.edit(this@OnboardingActivity) { set(BrowserSettings.themeColor, it) }; MainUtil.resetTextSizePref() },
                wheelDescription = stringResource(R.string.theme_color_wheel),
                brightnessLabel = stringResource(R.string.brightness),
                modifier = Modifier.padding(top = 16.dp),
            )
            SwitchLine(R.string.theme_color_bars, "$tick".let { BrowserSettings.colorBars.value }) {
                BrowserSettings.edit(this@OnboardingActivity) { set(BrowserSettings.colorBars, it) }
                MainUtil.resetTextSizePref()
                tick++
            }
        }
        // web pages: with System they follow the phone; otherwise their own switch
        if (mode != ThemeChoice.MODE_SYSTEM) {
            SwitchLine(R.string.ob_dark_pages, "$tick".let { BrowserSettings.themeWeb.value == 1 }) {
                ThemeChoice.setWebDark(this@OnboardingActivity, it); tick++
            }
        }
    }

    /** A titled row of segmented buttons ([labels]), [selected] marked. */
    @Composable
    private fun ChoiceRow(title: Int, labels: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
        Text(
            stringResource(title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
        )
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            labels.forEachIndexed { i, label ->
                SegmentedButton(
                    selected = i == selected,
                    onClick = { onSelect(i) },
                    shape = SegmentedButtonDefaults.itemShape(i, labels.size),
                    icon = {},
                ) { Text(stringResource(label), maxLines = 1, style = MaterialTheme.typography.labelMedium) }
            }
        }
    }

    @Composable
    private fun SwitchLine(title: Int, checked: Boolean, summary: Int = 0, onChange: (Boolean) -> Unit) {
        Row(
            Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                Text(stringResource(title), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                if (summary != 0) Text(stringResource(summary), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }

    @Composable
    private fun ColumnScope.BarStep() {
        var bottom by remember { mutableStateOf(PdfSettings.topToBottom.value || isSingleBar()) }
        var single by remember { mutableStateOf(isSingleBar()) }
        Heading(R.string.ob_bar_title, R.string.ob_bar_text)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (atBottom in listOf(false, true)) {
                val selected = bottom == atBottom
                Column(
                    Modifier.weight(1f).clip(MaterialTheme.shapes.large)
                        .border(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
                        .clickable {
                            bottom = atBottom
                            // the single bar sits at the bottom: choosing the top ends it
                            if (!atBottom) single = false
                            applyLayout(single, atBottom)
                        }
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PhoneSketch(atBottom, singleBar = atBottom && single)
                    Text(
                        stringResource(if (atBottom) R.string.ob_bar_bottom else R.string.ob_bar_top),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        SwitchLine(R.string.ob_single_bar, single, R.string.ob_single_bar_sum) {
            single = it
            if (it) bottom = true
            applyLayout(single, bottom)
        }
    }

    /**
     * The tab overview's layout (`ZoneSettings.tabListType`, the same setting as the overview's own layout menu): five
     * cards, each with a small sketch of that layout.
     */
    @Composable
    private fun ColumnScope.OverviewStep() {
        var chosen by remember { mutableIntStateOf(ZoneSettings.tabListType.value.coerceIn(0, 4)) }
        Heading(R.string.ob_overview_title, R.string.ob_overview_text)
        val layouts = listOf(
            0 to R.string.thumb_mode, 1 to R.string.list_mode, 2 to R.string.simple_mode,
            3 to R.string.stack_mode, 4 to R.string.carousel_mode,
        )
        for (row in layouts.chunked(2)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for ((value, label) in row) {
                    val selected = chosen == value
                    Column(
                        Modifier.weight(1f).clip(MaterialTheme.shapes.large)
                            .border(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
                            .clickable {
                                chosen = value
                                ZoneSettings.edit(this@OnboardingActivity) { set(ZoneSettings.tabListType, value) }
                            }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        OverviewSketch(value)
                        Text(
                            stringResource(label), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }

    /** A small phone showing the tab overview in [layout]: 0 grid, 1 list, 2 simple, 3 stack, 4 carousel. */
    @Composable
    private fun OverviewSketch(layout: Int) {
        val outline = MaterialTheme.colorScheme.outline
        val card = MaterialTheme.colorScheme.secondaryContainer
        val page = MaterialTheme.colorScheme.surfaceContainerHighest
        val line = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
        val small = RoundedCornerShape(4.dp)
        @Composable
        fun Card(modifier: Modifier) = Box(modifier.clip(small).background(card).padding(3.dp)) {
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(2.dp)).background(page))
        }
        Box(
            Modifier.width(68.dp).aspectRatio(0.55f).clip(RoundedCornerShape(12.dp)).border(2.dp, outline, RoundedCornerShape(12.dp)).padding(6.dp),
        ) {
            when (layout) {
                0 -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(2) { Card(Modifier.weight(1f).aspectRatio(0.8f)) }
                        }
                    }
                }
                1 -> Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(6) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Card(Modifier.size(16.dp, 12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(line))
                                Box(Modifier.fillMaxWidth(0.6f).height(3.dp).clip(CircleShape).background(line))
                            }
                        }
                    }
                }
                2 -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(9) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(5.dp).clip(CircleShape).background(line))
                            Box(Modifier.fillMaxWidth(if (it % 3 == 1) 0.6f else 0.85f).height(3.dp).clip(CircleShape).background(line))
                        }
                    }
                }
                3 -> Box(Modifier.fillMaxSize()) {
                    // as in the overview: a large front card with its title strip, the neighbouring cards (a bit
                    // narrower) peeking out above and below it
                    for (top in listOf(true, false)) {
                        Card(
                            Modifier.align(if (top) Alignment.TopCenter else Alignment.BottomCenter).padding(horizontal = 4.dp)
                                .fillMaxWidth().fillMaxHeight(0.3f).border(1.dp, outline, small),
                        )
                    }
                    Column(
                        Modifier.align(Alignment.Center).fillMaxWidth().fillMaxHeight(0.66f).clip(small).background(card)
                            .border(1.dp, outline, small).padding(3.dp),
                    ) {
                        Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(2.dp)).background(page))
                        Box(Modifier.padding(top = 3.dp).fillMaxWidth(0.7f).height(3.dp).clip(CircleShape).background(line))
                    }
                }
                else -> Row(
                    Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(Modifier.width(10.dp).fillMaxHeight(0.62f))
                    Card(Modifier.weight(1f).fillMaxHeight(0.8f))
                    Card(Modifier.width(10.dp).fillMaxHeight(0.62f))
                }
            }
        }
    }

    /** The layout is the single bottom bar (no top bar, the address bar among the bottom buttons). */
    private fun isSingleBar(): Boolean {
        val top = com.mycompany.app.pref.store.MainSettings.topItems.value
        return top.isNullOrEmpty() && com.mycompany.app.pref.store.MainSettings.botItems.value == com.mycompany.app.ui.settings.SINGLE_BOT
    }

    /**
     * Writes the bar layout: the single bottom bar (the Layout editor's "single bar" preset), or the classic two bars
     * with the address bar at the top or (moved) at the bottom.
     */
    private fun applyLayout(single: Boolean, bottom: Boolean) {
        val wasSingle = isSingleBar()
        if (single != wasSingle) {
            com.mycompany.app.pref.store.MainSettings.edit(this) {
                if (single) {
                    set(com.mycompany.app.pref.store.MainSettings.topItems, "")
                    set(com.mycompany.app.pref.store.MainSettings.botItems, com.mycompany.app.ui.settings.SINGLE_BOT)
                    set(com.mycompany.app.pref.store.MainSettings.botLongs, com.mycompany.app.ui.settings.SINGLE_LONG)
                    set(com.mycompany.app.pref.store.MainSettings.botSwipe, com.mycompany.app.ui.settings.SINGLE_SWIPE)
                } else {
                    set(com.mycompany.app.pref.store.MainSettings.topItems, com.mycompany.app.ui.settings.DEFAULT_TOP)
                    set(com.mycompany.app.pref.store.MainSettings.botItems, com.mycompany.app.ui.settings.DEFAULT_BOT)
                    set(com.mycompany.app.pref.store.MainSettings.botLongs, com.mycompany.app.ui.settings.DEFAULT_LONG)
                    set(com.mycompany.app.pref.store.MainSettings.botSwipe, com.mycompany.app.ui.settings.DEFAULT_SWIPE)
                }
            }
        }
        PdfSettings.edit(this) { set(PdfSettings.topToBottom, !single && bottom) }
    }

    /**
     * A small phone: the address bar at the top or at the bottom above the button bar, or ([singleBar]) one bar at the
     * bottom holding the buttons and the address field.
     */
    @Composable
    private fun PhoneSketch(addressAtBottom: Boolean, singleBar: Boolean = false) {
        val outline = MaterialTheme.colorScheme.outline
        val bar = MaterialTheme.colorScheme.secondaryContainer
        val pill = MaterialTheme.colorScheme.surface
        val dot = MaterialTheme.colorScheme.onSecondaryContainer
        @Composable
        fun AddressBar() = Box(Modifier.fillMaxWidth().height(18.dp).background(bar), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxWidth(0.7f).height(10.dp).clip(CircleShape).background(pill))
        }
        @Composable
        fun Buttons() = Row(
            Modifier.fillMaxWidth().height(14.dp).background(bar).padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
        ) { repeat(5) { Box(Modifier.size(4.dp).clip(CircleShape).background(dot)) } }
        @Composable
        fun SingleBar() = Row(
            Modifier.fillMaxWidth().height(20.dp).background(bar).padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(2) { Box(Modifier.size(4.dp).clip(CircleShape).background(dot)) }
            Box(Modifier.weight(1f).height(10.dp).clip(CircleShape).background(pill))
            repeat(2) { Box(Modifier.size(4.dp).clip(CircleShape).background(dot)) }
        }
        Column(
            Modifier.width(96.dp).aspectRatio(0.5f).clip(RoundedCornerShape(14.dp)).border(2.dp, outline, RoundedCornerShape(14.dp)).padding(4.dp),
        ) {
            if (!addressAtBottom) AddressBar()
            Column(Modifier.weight(1f).fillMaxWidth().padding(6.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(5) { Box(Modifier.fillMaxWidth(if (it % 2 == 0) 1f else 0.7f).height(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest)) }
            }
            if (singleBar) {
                SingleBar()
            } else {
                if (addressAtBottom) AddressBar()
                Buttons()
            }
        }
    }

    @Composable
    private fun ColumnScope.SearchStep() {
        var selected by remember { mutableIntStateOf(ZtwoSettings.searchType.value) }
        Heading(R.string.ob_search_title, R.string.ob_search_text)
        for (index in ENGINES) {
            val isSel = selected == index
            Row(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
                    .background(if (isSel) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)
                    .clickable { chooseEngine(index); selected = index }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EngineIcon(index)
                Text(
                    stringResource(MainConst.engineNames[index]), style = MaterialTheme.typography.bodyLarge,
                    color = if (isSel) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                )
                if (isSel) Icon(painterResource(R.drawable.outline_check_white_24), null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }

    /** The engine's own site icon ([com.mycompany.app.ui.components.EngineLogo]), else the plain search glyph tinted for the theme. */
    @Composable
    private fun EngineIcon(index: Int) {
        com.mycompany.app.ui.components.EngineLogo(index, 28.dp) {
            Icon(painterResource(MainConst.engineIcons[index]), null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    /** As the engine list's choice of a built-in engine: select it, make sure it is shown, store. */
    private fun chooseEngine(index: Int) {
        if (ZtwoSettings.searchType.value != index) {
            ZtwoSettings.editLocal { set(ZtwoSettings.searchType, index) }
            ZtwoSettings.editLocal { set(ZtwoSettings.searchUser, null) }
            ZtwoSettings.editLocal { set(ZtwoSettings.searchColor, 0) }
            PrefZtwo.saveSearch(applicationContext)
        }
        val enabled = MainUtil.zoneFlags()
        if (index < enabled.size && !enabled[index]) {
            enabled[index] = true
            ZoneSettings.edit(applicationContext) { set(ZoneSettings.searchUse, enabled.joinToString("/") { if (it) "1" else "0" }) }
        }
    }

    @Composable
    private fun ColumnScope.PrivacyStep() {
        var ads by remember { mutableStateOf(BrowserSettings.adsBlock.value) }
        // before the default filter lists are registered the choice is stored for that ([PdfSettings.cookieSeed]);
        // afterwards it switches the lists, as the Web Filter settings do
        var cookies by remember { mutableStateOf(PdfSettings.cookieSeed.value) }
        LaunchedEffect(Unit) {
            if (!PdfSettings.filterInit.value) {
                cookies = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.mycompany.app.dialog.DialogSetFilter.cookieBlockOn(applicationContext)
                }
            }
        }
        Heading(R.string.ob_privacy_title, R.string.ob_privacy_text)
        SwitchLine(R.string.ads_block, ads) {
            ads = it
            BrowserSettings.edit(this@OnboardingActivity) { set(BrowserSettings.adsBlock, it) }
        }
        SwitchLine(R.string.cookie_block, cookies, R.string.cookie_block_info) { on ->
            cookies = on
            PdfSettings.edit(this@OnboardingActivity) { set(PdfSettings.cookieSeed, on) }
            if (!PdfSettings.filterInit.value) {
                val context = applicationContext
                Thread { com.mycompany.app.dialog.DialogSetFilter.setCookieBlock(context, on) }.start()
            }
        }
    }

    @Composable
    private fun ColumnScope.DataStep() {
        val context: Context = this@OnboardingActivity
        val transfer = rememberBrowserTransfer()
        val pickBackup = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            val uriText = uri?.toString()
            if (result.resultCode != RESULT_OK || uri == null || TextUtils.isEmpty(uriText)) return@rememberLauncherForActivityResult
            if ("dat" != MainUtil.lowerExtOf(MainUri.treeDisplayPath(context, uriText))) {
                MainUtil.showToast(context, R.string.invalid_file)
                return@rememberLauncherForActivityResult
            }
            MainUtil.keepUriPermission(context, uri)
            restoreBackup(uriText!!)
        }
        Heading(R.string.ob_data_title, R.string.ob_data_text)
        DataCard(R.string.ob_restore_backup, R.string.ob_restore_backup_sum) {
            try {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                pickBackup.launch(intent)
            } catch (_: Exception) {
            }
        }
        DataCard(R.string.transfer_import_bookmarks, R.string.transfer_import_bookmarks_summary, transfer.importBookmarks)
        DataCard(R.string.transfer_import_passwords, R.string.transfer_import_passwords_summary, transfer.importPasswords)
    }

    @Composable
    private fun DataCard(title: Int, summary: Int, onClick: () -> Unit) {
        OutlinedButton(
            onClick = onClick, shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(summary), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    /** A restored OPUM backup replaces the settings, so the wizard ends and the app restarts with them. */
    private fun restoreBackup(uri: String) {
        var applied = false
        com.mycompany.app.ui.prefs.DialogShow.direct(
            this,
            onDismiss = {
                if (applied) {
                    SyncSettings.edit(applicationContext) { set(SyncSettings.splash, false) }
                    MainUtil.restartActivity(this, null, false)
                }
            },
        ) { host -> DialogBackupLoad(host, uri, object : DialogApplyListener { override fun onApply() { applied = true } }) }
    }

    @Composable
    private fun ColumnScope.DefaultStep() {
        val roles = remember { getSystemService(RoleManager::class.java) }
        var isDefault by remember { mutableStateOf(roles?.isRoleHeld(RoleManager.ROLE_BROWSER) == true) }
        val ask = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isDefault = roles?.isRoleHeld(RoleManager.ROLE_BROWSER) == true
        }
        Heading(R.string.ob_default_title, R.string.ob_default_text)
        if (isDefault) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.outline_check_white_24), null, tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.ob_default_done), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
            }
        } else {
            Button(
                onClick = {
                    try {
                        if (roles != null && roles.isRoleAvailable(RoleManager.ROLE_BROWSER)) ask.launch(roles.createRequestRoleIntent(RoleManager.ROLE_BROWSER))
                        else startActivity(Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS"))
                    } catch (_: Exception) {
                        try { startActivity(Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS")) } catch (_: Exception) {}
                    }
                },
            ) { Text(stringResource(R.string.default_browser)) }
        }
    }

    /**
     * Asks for the notification permission (downloads, incognito reminder). The first tap shows the system question;
     * once that was answered (or Android no longer shows it), the button opens the app's notification settings instead.
     */
    @Composable
    private fun ColumnScope.NotifyStep() {
        val context: Context = this@OnboardingActivity
        // MainUtil.hasPermission returns true when the permission is still missing
        var allowed by remember { mutableStateOf(!MainUtil.hasPermission(context, 5)) }
        var asked by rememberSaveable { mutableStateOf(false) }
        val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            allowed = !MainUtil.hasPermission(context, 5)
            asked = true
        }
        OnScreenResume { allowed = !MainUtil.hasPermission(context, 5) }
        Heading(R.string.ob_notify_title, R.string.ob_notify_text)
        if (allowed) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.outline_check_white_24), null, tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.ob_notify_done), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
            }
        } else {
            Button(
                onClick = {
                    if (!asked && android.os.Build.VERSION.SDK_INT >= 33) {
                        try { ask.launch(android.Manifest.permission.POST_NOTIFICATIONS) } catch (_: Exception) { asked = true }
                    } else {
                        try {
                            startActivity(
                                Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                                    .putExtra("android.provider.extra.APP_PACKAGE", context.packageName),
                            )
                        } catch (_: Exception) {
                        }
                    }
                },
            ) { Text(stringResource(R.string.ob_notify_allow)) }
        }
    }

    @Composable
    private fun ColumnScope.DoneStep() {
        Spacer(Modifier.height(48.dp))
        Box(
            Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.outline_check_white_24), null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.ob_done_title), style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
        Text(
            stringResource(R.string.ob_done_text), style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
    }

    companion object {
        private const val STEPS = 10

        /** Built-in engines offered, most common first (YouTube left out: it is not a web search). */
        private val ENGINES = intArrayOf(4, 3, 1, 6, 7, 8, 0, 5, 2)

        /** The wizard is open (so the browser does not open it twice, e.g. when it is re-created). */
        @Volatile
        @JvmStatic
        var isOpen = false
            private set

        /** Opens the wizard from the browser on first start. */
        @JvmStatic
        fun open(context: Context) {
            if (isOpen) return
            isOpen = true
            try {
                context.startActivity(Intent(context, OnboardingActivity::class.java))
            } catch (_: Exception) {
                isOpen = false
            }
        }
    }
}
