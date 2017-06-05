package com.apps.my.liener;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import java.lang.annotation.Retention;
import java.security.PublicKey;

import static com.apps.my.liener.Constant.BubbleSizeDelete;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by rahul on 3/12/16.
 */

public class BubbleHead implements SpringListener {
    private static final String TAG = BubbleHead.class.getSimpleName();

    private View view;
    private int height, width;
    private LayoutParams layoutParams;

    private BubbleListener bubbleListener;

    private static double TENSION = 800;
    private static double DAMPER = 20; //friction


    private SpringSystem mSpringSystem;
    private Spring mSpring;

    @Retention(SOURCE)
    @IntDef({HEAD_TYPE_DELETE, HEAD_TYPE_MAIN, HEAD_TYPE_TAB})
    public @interface HEAD_TYPE {
    }

    public static final int HEAD_TYPE_MAIN = 0;
    public static final int HEAD_TYPE_DELETE = 1;
    public static final int HEAD_TYPE_TAB = 2;

    private int defaultType;

    public BubbleHead(Context context, int height, int width, @HEAD_TYPE int head_type) {
        defaultType = head_type;
        this.width = width;
        this.height = height;
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = li.inflate(R.layout.browser_bubblehead, null);
        if (defaultType != HEAD_TYPE_DELETE) {
            setListener();
        }
        this.bubbleListener = null;
        setRebound();
    }

    private void setRebound(){

        mSpringSystem = SpringSystem.create();

        mSpring = mSpringSystem.createSpring();
        mSpring.addListener(this);

        SpringConfig config = new SpringConfig(TENSION, DAMPER);
        mSpring.setSpringConfig(config);
    }

    public void setProgressVisibility(int visibility) {
        // visibility can be View.VISIBLE or View.INVISIBLE
        view.findViewById(R.id.progressBar).setVisibility(visibility);
    }

    public void initParams(int x, int y) {

        layoutParams = new LayoutParams();
        layoutParams.x = x;
        layoutParams.y = y;
        if (defaultType == HEAD_TYPE_DELETE) {
            switchToDelete();
        }
    }
    public void initParams(int x, int y, int gravity) {
        layoutParams = new LayoutParams();
        layoutParams.x = x;
        layoutParams.y = y;
        if (defaultType == HEAD_TYPE_DELETE) {
            switchToDelete();
        }
        layoutParams.gravity = gravity;
    }

    public void switchToSmall() {
        layoutParams.width = Constant.BubbleSizeSmall;
        layoutParams.height = Constant.BubbleSizeSmall;
    }

    public void switchToLarge() {
        layoutParams.width = Constant.BubbleSizeLarge;
        layoutParams.height = Constant.BubbleSizeLarge;
    }

    public void switchToDelete() {
        ImageView delete = (ImageView) view.findViewById(R.id.headimage);
        delete.setImageResource(R.mipmap.delete);
        layoutParams.width = BubbleSizeDelete;
        layoutParams.height = BubbleSizeDelete;
        layoutParams.gravity = BubbleSizeDelete;
        view.setVisibility(View.INVISIBLE);
    }


//    BubbleListener fetchListener = null;
//
//    public void setListener(BubbleListener listener) {
//        this.fetchListener = listener;
//    }

//    public void sendTouchEvent(@BubbleListener.TOUCH_EVENT_TYPE int event_type) {
//        if (this.fetchListener != null)
//            this.fetchListener.onTouchEvent(event_type, defaultType);
//    }
//
//    public void sendClickEvent(@BubbleListener.CLICK_EVENT_TYPE int event_type) {
//        if (this.fetchListener != null)
//            this.fetchListener.onClickEvent(event_type, defaultType);
//    }


    private boolean onRightSide = true;
    private boolean isMoveEnabled = false;
    private boolean isOnDelete = false;

    public void setListener() {
        view.setOnTouchListener(new View.OnTouchListener() {
            int initialX, initialY;
            float initialTouchX, initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        mSpring.setEndValue(1f);
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        if (isMoveEnabled) {

                            if ((!onRightSide) && defaultType == HEAD_TYPE_MAIN) {
                                layoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            } else {
                                layoutParams.x = initialX - (int) (event.getRawX() - initialTouchX);
                            }
                            layoutParams.y = initialY - (int) (event.getRawY() - initialTouchY);
                            if(isOnDelete){
                                if(!onDeleteCheck()){
                                    isOnDelete = false;
//                                    sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_OFF_DELETE);
                                    if(bubbleListener!=null)
                                        bubbleListener.overDeleteArea(isOnDelete);
                                }
                            }
                            else {
//                                sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_UPDATE);
                                if(bubbleListener!=null)
                                    bubbleListener.updateView();
                                if (onDeleteCheck()) {
                                    Log.d(TAG, "in ondelete() called with: v = [" + v + "], event = [" + event + "]");
                                    isOnDelete = true;
//                                    sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_ON_DELETE);
                                    if(bubbleListener!=null)
                                        bubbleListener.overDeleteArea(isOnDelete);
                                }
                            }

                        } else {
                            Log.d(TAG, "onTouch() else called with: v = [" + v + "], event = [" + event + "]");
                            int x = (int) (event.getRawX() - initialTouchX);
                            int y = (int) (event.getRawY() - initialTouchY);
                            if ((x < -Constant.ENABLE_MOVE || x > Constant.ENABLE_MOVE) || (y < -Constant.ENABLE_MOVE || y > Constant.ENABLE_MOVE)){
                                Log.d(TAG, "onTouch() inside if called with: v = [" + v + "], event = [" + event + "]");
                                isMoveEnabled = true;
//                                sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_ADD_DELETE);
//                                if (defaultType == HEAD_TYPE_TAB)
//                                    sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_REMOVE_BROWSER);
                                if(bubbleListener!=null)
                                    bubbleListener.onMove(true);
                            }
                        }
                        return false;
                    case MotionEvent.ACTION_UP:


                        mSpring.setEndValue(0.3f);
                        if (isMoveEnabled) {
//                            sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_REMOVE_DELETE);
                            if(bubbleListener!=null && defaultType == HEAD_TYPE_MAIN)
                                bubbleListener.onMove(false);
                            if (onDeleteCheck()) {
//                                sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_DELETE);
                                if(bubbleListener!=null)
                                    bubbleListener.onDelete();
                                return true;
                            } else {
                                switch (defaultType) {
                                    case HEAD_TYPE_DELETE:
                                        break;
                                    case HEAD_TYPE_MAIN:
                                        moveBubbleToSide();
                                        break;
                                    case HEAD_TYPE_TAB:
                                        moveBubbleToOldPosition(initialX, initialY);
                                        if(bubbleListener!=null)
                                            bubbleListener.onMove(false);
//                                        sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_UPDATE);
//                                        sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_ADD_BROWSER);
                                        break;
                                }
//                                sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_UPDATE);
                                if(bubbleListener!=null)
                                    bubbleListener.updateView();
                                Log.d(TAG, "else in action_up");
                            }
                            isMoveEnabled = false;
                        }
                        return false;
                }
                return false;
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (defaultType) {
                    case HEAD_TYPE_DELETE:
                        break;
//                    case HEAD_TYPE_MAIN:
////                        sendClickEvent(BubbleListener.CLICK_EVENT_TYPE_EXPAND);
//                        if(bubbleListener!=null)
//                            bubbleListener.onClick();
//                        break;
//                    case HEAD_TYPE_TAB:
                    default:
//                        sendClickEvent(BubbleListener.CLICK_EVENT_TYPE_MINIMIZE);
                        if(bubbleListener!=null)
                            bubbleListener.onClick();
                        break;
                }
            }
        });
    }

    public boolean onDeleteCheck() {
        int y = (layoutParams.y - height / 4);
        int x = (layoutParams.x - width + BubbleSizeDelete / 2);
        Log.d(TAG, "onDeleteCheck() called " + " height: " + height + " widthmid: " + width + " x: " + x + " y: " + y);
        if ((x > -100 && x < 100) && (y > -100 && y < 100)) {
            Log.d("TESTING", "deleted");
            return true;
        } else return false;
    }

    public void moveBubbleToSide() {
        if (layoutParams.x > width) {
            if (onRightSide) {
                layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                onRightSide = false;
            } else {
                layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                onRightSide = true;
            }
        }
        layoutParams.x = 0;
    }

    public void moveBubbleToOldPosition(int initialX, int initialY) {
        layoutParams.x = initialX;
        layoutParams.y = initialY;
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        float value = (float) spring.getCurrentValue();
        float scale = 1f - (value * 0.5f);
        view.setScaleX(scale);
        view.setScaleY(scale);
    }


    @Override
    public void onSpringAtRest(Spring spring) {

    }

    @Override
    public void onSpringActivate(Spring spring) {

    }

    @Override
    public void onSpringEndStateChange(Spring spring) {

    }

    public int getDefaultType() {
        return defaultType;
    }

    public void setDefaultType(int defaultType) {
        this.defaultType = defaultType;
    }

    public LayoutParams getLayoutParams() {
        return layoutParams;
    }

    public void setLayoutParams(LayoutParams layoutParams) {
        this.layoutParams = layoutParams;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void setViewVisibility(int visibility){
        view.setVisibility(visibility);
    }

    public void setLayoutParamsSize(int size){
        layoutParams.setSize(size);
    }

    public int getLayoutParamsX(){
        return layoutParams.x;
    }

    public void setLayoutParamsX(int x){
        layoutParams.x=x;
    }

    public void setBubbleListener(BubbleListener bubbleListener){
        this.bubbleListener = bubbleListener;
    }
}
