package com.katiba.app.ui.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.data.repository.AuthRepository
import com.katiba.app.ui.theme.KatibaColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OTPVerificationScreen(
    userId: String,
    email: String,
    purpose: String, // "email_verification" or "password_reset"
    authRepository: AuthRepository,
    onVerificationSuccess: (String?) -> Unit, // resetToken for password_reset, null for email_verification
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var otpDigits by remember { mutableStateOf(List(6) { "" }) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resendCountdown by remember { mutableStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val coroutineScope = rememberCoroutineScope()

    // Countdown timer for resend
    LaunchedEffect(resendCountdown) {
        if (resendCountdown > 0) {
            delay(1000)
            resendCountdown--
        } else {
            canResend = true
        }
    }

    // Auto-focus first field
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    val isEmailVerification = purpose == "email_verification"
    val title = if (isEmailVerification) "Verify Your Email" else "Verify OTP"
    val subtitle = if (isEmailVerification) {
        "We've sent a 6-digit code to\n$email"
    } else {
        "Enter the 6-digit code sent to\n$email"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top-left back button (rounded square)
        IconButton(
            onClick = onBackClick,
            enabled = !isLoading,
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp)
                .align(Alignment.TopStart)
                .size(44.dp)
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = BackArrowIcon,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
                tint = KatibaColors.OnSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Moved down by ~100dp (original 48 + 100 extra)
            Spacer(modifier = Modifier.height(148.dp))

            // Animated mail icon
            AnimatedMailIcon()

            // Increased spacing between icon and title (original 24 + 50 extra)
            Spacer(modifier = Modifier.height(74.dp))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = KatibaColors.KenyaGreen,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // OTP Input Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                otpDigits.forEachIndexed { index, digit ->
                    OutlinedTextField(
                        value = digit,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                val newDigits = otpDigits.toMutableList()
                                newDigits[index] = newValue
                                otpDigits = newDigits

                                // Auto-focus next field
                                if (newValue.isNotEmpty() && index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                }

                                // Auto-verify when all filled
                                if (newDigits.all { it.isNotEmpty() }) {
                                    val otp = newDigits.joinToString("")
                                    coroutineScope.launch {
                                        verifyOtp(
                                            authRepository,
                                            userId,
                                            otp,
                                            purpose,
                                            onVerificationSuccess,
                                            onError = { errorMessage = it },
                                            onLoadingChange = { isLoading = it }
                                        )
                                    }
                                }
                            } else if (newValue.isEmpty() && index > 0) {
                                val newDigits = otpDigits.toMutableList()
                                newDigits[index] = ""
                                otpDigits = newDigits
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequesters[index]),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KatibaColors.KenyaGreen,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            cursorColor = KatibaColors.KenyaGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Resend OTP section
            if (canResend) {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = null
                            val result = authRepository.resendOtp(userId, purpose)
                            isLoading = false

                            result.onSuccess {
                                resendCountdown = 60
                                canResend = false
                                errorMessage = "Code resent successfully"
                            }.onFailure {
                                errorMessage = it.message ?: "Failed to resend code"
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Resend Code",
                        color = KatibaColors.KenyaGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Resend code in ${resendCountdown}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Verify button with solid/physical shadow styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                // Bottom shadow layer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            color = KatibaColors.DarkGreen,
                            shape = RoundedCornerShape(16.dp)
                        )
                )
                // Main button
                Button(
                    onClick = {
                        val otp = otpDigits.joinToString("")
                        if (otp.length == 6) {
                            coroutineScope.launch {
                                verifyOtp(
                                    authRepository,
                                    userId,
                                    otp,
                                    purpose,
                                    onVerificationSuccess,
                                    onError = { errorMessage = it },
                                    onLoadingChange = { isLoading = it }
                                )
                            }
                        } else {
                            errorMessage = "Please enter all 6 digits"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .align(Alignment.TopCenter),
                    enabled = !isLoading && otpDigits.all { it.isNotEmpty() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KatibaColors.KenyaGreen
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Verify",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedMailIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "mail")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = KatibaColors.KenyaGreen.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = MailCheckIcon,
                contentDescription = "Email Verification",
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer { translationY = bounce },
                tint = KatibaColors.KenyaGreen
            )
        }
    }
}

// Back arrow icon
private val BackArrowIcon: ImageVector
    get() = ImageVector.Builder(
        name = "BackArrow", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f) {
            moveTo(19f, 12f)
            horizontalLineTo(5f)
        }
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f) {
            moveTo(12f, 19f)
            lineTo(5f, 12f)
            lineTo(12f, 5f)
        }
    }.build()

// Mail with check icon
private val MailCheckIcon: ImageVector
    get() = ImageVector.Builder(
        name = "MailCheck", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f) {
            moveTo(4f, 4f)
            horizontalLineTo(20f)
            curveTo(21.1f, 4f, 22f, 4.9f, 22f, 6f)
            verticalLineTo(18f)
            curveTo(22f, 19.1f, 21.1f, 20f, 20f, 20f)
            horizontalLineTo(4f)
            curveTo(2.9f, 20f, 2f, 19.1f, 2f, 18f)
            verticalLineTo(6f)
            curveTo(2f, 4.9f, 2.9f, 4f, 4f, 4f)
            close()
        }
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f) {
            moveTo(22f, 6f)
            lineTo(12f, 13f)
            lineTo(2f, 6f)
        }
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f) {
            moveTo(9f, 12f)
            lineTo(11f, 14f)
            lineTo(15f, 10f)
        }
    }.build()

private suspend fun verifyOtp(
    authRepository: AuthRepository,
    userId: String,
    otp: String,
    purpose: String,
    onSuccess: (String?) -> Unit,
    onError: (String) -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {
    onLoadingChange(true)

    if (purpose == "email_verification") {
        val result = authRepository.verifyEmail(userId, otp)
        onLoadingChange(false)

        result.onSuccess {
            onSuccess(null)
        }.onFailure {
            onError(it.message ?: "Invalid or expired code")
        }
    } else {
        val result = authRepository.verifyResetOtp(userId, otp)
        onLoadingChange(false)

        result.onSuccess { resetToken ->
            onSuccess(resetToken)
        }.onFailure {
            onError(it.message ?: "Invalid or expired code")
        }
    }
}
