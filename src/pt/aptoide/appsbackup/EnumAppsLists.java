/**
 * EnumAppsLists,		part of aptoide
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

package pt.aptoide.appsbackup;

import pt.aptoide.appsbackup.R;
import android.content.Context;

/**
 * EnumAppsLists, typeSafes Aptoide's apps lists switching
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public enum EnumAppsLists {
	BACKUP,
	RESTORE;
	
	public static EnumAppsLists reverseOrdinal(int ordinal){
		return values()[ordinal];
	}
	
	public static EnumAppsLists getNext(EnumAppsLists current){
		if(current.ordinal()<1){
			return values()[current.ordinal()+1];
		}else{
			return current;
		}
	}
	
	public static EnumAppsLists getPrevious(EnumAppsLists current){
		if(current.ordinal()>0){
			return values()[current.ordinal()-1];
		}else{
			return current;
		}
	}
	
	public static int getCount(){
		return values().length-1;
	}
	
	public String toString(Context context){
		switch (this) {
			case BACKUP:
				return context.getString(R.string.backup);
			case RESTORE:
				return context.getString(R.string.restore);
	
			default:
				return "";
		}
	}
}
