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

package pt.aptoide.backupapps.data.webservices;

import android.content.Context;
import pt.aptoide.backupapps.R;

public enum EnumServerUploadApkStatus {
	SUCCESS,
	
	CONNECTION_ERROR,
	SERVER_ERROR,
	SERVER_ERROR_MISSING_FILE,
	SERVER_ERROR_SCREENSHOTS,
	SERVER_ERROR_SCREENSHOTS_UPLOAD,
	SERVER_ERROR_MD5,
	SERVER_ERROR_ICON_UPLOAD,
	SERVER_ERROR_GRAPHIC_UPLOAD,

	MISSING_TOKEN,
	MISSING_APK,
	MISSING_APK_NAME,
	MISSING_DESCRIPTION,
	MISSING_RATING,
	MISSING_CATEGORY,

	BAD_LOGIN,
	BAD_TOKEN,
	BAD_REPO,
	BAD_APK,
	BAD_RATING,
	BAD_CATEGORY,
	BAD_WEBSITE,
	BAD_EMAIL,
	TOKEN_INCONSISTENT_WITH_REPO,
	
	NO_MD5,
	BAD_APK_UPLOAD,
	APK_TOO_BIG,
	APK_DUPLICATE,
	APK_INFECTED_WITH_VIRUS,
	APK_BLACKLISTED;
	
	public static EnumServerUploadApkStatus reverseOrdinal(int ordinal){
		return values()[ordinal];
	}
	
	public String toString(Context context) {
		switch (this) {
			case APK_DUPLICATE:
				return  context.getString(R.string.apk_duplicate);
			case APK_INFECTED_WITH_VIRUS:
				return  context.getString(R.string.apk_infected);
			case APK_TOO_BIG:
				return  context.getString(R.string.apk_too_big);
			case BAD_APK:
				return  context.getString(R.string.invalid_apk);
			case BAD_APK_UPLOAD:
				return  context.getString(R.string.failed_apk_upload);
			case BAD_CATEGORY:
				return  context.getString(R.string.invalid_category);
			case BAD_EMAIL:
				return  context.getString(R.string.invalid_email);
			case BAD_LOGIN:
				return  context.getString(R.string.check_login);
			case BAD_RATING:
				return  context.getString(R.string.invalid_rating);
			case BAD_REPO:
				return  context.getString(R.string.invalid_repo_name);
			case BAD_TOKEN:
				return  context.getString(R.string.token_error);
			case BAD_WEBSITE:
				return  context.getString(R.string.invalid_website);
			case APK_BLACKLISTED:
				return  context.getString(R.string.apk_blacklisted);
			case CONNECTION_ERROR:
				return  context.getString(R.string.failed_server_connection);
			case MISSING_APK:
				return  context.getString(R.string.missing_apk);
			case MISSING_APK_NAME:
				return  context.getString(R.string.enter_apk_name);
			case MISSING_CATEGORY:
				return  context.getString(R.string.select_category);
			case MISSING_DESCRIPTION:
				return  context.getString(R.string.enter_description);
			case MISSING_RATING:
				return  context.getString(R.string.select_rating);
			case MISSING_TOKEN:
				return  context.getString(R.string.missing_token);
			case SERVER_ERROR_GRAPHIC_UPLOAD:
				return  context.getString(R.string.server_error_graphic_upload);
			case SERVER_ERROR_ICON_UPLOAD:
				return  context.getString(R.string.server_error_icon_upload);
			case SERVER_ERROR_MD5:
				return  context.getString(R.string.server_error_md5);
			case SERVER_ERROR_MISSING_FILE:
				return  context.getString(R.string.server_error_apk);
			case SERVER_ERROR_SCREENSHOTS_UPLOAD:
			case SERVER_ERROR_SCREENSHOTS:
				return  context.getString(R.string.server_error_screenshots);
			case SERVER_ERROR:
				return  context.getString(R.string.server_error);
			case TOKEN_INCONSISTENT_WITH_REPO:
				return  context.getString(R.string.repo_not_associated_with_user);
			case SUCCESS:
				return  context.getString(R.string.success);
	
			default:
				return  context.getString(R.string.server_error);
		}
	}
	
	
}
