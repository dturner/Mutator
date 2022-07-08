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

package com.tunjid.mutator.demo.snails

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.editor.HorizontalLayout
import com.tunjid.mutator.demo.editor.Paragraph
import com.tunjid.mutator.demo.editor.StyledDiv
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Input

@Composable
actual fun SnailCard(content: @Composable () -> Unit) {
    StyledDiv(content, "card")
}

@Composable
actual fun Snail(
    progress: Float,
    speed: Speed,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Input(
        type = InputType.Range,
        attrs = {
            classes("snail", "horizontallyCentered")
            style {
                val r = (color.red * 255).toInt().toHexString()
                val g = (color.green * 255).toInt().toHexString()
                val b = (color.blue * 255).toInt().toHexString()
                val hex = "#$r$g$b"
                property("--snailColor", hex)
            }
            min("0")
            max("100")
            value(progress)
            onInput { onValueChange(it.value?.toFloat() ?: 0f) }
        }
    )
}

@Composable
actual fun ColorSwatch(
    colors: List<Color>,
    onColorClicked: (Int) -> Unit
) {
    HorizontalLayout(centerOnMainAxis = true) {
        colors.forEachIndexed { index, color ->
            Button(
                attrs = {
                    classes("colorSwatch")
                    style {
                        backgroundColor(
                            rgb(
                                r = (color.red * 255).toInt(),
                                g = (color.green * 255).toInt(),
                                b = (color.blue * 255).toInt(),
                            )
                        )
                    }
                    onClick { onColorClicked(index) }
                },
                content = {
                }
            )
        }
    }
}

@Composable
actual fun ToggleButton(
    onClicked: () -> Unit
) {
    Button(
        attrs = {
            classes("toggleButton", "horizontallyCentered")
            title("Toggle mode")
            onClick { onClicked() }
        },
        content = {
            Paragraph("Toggle mode")
        }
    )
}

private fun Int.toHexString(): String {
    val hex = this.toString(16)
    return if (hex.length == 1) "0$hex" else hex
}