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
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.model.ValueModel;
import pt.iscte.pandionj.model.ValueModel.Role;

public class ValueFigure extends Figure {

	public ValueFigure(ValueModel model, Role role) {
		GridLayout layout = new GridLayout(2,false);
		setLayoutManager(layout);
		
		Label nameLabel = new Label(model.getName());
		nameLabel.setFont(new Font(null, Constants.FONT_FACE, Constants.VAR_FONT_SIZE, SWT.NONE));
		add(nameLabel);
		
		Label valueLabel = new Label(model.getCurrentValue());
		valueLabel.setOpaque(true);
		valueLabel.setFont(new Font(null, "Arial", 42, SWT.NONE));
		int lineWidth = Role.FIXED_VALUE.equals(role) ? 4 : 2;
		valueLabel.setBorder(new LineBorder(ColorConstants.black, lineWidth, SWT.LINE_SOLID));
		layout.setConstraint(valueLabel, new GridData(Constants.POSITION_WIDTH, Constants.POSITION_WIDTH));
		add(valueLabel);
		
		if(Role.FIXED_VALUE.equals(role))
			setBackgroundColor(ColorConstants.lightGray);
		
		setSize(-1, -1);
		
		model.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Display.getDefault().syncExec(() -> {
						valueLabel.setText(model.getCurrentValue());
						valueLabel.setBackgroundColor(Constants.HIGHLIGHT_COLOR);
				});
			}
		});
		
		if(role != null)
			setToolTip(new Label(role.toString()));
	}
}
