package pt.iscte.pandionj.figures;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.model.ObjectModel;

public class ObjectFigure extends Figure {

	private Map<String, Label> fieldLabels;
	
	public ObjectFigure(ObjectModel model) {

		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		setLayoutManager(layout);
		Figure fig = new Figure();
		fig.setLayoutManager(layout);
		fig.setBorder(new MarginBorder(Constants.MARGIN));
		
		fieldLabels = new HashMap<String, Label>();
		for (String f : model.getFields()) {
			Label label = new Label(f + " = " + model.getValue(f));
			add(label);
			fieldLabels.put(f, label);
		}
		add(fig);
		setBorder(new LineBorder(ColorConstants.black, Constants.ARROW_LINE_WIDTH));
		
		setSize(-1, -1);
		setPreferredSize(Constants.POSITION_WIDTH, Math.max(Constants.POSITION_WIDTH, model.getFields().size()*30));

		model.addObserver(new Observer() {
			
			@Override
			public void update(Observable o, Object arg) {
				String name = (String) arg;
				fieldLabels.get(name).setText(name + " = " + model.getValue(name));
			}
		});
	
	}
}
