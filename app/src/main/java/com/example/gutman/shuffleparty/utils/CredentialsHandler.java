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
	private static final String IMAGE_URL = "img_url";
	private static final String USER_ID = "user_id";

	public static void clearAll(Context context) {
		Context appContext = context.getApplicationContext();

		SharedPreferences sharedPreferences = getSharedPref(appContext);
		sharedPreferences.edit().clear().commit();
	}

	public static void setUserInfo(Context context, UserPrivate user) {
		Context appContext = context.getApplicationContext();

		String displayName = user.display_name;
		String country = user.country;
		String uri = user.uri;
		String userId = user.id;

		SharedPreferences sharedPref = getSharedPref(appContext);
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putString(DISPLAY_NAME, displayName);
		editor.putString(COUNTRY, country);
		editor.putString(URI, uri);
		editor.putString(USER_ID, userId);
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

	public static UserPrivate getUserInfo(Context context) {
		UserPrivate userPrivate = new UserPrivate();
		Context appContext = context.getApplicationContext();

		SharedPreferences sharedPref = getSharedPref(appContext);
		userPrivate.display_name = sharedPref.getString(DISPLAY_NAME, "");
		userPrivate.country = sharedPref.getString(COUNTRY, "IL");
		userPrivate.uri = sharedPref.getString(URI, "");
		userPrivate.id = sharedPref.getString(USER_ID, "");

		return userPrivate;
	}

	private static SharedPreferences getSharedPref(Context appContext)
	{
		return appContext.getSharedPreferences(ACCESS_TOKEN_NAME, Context.MODE_PRIVATE);
	}
}
