package pt.iscte.pandionj.extensions;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;

public class ImageAguia implements IObjectWidgetExtension {

	@Override
	public boolean accept(IType objectType) {
		return objectType.getFullyQualifiedName().equals("aguiaj.draw.Image");
	}

	@Override
	public IFigure createFigure(IObjectModel m) {
		return new Canvas(m);
	}
	
	private static class Canvas extends Figure {

		public Canvas(IObjectModel m) {
			setSize(m.getInt("width"), m.getInt("height"));
			setOpaque(true);
			setBackgroundColor(ColorConstants.green);
			IArrayModel array = m.getArray("values");
			add(new Label(array.getLength() + ""));
			array.registerDisplayObserver(new Observer() {
				
				@Override
				public void update(Observable o, Object arg) {
					System.out.println("!!! -  TODO interaction with object");
				}
			});
			
			m.registerDisplayObserver(new Observer() {
				
				@Override
				public void update(Observable o, Object arg) {
					if(arg.equals("height"))
						setSize(getSize().width, m.getInt("height"));
				}
			});
		}
	}

}
