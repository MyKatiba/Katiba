package com.katiba.app.data.service

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidGoogleSignInService(
    private val activity: Activity,
    private val serverClientId: String // Web Client ID from Firebase Console
) : GoogleSignInService {

    override suspend fun signIn(): Result<String> {
        return withContext(Dispatchers.Main) {
            try {
                val credentialManager = CredentialManager.create(activity)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = activity,
                    request = request
                )

                val credential = result.credential

                when {
                    credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                        Result.success(googleIdToken.idToken)
                    }
                    else -> Result.failure(Exception("Unexpected credential type: ${credential.type}"))
                }
            } catch (e: NoCredentialException) {
                // No Google account on device or user cancelled
                Result.failure(Exception("No Google account found. Please add a Google account to your device."))
            } catch (e: GetCredentialException) {
                Result.failure(Exception("Google Sign-In failed: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
