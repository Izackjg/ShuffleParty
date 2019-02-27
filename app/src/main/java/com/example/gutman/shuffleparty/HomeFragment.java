package com.example.gutman.shuffleparty;


import android.content.Context;
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

import com.example.gutman.shuffleparty.data.UserPrivateExtension;
import com.example.gutman.shuffleparty.utils.CredentialsHandler;
import com.example.gutman.shuffleparty.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment
{
	private TextView tvHomeRoomIdentifier;
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

		return view;
	}
}
