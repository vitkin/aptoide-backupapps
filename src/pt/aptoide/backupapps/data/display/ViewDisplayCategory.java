/**
 * ViewDisplayCategory,		part of Aptoide's data model
 * Copyright (C) 2011  Duarte Silveira
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

package pt.aptoide.backupapps.data.display;

import java.io.Serializable;
import java.util.LinkedList;

import pt.aptoide.backupapps.data.util.Constants;

import android.os.Parcel;
import android.os.Parcelable;

 /**
 * ViewDisplayCategory, models a Category, and its sub-categories
 * 			 				maintains insertion order
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ViewDisplayCategory implements Parcelable, Serializable{ 

	private static final long serialVersionUID = 1412655092087101812L;
	private LinkedList<ViewDisplayCategory> subCategories;
	private ViewDisplayCategory parentCategory;
	
	private String categoryName;
	private int categoryHashid;
	private int availableApps;
	
	/**
	 * ViewDisplayCategory Constructor
	 */
	public ViewDisplayCategory(String categoryName, int categoryHashid, int availableApps) {
		this.subCategories = new LinkedList<ViewDisplayCategory>();
		this.parentCategory = null;
		this.categoryName = categoryName;
		this.categoryHashid = categoryHashid;
		this.availableApps = availableApps;
	}
	
	private void setParentCategory(ViewDisplayCategory parentCategory){
		this.parentCategory = parentCategory;
	}
	
	public ViewDisplayCategory getParentCategory(){
		return this.parentCategory;
	}
	
	public void addSubCategory(ViewDisplayCategory subCategory){
		subCategory.setParentCategory(this);
		this.availableApps += subCategory.availableApps;
		this.subCategories.add(subCategory);		
	}
	
	public ViewDisplayCategory getSubCategory(int categoryHashid){
		ViewDisplayCategory category = null;
		for (ViewDisplayCategory subcategory : subCategories) {
			if(subcategory.getCategoryHashid() == categoryHashid){
				category = subcategory;
				break;
			}
		}
		return category;
	}	
	
	public String getCategoryName() {
		return categoryName;
	}

	public int getCategoryHashid() {
		return categoryHashid;
	}

	public int getAvailableApps() {
		return availableApps;
	}
	
	public boolean hasChildren(){
		return (subCategories.size() > 0);
	}

	
	public LinkedList<ViewDisplayCategory> getSubCategories(){
		return subCategories;
	}
	

	/**
	 * ViewDisplayListApps object reuse, clean references
	 */
	public void clean(){
		this.subCategories = null;
		this.parentCategory = null;
		this.categoryName = null;
		this.categoryHashid = Constants.EMPTY_INT;
		this.availableApps = Constants.EMPTY_INT;
	}

	/**
	 * ViewDisplayListApps object reuse, reConstructor
	 * 
	 * @param int size
	 */
	public void reuse(String categoryName, int categoryHashid, int availableApps) {
		this.subCategories = new LinkedList<ViewDisplayCategory>();
		this.parentCategory = null;
		this.categoryName = categoryName;
		this.categoryHashid = categoryHashid;
		this.availableApps = availableApps;
	}


	@Override
	public String toString() {
		StringBuilder listApps = new StringBuilder("Category: "+categoryName+" subCategories: ");
		for (ViewDisplayCategory subCategory : subCategories) {
			listApps.append(subCategory.getCategoryHashid()+" - "+subCategory.getCategoryName()+" ");
		}
		return listApps.toString();
	}

	
	@Override
	public int hashCode() {
		return categoryHashid;
	}

	
	@Override
	public boolean equals(Object object) {
		if(object instanceof ViewDisplayCategory){
			ViewDisplayCategory category = (ViewDisplayCategory) object;
			if(category.hashCode() == this.hashCode()){
				return true;
			}
		}
		return false;
	}
	
	
	
		
	
	// Parcelable stuff //
	


	public static final Parcelable.Creator<ViewDisplayCategory> CREATOR = new Parcelable.Creator<ViewDisplayCategory>() {
		public ViewDisplayCategory createFromParcel(Parcel in) {
			return new ViewDisplayCategory(in);
		}

		public ViewDisplayCategory[] newArray(int size) {
			return new ViewDisplayCategory[size];
		}
	};

	/** 
	 * we're annoyingly forced to create this even if we clearly don't need it,
	 *  so we just use the default return 0
	 *  
	 *  @return 0
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	private ViewDisplayCategory(Parcel in){
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(parentCategory, flags);
		
//		int size = this.subCategories.size();
//		out.writeInt(size);
//		for(int i=0; i<size; i++){
//			out.writeParcelable(this.subCategories.get(i),flags);
//		}
		out.writeSerializable(subCategories);
		
		out.writeString(categoryName);
		out.writeInt(categoryHashid);
		out.writeInt(availableApps);
	}

	@SuppressWarnings("unchecked")
	public void readFromParcel(Parcel in) {
		parentCategory = in.readParcelable(getClass().getClassLoader());
		
//		subCategories.clear();
//		int size = in.readInt();
//		for(int i=0; i<size; i++){
//			this.subCategories.add((ViewDisplayCategory) in.readParcelable(ViewDisplayCategory.class.getClassLoader()));
//		}
		subCategories = (LinkedList<ViewDisplayCategory>) in.readSerializable();
		
		categoryName = in.readString();
		categoryHashid = in.readInt();
		availableApps = in.readInt();
	}
	
}
