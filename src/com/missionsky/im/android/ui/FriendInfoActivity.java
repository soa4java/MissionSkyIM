package com.missionsky.im.android.ui;

import com.missionsky.im.android.R;


import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

/**
 * 好友信息界面
 * 
 * 
 * 
 */
public class FriendInfoActivity extends ActivitySupport {
	private ImageView titleBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.friend_info);
		init();
	}

	private void init() {
		getEimApplication().addActivity(this);
		titleBack = (ImageView) findViewById(R.id.title_back);
		titleBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
