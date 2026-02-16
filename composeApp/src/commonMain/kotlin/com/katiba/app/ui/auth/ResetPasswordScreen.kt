package com.katiba.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.data.api.AuthApiClient
import com.katiba.app.ui.theme.KatibaColors
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    resetToken: String,
    onResetSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val authApiClient = remember { AuthApiClient() }
    
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
                text = "🔒",
                fontSize = 64.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title
            Text(
                text = "Create New Password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = KatibaColors.KenyaGreen,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Subtitle
            Text(
                text = "Enter your new password below",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("New Password") },
                placeholder = { Text("Enter new password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
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
            
            // Confirm password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter new password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Reset button
            Button(
                onClick = {
                    when {
                        password.isBlank() -> errorMessage = "Please enter a password"
                        confirmPassword.isBlank() -> errorMessage = "Please confirm your password"
                        password != confirmPassword -> errorMessage = "Passwords do not match"
                        password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                        else -> {
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                
                                val result = authApiClient.resetPassword(resetToken, password)
                                isLoading = false
                                
                                result.onSuccess {
                                    onResetSuccess()
                                }.onFailure {
                                    errorMessage = it.message ?: "Failed to reset password"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
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
                        text = "Reset Password",
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
                    text = "Back to Login",
                    color = Color.Gray
                )
            }
        }
    }
}

// Eye icons (reused from other auth screens)
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
