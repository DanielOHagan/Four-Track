package com.example.daniel.fourtrack.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.daniel.fourtrack.R;
import com.example.daniel.fourtrack.notifications.*;
import com.example.daniel.fourtrack.project.Project;
import com.example.daniel.fourtrack.track.*;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

public class ProjectActivity extends ProjectListActivity {

    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final String LOG_TAG = "ProjectActivity";

    public static final String APP_DIR_NAME = "Four Track";
    public static final String FILE_EXTENSION = ".3gp";
    public static final File APP_DIR = new File(
            Environment.getExternalStorageDirectory(), APP_DIR_NAME
    );

    private Project mCurrentProject;
    private ArrayList<Track> mTrackList;
    private TrackAdapter mTrackAdapter;
    private int mPosition;
    private TextView mProjectPageTitle;
    private ToggleButton mPlaySelectedTglBtn;

    //activity lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        if (!hasPermissions()) {
            requestPermissions();
        } else {
            if (!APP_DIR.exists()) {
                promptRemakeAppDir();
            }
        }

        loadProject();
        buildTrackRecyclerView();
        setProjectPageViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //updateTrackRecyclerView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (Track track : mTrackList) {
            if (track.getMediaPlayer() != null && track.getMediaPlayer().isPlaying()) {
                track.stopPlaying();
                track.getPlayToggleBtn().setChecked(false);
            }

            track.stopRecording();
            track.getRecordToggleBtn().setChecked(false);
            track.releaseAllTrackResources();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (Track track : mTrackList) {
            track.releaseAllTrackResources();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Track track : mTrackList) {
            track.releaseAllTrackResources();
        }
    }

    //permission methods
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(
                ProjectActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        ProjectActivity.this, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean shouldShowRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.RECORD_AUDIO) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void requestPermissions() {
        if (shouldShowRationale()) {
            new AlertDialog.Builder(this).setTitle("Permission needed")
                    .setMessage("Your permission is needed to run this part of the application")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ProjectActivity.this, new String[]{
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }, REQUEST_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        //if application directory doesn't exist then create a new one
                        if (!APP_DIR.exists()) {
                            createAppDir();
                        }
                    } else {
                        toMainActivity();
                    }
                }
            }
        }
    }

    //loading and saving methods
    private void loadProject() {
        //gets track list from JSON file
        //if track list doesn't exist in JSON then a new one is created

        Bundle parcelBundle = getIntent().getExtras();
        ArrayList<Project> mProjectList = getStoredProjectList();

        mPosition = 0;

        if (parcelBundle != null) {
            mPosition = parcelBundle.getInt("Project position");
        } else {
            //creates dialog box which forces the user to the main menu
            ErrorAlert errorDialogBox = new ErrorAlert(
                    "parcelBundle was null", ProjectActivity.this
            );
            errorDialogBox.displayError();
            Log.e(LOG_TAG, "loadProject() parcel is null");
        }

        mCurrentProject = mProjectList.get(mPosition);
        mTrackList = mCurrentProject.getTrackList();

        if (mTrackList == null) {
            mTrackList = new ArrayList<>();
        }
    }

    private void saveProjectData() {
        //saves the current state of the project list with their tracks

        ArrayList<Project> mProjectList = getStoredProjectList();
        Bundle parcelBundle = getIntent().getExtras();
        int position = 0;
        SharedPreferences sharedPreferences = getSharedPreferences(
                "sharedPreferences", MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json;

        if (parcelBundle != null) {
            position = parcelBundle.getInt("Project position");
        } else {
            ErrorAlert errorDialogBox = new ErrorAlert(
                    "parcelBundle was null", this
            );
            errorDialogBox.displayError();
            Log.e(LOG_TAG, "saveProjectData() parcel is null");
        }

        mCurrentProject.setTrackList(mTrackList);
        mProjectList.set(position, mCurrentProject);
        json = gson.toJson(mProjectList);
        editor.putString("Project list", json);
        editor.apply();
    }


    private void buildTrackRecyclerView() {
        RecyclerView mTrackRecyclerView = findViewById(R.id.trackRecyclerView);
        RecyclerView.LayoutManager mTrackLayoutManager = new LinearLayoutManager(this);
        mTrackAdapter = new TrackAdapter(mTrackList);

        mTrackRecyclerView.setLayoutManager(mTrackLayoutManager);
        mTrackRecyclerView.setAdapter(mTrackAdapter);

        //set the view's listeners
        mTrackAdapter.setOnTrackItemClickListener(new TrackAdapter.OnTrackItemClickListener() {

            @Override
            public void onPlayToggle(final int position, boolean isChecked) {
                if (isChecked) {
                    mTrackList.get(position).setMediaPlayCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            onTrackCompletion(position);
                        }
                    });

                    if (!isAnyTrackRecording() && !isAnyTrackPlaying()) {
                        //placed before the startPlaying call so
                        //this is only run if this is the only track in use
                        setScreenKeepOnFlag(true);
                    }

                    mTrackList.get(position).startPlaying();

                    if (mTrackList.get(position).getMediaPlayer() == null ||
                            (mTrackList.get(position).getMediaPlayer() != null &&
                            !mTrackList.get(position).getMediaPlayer().isPlaying()))
                    {
                        //checks if the track's media player failed to start
                        setScreenKeepOnFlag(false);
                    }
                } else {
                    mTrackList.get(position).pausePlaying();

                    if (!isAnyTrackRecording() && !isAnyTrackPlaying()) {
                        setScreenKeepOnFlag(false);
                    }
                }
            }

            @Override
            public void onRecordToggle(int position, boolean isChecked) {
                if (isChecked) {
                    if (ableToRecord(position)) {
                        if (!isAnyTrackRecording() && !isAnyTrackPlaying()) {
                            //placed before the startRecording call so
                            //this is only run if this is the only track in use
                            setScreenKeepOnFlag(true);
                        }

                        mTrackList.get(position).startRecording();

                        if (!mTrackList.get(position).isRecording()) {
                            //checks if the track's media recorder failed to start
                            setScreenKeepOnFlag(false);
                        }
                    } else {
                        mTrackList.get(position).getRecordToggleBtn().setChecked(false);
                    }
                } else {
                    mTrackList.get(position).stopRecording();

                    if (!isAnyTrackRecording() && !isAnyTrackPlaying()) {
                        setScreenKeepOnFlag(false);
                    }

                    saveProjectData();
                }
            }

            @Override
            public void onSettingsClick(int position) {
                displayTrackSettingsMenu(position);
            }

            @Override
            public void onSeekBarChange(int position, SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mTrackList.get(position).getMediaPlayer() != null) {
                    mTrackList.get(position).getMediaPlayer().seekTo(progress);
                }
            }

            @Override
            public void onSeekBarTouchStart(int position, SeekBar seekBar) {
                if (mTrackList.get(position).getMediaPlayer() != null &&
                        mTrackList.get(position).getMediaPlayer().isPlaying())
                {
                    mTrackList.get(position).pausePlaying();
                    mTrackList.get(position).getPlayToggleBtn().setChecked(false);
                }
            }

            @Override
            public void onSeekBarTouchEnd(int position, SeekBar seekBar) {

            }
        });
    }

    private void displayTrackSettingsMenu(final int position) {
        final TrackSettingsMenu trackSettingsMenu = new TrackSettingsMenu(
                ProjectActivity.this, mTrackList.get(position)
        );

        trackSettingsMenu.buildMenu();
        trackSettingsMenu.setTrackSettingsI(new TrackSettingsMenu.TrackSettingsMenuI() {
            @Override
            public void onDeleteClick() {
                final DeletePrompt confirmDelete = new DeletePrompt(ProjectActivity.this);

                confirmDelete.displayPrompt(mTrackList.get(position).getTrackName());
                confirmDelete.setDeletePromptI(new DeletePrompt.DeletePromptI() {
                    @Override
                    public void onDeleteClick() {
                        deleteTrack(position);
                        confirmDelete.getDialog().dismiss();
                        trackSettingsMenu.getDialog().dismiss();
                    }
                });
            }

            @Override
            public void onSaveNameClick() {
                String input = trackSettingsMenu.getMenuTitleEditText().getText().toString();

                if (isTrackNameAvailable(input)) {
                    mTrackList.get(position).setTrackName(input);
                    mTrackList.get(position).getTrackTitleTextView().setText(input);
                    saveProjectData();
                }
            }

            @Override
            public void onPlaySelectedClick(boolean isChecked) {
                mTrackList.get(position).setPlaySelectedChecked(isChecked);
                saveProjectData();
            }

            @Override
            public void onLoopClick(boolean isChecked) {
                mTrackList.get(position).setLooping(isChecked);
                saveProjectData();
            }
        });
    }

    //assign variables, listeners and other tasks in onCreate
    private void setProjectPageViews() {
        Button addNewTrackBtn = findViewById(R.id.addNewTrackBtn);

        mProjectPageTitle = findViewById(R.id.projectPageTitle);
        mPlaySelectedTglBtn = findViewById(R.id.playSelectedToggleBtn);

        mProjectPageTitle.setText(mCurrentProject.getProjectName());

        addNewTrackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptNewTrack();
            }
        });

        mProjectPageTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                promptNewProjectName();
                return false;
            }
        });

        mPlaySelectedTglBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mTrackList.isEmpty()) {
                    if (isChecked) {
                        playSelectedTracks();
                    } else {
                        pauseSelectedTracks();
                    }
                } else {
                    mPlaySelectedTglBtn.setChecked(false);
                }
            }
        });
    }

    //play selected methods
    private void playSelectedTracks() {
        if (!mTrackList.isEmpty()) {

            //stores whether any tracks are actually played
            boolean selectedTracksPlaying = false;

            for (Track track : mTrackList) {
                if (track.isPlaySelectedChecked() && track.hasAudioFile()) {
                    track.stopRecording();
                    track.getPlayToggleBtn().setChecked(true);
                    track.getRecordToggleBtn().setChecked(false);
                    track.startPlaying();
                    selectedTracksPlaying = true;
                }
            }

            mPlaySelectedTglBtn.setChecked(selectedTracksPlaying);
        }
    }

    private void pauseSelectedTracks() {
        for (Track track : mTrackList) {
            if (track.isPlaySelectedChecked()) {
                track.stopRecording();
                track.getPlayToggleBtn().setChecked(false);
                track.getRecordToggleBtn().setChecked(false);
                track.pausePlaying();
            }
        }
    }

    private boolean havePlaySelectedFinished() {
        //checks if the tracks that have 'play selected' checked are finished
        for (Track track : mTrackList) {
            //checks for tracks with play selected that are still playing
            if (track.isPlaySelectedChecked() && (
                    track.getMediaPlayer() != null && track.getMediaPlayer().isPlaying()
            )) {
                return false;
            }
        }
        return true;
    }


    //prompt methods ask the user for an input by displaying a pop up box
    private void promptNewTrack() {
        final TextInputPrompt textInputPrompt = new TextInputPrompt(ProjectActivity.this);

        textInputPrompt.displayPrompt(R.string.alert_track_name_hint);
        textInputPrompt.setTextInputPromptI(new TextInputPrompt.TextInputPromptI() {
            @Override
            public void onConfirmClick() {
                String input = textInputPrompt.getAlertEditText().getText().toString();
                final int MAX_TRACK_NUMBER = 4;

                if (mTrackList.size() < MAX_TRACK_NUMBER) {
                    if (isTrackNameAvailable(input)) {
                        addNewTrack(mTrackList.size(), input);
                        textInputPrompt.getDialog().dismiss();
                    }
                } else {
                    Toast.makeText(
                            textInputPrompt.getActivity(),
                            "Track limit reached",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    private void promptRemakeAppDir() {
        new AlertDialog.Builder(this).setTitle("Missing App directory")
                .setMessage(
                        "We can't find the app directory! Press continue to try to make a new directory. " +
                                "Having an app directory is required for the application to work."
                ).setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createAppDir();

                for (Track track : mTrackList) {
                    track.resetTrack();
                }
            }
        }).setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toMainActivity();
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!APP_DIR.exists()) {
                    toMainActivity();
                }
            }
        }).create().show();
    }

    private void promptNewProjectName() {
        final TextInputPrompt textInputPrompt = new TextInputPrompt(ProjectActivity.this);

        textInputPrompt.displayPrompt(R.string.alert_project_name_hint);
        textInputPrompt.setTextInputPromptI(new TextInputPrompt.TextInputPromptI() {
            @Override
            public void onConfirmClick() {
                String input = textInputPrompt.getAlertEditText().getText().toString();

                if (isProjectNameAvailable(input)) {
                    mCurrentProject.setProjectName(input);
                    mProjectPageTitle.setText(input);
                    saveProjectData();
                    textInputPrompt.getDialog().dismiss();
                }
            }
        });
    }


    //valid process checks
    private boolean isTrackNameAvailable(String trackName) {
        //checks if the input is valid, if not it notifies the user why
        final int CHARACTER_LIMIT = 16;

        if (!trackName.isEmpty()) {
            if (trackName.length() < CHARACTER_LIMIT) {
                for (Track track : mTrackList) {
                    if (trackName.equals(track.getTrackName())) {
                        Toast.makeText(
                                ProjectActivity.this,
                                "Name already taken",
                                Toast.LENGTH_SHORT
                        ).show();
                        return false;
                    }
                }
                return true;
            } else {
                Toast.makeText(
                        ProjectActivity.this,
                        "Must be less than " + CHARACTER_LIMIT + " characters",
                        Toast.LENGTH_SHORT
                ).show();
                return false;
            }
        } else {
            Toast.makeText(
                    ProjectActivity.this,
                    "Name can't be empty",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }
    }

    private boolean ableToRecord(int position) {
        for (int i = 0; i < mTrackList.size(); i++) {
            if (i != position) {
                if (mTrackList.get(i).getMediaRecorder() != null) {
                    Toast.makeText(
                            this,
                            "Cannot have more than one track recording at the same time",
                            Toast.LENGTH_SHORT
                    ).show();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isAnyTrackRecording() {
        for (Track track : mTrackList) {
            if (track.isRecording()) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnyTrackPlaying() {
        for (Track track : mTrackList) {
            if (track.getMediaPlayer() != null && track.getMediaPlayer().isPlaying()) {
                return true;
            }
        }
        return false;
    }


    private void addNewTrack(int position, String trackName) {
        mTrackList.add(
                position, new Track(trackName)
        );
        mTrackAdapter.notifyItemInserted(position);
        saveProjectData();
    }

    private void deleteTrack(int position) {
        //removes project from shared preferences

        SharedPreferences sharedPreferences = getSharedPreferences(
                "sharedPreferences", MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String projectKey = mTrackList.get(position).getTrackName();
        editor.remove(projectKey);
        editor.apply();

        mTrackList.get(position).deleteAudioFile();
        mTrackList.get(position).releaseAllTrackResources();

        //removes project from Recycler View and Array List
        mTrackList.remove(position);
        mTrackAdapter.notifyItemRemoved(position);

        saveProjectData();
    }

    private void createAppDir() {
        if (APP_DIR.mkdirs()) {
            Toast.makeText(
                    ProjectActivity.this,
                    "App directory created",
                    Toast.LENGTH_SHORT
            ).show();
            Log.i(LOG_TAG, "App directory created at path: " + APP_DIR.getAbsolutePath());
        } else {
            ErrorAlert errorDialogBox = new ErrorAlert(
                    "Failed to create app folder in external storage",
                    ProjectActivity.this
            );
            errorDialogBox.displayError();
            Log.e(LOG_TAG, "createAppDir() directory creation failed");
        }
    }

    private void toMainActivity() {
        Intent intent = new Intent(
                ProjectActivity.this, MainActivity.class
        );
        startActivity(intent);
    }

    private void setScreenKeepOnFlag(boolean keepOn) {
        //if the track is playing or recording then the screen should
        //stay on until finished
        if (keepOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void onTrackCompletion(int position) {
        if (mTrackList.get(position).getMediaPlayer() != null &&
                !mTrackList.get(position).getMediaPlayer().isLooping())
        {
            if (havePlaySelectedFinished()) {
                mPlaySelectedTglBtn.setChecked(false);
            }

            mTrackList.get(position).getPlayToggleBtn().setChecked(false);
        }

        if (!isAnyTrackRecording() && !isAnyTrackPlaying()) {
            setScreenKeepOnFlag(false);
        }
    }
}