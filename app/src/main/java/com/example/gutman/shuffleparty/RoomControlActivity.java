package com.example.gutman.shuffleparty;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gutman.shuffleparty.data.Room;
import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RoomControlActivity extends AppCompatActivity
{
	private Button btnCreateRoom;
	private Button btnJoinRoom;
	private Button btnDebug;

	private RecyclerView connectedUsersView;

	private SpotifyService spotify;

	private String apiToken = null;

	private boolean DEBUG = true;

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
		connectedUsersView = findViewById(R.id.connectedUsersView);
		if (!DEBUG)
			btnDebug.setVisibility(View.GONE);
	}

	public void btnCreateRoom_onClick(View view)
	{
		final List<UserPrivate> userList = new ArrayList<>();

		spotify.getMe(new Callback<UserPrivate>()
		{
			@Override
			public void success(UserPrivate userPrivate, Response response)
			{
				userList.add(userPrivate);
				Room r = new Room(userList);

				FirebaseUtils.createRoomToDatabase(r);

				Intent i = new Intent(getBaseContext(), FragmentActivity.class);
				i.putExtra("ident", r.getIdentifier());
				startActivity(i);
			}

			@Override
			public void failure(RetrofitError error)
			{

			}
		});
	}

	public void btnClearSp_onClick(View view)
	{
		CredentialsHandler.clearAll(this);
		Intent i = new Intent(this, LoginActivity.class);
		startActivity(i);
		finish();
	}

	public void btnJoinRoom_onClick(View view)
	{
	}
}
