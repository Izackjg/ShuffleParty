package com.example.gutman.shuffleparty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends Activity
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

		if (CredentialsHandler.getToken(this) != null) {
			Intent intent = new Intent(this, MainActivity.class);
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
					CredentialsHandler.setToken(this, ApiToken, 3600, TimeUnit.SECONDS);

					Intent intent = new Intent(this, MainActivity.class);
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

	public void btnSpotifyLogin_onClick(View v) {
		AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SpotifyConstants.getClientID(), AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
		builder.setScopes(new String[]{"streaming"});
		AuthenticationRequest request = builder.build();

		AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
	}
}
