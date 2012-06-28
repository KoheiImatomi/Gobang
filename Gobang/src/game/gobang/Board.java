package game.gobang;

import java.io.Serializable;

public class Board implements Serializable{
	private static final long serialVersionUID = -6004445763491489103L;
	private final Drop[][] content;
	private final int size;
	private int preX = -1;
	private int preY = -1;
	
	public static enum Forbidden{
		SANSAN, YONYON, TYOUREN;
	}
	
	public Board(final int size) {
		content = new Drop[size][size];
		this.size = size;
		init();
	}

	public void init() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				content[i][j] = Drop.NULL;
			}
		}
	}

	public int size() {
		return size;
	}

	public Drop getDropAt(final int x, final int y) {
		if (!isValidIndex(x, y))
			throw new IllegalArgumentException(String.format("(%d, %d)", x, y));
		return content[y][x];
	}

	public boolean isValidIndex(final int x, final int y) {
		if (0 <= x && x < size && 0 <= y && y < size)
			return true;
		return false;
	}

	public boolean isDroppableAt(final int x, final int y) {
		if (!isValidIndex(x, y))
			throw new IllegalArgumentException(String.format("(%d, %d)", x, y));
		if (content[y][x] == Drop.NULL)
			return true;
		return false;
	}

	public void putAt(final int x, final int y, Drop d) {
		if (!isValidIndex(x, y))
			throw new IllegalArgumentException(String.format("(%d, %d)", x, y));
		content[y][x] = d;
		preX = x;
		preY = y;
	}
	
	public boolean undo(){
		if(!isValidIndex(preX, preY))
			return false;
		if(getDropAt(preX, preY) == Drop.NULL)
			return false;
		putAt(preX, preY, Drop.NULL);
		return true;
	}

	public boolean chechWin(Drop d) {
		int c;
		for (int s = 5 - size; s < size - 4; s++) {
			int x = s;
			int y = 0;
			c = 0;
			while (y < size) {
				if (isValidIndex(x, y)) {
					if (content[y][x] == d)
						c++;
					else
						c = 0;
					if (c == 5)
						return true;
				}
				x++;
				y++;
			}
		}
		for (int s = 2 * size - 5; s >= 4; s--) {
			int x = s;
			int y = 0;
			c = 0;
			while (y < size) {
				if (isValidIndex(x, y)) {
					if (content[y][x] == d)
						c++;
					else
						c = 0;
					if (c == 5)
						return true;
				}
				x--;
				y++;
			}
		}
		for (int x = 0; x < size; x++) {
			c = 0;
			for (int y = 0; y < size; y++) {
				if (content[y][x] == d)
					c++;
				else
					c = 0;
				if (c == 5)
					return true;
			}
		}
		for (int y = 0; y < size; y++) {
			c = 0;
			for (int x = 0; x < size; x++) {
				if (content[y][x] == d)
					c++;
				else
					c = 0;
				if (c == 5)
					return true;
			}
		}
		return false;
	}

	public void display() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				String s;
				if (content[i][j] == Drop.BLACK)
					s = "B";
				else if (content[i][j] == Drop.WHITE)
					s = "W";
				else
					s = "N";
				System.out.print(s + " ");
			}
			System.out.print("\n");
		}
	}
}
