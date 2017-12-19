package pt.iscte.pandionj.figures;

import java.util.Collection;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;

import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.Utils;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.PandionJConstants;

public class ReferenceFigure extends PandionJFigure<IReferenceModel> {

	private Label label;
	private ReferenceLabel refLabel;
	
	public ReferenceFigure(IReferenceModel model) {
		super(model, false);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 3;
		layout.verticalSpacing = 0;
		setLayoutManager(layout);
		label = new Label(model.getName());
		label.setForegroundColor(ColorConstants.black);
		FontManager.setFont(label, PandionJConstants.VAR_FONT_SIZE);
		
		String tooltip = Utils.getTooltip(model); 

		Collection<String> tags = model.getTags();
		if(!tags.isEmpty())
			tooltip += "\ntags: " + String.join(", ", tags);
			
		label.setToolTip(new Label(tooltip));

		add(label);
		refLabel = new ReferenceLabel(model);
		add(refLabel);
		layout.setConstraint(refLabel, new GridData(PandionJConstants.POSITION_WIDTH, PandionJConstants.POSITION_WIDTH));
	}
	
	public ConnectionAnchor getAnchor() {
		return refLabel.getAnchor();
	}

	public void setError() {
		refLabel.setError();
	}
}
