package model;

import java.util.Random;

class Dado {
	private final Random random;

	Dado() {
		this.random = new Random();
	}

	Dado(long semente) {
		this.random = new Random(semente);
	}

	int lancar() {
		return random.nextInt(6) + 1;
	}
}
