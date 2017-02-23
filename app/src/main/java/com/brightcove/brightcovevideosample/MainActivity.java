package com.brightcove.brightcovevideosample;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.event.Component;
import com.brightcove.player.event.Default;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.event.ListensFor;
import com.brightcove.player.mediacontroller.BrightcoveMediaController;
import com.brightcove.player.model.Video;
import com.brightcove.player.util.LifecycleUtil;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.brightcove.player.view.BrightcovePlayer;
import com.brightcove.player.view.BrightcoveVideoView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private LifecycleUtil lifecycleUtil;
    private BrightcoveVideoView brightcoveVideoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initVideoView();
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

        initListeners(eventEmitter);
    }

    private void initVideoView() {
        if (brightcoveVideoView == null) {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.activity_main);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            brightcoveVideoView = new BrightcoveExoPlayerVideoView(this);
            brightcoveVideoView.setMediaController((BrightcoveMediaController) null);
            brightcoveVideoView.finishInitialization();

            linearLayout.addView(brightcoveVideoView, layoutParams);
        }
    }

    private void initListeners(@NonNull EventEmitter eventEmitter) {
        eventEmitter.on(EventType.PROGRESS, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.v(TAG, "Reporting progress: "+ event.getIntegerProperty(Event.PLAYHEAD_POSITION));
            }
        });

        EventListener myListener = new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.i(TAG, "EventType:"+event.getType());
                switch(event.getType()) {
                    case EventType.PLAY:
                        break;
                    case EventType.DID_PLAY:
                        break;
                    case EventType.PAUSE:
                        break;
                    case EventType.DID_PAUSE:
                        break;
                }
            }
        };
        //Adding events to the event emitter
        eventEmitter.on(EventType.PLAY, myListener);
        eventEmitter.on(EventType.DID_PLAY, myListener);
        eventEmitter.on(EventType.PAUSE, myListener);
        eventEmitter.on(EventType.DID_PAUSE, myListener);

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

    @Override
    protected void onSaveInstanceState(final Bundle bundle) {
        // Give all the listeners a chance to run before calling super.
        brightcoveVideoView.getEventEmitter().on(EventType.ACTIVITY_SAVE_INSTANCE_STATE, new EventListener() {
            @Override
            @Default
            public void processEvent(Event event) {
                MainActivity.super.onSaveInstanceState(bundle);
            }
        });

        lifecycleUtil.activityOnSaveInstanceState(bundle);
    }
}
