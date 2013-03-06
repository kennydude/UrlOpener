package me.kennydude.dev.urlopener;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

/**
 * This does 50% of the magic of the host server stuff
 * 
 * The rest is used on the computer side to actually do the real HTTP magic
 * 
 * @author kennydude
 * 
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class HostServerService extends Service {
	public static final int NOTIF_ID = 35984395;
	public static final String REFRESH_ACTION = "me.kennydude.devtools.REFRESH_SERVER_STATUS";

	public static boolean SERVICE_RUNNING = false;
	
	ArrayList<AsyncServerSocket> mListeners = new ArrayList<AsyncServerSocket>();
	WebSocketConnection mComputerSocket = null;
	WebServer mComputerBridge;
	AsyncServer mLocalServer;
	
	@SuppressLint("UseSparseArrays")
	//Socket mComputerSocket;
	HashMap<Integer, AsyncSocket> mRequests = new HashMap<Integer, AsyncSocket>();

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		SERVICE_RUNNING = false;
		refreshActivity();

		mComputerSocket.close();
		mComputerBridge.stop();
		mLocalServer.stop();

		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.cancel(NOTIF_ID);
	}

	public void refreshActivity() {
		Intent i = new Intent(REFRESH_ACTION);
		sendBroadcast(i);
	}

	public void showNotification() {
		// Put in the foreground
		NotificationCompat.Builder notif = new NotificationCompat.Builder(this);
		notif.setOngoing(true);
		notif.setSmallIcon(R.drawable.ic_stat_tool);
		notif.setContentTitle(getString(R.string.host_server));

		if (mComputerSocket == null) {
			notif.setContentText(getString(R.string.host_server_background));
		} else {
			notif.setContentText(getString(R.string.host_server_ok));
		}

		notif.setPriority(NotificationCompat.PRIORITY_LOW);

		Notification n = notif.build();
		startForeground(NOTIF_ID, n);
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(NOTIF_ID, n);
	}
	
	public class WebSocketInterface extends BaseWebSocketHandler{
		
		public void onOpen(WebSocketConnection connection) {
			Log.d("wss", "Websocket added");
			mComputerSocket = connection;
			showNotification();
		}
		
		public void onClose(WebSocketConnection connection) {
			if(connection.equals(mComputerSocket)){
				mComputerSocket = null;
				showNotification();
				
				// Close all active connections
				for(AsyncSocket ass : mRequests.values()){
					ass.close();
					mRequests.remove(ass);
				}
			}
		}
		
		public void onMessage(WebSocketConnection connection, String message) {
			if(message.charAt(0) == 'E'){
				try{
					Integer reqId = Integer.parseInt( message.substring(1, 2) );
					Log.d("hss", "Request #" + reqId + " finished");
					
					mRequests.get( reqId ).close();
					mRequests.remove( reqId );
				} catch(Exception e){
					e.printStackTrace();
				}
			} else{
				try{
					Integer reqId = Integer.parseInt( message.split(" ")[0] );
					String sub = message.substring( reqId.toString().length() + 1 );
					
					mRequests.get(reqId).write(ByteBuffer.wrap(
							Base64.decode( sub, Base64.DEFAULT)
					));
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
	}

	public void handleCommand(Intent intent) {
		SERVICE_RUNNING = true;

		Log.d("hostserverservice", "Starting services");

		showNotification();
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				mComputerBridge = WebServers.createWebServer(4000)
		                .add("/", new WebSocketInterface());
		        mComputerBridge.start();
			}
			
		}).start();

		
		mLocalServer = new AsyncServer();
		mLocalServer.setAutostart(true);
		mLocalServer.listen(null, 4001, new ListenCallback(){

			@Override
			public void onCompleted(Exception err) {
				Log.d("hss", "Local Server failure");
				err.printStackTrace();
			}

			@Override
			public void onAccepted(AsyncSocket socket) {
				if(mComputerSocket == null){
					// Do not answer if there is no computer connected
					socket.close();
					return;
				}
				
				int rid = mRequests.size();
				while(mRequests.containsKey(rid)){
					rid += 1;
				}
				final int requestId = rid;
				
				mRequests.put(requestId, socket);
				
				socket.setClosedCallback(new CompletedCallback(){

					@Override
					public void onCompleted(Exception arg0) {
						mComputerSocket.send( "{ \"id\" : " + requestId + ", \"event\" : \"close\" }" );
						mRequests.remove(requestId);
					}
					
				});
				socket.setDataCallback(new DataCallback(){

					@Override
					public void onDataAvailable(DataEmitter arg0,
							ByteBufferList bb) {
						StringBuilder data = new StringBuilder();
						
						while (bb.remaining() > 0) {
		                    byte b = bb.get();
		                    data.append((char)b);
		                }
						
						String line = data.toString();
						
						try{
							JSONObject jo = new JSONObject();
							jo.put("id", requestId);
							jo.put("event", "d");
							jo.put("c", line);
							
							mComputerSocket.send( jo.toString() );
						} catch(Exception e){
							e.printStackTrace();
						}
					}
					
				});
			}
			
			@Override
			public void onListening(AsyncServerSocket ass) {
				mListeners.add(ass);
			}
			
		});
		
		Log.d("hostserverservice", "up");

		refreshActivity();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		handleCommand(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		return START_STICKY;
	}

}
