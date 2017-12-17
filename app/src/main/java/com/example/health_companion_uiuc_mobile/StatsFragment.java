package com.example.health_companion_uiuc_mobile;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.health_companion_uiuc_mobile.utils.HttpHelper;
import com.example.health_companion_uiuc_mobile.utils.NetworkHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.DecimalFormat;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class StatsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private GetActivityAsyncTask mGetActivityAsyncTask;
    private GetLabelAsyncTask mGetLabelAsyncTask;

    private View mView;
    private View mInfoUnavailableView;
    private ListView mLabelsListView;
    private LabelsListAdapter labelsListAdapter;

    private ProgressBar mReloadingListProgressBar;

    private static class LabelEntryViewHolder {
        public TextView labelsTextView;
    }

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_stats, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get progress bar spinner view
        mReloadingListProgressBar = mView.findViewById(R.id.loading_progress_bar);
        mInfoUnavailableView = mView.findViewById(R.id.info_unavailable);

        mLabelsListView = (ListView) mView.findViewById(R.id.label_list);
//        mLabelsListView.setEnabled(false);
        ViewCompat.setNestedScrollingEnabled(mLabelsListView, true);

        if (labelsListAdapter == null) {
            labelsListAdapter = new LabelsListAdapter(getActivity());
        }
        mLabelsListView.setAdapter(labelsListAdapter);

        mGetActivityAsyncTask = new GetActivityAsyncTask();
        mGetActivityAsyncTask.execute();

        mGetLabelAsyncTask = new GetLabelAsyncTask();
        mGetLabelAsyncTask.execute();


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Create a new AsynTask to fetch activity data from Azure
     */
    private class GetActivityAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            // Display progress bar
            mReloadingListProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;

            if (!NetworkHelper.checkNetworkAccess(getActivity())) {
                Toast.makeText(getActivity(), "Please check your network", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    result = HttpHelper.downloadUrl("http://health-companion-uiuc.azurewebsites.net/getactivity?userid=52KG66&daysBefore=0&today=2017-10-02");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onCancelled() {
            // Remove progress bar
            mReloadingListProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String result) {
            // Remove progress bar
            mReloadingListProgressBar.setVisibility(View.GONE);

            LabelEntryViewHolder holder;
            holder = new LabelEntryViewHolder();
            holder.labelsTextView = (TextView) mView.findViewById(R.id.label_section);
            holder.labelsTextView.setText(result);

        }
    }

    /**
     * Create a new AsynTask to fetch labels from Azure
     */
    private class GetLabelAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            // Display progress bar
            mReloadingListProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;

            if (!NetworkHelper.checkNetworkAccess(getActivity())) {
                Toast.makeText(getActivity(), "Please check your network", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    result = HttpHelper.downloadUrl("http://health-companion-uiuc.azurewebsites.net/getLabel?user_id=52KG66");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onCancelled() {
            // Remove progress bar
            mReloadingListProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String result) {
            // Remove progress bar
            mReloadingListProgressBar.setVisibility(View.GONE);
            if (result == null) {
                return;
            }

            LabelEntryViewHolder holder;
            holder = new LabelEntryViewHolder();
            holder.labelsTextView = (TextView) mView.findViewById(R.id.label_header);
            holder.labelsTextView.setText("Labels for Your Daily Life");

            try {
                JSONArray streamer = new JSONArray(result);
                String label = "";
                for (int i = 0; i < streamer.length(); i++) {
                    label = streamer.getString(i);
                    JSONArray labelStreamer = new JSONArray(label);
                    String activity = new JSONArray(labelStreamer.getString(2)).getString(0);
                    String feeling = new JSONArray(labelStreamer.getString(5)).getString(0);
                    String calories = new DecimalFormat("#.###").format(labelStreamer.getDouble(4));

                    label = activity + ", consuming around " + calories + " calorie, feeling " + feeling;
                    System.err.println("label: " + label);

                    labelsListAdapter.addLabel(label);
//                    mLabelsListView.setSelection(labelsListAdapter.getCount() - 1);
                }

//                LabelEntryViewHolder holder;
//                holder = new LabelEntryViewHolder();
//                holder.labelsTextView = (TextView) mView.findViewById(R.id.labelSection);
//                holder.labelsTextView.setText(label);


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


}
