package com.example.health_companion_uiuc_mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Joe on 12/16/17.
 */

public class LabelsListAdapter extends BaseAdapter {

    private final ArrayList<String> mLabels;
    private final LayoutInflater mLayoutInflater;

    /**
     *  View holder object for holding the output.
     */
    private static class LabelEntryViewHolder {
        public TextView labelsTextView;
    }

    /**
     * Create a ListView compatible adapter with an
     * upper bound on the maximum number of entries that will
     * be displayed in the ListView.
     */
    LabelsListAdapter(Context context) {
        mLabels = new ArrayList<>();
        mLayoutInflater = LayoutInflater.from(context);
    }

    void addLabel(String label) {
        mLabels.add(label);
        notifyDataSetChanged();
    }

    /**
     * Convenience method to clear all messages from the underlying
     * data store and update the UI.
     */
    void clearMessages() {
        mLabels.clear();
        this.notifyDataSetChanged();
    }

    void setData(ArrayList<String> data) {
        mLabels.clear();
        mLabels.addAll(data);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mLabels.size();
    }

    @Override
    public Object getItem(int position) {
        return mLabels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LabelEntryViewHolder holder;

        if (convertView == null) {
            holder = new LabelEntryViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.list_item_labels, parent, false);
            convertView.setTag(holder);
            convertView.setBackgroundResource(R.drawable.rounded_corners);

            holder.labelsTextView = (TextView) convertView.findViewById(R.id.label);
        } else {
            holder = (LabelEntryViewHolder) convertView.getTag();
        }

        holder.labelsTextView.setText(mLabels.get(position));
        return convertView;
    }
}
