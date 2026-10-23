package com.whatspent.app

import android.os.Build
import android.os.Bundle
import android.view.Display
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.whatspent.app.service.WhatsAppTrackerService
import com.whatspent.app.ui.navigation.WhatSpentNavGraph
import com.whatspent.app.ui.theme.ButterGoldAccent
import com.whatspent.app.ui.theme.CharcoalBentoSurface
import com.whatspent.app.ui.theme.MutedChampagneIcon
import com.whatspent.app.ui.theme.ObsidianBackground
import com.whatspent.app.ui.theme.OnButterGoldInk
import com.whatspent.app.ui.theme.WhatSpentTheme
import com.whatspent.app.util.BiometricAuthHelper

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enableHighRefreshRate()

        val app = application as WhatSpentApplication

        setContent {
            val dynamicColorEnabled by app.preferences.dynamicColorEnabled.collectAsState(initial = false)
            val biometricLockEnabled by app.preferences.biometricLockEnabled.collectAsState(initial = false)

            var isUnlocked by remember { mutableStateOf(!app.preferences.isBiometricLockEnabledSync()) }

            fun triggerBiometricAuth() {
                if (BiometricAuthHelper.canAuthenticate(this@MainActivity)) {
                    BiometricAuthHelper.authenticate(
                        activity = this@MainActivity,
                        title = "Unlock WhatSpent",
                        subtitle = "Verify your identity to view WhatsApp screen time records",
                        onSuccess = {
                            isUnlocked = true
                        },
                        onError = { error ->
                            Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    // Biometrics not enrolled or supported; unlock directly
                    isUnlocked = true
                }
            }

            // Automatically prompt authentication when lock is enabled and locked
            LaunchedEffect(biometricLockEnabled) {
                if (biometricLockEnabled && !isUnlocked) {
                    triggerBiometricAuth()
                } else if (!biometricLockEnabled) {
                    isUnlocked = true
                }
            }

            WhatSpentTheme(dynamicColor = dynamicColorEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (biometricLockEnabled && !isUnlocked) {
                        BiometricLockScreen(
                            onUnlockClick = { triggerBiometricAuth() }
                        )
                    } else {
                        WhatSpentNavGraph(repository = app.repository)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableHighRefreshRate()
        // User is inside WhatSpent, so they cannot be actively in WhatsApp
        WhatsAppTrackerService.forceExitActiveSession("WhatSpent app in foreground")
    }

    /**
     * Explicitly negotiates the highest supported refresh rate (e.g., 120Hz / 144Hz)
     * at the native panel resolution so OEM display controllers do not throttle WhatSpent to 60Hz.
     */
    private fun enableHighRefreshRate() {
        runCatching {
            val currentDisplay: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }

            val supportedModes = currentDisplay?.supportedModes ?: return
            val activeMode = currentDisplay.mode

            // Pick highest refresh rate mode matching current physical resolution
            val bestMode = supportedModes
                .filter {
                    it.physicalWidth == activeMode.physicalWidth &&
                        it.physicalHeight == activeMode.physicalHeight
                }
                .maxByOrNull { it.refreshRate }
                ?: supportedModes.maxByOrNull { it.refreshRate }
                ?: return

            val layoutParams = window.attributes
            if (layoutParams.preferredDisplayModeId != bestMode.modeId ||
                layoutParams.preferredRefreshRate != bestMode.refreshRate
            ) {
                layoutParams.preferredDisplayModeId = bestMode.modeId
                layoutParams.preferredRefreshRate = bestMode.refreshRate
                window.attributes = layoutParams
            }
        }
    }
}

/**
 * Clean Material 3 Lock Screen displayed when Biometric Lock is enabled.
 * Styled with Obsidian background, Charcoal bento surface, and Butter-Gold pill button.
 */
@Composable
private fun BiometricLockScreen(
    onUnlockClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(CharcoalBentoSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = ButterGoldAccent
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "WhatSpent Locked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Biometric protection is enabled to keep your WhatsApp screen time analytics private.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedChampagneIcon,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onUnlockClick,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButterGoldAccent,
                    contentColor = OnButterGoldInk
                ),
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = OnButterGoldInk
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Unlock with Biometrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnButterGoldInk
                )
            }
        }
    }
}
