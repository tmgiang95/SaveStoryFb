package com.example.savestoryfb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    public static final String APP_FOLDER_URL = Environment.getExternalStorageDirectory() + File.separator + "FacebookStories";
    WebView webView;
    String HTTP_URL = "https://www.fb.com";
    Button btnDownload;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.WebView1);
        btnDownload = findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Downloading ...", Toast.LENGTH_LONG).show();
                webView.loadUrl("javascript:window.HTMLOUT.processHTML((()=>{var a = [...document.querySelectorAll(\"#story_viewer_content div>img\")];var v = a[1].parentElement.getAttribute(\"data-store\");return v == null? a.sort((a,b) => b.naturalHeight - a.naturalHeight)[0].src: JSON.parse(v).src})())");//img
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);


        webView.addJavascriptInterface(new MyJavaScriptInterface(getBaseContext(), new OnHandleCheckTypeToDownload() {
            @Override
            public void onSuccessCheckStorySite(final int type) {
                Log.d("giangtm1", "check: " + type);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (type > 0) {
                            btnDownload.setVisibility(View.VISIBLE);
                        } else {
                            btnDownload.setVisibility(View.GONE);
                        }
                    }
                });
            }

            @Override
            public void onDownloading(final String url) {
                saveFile(url);
            }
        }), "HTMLOUT");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                webView.loadUrl("javascript:window.HTMLOUT.detectStorySite([...document.querySelectorAll(\"#story_viewer_content div>img\")].length)");//img

            }
        });
        //Register the WebView to be able to take display a menu...
        //You'll need this menu to choose an action on long press
        webView.loadUrl(HTTP_URL);

    }

    public void saveFile(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String storagePath = APP_FOLDER_URL;


                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = client.newCall(request).execute();
                    String extension = response.header("content-type").split("/")[1];
                    String fileName = System.currentTimeMillis() + "." + extension;
                    File f = new File(storagePath, fileName);
                    InputStream inputStream = response.body().byteStream();
                    FileOutputStream fos = new FileOutputStream(f);
                    byte[] buffer = new byte[1024];
                    int len1 = 0;
                    while ((len1 = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len1);
                    }
                    fos.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Download success !!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{APP_FOLDER_URL + "/" + fileName}, new String[]{extension}, null);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public void initAppFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "FacebookStories");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * Handles the result of the request for permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    postRequestPermissionsResult(requestCode, false);
                    requestPermission();
                    return;
                }
            }
            initAppFolder();
            postRequestPermissionsResult(requestCode, true);
            return;
        } else {
            requestPermission();
        }

        postRequestPermissionsResult(requestCode, false);
    }

    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // If we should give explanation of requested permissions
                // Show an alert dialog here with request explanation
                showAlertOkCancel(R.string.permission_dialog_title, R.string.permission_camera_gallery, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                100);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postRequestPermissionsResult(100, false);
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);
            }
        }

    }

    protected void showAlertOkCancel(@StringRes int titleId
            , @StringRes int messageId
            , final DialogInterface.OnClickListener okClickListener
            , final DialogInterface.OnClickListener cancelClickListener) {
        new AlertDialog.Builder(this)
                .setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(android.R.string.ok, okClickListener)
                .setNegativeButton(android.R.string.cancel, cancelClickListener)
                .show();
    }

    protected void postRequestPermissionsResult(final int reqCd, final boolean result) {
        Log.d("giangtm1", "postRequestPermissionsResult: reqCd=" + reqCd + ", result=" + result);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mniImageDownloaded:
//                Intent myIntent = new Intent(MainActivity.this, PreviewDownloadedActivity.class);
//                startActivity(myIntent);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setType("image/*");
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    /* An instance of this class will be registered as a JavaScript interface */

    static class MyJavaScriptInterface {
        OnHandleCheckTypeToDownload onHandleCheckTypeToDownload;
        Context context;

        public MyJavaScriptInterface(Context context, OnHandleCheckTypeToDownload onHandleCheckTypeToDownloadListener) {
            this.onHandleCheckTypeToDownload = onHandleCheckTypeToDownloadListener;
            this.context = context;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String url) {
            Log.d("giangtm1", "processHTML: " + url);
            onHandleCheckTypeToDownload.onDownloading(url);
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void detectStorySite(int code) {
            onHandleCheckTypeToDownload.onSuccessCheckStorySite(code);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }


}
