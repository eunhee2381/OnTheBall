package com.company.boogie.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.company.boogie.R
import com.company.boogie.models.ImageAnalyzer
import com.company.boogie.models.OverlayView
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * 카메라 설정과 TFLite 모델 로드, 실시간 추론을 구현
 *
 */
class Manager_CameraActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var tflite: Interpreter
    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView

    private var inputSize: Int = 0
    private var inputChannels: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_camera)

        Log.d("DEBUG", "onCreate called")

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // TFLite 모델 로드
//        tflite = Interpreter(loadModelFile("best.tflite"))

        Log.d("DEBUG", "Before loading TFLite model")
        tflite = Interpreter(loadModelFile("best.tflite"))
        Log.d("DEBUG", "After loading TFLite model")

//        try {
//            tflite = Interpreter(loadModelFile("best.tflite"))
//            Log.d("DEBUG", "Model loaded successfully")
//        } catch (e: Exception) {
//            Log.e("DEBUG", "Error loading model", e)
//        }

        // 입력 텐서의 크기 확인 - 디버깅 위한 부분
        val inputShape = tflite.getInputTensor(0).shape()
        val inputSize = inputShape[1]
        val inputChannels = inputShape[3]
        Log.d("DEBUG", "Model input size: $inputSize x $inputSize x $inputChannels")


        // 모델의 출력 텐서 크기 확인 - 디버깅 위한 부분
        val outputShape = tflite.getOutputTensor(0).shape()
        Log.d("DEBUG", "Model output size: ${outputShape.contentToString()}")

//        val outputShape = tflite.getOutputTensor(0).shape()
//        val outputBatchSize = outputShape[0]
//        val outputHeight = outputShape[1]
//        val outputWidth = outputShape[2]
//        val outputChannels = outputShape[3]
//        Log.d("Hello", "Model output size: ${outputBatchSize} x ${outputHeight} x ${outputWidth} x ${outputChannels}")


        // 카메라 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera(inputSize, inputChannels)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

    }

    // 카메라 권한 요청과 처리
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            startCamera(inputSize, inputChannels)
        } else {
            // 권한이 거부되었을 때의 처리
        }
    }

    // 실시간 객체 인식
    // 카메라 프레임을 캡처하여 추론을 수행하는 코드
    private fun startCamera(inputSize: Int, inputChannels: Int) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalyzer(tflite, overlayView))
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                // 카메라 시작 중 오류 발생 시 처리
                Log.e("DEBUG", "Error starting camera", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // TFLite 모델 파일 로드 함수
    private fun loadModelFile(fileName: String): MappedByteBuffer {
        Log.d("DEBUG", "Attempting to load model file: $fileName")
        try {
            val assetFileDescriptor = assets.openFd(fileName)
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            Log.d("DEBUG", "Model file loaded successfully")
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            Log.e("DEBUG", "Error loading model file: $fileName", e)
            throw RuntimeException("Error loading model file: $fileName", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        tflite.close()
    }

}