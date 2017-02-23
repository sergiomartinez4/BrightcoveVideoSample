package com.brightcove.brightcovevideosample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.event.Default;
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

    private static final String TAG = MainActivity.class.getName();

    private LifecycleUtil lifecycleUtil;
    private BrightcoveVideoView brightcoveVideoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initVideoView();
        if (brightcoveVideoView != null &&
                (lifecycleUtil == null || lifecycleUtil.baseVideoView != brightcoveVideoView)) {
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        lifecycleUtil.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            emitEvent(EventType.ENTER_FULL_SCREEN);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            emitEvent(EventType.EXIT_FULL_SCREEN);
        }
    }

    private void emitEvent(String eventType) {
        if (brightcoveVideoView != null) {
            brightcoveVideoView.getEventEmitter().emit(eventType);
        }
    }

    private void initVideoView() {
        if (brightcoveVideoView == null) {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.activity_main);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams (
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getVideoHeight()
            );

            brightcoveVideoView = new BrightcoveExoPlayerVideoView(this);
            brightcoveVideoView.setMediaController((BrightcoveMediaController) null);
            brightcoveVideoView.finishInitialization();

            linearLayout.addView(brightcoveVideoView, layoutParams);
            linearLayout.addView(createController());
        }
    }

    private int getVideoHeight() {
        Point size = getDisplaySize();
        int height;
        if (size.x > size.y) {
            height = size.x;
        } else {
            height = size.y;
        }

        return height/2;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private Point getDisplaySize() {
        WindowManager windowManager = (WindowManager) this.getApplicationContext().getSystemService(Activity.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(point);
        } else {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        return point;
    }

    //Creating one button to handle Play and Pause
    private LinearLayout createController() {
        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.HORIZONTAL);
        parent.setGravity(Gravity.CENTER_HORIZONTAL);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.gravity = Gravity.CENTER_HORIZONTAL;

        Button btnPlay = new Button(this);
        btnPlay.setBackground(makeSelector());
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (brightcoveVideoView != null) {
                    if (brightcoveVideoView.isPlaying()) {
                        brightcoveVideoView.getEventEmitter().emit(EventType.PAUSE);
                    } else {
                        brightcoveVideoView.getEventEmitter().emit(EventType.PLAY);
                    }
                }
            }
        });

        parent.addView(btnPlay, buttonParams);
        return parent;
    }

    private StateListDrawable makeSelector() {
        StateListDrawable res = new StateListDrawable();
        res.setExitFadeDuration(400);
        res.setAlpha(45);
        res.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.mipmap.play_pause_pressed));
        res.addState(new int[]{}, getResources().getDrawable(R.mipmap.play_pause));
        return res;
    }

    private void initListeners(@NonNull EventEmitter eventEmitter) {
        //Adding one event to a Event Listener
        eventEmitter.on(EventType.PROGRESS, new EventListener() {
            @Override
            public void processEvent(Event event) {
                //Log.v(TAG, "Reporting progress: "+ event.getIntegerProperty(Event.PLAYHEAD_POSITION));
            }
        });

        //For more on how the events work, please visit https://docs.brightcove.com/en/video-cloud/mobile-sdks/brightcove-player-sdk-for-android/getting-started/understanding-events.html
        EventListener myListener = new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.i(TAG, "EventType:"+event.getType());
                switch(event.getType()) {
                    case EventType.PLAY:
                    case EventType.DID_PLAY:
                    case EventType.PAUSE:
                    case EventType.DID_PAUSE:
                        //Do something.
                        break;
                    case EventType.VIDEO_SIZE_KNOWN:
                        int height = event.getIntegerProperty(Event.VIDEO_HEIGHT);
                        int width = event.getIntegerProperty(Event.VIDEO_WIDTH);
                        Log.i(TAG, "Video height:"+height +", width:"+width);
                        break;
                    case EventType.VIDEO_DURATION_CHANGED:
                        int duration = event.getIntegerProperty(Event.VIDEO_DURATION);
                        Log.i(TAG, "Video duration:"+duration);
                        break;
                    case EventType.DID_SET_VIDEO:
                        Video video = (Video) event.properties.get(Event.VIDEO);

                        Log.i(TAG, "Video set - :"+video.toString());
                        break;
                    default:
                        break;
                }
            }
        };
        //Adding several events to the same Event Listener.
        eventEmitter.on(EventType.PLAY, myListener);
        eventEmitter.on(EventType.DID_PLAY, myListener);
        eventEmitter.on(EventType.PAUSE, myListener);
        eventEmitter.on(EventType.DID_PAUSE, myListener);
        eventEmitter.on(EventType.DID_PAUSE, myListener);
        eventEmitter.on(EventType.VIDEO_SIZE_KNOWN, myListener);
        eventEmitter.on(EventType.VIDEO_DURATION_CHANGED, myListener);
        eventEmitter.on(EventType.DID_SET_VIDEO, myListener);

    }


    //Setting LifeCycleUtil calls

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
