package pt.iscte.pandionj;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.Filter;
import org.eclipse.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.eclipse.zest.layouts.progress.ProgressListener;

import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ModelElement;
import pt.iscte.pandionj.model.NullModel;
import pt.iscte.pandionj.model.ObjectModel;
import pt.iscte.pandionj.model.ValueModel;
import pt.iscte.pandionj.model.ReferenceModel;

public class PandionJLayoutAlgorithm implements LayoutAlgorithm {

	private static final int REF_OBJ_GAP = Constants.POSITION_WIDTH*2;
	
	
	@Override
	public void applyLayout(LayoutEntity[] entitiesToLayout, LayoutRelationship[] relationshipsToConsider, double x,
			double y, double width, double height, boolean asynchronous, boolean continuous)
					throws InvalidLayoutConfiguration {

		int refY = Constants.MARGIN;
		int objY = Constants.MARGIN;

		Map<ModelElement, LayoutEntity> yMap = new HashMap<ModelElement, LayoutEntity>();
		Map<ModelElement, Integer> refCount = new HashMap<>();

		for(LayoutEntity e : entitiesToLayout) {
			GraphNode node = (GraphNode) e.getGraphData();
			ModelElement element = (ModelElement) node.getData();
			
			if(element instanceof ObjectModel || element instanceof ArrayModel || element instanceof NullModel) {
				e.setLocationInLayout(x + (element instanceof NullModel ? Constants.POSITION_WIDTH : REF_OBJ_GAP), y + objY);
				objY += Constants.MARGIN + e.getHeightInLayout();
				yMap.put(element, e);
			}
		}

		for(LayoutEntity e : entitiesToLayout) {
			GraphNode node = (GraphNode) e.getGraphData();
			ModelElement element = (ModelElement) node.getData();
			if(element instanceof ReferenceModel) {
				ModelElement target = ((ReferenceModel) element).getTarget();
				Integer count = refCount.containsKey(target) ? refCount.get(target) : 0;
				LayoutEntity ent = yMap.get(target);
				if(ent != null) {
					refY = (int) (ent.getYInLayout() + ent.getHeightInLayout());
					e.setLocationInLayout(x + Constants.MARGIN, ent.getYInLayout() + count * Constants.OBJECT_SPACING);
					count += 1;
					refCount.put(target, count);
				}
				//				}
			}
			else if(element instanceof ValueModel){
				e.setLocationInLayout(x, refY);
				refY += e.getHeightInLayout() + Constants.MARGIN;
			}
		}
	
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public void setComparator(Comparator comparator) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFilter(Filter filter) {

	}

	@Override
	public void setEntityAspectRatio(double ratio) {

	}

	@Override
	public double getEntityAspectRatio() {
		return 1.0;
	}

	@Override
	public void addProgressListener(ProgressListener listener) {

	}

	@Override
	public void removeProgressListener(ProgressListener listener) {

	}

	@Override
	public void stop() {

	}

	@Override
	public void setStyle(int style) {

	}

	@Override
	public int getStyle() {
		return SWT.NONE;
	}

	@Override
	public void addEntity(LayoutEntity entity) {

	}

	@Override
	public void addRelationship(LayoutRelationship relationship) {

	}

	@Override
	public void removeEntity(LayoutEntity entity) {

	}

	@Override
	public void removeRelationship(LayoutRelationship relationship) {

	}

	@Override
	public void removeRelationships(List relationships) {

	}


}
