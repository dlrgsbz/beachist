package de.tjarksaul.wachmanager.modules.provision.ui

/**
 * Data validation state of the login form.
 */
data class ProvisionFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)