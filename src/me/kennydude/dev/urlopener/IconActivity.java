package me.kennydude.dev.urlopener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.kennydude.dev.urlopener.WakeLockActivity.WakeLockInfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class IconActivity extends Activity {
	
	class IconData{
		String app_name;
		Drawable app_ns;
	}
	
	class Background extends Drawable{
		public int i;
		@Override
		public void draw(Canvas canvas) {
			Paint p = new Paint();
			
			p.setColor(Color.YELLOW);
			int w = this.getBounds().width();
			
			canvas.drawRect(new Rect(0,0,w,w), p);
			
			p.setColor(Color.RED);
			
			canvas.drawLine(0, i, w, i, p);
			canvas.drawLine(0, w - i, w, w - i, p);
			
			canvas.drawLine(i, 0, i, w, p);
			canvas.drawLine(w-i, 0, w-i, w, p);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.OPAQUE;
		}

		@Override
		public void setAlpha(int alpha) {}

		@Override
		public void setColorFilter(ColorFilter cf) {}
		
	}
	
	Background sd;
	List<IconData> icons;
	FeedAdapter fa;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.icons);
        
        GridView gd = (GridView)findViewById(R.id.GridView);
        icons = new ArrayList<IconData>();
        fa = new FeedAdapter(this,android.R.layout.simple_list_item_1 , icons);
        gd.setAdapter(fa);
        
        gd.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				Toast.makeText(IconActivity.this, icons.get(pos).app_name, Toast.LENGTH_SHORT).show();
			}
        	
        });
        
        sd = new Background();
        DisplayMetrics outMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        
        int i = 14 /2;
        
        Log.d("pa", i + " ");
        
        sd.i = i;
        
        new GetApps().execute();
    }
    
    public class GetApps extends AsyncTask<Object, Object, Object>{

    	@SuppressWarnings("unchecked")
		@Override
    	protected void onPostExecute(Object r){
    		try{ dg.dismiss(); }
    		catch(Exception e){}
    		
    		icons.addAll((Collection<? extends IconData>) r);
	    	fa.notifyDataSetChanged();
    	}
    	ProgressDialog dg;
    	
    	@Override
    	protected void onPreExecute(){
    		dg = new ProgressDialog(IconActivity.this);
			dg.setMessage(getResources().getText(
					R.string.one_moment_please)
			);
			dg.setCancelable(false);
			dg.show();
    	}
    	
		@Override
		protected Object doInBackground(Object... arg0) {
			List<IconData> r = new ArrayList<IconData>();
			
			Intent intent = new Intent(Intent.ACTION_MAIN);
			List<String> got = new ArrayList<String>();
			
			for(ResolveInfo ai : 
				getPackageManager().queryIntentActivities(intent,PackageManager.GET_ACTIVITIES) ){
				if(!got.contains(ai.activityInfo.packageName)){
					IconData id = new IconData();
					id.app_name = ai.activityInfo.loadLabel(getPackageManager()).toString();
					try {
						id.app_ns = getPackageManager().getApplicationIcon( ai.activityInfo.packageName );
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
					r.add(id);
					got.add(ai.activityInfo.packageName);
				}
			}
			
			return r;
		}
    	
    }

    
    public class FeedAdapter extends ArrayAdapter<IconData> {
    	public FeedAdapter(Context context, int textViewResourceId,
				List<IconData> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				LayoutInflater li = getLayoutInflater();
				convertView  = li.inflate(R.layout.icons_item, null);
			}
			
			IconData dt = this.getItem(position);
			TextView tv = (TextView)convertView.findViewById(R.id.icon_text);
			tv.setText(dt.app_name);
			
			ImageView iv = (ImageView)convertView.findViewById(R.id.icon_image);
			
			iv.setBackgroundDrawable(sd);
			
			iv.setImageDrawable(dt.app_ns);
			
			return convertView;
		}
    }
}
