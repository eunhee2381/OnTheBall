package com.company.boogie.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/** 이미지 분석 및 TFLite 추론을 수행
 * tflite 파일의 정보에 따라 수정 필요
 */
class ImageAnalyzer(private val tflite: Interpreter, private val overlayView: OverlayView) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = toBitmap(imageProxy)
        if (bitmap != null) {
            // 이미지를 분석하고 추론 결과를 가져옴
            val inputBuffer = convertBitmapToByteBuffer(bitmap)
            val output = Array(1) { Array(3) { Array(30) { Array(30) { FloatArray(25) } } } }
//            val output = Array(1) { Array(3) { Array(15) { Array(15) { FloatArray(25) } } } }

            tflite.run(inputBuffer, output)

            // 로그 추가
//            try {
//                tflite.run(inputBuffer, output)
//                Log.d("ImageAnalyzer", "Model inference run successfully")
//            } catch (e: Exception) {
//                Log.e("ImageAnalyzer", "Error during model inference", e)
//            }

            // 출력 데이터 확인을 위한 로그 추가
//            Log.d("ImageAnalyzer", "Output tensor shape: [1, 3, 15, 15, 25]")
//            Log.d("ImageAnalyzer", "Output tensor shape: ${output.contentDeepToString()}")

            // 추론 결과 처리 및 바운딩 박스 그리기
            val boxes = processOutput(output)
            overlayView.setBoxes(boxes)
        }
        imageProxy.close()  // 이미지 분석 후 반드시 close() 호출
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun toBitmap(imageProxy: ImageProxy): Bitmap? {
        val image = imageProxy.image ?: return null
        if (image.format != ImageFormat.YUV_420_888) {
            return null
        }
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE)
        inputBuffer.rewind()
        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)
        var pixel = 0
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val value = intValues[pixel++]
                inputBuffer.putFloat((value shr 16 and 0xFF) / 255.0f)
                inputBuffer.putFloat((value shr 8 and 0xFF) / 255.0f)
                inputBuffer.putFloat((value and 0xFF) / 255.0f)
            }
        }
//        Log.d("InputBuffer", "Input buffer filled")
        return inputBuffer
    }


    private fun processOutput(output: Array<Array<Array<Array<FloatArray>>>>): List<Box> {
        val boxes = mutableListOf<Box>()
        for (batchIndex in output.indices) {
            for (channelIndex in output[batchIndex].indices) {
                for (rowIndex in output[batchIndex][channelIndex].indices) {
                    for (cellIndex in output[batchIndex][channelIndex][rowIndex].indices) {
                        val cell = output[batchIndex][channelIndex][rowIndex][cellIndex]
                        val confidence = cell[4]
                        Log.d("Box", "Confidence: $confidence, Cell: ${cell.joinToString()}")
                        if (confidence > CONFIDENCE_THRESHOLD) {
                            val x = cell[0]
                            val y = cell[1]
                            val width = cell[2]
                            val height = cell[3]
                            val left = (x - width / 2) * INPUT_SIZE
                            val top = (y - height / 2) * INPUT_SIZE
                            val right = (x + width / 2) * INPUT_SIZE
                            val bottom = (y + height / 2) * INPUT_SIZE
                            boxes.add(Box(left, top, right, bottom))
                            Log.d("Box", "Added box: ($left, $top, $right, $bottom) with confidence $confidence")
                        } else {
                            Log.d("Box", "Skipped box with confidence $confidence")
                        }
                    }
                }
            }
        }
        return boxes
    }

    companion object {
        private const val INPUT_SIZE = 480  // 모델의 입력 크기에 맞게 설정
        private const val PIXEL_SIZE = 3
        private const val OUTPUT_SIZE = 25  // 모델의 출력 크기에 맞게 설정
        private const val CONFIDENCE_THRESHOLD = 0.05  // 바운딩 박스를 그리기 위한 신뢰도 임계값
    }
}