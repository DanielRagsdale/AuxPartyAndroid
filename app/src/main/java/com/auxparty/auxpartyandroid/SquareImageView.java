package com.auxparty.auxpartyandroid;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * An ImageView that will always have height = width
 */
public class SquareImageView extends ImageView
{
    public SquareImageView(Context context)
    {
        super(context);
    }
    public SquareImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }
}
