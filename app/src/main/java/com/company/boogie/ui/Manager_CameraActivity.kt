package com.company.boogie.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.company.boogie.R
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * 카메라 설정과 TFLite 모델 로드, 실시간 추론을 구현
 */
class Manager_CameraActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var bitmap: Bitmap
    private lateinit var tflite: Interpreter
    private var isImageLoaded: Boolean = false
    private var detectionToast: Toast? = null

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var toastRunnable: Runnable
    private var toastCount = 0
    private val maxToastCount = 3

    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_IMAGE_CAPTURE = 2
    private val REQUEST_CAMERA_PERMISSION = 3

    private val classLabels = listOf(
        "Raspberry-Pi-1-Model-A+", "Raspberry-Pi-1-Model-B+", "Raspberry-Pi-2-Model-B",
        "Raspberry-Pi-3-Model-A+", "Raspberry-Pi-3-Model-B", "Raspberry-Pi-3-Model-B+",
        "Raspberry-Pi-4-Model-B", "Raspberry-Pi-5", "Raspberry-Pi-Zero-2-W", "Raspberry-Pi-Zero-WH",
        "Raspberry-Pi-Zero", "Raspberry-Pi-Zero-W", "Arduino-Nano", "Arduino-Pro-Mini", "Arduino-Mega",
        "Arduino-Zero", "Arduino-Uno", "Arduino-leonardo", "Arduino-Due", "Arduino-Micro"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_camera)

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        val cambtn: Button = findViewById(R.id.cameraButton)    // 카메라 선택
        val galbtn: Button = findViewById(R.id.galleryButton)    // 갤러리 이미지
        val detbtn: Button = findViewById(R.id.detectionButton)    // 객체 탐지

        // 모델 초기화
        initializeInterpreter("yolov5.tflite")

        // Load TFLite model
        tflite = Interpreter(loadModelFile())

        // 카메라 버튼 클릭 시
        cambtn.setOnClickListener {
            textView.text = ""
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA_PERMISSION)
            } else {
                dispatchTakePictureIntent()
            }
        }

        // 이미지 선택 버튼 클릭 시
        galbtn.setOnClickListener {
            textView.text = ""
            openImagePicker()
        }

        // 객체 탐지 버튼 클릭 시
        detbtn.setOnClickListener {
            if (isImageLoaded) {
//                Toast.makeText(this, "물체를 탐지중입니다.\n잠시만 기다려주세요", Toast.LENGTH_SHORT).show() // Toast 메시지 추가
                showDetectionToast()
                detectObjects()
            } else {
                showImageNotSelectedAlert()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
        isImageLoaded = true
    }

    private fun showImageNotSelectedAlert() {
        AlertDialog.Builder(this)
            .setTitle("알림")
            .setMessage("이미지를 선택하세요")
            .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
        isImageLoaded = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            imageUri?.let {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            bitmap = extras?.get("data") as Bitmap
            imageView.setImageBitmap(bitmap)
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("yolov5.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun initializeInterpreter(modelPath: String) {
        try {
            val assetFileDescriptor = assets.openFd(modelPath)
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            tflite = Interpreter(modelBuffer)

            // 모델의 입력 및 출력 크기 로그 출력
            val inputTensor = tflite.getInputTensor(0)
            val outputTensor = tflite.getOutputTensor(0)

            Log.d("dog", "Model Input Tensor Shape: ${inputTensor.shape().contentToString()}")
            Log.d("dog", "Model Output Tensor Shape: ${outputTensor.shape().contentToString()}")

            Log.d("dog", "Model Input DataType: ${inputTensor.dataType()}")
            Log.d("dog", "Model Output DataType: ${outputTensor.dataType()}")

        } catch (e: IOException) {
            Log.e("dog", "Error initializing TensorFlow Lite Interpreter.", e)
        }
    }

    // 안드로이드 코드에서 전처리 과정 수정
    private fun preprocessImage(bitmap: Bitmap, inputSize: Int): ByteBuffer {
        val inputBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val inputBuffer = ByteBuffer.allocateDirect(1 * 3 * inputSize * inputSize * 4).order(ByteOrder.nativeOrder())

        val floatArray = FloatArray(inputSize * inputSize * 3)
        var idx = 0
        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixelValue = inputBitmap.getPixel(x, y)
                floatArray[idx++] = ((pixelValue shr 16) and 0xFF) / 255.0f // R
                floatArray[idx++] = ((pixelValue shr 8) and 0xFF) / 255.0f  // G
                floatArray[idx++] = (pixelValue and 0xFF) / 255.0f          // B
            }
        }

        // 코랩과 동일하게 이미지 순서 조정 (H, W, C) -> (C, H, W)
        val reorderedFloatArray = FloatArray(inputSize * inputSize * 3)
        for (i in 0 until inputSize * inputSize) {
            reorderedFloatArray[i * 3] = floatArray[i * 3 + 2]  // B
            reorderedFloatArray[i * 3 + 1] = floatArray[i * 3 + 1]  // G
            reorderedFloatArray[i * 3 + 2] = floatArray[i * 3]  // R
        }

        inputBuffer.asFloatBuffer().put(reorderedFloatArray)

        // 디버깅용 로그
        Log.d("dog", "Preprocessed Image Buffer (first 100 floats): ${reorderedFloatArray.slice(0 until 100).joinToString(", ")}")


        return inputBuffer
    }

    private fun detectObjects() {
        val inputSize = 480
        val inputBuffer = preprocessImage(bitmap, inputSize)
        val outputBuffer = Array(1) { Array(14175) { FloatArray(25) } }

        if (::tflite.isInitialized) {
            tflite.run(inputBuffer, outputBuffer)
        } else {
            Log.e("dog", "TensorFlow Lite Interpreter is not initialized.")
            return
        }

        // 모델 출력 디버깅 로그 추가
        for (i in 0 until 10) {
            Log.d("dog", "Output[0][$i]: ${outputBuffer[0][i].contentToString()}")
        }

        val confidenceThreshold = 0.01  // 신뢰도 임계값
        val boxes = ArrayList<RectF>()
        val scores = ArrayList<Float>()
        val classIds = ArrayList<Int>()

        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        for (i in outputBuffer[0].indices) {
            val confidence = outputBuffer[0][i][4]
            if (confidence > confidenceThreshold) {
                val classScores = outputBuffer[0][i].slice(5 until outputBuffer[0][i].size)
                val maxClassScore = classScores.maxOrNull() ?: 0.0f
                val classId = classScores.indexOf(maxClassScore)
                val score = maxClassScore
                if (score > confidenceThreshold) {
                    val x_center = outputBuffer[0][i][0] * originalWidth / inputSize
                    val y_center = outputBuffer[0][i][1] * originalHeight / inputSize
                    val width = outputBuffer[0][i][2] * originalWidth / inputSize
                    val height = outputBuffer[0][i][3] * originalHeight / inputSize
                    val x1 = x_center - (width / 2)
                    val y1 = y_center - (height / 2)
                    val x2 = x_center + (width / 2)
                    val y2 = y_center + (height / 2)

                    boxes.add(RectF(x1, y1, x2, y2))
                    scores.add(score)
                    classIds.add(classId)
                }
            }
        }

        // 디버깅: 탐지된 객체 정보 출력
        Log.d("dog", "Detected boxes before NMS: ${boxes.joinToString(", ")}")
        Log.d("dog", "Detected scores before NMS: ${scores.joinToString(", ")}")
        Log.d("dog", "Detected class IDs before NMS: ${classIds.joinToString(", ")}")

        val nmsIndices = nonMaxSuppression(boxes, scores, 0.5f, 0.4f)
        val detectedObjects = nmsIndices.map { index ->
            val box = boxes[index]
            val score = scores[index]
            val classId = classIds[index]
            val label = classLabels[classId]
            "$label: ${score * 100.0}% at [${box.left}, ${box.top}, ${box.right}, ${box.bottom}]"
        }

        Log.d("dog", "Detected Objects: ${detectedObjects.joinToString("\n")}")
        textView.text = detectedObjects.joinToString("\n")
        drawDetectedObjects(boxes, nmsIndices)
        hideDetectionToast()
    }

    private fun drawDetectedObjects(boxes: ArrayList<RectF>, nmsIndices: List<Int>) {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 2.0f
        }

        nmsIndices.forEach { index ->
            val box = boxes[index]
            canvas.drawRect(box, paint)
        }

        imageView.setImageBitmap(mutableBitmap)
    }

    private fun nonMaxSuppression(boxes: ArrayList<RectF>, scores: ArrayList<Float>, scoreThreshold: Float, iouThreshold: Float): List<Int> {
        val indices = ArrayList<Int>()
        val order = scores.withIndex().sortedByDescending { it.value }.map { it.index }.toMutableList()

        while (order.isNotEmpty()) {
            val index = order[0]
            indices.add(index)
            val boxA = boxes[index]

            val remain = order.drop(1).filter { i ->
                val boxB = boxes[i]
                calculateIOU(boxA, boxB) < iouThreshold
            }
            order.clear()
            order.addAll(remain)
        }

        return indices
    }


    private fun calculateIOU(boxA: RectF, boxB: RectF): Float {
        val intersection = RectF(
            maxOf(boxA.left, boxB.left),
            maxOf(boxA.top, boxB.top),
            minOf(boxA.right, boxB.right),
            minOf(boxA.bottom, boxB.bottom)
        )

        val intersectionArea = maxOf(intersection.width(), 0f) * maxOf(intersection.height(), 0f)
        val boxAArea = boxA.width() * boxA.height()
        val boxBArea = boxB.width() * boxB.height()

        return intersectionArea / (boxAArea + boxBArea - intersectionArea)
    }


    private fun showDetectionToast() {
        detectionToast = Toast.makeText(this, "물체를 탐지중입니다.\n잠시만 기다려주세요", Toast.LENGTH_SHORT)
        detectionToast?.show()

        // Re-show the toast every short interval to keep it visible, up to 3 times
        toastCount = 1 // Reset toast count when starting

        toastRunnable = object : Runnable {
            override fun run() {
                if (toastCount < maxToastCount) {
                    detectionToast?.show()
                    toastCount++
                    handler.postDelayed(this, 2000) // Show toast every 2 seconds
                }
            }
        }
        handler.postDelayed(toastRunnable, 2000) // Start showing after 2 seconds

        // Stop showing the toast when the text is set in textView
        textView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (textView.text.isNotEmpty()) {
                handler.removeCallbacks(toastRunnable)
                detectionToast?.cancel()
            }
        }
    }


    private fun hideDetectionToast() {
        handler.removeCallbacks(toastRunnable)
        detectionToast?.cancel()
        toastCount = 0 // Reset toast count when hiding
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Log.e("Permission", "Camera permission denied")
                // 사용자에게 권한 거부 알림
            }
        }
    }
}