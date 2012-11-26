/**
 * ParserDOMSmallRequests, 	auxiliary class to Aptoide's ServiceData
 * Copyright (C) 2011 Duarte Silveira
 * duarte.silveira@caixamagica.pt
 * 
 * derivative work of previous Aptoide's RssHandler with
 * Copyright (C) 2009  Roberto Jacinto
 * roberto.jacinto@caixamagica.pt
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

package pt.aptoide.backupapps.data.xml;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.PreparedStatement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pt.aptoide.backupapps.data.model.ViewAppDownloadInfo;
import pt.aptoide.backupapps.data.webservices.EnumServerAddAppVersionCommentStatus;
import pt.aptoide.backupapps.data.webservices.EnumServerAddAppVersionLikeStatus;
import pt.aptoide.backupapps.data.webservices.EnumServerLoginCreateStatus;
import pt.aptoide.backupapps.data.webservices.EnumServerLoginStatus;
import pt.aptoide.backupapps.data.webservices.EnumServerStatus;
import pt.aptoide.backupapps.data.webservices.EnumServerUploadApkStatus;
import pt.aptoide.backupapps.data.webservices.ViewDownload;

import android.util.Log;


/**
 * ParserDOMSmallRequests, handles small requests xml DOM parsing
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ParserDOMSmallRequests{
	private ManagerXml managerXml = null;
	
		
	public ParserDOMSmallRequests(ManagerXml managerXml){
		this.managerXml = managerXml;
	}
	
	public EnumServerLoginCreateStatus parseServerLoginCreateReturn(HttpURLConnection connection) throws ParserConfigurationException, SAXException, IOException{
		EnumServerLoginCreateStatus status = EnumServerLoginCreateStatus.LOGIN_CREATE_SERVICE_UNAVAILABLE;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
		DocumentBuilder builder = factory.newDocumentBuilder();
        Document dom = builder.parse( connection.getInputStream() );
        dom.getDocumentElement().normalize();
        NodeList receivedStatusList = dom.getElementsByTagName(EnumXmlTagsServerLogin.status.toString());
        if(receivedStatusList.getLength()>0){
        	Node receivedStatus = receivedStatusList.item(0);
        	Log.d("Aptoide-ManagerUploads login create", receivedStatus.getNodeName());
        	Log.d("Aptoide-ManagerUploads login create", receivedStatus.getFirstChild().getNodeValue().trim());
        	if(receivedStatus.getFirstChild().getNodeValue().trim().equals("OK")){
        		status = EnumServerLoginCreateStatus.SUCCESS;
        	}else{
        		NodeList receivedErrorsList = dom.getElementsByTagName(EnumXmlTagsServerLogin.entry.toString());
    	        if(receivedErrorsList.getLength()>0){
    	        	Node receivedErrors = receivedErrorsList.item(0);
    	        	String error = receivedErrors.getFirstChild().getNodeValue();
    	        	Log.d("Aptoide-ManagerUploads login create", receivedErrors.getNodeName());
    	        	Log.d("Aptoide-ManagerUploads login create", receivedErrors.getFirstChild().getNodeValue().trim());
    	        	if(error.equals("Missing email parameter")
    	        		|| error.equals("Missing passhash parameter")
    	        		|| error.equals("Missing hmac parameter")
    	        		|| error.equals("Missing name parameter")
    	        		|| error.equals("Missing user-agent")){
    	        		status = EnumServerLoginCreateStatus.MISSING_PARAMETER;
    	        	}else if(error.equals("Invalid email format")
    	        		|| error.equals("Invalid passhash format")){
    	        		status = EnumServerLoginCreateStatus.BAD_LOGIN;
    	        	}else if(error.equals("Invalid hmac format")
    	        		|| error.equals("HMAC Authentication failure")){
    	        		status = EnumServerLoginCreateStatus.BAD_HMAC;
    	        	}else if(error.equals("The email provided already exists in the system")){
    	        		status = EnumServerLoginCreateStatus.USERNAME_ALREADY_REGISTERED;
    	        	}else if(error.equals("The email provided does not exist in the system yet")){
    	        		status = EnumServerLoginCreateStatus.UNKNOWN_USERNAME;
    	        	}else if(error.equals("User authentication failed")){
    	        		status = EnumServerLoginCreateStatus.BAD_LOGIN;
    	        	}else if(error.equals("That store name is invalid, you can only use letters, numbers or dashes.")
    	        		|| error.equals("That store name must be at least 3 characters long.")){
    	        		status = EnumServerLoginCreateStatus.BAD_REPO_NAME;
    	        	}else if(error.equals("You have to enter the username and password of the store.")){
    	        		status = EnumServerLoginCreateStatus.REPO_REQUIRES_AUTHENTICATION;
    	        	}else if(error.equals("That store name is already taken, you need to choose another one.")){
    	        		status = EnumServerLoginCreateStatus.REPO_ALREADY_EXISTS;
    	        	}else if(error.equals("The store could not be created. Please try again.")){
    	        		status = EnumServerLoginCreateStatus.SERVER_ERROR;
    	        	}
    	        }
        	}
        }
        
    return status;
	}
	
	public EnumServerLoginStatus parseServerConnectionCheckReturn(HttpURLConnection connection) throws ParserConfigurationException, SAXException, IOException{
		EnumServerLoginStatus status = EnumServerLoginStatus.REPO_SERVICE_UNAVAILABLE;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
		DocumentBuilder builder = factory.newDocumentBuilder();
        Document dom = builder.parse( connection.getInputStream() );
        dom.getDocumentElement().normalize();
        NodeList receivedErrorsList = dom.getElementsByTagName("error");
        Log.d("Aptoide-ManagerUploads checkServerConnection", "received errors: "+receivedErrorsList.getLength());
        if(receivedErrorsList.getLength()>0){
        	Node receivedError = receivedErrorsList.item(0);
        	String error = receivedError.getFirstChild().getNodeValue();
        	Log.d("Aptoide-ManagerUploads checkServerConnection", receivedError.getNodeName());
        	Log.d("Aptoide-ManagerUploads checkServerConnection", receivedError.getFirstChild().getNodeValue().trim());
        	if(error.startsWith("Invalid repo:")){
        		status = EnumServerLoginStatus.REPO_SERVICE_UNAVAILABLE;
        	}else if(error.startsWith("Private repository:")){
        		status = EnumServerLoginStatus.BAD_REPO_PRIVACY_LOGIN;
        	}
        }else{
        	status = EnumServerLoginStatus.SUCCESS;
        }
        
        return status;
	}
	
	public EnumServerLoginStatus parseServerLoginReturn(HttpURLConnection connection) throws ParserConfigurationException, SAXException, IOException{
		EnumServerLoginStatus status = EnumServerLoginStatus.LOGIN_SERVICE_UNAVAILABLE;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
		DocumentBuilder builder = factory.newDocumentBuilder();
        Document dom = builder.parse( connection.getInputStream() );
        dom.getDocumentElement().normalize();
        NodeList receivedStatusList = dom.getElementsByTagName(EnumXmlTagsServerLogin.status.toString());
        if(receivedStatusList.getLength()>0){
        	Node receivedStatus = receivedStatusList.item(0);
        	Log.d("Aptoide-ManagerUploads login", receivedStatus.getNodeName());
        	Log.d("Aptoide-ManagerUploads login", receivedStatus.getFirstChild().getNodeValue().trim());
        	if(receivedStatus.getFirstChild().getNodeValue().trim().equals("OK")){
        		status = EnumServerLoginStatus.SUCCESS;
        	}else{
        		NodeList receivedErrorsList = dom.getElementsByTagName(EnumXmlTagsServerLogin.entry.toString());
    	        if(receivedErrorsList.getLength()>0){
    	        	Node receivedErrors = receivedErrorsList.item(0);
    	        	String error = receivedErrors.getFirstChild().getNodeValue();
    	        	Log.d("Aptoide-ManagerUploads login", receivedErrors.getNodeName());
    	        	Log.d("Aptoide-ManagerUploads login", receivedErrors.getFirstChild().getNodeValue().trim());
    	        	if(error.equals("Missing authentication parameter(s): user and/or passhash")){
//    	        		status = EnumServerLoginStatus.MISSING_PARAMETER;
    	        		status = EnumServerLoginStatus.BAD_LOGIN;
    	        	}else if(error.equals("Invalid login credentials")){
    	        		status = EnumServerLoginStatus.BAD_LOGIN;
    	        	}else if(error.equals("The provided store does not exist.")){
    	        		status = EnumServerLoginStatus.REPO_SERVICE_UNAVAILABLE;
    	        	}
    	        }
        	}
        }
        if(status.equals(EnumServerLoginStatus.SUCCESS)){
        	String token = null;
        	NodeList receivedTokenList = dom.getElementsByTagName(EnumXmlTagsServerLogin.token.toString());
	        if(receivedTokenList.getLength()>0){
	        	Node receivedToken = receivedTokenList.item(0);
	        	token = receivedToken.getFirstChild().getNodeValue().trim();
	        	Log.d("Aptoide-ManagerUploads login", receivedToken.getNodeName());
	        	Log.d("Aptoide-ManagerUploads login", receivedToken.getFirstChild().getNodeValue().trim());
	        }
	        managerXml.serviceData.getManagerPreferences().setServerToken(token);
	        String repoName = null;
        	NodeList receivedRepoNameList = dom.getElementsByTagName(EnumXmlTagsServerLogin.repo.toString());
	        if(receivedRepoNameList.getLength()>0){
	        	Node receivedRepoName = receivedRepoNameList.item(0);
	        	repoName = receivedRepoName.getFirstChild().getNodeValue().trim();
	        	Log.d("Aptoide-ManagerUploads login", receivedRepoName.getNodeName());
	        	Log.d("Aptoide-ManagerUploads login", receivedRepoName.getFirstChild().getNodeValue().trim());
	        	managerXml.serviceData.getManagerPreferences().setInconsistentRepoName(repoName);
	        }
        }
    return status;
	}
	
	public EnumServerAddAppVersionLikeStatus parseAddAppVersionLikeReturn(HttpURLConnection connection) throws ParserConfigurationException, SAXException, IOException{
		EnumServerAddAppVersionLikeStatus status = EnumServerAddAppVersionLikeStatus.SERVICE_UNAVAILABLE;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
		DocumentBuilder builder = factory.newDocumentBuilder();
        Document dom = builder.parse( connection.getInputStream() );
        dom.getDocumentElement().normalize();
        NodeList receivedStatusList = dom.getElementsByTagName(EnumXmlTagsServerLogin.status.toString());
        if(receivedStatusList.getLength()>0){
        	Node receivedStatus = receivedStatusList.item(0);
        	Log.d("Aptoide-ManagerUploads addLike", receivedStatus.getNodeName());
        	Log.d("Aptoide-ManagerUploads addLike", receivedStatus.getFirstChild().getNodeValue().trim());
        	if(receivedStatus.getFirstChild().getNodeValue().trim().equals("OK")){
        		status = EnumServerAddAppVersionLikeStatus.SUCCESS;
        	}else{
        		NodeList receivedErrorsList = dom.getElementsByTagName(EnumXmlTagsServerLogin.entry.toString());
    	        if(receivedErrorsList.getLength()>0){
    	        	Node receivedErrors = receivedErrorsList.item(0);
    	        	String error = receivedErrors.getFirstChild().getNodeValue();
    	        	Log.d("Aptoide-ManagerUploads addLike", receivedErrors.getNodeName());
    	        	Log.d("Aptoide-ManagerUploads addLike", receivedErrors.getFirstChild().getNodeValue().trim());
    	        	if(error.equals("Missing authentication parameter(s): token or user&passhash")
    	        		|| error.equals("Missing repo parameter")
    	        		|| error.equals("Missing apkid parameter")
    	        		|| error.equals("Missing apkversion parameter")
    	        		|| error.equals("Missing like parameter")){
    	        		status = EnumServerAddAppVersionLikeStatus.MISSING_PARAMETER;
    	        	}else if(error.equals("Unknown token")
    	        		|| error.equals("Invalid login credentials")){
    	        		status = EnumServerAddAppVersionLikeStatus.BAD_TOKEN;
    	        	}else if(error.equals("Invalid repo!")){
    	        		status = EnumServerAddAppVersionLikeStatus.BAD_REPO;
    	        	}else if(error.equals("No apk was found with the given apphashid.")
    	        		|| error.equals("No apk was found with the given apkid and apkversion.")){
    	        		status = EnumServerAddAppVersionLikeStatus.BAD_APP_HASHID;
    	        	}
    	        }
        	}
        }
        return status;
	}
	
	public EnumServerAddAppVersionCommentStatus parseAddAppVersionCommentReturn(HttpURLConnection connection) throws ParserConfigurationException, SAXException, IOException{
		EnumServerAddAppVersionCommentStatus status = EnumServerAddAppVersionCommentStatus.SERVICE_UNAVAILABLE;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
		DocumentBuilder builder = factory.newDocumentBuilder();
        Document dom = builder.parse( connection.getInputStream() );
        dom.getDocumentElement().normalize();
        NodeList receivedStatusList = dom.getElementsByTagName(EnumXmlTagsServerLogin.status.toString());
        if(receivedStatusList.getLength()>0){
        	Node receivedStatus = receivedStatusList.item(0);
        	Log.d("Aptoide-ManagerUploads addComment", receivedStatus.getNodeName());
        	Log.d("Aptoide-ManagerUploads addComment", receivedStatus.getFirstChild().getNodeValue().trim());
        	if(receivedStatus.getFirstChild().getNodeValue().trim().equals("OK")){
        		status = EnumServerAddAppVersionCommentStatus.SUCCESS;
        	}else{
        		NodeList receivedErrorsList = dom.getElementsByTagName(EnumXmlTagsServerLogin.entry.toString());
    	        if(receivedErrorsList.getLength()>0){
    	        	Node receivedErrors = receivedErrorsList.item(0);
    	        	String error = receivedErrors.getFirstChild().getNodeValue();
    	        	Log.d("Aptoide-ManagerUploads addComment", receivedErrors.getNodeName());
    	        	Log.d("Aptoide-ManagerUploads addComment", receivedErrors.getFirstChild().getNodeValue().trim());
    	        	if(error.equals("Missing authentication parameter(s): token or user&passhash")
    	        		|| error.equals("Missing repo parameter")
    	        		|| error.equals("Missing apkid parameter")
    	        		|| error.equals("Missing apkversion parameter")
    	        		|| error.equals("Missing like parameter")){
    	        		status = EnumServerAddAppVersionCommentStatus.MISSING_PARAMETER;
    	        	}else if(error.equals("Unknown token")
    	        		|| error.equals("Invalid login credentials")){
    	        		status = EnumServerAddAppVersionCommentStatus.BAD_TOKEN;
    	        	}else if(error.equals("Invalid repo!")){
    	        		status = EnumServerAddAppVersionCommentStatus.BAD_REPO;
    	        	}else if(error.equals("No apk was found with the given apphashid.")
    	        		|| error.equals("No apk was found with the given apkid and apkversion.")){
    	        		status = EnumServerAddAppVersionCommentStatus.BAD_APP_HASHID;
    	        	}
    	        }
        	}
        }
		return status;
	}

	
	public EnumServerUploadApkStatus parseApkUploadXml(HttpURLConnection connection) throws ParserConfigurationException, SAXException, IOException{
		EnumServerUploadApkStatus status = EnumServerUploadApkStatus.SERVER_ERROR;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
		DocumentBuilder builder = factory.newDocumentBuilder();
        Document dom = builder.parse( connection.getInputStream() );
        dom.getDocumentElement().normalize();
        NodeList receivedStatusList = dom.getElementsByTagName(EnumXmlTagsServerLogin.status.toString());
        if(receivedStatusList.getLength()>0){
        	Node receivedStatus = receivedStatusList.item(0);
        	Log.d("Aptoide-ManagerUploads uploadApk", receivedStatus.getNodeName()+":  "+receivedStatus.getFirstChild().getNodeValue().trim());
        	if(receivedStatus.getFirstChild().getNodeValue().trim().equals("OK")){
        		status = EnumServerUploadApkStatus.SUCCESS;
        	}else{
        		NodeList receivedErrorsList = dom.getElementsByTagName(EnumXmlTagsServerLogin.entry.toString());
    	        if(receivedErrorsList.getLength()>0){
    	        	Node receivedErrors = receivedErrorsList.item(0);
    	        	String error = receivedErrors.getFirstChild().getNodeValue();
    	        	Log.d("Aptoide-ManagerUploads uploadApk", receivedErrors.getNodeName()+":  "+receivedErrors.getFirstChild().getNodeValue().trim());
    	        	
					if(error.equals("Missing token parameter")){
						status = EnumServerUploadApkStatus.MISSING_TOKEN;
					}else if(error.equals("You need to upload an APK") 
						||error.equals("Missing apk parameter")){
						status = EnumServerUploadApkStatus.MISSING_APK;
					}else if(error.equals("Missing apkname parameter")){
						status = EnumServerUploadApkStatus.MISSING_APK_NAME;
					}else if(error.equals("Missing description parameter")){
						status = EnumServerUploadApkStatus.MISSING_DESCRIPTION;
					}else if(error.equals("Missing rating parameter")){
						status = EnumServerUploadApkStatus.MISSING_RATING;
					}else if(error.equals("Missing category parameter")){
						status = EnumServerUploadApkStatus.MISSING_CATEGORY;
					}else if(error.equals("Invalid token!")){
						status = EnumServerUploadApkStatus.BAD_TOKEN;
					}else if(error.equals("Invalid repo!")){
						status = EnumServerUploadApkStatus.BAD_REPO;
					}else if( error.equals("An invalid APK was received, does not seem to contain all required data. Please verify that you selected the right file, and try again.")
						|| error.equals("An invalid APK was received, does not seem to contain data about the name, version code or version name. Please verify that you selected the right file, and try again.")
						|| error.equals("An invalid APK was received, does not seem to be a ZIP file or seems to have some errors. Please verify that you selected the right file, and try again.")
						|| error.equals("An invalid APK was received, does not seem to contain data about the label or icon. Please verify that you selected the right file, and try again.")
						|| error.equals("An invalid APK was received. Please verify that you selected the right file, and try again.") ){
						status = EnumServerUploadApkStatus.BAD_APK;
					}else if(error.equals("Invalid rating!")){
						status = EnumServerUploadApkStatus.BAD_RATING;
					}else if(error.equals("Invalid category!")){
						status = EnumServerUploadApkStatus.BAD_CATEGORY;
					}else if(error.equals("The website is not a valid URL.")){
						status = EnumServerUploadApkStatus.BAD_WEBSITE;
					}else if(error.equals("The e-mail address is not valid.")){
						status = EnumServerUploadApkStatus.BAD_EMAIL;
					}else if(error.equals("Token doesn't match with Repo.")){
						status = EnumServerUploadApkStatus.TOKEN_INCONSISTENT_WITH_REPO;
					}else if(error.equals("Unable to upload the apk. Please try again.") 
						|| error.equals("The file transfer stopped before the upload was complete. Please try again.")
						|| error.equals("The file transfer failed for some unknown reason. Please try again.") ){
						status = EnumServerUploadApkStatus.BAD_APK_UPLOAD;
					}else if(error.equals("The file you uploaded exceeds the maximum allowed size.")
						|| error.equals("The file you uploaded exceeds the maximum size") ){
						status = EnumServerUploadApkStatus.APK_TOO_BIG;
					}else if(error.equals("MD5 NOT existent")){
						status = EnumServerUploadApkStatus.NO_MD5;
					}else if(error.equals("Application duplicate: the uploaded apk already exists in this repository")){
						status = EnumServerUploadApkStatus.APK_DUPLICATE;
					}else if(error.equals("It's not possible to upload the required APK since an infection was detected. If you are the developer/owner of the application, please contact Aptoide Staff.")){
						status = EnumServerUploadApkStatus.APK_INFECTED_WITH_VIRUS;
					}else if(error.equals("Due to Intelectual Property reasons, it's not possible to upload the required APK. If you are the developer\\/owner of the application, please contact Aptoide Staff.")){
						status = EnumServerUploadApkStatus.APK_BLACKLISTED;
					}else if(error.equals("Unable to upload the apk icon.")){
						status = EnumServerUploadApkStatus.SERVER_ERROR_ICON_UPLOAD;
					}else if(error.equals("Unable to upload the Feature Graphic.")){
						status = EnumServerUploadApkStatus.SERVER_ERROR_GRAPHIC_UPLOAD;
					}else if(error.equals("MD5 processing failed, please try again.")){
						status = EnumServerUploadApkStatus.SERVER_ERROR_MD5;
					}else if(error.equals("The file you sent is missing. Maybe the form session has expired. Please upload the file again.")){
        	        	status = EnumServerUploadApkStatus.SERVER_ERROR_MISSING_FILE;
        	        }else if(error.startsWith("Invalid screenshot (")//'<screenshot reference here>') uploaded! Please review your files.")
						|| error.equals("Unable to download Google Market screenshots.")){
						status = EnumServerUploadApkStatus.SERVER_ERROR_SCREENSHOTS;
					}else if(error.startsWith("One of your screenshots (")//'<screenshot reference here>') failed to be uploaded. Please Try Again.")
						|| error.equals("Unable to upload the screenshots.")){
						status = EnumServerUploadApkStatus.SERVER_ERROR_SCREENSHOTS_UPLOAD;
					}
    	        }
        	}
        }
		return status;
	}
	
	
	public ViewAppDownloadInfo parseRepoAppDownloadXml(HttpURLConnection connection, int repoHashid){
		ViewAppDownloadInfo downloadInfo = null;
		int appHashid = 0;
		int appFullHashid = 0;
		
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
			DocumentBuilder builder = factory.newDocumentBuilder();
	        Document dom = builder.parse( connection.getInputStream() );
	        dom.getDocumentElement().normalize();
	        NodeList pkgList = dom.getElementsByTagName(EnumXmlTagsDownload.pkg.toString());
	        if(pkgList.getLength()>0){
	        	NodeList pkgNodes = pkgList.item(0).getChildNodes();
	        	
	        	for (int i=0; i<pkgNodes.getLength(); i++) {
					Node node = pkgNodes.item(i);
					EnumXmlTagsDownload tag = EnumXmlTagsDownload.safeValueOf(node.getNodeName());
					
					switch (tag) {
						case apphashid:
							appHashid = Integer.parseInt(node.getNodeValue());
							appFullHashid = (appHashid+"|"+repoHashid).hashCode();
							break;
							
						case path:
							String appRemotePathTail = node.getNodeValue();
							downloadInfo = new ViewAppDownloadInfo(appRemotePathTail, appFullHashid);
							downloadInfo.setAppHashid(appHashid);
							break;
							
						case md5h:
							downloadInfo.setMd5hash(node.getNodeValue());
							break;
							
						case sz:
							downloadInfo.setSize(Integer.parseInt(node.getNodeValue()));
							if(downloadInfo.getSize()==0){	//TODO complete this hack with a flag <1KB
								downloadInfo.setSize(1);
							}
							break;
							
						default:
							break;
					}
				}
				
				managerXml.getManagerDatabase().insertDownloadInfo(downloadInfo);
			
				return downloadInfo;
	        	
	        }else{
	        	return null;
	        }
		}catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}
	

}
