package pt.iscte.pandionj.extensibility;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;

public class ImageWidget implements ArrayWidgetExtension {

	@Override
	public boolean accept(Object[] array, String type, int dimensions) {
		if(!type.equals("int[][]"))
			return false;
		
		if(dimensions != 2 || array.length < 1)
			return false;
		
		int width = ((Object[]) array[0]).length;
		
		for(int y = 1; y < array.length; y++)
			if(array[y] == null || ((Object[]) array[y]).length != width)
				return false;
				
		return true;
	}

	
	@Override
	public IFigure createFigure(Object[] array, String type, int dimensions) {
		return new ImageFig((Object[][]) array);
	}
	
	
	private class ImageFig extends Figure {
		Object[][] array;
		ImageFig(Object[][] array) {
			this.array = array;
			setLayoutManager(new FlowLayout());
			setSize(array.length, array[0].length);
			setBackgroundColor(ColorConstants.white);
			setBorder(new LineBorder(ColorConstants.black));
			setOpaque(true);
		}
		
		@Override
		protected void paintFigure(Graphics g) {
			super.paintFigure(g);
			g.setForegroundColor(ColorConstants.red);
			for(int y = 0; y < array.length; y++)
				for(int x = 0; x < array[y].length; x++)
					if((int) array[x][y] != 0)
						g.drawPoint(x, y);
		}
		
		@Override
		public Dimension getPreferredSize(int wHint, int hHint) {
			return new Dimension(array[0].length, array.length);
		}
		
		public void positionChanged(Object oldValue, Object newValue, int i, int indexes) {
			
		}
	}

	

}
