package de.tjarksaul.wachmanager.modules.base

import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import timber.log.Timber


abstract class BaseViewModel<
        Action : ViewModelAction,
        State : ViewModelState,
        Effect : ViewModelEffect
        >(emptyState: State) : ViewModel() {

    //These are the interactors with the outer world>
    protected val actions: PublishSubject<Action> = PublishSubject.create<Action>()
    protected val effects: PublishSubject<Effect> = PublishSubject.create<Effect>()

    //The internal state handled by MvRx. Changes will automatically be pushed to [states]
    protected val state = RxViewState(emptyState)

    //The used disposable to kill all running task.
    protected val disposables = CompositeDisposable()

    /**
     * This handles the initialization of all Action callbacks for the [Action].
     */
    protected abstract fun handleActions()

    /**
     * This attaches a stream of actions to the ViewModel.
     * The actions will be done when passed.
     *
     * @param actions the stream of actions to do when passed.
     */
    fun attach(actions: Observable<Action>) {
        disposables.clear()

        //Route actions to the internal actions.
        disposables += actions
            .doOnNext {
                Timber.tag(this@BaseViewModel.javaClass.simpleName)
                Timber.d("$it")
            }
            .subscribe { this.actions.onNext(it) }

        disposables += effects.subscribe {
            Timber.tag(this@BaseViewModel.javaClass.simpleName)
            Timber.d("$it")
        }

        //Init the internal routing for Actions.
        handleActions()
    }

    final override fun onCleared() {
        disposables.clear()
        state.dispose()
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
    protected inline fun <reified A : Action, reified E : Effect> bindActionToEffect(noinline converter: (A) -> E) {
        disposables += actions.ofType<A>()
            .map(converter)
            .subscribe(effects::onNext)
    }

    /**
     * returns an observer for updates to the complete State.
     * Observe callback will ALWAYS be on MainThread.
     */
    fun states(): Observable<State> = state.observable.observeOnMainThread()

    /**
     * Observes a single Variable of the State.
     * Observe callback will ALWAYS be on MainThread.
     *
     * TODO: (BUG) This does not work with Optionals at the moment, when null is added.
     */
    fun <T> stateOf(extractor: State.() -> T): Observable<T> = state.observable
        .stateOf(extractor)
        .distinctUntilChanged()
        .observeOnMainThread()

    /**
     * Observes a single Variable of the State.
     * Observe callback will ALWAYS be on MainThread.
     */
    protected fun <T> Observable<T>.filterFromState(extractor: State.() -> Boolean): Observable<T> =
        switchMap { oldValue ->
            state.observable.map { extractor(it) }
                .take(1)
                .map { filter -> filter to oldValue }
        }
            .filter { (filter, _) -> filter }
            .map { (_, oldValue) -> oldValue }

    /**
     * Returns an observer for one time Effects that have to be displayed.
     * Observe callback will ALWAYS be on MainThread.
     */
    fun effects(): Observable<Effect> = effects.observeOnMainThread()

    /**
     * Get the stream of a specific effect [SubEffect] only.
     * Observe callback will ALWAYS be on MainThread.
     */
    inline fun <reified SubEffect : Effect> effect(): Observable<SubEffect> =
        effects().ofType(SubEffect::class.java)

}


//The below are marker interfaces to easily identify the correct types.

/**
 * The actions that may be done to the viewmodel.
 */
interface ViewModelAction

/**
 * The States the Viewmodel handles.
 */
interface ViewModelState

/**
 * The One-Time effects that may occur in the UI.
 * (Example: show a toast)
 */
interface ViewModelEffect


/**
 * Create a stateOf observer for ViewModel variables.
 * A stateOf can be simply created by using the lambda syntax for values in a class: Class::stateOf.
 */
private fun <S : ViewModelState, T> Observable<S>.stateOf(extractor: S.() -> T) =
    this.mapNotNull { extractor(it) }.distinctUntilChanged()

/**
 * Schedules the subscription to be called on the Main Thread.
 */
private fun <T> Observable<T>.observeOnMainThread() = this.observeOn(AndroidSchedulers.mainThread())