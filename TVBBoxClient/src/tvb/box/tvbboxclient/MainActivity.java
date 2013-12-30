package tvb.box.tvbboxclient;

import java.util.LinkedList;
import java.util.List;

import tvb.boxclient.Login;
import tvb.boxclient.Login.EXmppState;
import tvb.boxclient.PeerConn;
import tvb.boxclient.PeerConn.EPeerConnType;
import tvb.boxclient.PeerConn.I420Frame;
import tvb.boxclient.PeerConn.IceServer;
import tvb.boxclient.Presence;
import tvb.boxclient.Presence.EPresStatus;
import android.app.Activity;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity 
implements Login.IXmppStateObserver
, Presence.IPresenceStatusObserver
, PeerConn.IPeerConnObserver
{
	private static String TAG = "TVBBoxClient";
	
	private final Login          mLogin      = new Login("matty1", "123456", "120.236.21.178", "TVBBoxClient");
	private final Presence       mPresence   = new Presence();
	private final PeerConn       mPeerConn   = new PeerConn();
	private MainActivity         Observer    = this;
	private LinkedList<String>   mRoster     = new LinkedList<String>();
	private TextView             mTvContent;
	private Button               mBtVoiceCall;
	private VideoStreamsView     mVsv;

	Handler mhandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case 0:
				mTvContent.setText("connected");
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	    Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));
 /*
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	    Point displaySize = new Point();
	    getWindowManager().getDefaultDisplay().getSize(displaySize);
	    mVsv = new VideoStreamsView(this, displaySize);
	    setContentView(mVsv);
 */		
	    abortUnless(PeerConn.InitAndroidGlobals(this), "Failed to initializeAndroidGlobals");

	    AudioManager audioManager =
	            ((AudioManager) getSystemService(AUDIO_SERVICE));
	        // TODO(fischman): figure out how to do this Right(tm) and remove the
	        // suppression.
	        @SuppressWarnings("deprecation")
	        boolean isWiredHeadsetOn = audioManager.isWiredHeadsetOn();
	        audioManager.setMode(isWiredHeadsetOn ?
	            AudioManager.MODE_IN_CALL : AudioManager.MODE_IN_COMMUNICATION);
	        audioManager.setSpeakerphoneOn(!isWiredHeadsetOn);

// /*
	    mTvContent = (TextView) findViewById(R.id.text1);		
		mTvContent.setText("");
// */
		mLogin.AddXmppStateObserver(Observer);
		mLogin.DoLogin();   
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	public void XmppStateChange(final EXmppState newState)
	{
		// TODO Auto-generated method stub
	    runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	      	  switch (newState) 
	       	  {						
	       	  case E_XMPP_STATE_START:
	       		mTvContent.setText("正在连接...");
	       		  break;
	       		  
	       	  case E_XMPP_STATE_OPENING:
		       		mTvContent.setText("正在连接...");
	       		  break;
	        		  
	       	  case E_XMPP_STATE_OPEN:
	       		  Log.d(TAG, "XmppStateChange");
// /*	       		  
	       		  mTvContent.setText("等待好友上线...");
// */	       		  
	       		  
	       		  mPresence.AddPresStatusObserver(Observer);
	       		  mPresence.SetPresStatus(true, EPresStatus.E_PRES_SHOW_ONLINE);
	       		  
	       		  mPeerConn.AddPeerConnMsgObserver(Observer);
	       		  break;
	        		  
	       	  case E_XMPP_STATE_CLOSED:
		       		mTvContent.setText("正在关闭...");
	       		  break;
	        		  
	       	  default:
	       		  break;
	       	  }
	       }
	    });			
	}
	
	@Override
	public void PresUpdate(final String strJid, final EPresStatus eShow, final String strStatus, final boolean bAvailable)
	{
	    // TODO Auto-generated method stub
	    runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	    		if (bAvailable)
	    		{
	    			String strContent = "好友已在线,等待呼叫...";
// /*	    			
	    			mTvContent.setText(strContent);
// */
	    			mRoster.add(strJid);
	    		}
	    		else
	    		{
	    			String strContent = "等待好友上线...";
	    			mRoster.remove(strJid);
// /*	    			
	    			mTvContent.setText(strContent);
// */
	    		}
	        }
	    });		
	}
	
	@Override
	public void RecvPeerConnAccept(final String strFrom, final EPeerConnType eType)
	{
		// TODO Auto-generated method stub
	    runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	        	if (eType == EPeerConnType.E_PEERCONN_VOICE_FDX)
	        	{		
	        		IceServer server = new IceServer("stun:120.236.21.178:19302");
	        		List<IceServer> servers = new LinkedList<IceServer>();
	        		servers.add(server);
	        		mPeerConn.Start(EPeerConnType.E_PEERCONN_VOICE_FDX, strFrom, servers, true);
	        	}
	        }
	    });		
	}

	@Override
	public void RecvPeerConnAck(String strFrom)
	{
		// TODO Auto-generated method stub
	    runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	        }
	    });		
	}

	@Override
	public void RecvPeerConnBusy(String strFrom, String strMsg)
	{
		// TODO Auto-generated method stub
	    runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	        }
	    });		
	}

	@Override
	public void RecvPeerConnBye(final String strFrom) 
	{
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
	public void RecvPeerConnInvite(final String strFrom, final EPeerConnType eType) 
	{
		// TODO Auto-generated method stub
	    runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	        	if (eType == EPeerConnType.E_PEERCONN_VOICE_FDX)
	        	{		
	        		mPeerConn.SendPeerConnAccept(strFrom, EPeerConnType.E_PEERCONN_VOICE_FDX);
	        		IceServer server = new IceServer("stun:120.236.21.178:19302");
	        		List<IceServer> servers = new LinkedList<IceServer>();
	        		servers.add(server);
	        		mPeerConn.Start(EPeerConnType.E_PEERCONN_VOICE_FDX, strFrom, servers, false);
	        		mhandler.sendEmptyMessage(0);
	        	}else if(eType == EPeerConnType.E_PEERCONN_VV_FDX){
	        		Point displaySize = new Point();
	        	    getWindowManager().getDefaultDisplay().getSize(displaySize);
	        		mVsv = new VideoStreamsView(MainActivity.this, displaySize);
	        		RelativeLayout main = (RelativeLayout)findViewById(R.id.maina);
	        		main.addView(mVsv);
	        	    mPeerConn.SendPeerConnAccept(strFrom, EPeerConnType.E_PEERCONN_VV_FDX);
	        		IceServer server = new IceServer("stun:120.236.21.178:19302");
//	        		IceServer server = new IceServer("turn:120.236.21.178:3478", "3dinlife", "passwd");
	        		List<IceServer> servers = new LinkedList<IceServer>();
	        		servers.add(server);
//	        		mPeerConn.Start(EPeerConnType.E_PEERCONN_VOICE_FDX, strFrom, servers, false);
	        		mPeerConn.Start(EPeerConnType.E_PEERCONN_VV_FDX, strFrom, servers,
	        				new VideoRenderer(mVsv, VideoStreamsView.Endpoint.LOCAL), 
	        				new VideoRenderer(mVsv, VideoStreamsView.Endpoint.REMOTE), false);
	        	}
	        }
	    });		
	}
	    

	@Override
	public void RecvPeerConnMsg(String strFrom, String strMsg) 
	{
		// TODO Auto-generated method stub
	    runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	        }
	    });		
	}

	@Override
	public void RecvPeerConnReject(String strFrom, String strMsg) 
	{
		// TODO Auto-generated method stub
	    runOnUiThread(new Runnable()
	    {
	        public void run() 
	        {
	        }
	    });		
	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		mPresence.SetPresStatus(false, EPresStatus.E_PRES_SHOW_OFFLINE);
		mPresence.RemovePresStatusObserver();
		
		mPeerConn.RemovePeerConnMsgObserver();
		
		mLogin.RemoveXmppStateObserver();
		mLogin.DoLogout();
		super.onDestroy();
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
