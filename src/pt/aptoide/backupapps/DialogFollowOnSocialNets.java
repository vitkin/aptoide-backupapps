/**
 * DialogFollowOnSocialNets, part of Aptoide
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
package pt.aptoide.backupapps;

import pt.aptoide.backupapps.data.AIDLAptoideServiceData;
import pt.aptoide.backupapps.data.util.Constants;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;


/**
 * DialogFollowOnSocialNets, handles giving the user the option to follow on Social Networks
 * 
 * @author dsilveira
 *
 */
public class DialogFollowOnSocialNets extends Dialog{

	private AIDLAptoideServiceData serviceDataCaller = null;
	
	public DialogFollowOnSocialNets(Context context, AIDLAptoideServiceData serviceDataCaller) {
		super(context);
		this.serviceDataCaller = serviceDataCaller;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.social_networks);
//		setIcon(android.R.drawable.ic_menu_share);
		setContentView(R.layout.dialog_follow_on_social_nets);
		setCancelable(true);
		
		
		((Button)this.findViewById(R.id.find_facebook)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				Intent facebook;
				try {
					if(serviceDataCaller.callIsAppInstalled(Constants.FACEBOOK_PACKAGE_NAME)){
						facebook = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FACEBOOK_APTOIDE_ADDRESS));
					}else{
						facebook = new Intent(getContext(), WebViewFacebook.class);
					}
					getContext().startActivity(facebook);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		
		((Button)this.findViewById(R.id.follow_twitter)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				Intent twitter;
				try {
					if(serviceDataCaller.callIsAppInstalled(Constants.TWITTER_PACKAGE_NAME)){
						twitter = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.TWITTER_APTOIDE_URL));
					}else{
						twitter = new Intent(getContext(), WebViewTwitter.class);
					}
					getContext().startActivity(twitter);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}
}
