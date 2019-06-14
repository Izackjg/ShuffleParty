package com.example.gutman.shuffleparty.data;

import android.os.Parcel;

import java.io.Serializable;

import kaaes.spotify.webapi.android.models.UserPrivate;

public class UserPrivateExtension
{
	private UserPrivate userPrivate;
	private boolean admin;


	public UserPrivateExtension()
	{

	}

	public UserPrivateExtension(UserPrivate userPrivate, boolean admin)
	{
		this.userPrivate = userPrivate;
		this.admin = admin;
	}


	public boolean getAdmin()
	{
		return admin;
	}

	public UserPrivate getUserPrivate()
	{
		return userPrivate;
	}
}
