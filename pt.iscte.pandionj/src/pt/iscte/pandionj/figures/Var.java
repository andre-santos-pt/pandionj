package pt.iscte.pandionj.figures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;

class Var {
	enum Direction {
		NONE, FORWARD, BACKWARD;
	}
	
	final String id;
	final Color color;
	List<Integer> indexHistory;
	Object bound;
	boolean isBar;
	boolean markError;

	Var(String id, int index, Object bound, boolean isBar, Color color) {
		this.id = id;
		this.indexHistory = new ArrayList<>();
		indexHistory.add(index);
		this.bound = bound;
		this.color = color;
		this.isBar = isBar;
		markError = false;
	}

	int getCurrentIndex() {
		return indexHistory.get(indexHistory.size()-1);
	}

	boolean isBounded() {
		return bound != null;
	}

	int getBound() {
		return -1;
//		if(bound == null || bound instanceof String && !vars.containsKey((String) bound))
//			return -1;
//		else
//			return bound instanceof Integer ? (Integer) bound : vars.get((String) bound).getCurrentIndex();
	}

	Direction getDirection() {
		int i = getCurrentIndex() ;
		int b = getBound();
		return b == -1 || b == i ? Direction.NONE : i < b ? Direction.FORWARD : Direction.BACKWARD;
	}

	boolean isBar() {
		return isBar;
	}

	void updateIndex(int index) {
		indexHistory.add(index);			
	}

	List<Integer> getIndexes() {
		return indexHistory;
	}

	void markError() {
		markError = true;
	}
}