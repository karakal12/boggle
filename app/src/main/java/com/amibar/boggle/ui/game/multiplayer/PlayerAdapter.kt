package com.amibar.boggle.ui.game.multiplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.amibar.boggle.R
import com.amibar.boggle.data.User
import com.amibar.boggle.databinding.ItemPlayerBinding
import com.amibar.boggle.ui.game.multiplayer.PlayerAdapter.PlayerViewHolder
import com.amibar.boggle.ui.theme.BoggleTheme
import com.amibar.boggle.utils.base64ToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Adapter for displaying a list of players in the multiplayer lobby.
 * Binds User data to a layout showing their name and profile image.
 */
class PlayerAdapter
/**
 * Constructs a new PlayerAdapter.
 * @param playerList The list of players to display.
 */(
    /** List of users currently in the lobby.  */
    private val playerList: MutableList<User?>?
) : RecyclerView.Adapter<PlayerViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemPlayerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = playerList!![position]
        // Bind the player object to the layout using Data Binding
        holder.binding.setPlayer(player)
    }

    override fun getItemCount(): Int {
        return playerList?.size ?: 0
    }

    /**
     * ViewHolder class for individual player items in the RecyclerView.
     */
    class PlayerViewHolder
    /**
     * @param binding The binding object for the item layout.
     */(
        /** View binding for the player item layout.  */
        val binding: ItemPlayerBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    )
}

@Composable
fun Player(
    modifier: Modifier = Modifier,
    player: User
) {
    val bitmap = remember(player.profileImageBase64) {
        base64ToBitmap(player.profileImageBase64)
    }

    var backgroundColor by remember(bitmap) { mutableStateOf(Color.Transparent) }

    LaunchedEffect(bitmap) {
        bitmap?.let {
            val palette = withContext(Dispatchers.Default) {
                Palette.from(it).generate()
            }
            // Use dominant color with low alpha for a subtle background
            val color = palette.getDominantColor(0)
            if (color != 0) {
                backgroundColor = Color(color).copy(alpha = 0.15f)
            }
        } ?: run {
            backgroundColor = Color.Transparent
        }
    }

    Row(
        modifier = modifier
            .background(backgroundColor)
            .heightIn(max = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (bitmap != null) {
            Icon(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "profile picture",
                modifier = Modifier
                    .height(48.dp)
                    .width(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                tint = Color.Unspecified
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = "profile picture",
                modifier = Modifier
                    .height(48.dp)
                    .width(48.dp)
            )
        }
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            text = player.displayName,
            autoSize = TextAutoSize.StepBased(),
            softWrap = false,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview
@Composable
private fun PlayerPreview() {
    BoggleTheme {
        Surface {
            Player(
                player = User(displayName = "John Doe")
            )
        }
    }
}
