package ie.ucd.cobweb.cordova;

import ie.ucd.cobweb.Constant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class VACameraActivity extends Activity implements OnClickListener {

	private SurfaceView preview = null; // The surface view that will contain
										// the camera image
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false; // Flag to check if in preview
	private boolean cameraConfigured = false; // Flag to check if the camera has
												// been configured

	private String locationForPhoto; // Location where the photo should be
										// stored

	double vaH, vaV;

	PictureCallback png = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			// TODO Auto-generated method stub
			Log.d("onPicture", "MyCamera");
			camera.stopPreview();
			if (data == null) {
				Log.d("Nothing in the Data", "MyCamera");
			}
			FileOutputStream outStream = null;
			File file = new File(locationForPhoto);
			Log.d("path set to " + locationForPhoto, "MyCamera");
			try {
				outStream = new FileOutputStream(file.toString());
				Log.d("About to  Writing ", "MyCamera");
				outStream.write(data);
				Log.d("Finished Writing ", "MyCamera");
				outStream.close();
				Intent intent = getIntent();
				intent.putExtra(COBWEBCameraVAPlugin.HOR, vaH);
				intent.putExtra(COBWEBCameraVAPlugin.VER, vaV);

				setResult(Activity.RESULT_OK, intent);

				finish();
			} catch (FileNotFoundException e) {
				Log.d("Cant find file", "MyCamera");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d("IO exception", "MyCamera");
			}
		}
	};

	/*
	 * Trying new method , my old preview code and android main site does not
	 * work so see if this creates the right surface
	 */
	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {

		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			initPreview(width, height);
			startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {

		}
	};

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(rVal("layout", "cobwebvacamera"));
		// Log.d(" set layout  started ","MyCamera");
		preview = (SurfaceView) findViewById(rVal("id", "livefeed"));
		// Log.d(" get surface view   started ","MyCamera");
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		// Log.d(" Adding it  started ","MyCamera");

		// need for old android 3.0 phones
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		Button takeButton = (Button) findViewById(rVal("id", "takebutton"));
		takeButton.setOnClickListener(this);

		Bundle passingInLocation = getIntent().getExtras();
		locationForPhoto = passingInLocation.getString(Constant.FLOC);

	}

	private int rVal(String s0, String s1) {
		return getResources().getIdentifier(s1, s0, getPackageName());
	}

	@Override
	public void onResume() {
		super.onResume();

		// Log.e(" about to open camera ","MyCamera");
		camera = Camera.open();

		startPreview();
	}

	@Override
	public void onPause() {
		if (inPreview) {
			camera.stopPreview();
		}
		camera.release();
		camera = null;
		inPreview = false;

		super.onPause();

	}

	private void initPreview(int width, int height) {

		if (camera != null && previewHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(previewHolder);
			} catch (Throwable t) {
				Log.e("error in init", "myCamera");
			}

			Camera.Parameters parameters = camera.getParameters();
			if (!cameraConfigured) {

				int cW = 0, cH = 0;
				for (Camera.Size sz : parameters.getSupportedPreviewSizes()) {
					if (sz.width * sz.height > cW * cH) {
						cW = sz.width;
						cH = sz.height;
					}

				}

				parameters.setPreviewSize(cW, cH);

				cW = 0;
				cH = 0;
				for (Camera.Size sz : parameters.getSupportedPictureSizes()) {

					if (sz.width * sz.height > cW * cH) {
						cW = sz.width;
						cH = sz.height;

					}
				}
				parameters.setPictureSize(cW, cH);

				camera.setParameters(parameters);
				cameraConfigured = true;

			}
		}

	}

	private void startPreview() {
		if (cameraConfigured && camera != null) {
			camera.startPreview();
			inPreview = true;
		}
	}

	@Override
	public void onClick(View view) {

		camera.takePicture(null, null, png);
		Camera.Parameters cp = camera.getParameters();

		vaH = cp.getHorizontalViewAngle();
		vaV = cp.getVerticalViewAngle();
	}

}
