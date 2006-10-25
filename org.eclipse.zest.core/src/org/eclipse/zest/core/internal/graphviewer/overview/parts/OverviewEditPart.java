/*******************************************************************************
 * Copyright 2005-2006, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Chisel Group, University of Victoria
 *******************************************************************************/
package org.eclipse.mylar.zest.core.internal.graphviewer.overview.parts;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.mylar.zest.core.internal.nestedgraphviewer.policies.NullLayoutEditPolicy;

/**
 * 
 * @author Del Myers
 *
 */
public class OverviewEditPart extends AbstractGraphicalEditPart implements FigureListener {
	/* (non-Javadoc)
	 * @see org.eclipse.mylar.zest.core.internal.graphviewer.parts.GraphEditPart#createEditPolicies()
	 */
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new NullLayoutEditPolicy());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		IFigure figure = new ScalingContainerFigure();
		figure.setLayoutManager(new XYLayout());
		return figure;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
	 */
	public void activate() {
		GraphicalEditPart part = (GraphicalEditPart) getCastedModel().getRootEditPart().getContents();
		IFigure parentPane = part.getFigure();
		parentPane.addFigureListener(this);
		super.activate();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
	 */
	public void deactivate() {
		GraphicalEditPart part = (GraphicalEditPart) getCastedModel().getRootEditPart().getContents();
		IFigure parentPane = part.getFigure();
		parentPane.removeFigureListener(this);
		super.deactivate();
	}
	
	private EditPartViewer getCastedModel() {
		return (EditPartViewer)getModel();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	protected List getModelChildren() {
		GraphicalEditPart part = (GraphicalEditPart) getCastedModel().getRootEditPart().getContents();
		List children = part.getChildren();
		List modelChildren = new LinkedList();
		for (Iterator i = children.iterator(); i.hasNext();) {
			EditPart child = (EditPart) i.next();
			if (!(child instanceof ConnectionEditPart)) {
				if (child instanceof GraphicalEditPart) {
					modelChildren.add(child);
				}
			}
		}
		return modelChildren;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	protected void refreshVisuals() {
		super.refreshVisuals();
		
		Rectangle boundingArea = getBoundingArea();
		ScalingContainerFigure figure = (ScalingContainerFigure) getFigure();
		Dimension logicalSize = boundingArea.getSize();
		if (!figure.getLogicalSize().equals(logicalSize)) {
			figure.setLogicalSize(logicalSize.getCopy());
		}
		
	}
	/**
	 * @return
	 */
	private Rectangle getBoundingArea() {
		GraphicalEditPart part = (GraphicalEditPart) getCastedModel().getRootEditPart().getContents();
		//Dimension logicalSize = part.getFigure().getClientArea().getSize();
		Rectangle boundingArea = null;
		IFigure boundingFigure = part.getFigure();
		for (Iterator i = boundingFigure.getChildren().iterator(); i.hasNext();) {
			IFigure child = (IFigure) i.next();
			if (boundingArea == null) {
				boundingArea = child.getBounds().getCopy();
			} else {
				boundingArea.union(child.getBounds());
			}
		}
		if (boundingArea == null) {
			boundingArea = new Rectangle();
		}
		return boundingArea;
	}

	/**
	 * Translates the rectangle given in absolute coordinates first into coordinates
	 * that correspond to the coordinate system of this part's target viewer.
	 * @param rect
	 */
	public void translateToViewer(Rectangle rect) {
		Rectangle boundingArea = getBoundingArea();
		//translate to the correct sizing.
		getFigure().translateFromParent(rect);
		GraphicalEditPart part = (GraphicalEditPart) getCastedModel().getRootEditPart().getContents();
		IFigure boundingFigure = part.getFigure();
		//calculate the scaling difference between the bounding size and the actual
		//figure size.
		double wScale = (double)boundingFigure.getSize().width/(double)boundingArea.width;
		double hScale = (double)boundingFigure.getSize().height/(double)boundingArea.height;
		rect.scale(wScale, hScale);
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.FigureListener#figureMoved(org.eclipse.draw2d.IFigure)
	 */
	public void figureMoved(IFigure source) {
		refreshVisuals();
	}
}
