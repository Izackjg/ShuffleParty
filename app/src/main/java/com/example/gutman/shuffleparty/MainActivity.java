package com.example.gutman.shuffleparty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.client.Response;


public class MainActivity extends Activity
{
	private FirebaseDatabase database;
	private DatabaseReference reference;

	private ConnectionParams connectionParams;

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

		searchView = findViewById(R.id.search_view);

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
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
						final List<Track> trackList = tracksPager.tracks.items;
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
							public void onItemSelected(View itemView, Track item)
							{
								currentTrack = item;

								String trackUri = item.uri;

								spotifyAppRemote.getPlayerApi().play(trackUri);

								searchView.setQuery("", false);
								adapter.clearData();
								searchResults.setAdapter(adapter);

								//FirebaseUtils.saveTrackToDatabase(reference, item);

								Intent playTrackActivity = new Intent(main, PlayTrackActivity.class);
								playTrackActivity.putExtra("title", currentTrack.name);
								playTrackActivity.putExtra("artists", SpotifyUtils.toStringFromArtists(currentTrack));
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
}
