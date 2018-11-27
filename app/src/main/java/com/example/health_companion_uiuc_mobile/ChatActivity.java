package com.example.health_companion_uiuc_mobile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by sunaustin8 on 11/27/18.
 */

public class ChatActivity extends AppCompatActivity {
    public TextView titleView;
    public TextView messageView;

    //titleView = (TextView) itemView.findViewById(R.id.speakerTextView);
    //messageView = (TextView) itemView.findViewById(R.id.messageTextView);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);
    }
}
