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

package pt.aptoide.backupapps.ifaceutil;

import pt.aptoide.backupapps.R;
import android.content.Context;


/**
 * EnumAppStatus, typeSafes Aptoide's apps status
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public enum EnumAppStatus {
	INSTALLED,
	SYSTEM,
	PROTECTED,
	TOO_BIG,
	BACKED_UP,
	OTHER;
	
	public static EnumAppStatus reverseOrdinal(int ordinal){
		return values()[ordinal];
	}
	
	public String toString(Context context) {
		switch (this) {
			case INSTALLED:
				return context.getString(R.string.installed);
			case SYSTEM:
				return context.getString(R.string.system);
			case PROTECTED:
				return context.getString(R.string.protectd);
			case TOO_BIG:
				return context.getString(R.string.too_big);
			case BACKED_UP:
				return context.getString(R.string.backed_up);
				
			default:
				return "";
		}
	}
}
