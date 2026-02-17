package com.katiba.app.ui.auth

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import com.katiba.app.data.api.AuthApiClient
import com.katiba.app.ui.theme.KatibaColors
import katiba.composeapp.generated.resources.Res
import katiba.composeapp.generated.resources.app_icon
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

enum class ForgotPasswordStep {
    EMAIL_ENTRY,
    OTP_VERIFICATION,
    NEW_PASSWORD
}

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    onPasswordResetSuccess: () -> Unit
) {
    var currentStep by remember { mutableStateOf(ForgotPasswordStep.EMAIL_ENTRY) }
    var email by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var resetToken by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val authApiClient = remember { AuthApiClient() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top-left back button (rounded square)
        IconButton(
            onClick = {
                when (currentStep) {
                    ForgotPasswordStep.EMAIL_ENTRY -> onBackToLogin()
                    ForgotPasswordStep.OTP_VERIFICATION -> {
                        currentStep = ForgotPasswordStep.EMAIL_ENTRY
                        errorMessage = null
                    }
                    ForgotPasswordStep.NEW_PASSWORD -> {
                        currentStep = ForgotPasswordStep.OTP_VERIFICATION
                        errorMessage = null
                    }
                }
            },
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
                imageVector = ForgotBackArrowIcon,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
                tint = KatibaColors.OnSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Moved down by 100dp (original 48 + 100 extra)
            Spacer(modifier = Modifier.height(148.dp))

            // App icon
            Image(
                painter = painterResource(Res.drawable.app_icon),
                contentDescription = "Katiba App Icon",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step-specific title
            Text(
                text = when (currentStep) {
                    ForgotPasswordStep.EMAIL_ENTRY -> "Reset Password"
                    ForgotPasswordStep.OTP_VERIFICATION -> "Verify OTP"
                    ForgotPasswordStep.NEW_PASSWORD -> "Create New Password"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = when (currentStep) {
                    ForgotPasswordStep.EMAIL_ENTRY -> "Enter your email to receive a reset code"
                    ForgotPasswordStep.OTP_VERIFICATION -> "Enter the OTP sent to $email"
                    ForgotPasswordStep.NEW_PASSWORD -> "Enter your new password"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(90.dp))

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = KatibaColors.KenyaRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            when (currentStep) {
                ForgotPasswordStep.EMAIL_ENTRY -> {
                    EmailEntryStep(
                        email = email,
                        onEmailChange = {
                            email = it
                            errorMessage = null
                        },
                        isLoading = isLoading,
                        onSendOTP = {
                            if (email.isBlank()) {
                                errorMessage = "Please enter your email"
                                return@EmailEntryStep
                            }
                            if (!email.contains("@")) {
                                errorMessage = "Please enter a valid email"
                                return@EmailEntryStep
                            }

                            isLoading = true
                            errorMessage = null

                            coroutineScope.launch {
                                val result = authApiClient.forgotPassword(email)
                                isLoading = false

                                result.onSuccess { response ->
                                    userId = response.userId
                                    currentStep = ForgotPasswordStep.OTP_VERIFICATION
                                }.onFailure { error ->
                                    errorMessage = error.message ?: "Failed to send OTP"
                                }
                            }
                        }
                    )
                }

                ForgotPasswordStep.OTP_VERIFICATION -> {
                    OTPVerificationStep(
                        otp = otp,
                        onOtpChange = {
                            otp = it
                            errorMessage = null
                        },
                        isLoading = isLoading,
                        onVerifyOTP = {
                            if (otp.isBlank()) {
                                errorMessage = "Please enter the OTP"
                                return@OTPVerificationStep
                            }
                            if (otp.length != 6) {
                                errorMessage = "OTP must be 6 digits"
                                return@OTPVerificationStep
                            }

                            isLoading = true
                            errorMessage = null

                            coroutineScope.launch {
                                val result = authApiClient.verifyResetOtp(userId, otp)
                                isLoading = false

                                result.onSuccess { response ->
                                    resetToken = response.resetToken
                                    currentStep = ForgotPasswordStep.NEW_PASSWORD
                                }.onFailure { error ->
                                    errorMessage = error.message ?: "Invalid or expired OTP"
                                }
                            }
                        },
                        onResendOTP = {
                            isLoading = true
                            errorMessage = null

                            coroutineScope.launch {
                                val result = authApiClient.resendOtp(userId, "password_reset")
                                isLoading = false

                                result.onSuccess {
                                    errorMessage = "OTP resent successfully"
                                }.onFailure { error ->
                                    errorMessage = error.message ?: "Failed to resend OTP"
                                }
                            }
                        }
                    )
                }

                ForgotPasswordStep.NEW_PASSWORD -> {
                    NewPasswordStep(
                        newPassword = newPassword,
                        confirmPassword = confirmPassword,
                        passwordVisible = passwordVisible,
                        confirmPasswordVisible = confirmPasswordVisible,
                        onNewPasswordChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        onConfirmPasswordChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onConfirmPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                        isLoading = isLoading,
                        onResetPassword = {
                            if (newPassword.isBlank()) {
                                errorMessage = "Please enter a new password"
                                return@NewPasswordStep
                            }
                            if (newPassword.length < 6) {
                                errorMessage = "Password must be at least 6 characters"
                                return@NewPasswordStep
                            }
                            if (confirmPassword != newPassword) {
                                errorMessage = "Passwords do not match"
                                return@NewPasswordStep
                            }

                            isLoading = true
                            errorMessage = null

                            coroutineScope.launch {
                                val result = authApiClient.resetPassword(resetToken, newPassword)
                                isLoading = false

                                result.onSuccess {
                                    onPasswordResetSuccess()
                                }.onFailure { error ->
                                    errorMessage = error.message ?: "Failed to reset password"
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EmailEntryStep(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    onSendOTP: () -> Unit
) {
    val emailInteractionSource = remember { MutableInteractionSource() }
    val isEmailFocused by emailInteractionSource.collectIsFocusedAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            placeholder = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !isLoading,
            interactionSource = emailInteractionSource,
            leadingIcon = {
                Icon(
                    imageVector = MailIcon,
                    contentDescription = "Email",
                    modifier = Modifier.size(20.dp),
                    tint = if (isEmailFocused) KatibaColors.KenyaGreen else Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KatibaColors.KenyaGreen,
                focusedLabelColor = KatibaColors.KenyaGreen,
                cursorColor = KatibaColors.KenyaGreen
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Physical button with press animation
        PhysicalButton(
            text = "Send OTP",
            onClick = onSendOTP,
            isLoading = isLoading,
            enabled = !isLoading
        )
    }
}

@Composable
private fun OTPVerificationStep(
    otp: String,
    onOtpChange: (String) -> Unit,
    isLoading: Boolean,
    onVerifyOTP: () -> Unit,
    onResendOTP: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) onOtpChange(it) },
            label = { Text("OTP Code") },
            placeholder = { Text("Enter 6-digit code") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            enabled = !isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KatibaColors.KenyaGreen,
                focusedLabelColor = KatibaColors.KenyaGreen,
                cursorColor = KatibaColors.KenyaGreen
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Resend OTP link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Didn't receive the code? ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Resend",
                style = MaterialTheme.typography.bodySmall,
                color = KatibaColors.KenyaRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(enabled = !isLoading) { onResendOTP() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Physical button with press animation
        PhysicalButton(
            text = "Verify OTP",
            onClick = onVerifyOTP,
            isLoading = isLoading,
            enabled = !isLoading
        )
    }
}

@Composable
private fun NewPasswordStep(
    newPassword: String,
    confirmPassword: String,
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    isLoading: Boolean,
    onResetPassword: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = { Text("New Password") },
            placeholder = { Text("Enter new password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityToggle) {
                    Icon(
                        imageVector = if (passwordVisible) EyeOffIcon else EyeIcon,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KatibaColors.KenyaGreen,
                focusedLabelColor = KatibaColors.KenyaGreen,
                cursorColor = KatibaColors.KenyaGreen
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirm Password") },
            placeholder = { Text("Re-enter new password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onConfirmPasswordVisibilityToggle) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) EyeOffIcon else EyeIcon,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KatibaColors.KenyaGreen,
                focusedLabelColor = KatibaColors.KenyaGreen,
                cursorColor = KatibaColors.KenyaGreen
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Physical button with press animation
        PhysicalButton(
            text = "Reset Password",
            onClick = onResetPassword,
            isLoading = isLoading,
            enabled = !isLoading
        )
    }
}

/**
 * Reusable physical button with shadow and press animation
 */
@Composable
private fun PhysicalButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 100)
    )

    Box(
        modifier = modifier
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
        // Main button with press animation
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.TopCenter)
                .offset(y = pressOffset),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = KatibaColors.KenyaGreen,
                contentColor = Color.White
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// Back arrow icon
private val ForgotBackArrowIcon: ImageVector
    get() = ImageVector.Builder(
        name = "ForgotBackArrow", defaultWidth = 24.dp, defaultHeight = 24.dp,
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

// ─── Custom Vector Icons ────────────────────────────────────────────────────

// Note: MailIcon is reused from LoginScreen.kt (defined as internal)

private val EyeIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Eye", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f) {
            moveTo(1f, 12f)
            curveTo(1f, 12f, 5f, 5f, 12f, 5f)
            curveTo(19f, 5f, 23f, 12f, 23f, 12f)
            curveTo(23f, 12f, 19f, 19f, 12f, 19f)
            curveTo(5f, 19f, 1f, 12f, 1f, 12f)
            close()
        }
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f) {
            moveTo(12f, 12f)
            moveToRelative(-3f, 0f)
            arcToRelative(3f, 3f, 0f, true, true, 6f, 0f)
            arcToRelative(3f, 3f, 0f, true, true, -6f, 0f)
        }
    }.build()

private val EyeOffIcon: ImageVector
    get() = ImageVector.Builder(
        name = "EyeOff", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f) {
            moveTo(17.94f, 17.94f)
            curveTo(16.23f, 19.24f, 14.15f, 20f, 12f, 20f)
            curveTo(5f, 20f, 1f, 12f, 1f, 12f)
            curveTo(2.24f, 9.36f, 3.93f, 7.09f, 6.06f, 5.06f)
            moveTo(9.9f, 4.24f)
            curveTo(10.59f, 4.08f, 11.29f, 4f, 12f, 4f)
            curveTo(19f, 4f, 23f, 12f, 23f, 12f)
            curveTo(22.39f, 13.34f, 21.6f, 14.59f, 20.68f, 15.68f)
        }
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f) {
            moveTo(1f, 1f)
            lineTo(23f, 23f)
        }
    }.build()
