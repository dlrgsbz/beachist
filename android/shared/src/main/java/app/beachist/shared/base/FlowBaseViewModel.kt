package app.beachist.shared.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

abstract class FlowBaseViewModel<
        Action : ViewModelAction,
        State : ViewModelState,
        Effect : ViewModelEffect,
        >(private val emptyState: State) : ViewModel() {
    protected val _state = MutableStateFlow(emptyState)
    val state: StateFlow<State> = _state

    protected val actions: MutableStateFlow<Action?> = MutableStateFlow(null)
    protected val effects: MutableStateFlow<Effect?> = MutableStateFlow(null)

    fun <T> stateOf(extractor: State.() -> T): Flow<T> = state.stateOf(extractor)

    protected abstract fun handleActions()

    /**
     * This attaches a stream of actions to the ViewModel.
     * The actions will be done when passed.
     *
     * @param actions the stream of actions to do when passed.
     */
    fun attach(actions: Flow<Action?>) {
        //Route actions to the internal actions.
        actions
            .onEach {
                Timber.tag(this@FlowBaseViewModel.javaClass.simpleName)
                Timber.d("$it")
            }
            .onEach { this@FlowBaseViewModel.actions.emit(it) }
            .launchIn(viewModelScope)

        effects
            .onEach {
                Timber.tag(this@FlowBaseViewModel.javaClass.simpleName)
                Timber.d("$it")
            }
            .launchIn(viewModelScope)

        // Init the internal routing for Actions.
        handleActions()
    }

    final override fun onCleared() {
        _state.value = emptyState
        clearInternal()
    }

    /**
     * This is for clearing the internal state (example: disposables).
     * It can be implemented, empty by default.
     */
    protected open fun clearInternal() {}

    /**
     * Helper method to directly bind an action to an effect.
     * When the action is done, the effect is emitted directly.
     */
    protected suspend inline fun <reified A : Action, reified E : Effect> bindActionToEffect(noinline converter: (A) -> E) {
        actions.ofType<A>()
            .map(converter)
            .collect(effects::emit)
    }

    /**
     * Returns an observer for one time Effects that have to be displayed.
     * Observe callback will ALWAYS be on MainThread.
     */
    fun effects(): Flow<Effect> = effects.mapNotNull { it }.distinctUntilChanged()

    /**
     * Get the stream of a specific effect [SubEffect] only.
     * Observe callback will ALWAYS be on MainThread.
     */
    inline fun <reified SubEffect : Effect> effect(): Flow<SubEffect> =
        effects().ofType()

}

/**
 * Create a stateOf observer for ViewModel variables.
 * A stateOf can be simply created by using the lambda syntax for values in a class: Class::stateOf.
 */
private fun <S : ViewModelState, T> StateFlow<S>.stateOf(extractor: S.() -> T) =
    this.map { extractor(it) }.distinctUntilChanged()

fun <T> MutableStateFlow<T>.set(fn: T.() -> T) {
    this.value = fn(this.value)
}

inline fun <reified U> Flow<*>.ofType(): Flow<U> {
    return this.map { Timber.tag("ofType").d("$it should be ${U::class.java.simpleName}") }.filter { it is U }
        .map { it as U }
}
