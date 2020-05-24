package com.malcolm.joules.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.malcolm.joules.R;

public class MultiViewPager extends ViewPager {
    private final Point size;
    private final Point maxSize;
    /**
     * Maximum size.
     */
    private int mMaxWidth = -1;
    /**
     * Maximum size.
     */
    private int mMaxHeight = -1;
    /**
     * Child view inside a page to match the page size against.
     */
    private int mMatchWidthChildResId;
    /**
     * Internal state to schedule a new measurement pass.
     */
    private boolean mNeedsMeasurePage;
    public MultiViewPager(@NonNull Context context) {
        super(context);
        size = new Point();
        maxSize = new Point();
    }

    public MultiViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        size = new Point();
        maxSize = new Point();
    }

    private static void constrainTo(Point size, Point maxSize) {
        if (maxSize.x >= 0) {
            if (size.x > maxSize.x) {
                size.x = maxSize.x;
            }
        }
        if (maxSize.y >= 0) {
            if (size.y > maxSize.y) {
                size.y = maxSize.y;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        size.set(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
        if(mMaxHeight >= 0 || mMaxWidth >= 0){
            maxSize.set(mMaxWidth, mMaxHeight);
            constrainTo(size,maxSize);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(size.x, MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(size.y, MeasureSpec.EXACTLY);

        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        onMeasurePage(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mNeedsMeasurePage = true;
    }

    protected void onMeasurePage(int widthMeasure, int heightMeasure){
        if (!mNeedsMeasurePage) return;
        if (mMatchWidthChildResId == 0) {
            mNeedsMeasurePage = false;
        } else if (getChildCount() > 0){
            View child = getChildAt(0);
            child.measure(widthMeasure, heightMeasure);
            int pageWidth = child.getMeasuredWidth();
            View match = child.findViewById(mMatchWidthChildResId);
            if (match == null) {
                throw new NullPointerException(
                        "MatchWithChildResId did not find that ID in the first fragment of the ViewPager; "
                                + "is that view defined in the child view's layout? Note that MultiViewPager "
                                + "only measures the child for index 0.");
            }
            int childWidth = match.getMeasuredWidth();
            // Check that the measurement was successful
            if (childWidth > 0) {
                mNeedsMeasurePage = false;
                int difference = pageWidth - childWidth;
                setPageMargin(-difference);
                int offscreen = (int) Math.ceil((float) pageWidth / (float) childWidth) + 1;
                setOffscreenPageLimit(offscreen);
                requestLayout();
            }
        }

    }

    private void init(Context context, AttributeSet attributeSet){
        setClipChildren(false);
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.MultiViewPager);
        setMaxWidth(typedArray.getDimensionPixelSize(R.styleable.MultiViewPager_android_maxWidth, -1));
        setMaxHeight(typedArray.getDimensionPixelSize(R.styleable.MultiViewPager_android_maxHeight, -1));
        setMatchChildWidth(typedArray.getResourceId(R.styleable.MultiViewPager_matchChildWidth, 0));
        typedArray.recycle();
    }

    /**
     * Sets the child view inside a page to match the page size against.
     *
     * @param matchChildWidthResId the child id
     */
    public void setMatchChildWidth(int matchChildWidthResId) {
        if (mMatchWidthChildResId != matchChildWidthResId) {
            mMatchWidthChildResId = matchChildWidthResId;
            mNeedsMeasurePage = true;
        }
    }

    /**
     * Sets the maximum size.
     *
     * @param width in pixels
     */
    public void setMaxWidth(int width) {
        mMaxWidth = width;
    }

    /**
     * Sets the maximum size.
     *
     * @param height in pixels
     */
    public void setMaxHeight(int height) {
        mMaxHeight = height;
    }


}
