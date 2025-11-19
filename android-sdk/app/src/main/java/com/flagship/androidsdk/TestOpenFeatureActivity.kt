package com.flagship.androidsdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import com.flagship.sdk.facade.FlagShipConfig
import com.flagship.sdk.facade.FlagshipProvider
import dev.openfeature.kotlin.sdk.ImmutableContext
import dev.openfeature.kotlin.sdk.OpenFeatureAPI
import dev.openfeature.kotlin.sdk.Value
import java.util.concurrent.TimeUnit

class TestOpenFeatureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidsdkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OpenFeatureScreen(
                        modifier = Modifier.padding(innerPadding),
                        applicationContext = application,
                    )
                }
            }
        }
    }
}

@Composable
fun OpenFeatureScreen(
    modifier: Modifier = Modifier,
    applicationContext: android.app.Application,
) {
    var isInitialized by remember { mutableStateOf(false) }
    var initError by remember { mutableStateOf<String?>(null) }
    var darkModeEnabled by remember { mutableStateOf<Boolean?>(null) }
    var stringValue by remember { mutableStateOf<String?>(null) }
    var doubleValue by remember { mutableStateOf<Double?>(null) }
    var objectValue by remember { mutableStateOf<String?>(null) }
    var contextVariant by remember { mutableStateOf("default") }

    LaunchedEffect(Unit) {
        try {
            val config =
                FlagShipConfig(
                    applicationContext = applicationContext,
                    baseUrl = "http://10.0.2.2:8080",
                    tenantId = "tenant1",
                    refreshInterval = TimeUnit.SECONDS.toMillis(30),
                )

            val provider = FlagshipProvider("test-domain", config)
            OpenFeatureAPI.setProvider(provider)

            val defaultContextMap =
                mapOf(
                    "user_tier" to Value.String("premium"),
                    "country" to Value.String("IN"),
                    "user_group" to Value.String("beta_testersss"),
                    "is_logged_in" to Value.Boolean(true),
                    "is_accessibility_user" to Value.Boolean(false),
                    "device" to Value.String("mobile"),
                    "theme_pref" to Value.String("light"),
                    "session_count" to Value.Double(150.0),
                    "region" to Value.String("IN"),
                    "userId" to Value.Integer(3456),
                    "app_version" to Value.String("2.3.0"),
                    "user_tags" to Value.List(
                        listOf(
                            Value.String("early-adopter"),
                            Value.String("beta-tester"),
                            Value.String("premium"),
                        ),
                    ),
                )

            OpenFeatureAPI.setEvaluationContext(
                ImmutableContext(
                    targetingKey = "3456",
                    defaultContextMap,
                ),
            )

            isInitialized = true
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
            text = "Flagship OpenFeature Example",
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
                val newContextMap =
                    if (contextVariant == "default") {
                        mapOf(
                            "user_tier" to Value.String("premium"),
                            "country" to Value.String("IN"),
                            "user_group" to Value.String("regular_users"),
                            "is_logged_in" to Value.Boolean(false),
                            "is_accessibility_user" to Value.Boolean(false),
                            "device" to Value.String("tablet"),
                            "theme_pref" to Value.String("dark"),
                            "session_count" to Value.Double(5.0),
                            "region" to Value.String("EU"),
                            "userId" to Value.Integer(3456),
                            "app_version" to Value.String("1.8.0"),
                            "user_tags" to Value.List(
                                listOf(
                                    Value.String("regular"),
                                    Value.String("standard"),
                                ),
                            ),
                        )
                    } else {
                        mapOf(
                            "user_tier" to Value.String("premium"),
                            "country" to Value.String("US"),
                            "user_group" to Value.String("beta_testers"),
                            "is_logged_in" to Value.Boolean(true),
                            "is_accessibility_user" to Value.Boolean(true),
                            "device" to Value.String("mobile"),
                            "theme_pref" to Value.String("light"),
                            "session_count" to Value.Double(150.0),
                            "region" to Value.String("US"),
                            "userId" to Value.Integer(3456),
                            "app_version" to Value.String("2.3.0"),
                            "user_tags" to Value.List(
                                listOf(
                                    Value.String("early-adopter"),
                                    Value.String("beta-tester"),
                                    Value.String("premium"),
                                ),
                            ),
                        )
                    }

                OpenFeatureAPI.setEvaluationContext(
                    ImmutableContext(
                        targetingKey = "3456",
                        newContextMap,
                    ),
                )
                contextVariant = if (contextVariant == "default") "alternate" else "default"
                darkModeEnabled = null
                stringValue = null
                doubleValue = null
                objectValue = null
            },
            enabled = isInitialized,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "Update Context (Switch to ${if (contextVariant == "default") "Alternate" else "Default"})",
            )
        }

        Button(
            onClick = {
                val result =
                    OpenFeatureAPI.getClient().getBooleanDetails(
                        key = "dark_mode_toggle",
                        defaultValue = false,
                    )
                darkModeEnabled = result.value
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
                    OpenFeatureAPI.getClient().getStringDetails(
                        key = "homepage_layout_test",
                        defaultValue = "default",
                    )
                stringValue = result.value
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
                    OpenFeatureAPI.getClient().getDoubleDetails(
                        key = "search_result_limit",
                        defaultValue = 10.0,
                    )
                doubleValue = result.value
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
                val defaultObjMap =
                    mapOf(
                        "limit" to Value.Integer(10),
                        "enabled" to Value.Boolean(false),
                    )
                val result =
                    OpenFeatureAPI.getClient().getObjectDetails(
                        key = "recommendations_config",
                        defaultValue = Value.Structure.invoke(defaultObjMap),
                    )
                val structure = result.value.asStructure()
                if (structure != null) {
                    val layout = structure.get("layout")?.asString() ?: ""
                    val theme = structure.get("theme")?.asString() ?: ""
                    val cards = structure.get("cards")?.asInteger() ?: 0
                    objectValue = "layout: $layout, theme: $theme, cards: $cards"
                } else {
                    objectValue = result.value.toString()
                }
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
    }
}
