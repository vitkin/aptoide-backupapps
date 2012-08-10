/*
 * InterfaceAvailableAppsAdapter, part of appsBackup
 * Copyright (C) 2012 Duarte Silveira
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

import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayApplication;
import pt.caixamagica.aptoide.appsbackup.data.model.ViewListIds;
import android.view.View;
import android.view.ViewGroup;

/**
 * InterfaceAvailableAppsAdapter
 *
 * @author dsilveira
 *
 */
public interface InterfaceAvailableAppsAdapter {

	public abstract View getView(final int position, View convertView, ViewGroup parent);

	public abstract int getCount();

	public abstract ViewDisplayApplication getItem(int position);

	public abstract long getItemId(int position);

	public abstract void toggleSelectAll();

	public abstract void selectAll();

	public abstract void unselectAll();

	public abstract ViewListIds getSelectedIds();

	public abstract void resetDisplayAvailable();

	public abstract void refreshDisplayAvailable();

	public abstract void shutdownNow();
	
	public abstract boolean isDynamic();

}