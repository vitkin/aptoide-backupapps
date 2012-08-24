/**
 * ManagerUploads,		auxilliary class to Aptoide's ServiceData
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

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import pt.caixamagica.aptoide.appsbackup.data.AptoideServiceData;
import pt.caixamagica.aptoide.appsbackup.data.ViewClientStatistics;
import pt.caixamagica.aptoide.appsbackup.data.cache.ViewCache;
import pt.caixamagica.aptoide.appsbackup.data.model.ViewLogin;
import pt.caixamagica.aptoide.appsbackup.data.util.Constants;
import pt.caixamagica.aptoide.appsbackup.data.util.Security;
import pt.caixamagica.aptoide.appsbackup.debug.exceptions.UnsuccessfullSubmitException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * ManagerUploads, centralizes all upload processes
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ManagerUploads {
	
	private AptoideServiceData serviceData;

	private ConnectivityManager connectivityState;
	

	
//    private class IconsDownloadManager{
//    	private ExecutorService iconGettersPool;
//    	private AtomicInteger iconsDownloadedCounter;
//    	
//    	public IconsDownloadManager(){
//    		iconGettersPool = Executors.newFixedThreadPool(Constants.MAX_PARALLEL_DOWNLOADS);
//    		iconsDownloadedCounter = new AtomicInteger(0);
//    	}
//    	
//    	public void executeDownload(ViewDownload downloadInfo){
//    		iconGettersPool.execute(new GetIcon(downloadInfo));
//        }
//    	
//    	private class GetIcon implements Runnable{
//
//    		private ViewDownload iconDownload;
//    		
//			public GetIcon(ViewDownload iconDownloadInfo) {
//				this.iconDownload = iconDownloadInfo;
//			}
//
//			@Override
//			public void run() {
////				downloads.put(download.getNotification().getNotificationHashid(), download);
//				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
//				download(iconDownload, false);
//				recicleViewDownload(iconDownload);
//				if(iconsDownloadedCounter.incrementAndGet() >= Constants.ICONS_REFRESH_INTERVAL){
//					iconsDownloadedCounter.set(0);
//					serviceData.refreshAvailableDisplay();
//				}
//			}
//    		
//    	}
//    }
	
	
	private ViewClientStatistics getClientStatistics(){
		return serviceData.getStatistics();
	}
	
	private String getServerUsername(){
		return serviceData.getManagerPreferences().getServerLogin().getUsername();
	}
	
	private String getUserAgentString(){
		ViewClientStatistics clientStatistics = getClientStatistics();
		String userAgent = String.format(Constants.USER_AGENT_FORMAT
				, clientStatistics.getAptoideVersionNameInUse(), clientStatistics.getScreenDimensions().getFormattedString()
				, clientStatistics.getAptoideClientUUID(), getServerUsername());
//		Log.d("AptoideAppsBackup-ManagerUploads", "userAgent: "+userAgent);
		return userAgent;
	}
	
	
	public ManagerUploads(AptoideServiceData serviceData) {
		this.serviceData = serviceData;
		
		connectivityState = (ConnectivityManager)serviceData.getSystemService(Context.CONNECTIVITY_SERVICE);

	}
		
	

	
	public boolean isConnectionAvailable(){
		boolean connectionAvailable = false;
		try {
			connectionAvailable = connectivityState.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED;
			Log.d("ManagerDownloads", "isConnectionAvailable mobile: "+connectionAvailable);	
		} catch (Exception e) { }
		try {
			connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED;
			Log.d("ManagerDownloads", "isConnectionAvailable wifi: "+connectionAvailable);
		} catch (Exception e) { }
		try {
			connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(6).getState() == NetworkInfo.State.CONNECTED;
			Log.d("ManagerDownloads", "isConnectionAvailable wimax: "+connectionAvailable);
		} catch (Exception e) { }
		try {
			connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(9).getState() == NetworkInfo.State.CONNECTED;
			Log.d("ManagerDownloads", "isConnectionAvailable ethernet: "+connectionAvailable);
		} catch (Exception e) { }
		
		return connectionAvailable;
	}
	
	public boolean isPermittedConnectionAvailable(ViewIconDownloadPermissions permissions){
		boolean connectionAvailable = false;
		if(permissions.isWiFi()){
			try {
				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED;
				Log.d("ManagerDownloads", "isPermittedConnectionAvailable wifi: "+connectionAvailable);
			} catch (Exception e) { }
		} 
		if(permissions.isWiMax()){
			try {
				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(6).getState() == NetworkInfo.State.CONNECTED;
				Log.d("ManagerDownloads", "isPermittedConnectionAvailable wimax: "+connectionAvailable);
			} catch (Exception e) { }
		} 
		if(permissions.isMobile()){
			try {
				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED;
				Log.d("ManagerDownloads", "isPermittedConnectionAvailable mobile: "+connectionAvailable);
			} catch (Exception e) { }
		}
		if(permissions.isEthernet()){
			try {
				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(9).getState() == NetworkInfo.State.CONNECTED;
				Log.d("ManagerDownloads", "isPermittedConnectionAvailable ethernet: "+connectionAvailable);
			} catch (Exception e) { }
		}

		Log.d("ManagerDownloads", "isPermittedConnectionAvailable: "+connectionAvailable+"  permissions: "+permissions);
		return connectionAvailable;
	}
	
	
	
	
	public String calcHmac(ViewServerLogin serverLogin) throws InvalidKeyException, IllegalStateException, UnsupportedEncodingException, NoSuchAlgorithmException{
		StringBuilder hmacMessage = new StringBuilder(serverLogin.getUsername()+serverLogin.getPasshash()+serverLogin.getRepoName());

		if(serverLogin.isRepoPrivate()){
			hmacMessage.append("true"+serverLogin.getPrivUsername()+serverLogin.getPrivPassword());
		}else{
			hmacMessage.append("false");
		}
		
//		if(serverLogin.isUpdate()){
//			hmac_message.append("true");
//		}

		return Security.computeHmacSha1(hmacMessage.toString(), "bazaar_hmac");

	}
	
	
	public EnumServerLoginCreateStatus loginCreate(ViewServerLogin serverLogin){
		EnumServerLoginCreateStatus status = EnumServerLoginCreateStatus.LOGIN_CREATE_SERVICE_UNAVAILABLE;

		try {
			URL endpoint = new URL(Constants.URI_LOGIN_CREATE_WS);
			HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection(); //Careful with UnknownHostException. Throws MalformedURLException, IOException
			
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept", "application/xml");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

			connection.setRequestProperty("User-Agent", getUserAgentString());

			//Variable definition
			StringBuilder postArguments = new StringBuilder();
			postArguments.append(URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(serverLogin.getUsername(), "UTF-8"));
			postArguments.append("&"+URLEncoder.encode("passhash", "UTF-8") + "=" + URLEncoder.encode(serverLogin.getPasshash(), "UTF-8"));
			
			if(serverLogin.getRepoName() != null && !serverLogin.getRepoName().equals("")){
				postArguments.append("&"+URLEncoder.encode("repo", "UTF-8") + "=" + URLEncoder.encode(serverLogin.getRepoName(), "UTF-8"));
			}

			if(serverLogin.getNickname() != null && !serverLogin.getNickname().equals("")){
				postArguments.append("&"+URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(serverLogin.getNickname(), "UTF-8"));
			}
			if(serverLogin.isRepoPrivate()){
				postArguments.append("&"+URLEncoder.encode("privacy", "UTF-8") + "=" + URLEncoder.encode("true", "UTF-8"));
				postArguments.append("&"+URLEncoder.encode("privacy_user", "UTF-8") + "=" + URLEncoder.encode(serverLogin.getPrivUsername(), "UTF-8"));
				postArguments.append("&"+URLEncoder.encode("privacy_pass", "UTF-8") + "=" + URLEncoder.encode(serverLogin.getPrivPassword(), "UTF-8"));
			}else{
				postArguments.append("&"+URLEncoder.encode("privacy", "UTF-8") + "=" + URLEncoder.encode("false", "UTF-8"));				
			}

//			postArguments.append("&"+URLEncoder.encode("update", "UTF-8") + "=" + URLEncoder.encode(serverLogin., "UTF-8"));

			
			
			try {
				postArguments.append("&"+URLEncoder.encode("hmac", "UTF-8") + "=" + URLEncoder.encode(calcHmac(serverLogin), "UTF-8"));
			} catch (Exception e) {
				e.printStackTrace();
				status = EnumServerLoginCreateStatus.BAD_HMAC;
				return status;
			} 
			
			postArguments.append("&"+URLEncoder.encode("mode", "UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8"));


			connection.setDoOutput(true);
			connection.setDoInput(true);


			OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());
			output.write(postArguments.toString());
			output.flush();

			status = serviceData.getManagerXml().dom.parseServerLoginCreateReturn(connection);
			
//			if(status.equals(EnumServerLoginCreateStatus.SUCCESS)){
//				EnumServerLoginStatus loginStatus = login(serverLogin);
//				if(loginStatus.equals(EnumServerLoginStatus.SUCCESS)){
////			        serviceData.getManagerPreferences().setServerLogin(new ViewLogin(serverLogin.getUsername(), serverLogin.getPasshash()));
//				}else{
//					status = EnumServerLoginCreateStatus.SERVER_ERROR;
//				}
//		    }
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return status;
		
	}
	
	
	
	public EnumServerLoginStatus login(ViewServerLogin serverLogin){
		EnumServerLoginStatus status = EnumServerLoginStatus.LOGIN_SERVICE_UNAVAILABLE;
		String endpointString;
		if(serverLogin.getRepoName() == null){
			endpointString = String.format(Constants.URI_FORMAT_LOGIN_DEFAULT_REPO_WS, URLEncoder.encode(serverLogin.getUsername()), URLEncoder.encode(serverLogin.getPasshash()));
		}else{
			endpointString = String.format(Constants.URI_FORMAT_LOGIN_WS, URLEncoder.encode(serverLogin.getUsername()), URLEncoder.encode(serverLogin.getPasshash()), URLEncoder.encode(serverLogin.getRepoName()));
		}

//    	Log.d("Aptoide-ManagerUploads login", endpointString);

		try {
			URL endpoint = new URL(endpointString);
			HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection(); //Careful with UnknownHostException. Throws MalformedURLException, IOException
			
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/xml");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//			connection.setConnectTimeout(TIME_OUT);
			
			connection.setRequestProperty("User-Agent", getUserAgentString());

			status = serviceData.getManagerXml().dom.parseServerLoginReturn(connection);

	        if(status.equals(EnumServerLoginStatus.SUCCESS)){
		        serviceData.getManagerPreferences().setServerLogin(new ViewLogin(serverLogin.getUsername(), serverLogin.getPasshash()));
	        }
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return status;
		
	}
	
	public EnumServerAddAppVersionLikeStatus addAppVersionLike(String repoName, int appHashid, boolean like){	//TODO create ViewLike from args
		EnumServerAddAppVersionLikeStatus status = EnumServerAddAppVersionLikeStatus.SERVICE_UNAVAILABLE;
		String token = serviceData.getManagerPreferences().getToken();	//TODO add support for multiple servers
		String endpointString;
		
		if(like){
			endpointString = String.format(Constants.URI_FORMAT_ADD_LIKE_WS, URLEncoder.encode(token), URLEncoder.encode(repoName), URLEncoder.encode(Integer.toString(appHashid)));
		}else{
			endpointString = String.format(Constants.URI_FORMAT_ADD_DISLIKE_WS, URLEncoder.encode(token), URLEncoder.encode(repoName), URLEncoder.encode(Integer.toString(appHashid)));
		}
		
    	Log.d("Aptoide-ManagerUploads addLike: ", endpointString);

		try {
			URL endpoint = new URL(endpointString);
			HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection(); //Careful with UnknownHostException. Throws MalformedURLException, IOException
			
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/xml");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//			connection.setConnectTimeout(TIME_OUT);

//			connection.setRequestProperty("User-Agent", getUserAgentString());
			
			status = serviceData.getManagerXml().dom.parseAddAppVersionLikeReturn(connection);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return status;
	}
	
	public EnumServerAddAppVersionCommentStatus addAppVersionComment(String repoName, int appHashid, String commentBody, String subject, long answerTo){	//TODO create ViewComment from args
		EnumServerAddAppVersionCommentStatus status = EnumServerAddAppVersionCommentStatus.SERVICE_UNAVAILABLE;
		String token = serviceData.getManagerPreferences().getToken();	//TODO add support for multiple servers

		try {
			URL endpoint = new URL(Constants.URI_ADD_COMMENT_POST_WS);
			
			HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();//Careful with UnknownHostException 

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept", "application/xml");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

//			connection.setRequestProperty("User-Agent", getUserAgentString());

			//Variable definition
			StringBuilder postArguments = new StringBuilder();
			postArguments.append(URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(token, "UTF-8"));
			postArguments.append("&"+URLEncoder.encode("repo", "UTF-8") + "=" + URLEncoder.encode(repoName, "UTF-8"));
			postArguments.append("&"+URLEncoder.encode("apkid", "UTF-8") + "=" + URLEncoder.encode("apphashid", "UTF-8"));
			postArguments.append("&"+URLEncoder.encode("apkversion", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(appHashid), "UTF-8"));
			postArguments.append("&"+URLEncoder.encode("text", "UTF-8") + "=" + URLEncoder.encode(commentBody, "UTF-8"));

			String language = serviceData.getResources().getConfiguration().locale.getLanguage()+"_"+serviceData.getResources().getConfiguration().locale.getCountry();
			Log.d("Aptoide-ManagerUploads", "addAppVersionComment, language: "+language);

			postArguments.append("&"+URLEncoder.encode("lang", "UTF-8") + "=" + URLEncoder.encode( language, "UTF-8"));
			if(answerTo!=Constants.EMPTY_INT){
				postArguments.append("&"+URLEncoder.encode("answerto", "UTF-8") + "=" + URLEncoder.encode(Long.toString(answerTo), "UTF-8"));
			}
			if(subject!=null && subject.length()!=0){
				postArguments.append("&"+URLEncoder.encode("subject", "UTF-8") + "=" + URLEncoder.encode(subject, "UTF-8"));
			}
			postArguments.append("&"+URLEncoder.encode("mode", "UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8"));


			connection.setDoOutput(true);
			connection.setDoInput(true);


			OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());
			output.write(postArguments.toString());
			output.flush();

			status = serviceData.getManagerXml().dom.parseAddAppVersionCommentReturn(connection);

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return status;
	}	
	
	
	
	
	
	private static final String LINE_END = "\r\n";
	private static final String TWO_HYPHENS = "--";
	private static final int CHUNK_SIZE = 8096;
	

	public static String generateBoundary() { 
		try {
			// Create a secure random number generator
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");

			// Get 1024 random bits
			byte[] bytes = new byte[1024/8];
			sr.nextBytes(bytes);
			
			int seedByteCount = 10;
			byte[] seed = sr.generateSeed(seedByteCount);

			sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(seed);
			
			return "***"+Long.toString(sr.nextLong())+"***";
			
		} catch (NoSuchAlgorithmException e) {
		}
		return "*********";
	}
	
	private String boundary = generateBoundary();
	
	private String formPart(String fieldName, String fieldValue){
		return   TWO_HYPHENS + boundary + LINE_END
			   + "Content-Disposition: form-data; name=\"" + fieldName + "\""
			   + LINE_END + LINE_END + fieldValue + LINE_END;
	}
	
	private String formBinaryPartNoTail(String fieldName, String apkName){
		String contentType = "application/vnd.android.package-archive";
		return  TWO_HYPHENS + boundary + LINE_END
			  +	"Content-Disposition: form-data; name=\"" + fieldName + "\"; "
			  + "filename=\"" + apkName + ".apk\"" + LINE_END
			  + "Content-Type: " + contentType + LINE_END + LINE_END;  
	}
	
	public String hasErrors(String response) throws JSONException{
		JSONObject json;
		String status;
		String errors = null;
		
		json = new JSONObject(response);
		status = json.getString("status");
		if(status.equals("OK")){
			Log.d("AptoideUploader-Post-Errors", json.getString("errors")); //webservice returns success messages under errors JSON tag
		}else{
			errors = json.getString("errors");
		}
			
		if(errors == null){
			Log.d("AptoideUploader-Post-Errors", "Status OK");
			return "";
		}
		Log.d("AptoideUploader-Post-Errors", errors);
		return errors;
	}
	
	public EnumServerUploadApkStatus uploadApk(ViewApk viewApk){
		EnumServerUploadApkStatus status = uploadApk(viewApk, true);
		if(status.equals(EnumServerUploadApkStatus.SERVER_ERROR_MD5) || status.equals(EnumServerUploadApkStatus.NO_MD5) ){
			status = uploadApk(viewApk, true);
			if(status.equals(EnumServerUploadApkStatus.NO_MD5) ){
				status = uploadApk(viewApk, false);
			}
		}
		return status;
	}
	
	public EnumServerUploadApkStatus uploadApk(ViewApk viewApk, boolean justMd5){
		EnumServerUploadApkStatus status = EnumServerUploadApkStatus.SUCCESS;
		String apkPath = viewApk.getPath();
		String token = serviceData.getManagerPreferences().getToken();
		
		String body = formPart("uploadType", "4");

		if(viewApk.getCategory() != null){
			body += formPart("category", viewApk.getCategory() );
		}
		if(viewApk.getDescription() != null){
			body += formPart("description", viewApk.getDescription() );
		}
		if(viewApk.getPhone() != null){
			body += formPart("apk_phone", viewApk.getPhone() );
		}
		if(viewApk.getEmail() != null){
			body += formPart("apk_email", viewApk.getEmail() );
		}
		if(viewApk.getWebURL() != null){
			body += formPart("apk_website", viewApk.getWebURL() );
		}
		
		if(justMd5){
			ViewCache apk = serviceData.getManagerCache().getNewViewCache(viewApk.getPath());
			serviceData.getManagerCache().calculateMd5Hash(apk);
			Log.d("Aptoide-ManagerUploads", "UploadApk "+viewApk.getPath()+" - using just md5: "+apk.getMd5sum());
			body += formPart("apk_md5sum",  apk.getMd5sum());
		}else{
			Log.d("Aptoide-ManagerUploads", "UploadApk "+viewApk.getPath());
		}
		
		body += formPart("token", token )
			 + formPart("repo", viewApk.getRepository() )
			 + formPart("apkname", viewApk.getName() )
			 + formPart("rating", viewApk.getRating() )
			 + formPart("mode", "xml" );
		
		if(!justMd5){
			body += formBinaryPartNoTail("apk", "application/vnd.android.package-archive");
		}
		
		
		
		DataOutputStream outputStream = null;

		
		try {
			URL url = new URL(Constants.URI_UPLOAD_WS);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			// This fixes #515 : Out of memory bug
			connection.setChunkedStreamingMode(CHUNK_SIZE);
			connection.setConnectTimeout(120000);
			connection.setReadTimeout(120000);
			
			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			// Enable POST method
			connection.setRequestMethod("POST");
			
			connection.setInstanceFollowRedirects(true);

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
			
			connection.setRequestProperty("User-Agent", getUserAgentString());

			outputStream = new DataOutputStream( connection.getOutputStream() );

			outputStream.writeBytes(body);
			
			if(!justMd5){
				FileInputStream apk = new FileInputStream(apkPath);
				byte data[] = new byte[CHUNK_SIZE];
				long lenTotal = 0;
				int read;
				long uploadSize = viewApk.getSize();
				int progressPercentage = 0;
				while((read = apk.read(data, 0, CHUNK_SIZE)) != -1) {
				    outputStream.write(data,0,read);
				    lenTotal += read;
				    int newProgressPercentage = (int) (lenTotal*100/uploadSize);
				    Log.d("OutputApk", "sent: "+read+"bytes, Total: "+lenTotal+" completion: "+newProgressPercentage+"% app: "+viewApk.getName());
				    if(newProgressPercentage > (progressPercentage+10)){
				    	progressPercentage = newProgressPercentage;
				    	serviceData.uploadingProgressUpdate(viewApk.getAppHashid(), progressPercentage);
				    }
				}
			}
			
			outputStream.writeBytes(LINE_END + TWO_HYPHENS + boundary + TWO_HYPHENS + LINE_END);

			outputStream.flush();
			outputStream.close();
			
			status = serviceData.getManagerXml().dom.parseApkUploadXml(connection);
			
		} catch (Exception e) {
			status = EnumServerUploadApkStatus.CONNECTION_ERROR;
			e.printStackTrace();
			return status;
		} 
//		catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}  catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnsuccessfullSubmitException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return status;
	}

}
