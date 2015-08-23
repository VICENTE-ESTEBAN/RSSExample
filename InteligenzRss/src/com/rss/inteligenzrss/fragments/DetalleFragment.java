package com.rss.inteligenzrss.fragments;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rss.inteligenzrss.R;
import com.rss.inteligenzrss.entities.Item;
import com.rss.inteligenzrss.util.Util;

public class DetalleFragment extends Fragment{
	private static final String TAG = DetalleFragment.class.getSimpleName();

	public interface OnItemSelectedListener {
		public void onRssItemSelected(Item item);
	}


	//private static final String TAG = DetalleFragment.class.getSimpleName();

	private static String REQUEST_PARAM_ITEM = "itemFragment";
	ThumbnailTask mThumbnailTask;
	private ImageView mImgImage;
	private Item mItem = null;
	public static DetalleFragment newInstance(Item item) { 

		DetalleFragment _fragment = new DetalleFragment();  

		Bundle args = new Bundle();
		args.putParcelable(REQUEST_PARAM_ITEM, item);
		_fragment.setArguments(args);  

		return _fragment; 
	} 

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) 
		{
			mItem = savedInstanceState.getParcelable(REQUEST_PARAM_ITEM);	


		}
		else
		{
			mItem = getArguments().getParcelable(REQUEST_PARAM_ITEM);
		}

		fillControls();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		if(getActivity().isFinishing())
			if (mThumbnailTask!=null)
				mThumbnailTask.cancel(true);
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

		View view  = inflater.inflate(R.layout.fragment_detalle_rss, container, false);



		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		outState.putParcelable(DetalleFragment.REQUEST_PARAM_ITEM, mItem);
	}

	private void fillControls(){
		TextView _txtTitulo  = (TextView) getView().findViewById(R.id.txtTitulo);
		final TextView  _txtDescripcion = (TextView) getView().findViewById(R.id.txtDescripcion);
		mImgImage  = (ImageView) getView().findViewById(R.id.imgImage);


		Button _btnVerRssNavegador  = (Button) getView().findViewById(R.id.btnVerRssNavegador);

		if (!TextUtils.isEmpty(mItem.getUrlRss()))
		{
			_btnVerRssNavegador.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Context _context = getActivity();
					if(_context!=null)
					{
						if(Util.isConnected(_context))
						{
							try {


								Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mItem.getUrlRss()));
								startActivity(myIntent);
							} catch (ActivityNotFoundException ex) {

								if (ex.getMessage() != null && !ex.getMessage().isEmpty())
									Log.e(TAG, "No se pudo abrir ".concat(mItem.getUrlRss()), ex);

								Toast.makeText(getActivity(), getResources().getString(R.string.URLIncorrecta),  Toast.LENGTH_LONG).show();

							}
						}

						else
						{
							Toast toast = Toast.makeText(_context, getResources().getString(R.string.errorConexion), Toast.LENGTH_LONG);
							toast.show();
						}
					}
				}

			});
		}
		else
		{
			_btnVerRssNavegador.setVisibility(View.GONE);
		}


		_txtTitulo.setText(mItem.getTitle());

		_txtDescripcion.post(new Runnable(){

			@Override
			public void run() {
				_txtDescripcion.setText(Html.fromHtml(mItem.getDescription()));
			}        	
		});


		if(!TextUtils.isEmpty(mItem.getUrlImage()))
		{
			mThumbnailTask = new ThumbnailTask();
			mThumbnailTask.execute(mItem.getUrlImage());
		}
		else
			mImgImage.setVisibility(View.GONE);
	}


	class ThumbnailTask extends AsyncTask<String, Void,Bitmap> 
	{

		public ThumbnailTask() {
		}

		@Override
		protected void onPreExecute() {			
			super.onPreExecute();

			mImgImage.setVisibility(View.INVISIBLE);

			mImgImage.setImageDrawable(null);
		}

		@Override
		protected Bitmap doInBackground(String... params) {

			Context _context = getActivity();

			String _url = params[0];

			if (this.isCancelled())
				return null;

			final String _fileName = String.valueOf(params[0].hashCode());

			Bitmap bmpFoto = null;

			if (Util.existsBitmapInSD(_context, _fileName))
			{
				if (this.isCancelled())
					return null;

				bmpFoto = Util.getBitmapFromSD(
						getActivity(),  
						_fileName, 
						getResources().getDimensionPixelSize(R.dimen.imagen_ficha_detalle_width),
						getResources().getDimensionPixelSize(R.dimen.imagen_ficha_detalle_height));
			}
			else{				
				bmpFoto = Util.downloadImageBitmap(_context, _url);

				if (bmpFoto != null) {

					Util.SaveBitmapToSD(_context, _fileName, bmpFoto , Bitmap.CompressFormat.PNG);

					bmpFoto.recycle();
					bmpFoto = null;

					bmpFoto = Util.getBitmapFromSD(
							_context, 
							_fileName, 
							_context.getResources().getDimensionPixelSize(R.dimen.imagen_ficha_detalle_width),
							_context.getResources().getDimensionPixelSize(R.dimen.imagen_ficha_detalle_height));
				}
			}

			return bmpFoto;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {

			super.onPostExecute(bitmap);


			//you can check if the target row for the view is still the same when the async operation finishes
			if (getActivity()!=null && bitmap!=null) {				
				mImgImage.setImageBitmap(bitmap);
				mImgImage.invalidate();

				mImgImage.setVisibility(View.VISIBLE);	
			}


		}
	}

}
