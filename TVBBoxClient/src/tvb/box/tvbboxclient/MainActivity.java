package tvb.box.tvbboxclient;

import java.util.LinkedList;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import tvb.boxclient.Login;
import tvb.boxclient.Login.EXmppState;
import tvb.boxclient.Presence;
import tvb.boxclient.Presence.EPresStatus;

public class MainActivity extends Activity 
implements Login.IXmppStateObserver
, Presence.IPresenceStatusObserver
{
	private static String TAG = "TVBBoxClient";
	
	private final Login          mLogin          = new Login("TVBBox1", "TVBBox1", "120.236.21.179", "TVBBoxClient");
	private final Presence       mPresence       = new Presence();
	private MainActivity         Observer        = this;
	private LinkedList<String>   mRoster         = new LinkedList<String>();
	private TextView             mTvContent;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTvContent = (TextView) findViewById(R.id.tv_content);		
		mTvContent.setText("");
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

	@Override
	public void XmppStateChange(EXmppState newState)
	{
		// TODO Auto-generated method stub
		switch (newState) 
		{						
		case E_XMPP_STATE_START:
			break;
			
		case E_XMPP_STATE_OPENING:
			break;

		case E_XMPP_STATE_OPEN:
            Log.d(TAG, "XmppStateChange");
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
	
	@Override
	public void PresUpdate(final String strJid, final EPresStatus eShow, final String strStatus, final boolean bAvailable)
	{
	      // TODO Auto-generated method stub
	      runOnUiThread(new Runnable()
	      {
	          public void run() 
	          {
	      		mTvContent = (TextView) findViewById(R.id.tv_content);		
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
	          }
	        });		
	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		mPresence.SetPresStatus(false, EPresStatus.E_PRES_SHOW_OFFLINE);
		mPresence.RemovePresStatusObserver();
		mLogin.RemoveXmppStateObserver();
		mLogin.DoLogout();
		super.onDestroy();
	}	
}
