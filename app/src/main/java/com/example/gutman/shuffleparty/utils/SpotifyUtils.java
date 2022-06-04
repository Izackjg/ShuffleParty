package com.example.gutman.shuffleparty.utils;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.protocol.types.Artist;

import java.util.ArrayList;
import java.util.List;

import androidx.test.espresso.core.internal.deps.guava.base.Joiner;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

public class SpotifyUtils
{
	public enum SEARCH_TYPE {
		Album,
		Track
	}

	public static final int NO_REPEAT = 0;
	public static final int REPEAT = 1;

	public static SpotifyService getInstance(String apiToken)
	{
		SpotifyApi api = new SpotifyApi();
		api.setAccessToken(apiToken);
		return api.getService();
	}

	public static ConnectionParams getParams()
	{
		return new ConnectionParams.Builder(SpotifyConstants.ClientID)
				.setRedirectUri(SpotifyConstants.REDIRECT_URL)
				.showAuthView(true)
				.build();
	}

	public static String toStringFromArtists(Track item)
	{
		List<String> names = new ArrayList<>();
		for (ArtistSimple i : item.artists)
		{
			names.add(i.name);
		}
		Joiner joiner = Joiner.on(", ");
		return joiner.join(names);
	}

	public static String toStringFromArtists(com.spotify.protocol.types.Track item)
	{
		List<String> names = new ArrayList<>();
		for (Artist i : item.artists)
		{
			names.add(i.name);
		}
		Joiner joiner = Joiner.on(", ");
		return joiner.join(names);
	}

	public static String formatUnwantedCharsFromTitle(String title, String unwanted)
	{
		if (containsUnwantedChars(title, unwanted))
		{
			int index = title.indexOf(unwanted);
			return title.substring(0, index);
		}
		return title;
	}

	public static String formatTimeDuration(int totalSeconds)
	{
		int mins = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;

		return String.format("%02d:%02d", mins, seconds);
	}

	public static int getIndex(List<Track> tracks, com.spotify.protocol.types.Track currentStateTrack) {
		for (int i = 0; i < tracks.size(); i++) {
			Track t = tracks.get(i);
			if (t.uri.equals(currentStateTrack.uri))
				return i + 1;
		}
		return -1;
	}

	private static String getSpecificFormattedValues(com.spotify.protocol.types.Track currentStateTrack) {
		String name = currentStateTrack.name;
		String artists = toStringFromArtists(currentStateTrack);
		return "Name: " + name + ", Artists: " + artists;
	}

	private static String getSpecificFormattedValues(Track t) {
		String name = t.name;
		String artists = toStringFromArtists(t);
		return "Name: " + name + ", Artists: " + artists;
	}

	private static boolean containsUnwantedChars(String title, String unwanted)
	{
		if (title.contains(unwanted))
			return true;
		return false;
	}
}
