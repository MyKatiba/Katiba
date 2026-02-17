package com.katiba.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Kenyan flag gradient stripe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            KatibaColors.KenyaBlack,
                            KatibaColors.KenyaRed,
                            KatibaColors.KenyaGreen,
                            KatibaColors.KenyaWhite,
                            KatibaColors.KenyaGreen,
                            KatibaColors.KenyaRed,
                            KatibaColors.KenyaBlack
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Icon
            Text(
                text = "📧",
                fontSize = 64.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
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
                                // Handle backspace
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
            
            // Verify button
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
                    .height(56.dp),
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Back button
            TextButton(
                onClick = onBackClick,
                enabled = !isLoading
            ) {
                Text(
                    text = "Back",
                    color = Color.Gray
                )
            }
        }
    }
}

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
            onSuccess(null) // Navigate to civic data screen
        }.onFailure {
            onError(it.message ?: "Invalid or expired code")
        }
    } else {
        // password_reset
        val result = authRepository.verifyResetOtp(userId, otp)
        onLoadingChange(false)
        
        result.onSuccess { resetToken ->
            onSuccess(resetToken) // Navigate to reset password screen
        }.onFailure {
            onError(it.message ?: "Invalid or expired code")
        }
    }
}
