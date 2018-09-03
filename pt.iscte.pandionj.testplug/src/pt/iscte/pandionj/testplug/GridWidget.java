package pt.iscte.pandionj.testplug;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.extensibility.FontStyle;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.IPropertyProvider;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.PandionJUI;

public class GridWidget implements IArrayWidgetExtension {
	private static final int SIDE = 20;
	
	@Override
	public boolean accept(IArrayModel<?> e) {
		return e.isMatrix() && e.getComponentType().equals("char") && e.getLength() > 0 && e.getLength() <= 100;
	}

	@Override
	public IFigure createFigure(IArrayModel<?> e, IPropertyProvider args) {
		 GridFigure gridFigure = new GridFigure(e);
		 return gridFigure;
	}

	private static class GridFigure extends Figure {
		final IArrayModel<?> array;
		GridFigure(IArrayModel<?> array) {
			this.array = array;
			setOpaque(true);
			
//			setBackgroundColor(ColorConstants.lightGray);
//			setBorder(new LineBorder(ColorConstants.lightGray, 1));
			for (Object line : array.getModelElements()) {
				
			}
			array.registerDisplayObserver((a) -> {
				if(!array.getRuntimeModel().isTerminated())
					repaint();
			});
			Dimension dim = array.getMatrixDimension();
			GridLayout layout = new GridLayout(dim.width, true);
			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 0;
			setLayoutManager(layout);
			Font f = PandionJUI.getFont(30, FontStyle.BOLD);
			for(int line = 0; line < dim.height; line++)
				for(int col = 0; col < dim.width; col++) {
					IValueModel v = (IValueModel)((IArrayModel<?>)((IReferenceModel)array.getElementModel(line)).getModelTarget()).getElementModel(col);
					Label label = new Label(v.getCurrentValue());
					label.setFont(f);
					label.setToolTip(new Label(line + ", " + col));
					label.setBorder(new LineBorder(1));
//					label.setPreferredSize(new Dimension(SIDE, SIDE));
					add(label);
				}
					
		}
	}

}
