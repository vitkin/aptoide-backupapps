/**
 * ViewUploadInfo,		part of Aptoide's data model
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

package pt.aptoide.appsbackup.data.webservices;

import android.os.Parcel;
import android.os.Parcelable;
import pt.aptoide.appsbackup.data.util.Constants;

 /**
 * ViewUploadInfo, models an apk's upload info
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ViewUploadInfo implements Parcelable{

	private String localPath;
	private String appName;
	private int appHashid;
	private long size;
	
	private String repoName = "";

	
	/**
	 * ViewUploadInfo Constructor
	 *
	 * @param localPath
	 * @param appName
	 * @param appHashid
	 */
	public ViewUploadInfo(String localPath, String appName, int appHashid) {
		this.localPath = localPath;
		this.appName = appName;
		this.appHashid = appHashid;
	}
	

	public String getLocalPath() {
		return localPath;
	}

	public String getAppName() {
		return appName;
	}

	public int getAppHashid() {
		return appHashid;
	}
	
	public void setRepoName(String repoName){
		this.repoName = repoName;
	}
	
	public String getRepoName() {
		return repoName;
	}
	
	


	public long getSize() {
		return size;
	}


	public void setSize(long size) {
		this.size = size;
	}


	@Override
	public String toString() {
		return "ViewDownloadInfo: "
				+" localPath: "+localPath
				+" appName: "+appName
				+" appHashid: "+appHashid
				+" repoName: "+repoName;
	}


	/**
	 * ViewUploadInfo object reuse, clean references
	 */
	public void clean(){
		this.localPath = null;
		this.appName = null;
		this.appHashid = Constants.EMPTY_INT;
		this.repoName = null;
	}

	/**
	 * ViewUploadInfo object reuse, reConstructor
	 *  
	 * @param localPath
	 * @param appName
	 * @param applicationHashid
	 */
	public void reuse(String localPath, String appName, int appHashid) {
		this.localPath = localPath;
		this.appName = appName;
		this.appHashid = appHashid;
	}
	
	
	
	// Parcelable stuff //
	
	
	public static final Parcelable.Creator<ViewUploadInfo> CREATOR = new Parcelable.Creator<ViewUploadInfo>() {
		public ViewUploadInfo createFromParcel(Parcel in) {
			return new ViewUploadInfo(in);
		}

		public ViewUploadInfo[] newArray(int size) {
			return new ViewUploadInfo[size];
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

	protected ViewUploadInfo(Parcel in){
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(appHashid);
		out.writeString(appName);
		out.writeString(localPath);
		out.writeString(repoName);
		out.writeLong(size);
	}

	public void readFromParcel(Parcel in) {
		appHashid = in.readInt();
		appName = in.readString();
		localPath = in.readString();
		repoName = in.readString();
		size = in.readLong();
	}
	
	
}
