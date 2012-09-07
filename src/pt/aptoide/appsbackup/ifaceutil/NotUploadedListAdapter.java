/*
 * NotUploadedListAdapter.java, part of appsBackup
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
package pt.aptoide.appsbackup.ifaceutil;

import java.util.ArrayList;

import pt.aptoide.appsbackup.data.display.ViewApplicationUploadFailed;
import pt.aptoide.appsbackup.R;
import pt.aptoide.appsbackup.R.id;
import pt.aptoide.appsbackup.R.layout;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NotUploadedListAdapter extends BaseAdapter{

	private ImageLoader imageLoader;

	private Context context;

	private LayoutInflater layoutInflater;

	private ArrayList<ViewApplicationUploadFailed> notUploadedNames = null;

	/**
	 * UploadingListAdapter Constructor
	 *
	 * @param context
	 */
	public NotUploadedListAdapter(Context context, ArrayList<ViewApplicationUploadFailed> notUploadedNames){
		this.context = context;
		this.notUploadedNames = notUploadedNames;
		imageLoader = new ImageLoader(context);

		layoutInflater = LayoutInflater.from(context);
	} 


	public static class NotUploadedRowViewHolder{
		TextView failed_name;
		TextView failed_status;
		ImageView failed_icon;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		NotUploadedRowViewHolder rowViewHolder;

		if(convertView == null){
			convertView = layoutInflater.inflate(R.layout.row_app_not_uploaded, null);

			rowViewHolder = new NotUploadedRowViewHolder();
			rowViewHolder.failed_name = (TextView) convertView.findViewById(R.id.failed_name);
			rowViewHolder.failed_status = (TextView) convertView.findViewById(R.id.failed_status);
			rowViewHolder.failed_icon = (ImageView) convertView.findViewById(R.id.failed_icon);
			convertView.setTag(rowViewHolder);
		}else{
			rowViewHolder = (NotUploadedRowViewHolder) convertView.getTag();
		}

		rowViewHolder.failed_name.setText(getItem(position).getAppName());
		rowViewHolder.failed_status.setText(getItem(position).getUploadStatus().toString(context));
		imageLoader.DisplayImage(getItem(position).getIconCachePath(), rowViewHolder.failed_icon);

		return convertView;
	}


	@Override
	public int getCount() {
		if(notUploadedNames != null){
			return notUploadedNames.size();
		}else{
			return 0;
		}
	}

	@Override
	public ViewApplicationUploadFailed getItem(int position) {
		return notUploadedNames.get(position);
	}

	@Override
	public long getItemId(int position) {
		return notUploadedNames.get(position).getAppHashid();
	}
}