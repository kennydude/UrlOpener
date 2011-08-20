package me.kennydude.dev.urlopener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class WakeLockActivity extends ListActivity {
	
	List<WakeLockInfo> wake_locks;
	FeedAdapter fa;
	
	public class WakeLockInfo{
		public Integer pid;
		public String name;
		public String type;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wake_locks = new ArrayList<WakeLockInfo>();
        fa = new FeedAdapter(this,android.R.layout.simple_list_item_1 , wake_locks);
        this.getListView().setAdapter(fa);
        getListView().setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				final CharSequence[] items = {
					"Kill App",
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(WakeLockActivity.this);
				builder.setItems(items, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				        if(item == 0){
				        	if(ShellInterface.isSuAvailable()) {
				        		WakeLockInfo wli = wake_locks.get(position);
								String output = ShellInterface.getProcessOutput("kill " + wli.pid);
				        	}
				        }
				        dialog.dismiss();
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
				return false;
			}
        	
        });
        new LoadWakeLocks().execute();
    }
    
    public class FeedAdapter extends ArrayAdapter<WakeLockInfo> {
    	public FeedAdapter(Context context, int textViewResourceId,
				List<WakeLockInfo> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
			TextView ed = new TextView(WakeLockActivity.this);
			
			WakeLockInfo wli = this.getItem(position);
			
			String caption = "<b>" + wli.name + "</b><br/>Type:" + wli.type + "<br/>PID: " + wli.pid;
			ed.setPadding(5, 5, 5, 5);
			ed.setText(Html.fromHtml(caption));
			
			return ed;
		}
    }
    
    public class LoadWakeLocks extends AsyncTask<Object, Object, Object>{
    	
    	@SuppressWarnings("unchecked")
		@Override
    	protected void onPostExecute(Object r){
    		WakeLockActivity.this.wake_locks.addAll((Collection<? extends WakeLockInfo>) r);
    		fa.notifyDataSetChanged();
    	}
    	
		@Override
		protected Object doInBackground(Object... arg0) {
			try{
				if(ShellInterface.isSuAvailable()) {
					String output = ShellInterface.getProcessOutput("dumpsys power");
					String[] lines = output.split("\n");
					Boolean locks = false;
					
					List<WakeLockInfo> wake_locks = new ArrayList<WakeLockInfo>();
					for(String line : lines){
						if(line.startsWith("mLocks")){
							locks = true;
						} else if(locks == true){
							if(line.startsWith("  ")){ // GOT ONE! :D
								WakeLockInfo wli = new WakeLockInfo();
								Log.d("p", "Parsing: " + line);
								
								String parts[] = line.split(" +");
								
								wli.type = parts[1];
								wli.name = parts[2].substring(1, parts[2].length()-1);
								
								String x = parts[6].split("=")[1];
								
								wli.pid = Integer.parseInt(x.substring(0, x.length()-1));
								
								wake_locks.add(wli);
							} else{
								locks = false;
							}
						}
					}
					return wake_locks;
				} else{
					Log.d("o", "...");
				}
			} catch(Exception e){
				Log.e("e", "error");
				e.printStackTrace();
			}
			return null;
		}
    	
    }
}
