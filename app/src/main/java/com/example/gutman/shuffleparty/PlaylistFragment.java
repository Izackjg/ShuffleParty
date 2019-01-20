package com.example.gutman.shuffleparty;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.example.gutman.shuffleparty.utils.SpotifyConstants;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
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
	private boolean admin = true;
	private boolean paused;

	private Context main;
	private Activity mainActivity;

	private List<Track> playlistItems;
	private Track current;
	private String roomIdentifer;
	private int index = 0;

	private RelativeLayout fragPlayerLayout;

	private RecyclerView playlistView;
	private SpotifyTrackAdapter trackAdapter;

	private Button btnPlayPause;

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

		fragPlayerLayout = view.findViewById(R.id.frag_playerLayout);

		btnPlayPause = view.findViewById(R.id.frag_btnPlayPause);
		btnPlayPause.setOnClickListener(btnPlayPauseClickListener);

		tvTrackDur = view.findViewById(R.id.frag_tvTrackDur);
		tvTrackElap = view.findViewById(R.id.frag_tvTrackElap);
		tvTrackTitleArtists = view.findViewById(R.id.frag_tvTrackTitleArtist);

		progress = view.findViewById(R.id.frag_seekbarProgress);

		playlistView = view.findViewById(R.id.frag_playlistItemsView);

		playlistView.setLayoutManager(new LinearLayoutManager(main));
		playlistView.setHasFixedSize(true);

		setupAppRemote();
		trackAdapter = new SpotifyTrackAdapter(main);
		trackAdapter.setItemSelectedListener(trackSelectedListener);

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		Bundle args = getArguments();
		if (args != null)
			roomIdentifer = args.getString("ident");

		final String mainName = main.getClass().getSimpleName();

		final DatabaseReference ref = FirebaseUtils.getCurrentRoomUsersReference(roomIdentifer);
		final Query adminRef = ref.orderByChild("permType").equalTo(true);
		adminRef.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot)
			{
				for (DataSnapshot ds : dataSnapshot.getChildren())
				{
					admin = ds.child("permType").getValue(Boolean.class);
					Log.d(mainName, "ADMIN VAL: " + admin);
				}


				setupViewForCorrespondingUsers();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError)
			{

			}
		});

		setupRecyclerView();
	}

	private void setupAppRemote()
	{
		ConnectionParams params = SpotifyUtils.getParams();

		SpotifyAppRemote.connect(main, params, new Connector.ConnectionListener()
		{
			@Override
			public void onConnected(SpotifyAppRemote mSpotifyAppRemote)
			{
				playerApi = mSpotifyAppRemote.getPlayerApi();
				setupUpdateRunnable();

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

						paused = currentState.isPaused;

						// Convert to double from long so I can do some decimal math with them.
						// Meaning, I can subtract decimal values for the track end logic.
						double elapsedSeconds = (double) currentState.playbackPosition / 1000;
						double durationSeconds = (double) current.duration_ms / 1000;

						progress.setProgress((int) elapsedSeconds);
						tvTrackElap.setText(SpotifyUtils.formatTimeDuration((int) elapsedSeconds));

						if (elapsedSeconds >= durationSeconds - 1.75)
						{
							if (index == playlistItems.size() - 1)
								index = -1;

							index += 1;
							current = playlistItems.get(index);
							setupUI(current);
							playerApi.play(current.uri);
						}

						setupUpdateRunnable();
					}
				});
				handler.postDelayed(this, 1000);
			}
		};
	}

	private void setupUI(Track newTrack)
	{
		int dur = (int) newTrack.duration_ms / 1000;
		progress.setMax(dur);
		tvTrackDur.setText(SpotifyUtils.formatTimeDuration(dur));

		String aritstsFormatted = SpotifyUtils.toStringFromArtists(newTrack);
		tvTrackTitleArtists.setText(newTrack.name + SpotifyConstants.SEPERATOR + aritstsFormatted);
	}

	private void setupRecyclerView()
	{
		playlistItems = new ArrayList<>();

		// Get the database reference at the current connected room identifer.
		DatabaseReference ref = FirebaseUtils.getCurrentRoomTrackReference(roomIdentifer);
		// Set its event listener.
		ref.addValueEventListener(valueEventListener);
	}

	private void setupViewForCorrespondingUsers()
	{
		if (!admin)
			return;

		fragPlayerLayout.setVisibility(View.VISIBLE);
	}

	private void addToAdapter()
	{
		trackAdapter.setData(playlistItems);
		playlistView.setAdapter(trackAdapter);
		setupRecyclerViewDecor();
	}

	private void setupRecyclerViewDecor()
	{
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
			for (DataSnapshot ds : dataSnapshot.getChildren())
			{
				// Get the value, and convert it from Object to a Spotify Track.
				Track t = ds.getValue(Track.class);
				// Add it to the playlistItems.
				if (!playlistItems.contains(t))
				{
					playlistItems.add(t);
				}
			}
			// Setup the adapter
			addToAdapter();
		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError)
		{
			throw databaseError.toException();
		}
	};

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

	private View.OnClickListener btnPlayPauseClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if (paused)
			{
				playerApi.resume();
				btnPlayPause.setBackgroundResource(R.drawable.round_button_pause);
			} else
			{
				playerApi.pause();
				btnPlayPause.setBackgroundResource(R.drawable.round_button_play);
			}
		}
	};
}
