package com.example.gutman.shuffleparty;


import android.os.Bundle;
import android.print.PrinterId;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment
{
	private DatabaseReference userRef;

	private TextView tvHomeRoomIdentifier;
	private Button btnDisconnect;
	private Button btnDeleteRoom;

	private String userUri;
	private String roomIdentifier;

	public HomeFragment()
	{
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_home, container, false);

		tvHomeRoomIdentifier = view.findViewById(R.id.tvRoomHomeIdentifier);

		Bundle args = getArguments();
		if (args != null)
		{
			roomIdentifier = args.getString("ident");
			tvHomeRoomIdentifier.setText(roomIdentifier);
		}

		userUri = CredentialsHandler.getUserUri(container.getContext());
		userRef = FirebaseUtils.getUsersReference(roomIdentifier);

		btnDisconnect = view.findViewById(R.id.frag_btnDisconnect);
		btnDeleteRoom = view.findViewById(R.id.frag_btnDeleteRoom);

		btnDisconnect.setOnClickListener(btnDisconnectListener);
		btnDeleteRoom.setOnClickListener(btnDeleteRoomListener);

		return view;
	}

	private View.OnClickListener btnDisconnectListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Log.d("FragmentControlActivity", "LOGGING: onclick");
			userRef.orderByChild("uri").equalTo(userUri).addListenerForSingleValueEvent(new ValueEventListener()
			{
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot)
				{
					userRef= dataSnapshot.child("user").getRef();
					userRef.removeValue();
					userRef= dataSnapshot.child("admin").getRef();
					userRef.removeValue();
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError)
				{

				}
			});
		}
	};

	private View.OnClickListener btnDeleteRoomListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{

		}
	};
}
