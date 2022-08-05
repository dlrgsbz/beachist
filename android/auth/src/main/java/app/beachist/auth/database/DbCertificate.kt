package app.beachist.auth.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.beachist.auth.dto.Certificate

@Entity(tableName = "certificate")
internal data class DbCertificate(
    @PrimaryKey
    val thingName: String,
    val certificateId: String,
    val certificatePem: String,
    val publicKey: String,
    val privateKey: String,
    val dataEndpoint: String,
    val credentialsEndpoint: String,
) {
    fun toCertificate(): Certificate {
        return Certificate(thingName,
            certificateId,
            certificatePem,
            publicKey,
            privateKey,
            dataEndpoint,
            credentialsEndpoint)
    }
}

internal fun Certificate.toDbCertificate(): DbCertificate {
    return DbCertificate(thingName,
        certificateId,
        certificatePem,
        publicKey,
        privateKey,
        dataEndpoint,
        credentialsEndpoint)
}