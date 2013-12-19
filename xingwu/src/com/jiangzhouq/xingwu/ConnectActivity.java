package com.jiangzhouq.xingwu;

import java.util.LinkedList;

import tvb.boxclient.Login;
import tvb.boxclient.Login.EXmppState;
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
import android.widget.TextView;

public class ConnectActivity extends Activity implements Login.IXmppStateObserver, Presence.IPresenceStatusObserver{
	
	private final int HANDLER_PRESENCE_UPDATE = 0;
	
	private Login          mLogin = null;
	private final Presence       mPresence       = new Presence();
	private ConnectActivity      Observer        = this;
	private LinkedList<String>   mRoster         = new LinkedList<String>();
	private TextView             mTvContent;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
				case HANDLER_PRESENCE_UPDATE:
					String nullString = "none";
					Bundle bundle = msg.getData();
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
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connectactivity);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String sn = bundle.getString(Constants.BUNDLE_KEY_SN);
		mTvContent = (TextView) findViewById(R.id.tv_content);
		if(null == sn || sn.isEmpty()){
			if (Constants.LOG_SWITCH)
				Log.d(Constants.LOG_TAG, "sn ConnectActivity received is empty or null!");
			return;
		}
		
		mTvContent.setText(sn);
//		if(!sn.isEmpty()){
//			mLogin = new Login(sn, sn, Constants.TVB_LOGIN_SERVER_IP, Constants.TVB_LOGIN_TAG);
//			mLogin.AddXmppStateObserver(Observer);
//			mLogin.DoLogin();
//		}
	}
	
	@Override
	protected void onDestroy() {
//		mPresence.SetPresStatus(false, EPresStatus.E_PRES_SHOW_OFFLINE);
//		mPresence.RemovePresStatusObserver();
//		mLogin.RemoveXmppStateObserver();
//		mLogin.DoLogout();
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
		switch (newState) 
		{						
		case E_XMPP_STATE_START:
			break;
			
		case E_XMPP_STATE_OPENING:
			break;

		case E_XMPP_STATE_OPEN:
			if (Constants.LOG_SWITCH)
				Log.d(Constants.LOG_TAG, "XmppStateChange");
    		mTvContent = (TextView) findViewById(R.id.tv_content);		
			mTvContent.setText("等待好友上线...");
            mPresence.AddPresStatusObserver(Observer);
			mPresence.SetPresStatus(true, EPresStatus.E_PRES_SHOW_ONLINE);
			break;
			
		case E_XMPP_STATE_CLOSED:
			break;

		default:
			break;
		}
		
	}
}
