package com.company.boogie.ui

import android.Manifest
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
import com.company.boogie.R
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class Manager_CameraActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var bitmap: Bitmap
    private lateinit var tflite: Interpreter

    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_IMAGE_CAPTURE = 2
    private val REQUEST_CAMERA_PERMISSION = 3
    private val INPUT_SIZE = 480
    private val NUM_CLASSES = 20  // 클래스 수 (필요에 맞게 조정)

    private val classLabels = listOf(
        "Raspberry-Pi-1-Model-A-", "Raspberry-Pi-1-Model-B-", "Raspberry-Pi-2-Model-B",
        "Raspberry-Pi-3-Model-A-", "Raspberry-Pi-3-Model-B", "Raspberry-Pi-3-Model-B-",
        "Raspberry-Pi-4-Model-B", "Raspberry-Pi-5", "Raspberry-Pi-Zero-2-W",
        "Raspberry-Pi-Zero-WH", "Raspberry-Pi-Zero", "Raspberry-Pi-Zero-W",
        "Arduino-Nano", "Arduino-Pro-Mini", "Arduino-Mega", "Arduino-Zero",
        "Arduino-Uno", "Arduino-leonardo", "Arduino-Due", "Arduino-Micro"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_camera)

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)
        val button3: Button = findViewById(R.id.button3)

        // TFLite 모델 로드
        try {
            tflite = Interpreter(loadModelFile())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 카메라 버튼 클릭 시
        button1.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA_PERMISSION)
            } else {
                dispatchTakePictureIntent()
            }
        }

        // 객체 탐지 버튼 클릭 시
        button2.setOnClickListener {
            detectObjects()
        }

        // 이미지 선택 버튼 클릭 시
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

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("yolov5.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun detectObjects() {
        val inputBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val inputBuffer = ByteBuffer.allocateDirect(1 * 3 * INPUT_SIZE * INPUT_SIZE * 4).order(ByteOrder.nativeOrder())
        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val pixelValue = inputBitmap.getPixel(x, y)
                inputBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
                inputBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
                inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
            }
        }

        val outputBuffer = Array(1) { Array(25200) { FloatArray(6) } }

        tflite.run(inputBuffer, outputBuffer)

        Log.d("Detection", "Detection Output: ${outputBuffer.contentDeepToString()}")

        val detectedObjects = mutableListOf<String>()
        for (i in outputBuffer[0].indices) {
            val confidence = outputBuffer[0][i][4]
            if (confidence > 0.25) {  // 신뢰도 임계값 설정
                val classId = outputBuffer[0][i][5].toInt()  // 클래스 ID 바로 추출
                val label = classLabels.getOrNull(classId) ?: "Unknown"
                val text = "$label: ${confidence * 100.0}%"
                detectedObjects.add(text)
                Log.d("Detection", "Detected $label with score $confidence")
            }
        }

        textView.text = detectedObjects.joinToString("\n")
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