package com.example.gutman.shuffleparty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.gutman.shuffleparty.data.PermissionType;
import com.example.gutman.shuffleparty.data.Room;
import com.example.gutman.shuffleparty.data.UserPrivateExtension;
import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

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

	private UserPrivateExtension extension;
	private SpotifyService spotify;

	private boolean add = false;
	private String apiToken = null;

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
	}

	public void btnCreateRoom_onClick(View view)
	{
		final List<UserPrivate> userList = new ArrayList<>();

		spotify.getMe(new Callback<UserPrivate>()
		{
			@Override
			public void success(UserPrivate userPrivate, Response response)
			{
				Parcel in = Parcel.obtain();
				userPrivate.writeToParcel(in,0);

				CredentialsHandler.setUserInfo(getBaseContext(), userPrivate.display_name);

				extension = new UserPrivateExtension(userPrivate, PermissionType.Admin, in);

				userList.add(extension);
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

	public void btnJoinRoom_onClick(View view)
	{
		if (extension == null)
			Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
		
		final String tempIdentifier = "EYGMHD";
		DatabaseReference ref = FirebaseUtils.ROOM_REF;
		ref.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot)
			{
				if (dataSnapshot.hasChild(tempIdentifier)){
					add = true;
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError)
			{

			}
		});

		if (add)
			FirebaseUtils.addUserToRoom(tempIdentifier, extension);
	}

	public void btnClearSp_onClick(View view)
	{
		CredentialsHandler.clearAll(this);
		Intent i = new Intent(this, LoginActivity.class);
		startActivity(i);
		finish();
	}
}
