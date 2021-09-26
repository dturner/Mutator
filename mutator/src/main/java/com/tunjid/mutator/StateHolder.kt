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

package com.tunjid.mutator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface StateHolder<Action : Any, State : Any> {
    val state: StateFlow<State>
    val accept: (Action) -> Unit
}

fun <State: Any> StateHolder<Mutation<State>, State>.accept(
    mutator: State.() -> State
) = accept(Mutation(mutator))

fun <Action : Any, State : Any> scopedStateHolder(
    scope: CoroutineScope,
    initialState: State,
    started: SharingStarted = SharingStarted.WhileSubscribed(DefaultStopTimeoutMillis),
    transform: (Flow<Action>) -> Flow<Mutation<State>>
): StateHolder<Action, State> = object : StateHolder<Action, State> {
    val actions = MutableSharedFlow<Action>()

    override val state: StateFlow<State> =
        transform(actions)
            .reduceInto(initialState)
            .stateIn(
                scope = scope,
                started = started,
                initialValue = initialState
            )

    override val accept: (Action) -> Unit = { action ->
        scope.launch {
            // Suspend till downstream is connected
            actions.subscriptionCount.first { it > 0 }
            actions.emit(action)
        }
    }
}

fun <State : Any, SubState : Any> StateHolder<Mutation<State>, State>.derived(
    scope: CoroutineScope,
    mapper: (State) -> SubState,
    mutator: (State, SubState) -> State
) = object : StateHolder<Mutation<SubState>, SubState> {
    override val state: StateFlow<SubState> =
        this@derived.state
            .map { mapper(it) }
            .distinctUntilChanged()
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = mapper(this@derived.state.value)
            )

    override val accept: (Mutation<SubState>) -> Unit = { mutation ->
        this@derived.accept(Mutation {
            val currentState = this
            val mapped = mapper(currentState)
            val mutated = mutation.mutate(mapped)
            mutator(currentState, mutated)
        })
    }
}

private const val DefaultStopTimeoutMillis = 5000L