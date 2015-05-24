package com.mumuWeibo2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {

    Button btOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btOpen = (Button) findViewById(R.id.bt_open);
        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName(MainActivity.this.getPackageName(), "com.mumuWeibo2" +
                        ".SplashScreen");
                MainActivity.this.startActivity(intent);

            }
        });
    }


}
