package com.app.youtubewithiframe.UtilsForYT;

import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class myWebViewClient extends WebViewClient {
    ProgressBar progressBarVideo;
    WebView webViewFeed;

    public myWebViewClient(ProgressBar progressBarVideo, WebView webViewFeed) {
        this.webViewFeed = webViewFeed;
        this.progressBarVideo = progressBarVideo;
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (progressBarVideo != null)
            progressBarVideo.setVisibility(View.VISIBLE);
        if (webViewFeed != null)
            webViewFeed.setVisibility(View.VISIBLE);
        return super.shouldOverrideUrlLoading(view, url);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (progressBarVideo != null)
            progressBarVideo.setVisibility(View.GONE);
        if (webViewFeed != null)
            webViewFeed.setVisibility(View.VISIBLE);
        super.onPageFinished(view, url);
    }

}