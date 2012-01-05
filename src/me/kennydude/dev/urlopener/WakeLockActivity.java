package me.kennydude.dev.urlopener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class WakeLockActivity extends ListActivity {
	
	List<WakeLockInfo> wake_locks;
	FeedAdapter fa;
	
	public class WakeLockInfo{
		public Integer pid;
		public String name;
		public String type;
		public String package_name;
		public String app_name;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wake_locks = new ArrayList<WakeLockInfo>();
        fa = new FeedAdapter(this,android.R.layout.simple_list_item_1 , wake_locks);
        this.getListView().setAdapter(fa);
        getListView().setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				final CharSequence[] items = {
					getResources().getText(R.string.kill_app)
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(WakeLockActivity.this);
				builder.setItems(items, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				        if(item == 0){
				        	if(ShellInterface.isSuAvailable()) {
				        		WakeLockInfo wli = wake_locks.get(position);
								ShellInterface.getProcessOutput("kill " + wli.pid);
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
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.wake_lock, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			wake_locks.clear();
			fa.notifyDataSetChanged();
			new LoadWakeLocks().execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
			
			String caption = "<b>" + wli.name + "</b><br/>";
			caption += getResources().getString(R.string.type).replace("{type}", wli.type);
			caption += "<br/>";
			caption += getResources().getString(R.string.pid).replace("{pid}", wli.pid.toString());
			caption += "<br/>";
			caption += getResources().getString(R.string.process_name).replace("{pn}", wli.package_name);
			if(wli.app_name != null){
				caption += "<br/>";
				caption += getResources().getString(R.string.package_name).replace("{app}", wli.app_name);
			}
			
			ed.setPadding(5, 5, 5, 5);
			ed.setText(Html.fromHtml(caption));
			
			return ed;
		}
    }
    
    public class LoadWakeLocks extends AsyncTask<Object, Object, Object>{
    	
    	@SuppressWarnings("unchecked")
		@Override
    	protected void onPostExecute(Object r){
    		try{ dg.dismiss(); }
    		catch(Exception e){}
    		if(r == null){
    			Toast.makeText(WakeLockActivity.this,
    					getResources().getString(R.string.no_root),
    					Toast.LENGTH_LONG).show();
    		} else{
	    		WakeLockActivity.this.wake_locks.addAll((Collection<? extends WakeLockInfo>) r);
	    		fa.notifyDataSetChanged();
    		}
    	}
    	ProgressDialog dg;
    	
    	@Override
    	protected void onPreExecute(){
    		dg = new ProgressDialog(WakeLockActivity.this);
			dg.setMessage(getResources().getText(
					R.string.one_moment_please)
			);
			dg.setCancelable(false);
			dg.show();
    	}
    	
		@Override
		protected Object doInBackground(Object... arg0) {
			try{
				if(ShellInterface.isSuAvailable()) {
					String output = ShellInterface.getProcessOutput("dumpsys power");
					String[] lines = output.split("\n");
					Boolean locks = false;
					PackageManager pm = getPackageManager();
					
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
								
								// Now we have the PID, here's a sneaky pull to get the Package Name
								String ps_output = ShellInterface.getProcessOutput("ps " + wli.pid);
								String[] ps_lines = ps_output.split("\n");
								ps_output = ps_lines[1];
								ps_lines = ps_output.split(" +");
								ps_output = ps_lines[ps_lines.length - 1];
								wli.package_name = ps_output;
								try{
									PackageInfo pi = pm.getPackageInfo(wli.package_name, PackageManager.GET_GIDS);
									wli.app_name = pi.applicationInfo.loadLabel(pm).toString();
								} catch(Exception e){}
								
								wake_locks.add(wli);
							} else{
								locks = false;
							}
						}
					}
					return wake_locks;
				} else{
					return null;
				}
			} catch(Exception e){
				Log.e("e", "error");
				e.printStackTrace();
			}
			return null;
		}
    	
    }
}
