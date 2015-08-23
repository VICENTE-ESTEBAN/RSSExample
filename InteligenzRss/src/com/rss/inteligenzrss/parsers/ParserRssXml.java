package com.rss.inteligenzrss.parsers;


import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.rss.inteligenzrss.entities.Item;


/**
 * This class parses XML feeds from stackoverflow.com.
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class ParserRssXml {

	private static final String TAG = ParserRssXml.class.getSimpleName();

	private boolean mInItem = false;


	public ParserRssXml(){}

	public List<Item> parse(String xml) throws XmlPullParserException, IOException {

		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(new StringReader(xml));


		//String kk = readIt(in);
		return readItems(parser);

	}


	/*public String readIt(InputStream stream) {
		final char[] buffer = new char[1024];
		final StringBuilder out = new StringBuilder();
		Reader in ;
		try {
			in = new InputStreamReader(stream, "UTF-8");
			for (;;) {
				int rsz = in.read(buffer, 0, buffer.length);
				if (rsz < 0)
					break;
				out.append(buffer, 0, rsz);
			}
		}
		catch (UnsupportedEncodingException ex) {

		}
		catch (IOException ex) {

		}
		return out.toString();
	}*/

	private List<Item> readItems(XmlPullParser parser) throws XmlPullParserException, IOException{

		List<Item> _items = null;

		int evento = parser.getEventType();

		Item _newItem = null;

		while (evento != XmlPullParser.END_DOCUMENT)
		{
			switch (evento)
			{
			case XmlPullParser.START_DOCUMENT:
				_items = new ArrayList<Item>();
				break;
			case XmlPullParser.START_TAG:
			{
				String _tagName = parser.getName();
				if (_tagName.equals("item") || _tagName.equals("entry"))
				{
					mInItem = true;

					_newItem = new Item();
				}
				
				
				
				else if(mInItem)
				{

					//RSS
					if (_tagName.equals("pubDate"))
					{
						//Mon, 26 Aug 2013 14:30:13 +0000

						SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z", Locale.US);
						try{
							Date _date = sdf.parse(safeNextText(parser));
							_newItem.setDate(_date);
						}
						catch(ParseException ex)
						{
							if (ex.getMessage() != null && !ex.getMessage().isEmpty())
								Log.e(TAG, ex.getMessage(), ex);
						}
					}
					//ATOM 1.0
					else if (_tagName.equals("published"))
					{
						String _text = safeNextText(parser);

						//2015-08-21T09:34:15Z
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
						sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
						try{
							Date _date = sdf.parse(_text);
							_newItem.setDate(_date);
						}
						catch(ParseException ex)
						{
							if (ex.getMessage() != null && !ex.getMessage().isEmpty())
								Log.e(TAG, ex.getMessage(), ex);
						}

					}
					else if (_tagName.equals("link"))
					{
						String _text = safeNextText(parser);

						
						_newItem.setUrlRss(_text);
					}
					else if (_tagName.equals("description") ||
							_tagName.equals("summary") || 
							_tagName.equals("content"))
					{
						String _text = safeNextText(parser);

						boolean _isHTML = true;
						if (_tagName.equals("summary") || _tagName.equals("content"))
						{
							String _type = parser.getAttributeValue(null, "type");
							if (!TextUtils.isEmpty(_type) && _type.equals("html"))
							{
								_isHTML = true;
							}
						}


						if (_isHTML)
						{
							//inhabilitar las imágenes del html 
							String _htmlBody = _text.replaceAll("<img.+/(img)*>", "");


							//concateno el contenido porque pueden venir summary y context al mismo tiempo en el caso de ATOM 1.0
							String _oldDescription = _newItem.getDescription();
							//text con las imágenes deshabilitadas
							_newItem.setDescription(!TextUtils.isEmpty(_oldDescription)?_oldDescription.concat("\r\n").concat(_htmlBody):_htmlBody);


							////obtener las imágenes del html						
							//List<String> _images = getHtmlImage(_text);						
							//_newItem.setImages(_images);
						}
						else
							_newItem.setDescription(_text);

					}

					else if (_tagName.equals("title"))
					{
						_newItem.setTitle(safeNextText(parser));
					}
					/*else if (_tagName.equals("guid"))
	                    {
	                    //	_newItem.ssetGuid(safeNextText(parser));
	                    }*/	
				}  
			}
			break;

			case XmlPullParser.END_TAG:				
			{
				String _tagName = parser.getName();

				if (_tagName.equals("item") || _tagName.equals("entry"))
				{
					mInItem = false;

					if( _newItem != null)
					{
						_items.add(_newItem);            		
						_newItem = null;
					}
				}
			}
			break;
			}

			evento = parser.next();
		}

		//ordenar la colleción por fecha
		sortResult(_items);

		return _items;		
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

	private String safeNextText(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		String result = parser.nextText();
		if (parser.getEventType() != XmlPullParser.END_TAG) {
			parser.nextTag();
		}
		return result;
	}

	/*private List<String> getHtmlImage(String html){
		List<String> _imagesResult = new ArrayList<String>();

		String _imgRegex = "<img([\\w\\W]+?)/>";

		Pattern pImage = Pattern.compile(_imgRegex);
		Matcher mImage = pImage.matcher(html);
		while (mImage.find()) {

			 _imagesResult.add( mImage.group());
		}

		return _imagesResult;
	}*/

	/*private List<String> getHtmlImage1(String html){


		List<String> _images = new ArrayList<String>();

		try{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser _parser = factory.newPullParser();
			_parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			_parser.setInput( new StringReader ( html));
			int eventType = _parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG && "img".equals(_parser.getName())) {

					StringBuilder _sb = new StringBuilder();
					_sb.append("<html><head></head><body><img ");

					for (int _idxAttr=0;_idxAttr<_parser.getAttributeCount();_idxAttr++)
					{
						String _attName = _parser.getAttributeName(_idxAttr);
						String _attValue = _parser.getAttributeValue(_idxAttr);

						if(!_attName.equals("width") && !_attName.equals("height") && !_attName.equals("class"))
						{
							_sb.append(_attName);
							_sb.append("=");
							_sb.append("\"".concat(_attValue).concat("\" "));
						}
					}

					_sb.append("style=\"width:100%%\" ");

					_sb.append("/></body></html> ");

					_images.add(_sb.toString());
				}
				eventType = _parser.next();
			}

		} catch (XmlPullParserException ex) {
			if (ex.getMessage() != null && !ex.getMessage().isEmpty())
				Log.e(TAG, ex.getMessage(), ex);
		} catch (IOException ex) {
			if (ex.getMessage() != null && !ex.getMessage().isEmpty())
				Log.e(TAG, ex.getMessage(), ex);
		}

		return _images;
	}*/
}