package com.example.health_companion_uiuc_mobile;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sunaustin8 on 5/1/17.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    ArrayList<Participant> participants;

    public MessageAdapter(ArrayList<Participant> participants) {
        this.participants = participants;
    }

    /** Called to fill screen with views until entire screen is filled.
     * Then it recycles after that
     * @param parent the parent object which is the RecyclerView
     * @param viewType different types of views in our parent which is our RecyclerView
     * @return New ViewHolder that we have allocated
     */
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View movieListItem = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.message_information, parent, false);
        return new MessageViewHolder(movieListItem);
    }

    /** this method updates a ViewHolder every time it needs to hold data for a
     * different move. This updates everything in the RecyclerView
     * @param holder ViewHolder that has the View we need to update in the UI
     * @param position the index of current movie in ArrayList of messages
     */
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        final Participant participant = participants.get(position);

        String name = participant.getName();
        int cutOff = participant.getName().indexOf(0);
        holder.titleView.setText(name.substring(0, name.length()-5));
        holder.messageView.setText(participant.getMessage());

    }

    //class for the Adapter that links references to all the subviews and widgets
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView titleView;
        public TextView messageView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            titleView = (TextView) itemView.findViewById(R.id.speakerTextView);
            messageView = (TextView) itemView.findViewById(R.id.messageTextView);
        }
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

}

