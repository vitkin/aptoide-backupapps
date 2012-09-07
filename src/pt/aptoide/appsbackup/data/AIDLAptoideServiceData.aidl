/**
 * AIDLAptoideServiceData,		part of Aptoide's ServiceData
 * Copyright (C) 2011 Duarte Silveira
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
package pt.aptoide.appsbackup.data;

import pt.aptoide.appsbackup.data.system.ViewScreenDimensions;
import pt.aptoide.appsbackup.AIDLAptoideInterface;
import pt.aptoide.appsbackup.data.display.ViewDisplayListsDimensions;
import pt.aptoide.appsbackup.data.display.ViewDisplayCategory;
import pt.aptoide.appsbackup.data.display.ViewDisplayListApps;
import pt.aptoide.appsbackup.data.display.ViewDisplayListRepos;
import pt.aptoide.appsbackup.data.display.ViewDisplayAppVersionsInfo;
import pt.aptoide.appsbackup.data.display.ViewDisplayAppVersionStats;
import pt.aptoide.appsbackup.data.display.ViewDisplayAppVersionExtras;
import pt.aptoide.appsbackup.data.display.ViewDisplayListComments;
import pt.aptoide.appsbackup.data.model.ViewRepository;
import pt.aptoide.appsbackup.data.model.ViewListIds;
import pt.aptoide.appsbackup.AIDLAppInfo;
import pt.aptoide.appsbackup.AIDLReposInfo;
import pt.aptoide.appsbackup.AIDLSelfUpdate;
import pt.aptoide.appsbackup.AIDLUpload;
import pt.aptoide.appsbackup.AIDLLogin;
import pt.aptoide.appsbackup.data.listeners.ViewMyapp;
import pt.aptoide.appsbackup.data.preferences.ViewSettings;
import pt.aptoide.appsbackup.data.system.ViewHwFilters;
import pt.aptoide.appsbackup.data.webservices.ViewIconDownloadPermissions;
import pt.aptoide.appsbackup.data.webservices.ViewUploadInfo;
import pt.aptoide.appsbackup.data.webservices.ViewApk;
import pt.aptoide.appsbackup.data.webservices.ViewServerLogin;

/**
 * AIDLAptoideServiceData, IPC Interface definition for Aptoide's ServiceData
 *
 * @author dsilveira
 * @since 3.0
 *
 */
interface AIDLAptoideServiceData {
	
	void callRegisterSelfUpdateObserver(in AIDLSelfUpdate selfUpdateClient);
	void callAcceptSelfUpdate();
	void callRejectSelfUpdate();

	String callGetAptoideVersionName();
	void callStoreScreenDimensions(in ViewScreenDimensions screenDimensions);
	
	ViewDisplayListsDimensions callGetDisplayListsDimensions();
	
	void callSyncInstalledApps();
	void callCacheInstalledAppsIcons();
	
	void callRegisterReposObserver(in AIDLReposInfo reposInfoObserver);
	ViewDisplayListRepos callGetRepos();
	void callAddRepo(in ViewRepository repo);
	void callRemoveRepo(in int repoHashid);
	void callSetInUseRepo(in int repoHashid);
	void callUnsetInUseRepo(in int repoHashid);
	void callUpdateRepos();
	void callDelayedUpdateRepos();
	void callRemoveLogin(in int repoHashid);
	void callUpdateLogin(in ViewRepository repo);
	void callNoRepos();
	void callLoadingRepos();
	boolean callAnyReposInUse();
	
	void callRegisterInstalledAppsObserver(in AIDLAptoideInterface installedAppsObserver);
	int callRegisterAvailableAppsObserver(in AIDLAptoideInterface availableAppsObserver);
	void callUnregisterAvailableAppsObserver(in AIDLAptoideInterface availableAppsObserver);
	
	boolean callAreListsByCategory();
	void callSetListsBy(in boolean byCategory);
	
	int callGetTotalAvailableApps();
	int callGetTotalAvailableAppsInCategory(in int categoryHashid);
	
	ViewDisplayCategory callGetCategories();
	
	int callGetAppsSortingPolicy();
	void callSetAppsSortingPolicy(in int sortingPolicy);
	
	boolean callGetShowSystemApps();
	void callSetShowSystemApps(in boolean show);
	
	ViewDisplayListApps callGetInstalledApps();
	ViewDisplayListApps callGetAllAvailableApps();
	ViewDisplayListApps callGetAvailableApps(in int offset, in int range);
	ViewDisplayListApps callGetAvailableAppsByCategory(in int offset, in int range, in int categoryHashid);
	ViewDisplayListApps callGetUpdatableApps();
	
	void callUpdateAll();
	
	ViewDisplayListApps callGetAppSearchResults(in String searchString);
	
	void callRegisterAppInfoObserver(in AIDLAppInfo appInfoObserver, in int appHashid);
	void CallFillAppInfo(in int appHashid);
	void callAddVersionDownloadInfo(in int appHashid, in int repoHashid);
	void callAddVersionStatsInfo(in int appHashid, in int repoHashid);
	void callAddVersionExtraInfo(in int appHashid, in int repoHashid);
	void callRetrieveVersionComments(in int appHashid, in int repoHashid);
	ViewDisplayAppVersionsInfo callGetAppInfo(in int appHashid);
	int callGetAppVersionDownloadSize(in int appFullHashid);
	ViewDisplayAppVersionStats callGetAppStats(in int appFullHashid);
	ViewDisplayAppVersionExtras callGetAppExtras(in int appFullHashid);
	ViewDisplayListComments callGetVersionComments(in int appHashid);
	
	void callRegisterLoginObserver(in AIDLLogin loginObserver);
	
	int callServerLoginCreate(in ViewServerLogin serverLogin);
	void callServerLoginAfterCreate(in ViewServerLogin serverLogin);
	String callGetServerToken();
	int callServerLogin(in ViewServerLogin serverLogin);
	ViewServerLogin callGetServerLogin();
	void callClearServerLogin();
	int callAddAppVersionLike(in String repoName, in int appHashid, in boolean like);
	int callAddAppVersionComment(in String repoName, in int appHashid, in String commentBody, in String subject, in long answerTo);
	
	void callInstallApp(in int appHashid);
	void callUninstallApp(in int appHashid);
	void callUninstallApps(in ViewListIds appHashids);
	void callScheduleInstallApp(in int appHashid);
	void callUnscheduleInstallApp(in int appHashid);
	boolean callIsAppScheduledToInstall(in int appHashid);
	
	ViewDisplayListApps callGetScheduledApps();
	
	void callRegisterMyappReceiver(in AIDLAptoideInterface myappObserver);
	void callReceiveMyapp(in String uriString);
	ViewMyapp callGetWaitingMyapp();
	void callInstallMyapp(in ViewMyapp myapp);
	void callRejectedMyapp();
	ViewDisplayListRepos callGetWaitingMyappRepos();
	
	ViewSettings callGetSettings();
	ViewIconDownloadPermissions callGetIconDownloadPermissions();
	void callSetIconDownloadPermissions(in ViewIconDownloadPermissions iconDownloadPermissions);
	void callClearIconCache();
	void callClearApkCache();
	ViewHwFilters callGetHwFilters();
	void callSetHwFilter(in boolean on);
	void callSetAgeRating(in int rating);
	void callSetAutomaticInstall(in boolean on);
	void callResetAvailableApps();
	
	void callRegisterUploadObserver(in AIDLUpload uploadObserver);
	
	ViewUploadInfo callGetUploadInfo(in int appHashid);
	void callUploadApk(in ViewApk uploadingApk);
	
	boolean callIsInsertingRepo();
	
}
