package com.example.gutman.shuffleparty.utils;

import android.support.annotation.NonNull;
import android.support.test.espresso.core.internal.deps.guava.base.Joiner;

import com.example.gutman.shuffleparty.data.Room;
import com.example.gutman.shuffleparty.data.UserPrivateExtension;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;

public class FirebaseUtils
{
	public static final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();
	public static final DatabaseReference ROOM_REF = DATABASE.getReference().child("Rooms");

	public static void createRoomToDatabase(Room room)
	{
		DatabaseReference roomRef = ROOM_REF.child(room.getIdentifier());
		DatabaseReference userRef = roomRef.child("users").push();

		for (UserPrivate u : room.getConnectedUsers())
		{
			if (u != null)
				userRef.setValue(u);
		}
	}

	public static void addUserToRoom(String identifier, UserPrivateExtension u) {
		DatabaseReference currentRoomUserRef = getCurrentRoomUsersReference(identifier);
		DatabaseReference pushedRef = currentRoomUserRef.push();

		pushedRef.setValue(u);
	}

	public static void addTrackToDatabase(String identifier, Track t)
	{
		DatabaseReference currentRoomTrackRef = getCurrentRoomTrackReference(identifier);
		currentRoomTrackRef.push().setValue(t);
	}

	public static DatabaseReference getCurrentRoomTrackReference(String identifer)
	{
		return ROOM_REF.child(identifer).child("tracks");
	}

	public static DatabaseReference getCurrentRoomUsersReference(String identifier)
	{
		return ROOM_REF.child(identifier).child("users");
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
				for (DataSnapshot ds : dataSnapshot.getChildren())
				{
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

	public static void deleteRoomFromDatabase(String identifer)
	{
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
}
