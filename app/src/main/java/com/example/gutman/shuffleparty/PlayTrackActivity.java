package com.example.gutman.shuffleparty;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.client.Response;

public class PlayTrackActivity extends Activity
{
	private String REDIRECT_URI = "http://example.com/callback/";

	private long elapsed = 0;
	private Track currentTrack = null;

	private ConnectionParams connectionParams;
	private SpotifyAppRemote spotifyAppRemote;
	private SpotifyService spotify;
	private PlayerState playerState;

	private ImageView trackImageView;
	private Button btnPlay;
	private SeekBar playerProgress;

	private TextView tvTrackDuration;
	private TextView tvTrackElapsed;

	private Runnable updateSeekbarRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_track);

		String apiToken = CredentialsHandler.getToken(this);
		spotify = SpotifyUtils.getInstance(apiToken);

		playerProgress = findViewById(R.id.playerProgress);
		trackImageView = findViewById(R.id.trackImage);
		btnPlay = findViewById(R.id.btnPlay);

		tvTrackDuration = findViewById(R.id.tvTrackDuration);
		tvTrackElapsed = findViewById(R.id.tvTrackElapsed);

		initSeekbar();
		initRunnable();

		String title = getIntent().getStringExtra("title");
		String artists = getIntent().getStringExtra("artists");
		String combined = artists + " " + title;

		spotify.searchTracks(combined, new SpotifyCallback<TracksPager>()
		{
			@Override
			public void failure(SpotifyError spotifyError)
			{

			}

			@Override
			public void success(TracksPager tracksPager, Response response)
			{
				currentTrack = tracksPager.tracks.items.get(0);
			}
		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		connectionParams = new ConnectionParams.Builder(SpotifyConstants.getClientID())
				.setRedirectUri(REDIRECT_URI)
				.showAuthView(true)
				.build();

		SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener()
		{
			@Override
			public void onConnected(SpotifyAppRemote mSpotifyAppRemote)
			{
				spotifyAppRemote = mSpotifyAppRemote;

				initPlayer();

				long trackSec = currentTrack.duration_ms / 1000;
				tvTrackDuration.setText(formatTimeDuration((int)trackSec));

				Image trackImage = currentTrack.album.images.get(0);
				if (trackImage != null)
				{
					Picasso.get().load(trackImage.url).into(trackImageView);
				}

				playerProgress.setMax((int) trackSec);
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
					spotifyAppRemote.getPlayerApi().pause();
				else
					spotifyAppRemote.getPlayerApi().resume();

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
					seekBar.setProgress((int) elapsed);
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
					elapsed += 1;
					playerProgress.setProgress((int) elapsed);
					playerProgress.postDelayed(this, 1000);
				}
			}
		};
	}

	private void initPlayer()
	{
		if (spotifyAppRemote != null)
		{
			spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>()
			{
				@Override
				public void onEvent(PlayerState mPlayerState)
				{
					playerState = mPlayerState;

					if (mPlayerState.playbackPosition == mPlayerState.track.duration)
						elapsed = 0;

					if (mPlayerState.isPaused)
					{
						btnPlay.setBackgroundResource(R.drawable.round_button_play);
						playerProgress.removeCallbacks(updateSeekbarRunnable);
					} else
					{
						btnPlay.setBackgroundResource(R.drawable.round_button_pause);
						playerProgress.post(updateSeekbarRunnable);
					}
				}
			});
		}
	}

	private String formatTimeDuration(int totalSeconds) {
		int mins = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;

		return String.format("%02d:%02d", mins, seconds);
	}

}
