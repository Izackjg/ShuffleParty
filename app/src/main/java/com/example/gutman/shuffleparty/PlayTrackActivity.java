package com.example.gutman.shuffleparty;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
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
	private SpotifyService spotify;

	private String REDIRECT_URI = "http://example.com/callback/";
	private boolean isPlaying = false;

	private long elapsed = 0;
	private Track currentTrack = null;

	private ConnectionParams connectionParams;
	private SpotifyAppRemote spotifyAppRemote;

	private ImageView trackImageView;
	private Button btnPlay;
	private SeekBar playerProgress;

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

				long trackSec = currentTrack.duration_ms / 1000;
				Image trackImage = currentTrack.album.images.get(0);
				if (trackImage != null)
				{
					Picasso.get().load(trackImage.url).into(trackImageView);
				}

				isPlaying = !isPlaying;

				playerProgress.setMax((int) trackSec);
				playerProgress.post(updateSeekbarRunnable);
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
				if (isPlaying)
				{
					spotifyAppRemote.getPlayerApi().pause();
					isPlaying = !isPlaying;
					btnPlay.setBackgroundResource(R.drawable.round_button_play);
					playerProgress.removeCallbacks(updateSeekbarRunnable);
				} else
				{
					spotifyAppRemote.getPlayerApi().resume();
					isPlaying = !isPlaying;
					btnPlay.setBackgroundResource(R.drawable.round_button_pause);
					playerProgress.post(updateSeekbarRunnable);
				}
			}
		}
	}

	private void initRunnable(){
		updateSeekbarRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				if (currentTrack != null) {
					playerProgress.setProgress((int) elapsed);
					elapsed += 1;
					playerProgress.postDelayed(this, 1000);
				}
			}
		};
	}

}
