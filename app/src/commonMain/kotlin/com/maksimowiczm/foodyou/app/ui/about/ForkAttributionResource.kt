package com.maksimowiczm.foodyou.app.ui.about

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun forkAttributionResource(style: TextStyle = LocalTextStyle.current): AnnotatedString {
    val str = stringResource(Res.string.headline_fork_of_food_you)
    val link = stringResource(Res.string.link_food_you)
    val primary = MaterialTheme.colorScheme.primary

    return remember(str, link, primary, style) {
        buildAnnotatedString {
            val split = str.split(" ")
            split.forEachIndexed { index, word ->
                if (word.contains("Food") || word.contains("You")) {
                    withLink(LinkAnnotation.Url(link)) {
                        withStyle(style.merge(primary).toSpanStyle()) { append(word) }
                    }
                } else {
                    append(word)
                }

                if (index < split.lastIndex) {
                    append(" ")
                }
            }
        }
    }
}
