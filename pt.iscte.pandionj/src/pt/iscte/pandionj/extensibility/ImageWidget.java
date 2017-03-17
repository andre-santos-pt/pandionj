package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;

import pt.iscte.pandionj.model.ArrayPrimitiveModel;

public class ImageWidget implements ArrayPrimitiveWidgetExtension {

	@Override
	public boolean accept(Object[] array, int dimensions) {
		if(dimensions != 2 || array.length < 1)
			return false;
		
		int width = ((Object[]) array[0]).length;
		
		for(int y = 1; y < array.length; y++)
			if(array[y] == null || ((Object[]) array[y]).length != width)
				return false;
				
		return true;
	}

	@Override
	public boolean qualifies(ArrayPrimitiveModel arrayModel) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Figure createFigure(ArrayPrimitiveModel arrayModel) {
		return null;
	}

	@Override
	public void positionChanged(Object oldValue, Object newValue, int i, int indexes) {
		// TODO Auto-generated method stub
		
	}
	
	private class ImageFig extends Figure {
		
		ImageFig(int w, int h) {
			setLayoutManager(new FlowLayout());
			setSize(w, h);
		}
		
		@Override
		protected void paintFigure(Graphics graphics) {
			super.paintFigure(graphics);
			
		}
		
	}

}
