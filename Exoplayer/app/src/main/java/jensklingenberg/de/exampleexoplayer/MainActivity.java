package jensklingenberg.de.exampleexoplayer;

import android.app.Activity;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;

import java.util.Formatter;
import java.util.Locale;

/*
* Created by: Jens Klingenberg (jensklingenberg.de)
* GPLv3
*
*
* */

public class MainActivity extends Activity {

    private SurfaceView surfaceView;
    private ExoPlayer exoPlayer;
    private boolean bAutoplay=false;
    private boolean bIsPlaying=false;
    private boolean bControlsActive=true;
    private ImageButton btnPlay;
    private ImageButton btnFwd;
    private ImageButton btnPrev;
    private ImageButton btnRew;
    private ImageButton btnNext;
    private int RENDERER_COUNT = 300000;// Je Hochauflösender desto mehr
    private int minBufferMs =    250000;

    private final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private final int BUFFER_SEGMENT_COUNT = 256;
    private LinearLayout mediaController;
    private SeekBar seekPlayerProgress;
    private Handler handler;
    private TextView  txtCurrentTime;
    private TextView txtEndTime;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_main_layout);
        surfaceView = (SurfaceView) findViewById(R.id.sv_player);
        mediaController = (LinearLayout) findViewById(R.id.lin_media_controller);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initPlayer(0);
        initMediaControls();

        if(bAutoplay){
            exoPlayer.setPlayWhenReady(true);
            bIsPlaying=true;
            setProgress();
        }

    }

    private void initMediaControls() {
        initSurfaceView();
        initPlayButton();
        initSeekBar();
        initTxtTime();
        initFwd();
        initPrev();
        initRew();
        initNext();

    }

    private void initNext() {
        btnNext = (ImageButton) findViewById(R.id.next);
        btnNext.requestFocus();
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exoPlayer.seekTo(exoPlayer.getDuration());
            }
        });
    }

    private void initRew() {
        btnRew = (ImageButton) findViewById(R.id.rew);
        btnRew.requestFocus();
        btnRew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exoPlayer.seekTo(exoPlayer.getCurrentPosition()-10000);
            }
        });
    }

    private void initPrev() {
        btnPrev = (ImageButton) findViewById(R.id.prev);
        btnPrev.requestFocus();
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exoPlayer.seekTo(0);
            }
        });


    }


    private void initFwd() {
        btnFwd = (ImageButton) findViewById(R.id.ffwd);
        btnFwd.requestFocus();
        btnFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exoPlayer.seekTo(exoPlayer.getCurrentPosition()+10000);
            }
        });


    }

    private void initTxtTime() {
        txtCurrentTime = (TextView) findViewById(R.id.time_current);
        txtEndTime = (TextView) findViewById(R.id.player_end_time);
    }

    private void initSurfaceView() {
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMediaControls();
            }
        });
    }

    private String stringForTime(int timeMs) {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds =  timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void setProgress() {
        seekPlayerProgress.setProgress(0);
        seekPlayerProgress.setMax(0);
        seekPlayerProgress.setMax((int) exoPlayer.getDuration()/1000);


        handler = new Handler();
        //Make sure you update Seekbar on UI thread
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (exoPlayer != null && bIsPlaying ) {
                    seekPlayerProgress.setMax(0);
                    seekPlayerProgress.setMax((int) exoPlayer.getDuration()/1000);
                    int mCurrentPosition = (int) exoPlayer.getCurrentPosition() / 1000;
                    seekPlayerProgress.setProgress(mCurrentPosition);
                    txtCurrentTime.setText(stringForTime((int)exoPlayer.getCurrentPosition()));
                    txtEndTime.setText(stringForTime((int)exoPlayer.getDuration()));

                    handler.postDelayed(this, 1000);
                }

            }
        });


    }

    private void initSeekBar() {
        seekPlayerProgress = (SeekBar) findViewById(R.id.mediacontroller_progress);
        seekPlayerProgress.requestFocus();

        seekPlayerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }

                exoPlayer.seekTo(progress*1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

            seekPlayerProgress.setMax(0);
            seekPlayerProgress.setMax((int) exoPlayer.getDuration()/1000);

    }


    private void toggleMediaControls() {

        if(bControlsActive){
            hideMediaController();
            bControlsActive=false;

        }else{
            showController();
            bControlsActive=true;
            setProgress();
        }
    }

    private void showController() {
        mediaController.setVisibility(View.VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void hideMediaController() {
        mediaController.setVisibility(View.GONE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void initPlayButton() {
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnPlay.requestFocus();
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if(bIsPlaying){
                        exoPlayer.setPlayWhenReady(false);
                        bIsPlaying=false;
                    }else{
                        exoPlayer.setPlayWhenReady(true);
                        bIsPlaying=true;
                        setProgress();
                    }
            }
        });
    }

    private void initPlayer(int position) {
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:40.0) Gecko/20100101 Firefox/40.0";

        String url = "http://www.sample-videos.com/video/mp4/480/big_buck_bunny_480p_5mb.mp4";
        Allocator allocator = new DefaultAllocator(minBufferMs);
        DataSource dataSource = new DefaultUriDataSource(this, null, userAgent);


        ExtractorSampleSource sampleSource = new ExtractorSampleSource( Uri.parse(url), dataSource, allocator,
                BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);

        MediaCodecVideoTrackRenderer videoRenderer = new
                MediaCodecVideoTrackRenderer(this, sampleSource, MediaCodecSelector.DEFAULT,
                MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);

        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);

        exoPlayer = ExoPlayer.Factory.newInstance(RENDERER_COUNT);
        exoPlayer.prepare(videoRenderer, audioRenderer);
        exoPlayer.sendMessage(videoRenderer,
                MediaCodecVideoTrackRenderer.MSG_SET_SURFACE,
                surfaceView.getHolder().getSurface());
        exoPlayer.seekTo(position);


    }


}
