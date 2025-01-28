package com.social.media.decondition.ui.theme

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

class SudokuCellDrawable(
    private val backgroundColor: Int,
    private val borderColor: Int,
    private val row: Int,
    private val col: Int
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val thinLineWidth = 1f   // normal grid lines
    private val thickLineWidth = 5f  // every 3rd line

    override fun draw(canvas: Canvas) {
        val rect = bounds

        // 1) Fill background
        paint.color = backgroundColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(rect, paint)

        // 2) Setup border paint
        paint.color = borderColor
        paint.style = Paint.Style.STROKE

        // We'll default all lines to 'thinLineWidth'
        var topWidth    = thinLineWidth
        var bottomWidth = thinLineWidth
        var leftWidth   = thinLineWidth
        var rightWidth  = thinLineWidth

        // 3) Handle top boundary for the very first row (bold)
        if (row == 0) {
            topWidth = thickLineWidth
        }
        // 4) Handle left boundary for the very first column (bold)
        if (col == 0) {
            leftWidth = thickLineWidth
        }
        // 5) For typical Sudoku, the “thick lines” appear *below* each 3rd row:
        //    That means at row == 2, row == 5, and row == 8.
        if (row == 2 || row == 5 || row == 8) {
            bottomWidth = thickLineWidth
        }
        // 6) Similarly, columns 2, 5, and 8 get a thick right boundary.
        if (col == 2 || col == 5 || col == 8) {
            rightWidth = thickLineWidth
        }

        // Top border
        if (topWidth > 0) {
            paint.strokeWidth = topWidth
            val y = rect.top + (topWidth / 2f)
            canvas.drawLine(rect.left.toFloat(), y, rect.right.toFloat(), y, paint)
        }
        // Bottom border
        if (bottomWidth > 0) {
            paint.strokeWidth = bottomWidth
            val y = rect.bottom - (bottomWidth / 2f)
            canvas.drawLine(rect.left.toFloat(), y, rect.right.toFloat(), y, paint)
        }
        // Left border
        if (leftWidth > 0) {
            paint.strokeWidth = leftWidth
            val x = rect.left + (leftWidth / 2f)
            canvas.drawLine(x, rect.top.toFloat(), x, rect.bottom.toFloat(), paint)
        }
        // Right border
        if (rightWidth > 0) {
            paint.strokeWidth = rightWidth
            val x = rect.right - (rightWidth / 2f)
            canvas.drawLine(x, rect.top.toFloat(), x, rect.bottom.toFloat(), paint)
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }
}