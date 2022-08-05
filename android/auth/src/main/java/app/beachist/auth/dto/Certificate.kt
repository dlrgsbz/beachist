package app.beachist.auth.dto

data class Certificate(
    val thingName: String,
    val certificateId: String,
    val certificatePem: String,
    val publicKey: String,
    val privateKey: String,
    val dataEndpoint: String,
    val credentialsEndpoint: String,
)
