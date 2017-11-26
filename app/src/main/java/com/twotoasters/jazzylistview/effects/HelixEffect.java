/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.twotoasters.jazzylistview.effects;

import android.view.View;
import android.view.ViewPropertyAnimator;

import com.twotoasters.jazzylistview.JazzyEffect;

public class HelixEffect implements JazzyEffect
{

	private static final int INITIAL_ROTATION_ANGLE = 180;

	@Override
	public void initView(View item, int position, int scrollDirection)
	{
		item.setRotationY(INITIAL_ROTATION_ANGLE);
	}

	@Override
	public void setupAnimation(View item, int position, int scrollDirection,
			ViewPropertyAnimator animator)
	{
		animator.rotationYBy(INITIAL_ROTATION_ANGLE * scrollDirection);
	}

}
