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

	public Room() {
		identifier = AlphabeticUtils.getRandomStringSequence(IDENTIFIER_LEN);
		playlistTracks = new ArrayList<>();
		connectedUsers = new ArrayList<>();
	}

	public Room(List<UserPrivate> connectedUsers) {
		identifier = AlphabeticUtils.getRandomStringSequence(IDENTIFIER_LEN);
		this.playlistTracks = new ArrayList<>();
		this.connectedUsers = connectedUsers;
	}

	public void addTrackToPlaylist(Track t) {
		playlistTracks.add(t);
	}

	public void addUser(UserPrivate u) {
		connectedUsers.add(u);
	}

	public List<UserPrivate> getConnectedUsers()
	{
		return connectedUsers;
	}

	public List<Track> getPlaylistTracks()
	{
		return playlistTracks;
	}

	public String getIdentifier()
	{
		return identifier;
	}
}
