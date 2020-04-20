import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

	private static boolean noMoreCells;


	public enum CELL {
		VIRGIN_WATER,
		VISITED_WATER,
		ISLAND;
	}


	private static final boolean logEnabled = false;

	//Start Parmaetersd
	public static int width = 15;
	public static int height = 15;
	public static int myId;

	public static int myLifesReminder = 6;

	public static CELL[][] cells = {
			new CELL[15], new CELL[15], new CELL[15], new CELL[15], new CELL[15], new CELL[15], new CELL[15], new CELL[15], new CELL[15],
			new CELL[15], new CELL[15], new CELL[15], new CELL[15], new CELL[15], new CELL[15],
	};
	public static Renseignements myRenseignements;
	public static Renseignements oppRenseignements;
	public static List<Character> currentRoadmap = new ArrayList<>();

	//Path Finding

	public static int oppLifesReminder = 6;

	public static Integer sonarSector = null;

	public static int torpedoCooldown = 3;
	public static int sonarCooldown = 4;
	public static int silenceCooldown = 6;
	public static int mineCooldown = 3;
	public static int myLife = 6;
	public static int myX;
	public static int myY;
	private static final int maxResponseTime = 37;

	private static int oppMinen = 0;
	private static Set<Integer> localizedOppMinen = new HashSet<>();

	public static Set<Integer> myMines = new HashSet<>();

	public static Integer torpedoAt = null;
	public static boolean willUseTorpedo = false;

	private static ExecutorService executor = Executors.newFixedThreadPool(4);


	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		width = in.nextInt();
		height = in.nextInt();
		myId = in.nextInt();
		if (in.hasNextLine()) {
			in.nextLine();
		}
		for (int i = 0; i < height; i++) {
			char[] line = in.nextLine().toCharArray();
			for (int j = 0; j < line.length; j++) {
				cells[j][i] = line[j] == '.' ? CELL.VIRGIN_WATER : CELL.ISLAND;
			}
		}
		myRenseignements = new Renseignements("Me");
		oppRenseignements = new Renseignements("Opp");
		logIslands();

		// Write an action using System.out.println()
		// To debug: System.err.println("Debug messages...");

		String start = "";
		int startX = 0;
		int startY = 0;
		while (start.isEmpty()) {
			startX = ((int) (Math.random() * 15));
			startY = ((int) (Math.random() * 15));
			if (cells[startX][startY] == CELL.VIRGIN_WATER && startX > 2 && startX < 13 && startY > 2 && startY < 13 && Stream.of(
					new int[] { startX + 1, startY }, new int[] { startX - 1, startY }, new int[] { startX, startY - 1 },
					new int[] { startX, startY + 1 }).allMatch(coord -> isValidWater(coord[0], coord[1])) && new PathFinder(cells,
					20).findBestnextSteps(startX, startY)) {
				start = startX + " " + startY;

			}
		}
		cells[startX][startY] = CELL.VISITED_WATER;
		PathFinder currentPathFinder = new PathFinder(cells, 200);
		currentRoadmap = currentPathFinder.findBestnextStepsWithLimitedTime(startX, startY, 800);
		System.out.println(start);

		// game loop
		while (true) {
			willUseTorpedo = false;
			myX = in.nextInt();
			myY = in.nextInt();
			myLife = in.nextInt();
			int oppLife = in.nextInt();
			torpedoCooldown = in.nextInt();
			sonarCooldown = in.nextInt();
			silenceCooldown = in.nextInt();
			mineCooldown = in.nextInt();
			String sonarResult = in.next();
			if (in.hasNextLine()) {
				in.nextLine();
			}

			String opponentOrders = in.nextLine();
			long startTime = System.nanoTime();

			if (sonarSector != null && !sonarResult.isEmpty()) {
				if ("Y".equals(sonarResult)) {
					oppRenseignements.updateSonar(sonarSector);
				}
				else {
					oppRenseignements.updateSonarNotInSector(sonarSector);
				}
			}
			sonarSector = null;

			boolean oppAutoKillPossibility = opponentOrders.contains("TRIGGER") || opponentOrders.contains("TORPEDO");

			if (torpedoAt != null) {
				if (oppLife == oppLifesReminder) {
					oppRenseignements.setAbsenceVerifiedAtAndNear((torpedoAt % 15), Math.floorDiv(torpedoAt, 15));
				}
				else if (!oppAutoKillPossibility && oppLifesReminder - oppLife == 1) {
					oppRenseignements.setAbsenceVerifiedAtButNear((torpedoAt % 15), Math.floorDiv(torpedoAt, 15));
				}
				else if (!oppAutoKillPossibility && oppLifesReminder - oppLife == 2) {
					oppRenseignements.isAt((torpedoAt % 15), Math.floorDiv(torpedoAt, 15));
				}
			}

			torpedoAt = null;
			treatOppOrder(opponentOrders, oppRenseignements.getPossiblePresences());
			myLifesReminder = myLife;
			System.err.println("Opponent mines : " + localizedOppMinen.stream().map(String::valueOf).collect(Collectors.joining(",")));

			oppRenseignements.logAbsenceVerified();
			oppLifesReminder = oppLife;

			List<String> actions = new ArrayList<>();
			// Calcul de l'itinéraire

			cells[myX][myY] = CELL.VISITED_WATER;
/*
			int[] barycentre = oppRenseignements.isCloseTo(4);
			if (barycentre != null) {
				System.err.println("Point to attack : " + barycentre[0] + "-" + barycentre[1]);

			}
			if (barycentre != null) {
				Future<Boolean> futureRoadMap = executor.submit(() -> {
					if (Math.abs(barycentre[0] - myX) + Math.abs(barycentre[1] - myY) > 4) {
						PathFinder newPath = new PathFinder(cells, 100);
						List<Character> pathToAttack = newPath.findPathToPoint(myX, myY, barycentre[0], barycentre[1]);
						if (pathToAttack != null) {
							currentRoadmap = pathToAttack;
						}
					}
					return true;
				});
				int restTime = Math.min(30, (int) (maxResponseTime - (System.nanoTime() - startTime) / 1000000));
				System.err.println("Find path to attack (" + restTime + ")");

				try {
					futureRoadMap.get(restTime, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException | ExecutionException | TimeoutException e) {
					System.err.println("No time to find path to attack");
				}

			}*/

			List<Integer> possiblesPresences = oppRenseignements.getPossiblePresences();
			boolean canSonar = sonarCooldown == 0;
			boolean canDepositMine = mineCooldown == 0;

			if (currentRoadmap.size() < 15 && !noMoreCells || currentRoadmap.size() < 2
					|| !localizedOppMinen.isEmpty() && checkIfIsMineOnPath()) {
				if (noMoreCells) {
					actions.add("SURFACE");
					myRenseignements.updateSonar(sector(myX, myY));
					resetCells(myX, myY);
					noMoreCells = false;
				}
				int restTime = (int) (maxResponseTime - (System.nanoTime() - startTime) / 1000000);

				System.err.println("Need find another Path (" + restTime + ")");

				PathFinder pathFinder = new PathFinder(cells, 200);
				Future<Boolean> futureRoadMap = executor.submit(() -> {
					System.err.println("Begin roadmap search");
					pathFinder.findBestnextSteps(myX, myY);
					return true;
				});

				try {
					futureRoadMap.get(restTime, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException | ExecutionException | TimeoutException e) {
					pathFinder.log();

				}
				if (!pathFinder.bestSteps.isEmpty()) {
					currentRoadmap = pathFinder.bestSteps;
				}
				noMoreCells = currentRoadmap.size() < 7;
				System.err.println("New roadmap with size of: " + currentRoadmap.size());
			}
			int possiblePresenceSize = possiblesPresences.size();
			// Attack strategy
			if (possiblePresenceSize < 10) {
				int restTime = Math.min(37, (int) (maxResponseTime - (System.nanoTime() - startTime) / 1000000));
				if (restTime > 0) {
					System.err.println("Begin attack strategy search (" + restTime + ")");

					Future<Boolean> attackStrategyFuture = executor.submit(() -> {

						DuetInt choosenTargetTorpedo = new DuetInt(0, 0);
						DuetInt choosenTargetMine = new DuetInt(0, 0);

						if (torpedoCooldown == 0 && possiblePresenceSize < 5) {
							System.err.println("Search Torpedo tazrgets");
							int[] possibleTargets = possibleTargets(myX, myY, myX, myY, new HashSet<>(), new HashSet<>(),
									possiblesPresences, new int[15 * 15]);
							for (int i = 0; i < possibleTargets.length; i++) {
								if (possibleTargets[i] > 0) {
									System.err.print(i + ":" + possibleTargets[i] + ",");
								}
							}

							int maxIndex = maxAt(possibleTargets);
							if (logEnabled) {
								System.err.println("Possible targets = " + maxIndex + "(" + possibleTargets[maxIndex] + ")");
							}
							choosenTargetTorpedo = new DuetInt(maxIndex, possibleTargets[maxIndex]);
							System.err.println("torpedo target found");

						}
						if (!myMines.isEmpty()) {
							System.err.println("Search Minen tazrgets");
							int[] possibleTargets = possibleMines(possiblesPresences, new int[15 * 15]);

							int maxIndex = maxAt(possibleTargets);
							if (logEnabled) {
								System.err.println("Possible Mines = " + maxIndex + "(" + possibleTargets[maxIndex] + ")");
							}
							choosenTargetMine = new DuetInt(maxIndex, possibleTargets[maxIndex]);
							System.err.println("Mine target found");
						}
						System.err.println("Choose mine or torpedo");
						String commande = null;
						int choosenTarget = -1;
						if (choosenTargetTorpedo.getValue() >= choosenTargetMine.getValue() && choosenTargetTorpedo.getValue() > 0
								&& possiblePresenceSize < 5) {
							commande = "TORPEDO";
							choosenTarget = choosenTargetTorpedo.getKey();
							System.err.println("Attack strategy Torpedo : " + choosenTargetTorpedo);

						}
						else if (choosenTargetMine.getValue() > 0 && (possiblePresenceSize < 10)) {
							commande = "TRIGGER";
							choosenTarget = choosenTargetMine.getKey();
							System.err.println("Attack strategy Mine : " + choosenTargetMine);
						}

						if (commande != null) {
							int presenceX = choosenTarget % 15;
							int presenceY = Math.floorDiv(choosenTarget, 15);
							if (commande.equals("TRIGGER")) {
								myMines.remove(choosenTarget);
							}
							else {
								willUseTorpedo = true;
								myRenseignements.sendTorpedoAt(presenceX, presenceY);
							}
							actions.add(commande + " " + presenceX + " " + presenceY);
							torpedoAt = presenceX + presenceY * 15;
						}
						else {
							System.err.println("Nothing to attack");
						}
						return true;
					});

					try {
						attackStrategyFuture.get(restTime, TimeUnit.MILLISECONDS);
					}
					catch (InterruptedException | ExecutionException | TimeoutException e) {
						System.err.println("Not enough time to compute attack strategy");
					}
				}
				else {
					System.err.println("No time to compute attack");
				}

			}
			int restTime = Math.min(20, (int) (maxResponseTime - (System.nanoTime() - startTime) / 1000000));
			if (canSonar && restTime > 0) {
				System.err.println("Begin sector to sonar search (" + restTime + ")");
				Future<?> sonarFuture = executor.submit(() -> {
					sonarSector = oppRenseignements.chooseSectorToScan();
					if (sonarSector != null) {
						actions.add("SONAR " + sonarSector);
						System.err.println("SectorToSonar found : " + sonarSector);
					}
				});

				try {
					sonarFuture.get(restTime, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException | ExecutionException | TimeoutException e) {
					System.err.println("Not enough time to compute sonar");
				}
			}
			restTime = Math.min(20, (int) (maxResponseTime - (System.nanoTime() - startTime) / 1000000));

			if (canDepositMine && restTime > 0) {
				System.err.println("Begin direction de deposit search (" + restTime + ")");

				Future<?> mineFuture = executor.submit(() -> directionToDesposeMine().ifPresent(character -> {
					switch (character) {
						case 'N':
							myMines.add(myX + (myY - 1) * 15);
							break;
						case 'S':
							myMines.add(myX + (myY + 1) * 15);
							break;
						case 'E':
							myMines.add((myX + 1) + myY * 15);
							break;
						case 'W':
							myMines.add((myX - 1) + myY * 15);
							break;
					}
					actions.add("MINE " + character);
					System.err.println("Direction to mine found");

				}));

				try {
					mineFuture.get(restTime, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException | ExecutionException | TimeoutException e) {
					System.err.println("Not enough time to compute sonar");
				}
			}
			restTime = maxResponseTime - (int) (((System.nanoTime() - startTime) / 1000000));
			if (silenceCooldown == 0 && myRenseignements.getPossiblePresences().size() < 4 && currentRoadmap.size() > 10 && restTime > 0) {
				System.err.println("Check silence (" + restTime + ")");

				/*Future<?> addSilence = executor.submit(() -> {
					int i = 0;
					char direction = currentRoadmap.get(0);
					while (i < 4 && !currentRoadmap.isEmpty() && currentRoadmap.get(0).equals(direction)) {
						currentRoadmap.remove(0);
						i++;
						switch (direction) {
							case 'N':
								cells[myX][myY - i] = CELL.VISITED_WATER;
								break;
							case 'S':
								cells[myX][myY + i] = CELL.VISITED_WATER;
								break;
							case 'E':
								cells[myX + i][myY] = CELL.VISITED_WATER;
								break;
							case 'W':
								cells[myX - i][myY] = CELL.VISITED_WATER;
								break;
						}
					}
					actions.add("SILENCE " + direction + " " + (i));
					myRenseignements.updateSilence();
				});

				try {
					addSilence.get(restTime, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException | ExecutionException | TimeoutException e) {
					System.err.println("Not enough time to add silence");
				}*/

				actions.add("SILENCE N 0");
				myRenseignements.updateSilence();
			}

			String nextCharge = getNextCharge(willUseTorpedo);
			actions.add("MOVE " + currentRoadmap.get(0) + " " + nextCharge);
			restTime = maxResponseTime - (int) (((System.nanoTime() - startTime) / 1000000));

			//Try Torpedo with new position
			if (torpedoCooldown == 1 && nextCharge.equals("TORPEDO") && possiblesPresences.size() < 10 && restTime > 0) {
				System.err.println("Calculate Torpedo after move (" + restTime + ")");
				Future<?> addSilence = executor.submit(() -> {

					int nextX = myX;
					int nextY = myY;

					switch (currentRoadmap.get(0)) {
						case 'N':
							nextY--;
							break;
						case 'S':
							nextY++;
							break;
						case 'E':
							nextX++;
							break;
						case 'W':
							nextX--;
							break;
					}
					int[] possibleTargets = possibleTargets(nextX, nextY, nextX, nextY, new HashSet<>(), new HashSet<>(),
							possiblesPresences, new int[15 * 15]);

					int maxIndex = maxAt(possibleTargets);
					DuetInt choosenTargetTorpedo = new DuetInt(maxIndex, possibleTargets[maxIndex]);

					if (logEnabled) {
						System.err.println("Possible targets = " + choosenTargetTorpedo);
					}
					System.err.println("torpedo target found");

					if (choosenTargetTorpedo.value > 0) {
						int coors = choosenTargetTorpedo.key;
						int presenceX = coors % 15;
						int presenceY = Math.floorDiv(coors, 15);
						actions.add("TORPEDO " + presenceX + " " + presenceY);

					}
				});
				try {
					addSilence.get(restTime, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException | ExecutionException | TimeoutException e) {
					System.err.println("Not enough time to compute sonar");
				}
			}
			System.out.println(String.join(" | ", actions));
			myRenseignements.update(currentRoadmap.get(0));
			currentRoadmap.remove(0);
		}
	}


	private static boolean checkIfIsMineOnPath() {
		int x = myX;
		int y = myY;
		int i = 0;
		while (i < 20 && i < currentRoadmap.size() - 2) {
			switch (currentRoadmap.get(i)) {
				case 'N':
					y--;
					break;
				case 'S':
					y++;
					break;
				case 'E':
					x++;
					break;
				case 'W':
					x--;
					break;
			}
			if (checkIfIsMineisAround(x + y * 15)) {
				System.err.println("Mine is found at " + i);
				return true;
			}
			i++;
		}
		return false;
	}


	private static boolean checkIfIsMineisAround(int position) {
		return localizedOppMinen.contains(position) || localizedOppMinen.contains(position - 15) || localizedOppMinen.contains(position - 1)
				|| localizedOppMinen.contains(position + 1) || localizedOppMinen.contains(position + 15);
	}


	private static class DuetInt {

		private int key;
		private int value;


		public DuetInt(int key, int value) {
			this.key = key;
			this.value = value;
		}


		public int getKey() {
			return key;
		}


		public int getValue() {
			return value;
		}


		@Override
		public String toString() {
			return key + " : " + value;
		}
	}


	public static String getNextCharge(boolean willUseTorpedo) {
		if (torpedoCooldown > 0 || willUseTorpedo) {
			return "TORPEDO";
		}
		else if (sonarCooldown > 0) {
			return "SONAR";
		}
		else if (silenceCooldown > 0) {
			return "SILENCE";
		}
		else if (mineCooldown > 0) {
			return "MINE";
		}
		return "";
	}


	public static Optional<Character> directionToDesposeMine() {
		Map<Character, int[]> possibilities = new HashMap<>();
		possibilities.put('N', new int[] { myX, myY - 1 });
		possibilities.put('S', new int[] { myX, myY + 1 });
		possibilities.put('E', new int[] { myX + 1, myY });
		possibilities.put('W', new int[] { myX - 1, myY });

		return possibilities.entrySet()
				.stream()
				.filter(possibility -> isValidWater(possibility.getValue()[0], possibility.getValue()[1]) && !myMines.contains(
						possibility.getValue()[0] + possibility.getValue()[1] * 15))
				.map(possibility -> {
					int[] value = possibility.getValue();
					int x = value[0];
					int y = value[1];
					if (Stream.of(new int[] { x + 1, y }, new int[] { x - 1, y }, new int[] { x, y - 1 }, new int[] { x, y + 1 },
							new int[] { x + 1, y - 1 }, new int[] { x - 1, y - 1 }, new int[] { x - 1, y - 1 }, new int[] { x + 1, y + 1 })
							.allMatch(coord -> isValidWater(coord[0], coord[1]) && !myMines.contains(coord[0] + coord[1] * 15))) {
						return possibility.getKey();
					}
					return null;
				})
				.filter(Objects::nonNull)
				.findFirst();
	}


	private static int maxAt(int[] array) {

		int maxAt = 0;

		for (int i = 0; i < array.length; i++) {
			maxAt = array[i] > array[maxAt] ? i : maxAt;
		}
		return maxAt;

	}


	public static int sector(int x, int y) {
		return Math.floorDiv(x, 5) + 1 + (Math.floorDiv(y, 5) + 1) * 3;
	}


	public static int[] possibleTargets(int fromX, int fromY, int x, int y, Set<Integer> cellsCrossed, Set<Integer> counted,
			List<Integer> possiblePresence, int[] result) {
		cellsCrossed.add(x + y * 15);
		if (!counted.contains(x + y * 15) && (Math.abs(x - fromX) > 1 || Math.abs(y - fromY) > 1)) {
			counted.add(x + y * 15);
			if (possiblePresence.contains(x + y * 15)) {
				result[x + y * 15] += 2;
				System.err.println("Torpedo : " + x + "-" + y);
			}
			Stream.of(new int[] { x + 1, y }, new int[] { x - 1, y }, new int[] { x, y - 1 }, new int[] { x, y + 1 },
					new int[] { x + 1, y - 1 }, new int[] { x - 1, y - 1 }, new int[] { x - 1, y + 1 }, new int[] { x + 1, y + 1 })
					.map(coor -> coor[0] + coor[1] * 15)
					.filter(possiblePresence::contains)
					.forEach(coord -> result[x + y * 15] += 1);

		}

		if (cellsCrossed.size() < 5) {
			Stream.of(new int[] { x + 1, y }, new int[] { x - 1, y }, new int[] { x, y - 1 }, new int[] { x, y + 1 })
					.filter(coord -> !cellsCrossed.contains(coord[0] + coord[1] * 15))
					.filter(coord -> isValidWater(coord[0], coord[1]))
					.forEach(coord -> possibleTargets(fromX, fromY, coord[0], coord[1], cellsCrossed, counted, possiblePresence, result));
		}

		cellsCrossed.remove(x + y * 15);
		return result;
	}


	public static int[] possibleMines(List<Integer> possiblePresence, int[] result) {
		for (int mine : myMines) {
			int x = mine % 15;
			int y = Math.floorDiv(mine, 15);
			if ((Math.abs(x - myX) > 1 || Math.abs(y - myY) > 1)) {
				if (possiblePresence.contains(x + y * 15)) {
					result[x + y * 15] += 2;
				}
				Stream.of(new int[] { x + 1, y }, new int[] { x - 1, y }, new int[] { x, y - 1 }, new int[] { x, y + 1 },
						new int[] { x + 1, y - 1 }, new int[] { x - 1, y - 1 }, new int[] { x - 1, y - 1 }, new int[] { x + 1, y + 1 })
						.map(coor -> coor[0] + coor[1] * 15)
						.filter(possiblePresence::contains)
						.forEach(coord -> result[x + y * 15] += 1);
			}
		}

		return result;
	}


	public static void treatOppOrder(String order, List<Integer> possiblePresences) {
		String[] orders = order.split("\\|");
		for (String s : orders) {
			if (s.contains("MOVE")) {
				char direction = s.replace(" ", "").charAt(4);
				oppRenseignements.update(direction);
			}
			else if (s.contains("TORPEDO")) {
				if (myLife == myLifesReminder) {
					myRenseignements.setAbsenceVerifiedAtAndNear(myX, myY);
				}
				else if (myLifesReminder - myLife == 1) {
					myRenseignements.setAbsenceVerifiedAtButNear(myX, myY);
				}
				else if (myLifesReminder - myLife == 2) {
					myRenseignements.isAt(myX, myY);
				}
				String[] items = s.split(" ");
				int torpedoX = Integer.parseInt(items[1]);
				int torpedoY = Integer.parseInt(items[2]);
				oppRenseignements.sendTorpedoAt(torpedoX, torpedoY);
			}
			else if (s.contains("SILENCE")) {
				oppRenseignements.updateSilence();
				System.err.println("Silence of opp");
			}
			else if (s.contains("SONAR")) {
				String[] items = s.split(" ");
				int sector = Integer.parseInt(items[1]);
				System.err.println("Sonar of opp");
				if (sector == sector(myX, myY)) {
					myRenseignements.updateSonar(sector);
				}
				else {
					myRenseignements.updateSonarNotInSector(sector);
				}
			}
			else if (s.contains("SURFACE")) {
				String[] items = s.split(" ");
				int sector = Integer.parseInt(items[1]);
				oppRenseignements.surface();
				oppRenseignements.updateSonar(sector);
				System.err.println("Opp surfaced in sector : " + sector);
			}
			else if (s.contains("MINE")) {
				System.err.println("Mine of opp");
				if (possiblePresences.size() < 2) {
					possiblePresences.forEach(presence -> {
						int x = presence % 15;
						int y = Math.floorDiv(presence, 15);

						Stream.of(new int[] { x + 1, y }, new int[] { x - 1, y }, new int[] { x, y - 1 }, new int[] { x, y + 1 })
								.filter(coord -> isValidWater(coord[0], coord[1]))
								.forEach(coord -> localizedOppMinen.add(coord[0] * 15 + coord[1]));
					});
				}
				oppMinen++;
			}
			else if (s.contains("TRIGGER")) {
				System.err.println("Trigger of opp");
				oppMinen--;
			}
		}
	}


	private static void resetCells(int x, int y) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				cells[j][i] = cells[j][i] == CELL.ISLAND ? CELL.ISLAND : CELL.VIRGIN_WATER;
			}
		}
		cells[x][y] = CELL.VISITED_WATER;
	}


	public static class PathFinder {

		private List<Character> itineraire;
		private List<Character> itineraireAttack;
		private static final int numberOfTriesLimit = 8000;
		private static final int numberOfTriesLimitForAttack = 1000;
		private int limit;
		public Set<Integer> cellsVisited;
		private List<Character> bestSteps;

		private long numberOfTries = 0;
		private long numberOfTriesAttack = 0;

		private int xAttackArrival;
		private int yAttackArrival;
		Set<Character> directions = new HashSet<>(Arrays.asList('N', 'S', 'W', 'E'));
		private CELL[][] map;
		private boolean ignoreMines = false;


		public PathFinder(CELL[][] map, int limit) {
			this.limit = limit;
			this.map = map;

			cellsVisited = new HashSet<>();
			itineraireAttack = new ArrayList<>();
			itineraire = new ArrayList<>();
			bestSteps = new ArrayList<>();
		}


		public List<Character> findBestnextStepsWithLimitedTime(int currentX, int currentY, int time) {

			Future<Boolean> future = executor.submit(() -> {
				findBestnextSteps(currentX, currentY);

				return true;
			});

			try {
				future.get(time, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException | ExecutionException | TimeoutException e) {
				System.err.println("Finnished computing steps " + bestSteps.size() + "/" + limit + " numberOfTries = " + numberOfTries + "/"
						+ numberOfTriesLimit);
			}
			return bestSteps;
		}


		public List<Character> findPathToPoint(int currentX, int currentY, int toX, int toY) {
			if (goNear(currentX, currentY, toX, toY)) {
				findBestnextSteps(xAttackArrival, yAttackArrival);
				if (bestSteps.size() > 50) {
					List<Character> pathTpAttack = Stream.of(itineraireAttack, bestSteps)
							.flatMap(Collection::stream)
							.collect(Collectors.toList());
					System.err.println(itineraireAttack.stream().map(String::valueOf).collect(Collectors.joining(",")));

					System.err.println(pathTpAttack.stream().map(String::valueOf).collect(Collectors.joining(",")));
					return pathTpAttack;
				}
			}
			return null;
		}


		public void log() {
			System.err.println(
					"LOG : Finnished computing steps " + bestSteps.size() + "/" + limit + " numberOfTries = " + numberOfTries + "/"
							+ numberOfTriesLimit);
		}


		public boolean findBestnextSteps(int currentX, int currentY) {

			numberOfTries++;
			if (itineraire.size() >= limit) {
				if (itineraire.size() > bestSteps.size()) {
					bestSteps = new ArrayList<>(itineraire);
				}
				System.err.println("Finnished computing steps " + bestSteps.size() + "/" + limit + " numberOfTries = " + numberOfTries + "/"
						+ numberOfTriesLimit);
				return true;
			}
			for (char direction : directions) {
				int nextX = currentX, nextY = currentY;
				switch (direction) {
					case 'N':
						nextY = currentY - 1;
						break;
					case 'E':
						nextX = currentX + 1;
						break;
					case 'S':
						nextY = currentY + 1;
						break;
					case 'W':
						nextX = currentX - 1;
						break;
				}
				if (isValidCell(nextX, nextY)) {
					itineraire.add(direction);
					cellsVisited.add(nextX + nextY * 15);

					if (findBestnextSteps(nextX, nextY)) {
						return true;
					}
					itineraire.remove(itineraire.size() - 1);
					cellsVisited.remove(nextX + nextY * 15);
				}
			}
			if (itineraire.size() > bestSteps.size()) {
				bestSteps = new ArrayList<>(itineraire);
			}
			return false;
		}


		private boolean goNear(int x, int y, int xTo, int yTo) {
			Map<Character, Integer> direction = new HashMap<>();
			if (isValidCell(x, y - 1)) {
				direction.put('N', xTo - x);
			}
			if (isValidCell(x, y + 1)) {
				direction.put('S', x - xTo);
			}
			if (isValidCell(x + 1, y)) {
				direction.put('E', yTo - y);
			}
			if (isValidCell(x - 1, y)) {
				direction.put('W', y - yTo);
			}

			List<Character> directions = direction.entrySet()
					.stream()
					.sorted(Comparator.comparingInt(Map.Entry::getValue))
					.map(Map.Entry::getKey)
					.collect(Collectors.toList());

			cellsVisited.add(x + y * 15);
			if (Math.abs(x - xTo) < 3 && Math.abs(y - yTo) < 3) {
				xAttackArrival = x;
				yAttackArrival = y;
				return true;
			}

			if (itineraireAttack.size() > 10 || numberOfTriesAttack > numberOfTriesLimitForAttack) {
				return false;
			}

			for (Character dir : directions) {
				itineraireAttack.add(dir);
				numberOfTriesAttack++;
				if (dir.equals('N') && goNear(x, y - 1, xTo, yTo)) {
					return true;
				}

				if (dir.equals('S') && goNear(x, y + 1, xTo, yTo)) {
					return true;
				}

				if (dir.equals('E') && goNear(x + 1, y, xTo, yTo)) {
					return true;
				}

				if (dir.equals('W') && goNear(x - 1, y, xTo, yTo)) {
					return true;
				}
				itineraireAttack.remove(itineraireAttack.size() - 1);
			}
			cellsVisited.remove(x + y * 15);
			return false;
		}


		private boolean isValidCell(int x, int y) {
			boolean isOnTheMap = x >= 0 && x < width && y >= 0 && y < height;
			return isOnTheMap && map[x][y] == CELL.VIRGIN_WATER && !cellsVisited.contains(x + y * 15) && !checkIfIsMineisAround(x + y * 15);
		}

	}


	public static void logIslands() {

		System.err.println("Islands");
		for (int i = 0; i < height; i++) {
			StringBuilder line = new StringBuilder();
			for (int j = 0; j < width; j++) {
				line.append(cells[j][i] == CELL.ISLAND ? "-" : "X");
			}
			System.err.println(line);
		}
	}


	public static void logVisitedCells() {

		System.err.println("Visited Cells");
		for (int i = 0; i < height; i++) {
			StringBuilder line = new StringBuilder();
			for (int j = 0; j < width; j++) {
				line.append(cells[j][i] == CELL.VISITED_WATER ? "X" : "-");
			}
			System.err.println(line);
		}
	}


	private static boolean isValidWater(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height && cells[x][y] != CELL.ISLAND;
	}


	static class Renseignements {

		private String name;


		public Renseignements(String name) {
			this.name = name;
			updateAbsenceOnIsland();
			logAbsenceVerified();
		}


		public boolean[][] absenceExpected = {
				new boolean[15], new boolean[15], new boolean[15], new boolean[15], new boolean[15], new boolean[15], new boolean[15],
				new boolean[15], new boolean[15], new boolean[15], new boolean[15], new boolean[15], new boolean[15], new boolean[15],
				new boolean[15],
		};

		private List<Character> lastDirections = new ArrayList<>();


		private void reset() {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					absenceExpected[x][y] = false;
				}
			}
			updateAbsenceOnIsland();
			logAbsenceVerified();

		}


		public void surface() {
			lastDirections = new ArrayList<>();
		}


		public int[] isCloseTo(int trigger) {
			int minX = 14;
			int maxX = 0;
			int minY = 14;
			int maxY = 0;

			int sumX = 0;
			int sumY = 0;
			int count = 0;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (!absenceExpected[x][y]) {

						count++;
						sumX += x;
						sumY += y;
						if (x < minX) {
							minX = x;
						}
						if (x > maxX) {
							maxX = x;
						}
						if (y < minY) {
							minY = y;
						}
						if (y > maxY) {
							maxY = y;
						}
					}
				}
			}
			return count > 0 && (maxX - minX < trigger && maxY - minY < trigger) ?
					new int[] { Math.floorDiv(sumX, count), Math.floorDiv(sumY, count) } :
					null;

		}


		public void setAbsenceVerifiedAtAndNear(int x, int y) {
			System.err.println(name + " is not at " + x + "-" + y + " and near");
			Stream.of(new int[] { x, y }, new int[] { x + 1, y }, new int[] { x - 1, y }, new int[] { x, y - 1 }, new int[] { x, y + 1 },
					new int[] { x + 1, y - 1 }, new int[] { x - 1, y - 1 }, new int[] { x - 1, y - 1 }, new int[] { x + 1, y + 1 })
					.filter(coord -> isValidWater(coord[0], coord[1]))
					.forEach(coord -> absenceExpected[coord[0]][coord[1]] = true);
		}


		public void setAbsenceVerifiedAtButNear(int xPos, int yPos) {
			System.err.println(name + " IsNear " + xPos + "-" + yPos);
			Set<Integer> around = Stream.of(new int[] { xPos + 1, yPos }, new int[] { xPos - 1, yPos }, new int[] { xPos, yPos - 1 },
					new int[] { xPos, yPos + 1 }, new int[] { xPos + 1, yPos - 1 }, new int[] { xPos - 1, yPos - 1 },
					new int[] { xPos - 1, yPos + 1 }, new int[] { xPos + 1, yPos + 1 })
					.map(integer -> integer[0] + integer[1] * 15)
					.collect(Collectors.toSet());
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (!around.contains(x + y * 15)) {
						absenceExpected[x][y] = true;
					}
				}
			}
		}


		public void isAt(int xPos, int yPos) {
			System.err.println(name + " is at " + xPos + "-" + yPos);

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (xPos == x && yPos == y) {
						absenceExpected[x][y] = false;
					}
					else {
						absenceExpected[x][y] = true;
					}
				}
			}
		}


		public double minDistanceOfMe(int x, int y) {
			double distance = -1;
			for (int ly = 0; ly < height; ly++) {
				for (int lx = 0; lx < width; lx++) {
					if (!absenceExpected[lx][ly]) {
						double distancetmp = Math.sqrt(Math.pow(lx - x, 2) + Math.pow(ly - y, 2));
						if (distance == -1 || distancetmp < distance) {
							distance = distancetmp;
						}
					}

				}
			}
			return distance;
		}


		public List<Integer> getPossiblePresences() {
			int[] presences = new int[255];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (!absenceExpected[x][y]) {
						presences[y * 15 + x] += 2;
						if (isValidWater(x + 1, y) && !absenceExpected[x + 1][y]) {
							presences[y * 15 + x + 1] += 1;
						}
						if (isValidWater(x + 1, y + 1) && !absenceExpected[x + 1][y + 1]) {
							presences[(y + 1) * 15 + x + 1] += 1;
						}
						if (isValidWater(x, y + 1) && !absenceExpected[x][y + 1]) {
							presences[(y + 1) * 15 + x] += 1;
						}
						if (isValidWater(x - 1, y + 1) && !absenceExpected[x - 1][y + 1]) {
							presences[(y + 1) * 15 + x - 1] += 1;
						}
						if (isValidWater(x - 1, y) && !absenceExpected[x - 1][y]) {
							presences[(y) * 15 + x - 1] += 1;
						}
						if (isValidWater(x - 1, y - 1) && !absenceExpected[x - 1][y - 1]) {
							presences[(y - 1) * 15 + x - 1] += 1;
						}
						if (isValidWater(x, y - 1) && !absenceExpected[x][y - 1]) {
							presences[(y - 1) * 15 + x] += 1;
						}
						if (isValidWater(x + 1, y - 1) && !absenceExpected[x + 1][y - 1]) {
							presences[(y - 1) * 15 + x + 1] += 1;
						}
					}
				}
			}
			Map<Integer, Integer> map = new HashMap<>();

			for (int y = 0; y < 255; y++) {
				if (presences[y] != 0) {
					map.put(y, presences[y]);
				}
			}

		/*	System.err.println("Possible presences : " + map.entrySet()
					.stream()
					.sorted((e1, e2) -> e2.getValue() - e1.getValue())
					.map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
					.collect(Collectors.joining(",")));*/

			if (map.isEmpty()) {
				reset();
			}
			return map.entrySet()
					.stream()
					.sorted((e1, e2) -> e2.getValue() - e1.getValue())
					.map(Map.Entry::getKey)
					.collect(Collectors.toList());
		}


		public void update(char direction) {
			lastDirections.add(direction);
			switch (direction) {
				case 'N':
					updateNorth();
					break;
				case 'E':
					updateEast();
					break;
				case 'S':
					updateSouth();
					break;
				case 'W':
					updateWest();
					break;
			}
			updateAbsenceOnIsland();
		}


		public void updateNorth() {
			for (int i = 0; i < height - 1; i++) {
				for (int j = 0; j < width; j++) {
					absenceExpected[j][i] = absenceExpected[j][i + 1];
				}
			}
			for (int j = 0; j < width; j++) {
				absenceExpected[j][14] = true;
			}
		}


		public void updateSouth() {
			for (int i = height - 1; i > 0; i--) {
				for (int j = 0; j < width; j++) {
					absenceExpected[j][i] = absenceExpected[j][i - 1];
				}
			}

			for (int j = 0; j < width; j++) {
				absenceExpected[j][0] = true;
			}
		}


		public void updateWest() {
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width - 1; j++) {
					absenceExpected[j][i] = absenceExpected[j + 1][i];
				}
			}

			for (int j = 0; j < width; j++) {
				absenceExpected[14][j] = true;
			}
		}


		public void updateEast() {
			for (int i = 0; i < height; i++) {
				for (int j = width - 1; j > 0; j--) {
					absenceExpected[j][i] = absenceExpected[j - 1][i];
				}
			}

			for (int j = 0; j < width; j++) {
				absenceExpected[0][j] = true;
			}
		}


		public void updateAbsenceOnIsland() {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (cells[x][y] == CELL.ISLAND) {
						absenceExpected[x][y] = true;
					}
				}
			}

		}


		public void updateSonar(int sector) {
			int minY = Math.floorDiv((sector - 1), 3) * 5;
			int maxY = (Math.floorDiv(sector - 1, 3) + 1) * 5 - 1;

			int minX = ((sector - 1) % 3) * 5;
			int maxX = ((sector - 1) % 3 + 1) * 5 - 1;

			System.err.println("Opp in sector : " + sector + " " + minX + " " + maxX + " / " + minY + " " + maxY);
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if (i < minY || i > maxY || j < minX || j > maxX) {
						absenceExpected[j][i] = true;
					}
				}
			}
		}


		public void touched(int currentX, int currentY) {
			int dist = 4;
			int minY = Math.max(0, currentY - dist);
			int maxY = Math.min(14, currentY + dist);
			int minX = Math.max(0, currentX - dist);
			int maxX = Math.min(14, currentX + dist);

			System.err.println(name + " Touched. Opp in  : " + minX + " " + maxX + " / " + minY + " " + maxY);

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (y < minY || y > maxY || x < minX || x > maxX) {
						absenceExpected[x][y] = true;
					}
				}
			}

		}


		public void updateSonarNotInSector(int sector) {
			int minY = (int) (Math.floor((sector - 1) / 3.0) * 5);
			int maxY = ((int) (Math.floor((sector - 1) / 3.0)) + 1) * 5 - 1;
			int minX = ((sector - 1) % 3) * 5;
			int maxX = ((sector - 1) % 3 + 1) * 5 - 1;

			System.err.println(name + " Opp not in sector : " + sector + " " + minX + " " + maxX + " / " + minY + " " + maxY);
			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					absenceExpected[x][y] = true;
				}
			}
		}


		public void updateSilence() {
			for (Integer presence : getPossiblePresences()) {
				int presenceX = presence % 15;
				int presenceY = Math.floorDiv(presence, 15);
				boolean meetIsland = false;
				Set<Integer> lastVisitedCells = wherePassedToArriveHere(presenceX, presenceY);

				for (int i = 1;
					 i <= 4 && !meetIsland && !lastVisitedCells.contains(presenceX - i + presenceY * 15) && presenceX - i >= 0; i++) {
					absenceExpected[presenceX - i][presenceY] = false;
					meetIsland = cells[presenceX - i][presenceY] == CELL.ISLAND;
				}
				meetIsland = false;
				for (int i = 1;
					 i <= 4 && !meetIsland && !lastVisitedCells.contains(presenceX + i + presenceY * 15) && presenceX + i <= 14; i++) {
					absenceExpected[presenceX + i][presenceY] = false;
					meetIsland = cells[presenceX + i][presenceY] == CELL.ISLAND;

				}
				meetIsland = false;

				for (int i = 1;
					 i <= 4 && !meetIsland && !lastVisitedCells.contains(presenceX + (presenceY - i) * 15) && presenceY - i >= 0; i++) {
					absenceExpected[presenceX][presenceY - i] = false;
					meetIsland = cells[presenceX][presenceY - i] == CELL.ISLAND;

				}
				meetIsland = false;

				for (int i = 1;
					 i <= 4 && !meetIsland && !lastVisitedCells.contains(presenceX + (presenceY + i) * 15) && presenceY + i <= 14; i++) {
					absenceExpected[presenceX][presenceY + i] = false;
					meetIsland = cells[presenceX][presenceY + i] == CELL.ISLAND;

				}
			}
			lastDirections = new ArrayList<>();
			updateAbsenceOnIsland();
		}


		private Set<Integer> wherePassedToArriveHere(int x, int y) {
			Set<Integer> path = new HashSet<>();
			int currentX = x;
			int currentY = y;
			for (int i = lastDirections.size() - 1; i >= 0; i--) {
				char direction = lastDirections.get(i);
				if (direction == 'N') {
					currentY++;
				}
				if (direction == 'S') {
					currentY--;
				}

				if (direction == 'E') {
					currentX--;
				}

				if (direction == 'W') {
					currentX++;
				}
				path.add(currentX + currentY * 15);
			}
			return path;
		}


		public void logAbsenceVerified() {
			System.err.println(name);
			for (int y = 0; y < width; y++) {
				StringBuilder line = new StringBuilder();
				for (int x = 0; x < height; x++) {
					line.append(absenceExpected[x][y] ? "-" : "X");
				}
				System.err.println(line);
			}
		}


		public Integer chooseSectorToScan() {
			int[] scores = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (!absenceExpected[x][y]) {
						int currentSector = Math.floorDiv(y, 5) * 3 + Math.floorDiv(x, 5);
						scores[currentSector]++;
					}
				}
			}
			int max = scores[0];
			int index = 0;

			for (int i = 0; i < scores.length; i++) {
				if (max < scores[i]) {
					max = scores[i];
					index = i;
				}
			}

			StringBuilder line = new StringBuilder();
			for (int j = 0; j < 9; j++) {
				line.append(scores[j]).append(",");
			}
			System.err.println("Scores " + line + " Scan sector " + (index + 1));

			if (Arrays.stream(scores).filter(s -> s > 7).count() < 4) {
				return null;
			}
			return index + 1;
		}


		public void sendTorpedoAt(int myX, int myY) {
			Set<Integer> possiblePosisitons = possiblePositionForTorpedo(myX, myY, new HashSet<>(), new HashSet<>());
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (!possiblePosisitons.contains(x + y * 15)) {
						absenceExpected[x][y] = true;
					}
				}
			}
		}


		public static Set<Integer> possiblePositionForTorpedo(int x, int y, Set<Integer> cellsCrossed, Set<Integer> result) {
			cellsCrossed.add(x + y * 15);
			result.add(x + y * 15);

			if (cellsCrossed.size() < 5) {
				Arrays.stream(new int[][] { { x + 1, y }, { x - 1, y }, { x, y - 1 }, { x, y + 1 } })
						.filter(coord -> !cellsCrossed.contains(coord[0] + coord[1] * 15))
						.filter(coord -> isValidWater(coord[0], coord[1]))
						.forEach(coord -> possiblePositionForTorpedo(coord[0], coord[1], cellsCrossed, result));
			}

			cellsCrossed.remove(x + y * 15);
			return result;
		}

	}

}
