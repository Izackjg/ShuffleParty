package com.example.gutman.shuffleparty.data;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;

public class Room
{
	public static final int IDENTIFIER_LEN = 6;

	private String identifier;
	private List<Track> playlistTracks;
	private List<UserPrivate> connectedUsers;

	public Room(List<UserPrivate> connectedUsers) {
		identifier = AlphabeticUtils.getRandomStringSequence(IDENTIFIER_LEN);
		this.playlistTracks = new ArrayList<>();
		this.connectedUsers = connectedUsers;
	}

	public List<UserPrivate> getConnectedUsers()
	{
		return connectedUsers;
	}

	public String getIdentifier()
	{
		return identifier;
	}
}
