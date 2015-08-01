package com.missionsky.im.android.service;

import java.io.File;
import java.util.Calendar;
import com.missionsky.im.android.R;


import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import com.missionsky.im.android.common.Constant;
import com.missionsky.im.android.manager.MessageManager;
import com.missionsky.im.android.manager.NoticeManager;
import com.missionsky.im.android.manager.XmppConnectionManager;
import com.missionsky.im.android.model.IMMessage;
import com.missionsky.im.android.model.Notice;
import com.missionsky.im.android.ui.ChatActivity;
import com.missionsky.im.android.util.DateUtil;


import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

/**
 * �������
 * 
 * 
 * 
 */
public class IMChatService extends Service {
	private Context context;
	private NotificationManager notificationManager;
	private FileTransferRequest request;
	private File file;

	@Override
	public void onCreate() {
		context = this;
		super.onCreate();
		initChatManager();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initChatManager() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		XMPPConnection conn = XmppConnectionManager.getInstance().getConnection();
		conn.addPacketListener(pListener, new MessageTypeFilter(Message.Type.chat));
		//�����ļ�����
		FileTransferManager fileTransferManager = new FileTransferManager(conn);
		fileTransferManager.addFileTransferListener(new RecFileTransferListener());

	}

	PacketListener pListener = new PacketListener() {
		@Override
		public void processPacket(Packet arg0) {
			Message message = (Message) arg0;
			if (message != null && message.getBody() != null
					&& !message.getBody().equals("null")) {
				IMMessage msg = new IMMessage();
				String time = DateUtil.date2Str(Calendar.getInstance(),
						Constant.MS_FORMART);
				msg.setTime(time);
				msg.setContent(message.getBody());
				if (Message.Type.error == message.getType()) {
					msg.setType(IMMessage.ERROR);
				} else {
					msg.setType(IMMessage.SUCCESS);
				}
				String from = message.getFrom().split("/")[0];
				msg.setFromSubJid(from);

				// ����֪ͨ
				NoticeManager noticeManager = NoticeManager.getInstance(context);
				Notice notice = new Notice();
				notice.setTitle("�Ự��Ϣ");
				notice.setNoticeType(Notice.CHAT_MSG);
				notice.setContent(message.getBody());
				notice.setFrom(from);
				notice.setStatus(Notice.UNREAD);
				notice.setNoticeTime(time);

				// ��ʷ��¼
				IMMessage newMessage = new IMMessage();
				newMessage.setMsgType(0);
				newMessage.setFromSubJid(from);
				newMessage.setContent(message.getBody());
				newMessage.setTime(time);
				MessageManager.getInstance(context).saveIMMessage(newMessage);
				long noticeId = -1;

				noticeId = noticeManager.saveNotice(notice);

				if (noticeId != -1) {
					Intent intent = new Intent(Constant.NEW_MESSAGE_ACTION);
					intent.putExtra(IMMessage.IMMESSAGE_KEY, msg);
					intent.putExtra("notice", notice);
					sendBroadcast(intent);
					setNotiType(R.drawable.im,getResources().getString(R.string.new_message),
							notice.getContent(), ChatActivity.class, from);
				}
			}
		}
	};
	
	private class RecFileTransferListener implements FileTransferListener {
		@Override
		public void fileTransferRequest(FileTransferRequest prequest) {
			//���ܸ���
			System.out.println("The file received from: " + prequest.getRequestor());
			
			file = new File("mnt/sdcard/Xim/" + prequest.getFileName());
			request = prequest;
			
			final IncomingFileTransfer infiletransfer = request.accept();
			try {
				infiletransfer.recieveFile(file);
			} catch (XMPPException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
			showToast("���յ�һ���ļ���\n������sdcard/Xim");
			
		}
	}
	
	//��service��ʾToast
	private void showToast(final String text) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				// TODO �Զ����ɵķ������
				Toast.makeText(context, text, Toast.LENGTH_LONG).show();
			}
		});
		
	}
	
	private void showDialog(){
		final IncomingFileTransfer infiletransfer = request.accept();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("�ļ�����")
		.setMessage("�Ƿ��������"+request.getRequestor()+"���ļ���")
		.setPositiveButton("����", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO �Զ����ɵķ������
				try {
					infiletransfer.recieveFile(file);
				} 
				catch (XMPPException e)	{
					showToast("����ʧ��!");
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		})
		.setNegativeButton("�ܾ�", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO �Զ����ɵķ������
				request.reject();
				dialog.cancel();
			}
		}).show();
	}
	
	
	
	/**
	 * 
	 * ����Notification��method.
	 * 
	 * @param iconId
	 *            ͼ��
	 * @param contentTitle
	 *            ����
	 * @param contentText
	 *            ������
	 * @param activity
	 * 
	 */

	@SuppressWarnings({ "rawtypes" })
	private void setNotiType(int iconId, String contentTitle,
			String contentText, Class activity, String from) {
		/*
		 * �����µ�Intent����Ϊ���Notification������ʱ�� �����е�Activity
		 */

		Intent notifyIntent = new Intent(this, activity);
		notifyIntent.putExtra("to", from);
		/* ����PendingIntent��Ϊ���õ������е�Activity */
		PendingIntent appIntent = PendingIntent.getActivity(this, 0,notifyIntent, 0);
		/* ����Notication����������ز��� */
		Notification myNoti = new Notification();
		// ����Զ���ʧ
		myNoti.flags = Notification.FLAG_AUTO_CANCEL;
		/* ����statusbar��ʾ��icon */
		myNoti.icon = iconId;
		/* ����statusbar��ʾ��������Ϣ */
		myNoti.tickerText = contentTitle;
		/* ����notification����ʱͬʱ����Ĭ������ */
		myNoti.defaults = Notification.DEFAULT_SOUND;
		/* ����Notification�������Ĳ��� */
		myNoti.setLatestEventInfo(this, contentTitle, contentText, appIntent);
		/* �ͳ�Notification */
		notificationManager.notify(0, myNoti);
	}
	
	
}
