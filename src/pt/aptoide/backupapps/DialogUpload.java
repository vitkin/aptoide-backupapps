package pt.aptoide.backupapps;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import pt.aptoide.backupapps.R;
import pt.aptoide.backupapps.data.util.Security;
import pt.aptoide.backupapps.data.webservices.EnumServerStatus;
import pt.aptoide.backupapps.data.webservices.ViewApk;
import pt.aptoide.backupapps.data.AIDLAptoideServiceData;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author dsilveira
 * 
 * Implementation of the upload interface.
 */
public class DialogUpload extends Dialog{
	
	private AIDLAptoideServiceData serviceDataCaller = null;

	private EditText username;
	private EditText password;
//	private boolean isLoginSubmited;
	private boolean success;
	private CheckBox showPass;
	
//	private ViewApk uploadingApk;
	
	
	
	public DialogUpload(Context context, AIDLAptoideServiceData serviceDataCaller) {
		super(context);
		this.serviceDataCaller = serviceDataCaller;
//		isLoginSubmited = false;
		success = false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.example_login);
		
		((Button)this.findViewById(R.id.new_account)).setOnClickListener(new View.OnClickListener(){
			
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.aptoide.com/account/new-user"));
				getContext().startActivity(browserIntent);
			}
			
		});
		
		
		
		this.setTitle(this.getContext().getString(R.string.bazaar_login));
		
		username = ((EditText)findViewById(R.id.username));
//		username.setOnFocusChangeListener(new SetBlankOnFocusChangeListener());
		password = ((EditText)findViewById(R.id.password));
		
//		password.setOnFocusChangeListener(new SetBlankOnFocusChangeListener());
		
		showPass = (CheckBox) findViewById(R.id.show_password);

		showPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    password.setInputType(129);
//                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD );
                }
            }
        });
		
		((Button)findViewById(R.id.submit_login)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg) {
				success = false;
				if(username.getText().toString().trim().equals("")){
//				if(!((SetBlankOnFocusChangeListener)username.getOnFocusChangeListener()).getAlreadySetted()){
					Toast.makeText(DialogUpload.this.getContext(), DialogUpload.this.getContext().getString(R.string.no_username), Toast.LENGTH_SHORT).show();
				}else if(password.getText().toString().trim().equals("")){
//				}else if(!((SetBlankOnFocusChangeListener)password.getOnFocusChangeListener()).getAlreadySetted()){
					Toast.makeText(DialogUpload.this.getContext(), DialogUpload.this.getContext().getString(R.string.no_password), Toast.LENGTH_SHORT).show();
				}else{
						
					ProgressDialog dialogProgress = ProgressDialog.show(getContext(), getContext().getString(R.string.logging_in), getContext().getString(R.string.please_wait),true);
					dialogProgress.setIcon(android.R.drawable.ic_menu_info_details);
					dialogProgress.setCancelable(true);
					
//						final AtomicBoolean successLogin = new AtomicBoolean(true);
					
					dialogProgress.setOnDismissListener(new OnDismissListener(){
						public void onDismiss(DialogInterface arg0) {
//								if(successLogin.get()){
								
								if(success){
//										isLoginSubmited = true;
									dismiss();
									
									Log.d("Aptoide", "Logged in");
//										Intent loginAction = new Intent();
//										loginAction.setAction("pt.aptoide.LOGIN_ACTION");
//										LoginDialog.this.getContext().sendBroadcast(loginAction);
									
								}else{
//										switch (Response) {
//										case bad_login:
//										Toast.makeText(getContext(), DialogLogin.this.getContext().getString(R.string.bad_login), Toast.LENGTH_LONG).show();
//											break;
//
//										default:
//											break;
//										}
									
								}
								
//								}else{
//									Toast.makeText(getContext(), LoginDialog.this.getContext().getString(R.string.unabletoexecute), Toast.LENGTH_LONG).show();
//								}
						}
					});
					
//						new Login(getContext(), dialogProgress, username.getText().toString(), password.getText().toString()).execute();
					
				}
			}
		});
		
	}
	
//	public boolean isLoginSubmited(){
//		return isLoginSubmited;
//	}
	
//	public class Login extends AsyncTask<Void, Void, EnumServerStatus>{
//		
//		private Context context;
//		private ProgressDialog dialogProgress;
//		private String username;
//		private String password = null;
//		
//		public Login(Context context, ProgressDialog dialogProgress, String username, String password) {
//			this.context = context;
//			this.dialogProgress = dialogProgress;
//			this.username = username.toLowerCase();
//			MessageDigest md;
//			try {
//				md = MessageDigest.getInstance("SHA");
//				md.update(password.getBytes());
//				this.password = Security.byteArrayToHexString(md.digest());
//			} catch (NoSuchAlgorithmException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		@Override
//		protected EnumServerStatus doInBackground(Void... args) {
//			
//			if(password == null){
//				return null;
//			}else{
//				try {
//					return EnumServerStatus.reverseOrdinal(serviceDataCaller.callServerLogin(this.username, this.password));
//				} catch (RemoteException e) {
//					e.printStackTrace();
//					return null;
//				}
//			}
//		}
//		
//		protected void onPostExecute(EnumServerStatus status) {
//			if(status!=null){
//
//				if(status.equals(EnumServerStatus.SUCCESS)){
//					success = true;
//				}else{
//					success = false;
//					Toast.makeText(getContext(), status.toString(), Toast.LENGTH_SHORT).show();
//				}
//				
//			}else{
//				Toast.makeText(getContext(), context.getString(R.string.service_unavailable), Toast.LENGTH_SHORT).show();
//			}
//			dialogProgress.dismiss();
//	    }
//		
//	}
	
	
	
//	
//	
//	
//	public static ResponseHandler checkCredentials(Context context, String user, String password) throws IOException, ParserConfigurationException, SAXException{
//		
//		SAXParserFactory spf = SAXParserFactory.newInstance(); //Throws SAXException, ParserConfigurationException, SAXException 
//		SAXParser sp = spf.newSAXParser();
//		String url = String.format(Configs.WEB_SERVICE_GET_CHECK_CREDENTIALS, URLEncoder.encode(user), URLEncoder.encode(password));
//		
//		InputStream stream = NetworkApis.getInputStream(context, url);
//		BufferedInputStream bstream = new BufferedInputStream(stream);
//		
//		ResponseHandler tasteResponseReader = new ResponseHandler();
//		
//		sp.parse(new InputSource(bstream), tasteResponseReader);
//		
//		stream.close();
//		bstream.close();
//		
//		return tasteResponseReader;  
//		
//	}
	
}
