package de.tjarksaul.wachmanager.api

import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit

sealed class Async<out T> {
    class Running<out T> : Async<T>()
    data class Success<out T>(val data: T, val isCacheResponse: Boolean = false) : Async<T>()
    data class Failure<out T>(val error: Throwable) : Async<T>()
}

fun <T, R> Observable<Async<T>>.mapData(body: (T) -> R): Observable<Async<R>> {
    return this.map {
        when (it) {
            is Async.Success -> Async.Success(body(it.data), it.isCacheResponse)
            is Async.Failure -> Async.Failure(it.error)
            is Async.Running -> Async.Running<R>()
        }
    }
}

//fun <T, R> Observable<Async<T>>.mapDataNotNull(body: (T) -> R?): Observable<Async<R>> {
//    return this.mapNotNull { item ->
//        when (item) {
//            is Async.Success -> {
//                val data = body(item.data)
//                if (data != null) {
//                    Async.Success(data, item.isCacheResponse)
//                } else {
//                    null
//                }
//            }
//            is Async.Failure -> Async.Failure(item.error)
//            is Async.Running -> Async.Running<R>()
//        }
//    }
//}

fun <T, R> Observable<Async<T>>.flatMapData(body: (T) -> Observable<R>): Observable<Async<R>> {
    return this.flatMap {
        when (it) {
            is Async.Success -> body(it.data).toAsync()
            is Async.Failure -> Observable.just(Async.Failure(it.error))
            is Async.Running -> Observable.just(Async.Running())
        }
    }
}

fun <T, R> Observable<Async<T>>.flatMapDataAsync(body: (T) -> Observable<Async<R>>): Observable<Async<R>> {
    return this.flatMap {
        when (it) {
            is Async.Success -> body(it.data)
            is Async.Failure -> Observable.just(Async.Failure(it.error))
            is Async.Running -> Observable.just(Async.Running())
        }
    }
}

fun <T> Observable<Async<T>>.mapFailure(errorMapper: (Throwable) -> Throwable): Observable<Async<T>> {
    return this.map {
        when (it) {
            is Async.Failure -> Async.Failure(errorMapper(it.error))
            else -> it
        }
    }
}

fun <U, V> Observable<Async<U>>.mapDataSafely(transformer: (U) -> V): Observable<Async<V>> {
    return mapData(transformer).onErrorReturn { Async.Failure(it) }
}

fun <T> Observable<Async<T>>.doOnData(data: (T) -> Unit): Observable<Async<T>> {
    return doOnNext { result ->
        when (result) {
            is Async.Success -> data(result.data)
        }
    }
}

fun <T> Observable<Async<T>>.doOnFailure(handler: (Throwable) -> Unit): Observable<Async<T>> {
    return doOnNext { result ->
        when (result) {
            is Async.Failure -> handler(result.error)
        }
    }
}

fun <T> Observable<Async<T>>.filterRunning() = filter { it !is Async.Running }

fun <T> Observable<Async<T>>.onlySuccess() = filter { it is Async.Success }

fun <T, S> Observable<Async<T>>.switchMapData(
    mapper: (T) -> Observable<S>
): Observable<Async<S>> =
    switchMap { result ->
        when (result) {
            is Async.Success -> mapper(result.data).mapToAsync { it }
            is Async.Running -> Observable.just(Async.Running())
            is Async.Failure -> Observable.just(Async.Failure(result.error))
        }
    }

fun <T, S> Observable<Async<T>>.switchMapDataAsync(
    mapper: (T) -> Observable<Async<S>>
): Observable<Async<S>> =
    switchMap { result ->
        when (result) {
            is Async.Success -> mapper(result.data)
            is Async.Running -> Observable.just(Async.Running())
            is Async.Failure -> Observable.just(Async.Failure(result.error))
        }
    }
