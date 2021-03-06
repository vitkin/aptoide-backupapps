/**
 * Upload, part of Aptoide
 * Copyright (C) 2011 Duarte Silveira
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
package pt.aptoide.backupapps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import pt.aptoide.backupapps.R;
import pt.aptoide.backupapps.data.AptoideServiceData;
import pt.aptoide.backupapps.data.display.ViewApplicationUpload;
import pt.aptoide.backupapps.data.display.ViewApplicationUploadFailed;
import pt.aptoide.backupapps.data.display.ViewApplicationUploading;
import pt.aptoide.backupapps.data.model.ViewListIds;
import pt.aptoide.backupapps.data.util.Constants;
import pt.aptoide.backupapps.data.webservices.EnumServerUploadApkStatus;
import pt.aptoide.backupapps.data.webservices.ViewApk;
import pt.aptoide.backupapps.data.webservices.ViewUploadInfo;
import pt.aptoide.backupapps.debug.exceptions.AptoideException;
import pt.aptoide.backupapps.ifaceutil.NotUploadedListAdapter;
import pt.aptoide.backupapps.ifaceutil.UploadedListAdapter;
import pt.aptoide.backupapps.ifaceutil.UploadingListAdapter;
import pt.aptoide.backupapps.AIDLUpload;
import pt.aptoide.backupapps.data.AIDLAptoideServiceData;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * Upload, handles Aptoide's upload interface
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class Upload extends Activity {
	
	private boolean isRunning = false;
	private AtomicBoolean showingForm;
	private AtomicInteger apksReadyNumber;
	private int apksNumber;
//	private HashMap<Integer, ViewApk> waitingApks;
	private HashMap<Integer, ViewApk> uploadingApks;
	private HashMap<Integer, EnumServerUploadApkStatus> doneApks;
	
	LinearLayout uploading;
	UploadingListAdapter uploadingAdapter;
	ArrayList<ViewApplicationUploading> uploadingProgress;
	
	LinearLayout uploaded;
//	ArrayAdapter<String> uploadedAdapter;
	UploadedListAdapter uploadedAdapter;
	ArrayList<ViewApplicationUpload> uploadedNames;
	
	LinearLayout notUploaded;
	NotUploadedListAdapter notUploadedAdapter;
	ArrayList<ViewApplicationUploadFailed> notUploadedNames;
	
	Button backButton;
	AtomicBoolean goingBackEnabled;
	
	private ExecutorService uploadsThreadPool;
	
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
			
			Log.v("Aptoide-Upload", "Connected to ServiceData");
	        
			try {
				serviceDataCaller.callRegisterUploadObserver(serviceDataCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			handleUploads(getIntent());
			
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			serviceDataIsBound = false;
			serviceDataCaller = null;
			
			Log.v("Aptoide-Upload", "Disconnected from ServiceData");
		}
	};
	
	private AIDLUpload.Stub serviceDataCallback = new AIDLUpload.Stub() {
		@Override
		public void uploadingProgressSetCompletionTarget(int appHashid, int progressCompletionTarget) throws RemoteException {
			// TODO Auto-generated method stub
		}

		@Override
		public void uploadingProgressUpdate(int appHashid, int currentProgress) throws RemoteException {
			uploadingApks.get(appHashid).setProgress(currentProgress);
			interfaceTasksHandler.sendEmptyMessage(EnumUploadInterfaceTasks.UPDATE_PROGRESS.ordinal());			
		}

		@Override
		public void uploadingProgressIndeterminate(int appHashid) throws RemoteException {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void uploadDone(int appHashid, int status) throws RemoteException {
			Log.d("Aptoide-Upload", "upload done, hashid: "+appHashid+" status: "+EnumServerUploadApkStatus.reverseOrdinal(status));
			doneApks.put(appHashid, EnumServerUploadApkStatus.reverseOrdinal(status));
			apksReadyNumber.incrementAndGet();
			interfaceTasksHandler.sendEmptyMessage(EnumUploadInterfaceTasks.UPLOAD_DONE.ordinal());
		}
	};
	
	
	private Handler interfaceTasksHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	EnumUploadInterfaceTasks task = EnumUploadInterfaceTasks.reverseOrdinal(msg.what);
        	switch (task) {
				case UPLOAD_DONE:
					if(apksReadyNumber.get() == apksNumber){
						uploading.setVisibility(View.GONE);
					}
					refreshUploadedLists();
					break;
	
				case UPDATE_PROGRESS:
					refreshUploadingList();
					
				default:
					break;
				}
        }
	};
	
	
	private synchronized void refreshUploadedLists(){
		uploadedNames.clear();
		uploadedAdapter.notifyDataSetChanged();
		notUploadedNames.clear();
		notUploadedAdapter.notifyDataSetChanged();
		for (Entry<Integer, EnumServerUploadApkStatus> done : doneApks.entrySet()) {
			uploadingApks.get(done.getKey()).setUploading(false);
			uploadingApks.get(done.getKey()).setProgress(100);
			if(done.getValue().equals(EnumServerUploadApkStatus.SUCCESS)){
				uploaded.setVisibility(View.VISIBLE);
				uploadedNames.add(new ViewApplicationUpload(uploadingApks.get(done.getKey()).getAppHashid(), uploadingApks.get(done.getKey()).getName()));
			}else{
				notUploaded.setVisibility(View.VISIBLE);
//				HashMap<String, String> failed = new HashMap<String, String>();
//				failed.put("hashid", Integer.toString(done.getKey()));
//				failed.put("name", uploadingApks.get(done.getKey()).getName());
//				failed.put("status", done.getValue().toString(this));
//				notUploadedNames.add(failed);
				notUploadedNames.add(new ViewApplicationUploadFailed(done.getKey(), uploadingApks.get(done.getKey()).getName(),done.getValue()));
			}
		}
		uploadedAdapter.notifyDataSetChanged();
		notUploadedAdapter.notifyDataSetChanged();
		refreshUploadingList();
	}

	private synchronized void refreshUploadingList(){
		uploading.setVisibility(View.VISIBLE);
		uploadingProgress.clear();
		uploadingAdapter.notifyDataSetChanged();
		int visible = 0;
		for (Entry<Integer, ViewApk> uploading : uploadingApks.entrySet()){
			if(uploading.getValue().getProgress() != 100){
				visible++;
				ViewApplicationUploading upload = new ViewApplicationUploading(uploading.getValue().getAppHashid(), uploading.getValue().getName());
				uploadingProgress.add(upload);
			}
		}
		uploadingAdapter.notifyDataSetChanged();
		if(visible == 0){
			uploading.setVisibility(View.GONE);
			enableGoingBack();
		}
	}
	
	
	
	
	private void handleUploads(Intent intent){
		ViewListIds uploads = (ViewListIds) intent.getIntegerArrayListExtra("uploads");
		Log.d("Aptoide-AppsBackup", "uploads: "+uploads);
		if(uploads != null){
			apksNumber = uploads.size();
			for (int appHashid : uploads) {
				try {
					ViewUploadInfo uploadInfo = serviceDataCaller.callGetUploadInfo(appHashid);
					if(uploadInfo == null){
						throw new AptoideException("failed to retrieve uploadInfo");
					}
					Log.d("Aptoide-AppsBackup", "upload: "+uploadInfo);
					ViewApk uploadingApk = new ViewApk(uploadInfo.getAppHashid(), uploadInfo.getAppName(), uploadInfo.getLocalPath()); //TODO refactor ViewUploadInfo to deprecate ViewApk and to receive repo info from servicedata
					uploadingApk.setRepository(uploadInfo.getRepoName());
					uploadingApk.setSize(uploadInfo.getSize());
					uploadingApks.put(uploadingApk.getAppHashid(), uploadingApk);
//					waitingApks.add(uploadingApk);
					
				} catch (RemoteException e) {
					e.printStackTrace();
					continue;
				}
			}
			
			showUploadStatus();
//			if(waitingApks.size()>0){
//				new SubmitFormScreen(this, waitingApks.remove(0));
//			}
			
			for(ViewApk uploadingApk : uploadingApks.values()){
				if(!uploadingApk.isUploading()){
					upload(uploadingApk);
				}
			}
			
		}
	}
	
	public void submit(ViewApk uploadingApk){
//		uploadingApks.put(uploadingApk.getAppHashid(), uploadingApk);
		doneApks.remove(uploadingApk.getAppHashid());
		uploadingApk.resetProgress();
		showUploadStatus();
		refreshUploadedLists();
		if(!uploadingApk.isUploading()){
			upload(uploadingApk);
		}
//		if(waitingApks.size()>0){
//			new SubmitFormScreen(this, waitingApks.remove(0));
//		}else{
//			showUploadStatus();
//		}
	}
	
	public void upload(final ViewApk uploadingApk){
		disableGoingBack();
		uploadingApk.setUploading(true);
		uploadsThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					serviceDataCaller.callUploadApk(uploadingApk);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	
	public void showUploadStatus(){
		setContentView(R.layout.batch_upload_status);
    	showingForm.set(false);
		
		uploading = (LinearLayout) findViewById(R.id.uploading_apps);
		ListView uploadingList = (ListView) findViewById(R.id.uploading_list);
		uploadingAdapter = new UploadingListAdapter(this, uploadingProgress);
		uploadingList.setAdapter(uploadingAdapter);
		refreshUploadingList();
		
		uploaded = (LinearLayout) findViewById(R.id.uploaded_apps);
		ListView uploadedList = (ListView) findViewById(R.id.uploaded_list);
//		uploadedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, uploadedNames);
		uploadedAdapter = new UploadedListAdapter(this, uploadedNames);
		uploadedList.setAdapter(uploadedAdapter);
		uploaded.setVisibility(View.GONE);
		
		notUploaded = (LinearLayout) findViewById(R.id.failed_apps);
		ListView notUploadedList = (ListView) findViewById(R.id.failed_list);
//		notUploadedAdapter = new SimpleAdapter(this, notUploadedNames, R.layout.row_app_not_uploaded, new String[]{"name", "status"}, new int[]{R.id.failed_name, R.id.failed_status});
		notUploadedAdapter = new NotUploadedListAdapter(this, notUploadedNames);
		notUploadedList.setAdapter(notUploadedAdapter);
		notUploadedList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
				int appHashid = (int) notUploadedAdapter.getItemId(position);
				new SubmitFormScreen(Upload.this, uploadingApks.get(appHashid), doneApks.get(appHashid));
			}
		});
		notUploaded.setVisibility(View.GONE);
		
		backButton = (Button) findViewById(R.id.uploaded_exit);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		  });
		disableGoingBack();
	}
	
	private void enableGoingBack(){
//		backButton.setEnabled(true);
//		backButton.setVisibility(View.VISIBLE);
		backButton.setTextColor(Color.BLACK);
		backButton.setClickable(true);		
		goingBackEnabled.set(true);
	}
	
	private void disableGoingBack(){
//		backButton.setEnabled(false);
//		backButton.setVisibility(View.INVISIBLE);
		backButton.setTextColor(Color.GRAY);
		backButton.setClickable(false);
		goingBackEnabled.set(false);
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        if(!isRunning){
        	isRunning = true;

			if(!serviceDataIsBound){
	    		bindService(new Intent(this, AptoideServiceData.class), serviceDataConnection, Context.BIND_AUTO_CREATE);
	    	}
			
			showingForm = new AtomicBoolean(false);
			apksReadyNumber = new AtomicInteger(0);
			uploadingApks = new HashMap<Integer, ViewApk>();
//			waitingApks = new HashMap<Integer, ViewApk>();
			doneApks = new HashMap<Integer, EnumServerUploadApkStatus>();
			
			uploadingProgress = new ArrayList<ViewApplicationUploading>();
			uploadedNames = new ArrayList<ViewApplicationUpload>();
			notUploadedNames = new ArrayList<ViewApplicationUploadFailed>();
			
			
			uploadsThreadPool = Executors.newFixedThreadPool(Constants.MAX_PARALLEL_UPOADS);
			
			goingBackEnabled = new AtomicBoolean(true);
        }
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		Log.d("Aptoide-AppsBackup-Upload", "new Intent ");
		handleUploads(intent);
		super.onNewIntent(intent);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && !goingBackEnabled.get()) {
			Log.d("AptoideAppsBackup-Upload", "back press ignored");
			return true;
		}
		if(showingForm.get()){
			showUploadStatus();
			refreshUploadedLists();			
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}


	@Override
	public void finish() {
		Log.d("Aptoide-AppsBackup-Upload", "finish ");
		try {
			serviceDataCaller.callUpdateRepos();
			serviceDataCaller.callDelayedUpdateRepos();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if(serviceDataIsBound){
			unbindService(serviceDataConnection);
		}
		
		isRunning = false;
		super.finish();
	}
	
	
	public class SubmitFormScreen {
		private ViewApk uploadingApk;
		
		private ScrollView scrollView;
		
		private TextView errorIntro;
		
		private EditText appName;
		private Spinner appCategory;
		private Spinner appRating;
		
//		private TextView apkPath;

		private EditText appDescription;
		private EditText phoneNumber;
		private EditText eMail;
		private EditText webURL;
		private Button backButton;
		private Button submitButton;
		
		public String getAppName() {
			return appName.getText().toString();
		}
		
		public String getAppDescription() {
			return appDescription.getText().toString();
		}
		
		public String getAppCategory() {
			return appCategory.getSelectedItem().toString();
		}
		
		public String getAppCategoryPosition() {
			return Integer.toString( appCategory.getSelectedItemPosition()+1 );
		}
		
		public String getAppRating() {
			return appRating.getSelectedItem().toString();
		}
		
		public String getAppRatingPosition() {
			return Integer.toString( appRating.getSelectedItemPosition()+1 );
		}
		
		public String getPhoneNumber() {
			return phoneNumber.getText().toString();
		}
		
		public String getEMail() {
			return eMail.getText().toString();
		}
		
		public String getWebURL() {
			return webURL.getText().toString();
		}
		
		public SubmitFormScreen(Context context, ViewApk uploadingApk){
//			setUiState(EnumUIStateName.PRESENTING_SUBMIT_FORM);
			
			this.uploadingApk = uploadingApk;
			
	    	setContentView(R.layout.form_submit);
	    	showingForm.set(true);
			setAppNameBox();
			setAppRatingBox(context);
//			setApkPath();
			setAppCategoryBox(context);
			setAppDescriptionBox();
			setPhoneNumberBox();
			setEMailBox();
			setWebURLBox();
			setBackButton();
			setSubmitButton();
		}
		
		public SubmitFormScreen(Context context, ViewApk uploadingApk, EnumServerUploadApkStatus errorState){
			this.uploadingApk = uploadingApk;

	    	setContentView(R.layout.form_submit);
	    	showingForm.set(true);
			setAppNameBox();
			setAppRatingBox(context);
//			setApkPath();
			setAppCategoryBox(context);
			setAppDescriptionBox();
			setPhoneNumberBox();
			setEMailBox();
			setWebURLBox();
			setBackButton();
			setSubmitButton();
			
			switch (errorState) {
				case MISSING_DESCRIPTION:
					setErrorIntro(false, false, true, false, false);					
					break;
				case BAD_CATEGORY:
					setErrorIntro(false, true, false, false, false);
					break;
				case BAD_WEBSITE:
					setErrorIntro(false, false, false, true, false);
					break;
				case BAD_EMAIL:
					setErrorIntro(false, false, false, false, true);
					break;

				default:
					submit(uploadingApk);
					break;
			}
			
		}
		
		private void prepareErrorIntro(){
			errorIntro = (TextView) findViewById(R.id.form_intro);
			errorIntro.setTextColor(Color.RED);
			
			scrollView = (ScrollView) findViewById(R.id.scroll_view);
			scrollView.pageScroll(View.FOCUS_UP);
		}
		
		
		public void setErrorIntro(boolean missingAppName, boolean missingAppCategory, boolean missingDescription, boolean badWebsite, boolean badEmail){

			prepareErrorIntro();
			
			if(missingAppName){
				errorIntro.setText(R.string.enter_apk_name);
			}else if(missingAppCategory){
				errorIntro.setText(R.string.select_category);
			}else if(missingDescription){
				errorIntro.setText(R.string.enter_description);
			}else if(badWebsite){
				errorIntro.setText(R.string.invalid_website);
			}else if(badEmail){
				errorIntro.setText(R.string.invalid_email);
			}
			
			scrollView.pageScroll(View.FOCUS_UP);
		}
		
		
		public void setAppNameBox() {
			appName = (EditText) findViewById(R.id.form_name);
			appName.setText(uploadingApk.getName());
			appName.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
			        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
			        	Log.d("AptoideUploader-form", "App name: " + appName.getText());
			          return true;
			        }
					return false;
				}
			});
		}
		
		public void setAppRatingBox(Context context) {
			appRating = (Spinner)findViewById(R.id.form_rating);
			ArrayAdapter<CharSequence> newRatingAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.rating_array, android.R.layout.simple_spinner_item);
			newRatingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			appRating.setAdapter(newRatingAdapter);
			if(uploadingApk.getRating() !=  null){
				appRating.setSelection(Integer.parseInt(uploadingApk.getRating())-1);
			}
			appRating.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					Log.d("AptoideUploader-form", "App rating: " + (appRating.getSelectedItemPosition()+1) + " - " + appRating.getSelectedItem());
				}
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {				
				} 
			});
		}
		
		
		public void setAppCategorySelectionBox(){
			ArrayAdapter<CharSequence> newCategoryAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.category_array, android.R.layout.simple_spinner_item);
			newCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    appCategory.setAdapter(newCategoryAdapter);
		    appCategory.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					Log.d("AptoideUploader-form", "App category: " + (appCategory.getSelectedItemPosition()+1) + " - " + appCategory.getSelectedItem());
				}
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {				
				} 
			});			
		}
				
		public void setAppCategoryBox(Context context) {
			String categoryPrompt[] = {"Optional App category"};
			appCategory = (Spinner)findViewById(R.id.form_category);
			ArrayAdapter<CharSequence> categoryAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, categoryPrompt);
			categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			appCategory.setAdapter(categoryAdapter);
			if(uploadingApk.getCategory() !=  null){
				setAppCategorySelectionBox();
				appCategory.setSelection(Integer.parseInt(uploadingApk.getCategory())-1);
			}
			appCategory.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					setAppCategorySelectionBox();
				    return false;
				}
			});
		}
		
		public void setAppDescriptionBox() {
			appDescription = (EditText) findViewById(R.id.form_desc);
			if(uploadingApk.getDescription() !=  null){
				appDescription.setText(uploadingApk.getDescription());
			}
			appDescription.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
			        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
			        	Log.d("AptoideUploader-form", "App description: " + appDescription.getText());
			          return true;
			        }
					return false;
				}
			});
		}
		
		public void setPhoneNumberBox() {
			phoneNumber = (EditText) findViewById(R.id.form_phone);
			if(uploadingApk.getPhone() != null){
				phoneNumber.setText(uploadingApk.getPhone());
			}
			phoneNumber.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
			        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
			        	Log.d("AptoideUploader-form", "Phone Number: " + phoneNumber.getText());
			          return true;
			        }
					return false;
				}
			});
		}
		
		public void setEMailBox() {
			eMail = (EditText) findViewById(R.id.form_e_mail);
			if(uploadingApk.getEmail() != null){
				eMail.setText(uploadingApk.getEmail());
			}
			eMail.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
			        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
			        	Log.d("AptoideUploader-form", "E-mail: " + eMail.getText());
			          return true;
			        }
					return false;
				}
			});
		}
		
		public void setWebURLBox() {
			webURL = (EditText) findViewById(R.id.form_url);
			if(uploadingApk.getWebURL() != null){
				webURL.setText(uploadingApk.getWebURL());
			}
			webURL.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
			        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
			        	Log.d("AptoideUploader-form", "Web url: " + webURL.getText());
			          return true;
			        }
					return false;
				}
			});
		}
		
		public void setBackButton() {
			backButton = (Button) findViewById(R.id.form_back);
			backButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showUploadStatus();
					refreshUploadedLists();
				}
			  });
		}
		
		public void setSubmitButton() {
			submitButton = (Button) findViewById(R.id.form_submit);
			submitButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(submitButton.getWindowToken(), 0);
					
					
//					if(appName.getText().toString().equals("") && appCategory.getSelectedItem().equals("Optional App category") && (getWebState().equals(EnumWebStateName.ERROR) && errorState.equals(EnumErrorStates.MISSING_DESCRIPTION) && appDescription.getText().toString().equals(""))){
////						setErrorIntro(true, true, true, false, false);
////						Log.d("AptoideUploader-submit-error", "no app name   ,   no category   and   no description"); 
//					}else if(appName.getText().toString().equals("") && (getWebState().equals(EnumWebStateName.ERROR) && errorState.equals(EnumErrorStates.BAD_CATEGORY) && appCategory.getSelectedItem().equals("App category"))){
//						setErrorIntro(true, true, false, false, false);
//						Log.d("AptoideUploader-submit-error", "no app name   and   no category"); 
//					}else if(appName.getText().toString().equals("") && (getWebState().equals(EnumWebStateName.ERROR) && errorState.equals(EnumErrorStates.MISSING_DESCRIPTION) && appDescription.getText().toString().equals(""))){
//						setErrorIntro(true, false, true, false, false);
//						Log.d("AptoideUploader-submit-error", "no app name   and   no description"); 
//					}else if((getWebState().equals(EnumWebStateName.ERROR) && errorState.equals(EnumErrorStates.BAD_CATEGORY) && appCategory.getSelectedItem().equals("Optional App category")) && (getWebState().equals(EnumWebStateName.ERROR) && errorState.equals(EnumErrorStates.MISSING_DESCRIPTION) && appDescription.getText().toString().equals(""))){
//						setErrorIntro(false, true, true, false, false);
//						Log.d("AptoideUploader-submit-error", "no category   and   no description"); 
//					}else 
						if(appName.getText().toString().equals("")){
						setErrorIntro(true, false, false, false, false);
						Log.d("AptoideUploader-submit-error", "no app name"); 
//					}else if( getWebState().equals(EnumWebStateName.ERROR) && errorState.equals(EnumErrorStates.BAD_CATEGORY) && appCategory.getSelectedItem().equals("Optional App category") ){
//						setErrorIntro(false, true, false, false, false);
//						Log.d("AptoideUploader-submit-error", "no category"); 
//					}else if( getWebState().equals(EnumWebStateName.ERROR) && errorState.equals(EnumErrorStates.MISSING_DESCRIPTION) && appDescription.getText().toString().equals("") ){
//						setErrorIntro(false, false, true, false, false);
//						Log.d("AptoideUploader-submit-error", "no description"); 
					}else{
						Log.d("AptoideUploader-submit", "about to submit");
						
						uploadingApk.setDescription(getAppDescription());
						if(!appCategory.getSelectedItem().equals("Optional App category")){
							uploadingApk.setCategory( getAppCategoryPosition() );
						}
						uploadingApk.setRating( getAppRatingPosition() );
//						Log.d("AptoideUploader-apk", uploadingApk.toString());
						
						if(!getPhoneNumber().equals("")){
							uploadingApk.setPhone(getPhoneNumber());
						}else{
							uploadingApk.setPhone(null);
						}
						
						if(!getEMail().equals("")){
							uploadingApk.setEmail(getEMail());
						}else{
							uploadingApk.setEmail(null);
						}
						
						if(!getWebURL().equals("")){
							uploadingApk.setWebURL(getWebURL());
						}else{
							uploadingApk.setWebURL(null);
						}
						
						Log.d("AptoideUploader-dev", uploadingApk.toString());
						
						submit(uploadingApk);
					}
				}
			  });
		}
		
//		public void setApkPath() {
//			apkPath = (TextView) findViewById(R.id.form_apk_path);
//		    apkPath.setText(uploadingApk.getPath());
//		}
		
	}
	
}
