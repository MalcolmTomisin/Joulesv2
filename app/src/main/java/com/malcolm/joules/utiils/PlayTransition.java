package com.malcolm.joules.utiils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;

import com.malcolm.joules.R;

public class PlayTransition extends Transition {

    private static final String PROPERTY_BOUNDS = "circleTransition:bounds";
    private static final String PROPERTY_POSITION = "circleTransition:position";
    private static final String PROPERTY_IMAGE = "circleTransition:image";
    private static final String[] TRANSITION_PROPERTIES = {
            PROPERTY_BOUNDS,
            PROPERTY_POSITION,
    };

    private int mColor = Color.parseColor("#6c1622");

    public PlayTransition() {
    }
    public PlayTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PlayTransition);
        setColor(a.getColor(R.styleable.PlayTransition_colorCT, getColor()));
        a.recycle();
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    @Override
    public String[] getTransitionProperties() {
        return TRANSITION_PROPERTIES;
    }

    private void captureValues(TransitionValues transitionValues) {
        final View view = transitionValues.view;
        transitionValues.values.put(PROPERTY_BOUNDS, new Rect(
                view.getLeft(), view.getTop(), view.getRight(), view.getBottom()
        ));
        int[] position = new int[2];
        transitionValues.view.getLocationInWindow(position);
        transitionValues.values.put(PROPERTY_POSITION, position);
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        final View view = transitionValues.view;
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {

    }
}
