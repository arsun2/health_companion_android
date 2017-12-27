package com.example.health_companion_uiuc_mobile;

import android.content.Context;
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

import com.example.health_companion_uiuc_mobile.utils.HttpHelper;
import com.example.health_companion_uiuc_mobile.utils.NetworkHelper;
import com.example.health_companion_uiuc_mobile.utils.RequestPackage;
import com.fitbit.fitbitcommon.network.BasicHttpRequest;
import com.fitbit.fitbitcommon.network.BasicHttpRequestBuilder;
import com.fitbit.fitbitcommon.network.BasicHttpResponse;

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

    private int viewportLeft;
    private int viewportRight;

    private float[] caloriesArray;
    private int[] stepsArray;
    private float totalCal;
    private int totalSteps;

    private int year = 2017;
    private int month = 10;
    private int day = 2;
    private String userID = "";

    private static class LabelEntryViewHolder {
        public TextView labelsTextView;
    }

    public StatsFragment() {
        // Required empty public constructor
    }

    private static StatsFragment fragment = null;

    public static StatsFragment getInstance(){
        return fragment;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.select_date:
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(),"Date Picker");
                break;
            case R.id.submit_label:
                String name = mActivityNameEditText.getText().toString();
                String feeling = mActivityFeelingEditText.getText().toString();

                if ("".equals(name) || "".equals(feeling)) {
                    Toast.makeText(getActivity(), "Necessary Information Missed", Toast.LENGTH_SHORT).show();
                } else {
                    String startTime = "";
                    String endTime = "";
                    mPostLabelAsyncTask = new PostLabelAsyncTask();
                    mPostLabelAsyncTask.execute("52KG77", startTime, endTime, name,
                            Integer.toString(totalSteps), Float.toString(totalCal), feeling);
                }
                break;
            case R.id.reset_selection:
                mActivityNameEditText.getText().clear();
                mActivityFeelingEditText.getText().clear();
                previewX(true);
                break;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userID = getArguments().getString("userID");
        }
//        Toast.makeText(getActivity(), "Here! " + userID, Toast.LENGTH_LONG).show();
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
            StatsFragment.getInstance().refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    public void updateDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        getActivityData();
    }

    public void refresh() {
        getActivityData();
        getLabelData();
    }

    private void getActivityData() {
        mGetActivityAsyncTask = new GetActivityAsyncTask(year, month, day);
        mGetActivityAsyncTask.execute();
    }

    private void getLabelData() {
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
     * Create a new AsynTask to submit labels to Azure
     */
    private class PostLabelAsyncTask extends AsyncTask<String, Void, String> {

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
        protected void onPreExecute() {
            // Display progress bar
            mReloadingListProgressBar.setVisibility(View.VISIBLE);
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
            System.out.println("result: " + result);
            if ("ok, label saved".equals(result)) {
                Toast.makeText(getActivity(), "New Label Submitted Successfully", Toast.LENGTH_LONG).show();
                getLabelData();
            } else {
                Toast.makeText(getActivity(), "Failed to Submit New Label", Toast.LENGTH_LONG).show();
            }
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
            String url = "http://health-companion-uiuc.azurewebsites.net/getLabel?user_id=" + "52KG77";
            System.out.println(url);

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
            if (result == null) {
                return;
            }

            mView.findViewById(R.id.label_header).setVisibility(View.VISIBLE);

            labelsListAdapter.clearMessages();
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

                    labelsListAdapter.addLabel(label);
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

    /**
     * Create a new AsynTask to fetch activity data from Azure
     */
    class GetActivityAsyncTask extends AsyncTask<Void, Void, String> {
        private String year;
        private String month;
        private String day;

        public GetActivityAsyncTask(int year, int month, int day) {
            this.year = Integer.toString(year);
            this.month = month < 10? "0" + month : "" + month;
            this.day = day < 10? "0" + day : "" + day;
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

            if (result == null) {
                return;
            }

            StatsFragment.LabelEntryViewHolder holder;
            holder = new StatsFragment.LabelEntryViewHolder();
            holder.labelsTextView = (TextView) mView.findViewById(R.id.activity_header);
            holder.labelsTextView.setText("Intraday Activities for " + month + "/" + day + "/" + year);

            // Generate data for previewed chart and copy of that data for preview chart.
            ColumnChartData[] results = generateChartData(result, year, month, day);
            ColumnChartData data = results[0];
            ColumnChartData previewData = results[1];

            chart.setColumnChartData(data);
            chart.setZoomEnabled(false);
            chart.setScrollEnabled(false);

            previewChart.setColumnChartData(previewData);
            previewChart.setViewportChangeListener(new StatsFragment.ViewportListener());

            previewX(true);

            // label creation
            mView.findViewById(R.id.label_creation_header).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.activity_name).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.activity_feeling).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.label_creation_buttons).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Viewport listener for preview chart(lower one). in {@link #onViewportChanged(Viewport)} method change
     * viewport of upper chart.
     */
    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            // don't use animation, it is unnecessary when using preview chart because usually viewport changes
            // happens to often.
            chart.setCurrentViewport(newViewport);
            viewportLeft = (int) Math.round(newViewport.left + 0.5);
            viewportRight = (int) Math.round(newViewport.right + 0.5);

            updateRangeData();
        }

    }

    private void updateRangeData() {
        totalCal = 0;
        totalSteps = 0;
        for (int i = viewportLeft; i < viewportRight; i++) {
            totalCal += caloriesArray[i];
            totalSteps += stepsArray[i];
        }

        StatsFragment.LabelEntryViewHolder holder = new StatsFragment.LabelEntryViewHolder();
        holder.labelsTextView = (TextView) mView.findViewById(R.id.label_creation_description);
//        String time = "Start time: " + getTimeFormat(viewportLeft / 60, viewportLeft % 60)
//                + "      End time: " + getTimeFormat(viewportRight / 60, viewportRight % 60) + "\n";
        String rangeData = "Calories: " + new DecimalFormat("#.###").format(totalCal) + "        Steps: " + totalSteps;
        holder.labelsTextView.setText(rangeData);
    }

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

    private ColumnChartData[] generateChartData(String result, String year, String month, String day) {
        int numSubcolumns = 1;
        int numColumns = 1440; // number of minutes
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

        // prepare preview data, is better to use separate deep copy for preview chart.
        // set color to grey to make preview area more visible.
        ColumnChartData previewData = new ColumnChartData(data);
//        for (Column column : previewData.getColumns()) {
//            for (SubcolumnValue value : column.getValues()) {
//                value.setColor(ChartUtils.DEFAULT_DARKEN_COLOR);
//            }
//        }

        data.setAxisXBottom(getAxisXWithLabels(30));
        previewData.setAxisXBottom(getAxisXWithLabels(60));

        ColumnChartData[] results = new ColumnChartData[2];
        results[0] = data;
        results[1] = previewData;
        return results;

    }

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

    private void appendMinute(int minutes, StringBuilder sb) {
        if (minutes != 0) {
            if (minutes < 10) {
                sb.append(":0" + minutes);
            } else {
                sb.append(":" + minutes);
            }
        }
    }
}
