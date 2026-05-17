package com.amibar.boggle.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.max

/**
 * A custom TextView that always maintains a square aspect ratio.
 * It calculates the maximum of the measured width and height and applies it to both dimensions.
 * This is particularly useful for grid-based UIs like the Boggle board where each die should be square.
 */
class SquareTextView : AppCompatTextView {
    /**
     * Simple constructor to use when creating a view from code.
     * @param context The Context the view is running in.
     */
    constructor(context: Context) : super(context)

    /**
     * Constructor that is called when inflating a view from XML.
     * @param context The Context the view is running in.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    /**
     * Constructor that is called when inflating a view from XML and has a style attribute.
     * @param context The Context the view is running in.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource.
     */
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * Overridden to enforce square dimensions.
     * @param widthMeasureSpec Horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)


        // Get the measured dimensions calculated by the superclass
        val width = measuredWidth
        val height = measuredHeight

        // Determine the larger of the two dimensions to ensure it's a perfect square
        val dimen = max(width, height)

        // Apply the square dimensions
        setMeasuredDimension(dimen, dimen)
    }
}
