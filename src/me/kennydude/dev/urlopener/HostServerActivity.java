package me.kennydude.dev.urlopener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * We run as a service. Sorry that there isn't anything exciting here.
 * 
 * Move along... move along
 * 
 * @author kennydude
 *
 */
public class HostServerActivity extends Activity {
	
	public BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			syncButton();
		}
		
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.host_server);
        
        syncButton();
        
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	
        IntentFilter filter = new IntentFilter();
        filter.addAction(HostServerService.REFRESH_ACTION);
        registerReceiver(mReceiver, filter);
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	try{
    		this.unregisterReceiver(mReceiver);
    	} catch(Exception e){}
    }
    
    public Intent getServiceIntent(){
    	return new Intent(this, HostServerService.class);
    }
    
    public void syncButton(){
    	Button btn = (Button)findViewById(R.id.button);
    	if(HostServerService.SERVICE_RUNNING){
    		btn.setText(R.string.disable);
    		btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					stopService(getServiceIntent());
					syncButton();
				}
				
			});
    	} else{
    		btn.setText(R.string.enable);
    		btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startService(getServiceIntent());
					syncButton();
				}
				
			});
    	}
    }
    
}
