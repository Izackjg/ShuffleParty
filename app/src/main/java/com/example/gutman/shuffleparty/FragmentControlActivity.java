package com.example.gutman.shuffleparty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.gutman.shuffleparty.data.UserPrivateExtension;
import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

// TODO: DELETE USER WHEN HE HITS EXIT ROOM BUTTON. IF ADMIN PROMT FOR LEAVE ONLY OR DELETE ROOM.

public class FragmentControlActivity extends AppCompatActivity
{
	private DatabaseReference userRef;

	private Context main;

	private boolean admin = false;
	private String roomIdentifier;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);

		main = this;

		// Check if API Token has expired.
		String apiToken = CredentialsHandler.getToken(this);
		if (apiToken == null)
		{
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		}

		// Find the BottomNavView and add its item selected listener.
		BottomNavigationView navView = findViewById(R.id.navigation);
		navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

		roomIdentifier = getIntent().getStringExtra("ident");
		userRef = FirebaseUtils.getUsersReference(roomIdentifier);
		userRef.addListenerForSingleValueEvent(getAdminValueListener);

		Bundle b = new Bundle();
		b.putString("ident", roomIdentifier);

		// Start the HomeFragment first always.
		// This will always execute because it is in the onCreate method.
		Fragment homeFragment = new Fragment();
		homeFragment.setArguments(b);
		navView.setSelectedItemId(R.id.navigation_home);
		addFragment(homeFragment);
	}

	private final BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener =
			new BottomNavigationView.OnNavigationItemSelectedListener()
			{
				@SuppressLint("NonConstantResourceId")
				@Override
				public boolean onNavigationItemSelected(@NonNull MenuItem item)
				{
					Fragment fragment;
					// Return the FragmentManager for interacting with fragments associated with this activity.
					Fragment current = getSupportFragmentManager().findFragmentById(R.id.frame_container);

					Bundle b = new Bundle();
					b.putString("ident", roomIdentifier);

					switch (item.getItemId())
					{
						case R.id.navigation_home:
							if (current instanceof HomeFragment)
								return false;

							fragment = new HomeFragment();
							fragment.setArguments(b);
							loadFragment(fragment);
							return true;

						case R.id.navigation_search:
							if (current instanceof SearchTracksFragment)
								return false;

							fragment = new SearchTracksFragment();
							fragment.setArguments(b);
							loadFragment(fragment);
							return true;

						case R.id.navigation_playlist:
							if (current instanceof PlaylistFragment)
								return false;

							fragment = new PlaylistFragment();
							fragment.setArguments(b);
							loadFragment(fragment);
							return true;

						case R.id.navigation_users:
							if (current instanceof UsersFragment)
								return false;

							fragment = new UsersFragment();
							fragment.setArguments(b);
							loadFragment(fragment);
							return true;
						case R.id.navigation_leave:
							if (admin)
								deleteRoomDialogForAdmin();
							else
							{
								Intent i = new Intent(main, RoomCreationActivity.class);
								startActivity(i);
								finish();
							}
					}

					return false;
				}
			};

	private void deleteRoomDialogForAdmin()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Leave Room")
				.setMessage("Are you sure you want to leave this room?")
				.setNegativeButton("No", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// do nothing
					}
				}).setPositiveButton("Leave Room", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				userRef.addListenerForSingleValueEvent(new ValueEventListener()
				{
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot)
					{
						long childrenCount = 0;
						for (DataSnapshot ds : dataSnapshot.getChildren())
						{
							// Children count gets decreased by one, because it gets the track node, and the user node.
							// So we only need one, if there is only one node, we know users has been deleted.
							// User node deleted -> no users in the room.
							childrenCount = ds.getChildrenCount() - 1;

							//Log.d(main.getClass().getSimpleName(), "LOGGING CHILDREN COUNT: " + childrenCount);
							UserPrivateExtension extension = ds.getValue(UserPrivateExtension.class);
							//Log.d(main.getClass().getSimpleName(), "LOGGING KEY: " + ds.getKey());
							if (extension.getUserPrivate().uri.equals(CredentialsHandler.getUserUri(main)))
							{
								//Log.d(main.getClass().getSimpleName(), "LOGGING KEY IF: " + ds.getKey());
								userRef.child(ds.getKey()).removeValue();
								childrenCount--;
								//Log.d(main.getClass().getSimpleName(), "LOGGING CHILDREN COUNT: " + childrenCount);
								if (childrenCount == 0)
									FirebaseUtils.deleteRoomFromDatabase(roomIdentifier);
							}
						}

					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError)
					{

					}
				});
				Intent mainIntent = new Intent(main, RoomCreationActivity.class);
				startActivity(mainIntent);
				finish();
			}
		}).show();
	}

	private final ValueEventListener getAdminValueListener = new ValueEventListener()
	{
		@Override
		public void onDataChange(@NonNull DataSnapshot dataSnapshot)
		{
			for (DataSnapshot ds : dataSnapshot.getChildren())
			{
				UserPrivateExtension extension = ds.getValue(UserPrivateExtension.class);
				admin = extension.getAdmin();
			}
		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError)
		{

		}
	};

	private void loadFragment(Fragment f)
	{
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();

		transaction.replace(R.id.frame_container, f);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	private void addFragment(Fragment f)
	{
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();

		transaction.add(R.id.frame_container, f);
		transaction.addToBackStack(null);
		transaction.commit();
	}

}
