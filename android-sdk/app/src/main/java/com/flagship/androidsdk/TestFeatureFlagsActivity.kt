package com.flagship.androidsdk

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flagship.sdk.facade.FlagShipClient
import com.flagship.sdk.facade.FlagShipConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class TestFeatureFlagsActivity : AppCompatActivity() {
    private lateinit var etFlagKey: EditText
    private lateinit var etTargetingKey: EditText
    private lateinit var tvResults: TextView
    private lateinit var btnTestBoolean: Button
    private lateinit var btnTestString: Button
    private lateinit var btnTestInt: Button
    private lateinit var btnTestDouble: Button
    private lateinit var btnTestObject: Button
    private lateinit var btnClearResults: Button

    private var flagshipClient: FlagShipClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_feature_flags)

        initViews()
        initFlagshipClient()
        setupClickListeners()
    }

    private fun initViews() {
        etFlagKey = findViewById(R.id.etFlagKey)
        etTargetingKey = findViewById(R.id.etTargetingKey)
        tvResults = findViewById(R.id.tvResults)
        btnTestBoolean = findViewById(R.id.btnTestBoolean)
        btnTestString = findViewById(R.id.btnTestString)
        btnTestInt = findViewById(R.id.btnTestInt)
        btnTestDouble = findViewById(R.id.btnTestDouble)
        btnTestObject = findViewById(R.id.btnTestObj)
        btnClearResults = findViewById(R.id.btnClearResults)

        // Set default values
        etTargetingKey.setText("3456")
        etFlagKey.setText("dark_mode_toggle")
    }

    private fun initFlagshipClient() {
        try {
            val config =
                FlagShipConfig(
                    applicationContext = application,
                    baseUrl = "http://10.0.2.2:8080",
                    flagshipApiKey = "tenant1",
                    refreshInterval =
                        java.util.concurrent.TimeUnit.SECONDS
                            .toMillis(30),
                )

            flagshipClient = FlagShipClient.getInstance("test-domain", config)
            appendResult("✅ Flagship client initialized successfully")
        } catch (e: Exception) {
            appendResult("❌ Failed to initialize Flagship client: ${e.message}")
            Log.e("TestFeatureFlags", "Failed to initialize Flagship client", e)
        }
    }

    private fun setupClickListeners() {
        btnTestBoolean.setOnClickListener {
            testBooleanFlag()
        }

        btnTestString.setOnClickListener {
            testStringFlag()
        }

        btnTestInt.setOnClickListener {
            testIntFlag()
        }

        btnTestDouble.setOnClickListener {
            testDoubleFlag()
        }

        btnTestObject.setOnClickListener {
            testObjectFlag()
        }

        btnClearResults.setOnClickListener {
            tvResults.text = "Results cleared"
        }
    }

    private fun testObjectFlag() {
        val flagKey = getFlagKey() ?: return
        val targetingKey = getTargetingKey()

        @Serializable
        data class TestObj(
            val layout: String,
            val theme: String,
            val cards: Int,
        )

        val testObj = buildJsonObject {
            put("layout" , "vertical")
            put("theme" , "dark")
            put("cards" , 3)
        }

        try {
            val result =
                flagshipClient?.getJson(
                    key = flagKey,
                    defaultValue = testObj,
                    targetingKey = targetingKey,
                    context = getContext(),
                )

            // For object flags, SDK currently returns a JsonObject from kotlinx.serialization.json
            val jsonObject: JsonObject? = result?.value
            val layout = jsonObject?.get("layout")?.jsonPrimitive?.content ?: ""
            val theme = jsonObject?.get("theme")?.jsonPrimitive?.content ?: ""
            val cards = jsonObject?.get("cards")?.jsonPrimitive?.intOrNull ?: 0
            val resultText =
                """
                🔵 OBJECT FLAG TEST
                Flag Key: $flagKey
                Targeting Key: $targetingKey
                Variant Key: ${result?.variant ?: "N/A"}
                Value : Layout: $layout, Theme: $theme, Cards: $cards
                Reason: ${result?.reason}
                
                """.trimIndent()
            appendResult(resultText)
            //  Value: ${result?.value.toString()}
            Log.d("TestFeatureFlags", "Object flag result: $result")
        } catch (e: Exception) {
            appendResult("❌ Object flag test failed: ${e.message}")
            Log.e("TestFeatureFlags", "Object flag test failed", e)
        }
    }

    private fun testBooleanFlag() {
        val flagKey = getFlagKey() ?: return
        val targetingKey = getTargetingKey()

        try {
            val result =
                flagshipClient?.getBoolean(
                    key = flagKey,
                    defaultValue = false,
                    targetingKey = targetingKey,
                    context = getContext(),
                )

            val resultText =
                """
                🔵 BOOLEAN FLAG TEST
                Flag Key: $flagKey
                Targeting Key: $targetingKey
                Value: ${result?.value}
                Reason: ${result?.reason}
                Variant Key: ${result?.variant ?: "N/A"}
                
                """.trimIndent()

            appendResult(resultText)
            Log.d("TestFeatureFlags", "Boolean flag result: $result")
        } catch (e: Exception) {
            appendResult("❌ Boolean flag test failed: ${e.message}")
            Log.e("TestFeatureFlags", "Boolean flag test failed", e)
        }
    }

    private fun testStringFlag() {
        val flagKey = getFlagKey() ?: return
        val targetingKey = getTargetingKey()

        try {
            val result =
                flagshipClient?.getString(
                    key = flagKey,
                    defaultValue = "default-string",
                    targetingKey = targetingKey,
                    context = getContext(),
                )

            val resultText =
                """
                🟢 STRING FLAG TEST
                Flag Key: $flagKey
                Targeting Key: $targetingKey
                Value: "${result?.value}"
                Reason: ${result?.reason}
                Variant Key: ${result?.variant ?: "N/A"}
                
                """.trimIndent()

            appendResult(resultText)
            Log.d("TestFeatureFlags", "String flag result: $result")
        } catch (e: Exception) {
            appendResult("❌ String flag test failed: ${e.message}")
            Log.e("TestFeatureFlags", "String flag test failed", e)
        }
    }

    private fun testIntFlag() {
        val flagKey = getFlagKey() ?: return
        val targetingKey = getTargetingKey()

        try {
            val result =
                flagshipClient?.getInt(
                    key = flagKey,
                    defaultValue = 0,
                    targetingKey = targetingKey,
                    context = getContext(),
                )

            val resultText =
                """
                🟡 INTEGER FLAG TEST
                Flag Key: $flagKey
                Targeting Key: $targetingKey
                Value: ${result?.value}
                Reason: ${result?.reason}
                Variant Key: ${result?.variant ?: "N/A"}
                
                """.trimIndent()

            appendResult(resultText)
            Log.d("TestFeatureFlags", "Int flag result: $result")
        } catch (e: Exception) {
            appendResult("❌ Integer flag test failed: ${e.message}")
            Log.e("TestFeatureFlags", "Integer flag test failed", e)
        }
    }

    private fun testDoubleFlag() {
        val flagKey = getFlagKey() ?: return
        val targetingKey = getTargetingKey()

        try {
            val result =
                flagshipClient?.getDouble(
                    key = flagKey,
                    defaultValue = 0.0,
                    targetingKey = targetingKey,
                    context = getContext(),
                )

            val resultText =
                """
                🟠 DOUBLE FLAG TEST
                Flag Key: $flagKey
                Targeting Key: $targetingKey
                Value: ${result?.value}
                Reason: ${result?.reason}
                Variant Key: ${result?.variant ?: "N/A"}
                
                """.trimIndent()

            appendResult(resultText)
            Log.d("TestFeatureFlags", "Double flag result: $result")
        } catch (e: Exception) {
            appendResult("❌ Double flag test failed: ${e.message}")
            Log.e("TestFeatureFlags", "Double flag test failed", e)
        }
    }

    private fun getFlagKey(): String? {
        val flagKey = etFlagKey.text.toString().trim()
        if (flagKey.isEmpty()) {
            Toast.makeText(this, "Please enter a flag key", Toast.LENGTH_SHORT).show()
            return null
        }
        return flagKey
    }

    private fun getTargetingKey(): String =
        etTargetingKey.text
            .toString()
            .trim()
            .ifEmpty { "anonymous-user" }

    private fun appendResult(result: String) {
        val currentText = tvResults.text.toString()
        val newText =
            if (currentText == "No tests run yet" || currentText == "Results cleared") {
                result
            } else {
                "$currentText\n$result"
            }
        tvResults.text = newText
    }

    private fun getContext(): Map<String, Any> =
        mapOf(
            "user_tier" to "premium",
            "country" to "US",
            "user_group" to "beta_testers",
            "is_logged_in" to true,
            "is_accessibility_user" to true,
            "device" to "mobile",
            "theme_pref" to "light",
            "session_count" to 150.0,
            "region" to "US",
            "userId" to 3456,
            "app_version" to "2.3.0",
            "user_tags" to listOf("early-adopter", "beta-tester", "premium")
        )
}
