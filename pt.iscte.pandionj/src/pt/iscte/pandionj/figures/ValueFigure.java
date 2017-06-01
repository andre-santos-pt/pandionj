package pt.iscte.pandionj.figures;


import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.FontManager.Style;
import pt.iscte.pandionj.model.PrimitiveType;
import pt.iscte.pandionj.model.ValueModel;
import pt.iscte.pandionj.model.ValueModel.Role;
import pt.iscte.pandionj.parser.variable.Gatherer;

public class ValueFigure extends Figure {
	private ValueLabel valueLabel;
	private ValueModel model;
	private Figure extraFigure;

	public ValueFigure(ValueModel model, Role role) {
		this.model = model;

		GridLayout layout = new GridLayout(3, false);
		setLayoutManager(layout);

		Label nameLabel = new Label(model.getName());
		nameLabel.setToolTip(new Label(role.toString()));
		if(model.isInstance())
			FontManager.setFont(nameLabel, Constants.VAR_FONT_SIZE, Style.BOLD);
		else
			FontManager.setFont(nameLabel, Constants.VAR_FONT_SIZE);
		add(nameLabel);

		valueLabel = new ValueLabel(model);
		Dimension size = valueLabel.getSize();
		layout.setConstraint(valueLabel, new GridData(size.width, size.height));

		add(valueLabel);

		if(Role.FIXED_VALUE.equals(role))
			setBackgroundColor(ColorConstants.lightGray);

		setOpaque(false); 
		model.registerDisplayObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if(Role.GATHERER.equals(role))
					((Label) extraFigure).setText(parcels());

				else if(Role.MOST_WANTED_HOLDER.equals(role)) {
					List<IJavaPrimitiveValue> history = model.getHistory();
					String val = "?";
					try {
						val = history.get(history.size()-2).getValueString();
					} catch (DebugException e) {
						e.printStackTrace();
					}
					extraFigure.add(new HistoryLabel(val), 0);
				}
				
				layout();
			}

			private String parcels() {
				switch(((Gatherer) model.getVariable()).operation) {
				case SUMMATION: return sumParcels();
				case PRODUCT_SERIES: return "?"; // TODO product parcels
				default: return "";
				}
			}
		});

		if(Role.GATHERER.equals(role)) {
			extraFigure = new Label("");
			extraFigure.setForegroundColor(ColorConstants.gray);
			FontManager.setFont(extraFigure, Constants.VAR_FONT_SIZE);
			add(extraFigure);
		}
		else if(Role.MOST_WANTED_HOLDER.equals(role)) {
			extraFigure = new Figure();
			extraFigure.setLayoutManager(new FlowLayout());
			add(extraFigure);
		}
	}


	// TODO: prodParcels
	private String sumParcels() {
		List<IJavaPrimitiveValue> history = model.getHistory();
		if(history.size() == 1)
			return "";

		PrimitiveType pType = PrimitiveType.match(model.getType());

		Object v = pType.getValue(history.get(0));
		String parcels = v.toString();
		for(int i = 1; i < history.size(); i++) {
			Object x = pType.getValue(history.get(i));
			if(pType.equals(PrimitiveType.BYTE))		parcels += " + " + ((Byte) 		x - (Byte) v);
			else if(pType.equals(PrimitiveType.SHORT))	parcels += " + " + ((Short) 	x - (Short) v);
			else if(pType.equals(PrimitiveType.INT)) 	parcels += " + " + ((Integer) 	x - (Integer) v);
			else if(pType.equals(PrimitiveType.LONG))	parcels += " + " + ((Long) 		x - (Long) v);
			else if(pType.equals(PrimitiveType.FLOAT)) 	parcels += " + " + ((Float) 	x - (Float) v);
			else if(pType.equals(PrimitiveType.DOUBLE)) parcels += " + " + ((Double) 	x - (Double) v);
			v = x;
		}

		return "(" + parcels + ")";
	}

	



	

	private class HistoryLabel extends Label {

		public HistoryLabel(String val) {
			super(val);
			FontManager.setFont(this, Constants.VAR_FONT_SIZE);
			setForegroundColor(ColorConstants.gray);
		}
		
		@Override
		protected void paintFigure(Graphics g) {
			super.paintFigure(g);
			g.setForegroundColor(ColorConstants.gray);
			Rectangle r = getBounds();
			g.drawLine(r.getTopLeft(), r.getBottomRight());
		}
		
		@Override
		public Dimension getPreferredSize(int wHint, int hHint) {
			return new Dimension(Constants.POSITION_WIDTH/2, Constants.POSITION_WIDTH/2);
		}
	}


}
