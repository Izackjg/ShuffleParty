package com.example.gutman.shuffleparty;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.io.Serializable;
import java.util.List;

import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.Track;

public class FragmentActivity extends AppCompatActivity
{
	private BottomNavigationView navView;
	private String roomIdentifier;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);

		navView = findViewById(R.id.navigation);
		navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

		roomIdentifier = getIntent().getStringExtra("ident");

		Bundle b = new Bundle();
		b.putString("ident", roomIdentifier);

		if (b.getString("ident") != null)
		{
			Fragment homeFragment = new HomeFragment();
			homeFragment.setArguments(b);
			navView.setSelectedItemId(R.id.navigation_home);
			addFragment(homeFragment);
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

					Bundle b = new Bundle();
					b.putString("ident", roomIdentifier);

					switch (item.getItemId())
					{
						case R.id.navigation_home:
							if (current instanceof HomeFragment)
								return false;

							fragment = new HomeFragment();
							fragment.setArguments(b);
							loadFragment(fragment);
							return true;

						case R.id.navigation_search:
							if (current instanceof SearchFragment)
								return false;

							fragment = new SearchFragment();
							fragment.setArguments(b);
							loadFragment(fragment);
							return true;

						case R.id.navigation_playlist:
							if (current instanceof PlaylistFragment)
								return false;

							fragment = new PlaylistFragment();
							fragment.setArguments(b);
							loadFragment(fragment);
							return true;

						case R.id.navigation_users:
							if (current instanceof UsersFragment)
								return false;

							fragment = new UsersFragment();
							fragment.setArguments(b);
							loadFragment(fragment);
							return true;
						case R.id.navigation_exit:
							FirebaseUtils.deleteRoomFromDatabase(roomIdentifier);
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
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();

		transaction.replace(R.id.frame_container, f);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	private void addFragment(Fragment f)
	{
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();

		transaction.add(R.id.frame_container, f);
		transaction.addToBackStack(null);
		transaction.commit();
	}

}
