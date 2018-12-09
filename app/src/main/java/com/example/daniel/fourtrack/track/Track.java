package com.example.daniel.fourtrack.track;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.daniel.fourtrack.R;
import com.example.daniel.fourtrack.activities.ProjectActivity;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Track {

    private static final transient String LOG_TAG = "Track";

    @SerializedName("track_name")
    private String mTrackName;
    @SerializedName("track_id")
    private final String mTrackId;
    @SerializedName("file_path")
    private String mFilePath;
    @SerializedName("is_play_selected_checked")
    private boolean mPlaySelectedChecked;
    @SerializedName("is_looping")
    private boolean mLooping;
    @SerializedName("file_duration")
    private int mAudioFileDuration;

    private Boolean mRecording = false;

    private transient MediaRecorder mMediaRecorder;
    private transient MediaPlayer mMediaPlayer;
    private transient File mAudioRecording;
    private transient ToggleButton mPlayToggleBtn, mRecordToggleBtn;
    private transient SeekBar mSeekBar;
    private transient TextView mTrackTitleTextView, mTrackCurrentTimeTextView, mTrackDurationTextView;
    private transient MediaPlayer.OnCompletionListener mMediaPlayCompletionListener;

    public Track(String trackName) {
        mTrackName = trackName;
        mTrackId = UUID.randomUUID().toString();
    }

    public void setTrackCurrentTimeTextView(TextView trackCurrentTimeTextView) {
        mTrackCurrentTimeTextView = trackCurrentTimeTextView;
    }

    public void setTrackDurationTextView(TextView trackDurationTextView) {
        mTrackDurationTextView = trackDurationTextView;
    }

    public void setTrackName(String trackName) {
        mTrackName = trackName;
    }

    public void setSeekBar(SeekBar seekBar) {
        mSeekBar = seekBar;
    }

    public void setRecordToggleBtn(ToggleButton recordToggleBtn) {
        mRecordToggleBtn = recordToggleBtn;
    }

    public void setPlayToggleBtn(ToggleButton playToggleBtn) {
        mPlayToggleBtn = playToggleBtn;
    }

    public void setTrackTitleTextView(TextView trackTitleTextView) {
        mTrackTitleTextView = trackTitleTextView;
    }

    public void setLooping(boolean looping) {
        mLooping = looping;
    }

    public void setPlaySelectedChecked(boolean playSelectedChecked) {
        mPlaySelectedChecked = playSelectedChecked;
    }

    public void setMediaPlayCompletionListener(MediaPlayer.OnCompletionListener mediaPlayCompletionListener) {
        mMediaPlayCompletionListener = mediaPlayCompletionListener;
    }

    public void resetTrack() {
        //called when the app directory is re-created so the views display
        //no information about the files that no longer exist
        resetSeekBar();
        mPlayToggleBtn.setChecked(false);
        mRecordToggleBtn.setChecked(false);
        mRecording = false;
    }

    public boolean isLooping() {
        return mLooping;
    }

    public boolean isPlaySelectedChecked() {
        return mPlaySelectedChecked;
    }

    public boolean isRecording() {
        return mRecording;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public MediaRecorder getMediaRecorder() {
        return mMediaRecorder;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public TextView getTrackTitleTextView() {
        return mTrackTitleTextView;
    }

    public ToggleButton getRecordToggleBtn() {
        return mRecordToggleBtn;
    }

    public ToggleButton getPlayToggleBtn() {
        return mPlayToggleBtn;
    }

    public void startPlaying() {
        checkAudioRecording();
        if (mAudioRecording.exists() && !mRecordToggleBtn.isChecked()) {
            if (mMediaPlayer == null) {
                //create new media player if instance doesn't currently exist
                mMediaPlayer = new MediaPlayer();

                try {
                    mMediaPlayer.setDataSource(mAudioRecording.getAbsolutePath());
                } catch (IOException e) {
                    Log.e("Track playback", "setDataSource() failed");
                }

                try {
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    Log.e("Track playback", "prepare() failed");
                }
            }

            mMediaPlayer.start();
            mMediaPlayer.setLooping(mLooping);
            mMediaPlayer.setOnCompletionListener(mMediaPlayCompletionListener);

            updateTrackViews();
            updateTrackTime();
        } else {
            mPlayToggleBtn.setChecked(false);
        }
    }

    public void stopPlaying() {
        //sets the media player position to 0
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    public void pausePlaying() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
    }

    public void startRecording() {
        if (!mPlayToggleBtn.isChecked()) {
            configureMediaRecorder();
            try {
                mMediaRecorder.prepare();
            } catch (IOException e) {
                Log.e("Media Recorder", "prepare() failed");
            }
            mMediaRecorder.start();
            mRecording = true;
        } else {
            mRecordToggleBtn.setChecked(false);
        }
    }

    private void configureMediaRecorder() {
        MediaRecorder mediaRecorder = new MediaRecorder();

        checkAudioRecording();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(mAudioRecording.getAbsolutePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        mMediaRecorder = mediaRecorder;
    }

    public void stopRecording() {
        //has this null pointer check so this method can be called even when
        //not recording so we can be sure that the app is not recording
        if (mMediaRecorder != null) {

            try {
                mMediaRecorder.stop();
            } catch (RuntimeException e) {
                //deletes the file that is unusable by the media player
                e.printStackTrace();
                if (!mAudioRecording.delete()) {
                    Log.e(
                            LOG_TAG,
                            "stopRecording() failed to delete recording after " +
                                    "media recorder failed to stop"
                    );
                }
                mAudioRecording = null;
            }

            //release media recorder and reset the media player
            mSeekBar.setProgress(0);
            mTrackCurrentTimeTextView.setText(R.string.track_time_default);
            releaseAllTrackResources();
            checkAudioRecording();
            updateTrackViews();
            mRecording = false;
        }
    }

    public void deleteAudioFile() {
        checkAudioRecording();

        if (mAudioRecording != null) {
            if (mAudioRecording.exists()) {
                if (!mAudioRecording.delete()) {
                    Log.e("Track file", "Failed to delete() audio file");
                }
            }
        }
    }

    public boolean hasAudioFile() {
        return (mAudioRecording != null && mAudioRecording.exists());
    }

    public void releaseAllTrackResources() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void checkAudioRecording() {
        //makes mAudioRecording suitable for use
        String filePath = mFilePath;

        if (mAudioRecording == null) {
            if (filePath == null) {
                //sets file path if the track doesn't have a recording (before first recording or after deletion)
                filePath = ProjectActivity.APP_DIR.getAbsolutePath() + "/" +
                        mTrackId + ProjectActivity.FILE_EXTENSION;
            }
            mFilePath = filePath;
            mAudioRecording = new File(filePath);
        }
    }

    private void updateTrackTime() {
        //creates a separate thread to change the seek bar and text views to
        //display the track's current time
        Runnable runnable;
        Handler handler = new Handler();

        if (mMediaPlayer != null) {
            mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
            mTrackCurrentTimeTextView.setText(String.valueOf(
                    new SimpleDateFormat("mm:ss").format(new Date(
                            mMediaPlayer.getCurrentPosition()
                    ))
            ));

            if (mMediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        updateTrackTime();
                    }
                };

                handler.postDelayed(runnable, 5);
            }
        }
    }

    private void updateTrackViews() {
        //updates the mSeekBar and Text Views to display correct data
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(mFilePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mAudioFileDuration = mediaPlayer.getDuration();

        mSeekBar.setMax(mAudioFileDuration);
        mTrackDurationTextView.setText(String.valueOf(
                new SimpleDateFormat("mm:ss").format(new Date(
                        mAudioFileDuration
                ))
        ));

        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void resetSeekBar() {
        mSeekBar.setMax(100);
        mSeekBar.setProgress(0);
        mTrackCurrentTimeTextView.setText(R.string.track_time_default);
        mTrackDurationTextView.setText(R.string.track_time_default);
    }
}