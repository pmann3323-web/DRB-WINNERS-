package com.example.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object FirebaseAuthManager {
    private const val API_KEY = "AIzaSyD1D5v5neEjA_70nseqqDVLExz8fn5sz3I"
    private const val SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY"
    private const val SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$API_KEY"

    private val client = OkHttpClient()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    data class AuthResult(
        val isSuccess: Boolean,
        val email: String? = null,
        val localId: String? = null,
        val idToken: String? = null,
        val errorMessage: String? = null
    )

    suspend fun signInWithEmail(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email.trim())
                put("password", password.trim())
                put("returnSecureToken", true)
            }
            val request = Request.Builder()
                .url(SIGN_IN_URL)
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonObject = JSONObject(responseBody)
                AuthResult(
                    isSuccess = true,
                    email = jsonObject.optString("email"),
                    localId = jsonObject.optString("localId"),
                    idToken = jsonObject.optString("idToken")
                )
            } else {
                AuthResult(
                    isSuccess = false,
                    errorMessage = "Email or password is incorrect"
                )
            }
        } catch (e: Exception) {
            AuthResult(
                isSuccess = false,
                errorMessage = "Email or password is incorrect"
            )
        }
    }

    suspend fun sendEmailVerification(idToken: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val url = "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=$API_KEY"
            val json = JSONObject().apply {
                put("requestType", "VERIFY_EMAIL")
                put("idToken", idToken)
            }
            val request = Request.Builder()
                .url(url)
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            AuthResult(isSuccess = response.isSuccessful)
        } catch (e: Exception) {
            AuthResult(isSuccess = false, errorMessage = e.message)
        }
    }

    suspend fun checkEmailVerified(idToken: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=$API_KEY"
            val json = JSONObject().apply {
                put("idToken", idToken)
            }
            val request = Request.Builder()
                .url(url)
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val jsonObject = JSONObject(responseBody)
                val usersArr = jsonObject.optJSONArray("users")
                if (usersArr != null && usersArr.length() > 0) {
                    val userObj = usersArr.getJSONObject(0)
                    return@withContext userObj.optBoolean("emailVerified", false)
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email.trim())
                put("password", password.trim())
                put("returnSecureToken", true)
            }
            val request = Request.Builder()
                .url(SIGN_UP_URL)
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonObject = JSONObject(responseBody)
                AuthResult(
                    isSuccess = true,
                    email = jsonObject.optString("email"),
                    localId = jsonObject.optString("localId"),
                    idToken = jsonObject.optString("idToken")
                )
            } else {
                val jsonObject = JSONObject(responseBody)
                val errorMsg = jsonObject.optJSONObject("error")?.optString("message") ?: ""
                val userFriendlyMsg = if (errorMsg.contains("EMAIL_EXISTS")) {
                    "User already exists. Please sign in"
                } else {
                    "User already exists. Please sign in"
                }
                AuthResult(
                    isSuccess = false,
                    errorMessage = userFriendlyMsg
                )
            }
        } catch (e: Exception) {
            AuthResult(
                isSuccess = false,
                errorMessage = "User already exists. Please sign in"
            )
        }
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val RESET_URL = "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=$API_KEY"
            val json = JSONObject().apply {
                put("requestType", "PASSWORD_RESET")
                put("email", email.trim())
            }
            val request = Request.Builder()
                .url(RESET_URL)
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                AuthResult(isSuccess = true, errorMessage = "Password reset link sent to your email!")
            } else {
                AuthResult(isSuccess = false, errorMessage = "Failed to send reset email. Please check your email address.")
            }
        } catch (e: Exception) {
            AuthResult(isSuccess = false, errorMessage = "Network error while sending password reset email.")
        }
    }
}
