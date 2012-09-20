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

package pt.aptoide.backupapps.data.webservices;

import java.io.Serializable;

public class ViewDevelopper implements Serializable {

	private static final long serialVersionUID = -8833964136770246360L;
	
//	private String username;
//	private String password;
	private String repository;
	
	private String token;
	
//	private boolean batchUploader = false;
	
	private String phone = null;
	private String eMail = null;
	private String webURL = null;
	
//	private boolean isNull;
		
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

//	public boolean isBatchUploader() {
//		return batchUploader;
//	}
//
//	public void setBatchUploader(boolean batchUpload) {
//		this.batchUploader = batchUpload;
//	}

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

//	public String getUsername() {
//		return username;
//	}
//
//	protected void setUsername(String username) {
//		this.username = username;
//	}
//
//	public String getPassword() {
//		return password;
//	}
//
//	protected void setPassword(String password) {
//		this.password = password;
//	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

//	public boolean isNull() {
//		return isNull;
//	}
//
//	protected void setNull(boolean isNull) {
//		this.isNull = isNull;
//	}
	
	public ViewDevelopper(String token, String repository) {
//		setUsername(username);
//		setPassword(password);
//		setNull(false);
		setToken(token);
		setRepository(repository);
	}
	
	@Override
	public String toString(){
		return //"username: \""+ getUsername() +"\" password: \""+ getPassword() +
				"\" repository: \""+ getRepository() +"\" phone: \""+ getPhone() 
			 +"\" e-mail: \""+ getEmail() +"\" web URL: \""+ getWebURL() +"\" token: \""+ getToken() 
			 //+"\" isBatchUploader: \""+isBatchUploader()+"\""
			 ;
	}

}
