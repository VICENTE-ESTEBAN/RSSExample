package com.rss.inteligenzrss.entities;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable{
	private String title;//<![CDATA
	private String description;//html	
	private String urlImage;
	private String urlRss;
	private Date date;
	
	
	
	public String getUrlRss() {
		return urlRss;
	}
	public void setUrlRss(String urlRss) {
		
		if (!urlRss.startsWith("http://") && !urlRss.startsWith("https://"))
			urlRss = "http://" + urlRss;
		
		this.urlRss = urlRss;
	}

	
	 public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	


	public String getUrlImage() {
		return urlImage;
	}
	public void setUrlImage(String urlImage) {
		this.urlImage = urlImage;
	}
	
	

	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	
	public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {

		public Item createFromParcel(Parcel in) {
			return new Item(in);
		}

		public Item[] newArray(int size) {
			return new Item[size];
		}

	};
	
	public Item()
	{}
	public Item(Parcel in )
	{
		title = in.readString();
		description = in.readString();
		urlImage = in.readString();
		urlRss = in.readString();
		
		long _tmpDate = in.readLong();
		if(_tmpDate!=-1)
			this.date = new Date(in.readLong());
		else
			this.date = null;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		dest.writeString(title);
		dest.writeString(description);
		dest.writeString(urlImage);
		dest.writeString(urlRss);
		
		if(date!=null)
			dest.writeLong(this.date.getTime());
		else
			dest.writeLong(-1);
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
}
