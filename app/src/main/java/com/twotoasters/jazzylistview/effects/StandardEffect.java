package com.twotoasters.jazzylistview.effects;

import android.view.View;
import android.view.ViewPropertyAnimator;

import com.twotoasters.jazzylistview.JazzyEffect;

public class StandardEffect implements JazzyEffect {

    @Override
    public void initView(View item, int position, int scrollDirection) {
        // 효과 없음
    }

    @Override
    public void setupAnimation(View item, int position, int scrollDirection, ViewPropertyAnimator animator) {
        // 효과 없음
    }

}
