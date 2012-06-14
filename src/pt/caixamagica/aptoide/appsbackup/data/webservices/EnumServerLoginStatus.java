/**
 * EnumServerLoginStatus,		part of aptoide
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
 * EnumServerLoginStatus, typeSafes Server Login's Status in Aptoide
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public enum EnumServerLoginStatus {
	SUCCESS,
	LOGIN_SERVICE_UNAVAILABLE,
	BAD_LOGIN,
	REPO_NOT_FROM_DEVELOPPER,
	REPO_SERVICE_UNAVAILABLE,
	BAD_REPO_PRIVACY_LOGIN;
	
	public static EnumServerLoginStatus reverseOrdinal(int ordinal){
		return values()[ordinal];
	}
}
