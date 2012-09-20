package pt.aptoide.backupapps.ifaceutil.pagerIndicater;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pt.aptoide.backupapps.DialogBeforeRestoringAlert;
import pt.aptoide.backupapps.EnumAppsLists;
import pt.aptoide.backupapps.R;
import pt.aptoide.backupapps.data.AptoideServiceData;
import pt.aptoide.backupapps.data.AIDLAptoideServiceData;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.astuetz.viewpager.extensions.TabsAdapter;
import com.astuetz.viewpager.extensions.ViewPagerTabButton;

public class FixedTabsAdapter implements TabsAdapter {
	
	private ExecutorService threadsPool;
	
	private AIDLAptoideServiceData serviceDataCaller = null;

	private boolean serviceDataIsBound = false;

	private ServiceConnection serviceDataConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service.  We are communicating with the
			// service using AIDL, so here we set the remote service interface.
			serviceDataCaller = AIDLAptoideServiceData.Stub.asInterface(service);
			serviceDataIsBound = true;
			
			Log.v("Aptoide-TabsAdapter", "Connected to ServiceData");
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			serviceDataIsBound = false;
			serviceDataCaller = null;
			
			Log.v("Aptoide-Login", "Disconnected from ServiceData");
		}
	};	
	
	private Activity context;
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	launchDialogBeforeRestoringAlert();
        }
    };
	
	public FixedTabsAdapter(Activity context) {
		this.context = context;
		threadsPool = Executors.newSingleThreadExecutor();
		
		if(!serviceDataIsBound){
    		context.bindService(new Intent(context, AptoideServiceData.class), serviceDataConnection, Context.BIND_AUTO_CREATE);
    	}
	}

	@Override
	public View getView(int position) {
		
		ViewPagerTabButton tab;
		
		EnumAppsLists newVisiblePage = EnumAppsLists.reverseOrdinal(position);
//		Log.d("Aptoide-AppsBackup", "Viewing page: "+newVisiblePage);
		
		LayoutInflater inflater = context.getLayoutInflater();
		tab = (ViewPagerTabButton) inflater.inflate(R.layout.tab_fixed, null);
		tab.setLineColorSelected(0x8365148);
		tab.setLineColor(0x8365148);
		tab.setText(newVisiblePage.toString(context));
		
		return tab;
	}

	@Override
	public void onPageSelected(final int position) {
		threadsPool.execute(new Runnable() {
			public void run() {
				EnumAppsLists newVisiblePage = EnumAppsLists.reverseOrdinal(position);
				Log.d("Aptoide-AppsBackup", "Viewing page: " + newVisiblePage);
				String token = null;
				try {
					token = serviceDataCaller.callGetServerToken();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (newVisiblePage.equals(EnumAppsLists.RESTORE) && token == null) {
					handler.sendEmptyMessage(0);
				}
			}
		});
	}
	
	private void launchDialogBeforeRestoringAlert(){
		new DialogBeforeRestoringAlert(context).show();
	}
	
	public void destroy() {
		if(serviceDataIsBound){
			context.unbindService(serviceDataConnection);
		}
	}
	
}
