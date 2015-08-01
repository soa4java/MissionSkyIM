package com.missionsky.im.android.ui;

import com.missionsky.im.android.R;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;

import com.missionsky.im.android.common.Constant;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * ע�����
 * 
 * 
 */
public class RegisterActivity extends Activity {
	private Button register;
	private EditText password, username;
	private IQ result;
	SharedPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		register = (Button) findViewById(R.id.register);
		username = (EditText) findViewById(R.id.register_username);
		password = (EditText) findViewById(R.id.register_password);
		register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);
			}
		});
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			
			switch (msg.what) {
			case 1: {
				//ȡ��SharedPreferences����
				preferences = getSharedPreferences(Constant.LOGIN_SET, Context.MODE_PRIVATE);
				//��Xim_login_set�ļ���ȡ�ö�Ӧ�ļ���ֵ
				String host= preferences.getString("xmpp_host", null);
				System.out.println("ȡ�÷�������ַ�ǣ�"+host);
				
				ConnectionConfiguration config = new ConnectionConfiguration(host, 5222);
				config.setCompressionEnabled(true);
				config.setSASLAuthenticationEnabled(false);
				Connection connection = new XMPPConnection(config);
				

				// Connect to the server
				try {
					System.out.println("ֻ�����е���");
					connection.connect();
					System.out.println("666666666666666666666");

				} catch (XMPPException e) {
					// TODO �Զ����ɵ� catch ��
					Log.i("ERROR", "XMPP����ʧ��!", e);
					e.printStackTrace();
				}
				String domain = connection.getServiceName();
				System.out.println("ע������������ǣ�"+domain);
								
				Registration reg = new Registration();
				reg.setType(IQ.Type.SET);
				reg.setTo(connection.getServiceName());
				reg.setUsername(username.getText().toString());
				reg.setPassword(password.getText().toString());
				reg.addAttribute("android", "geolo_createUser_android");
				System.out.println("reg:" + reg);
				PacketFilter filter = new AndFilter(new PacketIDFilter(reg
						.getPacketID()), new PacketTypeFilter(IQ.class));
				PacketCollector collector = connection.createPacketCollector(filter);
				System.out.println("gggggg.........................");
				connection.sendPacket(reg);
				System.out.println("gggggg.........................");

				result = (IQ) collector.nextResult(SmackConfiguration
						.getPacketReplyTimeout());
				// Stop queuing results
				collector.cancel();// ֹͣ����results���Ƿ�ɹ��Ľ����

				if (result == null) {
					Toast.makeText(getApplicationContext(), "������û�з��ؽ��",
							Toast.LENGTH_SHORT).show();
				} else if (result.getType() == IQ.Type.ERROR) {
					if (result.getError().toString().equalsIgnoreCase(
							"conflict(409)")) {
						Toast.makeText(getApplicationContext(), "����˺��Ѿ�����",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "ע��ʧ��",
								Toast.LENGTH_SHORT).show();
					}
				} else if (result.getType() == IQ.Type.RESULT) {
					Toast.makeText(getApplicationContext(), "��ϲ��ע��ɹ�",
							Toast.LENGTH_SHORT).show();
				}
			}
				break;
			default:
				System.out.println("default.........................");
				break;
			}
		}
	};

	

}