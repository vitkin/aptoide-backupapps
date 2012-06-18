package pt.caixamagica.aptoide.appsbackup;

import pt.caixamagica.aptoide.appsbackup.R;
import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumServerLoginCreateStatus;
import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumServerLoginStatus;
import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewServerLogin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import pt.caixamagica.aptoide.appsbackup.data.AIDLAptoideServiceData;

/**
 * Implementation of the login interface.
 */
public class DialogLogin extends Dialog{
	
	private AIDLAptoideServiceData serviceDataCaller = null;
	
	private EditText username;
	private EditText password;
//	private boolean isLoginSubmited;
	private CheckBox showPass;
	
	private EditText repository;
	private CheckBox privt;
	private EditText priv_username;
	private EditText priv_password;
	private CheckBox priv_showPass;

	private boolean success;	
	private LoginState loginState;
	
	private ViewServerLogin serverLogin;
	
	private InvoqueType invoqueType;
	
	public static enum LoginState{
		WAITING,
		SUCCESS,
		BAD_LOGIN,
		REPO_NOT_FROM_USER
	}
	
	public static enum InvoqueType{ 
		CREDENTIALS_FAILED,
		NO_CREDENTIALS_SET,
		OVERRIDE_CREDENTIALS
	}
	
	
	public DialogLogin(Context context, AIDLAptoideServiceData serviceDataCaller, InvoqueType invoqueType) {
		super(context);
		this.serviceDataCaller = serviceDataCaller;
//		isLoginSubmited = false;
		this.invoqueType = invoqueType;
		success = false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_bazaar_login);
		
		loginState = LoginState.WAITING;
		
		((Button)this.findViewById(R.id.new_account)).setOnClickListener(new View.OnClickListener(){
			
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bazaarandroid.com/account/new-user"));
				getContext().startActivity(browserIntent);
			}
			
		});
		
		
		
		this.setTitle(R.string.bazaar_login);
		
		username = ((EditText)findViewById(R.id.username));
		password = ((EditText)findViewById(R.id.password));
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
		
		repository = ((EditText)findViewById(R.id.repository));
		privt = (CheckBox) findViewById(R.id.privt_store);
		privt.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					priv_username.setEnabled(true);
					priv_password.setEnabled(true);
					priv_showPass.setEnabled(true);
					priv_username.setVisibility(View.VISIBLE);
					priv_password.setVisibility(View.VISIBLE);
					priv_showPass.setVisibility(View.VISIBLE);
				}else{
					priv_username.setEnabled(false);
					priv_password.setEnabled(false);
					priv_showPass.setEnabled(false);
					priv_username.setVisibility(View.GONE);
					priv_password.setVisibility(View.GONE);
					priv_showPass.setVisibility(View.GONE);
				}
			}
		});
		priv_username = ((EditText)findViewById(R.id.priv_username));
		priv_username.setEnabled(false);
		priv_password = ((EditText)findViewById(R.id.priv_password));
		priv_password.setEnabled(false);
		priv_showPass = (CheckBox) findViewById(R.id.priv_show_password);
		priv_showPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                	priv_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                	priv_password.setInputType(129);
//                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD );
                }
            }
        });
		priv_showPass.setEnabled(false);
		
		
		((Button)findViewById(R.id.login)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg) {
				success = false;
				if(username.getText().toString().trim().equals("")){
					Toast.makeText(DialogLogin.this.getContext(), DialogLogin.this.getContext().getString(R.string.no_username), Toast.LENGTH_SHORT).show();
				}else if(password.getText().toString().trim().equals("")){
					Toast.makeText(DialogLogin.this.getContext(), DialogLogin.this.getContext().getString(R.string.no_password), Toast.LENGTH_SHORT).show();
				}else if(repository.getText().toString().trim().equals("")){
					Toast.makeText(DialogLogin.this.getContext(), DialogLogin.this.getContext().getString(R.string.no_repository), Toast.LENGTH_SHORT).show();
				}else if(privt.isChecked() && priv_username.getText().toString().trim().equals("")){
					Toast.makeText(DialogLogin.this.getContext(), DialogLogin.this.getContext().getString(R.string.no_private_repo_username), Toast.LENGTH_SHORT).show();
				}else if(privt.isChecked() && priv_password.getText().toString().trim().equals("")){
					Toast.makeText(DialogLogin.this.getContext(), DialogLogin.this.getContext().getString(R.string.no_private_repo_password), Toast.LENGTH_SHORT).show();
				}else{
						
					ProgressDialog loginProgress = ProgressDialog.show(getContext(), getContext().getString(R.string.logging_in), getContext().getString(R.string.please_wait),true);
					loginProgress.setIcon(R.drawable.ic_menu_info_details);
					loginProgress.setCancelable(true);
					loginProgress.setOnDismissListener(new OnDismissListener(){
						public void onDismiss(DialogInterface arg0) {
								if(success){
									dismiss();
									Log.d("Aptoide-DialogLogin", "Logged in");
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
					
					ViewServerLogin serverLogin =  new ViewServerLogin(username.getText().toString(), password.getText().toString());
					if(serverLogin.getPasshash() == null){
						serverLogin =  new ViewServerLogin(username.getText().toString(), password.getText().toString());
					}
					serverLogin.setRepoName(repository.getText().toString());
					if(privt.isChecked()){
						serverLogin.setRepoPrivate(priv_username.getText().toString(), priv_password.getText().toString());
					}

					Log.d("Aptoide-DialogLogin", "Logging in, login: "+serverLogin);
					new Login(getContext(), loginProgress, serverLogin).execute();
					
				}
			}
		});
		

		((Button)findViewById(R.id.new_account)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg) {
				success = false;
				if(username.getText().toString().trim().equals("")){
					Toast.makeText(DialogLogin.this.getContext(), DialogLogin.this.getContext().getString(R.string.no_username), Toast.LENGTH_SHORT).show();
				}else if(password.getText().toString().trim().equals("")){
					Toast.makeText(DialogLogin.this.getContext(), DialogLogin.this.getContext().getString(R.string.no_password), Toast.LENGTH_SHORT).show();
				}else if(repository.getText().toString().trim().equals("")){
					Toast.makeText(DialogLogin.this.getContext(), DialogLogin.this.getContext().getString(R.string.no_repository), Toast.LENGTH_SHORT).show();
				}else if(privt.isChecked() && priv_username.getText().toString().trim().equals("")){
					Toast.makeText(DialogLogin.this.getContext(), DialogLogin.this.getContext().getString(R.string.no_private_repo_username), Toast.LENGTH_SHORT).show();
				}else if(privt.isChecked() && priv_password.getText().toString().trim().equals("")){
					Toast.makeText(DialogLogin.this.getContext(), DialogLogin.this.getContext().getString(R.string.no_private_repo_password), Toast.LENGTH_SHORT).show();
				}else{
					
					serverLogin =  new ViewServerLogin(username.getText().toString(), password.getText().toString());
					if(serverLogin.getPasshash() == null){
						serverLogin =  new ViewServerLogin(username.getText().toString(), password.getText().toString());
					}
					serverLogin.setRepoName(repository.getText().toString());
					if(privt.isChecked()){
						serverLogin.setRepoPrivate(priv_username.getText().toString(), priv_password.getText().toString());
					}
					
//					(new DialogName(DialogLogin.this.getContext())).show();
					
					ProgressDialog createAccountProgress = ProgressDialog.show(getContext(), getContext().getString(R.string.new_account), getContext().getString(R.string.please_wait),true);
					createAccountProgress.setIcon(R.drawable.ic_menu_add);
					createAccountProgress.setCancelable(true);
					createAccountProgress.setOnDismissListener(new OnDismissListener(){
						public void onDismiss(DialogInterface arg0) {
								if(success){
									dismiss();
									Log.d("Aptoide-DialogLogin", "New User Created");
								}else{
									
								}
						}
					});
					
					Log.d("Aptoide-DialogLogin", "Creating new acocunt with login: "+serverLogin);
					new CreateAccount(getContext(), createAccountProgress, serverLogin).execute();
				}
			}
		});
		
		
		if(invoqueType.equals(InvoqueType.OVERRIDE_CREDENTIALS)){
			ViewServerLogin serverLogin = null;
			try {
				serverLogin = serviceDataCaller.callGetServerLogin();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			if(serverLogin != null){
				username.setText(serverLogin.getUsername());
				if(serverLogin.isRepoPrivate()){
					privt.setChecked(true);
					repository.setText(serverLogin.getRepoName());
					priv_username.setText(serverLogin.getPrivUsername());
					priv_password.setText(serverLogin.getPrivPassword());
				}
			}
		}
	}
	
	public class DialogName extends Dialog{

		public DialogName(Context context) {
			super(context);
		}
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.dialog_add_nickname);
			
			this.setTitle(R.string.name);
			
			final EditText nameBox = (EditText) findViewById(R.id.nickname);
			
			((Button)findViewById(R.id.submit)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					
					ProgressDialog createAccountProgress = ProgressDialog.show(getContext(), getContext().getString(R.string.new_account), getContext().getString(R.string.please_wait),true);
					createAccountProgress.setIcon(R.drawable.ic_menu_add);
					createAccountProgress.setCancelable(true);
					createAccountProgress.setOnDismissListener(new OnDismissListener(){
						public void onDismiss(DialogInterface arg0) {
								if(success){
									dismiss();
									Log.d("Aptoide-DialogLogin", "New User Created");
								}else{
									
								}
						}
					});
					
					serverLogin.setNickname(nameBox.getText().toString());
					Log.d("Aptoide-DialogLogin", "Creating new acocunt with login: "+serverLogin);
					new CreateAccount(getContext(), createAccountProgress, serverLogin).execute();
				}
			});
		}
		
	}

	
	public class Login extends AsyncTask<Void, Void, EnumServerLoginStatus>{
		
		private Context context;
		private ProgressDialog dialogProgress;
		private ViewServerLogin serverLogin;
		
		public Login(Context context, ProgressDialog dialogProgress, ViewServerLogin serverLogin) {
			this.context = context;
			this.dialogProgress = dialogProgress;
			this.serverLogin = serverLogin;
		}
		
		@Override
		protected EnumServerLoginStatus doInBackground(Void... args) {
			try {
				return EnumServerLoginStatus.reverseOrdinal(serviceDataCaller.callServerLogin(serverLogin));
			} catch (RemoteException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		protected void onPostExecute(EnumServerLoginStatus status) {
			if(status!=null){

				if(status.equals(EnumServerLoginStatus.SUCCESS)){
					success = true;
				}else{
					success = false;
					Toast.makeText(getContext(), status.toString(), Toast.LENGTH_SHORT).show();
				}
				
			}else{
				Toast.makeText(getContext(), context.getString(R.string.service_unavailable), Toast.LENGTH_SHORT).show();
			}
			dialogProgress.dismiss();
	    }
		
	}
	
	public class CreateAccount extends AsyncTask<Void, Void, EnumServerLoginCreateStatus>{
		
		private Context context;
		private ProgressDialog dialogProgress;
		private ViewServerLogin serverLogin;
		
		public CreateAccount(Context context, ProgressDialog dialogProgress, ViewServerLogin serverLogin) {
			this.context = context;
			this.dialogProgress = dialogProgress;
			this.serverLogin = serverLogin;
		}
		
		@Override
		protected EnumServerLoginCreateStatus doInBackground(Void... args) {
			try {
				return EnumServerLoginCreateStatus.reverseOrdinal(serviceDataCaller.callServerLoginCreate(serverLogin));
			} catch (RemoteException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		protected void onPostExecute(EnumServerLoginCreateStatus status) {
			if(status!=null){

				if(status.equals(EnumServerLoginCreateStatus.SUCCESS)){
					success = true;
				}else{
					success = false;
					Toast.makeText(getContext(), status.toString(), Toast.LENGTH_SHORT).show();
				}
				
			}else{
				Toast.makeText(getContext(), context.getString(R.string.service_unavailable), Toast.LENGTH_SHORT).show();
			}
			dialogProgress.dismiss();
	    }
		
	}
	
}
