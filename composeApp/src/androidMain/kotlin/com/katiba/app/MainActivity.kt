package com.katiba.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val googleSignInService = com.katiba.app.data.service.AndroidGoogleSignInService(
            activity = this,
            // Web Client ID from Firebase Console (must be the WEB client, not Android client)
            serverClientId = "576740059155-a2u27m9dscc6na0pe3md9lgl1s0t3rsf.apps.googleusercontent.com"
        )
        
        setContent {
            App(googleSignInService = googleSignInService)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
