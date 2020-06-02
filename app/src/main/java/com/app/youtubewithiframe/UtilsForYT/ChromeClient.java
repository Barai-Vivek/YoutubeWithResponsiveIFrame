package com.app.youtubewithiframe.UtilsForYT;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

public class ChromeClient extends WebChromeClient {
    private View mCustomView;
    private CustomViewCallback mCustomViewCallback;
    private int mOriginalSystemUiVisibility;
    private Activity activity;
    private WebView webView;

    public ChromeClient(Activity activity, WebView webView) {
        this.activity = activity;
    }

    public Bitmap getDefaultVideoPoster() {
        if (this == null) {
            return null;
        }
        return BitmapFactory.decodeResource(activity.getResources(), 2130837573);
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
        super.onProgressChanged(view, progress);
    }

    public void onHideCustomView() {
        ((FrameLayout) activity.getWindow().getDecorView()).removeView(this.mCustomView);
        this.mCustomView = null;
        activity.getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
        /*setRequestedOrientation(this.mOriginalOrientation);*/
        this.mCustomViewCallback.onCustomViewHidden();
        this.mCustomViewCallback = null;
    }

    public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
        if (this.mCustomView != null) {
            onHideCustomView();
            return;
        }
        this.mCustomView = paramView;
        this.mOriginalSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        this.mCustomViewCallback = paramCustomViewCallback;
        ((FrameLayout) activity.getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
        this.mCustomView.setBackgroundColor(Color.BLACK);
        activity.getWindow().getDecorView().setSystemUiVisibility(3846);
    }

    @Nullable
    @Override
    public View getVideoLoadingProgressView() {
        System.out.println("Video is loading");
        return super.getVideoLoadingProgressView();
    }
}