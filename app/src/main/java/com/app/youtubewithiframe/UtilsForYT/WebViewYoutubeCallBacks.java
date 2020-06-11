package com.app.youtubewithiframe.UtilsForYT;

public interface WebViewYoutubeCallBacks {
    public void seekBarPosition(String durationInSeconds, String seekSeconds); //set seekbar according to seconds
    public void totalDuration(String durationInSeconds); //total video duration in seconds
    public void playback(int isPlaying); //1 means playing 2 means paused (0 or any other is buffering)
}
