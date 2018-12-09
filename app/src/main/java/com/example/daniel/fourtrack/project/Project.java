package com.example.daniel.fourtrack.project;

import android.widget.TextView;

import com.example.daniel.fourtrack.track.Track;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Project {

    @SerializedName("project_name")
    private String mProjectName;
    @SerializedName("track_list")
    private ArrayList<Track> mTrackList;

    private transient TextView mProjectTitleTextView;

    public Project(String projectName, ArrayList<Track> trackList) {
        mProjectName = projectName;
        mTrackList = trackList;
    }

    public void setProjectTitleTextView(TextView projectTitleTextView) {
        mProjectTitleTextView = projectTitleTextView;
    }

    public void setProjectName(String projectName) {
        mProjectName = projectName;
    }

    public void setTrackList(ArrayList<Track> trackList) {
        mTrackList = trackList;
    }

    public String getProjectName() {
        return mProjectName;
    }

    public ArrayList<Track> getTrackList() {
        return mTrackList;
    }

    public TextView getProjectTitleTextView() {
        return mProjectTitleTextView;
    }

    public void deleteProjectAudio() {
        for (Track track : mTrackList) {
            track.deleteAudioFile();
        }
    }
}