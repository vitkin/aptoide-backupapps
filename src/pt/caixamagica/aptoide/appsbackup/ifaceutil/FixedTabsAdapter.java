package pt.caixamagica.aptoide.appsbackup.ifaceutil;

import pt.caixamagica.aptoide.appsbackup.EnumAppsLists;
import pt.caixamagica.aptoide.appsbackup.R;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;


import com.astuetz.viewpager.extensions.TabsAdapter;
import com.astuetz.viewpager.extensions.ViewPagerTabButton;

public class FixedTabsAdapter implements TabsAdapter {
	
	private Activity context;
	
	public FixedTabsAdapter(Activity context) {
		this.context = context;
	}
	
	@Override
	public View getView(int position) {
		ViewPagerTabButton tab;
		
		LayoutInflater inflater = context.getLayoutInflater();
		tab = (ViewPagerTabButton) inflater.inflate(R.layout.tab_fixed, null);
		tab.setText(EnumAppsLists.reverseOrdinal(position).toString(context));
		
		return tab;
	}
	
}
