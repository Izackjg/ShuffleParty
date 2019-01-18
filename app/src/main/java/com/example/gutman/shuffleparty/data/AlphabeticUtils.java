package com.example.gutman.shuffleparty.data;

import java.util.Random;

public class AlphabeticUtils
{
	public static final String ALPHABET_LOWER = "abcdefghijklmnopqrstuvwxyz";
	public static final String ALPHABET_UPPER = ALPHABET_LOWER.toUpperCase();
	public static final String NUMERALS = "1234567890";

	public static final String COMBINED = ALPHABET_UPPER + NUMERALS;

	public static String getRandomStringSequence(int len) {
		Random rnd = new Random();

		// Returns number between 0 and 1.
		double chance = Math.random();
		if (chance <= 0.01)
			return "PY5CH0";

		StringBuilder stringBuilder = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			stringBuilder.append(COMBINED.charAt(rnd.nextInt(COMBINED.length())));
		}
		return stringBuilder.toString();
	}
}
