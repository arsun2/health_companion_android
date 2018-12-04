package com.example.health_companion_uiuc_mobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by sunaustin8 on 12/3/18.
 */

public class ExtraActivity extends AppCompatActivity {
    private Button mColorButton;
    private Button workButton;
    private Button mGoogleButton;
    private EditText mGoogleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extra_activity);

        workButton = (Button) findViewById(R.id.workButton);
        workButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String googleSearch = mGoogleText.getText().toString();
                //String googleURL = "http://lmgtfy.com/?q=" + googleSearch;
                String workout = "https://www.bodybuilding.com/content/your-4-week-plan-for-guaranteed-muscle-growth.html";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(workout));
                startActivity(intent);
            }
        });

    }
}
