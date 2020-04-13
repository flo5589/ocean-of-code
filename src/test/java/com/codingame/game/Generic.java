package com.codingame.game;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Generic {

	@Test
	public void testArrayMap() {
		long startTime = System.nanoTime();
		Arrays.stream(new int[][] {
				{ 0, 10 }, { 0, 11 }, { 0, 14 }, { 9, 2 }, { 9, 3 }, { 10, 2 }, { 10, 3 }, { 0, 9 }, { 1, 9 }, { 2, 9 }, { 0, 10 },
				{ 1, 10 }, { 2, 10 }, { 6, 11 }, { 6, 12 }, { 7, 11 }, { 7, 12 }, { 1, 13 }, { 1, 14 }, { 2, 13 }, { 2, 14 }
		}).forEach(s -> {
		});

		long duration = (System.nanoTime() - startTime);
		System.err.println("Array stream of array : " + (duration));
		startTime = System.nanoTime();

		Arrays.asList(new int[][] {
				{ 0, 10 }, { 0, 11 }, { 0, 14 }, { 9, 2 }, { 9, 3 }, { 10, 2 }, { 10, 3 }, { 0, 9 }, { 1, 9 }, { 2, 9 }, { 0, 10 },
				{ 1, 10 }, { 2, 10 }, { 6, 11 }, { 6, 12 }, { 7, 11 }, { 7, 12 }, { 1, 13 }, { 1, 14 }, { 2, 13 }, { 2, 14 }
		}).forEach(s -> {
		});

		duration = (System.nanoTime() - startTime);
		System.err.println("list foreach : " + (duration));
		startTime = System.nanoTime();

		new HashSet<>(Arrays.asList(new int[][] {
				{ 0, 10 }, { 0, 11 }, { 0, 14 }, { 9, 2 }, { 9, 3 }, { 10, 2 }, { 10, 3 }, { 0, 9 }, { 1, 9 }, { 2, 9 }, { 0, 10 },
				{ 1, 10 }, { 2, 10 }, { 6, 11 }, { 6, 12 }, { 7, 11 }, { 7, 12 }, { 1, 13 }, { 1, 14 }, { 2, 13 }, { 2, 14 }
		})).stream().forEach(s -> {
		});

		duration = (System.nanoTime() - startTime);
		System.err.println("stream of hashset of array : " + (duration));
		startTime = System.nanoTime();

		Stream.of(new int[] { 0, 10 }, new int[] { 0, 11 }, new int[] { 0, 14 }, new int[] { 9, 2 }, new int[] { 9, 3 },
				new int[] { 10, 2 }, new int[] { 10, 3 }, new int[] { 0, 9 }, new int[] { 1, 9 }, new int[] { 2, 9 }, new int[] { 0, 10 },
				new int[] { 1, 10 }, new int[] { 2, 10 }, new int[] { 6, 11 }, new int[] { 6, 12 }, new int[] { 7, 11 },
				new int[] { 7, 12 }, new int[] { 1, 13 }, new int[] { 1, 14 }, new int[] { 2, 13 }, new int[] { 2, 14 }).forEach(s -> {
		});

		duration = (System.nanoTime() - startTime);
		System.err.println("Direct stream : " + (duration));

	}


	@Test
	public void testFindConcat() {
		long startTime = System.nanoTime();

		Set<String> cases = new HashSet<>();
		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 1000; j++) {
				cases.add(i + "-" + j);
			}
		}

		long duration = (System.nanoTime() - startTime);
		System.err.println("Time to exexute Srring : " + (duration / 1000000));
		startTime = System.nanoTime();

		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 1000; j++) {
				if (cases.contains(i + "-" + j)) {

				}
			}
		}

		duration = (System.nanoTime() - startTime);
		System.err.println("Time to get  : " + (duration / 1000000));
		startTime = System.nanoTime();

		Set<Integer> casesInt = new HashSet<>();
		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 1000; j++) {
				casesInt.add(i + j * 1000);
			}
		}

		duration = (System.nanoTime() - startTime);
		System.err.println("Time to exexute Int : " + (duration / 1000000));
		startTime = System.nanoTime();

		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 1000; j++) {
				if (casesInt.contains(i + j * 1000)) {

				}
			}
		}

		duration = (System.nanoTime() - startTime);
		System.err.println("Time to get int : " + (duration / 1000000));

	}


	@Test
	public void testloop() {
		long startTime = System.nanoTime();

		int count = 0;
		for (int i = 0; i < 4000; i++) {
			for (int j = 0; j < 4000; j++) {
				count += i + j * 4000;
			}
		}

		long duration = (System.nanoTime() - startTime);
		System.err.println("Two llops : " + (duration));
		startTime = System.nanoTime();
		int count2 = 0;
		for (int i = 0; i < 4000 * 4000; i++) {
			count2 += i;
		}

		duration = (System.nanoTime() - startTime);
		System.err.println("One loo : " + (duration));
		System.err.println("Finnished " + count + " " + count2);
	}

}
