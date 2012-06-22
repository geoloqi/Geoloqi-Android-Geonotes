package com.geoloqi.geonotes.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Display a simple {@link AlertDialog} with custom title
 * and message copy.
 * 
 * @author Tristan Waddington
 */
public class SimpleAlertDialogFragment extends DialogFragment {
    private static final String EXTRA_TITLE = "com.geoloqi.geonotes.app.title";
    private static final String EXTRA_MESSAGE = "com.geoloqi.geonotes.app.message";

    public static SimpleAlertDialogFragment newInstance(int title, int message) {
        SimpleAlertDialogFragment f = new SimpleAlertDialogFragment();
        
        // Configure our AlertDialog
        Bundle args = new Bundle();
        args.putInt(EXTRA_TITLE, title);
        args.putInt(EXTRA_MESSAGE, message);
        f.setArguments(args);
        
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt(EXTRA_TITLE);
        int message = getArguments().getInt(EXTRA_MESSAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        
        return builder.create();
    }
}