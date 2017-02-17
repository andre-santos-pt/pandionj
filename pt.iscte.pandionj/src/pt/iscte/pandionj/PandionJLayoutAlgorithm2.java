package pt.iscte.pandionj;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.Filter;
import org.eclipse.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutGraph;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.eclipse.zest.layouts.progress.ProgressListener;

import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ModelElement;
import pt.iscte.pandionj.model.NullModel;
import pt.iscte.pandionj.model.ObjectModel;
import pt.iscte.pandionj.model.ObjectModel.SiblingVisitor;
import pt.iscte.pandionj.model.ValueModel;
import pt.iscte.pandionj.model.ReferenceModel;

public class PandionJLayoutAlgorithm2 implements LayoutAlgorithm {

	private static final int REF_OBJ_GAP = Constants.POSITION_WIDTH*2;


	@Override
	public void applyLayout(LayoutEntity[] entitiesToLayout, LayoutRelationship[] relationshipsToConsider, double x,
			double y, double width, double height, boolean asynchronous, boolean continuous)
					throws InvalidLayoutConfiguration {

		int refY = Constants.MARGIN;

		// index model -> layout
		Map<ModelElement, LayoutEntity> map = new HashMap<>();
		for(LayoutEntity e : entitiesToLayout) {
			GraphNode node = (GraphNode) e.getGraphData();
			ModelElement element = (ModelElement) node.getData();
			map.put(element, e);
		}


		for(LayoutEntity e : entitiesToLayout) {
			GraphNode node = (GraphNode) e.getGraphData();
			ModelElement element = (ModelElement) node.getData();
			if(element instanceof ValueModel || element instanceof ReferenceModel){
				e.setLocationInLayout(x, refY);
				refY += e.getHeightInLayout();

				if(element instanceof ReferenceModel) {
					ModelElement target = ((ReferenceModel) element).getTarget();
					LayoutEntity targetE = map.get(target);
					if(targetE == null) {
						System.err.println("!! " + target);
						continue;
					}
					
					targetE.setLocationInLayout(e.getXInLayout() + REF_OBJ_GAP, e.getYInLayout());
					refY = (int) Math.max(targetE.getHeightInLayout(), refY);

					// expand connected
					if(target instanceof ObjectModel) {
						ObjectModel obj = (ObjectModel) target;
						int breath = obj.siblingsBreath();
						if(breath == 1) {
							obj.traverseSiblings((o, d, f) -> {
								if(map.containsKey(o))
								map.get(o).setLocationInLayout(x + (d+1) * REF_OBJ_GAP, e.getYInLayout());
								else
									System.err.println("not " + o);
							});
						}
						else {
							int depth = obj.siblingsDepth();
							int w = breath * REF_OBJ_GAP;
							int[] v = new int[depth+1];
							obj.traverseSiblings((o, d, f) -> {
								map.get(o).setLocationInLayout(x + (1 + v[d]++) * REF_OBJ_GAP, e.getYInLayout() + d*REF_OBJ_GAP);
							});
						}
					}
				}
				refY += Constants.MARGIN*2;

			}
		}
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public void setComparator(Comparator comparator) {

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
