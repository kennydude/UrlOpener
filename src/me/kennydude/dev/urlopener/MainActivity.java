package me.kennydude.dev.urlopener;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends ListActivity {
	
	List<DevTool> tools;
	FeedAdapter fa;
	
	public class DevTool{
		@SuppressWarnings("rawtypes")
		public Class ActivityClass;
		public int StringId;
		public int descriptionId;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        tools = new ArrayList<DevTool>();
        
        DevTool u = new DevTool();
        u.ActivityClass = UrlOpenerActivity.class;
        u.StringId = R.string.url_opener;
        u.descriptionId = R.string.url_opener_desc;
        tools.add(u);
        
        u = new DevTool();
        u.ActivityClass = WakeLockActivity.class;
        u.StringId = R.string.wake_locks;
        u.descriptionId = R.string.wake_locks_desc;
        tools.add(u);

        u = new DevTool();
        u.ActivityClass = Mod11Activity.class;
        u.StringId = R.string.modulus_11;
        u.descriptionId = R.string.modulus_11_desc;
        tools.add(u);
        
        u = new DevTool();
        u.ActivityClass = IconActivity.class;
        u.StringId = R.string.icons;
        u.descriptionId = R.string.icons_desc;
        tools.add(u);
        
        fa = new FeedAdapter(this,android.R.layout.simple_list_item_1 , tools);
        this.getListView().setAdapter(fa);
        
        getListView().setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				startActivity(new Intent(MainActivity.this, tools.get(arg2).ActivityClass));
			}
        	
        });
    }
    
    
    
    public class FeedAdapter extends ArrayAdapter<DevTool> {
    	public FeedAdapter(Context context, int textViewResourceId,
				List<DevTool> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
			TextView ed = new TextView(MainActivity.this);
			
			DevTool dt = this.getItem(position);
			
			ed.setPadding(15, 15, 15, 15);
			ed.setText(Html.fromHtml("<big><big><big>" + getText(dt.StringId) + "</big></big></big><br/>"
					+ getText(dt.descriptionId)));
			
			return ed;
		}
    }
}
