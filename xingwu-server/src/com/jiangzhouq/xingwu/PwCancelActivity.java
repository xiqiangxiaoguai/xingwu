package com.jiangzhouq.xingwu;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PwCancelActivity extends Activity implements OnClickListener{

	public static final int STATE_FIRST = 0;
	public static final int STATE_FIRST_CONFIRM = 1;
	public static final int STATE_OLD = 2;
	public static final int STATE_NEW =3;
	public static final int STATE_NEW_CONFIRM = 4;
	
	private int STATE = 0;
	Handler mhandler = new Handler(){};
	private StringBuilder sb = new StringBuilder();
	LinearLayout pwItems;
	private String pwOld = "";
	private TextView stateShow;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pw_settings);
		setpPwListener();
		pwItems = (LinearLayout) findViewById(R.id.pw_items);
		stateShow = (TextView) findViewById(R.id.state);
		getPwState();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	int getPwState() {
		if (Constants.LOG_SWITCH)
			Log.d(Constants.LOG_TAG, "getPwState()");
		SharedPreferences settings = getSharedPreferences("pw", MODE_PRIVATE);
		if (settings.contains("pw")) {
			if (!settings.getString("pw", "").equals("")) {
				pwOld = settings.getString("pw", "");
				STATE = STATE_OLD;
				stateShow.setText(R.string.pw_old);
				if (Constants.LOG_SWITCH)
					Log.d(Constants.LOG_TAG, "->STATE_OLD");
			}
		} else {
			STATE = STATE_FIRST;
			if (Constants.LOG_SWITCH)
				Log.d(Constants.LOG_TAG, "->STATE_FIRST");
		}
		return STATE;
	}
	
	void updatePwState(){
		if (Constants.LOG_SWITCH)
			Log.d(Constants.LOG_TAG, "updatePwState()");
		if(sb.length() == 4){
			switch(STATE){
			case STATE_FIRST:
				STATE = STATE_FIRST_CONFIRM;
				pwOld = sb.toString();
				stateShow.setText(R.string.pw_first_confirm);
				sb.delete(0, sb.length());
				invalidatePwItems();
				if (Constants.LOG_SWITCH)
					Log.d(Constants.LOG_TAG, "STATE_FIRST->STATE_FIRST_CONFIRM");
				break;
			case STATE_FIRST_CONFIRM:
				if(sb.toString().equals(pwOld)){
					SharedPreferences settings = getSharedPreferences("pw", MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("pw", pwOld);
					editor.commit();
					finish();
					if (Constants.LOG_SWITCH)
						Log.d(Constants.LOG_TAG, "STATE_FIRST_CONFIRM->finish()");
				}else{
					STATE = STATE_FIRST;
					stateShow.setText(R.string.pw_first);
					sb.delete(0, sb.length());
					invalidatePwItems();
					if (Constants.LOG_SWITCH)
						Log.d(Constants.LOG_TAG, "STATE_FIRST_CONFIRM->STATE_FIRST");
				}
				break;
			case STATE_OLD:
				if(sb.toString().equals(pwOld)){
					SharedPreferences settings = getSharedPreferences("pw", MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("pw", "");
					editor.commit();
					finish();
					if (Constants.LOG_SWITCH)
						Log.d(Constants.LOG_TAG, "STATE_OLD->finish()");
				}else{
					sb.delete(0, sb.length());
					invalidatePwItems();
					if (Constants.LOG_SWITCH)
						Log.d(Constants.LOG_TAG, "STATE_OLD->STATE_OLD");
				}
				break;
			case STATE_NEW:
				STATE = STATE_NEW_CONFIRM;
				pwOld = sb.toString();
				stateShow.setText(R.string.pw_new_confirm);
				sb.delete(0, sb.length());
				invalidatePwItems();
				if (Constants.LOG_SWITCH)
					Log.d(Constants.LOG_TAG, "STATE_NEW->STATE_NEW_CONFIRM");
				break;
			case STATE_NEW_CONFIRM:
				if(sb.toString().equals(pwOld)){
					SharedPreferences settings = getSharedPreferences("pw", MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("pw", pwOld);
					editor.commit();
					if (Constants.LOG_SWITCH)
						Log.d(Constants.LOG_TAG, "STATE_NEW_CONFIRM->finish()");
					finish();
				}else{
					STATE = STATE_NEW;
					stateShow.setText(R.string.pw_new);
					sb.delete(0, sb.length());
					invalidatePwItems();
					if (Constants.LOG_SWITCH)
						Log.d(Constants.LOG_TAG, "STATE_NEW_CONFIRM->STATE_NEW");
				}
				break;
			}
			if (Constants.LOG_SWITCH)
				Log.d(Constants.LOG_TAG, "pwOld:" + pwOld);
		}
	}
	
	void invalidatePwItems(){
		int pwItemsCount = pwItems.getChildCount();
		int sbCount = sb.length();
		for (int i = 0; i < pwItemsCount; i++){
			if(i < sbCount){
				((ImageView)pwItems.getChildAt(i)).setImageResource(R.drawable.ic_launcher);
			}else{
				((ImageView)pwItems.getChildAt(i)).setImageBitmap(null);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.one:
			sb.append(1);
			break;
		case R.id.two:
			sb.append(2);
			break;
		case R.id.three:
			sb.append(3);
			break;
		case R.id.four:
			sb.append(4);
			break;
		case R.id.five:
			sb.append(5);
			break;
		case R.id.six:
			sb.append(6);
			break;
		case R.id.seven:
			sb.append(7);
			break;
		case R.id.eight:
			sb.append(8);
			break;
		case R.id.nine:
			sb.append(9);
			break;
		case R.id.zero:
			sb.append(0);
			break;
		case R.id.del:
			if(sb.length() > 0)
				sb.deleteCharAt(sb.length() -1);
			break;
		}
		if (Constants.LOG_SWITCH)
			Log.d(Constants.LOG_TAG, "sb:" + sb);
		invalidatePwItems();
		if(sb.length() == 4){
			updatePwState();
		}
	}
	
	void setpPwListener(){
		Button button_one = (Button)findViewById(R.id.one);
		button_one.setOnClickListener(this);
		Button button_two = (Button)findViewById(R.id.two);
		button_two.setOnClickListener(this);
		Button button_three = (Button)findViewById(R.id.three);
		button_three.setOnClickListener(this);
		Button button_four = (Button)findViewById(R.id.four);
		button_four.setOnClickListener(this);
		Button button_five = (Button)findViewById(R.id.five);
		button_five.setOnClickListener(this);
		Button button_six = (Button)findViewById(R.id.six);
		button_six.setOnClickListener(this);
		Button button_seven = (Button)findViewById(R.id.seven);
		button_seven.setOnClickListener(this);
		Button button_eight = (Button)findViewById(R.id.eight);
		button_eight.setOnClickListener(this);
		Button button_nine = (Button)findViewById(R.id.nine);
		button_nine.setOnClickListener(this);
		Button button_zero = (Button)findViewById(R.id.zero);
		button_zero.setOnClickListener(this);
		Button button_del = (Button)findViewById(R.id.del);
		button_del.setOnClickListener(this);
	}
}
