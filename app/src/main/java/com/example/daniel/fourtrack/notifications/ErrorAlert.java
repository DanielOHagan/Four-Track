package com.example.daniel.fourtrack.notifications;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.fourtrack.R;
import com.example.daniel.fourtrack.activities.MainActivity;

public class ErrorAlert {

    //displays a dialog box inside the chosen activity using the input String

    private String mErrorMessage;
    private final Activity mActivity;

    public ErrorAlert(String errorMessage, Activity activity) {
        mErrorMessage = errorMessage;
        mActivity = activity;
    }

    public void displayError() {

        //creates dialog box and displays error message with activity

        AlertDialog.Builder dialogBoxBuilder = new AlertDialog.Builder(mActivity);
        View view = mActivity.getLayoutInflater().inflate(R.layout.error_alert, null);

        dialogBoxBuilder.setView(view);

        TextView errorAlertMessageTextView = view.findViewById(R.id.errorAlertMessageTextView);
        Button errorAlertMainActivityBtn = view.findViewById(R.id.errorAlertMainActivityBtn);

        AlertDialog dialog = dialogBoxBuilder.create();

        //if error message was not set for some reason then here it is assigned a default value
        if (mErrorMessage == null) {
            mErrorMessage = "Unknown error";
        }

        String message = mErrorMessage + "--Inside activity";

        errorAlertMessageTextView.setText(message);

        errorAlertMainActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, MainActivity.class);
                mActivity.startActivity(intent);
            }
        });

        dialog.show();
    }
}