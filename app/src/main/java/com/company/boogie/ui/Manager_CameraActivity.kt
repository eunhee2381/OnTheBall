package com.company.boogie.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest  // 카메라 권한 관련 import 추가
import com.company.boogie.R
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


/**
 * 카메라 설정과 TFLite 모델 로드, 실시간 추론을 구현
 *
 */


class Manager_CameraActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var bitmap: Bitmap
    private lateinit var tflite: Interpreter

    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_IMAGE_CAPTURE = 2
    private val REQUEST_CAMERA_PERMISSION = 3

    private val classLabels = listOf(
        "Raspberry-Pi-1-Model-A-", "Raspberry-Pi-1-Model-B-", "Raspberry-Pi-2-Model-B",
        "Raspberry-Pi-3-Model-A-", "Raspberry-Pi-3-Model-B", "Raspberry-Pi-3-Model-B-",
        "Raspberry-Pi-4-Model-B", "Raspberry-Pi-5", "Raspberry-Pi-Zero-2-W", "Raspberry-Pi-Zero-WH",
        "Raspberry-Pi-Zero", "Raspberry-Pi-Zero-W", "Arduino-Nano", "Arduino-Pro-Mini", "Arduino-Mega",
        "Arduino-Zero", "Arduino-Uno", "Arduino-leonardo", "Arduino-Due", "Arduino-Micro"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_camera)

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        val button1: Button = findViewById(R.id.button1)    // 카메라 버튼
        val button2: Button = findViewById(R.id.button2)
        val button3: Button = findViewById(R.id.button3)

        // Load TFLite model
        tflite = Interpreter(loadModelFile())

        button1.setOnClickListener {
            // 카메라 권한이 부여되었는지 확인
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA_PERMISSION)
            } else {
                dispatchTakePictureIntent()
            }
        }

        button2.setOnClickListener {
            detectObjects()
        }

        button3.setOnClickListener {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
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

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("yolov5.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun detectObjects() {
        // Assuming the model requires 480x480 images
        val inputSize = 480

        // Resize and preprocess the image
        val inputBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val inputBuffer = ByteBuffer.allocateDirect(1 * 3 * inputSize * inputSize * 4).order(ByteOrder.nativeOrder())
        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixelValue = inputBitmap.getPixel(x, y)
                inputBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
                inputBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
                inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
            }
        }

        // Define output format
        val outputBuffer = Array(1) { Array(14175) { FloatArray(25) } }

        // Run the inference
        tflite.run(inputBuffer, outputBuffer)

        // Logging output
        Log.d("dog", "Detection Output: ${outputBuffer.contentDeepToString()}")

        // List to store detected objects
        val detectedObjects = mutableListOf<String>()

        // Post-process the output to log detected objects
        for (i in outputBuffer[0].indices) {
            val confidence = outputBuffer[0][i][4]  // Confidence score
            Log.d("dog", "Confidence for detection $i: $confidence")
            if (confidence > 0.001) {  // Threshold for detecting objects
                val classScores = outputBuffer[0][i].slice(5 until outputBuffer[0][i].size)
                val classId = classScores.indexOf(classScores.maxOrNull()!!)
                val label = classLabels.getOrNull(classId) ?: "Unknown"
                val text = "$label: ${confidence * 100.0}%"
                detectedObjects.add(text)
                Log.d("dog", "Detected $label with score $confidence")
            }
        }

        textView.text = detectedObjects.joinToString("\n")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                dispatchTakePictureIntent()
            } else {
                // 권한이 거부된 경우 처리할 로직 추가
            }
        }
    }
}