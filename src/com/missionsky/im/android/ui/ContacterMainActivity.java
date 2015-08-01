package com.missionsky.im.android.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.missionsky.im.android.R;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverItems;

import com.missionsky.im.android.common.Constant;
import com.missionsky.im.android.manager.ContacterManager;
import com.missionsky.im.android.manager.MessageManager;
import com.missionsky.im.android.manager.NoticeManager;
import com.missionsky.im.android.manager.XmppConnectionManager;
import com.missionsky.im.android.manager.ContacterManager.MRosterGroup;
import com.missionsky.im.android.model.ChartHisBean;
import com.missionsky.im.android.model.Notice;
import com.missionsky.im.android.model.User;
import com.missionsky.im.android.ui.adapter.ContacterExpandAdapter;
import com.missionsky.im.android.ui.adapter.RecentChartAdapter;
import com.missionsky.im.android.util.StringUtil;
import com.missionsky.im.android.view.LayoutChangeListener;
import com.missionsky.im.android.view.ScrollLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 聊天主界面
 * 
 * 
 */
public class ContacterMainActivity extends AContacterActivity implements
		LayoutChangeListener, OnClickListener {
	private final static String TAG = "ContacterMainActivity";
	private LayoutInflater inflater;
	private ScrollLayout layout;
	private ImageView imageView;
	private ImageView tab1;
	private ImageView tab2;
	private ImageView tab3;
	private ListView chatRoomListView = null;
	private ExpandableListView contacterList = null;
	private ListView inviteList = null;
	private ContacterExpandAdapter expandAdapter = null;
	private RecentChartAdapter noticeAdapter = null;
	private ArrayAdapter<String> chatRoomAdapter = null;
	private List<ChartHisBean> inviteNotices = new ArrayList<ChartHisBean>();
	private List<DiscoverItems.Item> chatRoomList = new ArrayList<DiscoverItems.Item>();
	private List<String> groupNames;
	private List<String> newNames = new ArrayList<String>();
	private List<String> roomNames = new ArrayList<String>();
	private List<MRosterGroup> rGroups;
	private ImageView headIcon;
	private TextView noticePaopao;
	private ImageView iv_status;
	private User clickUser;
	private TextView textViewCreateRoom;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacter_main);
		init();
	}

	private void init() {
		initData();

		// 初始化UIheader
		iv_status = (ImageView) findViewById(R.id.imageView1);
		noticePaopao = (TextView) findViewById(R.id.notice_paopao);
		imageView = (ImageView) findViewById(R.id.top_bar_select);

		headIcon = (ImageView) findViewById(R.id.head_icon);
		headIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, UserInfoActivity.class);
				startActivity(intent);
			}
		});

		

		// 初始化UIbody
		tab1 = (ImageView) findViewById(R.id.tab1);
		tab2 = (ImageView) findViewById(R.id.tab2);
		tab3 = (ImageView) findViewById(R.id.tab3);

		getEimApplication().addActivity(this);
		inflater = LayoutInflater.from(context);
		layout = (ScrollLayout) findViewById(R.id.scrolllayout);
		layout.addChangeListener(this);
		View contacterTab1 = inflater.inflate(R.layout.contacter_tab1, null);
		View contacterTab2 = inflater.inflate(R.layout.contacter_tab2, null);
		View contacterTab3 = inflater.inflate(R.layout.contacter_tab3, null);
		layout.addView(contacterTab1);
		layout.addView(contacterTab2);
		layout.addView(contacterTab3);
		layout.setToScreen(1);

		// 我的好友
		contacterList = (ExpandableListView) findViewById(R.id.main_expand_list);
		expandAdapter = new ContacterExpandAdapter(context, rGroups);
		contacterList.setAdapter(expandAdapter);
		contacterList.setOnCreateContextMenuListener(onCreateContextMenuListener);
		contacterList.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				createChat((User) v.findViewById(R.id.username).getTag());
				return false;
			}
		});

		// 最近联系人
		inviteList = (ListView) findViewById(R.id.main_invite_list);
		inviteNotices = MessageManager.getInstance(context)
				.getRecentContactsWithLastMsg();
		noticeAdapter = new RecentChartAdapter(context, inviteNotices);
		inviteList.setAdapter(noticeAdapter);
		noticeAdapter.setOnClickListener(contacterOnClickJ);

		// 聊天室列表
		chatRoomListView = (ListView) findViewById(R.id.main_chat_room_list);
		chatRoomAdapter = new ArrayAdapter<String>(context,
				R.layout.chat_room_listview_item, roomNames);
		chatRoomListView.setAdapter(chatRoomAdapter);
		chatRoomListView.setOnCreateContextMenuListener(onCreateContextMenuListener);
		chatRoomListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
			}
		});
		textViewCreateRoom = (TextView) findViewById(R.id.textView_create_room);
		textViewCreateRoom.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				addChatRoom();
			}
		});

	}

	private void initData() {
		try {
			/** 获取好友列表 */
			groupNames = ContacterManager.getGroupNames(XmppConnectionManager
					.getInstance().getConnection().getRoster());
			rGroups = ContacterManager.getGroups(XmppConnectionManager
					.getInstance().getConnection().getRoster());

			/** 获取聊天室列表 */
			Iterator<?> it = ServiceDiscoveryManager
					.getInstanceFor(
							XmppConnectionManager.getInstance().getConnection())
					.discoverItems("conference.wangxc").getItems();
			while (it.hasNext()) {
				DiscoverItems.Item item = (DiscoverItems.Item) it.next();
				chatRoomList.add(item);
				roomNames.add(item.getName());
			}
		} catch (Exception e) {
			groupNames = new ArrayList<String>();
			rGroups = new ArrayList<MRosterGroup>();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshList();
		if (getUserOnlineState()) {
			iv_status.setImageDrawable(getResources().getDrawable(
					R.drawable.status_online));
		} else {
			iv_status.setImageDrawable(getResources().getDrawable(
					R.drawable.status_offline));
		}
	}

	/**
	 * 有新消息进来，最近联系人界面更新
	 */
	@Override
	protected void msgReceive(Notice notice) {
		for (ChartHisBean ch : inviteNotices) {
			if (ch.getFrom().equals(notice.getFrom())) {
				ch.setContent(notice.getContent());
				ch.setNoticeTime(notice.getNoticeTime());
				Integer x = ch.getNoticeSum() == null ? 0 : ch.getNoticeSum();
				ch.setNoticeSum(x + 1);
			}
		}
		noticeAdapter.setNoticeList(inviteNotices);
		noticeAdapter.notifyDataSetChanged();
		setPaoPao();

	}

	/**
	 * 通知点击
	 */
	private OnClickListener contacterOnClickJ = new OnClickListener() {

		@Override
		public void onClick(View v) {
			createChat((User) v.findViewById(R.id.new_content).getTag());
		}
	};

	/**
	 * 设置昵称
	 * 
	 * @param user
	 */
	private void setNickname(final User user) {
		final EditText name_input = new EditText(context);
		name_input.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		name_input.setHint("输入昵称");
		new AlertDialog.Builder(context).setTitle("修改昵称").setView(name_input)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String name = name_input.getText().toString();
						if (!"".equals(name))
							setNickname(user, name);
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 添加好友
	 */
	private void addSubscriber() {
		final EditText name_input = new EditText(context);
		name_input.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		name_input.setHint(getResources().getString(R.string.input_username));
		final EditText nickname = new EditText(context);
		nickname.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		nickname.setHint(getResources().getString(R.string.set_nickname));
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		layout.addView(name_input);
		layout.addView(nickname);
		new AlertDialog.Builder(context).setTitle("添加好友").setView(layout)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String userName = name_input.getText().toString();
						String nickname_in = nickname.getText().toString();
						if (StringUtil.empty(userName)) {
							showToast(getResources().getString(
									R.string.username_not_null));
							return;
						}
						userName = StringUtil.doEmpty(userName);
						if (StringUtil.empty(nickname_in)) {
							nickname_in = null;
						}

						if (isExitJid(StringUtil.getJidByName(userName),
								rGroups)) {
							showToast(getResources().getString(
									R.string.username_exist));
							return;
						}
						try {
							createSubscriber(StringUtil.getJidByName(userName),
									nickname_in, null);
						} catch (XMPPException e) {
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 加入组
	 * 
	 * @param user
	 */
	private void addToGroup(final User user) {
		LayoutInflater inflaterx = LayoutInflater.from(context);
		View dialogView = inflaterx.inflate(R.layout.yd_group_dialog, null);
		final Spinner spinner = (Spinner) dialogView.findViewById(R.id.list);
		ArrayAdapter<String> ada = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_dropdown_item, groupNames);
		spinner.setAdapter(ada);

		new AlertDialog.Builder(context).setTitle("移动" + "至分组")
				.setView(dialogView)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						String groupName = (spinner.getSelectedItem())
								.toString();
						if (StringUtil.notEmpty(groupName)) {
							groupName = StringUtil.doEmpty(groupName);
							if (newNames.contains(groupName)) {
								newNames.remove(groupName);
							}
							addUserGroupUI(user, groupName);
							addUserToGroup(user, groupName);
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 修改组名
	 * 
	 * @param user
	 */
	private void updateGroupNameA(final String groupName) {
		final EditText name_input = new EditText(context);
		name_input.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		name_input.setHint("输入组名");
		new AlertDialog.Builder(context).setTitle("修改组名").setView(name_input)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String gNewName = name_input.getText().toString();
						if (newNames.contains(gNewName)
								|| groupNames.contains(gNewName)) {
							showToast("组名已存在");
							return;
						}
						updateGroupNameUI(groupName, gNewName);
						updateGroupName(groupName, gNewName);
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 最近联系人列表Item点击事件监听，分为聊天消息和请求添加好友的消息
	 */
	public OnItemClickListener inviteListClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int arg2,
				long arg3) {
			final Notice notice = (Notice) view.findViewById(R.id.new_content)
					.getTag();
			if (notice.getNoticeType() == Notice.CHAT_MSG) {
				User user = new User();
				user.setJID("admin@10.1.14.58");
				createChat(user);
			} else {
				final String subFrom = notice.getFrom();
				new AlertDialog.Builder(context)
						.setMessage(subFrom + "请求添加您为好友")
						.setTitle("提示")
						.setPositiveButton("添加",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										sendSubscribe(Presence.Type.subscribed,
												subFrom);
										sendSubscribe(Presence.Type.subscribe,
												subFrom);
										refreshList();

									}
								})
						.setNegativeButton("拒绝",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										sendSubscribe(
												Presence.Type.unsubscribe,
												subFrom);
									}
								}).show();
			}

		}
	};

	/**
	 * 刷新当前的列表
	 */
	private void refreshList() {
		/** 刷新好友列表 */
		rGroups = ContacterManager.getGroups(XmppConnectionManager
				.getInstance().getConnection().getRoster());
		for (String newGroupName : newNames) {
			MRosterGroup mg = new MRosterGroup(newGroupName,
					new ArrayList<User>());
			rGroups.add(rGroups.size() - 1, mg);
		}
		expandAdapter.setContacter(rGroups);
		expandAdapter.notifyDataSetChanged();

		/** 刷新最近联系人列表 */
		inviteNotices = MessageManager.getInstance(context)
				.getRecentContactsWithLastMsg();
		noticeAdapter.setNoticeList(inviteNotices);
		noticeAdapter.notifyDataSetChanged();
		/**
		 * 有新消息进来的气泡设置
		 */
		setPaoPao();

	}

	@Override
	protected void addUserReceive(User user) {
		refreshList();
	}

	@Override
	protected void deleteUserReceive(User user) {
		if (user == null)
			return;
		Toast.makeText(context,
				(user.getName() == null) ? user.getJID() : user.getName()
						+ "被删除了", Toast.LENGTH_SHORT).show();
		refreshList();
	}

	
	@Override
	protected void changePresenceReceive(User user) {
		if (user == null)
			return;
		if (ContacterManager.contacters.get(user.getJID()) == null)
			return;
		if (!user.isAvailable())
			if (ContacterManager.contacters.get(user.getJID()).isAvailable())
				Toast.makeText(context,
						(user.getName() == null) ? user.getJID() : user
								.getName() + "上线了", Toast.LENGTH_SHORT).show();
		if (user.isAvailable())
			if (!ContacterManager.contacters.get(user.getJID()).isAvailable())
				Toast.makeText(context,
						(user.getName() == null) ? user.getJID() : user
								.getName() + "下线了", Toast.LENGTH_SHORT).show();
		refreshList();
	}

	@Override
	protected void updateUserReceive(User user) {
		refreshList();
	}

	@Override
	protected void subscripUserReceive(final String subFrom) {
		Notice notice = new Notice();
		notice.setFrom(subFrom);
		notice.setNoticeType(Notice.CHAT_MSG);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contacter_menu, menu);
		return true;
	}

	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		switch (item.getItemId()) {
		case R.id.menu_add_subscriber:
			addSubscriber();
			break;
		case R.id.menu_modify_state:
			modifyState();
			break;
		case R.id.menu_relogin:
			intent.setClass(context, LoginActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.menu_exit:
			isExit();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public void onBackPressed() {
		isExit();
	}

	@Override
	public void doChange(int lastIndex, int currentIndex) {
		if (lastIndex != currentIndex) {
			TranslateAnimation animation = null;
			LinearLayout layout = null;
			switch (currentIndex) {
			case 0:
				if (lastIndex == 1) {
					layout = (LinearLayout) tab1.getParent();
					animation = new TranslateAnimation(0, -layout.getWidth(),
							0, 0);
				} else if (lastIndex == 2) {
					layout = (LinearLayout) tab2.getParent();
					animation = new TranslateAnimation(layout.getLeft(),
							-((LinearLayout) tab1.getParent()).getWidth(), 0, 0);
				}
				break;
			case 1:
				if (lastIndex < 1) {
					layout = (LinearLayout) tab1.getParent();
					animation = new TranslateAnimation(-layout.getWidth(), 0,
							0, 0);
				} else if (lastIndex > 1) {
					layout = (LinearLayout) tab2.getParent();
					animation = new TranslateAnimation(layout.getLeft(), 0, 0,
							0);
				}
				break;
			case 2:
				if (lastIndex == 1) {
					layout = (LinearLayout) tab2.getParent();
					animation = new TranslateAnimation(0, layout.getLeft(), 0,
							0);
				} else if (lastIndex == 0) {
					layout = (LinearLayout) tab2.getParent();
					animation = new TranslateAnimation(
							-((LinearLayout) tab1.getParent()).getWidth(),
							layout.getLeft(), 0, 0);
				}
				break;
			}
			animation.setDuration(300);
			animation.setFillAfter(true);
			imageView.startAnimation(animation);
		}
	}
	
	/**
	 * 修改状态
	 */
	private void modifyState() {
		String[] states = new String[] { "在线", "隐身", "吃饭", "睡觉" };
		new AlertDialog.Builder(this)
				.setItems(states, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Presence presence = new Presence(Presence.Type.available);
						switch (which) {
						case 0:
							break;
						case 1:
							presence.setType(Presence.Type.unavailable);
							break;
						case 2:
							presence.setStatus("吃饭");
							break;
						case 3:
							presence.setStatus("睡觉");
							break;
						}
						XmppConnectionManager.getInstance().getConnection()
								.sendPacket(presence);
					}
				}).setPositiveButton("取消", null).setTitle("修改状态").show();
	}

	@Override
	public void onClick(View v) {

		if (v == tab1) {
			layout.snapToScreen(0);

		} else if (v == tab2) {
			layout.snapToScreen(1);

		} else if (v == tab3) {
			layout.snapToScreen(2);

		}
	}

	/**
	 * 上面滚动条上的气泡设置 有新消息来的通知气泡，数量设置,
	 */
	private void setPaoPao() {
		if (null != inviteNotices && inviteNotices.size() > 0) {
			int paoCount = 0;
			for (ChartHisBean c : inviteNotices) {
				Integer countx = c.getNoticeSum();
				paoCount += (countx == null ? 0 : countx);
			}
			if (paoCount == 0) {
				noticePaopao.setVisibility(View.GONE);
				return;
			}
			noticePaopao.setText(paoCount + "");
			noticePaopao.setVisibility(View.VISIBLE);
		} else {
			noticePaopao.setVisibility(View.GONE);
		}
	}

	/**
	 * 添加组
	 * 
	 * @param user
	 */
	private void addNewGroup() {
		final EditText name_input = new EditText(context);
		name_input.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		name_input.setHint("输入组名");
		new AlertDialog.Builder(context).setTitle("添加分组").setView(name_input)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String groupName = name_input.getText().toString();
						if (StringUtil.empty(groupName)) {
							return;
						}
						if (groupNames.contains(groupName)) {
							return;
						}
						addGroupNamesUi(groupName);

					}
				}).setNegativeButton("取消", null).show();
	}

	private void addChatRoom() {
		final EditText name_input = new EditText(context);
		name_input.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		name_input.setHint("输入房间名称");
		new AlertDialog.Builder(context).setTitle("创建房间").setView(name_input)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String roomName = name_input.getText().toString();
						if (StringUtil.empty(roomName)) {
							return;
						}
						if (roomNames.contains(roomName)) {
							return;
						}
						/** 创建房间并更新UI */
						roomNames.add(roomName);
						chatRoomAdapter.notifyDataSetChanged();
						createRoom(roomName);
					}
				}).setNegativeButton("取消", null).show();
	}

	/** 添加上下文菜单 */
	OnCreateContextMenuListener onCreateContextMenuListener = new OnCreateContextMenuListener() {
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			switch (v.getId()) {
			case R.id.main_expand_list:

				ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

				int type = ExpandableListView
						.getPackedPositionType(info.packedPosition);

				if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
					int gId = ExpandableListView
							.getPackedPositionGroup(info.packedPosition);
					String[] longClickItems = null;
					final String groupName = rGroups.get(gId).getName();
					if (StringUtil.notEmpty(groupName)
							&& !Constant.ALL_FRIEND.equals(groupName)
							&& !Constant.NO_GROUP_FRIEND.equals(groupName)) {
						longClickItems = new String[] { "添加分组", "更改组名", };
					} else {
						longClickItems = new String[] { "添加分组" };

					}
					new AlertDialog.Builder(context)
							.setItems(longClickItems,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											switch (which) {
											case 0:
												addNewGroup();
												break;

											case 1:
												updateGroupNameA(groupName);
												break;
											}
										}
									}).setTitle("选项").show();
				} else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

					String[] longClickItems = null;
					View vx = info.targetView;
					clickUser = (User) vx.findViewById(R.id.username).getTag();
					showToast(clickUser.getJID() + "---");

					if (StringUtil.notEmpty(clickUser.getGroupName())
							&& !Constant.ALL_FRIEND.equals(clickUser
									.getGroupName())
							&& !Constant.NO_GROUP_FRIEND.equals(clickUser
									.getGroupName())) {
						longClickItems = new String[] { "设置昵称", "添加好友", "删除好友",
								"移动到分组", "退出该组" };
					} else {
						longClickItems = new String[] { "设置昵称", "添加好友", "删除好友",
								"移动到分组" };
					}
					new AlertDialog.Builder(context)
							.setItems(longClickItems,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											switch (which) {
											case 0:
												setNickname(clickUser);
												break;
											case 1:
												addSubscriber();
												break;
											case 2:
												showDeleteDialog(clickUser);
												break;
											case 3:
												removeUserFromGroupUI(clickUser);

												removeUserFromGroup(clickUser,
														clickUser.getGroupName());
												addToGroup(clickUser);

												break;

											case 4:
												removeUserFromGroupUI(clickUser);
												removeUserFromGroup(clickUser,
														clickUser.getGroupName());
												break;
											}
										}
									}).setTitle("选项").show();
				}
				break;
			case R.id.main_chat_room_list:

				String[] longClickItems = new String[] { "创建房间", "删除房间",
						"邀请好友", "房间改名" };

				new AlertDialog.Builder(context)
						.setItems(longClickItems,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										switch (which) {
										case 0:
											addChatRoom();
											break;

										case 1:
											break;

										case 2:
											break;

										case 3:
											break;
										}
									}
								}).setTitle("选项").show();

				break;
			default:
				break;
			}

		}
	};

	/**
	 * UI级添加分组
	 */
	public void addGroupNamesUi(String newGroupName) {
		groupNames.add(newGroupName);
		newNames.add(newGroupName);
		MRosterGroup mg = new MRosterGroup(newGroupName, new ArrayList<User>());
		rGroups.add(rGroups.size() - 1, mg);
		expandAdapter.setContacter(rGroups);
		expandAdapter.notifyDataSetChanged();
	}

	/**
	 * UI级删除用户
	 */
	private void deleteUserUI(User user) {
		for (MRosterGroup g : rGroups) {
			List<User> us = g.getUsers();
			if (us != null && us.size() > 0) {
				if (us.contains(user)) {
					us.remove(user);
					g.setUsers(us);
				}
			}
		}
		expandAdapter.setContacter(rGroups);
		expandAdapter.notifyDataSetChanged();

	}

	/**
	 * UI级移动用户，把用户移除某组
	 */
	private void removeUserFromGroupUI(User user) {

		for (MRosterGroup g : rGroups) {
			if (g.getUsers().contains(user)) {
				if (StringUtil.notEmpty(g.getName())
						&& !Constant.ALL_FRIEND.equals(g.getName())) {
					List<User> users = g.getUsers();
					users.remove(user);
					g.setUsers(users);

				}
			}
		}
		expandAdapter.setContacter(rGroups);
		expandAdapter.notifyDataSetChanged();
	}

	/**
	 * UI级移动用户，把用户加入某组
	 */
	private void addUserGroupUI(User user, String groupName) {
		for (MRosterGroup g : rGroups) {
			if (groupName.equals(g.getName())) {
				List<User> users = g.getUsers();
				users.add(user);
				g.setUsers(users);
			}
		}
		expandAdapter.setContacter(rGroups);
		expandAdapter.notifyDataSetChanged();

	}

	/**
	 * UI更改组名
	 */
	private void updateGroupNameUI(String old, String newGroupName) {

		if (StringUtil.empty(old) || Constant.ALL_FRIEND.equals(old)
				|| Constant.NO_GROUP_FRIEND.equals(old)) {
			return;
		}
		if (StringUtil.empty(newGroupName)
				|| Constant.ALL_FRIEND.equals(newGroupName)
				|| Constant.NO_GROUP_FRIEND.equals(newGroupName)) {
			return;
		}

		if (newNames.contains(old)) {
			newNames.remove(old);
			newNames.add(newGroupName);
			return;
		}
		for (MRosterGroup g : rGroups) {
			if (g.getName().equals(old)) {
				g.setName(newGroupName);
			}
		}
		expandAdapter.notifyDataSetChanged();

	}

	/**
	 * 删除用户
	 */
	private void showDeleteDialog(final User clickUser) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				getResources().getString(R.string.delete_user_confim))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								deleteUserUI(clickUser);
								try {
									removeSubscriber(clickUser.getJID());
								} catch (XMPPException e) {
									Log.e(TAG, "", e);
								}
								NoticeManager.getInstance(context)
										.delNoticeHisWithSb(clickUser.getJID());
								MessageManager.getInstance(context)
										.delChatHisWithSb(clickUser.getJID());
							}
						})
				.setNegativeButton(getResources().getString(R.string.no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();

	}

	@Override
	protected void handReConnect(boolean isSuccess) {
		if (Constant.RECONNECT_STATE_SUCCESS == isSuccess) {
			iv_status.setImageDrawable(getResources().getDrawable(
					R.drawable.status_online));

		} else if (Constant.RECONNECT_STATE_FAIL == isSuccess) {
			iv_status.setImageDrawable(getResources().getDrawable(
					R.drawable.status_offline));
		}
	}

}