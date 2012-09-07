/**
 * StaticAvailableAppsListAdapter,		part of Aptoide's data model
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

package pt.aptoide.appsbackup.ifaceutil;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pt.aptoide.appsbackup.EnumAptoideInterfaceTasks;
import pt.aptoide.appsbackup.data.display.ViewDisplayApplication;
import pt.aptoide.appsbackup.data.display.ViewDisplayApplicationBackup;
import pt.aptoide.appsbackup.data.display.ViewDisplayListApps;
import pt.aptoide.appsbackup.data.model.ViewListIds;
import pt.aptoide.appsbackup.R;
import pt.aptoide.appsbackup.data.AIDLAptoideServiceData;
import android.content.Context;
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

 /**
 * StaticAvailableAppsListAdapter, models a static loading, available apps list adapter
 * 									extends BaseAdapter implements InterfaceAvailableAppsAdapter
 * 
 * @author dsilveira
 *
 */
public class StaticAvailableAppsListAdapter extends BaseAdapter implements InterfaceAvailableAppsAdapter{

	private Context context;
	private ListView listView;
	private LayoutInflater layoutInflater;
	
	ImageLoader imageLoader;

	private ViewDisplayListApps apps = null;
	private ViewDisplayListApps freshApps = null;
	
	private AvailableAppsManager appsManager;
	
	private AIDLAptoideServiceData serviceDataCaller = null;
	
	private Handler aptoideTasksHandler;
	
	public ArrayList<Integer> selectionsSavedState;

	
	private Handler interfaceTasksHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	EnumAptoideInterfaceTasks task = EnumAptoideInterfaceTasks.reverseOrdinal(msg.what);
        	switch (task) {
				
				case RESET_AVAILABLE_LIST_DISPLAY:
					resetDisplay();
					break;
	
				default:
					break;
			}
        }
    };
    
    

    private class AvailableAppsManager{
    	private ExecutorService availableColectorsPool;
    	
    	public AvailableAppsManager(){
    		availableColectorsPool = Executors.newSingleThreadExecutor();
    	}
    	
    	public void reset(){
        	try {
				availableColectorsPool.execute(new GetAvailableApps());
			} catch (Exception e) { }
        }
    	
    	private class GetAvailableApps implements Runnable{

			@Override
			public void run() {
				aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_PROGRESSBAR.ordinal());
				try {
					setFreshAvailableApps(serviceDataCaller.callGetAllAvailableApps());
					interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_AVAILABLE_LIST_DISPLAY.ordinal());
				} catch (RemoteException e) {
					e.printStackTrace();
				}

			}
    		
    	}
    }
	
	
	
	public static class AvailableRowViewHolder{
		ImageView app_icon;
		
		TextView app_name;
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
		
		AvailableRowViewHolder rowViewHolder;
		
		if(convertView == null){
			convertView = layoutInflater.inflate(R.layout.row_app_backup, null);
			
			rowViewHolder = new AvailableRowViewHolder();
			rowViewHolder.app_icon = (ImageView) convertView.findViewById(R.id.app_icon);
			rowViewHolder.app_name = (TextView) convertView.findViewById(R.id.app_name);
			rowViewHolder.version_name = (TextView) convertView.findViewById(R.id.version_name);

			rowViewHolder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
			
			rowViewHolder.status = (TextView) convertView.findViewById(R.id.status);
			rowViewHolder.size = (TextView) convertView.findViewById(R.id.size);
			
			rowViewHolder.check = (CheckBox) convertView.findViewById(R.id.check);
			
			convertView.setTag(rowViewHolder);
		}else{
			rowViewHolder = (AvailableRowViewHolder) convertView.getTag();
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
		
		rowViewHolder.status.setText(((ViewDisplayApplicationBackup) apps.get(position)).getStatus().toString(context));
		rowViewHolder.size.setText(((ViewDisplayApplicationBackup) apps.get(position)).getSize());
		
		rowViewHolder.check.setChecked(((ViewDisplayApplicationBackup) apps.get(position)).isChecked());
		rowViewHolder.check.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((ViewDisplayApplicationBackup) apps.get(position)).toggleCheck();
			}
		});
		
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
	
	@Override
	public void toggleSelectAll(){
		if(!apps.isEmpty() && ((ViewDisplayApplicationBackup) apps.get(0)).isChecked()){
			unselectAll();
		}else{
			selectAll();
		}
	}
	
	@Override
	public void selectAll(){
		for (ViewDisplayApplication backup : apps) {
			if(!((ViewDisplayApplicationBackup) backup).isChecked()){
				((ViewDisplayApplicationBackup) backup).toggleCheck();
			}
		}
		notifyDataSetChanged();
	}
	
	@Override
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
	
	@Override
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
	public StaticAvailableAppsListAdapter(Context context, ListView listView, AIDLAptoideServiceData serviceDataCaller, Handler aptoideTasksHandler) {
		
		this.context = context;
		this.serviceDataCaller = serviceDataCaller;
		this.aptoideTasksHandler = aptoideTasksHandler;
		
		apps = new ViewDisplayListApps();

		appsManager = new AvailableAppsManager();
		
		imageLoader = new ImageLoader(context);


		this.listView = listView;
		layoutInflater = LayoutInflater.from(context);
	} 
	
	
	
	@Override
	public void resetDisplayAvailable(){
		appsManager.reset();
//    	aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_INSTALLED_LIST_DISPLAY.ordinal());
	}
	
	@Override
	public void refreshDisplayAvailable(){
		notifyDataSetChanged();
	}
	
	
	
    private void initDisplay(){
		listView.setAdapter(this);    	
    }
	
	private synchronized void setFreshAvailableApps(ViewDisplayListApps freshInstalledApps){
		this.freshApps = freshInstalledApps;
	}
	
	private void resetDisplay(){
		if(freshApps == null || freshApps.isEmpty()){
			aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_NO_APPS.ordinal());
		}else{
			aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_LIST.ordinal());

			boolean savingSelectionState = false;
			if(this.apps.size() == freshApps.size()){
				savingSelectionState = true;
				saveSelectionState();
			}
			
	    	this.apps = freshApps;
			Log.d("Aptoide-StaticAvailableAppsListAdapter", "new Available List: "+getCount());
	   		initDisplay();
	    	refreshDisplayAvailable();

	    	if(savingSelectionState){
	    		restoreSelectedState();
	    	}
	    	
//	    	aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_UPDATABLE_LIST_DISPLAY.ordinal());
		}
	}

	
	@Override
	public boolean isDynamic() {
		return false;
	}
	
	@Override
	public void shutdownNow(){
		appsManager.availableColectorsPool.shutdownNow();
	}
	
}
