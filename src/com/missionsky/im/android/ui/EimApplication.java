package com.missionsky.im.android.ui;

import java.util.LinkedList;
import java.util.List;

import com.missionsky.im.android.manager.XmppConnectionManager;

import android.app.Activity;
import android.app.Application;

public class EimApplication extends Application {
	private List<Activity> activityList = new LinkedList<Activity>();

	// 添加Activity到容器中
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	// 遍历所有Activity并finish
	public void exit() {
		XmppConnectionManager.getInstance().disconnect();
		for (Activity activity : activityList) {
			activity.finish();
		}
	}
}
