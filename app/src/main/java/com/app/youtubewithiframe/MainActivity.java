package com.app.youtubewithiframe;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.youtubewithiframe.UtilsForYT.ChromeClient;
import com.app.youtubewithiframe.UtilsForYT.myWebViewClient;

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


        loadYoutubeVideo(webView, "VIDEO_ID", webviewProgress);
    }


    public void loadYoutubeVideo(WebView feedVideoWebView, String videoUrl, ProgressBar webviewProgress) {
        //you can also change height in percentage as per your screen.
        //this height refers to padding from bottom of screen.
        final String frameVideo = generateResponsiveLinkForYoutubeIframe(videoUrl,  "61");
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

        feedVideoWebView.loadDataWithBaseURL(null, frameVideo, "text/html", "utf-8", null);
    }

    public String generateResponsiveLinkForYoutubeIframe(String videoUrl, String heightPercent) {
        String head = "<head>" +
                "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\" />" +
                "<meta charset=\"utf-8\" />" +
                "<meta name=\"viewport\" content=\"user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, width=device-width\" />" +
                "<script src=\"https://www.youtube.com/iframe_api\"></script>" +
                "</head>";
        return "<html>" + head + "<body style='margin:0px;padding:0px;'><script type='text/javascript'" +
                "src='https://www.youtube.com/iframe_api'></script><script type='text/javascript'>" +
                "function onYouTubeIframeAPIReady(){ytplayer=new YT.Player('playerId'," +
                "{events:{onReady:onPlayerReady}})}function onPlayerReady(a){a.target.playVideo();}" +
                "</script><style>" +
                ".video-container {" +
                "position: relative;" +
                "padding-bottom: " + heightPercent + "%;" +
                "padding-top: 35px;" +
                "height: 0;" +
                "overflow: hidden;" +
                "}" +
                ".video-container iframe {" +
                "position: absolute;" +
                "top:0;" +
                "left: 0;" +
                "width: 100%; " +
                "height: 100%;}" +
                "</style><div class='video-container'>" +
                "<iframe id='playerId' type='text/html' src='https://www.youtube.com/embed/" + videoUrl + "?enablejsapi=1&rel=0&showinfo=0&playsinline=1&autoplay=1&modestbranding=1&version=3' frameborder='0' allowFullScreen='allowFullScreen'>" +
                "</div></body></html>";
    }

}