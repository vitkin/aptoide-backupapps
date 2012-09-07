package pt.aptoide.appsbackup.ifaceutil.pagerIndicater;

import java.util.ArrayList;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;


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