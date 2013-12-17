package com.jiangzhouq.xingwu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginServerActivity extends Activity implements OnClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loginserveractivity);
		Button button = (Button) findViewById(R.id.qrcode_enter);
		button.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		startActivity(new Intent(this, CaptureActivity.class));
	}
}
