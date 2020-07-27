package de.tjarksaul.wachmanager.api

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//interface RequestQueue {
//
//    suspend fun <T> handleWithDelay(block: () -> T): T
//
//    fun <T> addRequest(block: () -> T): T
//
//}
//
//class RequestQueueImpl : RequestQueue, CoroutineScope {
//    private val job = Job()
//    override val coroutineContext = Dispatchers.Unconfined + job
//    @ObsoleteCoroutinesApi
//    private val delayedHandlerActor = delayedRequestHandlerActor()
//
//    @ObsoleteCoroutinesApi
//    override suspend fun <T> handleWithDelay(block: () -> T): T {
//        val result = CompletableDeferred<T>()
//        delayedHandlerActor.send(DelayedHandlerMsg(result, block))
//        return result.await()
//    }
//}
//
//private data class DelayedHandlerMsg<RESULT>(val result: CompletableDeferred<RESULT>, val block: () -> RESULT)
//
//@ObsoleteCoroutinesApi
//private fun CoroutineScope.delayedRequestHandlerActor() = actor<DelayedHandlerMsg<*>>() {
//    for (message in channel) {
//        try {
//            println("got a message processing")
//            @Suppress("UNCHECKED_CAST")
//            val msgCast = message as DelayedHandlerMsg<Any?>
//            val result = msgCast.block()
//            println(result)
//            msgCast.result.complete(result)
//        } catch (e: Exception) {
//            message.result.completeExceptionally(e)
//        }
//    }
//}

//
//object RequestQueue {
//    private val requests: Request[]
//}

interface RequestCallback<T> {
    fun onFailure()

    fun onResponse(response: T)
}

class RetryingRequest<T>(request: Call<T>, callback: RequestCallback<T>, maxRetry: Int = 3) {
    private var retryCount = 0

    private val internalCallback = object : Callback<T> {
        override fun onFailure(call: Call<T>?, t: Throwable?) {
            Log.e("RequestQueue", "Problem calling Backend API {${t?.message}}")
            if (retryCount < maxRetry) {
                retry()
            } else {
                callback.onFailure()
            }
        }

        override fun onResponse(
            call: Call<T>?,
            response: Response<T>?
        ) {
            response?.isSuccessful.let {
                if (it !== null && it) {
                    callback.onResponse(response!!.body()!!)
                } else {
                    if (retryCount < maxRetry) {
                        retry()
                    } else {
                        callback.onFailure()
                    }
                }
            }
        }
    }

    fun retry() {
        retryCount++
        val request = requestClone.clone()
        request.enqueue(internalCallback)
    }

    private var requestClone: Call<T> = request.clone()

    init {
        request.enqueue(internalCallback)
    }
}