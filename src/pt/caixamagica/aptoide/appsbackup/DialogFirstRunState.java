/**
 * DialogFirstRunState, part of Aptoide
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
package pt.caixamagica.aptoide.appsbackup;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


/**
 * DialogFirstRunState, handles informing user of how to get going, when there is no login configured
 * 
 * @author dsilveira
 *
 */
public class DialogFirstRunState extends Dialog{
	
	
	public DialogFirstRunState(Context context) {
		super(context);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.first_run);
		setContentView(R.layout.dialog_first_run_state);
		
		
		((Button)this.findViewById(R.id.later)).setOnClickListener(new View.OnClickListener(){
			
			public void onClick(View v) {
 				dismiss();
			}
			
		});
		
		((Button)this.findViewById(R.id.setup_now)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
 				Intent login = new Intent(getContext(), BazaarLogin.class);
				login.putExtra("InvoqueType", BazaarLogin.InvoqueType.NO_CREDENTIALS_SET.ordinal());
				getContext().startActivity(login);
				dismiss();
			}
		});
		
	}
}
