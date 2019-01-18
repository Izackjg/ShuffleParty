package com.example.gutman.shuffleparty;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.example.gutman.shuffleparty.utils.SpotifyConstants;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.PlayerState;
import kaaes.spotify.webapi.android.models.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistFragment extends Fragment
{
	private Context main;
	private Activity mainActivity;

	private List<Track> playlistItems;
	private Track current;
	private String roomIdentifer;
	private int index;

	private RecyclerView playlistView;
	private SpotifyTrackAdapter trackAdapter;

	private Button btnPlayPause;
	private Button btnRepeat;
	private Button btnShuffle;

	private TextView tvTrackDur;
	private TextView tvTrackElap;
	private TextView tvTrackTitleArtists;

	private SeekBar progress;

	private PlayerApi playerApi;
	private PlayerState currentState;

	private Handler handler;
	private Runnable update;

	public PlaylistFragment()
	{
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		main = container.getContext();
		mainActivity = getActivity();

		handler = new Handler();

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_playlist, container, false);

		btnPlayPause = view.findViewById(R.id.frag_btnPlayPause);
		btnRepeat = view.findViewById(R.id.frag_btnRepeat);
		btnShuffle = view.findViewById(R.id.frag_btnShuffle);

		tvTrackDur = view.findViewById(R.id.frag_tvTrackDur);
		tvTrackElap = view.findViewById(R.id.frag_tvTrackElap);
		tvTrackTitleArtists = view.findViewById(R.id.frag_tvTrackTitleArtist);

		progress = view.findViewById(R.id.frag_seekbarProgress);

		playlistView = view.findViewById(R.id.frag_playlistItemsView);

		playlistView.setLayoutManager(new LinearLayoutManager(main));
		playlistView.setHasFixedSize(true);

		setupAppRemote();

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		Bundle args = getArguments();
		if (args != null)
			roomIdentifer = args.getString("ident");

		setupRecyclerView();
		//setupRecyclerViewDecor();
	}

	private void setupAppRemote(){
		ConnectionParams params = SpotifyUtils.getParams();

		SpotifyAppRemote.connect(main, params, new Connector.ConnectionListener()
		{
			@Override
			public void onConnected(SpotifyAppRemote mSpotifyAppRemote)
			{
				playerApi = mSpotifyAppRemote.getPlayerApi();
				setupUpdateRunnable();

				index = 0;
				current = playlistItems.get(index);
				int dur = (int) current.duration_ms / 1000;
				setupUI(current);

				playerApi.play(current.uri);
				if (mainActivity != null)
					mainActivity.runOnUiThread(update);

				progress.setMax(dur);
				tvTrackDur.setText(SpotifyUtils.formatTimeDuration(dur));
			}

			@Override
			public void onFailure(Throwable throwable)
			{

			}
		});
	}

	private void setupUpdateRunnable()
	{
		update = new Runnable()
		{
			@Override
			public void run()
			{
				playerApi.getPlayerState().setResultCallback(new CallResult.ResultCallback<PlayerState>()
				{
					@Override
					public void onResult(PlayerState playerState)
					{
						currentState = playerState;

						int elapsedSeconds = (int)currentState.playbackPosition / 1000;
						int durationSeconds = (int)currentState.track.duration / 1000;

						progress.setProgress(elapsedSeconds);
						tvTrackElap.setText(SpotifyUtils.formatTimeDuration(elapsedSeconds));

						if (elapsedSeconds >= durationSeconds - 4)
						{
							index += 1;
							current = playlistItems.get(index);
							playerApi.play(current.uri);
							setupUI(current);
						}


						setupUpdateRunnable();
					}
				});
				handler.postDelayed(this, 1000);
			}
		};
	}

	private void setupUI(Track newTrack) {
		int dur = (int) newTrack.duration_ms / 1000;
		progress.setMax(dur);
		tvTrackDur.setText(SpotifyUtils.formatTimeDuration(dur));

		String aritstsFormatted = SpotifyUtils.toStringFromArtists(newTrack);
		tvTrackTitleArtists.setText(newTrack.name + SpotifyConstants.SEPERATOR + aritstsFormatted);
	}

	private void setupRecyclerView(){
		playlistItems = new ArrayList<>();

		// Get the database reference at the current connected room identifer.
		DatabaseReference ref = FirebaseUtils.getCurrentRoomTrackReference(roomIdentifer);
		// Set its value event listener.
		ref.addValueEventListener(valueEventListener);
	}

	// Has events about data changes at a location.
	// In this specific case, the location is at the current connected room reference.
	private ValueEventListener valueEventListener = new ValueEventListener()
	{
		@Override
		public void onDataChange(@NonNull DataSnapshot dataSnapshot)
		{
			// DataSnapshot is used everytime, containing data from a Firebase Database location.
			// Any time you read Database data, I will receive the data as a DataSnapshot.

			// For all the children in the DataSnapshot
			for (DataSnapshot ds : dataSnapshot.getChildren()) {
				// Get the value, and convert it from Object to a Spotify Track.
				Track t = ds.getValue(Track.class);
				// Add it to the playlistItems.
				playlistItems.add(t);
			}
			// Setup the adapter.
			setupAdapter();
		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError)
		{

		}
	};

	private void setupAdapter(){
		trackAdapter = new SpotifyTrackAdapter(main, playlistItems);
		trackAdapter.setItemSelectedListener(trackSelectedListener);
		playlistView.setAdapter(trackAdapter);
		setupRecyclerViewDecor();
	}

	private void setupRecyclerViewDecor(){
		Drawable icon = ContextCompat.getDrawable(main, R.drawable.round_delete);
		ItemTouchHelper touchHelper = new ItemTouchHelper(new SwipeDeleteCallback(trackAdapter, new SwipeDeleteCallback.TrackSwipedListener()
		{
			@Override
			public void onSwipedDelete(int position)
			{
				if (position > index || position < index)
					return;
				if (position == playlistItems.size() - 1)
					index = 0;
				else
					index = position + 1;

				current = playlistItems.get(index);
				playerApi.play(current.uri);
				setupUI(current);
			}
		}, icon));
		touchHelper.attachToRecyclerView(playlistView);
	}

	private SpotifyTrackAdapter.TrackSelectedListener trackSelectedListener =
			new SpotifyTrackAdapter.TrackSelectedListener()
			{
				@Override
				public void onItemSelected(View itemView, Track item, int position)
				{
					if (index == position)
						return;

					index = position;
					current = item;
					setupUI(current);
					playerApi.play(current.uri);
				}
			};
}
