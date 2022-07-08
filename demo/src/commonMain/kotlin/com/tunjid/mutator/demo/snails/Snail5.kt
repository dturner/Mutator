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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.coroutines.emit
import com.tunjid.mutator.demo.MutedColors
import com.tunjid.mutator.demo.SPEED
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.editor.ColorSwatch
import com.tunjid.mutator.demo.editor.Paragraph
import com.tunjid.mutator.demo.editor.Snail
import com.tunjid.mutator.demo.editor.SnailCard
import com.tunjid.mutator.demo.editor.VerticalLayout
import com.tunjid.mutator.demo.intervalFlow
import com.tunjid.mutator.demo.speedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class Snail5State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
    val color: Color = Color.Blue,
    val colors: List<Color> = MutedColors.colors(false).map(::Color)
)

class Snail5StateHolder(
    private val scope: CoroutineScope
) {

    private val speed: Flow<Speed> = scope.speedFlow()

    private val speedChanges: Flow<Mutation<Snail5State>> = speed
        .map { Mutation { copy(speed = it) } }

    private val progressChanges: Flow<Mutation<Snail5State>> = speed
        .flatMapLatest {
            intervalFlow(SPEED * it.multiplier)
        }
        .map { Mutation { copy(progress = (progress + 1) % 100) } }

    private val userChanges = MutableSharedFlow<Mutation<Snail5State>>()

    val state: StateFlow<Snail5State> = merge(
        progressChanges,
        speedChanges,
        userChanges,
    )
        .scan(Snail5State()) { state, mutation -> mutation.mutate(state) }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Snail5State()
        )

    fun setSnailColor(index: Int) {
        scope.launch {
            userChanges.emit { copy(color = colors[index]) }
        }
    }

    fun setProgress(progress: Float) {
        scope.launch {
            userChanges.emit { copy(progress = progress) }
        }
    }
}

@Composable
fun Snail5() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail5StateHolder(scope) }
    val state by stateHolder.state.collectAsState()

    SnailCard {
        VerticalLayout {
            Paragraph(
                text = "Snail5"
            )
            Snail(
                progress = state.progress,
                color = state.color,
                onValueChange = { stateHolder.setProgress(it) }
            )
            ColorSwatch(
                colors = state.colors,
                onColorClicked = {
                    stateHolder.setSnailColor(it)
                }
            )
            Paragraph(
                text = "Progress: ${state.progress}; Speed: ${state.speed}"
            )
        }
    }
}