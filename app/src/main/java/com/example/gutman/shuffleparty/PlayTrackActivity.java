package com.example.gutman.shuffleparty;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.SpotifyConstants;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class PlayTrackActivity extends Activity
{
	private String REDIRECT_URI = "http://example.com/callback/";

	private int elapsed = 0;
	private int index = 0;
	private List<Track> playlistItems;
	private Track currentTrack = null;

	private ConnectionParams connectionParams;
	private SpotifyService spotify;
	private SpotifyAppRemote spotifyAppRemote;
	private PlayerState playerState;

	private TextView tvTrackTitle;
	private TextView tvTrackArtists;
	private ImageView trackImageView;
	private Button btnPlay;
	private SeekBar playerProgress;

	private TextView tvTrackDuration;
	private TextView tvTrackElapsed;

	private Handler updateSeekbarHandler;
	private Runnable updateSeekbarRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_track);

		updateSeekbarHandler = new Handler();

		String apiToken = CredentialsHandler.getToken(this);
		spotify = SpotifyUtils.getInstance(apiToken);

		playerProgress = findViewById(R.id.playerProgress);
		trackImageView = findViewById(R.id.trackImage);
		btnPlay = findViewById(R.id.btnPlay);

		tvTrackArtists = findViewById(R.id.tvTrackArtists);
		tvTrackTitle = findViewById(R.id.tvTrackTitle);

		tvTrackDuration = findViewById(R.id.tvTrackDuration);
		tvTrackElapsed = findViewById(R.id.tvTrackElapsed);

		initSeekbar();

		playlistItems = (List<Track>) getIntent().getSerializableExtra("list");
		index = 0;
		currentTrack = playlistItems.get(index);

		tvTrackTitle.setText(SpotifyUtils.formatUnwantedCharsFromTitle(currentTrack.name, "("));
		tvTrackArtists.setText(SpotifyUtils.toStringFromArtists(currentTrack));
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		initPlayerOnEvent();

		connectionParams = new ConnectionParams.Builder(SpotifyConstants.ClientID)
				.setRedirectUri(REDIRECT_URI)
				.showAuthView(true)
				.build();

		SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener()
		{
			@Override
			public void onConnected(SpotifyAppRemote mSpotifyAppRemote)
			{
				spotifyAppRemote = mSpotifyAppRemote;
				spotifyAppRemote.getPlayerApi().play(currentTrack.uri);

				updateUI(currentTrack);
				initPlayerOnEvent();

				initRunnable();
				runOnUiThread(updateSeekbarRunnable);
			}

			@Override
			public void onFailure(Throwable throwable)
			{

			}
		});
	}

	public void btnPlay_onClick(View v)
	{
		if (spotifyAppRemote != null)
		{
			if (currentTrack != null)
			{
				if (!playerState.isPaused)
				{
					spotifyAppRemote.getPlayerApi().pause();
					btnPlay.setBackgroundResource(R.drawable.round_button_play);
					updateSeekbarHandler.removeCallbacks(updateSeekbarRunnable);
				} else
				{
					spotifyAppRemote.getPlayerApi().resume();
					btnPlay.setBackgroundResource(R.drawable.round_button_pause);
					updateSeekbarHandler.post(updateSeekbarRunnable);
				}
			}
		}
	}

	private void initSeekbar()
	{
		playerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
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
		updateSeekbarRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				if (currentTrack != null)
				{
					if (elapsed == (currentTrack.duration_ms / 1000) - 2)
					{
						if (index >= playlistItems.size() - 1)
							index = 0;

						index += 1;
						currentTrack = playlistItems.get(index);

						updateUI(currentTrack);

						elapsed = 0;
						spotifyAppRemote.getPlayerApi().play(currentTrack.uri);
					}

					elapsed += 1;
					tvTrackElapsed.setText(SpotifyUtils.formatTimeDuration(elapsed));
					playerProgress.setProgress(elapsed);
				}
				updateSeekbarHandler.postDelayed(this, 1000);
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

	private void updateUI(Track current) {
		Image trackImage = current.album.images.get(0);
		if (trackImage != null)
		{
			Picasso.get().load(trackImage.url).into(trackImageView);
		}

		tvTrackTitle.setText(SpotifyUtils.formatUnwantedCharsFromTitle(current.name, "("));
		tvTrackArtists.setText(SpotifyUtils.toStringFromArtists(current));

		playerProgress.setMax((int)(current.duration_ms / 1000) - 2);
		playerProgress.setProgress(elapsed);

		tvTrackDuration.setText(SpotifyUtils.formatTimeDuration((int)current.duration_ms / 1000));
		tvTrackElapsed.setText(SpotifyUtils.formatTimeDuration(elapsed));
	}
}
