package com.plancktech.dserial_android.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.plancktech.dserial_android.Constants.Constants;
import com.plancktech.dserial_android.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private WebView webview;

    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int FILE_CHOOSER_RESULT_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mUploadMessageArr;
    private String mCameraPhotoPath = null;
    private long size = 0;
    private static final int PERMISSION_REQUEST_CODE = 123;

    // Server 1
    Server_1 server1;
    TextView infoip, msg;

    // Client 1
    TextView response;
    EditText editTextAddress, editTextPort;
    Button buttonConnect, buttonClear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Intent intentServer = new Intent(this, Server.class);
        //startActivity(intentServer);

        //Intent intentClient = new Intent(this, Client.class);
        //startActivity(intentClient);

        // Server 1
        setContentView(R.layout.activity_server1);
        infoip = (TextView) findViewById(R.id.infoip);
        msg = (TextView) findViewById(R.id.msg);
        server1 = new Server_1(this);
        infoip.setText(server1.getIpAddress()+":"+server1.getPort());

        // Client 1
        /*setContentView(R.layout.activity_client1);
        editTextAddress = (EditText) findViewById(R.id.addressEditText);
        editTextPort = (EditText) findViewById(R.id.portEditText);
        buttonConnect = (Button) findViewById(R.id.connectButton);
        buttonClear = (Button) findViewById(R.id.clearButton);
        response = (TextView) findViewById(R.id.responseTextView);

        buttonConnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Client_1 myClient = new Client_1(editTextAddress.getText()
                        .toString(), Integer.parseInt(editTextPort
                        .getText().toString()), response);
                myClient.execute();
            }
        });

        buttonClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                response.setText("");
            }
        });*/

        /*getSupportActionBar().hide();
        setContentView(R.layout.webview);
        webview = (WebView) findViewById(R.id.webView);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(Constants.INITIALIZING);
        progressDialog.setCancelable(false);

        loadRequest(MainActivity.this);*/

    }

    public void loadRequest(final Context c) {
        progressDialog.show();
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
            showWebView();
        } else {
            new AlertDialog.Builder(c)
                    .setMessage(Constants.NETWORK_NOT_AVAILABLE)
                    .setCancelable(false)
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
                            if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
                                showWebView();
                            } else {
                                dialog.dismiss();
                                progressDialog.show();
                                loadRequest(c);
                            }
                        }
                    }).show();
        }

    }

    public void showWebView() {
        initSettings();
        setListeners();
    }

    private void initSettings() {
        webview.getSettings().setJavaScriptEnabled(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            webview.getSettings().setAllowFileAccessFromFileURLs(true);
            webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webview.setFocusable(true);
        webview.setFocusableInTouchMode(true);
        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setGeolocationDatabasePath(getFilesDir().getPath());
        webview.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webview.getSettings().setDatabaseEnabled(true);
        webview.getSettings().setGeolocationEnabled(true);
        webview.getSettings().setLoadsImagesAutomatically(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setCacheMode(webview.getSettings().LOAD_DEFAULT);
        webview.getSettings().setAppCachePath(this.getCacheDir().getAbsolutePath());
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setAppCacheMaxSize(1024 * 1024 * 50);
        webview.getSettings().setSupportMultipleWindows(true);
        webview.setKeepScreenOn(true);
        webview.setFitsSystemWindows(true);
        if (Build.VERSION.SDK_INT >= 19) {
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else if(Build.VERSION.SDK_INT >=11 && Build.VERSION.SDK_INT < 19) {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void setListeners() {

        webview.setWebViewClient(new WebViewClient() {

            // If you will not use this method url links are open in new brower
            // not in webview
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                Log.e("webviewurl", ": " + url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressDialog.dismiss();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                progressDialog.dismiss();
                loadRequest(MainActivity.this);
            }

        });

        webview.setWebChromeClient(new WebChromeClient() {

            // For Android 5.0
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
                // Double check that we don't have any existing callbacks
                if (mUploadMessageArr != null) {
                    mUploadMessageArr.onReceiveValue(null);
                }
                mUploadMessageArr = filePath;
                Log.e("FileCooserParams => ", filePath.toString());

                if (isStoragePermissionGranted()) {
                    imageCameraIntent();
                }
                return true;
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                // Create AndroidExampleFolder at sdcard
                // Create AndroidExampleFolder at sdcard
                File imageStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES)
                        , "AndroidExampleFolder");
                if (!imageStorageDir.exists()) {
                    // Create AndroidExampleFolder at sdcard
                    imageStorageDir.mkdirs();
                }
                // Create camera captured image file path and name
                File file = new File(
                        imageStorageDir + File.separator + "IMG_"
                                + String.valueOf(System.currentTimeMillis())
                                + ".jpg");
                mCapturedImageURI = Uri.fromFile(file);
                // Camera capture image intent
                final Intent captureIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                        , new Parcelable[] { captureIntent });
                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);
            }
            // openFileChooser for Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }
            //openFileChooser for other Android versions
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType,
                                        String capture) {
                openFileChooser(uploadMsg, acceptType);
            }
        });

        webview.loadUrl(Constants.LOAD_URL_Test);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)

        return super.onKeyDown(keyCode, event);
    }

    public void imageCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "Unable to create Image File", ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
            } else {
                takePictureIntent = null;
            }
        }
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        //contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        contentSelectionIntent.setType("image/*");

        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        startActivityForResult(Intent.createChooser(chooserIntent, "Select images"), INPUT_FILE_REQUEST_CODE);
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mUploadMessageArr == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                    else if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                }
            }
            mUploadMessageArr.onReceiveValue(results);
            mUploadMessageArr = null;
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILE_CHOOSER_RESULT_CODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            if (requestCode == FILE_CHOOSER_RESULT_CODE) {
                if (null == this.mUploadMessage) {
                    return;
                }
                Uri result = null;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e,
                            Toast.LENGTH_LONG).show();
                }
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
        return;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{INTERNET, ACCESS_NETWORK_STATE, Manifest.permission.CAMERA, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE,}, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean internetAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean accessNetworkStateAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean writestorageAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean readstorageAccepted = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    if (internetAccepted && accessNetworkStateAccepted && cameraAccepted && cameraAccepted && writestorageAccepted && readstorageAccepted) {
                        imageCameraIntent();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            showMessageOKCancel("You need to allow all permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{INTERNET, ACCESS_NETWORK_STATE, Manifest.permission.CAMERA, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE,}, PERMISSION_REQUEST_CODE);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setCancelable(false)
                .create()
                .show();
    }

    private boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_NETWORK_STATE);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        int result4 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result5 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return result1 == PackageManager.PERMISSION_GRANTED
                && result2 == PackageManager.PERMISSION_GRANTED
                && result3 == PackageManager.PERMISSION_GRANTED
                && result4 == PackageManager.PERMISSION_GRANTED
                && result5 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        server1.onDestroy();
    }
}
