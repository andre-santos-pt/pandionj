package pt.iscte.pandionj.figures;

import java.util.Collection;

import org.eclipse.draw2d.Label;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IVariableModel;

public class ReferenceFigure extends Label {

	public ReferenceFigure(IVariableModel model) {
		super(model.getName());
		if(model.isInstance())
			FontManager.setFont(this, Constants.VAR_FONT_SIZE, FontManager.Style.BOLD);
		else
			FontManager.setFont(this, Constants.VAR_FONT_SIZE);
		
//		model.registerDisplayObserver(new Observer() {
//			public void update(Observable o, Object arg) {
//				if(model.getModelTarget() instanceof NullModel)
//					setToolTip(new Label("null"));
//				else
//					setToolTip(null);
//			}
//		});
		
		Collection<String> tags = model.getTags();
		if(!tags.isEmpty())
			setToolTip(new Label("tags: " + tags.toString()));
		
		// TODO repor com RuntimeModel
//		model.getStackFrame().registerDisplayObserver(new Observer() {
//			@Override
//			public void update(Observable o, Object arg) {
//				setVisible(model.isWithinScope());
//			}
//		});
	}
}
