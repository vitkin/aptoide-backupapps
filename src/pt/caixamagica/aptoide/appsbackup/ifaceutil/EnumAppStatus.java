/**
 * EnumAppStatus,		part of aptoide
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

package pt.caixamagica.aptoide.appsbackup.ifaceutil;


/**
 * EnumAppStatus, typeSafes Aptoide's apps status
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public enum EnumAppStatus {
	Installed,
	BackedUp,
	Other;
	
	public static EnumAppStatus reverseOrdinal(int ordinal){
		return values()[ordinal];
	}
	
	@Override
	public String toString() {
		switch (this) {
			case Installed:
				return super.toString();
	
			case BackedUp:
				return "Backed up";
				
			default:
				return "";
		}
	}
}
