/**
 * ManagerPreferences,		auxilliary class to Aptoide's ServiceData
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

package pt.aptoide.backupapps.data.preferences;

import java.util.UUID;

import pt.aptoide.backupapps.EnumAppsSorting;
import pt.aptoide.backupapps.R;
import pt.aptoide.backupapps.data.AptoideServiceData;
import pt.aptoide.backupapps.data.ViewClientStatistics;
import pt.aptoide.backupapps.data.model.ViewLogin;
import pt.aptoide.backupapps.data.system.ViewScreenDimensions;
import pt.aptoide.backupapps.data.util.Constants;
import pt.aptoide.backupapps.data.webservices.EnumIconDownloadsPermission;
import pt.aptoide.backupapps.data.webservices.ViewIconDownloadPermissions;
import pt.aptoide.backupapps.data.webservices.ViewServerLogin;
import pt.aptoide.backupapps.debug.AptoideLog;
import pt.aptoide.backupapps.debug.InterfaceAptoideLog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;

/**
 * ManagerPreferences, manages aptoide's preferences I/O
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ManagerPreferences implements InterfaceAptoideLog{
	
	private final String TAG = "Aptoide-ServiceData-ManagerPreferences";
	private SharedPreferences getPreferences;
	private SharedPreferences.Editor setPreferences;


	@Override
	public String getTag() {
		return TAG;
	}

	public ManagerPreferences(AptoideServiceData serviceData) {
		getPreferences = serviceData.getSharedPreferences(Constants.FILE_PREFERENCES, Context.MODE_PRIVATE);
		setPreferences = getPreferences.edit();
		AptoideLog.v(this, "gotSharedPreferences: "+Constants.FILE_PREFERENCES);
		if(getAptoideClientUUID() == null){
			createLauncherShortcut(serviceData.getApplicationContext());
			setAptoideClientUUID( UUID.randomUUID().toString() );
		}
	}
	
	private void createLauncherShortcut(Context context){
		Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
		shortcutIntent.setClassName(context, Constants.APTOIDE_CLASS_NAME);
		shortcutIntent.putExtra(Constants.APTOIDE_PACKAGE_NAME, context.getString(R.string.description));

		final Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.self_name));
		Parcelable iconResource = Intent.ShortcutIconResource.fromContext(context, R.drawable.icon);

		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
		intent.putExtra("duplicate", false);
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		context.sendBroadcast(intent);
	}

	
	public SharedPreferences getPreferences() {
		return getPreferences;
	}

	public SharedPreferences.Editor setPreferences() {
		return setPreferences;
	}

	
	private void setAptoideClientUUID(String uuid){
		setPreferences.putString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), uuid);
		setPreferences.commit();
	}
	
	public String getAptoideClientUUID(){
		return getPreferences.getString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), null);
	}
	
	public void setScreenDimensions(ViewScreenDimensions screenDimensions){
		setPreferences.putInt(EnumPreferences.SCREEN_WIDTH.name(), screenDimensions.getWidth());
		setPreferences.putInt(EnumPreferences.SCREEN_HEIGHT.name(), screenDimensions.getHeight());
		setPreferences.putFloat(EnumPreferences.SCREEN_DENSITY.name(), screenDimensions.getDensity());
		setPreferences.commit();
	}
	
	public ViewScreenDimensions getScreenDimensions(){
		return new ViewScreenDimensions(getPreferences.getInt(EnumPreferences.SCREEN_WIDTH.name(), Constants.NO_SCREEN), getPreferences.getInt(EnumPreferences.SCREEN_HEIGHT.name(), Constants.NO_SCREEN), getPreferences.getFloat(EnumPreferences.SCREEN_DENSITY.name(), Constants.NO_SCREEN));
	}
	
	public void completeStatistics(ViewClientStatistics statistics){
		statistics.completeStatistics(getAptoideClientUUID(), getScreenDimensions());
	}
	
	public boolean getShowApplicationsByCategory(){
		return getPreferences.getBoolean(EnumPreferences.SHOW_APPLICATIONS_BY_CATEGORY.name(), false);
	}
	
	public void setShowApplicationsByCategory(boolean byCategory){
		setPreferences.putBoolean(EnumPreferences.SHOW_APPLICATIONS_BY_CATEGORY.name(), byCategory);
		setPreferences.commit();
	}
	
	public boolean getShowSystemApplications(){
		return getPreferences.getBoolean(EnumPreferences.SHOW_SYSTEM_APPS.name(), false);
	}
	
	public void setShowSystemApplications(boolean show){
		setPreferences.putBoolean(EnumPreferences.SHOW_SYSTEM_APPS.name(), show);
		setPreferences.commit();
	}
	
	public int getAppsSortingPolicy(){
		return getPreferences.getInt(EnumPreferences.SORT_APPLICATIONS_BY.name(), EnumAppsSorting.ALPHABETIC.ordinal());
	}
	
	public void setAppsSortingPolicy(int sortingPolicy){
		setPreferences.putInt(EnumPreferences.SORT_APPLICATIONS_BY.name(), sortingPolicy);
		setPreferences.commit();
	}
	
	public void setHwFilter(boolean on){
		setPreferences.putBoolean(EnumPreferences.IS_HW_FILTER_ON.name(), on);
		setPreferences.commit();		
	}
	
	public boolean isHwFilterOn(){
		return getPreferences.getBoolean(EnumPreferences.IS_HW_FILTER_ON.name(), false);
	}
	
	public void setIconDownloadPermissions(ViewIconDownloadPermissions iconDownloadPermissions){
		setPreferences.putBoolean(EnumPreferences.DOWNLOAD_ICONS_.name()+EnumIconDownloadsPermission.WIFI.name(), iconDownloadPermissions.isWiFi());
		setPreferences.putBoolean(EnumPreferences.DOWNLOAD_ICONS_.name()+EnumIconDownloadsPermission.ETHERNET.name(), iconDownloadPermissions.isEthernet());
		setPreferences.putBoolean(EnumPreferences.DOWNLOAD_ICONS_.name()+EnumIconDownloadsPermission.WIMAX.name(), iconDownloadPermissions.isWiMax());
		setPreferences.putBoolean(EnumPreferences.DOWNLOAD_ICONS_.name()+EnumIconDownloadsPermission.MOBILE.name(), iconDownloadPermissions.isMobile());
		setPreferences.commit();		
	}
	
	public ViewIconDownloadPermissions getIconDownloadPermissions(){
		ViewIconDownloadPermissions permissions = new ViewIconDownloadPermissions(
													getPreferences.getBoolean(EnumPreferences.DOWNLOAD_ICONS_.name()+EnumIconDownloadsPermission.WIFI.name(), true)
													, getPreferences.getBoolean(EnumPreferences.DOWNLOAD_ICONS_.name()+EnumIconDownloadsPermission.ETHERNET.name(), true)
													, getPreferences.getBoolean(EnumPreferences.DOWNLOAD_ICONS_.name()+EnumIconDownloadsPermission.WIMAX.name(), true)
													, getPreferences.getBoolean(EnumPreferences.DOWNLOAD_ICONS_.name()+EnumIconDownloadsPermission.MOBILE.name(), true) );
		return permissions;
	}
	
	public void setAutomaticInstall(boolean on){
		setPreferences.putBoolean(EnumPreferences.AUTOMATIC_INSTALL.name(), on);
		setPreferences.commit();
	}
	
	public boolean isAutomaticInstallOn(){
		return getPreferences.getBoolean(EnumPreferences.AUTOMATIC_INSTALL.name(), false);
	}
	
	public void setAgeRating(EnumAgeRating rating){
		setPreferences.putInt(EnumPreferences.AGE_RATING.name(), rating.ordinal());
		setPreferences.commit();		
	}
	
	public EnumAgeRating getAgeRating(){
		return EnumAgeRating.reverseOrdinal(getPreferences.getInt(EnumPreferences.AGE_RATING.name(), EnumAgeRating.No_Filter.ordinal()));
	}
	
	
	public ViewSettings getSettings(){
		return new ViewSettings(getIconDownloadPermissions(), isHwFilterOn(), getAgeRating(), isAutomaticInstallOn());
	}
	
	//TODO move these next 3 values to database to support multiple servers
	
	public void setServerLogin(ViewLogin login){
		AptoideLog.v(this, "set serverLogin: "+login);
		setPreferences.putString(EnumPreferences.SERVER_USERNAME.name(), login.getUsername());
		setPreferences.putString(EnumPreferences.SERVER_PASSHASH.name(), login.getPassword());
		setPreferences.commit();				
	}
	
	public void clearServerLogin(){
		AptoideLog.v(this, "cleared serverLogin");
		setPreferences.remove(EnumPreferences.SERVER_USERNAME.name());
		setPreferences.remove(EnumPreferences.SERVER_PASSHASH.name());
		setPreferences.remove(EnumPreferences.SERVER_TOKEN.name());
		setPreferences.commit();
	}
	
	public ViewLogin getServerLogin(){
//		if(getPreferences.getString(EnumPreferences.SERVER_USERNAME.name(), null) == null && getPreferences.getString(EnumPreferences.SERVER_PASSHASH.name(), null) == null){
//			AptoideLog.v(this, "get serverLogin: username: "+getPreferences.getString(EnumPreferences.SERVER_USERNAME.name(), "")
//												+" password: "+getPreferences.getString(EnumPreferences.SERVER_PASSHASH.name(), ""));
//			return null; //TODO null object
//		}else{
			ViewLogin storedLogin = new ViewLogin( getPreferences.getString(EnumPreferences.SERVER_USERNAME.name(), "")
												, getPreferences.getString(EnumPreferences.SERVER_PASSHASH.name(), "") );
			AptoideLog.v(this, "get serverLogin: "+storedLogin);
			return storedLogin;
//		}
	}
	
	public void setServerToken(String token){
//		AptoideLog.v(this, "set serverToken: "+token);
		setPreferences.putString(EnumPreferences.SERVER_TOKEN.name(), token);
		setPreferences.commit();		
	}
	
	public String getToken(){
		return getPreferences.getString(EnumPreferences.SERVER_TOKEN.name(), null);
	}
	
	public void setServerInconsistentState(boolean inconsistent, String repoName, boolean isRepoPrivate){
		setPreferences.putBoolean(EnumPreferences.SERVER_INCONSISTENT.name(), inconsistent);
		setPreferences.commit();
		if(!inconsistent){
			setPreferences.remove(EnumPreferences.SERVER_NAME.name());
			setPreferences.remove(EnumPreferences.SERVER_PRIVATE.name());
			setPreferences.commit();
		}else{
			setInconsistentRepoName(repoName);
			setInconsistentRepoIsPrivate(isRepoPrivate);
		}
	}
	
	public boolean isServerInconsistenState(){
		return getPreferences.getBoolean(EnumPreferences.SERVER_INCONSISTENT.name(), false);
	}
	
	public void setInconsistentRepoName(String repoName){
		AptoideLog.v(this, "set InconsistentRepoName: "+repoName);
		setPreferences.putString(EnumPreferences.SERVER_NAME.name(), repoName);
		setPreferences.commit();		
	}
	
	private void setInconsistentRepoIsPrivate(boolean privat){
//		AptoideLog.v(this, "set InconsistentRepoIsPrivate: "+privat);
		setPreferences.putBoolean(EnumPreferences.SERVER_PRIVATE.name(), privat);
		setPreferences.commit();		
	}
	
	public String getInconsistentRepoName(){
		AptoideLog.v(this, "getInconsistentRepoName: "+getPreferences.getString(EnumPreferences.SERVER_NAME.name(), null));
		return getPreferences.getString(EnumPreferences.SERVER_NAME.name(), null);
	}
	
	public boolean isInconsistentRepoPrivate(){
		return getPreferences.getBoolean(EnumPreferences.SERVER_PRIVATE.name(), false);		
	}
	
	public void setServerInconsistentStore(ViewServerLogin serverLogin, String token){
		AptoideLog.v(this, "setServerInconsistentStore: "+serverLogin+" token: "+token);
		setServerLogin(serverLogin.getLogin());
		setServerToken(token);
		setServerInconsistentState(true, serverLogin.getRepoName(), serverLogin.isRepoPrivate());	
	}
	
	public ViewServerLogin getServerInconsistentStore(){
		ViewServerLogin serverLogin = new ViewServerLogin(getServerLogin());
		serverLogin.setRepoName(getInconsistentRepoName());
		if(isInconsistentRepoPrivate()){
			serverLogin.setRepoPrivate();
		}
		AptoideLog.v(this, "getServerInconsistentStore: "+serverLogin);
		return serverLogin;
	}
	
}
