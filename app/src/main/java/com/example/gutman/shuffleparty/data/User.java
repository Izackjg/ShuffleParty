package com.example.gutman.shuffleparty.data;

import kaaes.spotify.webapi.android.models.UserPrivate;

public class User
{
	private String displayName;
	private String country;
	private String product;

	public User()
	{

	}

	public User(UserPrivate other)
	{
		this.displayName = other.display_name;
		this.country = other.country;
		this.product = other.product;
	}

	public User(String displayName, String country, String product)
	{
		this.displayName = displayName;
		this.country = country;
		this.product = product;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getCountry()
	{
		return country;
	}

	public String getProduct()
	{
		return product;
	}
}
