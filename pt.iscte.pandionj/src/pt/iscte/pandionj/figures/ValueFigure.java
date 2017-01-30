package pt.iscte.pandionj.figures;


import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.model.ValueModel;

public class ValueFigure extends Figure {

	public ValueFigure(ValueModel model) {
		GridLayout layout = new GridLayout(2,false);
		setLayoutManager(layout);
		
		Label nameLabel = new Label(model.getName());
		nameLabel.setFont(new Font(null, "Arial", 16, SWT.NONE));
		add(nameLabel);
		
		Label valueLabel = new Label(model.getCurrentValue());
		valueLabel.setOpaque(true);
		valueLabel.setFont(new Font(null, "Arial", 42, SWT.NONE));
		valueLabel.setBorder(new LineBorder(ColorConstants.black, 1, SWT.LINE_SOLID));
		layout.setConstraint(valueLabel, new GridData(Constants.POSITION_WIDTH,Constants.POSITION_WIDTH));
		add(valueLabel);
		
		setSize(-1, -1);
		
		model.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				valueLabel.setText(model.getCurrentValue());
				valueLabel.setBackgroundColor(ColorConstants.cyan);
			}
		});
	}
}
