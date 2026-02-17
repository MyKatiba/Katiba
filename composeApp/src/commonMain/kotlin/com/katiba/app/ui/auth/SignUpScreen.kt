package com.katiba.app.ui.auth

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import katiba.composeapp.generated.resources.Res
import katiba.composeapp.generated.resources.app_icon
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

import com.katiba.app.data.repository.AuthRepository
import com.katiba.app.data.service.GoogleSignInService

@Composable
fun SignUpScreen(
    authRepository: AuthRepository,
    googleSignInService: GoogleSignInService? = null,
    onSignUpSuccess: (String, String) -> Unit, // (userId, email)
    onGoogleSignUpSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Interaction sources for tracking focus state
    val fullNameInteractionSource = remember { MutableInteractionSource() }
    val emailInteractionSource = remember { MutableInteractionSource() }
    val passwordInteractionSource = remember { MutableInteractionSource() }
    val confirmPasswordInteractionSource = remember { MutableInteractionSource() }
    val isFullNameFocused by fullNameInteractionSource.collectIsFocusedAsState()
    val isEmailFocused by emailInteractionSource.collectIsFocusedAsState()
    val isPasswordFocused by passwordInteractionSource.collectIsFocusedAsState()
    val isConfirmPasswordFocused by confirmPasswordInteractionSource.collectIsFocusedAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // App icon
        Image(
            painter = painterResource(Res.drawable.app_icon),
            contentDescription = "Katiba App Icon",
            modifier = Modifier.size(70.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Join Katiba",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = KatibaColors.KenyaGreen
        )

        Text(
            text = "Start your civic education journey",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(82.dp)) // 32dp + 50dp for moving form down

        // Full name field with user icon
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            placeholder = { Text("Enter your full name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            interactionSource = fullNameInteractionSource,
            leadingIcon = {
                Icon(
                    imageVector = UserIcon,
                    contentDescription = "Full Name",
                    modifier = Modifier.size(20.dp),
                    tint = if (isFullNameFocused) KatibaColors.KenyaGreen else Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KatibaColors.KenyaGreen,
                focusedLabelColor = KatibaColors.KenyaGreen,
                cursorColor = KatibaColors.KenyaGreen
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email field with mail icon
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
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

        Spacer(modifier = Modifier.height(12.dp))

        // Password field with lock icon
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("Create a password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            interactionSource = passwordInteractionSource,
            leadingIcon = {
                Icon(
                    imageVector = LockIcon,
                    contentDescription = "Password",
                    modifier = Modifier.size(20.dp),
                    tint = if (isPasswordFocused) KatibaColors.KenyaGreen else Color.Gray
                )
            },
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

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm password field with lock icon
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            placeholder = { Text("Re-enter your password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            interactionSource = confirmPasswordInteractionSource,
            leadingIcon = {
                Icon(
                    imageVector = LockIcon,
                    contentDescription = "Confirm Password",
                    modifier = Modifier.size(20.dp),
                    tint = if (isConfirmPasswordFocused) KatibaColors.KenyaGreen else Color.Gray
                )
            },
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

        Spacer(modifier = Modifier.height(28.dp))

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

            // Sign up button with physical shadow and press animation
            run {
                val signUpInteractionSource = remember { MutableInteractionSource() }
                val isSignUpPressed by signUpInteractionSource.collectIsPressedAsState()
                val signUpPressOffset by animateDpAsState(
                    targetValue = if (isSignUpPressed) 4.dp else 0.dp,
                    animationSpec = tween(durationMillis = 100)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .align(Alignment.BottomCenter)
                            .background(KatibaColors.DarkGreen, RoundedCornerShape(16.dp))
                    )
                    Button(
                        onClick = {
                            when {
                                fullName.isBlank() -> errorMessage = "Please enter your full name"
                                email.isBlank() -> errorMessage = "Please enter your email"
                                password.isBlank() -> errorMessage = "Please enter a password"
                                confirmPassword.isBlank() -> errorMessage = "Please confirm your password"
                                password != confirmPassword -> errorMessage = "Passwords do not match"
                                password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                                !email.contains("@") -> errorMessage = "Please enter a valid email"
                                else -> {
                                    coroutineScope.launch {
                                        isLoading = true
                                        errorMessage = null

                                        val result = authRepository.registerWithEmail(
                                            name = fullName,
                                            email = email,
                                            password = password,
                                            confirmPassword = confirmPassword
                                        )

                                        isLoading = false

                                        if (result.isSuccess) {
                                            val userId = result.getOrThrow()
                                            onSignUpSuccess(userId, email)
                                        } else {
                                            errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = signUpPressOffset),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        interactionSource = signUpInteractionSource,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KatibaColors.KenyaGreen,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Sign Up",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider with "OR"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = KatibaColors.SurfaceVariant)
                Text(
                    text = "  OR  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = KatibaColors.SurfaceVariant)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google sign up button with shadow and press animation
            run {
                val googleInteractionSource = remember { MutableInteractionSource() }
                val isGooglePressed by googleInteractionSource.collectIsPressedAsState()
                val googlePressOffset by animateDpAsState(
                    targetValue = if (isGooglePressed) 4.dp else 0.dp,
                    animationSpec = tween(durationMillis = 100)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    )
                    OutlinedButton(
                        onClick = {
                            if (googleSignInService == null) {
                                errorMessage = "Google Sign-Up not supported on this device"
                                return@OutlinedButton
                            }

                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                val signInResult = googleSignInService.signIn()
                                if (signInResult.isSuccess) {
                                    val idToken = signInResult.getOrThrow()
                                    val authResult = authRepository.loginWithGoogle(idToken)
                                    isLoading = false
                                    if (authResult.isSuccess) {
                                        onGoogleSignUpSuccess()
                                    } else {
                                        errorMessage = authResult.exceptionOrNull()?.message ?: "Google Auth failed"
                                    }
                                } else {
                                    isLoading = false
                                    errorMessage = signInResult.exceptionOrNull()?.message ?: "Google Sign-In canceled or failed"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = googlePressOffset),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        interactionSource = googleInteractionSource,
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = GoogleIcon,
                            contentDescription = "Google",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sign up with Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Login link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KatibaColors.KenyaGreen,
                    modifier = Modifier.clickable(onClick = onNavigateToLogin)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
    }
}

// ─── Custom Vector Icons ────────────────────────────────────────────────────

// Note: UserIcon, MailIcon, and LockIcon are reused from LoginScreen.kt (defined as internal)

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
