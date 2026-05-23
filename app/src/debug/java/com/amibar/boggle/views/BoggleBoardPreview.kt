package com.amibar.boggle.views

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.amibar.boggle.ui.shared.SampleData
import com.amibar.boggle.ui.theme.BoggleTheme

@Preview(name = "BoggleBoardNight", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "BoggleBoardLight", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun BoggleBoardPreview() {
    BoggleTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {
            BoggleBoard(
                viewModel = SampleData.boggleViewModel
            )
        }
    }
}
