package pt.iscte.pandionj.figures;


import static pt.iscte.pandionj.extensibility.PandionJConstants.ARROW_EDGE;

import java.util.List;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.Utils;
import pt.iscte.pandionj.extensibility.Direction;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.IVariableModel.Role;
import pt.iscte.pandionj.extensibility.ModelObserver;
import pt.iscte.pandionj.extensibility.PandionJConstants;
import pt.iscte.pandionj.model.PrimitiveType;

public class ValueFigure extends PandionJFigure<IValueModel> {
	private static final int ANNOTATION_FONT_SIZE = (int) Math.round(PandionJConstants.VAR_FONT_SIZE/1.5);
	
	private ValueLabel valueLabel;
//	private IValueModel model;
	private Figure extraFigure;
	private GridLayout layout;

	public ValueFigure(IValueModel model) {
		super(model, false);
		Role role = model.getRole();
		
		layout = new GridLayout(1, false);
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		setLayoutManager(layout);

		Figure comp = new Figure();
		GridLayout compLayout = new GridLayout(2,false);
		compLayout.marginHeight = 0;
		compLayout.marginWidth = 0;
		compLayout.horizontalSpacing = 3;
		compLayout.verticalSpacing = 0;
		
		comp.setLayoutManager(compLayout);
		
		String tooltip = Utils.getTooltip(model); 
		
		Label nameLabel = new Label(model.getName());
		nameLabel.setForegroundColor(ColorConstants.black);
		FontManager.setFont(nameLabel, PandionJConstants.VAR_FONT_SIZE);
		if(role != Role.NONE)
			tooltip += "\nrole: " + role.toString();
		
		nameLabel.setToolTip(new Label(tooltip));
		
		comp.add(nameLabel);


		valueLabel = new ValueLabel(model);
		Dimension size = valueLabel.getSize();
		compLayout.setConstraint(valueLabel, new GridData(size.width, size.height));
		comp.add(valueLabel);
		add(comp);
		
		layout.setConstraint(comp, new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));

		if(Role.FIXED_VALUE.equals(role) || Role.FIXED_ARRAY_INDEX.equals(role)) {
			valueLabel.setBorder(new LineBorder(PandionJConstants.Colors.CONSTANT, PandionJConstants.ARRAY_LINE_WIDTH, SWT.LINE_SOLID));
			valueLabel.setForegroundColor(PandionJConstants.Colors.CONSTANT);
		}

		model.registerDisplayObserver(new ModelObserver<Object>() {
			
			@Override
			public void update(Object arg) {
				if(isVisible()) {
					if(Role.GATHERER.equals(role)) {
						String parcels = parcels();
						((Label) extraFigure).setText(parcels);
					}
					else if(Role.MOST_WANTED_HOLDER.equals(role)) {
						List<String> history = model.getHistory();
						/* upon creation, the model has one element (initial);
						 * because the update event is fired after modification, there is always at least 2 elements in history
						 */
						String val = history.get(history.size()-2); 
						extraFigure.add(new HistoryLabel(val), 0);
					}
					layout();
				}
			}

			private String parcels() {
				if(model.getHistory().size() <= 1) {
					return "";
				}
				else {
					switch(model.getVariableRole().getGathererType()) {
					case SUM: return sumParcels();
					case MINUS: return minusParcels();
					case PROD: return prodParcels();
					default: return "";
					}
				}
			}
		});

		if(Role.GATHERER.equals(role)) {
			extraFigure = new Label("");
			extraFigure.setForegroundColor(PandionJConstants.Colors.ROLE_ANNOTATIONS);
			FontManager.setFont(extraFigure, ANNOTATION_FONT_SIZE);
			add(extraFigure);
			layout.setConstraint(extraFigure, new GridData(SWT.RIGHT, SWT.BEGINNING, false, false));
		}
		else if(Role.MOST_WANTED_HOLDER.equals(role)) {
			extraFigure = new Figure();
			extraFigure.setLayoutManager(new FlowLayout());
			add(extraFigure);
			layout.setConstraint(extraFigure, new GridData(SWT.RIGHT, SWT.DEFAULT, false, false));
		}
		else if(Role.STEPPER.equals(role) || Role.ARRAY_ITERATOR.equals(role)) {
			Direction direction = model.getVariableRole().getDirection();
			if(direction != Direction.NONE)
				setBorder(new ArrowBorder(direction));
		}
	}

	private String sumParcels() {
		List<String> history = model.getHistory();
		assert !history.isEmpty();

		PrimitiveType pType = PrimitiveType.match(model.getTypeName());

		Object v = pType.getValue(history.get(0));
		StringBuffer parcels = new StringBuffer(v.toString());
		for(int i = 1; i < history.size(); i++) {
			Object x = pType.getValue(history.get(i));
			if(pType.equals(PrimitiveType.BYTE))			parcels.append("+" + ((Byte) 		x - (Byte) v));
			else if(pType.equals(PrimitiveType.SHORT))	parcels.append("+" + ((Short) 		x - (Short) v));
			else if(pType.equals(PrimitiveType.INT)) 	parcels.append("+" + ((Integer) 		x - (Integer) v));
			else if(pType.equals(PrimitiveType.LONG))	parcels.append("+" + ((Long) 		x - (Long) v));
			else if(pType.equals(PrimitiveType.FLOAT)) 	parcels.append("+" + ((Float) 		x - (Float) v));
			else if(pType.equals(PrimitiveType.DOUBLE)) 	parcels.append("+" + ((Double) 		x - (Double) v));
			v = x;
		}
		return "(" + parcels.toString() + ")";
	}


	private String minusParcels() {
		List<String> history = model.getHistory();
		assert !history.isEmpty();

		PrimitiveType pType = PrimitiveType.match(model.getTypeName());

		Object v = pType.getValue(history.get(0));
		StringBuffer parcels = new StringBuffer(v.toString());
		for(int i = 1; i < history.size(); i++) {
			Object x = pType.getValue(history.get(i));
			if(pType.equals(PrimitiveType.BYTE))			parcels.append("-" + ((Byte) 		v - (Byte) x));
			else if(pType.equals(PrimitiveType.SHORT))	parcels.append("-" + ((Short) 		v - (Short) x));
			else if(pType.equals(PrimitiveType.INT)) 	parcels.append("-" + ((Integer) 	v - (Integer) x));
			else if(pType.equals(PrimitiveType.LONG))	parcels.append("-" + ((Long) 		v - (Long) x));
			else if(pType.equals(PrimitiveType.FLOAT)) 	parcels.append("-" + ((Float) 		v - (Float) x));
			else if(pType.equals(PrimitiveType.DOUBLE)) 	parcels.append("-" + ((Double) 	v - (Double) x));
			v = x;
		}
		return "(" + parcels.toString() + ")";
	}
	
	private String prodParcels() {
		List<String> history = model.getHistory();
		assert !history.isEmpty();

		PrimitiveType pType = PrimitiveType.match(model.getTypeName());

		Object v = pType.getValue(history.get(0));
		StringBuffer parcels = new StringBuffer(v.toString());
		for(int i = 1; i < history.size(); i++) {
			Object x = pType.getValue(history.get(i));
			if(pType.equals(PrimitiveType.BYTE))			parcels.append("x" + ((Byte) 		x / (Byte) v));
			else if(pType.equals(PrimitiveType.SHORT))	parcels.append("x" + ((Short) 		x / (Short) v));
			else if(pType.equals(PrimitiveType.INT)) 	parcels.append("x" + ((Integer) 	x / (Integer) v));
			else if(pType.equals(PrimitiveType.LONG))	parcels.append("x" + ((Long) 		x / (Long) v));
			else if(pType.equals(PrimitiveType.FLOAT)) 	parcels.append("x" + ((Float) 		x / (Float) v));
			else if(pType.equals(PrimitiveType.DOUBLE)) 	parcels.append("x" + ((Double) 	x / (Double) v));
			v = x;
		}
		return "(" + parcels.toString() + ")";
	}

	private class HistoryLabel extends Label {
		public HistoryLabel(String val) {
			super(val);
			FontManager.setFont(this, ANNOTATION_FONT_SIZE);
			setForegroundColor(ColorConstants.gray);
			
		}

		@Override
		protected void paintFigure(Graphics g) {
			super.paintFigure(g);
			g.setForegroundColor(ColorConstants.gray);
			Rectangle r = getBounds();
			g.drawLine(r.getTopLeft(), r.getBottomRight());
		}
	}

	private class ArrowBorder implements Border {
		Direction direction;

		ArrowBorder(Direction direction) {
			this.direction = direction;
		}
		@Override
		public Insets getInsets(IFigure figure) {
			return new Insets(0, 0, 0, 0);
		}

		@Override
		public Dimension getPreferredSize(IFigure figure) {
			return new Dimension();
		}

		@Override
		public boolean isOpaque() {
			return false;
		}

		@Override
		public void paint(IFigure figure, Graphics graphics, Insets insets) {
			graphics.setForegroundColor(PandionJConstants.Colors.ROLE_ANNOTATIONS);
			Rectangle r = figure.getBounds();
			int startY = direction == Direction.FORWARD ? 2 : 1 + (PandionJConstants.POSITION_WIDTH/3)*2;
			Point from = r.getLocation().getTranslated(r.width-6, startY);
			Point to = from.getTranslated(0, PandionJConstants.POSITION_WIDTH / 4);
			if(direction == Direction.FORWARD) {
				Point t = from;
				from = to;
				to = t;
			}
			drawArrow(graphics, from, to); 
		}

		private void drawArrow(Graphics g, Point from, Point to) {
			g.setLineStyle(Graphics.LINE_SOLID);
			g.drawLine(from, to);

			int yy = from.y < to.y ? -ARROW_EDGE : ARROW_EDGE;
			g.drawLine(to, to.getTranslated(-ARROW_EDGE, yy));
			g.drawLine(to, to.getTranslated(ARROW_EDGE, yy));
		}

	}
}
