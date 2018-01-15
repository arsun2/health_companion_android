package com.example.health_companion_uiuc_mobile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fitbit.authentication.AuthenticationManager;

import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.PreviewColumnChartView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlanFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PlanFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    private View mView;
    private View mInfoUnavailableView;

    private ProgressBar mReloadingListProgressBar;

    private ColumnChartView chart;
    private PreviewColumnChartView previewChart;

    private int year = 2017;
    private int month = 10;
    private int day = 2;
    private String userID = "";

    private static class LabelEntryViewHolder {
        public TextView labelsTextView;
    }

    public PlanFragment() {
        // Required empty public constructor
    }

    private static PlanFragment fragment = null;

    public static PlanFragment getInstance(){
        return fragment;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.select_date:
                DialogFragment newFragment = new DatePickerFragment(1);
                newFragment.show(getFragmentManager(),"Date Picker");
                break;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userID = getArguments().getString("userID");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        getPlanData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_plan, container, false);
        fragment = this;
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get progress bar spinner view
        mReloadingListProgressBar = mView.findViewById(R.id.loading_progress_bar);
        mInfoUnavailableView = mView.findViewById(R.id.info_unavailable);

        // charts
        chart = (ColumnChartView) mView.findViewById(R.id.chart);
        previewChart = (PreviewColumnChartView) mView.findViewById(R.id.chart_preview);

        // bind all buttons
        Button b = (Button) mView.findViewById(R.id.select_date);
        b.setOnClickListener(this);

        // present page
        refresh();
    }

    public void updateDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        getPlanData();
    }

    private void getPlanData() {
        Toast.makeText(getActivity(), userID, Toast.LENGTH_SHORT).show();
//        Toast.makeText(getActivity(), month + "/" + day + "/" + year, Toast.LENGTH_SHORT).show();
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
}
