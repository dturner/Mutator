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

package com.tunjid.mutator.demo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn

const val SPEED = 1600L

enum class Speed(val multiplier: Int) {
    One(1), Two(2), Three(3), Four(4)
}

val Speed.text get() = when(this) {
    Speed.One -> "1x"
    Speed.Two -> "2x"
    Speed.Three -> "3x"
    Speed.Four -> "4x"
}

fun intervalFlow(intervalMillis: Long) = flow {
    var value = 0L
    while (true) {
        emit(++value)
        delay(intervalMillis)
    }
}

fun CoroutineScope.speedFlow() =
    intervalFlow(4000)
        .map { Speed.values().random() }
        .shareIn(this, SharingStarted.WhileSubscribed())

fun Flow<Speed>.toInterval(): Flow<Long> =
    flatMapLatest {
        intervalFlow(SPEED / it.multiplier)
    }

fun <T> Flow<T>.toProgress(): Flow<Float> =
    scan(0f) { progress, _ -> (progress + 1) % 100 }

data class ColorInterpolationProgress(
    val progress: Float,
    val colors: List<Color>
)

fun interpolateColors(
    startColors: IntArray,
    endColors: IntArray
): Flow<ColorInterpolationProgress> = (0..100).asFlow()
    .onEach { delay(50) }
    .map { percentage ->
        val fraction = percentage.toFloat() / 100F
        val colors = startColors
            .zip(endColors)
            .map { (start, end) ->
                interpolateColors(
                    fraction = fraction,
                    startValue = start,
                    endValue = end
                )
            }
            .map(Int::toLong)
            .map(::Color)

        ColorInterpolationProgress(
            progress = fraction,
            colors = colors,
        )
    }
