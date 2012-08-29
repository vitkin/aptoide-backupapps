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

import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumServerUploadApkStatus;
import android.os.Parcel;
import android.os.Parcelable;

 /**
 * ViewDisplayApplication, models an Application
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ViewApplicationUploadFailed extends ViewApplicationUpload{

	protected EnumServerUploadApkStatus uploadStatus;
		

	protected ViewApplicationUploadFailed(){
		
	}

	/**
	 * ViewDisplayApplication Constructor
	 *
	 * @param appHashid
	 * @param appName
	 */
	public ViewApplicationUploadFailed(int appHashid, String appName, EnumServerUploadApkStatus uploadStatus) {
		super(appHashid, appName);
		this.uploadStatus = uploadStatus;
	}

	public EnumServerUploadApkStatus getUploadStatus() {
		return uploadStatus;
	}

	public void setUploadStatus(EnumServerUploadApkStatus uploadStatus) {
		this.uploadStatus = uploadStatus;
	}

	@Override
	public boolean equals(Object object) {
		if(object instanceof ViewApplicationUploadFailed){
			ViewApplicationUploadFailed app = (ViewApplicationUploadFailed) object;
			if(app.hashCode() == this.hashCode()){
				return true;
			}
		}
		return false;
	}
	
	
	
	// Parcelable stuff //
	
	
	public static final Parcelable.Creator<ViewApplicationUploadFailed> CREATOR = new Parcelable.Creator<ViewApplicationUploadFailed>() {
		public ViewApplicationUploadFailed createFromParcel(Parcel in) {
			return new ViewApplicationUploadFailed(in);
		}

		public ViewApplicationUploadFailed[] newArray(int size) {
			return new ViewApplicationUploadFailed[size];
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

	protected ViewApplicationUploadFailed(Parcel in){
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeInt(uploadStatus.ordinal());
	}

	public void readFromParcel(Parcel in) {
		super.readFromParcel(in);
		uploadStatus = EnumServerUploadApkStatus.reverseOrdinal(in.readInt());
	}

}
