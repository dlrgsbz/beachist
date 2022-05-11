package de.tjarksaul.wachmanager.modules.provision.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProvisionRequest(
    val station: Int,
)

@Serializable
data class ProvisionResponse(
    val privateKey: String,
    val publicKey: String,
    val certificateId: String,
    val certificatePem: String,
    val dataEndpoint: String,
    val credentialsEndpoint: String,
    val thingName: String,
)
