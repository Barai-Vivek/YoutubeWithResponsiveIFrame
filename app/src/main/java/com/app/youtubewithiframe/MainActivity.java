package com.app.youtubewithiframe;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.app.youtubewithiframe.UtilsForYT.ChromeClient;
import com.app.youtubewithiframe.UtilsForYT.myWebViewClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar webviewProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 16) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= 21) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        }

        webView = findViewById(R.id.youtubePlayerView);
        webviewProgress = findViewById(R.id.webviewProgress);


        loadYoutubeVideo(webView, "p3kWAyUSNsU", webviewProgress);
    }


    public void loadYoutubeVideo(WebView feedVideoWebView, String videoUrl, ProgressBar webviewProgress) {
        //you can also change height in percentage as per your screen.
        //this height refers to padding from bottom of screen.
        final String frameVideo = generateResponsiveLinkForYoutubeIframe(videoUrl, "61");
        System.out.println("Something with session frameVideo - " + videoUrl);

        myWebViewClient mWebViewClient = new myWebViewClient(webviewProgress, feedVideoWebView);
        feedVideoWebView.setWebViewClient(mWebViewClient);
        feedVideoWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        feedVideoWebView.setBackgroundColor(Color.parseColor("#000000"));
        feedVideoWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        feedVideoWebView.setWebChromeClient(new ChromeClient(MainActivity.this, feedVideoWebView));
        feedVideoWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        feedVideoWebView.getSettings().setJavaScriptEnabled(true);
        feedVideoWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        feedVideoWebView.getSettings().setBuiltInZoomControls(false);
        feedVideoWebView.getSettings().setSupportZoom(false);
        feedVideoWebView.getSettings().setLoadWithOverviewMode(true);
        feedVideoWebView.getSettings().setUseWideViewPort(true);

        feedVideoWebView.loadDataWithBaseURL("https://youtube.com/", frameVideo, "text/html", "utf-8", null);
    }

    public String generateResponsiveLinkForYoutubeIframe(String videoUrl, String heightPercent) {
        String responsiveStyle = "<style>\n" +
                "    .video-container {position: relative;padding-bottom: "+heightPercent+"%;padding-top: 35px;height: 0;overflow: hidden;}.video-container iframe {position: absolute;top:0;left: 0;width: 100%; height: 100%;}\n" +
                "</style>";

        String htmlParams = "{" +
                "\"videoId\": \"" + videoUrl + "\"," +
                "\"width\": \"100%\"," +
                "\"height\": \"100%\"," +
                "\"events\": {" +
                "\"onReady\": \"onReady\"," +
                "\"onStateChange\": \"onStateChange\"," +
                "\"onPlaybackQualityChange\": \"onPlaybackQualityChange\"," +
                "\"onError\":\"onPlayerError\"" +
                "}," +
                "\"playerVars\": {" +
                "\"cc_load_policy\": 1," +
                "\"iv_load_policy\": 3," +
                "\"controls\": 1," +
                "\"playsinline\": 1," +
                "\"autohide\": 1," +
                "\"showinfo\": 0," +
                "\"rel\": 0," +
                "\"modestbranding\":1," +
                "\"start\":0" +
                "  }" +
                "}";

        String rawHtml = loadAssetTextAsString(MainActivity.this, "CommonYoutube.html");
        return rawHtml.replaceAll("%@", htmlParams).replace("$$STYLE$$", responsiveStyle);
    }

    private String loadAssetTextAsString(Context context, String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.e("Responsive", "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("Responsive", "Error closing asset " + name);
                }
            }
        }

        return null;
    }

}