/**
 * RepoDeltaParser, 	auxiliary class to Aptoide's ServiceData
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

package pt.aptoide.appsbackup.data.xml;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import pt.aptoide.appsbackup.data.model.ViewAppDownloadInfo;
import pt.aptoide.appsbackup.data.model.ViewApplication;
import pt.aptoide.appsbackup.data.model.ViewIconInfo;
import pt.aptoide.appsbackup.data.model.ViewListIds;
import pt.aptoide.appsbackup.data.preferences.EnumAgeRating;
import pt.aptoide.appsbackup.data.preferences.EnumMinScreenSize;
import pt.aptoide.appsbackup.data.util.Constants;

import android.util.Log;

/**
 * RepoDeltaParser, handles Delta Repo xml Sax parsing
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ParserRepoDelta extends DefaultHandler{
	private ManagerXml managerXml = null;
	
	private ViewXmlParse parseInfo;
	private ViewApplication application;
	private ArrayList<ViewApplication> newApplications = new ArrayList<ViewApplication>();
	private ViewIconInfo icon;
	private ArrayList<ViewIconInfo> newIcons = new ArrayList<ViewIconInfo>();
	private ViewAppDownloadInfo downloadInfo;
	private ArrayList<ViewAppDownloadInfo> newDownloadInfo = new ArrayList<ViewAppDownloadInfo>(Constants.APPLICATIONS_IN_EACH_INSERT);
	private ViewListIds removedApplications = new ViewListIds();
	
	private EnumXmlTagsDelta tag = EnumXmlTagsDelta.apklst;
	
	private String packageName = "";
	String path;
	private boolean toRemove = false;
	private int repoSizeDifferential = 0;
	private int totalParsedApps = Constants.EMPTY_INT;
	
	private StringBuilder tagContentBuilder;
	
		
	public ParserRepoDelta(ManagerXml managerXml, ViewXmlParse parseInfo){
		this.managerXml = managerXml;
		this.parseInfo = parseInfo;
	}
	
	@Override
	public void characters(final char[] chars, final int start, final int length) throws SAXException {
		super.characters(chars, start, length);
		
		tagContentBuilder.append(new String(chars, start, length).trim());
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);

		tag = EnumXmlTagsDelta.safeValueOf(localName.trim());
		
		if(tag != null){
			switch (tag) {
				case apphashid:
					if(toRemove){
						removedApplications.add((Integer.parseInt(tagContentBuilder.toString())+"|"+parseInfo.getRepository().getHashid()).hashCode());
					}
					break;
			
				case apkid:
					packageName = tagContentBuilder.toString();
					break;
				case vercode:
					int versionCode = Integer.parseInt(tagContentBuilder.toString());
					application = new ViewApplication(packageName, versionCode, false);
					application.setRepoHashid(parseInfo.getRepository().getHashid());
					break;
				case ver:
					application.setVersionName(tagContentBuilder.toString());
					break;
				case name:
					application.setApplicationName(tagContentBuilder.toString());
					break;
				case catg2:
					application.setCategoryHashid((tagContentBuilder.toString().trim()).hashCode());
	//				Log.d("Aptoide-RepoBareParser", "app: "+application.getApplicationName()+", appHashid (Not full): "+application.getHashid()+", category: "+tagContentBuilder.toString().trim()+", categoryHashid: "+application.getCategoryHashid());
					break;
				case timestamp:
					application.setTimestamp(Long.parseLong(tagContentBuilder.toString()));
					break;
				case age:
					application.setRating(EnumAgeRating.safeValueOf(tagContentBuilder.toString().trim()).ordinal());
					break;
				case minScreen:
					application.setMinScreen(EnumMinScreenSize.valueOf(tagContentBuilder.toString().trim()).ordinal());
					break;
				case minSdk:
					application.setMinSdk(Integer.parseInt(tagContentBuilder.toString().trim()));
					break;
				case minGles:
					float gles = Float.parseFloat(tagContentBuilder.toString().trim());
					if(gles < 1.0){
						gles = 1;
					}
					application.setMinGles(gles);
					break;
					
				case icon:
					icon = new ViewIconInfo(tagContentBuilder.toString(), application.getFullHashid());
					break;
					
				case path:
					String appRemotePathTail = tagContentBuilder.toString();
					downloadInfo = new ViewAppDownloadInfo(appRemotePathTail, application.getFullHashid());
					break;
					
				case md5h:
					downloadInfo.setMd5hash(tagContentBuilder.toString());
					break;
					
				case sz:
					downloadInfo.setSize(Integer.parseInt(tagContentBuilder.toString()));
					if(downloadInfo.getSize()==0){	//TODO complete this hack with a flag <1KB
						downloadInfo.setSize(1);
					}
					break;
					
					
				case pkg:
					parseInfo.getNotification().incrementProgress(1);
					if(toRemove){
						repoSizeDifferential--;
					}else{
						repoSizeDifferential++;
						Log.d("Aptoide-ParseRepoDelta", "New application:  "+application.toStringDetails());
						newApplications.add(application);
						newIcons.add(icon);
						newDownloadInfo.add(downloadInfo);
					}
					toRemove = false;
					break;
				
					
				case basepath:
					path = tagContentBuilder.toString();
					if(!path.equals(parseInfo.getRepository().getBasePath())){
						parseInfo.getRepository().setBasePath(path);
					}
					break;	
				case iconspath:
					path = tagContentBuilder.toString();
					if(!path.equals(parseInfo.getRepository().getIconsPath())){
						parseInfo.getRepository().setIconsPath(tagContentBuilder.toString());
					}
					break;	
				case screenspath:
					path = tagContentBuilder.toString();
					if(!path.equals(parseInfo.getRepository().getScreensPath())){
						parseInfo.getRepository().setScreensPath(tagContentBuilder.toString());
					}
					break;	
				case appscount:
//					repoSizeDifferential = Integer.parseInt(tagContentBuilder.toString());
					parseInfo.getNotification().setProgressCompletionTarget(repoSizeDifferential);
					totalParsedApps = Integer.parseInt(tagContentBuilder.toString());
					break;
					
				case hash:
					Log.d("Aptoide-RepoDeltaParser", "server exception, sending info.xml when delta was required!");
				case delta:
					String delta = tagContentBuilder.toString();
					if(delta == ""){
						managerXml.parsingRepoDeltaFinished(parseInfo.getRepository(), 0);
						throw new SAXException("Empty delta -> no new apps!");
					}else{
						parseInfo.getRepository().setDelta(delta);
					}
					break;
					
				case repository:
					break;
					
				default:
					break;
			}
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		tagContentBuilder = new StringBuilder();
		
		if(localName.trim().equals("del")){
			toRemove = true;
		}
		
	}
	
	
	
	
	@Override
	public void startDocument() throws SAXException {	//TODO refacto Logs
		Log.d("Aptoide-RepoDeltaParser","Started parsing XML from " + parseInfo.getRepository().getRepoName() + " ...");
		super.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		parseInfo.getRepository().setSize(parseInfo.getRepository().getSize()+repoSizeDifferential);
		Log.d("Aptoide-RepoDeltaParser","Done parsing XML from " + parseInfo.getRepository().getRepoName() + " ... size diff: "+repoSizeDifferential);
		
		Log.d("Aptoide-RepoDeltaParser", "delta: "+parseInfo.getRepository().getDelta());

		managerXml.getManagerDatabase().updateRepository(parseInfo.getRepository());
		Log.d("Aptoide-RepoDeltaParser","removing apps: " + removedApplications + " ...");	
		managerXml.getManagerDatabase().removeApplications(removedApplications);
		Log.d("Aptoide-RepoDeltaParser","inserting new apps: " + newApplications + " ...");		
		managerXml.getManagerDatabase().insertApplications(newApplications);
		
		Log.d("Aptoide-RepoDeltaParser","inserting new apps icons: " + newIcons + " ...");	
		managerXml.getManagerDatabase().insertIconsInfo(newIcons);
		Log.d("Aptoide-RepoDeltaParser","inserting new apps downloadInfo: " + newDownloadInfo + " ...");	
		managerXml.getManagerDatabase().insertDownloadsInfo(newDownloadInfo);
		
		if(totalParsedApps > Constants.APPLICATIONS_IN_EACH_INSERT){
			managerXml.getManagerDatabase().optimizeQuerys();
		}
		
		managerXml.parsingRepoDeltaFinished(parseInfo.getRepository(), repoSizeDifferential);
		
		super.endDocument();
	}


}
