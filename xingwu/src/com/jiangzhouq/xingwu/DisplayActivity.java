package com.jiangzhouq.xingwu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class DisplayActivity extends Activity {

	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case 0:
				startActivity(new Intent(DisplayActivity.this, LoginServerActivity.class));
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display);
		mHandler.sendEmptyMessageDelayed(0, 2000);
	}
}
