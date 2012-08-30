package pt.caixamagica.aptoide.appsbackup.ifaceutil.pagerIndicater;

import pt.caixamagica.aptoide.appsbackup.DialogBeforeRestoringAlert;
import pt.caixamagica.aptoide.appsbackup.EnumAppsLists;
import pt.caixamagica.aptoide.appsbackup.R;
import pt.caixamagica.aptoide.appsbackup.data.AIDLAptoideServiceData;
import pt.caixamagica.aptoide.appsbackup.data.AptoideServiceData;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.astuetz.viewpager.extensions.TabsAdapter;
import com.astuetz.viewpager.extensions.ViewPagerTabButton;

public class FixedTabsAdapter implements TabsAdapter {
	
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
	
	public FixedTabsAdapter(Activity context) {
		this.context = context;
		
		if(!serviceDataIsBound){
    		context.bindService(new Intent(context, AptoideServiceData.class), serviceDataConnection, Context.BIND_AUTO_CREATE);
    	}
	}

	Button tabButtons[] = new Button[2];
	
	@Override
	public View getView(int position) {
		
		EnumAppsLists newVisiblePage = EnumAppsLists.reverseOrdinal(position);
//		Log.d("Aptoide-AppsBackup", "Viewing page: "+newVisiblePage);
		
		LayoutInflater inflater = context.getLayoutInflater();
		tabButtons[position] = (Button) inflater.inflate(R.layout.tab_fixed, null); 
		tabButtons[position].setText(newVisiblePage.toString(context));
		
		return tabButtons[position];
	}

	public void selectTab(int position) {
		for (int i = 0, pos = 0; i < tabButtons.length; i++) {
			if (this.tabButtons[i] instanceof Button) {
				this.tabButtons[i].setSelected(pos == position);
				pos++;
			}
		}
	}
	
	@Override
	public void onPageSelected(int position) {
		EnumAppsLists newVisiblePage = EnumAppsLists.reverseOrdinal(position);
		Log.d("Aptoide-AppsBackup", "Viewing page: "+newVisiblePage);
		String token = null;
		try {
			token = serviceDataCaller.callGetServerToken();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(newVisiblePage.equals(EnumAppsLists.RESTORE) && token == null){
        	new DialogBeforeRestoringAlert(context).show();
		}
		selectTab(position);
	}
	
	public void destroy() {
		if(serviceDataIsBound){
			context.unbindService(serviceDataConnection);
		}
	}
	
}
