package com.missionsky.im.android.ui;

import com.missionsky.im.android.model.LoginConfig;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Activity����֧����ӿ�.
 * 
 * 
 */
public interface IActivitySupport {
	/**
	 * 
	 * ��ȡEimApplication.
	 * 
	 * 
	 */
	public abstract EimApplication getEimApplication();

	/**
	 * 
	 * ��ֹ����.
	 * 
	 * 
	 */
	public abstract void stopService();

	/**
	 * 
	 * ��������.
	 * 
	 * 
	 */
	public abstract void startService();

	/**
	 * 
	 * У������-���û������͵�������,������true.
	 * 
	 * 
	 */
	public abstract boolean validateInternet();

	/**
	 * 
	 * У������-���û������ͷ���true.
	 * 
	 * 
	 */
	public abstract boolean hasInternetConnected();

	/**
	 * 
	 * �˳�Ӧ��.
	 * 
	 * 
	 */
	public abstract void isExit();

	/**
	 * 
	 * �ж�GPS�Ƿ��Ѿ�����.
	 * 
	 * 
	 */
	public abstract boolean hasLocationGPS();

	/**
	 * 
	 * �жϻ�վ�Ƿ��Ѿ�����.
	 * 
	 *
	 */
	public abstract boolean hasLocationNetWork();

	/**
	 * 
	 * ����ڴ濨.
	 * 
	 * 
	 */
	public abstract void checkMemoryCard();

	/**
	 * 
	 * ��ʾtoast.
	 * 
	 * @param text
	 *            ����
	 * @param longint
	 *            ������ʾ�೤ʱ��
	 * 
	 */
	public abstract void showToast(String text, int longint);

	/**
	 * 
	 * ��ʱ����ʾtoast.
	 * 
	 * @param text
	 * 
	 */
	public abstract void showToast(String text);

	/**
	 * 
	 * ��ȡ������.
	 * 
	 * @return
	 * 
	 */
	public abstract ProgressDialog getProgressDialog();

	/**
	 * 
	 * ���ص�ǰActivity������.
	 * 
	 * @return
	 *
	 */
	public abstract Context getContext();

	/**
	 * 
	 * ��ȡ��ǰ��¼�û���SharedPreferences����.
	 * 
	 * @return
	 * 
	 */
	public SharedPreferences getLoginUserSharedPre();

	/**
	 * 
	 * �����û�����.
	 * 
	 * @param loginConfig
	 * 
	 */
	public void saveLoginConfig(LoginConfig loginConfig);

	/**
	 * 
	 * ��ȡ�û�����.
	 * 
	 * @param loginConfig
	 * 
	 */
	public LoginConfig getLoginConfig();

	/**
	 * 
	 * �û��Ƿ����ߣ���ǰ�����Ƿ������ɹ���
	 * 
	 * @param loginConfig
	 * 
	 */
	public boolean getUserOnlineState();

	/**
	 * �����û�����״̬ true ���� false ������
	 * 
	 * @param isOnline
	 */
	public void setUserOnlineState(boolean isOnline);

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
	@SuppressWarnings("rawtypes")
	public void setNotiType(int iconId, String contentTitle,
			String contentText, Class activity, String from);
}
