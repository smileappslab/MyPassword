/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.twotoasters.jazzylistview.effects;

import android.view.View;
import android.view.ViewPropertyAnimator;

import com.twotoasters.jazzylistview.JazzyEffect;

public class SlideInEffect implements JazzyEffect
{
	@Override
	public void initView(View item, int position, int scrollDirection)
	{
		item.setTranslationY(item.getHeight() / 2 * scrollDirection);
	}

	@Override
	public void setupAnimation(View item, int position, int scrollDirection,
			ViewPropertyAnimator animator)
	{
		animator.translationYBy(-item.getHeight() / 2 * scrollDirection);
	}
}
