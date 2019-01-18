package com.example.gutman.shuffleparty;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment
{

	private TextView tvHomeRoomIdentifier;

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
		if (args != null) {
			String roomIdentifer = args.getString("ident");
			tvHomeRoomIdentifier.setText(roomIdentifer);
		}

		return view;
	}
}
