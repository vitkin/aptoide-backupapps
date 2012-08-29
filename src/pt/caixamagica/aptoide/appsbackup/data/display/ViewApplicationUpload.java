/**
 * ViewDisplayApplication,		part of Aptoide's data model
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

package pt.caixamagica.aptoide.appsbackup.data.display;

import pt.caixamagica.aptoide.appsbackup.data.util.Constants;
import android.os.Parcel;
import android.os.Parcelable;

 /**
 * ViewDisplayApplication, models an Application
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ViewApplicationUpload implements Parcelable{

	protected int appHashid;
	protected String appName;
	

	protected ViewApplicationUpload(){
		
	}

	/**
	 * ViewDisplayApplication Constructor
	 *
	 * @param appHashid
	 * @param appName
	 */
	public ViewApplicationUpload(int appHashid, String appName) {
		this.appHashid = appHashid;
		this.appName = appName;
	}
	
	public int getAppHashid() {
		return this.appHashid;
	}

	public String getIconCachePath() {
		return Constants.PATH_CACHE_ICONS+this.appHashid;
	}

	public String getAppName() {
		return this.appName;
	}


	/**
	 * ViewDisplayApplication object reuse clean references
	 *
	 * @param String uri
	 */
	public void clean(){
		this.appHashid = Constants.EMPTY_INT;
		this.appName = null;
	}
	
	/**
	 * ViewDisplayApplication available object reuse reConstructor
	 *
	 * @param appHashid
	 * @param appName
	 */
	public void reuse(int appHashid, String appName, String versionName) {
		this.appHashid = appHashid;
		this.appName = appName;
	}


	@Override
	public int hashCode() {
		return this.appHashid;
	}


	@Override
	public boolean equals(Object object) {
		if(object instanceof ViewApplicationUpload){
			ViewApplicationUpload app = (ViewApplicationUpload) object;
			if(app.hashCode() == this.hashCode()){
				return true;
			}
		}
		return false;
	}


	@Override
	public String toString() {
		return " AppHashid: "+appHashid+" Name: "+appName;
	}
	
	
	
	// Parcelable stuff //
	
	
	public static final Parcelable.Creator<ViewApplicationUpload> CREATOR = new Parcelable.Creator<ViewApplicationUpload>() {
		public ViewApplicationUpload createFromParcel(Parcel in) {
			return new ViewApplicationUpload(in);
		}

		public ViewApplicationUpload[] newArray(int size) {
			return new ViewApplicationUpload[size];
		}
	};

	/** 
	 * we're annoyingly forced to create this even if we clearly don't need it,
	 *  so we just use the default return 0
	 *  
	 *  @return 0
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	protected ViewApplicationUpload(Parcel in){
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(appHashid);
		out.writeString(appName);
	}

	public void readFromParcel(Parcel in) {
		appHashid = in.readInt();
		appName = in.readString();
	}

}
