package com.rss.inteligenzrss.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

public class Util {

	private static final String TAG = Util.class.getSimpleName();

	/**
	 * Comprueba si existe el fichero en el disco
	 *
	 * @param  context  Context actual de la aplicación
	 * @param  fileName Nombre del fichero
	 * @return      El fichero existe o no 
	 */
	public static boolean existsBitmapInSD(Context context, String fileName){

		//buscar en el directorio normal
		if (context!=null)
		{
			String _pathFile= context.getCacheDir().getAbsolutePath().concat(File.pathSeparator).concat(fileName);

			if (!TextUtils.isEmpty(_pathFile))
			{
				File _fileBitmap  = new File(_pathFile);

				return _fileBitmap.exists();			
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Devuelve el fichero bitmap si existe
	 *
	 * @param  context  Context actual de la aplicación
	 * @param  fileName Nombre del fichero
	 * @param reqWidth Width a la que se devolverá la imagen
	 * @param reqHeight Height a la que se devolverá la imagen
	 * @return      el fichero Bitmap
	 */
	public static Bitmap getBitmapFromSD(Context context, String fileName, Integer reqWidth, Integer reqHeight){

		Bitmap bitmap = null;

		//File mdir = RevistaApplication.getContext().getDir(getPathMagazine(idRevista), Context.MODE_PRIVATE);
		if(context!=null)
		{
			String _pathFile = context.getCacheDir().getAbsolutePath().concat(File.pathSeparator).concat(fileName);

			if (!TextUtils.isEmpty(_pathFile))
			{			
				FileInputStream fIn = null;

				int mSampleSize = 1;
				Options options = new BitmapFactory.Options();

				if (reqWidth!=null && reqHeight!=null)
				{		
					try{
						fIn  = new FileInputStream(_pathFile);

						options.inJustDecodeBounds = true;

						options.inSampleSize = mSampleSize;

						BitmapFactory.decodeStream(fIn, null, options);

						mSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

						options.inSampleSize = mSampleSize;

						// Do the actual decoding
						options.inJustDecodeBounds = false;
					} catch (FileNotFoundException e) {		
					}
					finally
					{
						if(fIn!=null)
							try {
								fIn.close();
							} catch (IOException e) {							
							}
					}
				}

				try{

					fIn  = new FileInputStream(_pathFile);

					bitmap = BitmapFactory.decodeStream(fIn, null, options);

				}
				catch (FileNotFoundException e) {

				}
				finally{		
					if(fIn!=null)
						try {
							fIn.close();
						} catch (IOException e) {						
						}
				}
			}
		}

		return bitmap;
	}

	/**
	 * Graba un fichero bitmap
	 *
	 * @param  context  Context actual de la aplicación
	 * @param  fileName Nombre del fichero
	 * @param  imagen   Bitmap que se va a grabar
	 * @param formatToCompress formato en el que se va a grabar el bitmap
	 * @return      La grabación ha tenido exito o no
	 */
	public static boolean SaveBitmapToSD( Context context, String fileName, Bitmap imagen, Bitmap.CompressFormat formatToCompress){

		boolean _isOK = true;

		if (imagen!=null && context!=null)
		{
			String _pathFile= context.getCacheDir().getAbsolutePath().concat(File.pathSeparator).concat(fileName);

			if (!TextUtils.isEmpty(_pathFile))
			{
				FileOutputStream fOut = null;

				try{
					ByteArrayOutputStream bytes = new ByteArrayOutputStream();

					imagen.compress(formatToCompress, 100, bytes);

					fOut  = new FileOutputStream(_pathFile);

					fOut.write(bytes.toByteArray());
				}
				catch(Exception ex)
				{
					if (!TextUtils.isEmpty(ex.getMessage()))
					{
						Log.e(TAG, ex.getMessage(), ex);
					}

					_isOK = false;

				}
				finally{

					if (fOut!=null)
					{
						try {
							fOut.flush();
						} catch (IOException e) {}

						try {
							fOut.close();
						} catch (IOException e) {}
					}	    
				}

				if (_isOK == false)
				{
					File _file  = new File(_pathFile);
					if (_file.exists())						
						_file.delete();
				}
			}
		}
		else
			_isOK = false;

		return _isOK;
	}

	/**
	 * Calcula la relación para obtener la imagen en modo sample
	 *
	 * @param  options  Parámetro para obtener la dimension actual del bitmap
	 * @param  reqWidth width buscada
	 * @param  reqHeight   height buscada
	 * @return      Relación para obtener la imagen en modo sample
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {

		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}

		// return (int)Math.pow(2d,
		// Math.floor(Math.log(inSampleSize)/Math.log(2d)));
		return inSampleSize;
	}
	
	
	/**
	 * Borra un fichero
	 *
	 * @param  context  Contexto actual
	 * @param  fileName nombre del fichero
	 * @return  
	 */
	public static void deletefile(Context context, String fileName){
		if(context!=null)
		{
			String _pathFile= context.getCacheDir().getAbsolutePath().concat(File.pathSeparator).concat(fileName);
			
			File _file  = new File(_pathFile);
			if(_file.exists())
				_file.delete();
		}
		
	}
	/**
	 * Devuelve el contenido de un fichero de texto
	 *
	 * @param  context  Contexto actual
	 * @param  fileName nombre del fichero
	 * @return  el contenido de un fichero de texto
	 */
	public static String getStringFromSD(Context context, String fileName){

		String _content = null;

		if(context!=null)
		{						
			String _pathFile= context.getCacheDir().getAbsolutePath().concat(File.pathSeparator).concat(fileName);
			
			File _file = new File(_pathFile);
			FileReader _reader = null;
			try {
				_reader = new FileReader(_file);
				char[] _chars = new char[(int) _file.length()];
				_reader.read(_chars);
				_content = new String(_chars);

				
			} catch (IOException ex) {
				if (ex.getMessage() != null && !ex.getMessage().isEmpty())
					Log.e(TAG, ex.getMessage(), ex); 
			} finally {
				if(_reader !=null){
					try {
					_reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}}
			}
		}
		
		return _content; 

	}

	/**
	 * Graba una cadena de texto en un fichero
	 *
	 * @param  context  Contexto actual
	 * @param  stringToSave cadena de texto que se va a grabar
	 * @param  fileName nombre del fichero
	 * @return  
	 */
	public static void SaveStringToSD(Context context, String stringToSave, String fileName){


		if(!TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(stringToSave))
		{
			String _pathFile = null;
			if(context!=null)
			{
				_pathFile= context.getCacheDir().getAbsolutePath().concat(File.pathSeparator).concat(fileName);

				boolean _isOK = true;
				FileOutputStream fos = null;
				try{
					File _file = new File(_pathFile);
					if (_file.exists())
						_file.delete();

					if(_file.createNewFile())
					{
						fos = new FileOutputStream(_pathFile);
						fos.write(stringToSave.getBytes());				
					}


				}
				catch (Exception ex) {
					if (ex.getMessage() != null && !ex.getMessage().isEmpty())
						Log.e(TAG, ex.getMessage(), ex); 

					_isOK = false;

				}
				finally
				{
					if (fos!=null)
					{
						try {
							fos.flush();
						} catch (IOException e) {			   
						}

						try {
							fos.close();
						} catch (IOException e) {			   
						}
					}
				}			

				if (_isOK == false)
				{
					File _file  = new File(_pathFile);
					if (_file.exists())						
						_file.delete();
				}
			}
		}

	}


	/** Comprueba si el móvil tiene algun modo de conexión activado
	 *
	 * @param  context  Contexto actual
	 * @return  DEvuelve si el móvil tiene algun modo de conexión activado 
	 */
	public static boolean isConnected(Context context) {
		boolean mIsConnected = false;
		if(context!=null)
		{
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);



			if (cm.getActiveNetworkInfo() != null) {
				mIsConnected = cm.getActiveNetworkInfo().isConnectedOrConnecting();
			}
			/*
			 * NetworkInfo networkInfo = null; if (connectivityManager != null) {
			 * networkInfo = connectivityManager
			 * .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			 * 
			 * } return networkInfo == null ? false : networkInfo.isConnected();
			 */
		}

		return mIsConnected;


	}
	
	
	/**Descarga una imagen de internet
	 *
	 * @param  context  Contexto actual
	 * @param  url Url desde donde se va a descargar
	 * @return      Bitmap descargado
	 */
	public static Bitmap downloadImageBitmap(Context context, String url) {
		Bitmap bmp = null;
		if (Util.isConnected(context))
		{
			InputStream is=null;

			URL urlFichero = null;

			try {
				urlFichero = new URL(url.replace(" ", "%20"));
				HttpURLConnection conn = (HttpURLConnection) urlFichero.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);

				conn.setInstanceFollowRedirects(true);

				conn.setDoInput(true);
				conn.connect();

				is = conn.getInputStream();

				/*BitmapFactory.Options mOpt = new BitmapFactory.Options();
			    mOpt.inDither=false;                     //Disable Dithering mode
			    mOpt.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
			    mOpt.inInputShareable=true;*/ 
				//mOpt.inTempStorage = new byte[32*1024];    

				bmp = BitmapFactory.decodeStream(is, null,  null);



			} catch (MalformedURLException ex) {
				if (ex.getMessage() != null && !ex.getMessage().isEmpty())
					Log.e(TAG, ex.getMessage(), ex);  

			} catch (IOException ex) {
				if (ex.getMessage() != null && !ex.getMessage().isEmpty())
					Log.e(TAG, ex.getMessage(), ex);        	
			}

			finally{

				if(is!=null)
					try {
						is.close();
					} catch (IOException e) {
					}
			}
		}


		return bmp;
	}

	/** Convierte el flujo de entrada en una cadena de caracteres
	 *
	 * @param  is  Stream de datos

	 * @return Cadena del flujo de datos
	 */
	public static String StreamToString(final InputStream is) {
		final char[] buffer = new char[1024];
		final StringBuilder out = new StringBuilder();
		Reader _in = null;
		try{
			_in = new InputStreamReader(is, "UTF-8");

			for (;;) {
				int rsz = _in.read(buffer, 0, buffer.length);
				if (rsz < 0)
					break;
				out.append(buffer, 0, rsz);
			}
		}
		catch (UnsupportedEncodingException ex) {
			/* ... */
		}
		catch (IOException ex) {
			/* ... */
		}
		finally{
			if(_in!=null)
				try {
					_in.close();
				} catch (IOException e) {						
				}
		}
		return out.toString();
	}

}
