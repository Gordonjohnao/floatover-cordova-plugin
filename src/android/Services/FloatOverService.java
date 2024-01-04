package org.apache.cordova.floatOver.Services;

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.webkit.JavascriptInterface;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import android.app.Activity;
import android.content.pm.PackageManager;

import org.apache.cordova.floatOver.Services.ServiceParameters;
import org.apache.cordova.floatOver.GeneralUtils.KeyDispatchLayout;


import java.util.Date;

/**
 * Created by Mohamed Sayed .
 */

 public class FloatOverService extends Service {

     String TAG = getClass().getSimpleName();

     private WindowManager windowManager;
     WindowManager.LayoutParams params_head_float,params_head_view,params_key_dispature;
     private int layout_param_type;
     LayoutInflater inflater;

     private View floatOverHead,floatOverView;
     ImageView imgClose;
     WebView webView;
     ImageView imageHead;
     ServiceParameters serviceParameters;
     private GestureDetector gestureDetector;
     private Context mContext;

    private Handler handler = new Handler();
    private int blinkDuration = 500; // Blinking duration in milliseconds
    private int borderColorOriginal;
    private int borderColorBlink;	 

   @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getStringExtra("action");

            if (action != null && action.equals("closeFloatService")) {
                closeFloatService();
            }
        }

        // Continue with your service logic

        return START_STICKY;
    }

    private void closeFloatService() {
      stopSelf();
      try {
	     if (floatOverView != null) windowManager.removeView(floatOverView);
	     if (floatOverHead != null) windowManager.removeView(floatOverHead);
	 }catch (Exception e){
	     e.printStackTrace();
	 }
        
    }

    // Other methods and code for your service


     @Override
     public IBinder onBind(Intent intent) {
         // Not used
         return null;
     }


     @Override
     public void onCreate() {
         super.onCreate();
         Log.d(TAG,"onCreate");

         gestureDetector = new GestureDetector(this, new SingleTapConfirm());

         serviceParameters = new ServiceParameters(this);
         animator = new MoveAnimator();
         windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

         inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
         floatOverHead = inflater.inflate(R.layout.service_over_apps_head, null, false);
         floatOverView = inflater.inflate(R.layout.service_over_apps_view, null, false);
         webView = (WebView) floatOverView.findViewById(R.id.webView);
         imageHead = (ImageView) floatOverHead.findViewById(R.id.imageHead);
	
	 //startBlinkingAnimation();
	// Save the original border color
	 borderColorOriginal = Color.BLUE;
        // Set the blinking color manually (for example, a lighter shade of blue)
        borderColorBlink = Color.parseColor("#d90f23"); // Manually set the color
        // Start the blinking animation
	 if (isInternetActive()) {
            // Start the wave animation
             startBlinkingAnimation();
        }
         imgClose = (ImageView) floatOverView.findViewById(R.id.imgClose);
         imgClose.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 stopSelf();
                 try {
                     if (floatOverView != null) windowManager.removeView(floatOverView);
                     if (floatOverHead != null) windowManager.removeView(floatOverHead);
                 }catch (Exception e){
                     e.printStackTrace();
                 }
             }
         });

	   
         webViewSettings();

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  
            layout_param_type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
         } else {
            layout_param_type = WindowManager.LayoutParams.TYPE_PHONE;
         }

         params_head_float = new WindowManager.LayoutParams(
                 WindowManager.LayoutParams.WRAP_CONTENT,
                 WindowManager.LayoutParams.WRAP_CONTENT,
                 layout_param_type,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                 PixelFormat.TRANSLUCENT);
         params_head_float.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
         
         params_head_view = new WindowManager.LayoutParams();
         params_head_view.width = WindowManager.LayoutParams.WRAP_CONTENT;
         params_head_view.height = WindowManager.LayoutParams.WRAP_CONTENT;
         params_head_view.type = layout_param_type;
         params_head_view.format = PixelFormat.TRANSLUCENT;
                
         adjustWebViewGravity();

         Boolean has_head = serviceParameters.getBoolean("has_head",true);
         final Boolean enable_hardware_back = serviceParameters.getBoolean("enable_hardware_back",true);
         if (has_head) {
           windowManager.addView(floatOverHead, params_head_float);
           showKeyDispatureVisibilty(false);
         }else {
           windowManager.addView(floatOverView, params_head_view);
           
           showKeyDispatureVisibilty(enable_hardware_back);
         }

         floatOverHead.setOnTouchListener(new View.OnTouchListener() {
             private int initialX;
             private int initialY;
             private float initialTouchX;
             private float initialTouchY;

             @Override
             public boolean onTouch(View v,MotionEvent event) {Log.d("TAG","onTouch ... Click");
                 if (event != null) {
                     if (gestureDetector.onTouchEvent(event)) {
                         // ....  click on the whole over app head event
                         Log.d("TAG","Click");
                         windowManager.removeView(floatOverHead);
                         floatOverHead = null;
                         windowManager.addView(floatOverView, params_head_view);
                         showKeyDispatureVisibilty(enable_hardware_back);

                         Log.d("TAG","Click");
                     }else {
                         switch (event.getAction()) {
                             case MotionEvent.ACTION_DOWN:
                                 Log.d("TAG","ACTION_DOWN");
                                 initialX = params_head_float.x;
                                 initialY = params_head_float.y;
                                 initialTouchX = event.getRawX();
                                 initialTouchY = event.getRawY();
                                 updateSize();
                                 animator.stop();

                                 v.animate().scaleXBy(-0.1f).setDuration(100).start();
                                 v.animate().scaleYBy(-0.1f).setDuration(100).start();
                                 break;
                             case MotionEvent.ACTION_MOVE:
                                 Log.d("TAG","ACTION_MOVE");
                                 int x = initialX - (int) (event.getRawX() - initialTouchX);
                                 int y = initialY + (int) (event.getRawY() - initialTouchY);
                                 params_head_float.x = x;
                                 params_head_float.y = y;
                                 windowManager.updateViewLayout(floatOverHead, params_head_float);
                                 break;
                             case MotionEvent.ACTION_UP:
                                 Boolean drag_to_side = serviceParameters.getBoolean("drag_to_side",true);
                                 if (drag_to_side) {
                                    goToWall();
                                 }
                                 Log.d("TAG","ACTION_UP");
                                 v.animate().cancel();
                                 v.animate().scaleX(1f).setDuration(100).start();
                                 v.animate().scaleY(1f).setDuration(100).start();
                                 break;
                         }
                     }
                 }
                 return false;
             }
         });
     }
 public static void openMainApp(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            // The app with the specified package name is not installed
            // Handle this case as needed
        }
    }
	 
// this is for wave animation

private void startWaveAnimation() {
        // Create a wave animation
        Animation waveAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f
        );
        waveAnimation.setDuration(1000); // Duration of the wave animation in milliseconds
        waveAnimation.setInterpolator(new LinearInterpolator());
        waveAnimation.setRepeatCount(Animation.INFINITE); // Infinite animation

        // Set the animation listener to handle animation events
        waveAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Animation started
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Animation ended
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Animation repeated
            }
        });

        // Start the animation
        imageHead.startAnimation(waveAnimation);
    }

    private boolean isInternetActive() {
        // Implement your internet connectivity check here
        // Return true if the internet is active, false otherwise
        return true;
    }

// this is for blinking functionality
	/*private void startBlinkingAnimation() {
        // Create a blinking animation
        Animation blinkAnimation = new AlphaAnimation(1, 0);
        blinkAnimation.setDuration(500); // Duration of each blink in milliseconds
        blinkAnimation.setInterpolator(new LinearInterpolator());
        blinkAnimation.setRepeatCount(Animation.INFINITE); // Infinite blinking

        // Set the animation listener to handle animation events
        blinkAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Animation started
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Animation ended
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Animation repeated
            }
        });

        // Start the animation
        imageHead.startAnimation(blinkAnimation);
    }*/
private void startBlinkingAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateBorderColor(borderColorBlink, borderColorOriginal);
                handler.postDelayed(this, blinkDuration);
            }
        }, blinkDuration);
    }

    private void animateBorderColor(int fromColor, int toColor) {
        GradientDrawable borderDrawable = createBorderDrawable(toColor);
        ObjectAnimator animator = ObjectAnimator.ofObject(
                imageHead,
                "borderColor",
                new ArgbEvaluator(),
                fromColor,
                toColor
        );

        animator.setDuration(blinkDuration / 2); // Blinking speed
        animator.start();
    }

    private GradientDrawable createBorderDrawable(int color) {
        GradientDrawable borderDrawable = new GradientDrawable();
        borderDrawable.setShape(GradientDrawable.OVAL);
        borderDrawable.setStroke(4, color); // Adjust border width as needed
        imageHead.setBackground(borderDrawable);
        return borderDrawable;
    }

	 
     @Override
     public void onStart(Intent intent, int startId) {
         super.onStart(intent, startId);
         String file_path = serviceParameters.getString("file_path");
       //  String file_path = intent.getExtras().getString("file_path");
         webView.loadUrl(file_path);
     }

     @Override
     public void onDestroy() {
         super.onDestroy();
	 imageHead.clearAnimation();
         try {
             if (floatOverView != null) {
               windowManager.removeView(floatOverView);
             }
             showKeyDispatureVisibilty(false);
         }catch (Exception e){
             e.printStackTrace();
         }

     }

     private KeyDispatchLayout rlKeyDispature;
     private View keyDispatureView;
     public void showKeyDispatureVisibilty(boolean visible) {

         if (keyDispatureView ==null)
         {
             keyDispatureView = inflater.inflate(R.layout.key_dispature, null, false);
         }
         rlKeyDispature = (KeyDispatchLayout) keyDispatureView.findViewById(R.id.tab_left);

         if (visible)
         {
             params_key_dispature = new WindowManager.LayoutParams(
                     WindowManager.LayoutParams.WRAP_CONTENT,
                     WindowManager.LayoutParams.WRAP_CONTENT,
                     layout_param_type,
                     WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                     PixelFormat.TRANSLUCENT);
             params_key_dispature.gravity = Gravity.CENTER;

             //This one is necessary.
             params_key_dispature.type = layout_param_type;
             // Play around with these two.
             params_key_dispature.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

             try
             {
                 windowManager.removeView(rlKeyDispature);
             }
             catch (Exception e){}

             windowManager.addView(rlKeyDispature, params_key_dispature);
             Log.d(TAG, "Key DISPATURE -- ADDED");
         }
         else
         {
             try
             {
                 windowManager.removeView(rlKeyDispature);
                 Log.d(TAG, "Key DISPATURE -- REMOVED");
             }
             catch (Exception e){}
         }

     }

     public void webViewSettings() {

               webView.setBackgroundColor(Color.TRANSPARENT);
	       //webView.addJavascriptInterface(new WebAppInterface(this), "FloatOver");
               WebSettings webSettings = webView.getSettings();
               //webSettings.setJavaScriptEnabled(true);
               //webSettings.setAppCacheMaxSize(10 * 1024 * 1024); // 10MB
               //webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
               //webSettings.setAllowFileAccess(true);
               //webSettings.setDomStorageEnabled(true);
               //webSettings.setAppCacheEnabled(true);
               try {
                   Log.d(TAG, "Enabling HTML5-Features");
                   Method m1 = WebSettings.class.getMethod("setDomStorageEnabled", new Class[]{Boolean.TYPE});
                   m1.invoke(webView.getSettings(), Boolean.TRUE);

                   Method m2 = WebSettings.class.getMethod("setDatabaseEnabled", new Class[]{Boolean.TYPE});
                   m2.invoke(webView.getSettings(), Boolean.TRUE);

                   Method m3 = WebSettings.class.getMethod("setDatabasePath", new Class[]{String.class});
                   m3.invoke(webView.getSettings(), "/data/data/" + getPackageName() + "/databases/");

                   Method m4 = WebSettings.class.getMethod("setAppCacheMaxSize", new Class[]{Long.TYPE});
                   m4.invoke(webView.getSettings(), 1024 * 1024 * 8);

                   Method m5 = WebSettings.class.getMethod("setAppCachePath", new Class[]{String.class});
                   m5.invoke(webView.getSettings(), "/data/data/" + getPackageName() + "/cache/");

                   Method m6 = WebSettings.class.getMethod("setAppCacheEnabled", new Class[]{Boolean.TYPE});
                   m6.invoke(webView.getSettings(), Boolean.TRUE);

                   Log.d(TAG, "Enabled HTML5-Features");
               } catch (NoSuchMethodException e) {
                   Log.e(TAG, "Reflection fail", e);
               } catch (InvocationTargetException e) {
                   Log.e(TAG, "Reflection fail", e);
               } catch (IllegalAccessException e) {
                   Log.e(TAG, "Reflection fail", e);
               }

              Boolean enable_close_btn = serviceParameters.getBoolean("enable_close_btn",true);
              if (enable_close_btn) {
                  imgClose.setVisibility(View.VISIBLE);
              }else {
                  imgClose.setVisibility(View.GONE);
              }

     }


     public void goToWall() {

            int middle = width / 2;
            float nearestXWall = params_head_float.x >= middle ? width : 0;
            animator.start(nearestXWall, params_head_float.y);

    }

     public void adjustWebViewGravity(){
       String vertical_position = serviceParameters.getString("vertical_position");
       String horizontal_position = serviceParameters.getString("horizontal_position");
       String position = vertical_position + "_" + horizontal_position;

       if(position.equals("top_right")) {
            params_head_view.gravity = Gravity.TOP | Gravity.RIGHT;
        }else if (position.equals("top_center")) {
            params_head_view.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        }else if (position.equals("top_left")) {
            params_head_view.gravity = Gravity.TOP | Gravity.LEFT;
        }else if (position.equals("center_right")) {
            params_head_view.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        }else if (position.equals("center_center")) {
            params_head_view.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        }else if (position.equals("center_left")) {
            params_head_view.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        }else if (position.equals("bottom_right")) {
            params_head_view.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        }else if (position.equals("bottom_center")) {
            params_head_view.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        }else if (position.equals("bottom_left")) {
            params_head_view.gravity = Gravity.BOTTOM | Gravity.LEFT;
        }else {
            params_head_view.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        }

     }

     MoveAnimator animator;
     int width;
     private void updateSize() {
         DisplayMetrics metrics = new DisplayMetrics();
         windowManager.getDefaultDisplay().getMetrics(metrics);
         Display display = windowManager.getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
         width = (size.x - floatOverHead.getWidth());

     }

     private void move(float deltaX, float deltaY) {
         params_head_float.x += deltaX;
         params_head_float.y += deltaY;
         windowManager.updateViewLayout(floatOverHead, params_head_float);
     }

     private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

         @Override
         public boolean onSingleTapUp(MotionEvent event) {
             return true;
         }
     }

     private class MoveAnimator implements Runnable {
         private Handler handler = new Handler(Looper.getMainLooper());
         private float destinationX;
         private float destinationY;
         private long startingTime;

         private void start(float x, float y) {
             this.destinationX = x;
             this.destinationY = y;
             startingTime = System.currentTimeMillis();
             handler.post(this);
         }

         @Override
         public void run() {
             if (floatOverHead.getRootView() != null && floatOverHead.getRootView().getParent() != null) {
                 float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / 400f);
                 float deltaX = (destinationX -  params_head_float.x) * progress;
                 float deltaY = (destinationY -  params_head_float.y) * progress;
                 move(deltaX, deltaY);
                 if (progress < 1) {
                     handler.post(this);
                 }
             }
         }

         private void stop() {
             handler.removeCallbacks(this);
         }
     }

     public class WebAppInterface {
  		Context mContext;

  		/** Instantiate the interface and set the context */
  		public WebAppInterface(Context c) {
  			mContext = c;
  		}

  		/** Close from inside web view  */
  		@JavascriptInterface
  		public void closeWebView() {
  		    Log.d("TAG","Click");
          stopSelf();
          try {
              if (floatOverView != null) windowManager.removeView(floatOverView);
              if (floatOverHead != null) windowManager.removeView(floatOverHead);
          }catch (Exception e){
              e.printStackTrace();
          }
  		}

      @JavascriptInterface
      public void openApp(){
        //mContext.startActivity(new Intent(mContext,com.beta23.driverapp.MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      }
  }
 }
