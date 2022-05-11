package de.tjarksaul.wachmanager.modules.auth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Certificate(
    @PrimaryKey
    val thingName: String,
    val certificateId: String,
    val certificatePem: String,
    val publicKey: String,
    val privateKey: String,
    val dataEndpoint: String,
    val credentialsEndpoint: String,
)
