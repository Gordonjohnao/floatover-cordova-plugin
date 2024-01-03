package org.apache.cordova.floatOver;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.Manifest;
//import android.support.v4.app.ActivityCompat;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;
import android.net.Uri;

import org.apache.cordova.floatOver.Services.FloatOverService;
import org.apache.cordova.floatOver.Services.ServiceParameters;


public class FloatOver extends CordovaPlugin {
	private static final String TAG = "FloatOver";
	private static final String ACTION_OPEN_OVER_APP_VIEW = "open";
	private static final String ACTION_CLOSE_OVER_APP_VIEW = "close";
	private static final String ACTION_CHECK_OVER_APP_PERMISSION = "checkPermission";
	private static final String FOLDER_PATH = "file:///android_asset/www/";
	public Activity activity;
	public ServiceParameters serviceParameters;
	CallbackContext callback;
	public FloatOver() {
		super();
	}

	@Override
	public boolean execute(String action, JSONArray options,
	final CallbackContext callbackContext) throws JSONException {
		activity = this.cordova.getActivity();
		serviceParameters = new ServiceParameters(activity);

		if (action.equals(ACTION_CHECK_OVER_APP_PERMISSION)) {

		if (Build.VERSION.SDK_INT >= 23) {
				if (!checkDrawOverAppsPermission(activity)) {
						Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:" + activity.getPackageName()));
						activity.startActivityForResult(intent, 0);
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Granting Draw Over Apps Permission "));
				}
				else
				{
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Permission already Granted "));
				}

		}
	    return true;
		}

	     else if (action.equals(ACTION_OPEN_OVER_APP_VIEW)) {

		if (checkDrawOverAppsPermission(activity)) {
			if (options.getJSONObject(0).has("path")) {
				openServiceWithHTMLfile(callbackContext,options.getJSONObject(0) );
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Draw Over Apps Should Start Successfully "));
			}

		}
		else
		{
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "No Permission Granted for Draw Over Apps"));
		}

		return true;
			}
		else if (action.equals(ACTION_CLOSE_OVER_APP_VIEW)) {
			    try {
			        activity.stopService(new Intent(activity, FloatOverService.class));
			        callbackContext.success(); // Notify success to JavaScript side
			    } catch (Exception e) {
			        callbackContext.error("Error while stopping the service: " + e.getMessage());
			    }
			}
	
 
        	return false;

		}

		public void openServiceWithHTMLfile(CallbackContext callbackContext,JSONObject option) throws JSONException {
				serviceParameters.setString(FOLDER_PATH + option.getString("path"),"file_path");
				if (option.has("hasHead")) {
						serviceParameters.setBoolean(option.getBoolean("hasHead"),"has_head");
				}
				if (option.has("dragToSide")) {
						serviceParameters.setBoolean(option.getBoolean("dragToSide"),"drag_to_side");
				}
				if (option.has("enableBackBtn")) {
						serviceParameters.setBoolean(option.getBoolean("enableBackBtn"),"enable_hardware_back");
				}
				if (option.has("enableCloseBtn")) {
						serviceParameters.setBoolean(option.getBoolean("enableCloseBtn"),"enable_close_btn");
				}
				if(option.has("verticalPosition")) {
					  serviceParameters.setString(option.getString("verticalPosition"),"vertical_position");
				 }
				if(option.has("horizontalPosition")) {
					  serviceParameters.setString(option.getString("horizontalPosition"),"horizontal_position");
				 }
        activity.startService(new Intent(activity,FloatOverService.class));

    }

		public static boolean checkDrawOverAppsPermission(Activity currentActivity) {
        Log.d("checkDrawPermission", "Called");
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(currentActivity)) {
                Log.d("checkDrawPermission", "false");
                return false;
            } else {
                Log.d("checkDrawPermission", "true");
                return true;
            }
        } else {
            Log.d("checkDrawPermission", "true");
            return true;
        }

    }

}
