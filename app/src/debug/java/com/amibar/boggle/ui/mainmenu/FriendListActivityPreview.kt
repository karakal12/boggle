package com.amibar.boggle.ui.mainmenu

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.amibar.boggle.ui.shared.SampleData
import com.amibar.boggle.ui.theme.BoggleTheme

@Preview(showBackground = true)
@Composable
fun FriendListScreenPreview() {
    BoggleTheme {
        FriendListScreenContent(
            friends = listOf(SampleData.player1, SampleData.player2),
            onAddFriend = {},
            onRefresh = {},
            onInviteFriend = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FriendListScreenPreviewWithSearch() {
    BoggleTheme {
        FriendListScreenContent(
            searchedUser = SampleData.player2,
            friends = listOf(SampleData.player1, SampleData.player2),
            searchQueryState = rememberTextFieldState("test@example.com"),
            onAddFriend = {},
            onRefresh = {},
            onInviteFriend = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
