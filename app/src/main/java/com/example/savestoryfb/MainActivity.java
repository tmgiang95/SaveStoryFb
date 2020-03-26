package com.example.savestoryfb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Environment;
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
        setContentView(R.layout.activity_main);
        initAppFolder();
        webView = findViewById(R.id.WebView1);
        btnDownload = findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Downloading ...", Toast.LENGTH_LONG).show();
                webView.loadUrl("javascript:window.HTMLOUT.checkType([...document.querySelectorAll(\"#story_viewer_content div>img\")][1].parentElement.getAttribute(\"data-store\")  == null ? 1 : 0)");//img
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);


        webView.addJavascriptInterface(new MyJavaScriptInterface(getBaseContext(), new OnHandleCheckTypeToDownload() {
            @Override
            public void onSuccessCheckType(final String type) {
                Log.d("giangtm1", "onSuccess: " + type);
                if (type.equals("1")) {
                    webView.post(new Runnable() {
                        public void run() {
                            webView.loadUrl("javascript:window.HTMLOUT.processHTML( [...document.querySelectorAll(\"#story_viewer_content div>img\")].sort((a,b) => b.naturalHeight - a.naturalHeight)[0].src," + type + ")");//img
                        }
                    });

                } else {
                    webView.post(new Runnable() {
                        public void run() {
                            webView.loadUrl("javascript:window.HTMLOUT.processHTML(JSON.parse([...document.querySelectorAll(\"#story_viewer_content div>img\")][1].parentElement.getAttribute(\"data-store\")).src," + type + ")");//video
                        }
                    });
                }
            }

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
            public void onDownloading(final String url, int type) {
                if (type == 1) {
                    saveimage(url);
                } else {
                    saveVideo(url);
                }

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

    public void saveVideo(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String storagePath = APP_FOLDER_URL;

                    String fileName = System.currentTimeMillis() + ".mp4";
                    File f = new File(storagePath, fileName);

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = client.newCall(request).execute();
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

                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{APP_FOLDER_URL + "/" + fileName}, new String[]{"mp4"}, null);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void saveimage(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = client.newCall(request).execute();
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    String fileName = APP_FOLDER_URL + "/" + System.currentTimeMillis() + ".jpg";
                    try (FileOutputStream out = new FileOutputStream(fileName)) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        // PNG is a lossless format, the compression factor (100) is ignored
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Download success !!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{fileName}, new String[]{"image/jpeg"}, null);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                Intent myIntent = new Intent(MainActivity.this, PreviewDownloadedActivity.class);
                startActivity(myIntent);
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
        public void processHTML(String url, int type) {
            Log.d("giangtm1", "processHTML: " + url);
            onHandleCheckTypeToDownload.onDownloading(url, type);
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void checkType(String url) {
            onHandleCheckTypeToDownload.onSuccessCheckType(url);
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
