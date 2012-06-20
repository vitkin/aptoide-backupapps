/**
 * SelfUpdate, part of Aptoide
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
package pt.caixamagica.aptoide.appsbackup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import pt.caixamagica.aptoide.appsbackup.data.AIDLAptoideServiceData;
import pt.caixamagica.aptoide.appsbackup.data.AptoideServiceData;
import pt.caixamagica.aptoide.appsbackup.data.model.ViewListIds;
import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumServerUploadApkStatus;
import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewApk;
import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewUploadInfo;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * SelfUpdate, handles Aptoide's self-Update interface
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class Upload extends Activity {
	
//	AlertDialog selfUpdate;
	private AtomicInteger apksReadyNumber;
	private int apksNumber;
//	private HashMap<Integer, ViewApk> waitingApks;
	private HashMap<Integer, ViewApk> uploadingApks;
	private HashMap<Integer, EnumServerUploadApkStatus> doneApks;
	
	LinearLayout uploading;
	UploadingListAdapter uploadingAdapter;
	ArrayList<HashMap<String, Integer>> uploadingProgress;
	
	LinearLayout uploaded;
	ArrayAdapter<String> uploadedAdapter;
	ArrayList<String> uploadedNames;
	
	LinearLayout notUploaded;
	NotUploadedListAdapter notUploadedAdapter;
	ArrayList<HashMap<String, String>> notUploadedNames;
	
	Button backButton;
	
	private ExecutorService cachedThreadPool;
	
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
			
			handleUploads();
			
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
	
	
	private void refreshUploadedLists(){
		uploadedNames.clear();
		uploadedAdapter.notifyDataSetChanged();
		notUploadedNames.clear();
		notUploadedAdapter.notifyDataSetChanged();
		for (Entry<Integer, EnumServerUploadApkStatus> done : doneApks.entrySet()) {
			uploadingApks.get(done.getKey()).setProgress(100);
			if(done.getValue().equals(EnumServerUploadApkStatus.NO_ERROR)){
				uploaded.setVisibility(View.VISIBLE);
				uploadedNames.add(uploadingApks.get(done.getKey()).getName());
			}else{
				notUploaded.setVisibility(View.VISIBLE);
				HashMap<String, String> failed = new HashMap<String, String>();
				failed.put("hashid", Integer.toString(done.getKey()));
				failed.put("name", uploadingApks.get(done.getKey()).getName());
				failed.put("status", done.getValue().toString().toLowerCase()); //TODO support proper Error strings with i18n
				notUploadedNames.add(failed);
			}
		}
		uploadedAdapter.notifyDataSetChanged();
		notUploadedAdapter.notifyDataSetChanged();
		refreshUploadingList();
	}

	private void refreshUploadingList(){
		uploading.setVisibility(View.VISIBLE);
		uploadingProgress.clear();
		uploadingAdapter.notifyDataSetChanged();
		int visible = 0;
		for (Entry<Integer, ViewApk> uploading : uploadingApks.entrySet()){
			if(uploading.getValue().getProgress() != 100){
				visible++;
				HashMap<String, Integer> upload = new HashMap<String, Integer>(uploadingApks.size());
				upload.put(uploading.getValue().getName(), uploading.getValue().getProgress());
				uploadingProgress.add(upload);
			}
		}
		uploadingAdapter.notifyDataSetChanged();
		if(visible == 0){
			uploading.setVisibility(View.GONE);			
		}
	}
	
	
	
	
	private void handleUploads(){
		ViewListIds uploads = (ViewListIds) getIntent().getIntegerArrayListExtra("uploads");
		Log.d("Aptoide-AppsBackup", "uploads: "+uploads);
		apksNumber = uploads.size();
		for (int appHashid : uploads) {
			try {
				ViewUploadInfo uploadInfo = serviceDataCaller.callGetUploadInfo(appHashid);
				Log.d("Aptoide-AppsBackup", "upload: "+uploadInfo);
				ViewApk uploadingApk = new ViewApk(uploadInfo.getAppHashid(), uploadInfo.getAppName(), uploadInfo.getLocalPath()); //TODO refactor ViewUploadInfo to deprecate ViewApk and to receive repo info from servicedata
				uploadingApk.setRepository(uploadInfo.getRepoName());
				uploadingApk.setSize(uploadInfo.getSize());
				uploadingApks.put(uploadingApk.getAppHashid(), uploadingApk);
				upload(uploadingApk);
//				waitingApks.add(uploadingApk);
				
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		showUploadStatus();
//		if(waitingApks.size()>0){
//			new SubmitFormScreen(this, waitingApks.remove(0));
//		}
	}
	
	public void submit(ViewApk uploadingApk){
//		uploadingApks.put(uploadingApk.getAppHashid(), uploadingApk);
		showUploadStatus();
		doneApks.remove(uploadingApk.getAppHashid());
		refreshUploadedLists();
		upload(uploadingApk);
//		if(waitingApks.size()>0){
//			new SubmitFormScreen(this, waitingApks.remove(0));
//		}else{
//			showUploadStatus();
//		}
	}
	
	public void upload(final ViewApk uploadingApk){
		cachedThreadPool.execute(new Runnable() {
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
		
		uploading = (LinearLayout) findViewById(R.id.uploading_apps);
		ListView uploadingList = (ListView) findViewById(R.id.uploading_list);
		uploadingAdapter = new UploadingListAdapter(this, uploadingProgress);
		uploadingList.setAdapter(uploadingAdapter);
		refreshUploadingList();
		
		uploaded = (LinearLayout) findViewById(R.id.uploaded_apps);
		ListView uploadedList = (ListView) findViewById(R.id.uploaded_list);
		uploadedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, uploadedNames);
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
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	
		if(!serviceDataIsBound){
    		bindService(new Intent(this, AptoideServiceData.class), serviceDataConnection, Context.BIND_AUTO_CREATE);
    	}
		
		apksReadyNumber = new AtomicInteger(0);
		uploadingApks = new HashMap<Integer, ViewApk>();
//		waitingApks = new HashMap<Integer, ViewApk>();
		doneApks = new HashMap<Integer, EnumServerUploadApkStatus>();
		
		uploadingProgress = new ArrayList<HashMap<String,Integer>>();
		uploadedNames = new ArrayList<String>();
		notUploadedNames = new ArrayList<HashMap<String,String>>();
		
		cachedThreadPool = Executors.newCachedThreadPool();
		
		super.onCreate(savedInstanceState);
	}

	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK ) {
//			Log.d("AptoideAppsBackup-Upload", "");
//			//TODO check where we are and decide accordingly
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}


	@Override
	public void finish() {
		try {
			serviceDataCaller.callUpdateRepos();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if(serviceDataIsBound){
			unbindService(serviceDataConnection);
		}
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
			
	    	setContentView(R.layout.submit_form);
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

	    	setContentView(R.layout.submit_form);
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
			
			if(missingAppName && missingAppCategory && missingDescription){
				errorIntro.setText(R.string.missing_name_and_category_and_description);
			}else if(missingAppName && missingAppCategory && badWebsite){
				errorIntro.setText(R.string.missing_name_and_category_and_bad_website);
			}else if(missingAppName && missingAppCategory && badEmail){
				errorIntro.setText(R.string.missing_name_and_category_and_bad_email);
			}else if(missingAppName && missingAppCategory){
				errorIntro.setText(R.string.missing_name_and_category);
			}else if(missingAppName && missingDescription){
				errorIntro.setText(R.string.missing_name_and_description);
			}else if(missingAppName && badWebsite){
				errorIntro.setText(R.string.missing_name_and_bad_website);
			}else if(missingAppName && badEmail){
				errorIntro.setText(R.string.missing_name_and_bad_email);
			}else if(missingAppCategory && missingDescription){
				errorIntro.setText(R.string.missing_category_and_description);
			}else if(missingAppCategory && badWebsite){
				errorIntro.setText(R.string.missing_category_and_bad_website);
			}else if(missingAppCategory && badEmail){
				errorIntro.setText(R.string.missing_category_and_bad_email);
			}else if(missingAppName){
				errorIntro.setText(R.string.missing_apk_name);
			}else if(missingAppCategory){
				errorIntro.setText(R.string.missing_category);
			}else if(missingDescription){
				errorIntro.setText(R.string.missing_description);
			}else if(badWebsite){
				errorIntro.setText(R.string.bad_website);
			}else if(badEmail){
				errorIntro.setText(R.string.bad_email);
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
		
		
		
		public void setAppCategoryBox(Context context) {
			String categoryPrompt[] = {"Optional App category"};
			appCategory = (Spinner)findViewById(R.id.form_category);
			ArrayAdapter<CharSequence> categoryAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, categoryPrompt);
			categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			appCategory.setAdapter(categoryAdapter);
			if(uploadingApk.getCategory() !=  null){
				appCategory.setSelection(Integer.parseInt(uploadingApk.getCategory())-1);
			}
			appCategory.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
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
	
	

	public static class UploadingRowViewHolder{
		TextView app_name;
		ProgressBar progress;
	}
	
	public class UploadingListAdapter extends BaseAdapter{

		private LayoutInflater layoutInflater;

		private ArrayList<HashMap<String, Integer>> uploadingProgress = null;
		
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			UploadingRowViewHolder rowViewHolder;
			
			if(convertView == null){
				convertView = layoutInflater.inflate(R.layout.row_app_uploading, null);
				
				rowViewHolder = new UploadingRowViewHolder();
				rowViewHolder.app_name = (TextView) convertView.findViewById(R.id.name);
				rowViewHolder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
				
				convertView.setTag(rowViewHolder);
			}else{
				rowViewHolder = (UploadingRowViewHolder) convertView.getTag();
			}
			
			HashMap<String, Integer> upload = uploadingProgress.get(position);
			String name = "";
			for (String nameKey : upload.keySet()) {
				name = nameKey;
			}
			rowViewHolder.app_name.setText(name);
			if(upload.get(name) != 0 && upload.get(name) < 99){
				rowViewHolder.progress.setIndeterminate(false);
				rowViewHolder.progress.setMax(100);
			}else{
				rowViewHolder.progress.setIndeterminate(true);
			}
			rowViewHolder.progress.setProgress(upload.get(name));
			
			
			return convertView;
		}
		
		
		@Override
		public int getCount() {
			if(uploadingProgress != null){
				return uploadingProgress.size();
			}else{
				return 0;
			}
		}

		@Override
		public HashMap<String, Integer> getItem(int position) {
			return uploadingProgress.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		
		/**
		 * UploadingListAdapter Constructor
		 *
		 * @param context
		 * @param ArrayList<HashMap<String, Integer>> uploadingProgress
		 */
		public UploadingListAdapter(Context context, ArrayList<HashMap<String, Integer>> uploadingProgress){
			
			this.uploadingProgress = uploadingProgress;

			layoutInflater = LayoutInflater.from(context);
		} 
	}
	
	
	public static class NotUploadedRowViewHolder{
		TextView failed_name;
		TextView failed_status;
	}
	
	public class NotUploadedListAdapter extends BaseAdapter{

		private LayoutInflater layoutInflater;

		private ArrayList<HashMap<String, String>> notUploadedNames = null;
		
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			NotUploadedRowViewHolder rowViewHolder;
			
			if(convertView == null){
				convertView = layoutInflater.inflate(R.layout.row_app_not_uploaded, null);
				
				rowViewHolder = new NotUploadedRowViewHolder();
				rowViewHolder.failed_name = (TextView) convertView.findViewById(R.id.failed_name);
				rowViewHolder.failed_status = (TextView) convertView.findViewById(R.id.failed_status);
				
				convertView.setTag(rowViewHolder);
			}else{
				rowViewHolder = (NotUploadedRowViewHolder) convertView.getTag();
			}
			
			rowViewHolder.failed_name.setText(getItem(position).get("name"));
			rowViewHolder.failed_status.setText(getItem(position).get("status"));
			
			
			return convertView;
		}
		
		
		@Override
		public int getCount() {
			if(notUploadedNames != null){
				return notUploadedNames.size();
			}else{
				return 0;
			}
		}

		@Override
		public HashMap<String, String> getItem(int position) {
			return notUploadedNames.get(position);
		}
		
		@Override
		public long getItemId(int position) {
			return Integer.parseInt(notUploadedNames.get(position).get("hashid"));
		}
		
		
		/**
		 * UploadingListAdapter Constructor
		 *
		 * @param context
		 * @param ArrayList<HashMap<String, Integer>> uploadingProgress
		 */
		public NotUploadedListAdapter(Context context, ArrayList<HashMap<String, String>> notUploadedNames){
			
			this.notUploadedNames = notUploadedNames;

			layoutInflater = LayoutInflater.from(context);
		} 
	}

	
}
