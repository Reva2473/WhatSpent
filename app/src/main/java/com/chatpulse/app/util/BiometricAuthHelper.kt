package com.chatpulse.app.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Helper object providing biometric and device credential (PIN/pattern/password)
 * authentication for locking and securing ChatPulse.
 */
object BiometricAuthHelper {

    private const val ALLOWED_AUTHENTICATORS = BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    /**
     * Checks if biometric authentication or device credential is enrolled and usable.
     */
    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(ALLOWED_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Launches the system BiometricPrompt modal using biometric or device credential fallback.
     *
     * @param activity The host [FragmentActivity] (or ComponentActivity with FragmentActivity compatibility).
     * @param title Title displayed on the biometric prompt.
     * @param subtitle Subtitle describing the reason for authentication.
     * @param onSuccess Callback invoked when authentication successfully passes.
     * @param onError Callback invoked when authentication fails or is cancelled.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock ChatPulse",
        subtitle: String = "Verify your identity to view WhatsApp screen time analytics",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Called when a biometric (e.g. fingerprint) is rejected. Prompt remains open for retry.
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
