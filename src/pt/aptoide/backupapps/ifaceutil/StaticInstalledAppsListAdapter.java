/**
 * StaticInstalledAppsListAdapter,		part of Aptoide's data model
 * Copyright (C) 2011  Duarte Silveira
 * duarte.silveira@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package pt.aptoide.backupapps.ifaceutil;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.aptoide.backupapps.EnumAppsLists;
import pt.aptoide.backupapps.EnumAptoideInterfaceTasks;
import pt.aptoide.backupapps.R;
import pt.aptoide.backupapps.data.display.ViewDisplayApplication;
import pt.aptoide.backupapps.data.display.ViewDisplayApplicationBackup;
import pt.aptoide.backupapps.data.display.ViewDisplayListApps;
import pt.aptoide.backupapps.data.model.ViewListIds;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import pt.aptoide.backupapps.data.AIDLAptoideServiceData;

 /**
 * StaticInstalledAppsListAdapter, models a static loading, installed apps list adapter
 * 									extends baseAdapter
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class StaticInstalledAppsListAdapter extends BaseAdapter{

	private ListView listView;
	private LayoutInflater layoutInflater;

	private AtomicBoolean zeroReset;
	
	private ImageLoader imageLoader;

	private ViewDisplayListApps apps = null;
	private ViewDisplayListApps freshApps = null;
	
	private InstalledAppsManager appsManager;
	
	private AIDLAptoideServiceData serviceDataCaller = null;
	
	private Handler aptoideTasksHandler;
	
	public Context context;
	
	public ArrayList<Integer> selectionsSavedState;
	

	
	private Handler interfaceTasksHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	EnumAptoideInterfaceTasks task = EnumAptoideInterfaceTasks.reverseOrdinal(msg.what);
        	switch (task) {
				
				case RESET_INSTALLED_LIST_DISPLAY:
					resetDisplay();
					break;
	
				default:
					break;
			}
        }
    };
    
    

    private class InstalledAppsManager{
    	private ExecutorService installedColectorsPool;
    	
    	public InstalledAppsManager(){
    		installedColectorsPool = Executors.newSingleThreadExecutor();
    	}
    	
    	public void cacheInstalledAppsIcons(){
    		installedColectorsPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						serviceDataCaller.callCacheInstalledAppsIcons();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});    		
    	}
    	
    	public void reset(){
        	try {
				installedColectorsPool.execute(new GetInstalledApps());
			} catch (Exception e) { }
        }
    	
    	private class GetInstalledApps implements Runnable{

			@Override
			public void run() {
				try {
					setFreshInstalledApps(serviceDataCaller.callGetInstalledApps());
					aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_INSTALLED_TO_PROGRESSBAR.ordinal());
					interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_INSTALLED_LIST_DISPLAY.ordinal());
				} catch (RemoteException e) {
					e.printStackTrace();
				}

			}
    		
    	}
    }
	
	
	
	public static class InstalledRowViewHolder{
		ImageView app_icon;
		
		TextView app_name;
		TextView version_prefix;
		TextView version_name;

		TextView timestamp;
		TextView status;
		TextView size;
		
		CheckBox check;
		
		public void setChecked(boolean checked){
			check.setChecked(checked);
		}
	}
	

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		InstalledRowViewHolder rowViewHolder;
		
		if(convertView == null){
			convertView = layoutInflater.inflate(R.layout.row_app_backup, null);
			
			rowViewHolder = new InstalledRowViewHolder();
			rowViewHolder.app_icon = (ImageView) convertView.findViewById(R.id.app_icon);
			rowViewHolder.app_name = (TextView) convertView.findViewById(R.id.app_name);
			rowViewHolder.version_prefix = (TextView) convertView.findViewById(R.id.version_prefix);
			rowViewHolder.version_name = (TextView) convertView.findViewById(R.id.version_name);
			
			rowViewHolder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
			
			rowViewHolder.status = (TextView) convertView.findViewById(R.id.status);
			rowViewHolder.size = (TextView) convertView.findViewById(R.id.size);
			
			rowViewHolder.check = (CheckBox) convertView.findViewById(R.id.check);
			
			convertView.setTag(rowViewHolder);
		}else{
			rowViewHolder = (InstalledRowViewHolder) convertView.getTag();
		}
		
//		File iconCache = new File(apps.get(position).getIconCachePath());
//		if(iconCache.exists() && iconCache.length() > 0){
//			rowViewHolder.app_icon.setImageURI(Uri.parse(apps.get(position).getIconCachePath()));
//		}else{
//			rowViewHolder.app_icon.setImageResource(android.R.drawable.sym_def_app_icon);
//		}
		
		imageLoader.DisplayImage(apps.get(position).getIconCachePath(), rowViewHolder.app_icon);
		
		
		rowViewHolder.app_name.setText(apps.get(position).getAppName());
		rowViewHolder.version_name.setText(" "+apps.get(position).getVersionName());
		
		rowViewHolder.timestamp.setText(((ViewDisplayApplicationBackup) apps.get(position)).getFormatedTimestamp());

		EnumAppStatus status = ((ViewDisplayApplicationBackup) apps.get(position)).getStatus();
		rowViewHolder.status.setText(status.toString(context));
		switch (status) {
			case SYSTEM:
			case PROTECTED:
			case TOO_BIG:
				rowViewHolder.status.setTextColor(Color.GRAY);
				rowViewHolder.app_name.setTextColor(Color.LTGRAY);
				rowViewHolder.version_prefix.setTextColor(Color.LTGRAY);
				rowViewHolder.version_name.setTextColor(Color.LTGRAY);
				rowViewHolder.size.setTextColor(Color.LTGRAY);
				break;
				
			case INSTALLED:
				rowViewHolder.status.setText("");	
			default:
				rowViewHolder.status.setTextColor(Color.rgb(Integer.parseInt("CC", 16), Integer.parseInt("66", 16), Integer.parseInt("00", 16)));
				rowViewHolder.app_name.setTextColor(Color.BLACK);
				rowViewHolder.version_prefix.setTextColor(Color.BLACK);
				rowViewHolder.version_name.setTextColor(Color.BLACK);
				rowViewHolder.size.setTextColor(Color.rgb(Integer.parseInt("4F", 16), Integer.parseInt("4F", 16), Integer.parseInt("4F", 16)));				
				break;
		}

		final ViewDisplayApplicationBackup installed = ((ViewDisplayApplicationBackup) apps.get(position));
		rowViewHolder.size.setText(installed.getSize());
		
		rowViewHolder.check.setChecked(installed.isChecked());
		rowViewHolder.check.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(installed.toggleCheck()){
		    		if(installed.getStatus().equals(EnumAppStatus.SYSTEM) || installed.getStatus().equals(EnumAppStatus.PROTECTED)){
		    			Toast.makeText(context, context.getString(R.string.app_system_backup_may_fail, installed.getAppName()), Toast.LENGTH_LONG).show();
		    		}else if(installed.getStatus().equals(EnumAppStatus.TOO_BIG)){
		    			Toast.makeText(context, context.getString(R.string.app_too_big_backup_may_fail, installed.getAppName()), Toast.LENGTH_LONG).show();
		    		}
				}
			}
		});
		
//		if(((ViewDisplayApplicationInstalled) apps.get(position)).isDowngradable()){
//			rowViewHolder.app_downgradable.setVisibility(View.VISIBLE);
//		}else{
//			rowViewHolder.app_downgradable.setVisibility(View.INVISIBLE);
//		}
//
//		if(((ViewDisplayApplicationInstalled) apps.get(position)).isUpdatable()){
//			rowViewHolder.app_upgradable.setVisibility(View.VISIBLE);
//		}else{
//			rowViewHolder.app_upgradable.setVisibility(View.INVISIBLE);
//		}
		
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		return apps.size();
	}

	@Override
	public ViewDisplayApplication getItem(int position) {
		return apps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return apps.get(position).getAppHashid();
	}
	
	public void toggleSelectAll(){
		if(!apps.isEmpty() && ((ViewDisplayApplicationBackup) apps.get(0)).isChecked()){
			unselectAll();
		}else{
			selectAll();
		}
	}
	
	public void selectAll(){
		for (ViewDisplayApplication backup : apps) {
			if(!((ViewDisplayApplicationBackup) backup).isChecked()){
				((ViewDisplayApplicationBackup) backup).toggleCheck();
			}
		}
		notifyDataSetChanged();
	}
	
	public void unselectAll(){
		for (ViewDisplayApplication backup : apps) {
			if(((ViewDisplayApplicationBackup) backup).isChecked()){
				((ViewDisplayApplicationBackup) backup).toggleCheck();
			}
		}
		notifyDataSetChanged();
	}
	
	public void saveSelectionState(){
		selectionsSavedState = new ArrayList<Integer>();
		int i;
		ViewDisplayApplication app;
		for (i=0; i<apps.size(); i++) {
			app = apps.get(i);
			ViewDisplayApplicationBackup backup = ((ViewDisplayApplicationBackup) app);
			if(backup.isChecked()){
				selectionsSavedState.add(i);
			}
		}
	}
	
	public void restoreSelectedState(){
		for (Integer selected : selectionsSavedState) {
			((ViewDisplayApplicationBackup) apps.get(selected)).toggleCheck();
		}
	}
	
	public ViewListIds getSelectedIds(){
		ViewListIds selected = new ViewListIds();
		for (ViewDisplayApplication app: apps) {
			ViewDisplayApplicationBackup backup = ((ViewDisplayApplicationBackup) app);
			if(backup.isChecked()){
				selected.add(backup.getAppHashid());
			}
		}
		return selected;
	}
	
	
	/**
	 * StaticInstalledAppsListAdapter Constructor
	 *
	 * @param context
	 * @param textViewResourceId
	 */
	public StaticInstalledAppsListAdapter(Context context, ListView listView, AIDLAptoideServiceData serviceDataCaller, Handler aptoideTasksHandler) {
		
		this.context = context;
		this.serviceDataCaller = serviceDataCaller;
		this.aptoideTasksHandler = aptoideTasksHandler;
		
		zeroReset = new AtomicBoolean(false);
		
		apps = new ViewDisplayListApps();

		appsManager = new InstalledAppsManager();
		
		imageLoader = new ImageLoader(context);

		this.listView = listView;
		layoutInflater = LayoutInflater.from(context);
		
		selectionsSavedState = new ArrayList<Integer>();
		
	} 
	
	public void zeroResetDisplayInstalled(){
		zeroReset.set(true);
		appsManager.reset();
	}
	
	public void resetDisplayInstalled(){
		zeroReset.set(false);
		appsManager.reset();
	}
	
	public void refreshDisplayInstalled(){
		zeroReset.set(false);
		notifyDataSetChanged();
	}
	
	
	
    private void initDisplay(){
		listView.setAdapter(this);    	
    }
	
	private synchronized void setFreshInstalledApps(ViewDisplayListApps freshInstalledApps){
		this.freshApps = freshInstalledApps;
	}
	
	private void resetDisplay(){
		if((freshApps == null || freshApps.isEmpty()) && !zeroReset.get()){
			aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_INSTALLED_TO_NO_APPS.ordinal());
		}else{
			aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_INSTALLED_TO_LIST.ordinal());
		
			boolean savingSelectionState = false;
			if(this.apps.size() == freshApps.size()){
				savingSelectionState = true;
				saveSelectionState();
			}
			
	    	this.apps = freshApps;
			Log.d("Aptoide-StaticInstalledAppsListAdapter", "new Installed List: "+getCount());
	   		initDisplay();
	    	refreshDisplayInstalled();
	    	
	    	if(savingSelectionState){
	    		restoreSelectedState();
	    	}

	    	if(!zeroReset.get()){
	    		aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_AVAILABLE_LIST_DISPLAY.ordinal());
//	    		aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_UPDATABLE_LIST_DISPLAY.ordinal());
	    	}
	    	
	    	appsManager.cacheInstalledAppsIcons();
		}
	}
	
	public void shutdownNow(){
		appsManager.installedColectorsPool.shutdownNow();
	}
	
}
