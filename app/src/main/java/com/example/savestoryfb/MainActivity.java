package com.example.savestoryfb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.savestoryfb.previewscreen.PreviewDownloadedActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class MainActivity extends BaseActivity {
    public static final String APP_FOLDER_URL = Environment.getExternalStorageDirectory() + File.separator + "FacebookStories";
    public static final String TAG = "giangtm1";

    WebView webView;
    String HTTP_URL = "https://www.fb.com";
    Button btnDownload;
    RelativeLayout rvLoading;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){
            initAppFolder();
        }
        initAppFolder();
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.WebView1);
        btnDownload = findViewById(R.id.btnDownload);
        rvLoading = findViewById(R.id.rvLoading);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermission();
                } else {
                    Toast.makeText(MainActivity.this, "Downloading ...", Toast.LENGTH_LONG).show();
                    webView.loadUrl("javascript:window.HTMLOUT.processHTML((()=>{var a = [...document.querySelectorAll(\"#story_viewer_content div>img\")];var v = a[1].parentElement.getAttribute(\"data-store\");return v == null? a.sort((a,b) => b.naturalHeight - a.naturalHeight)[0].src: JSON.parse(v).src})())");
                }
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);


        webView.addJavascriptInterface(new MyJavaScriptInterface(getBaseContext(), new OnHandleCheckTypeToDownload() {
            @Override
            public void onSuccessCheckStorySite(final int type) {
                Log.d(TAG, "check: " + type);
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
                webView.loadUrl("javascript:window.HTMLOUT.detectStorySite(document.querySelectorAll(\"#story_viewer_content div>img\").length)");//img

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
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = client.newCall(request).execute();
                    String contentType = response.header("content-type");
                    if (contentType != null){
                        String extension = contentType.split("/")[1];
                        String fileName = System.currentTimeMillis() + "." + extension;
                        File f = new File(APP_FOLDER_URL, fileName);
                        ResponseBody responseBody = response.body();
                        if (responseBody != null){
                            InputStream inputStream = responseBody.byteStream();
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

                        } else {
                            Log.d(TAG, "run saveFile: responeseBody is null");
                        }
                       } else {
                        Log.d(TAG, "run saveFile: Content Type is null");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

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
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermission();
                } else {
                    rvLoading.setVisibility(View.VISIBLE);
                    Intent myIntent = new Intent(MainActivity.this, PreviewDownloadedActivity.class);
                    startActivityForResult(myIntent,1);
                }
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            rvLoading.setVisibility(View.GONE);
        }
    }
    /* An instance of this class will be registered as a JavaScript interface */

    static class MyJavaScriptInterface {
        OnHandleCheckTypeToDownload onHandleCheckTypeToDownload;
        Context context;

        MyJavaScriptInterface(Context context, OnHandleCheckTypeToDownload onHandleCheckTypeToDownloadListener) {
            this.onHandleCheckTypeToDownload = onHandleCheckTypeToDownloadListener;
            this.context = context;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String url) {
            Log.d(TAG, "processHTML: " + url);
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
