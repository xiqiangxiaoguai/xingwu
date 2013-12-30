package com.jiangzhouq.xingwu;

import java.util.LinkedList;
import java.util.List;

import tvb.boxclient.PeerConn;
import tvb.boxclient.PeerConn.EPeerConnType;
import tvb.boxclient.PeerConn.I420Frame;
import tvb.boxclient.PeerConn.IPeerConnObserver;
import tvb.boxclient.PeerConn.IceServer;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class VideoActivity extends Activity implements IPeerConnObserver{
	private VideoStreamsView     mVsv;
	private final PeerConn       mPeerConn   = new PeerConn();
	private String strJid;
	Handler mHandler = new Handler(){};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(
				this));
		abortUnless(PeerConn.InitAndroidGlobals(this),
				"Failed to initializeAndroidGlobals");
		Intent intent =  getIntent();
		Bundle bundle = intent.getExtras();
		strJid = bundle.getString("strJid");
		
		Point displaySize = new Point();
	    getWindowManager().getDefaultDisplay().getSize(displaySize);
	    mVsv = new VideoStreamsView(this, displaySize);
	    setContentView(mVsv);

	    AudioManager audioManager =
	            ((AudioManager) getSystemService(AUDIO_SERVICE));
	        // TODO(fischman): figure out how to do this Right(tm) and remove the
	        // suppression.
	        @SuppressWarnings("deprecation")
	        boolean isWiredHeadsetOn = audioManager.isWiredHeadsetOn();
        audioManager.setMode(isWiredHeadsetOn ?
            AudioManager.MODE_IN_CALL : AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(!isWiredHeadsetOn);
        mPeerConn.AddPeerConnMsgObserver(this);
        mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mPeerConn.SendPeerConnInvite(strJid, EPeerConnType.E_PEERCONN_VV_FDX);
			}
		}, 2000);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mPeerConn.SendPeerConnBye(strJid);
		mPeerConn.Stop();
		mPeerConn.RemovePeerConnMsgObserver();
	}
	private static void abortUnless(boolean condition, String msg) 
	{
		if (!condition)
		{
			throw new RuntimeException(msg);
		}
	}
	@Override
	public void RecvPeerConnAccept(final String strFrom, final EPeerConnType eType) {
		// TODO Auto-generated method stub
		if (Constants.LOG_SWITCH)
			Log.d(Constants.LOG_TAG, "video RecvPeerConnAccept");
		runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
//	        	if (eType == EPeerConnType.E_PEERCONN_VOICE_FDX)
	        	if (eType == EPeerConnType.E_PEERCONN_VV_FDX)
	        	{		
	        		IceServer server = new IceServer("stun:"+ Constants.TVB_LOGIN_SERVER_IP +":19302");
//	        		IceServer server = new IceServer("turn:120.236.21.178:3478", "3dinlife", "passwd");
	        		List<IceServer> servers = new LinkedList<IceServer>();
	        		servers.add(server);
	        		if (Constants.LOG_SWITCH)
						Log.d(Constants.LOG_TAG, "server ready" + " strFrom:" + strFrom);
//	        		mPeerConn.Start(EPeerConnType.E_PEERCONN_VOICE_FDX, strFrom, servers, true);
	        		mPeerConn.Start(EPeerConnType.E_PEERCONN_VV_FDX, strFrom, servers,
	        				new VideoRenderer(mVsv, VideoStreamsView.Endpoint.LOCAL), 
	        				new VideoRenderer(mVsv, VideoStreamsView.Endpoint.REMOTE), true);
	        		if (Constants.LOG_SWITCH)
						Log.d(Constants.LOG_TAG, "VV_Start");
	        	}
	        }
	    });		
	}

	@Override
	public void RecvPeerConnAck(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void RecvPeerConnBusy(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
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
	        }
	    });		
	}

	@Override
	public void RecvPeerConnInvite(final String strFrom, final EPeerConnType eType) {
		// TODO Auto-generated method stub
		if (Constants.LOG_SWITCH)
			Log.d(Constants.LOG_TAG, "video RecvPeerConnInvite");
		runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
//	        	if (eType == EPeerConnType.E_PEERCONN_VOICE_FDX)
	        	if (eType == EPeerConnType.E_PEERCONN_VV_FDX)
	        	{		
	        		mPeerConn.SendPeerConnAccept(strFrom, EPeerConnType.E_PEERCONN_VV_FDX);
	        		IceServer server = new IceServer("stun:"+ Constants.TVB_LOGIN_SERVER_IP +":19302");
//	        		IceServer server = new IceServer("turn:120.236.21.178:3478", "3dinlife", "passwd");
	        		List<IceServer> servers = new LinkedList<IceServer>();
	        		servers.add(server);
	        		if (Constants.LOG_SWITCH)
						Log.d(Constants.LOG_TAG, "server ready");
//	        		mPeerConn.Start(EPeerConnType.E_PEERCONN_VOICE_FDX, strFrom, servers, false);
	        		mPeerConn.Start(EPeerConnType.E_PEERCONN_VV_FDX, strFrom, servers,
	        				new VideoRenderer(mVsv, VideoStreamsView.Endpoint.LOCAL), 
	        				new VideoRenderer(mVsv, VideoStreamsView.Endpoint.REMOTE), false);
	        		if (Constants.LOG_SWITCH)
						Log.d(Constants.LOG_TAG, "VV_start");
	        	}
	        }
	    });		
	}

	@Override
	public void RecvPeerConnMsg(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void RecvPeerConnReject(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
	private class VideoRenderer implements PeerConn.IVideoRenderer
	{
		private final VideoStreamsView          mView;
		private final VideoStreamsView.Endpoint mVideoStream;
		public VideoRenderer(VideoStreamsView videoView, VideoStreamsView.Endpoint localOrRemote)
		{
			mView        = videoView;
			mVideoStream = localOrRemote;
		}
		
		@Override
		public void RenderFrame(I420Frame frame)
		{
			// TODO Auto-generated method stub
			mView.queueFrame(mVideoStream, frame);
		}

		@Override
		public void SetSize(final int nWidth, final int nHeight) 
		{
			// TODO Auto-generated method stub
			mView.queueEvent(new Runnable()
			{
				public void run()
				{
					mView.setSize(mVideoStream, nWidth, nHeight);
				}
			});				
		}		
	}
}
