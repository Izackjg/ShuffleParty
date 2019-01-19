package com.example.gutman.shuffleparty;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gutman.shuffleparty.data.PermissionType;
import com.example.gutman.shuffleparty.data.UserPrivateExtension;
import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.UserPrivate;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment
{
	private Context main;
	private Activity mainActivity;
	private RecyclerView connectedUsersView;

	private List<UserPrivate> users;
	private List<PermissionType> permissionTypes;
	private SpotifyUserAdapter userAdapter;

	private String roomIdentifer;

	public UsersFragment()
	{
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		main = container.getContext();
		mainActivity = getActivity();

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_users, container, false);

		connectedUsersView = view.findViewById(R.id.frag_user_list);
		connectedUsersView.setLayoutManager(new LinearLayoutManager(main));
		connectedUsersView.setHasFixedSize(true);

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		Bundle b = getArguments();
		if (b != null)
			roomIdentifer = b.getString("ident");

		setupRecyclerView();
	}

	private void setupRecyclerView()
	{
		users = new ArrayList<>();
		permissionTypes = new ArrayList<>();

		// Get the database reference at the current connected room identifer.
		DatabaseReference ref = FirebaseUtils.getCurrentRoomUsersReference(roomIdentifer);
		// Set its value event listener.
		ref.addValueEventListener(valueEventListener);
	}

	// Has events about data changes at a location.
	// In this specific case, the location is at the current connected room reference.
	private ValueEventListener valueEventListener = new ValueEventListener()
	{
		@Override
		public void onDataChange(@NonNull DataSnapshot dataSnapshot)
		{
			// DataSnapshot is used everytime, containing data from a Firebase Database location.
			// Any time you read Database data, I will receive the data as a DataSnapshot.

			// For all the children in the DataSnapshot
			for (DataSnapshot ds : dataSnapshot.getChildren())
			{
				// Get the PermissionType value, and convert it from Object to a PermissionType.
				// Get the value, and convert it from Object to a Spotify UserPrivate class.
				UserPrivate userPrivate = ds.child("user").getValue(UserPrivate.class);
				PermissionType type = ds.child("permType").getValue(PermissionType.class);
				// Add it to the playlistItems and permissionTypes respectively.
				users.add(userPrivate);
				permissionTypes.add(type);
			}
			// Setup the adapter.
			setupAdapter();
		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError)
		{

		}
	};

	private void setupAdapter()
	{
		userAdapter = new SpotifyUserAdapter(main, users, permissionTypes);
		connectedUsersView.setAdapter(userAdapter);
	}
}
