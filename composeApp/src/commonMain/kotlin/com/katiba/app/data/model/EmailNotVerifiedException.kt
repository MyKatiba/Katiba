package com.katiba.app.data.model

/**
 * Thrown when login or signup fails because the email is not yet verified.
 *
 * @param userId  The account's userId, needed to resend / verify OTP.
 * @param email   The email address, shown on the OTP screen.
 */
class EmailNotVerifiedException(
    val userId: String,
    val email: String,
    message: String = "Please verify your email before logging in"
) : Exception(message)
