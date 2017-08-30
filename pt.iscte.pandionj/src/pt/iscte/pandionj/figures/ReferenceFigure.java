package pt.iscte.pandionj.figures;

import java.util.Collection;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IReferenceModel;

public class ReferenceFigure extends PandionJFigure<IReferenceModel> {

	private ReferenceLabel refLabel;
	
	public ReferenceFigure(IReferenceModel model) {
		super(model);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 3;
		layout.verticalSpacing = 0;
		setLayoutManager(layout);
		Label label = new Label(model.getName());
		if(model.isInstance())
			FontManager.setFont(label, Constants.VAR_FONT_SIZE, FontManager.Style.BOLD);
		else
			FontManager.setFont(label, Constants.VAR_FONT_SIZE);

		Collection<String> tags = model.getTags();
		if(!tags.isEmpty())
			label.setToolTip(new Label("tags: " + tags.toString()));

		add(label);
		refLabel = new ReferenceLabel(model);
		add(refLabel);
		layout.setConstraint(refLabel, new GridData(Constants.POSITION_WIDTH, Constants.POSITION_WIDTH));
	}
	
	public ConnectionAnchor getAnchor() {
		return new PositionAnchor(refLabel, PositionAnchor.Position.CENTER);
	}
}
