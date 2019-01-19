package com.example.gutman.shuffleparty.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.inputmethod.EditorInfo;

import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.models.UserPrivate;

public class CredentialsHandler
{
	private static final String ACCESS_TOKEN_NAME = "webapi.credentials.access_token";

	private static final String ACCESS_TOKEN = "access_token";
	private static final String EXPIRES_AT = "expires_at";

	private static final String DISPLAY_NAME = "display_name";
	private static final String COUNTRY = "country";
	private static final String URI = "user_uri";
	private static final String USER_ID = "user_id";

	public static void clearAll(Context context) {
		Context appContext = context.getApplicationContext();

		SharedPreferences sharedPreferences = getSharedPref(appContext);
		sharedPreferences.edit().clear().commit();
	}

	public static void setUserInfo(Context context, String displayName) {
		Context appContext = context.getApplicationContext();

		SharedPreferences sharedPref = getSharedPref(appContext);
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putString(DISPLAY_NAME, displayName);
		editor.apply();
	}

	public static void setToken(Context context, String token, long expiresIn, TimeUnit unit)
	{
		Context appContext = context.getApplicationContext();

		long now = System.currentTimeMillis();
		long expiresAt = now + unit.toMillis(expiresIn);

		SharedPreferences sharedPref = getSharedPref(appContext);
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putString(ACCESS_TOKEN, token);
		editor.putLong(EXPIRES_AT, expiresAt);

		editor.apply();
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

	public static String getUserDisplayName(Context context) {
		Context appContext = context.getApplicationContext();

		SharedPreferences sharedPref = getSharedPref(appContext);
		String displayName = sharedPref.getString(DISPLAY_NAME, "");

		return displayName;
	}

	private static SharedPreferences getSharedPref(Context appContext)
	{
		return appContext.getSharedPreferences(ACCESS_TOKEN_NAME, Context.MODE_PRIVATE);
	}
}
