package com.example.gutman.shuffleparty;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.gutman.shuffleparty.utils.SpotifyConstants;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Result;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Empty;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;

import java.util.List;
import java.util.Random;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

public class PlaylistActivity extends Activity
{
	private boolean paused;
	private boolean shuffle;
	private boolean repeat;

	private List<Track> playlistItems;
	private Track current;
	private int index;

	private PlayerApi playerApi;
	private PlayerState currentState;

	private Handler handler;
	private Runnable update;

	private RecyclerView playlistView;
	private SpotifyTrackAdapter trackAdapter;

	private Button btnPlayPause;
	private Button btnRepeat;
	private Button btnShuffle;

	private TextView tvTrackDur;
	private TextView tvTrackElap;
	private TextView tvTrackTitleArtists;

	private SeekBar progress;

	private Context main;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playlist);

		main = this;
		handler = new Handler();

		playlistItems = (List<Track>) getIntent().getSerializableExtra("pl");
		trackAdapter = new SpotifyTrackAdapter(this, playlistItems);
		trackAdapter.setItemSelectedListener(trackSelectedListener);

		playlistView = findViewById(R.id.playlistItemsView);

		playlistView.setHasFixedSize(true);
		playlistView.setLayoutManager(new LinearLayoutManager(this));
		playlistView.setAdapter(trackAdapter);

		Drawable icon = ContextCompat.getDrawable(this, R.drawable.round_delete);
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

		btnPlayPause = findViewById(R.id.btnPlayPause);
		btnRepeat = findViewById(R.id.btnRepeat);
		btnShuffle = findViewById(R.id.btnShuffle);

		tvTrackDur = findViewById(R.id.tvTrackDur);
		tvTrackElap = findViewById(R.id.tvTrackElap);
		tvTrackTitleArtists = findViewById(R.id.tvTrackTitleArtist);

		progress = findViewById(R.id.seekbarProgress);
		progress.setOnSeekBarChangeListener(seekBarChangeListener);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		ConnectionParams params = SpotifyUtils.getParams();

		SpotifyAppRemote.connect(this, params, new Connector.ConnectionListener()
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
				runOnUiThread(update);

				progress.setMax(dur);
				tvTrackDur.setText(SpotifyUtils.formatTimeDuration(dur));
			}

			@Override
			public void onFailure(Throwable throwable)
			{

			}
		});
	}

	public void btnPlayPause_onClick(View view)
	{
		paused = !paused;
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

	public void btnRepeat_onClick(View view)
	{
		repeat = !repeat;

		if (repeat)
		{
			btnRepeat.setBackgroundResource(R.drawable.round_repeat);
		} else if (!repeat)
		{
			btnRepeat.setBackgroundResource(R.drawable.round_repeat_green);
		}
	}

	public void btnShuffle_onClick(View view)
	{
		shuffle = !shuffle;

		if (shuffle)
		{
			btnShuffle.setBackgroundResource(R.drawable.round_shuffle);
		} else
		{
			btnShuffle.setBackgroundResource(R.drawable.round_shuffle_green);
		}
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

						int elapsed = (int) currentState.playbackPosition / 1000;
						int dur = (int) currentState.track.duration / 1000;

						Log.d(main.getClass().getSimpleName(), "ELAPSED: " + elapsed + ": DUR: " + dur);

						if (elapsed >= dur - 3)
						{
							Log.d(main.getClass().getSimpleName(), "END OF TRACK");
							endOfTrack();
						}

						progress.setProgress(elapsed);
						tvTrackElap.setText(SpotifyUtils.formatTimeDuration(elapsed));

						initUpdate();
					}
				});
				handler.postDelayed(this, 1000);
			}
		};
	}

	private void updateUi(Track newTrack)
	{
		int dur = (int) newTrack.duration_ms / 1000;
		progress.setMax(dur);
		tvTrackDur.setText(SpotifyUtils.formatTimeDuration(dur));

		String aritstsFormatted = SpotifyUtils.toStringFromArtists(newTrack);
		tvTrackTitleArtists.setText(newTrack.name + SpotifyConstants.SEPERATOR + aritstsFormatted);
	}

	private void endOfTrack()
	{
		int t = index;

		if (shuffle)
			index = new Random().nextInt(playlistItems.size());
		if (repeat)
		{
			index = t;
		}
		if (!shuffle && !repeat)
		{
			if (index == playlistItems.size() - 1)
				index = -1;
			index += 1;
		}

		current = playlistItems.get(index);
		playerApi.play(current.uri);
		updateUi(current);
	}

	private SpotifyTrackAdapter.TrackSelectedListener trackSelectedListener = new SpotifyTrackAdapter.TrackSelectedListener()
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
}