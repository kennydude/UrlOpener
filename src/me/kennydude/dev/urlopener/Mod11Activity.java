package me.kennydude.dev.urlopener;

import java.util.regex.Pattern;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class Mod11Activity extends Activity {
	String reverse(String in){
		return (new StringBuffer(in)).reverse().toString();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.mod11);
        
        Button btn = (Button)this.findViewById(R.id.button);
        btn.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				// MOD11 CODE HERE
				EditText ed = (EditText)findViewById(R.id.value);
				String t = ed.getText().toString();
				Log.d("Text", t);
				boolean valid = false;
				
				CheckBox cbox = (CheckBox)findViewById(R.id.check);
				
				Pattern pattern = Pattern.compile("^[0-9]+$");
				if(cbox.isChecked()){
					pattern = Pattern.compile("^[0-9X]+$");
				}
				if(pattern.matcher(t).find()){
					valid = true;
				}
				
				TextView result = (TextView)findViewById(R.id.result);
				result.setVisibility(View.VISIBLE);
								
				if( valid == true ){
					int startPower = 2;
					int subtotal = 0;
					
					if(cbox.isChecked()){
						startPower = 1;
					}
					
					for(char digit : reverse(t).toCharArray()){
						if(digit == 'X'){
							subtotal += 10 * startPower;
						} else{
							subtotal += Integer.decode(digit + "") * startPower;
						}
						Log.d("calc", digit + " * " + startPower + ". ST: " + subtotal);
						if(startPower < 10){
							startPower += 1;
						}
					}
					
					int modulus = subtotal % 11;
					
					if(cbox.isChecked()){
						if(modulus == 0){
							result.setText(t + ": Check Digit is VALID!");
						} else{
							result.setText(t + ": Check Digit is INVALID!");
						}
					} else{
						modulus = 11 - modulus;
						if(modulus == 10){
							result.setText(t + ": Result: " + modulus + " (Could be an X in ISBN)");
						} else{
							result.setText(t + ": Result: " + modulus);
						}
					}
				} else{
					result.setText(t + ": Not a number!");
				}
			}
        	
        });
    }
}
