package com.jiangzhouq.xingwu;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tvb.boxclient.PeerConn;
import tvb.boxclient.PeerConn.EPeerConnType;
import tvb.boxclient.PeerConn.IceServer;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AudioActivity extends Activity implements PeerConn.IPeerConnObserver, OnClickListener {
	private final PeerConn       mPeerConn   = new PeerConn();
//	private LinkedList<String>   mRoster     = new LinkedList<String>();
	private final int HANDLER_MSG_ACCEPT = 1;
	private final int HANDLER_MSG_ASK = 2;
	private final int HANDLER_MSG_BYE = 3;
	private final int HANDLER_MSG_INVITE = 4;
	private final int HANDLER_MSG_MSG = 5;
	private final int HANDLER_MSG_REJECT = 6;
	private final int HANDLER_MSG_TIME = 9999;
	private int cSecs =0;
	private TextView timeCount;
	private Timer timer = null;
	private TimerTask task = null;
	private String strJid = "";
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
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
				timeCount.setText(String.format("%1$02d:%2$02d:%3$02d",hour, min, sec));
				break;
			}
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
	private void stopTimer(){
		if(null != timer){
			task.cancel();
			task = null;
			timer.cancel();
			timer.purge();
			timer = null;
			mHandler.removeMessages(HANDLER_MSG_TIME);
			timeCount.setText(String.format("%1$02d:%2$02d:%3$02d",0, 0, 0));
		}
	}
	void handleConnect(Message msg, final boolean isPost){
		Bundle bundle = msg.getData();
		final String strFrom = bundle.getString("strFrom");
		
		new Thread(new Runnable() {
			
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
			}
		}).start();
		
		if (Constants.LOG_SWITCH)
			Log.d(Constants.LOG_TAG, "successfully started audio:" + isPost);
		ImageView audioLoading = (ImageView) findViewById(R.id.audio_state);
		audioLoading.setBackgroundResource(R.drawable.voicesearch_loading006);
		
		startTimer();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audioactivity);

		Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(
				this));
		abortUnless(PeerConn.InitAndroidGlobals(this),
				"Failed to initializeAndroidGlobals");
		Intent intent =  getIntent();
		Bundle bundle = intent.getExtras();
		strJid = bundle.getString("strJid");
		timeCount = (TextView) findViewById(R.id.audio_content);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		ImageView audioLoading = (ImageView) findViewById(R.id.audio_state);
		audioLoading.setBackgroundResource(R.drawable.audio_loading);
		AnimationDrawable anim = (AnimationDrawable) audioLoading
				.getBackground();
		anim.start();
		
		AudioManager audioManager = ((AudioManager) getSystemService(AUDIO_SERVICE));
		// TODO(fischman): figure out how to do this Right(tm) and remove the
		// suppression.
		@SuppressWarnings("deprecation")
		boolean isWiredHeadsetOn = audioManager.isWiredHeadsetOn();
		audioManager.setMode(isWiredHeadsetOn ? AudioManager.MODE_IN_CALL
				: AudioManager.MODE_IN_COMMUNICATION);
		audioManager.setSpeakerphoneOn(!isWiredHeadsetOn);
		mPeerConn.AddPeerConnMsgObserver(AudioActivity.this);
		
		
		LinearLayout hangupBtn = (LinearLayout) findViewById(R.id.hangup);
		hangupBtn.setOnClickListener(this);
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mPeerConn.SendPeerConnInvite(strJid, EPeerConnType.E_PEERCONN_VOICE_FDX);
			}
		}, 2000);
	}
	
	@Override
	protected void onDestroy() {
		stopTimer();
		mPeerConn.RemovePeerConnMsgObserver();
		super.onDestroy();
	}

	private static void abortUnless(boolean condition, String msg) {
		if (!condition) {
			throw new RuntimeException(msg);
		}
	}

	@Override
	public void RecvPeerConnAccept(final String strFrom, final EPeerConnType eType) {
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
	public void RecvPeerConnAck(String arg0) {
		AudioActivity.this.finish();
		runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	        }
	    });	
	}

	@Override
	public void RecvPeerConnBusy(String arg0, String arg1) {
		runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	        }
	    });	
	}

	@Override
	public void RecvPeerConnBye(final String strFrom) {
		// TODO Auto-generated method stub
	    runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	        	mPeerConn.Stop();
	        	mPeerConn.SendPeerConnAck(strFrom);
	        	AudioActivity.this.finish();
	        }
	    });	
	}

	@Override
	public void RecvPeerConnInvite(final String strFrom, final EPeerConnType eType) {
    	if (eType == EPeerConnType.E_PEERCONN_VOICE_FDX)
    	{		
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
		runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	        }
	    });	
	}

	@Override
	public void RecvPeerConnReject(String arg0, String arg1) {
		// TODO Auto-generated method stub
	    runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	        }
	    });	
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.hangup:
			mPeerConn.SendPeerConnBye(strJid);
			break;
		}
	}
}
