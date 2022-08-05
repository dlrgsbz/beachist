package app.beachist.shared.base

import io.reactivex.Maybe
import io.reactivex.Observable

fun <T, R> Observable<T>.mapNotNull(body: (T) -> R?): Observable<R> {
    return filter { body(it) != null }.map { body(it)!! }
}


fun <T, R> Maybe<T>.mapNotNull(body: (T) -> R?): Maybe<R> {
    return filter { body(it) != null }.map { body(it)!! }
}
