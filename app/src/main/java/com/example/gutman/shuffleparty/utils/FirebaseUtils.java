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

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;

public class FirebaseUtils
{
	public static final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();
	public static final DatabaseReference ROOM_REF = DATABASE.getReference().child("Rooms");

	public static void addRoomToDatabase(Room room, String createdAt)
	{
		DatabaseReference roomRef = ROOM_REF.child(room.getIdentifier());
		roomRef.child("created").setValue(createdAt);

		for (UserPrivateExtension u : room.getConnectedUsers())
		{
			if (u != null)
				addUserToRoom(room.getIdentifier(), u);
		}
	}

	public static void addUserToRoom(String identifier, UserPrivateExtension u)
	{
		DatabaseReference currentRoomUserRef = getUsersReference(identifier);
		DatabaseReference pushedRef = currentRoomUserRef.push();

		pushedRef.setValue(u);
	}

	public static void addTrackToDatabase(String identifier, Track t)
	{
		DatabaseReference currentRoomTrackRef = getTrackReference(identifier);
		currentRoomTrackRef.push().setValue(t);
	}

	public static DatabaseReference getTrackReference(String identifer)
	{
		return ROOM_REF.child(identifer).child("tracks");
	}

	public static DatabaseReference getUsersReference(String identifier)
	{
		return ROOM_REF.child(identifier).child("users");
	}

	public static void deleteRoomFromDatabase(String identifer)
	{
		DatabaseReference ref = ROOM_REF.child(identifer);
		ref.removeValue();
	}
}
