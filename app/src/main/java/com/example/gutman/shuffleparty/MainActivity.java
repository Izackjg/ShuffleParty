package com.example.gutman.shuffleparty;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity
{
	private ConnectionParams connectionParams;

	private List<Track> playlistItems = new ArrayList<>();

	private SpotifyService spotify;
	private SpotifyAppRemote spotifyAppRemote;

	private static String apiToken;

	private Context main;
	private SearchView searchView;
	private RecyclerView searchResults;
	private SpotifyTrackAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		main = this;

		String apiToken = CredentialsHandler.getToken(this);
		if (apiToken.equals("")) {
			Intent i = new Intent(this, LoginActivity.class);
			startActivity(i);
			finish();
		}
		spotify = SpotifyUtils.getInstance(apiToken);

		initSearchbar();

		searchResults = findViewById(R.id.search_results);
		searchResults.setLayoutManager(new LinearLayoutManager(this));
		searchResults.setHasFixedSize(true);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		connectionParams = SpotifyUtils.getParams();

		SpotifyAppRemote.connect(this, connectionParams,
				new Connector.ConnectionListener()
				{
					public void onConnected(SpotifyAppRemote mSpotifyAppRemote)
					{
						spotifyAppRemote = mSpotifyAppRemote;

						adapter = new SpotifyTrackAdapter(main, new SpotifyTrackAdapter.TrackSelectedListener()
						{
							@Override
							public void onItemSelected(View itemView, Track item, int position)
							{
								FirebaseUtils.addTrackToDatabase(item);

								playlistItems.add(item);

								searchView.setQuery("", false);
								adapter.clearData();
								searchResults.setAdapter(adapter);

								if (playlistItems.size() >= 3)
								{
									Intent playlistActivity = new Intent(main, PlaylistActivity.class);
									playlistActivity.putExtra("pl", (Serializable) playlistItems);
									startActivity(playlistActivity);
								}
							}
						});
					}

					public void onFailure(Throwable throwable)
					{
						Log.e("MyActivity", throwable.getMessage(), throwable);
					}
				});
	}

	private void initSearchbar()
	{
		searchView = findViewById(R.id.search_view);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(final String query)
			{
				if (adapter.getItemCount() != 0)
				{
					adapter.clearData();
					searchResults.setAdapter(adapter);
				}

				if (query.isEmpty())
					return false;

				spotify.searchTracks(query, new SpotifyCallback<TracksPager>()
				{
					@Override
					public void failure(SpotifyError spotifyError)
					{
						Log.e(main.getClass().getSimpleName(), spotifyError.getErrorDetails().toString());
					}

					@Override
					public void success(TracksPager tracksPager, Response response)
					{
						adapter.addData(tracksPager.tracks.items);
						searchResults.setAdapter(adapter);
					}
				});
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				return false;
			}
		});
	}
}