package com.rss.inteligenzrss.parsers;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.rss.inteligenzrss.entities.Item;

/**
 * This class parses XML feeds from stackoverflow.com.
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class ParserRssJson {
	private static final String TAG = ParserRssJson.class.getSimpleName();

	public ParserRssJson(){}


	public List<Item> parse(String json) throws JSONException {
		List<Item> _postResult = null;


		JSONObject _jObj  = new JSONObject(json);

		JSONObject _responseData = _jObj.optJSONObject("responseData");
		if (_responseData!=null)
		{
			_postResult = new ArrayList<Item>();
			
			JSONArray _entries = _responseData.optJSONObject("feed").optJSONArray("entries");
			if (null != _entries && _entries.length() > 0) {
				
				for (int i = 0; i < _entries.length(); i++) {
					JSONObject _entryJSON = (JSONObject) _entries.getJSONObject(i);
					
					Item _entry = new Item();
					_entry.setDescription(_entryJSON.optString("content"));
					_entry.setTitle(_entryJSON.optString("title"));
					_entry.setUrlRss(_entryJSON.optString("link"));
					//"publishedDate":"Wed, 19 Aug 2015 20:26:42 -0700",
					 SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z", Locale.US);
					 try{
						 Date _date = sdf.parse(_entryJSON.optString("publishedDate"));
						 _entry.setDate(_date);
					 }
					 catch(ParseException ex)
					 {
						 if (ex.getMessage() != null && !ex.getMessage().isEmpty())
								Log.e(TAG, ex.getMessage(), ex);
					 }
					
					_postResult.add(_entry);
				}
			}
		}
		else
		{		
			String _status = _jObj.optString("status");
			if (!TextUtils.isEmpty(_status) &&  _status.equalsIgnoreCase("ok")) {
	
				_postResult = new ArrayList<Item>();
	
				JSONArray _posts = _jObj .optJSONArray("posts");
	
				if(_posts!=null && _posts.length()>0)
				{
					for (int i = 0; i < _posts.length(); i++) {
						JSONObject _postJSON = (JSONObject) _posts.getJSONObject(i);
						Item _post = new Item();
						
						
						_post.setUrlRss(_postJSON.optString("url"));
						_post.setTitle(_postJSON.optString("title"));
						_post.setDescription(_postJSON.optString("content"));
						

						//"date":"2015-08-05 02:59:30"
						 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
						 try{
							 Date _date = sdf.parse(_postJSON.optString("date"));
							 _post.setDate(_date);
						 }
						 catch(ParseException ex)
						 {
							 if (ex.getMessage() != null && !ex.getMessage().isEmpty())
									Log.e(TAG, ex.getMessage(), ex);
						 }
								
						JSONArray _attachments = _postJSON.optJSONArray("attachments");
						if (null != _attachments && _attachments.length() > 0) {
							JSONObject _attachmentJSON = _attachments.getJSONObject(0);
							
							String _mimeType = _attachmentJSON.optString("mime_type");
							if(_mimeType.indexOf("image")!=-1)
							{
								//Attachment _attachment = new Attachment();
								//_attachment.setUrl(_attachmentJSON.optString("url"));
								
								_post.setUrlImage(_attachmentJSON.optString("url"));
							}
						}
						
						_postResult.add(_post);
					}
				}
			}
		}
		
		//ordenar la colleción por fecha
		sortResult(_postResult);
		
		return _postResult;
	}
	
	private void sortResult(List<Item> listToSort){
		if(listToSort!=null && listToSort.size()>0)
		{
			Collections.sort(listToSort, new Comparator<Item>()
					{
				@Override
				public int compare(Item o1, Item o2) {
					Date _o1Date = o1.getDate();
					Date _o2Date = o2.getDate();
					if(_o1Date!=null && _o2Date!=null)
					{
						return _o1Date.compareTo(_o2Date);
					}
					else
						return -1;
				}
					});
		}

	}
	
}
