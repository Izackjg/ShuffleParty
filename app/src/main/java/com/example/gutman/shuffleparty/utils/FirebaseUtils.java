package com.example.gutman.shuffleparty.utils;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.espresso.core.internal.deps.guava.base.Joiner;

import com.example.gutman.shuffleparty.data.Room;
import com.example.gutman.shuffleparty.data.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;

public class FirebaseUtils
{
	public static final String MAIN = "Tracks";
	public static final String TITLE_CHILD = "Title";
	public static final String ARTISTS_CHILD = "Artists";
	public static final String EXPLICITY_CHILD = "Explicity";
	public static final String DURATION_MS_CHILD = "DurationMs";
	public static final String TRACK_URI_CHILD = "TrackUri";
	public static final String IMAGE_URL_CHILD = "ImageUrl";

	public static final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();
	public static final DatabaseReference ROOM_REF = DATABASE.getReference().child("Rooms");

	public static void createRoomToDatabase(Room room)
	{
		DatabaseReference roomRef = ROOM_REF.child(room.getIdentifier());
		DatabaseReference userRef = roomRef.child("users").push();

		UserPrivate u1 = room.getConnectedUsers().get(0);
		if (u1 != null)
		{
			userRef.setValue(u1);
		}
	}

	public static void addTrackToDatabase(String identifier, Track t)
	{
		DatabaseReference currentRoomRef = ROOM_REF.child(identifier);
		currentRoomRef.child("tracks").push().setValue(t);
	}

	public static DatabaseReference getCurrentRoomTrackReference(String identifer) {
		return ROOM_REF.child(identifer).child("tracks");
	}

	// TODO: ADD A PARAMETER TO THIS - DATABASEREF
	// TODO: THEN ADD THE LISTENER TO THE REF AND USE THAT IN THE PLAYLISTFRAGMENT. HAVING IT RETURN THE LIST.
	public static List<Track> getTracksFromDatabase(String identifer)
	{
		final List<Track> trackList = new ArrayList<>();

		DatabaseReference currentRoomRef = ROOM_REF.child(identifer).child("tracks");
		currentRoomRef.addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot)
			{
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					Track t = ds.getValue(Track.class);
					trackList.add(t);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError)
			{

			}
		});

		return trackList;
	}

	public static void deleteRoomFromDatabase(String identifer) {
		DatabaseReference ref = ROOM_REF.child(identifer);
		ref.removeValue();
	}

	private static String getAlbumArtists(Track item)
	{
		List<String> names = new ArrayList<>();
		for (ArtistSimple i : item.artists)
		{
			names.add(i.name);
		}
		Joiner joiner = Joiner.on(", ");
		return joiner.join(names);
	}

	private static List<String> getAlbumArtistsSplit(String artists)
	{
		List<String> individualArtists = Arrays.asList(artists.split(","));
		List<String> individualArtistsTrimmed = new ArrayList<>();
		for (int i = 0; i < individualArtists.size(); i++)
		{
			String current = individualArtists.get(i);
			individualArtistsTrimmed.add(current.trim());
		}
		return individualArtistsTrimmed;
	}

	public static List<ArtistSimple> getAlbumArtists(String artists)
	{
		List<String> individualArtists = getAlbumArtistsSplit(artists);
		List<ArtistSimple> artistSimpleList = new ArrayList<>();
		for (String s : individualArtists)
		{
			ArtistSimple current = new ArtistSimple();
			current.name = s;
			artistSimpleList.add(current);
		}
		return artistSimpleList;
	}

}
