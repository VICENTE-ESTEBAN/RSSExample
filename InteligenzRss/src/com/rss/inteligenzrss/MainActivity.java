package com.rss.inteligenzrss;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.rss.inteligenzrss.entities.Item;
import com.rss.inteligenzrss.fragments.DetalleFragment;
import com.rss.inteligenzrss.fragments.MainFragment;

public class MainActivity extends Activity implements
		MainFragment.OnItemSelectedListener {

	private static final int PREFERENCE_REQUEST = 0;

	FrameLayout mFlContent = null;

	private String TAGNAME_DETALLE = "fragmentDetalle";
	private MainFragment mMainFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mFlContent = (FrameLayout) this.findViewById(R.id.flContent);

		mMainFragment = (MainFragment) getFragmentManager().findFragmentById(
				R.id.main_fragment);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PREFERENCE_REQUEST) {
			if (resultCode == RESULT_OK) {

				clearDetalle();

				//limpiar el criterio de búsqueda antes de cargar las rss del nuevo proveedor
				mMainFragment.clearSearchText();
				
				// cargar las rss del nuevo proveedor
				mMainFragment.loadRSS();
			}
		}
	}

	@Override
	public void onRssItemSelected(Item item) {

		if (mFlContent != null) {
			// en el caso de tablets, añadir a la derecha de la pantalla el
			// detalle de la rss seleccionada
			final FragmentManager fm = getFragmentManager();

			FragmentTransaction ft = fm.beginTransaction();

			DetalleFragment _fragment = DetalleFragment.newInstance(item);

			ft.replace(R.id.flContent, _fragment, TAGNAME_DETALLE);

			ft.commit();
		} else {
			// en el caso de móviles, abrir un nuevo formulario con el detalle
			// de la rss seleccionada
			Intent _intent = new Intent(this, DetalleActivity.class);
			_intent.putExtra(DetalleActivity.REQUEST_PARAM_ITEM, item);

			startActivity(_intent);
		}

	}

	@Override
	public void onOpenSettings() {
		// abrir la actividad de Preferencias
		Intent _intent = new Intent(this, PrefActivity.class);
		this.startActivityForResult(_intent, PREFERENCE_REQUEST);
	}

	//eliminar el detalle actual en el caso de ser tablet
	private void clearDetalle(){
		if (mFlContent != null) {
			final FragmentManager fm = getFragmentManager();

			FragmentTransaction ft = fm.beginTransaction();

			Fragment _fragment = fm.findFragmentByTag(TAGNAME_DETALLE);
			if (_fragment != null)
				ft.remove(_fragment);

			ft.commit();
		}

	}

	@Override
	public void onSearch() {
		clearDetalle();
	}

}
