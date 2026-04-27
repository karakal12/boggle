package com.amibar.boggle.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A custom TextView that always maintains a square aspect ratio.
 * It calculates the maximum of the measured width and height and applies it to both dimensions.
 * This is particularly useful for grid-based UIs like the Boggle board where each die should be square.
 */
public class SquareTextView extends androidx.appcompat.widget.AppCompatTextView {
    
    /**
     * Simple constructor to use when creating a view from code.
     * @param context The Context the view is running in.
     */
    public SquareTextView(@NonNull Context context) {
        super(context);
    }

    /**
     * Constructor that is called when inflating a view from XML.
     * @param context The Context the view is running in.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public SquareTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor that is called when inflating a view from XML and has a style attribute.
     * @param context The Context the view is running in.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource.
     */
    public SquareTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Overridden to enforce square dimensions.
     * @param widthMeasureSpec Horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        // Get the measured dimensions calculated by the superclass
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        // Determine the larger of the two dimensions to ensure it's a perfect square
        int dimen = Math.max(width, height);

        // Apply the square dimensions
        setMeasuredDimension(dimen, dimen);
    }
}
