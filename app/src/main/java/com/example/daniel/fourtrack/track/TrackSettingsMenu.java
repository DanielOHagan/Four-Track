package com.example.daniel.fourtrack.track;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.example.daniel.fourtrack.R;

public class TrackSettingsMenu {

    public interface TrackSettingsMenuI {
        void onDeleteClick();
        void onSaveNameClick();
        void onPlaySelectedClick(boolean isChecked);
        void onLoopClick(boolean isChecked);
    }

    private final Track mSelectedTrack;
    private final Activity mCurrentActivity;
    private TrackSettingsMenuI mTrackSettingsI;
    private AlertDialog mDialog;
    private EditText mMenuTitleEditText;

    public TrackSettingsMenu(Activity mCurrentActivity, Track mSelectedTrack) {
        this.mCurrentActivity = mCurrentActivity;
        this.mSelectedTrack = mSelectedTrack;
    }

    public void buildMenu() {
        final AlertDialog.Builder dialogBoxBuilder = new AlertDialog.Builder(mCurrentActivity);
        View menuView = mCurrentActivity.getLayoutInflater().inflate(
                R.layout.track_settings_menu, null
        );
        Button menuCloseBtn = menuView.findViewById(R.id.trackCloseBtn);
        Button menuStopBtn = menuView.findViewById(R.id.trackStopBtn);
        Button menuDeleteBtn = menuView.findViewById(R.id.trackDeleteBtn);
        Button menuSaveNameBtn = menuView.findViewById(R.id.trackSaveNameBtn);
        CheckBox menuPlaySelected = menuView.findViewById(R.id.trackPlaySelectedCheck);
        CheckBox menuLoop = menuView.findViewById(R.id.trackLoopCheckBox);

        mMenuTitleEditText = menuView.findViewById(R.id.trackTitle);

        dialogBoxBuilder.setView(menuView);
        mMenuTitleEditText.setHint(mSelectedTrack.getTrackName());
        menuPlaySelected.setChecked(mSelectedTrack.isPlaySelectedChecked());
        menuLoop.setChecked(mSelectedTrack.isLooping());

        //set listeners for menu's views
        menuCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        menuStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this works but doesn't reset the seek bar so when used it
                //appears that the stop button doesn't work.
                //this button's main function is to release resources
                mSelectedTrack.stopPlaying();
            }
        });

        menuDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTrackSettingsI.onDeleteClick();
            }
        });

        menuSaveNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTrackSettingsI.onSaveNameClick();
            }
        });

        menuPlaySelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTrackSettingsI.onPlaySelectedClick(isChecked);
            }
        });

        menuLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTrackSettingsI.onLoopClick(isChecked);
            }
        });

        mDialog = dialogBoxBuilder.create();
        mDialog.show();
    }

    public void setTrackSettingsI(TrackSettingsMenuI trackSettingsI) {
        mTrackSettingsI = trackSettingsI;
    }

    public EditText getMenuTitleEditText() {
        return mMenuTitleEditText;
    }

    public AlertDialog getDialog() {
        return mDialog;
    }
}