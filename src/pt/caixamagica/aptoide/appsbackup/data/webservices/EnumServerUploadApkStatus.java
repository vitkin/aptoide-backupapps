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

import java.util.HashMap;
import java.util.Map;

import pt.caixamagica.aptoide.appsbackup.R;

public enum EnumServerUploadApkStatus {
	NO_ERROR("No error"),
	
	UNPREDICTABLE_ERROR("Unpredictable error"),
	
	CONNECTION_ERROR("Connection error"),
	SERVER_ERROR("Server error"),

	
	HTTP_SEQUENCE_ERROR("HTTP sequence error"),
	HTML_ERROR("HTML error"),
	COOKIE_ERROR("Cookie error"),

	BAD_LOGIN("Bad login"),
	
	
	MISSING_TOKEN("Missing token"),
	MISSING_APK("Missink APK"),
	MISSING_APK_NAME("Missing APK name"),
	MISSING_DESCRIPTION("Missing description"),
	MISSING_RATING("Missing rating"),
	MISSING_CATEGORY("Missing category"),
	
	BAD_TOKEN("Bad token"),
	BAD_REPO("Bad repo"),
	BAD_APK("Bad APK"),
	BAD_RATING("Bad rating"),
	BAD_CATEGORY("Bad category"),
	BAD_WEBSITE("Bad website"),
	BAD_EMAIL("Bad e-mail"),
	BAD_SCREENSHOT("Bad screenshot"),
	TOKEN_INCONSISTENT_WITH_REPO("Token inconsisten with repo"),
	
	BAD_ICON_UPLOAD("Bad icon upload"),
	BAD_SCREENSHOT_UPLOAD("Bad screenshot upload"),
	BAD_FEATURE_GRAPHIC_UPLOAD("Bad feature graphic upload"),
	BAD_APK_UPLOAD("Bad APK upload"),
	APK_TOO_BIG("APK too big"),
	APK_DUPLICATE("APK duplicated"),
	APK_INFECTED_WITH_VIRUS("APK infected with virus"),
	BLACK_LISTED("Black listed");
	
	public static EnumServerUploadApkStatus reverseOrdinal(int ordinal){
		return values()[ordinal];
	}
	
    private final String uploadDescription;
    // Reverse-lookup map for getting a day from an abbreviation
    private static final Map<String, EnumServerUploadApkStatus> lookup = new HashMap<String, EnumServerUploadApkStatus>();
    static {
        for (EnumServerUploadApkStatus d : EnumServerUploadApkStatus.values())
            lookup.put(d.getApkUploadStatus(), d);
    }

    private EnumServerUploadApkStatus(String uploadDescription) {
        this.uploadDescription = uploadDescription;
    }

    public String getApkUploadStatus() {
        return uploadDescription;
    }

    public static EnumServerUploadApkStatus get(String uploadDescription) {
        return lookup.get(uploadDescription);
    }

}
