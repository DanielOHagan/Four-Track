package com.example.daniel.fourtrack.notifications;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.fourtrack.R;

public class DeletePrompt {

    public interface DeletePromptI {
        void onDeleteClick();
    }

    private final Activity activity;
    private AlertDialog dialog;
    private DeletePromptI deletePromptI;

    public DeletePrompt(Activity activity) {
        this.activity = activity;
    }

    public void setDeletePromptI(DeletePromptI deletePromptI) {
        this.deletePromptI = deletePromptI;
    }

    public AlertDialog getDialog() {
        return dialog;
    }

    public void displayPrompt(String objectName) {
        AlertDialog.Builder dialogBoxBuilder = new AlertDialog.Builder(activity);
        View alertView = activity.getLayoutInflater().inflate(
                R.layout.confirm_delete_alert, null
        );
        TextView alertTextView = alertView.findViewById(R.id.alertTextView);
        Button alertCancelBtn = alertView.findViewById(R.id.alertCancelBtn);
        Button alertConfirmBtn = alertView.findViewById(R.id.alertDeleteBtn);

        dialogBoxBuilder.setView(alertView);
        alertTextView.setText(String.format("Are you sure you wish to delete: %s", objectName));

        alertCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        alertConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePromptI.onDeleteClick();
            }
        });

        dialog = dialogBoxBuilder.create();
        dialog.show();
    }
}