package com.example.gutman.shuffleparty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gutman.shuffleparty.data.Room;
import com.example.gutman.shuffleparty.data.UserPrivateExtension;
import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RoomCreationActivity extends AppCompatActivity
{
	private DatabaseReference roomRef;

	private Context main;

	private Button btnCreateRoom;
	private Button btnJoinRoom;
	private EditText etRoomCode;

	private UserPrivate userPrivate;
	private UserPrivateExtension extension;

	private boolean admin = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_room_creation);

		main = this;

		// Get the main database reference.
		// In this case it is all the rooms.
		roomRef = FirebaseUtils.ROOM_REF;

		FirebaseApp.initializeApp(this);

		// Check if API Token has expired.
		String apiToken = CredentialsHandler.getToken(this);
		if (apiToken == null)
		{
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		}

		// Get the instance of Spotify using the retrieved API Token from SharedPref.
		SpotifyService spotify = SpotifyUtils.getInstance(apiToken);

		// Get all the controls on the view.
		btnCreateRoom = findViewById(R.id.btnCreateRoom);
		btnJoinRoom = findViewById(R.id.btnJoinRoom);
		etRoomCode = findViewById(R.id.etRoomCode);

		if (spotify == null)
			return;

		// Get user from Spotify API.
		spotify.getMe(new Callback<UserPrivate>()
		{
			@Override
			public void success(UserPrivate mUserPrivate, Response response)
			{
				userPrivate = mUserPrivate;

				admin = userPrivate.product.equals("premium");

				CredentialsHandler.setUserInfo(main, userPrivate);
			}

			@Override
			public void failure(RetrofitError error)
			{

			}
		});
	}

	public void btnCreateRoom_onClick(View view)
	{
		final List<UserPrivateExtension> userList = new ArrayList<>();
		if (userPrivate == null)
			return;

		@SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("EEE, MMM d, yyyy HH:mm");
		String dateFormatted = df.format(Calendar.getInstance().getTime());

		// Create a new UserExtension that contains it's permissions.
		extension = new UserPrivateExtension(userPrivate, admin);

		// Add it to the list of users.
		userList.add(extension);

		// Create a new room, with a unique identifier generated in the constructor.
		Room r = new Room(userList);

		// Add that room the to database.
		FirebaseUtils.addRoomToDatabase(r, dateFormatted);

		// Start the FragmentControlActivity, passing on the room identifier.
		// This allows us to pass the room identifier to all the fragments.
		// Having the room identifier access allows us to get/add items from and to the database.
		startFragmentActivityHolder(r.getIdentifier());
	}

	public void btnJoinRoom_onClick(View view)
	{
		// Get the code from the EditText, and then change it to fully uppercase - incase the user entered it in lowercase.
		final String roomCodeText = etRoomCode.getText().toString().toUpperCase().trim();
		if (roomCodeText.isEmpty())
		{
			Toast.makeText(this, "Room code cannot be an empty charachter literal.", Toast.LENGTH_SHORT).show();
			return;
		}

		// Add a listener for a single event.
		// Added to the highest reference -> "Rooms".
		roomRef.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot)
			{
				if (!dataSnapshot.hasChild(roomCodeText))
				{
					Toast.makeText(RoomCreationActivity.this, "Room does not exist.", Toast.LENGTH_LONG).show();
					return;
				}

				extension = new UserPrivateExtension(userPrivate, admin);
				DatabaseReference joinRoomUserRef = FirebaseUtils.getUsersReference(roomCodeText);

				// Query to check if the user is existing in the room.
				// If user is existing, the query will not be null.
				Query userExistsQuery = joinRoomUserRef.orderByChild("uri").equalTo(userPrivate.uri);

				// If the query is null, then the user is already existing in the room.
				// Meaning we don't need to add him to the user ref of the database.
				// This fixes the issue of having copies of the same user in the Database - as well in the UserFragment.

				// Start the FragmentControlActivity, passing on the room identifier.
				// This allows us to pass the room identifier to all the fragments.
				// Having the room identifier access allows us to get/add items from and to the database.
				startFragmentActivityHolder(roomCodeText);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError)
			{

			}
		});

	}

	private void startFragmentActivityHolder(String identifier)
	{
		Intent i = new Intent(main, FragmentControlActivity.class);
		i.putExtra("ident", identifier);
		startActivity(i);
	}
}
