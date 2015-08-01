package com.missionsky.im.android.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.missionsky.im.android.common.Constant;
import com.missionsky.im.android.manager.ContacterManager;
import com.missionsky.im.android.manager.XmppConnectionManager;
import com.missionsky.im.android.manager.ContacterManager.MRosterGroup;
import com.missionsky.im.android.model.Notice;
import com.missionsky.im.android.model.User;
import com.missionsky.im.android.util.StringUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public abstract class AContacterActivity extends ActivitySupport {

	private ContacterReceiver receiver = null;
	protected int noticeNum = 0;// ֪ͨ������δ����Ϣ����

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	private void init() {
		receiver = new ContacterReceiver();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();

		filter.addAction(Constant.ROSTER_ADDED);
		filter.addAction(Constant.ROSTER_DELETED);
		filter.addAction(Constant.ROSTER_PRESENCE_CHANGED);
		filter.addAction(Constant.ROSTER_UPDATED);
		filter.addAction(Constant.ROSTER_SUBSCRIPTION);
		// ��������
		filter.addAction(Constant.NEW_MESSAGE_ACTION);
		filter.addAction(Constant.ACTION_SYS_MSG);

		filter.addAction(Constant.ACTION_RECONNECT_STATE);
		registerReceiver(receiver, filter);
		super.onResume();
	}

	private class ContacterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			
			String action = intent.getAction();

			User user = intent.getParcelableExtra(User.userKey);
			Notice notice = (Notice) intent.getSerializableExtra("notice");

			if (Constant.ROSTER_ADDED.equals(action)) {
				addUserReceive(user);
			} else if (Constant.ROSTER_DELETED.equals(action)) {
				deleteUserReceive(user);
			} else if (Constant.ROSTER_PRESENCE_CHANGED.equals(action)) {
				changePresenceReceive(user);
			} else if (Constant.ROSTER_UPDATED.equals(action)) {
				updateUserReceive(user);
			} else if (Constant.ROSTER_SUBSCRIPTION.equals(action)) {
				subscripUserReceive(intent
						.getStringExtra(Constant.ROSTER_SUB_FROM));
			} else if (Constant.NEW_MESSAGE_ACTION.equals(action)) {
				msgReceive(notice);
			} else if (Constant.ACTION_RECONNECT_STATE.equals(action)) {
				boolean isSuccess = intent.getBooleanExtra(Constant.RECONNECT_STATE, true);
				handReConnect(isSuccess);
			}
		}
	}

	/**
	 * roster�����һ��subcriber
	 * 
	 * @param user
	 */
	protected abstract void addUserReceive(User user);

	/**
	 * rosterɾ����һ��subscriber
	 * 
	 * @param user
	 */
	protected abstract void deleteUserReceive(User user);

	/**
	 * roster�е�һ��subscriber��״̬��Ϣ��Ϣ�����˸ı�
	 * 
	 * @param user
	 */
	protected abstract void changePresenceReceive(User user);

	/**
	 * roster�е�һ��subscriber��Ϣ������
	 * 
	 * @param user
	 */
	protected abstract void updateUserReceive(User user);

	/**
	 * �յ�һ�������������
	 * 
	 * @param subFrom
	 */
	protected abstract void subscripUserReceive(String subFrom);

	/**
	 * ������Ϣ����
	 * 
	 * @param user
	 */
	protected abstract void msgReceive(Notice notice);

	/**
	 * �ظ�һ��presence��Ϣ���û�
	 * 
	 * @param type
	 * @param to
	 */
	protected void sendSubscribe(Presence.Type type, String to) {
		Presence presence = new Presence(type);
		presence.setTo(to);
		XmppConnectionManager.getInstance().getConnection()
				.sendPacket(presence);
	}

	/**
	 * �޸�������ѵ��ǳ�
	 * 
	 * @param user
	 * @param nickname
	 */
	protected void setNickname(User user, String nickname) {
		/*
		 * ContacterManager.addUserToGroup(user,user.getGroupName(),
		 * ConnectionUtils.getConnection());
		 */
		ContacterManager.setNickname(user, nickname, XmppConnectionManager
				.getInstance().getConnection());
	}

	/**
	 * ��һ��������ӵ�һ������ ���Ƴ���ǰ���飬Ȼ����ӵ��·���
	 * 
	 * @param user
	 * @param groupName
	 */
	protected void addUserToGroup(final User user, final String groupName) {

		if (null == user) {
			return;
		}
		if (StringUtil.notEmpty(groupName) && Constant.ALL_FRIEND != groupName
				&& Constant.NO_GROUP_FRIEND != groupName) {
			ContacterManager.addUserToGroup(user, groupName,
					XmppConnectionManager.getInstance().getConnection());
		}
	}

	/**
	 * ��һ�����Ѵ�����ɾ��
	 * 
	 * @param user
	 * @param groupName
	 */
	protected void removeUserFromGroup(User user, String groupName) {

		if (null == user) {
			return;
		}
		if (StringUtil.notEmpty(groupName)
				&& !Constant.ALL_FRIEND.equals(groupName)
				&& !Constant.NO_GROUP_FRIEND.equals(groupName))
			ContacterManager.removeUserFromGroup(user, groupName,
					XmppConnectionManager.getInstance().getConnection());

	}

	/**
	 * ���һ����ϵ��
	 * 
	 * @param userJid
	 *            ��ϵ��JID
	 * @param nickname
	 *            ��ϵ���ǳ�
	 * @param groups
	 *            ��ϵ����ӵ���Щ��
	 * @throws XMPPException
	 */
	protected void createSubscriber(String userJid, String nickname,
			String[] groups) throws XMPPException {
		XmppConnectionManager.getInstance().getConnection().getRoster()
				.createEntry(userJid, nickname, groups);
	}

	/**
	 * ɾ��һ����ϵ��
	 * 
	 * @param userJid
	 *            ��ϵ�˵�JID
	 * @throws XMPPException
	 */
	protected void removeSubscriber(String userJid) throws XMPPException {
		ContacterManager.deleteUser(userJid);

	}

	/**
	 * �޸�һ���������
	 * 
	 * @param groupName
	 */
	protected void updateGroupName(String oldGroupName, String newGroupName) {
		XmppConnectionManager.getInstance().getConnection().getRoster()
				.getGroup(oldGroupName).setName(newGroupName);
	}

	/**
	 * ��ӷ���
	 * 
	 * @param newGroupName
	 */
	protected void addGroup(String newGroupName) {
		ContacterManager.addGroup(newGroupName, XmppConnectionManager
				.getInstance().getConnection());
	}

	/**
	 * ����������
	 * 
	 * @param roomName
	 * @return
	 */
	protected static boolean createRoom(String roomName) {
		MultiUserChat muc = new MultiUserChat(XmppConnectionManager
				.getInstance().getConnection(), roomName
				+ "@conference.wangxc");
		try {
			muc.create("room_nickname");
			Form form = muc.getConfigurationForm();
			Form submitForm = form.createAnswerForm();
			for (Iterator<?> fields = form.getFields(); fields.hasNext();) {
				FormField field = (FormField) fields.next();
				if (!FormField.TYPE_HIDDEN.equals(field.getType())
						&& field.getVariable() != null) {
					submitForm.setDefaultAnswer(field.getVariable());
				}
			}
			submitForm.setAnswer("muc#roomconfig_persistentroom", true);
			submitForm.setAnswer("muc#roomconfig_membersonly", false);
			submitForm.setAnswer("muc#roomconfig_allowinvites", true);
			submitForm.setAnswer("muc#roomconfig_enablelogging", true);
			submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
			submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
			submitForm.setAnswer("x-muc#roomconfig_registration", false);
			/** ����������ӵ���� */
			List<String> owners = new ArrayList<String>();
			owners.add("admin@wangxc");
			submitForm.setAnswer("muc#roomconfig_roomowners", owners);
			muc.sendConfigurationForm(submitForm);
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * ����һ������
	 * 
	 * @param user
	 */
	protected void createChat(User user) {
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra("to", user.getJID());
		intent.putExtra("from", user.getFrom());
		startActivity(intent);
	}

	/**
	 * �����ӷ���
	 * 
	 * @param isSuccess
	 */
	protected abstract void handReConnect(boolean isSuccess);

	/**
	 * �ж��û����Ƿ����
	 * 
	 * @param userName
	 * @param groups
	 * @return
	 */
	protected boolean isExitJid(String userJid, List<MRosterGroup> groups) {
		for (MRosterGroup g : groups) {
			List<User> users = g.getUsers();
			if (users != null && users.size() > 0) {
				for (User u : users) {
					if (u.getJID().equals(userJid)) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
