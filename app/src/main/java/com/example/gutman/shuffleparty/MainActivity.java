package com.example.gutman.shuffleparty;

import com.example.gutman.shuffleparty.SearchResultsAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.io.Serializable;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.client.Response;

public class MainActivity extends Activity
{
	private FirebaseDatabase database;
	private DatabaseReference reference;

	private ConnectionParams connectionParams;

	private List<Track> trackList;
	private Track currentTrack;

	private SpotifyService spotify;
	private SpotifyAppRemote spotifyAppRemote;

	private static final String REDIRECT_URI = "http://example.com/callback/";
	private static String apiToken;

	private Context main;
	private SearchView searchView;
	private RecyclerView searchResults;
	private SearchResultsAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		apiToken = CredentialsHandler.getToken(this);
		if (apiToken == null)
		{
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}

		spotify = SpotifyUtils.getInstance(apiToken);
		main = this;

		initSearchbar();

		searchResults = findViewById(R.id.search_results);
		searchResults.setLayoutManager(new LinearLayoutManager(this));
		searchResults.setHasFixedSize(true);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		connectionParams =
				new ConnectionParams.Builder(SpotifyConstants.getClientID())
						.setRedirectUri(REDIRECT_URI)
						.showAuthView(true)
						.build();

		SpotifyAppRemote.connect(this, connectionParams,
				new Connector.ConnectionListener()
				{
					public void onConnected(SpotifyAppRemote mSpotifyAppRemote)
					{
						spotifyAppRemote = mSpotifyAppRemote;
						adapter = new SearchResultsAdapter(main, new SearchResultsAdapter.TrackItemSelectedListener()
						{
							@Override
							public void onItemSelected(View itemView, Track item, int position)
							{
								currentTrack = item;

								String trackUri = item.uri;

								spotifyAppRemote.getPlayerApi().play(trackUri);

								searchView.setQuery("", false);
								adapter.clearData();
								searchResults.setAdapter(adapter);

								Intent playTrackActivity = new Intent(main, PlayTrackActivity.class);
								playTrackActivity.putExtra("index", position);
								playTrackActivity.putExtra("list", (Serializable) trackList);
								startActivity(playTrackActivity);
							}
						});
					}

					public void onFailure(Throwable throwable)
					{
						Log.e("MyActivity", throwable.getMessage(), throwable);
					}
				});
	}

	private void initSearchbar(){
		searchView = findViewById(R.id.search_view);
		searchView.setQuery("query", false);

		searchView.setOnCloseListener(new SearchView.OnCloseListener()
		{
			@Override
			public boolean onClose()
			{

				return true;
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				String randomQ = SpotifyUtils.getRandomTitle();
				spotify.searchTracks(randomQ, new SpotifyCallback<TracksPager>()
				{
					@Override
					public void failure(SpotifyError spotifyError)
					{
						Log.e(main.getClass().getSimpleName(), spotifyError.getErrorDetails().toString());
					}

					@Override
					public void success(TracksPager tracksPager, Response response)
					{
						trackList = tracksPager.tracks.items;
						adapter.addData(trackList);
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
