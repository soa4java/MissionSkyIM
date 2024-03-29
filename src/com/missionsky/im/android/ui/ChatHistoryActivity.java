package com.missionsky.im.android.ui;

import java.util.Collections;
import java.util.List;

import com.missionsky.im.android.R;

import com.missionsky.im.android.manager.ContacterManager;
import com.missionsky.im.android.manager.MessageManager;
import com.missionsky.im.android.manager.XmppConnectionManager;
import com.missionsky.im.android.model.IMMessage;
import com.missionsky.im.android.model.User;
import com.missionsky.im.android.util.StringUtil;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ChatHistoryActivity extends ActivitySupport {
	private ImageView titleBack;
	private List<IMMessage> msgList;
	private MessageManager msgManager;
	private ListView listView;
	private MsgHisListAdapter adapter;
	private String to;
	private int pageSize = 10;
	private int currentPage = 1;
	private int pageCount;// 总页数
	private int recordCount;// 记录总数
	private ImageView imageViewLeft;// 上一页
	private ImageView imageViewRight;// 上一页
	private TextView editTextPage;// 当前页
	private Button delBtn;
	private TextView textViewPage;// 总页数
	private User user;// 聊天人
	private TextView ivTitleName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chathistory);
		init();
	}

	private void init() {
		to = getIntent().getStringExtra("to");
		if (to == null)
			return;
		msgManager = MessageManager.getInstance(context);
		getEimApplication().addActivity(this);
		titleBack = (ImageView) findViewById(R.id.title_back);
		titleBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		ivTitleName = (TextView) findViewById(R.id.ivTitleName);
		user = ContacterManager.getByUserJid(to, XmppConnectionManager
				.getInstance().getConnection());
		String data = getResources().getString(R.string.chat_his_with_sb);
		if (null != user) {
			data = String.format(data, user.getName());
		} else {
			data = String.format(data, StringUtil.getUserNameByJid(to));
		}

		ivTitleName.setText(data);
		recordCount = MessageManager.getInstance(context).getChatCountWithSb(to);
		pageCount = (recordCount + pageSize - 1) / pageSize;
		imageViewLeft = (ImageView) findViewById(R.id.imageViewLeft);
		imageViewRight = (ImageView) findViewById(R.id.imageViewRight);
		editTextPage = (TextView) findViewById(R.id.editTextPage);
		editTextPage.setText(currentPage + "");
		imageViewRight.setOnClickListener(nextClick);
		imageViewLeft.setOnClickListener(preClick);
		textViewPage = (TextView) findViewById(R.id.textViewPage);
		textViewPage.setText("" + pageCount);
		delBtn = (Button) findViewById(R.id.buttonDelete);
		delBtn.setOnClickListener(delClick);

		listView = (ListView) findViewById(R.id.listViewHistory);
		msgList = msgManager.getMessageListByFrom(to, currentPage, pageSize);
		if (msgList != null && msgList.size() > 0) {
			Collections.sort(msgList);
			adapter = new MsgHisListAdapter(context, msgList);
			listView.setAdapter(adapter);
		}

	}

	private class MsgHisListAdapter extends BaseAdapter {

		private List<IMMessage> items;
		private Context context;
		private LayoutInflater inflater;

		public MsgHisListAdapter(Context context, List<IMMessage> items) {
			this.context = context;
			this.items = items;

		}

		public void refreshList(List<IMMessage> items) {
			Collections.sort(items);
			this.items = items;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return items == null ? 0 : items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			inflater = LayoutInflater.from(context);
			IMMessage message = items.get(position);
			Holder holder = null;
			if (convertView == null) {
				convertView = this.inflater.inflate(R.layout.chathistory_item,
						null);
				holder = new Holder();
				holder.name = (TextView) convertView
						.findViewById(R.id.tvHistoryName);
				holder.time = (TextView) convertView
						.findViewById(R.id.tvHistoryTime);
				holder.content = (TextView) convertView
						.findViewById(R.id.tvMsgItem);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			if (message.getMsgType() == 0) {
				if (user == null) {
					holder.name.setText(StringUtil.getUserNameByJid(to));
				} else {
					holder.name.setText(user.getName());
				}

			} else {
				holder.name.setText("我");
			}

			holder.time.setText(message.getTime().substring(0, 19));
			holder.content.setText(message.getContent());

			return convertView;
		}

		class Holder {
			TextView name;
			TextView time;
			TextView content;
		}

	}

	private OnClickListener nextClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (currentPage >= pageCount) {
				return;
			}
			currentPage += 1;
			editTextPage.setText(currentPage + "");
			msgList = msgManager.getMessageListByFrom(to, currentPage, pageSize);
			adapter.refreshList(msgList);
		}
	};
	private OnClickListener preClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (currentPage <= 1) {
				return;
			}
			currentPage = currentPage - 1;
			editTextPage.setText(currentPage + "");
			msgList = msgManager.getMessageListByFrom(to, currentPage, pageSize);
			adapter.refreshList(msgList);
		}
	};
	private OnClickListener delClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			msgManager.delChatHisWithSb(to);
			finish();
		}
	};

}
