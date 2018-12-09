package com.example.daniel.fourtrack.project;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.daniel.fourtrack.R;

public class ProjectSettingsMenu {

    public interface ProjectSettingsMenuI {
        void onDeleteClick();
        void onSaveNameClick();
    }

    private final Project mSelectedProject;
    private final Activity mCurrentActivity;
    private ProjectSettingsMenuI mProjectSettingsMenuI;
    private AlertDialog mDialog;
    private EditText mMenuTitleEditText;

    public ProjectSettingsMenu(Activity currentActivity, Project selectedProject) {
        mCurrentActivity = currentActivity;
        mSelectedProject = selectedProject;
    }

    public void buildMenu() {
        final AlertDialog.Builder dialogBoxBuilder = new AlertDialog.Builder(mCurrentActivity);
        View menuView = mCurrentActivity.getLayoutInflater().inflate(
                R.layout.project_settings_menu, null
        );
        Button menuDeleteBtn = menuView.findViewById(R.id.projectDeleteBtn);
        Button menuSaveNameBtn = menuView.findViewById(R.id.projectSaveNameBtn);

        mMenuTitleEditText = menuView.findViewById(R.id.projectTitleEditText);

        dialogBoxBuilder.setView(menuView);
        mMenuTitleEditText.setHint(mSelectedProject.getProjectName());

        menuView.findViewById(R.id.projectCloseBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        menuDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProjectSettingsMenuI.onDeleteClick();
            }
        });

        menuSaveNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProjectSettingsMenuI.onSaveNameClick();
            }
        });

        mDialog = dialogBoxBuilder.create();
        mDialog.show();
    }

    public AlertDialog getDialog() {
        return mDialog;
    }

    public void setProjectSettingsMenuI(ProjectSettingsMenuI projectSettingsMenuI) {
        mProjectSettingsMenuI = projectSettingsMenuI;
    }

    public EditText getMenuTitleEditText() {
        return mMenuTitleEditText;
    }
}