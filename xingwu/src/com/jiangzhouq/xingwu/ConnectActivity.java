package com.jiangzhouq.xingwu;

import java.io.Serializable;
import java.util.LinkedList;

import tvb.boxclient.Login;
import tvb.boxclient.Login.EXmppState;
import tvb.boxclient.PeerConn;
import tvb.boxclient.Presence;
import tvb.boxclient.Presence.EPresStatus;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ConnectActivity extends Activity implements Login.IXmppStateObserver, Presence.IPresenceStatusObserver{
	
	private final int HANDLER_PRESENCE_UPDATE = 0;
	private final int HANDLER_XAMPP_STATE_UPDATE = 1;
	
	private final int MESSAGE_E_XMPP_STATE_START = 0;
	private final int MESSAGE_E_XMPP_STATE_OPENING = 1;
	private final int MESSAGE_E_XMPP_STATE_OPEN = 2;
	private final int MESSAGE_E_XMPP_STATE_CLOSED = 3;
	
	private Login          mLogin = null;
	private final Presence       mPresence       = new Presence();
	private ConnectActivity      Observer        = this;
	private LinkedList<String>   mRoster         = new LinkedList<String>();
	private TextView             mTvContent;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			switch(msg.what){
				case HANDLER_PRESENCE_UPDATE:
					String nullString = "none";
					boolean bAvailable = bundle.getBoolean("bAvailable", false);
					String strJid = bundle.getString("strJid",nullString);
					if(strJid.equals(nullString)){
						if (Constants.LOG_SWITCH)
							Log.d(Constants.LOG_TAG, "The msg mHandler received has somethins wrong:strJid in the bundle is empty!");
						break;
					}
					if (bAvailable)
		    		{
		    			String strContent = "在线好友：" + strJid;
		    			mTvContent.setText(strContent);
		    			mRoster.add(strJid);
		    		}
		    		else
		    		{
		    			String strContent = "好友下线：" + strJid;
		    			mRoster.remove(strJid);
		    			mTvContent.setText(strContent);
		    		}
					break;
				case HANDLER_XAMPP_STATE_UPDATE:
					switch(bundle.getInt("state")){
					case MESSAGE_E_XMPP_STATE_START:
						mTvContent.setText("准备上线...");
						break;
					case MESSAGE_E_XMPP_STATE_OPENING:
						mTvContent.setText("正在上线...");
						break;
					case MESSAGE_E_XMPP_STATE_OPEN:
			    		mTvContent = (TextView) findViewById(R.id.tv_content);		
						mTvContent.setText("等待好友上线...");
			            mPresence.AddPresStatusObserver(Observer);
						mPresence.SetPresStatus(true, EPresStatus.E_PRES_SHOW_ONLINE);
						break;
					case MESSAGE_E_XMPP_STATE_CLOSED:
						mTvContent.setText("关闭...");
						break;
					}
					break;
			}
			if (Constants.LOG_SWITCH)
				Log.d(Constants.LOG_TAG, (String) mTvContent.getText());
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connectactivity);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		final String sn = bundle.getString(Constants.BUNDLE_KEY_SN);
		mTvContent = (TextView) findViewById(R.id.tv_content);
		if(null == sn || sn.isEmpty()){
			if (Constants.LOG_SWITCH)
				Log.d(Constants.LOG_TAG, "sn ConnectActivity received is empty or null!");
			return;
		}
		mTvContent.setText(sn);
		ImageView image = (ImageView) findViewById(R.id.tv_state);
		image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLogin = new Login(sn, sn, Constants.TVB_LOGIN_SERVER_IP, Constants.TVB_LOGIN_TAG);
				mLogin.AddXmppStateObserver(Observer);
				abortUnless(PeerConn.InitAndroidGlobals(this), "Failed to initializeAndroidGlobals");
				mLogin.DoLogin();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		mPresence.SetPresStatus(false, EPresStatus.E_PRES_SHOW_OFFLINE);
		mPresence.RemovePresStatusObserver();
		mLogin.RemoveXmppStateObserver();
		mLogin.DoLogout();
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_connectactivity, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.setting:
			startActivity(new Intent(this, SettingActivity.class));
			break;
		}
		return true;
	}
	private static void abortUnless(boolean condition, String msg) 
	{
		if (!condition)
		{
			throw new RuntimeException(msg);
		}
	}
	@Override
	public void PresUpdate(final String strJid, final EPresStatus eShow, final String strStatus, final boolean bAvailable) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("strJid", strJid);
		/*
		 * the eshow must implements Serializable
		 */
		//bundle.putSerializable("eShow", eShow);
		//bundle.putString("strStatus", strStatus);
		bundle.putBoolean("bAvailable", bAvailable);
		msg.setData(bundle);
		msg.what = HANDLER_PRESENCE_UPDATE;
		mHandler.sendMessage(msg);
	}
	@Override
	public void XmppStateChange(EXmppState newState) {
		
		Message msg = new Message();
		Bundle bundle = new Bundle();
		
		switch (newState) 
		{			
		case E_XMPP_STATE_START:
			bundle.putInt("state", MESSAGE_E_XMPP_STATE_START);
			break;
			
		case E_XMPP_STATE_OPENING:
			bundle.putInt("state", MESSAGE_E_XMPP_STATE_OPENING);
			break;

		case E_XMPP_STATE_OPEN:
			bundle.putInt("state", MESSAGE_E_XMPP_STATE_OPEN);
			break;
			
		case E_XMPP_STATE_CLOSED:
			bundle.putInt("state", MESSAGE_E_XMPP_STATE_CLOSED);
			break;

		default:
			break;
		}
		msg.setData(bundle);
		msg.what = HANDLER_XAMPP_STATE_UPDATE;
		mHandler.sendMessage(msg);
		
		if (Constants.LOG_SWITCH)
			Log.d(Constants.LOG_TAG, (String) mTvContent.getText());
	}
}
