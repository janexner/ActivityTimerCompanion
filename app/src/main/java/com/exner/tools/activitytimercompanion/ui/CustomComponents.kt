package com.exner.tools.activitytimercompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.exner.tools.activitytimercompanion.ui.theme.Theme

@Composable
fun HeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
    )
}

@Composable
fun BodyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier,
    )
}

@Composable
fun SmallBodyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
    )
}

@Composable
fun TextAndSwitch(
    text: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?
) {
    ListItem(
        headlineContent = {
            Text(
                text = text,
                maxLines = 3,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
            )
        },
    )
}

@Composable
fun TextAndTriStateToggle(
    text: String,
    currentTheme: Theme,
    updateTheme: (Theme) -> Unit
) {
    val states = listOf(
        Theme.Auto,
        Theme.Dark,
        Theme.Light,
    )

    ListItem(
        headlineContent = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        trailingContent = {
            Surface(
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .wrapContentSize()
            ) {
                Row(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    states.forEach { thisTheme ->
                        Text(
                            text = thisTheme.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(50))
                                .clickable {
                                    updateTheme(thisTheme)
                                }
                                .background(
                                    if (thisTheme == currentTheme) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                                .padding(
                                    vertical = 8.dp,
                                    horizontal = 16.dp,
                                ),
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun DefaultSpacer() {
    Spacer(modifier = Modifier.size(16.dp))
}

@Composable
fun IconSpacer() {
    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
}

@Composable
fun TextFieldForTimes(
    value: Int,
    label: @Composable (() -> Unit)?,
    onValueChange: (Int) -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    TextField(
        value = text,
        label = label,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        onValueChange = { raw ->
            text = raw
            if (raw.isNotEmpty() && raw.isDigitsOnly()) {
                val parsed = text.toInt()
                onValueChange(parsed)
            }
        },
        placeholder = placeholder,
        textStyle = MaterialTheme.typography.bodyLarge
    )
}