package com.example.gutman.shuffleparty;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

// TODO: AT END OF TRACK ADD NEXT TRACK TO PLAYBACK QUEUE IN LIST
// TODO: OR ADD IT TO PLAYBACK QUEUE WHEN NAVIGATING TO THIS FRAGMENT
// SEE https://github.com/spotify-web-api-java/spotify-web-api-java/blob/develop/examples/data/player/AddItemToUsersPlaybackQueueExample.java

public class PlaylistFragment extends Fragment
{
	private Context main;
	private Activity mainActivity;

	private List<Track> playlistItems;
	private Track current;
	private String roomIdentifier;
	private int index = 0;

	private SpotifyTrackAdapter trackAdapter;
	private SpotifyAppRemote spotifyAppRemote;
	private PlayerApi playerApi;
	private PlayerState currentState;

	private Handler handler;

	private RecyclerView playlistView;
	private SeekBar seekBarProgress;
	private TextView tvTrackDur;
	private TextView tvTrackElapsed;
	private TextView tvTrackTitleArtists;
	private ImageView ivTrackImage;

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

		tvTrackDur = view.findViewById(R.id.tvTrackDur);
		tvTrackElapsed = view.findViewById(R.id.frag_tvTrackElap_admin);
		tvTrackTitleArtists = view.findViewById(R.id.tvTrackTitleArtist);
		ivTrackImage = view.findViewById(R.id.ivTrackImage);

		seekBarProgress = view.findViewById(R.id.seekbarProgress);
		seekBarProgress.setOnSeekBarChangeListener(seekBarChangeListener);

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

		DatabaseReference trackRef = FirebaseUtils.getTrackReference(roomIdentifier);
		trackRef.addValueEventListener(valueEventListener);
	}

	private void setupUI(Track newTrack)
	{
		int dur = (int) newTrack.duration_ms / 1000;
		seekBarProgress.setMax(dur);
		tvTrackDur.setText(SpotifyUtils.formatTimeDuration(dur));

		String artistsFormatted = SpotifyUtils.toStringFromArtists(newTrack);
		tvTrackTitleArtists.setText(newTrack.name + SpotifyConstants.SEPERATOR + artistsFormatted);

	}

	// Track from Spotify Player State.
	private void setupUI(com.spotify.protocol.types.Track newTrack)
	{
		int dur = (int) newTrack.duration / 1000;
		seekBarProgress.setMax(dur);
		tvTrackDur.setText(SpotifyUtils.formatTimeDuration(seekBarProgress.getMax()));

		String artistsFormatted = SpotifyUtils.toStringFromArtists(newTrack);
		tvTrackTitleArtists.setText(newTrack.name + SpotifyConstants.SEPERATOR + artistsFormatted);
	}

	private void setupImage(Image image)
	{
		int imgH = image.height;
		int imgW = image.width;
		ivTrackImage.getLayoutParams().height = imgH;
		ivTrackImage.getLayoutParams().width = imgW;
		Picasso.get().load(image.url).into(ivTrackImage);
	}

	private void setupSeeker(int currentTime) {
		currentTime /= 1000;
		tvTrackElapsed.setText(SpotifyUtils.formatTimeDuration(currentTime));
		seekBarProgress.setProgress(currentTime);
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
				mainActivity.runOnUiThread(updateSeekerUI);

				if (mSpotifyAppRemote.isConnected())
				{
					// If connected, whether from app or random Spotify song, setup the UI and PlayerState
					// based on the connected Spotify Remote.
					playerApi.getPlayerState().setResultCallback(playerState -> {
						currentState = playerState;
						setupUI(currentState.track);
					});
				} else
				{
					// Else it is not connected, so that means that we get the song based on the list of songs.
					current = playlistItems.get(index);
					setupUI(current);
				}
			}

			@Override
			public void onFailure(Throwable throwable)
			{

			}
		});
	}

	private Runnable updateSeekerUI = new Runnable()
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
					setupSeeker((int)currentState.playbackPosition);
				}
			});
			handler.postDelayed(this, 1000);
		}
	};

	private ValueEventListener valueEventListener = new ValueEventListener()
	{
		@Override
		public void onDataChange(@NonNull DataSnapshot snapshot)
		{
			playlistItems.clear();

			for (DataSnapshot ds : snapshot.getChildren())
			{
				Track t = ds.getValue(Track.class);
				playlistItems.add(t);
			}
			trackAdapter.setData(playlistItems);
			playlistView.setAdapter(trackAdapter);

			if (index >= playlistItems.size())
				return;
			setupImage(playlistItems.get(index).album.images.get(0));
		}

		@Override
		public void onCancelled(@NonNull DatabaseError error)
		{
			throw error.toException();
		}
	};

	private final SpotifyTrackAdapter.TrackSelectedListener trackSelectedListener =
			new SpotifyTrackAdapter.TrackSelectedListener()
			{
				@Override
				public void onItemSelected(View itemView, final Track item, final int position)
				{
					index = position;
					current = item;
					setupUI(current);
					setupImage(current.album.images.get(0));
					playerApi.play(current.uri);
				}
			};

	private final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
	{
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			if (fromUser)
			{
				playerApi.seekTo(progress * 1000L);
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

	public class SpotifyAsyncTask extends AsyncTask<PlayerState, Double, Boolean>
	{
		@Override
		protected Boolean doInBackground(PlayerState... playerStates)
		{
			PlayerState state = playerStates[0];
			com.spotify.protocol.types.Track stateTrack = state.track;

			if (state == null && current == null)
				return false;
			if (stateTrack == null)
				return false;

			double elapsedSeconds = state.playbackPosition / 1000.0;
			double elapsedSecondsRounded = Math.floor(elapsedSeconds);

			publishProgress(elapsedSecondsRounded);

			return true;
		}

		// Called on UI thread after calling publishProgress(Double...)
		@Override
		protected void onProgressUpdate(Double... values)
		{
			super.onProgressUpdate(values);
			double val = values[0];
			seekBarProgress.setProgress((int) val);
			tvTrackElapsed.setText(SpotifyUtils.formatTimeDuration(seekBarProgress.getProgress()));
		}
	}
}
