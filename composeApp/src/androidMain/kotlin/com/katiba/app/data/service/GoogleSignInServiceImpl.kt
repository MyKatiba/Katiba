package com.katiba.app.data.service

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidGoogleSignInService(
    private val context: Context,
    private val serverClientId: String // Web Client ID from Firebase Console
) : GoogleSignInService {

    override suspend fun signIn(): Result<String> {
        return withContext(Dispatchers.Main) {
            try {
                val credentialManager = CredentialManager.create(context)
                
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false) // Or true if preferred
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    Result.success(credential.idToken)
                } else {
                    Result.failure(Exception("Unexpected credential type"))
                }
            } catch (e: GetCredentialException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
