package com.example.gutman.shuffleparty.utils;

import android.util.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SpotifyHttpPost
{
	private static final String TOKEN_ENDPOINT = "https://accounts.spotify.com/api/token";

	public static String postToSpotifyForAccesAndRefreshTokens(String code) throws
			IOException
	{
		OkHttpClient client = new OkHttpClient();

		//MediaType form = MediaType.parse("application/x-www-form-urlencoded");

		RequestBody body = new FormBody.Builder()
				.add("grant_type", "authorization_code")
				.add("code", code)
				.add("redirect_uri", SpotifyConstants.REDIRECT_URL)
				.build();

		byte[] encodedBytes = SpotifyConstants.Combined.getBytes(StandardCharsets.UTF_8);
		String finalEncoded = Base64.encodeToString(encodedBytes, Base64.NO_WRAP);

		Request request = new Request.Builder()
				.url(TOKEN_ENDPOINT)
				.addHeader("Authorization", "Basic " + finalEncoded)
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.post(body)
				.build();

		Response response = client.newCall(request).execute();
		if (!response.isSuccessful())
			throw new IOException("Unexpected Code: " + response);
		return response.body().string();
	}

	public static String requestRefreshedAccessToken(String refreshToken) throws
			IOException
	{
		OkHttpClient client = new OkHttpClient();

		RequestBody body = new FormBody.Builder()
				.add("grant_type", "refresh_token")
				.add("refresh_token", refreshToken)
				.build();

		byte[] encodedBytes = SpotifyConstants.Combined.getBytes(StandardCharsets.UTF_8);
		String finalEncoded = Base64.encodeToString(encodedBytes, Base64.NO_WRAP);

		Request request = new Request.Builder()
				.url(TOKEN_ENDPOINT)
				.addHeader("Authorization", "Basic " + finalEncoded)
				.post(body)
				.build();

		Response response = client.newCall(request).execute();
		if (!response.isSuccessful())
			throw new IOException("Unexpected Code: " + response);
		return response.body().string();
	}
}
