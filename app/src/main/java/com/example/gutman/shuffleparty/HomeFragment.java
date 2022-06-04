package com.example.gutman.shuffleparty;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


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
