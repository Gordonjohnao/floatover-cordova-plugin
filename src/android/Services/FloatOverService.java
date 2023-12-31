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
import android.animation.ValueAnimator;
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
import android.net.ConnectivityManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.animation.PropertyValuesHolder;
import android.animation.AnimatorSet;
import android.webkit.JavascriptInterface;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.net.NetworkCapabilities;
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
import android.net.NetworkInfo;
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

    private static final String NEW_TAG = "InternetCheck";
    private static final int CHECK_INTERVAL = 3000; // 1 second interval	 

    private Handler handler = new Handler();
    private int blinkDuration = 500; // Blinking duration in milliseconds
    private int borderColorOriginal;
    private int borderColorBlink;	

    private RoundedImageView roundedImageView;
    private AnimatorSet animatorSet;

   
    private int originalBorderColor;
    private int pulsatingBorderColor;
    private int animationDuration = 1000;// Pulse animation duration in milliseconds

    public static final String ACTION_STOP_SERVICE = "org.apache.cordova.floatOver.Services.STOP_SERVICE";


  	 
	

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
        //borderColorOriginal = Color.BLUE;
        // Set the blinking color manually (for example, a lighter shade of blue)
       // borderColorBlink = Color.parseColor("#d90f23"); // Manually set the color
        // Start the blinking animation
      // if (isInternetActive()) {
        //startBlinkingAnimation();
	 //startPulsatingAnimation();
        //} 
	// Set the original corner radius value manually
        //originalCornerRadius = 30; // Set the desired value in dp or pixels

        // Set the pulsating corner radius value manually
        //pulsatingCornerRadius = 40; // Set the desired value in dp or pixels

        // Start the pulsating animation
       // startPulsatingAnimation();
	/*ImageView iv =  (ImageView) floatOverHead.findViewById(R.id.imageHead);

	ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
	                    iv,
	                    PropertyValuesHolder.ofFloat("scaleX", 1.2f),
	                    PropertyValuesHolder.ofFloat("scaleY", 1.2f));
	scaleDown.setDuration(310);
	
	scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
	scaleDown.setRepeatMode(ObjectAnimator.REVERSE);
	
	scaleDown.start();   */
	RoundedImageView roundedImageView = floatOverHead.findViewById(R.id.imageHead);
	BorderPulseAnimation borderPulseAnimation = new BorderPulseAnimation(roundedImageView);
	borderPulseAnimation.startPulseAnimation();



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
                         //floatOverHead = null;
			 Context context = FloatOverService.this;
                         String packageName = "com.beta23.driverapp";  // Replace with the actual package name

                         openMainApp(context, packageName);
			 //openMainApp()
                         //windowManager.removeView(floatOverView, params_head_view);
                         //showKeyDispatureVisibilty(enable_hardware_back);

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

  

public class BorderPulseAnimation {

    private RoundedImageView roundedImageView;
    private AnimatorSet animatorSet;

    public BorderPulseAnimation(RoundedImageView roundedImageView) {
        this.roundedImageView = roundedImageView;
        this.animatorSet = new AnimatorSet();
    }

    public void startPulseAnimation() {
        int startColor = Color.parseColor("#0000FF");
        int endColor = Color.parseColor("#FF0000");

        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimator.setDuration(1000);
        colorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimator.setRepeatMode(ValueAnimator.REVERSE);

        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatedValue = (int) animator.getAnimatedValue();
                roundedImageView.setBorderColor(animatedValue);
            }
        });

        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(roundedImageView, "scaleX", 1.2f);
        scaleXAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        scaleXAnimator.setRepeatMode(ObjectAnimator.REVERSE);

        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(roundedImageView, "scaleY", 1.2f);
        scaleYAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        scaleYAnimator.setRepeatMode(ObjectAnimator.REVERSE);

        animatorSet.playTogether(colorAnimator, scaleXAnimator, scaleYAnimator);
        animatorSet.start();
    }

    public void stopPulseAnimation() {
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.end();
        }
    }
}
	 
private boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager 
          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
    return true;
}
	 
private boolean isInternetActive() {
    ConnectivityManager connectivityManager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    if (connectivityManager != null) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // For Android 10 (API level 29) and above
            NetworkCapabilities capabilities = connectivityManager
                    .getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
		  
        } else {
            // For Android versions before 10
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
		 
        }
	  
    }

    return false;
}

/*private boolean isInternetActive() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }

        return false;
    }*/	 
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
            // Save the original border color
            borderColorOriginal = Color.BLUE;

            if (isInternetActive()) {
                // Set the blinking color manually (for example, a lighter shade of blue)
                borderColorBlink = Color.parseColor("#d90f23"); // Manually set the color   
                animateBorderColor(borderColorBlink, borderColorOriginal);
            } else {
                // If internet is not available, show only the original color
                animateBorderColor(borderColorOriginal, borderColorOriginal);
            }

            handler.postDelayed(this, blinkDuration);
        }
    }, blinkDuration);
}
    private void startPulsatingAnimation() {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), originalBorderColor, pulsatingBorderColor);
        animator.setDuration(animationDuration);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatedValue = (int) animator.getAnimatedValue();
                applyBorderColor(animatedValue);
            }
        });

        animator.start();
    }

    private void applyBorderColor(int borderColor) {
        GradientDrawable borderDrawable = createBorderDrawable(borderColor);
        imageHead.setBackground(borderDrawable);
    }

    private GradientDrawable createBorderDrawable(int borderColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setStroke(4, borderColor); // Set border width and color manually
        drawable.setCornerRadius(30);
        return drawable;
    }
 

private void animateBorderColor(int fromColor, int toColor) {
    if (isInternetActive()) {
        GradientDrawable borderDrawable = createBorderDrawable(toColor);
        ObjectAnimator animator = ObjectAnimator.ofObject(
                imageHead,
                "borderColor",
                new ArgbEvaluator(),
                toColor,
                toColor
        );
    } else {
        GradientDrawable borderDrawable = createBorderDrawable(toColor);
        ObjectAnimator animator = ObjectAnimator.ofObject(
                imageHead,
                "borderColor",
                new ArgbEvaluator(),
                fromColor,
                fromColor
        );
    }
}



   /* private void animateBorderColor(int fromColor, int toColor) {
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
    }*/

    /*private GradientDrawable createBorderDrawable(int color) {
        GradientDrawable borderDrawable = new GradientDrawable();
        borderDrawable.setShape(GradientDrawable.OVAL);
        borderDrawable.setStroke(4, color); // Adjust border width as needed
        imageHead.setBackground(borderDrawable);
        return borderDrawable;
    }*/

	 
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
	windowManager.removeView(floatOverHead);
        floatOverHead = null;
	handler.removeCallbacksAndMessages(null);
	     
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
