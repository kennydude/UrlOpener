package me.kennydude.dev.urlopener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button b = (Button)findViewById(R.id.url_opener);
        b.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, UrlOpenerActivity.class));
			}
        });
        b = (Button)findViewById(R.id.wake_locks);
        b.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, WakeLockActivity.class));
			}
        });
    }
}
