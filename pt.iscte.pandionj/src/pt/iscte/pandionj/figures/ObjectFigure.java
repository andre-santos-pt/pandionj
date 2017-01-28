package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.model.ObjectModel;

public class ObjectFigure extends Figure {

	public ObjectFigure(ObjectModel model) {

		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		setLayoutManager(layout);
//		add(new Label("toString() = " + ))
		Figure fig = new Figure();
		fig.setLayoutManager(layout);
		fig.setBorder(new LineBorder(ColorConstants.black, Constants.ARROW_LINE_WIDTH));
		for (String f : model.getFields()) {
			fig.add(new Label(f + " = " + model.getValue(f)));
		}
		add(fig);
	
		setBorder(new MarginBorder(Constants.MARGIN));
	}
}
