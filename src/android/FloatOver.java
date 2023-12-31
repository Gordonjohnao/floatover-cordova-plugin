package com.test.cordova.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class FloatOver extends CordovaPlugin {

    private WindowManager windowManager;
    private View floatingView;
    private ImageView floatingIcon;
    private boolean isBlinking = false;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("showFloatingIcon")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showFloatingIcon();
                    startBlinking();
                }
            });
            callbackContext.success();
            return true;
        }
        return false;
    }

    private void showFloatingIcon() {
        if (windowManager == null) {
            windowManager = (WindowManager) cordova.getActivity().getSystemService(Context.WINDOW_SERVICE);

            LayoutInflater inflater = (LayoutInflater) cordova.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            floatingView = inflater.inflate(R.layout.floating_icon_layout, null);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                            WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT
            );

            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = 100;

            floatingIcon = floatingView.findViewById(R.id.floatingIcon);
            // Set click listener to open the main app
            floatingIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openMainApp();
                }
            });

            // Set touch listener for drag-and-drop
            floatingIcon.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(floatingView, params);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            windowManager.addView(floatingView, params);
        }
    }

    private void startBlinking() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isInternetConnected()) {
                    if (isBlinking) {
                        floatingIcon.setVisibility(View.VISIBLE);
                    } else {
                        floatingIcon.setVisibility(View.INVISIBLE);
                    }
                    isBlinking = !isBlinking;
                    handler.postDelayed(this, 500); // Blink every 500 milliseconds
                } else {
                    // If no internet, stop blinking
                    floatingIcon.setVisibility(View.VISIBLE);
                }
            }
        }, 0);
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) cordova.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private void openMainApp() {
        // Launch the main Cordova activity
        Context context = cordova.getActivity().getApplicationContext();
        Intent intent = new Intent(context, CordovaActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (windowManager != null && floatingView != null) {
            windowManager.removeView(floatingView);
            windowManager = null;
            floatingView = null;
        }
    }
}
