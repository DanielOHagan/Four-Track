package com.example.daniel.fourtrack.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.daniel.fourtrack.R;
import com.example.daniel.fourtrack.notifications.DeletePrompt;
import com.example.daniel.fourtrack.notifications.TextInputPrompt;
import com.example.daniel.fourtrack.project.*;
import com.example.daniel.fourtrack.track.Track;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ProjectListActivity extends AppCompatActivity {

    private ArrayList<Project> mProjectList;
    private ProjectAdapter mProjectAdapter;

    //activity lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        mProjectList = getStoredProjectList();

        if (mProjectList == null) {
            mProjectList = new ArrayList<>();
        }

        buildRecyclerView();

        findViewById(R.id.addNewProjectBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptNewProject();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProjectList();
    }

    //loading and saving methods
    private void saveProjectList() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                "sharedPreferences", MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mProjectList);

        editor.putString("Project list", json);
        editor.apply();
    }

    protected ArrayList<Project> getStoredProjectList() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                "sharedPreferences", MODE_PRIVATE
        );
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Project list", null);

        Type type = new TypeToken<ArrayList<Project>>() {}.getType();

        return gson.fromJson(json, type);
    }

    //recycler view methods
    private void buildRecyclerView() {
        RecyclerView mRecyclerView = findViewById(R.id.projectsRecyclerView);
        RecyclerView.LayoutManager mProjectLayoutManager = new LinearLayoutManager(this);
        mProjectAdapter = new ProjectAdapter(mProjectList);

        mRecyclerView.setLayoutManager(mProjectLayoutManager);
        mRecyclerView.setAdapter(mProjectAdapter);

        mProjectAdapter.setOnItemClickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(
                        ProjectListActivity.this, ProjectActivity.class
                );
                Bundle parcelBundle = new Bundle();
                parcelBundle.putInt("Project position", position);
                intent.putExtras(parcelBundle);
                startActivity(intent);
            }

            @Override
            public void onSettingsClick(int position) {
                displayProjectSettingsMenu(
                        position, ProjectListActivity.this
                );
            }
        });
    }

    private void updateProjectList() {
        //updates mProjectList to contain the new Project name
        ArrayList<Project> projectArrayList = getStoredProjectList();

        if (projectArrayList != null) {
            for (int i = 0; i < projectArrayList.size(); i++) {
                //copy the saved data onto the variable data
                mProjectList.get(i).setProjectName(projectArrayList.get(i).getProjectName());
                mProjectList.get(i).setTrackList(projectArrayList.get(i).getTrackList());
                for (int k = 0; k < projectArrayList.get(i).getTrackList().size(); k++) {
                    mProjectList.get(i).getTrackList().get(k).setLooping(
                            projectArrayList.get(i).getTrackList().get(k).isLooping()
                    );
                    mProjectList.get(i).getTrackList().get(k).setPlaySelectedChecked(
                            projectArrayList.get(i).getTrackList().get(k).isPlaySelectedChecked()
                    );
                }
            }

            //update the project recycler view
            if (mProjectList != null) {
                for (Project project : mProjectList) {
                    if (project != null && project.getProjectTitleTextView() != null) {
                        project.getProjectTitleTextView().setText(project.getProjectName());
                    }
                }
            }


        }
    }

    //prompt methods ask the user for an input by displaying a pop up box
    private void promptNewProject() {
        final TextInputPrompt textInputPrompt = new TextInputPrompt(ProjectListActivity.this);

        textInputPrompt.displayPrompt(R.string.alert_project_name_hint);
        textInputPrompt.setTextInputPromptI(new TextInputPrompt.TextInputPromptI() {
            @Override
            public void onConfirmClick() {
                //sets the confirm button to check that the input is suitable to progress
                String input = textInputPrompt.getAlertEditText().getText().toString();

                if (isProjectNameAvailable(input)) {
                    addNewProject(mProjectList.size(), input);
                    textInputPrompt.getDialog().dismiss();
                }
            }
        });
    }

    private void displayProjectSettingsMenu(final int position, Activity currentContext) {
        final ProjectSettingsMenu projectSettingsMenu = new ProjectSettingsMenu(
                currentContext, mProjectList.get(position)
        );
        projectSettingsMenu.buildMenu();

        projectSettingsMenu.setProjectSettingsMenuI(new ProjectSettingsMenu.ProjectSettingsMenuI() {
            @Override
            public void onDeleteClick() {
                final DeletePrompt confirmDelete = new DeletePrompt(ProjectListActivity.this);

                confirmDelete.displayPrompt(mProjectList.get(position).getProjectName());
                confirmDelete.setDeletePromptI(new DeletePrompt.DeletePromptI() {
                    @Override
                    public void onDeleteClick() {
                        deleteProject(position);
                        confirmDelete.getDialog().dismiss();
                        projectSettingsMenu.getDialog().dismiss();
                    }
                });
            }

            @Override
            public void onSaveNameClick() {
                String input = projectSettingsMenu.getMenuTitleEditText().getText().toString();

                if (isProjectNameAvailable(input)) {
                    mProjectList.get(position).setProjectName(input);

                    Toast.makeText(ProjectListActivity.this,
                            "Project name saved",
                            Toast.LENGTH_SHORT).show();

                    if (mProjectList.get(position).getProjectTitleTextView() != null) {
                        mProjectList.get(position).getProjectTitleTextView().setText(input);
                    }

                    saveProjectList();
                }
            }
        });
    }

    //valid process checks
    protected boolean isProjectNameAvailable(String projectName) {
        //checks if the input is valid, if not it notifies the user why
        int CHARACTER_LIMIT = 16;

        if (!projectName.isEmpty()) {
            if (projectName.length() < CHARACTER_LIMIT) {
                for (int i = 0; i < mProjectList.size(); i++) {
                    if (projectName.equals(mProjectList.get(i).getProjectName())) {
                        Toast.makeText(ProjectListActivity.this,
                                "Name already taken",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                return true;
            } else {
                Toast.makeText(ProjectListActivity.this,
                        "Must be less than " + CHARACTER_LIMIT + " characters",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(ProjectListActivity.this,
                    "Name can't be empty",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    private void addNewProject(int position, String projectName) {
        mProjectList.add(
                position, new Project(projectName, new ArrayList<Track>())
        );
        mProjectAdapter.notifyItemInserted(position);
        saveProjectList();
    }

    private void deleteProject(int position) {
        //remove project from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(
                "sharedPreferences", MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String projectKey = mProjectList.get(position).getProjectName();
        editor.remove(projectKey);
        editor.apply();

        mProjectList.get(position).deleteProjectAudio();

        //remove project from Recycler View and Array List
        mProjectList.remove(position);
        mProjectAdapter.notifyItemRemoved(position);

        saveProjectList();
    }
}