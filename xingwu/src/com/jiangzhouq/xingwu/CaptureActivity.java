package com.jiangzhouq.xingwu;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.jiangzhouq.zxing.camera.CameraManager;
import com.jiangzhouq.zxing.decode.BitmapLuminanceSource;
import com.jiangzhouq.zxing.decode.CaptureActivityHandler;
import com.jiangzhouq.zxing.decode.RGBLuminanceSource;
import com.jiangzhouq.zxing.decode.ResultHandler;
import com.jiangzhouq.zxing.decode.ResultHandlerFactory;
import com.jiangzhouq.zxing.view.ViewfinderView;

public class CaptureActivity extends Activity implements SurfaceHolder.Callback ,OnClickListener{

	private boolean hasSurface = false;
	private CameraManager cameraManager;
	private ViewfinderView viewfinderView;
	private CaptureActivityHandler handler;
	private BeepManager beepManager;
	private Collection<BarcodeFormat> decodeFormats;
	private String characterSet;
	private AmbientLightManager ambientLightManager;
	private Map<DecodeHintType, ?> decodeHints;
	private boolean flash_state = false;
	private final int REQUEST_CODE_CHOOSER = 0;
	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}
	Handler mhandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what){
			case 0:
				ImageView image = (ImageView) findViewById(R.id.test);
				image.setImageBitmap(scanBitmap);
				break;
			}
		};
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_captureactivity);

		hasSurface = false;
		beepManager = new BeepManager(this);
		ambientLightManager = new AmbientLightManager(this);
		
		Button btn_gallery = (Button) findViewById(R.id.gallery);
		btn_gallery.setOnClickListener(this);
		Button btn_flash = (Button) findViewById(R.id.flash);
		btn_flash.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		cameraManager = new CameraManager(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		handler = null;
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}
		beepManager.updatePrefs();
		ambientLightManager.start(cameraManager);
		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		if(flash_state){
			ambientLightManager.setTorch(false);
			flash_state = false;
		}
		super.onPause();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			if (Constants.LOG_SWITCH)
				Log.d(Constants.LOG_TAG,
						"initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						decodeHints, characterSet, cameraManager);
			}
			// decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			// displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			if (Constants.LOG_SWITCH)
				Log.d(Constants.LOG_TAG,
						"Unexpected error initializing camera", e);
			// displayFrameworkBugMessageAndExit();
		}
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param scaleFactor
	 *            amount by which thumbnail was scaled
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(
				this, rawResult);
		boolean fromLiveScan = barcode != null;
		if (fromLiveScan) {
			beepManager.playBeepSoundAndVibrate();
		}
		if (Constants.LOG_SWITCH)
			Log.d(Constants.LOG_TAG, "test image size:" + barcode.getWidth() + "*" + barcode.getHeight());
		Intent intent = new Intent();
		intent.putExtra(Constants.BUNDLE_KEY_SN, rawResult.getText());
		CaptureActivity.this.setResult(RESULT_OK, intent);
		CaptureActivity.this.finish();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Camera camera = cameraManager.getCamera();
		if(camera != null){
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			Camera.Parameters parameters = camera.getParameters();// 获得相机参数
			Size s = parameters.getPreviewSize();
			double w = s.height;
			double h = s.width;
			if (Constants.LOG_SWITCH)
				Log.d(Constants.LOG_TAG, "camera w:" + w + " h:" + h);
			RelativeLayout.LayoutParams params = null;
			
			if((double)width/w < (double)height/h){
				params = new RelativeLayout.LayoutParams( (int)(height*(w/h)), height);
			}else{
				params = new RelativeLayout.LayoutParams( width, (int)(width*(h/w)));
			}
			cameraManager.initFromCameraParameters(width, height);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			surfaceView.setLayoutParams(params);
			camera.setParameters(parameters);// 设置相机参数
			camera.startPreview();// 开始预览
			if (Constants.LOG_SWITCH)
				Log.d(Constants.LOG_TAG, "surfaceChanged w: " + surfaceView.getWidth() + " h:" + surfaceView.getHeight());
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			if (Constants.LOG_SWITCH)
				Log.d(Constants.LOG_TAG,
						"*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		if (Constants.LOG_SWITCH)
			Log.d(Constants.LOG_TAG, "onresume surfaceView.size:" + surfaceView.getHeight() +" " + surfaceView.getWidth());
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.gallery:
			Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"  
            innerIntent.setType("image/*");  
            Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");  
            this.startActivityForResult(wrapperIntent, REQUEST_CODE_CHOOSER);  
			break;
		case R.id.flash:
			if(flash_state){
				ambientLightManager.setTorch(false);
				flash_state = false;
			}else{
				ambientLightManager.setTorch(true);
				flash_state = true;
			}
			break;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK){  
            switch(requestCode){  
            case REQUEST_CODE_CHOOSER:  
                //获取选中图片的路径  
                Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                if (!cursor.moveToFirst()) {  
                    return;
                }  
                final String  photo_path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));  
                cursor.close();  
                  if (Constants.LOG_SWITCH)
					Log.d(Constants.LOG_TAG, "photo_path:" + photo_path);
//                mProgress = new ProgressDialog(MipcaActivityCapture.this);  
//                mProgress.setMessage("正在扫描...");  
//                mProgress.setCancelable(false);  
//                mProgress.show();  
                  
                new Thread(new Runnable() {  
                    @Override  
                    public void run() {  
                        Result result = scanningImage(photo_path);  
                        if (result != null) {  
//                            Message m = mHandler.obtainMessage();  
//                            m.what = PARSE_BARCODE_SUC;  
//                            m.obj = result.getText();  
//                            mHandler.sendMessage(m);  
                        	if (Constants.LOG_SWITCH)
								Log.d(Constants.LOG_TAG, "result.getText():" + result.getText());
                        	Intent intent = new Intent();
                    		intent.putExtra(Constants.BUNDLE_KEY_SN, result.getText());
                    		CaptureActivity.this.setResult(RESULT_OK, intent);
                    		CaptureActivity.this.finish();
                        } else {  
                        	if (Constants.LOG_SWITCH)
								Log.d(Constants.LOG_TAG, "result == null");
//                            Message m = mHandler.obtainMessage();  
//                            m.what = PARSE_BARCODE_FAIL;  
//                            m.obj = "Scan failed!";  
//                            mHandler.sendMessage(m);  
                        }  
                          
                    }  
                }).start();  
                  
                break;  
              
            }  
		}
		super.onActivityResult(requestCode, resultCode, data);
		
	}
	Bitmap scanBitmap;
	Result scanningImage(String path) {  
		
		 if(TextUtils.isEmpty(path)){  
		        return null;  
		    } 
		 
		MultiFormatReader multiFormatReader = new MultiFormatReader();

		Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(
				2);
		Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
		if (decodeFormats == null || decodeFormats.isEmpty()) {
			decodeFormats = new Vector<BarcodeFormat>();

			decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
			decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
			decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
		}
		hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

		 hints.put(DecodeHintType.CHARACTER_SET, "UTF8");

		multiFormatReader.setHints(hints);
		BitmapFactory.Options options = new BitmapFactory.Options();  
	    options.inJustDecodeBounds = true; // 先获取原大小  
	    scanBitmap = BitmapFactory.decodeFile(path, options);  
	    options.inJustDecodeBounds = false; // 获取新的大小  
	    int sampleSize = (int) (options.outHeight / (float) 200);  
	    if (sampleSize <= 0)  
	        sampleSize = 1;  
	    options.inSampleSize = sampleSize;  
	    scanBitmap = BitmapFactory.decodeFile(path, options); 
	    mhandler.sendEmptyMessage(0);
		Result rawResult = null;
		try {
			rawResult = multiFormatReader
					.decodeWithState(new BinaryBitmap(new HybridBinarizer(
							new BitmapLuminanceSource(scanBitmap))));
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(null != rawResult)
			return rawResult;
		return null;
	}  
	
}
