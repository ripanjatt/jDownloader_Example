package com.ripanjatt.jdownloader_example;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;

import javaDownloader.Downloader;
import javaDownloader.Listeners;

public class MainActivity extends AppCompatActivity {

    /*
    A simple example that uses the jDownloader Library to download a file!
     */
    private Handler handler;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();
        handler = new Handler();
        Objects.requireNonNull(getSupportActionBar()).setTitle("Example");
        /*
        A WebView to get the link and length to the file to make example simple!
         */
        final WebView webView = findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // getting filename from the url using standard android webkit!
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                alert(url, fileName, contentLength);
            }
        });
        Button button = findViewById(R.id.download);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText text = findViewById(R.id.link);
                String link = text.getText().toString();
                if(!link.equals("")){
                    webView.loadUrl(link);
                }
            }
        });

    }

    private void alert(final String url, final String fileName, final long length){
        /*
        An alert dialog to start the download!
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download?");
        builder.setMessage("File: " + fileName);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                /*
                Creating the downloader object!
                It takes 4 parameters:
                -> url: the link to download the file!
                -> filename: to save the file with that specific name(can be user defined)!
                -> downloadPath: the path to the download folder!
                -> length: the length of the file in bytes(can be 0)!
                 */
                Downloader downloader = new Downloader(url, fileName, (Environment.getExternalStorageDirectory() + "/Test/"), length);
                downloader.download();
                // setting up ProgressListener to keep track of the download!
                downloader.setOnProgressListener(new Listeners.ProgressListener() {

                    // onStart is triggered when download starts!
                    @Override
                    public void onStart() {
                        // updating the ui!
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView name = findViewById(R.id.filename);
                                name.setText(fileName);
                                TextView progress = findViewById(R.id.progress);
                                progress.setText(("Starting..."));
                                Toast.makeText(MainActivity.this, "Download Started!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    // onProgress gives the percentage of downloaded file(will be zero if length of file is set to 0)!
                    @Override
                    public void onProgress(final double progressPercent) {
                        // updating the ui!
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView progress = findViewById(R.id.progress);
                                progress.setText((progressPercent + "%"));
                            }
                        });
                    }

                    // onSpeedInKB gives the speed of the network in Kilobytes per Second!
                    @Override
                    public void onSpeedInKB(final double speed) {
                        // updating the ui!
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView speedText = findViewById(R.id.speed);
                                speedText.setText((speed + " KB/s"));
                            }
                        });
                    }

                    // onComplete gets triggered when file is downloaded!
                    @Override
                    public void onComplete() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView progress = findViewById(R.id.progress);
                                TextView speed = findViewById(R.id.speed);
                                progress.setText(("Downloaded!"));
                                speed.setText((""));
                                Toast.makeText(MainActivity.this, "Downloaded!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
        builder.setNegativeButton("No", null);
        builder.setCancelable(true);
        builder.show();
    }

    private void getPermissions(){
        // gets permission for the read/write!
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }
}
