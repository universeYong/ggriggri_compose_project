package com.ahn.ggriggri.screen.ui.main.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageCompressionUtilsTest {

    @Test
    fun `최대 크기보다 작은 사진은 줄이지 않고 디코딩한다`() {
        assertEquals(1, ImageCompressionUtils.calculateInSampleSize(400, 300, 800))
    }

    @Test
    fun `두 배로 줄이면 최대 크기보다 작아지는 사진은 그대로 디코딩한다`() {
        assertEquals(1, ImageCompressionUtils.calculateInSampleSize(1500, 1500, 800))
    }

    @Test
    fun `1200만 화소 사진은 4분의 1로 줄여서 디코딩한다`() {
        // 4000x3000 -> 1000x750. 한 번 더 줄이면 긴 변이 500으로 800보다 작아진다.
        assertEquals(4, ImageCompressionUtils.calculateInSampleSize(4000, 3000, 800))
    }

    @Test
    fun `5000만 화소 사진은 8분의 1로 줄여서 디코딩한다`() {
        // 8160x6120 -> 1020x765
        assertEquals(8, ImageCompressionUtils.calculateInSampleSize(8160, 6120, 800))
    }

    @Test
    fun `세로 사진도 긴 변을 기준으로 줄인다`() {
        assertEquals(4, ImageCompressionUtils.calculateInSampleSize(3000, 4000, 800))
    }

    @Test
    fun `긴 파노라마 사진은 긴 변을 기준으로 줄인다`() {
        // 10000x1000 -> 1250x125
        assertEquals(8, ImageCompressionUtils.calculateInSampleSize(10000, 1000, 800))
    }
}
