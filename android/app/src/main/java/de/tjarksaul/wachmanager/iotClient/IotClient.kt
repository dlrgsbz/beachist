package de.tjarksaul.wachmanager.iotClient

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.amazonaws.mobileconnectors.iot.*
import com.amazonaws.regions.Regions
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.charset.Charset
import java.util.*


data class IotConfig(
    val iotEndpoint: String,
    val clientId: String,
    val certificateId: String,
    val region: Regions,
    val certificatePem: String,
    val privateKey: String,
)

private const val MQTT_CONNECTION_KEEP_ALIVE_TIME_SECONDS = 30

class IotClient(private val gson: Gson, private val tempDirectory: String) : CoroutineScope {
    private val LOG_TAG = "IotClient"

    private var connection: AWSIotMqttManager? = null
    // todo: this should be a BehaviorSubject
    private val connectionState: MutableLiveData<IotConnectionState> =
        MutableLiveData(IotConnectionState.Initial)
    private var activeConfig: IotConfig? = null

    override val coroutineContext = Dispatchers.IO + SupervisorJob()

    private val connectionCallback =
        AWSIotMqttClientStatusCallback { status, throwable ->
            if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost) {
                Timber.tag(LOG_TAG).i(throwable, "connection interrupted")
                connectionState.postValue(IotConnectionState.ConnectionLost())
            } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                Timber.tag(LOG_TAG).i("Connection restored/established")
                connectionState.postValue(IotConnectionState.Connected)
            }
        }

    @SuppressLint("BinaryOperationInTimber")
    @Synchronized
    fun connect(config: IotConfig) {
        launch {
            if (activeConfig != null || connection != null) {
                Timber.tag(LOG_TAG).d(
                    "connect - Attempted to initiate connection without disconnecting and releasing resources first"
                )
                return@launch
            }

            activeConfig = config
            if (connection == null) {
                connection = AWSIotMqttManager(config.clientId, config.iotEndpoint)
                connection?.keepAlive = MQTT_CONNECTION_KEEP_ALIVE_TIME_SECONDS
                connection?.mqttLastWillAndTestament = lastWill(config)

                val keystoreName = "keystore"
                val keystorePassword = "verysecret"
                if (!AWSIotKeystoreHelper.isKeystorePresent(tempDirectory, keystoreName)) {
                    AWSIotKeystoreHelper.saveCertificateAndPrivateKey(
                        config.certificateId,
                        config.certificatePem,
                        config.privateKey,
                        tempDirectory,
                        keystoreName,
                        keystorePassword,
                    )
                }

                if (!AWSIotKeystoreHelper.keystoreContainsAlias(
                        config.certificateId,
                        tempDirectory,
                        keystoreName,
                        keystorePassword,
                    )
                ) {
                    AWSIotKeystoreHelper.saveCertificateAndPrivateKey(
                        config.certificateId,
                        config.certificatePem,
                        config.privateKey,
                        tempDirectory,
                        keystoreName,
                        keystorePassword,
                    )
                }

                val keystore = AWSIotKeystoreHelper.getIotKeystore(
                    config.certificateId,
                    tempDirectory,
                    keystoreName,
                    keystorePassword
                )

                // todo: retry this?
                connection?.connect(keystore, connectionCallback)
            }
        }
    }

    private fun lastWill(config: IotConfig): AWSIotMqttLastWillAndTestament {
        val shadow = gson.toJson(
            Shadow(
                state = ShadowState(
                    reported = ShadowData(
                        connected = false
                    )
                )
            )
        ).toString()

        return AWSIotMqttLastWillAndTestament(
            "${config.clientId}/connected/update",
            shadow,
            AWSIotMqttQos.QOS1
        )
    }

    @Synchronized
    fun disconnect() {
        launch {
            val success =
                @Suppress("TooGenericExceptionCaught")
                try {
                    connection?.disconnect()
                } catch (e: Exception) {
                    Timber.tag(LOG_TAG).w("disconnect - Error while disconnecting MQTT")

                    false
                } finally {
                    Timber.tag(LOG_TAG).d("disconnect - Releasing resources")
                    activeConfig = null
                    connection = null
                }

            Timber.tag(LOG_TAG).d("disconnect - Finished successfully = $success")
        }
    }

    fun getConnectionState(): LiveData<IotConnectionState> = connectionState
    fun peekConnectionState(): IotConnectionState? = connectionState.value

    @Synchronized
    fun subscribe(
        topic: String,
        statusCallback: IotClientAction<MqttSubscriptionStatus>,
        action: IotClientAction<String>,
    ) {
        launch {
            runCatching {
                connection?.subscribeToTopic(topic, AWSIotMqttQos.QOS1) { topic, data ->
                    Timber.tag(LOG_TAG).d("message on topic $topic")
                    // todo: parse directly to data from type
                    action(String(data, Charset.forName("UTF-8")))
                }
            }.onSuccess {
                statusCallback(MqttSubscriptionStatus.Success)
            }.onFailure {
                Timber.tag(LOG_TAG).e(it, "Failed to subscribe to $topic")
                statusCallback(MqttSubscriptionStatus.Error(it))
            }
        }
    }

    @Synchronized
    fun publish(topic: String, data: String, priority: Priority = Priority.AT_LEAST_ONCE) {
        launch {
            // todo: add retry?
            val callback = AWSIotMqttMessageDeliveryCallback { status, _ ->
                Timber.tag(LOG_TAG).d("Published to $topic with status $status")
            }
            connection?.publishString(data, topic, priority.toQOS(), callback, null)
        }
    }

    @Synchronized
    fun unsubscribe(
        topic: String,
        statusCallback: IotClientAction<MqttSubscriptionStatus>,
    ) {
        launch {
            runCatching {
                connection?.unsubscribeTopic(topic)
            }.onSuccess {
                statusCallback(MqttSubscriptionStatus.Success)
            }.onFailure {
                Timber.tag(LOG_TAG).e(it, "Failed to unsubscribe from $topic")
                statusCallback(MqttSubscriptionStatus.Error(it))
            }
        }
    }

    private fun Priority.toQOS(): AWSIotMqttQos {
        return if (this == Priority.AT_LEAST_ONCE) AWSIotMqttQos.QOS1 else AWSIotMqttQos.QOS0
    }
}

typealias IotClientAction<T> = (T) -> Unit

sealed class MqttSubscriptionStatus {
    object Success : MqttSubscriptionStatus()
    data class Error(val error: Throwable) : MqttSubscriptionStatus()
}

enum class Priority {
    AT_MOST_ONCE,
    AT_LEAST_ONCE
}