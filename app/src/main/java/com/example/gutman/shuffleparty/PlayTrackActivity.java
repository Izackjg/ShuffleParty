package com.example.gutman.shuffleparty;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.List;

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

		int index = getIntent().getIntExtra("index", 0);
		List<Track> trackList = (List<Track>) getIntent().getSerializableExtra("list");
		currentTrack = trackList.get(index);
		tvTrackTitle.setText(SpotifyUtils.formatUnwantedCharsFromTitle(currentTrack.name, "("));
		tvTrackArtists.setText(SpotifyUtils.toStringFromArtists(currentTrack));
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		initPlayerOnEvent();

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

				initPlayerOnEvent();

				long trackSec = (currentTrack.duration_ms / 1000) - 2;
				tvTrackDuration.setText(formatTimeDuration((int) trackSec + 2));

				Image trackImage = currentTrack.album.images.get(0);
				if (trackImage != null)
				{
					Picasso.get().load(trackImage.url).into(trackImageView);
				}

				playerProgress.setMax((int) trackSec);
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
					if (elapsed == currentTrack.duration_ms / 1000)
						elapsed = 0;

					elapsed += 1;
					tvTrackElapsed.setText(formatTimeDuration((int) elapsed));
					playerProgress.setProgress((int) elapsed);
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

	private String formatTimeDuration(int totalSeconds)
	{
		int mins = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;

		return String.format("%02d:%02d", mins, seconds);
	}
}
