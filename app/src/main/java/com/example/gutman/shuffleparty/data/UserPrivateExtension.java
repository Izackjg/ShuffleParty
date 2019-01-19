package com.example.gutman.shuffleparty.data;

import android.os.Parcel;

import kaaes.spotify.webapi.android.models.UserPrivate;

public class UserPrivateExtension extends UserPrivate
{
	private UserPrivate user;
	private PermissionType permType;
	private Parcel in;

	public UserPrivateExtension()
	{

	}

	public UserPrivateExtension(UserPrivate userPrivate, PermissionType permType, Parcel in)
	{
		super(in);
		this.user = userPrivate;
		this.permType = permType;
	}

	public PermissionType getPermType()
	{
		return permType;
	}

	public UserPrivate getUser()
	{
		return user;
	}
}
