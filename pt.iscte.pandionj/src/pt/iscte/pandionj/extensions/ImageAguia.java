package pt.iscte.pandionj.extensions;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.core.IType;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;
import pt.iscte.pandionj.extensions.images.GrayscaleImageWidget;
import pt.iscte.pandionj.extensions.images.GrayscaleImageWidget.ImageFig;
import pt.iscte.pandionj.model.ModelObserver;

public class ImageAguia implements IObjectWidgetExtension {

	@Override
	public boolean accept(IType objectType) {
		return objectType.getFullyQualifiedName().equals("aguiaj.draw.Image");
	}

	@Override
	public IFigure createFigure(IObjectModel m) {
		IArrayModel array = m.getArray("data");
		ImageFig imageFig = new GrayscaleImageWidget.ImageFig(array);
		m.registerDisplayObserver(new ModelObserver() {
			@Override
			public void update(Object arg) {
				if(arg.equals("data"))
					imageFig.updateModel(m.getArray("data"));
			}
		});
		return imageFig;
//		return new Canvas(m);
	}
	
	private static class Canvas extends Figure {

		private IArrayModel array;
		
		public Canvas(IObjectModel m) {
			array = m.getArray("data");
			setSize(((Object[])array.getValues()[0]).length, array.getLength());
			setOpaque(true);
			setBackgroundColor(ColorConstants.green);
			array.registerDisplayObserver(new ModelObserver() {
				
				@Override
				public void update(Object arg) {
					System.out.println("!!! -  TODO interaction with object");
				}
			});
			
			m.registerDisplayObserver(new ModelObserver() {
				
				@Override
				public void update(Object arg) {
					if(arg.equals("height"))
						setSize(getSize().width, m.getInt("height"));
				}
			});
		}
		
		@Override
		protected void paintFigure(Graphics graphics) {
//			Object[][] m = (Object[][]) array.getValues();
//			for(int y = 0; y < m.length; y++)
//				for(int x = 0; x < m[i].length; i++)
//					graphics.drawImage(image, p);
			
		}
	}

}
