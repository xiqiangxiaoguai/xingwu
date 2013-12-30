package com.jiangzhouq.xingwu;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class LoginServerActivity extends Activity implements OnClickListener,TextWatcher{
	private EditText sn_editor;
	private ImageView user_state;
	private ImageView content_del;
	private final int REQUEST_CODE_CAPTURE = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loginserveractivity);
		ImageView qr_button = (ImageView) findViewById(R.id.qrcode_enter);
		qr_button.setOnClickListener(this);
		Button login_button = (Button) findViewById(R.id.login);
		login_button.setOnClickListener(this);
		sn_editor = (EditText) findViewById(R.id.sn_editor);
		sn_editor.addTextChangedListener(this);
		user_state = (ImageView) findViewById(R.id.user_state);
		content_del = (ImageView) findViewById(R.id.content_del);
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
				writePreference(sn_inputed);
				Intent intent = new Intent(this, ConnectActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(Constants.BUNDLE_KEY_SN, sn_inputed);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
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
					writePreference(sn_from_qr);
					Intent intent = new Intent(this, ConnectActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString(Constants.BUNDLE_KEY_SN, sn_from_qr);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();
				}
			}
		}
	}
	private void writePreference(String str){
		SharedPreferences settings = getSharedPreferences("sn", MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("sn", str);
		editor.commit();
	}

	@Override
	public void afterTextChanged(Editable s) {
		if(s.toString().length() > 0){
			user_state.setImageResource(R.drawable.login_user_hightlighted);
			content_del.setVisibility(View.VISIBLE);
		}else{
			user_state.setImageResource(R.drawable.login_user);
			content_del.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}
}
