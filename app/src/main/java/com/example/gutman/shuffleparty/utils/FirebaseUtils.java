package com.example.gutman.shuffleparty.utils;

import com.example.gutman.shuffleparty.data.Room;
import com.example.gutman.shuffleparty.data.UserPrivateExtension;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import kaaes.spotify.webapi.android.models.Track;

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
