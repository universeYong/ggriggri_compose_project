package com.ahn.ggriggri.screen.ui.main.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCompressionUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    suspend fun compressImage(uri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ImageCompressionUtils", "이미지 압축 시작: $uri")
                
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (bitmap == null) {
                    Log.e("ImageCompressionUtils", "비트맵 디코딩 실패")
                    return@withContext null
                }
                
                // 이미지 크기 조절 (최대 800x800)
                val maxSize = 800
                val ratio = minOf(
                    maxSize.toFloat() / bitmap.width, 
                    maxSize.toFloat() / bitmap.height
                )
                val newWidth = (bitmap.width * ratio).toInt()
                val newHeight = (bitmap.height * ratio).toInt()
                
                Log.d("ImageCompressionUtils", "원본 크기: ${bitmap.width}x${bitmap.height}")
                Log.d("ImageCompressionUtils", "압축 크기: ${newWidth}x${newHeight}")
                
                val compressedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                
                // 압축된 이미지를 임시 파일로 저장
                val tempFile = File.createTempFile("compressed_", ".jpg", context.cacheDir)
                val outputStream = FileOutputStream(tempFile)
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // 80% 품질
                outputStream.close()
                
                // 메모리 정리
                bitmap.recycle()
                compressedBitmap.recycle()
                
                val compressedUri = Uri.fromFile(tempFile)
                Log.d("ImageCompressionUtils", "이미지 압축 완료: $compressedUri")
                Log.d("ImageCompressionUtils", "압축된 파일 크기: ${tempFile.length()} bytes")
                
                compressedUri
            } catch (e: Exception) {
                Log.e("ImageCompressionUtils", "이미지 압축 실패", e)
                null
            }
        }
    }
}







