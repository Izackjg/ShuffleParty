package com.example.gutman.shuffleparty.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

public class CredentialsHandler
{
	private static final String ACCESS_TOKEN_NAME = "webapi.credentials.access_token";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String EXPIRES_AT = "expires_at";

	private static final String COUNT = "count";
	public static int COUNT_NUM = 1;

	public static void setToken(Context context, String token, long expiresIn, TimeUnit unit)
	{
		Context appContext = context.getApplicationContext();

		long now = System.currentTimeMillis();
		long expiresAt = now + unit.toMillis(expiresIn);

		SharedPreferences sharedPref = getSharedPref(appContext);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(ACCESS_TOKEN, token);
		editor.putLong(EXPIRES_AT, expiresAt);
		editor.putInt(COUNT, COUNT_NUM);
		editor.apply();

		COUNT_NUM += 1;
	}

	public static String getToken(Context context)
	{
		Context appContext = context.getApplicationContext();
		SharedPreferences sharedPref = getSharedPref(appContext);

		String token = sharedPref.getString(ACCESS_TOKEN, null);
		long expiresAt = sharedPref.getLong(EXPIRES_AT, 0L);

		if (token == null || expiresAt < System.currentTimeMillis())
		{
			return null;
		}

		return token;
	}

	private static SharedPreferences getSharedPref(Context appContext)
	{
		return appContext.getSharedPreferences(ACCESS_TOKEN_NAME, Context.MODE_PRIVATE);
	}
}
