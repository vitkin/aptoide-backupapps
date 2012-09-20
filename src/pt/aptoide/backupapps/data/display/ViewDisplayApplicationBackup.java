/**
 * ViewDisplayApplicationBackup,		part of Aptoide's data model
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

package pt.aptoide.backupapps.data.display;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import pt.aptoide.backupapps.data.util.Constants;
import pt.aptoide.backupapps.ifaceutil.EnumAppStatus;
import android.os.Parcel;
import android.os.Parcelable;

 /**
 * ViewDisplayApplicationBackup, models a Backup Application
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ViewDisplayApplicationBackup extends ViewDisplayApplication{

	private long timestamp;
	private EnumAppStatus status;
	private int size;
	
	private boolean check = false;
	
	
	/**
	 * ViewDisplayApplication Backup Constructor
	 *
	 * @param appHashid
	 * @param appName
	 * @param installedVersionName
	 * @param timestamp
	 * @param size
	 * @param status
	 */
	public ViewDisplayApplicationBackup(int appHashid, String appName, String installedVersionName, long timestamp, int size, EnumAppStatus status) {
		super(appHashid, appName, installedVersionName);
		
		this.timestamp = timestamp;
		this.status = status;
		this.size = size;
	}
	
	public String getFormatedTimestamp(){
		return (new SimpleDateFormat("yyyy-MM-dd").format(new Date(timestamp)));
	}

	public EnumAppStatus getStatus() {
		return this.status;
	}

	public String getSize() {
		int M = Constants.KBYTES_TO_BYTES; //Base value is being stored as KB already
		
		if(this.size > M){
			return (this.size/M)+"MB";
		}else{
			return this.size+"KB";
		}
	}
	
	public boolean toggleCheck(){
		return (this.check = !this.check);
	}
	
	public boolean isChecked(){
		return this.check;
	}
	
	

	/**
	 * ViewDisplayApplication object reuse clean references
	 *
	 * @param String uri
	 */
	public void clean(){
		super.clean();
		
		this.timestamp = Constants.EMPTY_INT;
		this.status = null;
		this.size = Constants.EMPTY_INT;
		
		this.check = false;
	}
	
	/**
	 * ViewDisplayApplication Backup object reuse reConstructor
	 *
	 * @param appHashid
	 * @param appName
	 * @param installedVersionName
	 * @param isUpdatable
	 * @param isDowngradable
	 */
	public void reuse(int appHashid, String appName, String installedVersionName, int timestamp, EnumAppStatus status, int size) {
		super.reuse(appHashid, appName, installedVersionName);
		
		this.timestamp = timestamp;
		this.status = status;
		this.size = size;
	}


	@Override
	public String toString() {
		return super.toString()+" timestamp: "+getFormatedTimestamp()+" status: "+status+" size:"+getSize();
	}
	
	
	
	// Parcelable stuff //
	
	
	public static final Parcelable.Creator<ViewDisplayApplicationBackup> CREATOR = new Parcelable.Creator<ViewDisplayApplicationBackup>() {
		public ViewDisplayApplicationBackup createFromParcel(Parcel in) {
			return new ViewDisplayApplicationBackup(in);
		}

		public ViewDisplayApplicationBackup[] newArray(int size) {
			return new ViewDisplayApplicationBackup[size];
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

	private ViewDisplayApplicationBackup(Parcel in){
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(appHashid);
		out.writeString(appName);
		out.writeString(versionName);
		out.writeString(iconCachePath);
		out.writeLong(timestamp);
		out.writeInt(status.ordinal());
		out.writeInt(size);
		out.writeValue(check);
	}

	@Override
	public void readFromParcel(Parcel in) {
		appHashid = in.readInt();
		appName = in.readString();
		versionName = in.readString();
		iconCachePath = in.readString();
		timestamp = in.readLong();
		status = EnumAppStatus.reverseOrdinal(in.readInt());
		size = in.readInt();
		check = (Boolean) in.readValue(null);
	}

}
