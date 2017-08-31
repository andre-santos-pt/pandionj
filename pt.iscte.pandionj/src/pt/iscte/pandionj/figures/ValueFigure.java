package pt.iscte.pandionj.figures;


import static pt.iscte.pandionj.Constants.ARROW_EDGE;

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

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.FontManager.Style;
import pt.iscte.pandionj.extensibility.Direction;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.IVariableModel.Role;
import pt.iscte.pandionj.model.ModelObserver;
import pt.iscte.pandionj.model.PrimitiveType;

public class ValueFigure extends PandionJFigure<IValueModel> {
	private ValueLabel valueLabel;
	private IValueModel model;
	private Figure extraFigure;
	private GridLayout layout;

	public ValueFigure(IValueModel model) {
		super(model, false);
		this.model = model;
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
		
		Label nameLabel = new Label(model.getName());
		if(role != null)
			nameLabel.setToolTip(new Label(role.toString()));
		
		comp.add(nameLabel);

		if(model.isInstance())
			FontManager.setFont(nameLabel, Constants.VAR_FONT_SIZE, Style.BOLD);
		else
			FontManager.setFont(nameLabel, Constants.VAR_FONT_SIZE);

		valueLabel = new ValueLabel(model);
		Dimension size = valueLabel.getSize();
		compLayout.setConstraint(valueLabel, new GridData(size.width, size.height));
		comp.add(valueLabel);
		add(comp);
		
		layout.setConstraint(comp, new GridData(SWT.RIGHT, SWT.DEFAULT, false, false));

		if(Role.FIXED_VALUE.equals(role)) {
			valueLabel.setBorder(new LineBorder(Constants.Colors.CONSTANT, Constants.ARRAY_LINE_WIDTH, SWT.LINE_SOLID));
			valueLabel.setForegroundColor(Constants.Colors.CONSTANT);
//			nameLabel.setForegroundColor(Constants.Colors.CONSTANT);
		}

//		setOpaque(false); 
		model.registerDisplayObserver(new ModelObserver() {
			
			@Override
			public void update(Object arg) {
//				setVisible(model.isWithinScope());
				if(isVisible()) {
					if(Role.GATHERER.equals(role)) {
						String parcels = parcels();
						((Label) extraFigure).setText(parcels);
//						Dimension dim = FigureUtilities.getTextExtents(parcels, extraFigure.getFont());
//						layout.setConstraint(extraFigure, new GridData(dim.width, dim.height));
//						layout.layout(ValueFigure.this);
					}
					else if(Role.MOST_WANTED_HOLDER.equals(role)) {
						List<String> history = model.getHistory();
						String val = history.get(history.size()-2); // FIXME bug?
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

		// TODO repor com RuntimeModel
		//		model.getStackFrame().registerDisplayObserver(new Observer() {
		//			@Override
		//			public void update(Observable o, Object arg) {
		//				setVisible(model.isWithinScope());
		//			}
		//		});
		//		model.getRuntimeModel().registerDisplayObserver((o,a) -> setVisible(model.isWithinScope()));

		if(Role.GATHERER.equals(role)) {
			add(new Label());
			extraFigure = new Label("");
			extraFigure.setForegroundColor(ColorConstants.gray);
			FontManager.setFont(extraFigure, Constants.VAR_FONT_SIZE);
			add(extraFigure);
			layout.setConstraint(extraFigure, new GridData(SWT.RIGHT, SWT.DEFAULT, false, false));
		}
		else if(Role.MOST_WANTED_HOLDER.equals(role)) {
			add(new Label());
			extraFigure = new Figure();
			extraFigure.setLayoutManager(new FlowLayout());
			add(extraFigure);
			layout.setConstraint(extraFigure, new GridData(SWT.RIGHT, SWT.DEFAULT, false, false));
		}
		else if(Role.STEPPER.equals(role)) {
			setBorder(new ArrowBorder(model.getVariableRole().getDirection()));
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
			else if(pType.equals(PrimitiveType.INT)) 	parcels.append("+" + ((Integer) 	x - (Integer) v));
			else if(pType.equals(PrimitiveType.LONG))	parcels.append("+" + ((Long) 		x - (Long) v));
			else if(pType.equals(PrimitiveType.FLOAT)) 	parcels.append("+" + ((Float) 		x - (Float) v));
			else if(pType.equals(PrimitiveType.DOUBLE)) 	parcels.append("+" + ((Double) 	x - (Double) v));
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
			FontManager.setFont(this, Constants.VAR_FONT_SIZE/2);
			setForegroundColor(ColorConstants.gray);
			// TODO text align center
//			setLabelAlignment(PositionConstants.CENTER);
			
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
			return new Dimension(valueLabel.getSize().width, Constants.POSITION_WIDTH/2);
		}
	}

	private class ArrowBorder implements Border {
		Direction direction;

		ArrowBorder(Direction direction) {
			this.direction = direction;
		}
		@Override
		public Insets getInsets(IFigure figure) {
			return new Insets(0, 0, 0, 3);
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
			graphics.setLineStyle(SWT.LINE_DOT);
			graphics.setForegroundColor(Constants.Colors.ILLUSTRATION);
			Rectangle r = figure.getBounds();
			Point from = r.getLocation().getTranslated(r.width-6, 10);
			Point to = from.getTranslated(0, Constants.POSITION_WIDTH / 2);
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
