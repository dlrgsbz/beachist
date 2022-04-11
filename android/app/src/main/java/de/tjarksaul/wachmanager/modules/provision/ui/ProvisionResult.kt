package de.tjarksaul.wachmanager.modules.provision.ui

/**
 * Authentication result : success (user details) or error message.
 */
data class ProvisionResult(
    val success: ProvisionDataView? = null,
    val error: Int? = null
)
