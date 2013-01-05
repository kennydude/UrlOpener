package me.kennydude.dev.urlopener;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class ANRActivity extends Activity {

	@SuppressWarnings("null")
	@Override
	public void onCreate(Bundle bis){
		super.onCreate(bis);
		
		MenuItem i = null;
		i.getIcon();
	}
}
