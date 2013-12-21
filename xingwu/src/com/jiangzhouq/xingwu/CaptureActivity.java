package com.jiangzhouq.xingwu;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.jiangzhouq.zxing.camera.CameraManager;
import com.jiangzhouq.zxing.decode.CaptureActivityHandler;
import com.jiangzhouq.zxing.decode.ResultHandler;
import com.jiangzhouq.zxing.decode.ResultHandlerFactory;
import com.jiangzhouq.zxing.view.ViewfinderView;

public class CaptureActivity extends Activity implements SurfaceHolder.Callback {

	private boolean hasSurface = false;
	private CameraManager cameraManager;
	private ViewfinderView viewfinderView;
	private CaptureActivityHandler handler;
	private BeepManager beepManager;
	private Collection<BarcodeFormat> decodeFormats;
	private String characterSet;
	private AmbientLightManager ambientLightManager;
	private Map<DecodeHintType, ?> decodeHints;
	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}
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
}
