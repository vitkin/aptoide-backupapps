/*
 * Aptoide Uploader		uploads android apps to yout Bazaar repository
 * Copyright (C) 20011  Duarte Silveira
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

package pt.caixamagica.aptoide.appsbackup.data.webservices;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.Parcel;
import android.os.Parcelable;


public class ViewApk implements Parcelable{

	private int appHashid;
	
	private String path;
	private String name;
	
	private String description;
	private String repository;
	private String category;
	private String rating;
	
	private String phone = null;
	private String eMail = null;
	private String webURL = null;
	
	private long size = 0;
	private AtomicBoolean isUploading;
	private int progress = 0;
	
//	private ArrayList<String> screenShotsPaths = new ArrayList<String>(5);
//	
//	
//	public ArrayList<String> getScreenShotsPaths() {
//		return screenShotsPaths;
//	}
//
//	public void setScreenShotsPaths(ArrayList<String> paths) {
//		this.screenShotsPaths = paths;
//	}
//	
//	public void addScreenShotPath(String path){
//		screenShotsPaths.add(path);
//	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}
	
	public int getAppHashid(){
		return appHashid;
	}

	
	
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return eMail;
	}

	public void setEmail(String email) {
		this.eMail = email;
	}

	public String getWebURL() {
		return webURL;
	}

	public void setWebURL(String webURL) {
		this.webURL = webURL;
	}
	
		

	
	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	public boolean isUploading(){
		return isUploading.get();
	}
	
	public void setUploading(boolean uploading){
		this.isUploading.set(uploading);
	}

	public int getProgress() {
		return progress;
	}
	
	public void resetProgress(){
		this.progress = 0;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}
	
	

	public ViewApk(int appHashid, String name, String path){
		this.appHashid = appHashid;
		this.path = path;
		this.name = name;
		this.isUploading = new AtomicBoolean(false);
	}
	
	@Override
	public String toString(){
		return "App hashid: \""+appHashid+"\" name: \""+ name +"\" description: \""+ description 
		   +"\" category: \""+ category +"\" rating: \""+ rating +"\" path: \""+ path +"\""+"Repository: \""+ repository +"\""
		   +"\" phone: \""+ getPhone() +"\" e-mail: \""+ getEmail() +"\" web URL: \""+ getWebURL();
	}
	
	
	
	// Parcelable stuff //
	
	
	public static final Parcelable.Creator<ViewApk> CREATOR = new Parcelable.Creator<ViewApk>() {
		public ViewApk createFromParcel(Parcel in) {
			return new ViewApk(in);
		}

		public ViewApk[] newArray(int size) {
			return new ViewApk[size];
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

	protected ViewApk(Parcel in){
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(appHashid);
		out.writeString(path);
		out.writeString(name);
		out.writeString(description);
		out.writeString(repository);
		out.writeString(category);
		out.writeString(rating);
		out.writeString(phone);
		out.writeString(eMail);
		out.writeString(webURL);
		out.writeLong(size);
	}

	public void readFromParcel(Parcel in) {
		appHashid = in.readInt();
		path = in.readString();
		name = in.readString();
		description = in.readString();
		repository = in.readString();
		category = in.readString();
		rating = in.readString();
		phone = in.readString();
		eMail = in.readString();
		webURL = in.readString();
		size = in.readLong();
	}
}
