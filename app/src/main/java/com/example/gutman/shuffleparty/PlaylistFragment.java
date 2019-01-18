package com.example.gutman.shuffleparty;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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

import com.example.gutman.shuffleparty.R;
import com.example.gutman.shuffleparty.utils.SpotifyConstants;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.PlayerState;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistFragment extends Fragment
{

	private Context main;
	private Activity mainActivity;

	private List<Track> playlistItems;
	private Track current;
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

		playlistView.setLayoutManager(new LinearLayoutManager(getActivity()));
		playlistView.setHasFixedSize(true);

		initAppRemote();

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		Bundle args = getArguments();
		if (args != null) {
			playlistItems = (List<Track>)args.getSerializable("pl");
			trackAdapter = new SpotifyTrackAdapter(getActivity(), playlistItems);
			trackAdapter.setItemSelectedListener(trackSelectedListener);
			playlistView.setAdapter(trackAdapter);
			initRecyclerViewDecor();
		}

		//initAppRemote();
	}

	private void initAppRemote(){
		ConnectionParams params = SpotifyUtils.getParams();

		SpotifyAppRemote.connect(main, params, new Connector.ConnectionListener()
		{
			@Override
			public void onConnected(SpotifyAppRemote mSpotifyAppRemote)
			{
				playerApi = mSpotifyAppRemote.getPlayerApi();
				initUpdate();

				index = 0;
				current = playlistItems.get(index);
				int dur = (int) current.duration_ms / 1000;
				updateUi(current);

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

	private void initRecyclerViewDecor(){
		Drawable icon = ContextCompat.getDrawable(getActivity(), R.drawable.round_delete);
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
				updateUi(current);
			}
		}, icon));
		touchHelper.attachToRecyclerView(playlistView);
	}

	private void initUpdate()
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
						int durationSeconds = (int)current.duration_ms / 1000;

						Log.d(main.getClass().getSimpleName(), "ELAPSED TIME = " + elapsedSeconds);

						progress.setProgress(elapsedSeconds);
						tvTrackElap.setText(SpotifyUtils.formatTimeDuration(elapsedSeconds));

						if (elapsedSeconds >= durationSeconds - 3)
						{
							index += 1;
							current = playlistItems.get(index);
							playerApi.play(current.uri);
							updateUi(current);
						}
						//
						//						if (currentState.playbackPosition >= currentState.track.duration - 3)
						//						{
						//							endOfTrack();
						//						}
						//
						//						int elapsed = (int) currentState.playbackPosition / 1000;
						//						double elapsedMins = (double) elapsed / 60;
						//
						//						int dur = (int) currentState.track.duration / 1000;
						//						double durMins = (double) dur / 60;
						//
						//						double crossfade = 1 / 3 / 10;
						//
						//						double finalDur = durMins - crossfade;

						initUpdate();
					}
				});
				handler.postDelayed(this, 1000);
			}
		};
	}

	private void updateUi(Track newTrack) {
		int dur = (int) newTrack.duration_ms / 1000;
		progress.setMax(dur);
		tvTrackDur.setText(SpotifyUtils.formatTimeDuration(dur));

		String aritstsFormatted = SpotifyUtils.toStringFromArtists(newTrack);
		tvTrackTitleArtists.setText(newTrack.name + SpotifyConstants.SEPERATOR + aritstsFormatted);
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
					updateUi(current);
					playerApi.play(current.uri);
				}
			};
}
