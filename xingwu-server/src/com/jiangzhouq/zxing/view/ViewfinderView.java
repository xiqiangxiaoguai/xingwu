/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiangzhouq.zxing.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.jiangzhouq.xingwu.R;
import com.jiangzhouq.zxing.camera.CameraManager;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

  private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
  private static final long ANIMATION_DELAY = 80L;
  private static final int CURRENT_POINT_OPACITY = 0xA0;
  private static final int MAX_RESULT_POINTS = 20;
  private static final int POINT_SIZE = 6;

  private static final int OPAQUE = 0xFF;  
  
  /** 
   * 四个绿色边角对应的长度 
   */  
  private int ScreenRate;  
    
  /** 
   * 四个绿色边角对应的宽度 
   */  
  private static final int CORNER_WIDTH = 10;  
  /** 
   * 扫描框中的中间线的宽度 
   */  
  private static final int MIDDLE_LINE_WIDTH = 6;  
    
  /** 
   * 扫描框中的中间线的与扫描框左右的间隙 
   */  
  private static final int MIDDLE_LINE_PADDING = 5;  
    
  /** 
   * 中间那条线每次刷新移动的距离 
   */  
  private static final int SPEEN_DISTANCE = 6;  
    
  /** 
   * 手机的屏幕密度 
   */  
  private static float density;  
  /** 
   * 字体大小 
   */  
  private static final int TEXT_SIZE = 16;  
  /** 
   * 字体距离扫描框下面的距离 
   */  
  private static final int TEXT_PADDING_TOP = 30;  
    
    
  /** 
   * 中间滑动线的最顶端位置 
   */  
  private int slideTop;  
    
  /** 
   * 中间滑动线的最底端位置 
   */  
  private int slideBottom;  
  
  private CameraManager cameraManager;
  private final Paint paint;
  private Bitmap resultBitmap;
  private final int maskColor;
  private final int resultColor;
  private final int laserColor;
  private final int resultPointColor;
  private int scannerAlpha;
  private List<ResultPoint> possibleResultPoints;
  private List<ResultPoint> lastPossibleResultPoints;
  // This constructor is used when the class is built from an XML resource.
  public ViewfinderView(Context context, AttributeSet attrs) {
    super(context, attrs);
    density = context.getResources().getDisplayMetrics().density;  
    //将像素转换成dp  
    ScreenRate = (int)(20 * density);  
    // Initialize these once for performance rather than calling them every time in onDraw().
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Resources resources = getResources();
    maskColor = resources.getColor(R.color.viewfinder_mask);
    resultColor = resources.getColor(R.color.result_view);
    laserColor = resources.getColor(R.color.viewfinder_laser);
    resultPointColor = resources.getColor(R.color.possible_result_points);
    scannerAlpha = 0;
    possibleResultPoints = new ArrayList<ResultPoint>(5);
    lastPossibleResultPoints = null;
  }
  
  
  public void setCameraManager(CameraManager cameraManager) {
    this.cameraManager = cameraManager;
  }

  @Override
  public void onDraw(Canvas canvas) {
    if (cameraManager == null) {
      return; // not ready yet, early draw before done configuring
    }
    Rect frame = cameraManager.getFramingRect();
    Rect previewFrame = cameraManager.getFramingRectInPreview();    
    if (frame == null || previewFrame == null) {
      return;
    }
    int width = canvas.getWidth();
    
    int height = canvas.getHeight();
    // Draw the exterior (i.e. outside the framing rect) darkened
    paint.setColor(resultBitmap != null ? resultColor : maskColor);
    canvas.drawRect(0, 0, width, frame.top, paint);
    canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
    canvas.drawRect(0, frame.bottom + 1, width, height, paint);
    canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
    if (resultBitmap != null) {
      // Draw the opaque result bitmap over the scanning rectangle
      paint.setAlpha(CURRENT_POINT_OPACITY);
      canvas.drawBitmap(resultBitmap, null, frame, paint);
    } else {

      // Draw a red "laser scanner" line through the middle to show decoding is active
//      paint.setColor(laserColor);
//      paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
//      scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
//      int middle = frame.height() / 2 + frame.top;
//      canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);
      
      float scaleX = frame.width() / (float) previewFrame.width();
      float scaleY = frame.height() / (float) previewFrame.height();

      
      //画扫描框边上的角，总共8个部分  
      paint.setColor(Color.argb(255, 2, 168, 223));  
      canvas.drawRect(frame.left, frame.top - CORNER_WIDTH, frame.left + ScreenRate,  
              frame.top , paint);  
      canvas.drawRect(frame.left - CORNER_WIDTH, frame.top - CORNER_WIDTH, frame.left, frame.top  
              + ScreenRate, paint);  
      canvas.drawRect(frame.right - ScreenRate, frame.top - CORNER_WIDTH, frame.right,  
              frame.top, paint);  
      canvas.drawRect(frame.right, frame.top - CORNER_WIDTH, frame.right + CORNER_WIDTH, frame.top  
              + ScreenRate, paint);  
      canvas.drawRect(frame.left - CORNER_WIDTH, frame.bottom - ScreenRate, frame.left  
              , frame.bottom + CORNER_WIDTH, paint);  
      canvas.drawRect(frame.left, frame.bottom,  
              frame.left + ScreenRate, frame.bottom + CORNER_WIDTH, paint);  
      canvas.drawRect(frame.right - ScreenRate, frame.bottom,  
              frame.right, frame.bottom + CORNER_WIDTH, paint);  
      canvas.drawRect(frame.right , frame.bottom - ScreenRate,  
              frame.right + CORNER_WIDTH, frame.bottom + CORNER_WIDTH, paint);  

        
      //绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE 
      if(slideTop < frame.top){
    	  slideTop = frame.top;
      }
      if(slideTop >= frame.bottom){  
          slideTop = frame.top;  
      }  
      slideTop += SPEEN_DISTANCE; 
      Rect lineRect = new Rect();  
      lineRect.left = frame.left;  
      lineRect.right = frame.right;  
      lineRect.top = slideTop;  
      lineRect.bottom = slideTop + 18;  
      canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.qb_scan_light))).getBitmap(), null, lineRect, paint);   
        

      
      List<ResultPoint> currentPossible = possibleResultPoints;
      List<ResultPoint> currentLast = lastPossibleResultPoints;
      int frameLeft = frame.left;
      int frameTop = frame.top;
      if (currentPossible.isEmpty()) {
        lastPossibleResultPoints = null;
      } else {
        possibleResultPoints = new ArrayList<ResultPoint>(5);
        lastPossibleResultPoints = currentPossible;
        paint.setAlpha(CURRENT_POINT_OPACITY);
        paint.setColor(resultPointColor);
        synchronized (currentPossible) {
          for (ResultPoint point : currentPossible) {
            canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                              frameTop + (int) (point.getY() * scaleY),
                              POINT_SIZE, paint);
          }
        }
      }
      if (currentLast != null) {
        paint.setAlpha(CURRENT_POINT_OPACITY / 2);
        paint.setColor(resultPointColor);
        synchronized (currentLast) {
          float radius = POINT_SIZE / 2.0f;
          for (ResultPoint point : currentLast) {
            canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                              frameTop + (int) (point.getY() * scaleY),
                              radius, paint);
          }
        }
      }

      // Request another update at the animation interval, but only repaint the laser line,
      // not the entire viewfinder mask.
      postInvalidateDelayed(ANIMATION_DELAY,
                            frame.left - POINT_SIZE,
                            frame.top - POINT_SIZE,
                            frame.right + POINT_SIZE,
                            frame.bottom + POINT_SIZE);
    }
  }

  public void drawViewfinder() {
    Bitmap resultBitmap = this.resultBitmap;
    this.resultBitmap = null;
    if (resultBitmap != null) {
      resultBitmap.recycle();
    }
    invalidate();
  }

  /**
   * Draw a bitmap with the result points highlighted instead of the live scanning display.
   *
   * @param barcode An image of the decoded barcode.
   */
  public void drawResultBitmap(Bitmap barcode) {
    resultBitmap = barcode;
    invalidate();
  }

  public void addPossibleResultPoint(ResultPoint point) {
    List<ResultPoint> points = possibleResultPoints;
    synchronized (points) {
      points.add(point);
      int size = points.size();
      if (size > MAX_RESULT_POINTS) {
        // trim it
        points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
      }
    }
  }

}
