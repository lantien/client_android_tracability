package com.example.boudalia.platformtracability;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

public class showReport extends DialogFragment {



    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle b = getArguments();

        builder.setMessage(b.getString("message"));
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
