package com.missionsky.im.android.ui;

import com.missionsky.im.android.R;

import com.missionsky.im.android.manager.XmppConnectionManager;
import com.missionsky.im.android.model.LoginConfig;
import com.missionsky.im.android.task.LoginTask;
import com.missionsky.im.android.util.StringUtil;
import com.missionsky.im.android.util.ValidateUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class LoginActivity extends ActivitySupport {
	private EditText edt_username, edt_pwd;
	private CheckBox rememberCb, autologinCb, novisibleCb;
	private Button btn_login;
	private Button btn_register;
	private LoginConfig loginConfig;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 校验SD卡
		checkMemoryCard();
		// 检测网络和版本
		validateInternet();
		// 初始化xmpp配置
		XmppConnectionManager.getInstance().init(loginConfig);
	}

	protected void init() {
		loginConfig = getLoginConfig();
		// 如果为自动登录
		if (loginConfig.isAutoLogin()) {
			LoginTask loginTask = new LoginTask(LoginActivity.this, loginConfig);
			loginTask.execute();
		}

		edt_username = (EditText) findViewById(R.id.ui_username_input);
		edt_pwd = (EditText) findViewById(R.id.ui_password_input);
		rememberCb = (CheckBox) findViewById(R.id.remember);
		autologinCb = (CheckBox) findViewById(R.id.autologin);
		novisibleCb = (CheckBox) findViewById(R.id.novisible);
		btn_register=(Button) findViewById(R.id.ui_register_btn);
		btn_login = (Button) findViewById(R.id.ui_login_btn);

		// 初始化各组件的默认状态
		edt_username.setText(loginConfig.getUsername());
		edt_pwd.setText(loginConfig.getPassword());
		rememberCb.setChecked(loginConfig.isRemember());
		autologinCb.setChecked(loginConfig.isAutoLogin());
		novisibleCb.setChecked(loginConfig.isNovisible());

		rememberCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (!isChecked)
					autologinCb.setChecked(false);
			}
		});
		//注册按钮
		btn_register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				Intent intent = new Intent();
				intent.setClass(context, RegisterActivity.class);
				//intent.putExtra("192.168.107.1",Host);
				startActivity(intent);
			}
		});
		//登录按钮
		btn_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkData() && validateInternet()) {
					String username = edt_username.getText().toString();
					String password = edt_pwd.getText().toString();

					// 先记录下各组件的目前状态,登录成功后才保存
					loginConfig.setPassword(password);
					loginConfig.setUsername(username);
					loginConfig.setRemember(rememberCb.isChecked());
					loginConfig.setAutoLogin(autologinCb.isChecked());
					loginConfig.setNovisible(novisibleCb.isChecked());

					LoginTask loginTask = new LoginTask(LoginActivity.this,
							loginConfig);
					loginTask.execute();
				}
			}
		});
	}

	private boolean checkData() {
		boolean checked = false;
		checked = (!ValidateUtil.isEmpty(edt_username, "登录名") && !ValidateUtil
				.isEmpty(edt_pwd, "密码"));
		return checked;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.login_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final EditText xmppHostText = new EditText(context);
		xmppHostText.setText(loginConfig.getXmppHost());
		switch (item.getItemId()) {
		case R.id.menu_login_set:
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setTitle("服务器设置")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(xmppHostText)
					.setMessage("请设置服务器IP地址")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which) {
									String xmppHost = StringUtil.doEmpty(xmppHostText.getText()	.toString());
									loginConfig.setXmppHost(xmppHost);
									XmppConnectionManager.getInstance().init(loginConfig);
									LoginActivity.this.saveLoginConfig(loginConfig);
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							}).create().show();

			break;
		
		case R.id.menu_exit:
			isExit();
			break;
		}
		return true;

	}

	@Override
	public void onBackPressed() {
		isExit();
	}
}
