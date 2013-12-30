package com.jiangzhouq.xingwu;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PwManagerActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pw_manager);
		ActionBar action = getActionBar();
		action.setDisplayHomeAsUpEnabled(true);
	}
	@Override
	protected void onResume() {
		RelativeLayout pw_change  = (RelativeLayout) findViewById(R.id.pw_change);
		pw_change.setOnClickListener(this);
		RelativeLayout pw_cancel = (RelativeLayout) findViewById(R.id.pw_cancel);
		if(getPwState() == PwActivity.STATE_FIRST){
			((TextView)pw_change.getChildAt(0)).setText(R.string.pw_create);
			((TextView)pw_cancel.getChildAt(0)).setTextColor(getResources().getColor(R.color.bg_item_border));
			pw_cancel.setEnabled(false);
		}else{
			pw_cancel.setEnabled(true);
			((TextView)pw_change.getChildAt(0)).setText(R.string.pw_change);
			((TextView)pw_cancel.getChildAt(0)).setTextColor(getResources().getColor(android.R.color.black));
			pw_cancel.setOnClickListener(this);
		}
		super.onResume();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		}
		return true;
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.pw_cancel:
			startActivity(new Intent(this, PwCancelActivity.class));
			break;
		case R.id.pw_change:
			startActivity(new Intent(this, PwActivity.class));
			break;
		}
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
