package com.example.gutman.shuffleparty.utils;

import android.support.test.espresso.core.internal.deps.guava.base.Joiner;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.protocol.types.Artist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;

public class SpotifyUtils
{
	public enum SEARCH_TYPE {
		Album,
		Track
	}

	public static final int NO_REPEAT = 0;
	public static final int REPEAT = 1;

	public static String getRandomTitle()
	{
		List<String> titles = Arrays.asList("Good Form", "Never Recover", "Mama", "Eminem Fall", "Kazka Cry", "rockstar", "Blame It On Me",
				"GOKU", "Psycho", "Meek Mill Going Bad", "Bartier Cardi", "Commando", "Bosses Dont Speak", "Slippery", "Get Right Witcha", "Walk It Talk It",
				"Congratulations", "Plastic", "MotorSport", "Drop Top Benz", "Multi Millionaire", "Juicy J Neighbor", "Tyga Dip", "Talk Regardless", "LLC",
				"Designer Outlet", "Designer Overseas", "Sicko Mode", "Stargazing", "Carousel", "Logic Iconic", "NAV Know Me", "Quavo RERUN", "Everybody Dies", "Uproar",
				"BACK ON MY SH*T", "Stranger Things", "The Passion", "WORKIN ME", "Icon", "Kelly Price", "Saint", "Bodak Yellow", "Ty Dolla Sign Clout", "goosebumps",
				"Antidote", "44 More", "Heavy Camp", "Flexicution", "Contra", "BUTTERFLY EFFECT", "Chun Li", "Rae Sremmurd Close", "Stir Fry", "6 Foot 7 Foot");

		String item = titles.get(new Random().nextInt(titles.size()));
		return item;
	}

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

	private static boolean containsUnwantedChars(String title, String unwanted)
	{
		if (title.contains(unwanted))
			return true;
		return false;
	}
}
