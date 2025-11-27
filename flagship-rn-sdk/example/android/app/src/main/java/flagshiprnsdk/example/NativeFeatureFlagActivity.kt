package flagshiprnsdk.example

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.flagshiprnsdk.android.FlagshipSdk

class NativeFeatureFlagActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var flagValueText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val titleText = TextView(this).apply {
            text = "Native Android Feature Flag Screen"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        }
        layout.addView(titleText)

        statusText = TextView(this).apply {
            text = "SDK Initialized: ${FlagshipSdk.isInitialized()}"
            textSize = 16f
            setPadding(0, 0, 0, 16)
        }
        layout.addView(statusText)

        flagValueText = TextView(this).apply {
            text = "Flag Value: Not evaluated yet"
            textSize = 18f
            setPadding(0, 0, 0, 32)
        }
        layout.addView(flagValueText)

        val evaluateButton = Button(this).apply {
            text = "Evaluate dark_mode_toggle"
            setOnClickListener {
                val value = FlagshipSdk.getBooleanValue("dark_mode_toggle", false)
                flagValueText.text = "dark_mode_toggle: $value"
            }
        }
        layout.addView(evaluateButton)

        val setContextButton = Button(this).apply {
            text = "Set Context from Native"
            setOnClickListener {
                val success = FlagshipSdk.setContext(
                    targetingKey = "native-user-789",
                    context = mapOf(
                        "platform" to "android_native",
                        "user_tier" to "premium",
                        "is_logged_in" to true,
                        "cohort" to listOf("SPORTANS")
                    )
                )
                statusText.text = "Context Set: $success | SDK Initialized: ${FlagshipSdk.isInitialized()}"
            }
        }
        layout.addView(setContextButton)

        val closeButton = Button(this).apply {
            text = "Close"
            setOnClickListener {
                finish()
            }
        }
        layout.addView(closeButton)

        setContentView(layout)
    }
}

