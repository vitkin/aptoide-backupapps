/*
 * EnumServerUploadApkStatus, part of Aptoide Apps Backup
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

public enum EnumServerUploadApkStatus {
	NO_ERROR,
	
	UNPREDICTABLE_ERROR,
	
	CONNECTION_ERROR,
	SERVER_ERROR,

	
	HTTP_SEQUENCE_ERROR,
	HTML_ERROR,
	COOKIE_ERROR,

	BAD_LOGIN,
	
	
	MISSING_TOKEN,
	MISSING_APK,
	MISSING_APK_NAME,
	MISSING_DESCRIPTION,
	MISSING_RATING,
	MISSING_CATEGORY,
	
	BAD_TOKEN,
	BAD_REPO,
	BAD_APK,
	BAD_RATING,
	BAD_CATEGORY,
	BAD_WEBSITE,
	BAD_EMAIL,
	BAD_SCREENSHOT,
	TOKEN_INCONSISTENT_WITH_REPO,
	
	BAD_ICON_UPLOAD,
	BAD_SCREENSHOT_UPLOAD,
	BAD_FEATURE_GRAPHIC_UPLOAD,
	BAD_MD5,
	NO_MD5,
	BAD_APK_UPLOAD,
	APK_TOO_BIG,
	APK_DUPLICATE,
	APK_INFECTED_WITH_VIRUS,
	BLACK_LISTED;
	
	public static EnumServerUploadApkStatus reverseOrdinal(int ordinal){
		return values()[ordinal];
	}
}
