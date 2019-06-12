package com.example.gutman.shuffleparty.data;

import android.os.Parcel;

import kaaes.spotify.webapi.android.models.UserPrivate;

public class UserPrivateExtension extends UserPrivate
{
	private boolean admin;
	private Parcel in;

	public UserPrivateExtension()
	{

	}

	public UserPrivateExtension(UserPrivate userPrivate, boolean admin, Parcel in)
	{
		super(in);
		this.admin = admin;
	}

	public boolean getAdmin()
	{
		return admin;
	}
}
