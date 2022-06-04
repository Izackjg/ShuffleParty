package com.example.gutman.shuffleparty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.example.gutman.shuffleparty.utils.SpotifyConstants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity
{
	private Context main;
	private static final int REQUEST_CODE = 1337;
	private static final String REDIRECT_URI = "http://example.com/callback/";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		main = this;
		Button btnSpotifyLogin = findViewById(R.id.btnSpotifyLogin);

		if (CredentialsHandler.getToken(this) != null)
		{
			Intent intent = new Intent(this, RoomCreationActivity.class);
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
			final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

			switch (response.getType())
			{
				case TOKEN:
					String apiToken = response.getAccessToken();
					CredentialsHandler.setToken(this, apiToken, 1, TimeUnit.HOURS);
					Intent intent = new Intent(this, RoomCreationActivity.class);
					startActivity(intent);
					finish();
					break;
				case CODE:
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
		builder.setScopes(new String[]{"streaming", "user-modify-playback-state", "user-read-private", "app-remote-control"});
		AuthenticationRequest request = builder.build();

		AuthenticationClient.openLoginActivity((Activity) main, REQUEST_CODE, request);
		userInRoom();
	}

	private boolean userInRoom(){
		String userUri = CredentialsHandler.getUserUri(this);
		DatabaseReference ref = FirebaseUtils.ROOM_REF;
		Query contains = ref.orderByChild("users").equalTo(userUri);
		Log.d(main.getClass().getSimpleName(), "F");
		return false;
	}
}

