package com.example.gutman.shuffleparty;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.concurrent.ExecutionException;

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
	private Runnable playerStateUpdateRunnable;

	public PlaylistFragment()
	{
		// Required empty public constructor
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null)
			index = savedInstanceState.getInt("idx");
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

		playlistItems = new ArrayList<>();
		trackAdapter = new SpotifyTrackAdapter(main, playlistItems);
		trackAdapter.setItemSelectedListener(trackSelectedListener);
		setupRecyclerViewDecor();

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
		setupDatabaseListener();
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

				if (mSpotifyAppRemote.isConnected())
				{
					playerApi.getPlayerState().setResultCallback(new CallResult.ResultCallback<PlayerState>()
					{
						@Override
						public void onResult(PlayerState mPlayerState)
						{
							currentState = mPlayerState;
							setupUpdateRunnable();
							if (mainActivity != null)
								mainActivity.runOnUiThread(playerStateUpdateRunnable);
							setupUI(currentState.track);
						}
					});
					return;
				}

				current = playlistItems.get(index);
				int dur = (int) current.duration_ms / 1000;
				setupUI(current);

				playerApi.play(current.uri);

				progress.setMax(dur);
				tvTrackDur.setText(SpotifyUtils.formatTimeDuration(progress.getMax()));
			}

			@Override
			public void onFailure(Throwable throwable)
			{

			}
		});
	}

	private void setupUpdateRunnable()
	{
		playerStateUpdateRunnable = new Runnable()
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

						String name = main.getClass().getSimpleName();
						paused = currentState.isPaused;

						boolean end = false;
						SpotifyAsyncTask spotifyTask = new SpotifyAsyncTask();

						try
						{
							end = spotifyTask.execute(currentState).get();
						} catch (InterruptedException e)
						{
						} catch (ExecutionException e)
						{
						}

						if (end)
						{
							Log.d(name, "TRACK END " + end);
							if (index == playlistItems.size() - 1)
								index = -1;

							index += 1;
							current = playlistItems.get(index);
							setupUI(current);
							playerApi.play(current.uri);
						}
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
		tvTrackDur.setText(SpotifyUtils.formatTimeDuration(progress.getMax()));

		String aritstsFormatted = SpotifyUtils.toStringFromArtists(newTrack);
		tvTrackTitleArtists.setText(newTrack.name + SpotifyConstants.SEPERATOR + aritstsFormatted);
	}

	private void setupUI(com.spotify.protocol.types.Track newTrack)
	{
		int dur = (int) newTrack.duration / 1000;
		progress.setMax(dur);
		tvTrackDur.setText(SpotifyUtils.formatTimeDuration(progress.getMax()));

		String aritstsFormatted = SpotifyUtils.toStringFromArtists(newTrack);
		tvTrackTitleArtists.setText(newTrack.name + SpotifyConstants.SEPERATOR + aritstsFormatted);
	}

	private void setupDatabaseListener()
	{
		// Get the database reference at the current connected room identifer.
		DatabaseReference ref = FirebaseUtils.getCurrentRoomTrackReference(roomIdentifer);
		// Set its event listener.
		ref.addValueEventListener(valueEventListener);
	}

	private void setDataToAdapter()
	{
		trackAdapter.setData(playlistItems);
		playlistView.setAdapter(trackAdapter);
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
					playlistItems.add(t);
			}
			// Setup the adapter
			setDataToAdapter();
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

	public class SpotifyAsyncTask extends AsyncTask<PlayerState, Double, Boolean>
	{
		@Override
		protected Boolean doInBackground(PlayerState... playerStates)
		{
			if (current == null)
				return false;

			PlayerState state = playerStates[0];

			double elapsedSeconds = state.playbackPosition / 1000.0;
			double elapsedSecondsRound = Math.ceil(elapsedSeconds);

			publishProgress(elapsedSecondsRound);

			double dur = current.duration_ms / 1000.0;

			double decimal = (dur % 1.0) * 2.0;
			double end = Math.floor(dur - decimal);

			if ((int) elapsedSecondsRound >= (int) end)
				return true;
			else
				return false;
		}

		@Override
		protected void onProgressUpdate(Double... values)
		{
			super.onProgressUpdate(values);
			double val = values[0];
			progress.setProgress((int) val);
			tvTrackElap.setText(SpotifyUtils.formatTimeDuration(progress.getProgress()));
		}
	}
}
