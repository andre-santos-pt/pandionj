package pt.iscte.pandionj.figures;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.model.ObjectModel;

public class ObjectFigure extends Figure {
	private ObjectModel model;
	private Map<String, Label> fieldLabels;
	private Label label;

	public ObjectFigure(ObjectModel model) {
		this.model = model;
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		setLayoutManager(layout);
		RoundedRectangle fig = new RoundedRectangle();
		fig.setCornerDimensions(new Dimension(10, 10));
		fig.setLayoutManager(layout);
		fig.setBorder(new MarginBorder(Constants.OBJECT_PADDING));
		fig.setBackgroundColor(Constants.OBJECT_COLOR);

		fieldLabels = new HashMap<String, Label>();
		//		for (String f : model.getFields()) {
		//			Label label = new Label(f + " = " + model.getValue(f));
		//			fig.add(label);
		//			fieldLabels.put(f, label);
		//		}

		label = new Label();
		label.setFont(new Font(null, Constants.FONT_FACE, Constants.VALUE_FONT_SIZE, SWT.NONE));
		fig.add(label);
		add(fig);


		setBorder(new MarginBorder(Constants.OBJECT_PADDING));
		//		setBorder(new LineBorder(ColorConstants.black, Constants.ARROW_LINE_WIDTH));
		setOpaque(false);
		setSize(-1, -1);

		
		//		setPreferredSize(Constants.POSITION_WIDTH, Math.max(Constants.POSITION_WIDTH, model.getFields().size()*30));

		//		model.addObserver(new Observer() {
		//			
		//			@Override
		//			public void update(Observable o, Object arg) {
		//				String name = (String) arg;
		//				Display.getDefault().syncExec(() -> {
		//					fieldLabels.get(name).setText(name + " = " + model.getValue(name));
		//				});
		//			}
		//		});
		
		model.registerObserver(new Observer() {
			public void update(Observable o, Object arg) {
				Display.getDefault().syncExec(() -> {
					label.setText(model.toStringValue());
//					updateSize();
				});
			}
		});

		try {
			setToolTip(new Label(":" + model.getContent().getJavaType().getName()+ "\n" + "FIELD VALUES"));
		} catch (DebugException e) {
			e.printStackTrace();
		}
		label.setText(model.toStringValue());
		setPreferredSize(getPreferredSize().expand(Constants.OBJECT_PADDING, Constants.OBJECT_PADDING));
	}
	
	private void updateSize() {
		Dimension textExtents = TextUtilities.INSTANCE.getTextExtents(label.getText(), label.getFont());
		System.out.println(getPreferredSize());
		setPreferredSize(textExtents.expand(Constants.OBJECT_PADDING, Constants.OBJECT_PADDING));
		layout();
	}
	
	
}
