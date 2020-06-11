package com.app.youtubewithiframe;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.youtubewithiframe.UtilsForYT.ChromeClient;
import com.app.youtubewithiframe.UtilsForYT.WebViewYoutubeCallBacks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.Locale;

public class ResponsiveYoutubePlayerWithJS extends AppCompatActivity implements WebViewYoutubeCallBacks {

    private Activity activity;
    private Context context;
    private WebView webView;
    private View overlayWebView;
    private FrameLayout frmVideoMain;
    private RelativeLayout relControls;
    private ImageView play, ivFullScreen;
    private SeekBar seekBar;
    private ProgressBar webviewProgress;
    private Handler handler;
    private Runnable runnable;
    private Animation slideUpAnimation, slideDownAnimation;

    private WebViewYoutubeCallBacks webViewYoutubeCallBacks;  //get all playback to handle controlls
    private int videoTotalDurationInSec = 0;  //total video duration
    private int currentVideoDurationInSec = 0;  //current video duration
    private int videoPlaying = 0;  // 1 means video is playing
    private int toggledFullScreen = 0;   // 0 means small screen 1 means full screen
    private String time = ""; //time that can be shown
    private boolean isVideoLoaded = false; //check video video has been loaded
    private int tickPosition = 0; //to ignore webview call backs when seekbar position seeks

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responsive_youtube_player_js);
        activity = ResponsiveYoutubePlayerWithJS.this;
        context = getApplicationContext();
        webViewYoutubeCallBacks = this;

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

        initControls();

        overlayWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Clicked");
                if (isVideoLoaded) {
                    hideShowControls();
                }
            }
        });

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Tp prevent web view touch
                return true;
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideoLoaded) {
                    if (videoPlaying == 1) {
                        webView.loadUrl("javascript:pause();");
                        if (relControls.getVisibility() == View.GONE) {
                            relControls.startAnimation(slideUpAnimation);
                            relControls.setVisibility(View.VISIBLE);
                        }
                    } else {
                        webView.loadUrl("javascript:play();");
                        hideShowControls();
                    }
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.isPressed()) {
                    tickPosition = 2;
                    float seekTo = Float.parseFloat(changeNumberToTwoDigits(videoTotalDurationInSec + "")) * (Float.parseFloat(changeNumberToTwoDigits(progress + "")) / 100);
                    convertSecondsToHoursAndMinutes((int) seekTo, (TextView) findViewById(R.id.seekTime));
                    webView.loadUrl("javascript:seek(" + seekTo + ");");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ivFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                toggleVideoToFullScreen();
            }
        });

        initWebView();
        loadWebViewData("p3kWAyUSNsU");
    }

    //show hide controls with animation
    private void hideShowControls() {
        handler.removeCallbacks(runnable);
        System.out.println("Click is here");
        if (relControls.getVisibility() == View.VISIBLE) {
            relControls.setVisibility(View.GONE);
            relControls.startAnimation(slideDownAnimation);
        } else if (relControls.getVisibility() == View.GONE) {
            relControls.startAnimation(slideUpAnimation);
            relControls.setVisibility(View.VISIBLE);
            hideControls();
        }
    }

    public void initControls() {
        frmVideoMain = findViewById(R.id.frmVideoMain);
        overlayWebView = findViewById(R.id.overlayWebView);
        webView = findViewById(R.id.youtubePlayerView);
        relControls = findViewById(R.id.relControls);
        play = findViewById(R.id.play);
        ivFullScreen = findViewById(R.id.ivFullScreen);
        seekBar = findViewById(R.id.seekBar);
        webviewProgress = findViewById(R.id.webviewProgress);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        webView.getLayoutParams().height = (int) (displayMetrics.widthPixels * 0.5625);
        frmVideoMain.getLayoutParams().height = (int) (displayMetrics.widthPixels * 0.5625);

        slideUpAnimation = AnimationUtils.loadAnimation(context,
                R.anim.slide_up_fast);

        slideDownAnimation = AnimationUtils.loadAnimation(context,
                R.anim.slide_down_fast);

        //to hide controlls
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (relControls.getVisibility() == View.VISIBLE) {
                    relControls.setVisibility(View.GONE);
                    relControls.startAnimation(slideDownAnimation);
                }
            }
        };
    }

    public void initWebView() {
        myWebViewClient mWebViewClient = new myWebViewClient(webviewProgress, webView);
        webView.setWebViewClient(mWebViewClient);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.setBackgroundColor(Color.parseColor("#000000"));
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebChromeClient(new ChromeClient(ResponsiveYoutubePlayerWithJS.this, webView));
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

    }

    public void loadWebViewData(String video_id) {
        String htmlParams = "{" +
                "\"videoId\": \"" + video_id + "\"," +
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
                "\"controls\": 0," +
                "\"playsinline\": 1," +
                "\"autohide\": 1," +
                "\"showinfo\": 0," +
                "\"rel\": 0," +
                "\"modestbranding\":1," +
                "\"start\":0" +
                "  }" +
                "}";

        String rawHtml = loadAssetTextAsString(ResponsiveYoutubePlayerWithJS.this, "Youtube.html");
        String newHTML = rawHtml.replaceAll("%@", htmlParams);
        webView.loadDataWithBaseURL("https://youtube.com/", newHTML, "text/html", "UTF-8", null);
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

    @Override
    public void seekBarPosition(String durationInSeconds, String seekSeconds) {
        float seekPosition = (Float.parseFloat(changeNumberToTwoDigits(seekSeconds)) * 100) / Float.parseFloat(changeNumberToTwoDigits(durationInSeconds));
        seekBar.setProgress((int) seekPosition);
    }

    @Override
    public void totalDuration(String durationInSeconds) {
        videoTotalDurationInSec = Integer.parseInt(durationInSeconds);
        convertSecondsToHoursAndMinutes(Integer.parseInt(durationInSeconds), (TextView) findViewById(R.id.totalTime));
        System.out.println("Total video duration" + (Float.parseFloat(changeNumberToTwoDigits(durationInSeconds)) / 60));
    }

    @Override
    public void playback(int isPlaying) {
        videoPlaying = isPlaying;
        if (isPlaying == 1) {
            if (webviewProgress.getVisibility() == View.VISIBLE)
                webviewProgress.setVisibility(View.GONE);
            play.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
        } else if (isPlaying == 3) {
            if (webviewProgress.getVisibility() == View.GONE)
                webviewProgress.setVisibility(View.VISIBLE);
            play.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
        } else {
            if (webviewProgress.getVisibility() == View.VISIBLE)
                webviewProgress.setVisibility(View.GONE);
            play.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
        }
    }

    public class myWebViewClient extends WebViewClient {
        ProgressBar progressBarVideo;
        WebView webViewFeed;

        public myWebViewClient(ProgressBar progressBarVideo, WebView webViewFeed) {
            this.webViewFeed = webViewFeed;
            this.progressBarVideo = progressBarVideo;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url != null && !url.isEmpty() && Uri.parse(url).getScheme().equalsIgnoreCase("embed")) {
                if (Uri.parse(url).getQueryParameter("duration") != null) {
                    String duration = Uri.parse(url).getQueryParameter("duration");
                    webViewYoutubeCallBacks.totalDuration(duration);
                    if (Uri.parse(url).getQueryParameter("position") != null) {
                        if (tickPosition > 0) {
                            if (tickPosition > 1) {
                                tickPosition = tickPosition - 1;
                            } else {
                                tickPosition = 0;
                            }
                        } else {
                            currentVideoDurationInSec = (int) Float.parseFloat(changeNumberToTwoDigits(Uri.parse(url).getQueryParameter("position")));
                            webViewYoutubeCallBacks.seekBarPosition(duration, Uri.parse(url).getQueryParameter("position"));
                            convertSecondsToHoursAndMinutes((int) Float.parseFloat(changeNumberToTwoDigits(Uri.parse(url).getQueryParameter("position"))), (TextView) findViewById(R.id.seekTime));
                        }
                    }
                }
                if (Uri.parse(url).getQueryParameter("playback") != null) {
                    checkIfBuffering(Integer.parseInt(Uri.parse(url).getQueryParameter("playback")));
                }
                if (Uri.parse(url).getQueryParameter("failed") != null) {
                    if (Uri.parse(url).getQueryParameter("failed").equalsIgnoreCase("false")) {
                        isVideoLoaded = true;
                    }
                }
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (progressBarVideo != null)
                progressBarVideo.setVisibility(View.GONE);
            if (webViewFeed != null)
                webViewFeed.setVisibility(View.VISIBLE);
            play.performClick();
            super.onPageFinished(view, url);
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest
                request) {
            if (request.getUrl() != null) {
                String url = request.getUrl().toString();
                String extension = MimeTypeMap.getFileExtensionFromUrl(url);
                //I have some folders for files with the same extension
                if (extension.equals("css") && url.contains("www-player")) {
                    System.out.println("Intercepting");
                    try {
                        return new WebResourceResponse(getMimeType(url), "UTF-8", getAssets().open("youtubeplayer-mode.css"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        //get mime type by url
        public String getMimeType(String url) {
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(url);
            if (extension != null) {
                if (extension.equals("js")) {
                    return "text/javascript";
                } else if (extension.equals("woff")) {
                    return "application/font-woff";
                } else if (extension.equals("woff2")) {
                    return "application/font-woff2";
                } else if (extension.equals("ttf")) {
                    return "application/x-font-ttf";
                } else if (extension.equals("eot")) {
                    return "application/vnd.ms-fontobject";
                } else if (extension.equals("svg")) {
                    return "image/svg+xml";
                }
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            return type;
        }

    }

    //conver number to have max 2 fraction example 1.2333 = 1.23
    public String changeNumberToTwoDigits(String number1) {
        try {
            Long number = Long.parseLong(number1);
            NumberFormat nf = NumberFormat.getInstance(new Locale("en"));
            nf.setMinimumIntegerDigits(2);
            nf.setMaximumFractionDigits(2);
            return nf.format(number);
        } catch (Exception e) {
            return number1;
        }
    }

    //convet seconds to time that can be shown
    public void convertSecondsToHoursAndMinutes(int seconds, final TextView textView) {
        int hr = seconds / 3600;
        int min = (seconds % 3600) / 60;
        int sec = (seconds % 3600) % 60;
        System.out.print(hr + ":" + min + ":" + sec);

        time = "";

        if (hr > 0) {
            time = changeTimeToTwoDigit(hr + "") + ":";
        }

        if (min > 0) {
            if (sec > 0) {
                time = time + changeTimeToTwoDigit(min + "") + ":" + changeTimeToTwoDigit(sec + "");
            } else {
                time = time + changeTimeToTwoDigit(min + "") + ":00";
            }
        } else {
            if (sec > 0) {
                time = time + "00" + ":" + changeTimeToTwoDigit(sec + "");
            } else {
                time = time + "00" + ":00";
            }
        }
        if (textView != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(time);
                }
            });
        }
        System.out.print("final time = " + time);
    }

    public String changeTimeToTwoDigit(String number1) {
        try {
            Long number = Long.parseLong(number1);
            NumberFormat nf = NumberFormat.getInstance(new Locale("en"));
            nf.setMinimumIntegerDigits(2);
            nf.setMaximumFractionDigits(0);
            return nf.format(number);
        } catch (Exception e) {
            return number1;
        }
    }

    public void toggleVideoToFullScreen() {
        if (toggledFullScreen == 0) {
            toggledFullScreen = 1;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            frmVideoMain.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
            overlayWebView.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
            ivFullScreen.setImageDrawable(getResources().getDrawable(R.drawable.ic_fullscreen_exit_24));
        } else {
            toggledFullScreen = 0;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            frmVideoMain.getLayoutParams().height = (int) (displayMetrics.widthPixels * 0.5625);
            overlayWebView.getLayoutParams().height = (int) (displayMetrics.widthPixels * 0.5625);
            ivFullScreen.setImageDrawable(getResources().getDrawable(R.drawable.ic_fullscreen_24));
        }
        hideControls();
    }

    //activate handler to hide controls after 3 secs
    private void hideControls() {
        handler.postDelayed(runnable, 3000);
    }

    public void checkIfBuffering(int playback) {
        switch (playback) {
            case 0: {
                if (currentVideoDurationInSec > (videoTotalDurationInSec - 1)) {
                    currentVideoDurationInSec = 0;
                    webViewYoutubeCallBacks.seekBarPosition(videoTotalDurationInSec + "", currentVideoDurationInSec + "");
                    webViewYoutubeCallBacks.playback(2);
                } else {
                    if (currentVideoDurationInSec != 0) {
                        webViewYoutubeCallBacks.playback(3);
                    }
                }
                break;
            }
            case 1: {
                webViewYoutubeCallBacks.playback(1);
                break;
            }
            case 2: {
                webViewYoutubeCallBacks.playback(2);
                break;
            }
            case 3: {
                if (currentVideoDurationInSec != 0) {
                    webViewYoutubeCallBacks.playback(3);
                }
                break;
            }
            default: {
                webViewYoutubeCallBacks.playback(3);
            }
            break;
        }
    }
}