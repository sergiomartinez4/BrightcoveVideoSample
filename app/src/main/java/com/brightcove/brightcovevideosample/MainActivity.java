package com.brightcove.brightcovevideosample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.mediacontroller.BrightcoveMediaController;
import com.brightcove.player.model.Video;
import com.brightcove.player.util.LifecycleUtil;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.brightcove.player.view.BrightcoveVideoView;

public class MainActivity extends AppCompatActivity {

    private LifecycleUtil lifecycleUtil;
    private BrightcoveVideoView brightcoveVideoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        brightcoveVideoView = (BrightcoveExoPlayerVideoView) findViewById(R.id.brightcove_video_view);
        brightcoveVideoView.setMediaController((BrightcoveMediaController) null);


        if (lifecycleUtil == null || lifecycleUtil.baseVideoView != brightcoveVideoView) {
            lifecycleUtil = new LifecycleUtil(brightcoveVideoView);
            lifecycleUtil.onCreate(savedInstanceState, this);
        }

        EventEmitter eventEmitter = brightcoveVideoView.getEventEmitter();

        Catalog catalog = new Catalog(eventEmitter, getString(R.string.account), getString(R.string.policy));
        catalog.findVideoByID(getString(R.string.videoId), new VideoListener() {

            // Add the video found to the queue with add().
            // Start playback of the video with start().
            @Override
            public void onVideo(Video video) {
                brightcoveVideoView.add(video);
                brightcoveVideoView.start();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        lifecycleUtil.activityOnStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lifecycleUtil.activityOnPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        lifecycleUtil.activityOnResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        lifecycleUtil.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lifecycleUtil.activityOnDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        lifecycleUtil.activityOnStop();
    }
}
