package ie.ucd.cobweb.cordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;

import android.os.Build;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import android.app.Activity;

import android.os.Bundle;

import android.os.Environment;
import android.util.Log;

public class COBWEBCameraVAPlugin extends CordovaPlugin {

	private static final String VA = "View Angle";
	public static final String HOR = "Horizontal";
	public static final String VER = "Vertical";
	private static final String SFOLDER = "COBWEB";
	private static final String EMSG = "Error taking photo";
	private final static String FLOC = "FileLocation";

	private CallbackContext cbContext;
	private String fileP;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {

		cbContext = callbackContext;
		PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
		r.setKeepCallback(true);
		callbackContext.sendPluginResult(r);

		final CordovaPlugin cp = this;

		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				Activity activity = cordova.getActivity();
				Context context = activity.getApplicationContext();

				try {

					File storageDir;
					if (Environment.DIRECTORY_PICTURES == null) {
						storageDir = new File(activity.getCacheDir()
								.getAbsolutePath() + "/" + SFOLDER + "/");
					} else {

						File picDir = new File(Environment
								.getExternalStoragePublicDirectory(
										Environment.DIRECTORY_PICTURES)
								.getAbsolutePath());
						if (!picDir.exists()) {
							Log.d(SFOLDER, "Creating pictures directory: "
									+ picDir.mkdir());
						}

						storageDir = new File(Environment
								.getExternalStoragePublicDirectory(
										Environment.DIRECTORY_PICTURES)
								.getAbsolutePath()
								+ "/" + SFOLDER + "/");
					}

					File file = File.createTempFile("IMG_" + UUID.randomUUID(),
							".jpeg", storageDir);

					fileP = file.getAbsolutePath();

					// Create camera intent
					Intent intent = new Intent(context,
							ie.ucd.cobweb.cordova.VACameraActivity.class);
					intent.putExtra(FLOC, fileP);
					cordova.startActivityForResult(cp, intent, 0);

				} catch (IOException e) {
					e.printStackTrace();

				}
			}
		});

		return true;

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		try {
			if (resultCode == Activity.RESULT_OK) {

				Bundle b = intent.getExtras();

				JSONObject photo = new JSONObject();
				photo.put(FLOC, fileP);
				JSONObject va = new JSONObject();
				va.put(HOR, b.getDouble(HOR));
				va.put(VER, b.getDouble(VER));
				photo.put(VA, va);

				cbContext.success(photo);
			} else {
				cbContext.error(EMSG);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			cbContext.error(EMSG);
		}

	}
}
