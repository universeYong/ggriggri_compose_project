package com.ahn.ggriggri.screen.ui.main.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
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

                // 1) 픽셀은 읽지 않고 크기만 먼저 읽는다.
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, bounds)
                }
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                    Log.e("ImageCompressionUtils", "이미지 크기 읽기 실패")
                    return@withContext null
                }

                // 2) 원본을 통째로 메모리에 올리지 않도록, 필요한 크기에 가깝게 줄여서 디코딩한다.
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_SIZE)
                }
                val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                }

                if (bitmap == null) {
                    Log.e("ImageCompressionUtils", "비트맵 디코딩 실패")
                    return@withContext null
                }

                // 이미지 크기 조절 (최대 800x800, 작은 사진은 확대하지 않음)
                val (newWidth, newHeight) = calculateTargetSize(bitmap.width, bitmap.height, MAX_SIZE)

                Log.d("ImageCompressionUtils", "원본 크기: ${bounds.outWidth}x${bounds.outHeight}")
                Log.d("ImageCompressionUtils", "압축 크기: ${newWidth}x${newHeight}")

                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

                // 카메라 사진은 회전 정보를 EXIF에만 적어 두므로, 다시 저장하기 전에 픽셀에 반영한다.
                // (JPEG로 다시 저장하면 EXIF가 사라져 세로 사진이 누워 보인다.)
                val degrees = rotationDegrees(readExifOrientation(uri))
                val compressedBitmap = if (degrees == 0) {
                    scaledBitmap
                } else {
                    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
                    Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
                        .also { if (it !== scaledBitmap) scaledBitmap.recycle() }
                }

                // 압축된 이미지를 임시 파일로 저장
                val tempFile = File.createTempFile(TEMP_PREFIX, ".jpg", context.cacheDir)
                try {
                    FileOutputStream(tempFile).use { outputStream ->
                        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // 80% 품질
                    }
                } catch (e: Exception) {
                    // 쓰다가 실패한 파일을 캐시에 남기지 않는다.
                    tempFile.delete()
                    throw e
                }

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

    /** EXIF의 방향 값을 읽는다. 읽지 못하면(EXIF가 없는 PNG 등) 회전 없음으로 본다. */
    private fun readExifOrientation(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            Log.w("ImageCompressionUtils", "EXIF 방향 읽기 실패: ${e.javaClass.simpleName}")
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    /**
     * [compressImage]가 만든 임시 파일을 지운다. 업로드가 끝나면(성공·실패 모두) 호출한다.
     * 이 클래스가 캐시 폴더에 만든 파일이 아니면 건드리지 않는다.
     */
    suspend fun deleteTempFile(uri: Uri) {
        withContext(Dispatchers.IO) {
            val file = uri.path?.let { File(it) } ?: return@withContext
            val isOwnTempFile = file.parentFile == context.cacheDir && file.name.startsWith(TEMP_PREFIX)
            if (isOwnTempFile && !file.delete()) {
                Log.w("ImageCompressionUtils", "임시 파일 삭제 실패: ${file.name}")
            }
        }
    }

    companion object {
        private const val TEMP_PREFIX = "compressed_"

        /** 압축 결과의 가로·세로 최대 크기(px). */
        const val MAX_SIZE = 800

        /**
         * 디코딩할 때 쓸 축소 배수(2의 거듭제곱)를 구한다.
         * 최종 결과는 긴 변이 [maxSize]가 되므로, 줄인 뒤에도 긴 변이 [maxSize] 이상으로 남는
         * 가장 큰 배수를 골라 이후의 정밀 축소에서 화질이 떨어지지 않게 한다.
         */
        fun calculateInSampleSize(width: Int, height: Int, maxSize: Int): Int {
            val longSide = maxOf(width, height)
            var sampleSize = 1
            while (longSide / (sampleSize * 2) >= maxSize) {
                sampleSize *= 2
            }
            return sampleSize
        }

        /**
         * EXIF 방향 값을 시계 방향 회전 각도로 바꾼다.
         * 좌우 반전이 섞인 값(전면 카메라 일부)은 드물어 회전하지 않고 그대로 둔다.
         */
        fun rotationDegrees(exifOrientation: Int): Int = when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        /**
         * 비율을 유지하면서 긴 변이 [maxSize]를 넘지 않는 크기를 구한다.
         * 이미 작은 사진은 확대하지 않고 그대로 둔다.
         */
        fun calculateTargetSize(width: Int, height: Int, maxSize: Int): Pair<Int, Int> {
            val ratio = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height).coerceAtMost(1f)
            return Pair(
                (width * ratio).toInt().coerceAtLeast(1),
                (height * ratio).toInt().coerceAtLeast(1)
            )
        }
    }
}
