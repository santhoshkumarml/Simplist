package com.example.ankit.simplist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link ConfigFragmentInteractionListener}
 * interface.
 */
public class ConfigFragment extends Fragment implements AbsListView.OnItemClickListener,
        AddConfigDialogFragment.AddConfigDialogFragmentListener, RemoveConfigDialogFragment.RemoveConfigDialogFragmentListener {

    private static Logger LOGGER = Logger.getLogger(ConfigFragment.class.getCanonicalName());

    private static final String FILENAME = "config_file";

    private ConfigFragmentInteractionListener mListener;

    private ConfigContent configContent = new ConfigContent();

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static ConfigFragment newInstance() {
        ConfigFragment fragment = new ConfigFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConfigFragment() {
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }


    private void loadConfigItems(Context context) {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(FILENAME);
            byte[] data = new byte[1024];
            fis.read(data, 0, 1024);
            this.configContent.addAllConfigElements((List<ConfigElement>)deserialize(data));
        } catch (IOException e) {
            //LOGGER.log(Level.WARNING, e.getMessage());
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        } finally {
            if(fis!=null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, e.getMessage());
                }
            }
        }
    }

    private void writeConfigItems(Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(serialize(this.configContent.getConfigElements()));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        } finally {
            if(fos!=null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, e.getMessage());
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ConfigFragment fragment = this;

        loadConfigItems(getActivity());

        // TODO: Change Adapter to display your content
        mAdapter = new ArrayAdapter<ConfigElement>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, this.configContent.getConfigElements());

        FloatingActionButton fab = (FloatingActionButton) this.getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddConfigDialogFragment newFragment = new AddConfigDialogFragment();
                newFragment.setListener(fragment);
                newFragment.show(getFragmentManager(), "Add Item");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ConfigFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ConfigFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RemoveConfigDialogFragment newFragment = new RemoveConfigDialogFragment();
        ConfigFragment fragment = this;
        newFragment.setListener(fragment);
        newFragment.setConfigElement(this.configContent.getConfigElement(position));
        newFragment.show(getFragmentManager(), "Config Item Options");
    }

//    /**
//     * The default content for this Fragment has a TextView that is shown when
//     * the list is empty. If you would like to change the text, call this method
//     * to supply the text it should use.
//     */
//    public void setEmptyText(CharSequence emptyText) {
//        View emptyView = mListView.getEmptyView();
//
//        if (emptyView instanceof TextView) {
//            ((TextView) emptyView).setText(emptyText);
//        }
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface ConfigFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    @Override
    public void saveItem(String itemName, String sensorId) {
        this.configContent.addConfigElement(new ConfigElement(sensorId, itemName));
        writeConfigItems(getActivity());
    }

    @Override
    public void removeItem(String itemName, String sensorId) {
        this.configContent.removeConfigElement(sensorId);
        writeConfigItems(getActivity());
        ((BaseAdapter)mListView.getAdapter()).notifyDataSetChanged();
    }

}
