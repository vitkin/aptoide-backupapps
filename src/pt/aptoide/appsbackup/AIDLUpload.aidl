/**
 * AIDLUpload,		part of Aptoide
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
package pt.aptoide.appsbackup;

/**
 * AIDLUpload, models App's upload callback AIDL IPC
 *
 * @author dsilveira
 * @since 3.0
 *
 */
interface AIDLUpload{
	
	void uploadingProgressSetCompletionTarget(in int appHashid, in int progressCompletionTarget);
	void uploadingProgressUpdate(in int appHashid, in int currentProgress);
	void uploadingProgressIndeterminate(in int appHashid);
	
	void uploadDone(in int appHashid, in int status);
	
}
