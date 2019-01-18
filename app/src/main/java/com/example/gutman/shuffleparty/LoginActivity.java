package com.example.gutman.shuffleparty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.SpotifyConstants;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends AppCompatActivity
{
	private static final int REQUEST_CODE = 1337;
	private static final String REDIRECT_URI = "http://example.com/callback/";
	private static String ApiToken;

	private Button btnSpotifyLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		btnSpotifyLogin = findViewById(R.id.btnSpotifyLogin);

		if (CredentialsHandler.getToken(this) != null)
		{
			Intent intent = new Intent(this, RoomControlActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE)
		{
			AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

			switch (response.getType())
			{
				case TOKEN:
					ApiToken = response.getAccessToken();
					CredentialsHandler.setToken(this, ApiToken, 365, TimeUnit.DAYS);
					Intent intent = new Intent(this, RoomControlActivity.class);
					startActivity(intent);
					finish();
					break;
				case ERROR:
					break;
				default:
					finish();
					break;
			}
		}
	}

	public void btnSpotifyLogin_onClick(View v)
	{
		AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SpotifyConstants.ClientID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
		builder.setScopes(new String[]{"streaming", "user-read-private", "user-read-recently-played", "user-modify-playback-state", "app-remote-control"});
		AuthenticationRequest request = builder.build();

		AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
	}
}
