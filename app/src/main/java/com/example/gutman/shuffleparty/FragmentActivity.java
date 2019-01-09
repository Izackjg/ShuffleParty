package com.example.gutman.shuffleparty;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.io.Serializable;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class FragmentActivity extends AppCompatActivity
{

	private BottomNavigationView navView;
	private List<Track> playlistItems = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);

		navView = findViewById(R.id.navigation);
		navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

		loadFragment(new PlaylistFragment());

		playlistItems = (List<Track>) getIntent().getSerializableExtra("pl");
		if (playlistItems != null) {
			Bundle b = new Bundle();
			b.putSerializable("pl", (Serializable) playlistItems);
			Fragment playlistFragment = new PlaylistFragment();
			playlistFragment.setArguments(b);

			getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, playlistFragment).commit();
		}
	}

	private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener =
			new BottomNavigationView.OnNavigationItemSelectedListener()
			{
				@Override
				public boolean onNavigationItemSelected(@NonNull MenuItem item)
				{
					Fragment fragment;

					switch (item.getItemId()) {
						case R.id.navigation_search:
							fragment = new SearchFragment();
							loadFragment(fragment);
							return true;
						case R.id.navigation_playlist:
							fragment = new PlaylistFragment();
							loadFragment(fragment);
							return true;
						case R.id.navigation_users:
							fragment = new UsersFragment();
							loadFragment(fragment);
							return true;
					}

					return false;
				}
			};

	private void loadFragment(Fragment f) {
		FragmentTransaction transaction =  getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.frame_container, f);
		transaction.addToBackStack(null);
		transaction.commit();
	}

}
