package com.missionsky.im.android.ui;

import java.util.Timer;
import java.util.TimerTask;

import com.missionsky.im.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class WelcomeActivity extends Activity {
	int times = 0;
	private Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_welcome);
		startTimer();
	}

	private void openNextPage() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	private class SpinnerTask extends TimerTask {
		public void run() {
			times++;
			if (times >= 2) {
				timer.cancel();
				times = 0;
				// 2秒结束打开消息列表
				openNextPage();
			}
		}
	}

	public void startTimer() {
		if (timer == null) {
			timer = new Timer();
			timer.schedule(new SpinnerTask(), 100, 1000);
		}
	}
}
