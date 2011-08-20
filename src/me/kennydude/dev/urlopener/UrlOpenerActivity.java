package me.kennydude.dev.urlopener;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class UrlOpenerActivity extends Activity {
	SharedPreferences x;
	ArrayAdapter<String> bonder;
	List<String> recent;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.url_opener);
        x = getSharedPreferences("UrlOpenerPrefs", Context.MODE_PRIVATE);
        ListView thelist = (ListView) findViewById(R.id.RecentUrls);
        recent = new ArrayList<String>();
        bonder = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,recent);
        for(Object mru : x.getAll().values()){
        	String uri = (String)mru;
        	recent.add(uri);
        }
        thelist.setAdapter(bonder);
        thelist.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int p,
					long arg3) {
				String url = bonder.getItem(p);
				EditText url_box = (EditText)findViewById(R.id.UrlToOpen);
				url_box.setText(url);
			}
        });
        Button openurl = (Button)this.findViewById(R.id.OpenUrl);
        openurl.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				EditText url_box = (EditText)findViewById(R.id.UrlToOpen);
				String url = url_box.getText().toString();
				try{
					Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
					browserIntent = Intent.createChooser(browserIntent, getResources().getString(R.string.select_application));
					startActivity(browserIntent);
					if(!recent.contains(url)){
						if(recent.size() >= 5)
							recent = recent.subList(0, 4);
						recent.add(url);
						bonder.notifyDataSetInvalidated();
						Editor editor = x.edit();
						editor.clear();
						int pos = 1;
						for(String r_url : recent){
							editor.putString(pos + "",r_url);
							pos += 1;
						}
						editor.commit();
					}
				} catch(Exception ex){
					ex.printStackTrace();
					Toast.makeText(UrlOpenerActivity.this,
							R.string.something_broke,
							Toast.LENGTH_LONG).show();
				}
			}
        });
        openurl = (Button)this.findViewById(R.id.MoreApps);
        openurl.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				String url = "market://search?q=pub%3A%40kennydude";
				Intent open = new Intent("android.intent.action.VIEW", Uri.parse(url));
				try{
					startActivity(open);
				} catch(ActivityNotFoundException ex){
					ex.printStackTrace();
					Toast.makeText(UrlOpenerActivity.this,
							R.string.no_market,
							Toast.LENGTH_LONG).show();
				}
			}
        });
    }
}