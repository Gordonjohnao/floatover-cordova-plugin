package com.test.cordova.plugin;
// The native alert dialog API
import android.app.AlertDialog;
// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FloatOver extends CordovaPlugin {
  @Override
  public boolean execute(String action, JSONArray args,
    final CallbackContext callbackContext) {

    // Verify that the user sent a 'show' action
    if (!action.equals("show")) {
        callbackContext.error("\"" + action + "\" is not a recognized action.");
        return false;
    }

    String message;
    String title;
    try{
        JSONObject options = args.getJSONObject(0);
        message = options.getString("message");
        title = options.getString("title");
    } catch (JSONException e) {
        callbackContext.error("Error encountered: " + e.getMessage());
        return false;
    }       

    AlertDialog.Builder builder = new AlertDialog.Builder(cordova.getActivity());
    builder.setMessage(message).setTitle(title);
    AlertDialog dialog = builder.create();
    dialog.show();

    // Send a positive result to the callbackContext
    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
    callbackContext.sendPluginResult(pluginResult);

    return true;
  }
}