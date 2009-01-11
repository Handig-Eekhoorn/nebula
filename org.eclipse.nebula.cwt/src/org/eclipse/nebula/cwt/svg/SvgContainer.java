package org.eclipse.nebula.cwt.svg;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;

public class SvgContainer extends SvgGraphic {

	protected List<SvgElement> elements;
	
	SvgContainer(SvgContainer container, String id) {
		super(container, id);
		elements = new ArrayList<SvgElement>();
	}

	void add(SvgElement element) {
		elements.add(element);
	}
	
	public void apply(GC gc) {
		for(SvgElement element : elements) {
			if(element instanceof SvgGraphic) {
				((SvgGraphic) element).apply(gc);
			}
		}
	}
	
	public SvgElement[] getElements() {
		return elements.toArray(new SvgElement[elements.size()]);
	}

	public boolean isEmpty() {
		return elements.isEmpty();
	}

}