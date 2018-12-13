package com.example.gutman.shuffleparty.utils;

import android.support.test.espresso.core.internal.deps.guava.base.Joiner;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

public class SpotifyUtils
{

	public static SpotifyService getInstance(String apiToken) {
		SpotifyApi api = new SpotifyApi();
		api.setAccessToken(apiToken);
		return api.getService();
	}

	public static String toStringFromArtists(Track item) {
		List<String> names = new ArrayList<>();
		for (ArtistSimple i : item.artists)
		{
			names.add(i.name);
		}
		Joiner joiner = Joiner.on(", ");
		return joiner.join(names);
	}
}
