package com.example.gutman.shuffleparty;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.io.Serializable;
import java.util.List;

import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.Track;

public class FragmentActivity extends AppCompatActivity
{

	private BottomNavigationView navView;
	private List<Track> playlistItems = null;
	private String roomIdentifier = "";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);

		navView = findViewById(R.id.navigation);
		navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

		loadFragment(new PlaylistFragment());

		playlistItems = (List<Track>) getIntent().getSerializableExtra("pl");
		roomIdentifier = getIntent().getStringExtra("ident");

		if (roomIdentifier != null || !roomIdentifier.equals("")) {
			Bundle b = new Bundle();
			b.putString("ident", roomIdentifier);
			Fragment homeFragment = new HomeFragment();
			homeFragment.setArguments(b);

			navView.setSelectedItemId(R.id.navigation_home);
			loadFragment(homeFragment);
		}

		if (playlistItems != null)
		{
			Bundle b = new Bundle();
			b.putSerializable("pl", (Serializable) playlistItems);
			Fragment playlistFragment = new PlaylistFragment();
			playlistFragment.setArguments(b);

			navView.setSelectedItemId(R.id.navigation_playlist);

			loadFragment(playlistFragment);
		}
	}

	private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener =
			new BottomNavigationView.OnNavigationItemSelectedListener()
			{
				@Override
				public boolean onNavigationItemSelected(@NonNull MenuItem item)
				{
					Fragment fragment;
					Fragment current = getSupportFragmentManager().findFragmentById(R.id.frame_container);

					switch (item.getItemId())
					{
						case R.id.navigation_home:
							if (current instanceof HomeFragment)
								return false;
							fragment = new HomeFragment();
							loadFragment(fragment);
							return true;

						case R.id.navigation_search:
							if (current instanceof SearchFragment)
								return false;

							fragment = new SearchFragment();
							loadFragment(fragment);
							return true;

						case R.id.navigation_playlist:
							if (current instanceof PlaylistFragment)
								return false;

							fragment = new PlaylistFragment();
							loadFragment(fragment);
							return true;

						case R.id.navigation_users:
							if (current instanceof UsersFragment)
								return false;

							fragment = new UsersFragment();
							loadFragment(fragment);
							return true;
						case R.id.navigation_exit:
							Intent i = new Intent(getBaseContext(), RoomControlActivity.class);
							startActivity(i);
							finish();
							return true;
					}

					return false;
				}
			};

	private void loadFragment(Fragment f)
	{
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.frame_container, f);
		transaction.addToBackStack(null);
		transaction.commit();
	}

}
