package com.example.daniel.fourtrack.notifications;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.daniel.fourtrack.R;

public class TextInputPrompt {

    public interface TextInputPromptI {
        void onConfirmClick();
    }

    private final Activity activity;
    private TextInputPromptI mTextInputPromptI;
    private EditText alertEditText;
    private AlertDialog dialog;

    public TextInputPrompt(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return activity;
    }

    public AlertDialog getDialog() {
        return dialog;
    }

    public EditText getAlertEditText() {
        return alertEditText;
    }

    public void displayPrompt(int inputHintResource) {
        AlertDialog.Builder dialogBoxBuilder = new AlertDialog.Builder(activity);
        View alertView = activity.getLayoutInflater().inflate(R.layout.text_input_alert, null);
        Button alertConfirmBtn = alertView.findViewById(R.id.alertConfirm);

        dialogBoxBuilder.setView(alertView);
        alertEditText = alertView.findViewById(R.id.alertEditText);
        alertEditText.setHint(inputHintResource);

        alertConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextInputPromptI.onConfirmClick();
            }
        });

        dialog = dialogBoxBuilder.create();
        dialog.show();
    }

    public void setTextInputPromptI(TextInputPromptI textInputPromptI) {
        this.mTextInputPromptI = textInputPromptI;
    }
}