package com.example.gutman.shuffleparty.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit.http.POST;


public class CredentialsHandler
{
	private static final String ACCESS_TOKEN_NAME = "webapi.credentials.access_token";

	private static final String ACCESS_TOKEN = "access_token";
	private static final String EXPIRES_AT = "expires_at";

	private static final String DISPLAY_NAME = "display_name";
	private static final String PRODUCT_TYPE = "product_type";

	public static void clearAll(Context context)
	{
		Context appContext = context.getApplicationContext();

		SharedPreferences sharedPreferences = getSharedPref(appContext);
		sharedPreferences.edit().clear().commit();
	}

	public static void setUserInfo(Context context, String displayName, String product)
	{
		Context appContext = context.getApplicationContext();

		SharedPreferences sharedPref = getSharedPref(appContext);
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putString(DISPLAY_NAME, displayName);
		editor.putString(PRODUCT_TYPE, product);
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

	public static String getUserDisplayName(Context context)
	{
		Context appContext = context.getApplicationContext();

		SharedPreferences sharedPref = getSharedPref(appContext);
		String displayName = sharedPref.getString(DISPLAY_NAME, "");

		return displayName;
	}

	public static String getUserProduct(Context context)
	{
		Context appContext = context.getApplicationContext();

		SharedPreferences sharedPref = getSharedPref(appContext);
		String product = sharedPref.getString(PRODUCT_TYPE, "");

		return product;
	}

	private static SharedPreferences getSharedPref(Context appContext)
	{
		return appContext.getSharedPreferences(ACCESS_TOKEN_NAME, Context.MODE_PRIVATE);
	}
}
