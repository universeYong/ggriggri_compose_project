package com.ahn.ggriggri.screen.main.home.component.cardview

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun QuestionCard(
   question: String,
   emojiDrawable: Drawable,
   backgroundColor: Color
) {
    CustomCard(backgroundColor = backgroundColor) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ){
            AndroidView(
                factory = { context ->
                    ImageView(context).apply { setImageDrawable(emojiDrawable) }
                },
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}