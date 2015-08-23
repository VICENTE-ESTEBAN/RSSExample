package com.rss.inteligenzrss.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rss.inteligenzrss.R;
import com.rss.inteligenzrss.adapters.XmlRssAdapter;
import com.rss.inteligenzrss.entities.Item;
import com.rss.inteligenzrss.parsers.ParserRssJson;
import com.rss.inteligenzrss.parsers.ParserRssXml;
import com.rss.inteligenzrss.util.Util;

public class MainFragment extends ListFragment{
	private static final String TAG = MainFragment.class.getSimpleName();

	//nombre del fichero donde se va a grabar la última petición realizada en formato json
	private final String FILE_LASTREQUEST_JSON = "LastRequest_json";
	//nombre del fichero donde se va a grabar la última petición realizada en formato xml
	private final String FILE_LASTREQUEST_XML = "LastRequest_xml";

	private FrameLayout  mFlProgress;
	private EditText mTxtSearch;
	private ImageView mBtnSearch;
	private ImageView mBtnSettings;
	private List<Item> mItems = null;

	private SearchTask mSearchTask = null;

	private OnItemSelectedListener mlistener = null;
	public interface OnItemSelectedListener {
		public void onRssItemSelected(Item item);	
		public void onOpenSettings();
		public void onSearch();
	}



	private DownloadJSONTask mDownloadJsonTask = null;
	private DownloadXmlTask mDownloadXmlTask = null;


	public static MainFragment newInstance() { 

		MainFragment _fragment = new MainFragment();  

		Bundle args = new Bundle(); 		
		_fragment.setArguments(args);  

		return _fragment; 
	} 


	@Override
	public void onAttach(final Activity activity) {	
		super.onAttach(activity);

		if (activity instanceof OnItemSelectedListener) {
			//la actividad debe implementar OnItemSelectedListener, para que al seleccionar una rss, 
			//comunicarnos con la actividad y poder mostrar la rss selecionada
			mlistener = (OnItemSelectedListener) activity;
		} else {
			throw new ClassCastException(activity.toString() + " debe implementar MainFragment.OnItemSelectedListener");
		}


	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id) {
		super.onListItemClick(l, v, pos, id);

		XmlRssAdapter _adapter = (XmlRssAdapter) this.getListAdapter();

		Item _itemSelected = _adapter.getItem(pos);

		if (mlistener!=null)
		{
			//comunicar al listener la rss seleccionada
			mlistener.onRssItemSelected(_itemSelected);
		}


	}

	@Override
	public void onPause() {
		super.onPause();

		if(getActivity().isFinishing())
		{
			if (mDownloadXmlTask!=null 
					&& mDownloadXmlTask.getStatus() != AsyncTask.Status.FINISHED)
				mDownloadXmlTask.cancel(true);

			if (mDownloadJsonTask!=null 
					&& mDownloadJsonTask.getStatus() != AsyncTask.Status.FINISHED)
				mDownloadJsonTask.cancel(true);


			if (mSearchTask!=null 
					&& mSearchTask.getStatus() != AsyncTask.Status.FINISHED)
				mSearchTask.cancel(true);

		}
	}

	/* 
	 * When the system is ready for the Fragment to appear, this displays
	 * the Fragment's View
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment		
		super.onCreateView(inflater, container, savedInstanceState);

		View view  = inflater.inflate(R.layout.fragment_main, container, false);

		mFlProgress = (FrameLayout) view.findViewById(R.id.flProgress);

		mBtnSettings= (ImageView)view.findViewById(R.id.btnSettings);
		mBtnSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//abrir la pantalla de settings
				if (mlistener!=null)
					mlistener.onOpenSettings();
			}
		});
		
		mBtnSearch = (ImageView)view.findViewById(R.id.btnSearch);
		mBtnSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//al pulsar en el botón de búsqueda de la actionbar, realizar la búsqueda
				searchRSS();
			}
		});
		mTxtSearch = (EditText)view.findViewById(R.id.txtSearch);
	
		mTxtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		        	//al pulsar en el botón de búsqueda del softkeyboard, realizar la búsqueda
		        	searchRSS();
		            return true;
		        }
		        return false;
		    }
		});
		

		internalLoadRSS(true);

		return view;
	}

	
	public void clearSearchText(){
		this.mTxtSearch.setText("");
	}
	
	public void loadRSS(){
		//cargar y mostrar los datos de las rss del proveedor actual
		internalLoadRSS(false);
	}
	private void internalLoadRSS(boolean loadLastRequest){


		final Activity _context = getActivity();

		if(_context!=null)
		{
			if(Util.isConnected(_context))
			{
				//obtener el proveedor actual de las preferencias
				SharedPreferences _sharedPreference = PreferenceManager.getDefaultSharedPreferences(_context);
				
				String _defaultValue = getResources().getString(R.string.PreferenceDefault);
				String _newPreference = _sharedPreference.getString("PREF_LIST", _defaultValue);

				//comprobar el formato XML/JSON del proveedor de rss
				if(_newPreference.indexOf("[XML]---")!=-1)
				{        		
					String _url = _newPreference.replace("[XML]---", "");
					//RSS 2.0 o ATOM 1.0
					mDownloadXmlTask = new DownloadXmlTask();
					mDownloadXmlTask.execute(_url);
				}
				else
				{

					String _url = _newPreference.replace("[JSON]---","");
					//JSON
					mDownloadJsonTask = new DownloadJSONTask();		
					mDownloadJsonTask.execute(_url);
				}
			}
			else
			{
				//cargar los datos grabados con la última petición o simplemente mostrar un toast indicando que no se pudieron cargar los datos
				if(loadLastRequest)
				{
					//leer la última petición grabada en formato json
					String _lastRequest = Util.getStringFromSD(_context, FILE_LASTREQUEST_JSON);
					List<Item> _items = null;
					
					//existe una petición grabada json
					if(!TextUtils.isEmpty(_lastRequest))
					{
						//parsear la última petición con el parser json
						ParserRssJson _parser = new ParserRssJson();
						try {
							_items = _parser.parse(_lastRequest);
						} catch (JSONException ex) {
							if (ex.getMessage() != null && !ex.getMessage().isEmpty())
								Log.e(TAG, ex.getMessage(), ex);
						}				
					}
					else
					{
						//leer la última petición grabada en formato xml
						_lastRequest = Util.getStringFromSD(_context, FILE_LASTREQUEST_XML);		
						//existe una petición grabada xml
						if(!TextUtils.isEmpty(_lastRequest))
						{
							//parsear la última petición con el parser xml
							ParserRssXml _parser = new ParserRssXml();
							try {
								_items = _parser.parse(_lastRequest);
							} catch (XmlPullParserException | IOException ex) {		
								if (ex.getMessage() != null && !ex.getMessage().isEmpty())
									Log.e(TAG, ex.getMessage(), ex);
							}	
						}
					}
	
	
	
					mFlProgress.post(new Runnable(){
	
						@Override
						public void run() {
							mFlProgress.setVisibility(View.GONE);
						}
					});
	
					if(_items==null)
					{
						//no se ha podido descargar ninguna rss ni existe ninguna grabada. Comunicarselo al usuario
						AlertDialog.Builder builder = new AlertDialog.Builder(_context);
						builder.setMessage(getResources().getString(R.string.errorConexion));
	
						builder.setCancelable(false);
	
						builder.setPositiveButton(android.R.string.yes, new android.content.DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which) {						
	
								_context.finish();
							}						
						});
	
						AlertDialog dialog = builder.create();
						dialog.show();
					}
					else
					{
						//existen datos grabados. Mostrarselos al usuario e indicarselo con un Toast
						loadListView(_context , _items);
	
						Toast toast = Toast.makeText(_context, getResources().getString(R.string.ErrorConCarga), Toast.LENGTH_LONG);
						toast.show();
					}
				}
				else
				{
					
					Toast toast = Toast.makeText(_context, getResources().getString(R.string.errorConexion), Toast.LENGTH_LONG);
					toast.show();
				}

			}
		}
	}


	// Implementation of AsyncTask used to download XML feed from stackoverflow.com.
	private class DownloadXmlTask extends AsyncTask<String, Void, List<Item>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mFlProgress.setVisibility(View.VISIBLE);
		}
		@Override
		protected List<Item> doInBackground(String... urls) {
			Context _context  = getActivity();
			List<Item> _items = null;
			try {					
				URL url = new URL(urls[0]);


				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);

				conn.connect();

				InputStream _stream = null;

				try{

					if(!this.isCancelled())
					{
						_stream = conn.getInputStream();

						//cargar la cadena xml obtenida
						String _xml = Util.StreamToString(_stream); 

						ParserRssXml _parser = new ParserRssXml();
						_items = _parser.parse(_xml);

						//grabar la última respuesta
						Util.SaveStringToSD(_context, _xml, FILE_LASTREQUEST_XML);

						//borrar el anterior fichero JSON si existe para que sólo existe un fichero con la última petición
						Util.deletefile(_context, FILE_LASTREQUEST_JSON);
					}
				}
				catch(Exception ex)
				{
					if (ex.getMessage() != null && !ex.getMessage().isEmpty())
						Log.e(TAG, ex.getMessage(), ex);
				}
				finally{
					if(_stream!=null)
						_stream.close();
				}					


			} catch (IOException e) {
				//  return getResources().getString(R.string.connection_error);
			}


			return _items;
		}

		@Override
		protected void onPostExecute(List<Item> items) {
			Context _context  = getActivity();

			if(_context!=null)
			{

				//cargar los datos en el listview
				loadListView(_context , items);
			}
		}


	}

	private class DownloadJSONTask extends AsyncTask<String, Void, List<Item>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mFlProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<Item> doInBackground(String... urls) {
			List<Item> _items = null;


			Context _context  = getActivity();
			try {


				URL url = new URL(urls[0]);


				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);

				conn.connect();

				InputStream _stream = null;

				try{
					if(!this.isCancelled())
					{
						_stream = conn.getInputStream();

						String _json = Util.StreamToString(_stream); 

						ParserRssJson _parser = new ParserRssJson();
						_items = _parser.parse(_json);


						//grabar la última respuesta
						Util.SaveStringToSD(_context, _json, FILE_LASTREQUEST_JSON);

						//borrar el anterior fichero XML si existe  para que sólo existe un fichero con la última petición
						Util.deletefile(_context, FILE_LASTREQUEST_XML);
					}
				}
				catch(Exception ex)
				{
					if (ex.getMessage() != null && !ex.getMessage().isEmpty())
						Log.e(TAG, ex.getMessage(), ex);
				}
				finally{
					if(_stream!=null)
						_stream.close();
				}


			} catch (IOException e) {
				//  return getResources().getString(R.string.connection_error);
			}


			return _items;
		}


		@Override
		protected void onPostExecute(List<Item> items) {
			Context _context  = getActivity();

			if(_context!=null)
			{
				//cargar los datos en el listview
				loadListView(_context , items);
			}
		}

	}

	/**
	 * Realiza la búsqueda sobre el título en la colección de rss actuales 
	 * @return     
	 */
	private void searchRSS()
	{
		InputMethodManager imm = (InputMethodManager) ((Activity)getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
    	//ocultar el teclado al perder el foco
		imm.hideSoftInputFromWindow(mTxtSearch.getWindowToken(),0); 
       
        
		String criteria =  mTxtSearch.getText().toString();
		
		//realizar la búsqueda con el criterio pasado como parámetro
		if (mSearchTask!=null 
				&& mSearchTask.getStatus() != AsyncTask.Status.FINISHED)
			mSearchTask.cancel(true);

		mSearchTask = new SearchTask();
		mSearchTask.execute(criteria);
	}



	private class SearchTask extends AsyncTask<String, Void, List<Item>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mFlProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<Item> doInBackground(String... params) {
			List<Item> _items;

			String _criteria = params[0];


			if(!TextUtils.isEmpty(_criteria))
			{

				_items = new ArrayList<Item>();

				//buscar los items cuyo título contiene esa cadena
				for (Item _item:mItems)
				{
					if (_item.getTitle().toLowerCase().indexOf(_criteria)!=-1)
					{
						_items.add(_item);
					}
				}


			}
			else
			{
				_items = mItems;
			}


			return _items;
		}

		@Override
		protected void onPostExecute(List<Item> result) {

			super.onPostExecute(result);

			Context _context = getActivity();

			if(_context!=null && !this.isCancelled())
			{
				if(mlistener!=null)
					mlistener.onSearch();

				//rellenar el listview
				ListAdapter _adapter = new XmlRssAdapter(_context, result);
				setListAdapter(_adapter);		

				mFlProgress.setVisibility(View.GONE);
			}
		}

	}

	/**
	 * Carga e listview con los datos que se le pasan como parámetro
	 *
	 * * @param  context  contexto actual
	 * @param  _items  Items con los que se va a rellenar el listado
	 * @return     
	 */
	private void loadListView(Context context , List<Item> _items){
		if(context!=null)
		{
			boolean _hasResults = _items.size()>0;
			if(_hasResults)
			{
				//eliminar el color opaco inicial y poner un fondo transparente al framelayout, para que se vea el progressbar
				//y los controles inferiores en posteriores operaciones de actualización del listview
				mFlProgress.setBackgroundColor(Color.TRANSPARENT);
			}

			if(mlistener!=null)
				mlistener.onSearch();

			//rellenar el listview
			ListAdapter _adapter = new XmlRssAdapter(context, _items);
			setListAdapter(_adapter);		

			mFlProgress.setVisibility(View.GONE);

			mItems = _items;
		}
	}
	
	
}
