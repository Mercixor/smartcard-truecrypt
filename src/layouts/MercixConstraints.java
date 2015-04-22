package layouts;

public class MercixConstraints {
	int	x_pos, y_pos, cols, rows;

	public MercixConstraints(int x_pos, int y_pos, int cols, int rows) {
		this.x_pos = x_pos;
		this.y_pos = y_pos;
		this.cols = cols;
		this.rows = rows;
	}

	public int getXPos() {
		return x_pos;
	}

	public int getYPos() {
		return y_pos;
	}

	public int getCols() {
		return cols;
	}

	public int getRows() {
		return rows;
	}
}
