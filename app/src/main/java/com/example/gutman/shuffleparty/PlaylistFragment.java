package com.example.gutman.shuffleparty;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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

public class PlaylistFragment extends Fragment
{

	private DatabaseReference usersRef;
	private DatabaseReference trackRef;

	private boolean paused;

	private Context main;
	private Activity mainActivity;

	private List<Track> playlistItems;
	private Track current;
	private String roomIdentifier;
	private int index = 0;

	private RecyclerView playlistView;
	private SpotifyTrackAdapter trackAdapter;

	private Button btnPlayPause;

	private TextView tvTrackDur;
	private TextView tvTrackElap;
	private TextView tvTrackTitleArtists;

	private SeekBar progress;

	private SpotifyAppRemote spotifyAppRemote;
	private PlayerApi playerApi;
	private PlayerState currentState;

	private Handler handler;

	public PlaylistFragment()
	{
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		// Returns the context the view is currently running in. Usually the currently active Activity.
		main = container.getContext();
		// Returns FragmentControlActivity
		mainActivity = getActivity();

		handler = new Handler();

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_playlist, container, false);

		btnPlayPause = view.findViewById(R.id.btnPlayPause);
		btnPlayPause.setOnClickListener(btnPlayPauseClickListener);

		tvTrackDur = view.findViewById(R.id.tvTrackDur);
		tvTrackElap = view.findViewById(R.id.frag_tvTrackElap_admin);
		tvTrackTitleArtists = view.findViewById(R.id.tvTrackTitleArtist);

		progress = view.findViewById(R.id.seekbarProgress);
		progress.setOnSeekBarChangeListener(seekBarChangeListener);

		playlistView = view.findViewById(R.id.playlistItemsView);

		playlistView.setLayoutManager(new LinearLayoutManager(main));
		playlistView.setHasFixedSize(true);

		playlistItems = new ArrayList<>();
		trackAdapter = new SpotifyTrackAdapter(main, playlistItems);
		trackAdapter.setItemSelectedListener(trackSelectedListener);

		setupAppRemote();

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		Bundle args = getArguments();
		if (args != null)
			roomIdentifier = args.getString("ident");

		usersRef = FirebaseUtils.getUsersReference(roomIdentifier);

		trackRef = FirebaseUtils.getTrackReference(roomIdentifier);
		trackRef.addValueEventListener(valueEventListener);
	}

	private void setupAppRemote()
	{
		ConnectionParams params = SpotifyUtils.getParams();

		SpotifyAppRemote.connect(main, params, new Connector.ConnectionListener()
		{
			@Override
			public void onConnected(final SpotifyAppRemote mSpotifyAppRemote)
			{
				spotifyAppRemote = mSpotifyAppRemote;
				playerApi = spotifyAppRemote.getPlayerApi();

				if (mSpotifyAppRemote.isConnected())
				{
					// If connected, whether from app or random Spotify song, setup the UI and PlayerState
					// based on the connected Spotify Remote.
					playerApi.getPlayerState().setResultCallback(new CallResult.ResultCallback<PlayerState>()
					{
						@Override
						public void onResult(PlayerState playerState)
						{
							currentState = playerState;
							mainActivity.runOnUiThread(playerStateUpdateRunnable);
							setupUI(currentState.track);
						}
					});
				} else
				{
					// Else it is not connected, so that means that we get the song based on the list of songs.
					current = playlistItems.get(index);
					setupUI(current);
					playerApi.play(current.uri);
					mainActivity.runOnUiThread(playerStateUpdateRunnable);
				}
			}

			@Override
			public void onFailure(Throwable throwable)
			{

			}
		});
	}

	// Track from Wrapper
	private void setupUI(Track newTrack)
	{
		int dur = (int) newTrack.duration_ms / 1000;
		progress.setMax(dur);
		tvTrackDur.setText(SpotifyUtils.formatTimeDuration(progress.getMax()));

		String aritstsFormatted = SpotifyUtils.toStringFromArtists(newTrack);
		tvTrackTitleArtists.setText(newTrack.name + SpotifyConstants.SEPERATOR + aritstsFormatted);
	}

	// Track from Spotify Player State.
	private void setupUI(com.spotify.protocol.types.Track newTrack)
	{
		int dur = (int) newTrack.duration / 1000;
		progress.setMax(dur);
		tvTrackDur.setText(SpotifyUtils.formatTimeDuration(progress.getMax()));

		String aritstsFormatted = SpotifyUtils.toStringFromArtists(newTrack);
		tvTrackTitleArtists.setText(newTrack.name + SpotifyConstants.SEPERATOR + aritstsFormatted);
	}

	private void setDataToAdapter()
	{
		trackAdapter.setData(playlistItems);
		playlistView.setAdapter(trackAdapter);
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

			playlistItems.clear();

			// For all the children in the DataSnapshot
			for (DataSnapshot ds : dataSnapshot.getChildren())
			{
				// Get the value, and convert it from Object to a Spotify Track.
				Track t = ds.getValue(Track.class);
				// Add it to the playlistItems.
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
				public void onItemSelected(View itemView, final Track item, final int position)
				{
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

	private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
	{
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			if (fromUser)
			{
				playerApi.seekTo(progress * 1000);
				seekBar.setProgress(progress);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{

		}
	};

	private Runnable playerStateUpdateRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			// Contact spotify api and set a callback when api returns a result of type PlayerState.
			playerApi.getPlayerState().setResultCallback(new CallResult.ResultCallback<PlayerState>()
			{
				@Override
				public void onResult(PlayerState playerState)
				{
					// Set the global variable to the result we get from the Callback.
					// I do this so I can access this variable from anywhere in the code.
					currentState = playerState;

					// Check if the current state is paused.
					paused = currentState.isPaused;

					// Has the track ended? Default is false, because it hasn't ended.
					boolean hasTrackEnded = false;

					// Create a new SpotifyAsnycTask which deals with the small task of calculations based on the current
					// track and it's duration.
					SpotifyAsyncTask spotifyTask = new SpotifyAsyncTask();

					try
					{
						// Get the result of the execution. This returns a boolean.
						hasTrackEnded = spotifyTask.execute(currentState).get();
						Log.d(main.getClass().getSimpleName(), "LOGGING HASTRACKENDED: " + hasTrackEnded);
					} catch (InterruptedException e)
					{
					} catch (ExecutionException e)
					{
					}

					// If the boolean is true, then the track has ended.
					if (hasTrackEnded)
					{
						endOfTrack();
						setupUI(current);
					}
				}
			});
			// Continue this thread each second.
			handler.postDelayed(this, 1000);
		}
	};

	private void endOfTrack()
	{
		Log.d(main.getClass().getSimpleName(), "LOGGING INSIDE END OF TRACK");
		// If current isn't null, play the next track regularly.

		if (index == playlistItems.size() - 1)
			index = -1;

		// Play next track.
		// Get track based on index, setup UI, and play that track.
		index += 1;
		current = playlistItems.get(index);
		playerApi.play(current.uri);

		// If current is null, that means we have just returned from navigating another fragment.
		// This means that index will be 0, and if we didn't do this check,
		// it would update the current track to be the second in the list,
		// although that might not be the case since we have just returned from navigating another fragment,
		// meaning that the current track could be the player state track.


		// Get index based on if the two track uri's are equal.
		index = currentState.track == null ? 0 : SpotifyUtils.getIndex(playlistItems, currentState.track);
		// If that method returned -1, it means the current state playing track isn't in the playing.
		// Threfore, at the end of that current state track, set the index to 0. Therefore forcing playing the first track.
		if (index == -1)
			index = 0;

		current = playlistItems.get(index);
		playerApi.play(current.uri);
	}

	public class SpotifyAsyncTask extends AsyncTask<PlayerState, Double, Boolean>
	{
		// This method does all of this calculation on another thread.
		// This is why we have publishProgress(double) to allow the main thread GUI to access the elapsed seconds.
		@Override
		protected Boolean doInBackground(PlayerState... playerStates)
		{
			// PlayerState... -> Can receive multiple PlayerState objects, or an array of them.
			// In this case I know I only pass one, so it is the first index.
			PlayerState state = playerStates[0];
			com.spotify.protocol.types.Track stateTrack = state.track;

			// state and current can also be null. For example, a person creating a room the state will be null,
			// because he hasn't navigated to the PlaylistFragment ever, meaning current and state will be null,
			// until the user starts a playback.
			if (state == null && current == null)
				return false;
			if (stateTrack == null)
				return false;

			// Playback Position is in ms.
			// Get the state's elapsed seconds, and convert it into seconds.
			double elapsedSeconds = state.playbackPosition / 1000.0;
			double elapsedSecondsRound = Math.floor(elapsedSeconds);

			// Passes the progress to the onProgressUpdate(Double...)
			publishProgress(elapsedSecondsRound);

			double dur = 0.0;
			// If the current track isn't null, then take the current tracks duration.
			// The current track will be null when we navigate to other fragments.
			if (current != null)
				dur = (current.duration_ms / 1000.0);
				// If the current track is null, this means we have navigated to other fragments.
				// Get the PlayerState track duration -> usually the track that is currently playing.
			else if (current == null && state != null)
				dur = (state.track.duration / 1000.0);

			// I do these above lines so that in the method: setupAppRemote
			// I am able to setup the UI and progress update, whether or not the variable current is null.

			// *****

			// This gets the value of the decimal point after the duration.
			// We multiply it by a lowest possible value, but high enough
			// to register the track ending.
			// But still large enough to have the track end.

			//double decimal = dur % 1.0;
			elapsedSecondsRound = Math.floor(elapsedSecondsRound);

			// Floor the value, because we want to lowest value.
			double end = Math.floor(dur);

			return (int) elapsedSecondsRound >= (int)end - 4;
		}

		// Called on UI thread after calling publishProgress(Double...)
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
