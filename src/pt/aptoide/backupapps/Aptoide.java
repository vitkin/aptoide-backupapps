/**
 * Aptoide, Alternative client-side Android Package Manager
 * from v3.0 Copyright (C) 2011 Duarte Silveira 
 * duarte.silveira@caixamagica.pt
 * 
 * derivative work of original Aptoide Copyright (C) 2009 Roberto Jacinto
 * roberto.jacinto@caixamágica.pt
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
package pt.aptoide.backupapps;


import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import pt.aptoide.backupapps.R;
import pt.aptoide.backupapps.data.AptoideServiceData;
import pt.aptoide.backupapps.data.EnumAvailableAppsStatus;
import pt.aptoide.backupapps.data.display.ViewDisplayApplicationBackup;
import pt.aptoide.backupapps.data.display.ViewDisplayApplicationInstalled;
import pt.aptoide.backupapps.data.display.ViewDisplayCategory;
import pt.aptoide.backupapps.data.listeners.ViewMyapp;
import pt.aptoide.backupapps.data.model.ViewListIds;
import pt.aptoide.backupapps.data.system.ViewScreenDimensions;
import pt.aptoide.backupapps.data.util.Constants;
import pt.aptoide.backupapps.debug.AptoideLog;
import pt.aptoide.backupapps.debug.InterfaceAptoideLog;
import pt.aptoide.backupapps.ifaceutil.DynamicAvailableAppsListAdapter;
import pt.aptoide.backupapps.ifaceutil.EnumAppStatus;
import pt.aptoide.backupapps.ifaceutil.InterfaceAvailableAppsAdapter;
import pt.aptoide.backupapps.ifaceutil.StaticAvailableAppsListAdapter;
import pt.aptoide.backupapps.ifaceutil.StaticCategoriesListAdapter;
import pt.aptoide.backupapps.ifaceutil.StaticInstalledAppsListAdapter;
import pt.aptoide.backupapps.ifaceutil.StaticUpdatableAppsListAdapter;
import pt.aptoide.backupapps.ifaceutil.StaticAvailableAppsListAdapter.AvailableRowViewHolder;
import pt.aptoide.backupapps.ifaceutil.StaticInstalledAppsListAdapter.InstalledRowViewHolder;
import pt.aptoide.backupapps.ifaceutil.pagerIndicater.FixedTabsAdapter;
import pt.aptoide.backupapps.ifaceutil.pagerIndicater.ViewPagerAdapter;

import com.astuetz.viewpager.extensions.FixedTabsView;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import pt.aptoide.backupapps.AIDLAptoideInterface;
import pt.aptoide.backupapps.data.AIDLAptoideServiceData;

/**
 * Aptoide, the main interface class
 * 			displays the available apps list
 * 
 * @author dsilveira
 *
 */
public class Aptoide extends Activity implements InterfaceAptoideLog, OnItemClickListener { 
	
	private final String TAG = "Aptoide";
	private String versionName;
	private boolean isRunning = false;
	
	ContextThemeWrapper theme;
	
//	private GestureDetector swypeDetector;
//	private View.OnTouchListener swypeListener;
//	private AtomicBoolean swyping = null;
//	private Handler swypeDelayHandler = null;
//
//	private TextView previousListTitle = null;
//	private TextView currentListTitle = null;
//	private TextView nextListTitle = null;
//	private ImageView previousViewArrow = null;
//	private ImageView nextViewArrow = null;

//	private ImageView searchView;
	private FixedTabsView pageIndicator;
	private FixedTabsAdapter pageIndicatorAdapter;
	private ViewPager appsListPager = null;
//	private View emptyCategoriesList;
	private View emptyAvailableAppsList;
	private View emptyInstalledAppsList;
//	private View emptyUpdatableAppsList;
//	private View loadingCategoriesList;
	private View loadingAvailableAppsList;
	private View loadingAppsTitle;
	private View loadingAppsTitleWaitingOnServer;
	private ProgressBar loadingAvailableAppsUnknownProgress;
	private ProgressBar loadingAvailableAppsProgress;
	private AtomicInteger loadingAvailableAppsProgressCompletionTarget;
	private AtomicInteger loadingAvailableAppsProgressCurrent;
	private View loadingInstalledAppsList;
//	private View loadingUpdatableAppsList;
//	private ListView categoriesListView = null;
	private ListView availableAppsListView = null;
	private ListView installedAppsListView = null;
//	private ListView updatableAppsListView = null;
	private EnumAppsLists currentAppsList = null;
	
	private AtomicBoolean synchronizingInstalledApps = null;
	private String blockedSearchQuery = "";
	
	private String authenticationToken = null;
//	private enum EnumFlipperChildType{ EMPTY, LOADING, LIST };
	
	private boolean availableByCategory = true;
	private EnumAppsSorting appsSortingPolicy = null;
	private AtomicBoolean loadingRepos = null;
	private AtomicBoolean resettingFlipper = null;
	private AtomicBoolean highPriority = null;
	
	private ArrayList<EnumAptoideInterfaceTasks> queuedFlipperChanges;
	
	
	private boolean anyReposInUse = true;
	
//	private ArrayList<ViewMyapp> handlingMyapps;
	
//	private StaticCategoriesListAdapter categoriesAdapter = null;
//	private DynamicAvailableAppsListAdapter availableAdapter = null;
	private InterfaceAvailableAppsAdapter availableAdapter = null;
	private StaticInstalledAppsListAdapter installedAdapter = null;
//	private StaticUpdatableAppsListAdapter updatableAdapter = null;
	
	
	private AIDLAptoideServiceData serviceDataCaller = null;

	private boolean serviceDataSeenRunning = false;
	private boolean serviceDataIsBound = false;

	
	private ServiceConnection serviceDataConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service.  We are communicating with the
			// service using AIDL, so here we set the remote service interface.
			serviceDataCaller = AIDLAptoideServiceData.Stub.asInterface(service);
			serviceDataIsBound = true;
			
			AptoideLog.v(Aptoide.this, "Connected to ServiceData");	
//			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.ZERO_RESET_INSTALLED_LIST_DISPLAY.ordinal());
			
			if(!serviceDataSeenRunning){
				synchronizingInstalledApps.set(true);
				try {
		            
		            AptoideLog.v(Aptoide.this, "Called for a synchronization of installed Packages, because serviceData wasn't previously running");

		            switchInstalledToProgressBar();
//		            showInstalledList();
		            
		            serviceDataCaller.callSyncInstalledApps();	
		        } catch (RemoteException e) {
					// TODO Auto-generated catch block
		            e.printStackTrace();
		        }
			}
			
            
            try {
                AptoideLog.v(Aptoide.this, "Called for registering as InstalledApps Observer");
				serviceDataCaller.callRegisterInstalledAppsObserver(serviceDataCallback);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        
	        handleIncomingIntent(getIntent());
			
	        
	        try {
				versionName = serviceDataCaller.callGetAptoideVersionName();
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        
            
			DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			ViewScreenDimensions screenDimensions = new ViewScreenDimensions(displayMetrics.widthPixels, displayMetrics.heightPixels, displayMetrics.density);
			AptoideLog.d(Aptoide.this, screenDimensions.toString());
	        try {
	            AptoideLog.v(Aptoide.this, "Called for screenDimensions storage");
	            serviceDataCaller.callStoreScreenDimensions(screenDimensions);
	        } catch (RemoteException e) {
				// TODO Auto-generated catch block
	            e.printStackTrace();
	        }

			initListsAdapters();
	        
	        try {
//	            AptoideLog.v(Aptoide.this, "Called for checking if AvailableApps are by Category");
//	            availableByCategory = serviceDataCaller.callAreListsByCategory();
	            
	            AptoideLog.v(Aptoide.this, "Called for getting apps sorting policy");
	            appsSortingPolicy = EnumAppsSorting.reverseOrdinal(serviceDataCaller.callGetAppsSortingPolicy());	        	
	        		            
	            AptoideLog.v(Aptoide.this, "Called for registering as AvailableApps Observer");
	            if(serviceDataCaller.callRegisterAvailableAppsObserver(serviceDataCallback) == EnumAvailableAppsStatus.NO_REPO_IN_USE.ordinal()){
//	            	DialogLogin dialogLogin = new DialogLogin(Aptoide.this, serviceDataCaller, DialogLogin.InvoqueType.NO_CREDENTIALS_SET);
//	            	dialogLogin.setOnDismissListener(new OnDismissListener() {
//						@Override
//						public void onDismiss(DialogInterface dialog) {
//							try {
//								authenticationToken = serviceDataCaller.callGetServerToken();
//							} catch (RemoteException e) {
//								e.printStackTrace();
//								finish();
//							}
//							if(authenticationToken == null){
//								finish();
//							}
//						}
//					});
//					dialogLogin.show();
	            	anyReposInUse = false;
	            	new DialogFirstRunState(Aptoide.this).show();
	            }

//	            AptoideLog.v(Aptoide.this, "Called for registering as Myapp Observer");
//	            serviceDataCaller.callRegisterMyappReceiver(serviceDataCallback);

	        } catch (RemoteException e) {
				// TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        
	        
//	        switchAvailableToEmpty();
//	        switchInstalledToEmpty();
//	        switchUpdatableToEmpty();
	        
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			serviceDataCaller = null;
			serviceDataIsBound = false;
			
			AptoideLog.v(Aptoide.this, "Disconnected from ServiceData");
		}
	};
	
	
	
	private AIDLAptoideInterface.Stub serviceDataCallback = new AIDLAptoideInterface.Stub() {

		@Override
		public void refreshInstalledDisplay() throws RemoteException {
			AptoideLog.v(Aptoide.this, "received refreshInstalledDisplay callback");
			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.REFRESH_INSTALLED_DISPLAY.ordinal());
		}
		
		@Override
		public void newInstalledListDataAvailable() throws RemoteException {
			AptoideLog.v(Aptoide.this, "received newInstalledListDataAvailable callback");
			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SYNCHRONIZED_INSTALLED_LIST.ordinal());
		}
		
		@Override
		public void newAvailableListDataAvailable() throws RemoteException {
			AptoideLog.v(Aptoide.this, "received newAvailableListDataAvailable callback");
			reloadDisplayAvailable();
		}

		@Override
		public void resetAvailableListData() throws RemoteException {
			AptoideLog.v(Aptoide.this, "received resetAvailableListData callback");	
//			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_PROGRESSBAR.ordinal());
//			if( category == null || category.getCategoryHashid() == Constants.TOP_CATEGORY || category.hasChildren() )	
			resetDisplayAvailable();
		}

		@Override
		public void refreshAvailableDisplay() throws RemoteException {
			AptoideLog.v(Aptoide.this, "received refreshAvailableDisplay callback");
			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.REFRESH_AVAILABLE_DISPLAY.ordinal());
			
		}

		@Override
		public void switchAvailableToStaticList() throws RemoteException {
			AptoideLog.v(Aptoide.this, "received switchAvailableToStaticList callback");
			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_STATIC_LIST.ordinal());
		}

		@Override
		public void switchAvailableToDynamicList() throws RemoteException {
			AptoideLog.v(Aptoide.this, "received switchAvailableToDynamicList callback");
			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_DYNAMIC_LIST.ordinal());			
		}

		@Override
		public void switchAvailableToWaitingOnServer() throws RemoteException {
			AptoideLog.v(Aptoide.this, "received switchAvailableToWaitingOnServer callback");
			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_WAITING_ON_SERVER.ordinal());
		}

		@Override
		public void noAvailableListDataAvailable() throws RemoteException {
			AptoideLog.v(Aptoide.this, "received noAvailableApps callback");
			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_NO_APPS.ordinal());
		}

		@Override
		public void loadingAvailableListDataAvailable() throws RemoteException {
			AptoideLog.v(Aptoide.this, "received loadingAvailableApps callback");
			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_PROGRESSBAR.ordinal());
		}

		@Override
		public void loadingAvailableListProgressSetCompletionTarget(int progressCompletionTarget) throws RemoteException {
			AptoideLog.v(Aptoide.this, "received loadingAvailableApps callback, progress completion target: "+progressCompletionTarget);
			loadingAvailableAppsProgressCompletionTarget.set(progressCompletionTarget);
			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.AVAILABLE_PROGRESS_SET_COMPLETION_TARGET.ordinal());
		}

		@Override
		public void loadingAvailableListProgressUpdate(int currentProgress) throws RemoteException {
			AptoideLog.v(Aptoide.this, "received loadingAvailableApps callback, progress update: "+currentProgress);
			loadingAvailableAppsProgressCurrent.set(currentProgress);
			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.AVAILABLE_PROGRESS_UPDATE.ordinal());
		}

		@Override
		public void loadingAvailableListProgressIndeterminate() throws RemoteException {
			loadingAvailableAppsProgressCompletionTarget.set(0);
			loadingAvailableAppsProgressCurrent.set(0);
			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.AVAILABLE_PROGRESS_INDETERMINATE.ordinal());
		}

		@Override
		public void loadingInstalledListDataAvailable() throws RemoteException {
			AptoideLog.v(Aptoide.this, "received loadingInstalledApps callback");
			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_INSTALLED_TO_PROGRESSBAR.ordinal());
		}

		@Override
		public void handleMyapp() throws RemoteException {
//			AptoideLog.v(Aptoide.this, "received handleMyapp callback");
//			ViewMyapp myapp = null;
//			try {
//				myapp = serviceDataCaller.callGetWaitingMyapp();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if(myapp != null){
//				handlingMyapps.add(myapp);
//			}
//			interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.HANDLE_MYAPP.ordinal());
		}

		@Override
		public void startedLoadingRepos() throws RemoteException {
			loadingRepos.set(true);
		}

		@Override
		public void finishedLoadingRepos() throws RemoteException {
			loadingRepos.set(false);
		}

		@Override
		public void shutDown() throws RemoteException {
			finish();
		}
		
	};
	
    
    private Handler interfaceTasksHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	EnumAptoideInterfaceTasks task = EnumAptoideInterfaceTasks.reverseOrdinal(msg.what);
        	switch (task) {
        		case SYNCHRONIZED_INSTALLED_LIST:
	    			if(synchronizingInstalledApps.get()){
	    				synchronizingInstalledApps.set(false);
	    				if(!blockedSearchQuery.equals("")){
	    					startSearch(blockedSearchQuery, false, null, false);
	    					blockedSearchQuery = "";
	    				}        		
	    			}	
	    			installedAdapter.resetDisplayInstalled();
	        		installedAppsListView.setAdapter(installedAdapter);
        			break;
        	
	        	case REFRESH_INSTALLED_DISPLAY:
	        		installedAdapter.refreshDisplayInstalled();
					break;
        	
	        	case ZERO_RESET_INSTALLED_LIST_DISPLAY:
	    			installedAdapter.zeroResetDisplayInstalled();
	        		installedAppsListView.setAdapter(installedAdapter);
					break;
        	
	        	case RESET_INSTALLED_LIST_DISPLAY:
	        		installedAdapter.resetDisplayInstalled();
					break;
					
//	        	case RESET_CATEGORIES:
//	        		resetDisplayCategories();
//	        		break;
				
				case RESET_AVAILABLE_LIST_DISPLAY:
					resetDisplayAvailable(); 
					break;
					
				case REFRESH_AVAILABLE_DISPLAY:
//					if(!availableByCategory || (categoriesAdapter.getCategory() != null && !categoriesAdapter.getCategory().hasChildren())){
						availableAdapter.refreshDisplayAvailable();
//					}
					break;
				
//				case RESET_UPDATABLE_LIST_DISPLAY:
//					updatableAdapter.resetDisplayUpdatable();
//					break;
//					
//				case REFRESH_UPDATABLE_DISPLAY:
//					updatableAdapter.refreshDisplayUpdatable();
//					break;
					
				case SWITCH_AVAILABLE_TO_PROGRESSBAR:
					switchAvailableToProgressBar();
					break;
					
				case AVAILABLE_PROGRESS_SET_COMPLETION_TARGET:
					availableProgressSetCompletionTarget();
					break;
					
				case AVAILABLE_PROGRESS_UPDATE:
					availableProgressUpdate();
					break;
					
				case AVAILABLE_PROGRESS_INDETERMINATE:
					availableProgressIndeterminate(true);
					break;
					
				case SWITCH_INSTALLED_TO_PROGRESSBAR:
		    		switchInstalledToProgressBar();	
					break;
					
				case SWITCH_INSTALLED_TO_NO_APPS:
		    		switchInstalledToEmpty();	
					break;
					
				case SWITCH_INSTALLED_TO_LIST:
		    		switchInstalledToList();	
					break;
					
				case SWITCH_AVAILABLE_TO_NO_APPS:
					switchAvailableToEmpty();
					break;
					
				case SWITCH_AVAILABLE_TO_LIST:
					switchAvailableToList();
					break;
					
				case SWITCH_AVAILABLE_TO_STATIC_LIST:
					switchAvailableToStaticList();
					break;
					
				case SWITCH_AVAILABLE_TO_DYNAMIC_LIST:
					switchAvailableToDynamicList();
					break;
					
				case SWITCH_AVAILABLE_TO_WAITING_ON_SERVER:
					switchAvailableToWaitingOnServer();
					break;
					
				case SWITCH_AVAILABLE_TO_CATEGORIES:
					switchAvailableToCategory();
					break;
					
				case SWITCH_UPDATABLE_TO_PROGRESSBAR:
		    		switchUpdatableToProgressBar();	
					break;
					
				case SWITCH_UPDATABLE_TO_NO_APPS:
		    		switchUpdatableToEmpty();	
					break;
					
				case SWITCH_UPDATABLE_TO_LIST:
		    		switchUpdatableToList();	
					break;
					
//				case HANDLE_MYAPP:
//					handleMyapp();
//					break;
	
				default:
					break;
			}
        }
    };
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!isRunning){

			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			
        	theme = new ContextThemeWrapper(this, R.style.DialogTheme);
        	
//			currentAppsList = EnumAppsLists.Backup;
	
//			handlingMyapps = new ArrayList<ViewMyapp>();
			
			queuedFlipperChanges = new ArrayList<EnumAptoideInterfaceTasks>();
			
//			swypeDetector = new GestureDetector(new SwypeDetector());
//			swypeListener = new View.OnTouchListener() {
//									@Override
//									public boolean onTouch(View v, MotionEvent event) {
//										return swypeDetector.onTouchEvent(event);
//									}
//								};
//			swyping = new AtomicBoolean(false);
//			swypeDelayHandler = new Handler();
			
			synchronizingInstalledApps = new AtomicBoolean(false);
			loadingRepos = new AtomicBoolean(false);
//			resettingFlipper = new AtomicBoolean(false);
			highPriority = new AtomicBoolean(true);
			
			makeSureServiceDataIsRunning();
			
			isRunning = true;

			
	        setContentView(R.layout.aptoide);
			
	        
//			searchView = (ImageView) findViewById(R.id.search_button);
//			searchView.setOnTouchListener(new SearchClickListener());

			
//			previousViewArrow = (ImageView) findViewById(R.id.previous);
//			previousViewArrow.setOnClickListener(new OnPreviousClickedListener());
//			previousViewArrow.setVisibility(View.INVISIBLE);
			
//			previousListTitle = (TextView) findViewById(R.id.previous_title);
//			previousListTitle.setText(getString(R.string.available));
//			previousListTitle.setOnClickListener(new OnPreviousClickedListener());
//			previousListTitle.setVisibility(View.INVISIBLE);
//			currentListTitle = (TextView) findViewById(R.id.current_title);
//			currentListTitle.setText(getString(R.string.available));
//			currentListTitle.setClickable(false);
//			nextListTitle = (TextView) findViewById(R.id.next_title);
//			nextListTitle.setText(getString(R.string.installed));
//			nextListTitle.setOnClickListener(new OnNextClickedListener());

//			nextViewArrow = (ImageView) findViewById(R.id.next);
//			nextViewArrow.setOnClickListener(new OnNextClickedListener());
			
			
//			View categoriesView = LinearLayout.inflate(this, R.layout.apps_list, appsListFlipper);
			View availableView = LinearLayout.inflate(this, R.layout.list_apps, appsListPager);
			View installedView = LinearLayout.inflate(this, R.layout.list_apps, appsListPager);
//			View updatableView = LinearLayout.inflate(this, R.layout.list_apps, appsListFlipper);

			
//			emptyCategoriesList = categoriesView.findViewById(android.R.id.empty);
//			emptyCategoriesList.setVisibility(View.GONE);
//			emptyAvailableAppsList = LinearLayout.inflate(this, R.layout.list_apps_empty, appsListFlipper);
			emptyAvailableAppsList = availableView.findViewById(android.R.id.empty);
			emptyAvailableAppsList.setVisibility(View.GONE);
			emptyAvailableAppsList.setTag(EnumAppsLists.RESTORE);
//			emptyInstalledAppsList = LinearLayout.inflate(this, R.layout.list_apps_empty, appsListFlipper);
			emptyInstalledAppsList = installedView.findViewById(android.R.id.empty);
			emptyInstalledAppsList.setVisibility(View.GONE);
			emptyInstalledAppsList.setTag(EnumAppsLists.BACKUP);
//			emptyUpdatableAppsList = LinearLayout.inflate(this, R.layout.list_apps_empty, appsListFlipper);
//			emptyUpdatableAppsList = updatableView.findViewById(android.R.id.empty);
//			emptyUpdatableAppsList.setVisibility(View.GONE);
//			emptyUpdatableAppsList.setTag(EnumAppsLists.Updates);
			
			
//			loadingCategoriesList = categoriesView.findViewById(R.id.loading);
//			loadingAvailableAppsList = LinearLayout.inflate(this, R.layout.list_loading, appsListFlipper);
			loadingAvailableAppsList = availableView.findViewById(R.id.loading);
			loadingAvailableAppsList.setTag(EnumAppsLists.RESTORE);
			loadingAppsTitle = loadingAvailableAppsList.findViewById(R.id.loading_title);
			loadingAppsTitleWaitingOnServer = loadingAvailableAppsList.findViewById(R.id.loading_title_waiting_on_server);
			loadingAvailableAppsUnknownProgress = (ProgressBar) loadingAvailableAppsList.findViewById(R.id.loading_bar);
			loadingAvailableAppsProgress = (ProgressBar) loadingAvailableAppsList.findViewById(R.id.loading_progress_bar);
			loadingAvailableAppsProgressCompletionTarget = new AtomicInteger(0);
			loadingAvailableAppsProgressCurrent = new AtomicInteger(0);
//			loadingInstalledAppsList = LinearLayout.inflate(this, R.layout.list_loading, appsListFlipper);
			loadingInstalledAppsList = installedView.findViewById(R.id.loading);
			loadingInstalledAppsList.setTag(EnumAppsLists.BACKUP);
//			loadingUpdatableAppsList = LinearLayout.inflate(this, R.layout.list_loading, appsListFlipper);
//			loadingUpdatableAppsList = updatableView.findViewById(R.id.loading);
//			loadingUpdatableAppsList.setTag(EnumAppsLists.Updates);
			
//			categoriesListView = new ListView(this);
//			categoriesListView = (ListView) categoriesView.findViewById(android.R.id.list);
//			categoriesListView.setCacheColorHint(Color.TRANSPARENT);
//			categoriesListView.setOnTouchListener(swypeListener);
//			categoriesListView.setOnItemClickListener(this);
//			categoriesListView.setTag(EnumAppsLists.Available);
//			categoriesListView.setPersistentDrawingCache(ViewGroup.PERSISTENT_ALL_CACHES);
			
//			availableAppsListView = new ListView(this);
			availableAppsListView = (ListView) availableView.findViewById(android.R.id.list);
			availableAppsListView.setCacheColorHint(Color.TRANSPARENT);
//			availableAppsListView.setOnTouchListener(swypeListener);
			availableAppsListView.setOnItemClickListener(this);
			availableAppsListView.setTag(EnumAppsLists.RESTORE);
			availableAppsListView.setPersistentDrawingCache(ViewGroup.PERSISTENT_ALL_CACHES);
	//		appsListFlipper.addView(availableAppsList);
			
//			installedAppsListView = new ListView(this);
			installedAppsListView = (ListView) installedView.findViewById(android.R.id.list);
			installedAppsListView.setCacheColorHint(Color.TRANSPARENT);
//			installedAppsListView.setOnTouchListener(swypeListener);
			installedAppsListView.setOnItemClickListener(this);
			installedAppsListView.setTag(EnumAppsLists.BACKUP);
			installedAppsListView.setPersistentDrawingCache(ViewGroup.PERSISTENT_ALL_CACHES);
	//		appsListFlipper.addView(installedAppsList);
			
//			updatableAppsListView = new ListView(this);
//			updatableAppsListView = (ListView) updatableView.findViewById(android.R.id.list);
//			updatableAppsListView.setCacheColorHint(Color.TRANSPARENT);
//			updatableAppsListView.setOnTouchListener(swypeListener);
//			updatableAppsListView.setOnItemClickListener(this);
//			updatableAppsListView.setTag(EnumAppsLists.Updates);
//			updatableAppsListView.setPersistentDrawingCache(ViewGroup.PERSISTENT_ALL_CACHES);
	//		appsListFlipper.addView(updatableAppsList);
			
			Button backup = (Button) installedView.findViewById(R.id.action);
			backup.setText(R.string.backup_selected_apps);
			backup.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d("Aptoide-AppsBackup", "Clicked on backup");

//					if(selectedVersion.getRepoUri() != null){
//						if(authenticationToken == null){
//							try {
//								authenticationToken = serviceDataCaller.callGetServerToken();
//							} catch (RemoteException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//						if(authenticationToken == null){
//							Log.d("Aptoide-AppsBackup", "No login set");
//							DialogLogin loginComments = new DialogLogin(AppInfo.this, serviceDataCaller, DialogLogin.InvoqueNature.NO_CREDENTIALS_SET);
//							loginComments.setOnDismissListener(new OnDismissListener() {
//								@Override
//								public void onDismiss(DialogInterface dialog) {
//									addAppVersionComment();
//								}
//							});
//							loginComments.show();
//						}else{
//							addAppVersionComment();						
//						}
//					}
					try {
						authenticationToken = serviceDataCaller.callGetServerToken();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					ViewListIds uploads = new ViewListIds();
					
					for (int i = 0; i < installedAdapter.getCount(); i++) {
						if(((ViewDisplayApplicationBackup) installedAdapter.getItem(i)).isChecked()){
							uploads.add(installedAdapter.getItem(i).getAppHashid());
						}
					}
					
					if(uploads.size() > 0){
						installedAdapter.unselectAll();
						if(authenticationToken != null){
							Intent upload = new Intent(Aptoide.this, Upload.class);
							upload.putIntegerArrayListExtra("uploads", uploads);
							startActivity(upload);
						}else{
							AptoideLog.d(Aptoide.this, "can't backup with no server login configured" );
	//						Toast.makeText(Aptoide.this, R.string.login_required, Toast.LENGTH_SHORT).show();
							login(uploads, EnumAppsLists.BACKUP);
						}
					}
					
				}
			});
			
			Button restore = (Button) availableView.findViewById(R.id.action);
			restore.setText(R.string.restore_selected_apps);
			restore.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.d("Aptoide-AppsBackup", "Clicked on restore");
					try {
						anyReposInUse = serviceDataCaller.callAnyReposInUse();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					ViewListIds restores = new ViewListIds();
					ViewDisplayApplicationBackup app;
					for (int i = 0; i < availableAdapter.getCount(); i++) {
						app = ((ViewDisplayApplicationBackup) availableAdapter.getItem(i));
						if(app.isChecked()){
							restores.add(availableAdapter.getItem(i).getAppHashid());
							Toast.makeText(Aptoide.this, getString(R.string.restoring_app, availableAdapter.getItem(i).getAppName()), Toast.LENGTH_SHORT).show();
						}
					}
					
					availableAdapter.unselectAll();
					
					if(anyReposInUse){
						for (Integer appHashid : restores) {
							try {
								serviceDataCaller.callInstallApp(appHashid);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
					}else{
						AptoideLog.d(Aptoide.this, "can't restore with no repo configured" );
//						Toast.makeText(Aptoide.this, R.string.login_required, Toast.LENGTH_SHORT).show();
						login(restores, EnumAppsLists.RESTORE);
					}
				}
			});
			
			
			ArrayList<View> pages = new ArrayList<View>();
			pages.add(installedView);
			pages.add(availableView);
			
			appsListPager = (ViewPager) findViewById(R.id.list_pager);
			ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(pages);
			appsListPager.setAdapter(pagerAdapter);

			pageIndicator = (FixedTabsView) findViewById(R.id.indicator);
			pageIndicatorAdapter = new FixedTabsAdapter(this);
			pageIndicator.setAdapter(pageIndicatorAdapter);
			pageIndicator.setViewPager(appsListPager);
			
//			appsListFlipper.addView(loadingAvailableAppsList);
//			appsListFlipper.addView(loadingInstalledAppsList);
//			appsListFlipper.addView(loadingUpdatableAppsList);
//			appsListPager.addView(availableView);
//			appsListPager.addView(installedView);
//			appsListFlipper.addView(updatableView);
        }
    }

	private void makeSureServiceDataIsRunning(){
    	ActivityManager activityManager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
    	for (RunningServiceInfo runningService : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if(runningService.service.getClassName().equals(Constants.SERVICE_DATA_CLASS_NAME)){
				this.serviceDataSeenRunning = true;
				break;
			}
		}

    	if(!serviceDataIsBound){
    		startService(new Intent(this, AptoideServiceData.class));	//TODO uncomment this to make service independent of Aptoide's lifecycle
    		bindService(new Intent(this, AptoideServiceData.class), serviceDataConnection, Context.BIND_AUTO_CREATE);
    	}else{
    		handleIncomingIntent(getIntent());
    	}
    }
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		handleIncomingIntent(intent);
		super.onNewIntent(intent);
	}
	
	private void cleanAptoideIntent(){
		Intent aptoide = new Intent(this, Aptoide.class);
		aptoide.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(aptoide);		
	}

	
	private void handleIncomingIntent(Intent incomingIntent){
        if(incomingIntent.getData() != null){
    		AptoideLog.d(this, "received intent: "+incomingIntent.getDataString()); 
        	if(incomingIntent.getType() != null && incomingIntent.getType().equals(Constants.MIMETYPE_MYAPP)){
        		AptoideLog.d(this, "received myapp: "+incomingIntent.getDataString());        		
		        try {
					serviceDataCaller.callReceiveMyapp(incomingIntent.getDataString());
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else if(incomingIntent.getScheme().equals(Constants.SCHEME_MARKET) 
        			|| (incomingIntent.getScheme().equals(Constants.SCHEME_HTTPS) && incomingIntent.getData().getHost().equals(Constants.HOST_MARKET))){
        		String query = incomingIntent.getData().getQuery().split("&")[0].split("=")[1];
        		if(query.contains(":")){
        			query = query.split(":")[1];
        		}
        		AptoideLog.d(this, "received market query: "+query);
        		if(!synchronizingInstalledApps.get()){
        			startSearch(query, false, null, false);
        		}else{
        			blockedSearchQuery = query;
        		}
        	}
        	
        	cleanAptoideIntent();
        }
	}
	
	private void login(){
		login(null, null);
	}
	
	private void login(ViewListIds uploads, EnumAppsLists action){
		Intent login = new Intent(Aptoide.this, BazaarLogin.class);
		login.putExtra("InvoqueType", BazaarLogin.InvoqueType.NO_CREDENTIALS_SET.ordinal());
		if(uploads != null){
			if(action.equals(EnumAppsLists.BACKUP)){
				login.putIntegerArrayListExtra("uploads", uploads);
			}else{
				login.putIntegerArrayListExtra("restores", uploads);				
			}
		}
		startActivity(login);
	}
	
	public void setAvailableListBy(boolean byCategory){
		AptoideLog.d(Aptoide.this, "setAvailableList ByCategory? "+byCategory);
		try {
			serviceDataCaller.callSetListsBy(byCategory);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setAppsSortingPolicy(EnumAppsSorting sortingPolicy){
		AptoideLog.d(Aptoide.this, "setAppsSortingPolicy to: "+sortingPolicy);
		try {
			serviceDataCaller.callSetAppsSortingPolicy(sortingPolicy.ordinal());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setShowSystemApps(Boolean state){
		AptoideLog.d(Aptoide.this, "setShowSystemAppsn to: "+state);
		try {
			serviceDataCaller.callSetShowSystemApps(state);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initListsAdapters(){
		AptoideLog.d(Aptoide.this, "init lists adapters");
//		categoriesAdapter = new StaticCategoriesListAdapter(this, categoriesListView, serviceDataCaller, interfaceTasksHandler);
//		categoriesAdapter = new StaticCategoriesListAdapter(this, availableAppsListView, serviceDataCaller, interfaceTasksHandler);
//		availableAdapter = new DynamicAvailableAppsListAdapter(this, availableAppsListView, serviceDataCaller, interfaceTasksHandler);
		availableAdapter = new StaticAvailableAppsListAdapter(this, availableAppsListView, serviceDataCaller, interfaceTasksHandler);
		installedAdapter = new StaticInstalledAppsListAdapter(this, installedAppsListView, serviceDataCaller, interfaceTasksHandler);
//		updatableAdapter = new StaticUpdatableAppsListAdapter(this, updatableAppsListView, serviceDataCaller, interfaceTasksHandler);
	}
	
	public void resetDisplayAvailable(){
//		if(availableByCategory){
//			categoriesAdapter.resetDisplayCategories();
//		}else{
//			availableAdapter.resetDisplay(category);
//			availableAdapter.resetDisplay(null);
			availableAdapter.resetDisplayAvailable();
//		}
	}
	
	public void reloadDisplayAvailable(){
//		if(!availableByCategory || categoriesAdapter.getCategory() != null && !categoriesAdapter.getCategory().hasChildren()){
//			availableAdapter.reloadDisplay();
			availableAdapter.resetDisplayAvailable();
//		}
	}
	
	
	
//	private synchronized void switchFlipperElements(EnumAptoideInterfaceTasks switchAction){
//		if(!swyping.get()){
//			resettingFlipper.set(true);
//			View available = appsListFlipper.findViewWithTag(EnumAppsLists.Available);
//			View installed = appsListFlipper.findViewWithTag(EnumAppsLists.Installed);
//			View updates = appsListFlipper.findViewWithTag(EnumAppsLists.Updates);
//			
//			switch (switchAction) {
//				case SWITCH_AVAILABLE_TO_PROGRESSBAR:
//					available = loadingAvailableAppsList;
//					updates = loadingUpdatableAppsList;				
//					break;
//					
//				case SWITCH_AVAILABLE_TO_CATEGORIES:
//					available = categoriesListView;
//					break;
//					
//				case SWITCH_AVAILABLE_TO_LIST:
//					available = availableAppsListView;
//					break;
//					
//				case SWITCH_AVAILABLE_TO_NO_APPS:
//					available = emptyAvailableAppsList;
//					updates = emptyUpdatableAppsList;
//					break;
//					
//				case SWITCH_INSTALLED_TO_PROGRESSBAR:
//					installed = loadingInstalledAppsList;
//					updates = loadingUpdatableAppsList;
//					break;
//					
//				case SWITCH_INSTALLED_TO_LIST:
//					installed = installedAppsListView;
//					break;
//					
//				case SWITCH_INSTALLED_TO_NO_APPS:
//					installed = emptyInstalledAppsList;
//					updates = emptyUpdatableAppsList;
//					break;
//					
//				case SWITCH_UPDATABLE_TO_PROGRESSBAR:
//					updates = loadingUpdatableAppsList;
//					break;
//					
//				case SWITCH_UPDATABLE_TO_LIST:
//					updates = updatableAppsListView;
//					break;
//					
//				case SWITCH_UPDATABLE_TO_NO_APPS:
//					updates = emptyUpdatableAppsList;
//					break;
//					
//				default:
//					break;
//			}
//	
//			appsListFlipper.invalidate();
//			appsListFlipper.removeAllViews();
//			
//			appsListFlipper.addView(available, EnumAppsLists.Available.ordinal());
//			appsListFlipper.addView(installed, EnumAppsLists.Installed.ordinal());
//			appsListFlipper.addView(updates, EnumAppsLists.Updates.ordinal());
//	
//			appsListFlipper.clearAnimation();
//			
//			switch (currentAppsList) {
//				case Available:
//					break;
//			
//				case Installed:
//					appsListFlipper.showNext();
//					break;
//				
//				case Updates:
//					appsListFlipper.showNext();
//					appsListFlipper.showNext();
//					break;
//		
//				default:
//					break;
//			}
//			
//			if(!queuedFlipperChanges.isEmpty()){
//				switchFlipperElements(queuedFlipperChanges.remove(0));
//			}
//			resettingFlipper.set(false);
//		}else{
//			queuedFlipperChanges.add(switchAction);
//		}
//	}
	
	private void switchAvailableToProgressBar(){
		AptoideLog.d(Aptoide.this, "switching available to progressBar");

		availableProgressIndeterminate(true);
		
//        appsListFlipper.invalidate();
//        appsListFlipper.removeViewAt(EnumAppsLists.Available.ordinal());
//        appsListFlipper.addView(loadingAvailableAppsList, EnumAppsLists.Available.ordinal());
////		switchFlipperElements(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_PROGRESSBAR);
		
		emptyAvailableAppsList.setVisibility(View.GONE);
		availableAppsListView.setVisibility(View.GONE);
		loadingAvailableAppsList.setVisibility(View.VISIBLE);
        loadingAppsTitle.setVisibility(View.VISIBLE);
        loadingAppsTitleWaitingOnServer.setVisibility(View.GONE);
        
//        switchUpdatableToProgressBar();
	}
	
	private void availableProgressSetCompletionTarget(){
		availableProgressIndeterminate(false);
		loadingAvailableAppsProgress.setIndeterminate(false);
		loadingAvailableAppsProgress.setMax(loadingAvailableAppsProgressCompletionTarget.get());
	}
	
	private void availableProgressUpdate(){
		loadingAvailableAppsProgress.setProgress(loadingAvailableAppsProgressCurrent.get());
	}
	
	private void availableProgressIndeterminate(boolean indeterminate){
		if(indeterminate){
			loadingAvailableAppsUnknownProgress.setVisibility(View.VISIBLE);
			loadingAvailableAppsProgress.setVisibility(View.GONE);
		}else{
			loadingAvailableAppsUnknownProgress.setVisibility(View.GONE);
			loadingAvailableAppsProgress.setVisibility(View.VISIBLE);			
		}
	}

	private void switchAvailableToList(){
		AptoideLog.d(Aptoide.this, "switching available to list");
//        appsListFlipper.invalidate();
//        appsListFlipper.removeViewAt(EnumAppsLists.Available.ordinal());
//        appsListFlipper.addView(availableAppsListView, EnumAppsLists.Available.ordinal());
////		switchFlipperElements(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_LIST);
		
		emptyAvailableAppsList.setVisibility(View.GONE);
		availableAppsListView.setVisibility(View.VISIBLE);
		loadingAvailableAppsList.setVisibility(View.GONE);
	}
	
	private void switchAvailableToStaticList(){
		AptoideLog.d(Aptoide.this, "switching available to static");
		availableAdapter = new StaticAvailableAppsListAdapter(this, availableAppsListView, serviceDataCaller, interfaceTasksHandler);
	}
	
	private void switchAvailableToDynamicList(){
		AptoideLog.d(Aptoide.this, "switching available to dynamic");
		availableAdapter.shutdownNow();
		availableAdapter = new DynamicAvailableAppsListAdapter(this, availableAppsListView, serviceDataCaller, interfaceTasksHandler);		
	}
	
	private void switchAvailableToWaitingOnServer(){
		AptoideLog.d(Aptoide.this, "switching available to waiting on server");
		switchAvailableToProgressBar();
        loadingAppsTitle.setVisibility(View.GONE);
        loadingAppsTitleWaitingOnServer.setVisibility(View.VISIBLE);
		
	}
	
	private void switchAvailableToCategory(){
		AptoideLog.d(Aptoide.this, "switching available to category");
//		appsListFlipper.invalidate();
//        appsListFlipper.removeViewAt(EnumAppsLists.Available.ordinal());
//        appsListFlipper.addView(categoriesListView, EnumAppsLists.Available.ordinal());
////		switchFlipperElements(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_CATEGORIES);
		
//		availableAdapter.setLoadingFooter(false);
//		availableAdapter.setLoadingHeader(false);
		
		emptyAvailableAppsList.setVisibility(View.GONE);
		availableAppsListView.setVisibility(View.VISIBLE);
		loadingAvailableAppsList.setVisibility(View.GONE);
	}
	
	private void switchAvailableToEmpty(){
		AptoideLog.d(Aptoide.this, "switching available to empty");
//		appsListFlipper.invalidate();
//        appsListFlipper.removeViewAt(EnumAppsLists.Available.ordinal());
//        appsListFlipper.addView(emptyAvailableAppsList, EnumAppsLists.Available.ordinal());
////		switchFlipperElements(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_NO_APPS);
		
		emptyAvailableAppsList.setVisibility(View.VISIBLE);
		availableAppsListView.setVisibility(View.INVISIBLE);
		loadingAvailableAppsList.setVisibility(View.GONE);
        
//        switchUpdatableToEmpty();
	}

	private void switchInstalledToProgressBar(){
		AptoideLog.d(Aptoide.this, "switching installed to progressBar");
//        appsListFlipper.invalidate();
//        appsListFlipper.removeViewAt(EnumAppsLists.Installed.ordinal());
//        appsListFlipper.addView(loadingInstalledAppsList, EnumAppsLists.Installed.ordinal());
////		switchFlipperElements(EnumAptoideInterfaceTasks.SWITCH_INSTALLED_TO_PROGRESSBAR);
		
		emptyInstalledAppsList.setVisibility(View.GONE);
		installedAppsListView.setVisibility(View.INVISIBLE);
		loadingInstalledAppsList.setVisibility(View.VISIBLE);
        
//        switchUpdatableToProgressBar();
		switchAvailableToProgressBar();
	}

	private void switchInstalledToList(){
		AptoideLog.d(Aptoide.this, "switching installed to list");
//        appsListFlipper.invalidate();
//        appsListFlipper.removeViewAt(EnumAppsLists.Installed.ordinal());
//        appsListFlipper.addView(installedAppsListView, EnumAppsLists.Installed.ordinal());
////		switchFlipperElements(EnumAptoideInterfaceTasks.SWITCH_INSTALLED_TO_LIST);
		
		emptyInstalledAppsList.setVisibility(View.GONE);
		installedAppsListView.setVisibility(View.VISIBLE);
		loadingInstalledAppsList.setVisibility(View.GONE);
	}
	
	private void switchInstalledToEmpty(){
		AptoideLog.d(Aptoide.this, "switching installed to empty");
//		appsListFlipper.invalidate();
//		appsListFlipper.removeViewAt(EnumAppsLists.Installed.ordinal());
//        appsListFlipper.addView(emptyInstalledAppsList, EnumAppsLists.Installed.ordinal());
////		switchFlipperElements(EnumAptoideInterfaceTasks.SWITCH_INSTALLED_TO_NO_APPS);
		
		emptyInstalledAppsList.setVisibility(View.VISIBLE);
		installedAppsListView.setVisibility(View.INVISIBLE);
		loadingInstalledAppsList.setVisibility(View.GONE);
	}

	private void switchUpdatableToProgressBar(){
		AptoideLog.d(Aptoide.this, "switching updatable to progressBar");
//        appsListFlipper.invalidate();
//        appsListFlipper.removeViewAt(EnumAppsLists.Updates.ordinal());
//        appsListFlipper.addView(loadingUpdatableAppsList, EnumAppsLists.Updates.ordinal());
////		switchFlipperElements(EnumAptoideInterfaceTasks.SWITCH_UPDATABLE_TO_PROGRESSBAR);
		
//		emptyUpdatableAppsList.setVisibility(View.GONE);
//		updatableAppsListView.setVisibility(View.INVISIBLE);
//		loadingUpdatableAppsList.setVisibility(View.VISIBLE);
	}

	private void switchUpdatableToList(){
		AptoideLog.d(Aptoide.this, "switching updatable to list");
//        appsListFlipper.invalidate();
//        appsListFlipper.removeViewAt(EnumAppsLists.Updates.ordinal());
//        appsListFlipper.addView(updatableAppsListView, EnumAppsLists.Updates.ordinal());
////		switchFlipperElements(EnumAptoideInterfaceTasks.SWITCH_UPDATABLE_TO_LIST);
		
//		emptyUpdatableAppsList.setVisibility(View.GONE);
//		updatableAppsListView.setVisibility(View.VISIBLE);
//		loadingUpdatableAppsList.setVisibility(View.GONE);
	}
	
	private void switchUpdatableToEmpty(){
		AptoideLog.d(Aptoide.this, "switching updatable to empty");
//		appsListFlipper.invalidate();
//		appsListFlipper.removeViewAt(EnumAppsLists.Updates.ordinal());
//        appsListFlipper.addView(emptyUpdatableAppsList, EnumAppsLists.Updates.ordinal());
////		switchFlipperElements(EnumAptoideInterfaceTasks.SWITCH_UPDATABLE_TO_NO_APPS);
		
//		emptyUpdatableAppsList.setVisibility(View.VISIBLE);
//		updatableAppsListView.setVisibility(View.INVISIBLE);
//		loadingUpdatableAppsList.setVisibility(View.GONE);
	}
	
	
	
//	private void showAvailableList(){
////		if(!resettingFlipper.get()){
//		
//			switch (currentAppsList) {
//				case Available:
////					appsListFlipper.clearAnimation();
////					appsListFlipper.showNext();
////					appsListFlipper.showPrevious();
//					break;
//			
//				case Installed:
//					showPreviousList();
//					break;
//				
////				case Updates:
////					showPreviousList();
////					showPreviousList();
////					break;
//		
//				default:
//					break;
//			}
////		}
//	}
//	
//	private void showInstalledList(){
////		if(!resettingFlipper.get()){
//		
//			switch (currentAppsList) {
//				case Available:
//					showNextList();
//					break;
//				
////				case Updates:
////					showPreviousList();
////					break;
//		
//				case Installed:
////					appsListFlipper.clearAnimation();
////					appsListFlipper.showNext();
////					appsListFlipper.showPrevious();
//					break;
//					
//				default:
//					break;
//			}
////		}
//	}
//	
//	private void showUpdatableList(){
////		if(!resettingFlipper.get()){
//		
//			switch (currentAppsList) {
//				case Available:
//					showNextList();
//					showNextList();
//					break;
//				
//				case Installed:
//					showNextList();
//					break;
//				
////				case Updates:
////					appsListFlipper.clearAnimation();
////					appsListFlipper.showPrevious();
////					appsListFlipper.showNext();
////					break;
//		
//				default:
//					break;
//			}
////		}
//	}
	
    
//	private void showNextList(){
//		if(!EnumAppsLists.getNext(currentAppsList).equals(currentAppsList)){
//			appsListPager.setOutAnimation(AnimationUtils.loadAnimation(Aptoide.this, R.anim.flip_out_next));
//			appsListPager.setInAnimation(AnimationUtils.loadAnimation(Aptoide.this, R.anim.flip_in_next));
//			appsListPager.showNext();
//			previousListTitle.setTextColor(Color.GRAY);
//			nextListTitle.setTextColor(Color.WHITE);
//			currentAppsList = EnumAppsLists.getNext(currentAppsList);
////			putElementsIntoTitleBar(currentAppsList);
//		}
//	}
//	
//	private void showPreviousList(){
//		if(!EnumAppsLists.getPrevious(currentAppsList).equals(currentAppsList)){
//			appsListPager.setOutAnimation(AnimationUtils.loadAnimation(Aptoide.this, R.anim.flip_out_previous));
//			appsListPager.setInAnimation(AnimationUtils.loadAnimation(Aptoide.this, R.anim.flip_in_previous));
//			appsListPager.showPrevious();
//			previousListTitle.setTextColor(Color.WHITE);
//			nextListTitle.setTextColor(Color.GRAY);
//			currentAppsList = EnumAppsLists.getPrevious(currentAppsList);
////			putElementsIntoTitleBar(currentAppsList);
//		}
//	}
//    
//	public void putElementsIntoTitleBar(EnumAppsLists currentAppList) {
////		switch (currentAppList) {
////		case Available:
////			nextViewArrow.setVisibility(View.VISIBLE);
//			nextListTitle.setText(getString(R.string.installed));
////			previousViewArrow.setVisibility(View.INVISIBLE);
////			previousListTitle.setVisibility(View.INVISIBLE);
////			currentListTitle.setText(getString(R.string.available));
////			break;
////		case Installed:
////			nextViewArrow.setVisibility(View.VISIBLE);
////			previousViewArrow.setVisibility(View.VISIBLE);
////			currentListTitle.setText(getString(R.string.installed));
////			nextListTitle.setVisibility(View.VISIBLE);
////			nextListTitle.setText(getString(R.string.updates));
////			previousListTitle.setVisibility(View.VISIBLE);
//			previousListTitle.setText(getString(R.string.available));
////			break;
////		case Updates:
////			nextViewArrow.setVisibility(View.INVISIBLE);
////			previousViewArrow.setVisibility(View.VISIBLE);
////			currentListTitle.setText(getString(R.string.updates));
////			nextListTitle.setVisibility(View.INVISIBLE);
////			previousListTitle.setText(getString(R.string.installed));
////			break;
////		}
//
//	}
//
//	class OnPreviousClickedListener implements View.OnClickListener {
//		public void onClick(View v) {
//			Log.d("previous title", "previous title click");
//			showPreviousList();
//		}
//	}
//
//	class OnNextClickedListener implements View.OnClickListener {
//		public void onClick(View v) {
//			Log.d("next title", "next title click");
//			showNextList();
//		}
//	}
//
//    class SwypeDetector extends SimpleOnGestureListener {
//
//    	private static final int SWIPE_MIN_DISTANCE = 80;
//    	private static final int SWIPE_MAX_OFF_PATH = 250;
//    	private static final int SWIPE_THRESHOLD_VELOCITY = 150;
//    	
//    	private ExecutorService scrollBlocker = Executors.newSingleThreadExecutor();
//
//		@Override
//    	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
////    		Toast.makeText( Aptoide.this, availableAdapter.getItem( availableAppsList.pointToPosition(Math.round(e1.getX()), Math.round(e1.getY()) )).toString(), Toast.LENGTH_LONG );
//    		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH){
//    			return false;
//    		}else{
//    			swyping.set(true);
//	    		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//	        		Log.d("Aptoide","Swype right");
//	        		if(EnumAppsLists.getNext(currentAppsList).equals(currentAppsList)){
//	        			appsListPager.startAnimation(AnimationUtils.loadAnimation(Aptoide.this, R.anim.flip_resist_next));
//	        		}else{
//		        		showNextList();
//	        		}
//	    		} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//	        		Log.d("Aptoide","Swype left");
//	        		if(EnumAppsLists.getPrevious(currentAppsList).equals(currentAppsList)){
//	        			appsListPager.startAnimation(AnimationUtils.loadAnimation(Aptoide.this, R.anim.flip_resist_previous));
//	        		}else{
//		        		showPreviousList();
//	        		}
//	    		}
//	    		scrollBlocker.execute(new Swyping());
//				
//	    		return super.onFling(e1, e2, velocityX, velocityY);
//    		}
//    	}
//		
//		class Swyping implements Runnable{
//			@Override
//			public void run() {
//				swypeDelayHandler.postDelayed(new Runnable() {
//	                public void run() {
//	                	swyping.set(false);
//
////	        			if(!queuedFlipperChanges.isEmpty()){
////	        				switchFlipperElements(queuedFlipperChanges.remove(0));
////	        			}
//	                }
//	            }, 500);
//			}
//		}
//		
//    }
    
//    @Override
//    public boolean onSearchRequested() {
//		availableAdapter.sleep();
//    	return super.onSearchRequested();
//    }
    

//	class SearchClickListener implements OnTouchListener {
//
//		public boolean onTouch(View v, MotionEvent event) {
//			if (event.getAction() == MotionEvent.ACTION_DOWN) {
//				// Button was pressed, change button background
//				searchView.setImageResource(R.drawable.searchover);
//				return true;
//			} else if (event.getAction() == MotionEvent.ACTION_UP) {
//				// Button was released, reset button background
//				searchView.setImageResource(R.drawable.search);
//				onSearchRequested();
//				return true;
//			}
//
//			return true;
//		}
//
//	};
//	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
////		setHighPriority(true);
//		return swypeDetector.onTouchEvent(event);
//	}
	
//	private void setHighPriority( boolean high){
//		if(high && !highPriority.get()){
//			highPriority.set(true);
//			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
//		}else if(!high && highPriority.get()){
//			highPriority.set(false);
//			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);			
//		}
//	}

	
//	private void viewApp(int position){
//		availableAdapter.sleep();
//		int appHashid = 0;
//		switch (currentAppsList) {
//			case Backup:
//	    		//remove header (loading) influence
//				position--;
//				
//				appHashid = availableAdapter.getItem(position).getAppHashid();
//				break;
//				
//			case Installed:
//				appHashid = installedAdapter.getItem(position).getAppHashid();
//				break;
//				
////			case Updates:
////				appHashid = updatableAdapter.getItem(position).getAppHashid();
////				break;
//	
//			default:
//				break;
//		}
//		AptoideLog.d(this, "Onclick position: "+position+" appHashid: "+appHashid);
//		Intent appInfo = new Intent(this,AppInfo.class);
//		appInfo.putExtra("appHashid", appHashid);
//		startActivity(appInfo);
////		setHighPriority(false);
//	}
	
    @Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long positionLong) {
    	
    	if(appsListPager.getCurrentItem() == EnumAppsLists.BACKUP.ordinal()){
    		ViewDisplayApplicationBackup installed = ((ViewDisplayApplicationBackup) installedAdapter.getItem(position));
	    	InstalledRowViewHolder viewHolder = (InstalledRowViewHolder) view.getTag();
	    	if(installed.toggleCheck()){
	    		viewHolder.setChecked(true);
	    		if(installed.getStatus().equals(EnumAppStatus.SYSTEM) || installed.getStatus().equals(EnumAppStatus.PROTECTED)){
	    			Toast.makeText(this, getString(R.string.app_system_backup_may_fail, installed.getAppName()), Toast.LENGTH_LONG).show();
	    		}else if(installed.getStatus().equals(EnumAppStatus.TOO_BIG)){
	    			Toast.makeText(this, getString(R.string.app_too_big_backup_may_fail, installed.getAppName()), Toast.LENGTH_LONG).show();
	    		}
	    	}else{
	    		viewHolder.setChecked(false);
	    	}
    	}else{
    		ViewDisplayApplicationBackup available = ((ViewDisplayApplicationBackup) availableAdapter.getItem(position));
    		if(view.getTag() instanceof DynamicAvailableAppsListAdapter.AvailableRowViewHolder){
	    		DynamicAvailableAppsListAdapter.AvailableRowViewHolder viewHolder = (DynamicAvailableAppsListAdapter.AvailableRowViewHolder) view.getTag();
	    		
	    		if(available.toggleCheck()){
		    		viewHolder.setChecked(true);
		    	}else{
		    		viewHolder.setChecked(false);
		    	}
    		}else{
    			AvailableRowViewHolder viewHolder = (AvailableRowViewHolder) view.getTag();
	    		
	    		if(available.toggleCheck()){
		    		viewHolder.setChecked(true);
		    	}else{
		    		viewHolder.setChecked(false);
		    	}
    		}
    	}
    	
//    	if(!swyping.get()){
//    		if(availableByCategory){
//    			if(categoriesAdapter.getCategory().hasChildren()){
//    				position--;
//    				final int categoryHashid = categoriesAdapter.getItem(position).getCategoryHashid();
//    				ViewDisplayCategory category = categoriesAdapter.getItem(position);
//    				AptoideLog.d(this, "Onclick position: "+position+" categoryHashid: "+categoryHashid+" category: "+category);
//    				categoriesAdapter.gotoSubCategory(categoryHashid);    				
//    				if(!category.hasChildren()){
//    					availableAdapter.resetDisplay(category);
//    				}
//    			}else{
//    				viewApp(position);
//    			}
//    		}else{
//    			viewApp(position);
//    		}
//    	}
	}

	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
////		if (keyCode == KeyEvent.KEYCODE_BACK && currentAppsList.equals(EnumAppsLists.Available) && availableByCategory && categoriesAdapter.getCategory() != null && categoriesAdapter.getCategory().getCategoryHashid() != Constants.TOP_CATEGORY*/) {
////			AptoideLog.d(this, "click back, new category: "+categoriesAdapter.getCategory().getParentCategory());
////			if(!categoriesAdapter.getCategory().hasChildren()){
////				availableAdapter.sleep();
////				interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_CATEGORIES.ordinal());
////			}
////			categoriesAdapter.gotoParentCategory();
////			return true;
////		}
//		return super.onKeyDown(keyCode, event);
//	}
    

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		currentAppsList = EnumAppsLists.reverseOrdinal(appsListPager.getCurrentItem());
		switch (currentAppsList) {
			case RESTORE:
//				menu.add(Menu.NONE, EnumOptionsMenu.DELETE.ordinal(), EnumOptionsMenu.DELETE.ordinal(), R.string.delete)
//					.setIcon(R.drawable.ic_menu_close_clear_cancel);
				break;
				
			case BACKUP:
				menu.add(Menu.NONE, EnumOptionsMenu.UNINSTALL.ordinal(), EnumOptionsMenu.UNINSTALL.ordinal(), R.string.uninstall)
					.setIcon(android.R.drawable.ic_menu_delete);
				break;
				
//			case Updates:
//				menu.add(Menu.NONE, EnumOptionsMenu.UPDATE_ALL.ordinal(), EnumOptionsMenu.UPDATE_ALL.ordinal(), R.string.update_all)
//					.setIcon(R.drawable.ic_menu_refresh);
//				menu.add(Menu.NONE, EnumOptionsMenu.DISPLAY_OPTIONS.ordinal(), EnumOptionsMenu.DISPLAY_OPTIONS.ordinal(), R.string.display_options)
//				.setIcon(R.drawable.ic_menu_filter);
//				break;
				
			default:
				break;
		}

//		menu.add(Menu.NONE, EnumOptionsMenu.MANAGE_REPO.ordinal(), EnumOptionsMenu.MANAGE_REPO.ordinal(), R.string.manage_repos)
//			.setIcon(R.drawable.ic_menu_archive);
//		menu.add(Menu.NONE,EnumOptionsMenu.SCHEDULED_DOWNLOADS.ordinal(),EnumOptionsMenu.SCHEDULED_DOWNLOADS.ordinal(),R.string.scheduled_downloads)
//			.setIcon(R.drawable.ic_menu_clock);
//		menu.add(Menu.NONE, EnumOptionsMenu.SEARCH_MENU.ordinal(),EnumOptionsMenu.SEARCH_MENU.ordinal(),R.string.menu_search)
//			.setIcon(android.R.drawable.ic_menu_search);
		menu.add(Menu.NONE, EnumOptionsMenu.DISPLAY_OPTIONS.ordinal(), EnumOptionsMenu.DISPLAY_OPTIONS.ordinal(), R.string.display_options)
			.setIcon(android.R.drawable.ic_menu_sort_by_size);
		
		menu.add(Menu.NONE, EnumOptionsMenu.UN_SELECT_ALL.ordinal(), EnumOptionsMenu.UN_SELECT_ALL.ordinal(), R.string.un_select_all)
			.setIcon(android.R.drawable.ic_menu_add);

		menu.add(Menu.NONE, EnumOptionsMenu.ABOUT.ordinal(), EnumOptionsMenu.ABOUT.ordinal(),R.string.about)
			.setIcon(android.R.drawable.ic_menu_help);
		menu.add(Menu.NONE, EnumOptionsMenu.SETTINGS.ordinal(), EnumOptionsMenu.SETTINGS.ordinal(), R.string.settings)
			.setIcon(android.R.drawable.ic_menu_preferences);
		
		menu.add(Menu.NONE, EnumOptionsMenu.LOGIN.ordinal(), EnumOptionsMenu.LOGIN.ordinal(), R.string.login)
			.setIcon(android.R.drawable.ic_menu_edit);
		
		menu.add(Menu.NONE, EnumOptionsMenu.FOLLOW.ordinal(), EnumOptionsMenu.FOLLOW.ordinal(),R.string.social_networks)
			.setIcon(android.R.drawable.ic_menu_share);
		
		
		return super.onCreateOptionsMenu(menu);
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		EnumOptionsMenu menuEntry = EnumOptionsMenu.reverseOrdinal(item.getItemId());
		Log.d("Aptoide-OptionsMenu", "menuOption: "+menuEntry+" itemid: "+item.getItemId());
		switch (menuEntry) {
//			case MANAGE_REPO:
//				availableAdapter.sleep();
//				Intent manageRepo = new Intent(this, ManageRepos.class);
//				startActivity(manageRepo);
//				return true;
		
			case UNINSTALL:
				try {
					serviceDataCaller.callUninstallApps(installedAdapter.getSelectedIds());
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
				
			case DELETE:
				//TODO improve listIds as an extended parcelable array ;) then use it as a carrier for checked hashids list
				return true;
				
			case DISPLAY_OPTIONS:
				if(!loadingRepos.get()){
					//TODO refactor extract dialog management class
					LayoutInflater displayOptionsInflater = LayoutInflater.from(this);
					View displayOptions = displayOptionsInflater.inflate(R.layout.dialog_display_options, null);
					Builder dialogBuilder = new AlertDialog.Builder(theme).setView(displayOptions);
					final AlertDialog sortDialog = dialogBuilder.create();
					sortDialog.setIcon(android.R.drawable.ic_menu_sort_by_size);
					sortDialog.setTitle(getString(R.string.display_options));

					// ***********************************************************
					// Categories
//					final RadioButton byCategory = (RadioButton) displayOptions.findViewById(R.id.by_category);
//					final RadioButton byAll = (RadioButton) displayOptions.findViewById(R.id.by_all);
//					if(availableByCategory){
//						byCategory.setChecked(true);
//					}else{
//						byAll.setChecked(true);
//					}
//	
//					final View spacer = displayOptions.findViewById(R.id.spacer);
//					
//					if(currentAppsList != EnumAppsLists.Available){
//						spacer.setVisibility(View.GONE);
//						((RadioGroup) displayOptions.findViewById(R.id.group_show)).setVisibility(View.GONE);
//					}
					
					// ***********************************************************
					// Sorting				
					final View group_sort = displayOptions.findViewById(R.id.group_sort);
					final RadioButton byAlphabetic = (RadioButton) displayOptions.findViewById(R.id.by_alphabetic);
					final RadioButton byFreshness = (RadioButton) displayOptions.findViewById(R.id.by_freshness);
					final RadioButton bySize = (RadioButton) displayOptions.findViewById(R.id.by_size);

//					spacer.setVisibility(View.VISIBLE);
					group_sort.setVisibility(View.VISIBLE);
					switch (appsSortingPolicy) {
						case ALPHABETIC:
							byAlphabetic.setChecked(true);
							break;
							
						case FRESHNESS:
							byFreshness.setChecked(true);
							break;
							
						case SIZE:
							bySize.setChecked(true);
							break;
							
						default:
							break;
					}
					

					// ***********************************************************

					final CheckBox showSystemApps = (CheckBox) displayOptions.findViewById(R.id.show_system_apps);
					boolean showSystemAppsState = false;
					try {
						showSystemAppsState = serviceDataCaller.callGetShowSystemApps();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					showSystemApps.setChecked(showSystemAppsState);
					final boolean storedShowSystemAppsState = showSystemAppsState;
					
					sortDialog.setButton(getString(R.string.done), new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
//							boolean byCategoryChanged = false;
							EnumAppsSorting newSortingPolicy = null;
							
//							if(byCategory.isChecked() != availableByCategory){
//								byCategoryChanged = true;
//								availableByCategory = byCategory.isChecked();
//								setAvailableListBy(availableByCategory);
//							}
							
							if(showSystemApps.isChecked() != storedShowSystemAppsState){
								setShowSystemApps(showSystemApps.isChecked());
							}
							
							if(byAlphabetic.isChecked()){
								newSortingPolicy = EnumAppsSorting.ALPHABETIC;
							}else if(byFreshness.isChecked()){
								newSortingPolicy = EnumAppsSorting.FRESHNESS;
							}else if(bySize.isChecked()){
								newSortingPolicy = EnumAppsSorting.SIZE;
							}
							if(newSortingPolicy != null && newSortingPolicy != appsSortingPolicy){
//								availableAdapter.sleep();
								appsSortingPolicy = newSortingPolicy;
								setAppsSortingPolicy(appsSortingPolicy);
							}
							
//							if(byCategoryChanged){
//								if(availableByCategory){
//									availableAdapter.sleep();
//									categoriesAdapter.resetDisplayCategories();
//								}else{
//									availableAdapter.resetDisplay(null);								
//								}
//							}
							sortDialog.dismiss();
						}
					});
					
					sortDialog.show();
				}else{
					Toast.makeText(Aptoide.this, getString(R.string.option_not_available_while_updating_repos), Toast.LENGTH_SHORT).show();					
				}
				return true;
				
//			case SEARCH_MENU:
//				onSearchRequested();
//				return true;
				
			case UN_SELECT_ALL:				
				switch (currentAppsList) {
					case RESTORE:
						if(availableAdapter.isDynamic()){
							Toast.makeText(Aptoide.this, getString(R.string.too_many_apps_to_select_at_once), Toast.LENGTH_SHORT).show();
							return true;
						}
						availableAdapter.toggleSelectAll();						
						break;
						
					case BACKUP:
						installedAdapter.toggleSelectAll();						
						break;
	
					default:
						break;
				}				
				return true;
				
			case ABOUT:
				LayoutInflater aboutInflater = LayoutInflater.from(this);
				View about = aboutInflater.inflate(R.layout.about, null);
				TextView info = (TextView)about.findViewById(R.id.credits);
				info.setText(getString(R.string.credits, versionName));
				Builder aboutCreator = new AlertDialog.Builder(theme).setView(about);
				final AlertDialog aboutDialog = aboutCreator.create();
				aboutDialog.setIcon(R.drawable.icon);
				aboutDialog.setTitle(R.string.self_name);
//				aboutDialog.setButton(getText(R.string.changelog), new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int	whichButton) {
//						Uri uri = Uri.parse(getString(R.string.changelog_url));
//						startActivity(new Intent( Intent.ACTION_VIEW, uri));
//					}
//				});
				aboutDialog.show();
				return true;
				
			case SETTINGS:
//				availableAdapter.sleep();
				Intent settings = new Intent(this, Settings.class);
				startActivity(settings);
				return true;	
				
			case LOGIN:
//				boolean insertingRepo = false;
//				try {
//					insertingRepo = serviceDataCaller.callIsInsertingRepo();
//				} catch (RemoteException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				if(insertingRepo){
//					AptoideLog.d(Aptoide.this, getString(R.string.updating_repo_please_wait));
//					Toast.makeText(getApplicationContext(), getResources().getString(R.string.updating_repo_please_wait), Toast.LENGTH_SHORT).show();
//				}
//				else{
					Log.d("Aptoide-Settings", "clicked set server login");
					String token = null;
					try {
						token = serviceDataCaller.callGetServerToken();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(token == null){
						Log.d("Aptoide-Settings", "No login set");
						Intent login = new Intent(Aptoide.this, BazaarLogin.class);
						login.putExtra("InvoqueType", BazaarLogin.InvoqueType.NO_CREDENTIALS_SET.ordinal());
						startActivity(login);
	//					DialogLogin dialogLogin = new DialogLogin(Settings.this, serviceDataCaller, DialogLogin.InvoqueType.NO_CREDENTIALS_SET);
	//					loginComments.setOnDismissListener(new OnDismissListener() {
	//						@Override
	//						public void onDismiss(DialogInterface dialog) {
	//							addAppVersionComment();
	//						}
	//					});
	//					dialogLogin.show();
					}else{
						Log.d("Aptoide-Settings", "Login edit");
						Intent login = new Intent(Aptoide.this, BazaarLogin.class);
						login.putExtra("InvoqueType", BazaarLogin.InvoqueType.OVERRIDE_CREDENTIALS.ordinal());
						startActivity(login);
	//					DialogLogin dialogLogin = new DialogLogin(Settings.this, serviceDataCaller, DialogLogin.InvoqueType.OVERRIDE_CREDENTIALS);
	//					Toast.makeText(Settings.this, "Login already set", Toast.LENGTH_SHORT).show();
	//					dialogLogin.show();
					}
//				}
				return true;
				
			case FOLLOW:
				new DialogFollowOnSocialNets(this, serviceDataCaller).show();
				return true;
			
//			case SCHEDULED_DOWNLOADS:
//				availableAdapter.sleep();
//				Intent manageScheduled = new Intent(this, ManageScheduled.class);
//				startActivity(manageScheduled);
//				return true;
				
//			case UPDATE_ALL:
//				if(!loadingRepos.get()){
//					AptoideLog.d(this, "Update all");
//					try {
//						serviceDataCaller.callUpdateAll();
//					} catch (RemoteException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}else{
//					Toast.makeText(Aptoide.this, "Option not available while updating stores!", Toast.LENGTH_SHORT).show();
//				}
//				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
//	public void handleMyapp(){	//TODO refactor hardcoded strings
//		for (final ViewMyapp myapp : handlingMyapps) {
//			final AlertDialog installMyapp = new AlertDialog.Builder(this).create();
//			installMyapp.setTitle("Install");
//			installMyapp.setMessage("Do you wish to install: " + myapp.getName() + " ?");
//			installMyapp.setButton(getText(R.string.yes), new OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					installMyapp.dismiss();
//					try {
//						serviceDataCaller.callInstallMyapp(myapp);
//					} catch (RemoteException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			});
//			installMyapp.setButton2(getText(R.string.no), new OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					installMyapp.dismiss();
//					try {
//						serviceDataCaller.callRejectedMyapp();
//					} catch (RemoteException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			});
//			installMyapp.show();
//		}
//	}


    @Override
	public String getTag() {
		return TAG;
	}
    

	@Override
	protected void onDestroy() {
		if (serviceDataIsBound) {
			pageIndicatorAdapter.destroy();
			try {
				serviceDataCaller.callUnregisterAvailableAppsObserver(serviceDataCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
            unbindService(serviceDataConnection);
            serviceDataIsBound = false;
        }
//		categoriesAdapter.shutdownNow();
		availableAdapter.shutdownNow();
		installedAdapter.shutdownNow();
//		updatableAdapter.shutdownNow();
		
		super.onDestroy();
	}
	
}
