/**
 * ViewApplication,		part of Aptoide's data model
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

package pt.aptoide.backupapps.data.model;

import pt.aptoide.backupapps.data.util.Constants;
import android.content.ContentValues;

 /**
 * ViewApplication, models an application
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ViewApplication {

	private ContentValues values;
	private int categoryHashid;
	
	
	/**
	 * ViewApplication constructor
	 * 
	 * @param String applicationName
	 * @param String packageName
	 * @param String versionName
	 * @param int versionCode
	 * @param boolean isTypeInstalled
	 * 
	 */
	public ViewApplication(String applicationName, String packageName, String versionName, int versionCode, boolean isTypeInstalled) {
		this(packageName, versionCode, isTypeInstalled);
		setVersionName(versionName);
		setApplicationName(applicationName);
	}
	
	/**
	 * ViewApplication embrio constructor
	 * 
	 * @param String packageName
	 * @param int versionCode
	 * @param boolean isTypeInstalled
	 * 
	 */
	public ViewApplication(String packageName, int versionCode, boolean isTypeInstalled) {
		if(isTypeInstalled){
			this.values = new ContentValues(Constants.NUMBER_OF_COLUMNS_APP_INSTALLED);
		}else{
			this.values = new ContentValues(Constants.NUMBER_OF_COLUMNS_APPLICATION);			
		}
		setPackageName(packageName);
		setVersionCode(versionCode);
		setAppHashid(packageName, versionCode);
	}
	
	
	private void setPackageName(String packageName){
		values.put(Constants.KEY_APPLICATION_PACKAGE_NAME, packageName);		
	}
	
	public String getPackageName(){
		return values.getAsString(Constants.KEY_APPLICATION_PACKAGE_NAME);
	}
	
	private void setVersionCode(int versionCode){
		values.put(Constants.KEY_APPLICATION_VERSION_CODE, versionCode);
	}
	
	public int getVersionCode(){
		return values.getAsInteger(Constants.KEY_APPLICATION_VERSION_CODE);
	}
	
	private void setAppHashid(String packageName, int versionCode){
		values.put(Constants.KEY_APPLICATION_HASHID, (packageName+'|'+versionCode).hashCode());
	}
	
	public int getHashid() {
		return values.getAsInteger(Constants.KEY_APPLICATION_HASHID);
	}
	
	public void setVersionName(String versionName){
		values.put(Constants.KEY_APPLICATION_VERSION_NAME, versionName);		
	}
	
	public String getVersionName(){
		return values.getAsString(Constants.KEY_APPLICATION_VERSION_NAME);
	}
	
	public void setApplicationName(String applicationName){
		values.put(Constants.KEY_APPLICATION_NAME, applicationName);		
	}
	
	public String getApplicationName(){
		return values.getAsString(Constants.KEY_APPLICATION_NAME);
	}
	
	public void setTimestamp(long timestamp){
		values.put(Constants.KEY_APPLICATION_TIMESTAMP, timestamp);
	}
	
	public long getTimestamp(){
		return values.getAsLong(Constants.KEY_APPLICATION_TIMESTAMP);
	}
	
	public void setMinScreen(int minScreen){
		values.put(Constants.KEY_APPLICATION_MIN_SCREEN, minScreen);
	}
	
	public int getMinScreen(){
		return values.getAsInteger(Constants.KEY_APPLICATION_MIN_SCREEN);
	}
	
	public void setMinSdk(int minSdk){
		values.put(Constants.KEY_APPLICATION_MIN_SDK, minSdk);
	}
	
	public int getMinSdk(){
		return values.getAsInteger(Constants.KEY_APPLICATION_MIN_SDK);
	}
	
	public void setMinGles(float minGles){
		values.put(Constants.KEY_APPLICATION_MIN_SDK, minGles);
	}
	
	public float getMinGles(){
		return values.getAsFloat(Constants.KEY_APPLICATION_MIN_GLES);
	}
	
	public void setRating(int rating){
		values.put(Constants.KEY_APPLICATION_RATING, rating);
	}
	
	public int getRating() {
		return values.getAsInteger(Constants.KEY_APPLICATION_RATING);
	}
	
	/**
	 * setRepoHashid, sets this application's repo hashid
	 * 
	 * @param int repoHashid, repoUri.hashCode()
	 */
	public void setRepoHashid(int repoHashid){
		values.put(Constants.KEY_APPLICATION_REPO_HASHID, repoHashid);
		values.put(Constants.KEY_APPLICATION_FULL_HASHID, (getHashid()+"|"+repoHashid).hashCode());
	}
	
	public int getRepoHashid(){
		return values.getAsInteger(Constants.KEY_APPLICATION_REPO_HASHID);
	}
	
	public int getFullHashid(){
		return values.getAsInteger(Constants.KEY_APPLICATION_FULL_HASHID);
	}	
	
	/**
	 * setCategoryHashid, sets this application's category hashid
	 * 
	 * @param int categoryHashid, categoryName.hashCode()
	 */
	public void setCategoryHashid(int categoryHashid){
		this.categoryHashid = categoryHashid;
	}
	
	public int getCategoryHashid(){
		return this.categoryHashid;
	}
	
	
	public ContentValues getValues(){
		return this.values;
	}

	

	/**
	 * ViewApplication object reuse, clean references
	 */
	public void clean(){
		this.values = null;
		this.categoryHashid = Constants.EMPTY_INT;
	}
	
	/**
	 * ViewApplication object reuse, reConstructor
	 * 
	 * @param String applicationName
	 * @param String packageName
	 * @param String versionName
	 * @param int versionCode
	 * @param boolean isTypeInstalled
	 * 
	 */
	public void reuse(String applicationName, String packageName, String versionName, int versionCode, boolean isTypeInstalled) {
		if(isTypeInstalled){
			this.values = new ContentValues(Constants.NUMBER_OF_COLUMNS_APP_INSTALLED);
		}else{
			this.values = new ContentValues(Constants.NUMBER_OF_COLUMNS_APPLICATION);			
		}
		setPackageName(packageName);
		setVersionCode(versionCode);
		setAppHashid(packageName, versionCode);
		setVersionName(versionName);
		setApplicationName(applicationName);
	}
	
	/**
	 * ViewApplication object reuse, reConstructor
	 * 
	 * @param String packageName
	 * @param int versionCode
	 * @param boolean isTypeInstalled
	 * 
	 */
	public void reuse(String packageName, int versionCode, boolean isTypeInstalled) {
		if(isTypeInstalled){
			this.values = new ContentValues(Constants.NUMBER_OF_COLUMNS_APP_INSTALLED);
		}else{
			this.values = new ContentValues(Constants.NUMBER_OF_COLUMNS_APPLICATION);			
		}
		setPackageName(packageName);
		setVersionCode(versionCode);
		setAppHashid(packageName, versionCode);
	}


	@Override
	public int hashCode() {
		return this.getHashid();
	}


	@Override
	public boolean equals(Object object) {
		if(object instanceof ViewApplication){
			ViewApplication app = (ViewApplication) object;
			if(app.hashCode() == this.hashCode()){
				return true;
			}
		}
		return false;
	}


	@Override
	public String toString() {
		return this.getApplicationName()+" v"+getVersionName();
	}
	
	public String toStringDetails(){
		String details = toString()
				+" versionCode: "+getVersionCode()
				+" package: "+getPackageName();
		try {
			details +=" category: "+getCategoryHashid();
		} catch (Exception e) { }
		try {
			details +=" repo: "+getRepoHashid();
		} catch (Exception e) { }
		try {
			details +=" timestamp: "+getTimestamp();
		} catch (Exception e) { }
		try {
			details +=" rating: "+getRating();
		} catch (Exception e) { }
		try {
			details +=" min_screen: "+getMinScreen();
		} catch (Exception e) { }
		try {
			details +=" min_sdk: "+getMinSdk();
		} catch (Exception e) { }
		try {
			details +=" min_gles: "+getMinGles();
		} catch (Exception e) { }
		
		return details;
	}
	
}
