/*
 * UploadedListAdapter.java, part of appsBackup
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
package pt.aptoide.backupapps.ifaceutil;

import java.util.ArrayList;

import pt.aptoide.backupapps.R;
import pt.aptoide.backupapps.R.id;
import pt.aptoide.backupapps.R.layout;
import pt.aptoide.backupapps.data.display.ViewApplicationUpload;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UploadedListAdapter extends BaseAdapter{


	private ImageLoader imageLoader;

	private LayoutInflater layoutInflater;

	private ArrayList<ViewApplicationUpload> uploadedNames = null;


	public static class UploadedRowViewHolder{
		TextView uploaded_name;
		ImageView uploaded_icon;
	}

	public UploadedListAdapter(Context context, ArrayList<ViewApplicationUpload> uploadedNames){
		this.uploadedNames = uploadedNames;
		imageLoader = new ImageLoader(context);

		layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return uploadedNames.size();
	}

	@Override
	public ViewApplicationUpload getItem(int position) {
		return uploadedNames.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		UploadedRowViewHolder rowViewHolder;
		ViewApplicationUpload uploaded = this.getItem( position ); 

		if(convertView == null){
			convertView = layoutInflater.inflate(R.layout.row_app_uploaded, null);
			rowViewHolder = new UploadedRowViewHolder();
			rowViewHolder.uploaded_name = (TextView) convertView.findViewById(R.id.uploaded_name);
			rowViewHolder.uploaded_icon = (ImageView) convertView.findViewById(R.id.uploaded_icon);
			convertView.setTag(rowViewHolder);
		}else{
			rowViewHolder = (UploadedRowViewHolder) convertView.getTag();
		}

		rowViewHolder.uploaded_name.setText(uploaded.getAppName());
		imageLoader.DisplayImage(uploaded.getIconCachePath(), rowViewHolder.uploaded_icon);

		return convertView;
	}

}