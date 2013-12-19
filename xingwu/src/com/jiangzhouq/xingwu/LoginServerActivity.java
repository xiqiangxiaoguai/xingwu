package com.jiangzhouq.xingwu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginServerActivity extends Activity implements OnClickListener{
	private EditText sn_editor;
	private final int REQUEST_CODE_CAPTURE = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loginserveractivity);
		Button qr_button = (Button) findViewById(R.id.qrcode_enter);
		qr_button.setOnClickListener(this);
		Button login_button = (Button) findViewById(R.id.login);
		login_button.setOnClickListener(this);
		sn_editor = (EditText) findViewById(R.id.sn_editor);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.qrcode_enter:
			startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_CODE_CAPTURE);
			break;
		case R.id.login:
			String sn_inputed = sn_editor.getEditableText().toString();
			if(!sn_inputed.isEmpty()){
				Intent intent = new Intent(this, ConnectActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(Constants.BUNDLE_KEY_SN, sn_inputed);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			break;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == REQUEST_CODE_CAPTURE){
				String sn_from_qr = data.getStringExtra(Constants.BUNDLE_KEY_SN);
				if(!sn_from_qr.isEmpty()){
					Intent intent = new Intent(this, ConnectActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString(Constants.BUNDLE_KEY_SN, sn_from_qr);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		}
	}
}
