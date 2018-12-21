package com.example.gutman.shuffleparty;

import android.app.Activity;
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
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;

import java.util.List;
import java.util.Random;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

public class PlaylistActivity extends Activity
{
	private ConnectionParams connectionParams;
	private SpotifyService spotify;
	private SpotifyAppRemote spotifyAppRemote;
	private PlayerState playerState;

	private long duration;
	private int elapsed;
	private int index;
	private List<Track> playlistItems;
	private Track currentTrack;

	private SpotifyItemAdapter adapter;

	private Button btnPlayPause;
	private Button btnShuffle;
	private Button btnRepeat;

	private RecyclerView playlistItemsView;
	private SeekBar seekbarProgress;

	private TextView tvTrackDur;
	private TextView tvTrackElap;
	private TextView tvTrackTitleArtist;

	private Handler seekbarHandler;
	private Runnable updateRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playlist);

		seekbarHandler = new Handler();

		btnPlayPause = findViewById(R.id.btnPlayPause);
		btnRepeat = findViewById(R.id.btnRepeat);
		btnShuffle = findViewById(R.id.btnShuffle);

		playlistItemsView = findViewById(R.id.playlistItemsView);

		seekbarProgress = findViewById(R.id.seekbarProgress);
		initSeekbar();

		tvTrackTitleArtist = findViewById(R.id.tvTrackTitleArtist);
		tvTrackDur = findViewById(R.id.tvTrackDur);
		tvTrackElap = findViewById(R.id.tvTrackElap);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		init();

		connectionParams = new ConnectionParams.Builder(SpotifyConstants.ClientID)
				.setRedirectUri(SpotifyConstants.REDIRECT_URL)
				.showAuthView(true)
				.build();

		SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener()
		{
			@Override
			public void onConnected(SpotifyAppRemote mSpotifyAppRemote)
			{
				spotifyAppRemote = mSpotifyAppRemote;
				initPlayerOnEvent();

				if (playerState != null && !playerState.isPaused)
					return;

				if (index == 0)
				{
					spotifyAppRemote.getPlayerApi().play(currentTrack.uri);
				}

				initRunnable();
				runOnUiThread(updateRunnable);
				updateUI(currentTrack);
			}

			@Override
			public void onFailure(Throwable throwable)
			{

			}
		});
	}

	public void btnPlayPause_onClick(View view)
	{
		if (spotifyAppRemote != null)
		{
			if (currentTrack != null)
			{
				if (!playerState.isPaused)
				{
					spotifyAppRemote.getPlayerApi().pause();
					btnPlayPause.setBackgroundResource(R.drawable.round_button_play);
					seekbarHandler.removeCallbacks(updateRunnable);
				} else
				{
					spotifyAppRemote.getPlayerApi().resume();
					btnPlayPause.setBackgroundResource(R.drawable.round_button_pause);
					seekbarHandler.post(updateRunnable);
				}
			}
		}
	}

	public void btnRepeat_onClick(View view)
	{
		if (playerState.playbackOptions.repeatMode == 1)
		{
			btnRepeat.setBackgroundResource(R.drawable.round_repeat);
			spotifyAppRemote.getPlayerApi().setRepeat(SpotifyUtils.NO_REPEAT);
		} else if (playerState.playbackOptions.repeatMode == 0)
		{
			btnRepeat.setBackgroundResource(R.drawable.round_repeat_green);
			spotifyAppRemote.getPlayerApi().setRepeat(SpotifyUtils.REPEAT);
		}
	}

	public void btnShuffle_onClick(View view)
	{
		if (playerState.playbackOptions.isShuffling)
		{
			spotifyAppRemote.getPlayerApi().setShuffle(!playerState.playbackOptions.isShuffling);
			btnShuffle.setBackgroundResource(R.drawable.round_shuffle);
		} else
		{
			spotifyAppRemote.getPlayerApi().setShuffle(playerState.playbackOptions.isShuffling);
			btnShuffle.setBackgroundResource(R.drawable.round_shuffle_green);
		}
	}

	private void updateUI(Track current)
	{
		// In the event of removing the last track in the list.
		if (current == null)
		{
			elapsed = 0;
			tvTrackElap.setText(SpotifyUtils.formatTimeDuration(elapsed));
			tvTrackDur.setText(SpotifyUtils.formatTimeDuration(elapsed));
			tvTrackTitleArtist.setText("");
			return;
		}

		String formattedArtists = SpotifyUtils.toStringFromArtists(current);
		tvTrackTitleArtist.setText(current.name + " • " + formattedArtists);

		elapsed = 0;
		seekbarProgress.setMax((int) duration - 2);
		seekbarProgress.setProgress(elapsed);

		tvTrackDur.setText(SpotifyUtils.formatTimeDuration((int) duration));
		tvTrackElap.setText(SpotifyUtils.formatTimeDuration(elapsed));
	}

	private void initSeekbar()
	{
		seekbarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				if (fromUser)
				{
					elapsed = progress;
					seekBar.setProgress(elapsed);
					spotifyAppRemote.getPlayerApi().seekTo(elapsed * 1000);
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
		});
	}

	private void initRunnable()
	{
		updateRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				if (currentTrack != null)
				{
					if (elapsed == duration - 2)
					{
						if (index == playlistItems.size() - 1)
							index = -1;

						int temp = index;

						if (playerState.playbackOptions.isShuffling)
							index = new Random().nextInt(playlistItems.size());
						if (playerState.playbackOptions.repeatMode == 0)
						{
							index = temp;
							elapsed = 0;
						}
						if (playerState.playbackOptions.repeatMode == 0 && !playerState.playbackOptions.isShuffling)
							index += 1;

						currentTrack = playlistItems.get(index);
						spotifyAppRemote.getPlayerApi().play(currentTrack.uri);
						updateUI(currentTrack);
						Log.d(getClass().getSimpleName(), "NEXT TRACK");
					}

					elapsed += 1;
					tvTrackElap.setText(SpotifyUtils.formatTimeDuration(elapsed));
					seekbarProgress.setProgress(elapsed);

					seekbarHandler.postDelayed(this, 1000);
				}
			}
		};
	}

	private void initPlayerOnEvent()
	{
		if (spotifyAppRemote != null)
		{
			spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>()
			{
				@Override
				public void onEvent(PlayerState mPlayerState)
				{
					playerState = mPlayerState;
				}
			});
		}
	}

	private void init()
	{
		index = 0;
		elapsed = 0;

		playlistItems = (List<Track>) getIntent().getSerializableExtra("pl");
		currentTrack = playlistItems.get(index);
		duration = currentTrack.duration_ms / 1000;

		playlistItemsView.setHasFixedSize(true);
		playlistItemsView.setLayoutManager(new LinearLayoutManager(this));

		adapter = new SpotifyItemAdapter(this, playlistItems);
		adapter.setItemSelectedListener(new SpotifyItemAdapter.ItemSelectedListener()
		{
			@Override
			public void onItemSelected(View itemView, Object item, int position)
			{
				if (currentTrack == item)
					return;
				if (item instanceof Track)
				{
					Track tItem = (Track) item;
					index = position;
					currentTrack = tItem;
					spotifyAppRemote.getPlayerApi().play(currentTrack.uri);
					updateUI(currentTrack);
				}
			}
		});

		playlistItemsView.setAdapter(adapter);

		Drawable icon = ContextCompat.getDrawable(this, R.drawable.round_delete);
		ItemTouchHelper touchHelper = new ItemTouchHelper(new SwipeDeleteCallback(adapter, icon));
		touchHelper.attachToRecyclerView(playlistItemsView);
	}
}
