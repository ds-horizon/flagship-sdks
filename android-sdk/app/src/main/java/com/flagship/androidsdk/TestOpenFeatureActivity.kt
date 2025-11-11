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
import com.flagship.sdk.facade.FlagshipProvider
import dev.openfeature.kotlin.sdk.ImmutableContext
import dev.openfeature.kotlin.sdk.OpenFeatureAPI
import dev.openfeature.kotlin.sdk.Value
import java.util.concurrent.TimeUnit

class TestOpenFeatureActivity : AppCompatActivity() {
    private lateinit var etFlagKey: EditText
    private lateinit var etTargetingKey: EditText
    private lateinit var tvResults: TextView
    private lateinit var btnTestBoolean: Button
    private lateinit var btnTestString: Button
    private lateinit var btnTestInt: Button
    private lateinit var btnTestDouble: Button
    private lateinit var btnTestObject: Button
    private lateinit var btnClearResults: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_feature_flags)

        initViews()
        initOpenFeature()
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
    }

    fun initOpenFeature() {
        val config =
            FlagShipConfig(
                applicationContext = this@TestOpenFeatureActivity.application,
                baseUrl = "http://10.0.2.2:8080", // Default URL, can be changed
                tenantId = "tenant1", // Default tenant, can be changed
                refreshInterval = TimeUnit.SECONDS.toMillis(10),
            )

        val provider = FlagshipProvider("ABcd", config)

        OpenFeatureAPI.setProvider(provider)

        OpenFeatureAPI.setEvaluationContext(
            ImmutableContext(
                targetingKey = "3456",
                getContext(),
            ),
        )

        // Context and targeting key are already provided during init elsewhere

        // Get the OpenFeature client and pass it to the RN SDK
        print("🚀 FeatureFlagManager: SDK initialized successfully")
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
        
        data class TestObj(
            val layout: String,
            val theme: String,
            val cards: Int,
        )
        val testObj = TestObj("vertical", "dark", 3)

        try {
            val mapOf : Map<String , Value> = mapOf(
                "layout" to Value.String("vertical"),
                "theme" to Value.String("dark"),
                "cards" to Value.Integer(3)
            )
            val result =
                OpenFeatureAPI.getClient().getObjectDetails(
                    key = flagKey,
                    defaultValue = Value.Structure.invoke(mapOf),
                )
            val resultOriginal = result.value.asStructure()?.get("layout")?.asString()
            val resultOriginal1 = result.value.asStructure()?.get("theme")?.asString()
            val resultOriginal2 = result.value.asStructure()?.get("cards")?.asInteger()

            val resultText =
                """
                🔵 OBJECT FLAG TEST
                Flag Key: $flagKey
                Value: $resultOriginal $resultOriginal1 $resultOriginal2
                Reason: ${result.reason}
                Variant Key: ${result.variant ?: "N/A"}
                
                """.trimIndent()

            appendResult(resultText)
            Log.d("TestFeatureFlags", "Object flag result: $result")
        } catch (e: Exception) {
            appendResult("❌ Object flag test failed: ${e.message}")
            Log.e("TestFeatureFlags", "Object flag test failed", e)
        }
    }

    private fun testBooleanFlag() {
        val flagKey = getFlagKey() ?: return

        try {
            val result =
                OpenFeatureAPI.getClient().getBooleanDetails(
                    key = flagKey,
                    defaultValue = false,
                )

            val resultText =
                """
                🔵 BOOLEAN FLAG TEST
                Flag Key: $flagKey
                Value: ${result.value}
                Reason: ${result.reason}
                Variant Key: ${result.variant ?: "N/A"}
                
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

        try {
            val result =
                OpenFeatureAPI.getClient().getStringDetails(
                    key = flagKey,
                    defaultValue = "default-string",
                )

            val resultText =
                """
                🟢 STRING FLAG TEST
                Flag Key: $flagKey
                Value: "${result.value}"
                Reason: ${result.reason}
                Variant Key: ${result.variant ?: "N/A"}
                
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

        try {
            val result =
                OpenFeatureAPI.getClient().getIntegerDetails(
                    key = flagKey,
                    defaultValue = 0,
                )

            val resultText =
                """
                🟡 INTEGER FLAG TEST
                Flag Key: $flagKey
                Value: ${result.value}
                Reason: ${result.reason}
                Variant Key: ${result.variant ?: "N/A"}
                
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

        try {
            val result =
                OpenFeatureAPI.getClient().getDoubleDetails(
                    key = flagKey,
                    defaultValue = 0.0,
                )

            val resultText =
                """
                🟠 DOUBLE FLAG TEST
                Flag Key: $flagKey
                Value: ${result.value}
                Reason: ${result.reason}
                Variant Key: ${result.variant ?: "N/A"}
                
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

    private fun getTargetingKey(): String = etTargetingKey.text.toString().trim().ifEmpty { "3456" }

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

    private fun getContext(): Map<String, Value> =
        mapOf(
            "user_tier" to Value.String("premium"),
            "country" to Value.String("US"),
            "user_group" to Value.String("beta_testers"),
            "is_logged_in" to Value.Boolean(true),
            "is_accessibility_user" to Value.Boolean(true),
            "device" to Value.String("mobile"),
            "theme_pref" to Value.String("light"),
            "session_count" to Value.Integer(150),
            "region" to Value.String("US"),
            "userId" to Value.Integer(3456),
            "app_version" to Value.String("2.3.0"),
        )
}
