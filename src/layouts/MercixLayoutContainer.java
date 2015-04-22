package layouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Toolkit;
import java.time.LocalDateTime;
import java.util.Vector;

public class MercixLayoutContainer implements LayoutManager2 {

	Container					cont;
	Vector<MercixConstraints>	compConstr		= new Vector<MercixConstraints>();
	int							cols, rows, xGap, yGap;
	final int					def_width		= 100, def_height = 25;
	boolean						debug			= false;
	boolean						sizeCalculated	= false;

	public MercixLayoutContainer(int cols, int rows, int xGap, int yGap) {
		this.cols = cols;
		this.rows = rows;
		this.xGap = xGap;
		this.yGap = yGap;
		if (debug)
			System.out.println("Neues Layout: Cols:" + cols + " Rows:" + rows
					+ " xGap:" + xGap + " yGap:" + yGap);
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	/**
	 * Fügt eine Komponente dem Layout mit den gewünschten Werten hinzu.
	 *
	 * @param comp Die zu hinzufügende Komponente
	 * @param constraints Die gewünschten Werte der Komponente
	 */
	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if (debug)
			debug("addComponent", comp.getParent());
		compConstr.addElement((MercixConstraints) constraints);
	}

	@Override
	public void layoutContainer(Container parent) {
		if (debug)
			debug("layoutContainer", parent);
		if (!sizeCalculated) {
			parent.setSize(preferredLayoutSize(parent));
			if (debug)
				System.out.println("Calc Size: x=" + parent.getWidth()
						+ " | y=" + parent.getHeight());
		}
		Dimension containerSize = parent.getSize();
		Dimension maxSize = maximumLayoutSize(parent);
		int colSize = 0, rowSize = 0;
		if (containerSize.getWidth() > maxSize.getWidth()) {
			colSize = (int) maxSize.getWidth() / cols;
		} else {
			colSize = (int) containerSize.getWidth() / cols;
		}
		if (containerSize.getHeight() > maxSize.getHeight()) {
			rowSize = (int) maxSize.getHeight() / rows;
		} else {
			rowSize = (int) containerSize.getHeight() / rows;
		}
		if (debug)
			System.out.println("Container Größe: X:" + containerSize.width
					+ " | Y:" + containerSize.height + " | ColSize:" + colSize
					+ " | RowSize:" + rowSize);
		for (int i = 0; i < parent.getComponentCount(); i++) {
			MercixConstraints compConst = compConstr.elementAt(i);
			int x_pos = xGap + colSize * (compConst.getXPos() - 1);
			int y_pos = yGap + rowSize * (compConst.getYPos() - 1);
			int width = colSize * compConst.getCols() - 2 * xGap;
			int height = rowSize * compConst.getRows() - 2 * yGap;
			if (debug)
				System.out.println("Element Eigenschaften: I:" + i + " | x: "
						+ x_pos + " | y: " + y_pos + " | Width:" + width
						+ " | Height:" + height);
			parent.getComponent(i).setBounds(x_pos, y_pos, width, height);
		}
	}

	public Dimension minimumLayoutSize(Container parent) {
		if (debug)
			debug("minimumLayout", parent);
		if (!sizeCalculated) {
			sizeCalculated = true;
			return new Dimension(xGap * 2 * cols + def_width * cols, yGap * 2
					* rows + def_height * rows);
		} else
			return parent.getSize();
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return minimumLayoutSize(parent);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		// TODO - Element dem entsprechenden Consraint zuordnen
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}

	@Override
	public void invalidateLayout(Container target) {
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		if (debug)
			System.out.println("---- Maximum Layout ----");
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	private void debug(String callName, Container parent) {
		System.out.println("---- " + callName + " ----");
		String compNames = "";
		for (Component comp : parent.getComponents()) {
			compNames = compNames + comp.getClass() + " ";
		}
		System.out.println("Parent: " + parent.getClass() + " | CompCount: "
				+ parent.getComponentCount() + " | Comps: " + compNames);
		if (callName.equals("layoutContainer"))
			System.out.println("Vektorgröße:" + compConstr.size()
					+ " | Timestamp:" + LocalDateTime.now());

	}
}
