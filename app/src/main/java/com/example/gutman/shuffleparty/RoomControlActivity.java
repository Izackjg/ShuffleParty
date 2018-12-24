package com.example.gutman.shuffleparty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gutman.shuffleparty.data.AlphabeticUtils;
import com.example.gutman.shuffleparty.data.Room;
import com.example.gutman.shuffleparty.data.User;
import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.google.firebase.FirebaseApp;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RoomControlActivity extends Activity
{
	private Button btnCreateRoom;
	private Button btnJoinRoom;
	private TextView tvRoomIdentifier;
	private RecyclerView connectedUsersView;

	private SpotifyService spotify;

	private String apiToken;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_room_creation);

		FirebaseApp.initializeApp(this);

		apiToken = CredentialsHandler.getToken(this);
		if (apiToken == null)
		{
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		}

		spotify = SpotifyUtils.getInstance(apiToken);

		btnCreateRoom = findViewById(R.id.btnCreateRoom);
		btnJoinRoom = findViewById(R.id.btnJoinRoom);
		tvRoomIdentifier = findViewById(R.id.roomIdentifier);
		connectedUsersView = findViewById(R.id.connectedUsersView);
	}

	public void btnCreateRoom_onClick(View view)
	{
		final List<User> userList = new ArrayList<>();
		spotify.getMe(new Callback<UserPrivate>()
		{
			@Override
			public void success(UserPrivate userPrivate, Response response)
			{
				User u = new User(userPrivate);
				userList.add(u);

				Room r = new Room(userList);

				tvRoomIdentifier.setText(r.getIdentifier());
				tvRoomIdentifier.setVisibility(View.VISIBLE);

				FirebaseUtils.createRoomToDatabase(r);
			}

			@Override
			public void failure(RetrofitError error)
			{

			}
		});
	}

	public void btnJoinRoom_onClick(View view)
	{
	}
}
