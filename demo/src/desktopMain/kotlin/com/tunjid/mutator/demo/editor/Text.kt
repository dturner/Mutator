/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tunjid.mutator.demo.editor

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
actual fun Heading1(text: String) {
    StyledText(
        text = text,
        style = MaterialTheme.typography.h3.copy(
            MaterialTheme.colors.onSurface
        )
    )
}

@Composable
actual fun Heading2(text: String) {
    StyledText(
        modifier = Modifier.padding(vertical = 8.dp),
        text = text,
        style = MaterialTheme.typography.h4.copy(
            MaterialTheme.colors.onSurface
        )
    )
}

@Composable
actual fun Heading3(text: String) {
    StyledText(
        modifier = Modifier.padding(vertical = 8.dp),
        text = text,
        style = MaterialTheme.typography.h5.copy(
            MaterialTheme.colors.onSurface
        )
    )
}

@Composable
actual fun Paragraph(text: String) {
    StyledText(
        modifier = Modifier.padding(vertical = 8.dp),
        text = text,
        style = MaterialTheme.typography.body1.copy(
            MaterialTheme.colors.onSurface
        )
    )
}


@Composable
private fun StyledText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        style = style,
        text = text
    )
}
