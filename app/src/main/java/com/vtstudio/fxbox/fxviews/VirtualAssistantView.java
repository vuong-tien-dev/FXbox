package com.vtstudio.fxbox.fxviews;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.vtstudio.fxbox.R;

public class VirtualAssistantView extends LottieAnimationView {
    private Handler statusHandler;
    private Runnable  runnable;
    private static final int statusChangeTime = 10000;
    private boolean isBoring  = true;
    private boolean isSleep;
    private boolean canStatusShow;

    public VirtualAssistantView(Context context) {
        super(context);
        setAnimation(R.raw.chatbot_say_hi);
        setScaleX(1.5f);
        setScaleY(1.5f);
    }

    public VirtualAssistantView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAnimation(R.raw.chatbot_say_hi);
        setScaleX(1.5f);
        setScaleY(1.5f);
    }

    public VirtualAssistantView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAnimation(R.raw.chatbot_say_hi);
        setScaleX(1.5f);
        setScaleY(1.5f);
    }

    private void init (){

        if (statusHandler == null) {
            statusHandler = new Handler();
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                if(isBoring) {
                    if(isSleep) showWakeUpStatus();
                    else  showFallSleepStatus();
                }
                if(canStatusShow && statusHandler != null) statusHandler.postDelayed(runnable, statusChangeTime);
            }
        };
       statusHandler.postDelayed(runnable, statusChangeTime);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(statusHandler != null) {
            statusHandler.removeCallbacksAndMessages(null);
        }
        statusHandler = null;
        runnable = null;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    //    public void sayHi () {
//        setAnimation(R.raw.chatbot_say_hi);
//        setRepeatCount(0);
//        playAnimation();
//    }
//
//    public void sayHello () {
//        setAnimation(R.raw.chatbot_say_hello);
//        setRepeatCount(0);
//        playAnimation();
//    }

    public void showSuccessStatus () {
        isBoring = false;
        setAnimation(R.raw.chatbot_success);
        setRepeatCount(0);
        playAnimation();
    }

//    public void sayHowCanIHelpYou() {
//        setAnimation(R.raw.chatbot_say_how_can_i_help_you);
//        setRepeatCount(0);
//        playAnimation();
//    }

//    public void sayNo() {
//        setAnimation(R.raw.chatbot_say_no);
//        setRepeatCount(0);
//        playAnimation();
//    }
//
//    public void sayYes() {
//        setAnimation(R.raw.chatbot_say_yes);
//        setRepeatCount(0);
//        playAnimation();
//    }

    public void showFailedStatus() {
        isBoring = false;
        setAnimation(R.raw.chatbot_failed);
        setRepeatCount(0);
        playAnimation();
    }

    public void showFallSleepStatus() {
        isSleep = true;
        setAnimation(R.raw.chatbot_fall_sleep);
        setRepeatCount(0);
        playAnimation();
    }

    public void showWakeUpStatus() {
        isSleep = false;
        setAnimation(R.raw.chatbot_wake_up);
        setRepeatCount(0);
        playAnimation();
    }

    public void showErrolStatus() {
        isBoring = false;
        setAnimation(R.raw.chatbot_errol);
        setRepeatCount(0);
        playAnimation();
    }

    public void showLoadingStatus() {
        isBoring = false;
        setAnimation(R.raw.chatbot_loading);
        setRepeatCount(LottieDrawable.INFINITE);
        playAnimation();
    }

    public void showSearchingStatus() {
        isBoring = false;
        setAnimation(R.raw.chatbot_searching);
        setRepeatCount(LottieDrawable.INFINITE);
        playAnimation();
    }

//    public void sayThanks() {
//        setAnimation(R.raw.chatbot_say_thanks);
//        setRepeatCount(0);
//        playAnimation();
//    }

    public void autoMakeStatus (boolean auto){
        canStatusShow = auto;
    }
}
