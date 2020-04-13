package com.codingame.game;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class PathFinding {

/*
	public static Player.CELL[][] cells = {
			new Player.CELL[15], new Player.CELL[15], new Player.CELL[15], new Player.CELL[15], new Player.CELL[15], new Player.CELL[15],
			new Player.CELL[15], new Player.CELL[15], new Player.CELL[15], new Player.CELL[15], new Player.CELL[15], new Player.CELL[15],
			new Player.CELL[15], new Player.CELL[15], new Player.CELL[15],
	};

	@Before
	public void prepareMap() {
		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 15; j++) {
				cells[j][i] = Player.CELL.VIRGIN_WATER;
			}
		}
		Arrays.stream(new int[][] {
				{ 0, 10 }, { 0, 11 }, { 0, 14 }, { 9, 2 }, { 9, 3 }, { 10, 2 }, { 10, 3 }, { 0, 9 }, { 1, 9 }, { 2, 9 }, { 0, 10 },
				{ 1, 10 }, { 2, 10 }, { 6, 11 }, { 6, 12 }, { 7, 11 }, { 7, 12 }, { 1, 13 }, { 1, 14 }, { 2, 13 }, { 2, 14 }
		}).forEach(coord -> cells[coord[0]][coord[1]] = Player.CELL.ISLAND);
	}


	@Test
	public void testFindRandom() {
		Player.PathFinder pathFinding = new Player.PathFinder(cells, 169);
		long startTime = System.nanoTime();
		pathFinding.findBestnextSteps(0, 0);

		long duration = (System.nanoTime() - startTime);
		System.err.println("Time to exexute : " + (duration / 1000000));
	}*/

}
