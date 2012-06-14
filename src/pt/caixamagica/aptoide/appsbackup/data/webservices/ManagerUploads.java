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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import pt.caixamagica.aptoide.appsbackup.data.AptoideServiceData;
import pt.caixamagica.aptoide.appsbackup.data.ViewClientStatistics;
import pt.caixamagica.aptoide.appsbackup.data.model.ViewLogin;
import pt.caixamagica.aptoide.appsbackup.data.util.Constants;
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
		return String.format(Constants.USER_AGENT_FORMAT
				, clientStatistics.getAptoideVersionNameInUse(), clientStatistics.getScreenDimensions().getFormattedString()
				, clientStatistics.getAptoideClientUUID(), getServerUsername());
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
	
	
	
	public EnumServerLoginStatus login(ViewServerLogin serverLogin){
		EnumServerLoginStatus status = EnumServerLoginStatus.LOGIN_SERVICE_UNAVAILABLE;
		String endpointString = String.format(Constants.URI_FORMAT_LOGIN_WS, URLEncoder.encode(serverLogin.getUsername()), URLEncoder.encode(serverLogin.getPasshash()));

//    	Log.d("Aptoide-ManagerUploads login", endpointString);

		try {
			URL endpoint = new URL(endpointString);
			HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection(); //Careful with UnknownHostException. Throws MalformedURLException, IOException
			
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/xml");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//			connection.setConnectTimeout(TIME_OUT);
			
//			connection.setRequestProperty("User-Agent", getUserAgentString());

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
		EnumServerUploadApkStatus status = EnumServerUploadApkStatus.NO_ERROR;
		String apkPath = viewApk.getPath();
		String token = serviceData.getManagerPreferences().getToken();
		
		String body = formPart("isAptUploader", "true");

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
		
		body += formPart("token", token )
			 + formPart("repo", viewApk.getRepository() )
			 + formPart("apkname", viewApk.getName() )
			 + formPart("rating", viewApk.getRating() )
			 + formPart("mode", "xml" )
			 + formBinaryPartNoTail("apk", "application/vnd.android.package-archive");
		
		
		
		DataOutputStream outputStream = null;

		
		try {
			URL url = new URL(Constants.URI_UPLOAD_WS);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			// This fixes #515 : Out of memory bug
			connection.setChunkedStreamingMode(CHUNK_SIZE);

			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			// Enable POST method
			connection.setRequestMethod("POST");
			
			connection.setInstanceFollowRedirects(true);

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
//		connection.setRequestProperty("User-Agent", USER_AGENT);
//		
//		if(!cookie.isNull()){
//			connection.addRequestProperty("Cookie", cookie.getCookie());
//		}

			outputStream = new DataOutputStream( connection.getOutputStream() );

			outputStream.writeBytes(body);
			
//		if(submitting){
			
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
//		return lenTotal;

			outputStream.writeBytes(LINE_END + TWO_HYPHENS + boundary + TWO_HYPHENS + LINE_END);
//		}

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
