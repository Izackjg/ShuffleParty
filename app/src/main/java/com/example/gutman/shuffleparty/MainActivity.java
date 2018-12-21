package com.example.gutman.shuffleparty;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

// TODO: ADD/FIX ALBUM SEARCH FUNCTIONALITY.
public class MainActivity extends Activity
{
	private SpotifyUtils.SEARCH_TYPE TYPE = SpotifyUtils.SEARCH_TYPE.Track;

	private ConnectionParams connectionParams;

	private List<Track> playlistItems = new ArrayList<>();

	private SpotifyService spotify;

	private static String apiToken;

	private Context main;
	private SearchView searchView;
	private RecyclerView searchResults;
	private SpotifyItemAdapter adapter;

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
		connectionParams = SpotifyUtils.getParams();

		SpotifyAppRemote.connect(this, connectionParams,
				new Connector.ConnectionListener()
				{
					public void onConnected(SpotifyAppRemote mSpotifyAppRemote)
					{
						adapter = new SpotifyItemAdapter(main, new SpotifyItemAdapter.ItemSelectedListener()
						{
							@Override
							public void onItemSelected(View itemView, Object item, int position)
							{
								if (item instanceof Track){
									adapter.setItemSelectedListener(onTrackSelectedListener);
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
			public boolean onQueryTextSubmit(String query)
			{
				if (query.isEmpty())
					return false;

				if (TYPE == SpotifyUtils.SEARCH_TYPE.Album)
					searchForAlbum(query);
				else
					searchForTrack(query);

				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				return false;
			}
		});
	}

	private void searchForAlbum(String query)
	{
		spotify.searchAlbums(query, new Callback<AlbumsPager>()
		{
			@Override
			public void success(AlbumsPager albumsPager, Response response)
			{
				List<AlbumSimple> albumSimples = albumsPager.albums.items;

				List<AlbumSimple> albums = new ArrayList<>();
				for (AlbumSimple as : albumSimples)
				{
					albums.add(as);
				}

				adapter.addData(albums);
				searchResults.setAdapter(adapter);
			}

			@Override
			public void failure(RetrofitError error)
			{

			}
		});
	}

	private void searchForTrack(String query)
	{
		spotify.searchTracks(query, new Callback<TracksPager>()
		{
			@Override
			public void success(TracksPager tracksPager, Response response)
			{
				List<Track> trackList = tracksPager.tracks.items;

				if (adapter.getItemCount() != 0)
					adapter.clearData();

				adapter.addData(trackList);
				searchResults.setAdapter(adapter);
			}

			@Override
			public void failure(RetrofitError error)
			{

			}
		});
	}

	private SpotifyItemAdapter.ItemSelectedListener onAlbumSimpleSelected = new SpotifyItemAdapter.ItemSelectedListener()
	{
		@Override
		public void onItemSelected(View itemView, Object item, int position)
		{
			AlbumSimple asItem = (AlbumSimple) item;
			spotify.getAlbumTracks(asItem.id, new Callback<Pager<Track>>()
			{
				@Override
				public void success(Pager<Track> trackPager, Response response)
				{
					adapter.clearData();
					adapter.setItemSelectedListener(onTrackSelectedListener);
					List<Track> trackSimples = trackPager.items;
					adapter.addData(trackSimples);
					searchResults.setAdapter(adapter);
				}

				@Override
				public void failure(RetrofitError error)
				{

				}
			});
		}
	};

	private SpotifyItemAdapter.ItemSelectedListener onTrackSelectedListener = new SpotifyItemAdapter.ItemSelectedListener()
	{
		@Override
		public void onItemSelected(View itemView, Object item, int position)
		{
			Track tItem = (Track) item;
			if (!playlistItems.contains(tItem))
				playlistItems.add(tItem);

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
	};
}