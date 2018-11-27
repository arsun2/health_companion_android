package com.example.health_companion_uiuc_mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.io.*;

import com.example.health_companion_uiuc_mobile.utils.HttpHelper;
import com.example.health_companion_uiuc_mobile.utils.NetworkHelper;
import com.example.health_companion_uiuc_mobile.utils.RequestPackage;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.PreviewColumnChartView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class StatsFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private GetActivityAsyncTask mGetActivityAsyncTask;
    private GetLabelAsyncTask mGetLabelAsyncTask;
    private PostLabelAsyncTask mPostLabelAsyncTask;

    private View mView;
    private View mInfoUnavailableView;
    private ListView mLabelsListView;
    private LabelsListAdapter labelsListAdapter;

    private ProgressBar mReloadingListProgressBar;

    private ColumnChartView chart;
    private PreviewColumnChartView previewChart;

    private EditText mActivityNameEditText;
    private EditText mActivityFeelingEditText;

    private Button walkingButton;
    private Button runningButton;

    private int viewportLeft;
    private int viewportRight;
    private boolean walkingTrue = false;
    private boolean runningTrue = false;

    private float[] caloriesArray;
    private int[] stepsArray;
    private float totalCal;
    private int totalSteps;

    private int year = 2017;
    private int month = 10;
    private int day = 2;
    private String userID = "";

    /**
     *  View holder object for holding the output.
     */
    private static class LabelEntryViewHolder {
        public TextView labelsTextView;
    }

    /**
     *  For DatePickerFragment to get an instance of this fragment to update date
     */
    private static StatsFragment fragment = null;

    public static StatsFragment getInstance(){
        return fragment;
    }

    /**
     *  Required empty public constructor
     */
    public StatsFragment() {}


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get userID from the bundle passed from activity
        if (getArguments() != null) {
            userID = getArguments().getString("userID");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_stats, container, false);
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

        b = (Button) mView.findViewById(R.id.submit_label);
        b.setOnClickListener(this);

        b = (Button) mView.findViewById(R.id.reset_selection);
        b.setOnClickListener(this);

        b = (Button) mView.findViewById(R.id.walkingButton);
        b.setOnClickListener(this);

        b = (Button) mView.findViewById(R.id.runningButton);
        b.setOnClickListener(this);

        b = (Button) mView.findViewById(R.id.chatButton);
        b.setOnClickListener(this);

        // edit texts
        mActivityNameEditText = (EditText) mView.findViewById(R.id.activity_name);
        mActivityFeelingEditText = (EditText) mView.findViewById(R.id.activity_feeling);

        // initialize label list
        mLabelsListView = (ListView) mView.findViewById(R.id.label_list);
        ViewCompat.setNestedScrollingEnabled(mLabelsListView, true);

        if (labelsListAdapter == null) {
            labelsListAdapter = new LabelsListAdapter(getActivity());
        }
        mLabelsListView.setAdapter(labelsListAdapter);

        // present page
        refresh();
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

        if (id == R.id.action_refresh) {
            refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.select_date:
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "Date Picker");
                break;
            case R.id.runningButton:
                InfoStore.runningTrue = true;
                InfoStore.walkingTrue = false;
                getActivity().finish();
                startActivity(getActivity().getIntent());
                break;
            case R.id.submit_label:
                String name = mActivityNameEditText.getText().toString();
                String feeling = mActivityFeelingEditText.getText().toString();

                if ("".equals(name) || "".equals(feeling)) {
                    Toast.makeText(getActivity(), "Necessary Information Missed", Toast.LENGTH_SHORT).show();
                } else {
                    // TODO: what is the correct time format to be passed to the nodejs backend
                    String startTime = year + "-" + fillZero(month) + "-" + fillZero(day) + "T" +
                            fillZero(viewportLeft / 60) + ":" + fillZero(viewportLeft % 60) + ":00-05:00";
                    String endTime = year + "-" + fillZero(month) + "-" + fillZero(day) + "T" +
                            fillZero(viewportRight / 60) + ":" + fillZero(viewportRight % 60) + ":00-05:00";

                    mPostLabelAsyncTask = new PostLabelAsyncTask();
                    mPostLabelAsyncTask.execute(userID, startTime, endTime, name,
                            Integer.toString(totalSteps), Float.toString(totalCal), feeling);
                }
                break;
            case R.id.reset_selection:
                mActivityNameEditText.getText().clear();
                mActivityFeelingEditText.getText().clear();
                previewX(true);
                break;
            case R.id.walkingButton:
                InfoStore.walkingTrue = true;
                InfoStore.runningTrue = false;
                getActivity().finish();
                startActivity(getActivity().getIntent());
                break;

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
        void onFragmentInteraction(Uri uri);
    }


    /**
     *  Update date info and get the activity data using this updated date
     *  @param day new day data
     *  @param month new month data
     *  @param year new year data
     */
    public void updateDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;

        getActivityData();
    }

    /**
     *  refresh the page
     */
    public void refresh() {
        mActivityNameEditText.getText().clear();
        mActivityFeelingEditText.getText().clear();
        getActivityData();
        getLabelData();
    }

    /**
     *  New an async task to get activity data from the server using the existing date
     */
    private void getActivityData() {
        mGetActivityAsyncTask = new GetActivityAsyncTask(year, month, day);
        mGetActivityAsyncTask.execute();
    }

    /**
     *  New an async task to get label data from the server
     */
    private void getLabelData() {
        mGetLabelAsyncTask = new GetLabelAsyncTask();
        mGetLabelAsyncTask.execute();
    }

    /**
     *  AsyncTask to fetch labels from the Azure server
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
            String url = "http://health-companion-uiuc.azurewebsites.net/getLabel?user_id=" + userID;
            //System.out.println(url);

            if (!NetworkHelper.checkNetworkAccess(getActivity())) {
                Toast.makeText(getActivity(), "Please check your network", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    result = HttpHelper.downloadUrl(url);
                    System.out.println(result);
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

        protected void updateWalking() {

        }

        @Override
        protected void onPostExecute(String result) {
            // Remove progress bar
            mReloadingListProgressBar.setVisibility(View.GONE);
            if (result == null) {
                return;
            }
            String res = "[[\"Sun Oct 01 2017 11:00:00 GMT-0500 (CDT)\",\"Sun Oct 01 2017 13:00:00 GMT-0500 (CDT)\",\"[\\\"Walking to Siebel\\\"]\",\"5267\",\"613.5312795639038\",\"[\\\"refreshing\\\"]\"],[\"Sun Oct 01 2017 19:00:00 GMT-0500 (CDT)\",\"Sun Oct 01 2017 20:00:00 GMT-0500 (CDT)\",\"[\\\"Working out at CRCE\\\"]\",\"8242\",\"765.5814476013184\",\"[\\\"exhausted\\\"]\"],[\"Sun Oct 01 2017 00:00:00 GMT-0500 (CDT)\",\"Sun Oct 01 2017 01:00:00 GMT-0500 (CDT)\",\"[\\\"Walking back home\\\"]\",\"2871\",\"342.68400382995605\",\"[\\\"tired\\\"]\"],[\"Sun Oct 01 2017 00:00:00 GMT-0500 (CDT)\",\"Sun Oct 01 2017 23:00:00 GMT-0500 (CDT)\",\"[\\\"Walking with friends\\\"]\",\"26612\",\"3089.7404947280884\",\"[\\\"relaxed\\\"]\"],[\"Thu Sep 28 2017 19:00:00 GMT-0500 (CDT)\",\"Thu Sep 28 2017 19:00:00 GMT-0500 (CDT)\",\"[\\\"Swimming\\\"]\",\"2013\",\"165.24984216690063\",\"[\\\"awesome\\\"]\"],[\"Sun Oct 01 2017 08:16:00 GMT-0500 (CDT)\",\"Sun Oct 01 2017 08:35:00 GMT-0500 (CDT)\",\"[\\\"groceries\\\"]\",\"97.98224020004272\",\"97.98224020004272\",\"[\\\"beautiful gifts at CM\\\"]\"],[\"2018-04-12T11:30:00-05:00\",\"2018-04-12T12:42:00-05:00\",\"[\\\"Running\\\\n\\\"]\",\"0\",\"0.0\",\"[\\\"fulfilled\\\"]\"],[\"2017-10-02T14:26:00-05:00\",\"2017-10-02T15:38:00-05:00\",\"[\\\"Biking to market\\\"]\",\"523\",\"61.93696\",\"[\\\"free\\\"]\"],[\"2017-10-02T11:30:00-05:00\"," +
                    "\"2017-10-02T12:42:00-05:00\",\"[\\\"running\\\"]\",\"697\",\"87.32096\",\"[\\\"good\\\"]\"]]";
            // Show label header
            mView.findViewById(R.id.label_header).setVisibility(View.VISIBLE);

            // extract labels from the result String and put them into adapter
            labelsListAdapter.clearMessages();
            try {
                System.out.println(result);
                JSONArray streamer = new JSONArray(res);
                String label = "";
                for (int i = streamer.length() - 1; i >= 0; i--) {
                    label = streamer.getString(i);
                    JSONArray labelStreamer = new JSONArray(label);
                    String activity = new JSONArray(labelStreamer.getString(2)).getString(0);
                    String feeling = new JSONArray(labelStreamer.getString(5)).getString(0);
                    String calories = new DecimalFormat("#.###").format(labelStreamer.getDouble(4));

                    label = activity + ", consuming around " + calories + " calorie, feeling " + feeling;
                    if(!InfoStore.walkingTrue && !InfoStore.runningTrue) {
                        labelsListAdapter.addLabel(label);
                    }
                    else if(InfoStore.walkingTrue && !InfoStore.runningTrue){
                        System.out.println("hey1");
                        if(label.toLowerCase().contains("walking")){
                            labelsListAdapter.addLabel(label);
                        }
                    }
                    else{
                        if(activity.toLowerCase().contains("running")){
                            labelsListAdapter.addLabel(label);
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  AsyncTask to submit new label to Azure server
     */
    private class PostLabelAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            // Display progress bar
            mReloadingListProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            String url = "http://health-companion-uiuc.azurewebsites.net/insertLabel";

            if (!NetworkHelper.checkNetworkAccess(getActivity())) {
                Toast.makeText(getActivity(), "Please check your network", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    RequestPackage requestPackage = new RequestPackage();
                    requestPackage.setEndPoint(url);
                    requestPackage.setMethod("POST");
                    requestPackage.setParam("user_id", strings[0]);
                    requestPackage.setParam("periodStart", strings[1]);
                    requestPackage.setParam("periodEnd", strings[2]);
                    requestPackage.setParam("labelName", strings[3]);
                    requestPackage.setParam("steps", strings[4]);
                    requestPackage.setParam("cals", strings[5]);
                    requestPackage.setParam("subjTag", strings[6]);

                    result = HttpHelper.downloadFromFeed(requestPackage);
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

            // show operation status
            if ("ok, label saved".equals(result)) {
                Toast.makeText(getActivity(), "New Label Submitted Successfully", Toast.LENGTH_LONG).show();
                getLabelData();
            } else {
                Toast.makeText(getActivity(), "Failed to Submit New Label", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     *  AsyncTask to fetch activity data from Azure server
     */
    private class GetActivityAsyncTask extends AsyncTask<Void, Void, String> {
        private String year;
        private String month;
        private String day;

        /**
         *  Constructor: reformat date info
         */
        public GetActivityAsyncTask(int year, int month, int day) {
            this.year = Integer.toString(year);
            this.month = fillZero(month);
            this.day = fillZero(day);
        }

        @Override
        protected void onPreExecute() {
            // Display progress bar
            mReloadingListProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String url = "http://health-companion-uiuc.azurewebsites.net/getactivity?userid=" + userID + "&daysBefore=0&today=" + year + "-" + month + "-" + day;
            System.out.println(url);
            String result = null;

            if (!NetworkHelper.checkNetworkAccess(getActivity())) {
                Toast.makeText(getActivity(), "Please check your network", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    result = HttpHelper.downloadUrl(url);
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

            // If no result, stop immediately
            if (result == null) {
                return;
            }

            // Show activity header
            StatsFragment.LabelEntryViewHolder holder;
            holder = new StatsFragment.LabelEntryViewHolder();
            holder.labelsTextView = (TextView) mView.findViewById(R.id.activity_header);
            holder.labelsTextView.setText("Intraday Activities for " + month + "/" + day + "/" + year);

            // Generate data for previewed chart and copy of that data for preview chart
            ColumnChartData[] results = generateChartData(result, year, month, day);
            ColumnChartData data = results[0];
            ColumnChartData previewData = results[1];

            // Configure charts
            chart.setColumnChartData(data);
            chart.setZoomEnabled(false);
            chart.setScrollEnabled(false);

            previewChart.setColumnChartData(previewData);
            previewChart.setViewportChangeListener(new StatsFragment.ViewportListener());

            // Show charts
            previewX(true);

            // Show label creation section
            mView.findViewById(R.id.label_creation_header).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.activity_name).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.activity_feeling).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.label_creation_buttons).setVisibility(View.VISIBLE);
        }
    }


    /**
     *  Charts-related methods
     *  Ref: https://github.com/lecho/hellocharts-android
     */

    /**
     *  Show the preview charts
     *  @param animate whether to use animation
     */
    private void previewX(boolean animate) {
        Viewport tempViewport = new Viewport(chart.getMaximumViewport());
        float dx = tempViewport.width() * 23 / 48;
        tempViewport.inset(dx, 0);
        if (animate) {
            previewChart.setCurrentViewportWithAnimation(tempViewport);
        } else {
            previewChart.setCurrentViewport(tempViewport);
        }
        previewChart.setZoomType(ZoomType.HORIZONTAL);
    }

    /**
     *  Extract data from the result String and transform them into chart data
     *  @param result result String fetched from the server
     *  @param year existing year info
     *  @param month existing month info
     *  @param day existing day info
     */
    private ColumnChartData[] generateChartData(String result, String year, String month, String day) {
        int numSubcolumns = 1;
        int numColumns = 1440; // number of minutes

        // to filter dates
        String date = year + "-" + month + "-" + day;

        // accumulate calories and step data for each minutes from JSON result
        caloriesArray = new float[numColumns];
        stepsArray = new int[numColumns];

        try {
            JSONArray streamer = new JSONArray(result);
            for (int i = 0; i < streamer.length(); i++) {
                JSONArray entry = new JSONArray(streamer.getString(i));

                String wholeTime = entry.getString(0);
                if (!date.equals(wholeTime.split("T")[0])) {
                    continue;
                }
                String time = wholeTime.split("T")[1];
                String hour = time.split(":")[0];
                String minute = time.split(":")[1];
                int minutes = Integer.valueOf(hour) * 60 + Integer.valueOf(minute);

                float calories = (float) entry.getDouble(1);
                int steps = entry.getInt(2);

                caloriesArray[minutes] += calories;
                stepsArray[minutes] += steps;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // store as chart data
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; i++) {
            values = new ArrayList<SubcolumnValue>();
            for (int j = 0; j < numSubcolumns; j++) {
                values.add(new SubcolumnValue(caloriesArray[i], ChartUtils.pickColor()));
            }
            columns.add(new Column(values));
        }
        ColumnChartData data = new ColumnChartData(columns);
        data.setAxisYLeft(new Axis().setHasLines(true));

        // use separate deep copy for preview chart.
        ColumnChartData previewData = new ColumnChartData(data);
        data.setAxisXBottom(getAxisXWithLabels(30));
        previewData.setAxisXBottom(getAxisXWithLabels(60));

        // return results
        ColumnChartData[] results = new ColumnChartData[2];
        results[0] = data;
        results[1] = previewData;
        return results;

    }

    /**
     *  Get axis X for chart
     *  @param step the step we want for axis X
     */
    private Axis getAxisXWithLabels(int step) {
        List<AxisValue> values = new ArrayList<AxisValue>();

        for (int i = 0; i < 1440; i += step) {
            AxisValue axisValue = new AxisValue((float) i);
            int hour = i / 60;
            int minutes = i % 60;
            axisValue = axisValue.setLabel(getTimeFormat(hour, minutes));
            values.add(axisValue);
        }

        return new Axis(values);
    }

    /**
     *  Transform hour and minutes data into standard time format
     *  @param hour hour data
     *  @param minutes minutes data
     */
    private String getTimeFormat(int hour, int minutes) {
        StringBuilder sb = new StringBuilder();
        if (hour == 0 || hour == 24) {
            sb.append(12);
            appendMinute(minutes, sb);
            sb.append(" AM");
        } else if (hour > 12) {
            sb.append(hour - 12);
            appendMinute(minutes, sb);
            sb.append(" PM");
        } else if (hour == 12) {
            sb.append(12);
            appendMinute(minutes, sb);
            sb.append(" PM");
        } else {
            sb.append(hour);
            appendMinute(minutes, sb);
            sb.append(" AM");
        }

        return sb.toString();
    }

    /**
     *  Append proper minutes info
     *  @param minutes minutes data
     *  @param sb temp result container
     */
    private void appendMinute(int minutes, StringBuilder sb) {
        if (minutes != 0) {
            sb.append(":" + fillZero(minutes));
        }
    }

    /**
     *  Append a zero for single digit number
     *  @param num the number to check
     */
    private String fillZero(int num) {
        return (num < 10)? "0" + num : "" + num;
    }


    /**
     * Viewport listener for preview chart(lower one).
     * in {@link #onViewportChanged(Viewport)} method change viewport of upper chart.
     */
    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            chart.setCurrentViewport(newViewport);

            // record the boundary value for the current viewport
            viewportLeft = (int) Math.round(newViewport.left + 0.5);
            viewportRight = (int) Math.round(newViewport.right + 0.5);

            updateRangeData();
        }
    }

    /**
     * Update the total calories and steps data using the current boundary value
     */
    private void updateRangeData() {
        totalCal = 0;
        totalSteps = 0;
        for (int i = viewportLeft; i < viewportRight; i++) {
            totalCal += caloriesArray[i];
            totalSteps += stepsArray[i];
        }

        // Present the latest range data on the page
        StatsFragment.LabelEntryViewHolder holder = new StatsFragment.LabelEntryViewHolder();
        holder.labelsTextView = (TextView) mView.findViewById(R.id.label_creation_description);
        String rangeData = "Calories: " + new DecimalFormat("#.###").format(totalCal) + "        Steps: " + totalSteps;
        holder.labelsTextView.setText(rangeData);
    }
}
