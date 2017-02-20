package pt.iscte.pandionj;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ModelElement;
import pt.iscte.pandionj.model.NullModel;
import pt.iscte.pandionj.model.ObjectModel;
import pt.iscte.pandionj.model.ObjectModel.SiblingVisitor;
import pt.iscte.pandionj.model.ValueModel;
import pt.iscte.pandionj.model.ReferenceModel;

public class PandionJLayoutAlgorithm2 implements LayoutAlgorithm {

	private static final int REF_OBJ_GAP = Constants.POSITION_WIDTH*2;

	// index model -> layout
	private Map<ModelElement, LayoutEntity> map = new WeakHashMap<>();
	private Set<LayoutEntity> dirty = new HashSet<>();

	private void setLocation(LayoutEntity e, double x, double y) {
		if(dirty.contains(e)) {
			e.setLocationInLayout(x, y);
			dirty.remove(e);
		}
	}

	@Override
	public void applyLayout(LayoutEntity[] entitiesToLayout, LayoutRelationship[] relationshipsToConsider, double x,
			double y, double width, double height, boolean asynchronous, boolean continuous)
					throws InvalidLayoutConfiguration {

		int refY = Constants.MARGIN;

		for(LayoutEntity e : entitiesToLayout) {
			GraphNode node = (GraphNode) e.getGraphData();
			ModelElement element = (ModelElement) node.getData();
			if(!map.containsKey(element)) {
				map.put(element, e);
				if(element instanceof ReferenceModel)
					element.asReference().addObserver((o,a) -> dirty.add(e));

				dirty.add(e);
			}
		}


		for(LayoutEntity e : entitiesToLayout) {
			GraphNode node = (GraphNode) e.getGraphData();
			ModelElement element = (ModelElement) node.getData();
			if(element instanceof ValueModel || element instanceof ReferenceModel){
				setLocation(e, x, refY);

				refY += e.getHeightInLayout();

				if(element instanceof ReferenceModel) {
					ModelElement target = ((ReferenceModel) element).getTarget();
					LayoutEntity targetE = map.get(target);
					if(targetE == null) {
						System.err.println("!! " + target);
						continue;
					}
				

					refY = (int) Math.max(targetE.getHeightInLayout(), refY);

					// expand connected
					if(target instanceof ObjectModel) {
						ObjectModel obj = (ObjectModel) target;
						int breath = obj.siblingsBreath();
						if(breath == 1) {
							class Visitor implements SiblingVisitor {
//								Multiset<Integer> count = HashMultiset.create();
								public void accept(ModelElement o, ModelElement parent, int index, int d, String field) {
									if(map.containsKey(o)) {
										LayoutEntity n = map.get(o);
//										count.add(d);
//										int c = count.count(d);
										switch(index) {
										case 1:
											setLocation(n, x + d* REF_OBJ_GAP, e.getYInLayout() + REF_OBJ_GAP/2);
											break;
										case 2:
											setLocation(n, x + d* REF_OBJ_GAP, e.getYInLayout() - REF_OBJ_GAP/2);
											break;
										default:
											int dist = (d+1) * REF_OBJ_GAP;
											if(o instanceof NullModel)
												dist -= REF_OBJ_GAP / 2;
											setLocation(n, x + dist, e.getYInLayout());
										}
									}
									else
										System.err.println("not " + o);
								}

							}
							setLocation(targetE, e.getXInLayout() + REF_OBJ_GAP, e.getYInLayout());
							Visitor v = new Visitor();
							obj.traverseSiblings(v, true);
						}
						else {
//							int depth = obj.siblingsDepth();
							
							int w = breath*2 * REF_OBJ_GAP;
						
							Multiset<Integer> count = HashMultiset.create();
							int yy = refY;
							obj.traverseSiblingsDepth((o, p, i, d, f) -> {
								
								if(map.containsKey(o)) {
									if(o instanceof ObjectModel)
										System.out.println(((ObjectModel) o).toStringValue());
//									count.add(d);
//									LayoutEntity n = map.get(o);
//									int tmp = (int) (w/Math.pow(2, d));
//									int dist = count.count(d) * tmp;
//									if(d == 0) {
//										setLocation(n, REF_OBJ_GAP + (w/2), e.getYInLayout() + d*REF_OBJ_GAP/2);
//									}
//									else {
//										double xx = map.get(p).getXInLayout() - REF_OBJ_GAP/2;
//										xx += i * REF_OBJ_GAP;
//										setLocation(n, xx, e.getYInLayout() + d*REF_OBJ_GAP/2);
//									}
////										setLocation(n, x + count.count(d) * REF_OBJ_GAP, e.getYInLayout() + d*REF_OBJ_GAP/2);
//										
								}
								else
									System.err.println("not " + o);
							}, true);
						}
					}
					else {
						int dist = target instanceof NullModel ? REF_OBJ_GAP / 2 : REF_OBJ_GAP;
						setLocation(targetE, e.getXInLayout() + dist, e.getYInLayout());
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
		System.out.println("ADD " + entity);
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
