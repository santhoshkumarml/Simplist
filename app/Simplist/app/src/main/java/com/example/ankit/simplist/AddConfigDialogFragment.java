package com.example.ankit.simplist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.EditText;

/**
 * Created by santhosh on 11/15/15.
 */
public class AddConfigDialogFragment extends DialogFragment {

    public interface AddConfigDialogFragmentListener {
        public void saveItem(String itemName, String sensorId);
    }

    private AddConfigDialogFragmentListener listener = null;

    public AddConfigDialogFragment() {
    }
    public void setListener(AddConfigDialogFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.new_item_create, null))
                // Add action buttons
                .setPositiveButton(R.string.save_config, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Do Nothing
                        try {
                            EditText itemName = (EditText) ((Dialog)
                                    dialog).findViewById(R.id.itemName);

                            EditText sensorId = (EditText) ((Dialog)
                                    dialog).findViewById(R.id.sensorId);
                            listener.saveItem(itemName.getText().toString(), sensorId.getText().toString());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel_config, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddConfigDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
