/*
 * UploadingListAdapter.java, part of appsBackup
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

import java.util.ArrayList;

import pt.caixamagica.aptoide.appsbackup.R;
import pt.caixamagica.aptoide.appsbackup.R.id;
import pt.caixamagica.aptoide.appsbackup.R.layout;
import pt.caixamagica.aptoide.appsbackup.data.display.ViewApplicationUploading;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UploadingListAdapter extends BaseAdapter{

	private ImageLoader imageLoader;
	
	private LayoutInflater layoutInflater;

	private ArrayList<ViewApplicationUploading> uploadingProgress = null;

	/**
	 * UploadingListAdapter Constructor
	 *
	 * @param context
	 * @param ArrayList<HashMap<String, Integer>> uploadingProgress
	 */
	public UploadingListAdapter(Context context, ArrayList<ViewApplicationUploading> uploadingProgress){
		this.uploadingProgress = uploadingProgress;
		imageLoader = new ImageLoader(context);

		layoutInflater = LayoutInflater.from(context);

	} 

	public static class UploadingRowViewHolder{
		TextView app_name;
		ProgressBar app_progress;
		ImageView app_icon;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		UploadingRowViewHolder rowViewHolder;

		if(convertView == null){
			convertView = layoutInflater.inflate(R.layout.row_app_uploading, null);

			rowViewHolder = new UploadingRowViewHolder();
			rowViewHolder.app_name = (TextView) convertView.findViewById(R.id.uploading_name);
			rowViewHolder.app_progress = (ProgressBar) convertView.findViewById(R.id.uploading_progress);
			rowViewHolder.app_icon = (ImageView) convertView.findViewById(R.id.uploading_icon); 
			convertView.setTag(rowViewHolder);
		}else{
			rowViewHolder = (UploadingRowViewHolder) convertView.getTag();
		}

		ViewApplicationUploading upload = uploadingProgress.get(position);

		rowViewHolder.app_name.setText(upload.getAppName());
		if(upload.getAppProgress() != 0 && upload.getAppProgress() < 99){
			rowViewHolder.app_progress.setIndeterminate(false);
			rowViewHolder.app_progress.setMax(100);
		}else{
			rowViewHolder.app_progress.setIndeterminate(true);
		}
		rowViewHolder.app_progress.setProgress(upload.getAppProgress());
		imageLoader.DisplayImage(upload.getIconCachePath(), rowViewHolder.app_icon);

		return convertView;
	}


	@Override
	public int getCount() {
		if(uploadingProgress != null){
			return uploadingProgress.size();
		}else{
			return 0;
		}
	}

	@Override
	public ViewApplicationUploading getItem(int position) {
		return uploadingProgress.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}