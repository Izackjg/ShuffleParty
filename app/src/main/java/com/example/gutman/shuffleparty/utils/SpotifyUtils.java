package com.example.gutman.shuffleparty.utils;

import android.support.test.espresso.core.internal.deps.guava.base.Joiner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

public class SpotifyUtils
{

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

	public static String formatUnwantedCharsFromTitle(String title, String unwanted) {
		if (containsUnwantedChars(title, unwanted))
		{
			int index = title.indexOf(unwanted);
			return title.substring(0, index);
		}
		return title;
	}

	private static boolean containsUnwantedChars(String title, String unwanted)
	{
		if (title.contains(unwanted))
			return true;
		return false;
	}
}
