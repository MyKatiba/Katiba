package com.katiba.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
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
import com.katiba.app.ui.theme.KatibaColors

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = { println("TODO: Forgot password flow") },
    onGoogleSignIn: () -> Unit = { println("TODO: Google sign-in tapped") }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Kenyan flag gradient stripe at the top
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // App logo / name
            Text(
                text = "📜",
                fontSize = 64.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Katiba",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = KatibaColors.KenyaGreen
            )

            Text(
                text = "Welcome back, Mzalendo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                placeholder = { Text("Enter your email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KatibaColors.KenyaGreen,
                    focusedLabelColor = KatibaColors.KenyaGreen,
                    cursorColor = KatibaColors.KenyaGreen
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
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

            // Forgot password
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodySmall,
                    color = KatibaColors.KenyaRed,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateToForgotPassword() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login button
            Button(
                onClick = onLoginSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KatibaColors.KenyaGreen,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
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

            // Google sign in button
            OutlinedButton(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Icon(
                    imageVector = GoogleIcon,
                    contentDescription = "Google",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign in with Google",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign up link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KatibaColors.KenyaGreen,
                    modifier = Modifier.clickable(onClick = onNavigateToSignUp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── Custom Vector Icons ────────────────────────────────────────────────────

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

internal val GoogleIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Google", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        // Blue
        path(fill = SolidColor(Color(0xFF4285F4))) {
            moveTo(22.56f, 12.25f)
            curveTo(22.56f, 11.47f, 22.49f, 10.72f, 22.36f, 10f)
            horizontalLineTo(12f)
            verticalLineTo(14.26f)
            horizontalLineTo(17.92f)
            curveTo(17.66f, 15.63f, 16.88f, 16.79f, 15.71f, 17.58f)
            verticalLineTo(20.33f)
            horizontalLineTo(19.28f)
            curveTo(21.36f, 18.42f, 22.56f, 15.6f, 22.56f, 12.25f)
            close()
        }
        // Green
        path(fill = SolidColor(Color(0xFF34A853))) {
            moveTo(12f, 23f)
            curveTo(14.97f, 23f, 17.46f, 22.02f, 19.28f, 20.33f)
            lineTo(15.71f, 17.58f)
            curveTo(14.73f, 18.23f, 13.48f, 18.63f, 12f, 18.63f)
            curveTo(9.14f, 18.63f, 6.71f, 16.7f, 5.84f, 14.1f)
            horizontalLineTo(2.18f)
            verticalLineTo(16.94f)
            curveTo(3.99f, 20.53f, 7.7f, 23f, 12f, 23f)
            close()
        }
        // Yellow
        path(fill = SolidColor(Color(0xFFFBBC05))) {
            moveTo(5.84f, 14.1f)
            curveTo(5.62f, 13.45f, 5.49f, 12.75f, 5.49f, 12f)
            curveTo(5.49f, 11.25f, 5.62f, 10.55f, 5.84f, 9.9f)
            verticalLineTo(7.06f)
            horizontalLineTo(2.18f)
            curveTo(1.43f, 8.55f, 1f, 10.22f, 1f, 12f)
            curveTo(1f, 13.78f, 1.43f, 15.45f, 2.18f, 16.94f)
            lineTo(5.84f, 14.1f)
            close()
        }
        // Red
        path(fill = SolidColor(Color(0xFFEA4335))) {
            moveTo(12f, 5.38f)
            curveTo(13.62f, 5.38f, 15.06f, 5.94f, 16.21f, 7.02f)
            lineTo(19.36f, 3.87f)
            curveTo(17.45f, 2.09f, 14.97f, 1f, 12f, 1f)
            curveTo(7.7f, 1f, 3.99f, 3.47f, 2.18f, 7.06f)
            lineTo(5.84f, 9.9f)
            curveTo(6.71f, 7.3f, 9.14f, 5.38f, 12f, 5.38f)
            close()
        }
    }.build()
