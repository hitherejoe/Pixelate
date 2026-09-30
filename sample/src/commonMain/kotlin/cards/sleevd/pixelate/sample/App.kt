package cards.sleevd.pixelate.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cards.sleevd.pixelate.DefaultBlockRatio
import cards.sleevd.pixelate.MinimumSafeBlockRatio
import cards.sleevd.pixelate.Pixelate
import cards.sleevd.pixelate.pixelate

@Composable
public fun App() {
    MaterialTheme { Scaffold { padding -> Sample(Modifier.padding(padding)) } }
}

@Composable
private fun Sample(modifier: Modifier = Modifier) {
    var revealed by remember { mutableStateOf(false) }
    var feather by remember { mutableStateOf(true) }
    var blockRatio by remember { mutableFloatStateOf(DefaultBlockRatio) }

    var nonce by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("compose-pixelate", style = MaterialTheme.typography.headlineSmall)

        Toggle("Reveal real values", revealed) { revealed = it }
        Toggle("Feather edges", feather) { feather = it }

        Column {
            Text(
                text =
                    "blockRatio ${blockRatio.format()}" +
                        if (blockRatio < MinimumSafeBlockRatio) " (below safe threshold)" else "",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (blockRatio < MinimumSafeBlockRatio) FontWeight.Bold else null,
            )
            Slider(value = blockRatio, onValueChange = { blockRatio = it }, valueRange = 0.05f..0.5f)
        }

        SampleCard("Modifier.pixelate() on one Text") {
            Text(
                text = "$1${nonce}4,502.88",
                style = MaterialTheme.typography.headlineMedium,
                modifier =
                    if (revealed) Modifier
                    else Modifier.pixelate(blockRatio = blockRatio, featherEdges = feather),
            )
        }

        SampleCard("Pixelate { } around a whole subtree") {
            if (revealed) {
                Figures(nonce)
            } else {
                Pixelate(blockRatio = blockRatio, featherEdges = feather) { Figures(nonce) }
            }
        }

        Button(onClick = { nonce = (nonce + 1) % 10 }, modifier = Modifier.fillMaxWidth()) {
            Text("Change the hidden value (currently $nonce)")
        }

        Text(
            "Tap to change the value. The mosaic should shift even while pixelated.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun Figures(nonce: Int) {
    Column {
        Text("$1${nonce}4,502.88", style = MaterialTheme.typography.headlineMedium)
        Text("+12.4% this month", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SampleCard(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Card { Column(Modifier.padding(16.dp)) { content() } }
    }
}

@Composable
private fun Toggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}

private fun Float.format(): String {
    val hundredths = (this * 100).toInt()
    return "0.${hundredths.toString().padStart(2, '0')}"
}
