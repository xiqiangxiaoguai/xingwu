package com.jiangzhouq.xingwu;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class DisplayActivity extends Activity {

	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case 0:
				SharedPreferences settings = getSharedPreferences("sn", MODE_PRIVATE);

				if (getPwState() == PwActivity.STATE_OLD) {
					startActivity(new Intent(DisplayActivity.this,PwLoginActivity.class));
				} else {
					if (settings.contains("sn")) {
						if (!settings.getString("sn", "").equals("")) {
							Intent intent = new Intent(DisplayActivity.this,ConnectActivity.class);
							Bundle bundle = new Bundle();
							bundle.putString(Constants.BUNDLE_KEY_SN,settings.getString("sn", ""));
							intent.putExtras(bundle);
							startActivity(intent);
						}
					} else {
						startActivity(new Intent(DisplayActivity.this,LoginServerActivity.class));
					}
				}
				finish();
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display);
		mHandler.sendEmptyMessageDelayed(0, 1000);
	}
	
	int getPwState() {
		int STATE = PwActivity.STATE_FIRST;
		if (Constants.LOG_SWITCH)
			Log.d(Constants.LOG_TAG, "getPwState()");
		SharedPreferences settings = getSharedPreferences("pw", MODE_PRIVATE);
		if (settings.contains("pw")) {
			if (!settings.getString("pw", "").equals("")) {
				STATE = PwActivity.STATE_OLD;
			}
		}
		return STATE;
	}
}
