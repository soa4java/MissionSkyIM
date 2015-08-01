package com.missionsky.im.android.manager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import android.content.Context;

public class UserManager {
	private static UserManager userManager = null;

	private UserManager() {

	}

	public static UserManager getInstance(Context context) {
		if (userManager == null) {
			userManager = new UserManager();
		}
		return userManager;
	}

	/**
	 * 根据jid获取用户VCard
	 * 
	 * @param jid
	 * @return
	 */
	public VCard getUserVCard(String jid) {
		XMPPConnection xmppConn = XmppConnectionManager.getInstance()
				.getConnection();
		VCard vcard = new VCard();
		try {
			vcard.load(xmppConn, jid);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return vcard;
	}

	/**
	 * 保存用户的VCard
	 * 
	 * @param vCard
	 * @return
	 */
	public VCard saveUserVCard(VCard vCard) {
		XMPPConnection xmppConn = XmppConnectionManager.getInstance()
				.getConnection();
		try {
			vCard.save(xmppConn);
			return getUserVCard(vCard.getJabberId());
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取用户头像，返回输入流
	 * 
	 * @param jid
	 * @return
	 */
	public InputStream getUserImage(String jid) {
		XMPPConnection connection = XmppConnectionManager.getInstance()
				.getConnection();
		InputStream ic = null;
		try {
			VCard vcard = new VCard();
			vcard.load(connection, jid);

			if (vcard == null || vcard.getAvatar() == null) {
				return null;
			}
			ByteArrayInputStream bais = new ByteArrayInputStream(
					vcard.getAvatar());
			return bais;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ic;
	}
}
