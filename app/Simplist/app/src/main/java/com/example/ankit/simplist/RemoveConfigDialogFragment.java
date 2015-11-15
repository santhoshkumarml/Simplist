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
public class RemoveConfigDialogFragment extends DialogFragment {

    public interface RemoveConfigDialogFragmentListener {
        public void removeItem(String itemName, String sensorId);
    }

    private RemoveConfigDialogFragmentListener listener = null;
    private ConfigElement configElement;

    public RemoveConfigDialogFragment() {
    }

    public void setListener(RemoveConfigDialogFragmentListener listener) {
        this.listener = listener;
    }
    public void setConfigElement(ConfigElement element) {
        this.configElement = element;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.config_item_options)
                .setItems(R.array.config_item_option_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: listener.removeItem(configElement.getContent(), configElement.getId());
                                    break;
                            default: break;
                        }
                    }
                });
        return builder.create();
    }
}
