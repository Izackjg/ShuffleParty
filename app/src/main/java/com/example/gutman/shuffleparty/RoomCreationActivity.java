package com.example.gutman.shuffleparty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RoomCreationActivity extends AppCompatActivity
{
	private Button btnCreateRoom;
	private Button btnJoinRoom;
	private Button btnClearSP;
	private EditText etRoomCode;

	private Parcel in;
	private UserPrivate userPrivate;
	private UserPrivateExtension extension;

	private SpotifyService spotify;

	private boolean admin = false;
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
		etRoomCode = findViewById(R.id.etRoomCode);

		if (spotify == null)
			return;
		spotify.getMe(new Callback<UserPrivate>()
		{
			@Override
			public void success(UserPrivate mUserPrivate, Response response)
			{
				in = Parcel.obtain();

				userPrivate = mUserPrivate;
				userPrivate.writeToParcel(in, 0);

				admin = userPrivate.product.equals("premium");
				if (!admin)
					admin = userPrivate.display_name.equals("pyschopenguin");

				CredentialsHandler.setUserInfo(getBaseContext(), userPrivate);
			}

			@Override
			public void failure(RetrofitError error)
			{

			}
		});
	}

	public void btnCreateRoom_onClick(View view)
	{
		final List<UserPrivate> userList = new ArrayList<>();
		if (userPrivate == null || in == null)
			return;

		DateFormat df = new SimpleDateFormat("EEE, MMM d, yyyy HH:mm");
		String dateFormatted = df.format(Calendar.getInstance().getTime());

		// Create a new UserExtension that contains it's permissions.
		extension = new UserPrivateExtension(userPrivate, admin, in);

		// Add it to the list of users.
		userList.add(extension);

		// Create a new room, with a unique identifier generated in the constructor.
		Room r = new Room(userList);

		// Add that room the to database.
		FirebaseUtils.addRoomToDatabase(r, dateFormatted);

		// Start the FragmentControlActivity, passing on the room identifer.
		// This allows us to pass the room identifer to all the fragments.
		// Having the room identifer access allows us to get/add items from and to the database.
		startFragmentActivityHolder(r.getIdentifier());
	}

	public void btnJoinRoom_onClick(View view)
	{
		// Get the code from the EditText, and then change it to fully uppercase - incase the user entered it in lowercase.
		final String roomCodeText = etRoomCode.getText().toString().toUpperCase().trim();
		if (roomCodeText.equals("") || roomCodeText.equals(" "))
		{
			Toast.makeText(this, "Room code cannot be an empty charachter literal.", Toast.LENGTH_SHORT).show();
			return;
		}

		// Get the main database reference.
		// In this case it is all the rooms.
		DatabaseReference ref = FirebaseUtils.ROOM_REF;
		// Add a listener for a single event.
		ref.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot)
			{
				if (!dataSnapshot.hasChild(roomCodeText))
				{
					Toast.makeText(RoomCreationActivity.this, "Room does not exist.", Toast.LENGTH_LONG).show();
					return;
				}

				extension = new UserPrivateExtension(userPrivate, admin, in);
				// Add the user to the specific room.
				FirebaseUtils.addUserToRoom(roomCodeText, extension);
				// Start the FragmentControlActivity, passing on the room identifer.
				// This allows us to pass the room identifer to all the fragments.
				// Having the room identifer access allows us to get/add items from and to the database.
				startFragmentActivityHolder(roomCodeText);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError)
			{

			}
		});

	}

	private void startFragmentActivityHolder(String identifer)
	{
		Intent i = new Intent(getBaseContext(), FragmentControlActivity.class);
		i.putExtra("ident", identifer);
		startActivity(i);
	}

}
