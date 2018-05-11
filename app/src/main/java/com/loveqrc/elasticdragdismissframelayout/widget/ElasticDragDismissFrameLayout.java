package com.loveqrc.elasticdragdismissframelayout.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.loveqrc.elasticdragdismissframelayout.AnimUtils;
import com.loveqrc.elasticdragdismissframelayout.R;

import java.util.ArrayList;
import java.util.List;

public class ElasticDragDismissFrameLayout extends FrameLayout {

    private float dragDismissDistance = Float.MAX_VALUE;
    private float dragDismissFraction = -1f;
    private float dragDismissScale = 1f;
    private boolean shouldScale = false;
    private float dragElacticity = 0.8f;


    private float totalDrag;
    private boolean draggingDown = false;
    private boolean draggingUp = false;
    private int mLastActionEvent;


    private List<ElasticDragDismissCallback> callbacks;

    public ElasticDragDismissFrameLayout(@NonNull Context context) {
        this(context, null, 0, 0);
    }

    public ElasticDragDismissFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public ElasticDragDismissFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ElasticDragDismissFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ElasticDragDismissFrameLayout, 0, 0);

        if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissDistance)) {
            dragDismissDistance = a.getDimensionPixelSize(R.styleable
                    .ElasticDragDismissFrameLayout_dragDismissDistance, 0);
        } else if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissFraction)) {
            dragDismissFraction = a.getFloat(R.styleable
                    .ElasticDragDismissFrameLayout_dragDismissFraction, dragDismissFraction);
        }
        if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissScale)) {
            dragDismissScale = a.getFloat(R.styleable
                    .ElasticDragDismissFrameLayout_dragDismissScale, dragDismissScale);
            shouldScale = dragDismissScale != 1f;
        }
        if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragElasticity)) {
            dragElacticity = a.getFloat(R.styleable.ElasticDragDismissFrameLayout_dragElasticity,
                    dragElacticity);
        }
        a.recycle();

    }

    public  abstract static class ElasticDragDismissCallback {
        void onDrag(float elasticOffset, float elasticOffsetPixels,
                    float rawOffset, float rawOffsetPixels) {
        }

        public abstract void onDragDismissed();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        //只监听垂直方向的nestedScroll
        return (nestedScrollAxes & View.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        //nestedScroll前判断是否消费滚动的距离
        Log.d("ElasticDragDismissFrame", "dy:" + dy);
        if (draggingDown && dy > 0 || draggingUp && dy < 0) {
            dragScale(dy);
            consumed[1] = dy;
        }
    }


    /**
     * @param target       发起滚动的view
     * @param dxConsumed   x方向已经移动的距离
     * @param dyConsumed   y方向已经移动的距离
     * @param dxUnconsumed x方向没移动的距离
     * @param dyUnconsumed y方向没移动的距离
     */
    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        //正在嵌套滚动
        //当滚动到底部(或顶部)不能再滑动的时候，需要做拖拽处理
        Log.d("ElasticDragDismissFrame", "dyUnconsumed:" + dyUnconsumed);
        dragScale(dyUnconsumed);
    }

    /**
     * 拖拽缩放
     */
    private void dragScale(int scroll) {
        //只处理到达底部(或顶部)后再继续拖动的情况
        if (scroll == 0) {
            return;
        }
        //统计移动的距离
        totalDrag += scroll;

        if (scroll < 0 && !draggingUp && !draggingDown) {
            draggingDown = true;
            if (shouldScale) {
                setPivotY(0);
            }
        } else if (scroll > 0 && !draggingUp && !draggingDown) {
            draggingUp = true;
            if (shouldScale) {
                setPivotY(getHeight());
            }
        }
        float dragFraction = (float) Math.log10(1 + Math.abs(totalDrag) / dragDismissDistance);
        float dragTo = dragFraction * dragDismissDistance * dragElacticity;

        if (draggingUp) {
            dragTo *= -1;
        }

        setTranslationY(dragTo);

        if (shouldScale) {
            final float scale = 1 - ((1 - dragDismissScale) * dragFraction);
            setScaleX(scale);
            setScaleY(scale);
        }


        if ((draggingDown && totalDrag >= 0)
                || (draggingUp && totalDrag <= 0)) {
            totalDrag = dragTo = dragFraction = 0;
            draggingDown = draggingUp = false;
            setTranslationY(0f);
            setScaleX(1f);
            setScaleY(1f);
        }
        dispatchDragCallback(dragFraction, dragTo,
                Math.min(1f, Math.abs(totalDrag) / dragDismissDistance), totalDrag);

    }

    private void dispatchDragCallback(float elasticOffset, float elasticOffsetPixels,
                                      float rawOffset, float rawOffsetPixels) {
        if (callbacks != null && !callbacks.isEmpty()) {
            for (ElasticDragDismissCallback callback : callbacks) {
                callback.onDrag(elasticOffset, elasticOffsetPixels,
                        rawOffset, rawOffsetPixels);
            }
        }
    }

    public void addListener(ElasticDragDismissCallback listener) {
        if (callbacks == null) {
            callbacks = new ArrayList<>();
        }
        callbacks.add(listener);
    }

    public void removeListener(ElasticDragDismissCallback listener) {
        if (callbacks != null && callbacks.size() > 0) {
            callbacks.remove(listener);
        }
    }
    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
        mLastActionEvent = ev.getAction();
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (dragDismissFraction > 0f) {
            dragDismissDistance = h * dragDismissFraction;
        }
    }

    @Override
    public void onStopNestedScroll(View child) {
        //停止嵌套滚动
        if (Math.abs(totalDrag) >= dragDismissDistance) {
            dispatchDismissCallback();
        } else { // settle back to natural position
            if (mLastActionEvent == MotionEvent.ACTION_DOWN) {
                setTranslationY(0f);
                setScaleX(1f);
                setScaleY(1f);
            } else {
                animate()
                        .translationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200L)
                        .setInterpolator(AnimUtils.getFastOutSlowInInterpolator(getContext()))
                        .setListener(null)
                        .start();
            }
            totalDrag = 0;
            draggingDown = draggingUp = false;
            dispatchDragCallback(0f, 0f, 0f, 0f);
        }
    }

    private void dispatchDismissCallback() {
        if (callbacks != null && !callbacks.isEmpty()) {
            for (ElasticDragDismissCallback callback : callbacks) {
                callback.onDragDismissed();
            }
        }
    }


}
