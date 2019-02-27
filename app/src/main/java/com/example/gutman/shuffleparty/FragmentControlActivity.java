package com.example.gutman.shuffleparty;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.gutman.shuffleparty.data.UserPrivateExtension;
import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

// TODO: DELETE USER WHEN HE HITS EXIT ROOM BUTTON. IF ADMIN PROMT FOR LEAVE ONLY OR DELETE ROOM.

public class FragmentControlActivity extends AppCompatActivity
{
	private DatabaseReference userRef;

	private Context main;
	private BottomNavigationView navView;

	private boolean debug = true;
	private boolean admin = false;
	private String roomIdentifier;
	private String uri;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);

		main = this;

		String apiToken = CredentialsHandler.getToken(this);
		if (apiToken == null)
		{
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		}

		navView = findViewById(R.id.navigation);
		navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

		roomIdentifier = getIntent().getStringExtra("ident");
		userRef = FirebaseUtils.getUsersReference(roomIdentifier);
		userRef.addListenerForSingleValueEvent(getAdminValueListener);

		uri = CredentialsHandler.getUserUri(this);

		Bundle b = new Bundle();
		b.putString("ident", roomIdentifier);

		boolean bundleValuesNull = b.getString("ident") == null;

		if (!bundleValuesNull)
		{
			Fragment homeFragment = new HomeFragment();
			homeFragment.setArguments(b);
			navView.setSelectedItemId(R.id.navigation_home);
			addFragment(homeFragment);
		}
	}

	private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener =
			new BottomNavigationView.OnNavigationItemSelectedListener()
			{
				@Override
				public boolean onNavigationItemSelected(@NonNull MenuItem item)
				{
					Fragment fragment;
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
							if (current instanceof SearchFragment)
								return false;

							fragment = new SearchFragment();
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
							else {
								Intent i = new Intent(main, RoomControlActivity.class);
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
		builder.setTitle("Delete Room")
				.setMessage("Are you sure you want to delete this room?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (!debug)
							FirebaseUtils.deleteRoomFromDatabase(roomIdentifier);
						Intent i = new Intent(main, RoomControlActivity.class);
						startActivity(i);
						finish();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// do nothing
			}
		}).setNeutralButton("Leave Room", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// delete user from room. if user is only one with premium, delete room.
			}
		}).show();
	}

	private ValueEventListener getAdminValueListener = new ValueEventListener()
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
