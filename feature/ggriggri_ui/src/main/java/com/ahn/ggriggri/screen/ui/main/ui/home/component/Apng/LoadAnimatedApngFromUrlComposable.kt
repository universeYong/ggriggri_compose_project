package com.ahn.ggriggri.screen.ui.main.ui.home.component.Apng

import android.util.Log
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ahn.common_ui.R
import com.github.penfeizhou.animation.apng.APNGDrawable
import com.github.penfeizhou.animation.loader.Loader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.net.URL

@Composable
fun LoadAnimatedApngFromUrlComposable(imageUrl: String, modifier: Modifier = Modifier) {
    var apngDrawable by remember { mutableStateOf<APNGDrawable?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorOccurred by remember { mutableStateOf(false) }

    LaunchedEffect(imageUrl) {

        isLoading = true
        errorOccurred = false
        apngDrawable?.stop()
        apngDrawable = null

        if (imageUrl.isBlank()) {
            isLoading = false
            errorOccurred = true
            return@LaunchedEffect
        }

        launch(Dispatchers.IO) {
            runCatching {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()
                val bytes = inputStream.readBytes()
                inputStream.close()
                val customReader = com.github.penfeizhou.animation.io.StreamReader(
                    ByteArrayInputStream(bytes)
                )

                val loader = object : Loader {
                    override fun obtain(): com.github.penfeizhou.animation.io.Reader {
                        return customReader
                    }
                }

                val drawable = APNGDrawable(loader)
                val decoder = drawable.frameSeqDecoder
                Log.d("LoadAPNG", "APNGDrawable created. PreferredLoopCount: ${decoder}")

                withContext(Dispatchers.Main) {
                    apngDrawable = drawable
                    drawable.start()
                    Log.d(
                        "LoadAPNG",
                        "drawable.start() called. isRunning: ${drawable.isRunning}"
                    )
                    isLoading = false
                }
            }.getOrElse {
                Log.e("LoadAPNG", "Error loading APNG: $imageUrl", it)
                withContext(Dispatchers.Main) {
                    errorOccurred = true
                    isLoading = false
                }
            }
        }
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isLoading) {
            Log.d("LoadAPNG_UI", "UI State: isLoading for $imageUrl")
            CircularProgressIndicator(modifier = Modifier.size(30.dp))
        } else if (errorOccurred || apngDrawable == null) {
            Log.d(
                "LoadAPNG_UI",
                "UI State: errorOccurred ($errorOccurred) or apngDrawable is null (${apngDrawable == null}) for $imageUrl"
            )
            Image(
                painter = painterResource(id = R.drawable.baseline_error_24),
                contentDescription = "APNG ERROR",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Log.d(
                "LoadAPNG_UI",
                "UI State: Displaying AndroidView for $imageUrl. apngDrawable isNull: false, isRunning: ${apngDrawable?.isRunning}"
            )
            Log.d(
                "LoadAPNG_AndroidView",
                "AndroidView update block. apngDrawable isNull: ${apngDrawable == null}, isRunning: ${apngDrawable?.isRunning}"
            ) // ★★★ 로그 추가 ★★★
            AndroidView(
                factory = {
                    Log.d("LoadAPNG_AndroidView", "AndroidView factory block.") // ★★★ 로그 추가 ★★★
                    ImageView(it)
                },
                modifier = Modifier.fillMaxSize(),
                update = { imageView ->
                    Log.d(
                        "LoadAPNG_AndroidView",
                        "AndroidView update - Setting drawable. isRunning: ${apngDrawable?.isRunning}"
                    ) // ★★★ 로그 추가 ★★★
                    imageView.setImageDrawable(apngDrawable)
                }
            )
        }
    }
}