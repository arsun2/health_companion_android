package com.example.health_companion_uiuc_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sunaustin8 on 11/27/18.
 */

public class ChatActivity extends AppCompatActivity {
    public TextView titleView;
    public TextView messageView;
    public static Button extraButton;
    public static Button sendMessage;
    public static EditText mNewMessage;
    public static RecyclerView mRecyclerView;
    private MessageAdapter messageAdapter;
    private ArrayList<Participant> parList;

    //titleView = (TextView) itemView.findViewById(R.id.speakerTextView);
    //messageView = (TextView) itemView.findViewById(R.id.messageTextView);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);

        extraButton = (Button) findViewById(R.id.editButton);
        sendMessage = (Button) findViewById(R.id.messageButton);
        mNewMessage = (EditText) findViewById(R.id.newMessage);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));

        parList = new ArrayList<>();
        Participant welcome = new Participant("Health Companion", "Try to walk to the store!");
        parList.add(welcome);
        messageAdapter = new MessageAdapter(parList);
        mRecyclerView.setAdapter(messageAdapter);


        //opens the edit Detail Activity upon the clicking of this button
        extraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ExtraActivity.class);
                startActivity(intent);
            }
        });

       sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mNewMessage.getText().toString();
                Participant newPar = new Participant("You", message);
                parList.add(newPar);
            }
        });
        messageAdapter.notifyDataSetChanged();
    }
}
