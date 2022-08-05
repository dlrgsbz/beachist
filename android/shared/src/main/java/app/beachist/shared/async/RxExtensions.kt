package app.beachist.shared.async

import io.reactivex.Maybe
import io.reactivex.Observable

fun <T> Observable<T>.toAsync(): Observable<Async<T>> =
    map { Async.Success(it) as Async<T> }
        .onErrorReturn { Async.Failure(it) }
        .startWith(Async.Running())

fun <S, T> Observable<T>.mapToAsync(mapper: (T) -> S): Observable<Async<S>> {
    return map(mapper)
        .map { Async.Success(it) as Async<S> }
        .onErrorReturn { Async.Failure(it) }
}

fun <S, T> Maybe<T>.mapToAsync(mapper: (T) -> S): Maybe<Async<S>> {
    return map(mapper)
        .map { Async.Success(it) as Async<S> }
        .onErrorReturn { Async.Failure(it) }
}
