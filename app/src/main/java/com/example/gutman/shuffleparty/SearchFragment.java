package com.example.gutman.shuffleparty;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment
{
	private Context main;
	private Activity mainActivity;

	private ConnectionParams connectionParams;

	private List<Track> playlistItems = new ArrayList<>();

	private SpotifyService spotify;
	private SpotifyAppRemote spotifyAppRemote;

	private static String apiToken;

	private SearchView searchView;
	private RecyclerView searchResults;
	private SpotifyTrackAdapter adapter;

	public SearchFragment()
	{
		// Required empty public constructor
	}

	@Override
	public void onStart()
	{
		super.onStart();

		// Use this method to do the checking so I'm able to use final.
		// Meaning I can only set this value once, so I can set it to null or the roomIdentifer value.
		final String roomIdentifer = getRoomIdentifer();

		adapter = new SpotifyTrackAdapter(mainActivity, new SpotifyTrackAdapter.TrackSelectedListener()
		{
			@Override
			public void onItemSelected(View itemView, Track item, int position)
			{
				if (roomIdentifer != null)
					FirebaseUtils.addTrackToDatabase(roomIdentifer, item);

				playlistItems.add(item);

				searchView.setQuery("", false);
				adapter.clearData();
				searchResults.setAdapter(adapter);
			}
		});
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		main = container.getContext();
		mainActivity = getActivity();

		View view = inflater.inflate(R.layout.fragment_search, container, false);

		apiToken = CredentialsHandler.getToken(mainActivity);
		if (apiToken == null)
		{
			Intent i = new Intent(main, LoginActivity.class);
			startActivity(i);
		}

		spotify = SpotifyUtils.getInstance(apiToken);

		initSearchbar(view);

		searchResults = view.findViewById(R.id.frag_search_results);
		searchResults.setLayoutManager(new LinearLayoutManager(mainActivity));
		searchResults.setHasFixedSize(true);

		return view;
	}

	private void initSearchbar(View v)
	{
		searchView = v.findViewById(R.id.frag_search_view);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
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
					}

					@Override
					public void success(TracksPager tracksPager, Response response)
					{
						adapter.setData(tracksPager.tracks.items);
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

	private String getRoomIdentifer()
	{
		Bundle b = getArguments();
		if (b != null)
		{
			return b.getString("ident");
		}
		return null;
	}

}
