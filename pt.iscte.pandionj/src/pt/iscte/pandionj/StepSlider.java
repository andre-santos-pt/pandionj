package pt.iscte.pandionj;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Slider;

class StepSlider extends Slider {

	private int selection;
	
	public StepSlider(Composite parent, int style) {
		super(parent, style);
		
		addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				super.widgetSelected(e);
			}
		});
	}
	
	@Override
	public void setSelection(int value) {
		super.setSelection(value);
		selection = value;
	}
	

}
