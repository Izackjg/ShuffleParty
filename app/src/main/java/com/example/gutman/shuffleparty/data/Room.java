package com.example.gutman.shuffleparty.data;

import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.List;

public class Room
{
	public static final int IDENTIFIER_LEN = 6;

	private String identifier;
	private List<Track> playlistTracks;
	private List<User> connectedUsers;

	public Room() {
		identifier = AlphabeticUtils.getRandomStringSequence(IDENTIFIER_LEN);
		playlistTracks = new ArrayList<>();
		connectedUsers = new ArrayList<>();
	}

	public Room(List<User> connectedUsers) {
		identifier = AlphabeticUtils.getRandomStringSequence(IDENTIFIER_LEN);
		this.playlistTracks = new ArrayList<>();
		this.connectedUsers = connectedUsers;
	}

	public void addTrackToPlaylist(Track t) {
		playlistTracks.add(t);
	}

	public void addUser(User u) {
		connectedUsers.add(u);
	}

	public List<User> getConnectedUsers()
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
