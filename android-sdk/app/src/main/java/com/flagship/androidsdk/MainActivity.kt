package com.flagship.androidsdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flagship.androidsdk.ui.theme.AndroidsdkTheme
import com.flagship.sdk.facade.FlagShipClient
import com.flagship.sdk.facade.FlagShipConfig
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidsdkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        applicationContext = application,
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    applicationContext: android.app.Application,
) {
    val defaultContext = remember {
        mapOf(
            "user_tier" to "premium",
            "country" to "US",
            "user_group" to "beta_testerss",
            "is_logged_in" to true,
            "is_accessibility_user" to false,
            "device" to "mobile",
            "theme_pref" to "light",
            "session_count" to 150.0,
            "region" to "US",
            "userId" to 3456,
            "app_version" to "2.5.0",
            "user_tags" to listOf("early-adopter", "beta-tester", "premium"),
        )
    }

    var isInitialized by remember { mutableStateOf(false) }
    var initError by remember { mutableStateOf<String?>(null) }
    var darkModeEnabled by remember { mutableStateOf<Boolean?>(null) }
    var stringValue by remember { mutableStateOf<String?>(null) }
    var doubleValue by remember { mutableStateOf<Double?>(null) }
    var objectValue by remember { mutableStateOf<String?>(null) }
    var semverValue by remember { mutableStateOf<String?>(null) }
    var flagshipClient by remember { mutableStateOf<FlagShipClient?>(null) }

    LaunchedEffect(Unit) {
        try {
            val config =
                FlagShipConfig(
                    applicationContext = applicationContext,
                    baseUrl = "http://10.0.2.2:8080",
                    flagshipApiKey = "tenant1",
                    refreshInterval = TimeUnit.SECONDS.toMillis(30),
                )
            flagshipClient = FlagShipClient.getInstance("test-domain", config)
            isInitialized = true
            flagshipClient?.onContextChange(emptyMap(), defaultContext)
        } catch (e: Exception) {
            initError = e.message ?: "Initialization failed"
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Flagship Android SDK Example",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        StatusCard(
            label = "SDK Status:",
            value =
                when {
                    isInitialized -> "✓ Initialized"
                    initError != null -> "✗ Error: $initError"
                    else -> "⏳ Initializing..."
                },
            isSuccess = isInitialized,
            isError = initError != null,
        )

        Button(
            onClick = {
                val result =
                    flagshipClient?.getBoolean(
                        key = "dark_mode_toggle",
                        defaultValue = false,
                        targetingKey = "3456",
                        context = defaultContext,
                    )
                darkModeEnabled = result?.value
            },
            enabled = isInitialized,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Evaluate Dark Mode Toggle")
        }

        if (darkModeEnabled != null) {
            StatusCard(
                label = "Dark Mode Toggle:",
                value = if (darkModeEnabled == true) "✓ Enabled" else "✗ Disabled",
                isSuccess = darkModeEnabled == true,
            )
        }

        Button(
            onClick = {
                val result =
                    flagshipClient?.getString(
                        key = "homepage_layout_test",
                        defaultValue = "default",
                        targetingKey = "3456",
                        context = defaultContext,
                    )
                stringValue = result?.value
            },
            enabled = isInitialized,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Evaluate Homepage Layout")
        }

        if (stringValue != null) {
            StatusCard(
                label = "Homepage Layout:",
                value = "Value: $stringValue",
                isSuccess = true,
            )
        }

        Button(
            onClick = {
                val result =
                    flagshipClient?.getDouble(
                        key = "search_result_limit",
                        defaultValue = 10.0,
                        targetingKey = "3456",
                        context = defaultContext,
                    )
                doubleValue = result?.value
            },
            enabled = isInitialized,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Evaluate Search Result Limit")
        }

        if (doubleValue != null) {
            StatusCard(
                label = "Search Result Limit:",
                value = "Value: $doubleValue",
                isSuccess = true,
            )
        }

        Button(
            onClick = {
                val defaultObj = buildJsonObject {
                    put("limit", 10)
                    put("enabled", false)
                }
                val result =
                    flagshipClient?.getJson(
                        key = "recommendations_config",
                        defaultValue = defaultObj,
                        targetingKey = "3456",
                        context = defaultContext,
                    )
                objectValue = result?.value?.toString()
            },
            enabled = isInitialized,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Evaluate Recommendations Config")
        }

        if (objectValue != null) {
            StatusCard(
                label = "Recommendations Config:",
                value = objectValue ?: "",
                isSuccess = true,
            )
        }

        Button(
            onClick = {
                val result =
                    flagshipClient?.getString(
                        key = "min_supported_app_version",
                        defaultValue = "1.0.0",
                        targetingKey = "3456",
                        context = defaultContext,
                    )
                semverValue = result?.value
            },
            enabled = isInitialized,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Evaluate Min Supported App Version")
        }

        if (semverValue != null) {
            StatusCard(
                label = "Min Supported App Version:",
                value = "Value: $semverValue",
                isSuccess = true,
            )
        }
    }
}

@Composable
fun StatusCard(
    label: String,
    value: String,
    isSuccess: Boolean = false,
    isError: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when {
                        isError -> MaterialTheme.colorScheme.errorContainer
                        isSuccess -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color =
                    when {
                        isError -> MaterialTheme.colorScheme.onErrorContainer
                        isSuccess -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
    }
}
