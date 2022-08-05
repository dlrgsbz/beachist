package app.beachist.shared.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <T> Flow<T>.delay(millis: Long): Flow<T> = this.map {
    kotlinx.coroutines.delay(millis)
    it
}
