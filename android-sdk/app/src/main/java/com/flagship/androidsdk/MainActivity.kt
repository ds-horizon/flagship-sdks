package com.flagship.androidsdk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flagship.androidsdk.ui.theme.AndroidsdkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidsdkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onOpenFeatureClick = {
                            startActivity(Intent(this@MainActivity, TestOpenFeatureActivity::class.java))
                        },
                        onTestFeatureFlagsClick = {
                            startActivity(Intent(this@MainActivity, TestFeatureFlagsActivity::class.java))
                        },
                    )
                }
            }
        }
//        lifecycleScope.launch {
//            Log.d("Farhan", "Fetching config")
//            FlagshipHttpTransport.createForTesting(enableLogging = true).fetchConfig().collect { value ->
//                 Log.d("Farhan", "Config fetched: $value")
//            }
//
//        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onOpenFeatureClick: () -> Unit = {},
    onTestFeatureFlagsClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Flagship SDK Demo",
            modifier = Modifier.padding(bottom = 32.dp),
        )

        Button(
            onClick = onOpenFeatureClick,
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Open OpenFeature Tests")
        }

        Button(
            onClick = onTestFeatureFlagsClick,
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Test Feature Flags")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AndroidsdkTheme {
        MainScreen()
    }
}
