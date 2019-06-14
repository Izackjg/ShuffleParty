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
	private List<UserPrivateExtension> connectedUsers;

	public Room(List<UserPrivateExtension> connectedUsers) {
		identifier = AlphabeticUtils.getRandomStringSequence(IDENTIFIER_LEN);
		this.playlistTracks = new ArrayList<>();
		this.connectedUsers = connectedUsers;
	}

	public List<UserPrivateExtension> getConnectedUsers()
	{
		return connectedUsers;
	}

	public String getIdentifier()
	{
		return identifier;
	}
}
