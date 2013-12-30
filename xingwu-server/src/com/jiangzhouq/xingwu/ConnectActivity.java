package com.jiangzhouq.xingwu;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tvb.boxclient.Login;
import tvb.boxclient.Login.EXmppState;
import tvb.boxclient.PeerConn;
import tvb.boxclient.PeerConn.EPeerConnType;
import tvb.boxclient.PeerConn.IceServer;
import tvb.boxclient.Presence;
import tvb.boxclient.Presence.EPresStatus;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConnectActivity extends Activity implements Login.IXmppStateObserver, Presence.IPresenceStatusObserver, OnClickListener, PeerConn.IPeerConnObserver{
	
	private final int HANDLER_PRESENCE_UPDATE = 0;
	private final int HANDLER_XAMPP_STATE_UPDATE = 1;
	
	private final int MESSAGE_E_XMPP_STATE_START = 0;
	private final int MESSAGE_E_XMPP_STATE_OPENING = 1;
	private final int MESSAGE_E_XMPP_STATE_OPEN = 2;
	private final int MESSAGE_E_XMPP_STATE_CLOSED = 3;
	
	private final int HANDLER_MSG_ACCEPT = 4;
	private final int HANDLER_MSG_ASK = 5;
	private final int HANDLER_MSG_BYE = 6;
	private final int HANDLER_MSG_INVITE = 7;
	private final int HANDLER_MSG_MSG = 8;
	private final int HANDLER_MSG_REJECT = 9;
	private final int HANDLER_MSG_TIME = 9999;
	private int cSecs =0;
	private TextView mTvContent;
	private Timer timer = null;
	private TimerTask task = null;
	
	private Login          mLogin = null;
	private final Presence       mPresence       = new Presence();
	private ConnectActivity      Observer        = this;
	private LinkedList<String>   mRoster         = new LinkedList<String>();
	private final PeerConn       mPeerConn   = new PeerConn();
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
//		    			String strContent = "在线好友：" + strJid;
		    			String strContent = "连接成功";
		    			mTvContent.setText(strContent);
		    			mRoster.add(strJid);
		    			AudioManager audioManager = ((AudioManager) getSystemService(AUDIO_SERVICE));
		    			// TODO(fischman): figure out how to do this Right(tm) and remove the
		    			// suppression.
		    			@SuppressWarnings("deprecation")
		    			boolean isWiredHeadsetOn = audioManager.isWiredHeadsetOn();
		    			audioManager.setMode(isWiredHeadsetOn ? AudioManager.MODE_IN_CALL
		    					: AudioManager.MODE_IN_COMMUNICATION);
		    			audioManager.setSpeakerphoneOn(!isWiredHeadsetOn);
		    			mPeerConn.AddPeerConnMsgObserver(ConnectActivity.this);
		    			updateUI(true);
		    		}
		    		else
		    		{
//		    			String strContent = "好友下线：" + strJid;
		    			String strContent = "等待好友上线...";
		    			mRoster.remove(strJid);
		    			mTvContent.setText(strContent);
		    			updateUI(false);
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
					updateUI(false);
					break;
				case HANDLER_MSG_ACCEPT:
					handleConnect(msg, true);
					break;
				case HANDLER_MSG_ASK:
					break;
				case HANDLER_MSG_BYE:
					break;
				case HANDLER_MSG_INVITE:
					handleConnect(msg, false);
					break;
				case HANDLER_MSG_MSG:
					break;
				case HANDLER_MSG_REJECT:
					break;
				case HANDLER_MSG_TIME:
					cSecs ++;
					int hour = cSecs/3600;
					int min = (cSecs%3600)/60;
					int sec = cSecs%60;
					mTvContent.setText(String.format("%1$02d:%2$02d:%3$02d",hour, min, sec));
					break;
			}
			if (Constants.LOG_SWITCH)
				Log.d(Constants.LOG_TAG, (String) mTvContent.getText());
		};
	};
	private void startTimer(){
		cSecs = 0;
		if(null == timer){
			if(null == task){
				task = new TimerTask() {
					@Override
					public void run() {
						mHandler.sendEmptyMessage(HANDLER_MSG_TIME);
					}
				};
			}
		}
		timer = new Timer(true);
		timer.schedule(task, 1000, 1000);
	}
	
	void handleConnect(Message msg, final boolean isPost){
		if (Constants.LOG_SWITCH)
			Log.d(Constants.LOG_TAG, "handleConnect");
		Bundle bundle = msg.getData();
		final String strFrom = bundle.getString("strFrom");
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				if(!isPost){
					mPeerConn.SendPeerConnAccept(strFrom, EPeerConnType.E_PEERCONN_VOICE_FDX);
				}
				IceServer server = new IceServer("stun:"+ Constants.TVB_LOGIN_SERVER_IP +":19302");
				
				List<IceServer> servers = new LinkedList<IceServer>();
				servers.add(server);
				if (Constants.LOG_SWITCH)
					Log.d(Constants.LOG_TAG, "start to start audio:" + isPost);
				if(isPost){
					mPeerConn.Start(EPeerConnType.E_PEERCONN_VOICE_FDX, strFrom, servers, true);
				}else{
					mPeerConn.Start(EPeerConnType.E_PEERCONN_VOICE_FDX, strFrom, servers, false);
				}
				if (Constants.LOG_SWITCH)
					Log.d(Constants.LOG_TAG, "successfully started audio:" + isPost);
			}
			
		}).start();
		startTimer();
	}
	
	private void stopTimer(){
		if(null != timer){
			task.cancel();
			task = null;
			timer.cancel();
			timer.purge();
			timer = null;
			mHandler.removeMessages(HANDLER_MSG_TIME);
			mTvContent.setText(String.format("%1$02d:%2$02d:%3$02d",0, 0, 0));
		}
	}
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
//		mTvContent.setText(sn);
//		ImageView image = (ImageView) findViewById(R.id.tv_state);
//		image.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mLogin = new Login(sn, "123456", Constants.TVB_LOGIN_SERVER_IP, Constants.TVB_LOGIN_TAG);
				mLogin.AddXmppStateObserver(Observer);
				abortUnless(PeerConn.InitAndroidGlobals(this), "Failed to initializeAndroidGlobals");
				mLogin.DoLogin();
			}
		}, 2000);
		
				
				
//			}
//		});
		LinearLayout videoBtn = (LinearLayout) findViewById(R.id.video);
		videoBtn.setOnClickListener(this);
		LinearLayout audioBtn = (LinearLayout) findViewById(R.id.audio);
		audioBtn.setOnClickListener(this);
		updateUI(false);
	} 
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		ImageView image = (ImageView) findViewById(R.id.tv_state);
		AnimationDrawable anim = (AnimationDrawable) image.getDrawable();
		anim.start();
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
	void updateUI( boolean enable){
		LinearLayout videoBtn = (LinearLayout) findViewById(R.id.video);
		LinearLayout audioBtn = (LinearLayout) findViewById(R.id.audio);
		videoBtn.setClickable(enable);
		audioBtn.setClickable(enable);
		ImageView image = (ImageView) findViewById(R.id.tv_state);
		if(!enable){
			audioBtn.setBackgroundResource(R.drawable.bg_btn_video_disable);
			videoBtn.setBackgroundResource(R.drawable.bg_btn_video_disable);
			image.setBackgroundResource(R.drawable.logo_server);
			image.setImageAlpha(255);
		}else{
			audioBtn.setBackgroundResource(R.drawable.bg_btn_video);
			videoBtn.setBackgroundResource(R.drawable.bg_btn_video);
			image.setBackgroundResource(R.drawable.logo_server_highlight);
			image.setImageAlpha(0);
		}
		
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
	public void XmppStateChange(final EXmppState newState) {
		 
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

	@Override
	public void onClick(View v) {
		Intent intent ;
		Bundle bundle;
		switch(v.getId()){
		case R.id.video:
			intent  = new Intent(this, VideoActivity.class);
			bundle = new Bundle();
			bundle.putString("strJid", mRoster.getFirst());
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.audio:
			intent  = new Intent(this, AudioActivity.class);
			bundle = new Bundle();
			bundle.putString("strJid", mRoster.getFirst());
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
	}
	@Override
	public void RecvPeerConnAccept(String strFrom, EPeerConnType eType) {
		if (eType == EPeerConnType.E_PEERCONN_VOICE_FDX)
    	{		
    		Message msg = new Message();
    		Bundle bundle = new Bundle();
    		bundle.putString("strFrom", strFrom);
    		msg.setData(bundle);
    		msg.what = HANDLER_MSG_ACCEPT;
    		mHandler.sendMessage(msg);
    	}
	}
	@Override
	public void RecvPeerConnAck(String strFrom) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void RecvPeerConnBusy(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void RecvPeerConnBye(final String strFrom) {
		runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	        	mPeerConn.Stop();
	        	mPeerConn.SendPeerConnAck(strFrom);
	        	ConnectActivity.this.finish();
	        }
	    });	
	}
	@Override
	public void RecvPeerConnInvite(final String strFrom, final EPeerConnType eType) {
		if (eType == EPeerConnType.E_PEERCONN_VOICE_FDX)
    	{		
//					mPeerConn.SendPeerConnAccept(strFrom, EPeerConnType.E_PEERCONN_VOICE_FDX);
//					IceServer server = new IceServer("stun:"+ Constants.TVB_LOGIN_SERVER_IP +":19302");
//					
//					List<IceServer> servers = new LinkedList<IceServer>();
//					servers.add(server);
//					if (Constants.LOG_SWITCH)
//						Log.d(Constants.LOG_TAG, "server addded" );
//					mPeerConn.Start(EPeerConnType.E_PEERCONN_VOICE_FDX, strFrom, servers, false);
//					
//					if (Constants.LOG_SWITCH)
//						Log.d(Constants.LOG_TAG, "conn started");
//					startTimer();
    		Message msg = new Message();
    		Bundle bundle = new Bundle();
    		bundle.putString("strFrom", strFrom);
    		msg.setData(bundle);
    		msg.what = HANDLER_MSG_INVITE;
    		mHandler.sendMessage(msg);
    	}
	}
	@Override
	public void RecvPeerConnMsg(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void RecvPeerConnReject(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
}
