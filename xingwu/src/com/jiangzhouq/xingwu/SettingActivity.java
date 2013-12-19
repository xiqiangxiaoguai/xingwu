package com.jiangzhouq.xingwu;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

public class SettingActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		ActionBar action = getActionBar();
		action.setDisplayHomeAsUpEnabled(true);
		RelativeLayout pw_layout = (RelativeLayout) findViewById(R.id.pw);
		pw_layout.setOnClickListener(this);
		RelativeLayout update_layout  = (RelativeLayout) findViewById(R.id.update);
		update_layout.setOnClickListener(this);
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
		case R.id.pw:
			break;
		case R.id.update:
			UmengUpdateAgent.forceUpdate(this);
			UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
		        @Override
		        public void onUpdateReturned(int updateStatus,UpdateResponse updateInfo) {
		            switch (updateStatus) {
		            case 1: // has no update
		                Toast.makeText(SettingActivity.this, "没有更新", Toast.LENGTH_SHORT)
		                        .show();
		                break;
		            case 3: // time out
		                Toast.makeText(SettingActivity.this, "超时", Toast.LENGTH_SHORT)
		                        .show();
		                break;
		            }
		        }
		});
			break;
		}
	}
}
