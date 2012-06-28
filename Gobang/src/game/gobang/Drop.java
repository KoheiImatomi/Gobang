package game.gobang;


public enum Drop {
	BLACK(0), WHITE(1), NULL(2);
	
	private int number;

	private Drop(final int n) {
		number = n;
	}

	public int number() {
		return number;
	}

	public static Drop valueOf(int num) {
		for (Drop m : values()) {
			if (m.number == num)
				return m;
		}
		throw new IllegalArgumentException();
	}
}
