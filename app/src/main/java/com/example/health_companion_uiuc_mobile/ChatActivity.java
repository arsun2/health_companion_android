package com.example.health_companion_uiuc_mobile;

import android.content.Intent;
import android.net.Uri;
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
 * Chatbot activity
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
        Participant welcome = new Participant("Health Companion", "Welcome to Health Companion - Send h for help");
        Participant start = new Participant("Health Companion", "Reminder: Get your cardio in at 4!");
        parList.add(welcome);
        parList.add(start);
        messageAdapter = new MessageAdapter(parList);
        mRecyclerView.setAdapter(messageAdapter);

        extraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ExtraActivity.class);
                startActivity(intent);
            }
        });

       //handles population of new messages and prompts appropiate response to user request
       sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mNewMessage.getText().toString();
                Participant newPar = new Participant("You", message);
                parList.add(newPar);
                if(message.equals("h")){
                    String helpMessage = "Help - List of commands:\n" +
                            "Press n to create a calendar event\nPress l to open a list of workout plans" +
                            "\nPress p to view your progress in the past month\nPress f to open Fitbit app\nPress v to view a map of" +
                             " exercise activity";
                    Participant newParHelp = new Participant("Health Companion", helpMessage);
                    parList.add(newParHelp);
                }
                if(message.equals("l")){
                    String workout = "https://www.bodybuilding.com/content/your-4-week-plan-for-guaranteed-muscle-growth.html";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse(workout));
                    startActivity(intent);
                }
                if(message.equals("p")){
                    Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                messageAdapter = new MessageAdapter(parList);
                mRecyclerView.setAdapter(messageAdapter);
            }
        });
        messageAdapter.notifyDataSetChanged();
    }
}
