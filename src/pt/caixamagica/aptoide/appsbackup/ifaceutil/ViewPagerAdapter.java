package pt.caixamagica.aptoide.appsbackup.ifaceutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import pt.caixamagica.aptoide.appsbackup.R;


import com.astuetz.viewpager.extensions.FixedTabsView;
import com.astuetz.viewpager.extensions.TabsAdapter;
import com.astuetz.viewpager.extensions.ViewPagerTabButton;



import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class ViewPagerAdapter extends PagerAdapter {
	
	private ArrayList<View> pages;
	
	public ViewPagerAdapter(ArrayList<View> pages) {
		this.pages=pages;
		
	}
	

	@Override
	public int getCount() {
		return pages.size();
	}

	@Override
	public Object instantiateItem(View pager, int position) {
		
		((ViewPager)pager).addView(pages.get(position),0);
		
		return pages.get(position);
	}
	
	@Override
	public void destroyItem(View pager, int position, Object view) {
		((ViewPager) pager).removeView((View) view);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}
	

}