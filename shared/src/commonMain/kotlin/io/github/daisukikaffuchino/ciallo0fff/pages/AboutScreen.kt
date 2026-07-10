package io.github.daisukikaffuchino.ciallo0fff.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ciallo0fff.shared.generated.resources.*
import ciallo0fff.shared.generated.resources.Res
import ciallo0fff.shared.generated.resources.avatar
import ciallo0fff.shared.generated.resources.ic_article
import ciallo0fff.shared.generated.resources.ic_chat_info
import ciallo0fff.shared.generated.resources.ic_code
import ciallo0fff.shared.generated.resources.ic_emoji_objects
import ciallo0fff.shared.generated.resources.ic_github
import ciallo0fff.shared.generated.resources.ic_launcher
import ciallo0fff.shared.generated.resources.ic_person_edit
import com.yhz.composetoast.Toast
import io.github.daisukikaffuchino.ciallo0fff.AppVersionDisplay
import io.github.daisukikaffuchino.ciallo0fff.PlatformActions
import io.github.daisukikaffuchino.ciallo0fff.ScrollablePage
import io.github.daisukikaffuchino.ciallo0fff.SettingValueRow
import io.github.daisukikaffuchino.ciallo0fff.expressiveContainerColor
import io.github.daisukikaffuchino.ciallo0fff.expressiveShape
import io.github.daisukikaffuchino.ciallo0fff.segmentedSection
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private const val DeveloperUrl = "https://github.com/daisukiKaffuChino"
private const val SourceUrl = "https://github.com/daisukiKaffuChino/Ciallo0FFF-Multiplatform"

@Composable
internal fun AboutScreen(modifier: Modifier, hideDesktopScrollbar: Boolean) {
    var versionClicks by remember { mutableStateOf(0) }
    var firstVersionClick by remember { mutableStateOf<TimeMark?>(null) }
    val cloverToast = stringResource(Res.string.toast_clover)

    fun onVersionClick() {
        val firstClick = firstVersionClick
        if (firstClick == null || firstClick.elapsedNow() > 1_200.milliseconds) {
            firstVersionClick = TimeSource.Monotonic.markNow()
            versionClicks = 1
        } else {
            versionClicks += 1
        }
        if (versionClicks >= 5) {
            Toast.show(cloverToast)
            versionClicks = 0
            firstVersionClick = null
        }
    }

    ScrollablePage(
        modifier = modifier,
        verticalSpacing = 8.dp,
        hideDesktopScrollbar = hideDesktopScrollbar,
    ) {
        item {
            Surface(
                shape = expressiveShape(),
                color = expressiveContainerColor(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(22.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.size(64.dp),
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.ic_launcher),
                            contentDescription = stringResource(Res.string.app_name),
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            stringResource(Res.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            stringResource(Res.string.app_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        segmentedSection(Res.string.section_project) {
            SettingValueRow(stringResource(Res.string.about_version), AppVersionDisplay, Res.drawable.ic_code) {
                onVersionClick()
            }
            SettingValueRow(
                stringResource(Res.string.about_app_developer),
                stringResource(Res.string.about_app_developer_summary),
                Res.drawable.ic_person_edit,
                trailingIcon = Res.drawable.avatar,
                trailingIconTint = false,
                trailingIconSize = 32.dp,
            ) {
                PlatformActions.openUrl(DeveloperUrl)
            }
            SettingValueRow(
                stringResource(Res.string.about_open_source),
                stringResource(Res.string.about_open_source_summary),
                Res.drawable.ic_github,
            ) {
                PlatformActions.openUrl(SourceUrl)
            }
            SettingValueRow(
                stringResource(Res.string.about_changelog),
                stringResource(Res.string.about_changelog_summary),
                Res.drawable.ic_article,
            ) {}
        }
        segmentedSection(Res.string.section_other) {
            SettingValueRow(
                title = stringResource(Res.string.about_overview),
                summary = stringResource(Res.string.about_overview_summary),
                icon = Res.drawable.ic_chat_info,
            ) {}
            SettingValueRow(
                title = stringResource(Res.string.about_tips),
                summary = stringResource(Res.string.about_tips_summary),
                icon = Res.drawable.ic_emoji_objects,
            ) {}
        }
    }
}
