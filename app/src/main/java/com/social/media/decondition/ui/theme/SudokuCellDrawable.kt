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
    private val col: Int,
    private val borderWidth: Int
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(canvas: Canvas) {
        val rect = bounds

        // Draw background
        paint.color = backgroundColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(rect, paint)

        // Determine border widths
        val lineBorderWidth = borderWidth / 2
        val thinBorderWidth = borderWidth
        val thickBorderWidth = borderWidth * 2

        val isTopBorderBoxBoundary = row % 3 == 0 && row != 0
        val isBottomBorderBoxBoundary = row % 3 == 2 && row != 8
        val isLeftBorderBoxBoundary = col % 3 == 0 && col != 0
        val isRightBorderBoxBoundary = col % 3 == 2 && col != 8

        val isTopBorderSudokuBoundary = row == 0
        val isBottomBorderSudokuBoundary = row == 8
        val isLeftBorderSudokuBoundary = col == 0
        val isRightBorderSudokuBoundary = col == 8

        var topBorderWidth = lineBorderWidth
        var bottomBorderWidth = lineBorderWidth
        var leftBorderWidth = lineBorderWidth
        var rightBorderWidth = lineBorderWidth

        if (isTopBorderBoxBoundary) topBorderWidth = thinBorderWidth
        if (isTopBorderSudokuBoundary) topBorderWidth = thickBorderWidth

        if (isBottomBorderBoxBoundary) bottomBorderWidth = thinBorderWidth
        if (isBottomBorderSudokuBoundary) bottomBorderWidth = thickBorderWidth

        if (isLeftBorderBoxBoundary) leftBorderWidth = thinBorderWidth
        if (isLeftBorderSudokuBoundary) leftBorderWidth = thickBorderWidth

        if (isRightBorderBoxBoundary) rightBorderWidth = thinBorderWidth
        if (isRightBorderSudokuBoundary) rightBorderWidth = thickBorderWidth

        paint.color = borderColor
        paint.style = Paint.Style.STROKE

        // Draw borders using calculated widths
        // Top border
        if (topBorderWidth > 0) {
            paint.strokeWidth = topBorderWidth.toFloat()
            val y = rect.top + topBorderWidth / 2f
            canvas.drawLine(rect.left.toFloat(), y, rect.right.toFloat(), y, paint)
        }

        // Bottom border
        if (bottomBorderWidth > 0) {
            paint.strokeWidth = bottomBorderWidth.toFloat()
            val y = rect.bottom - bottomBorderWidth / 2f
            canvas.drawLine(rect.left.toFloat(), y, rect.right.toFloat(), y, paint)
        }

        // Left border
        if (leftBorderWidth > 0) {
            paint.strokeWidth = leftBorderWidth.toFloat()
            val x = rect.left + leftBorderWidth / 2f
            canvas.drawLine(x, rect.top.toFloat(), x, rect.bottom.toFloat(), paint)
        }

        // Right border
        if (rightBorderWidth > 0) {
            paint.strokeWidth = rightBorderWidth.toFloat()
            val x = rect.right - rightBorderWidth / 2f
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