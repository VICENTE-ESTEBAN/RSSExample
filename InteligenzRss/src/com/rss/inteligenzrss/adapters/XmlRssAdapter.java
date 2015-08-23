package com.rss.inteligenzrss.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rss.inteligenzrss.R;
import com.rss.inteligenzrss.entities.Item;
import com.rss.inteligenzrss.util.Util;

public class XmlRssAdapter extends BaseAdapter{

	private List<Item> mItems; 
	private Context mContext;
	public XmlRssAdapter(Context context, List<Item> items){
		this.mItems = items;
		this.mContext = context;
	}
	
	@Override
	public int getCount() {
		if(mItems!=null)
			return mItems.size();
		else
			return 0;
	}

	@Override
	public Item getItem(int position) {
		if(mItems!=null)
			return mItems.get(position);
		else
			return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final ViewHolder holder;
		if (convertView == null)
		{
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_rss, parent, false);

			holder = new ViewHolder();

			
			
			holder.txtTitulo = (TextView) convertView.findViewById(R.id.txtTitulo);	
			
			holder.txtDescripcion = (TextView) convertView.findViewById(R.id.txtDescripcion);
			holder.imgImage= (ImageView) convertView.findViewById(R.id.imgImage);
			
			
			convertView.setTag(holder);
		} else 
		{
			holder = (ViewHolder)convertView.getTag();
		}	
		
		
		holder.position = position;

		
		final Item _item = mItems.get(position);
		holder.txtTitulo.setText(_item.getTitle());
		
		
		
		
		//en principio deberá poner el detalle de la noticia en 2 líneas, pero es html...como pongo resumo en 2 lineas ese html...
		//si me decis como lo quereis lo modifico
		holder.txtDescripcion.setText("");	
		//holder.txtDescripcion.setText(Html.fromHtml(_item.getDescription()));	
		
		//si hay imagen mostrarla
		if (!TextUtils.isEmpty(_item.getUrlImage()))
		{
			
			new ThumbnailTask(position, _item.getUrlImage(), holder).execute();
		}
		else
		{
			holder.imgImage.setVisibility(View.GONE);
		}

		
		//modificar el color de la celda del listado si es par o impar
		if (position%2==0)
			convertView.setBackgroundColor(Color.parseColor("#7DADD8E6"));
		else
			convertView.setBackgroundColor(Color.parseColor("#7DC0C0C0"));
		
		return convertView;
	}
	
	
	
	class ViewHolder{
		TextView txtTitulo;        
        TextView txtDescripcion;
        ImageView imgImage;
        
        int position;
	} 
	
	
	class ThumbnailTask extends AsyncTask<Void,Void,Bitmap> 
	{
		int position;
		String url;
		ViewHolder holder;



		public ThumbnailTask( int position, String url, ViewHolder holder) {
			this.position = position;
			this.url = url;
			this.holder = holder;
		}

		@Override
		protected void onPreExecute() {			
			super.onPreExecute();

			holder.imgImage.setVisibility(View.INVISIBLE);

			holder.imgImage.setImageDrawable(null);
		}

		@Override
		protected Bitmap doInBackground(Void... params) {

			if (this.isCancelled())
				return null;
			final String _fileName = String.valueOf(url.hashCode());

			Bitmap bmpFoto = null;
			//comprobar si existe en la sd
			if (Util.existsBitmapInSD(mContext, _fileName))
			{
				if (this.isCancelled())
					return null;

				//obtener la imagen con las dimensiones que necesitamos en el listado
				bmpFoto = Util.getBitmapFromSD(
						mContext,  
						_fileName, 
						mContext.getResources().getDimensionPixelSize(R.dimen.imagen_listado_width),
						mContext.getResources().getDimensionPixelSize(R.dimen.imagen_listado_height));
			}
			else{
				//descargar de internet la imagen
				bmpFoto = Util.downloadImageBitmap(mContext, url);

				if (bmpFoto != null) {
					//grabar la imagen en la sd
					Util.SaveBitmapToSD(mContext, _fileName, bmpFoto , Bitmap.CompressFormat.PNG);

					bmpFoto.recycle();
					bmpFoto = null;

					if (this.isCancelled())
						return null;

					//obtener la imagen con las dimensiones que necesitamos en el listado
					bmpFoto = Util.getBitmapFromSD(
							mContext, 
							_fileName, 
							mContext.getResources().getDimensionPixelSize(R.dimen.imagen_listado_width),
							mContext.getResources().getDimensionPixelSize(R.dimen.imagen_listado_height));
				}
			}

			return bmpFoto;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {

			super.onPostExecute(bitmap);


			//comprobar que la posicion del listitem sigue siendo la misma para mostrar la imagen
			if (holder.position == position && bitmap!=null) {				

				holder.imgImage.setImageBitmap(bitmap);
				holder.imgImage.invalidate();
				
				holder.imgImage.setVisibility(View.VISIBLE);	
			}

			
		}
	}

	
}
