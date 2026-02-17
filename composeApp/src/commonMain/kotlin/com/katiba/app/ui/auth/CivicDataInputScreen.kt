package com.katiba.app.ui.auth

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.data.api.UserApiClient
import com.katiba.app.ui.theme.KatibaColors
import katiba.composeapp.generated.resources.Res
import katiba.composeapp.generated.resources.app_icon
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun CivicDataInputScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var nationalId by remember { mutableStateOf("") }
    var county by remember { mutableStateOf("") }
    var constituency by remember { mutableStateOf("") }
    var ward by remember { mutableStateOf("") }
    var isRegisteredVoter by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val userApiClient = remember { UserApiClient() }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(92.dp))
        
        // App Icon
        Image(
            painter = painterResource(Res.drawable.app_icon),
            contentDescription = "Katiba App Icon",
            modifier = Modifier.size(80.dp)
        )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = "Your Civic Information",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = KatibaColors.KenyaGreen,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle
            Text(
                text = "Help us personalize your civic education experience",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // National ID field
            OutlinedTextField(
                value = nationalId,
                onValueChange = { if (it.length <= 20) nationalId = it },
                label = { Text("National ID Number") },
                placeholder = { Text("Enter your ID number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KatibaColors.KenyaGreen,
                    focusedLabelColor = KatibaColors.KenyaGreen,
                    cursorColor = KatibaColors.KenyaGreen
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // County field
            OutlinedTextField(
                value = county,
                onValueChange = { county = it },
                label = { Text("County") },
                placeholder = { Text("e.g., Nairobi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KatibaColors.KenyaGreen,
                    focusedLabelColor = KatibaColors.KenyaGreen,
                    cursorColor = KatibaColors.KenyaGreen
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Constituency field
            OutlinedTextField(
                value = constituency,
                onValueChange = { constituency = it },
                label = { Text("Constituency") },
                placeholder = { Text("e.g., Westlands") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KatibaColors.KenyaGreen,
                    focusedLabelColor = KatibaColors.KenyaGreen,
                    cursorColor = KatibaColors.KenyaGreen
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ward field
            OutlinedTextField(
                value = ward,
                onValueChange = { ward = it },
                label = { Text("Ward") },
                placeholder = { Text("e.g., Kitisuru") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KatibaColors.KenyaGreen,
                    focusedLabelColor = KatibaColors.KenyaGreen,
                    cursorColor = KatibaColors.KenyaGreen
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Registered Voter Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Registered Voter",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Are you registered to vote?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = isRegisteredVoter,
                        onCheckedChange = { isRegisteredVoter = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = KatibaColors.KenyaGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Submit button with physical UI and press animation
            val buttonInteractionSource = remember { MutableInteractionSource() }
            val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
            val buttonPressOffset by animateDpAsState(
                targetValue = if (isButtonPressed) 4.dp else 0.dp,
                animationSpec = tween(durationMillis = 100)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                // Shadow box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .align(Alignment.BottomCenter)
                        .background(KatibaColors.DarkGreen, RoundedCornerShape(16.dp))
                )
                Button(
                    onClick = {
                        if (nationalId.isBlank() || county.isBlank() || 
                            constituency.isBlank() || ward.isBlank()) {
                            errorMessage = "Please fill in all fields"
                            return@Button
                        }
                        
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = null
                            
                            val result = userApiClient.updateProfile(
                                nationalId = nationalId,
                                county = county,
                                constituency = constituency,
                                ward = ward,
                                isRegisteredVoter = isRegisteredVoter
                            )
                            
                            isLoading = false
                            
                            result.onSuccess {
                                onComplete()
                            }.onFailure {
                                errorMessage = it.message ?: "Failed to save civic data"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = buttonPressOffset),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    interactionSource = buttonInteractionSource,
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
                            text = "Complete Setup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
    }
}
