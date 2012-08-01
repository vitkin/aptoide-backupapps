/**
 * ViewServerLogin,		auxilliary class to Aptoide's ServiceData
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

package pt.caixamagica.aptoide.appsbackup.data.webservices;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import pt.caixamagica.aptoide.appsbackup.data.util.Constants;
import pt.caixamagica.aptoide.appsbackup.data.util.Security;

import android.os.Parcel;
import android.os.Parcelable;


 /**
 * ViewServerLogin, models a server's authentication
 * 
 * @author dsilveira
 *
 */
public class ViewServerLogin implements Parcelable{

	private String username;
	private String passhash;
	private String repoName;
	private boolean repoIsPrivate;
	private String priv_username;
	private String priv_password;
	
	private String nickname;

	/**
	 * ViewLogin Constructor
	 *
	 * @param username
	 * @param passhash
	 */
	public ViewServerLogin(String username, String password) {
		this.username = username.toLowerCase();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA");
			md.update(password.getBytes());
			this.passhash = Security.byteArrayToHexString(md.digest());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.repoIsPrivate = false;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPasshash() {
		return this.passhash;
	}
	
	public void setRepoName(String repoName){
		this.repoName = repoName;
	}
	
	public String getRepoName(){
		return this.repoName;
	}
	
	public String getRepoUri(){
		return Constants.SCHEME_HTTP_PREFIX+this.repoName+Constants.DOMAIN_APTOIDE_STORE;
	}
	
	public void setRepoPrivate(String priv_username, String priv_password){
		this.repoIsPrivate = true;
		this.priv_username = priv_username;
		this.priv_password = priv_password;
	}
	
	public boolean isRepoPrivate(){
		return repoIsPrivate;
	}
	
	public String getPrivUsername(){
		return this.priv_username;
	}
	
	public String getPrivPassword(){
		return this.priv_password;
	}
	

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	

	@Override
	public String toString() {
		return "Username: "+username+" Passhash: "+passhash+" RepoName: "+repoName+" repoIsPrivate: "+repoIsPrivate;
	}
	
	
	
	// Parcelable stuff //
	
	
	public static final Parcelable.Creator<ViewServerLogin> CREATOR = new
			Parcelable.Creator<ViewServerLogin>() {

		@Override
		public ViewServerLogin createFromParcel(Parcel in) {
			return new ViewServerLogin(in);
		}

		public ViewServerLogin[] newArray(int size) {
			return new ViewServerLogin[size];
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

	private ViewServerLogin(Parcel in){
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(this.username);
		out.writeString(this.passhash);
		out.writeString(this.repoName);
		out.writeValue(this.repoIsPrivate);
		out.writeString(this.priv_username);
		out.writeString(this.priv_password);
		out.writeString(this.nickname);
	}

	public void readFromParcel(Parcel in) {
		this.username = in.readString();
		this.passhash = in.readString();
		this.repoName = in.readString();
		this.repoIsPrivate = (Boolean) in.readValue(null);
		this.priv_username = in.readString();
		this.priv_password = in.readString();
		this.nickname = in.readString();
	}
	
}
