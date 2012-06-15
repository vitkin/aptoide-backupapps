/**
 * EnumServerLogiCreatenStatus,		part of aptoide
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

/**
 * EnumServerLoginCreateStatus, typeSafes Server Login's Creation Status in Aptoide
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public enum EnumServerLoginCreateStatus {
	SUCCESS,
	REPO_SERVICE_UNAVAILABLE,
	LOGIN_CREATE_SERVICE_UNAVAILABLE,
	USERNAME_NOT_PROPER_EMAIL,
	BAD_PASSWORD_HASH,
	BAD_HMAC,
	BAD_USER_AGENT,
	USERNAME_ALREADY_REGISTERED,
	MISSING_PARAMETER,
	UNKNOWN_USERNAME,				// on nickname update
	BAD_LOGIN, 						// on nickname update
	BAD_REPO_NAME,					
	REPO_REQUIRES_AUTHENTICATION,
	REPO_ALREADY_EXISTS,
	REPO_NOT_FROM_DEVELOPPER,
	BAD_REPO_PRIVACY_LOGIN,
	SERVER_ERROR;
	
	public static EnumServerLoginCreateStatus reverseOrdinal(int ordinal){
		return values()[ordinal];
	}
}
