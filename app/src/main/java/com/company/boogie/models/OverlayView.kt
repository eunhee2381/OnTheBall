package com.company.boogie.models

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

/** 추론 결과를 바탕으로 바운딩 박스를 그리는 역할
 *  현재 바운딩박스가 안그려지고 있음...
 */
class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 8.0f
    }

    private val boxes = mutableListOf<Box>()

    fun setBoxes(boxes: List<Box>) {
        this.boxes.clear()
        this.boxes.addAll(boxes)
        invalidate()
//        Log.d("OverlayView", "setBoxes called with ${boxes.size} boxes")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (box in boxes) {
            canvas.drawRect(box.left, box.top, box.right, box.bottom, paint)
//            Log.d("OverlayView", "Drawing box: (${box.left}, ${box.top}, ${box.right}, ${box.bottom})")
        }
    }
}

data class Box(val left: Float, val top: Float, val right: Float, val bottom: Float)