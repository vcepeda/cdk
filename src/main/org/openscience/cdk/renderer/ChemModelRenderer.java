/*  Copyright (C) 2008-2009  Gilleain Torrance <gilleain.torrance@gmail.com>
 *                2008-2009  Arvid Berg <goglepox@users.sf.net>
 *                     2009  Stefan Kuhn <shk3@users.sf.net>
 *                     2009  Egon Willighagen <egonw@users.sf.net>
*
*  Contact: cdk-devel@list.sourceforge.net
*
*  This program is free software; you can redistribute it and/or
*  modify it under the terms of the GNU Lesser General Public License
*  as published by the Free Software Foundation; either version 2.1
*  of the License, or (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program; if not, write to the Free Software
*  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.openscience.cdk.renderer;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.font.IFontManager;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.BondLength;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.Margin;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.Scale;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.ZoomFactor;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.visitor.IDrawVisitor;

/**
 * A general renderer for {@link IChemModel}s, {@link IReaction}s, and
 * {@link IMolecule}s. The chem object
 * is converted into a 'diagram' made up of {@link IRenderingElement}s. It takes
 * an {@link IDrawVisitor} to do the drawing of the generated diagram. Various
 * display properties can be set using the {@link RendererModel}.<p>
 *
 * This class has several usage patterns. For just painting fit-to-screen do:
 * <pre>
 *   renderer.paintMolecule(molecule, visitor, drawArea)
 * </pre>
 * for painting at a scale determined by the bond length in the RendererModel:
 * <pre>
 *   if (moleculeIsNew) {
 *     renderer.setup(molecule, drawArea);
 *   }
 *   Rectangle diagramSize = renderer.paintMolecule(molecule, visitor);
 *   // ...update scroll bars here
 * </pre>
 * to paint at full screen size, but not resize with each change:
 * <pre>
 *   if (moleculeIsNew) {
 *     renderer.setScale(molecule);
 *     Rectangle diagramBounds = renderer.calculateDiagramBounds(molecule);
 *     renderer.setZoomToFit(diagramBounds, drawArea);
 *     renderer.paintMolecule(molecule, visitor);
 *   } else {
 *     Rectangle diagramSize = renderer.paintMolecule(molecule, visitor);
 *   // ...update scroll bars here
 *   }
 * </pre>
 * finally, if you are scrolling, and have not changed the diagram:
 * <pre>
 *   renderer.repaint(visitor)
 * </pre>
 * will just repaint the previously generated diagram, at the same scale.<p>
 *
 * There are two sets of methods for painting IChemObjects - those that take
 * a Rectangle that represents the desired draw area, and those that return a
 * Rectangle that represents the actual draw area. The first are intended for
 * drawing molecules fitted to the screen (where 'screen' means any drawing
 * area) while the second type of method are for drawing bonds at the length
 * defined by the {@link RendererModel} parameter bondLength.<p>
 *
 * There are two numbers used to transform the model so that it fits on screen.
 * The first is <tt>scale</tt>, which is used to map model coordinates to
 * screen coordinates. The second is <tt>zoom</tt> which is used to, well,
 * zoom the on screen coordinates. If the diagram is fit-to-screen, then the
 * ratio of the bounds when drawn using bondLength and the bounds of
 * the screen is used as the zoom.<p>
 *
 * So, if the bond length on screen is set to 40, and the average bond length
 * of the model is 2 (unitless, but roughly &Aring;ngstrom scale) then the
 * scale will be 20. If the model is 10 units wide, then the diagram drawn at
 * 100% zoom will be 10 * 20 = 200 in width on screen. If the screen is 400
 * pixels wide, then fitting it to the screen will make the zoom 200%. Since the
 * zoom is just a floating point number, 100% = 1 and 200% = 2.
 *
 * @author maclean
 * @cdk.module renderextra
 */
public class ChemModelRenderer extends AbstractRenderer
  implements IRenderer<IChemModel> {

	/**
	 * Generators specific to reactions
	 */
	private List<IGenerator<IReaction>> reactionGenerators;
	
    /**
     * A renderer that generates diagrams using the specified
     * generators and manages fonts with the supplied font manager.
     *
     * @param generators
     *            a list of classes that implement the IGenerator interface
     * @param fontManager
     *            a class that manages mappings between zoom and font sizes
     */
	public ChemModelRenderer(List<IGenerator<IAtomContainer>> generators, IFontManager fontManager) {
		this.generators = generators;
        this.fontManager = fontManager;
        for (IGenerator generator : generators)
            rendererModel.registerParameters(generator);
    }
	
	public ChemModelRenderer(List<IGenerator<IAtomContainer>> generators, 
	                List<IGenerator<IReaction>> reactionGenerators, 
	                IFontManager fontManager) {
	    this(generators, fontManager);
        for (IGenerator<IReaction> generator : reactionGenerators)
            rendererModel.registerParameters(generator);
        this.reactionGenerators = reactionGenerators;
        this.setup();
	}
	
	/**
	 * Setup the transformations necessary to draw this Chem Model.
	 *
	 * @param chemModel
	 * @param screen
	 */
	public void setup(IChemModel chemModel, Rectangle screen) {
	    this.setScale(chemModel);
	    Rectangle2D bounds = BoundsCalculator.calculateBounds(chemModel);
	    if(bounds != null)
	        this.modelCenter = new Point2d(bounds.getCenterX(), bounds.getCenterY());
	    this.drawCenter = new Point2d(screen.getCenterX(), screen.getCenterY());
	    this.setup();
	}

	/**
	 * Set the scale for an IChemModel. It calculates the average bond length of
	 * the model and calculates the multiplication factor to transform this
	 * to the bond length that is set in the RendererModel.
	 *
	 * @param chemModel
	 */
	public void setScale(IChemModel chemModel) {
	    double bondLength = AverageBondLengthCalculator.calculateAverageBondLength(chemModel);
	    double scale = this.calculateScaleForBondLength(bondLength);

	    // store the scale so that other components can access it
	    this.rendererModel.getParameter(Scale.class).setValue(scale);
	}

    /**
     * Paint an IChemModel using the IDrawVisitor at a scale determined by the
     * bond length in RendererModel.
     *
     * @param chemModel
     *            the chem model to draw
     * @param drawVisitor
     *            the visitor used to draw with
     * @return the rectangular area that the diagram will occupy on screen
     */
    public Rectangle paint(IChemModel chemModel, IDrawVisitor drawVisitor) {

        IMoleculeSet moleculeSet = chemModel.getMoleculeSet();
        IReactionSet reactionSet = chemModel.getReactionSet();

        if (moleculeSet == null && reactionSet != null) {
            return paintReactionSet(reactionSet, drawVisitor);
        }

        if (moleculeSet != null && reactionSet == null) {
            return paintMoleculeSet(moleculeSet, drawVisitor);
        }

        if (moleculeSet != null && reactionSet != null) {
            Rectangle2D totalBounds = BoundsCalculator.calculateBounds(reactionSet);
            totalBounds = totalBounds.createUnion(
                    BoundsCalculator.calculateBounds(moleculeSet));
            this.setupTransformNatural(totalBounds);
            ElementGroup diagram = new ElementGroup();
            for (IReaction reaction : reactionSet.reactions()) {
                diagram.add(this.generateDiagram(reaction));
            }
            diagram.add(this.generateDiagram(moleculeSet));
            this.paint(drawVisitor, diagram);

            // the size of the painted diagram is returned
            return this.convertToDiagramBounds(totalBounds);
        }
        return new Rectangle(0, 0, 0, 0);
    }

	public Rectangle paintReactionSet(
            IReactionSet reactionSet, IDrawVisitor drawVisitor) {
        // total up the bounding boxes
        Rectangle2D totalBounds = new Rectangle2D.Double();
        for (IReaction reaction : reactionSet.reactions()) {
            Rectangle2D modelBounds = BoundsCalculator.calculateBounds(reaction);
            if (totalBounds == null) {
                totalBounds = modelBounds;
            } else {
                totalBounds = totalBounds.createUnion(modelBounds);
            }
        }

        // setup and draw
        this.setupTransformNatural(totalBounds);
        ElementGroup diagram = new ElementGroup();
        for (IReaction reaction : reactionSet.reactions()) {
            diagram.add(this.generateDiagram(reaction));
        }
        this.paint(drawVisitor, diagram);

        // the size of the painted diagram is returned
        return this.convertToDiagramBounds(totalBounds);
    }

	public Rectangle paintReaction(
	        IReaction reaction, IDrawVisitor drawVisitor) {

        // calculate the bounds
        Rectangle2D modelBounds = BoundsCalculator.calculateBounds(reaction);

        // setup and draw
        this.setupTransformNatural(modelBounds);
        IRenderingElement diagram = this.generateDiagram(reaction);
        this.paint(drawVisitor, diagram);

        return this.convertToDiagramBounds(modelBounds);
    }

	public Rectangle paintMoleculeSet(
            IMoleculeSet moleculeSet, IDrawVisitor drawVisitor) {
        // total up the bounding boxes
        Rectangle2D totalBounds = new Rectangle2D.Double();
        for (IAtomContainer molecule : moleculeSet.molecules()) {
            Rectangle2D modelBounds = BoundsCalculator.calculateBounds(molecule);
            if (totalBounds == null) {
                totalBounds = modelBounds;
            } else {
                totalBounds = totalBounds.createUnion(modelBounds);
            }
        }

        // setup and draw
        this.setupTransformNatural(totalBounds);
        ElementGroup diagram = new ElementGroup();
        for (IAtomContainer molecule : moleculeSet.molecules()) {
            diagram.add(this.generateDiagram(molecule));
        }
        this.paint(drawVisitor, diagram);

        return this.convertToDiagramBounds(totalBounds);
    }

	/**
     * Paint a ChemModel.
     *
     * @param chemModel
     * @param drawVisitor the visitor that does the drawing
     * @param bounds the bounds of the area to paint on.
     * @param resetCenter
     *     if true, set the modelCenter to the center of the ChemModel's bounds.
     */
    public void paintChemModel(IChemModel chemModel,
            IDrawVisitor drawVisitor, Rectangle2D bounds, boolean resetCenter) {
        // check for an empty model
        IMoleculeSet moleculeSet = chemModel.getMoleculeSet();
        IReactionSet reactionSet = chemModel.getReactionSet();

        // nasty, but it seems that reactions can be read in as ChemModels
        // with BOTH a ReactionSet AND a MoleculeSet...
        if (moleculeSet == null || reactionSet != null) {
            if (reactionSet != null) {
                paintReactionSet(reactionSet, drawVisitor, bounds, resetCenter);
            }
            return;
        }

        // calculate the total bounding box
        Rectangle2D modelBounds = BoundsCalculator.calculateBounds(moleculeSet);

        this.setupTransformToFit(bounds, modelBounds,
                AverageBondLengthCalculator.calculateAverageBondLength(chemModel), resetCenter);

        // generate the elements
        IRenderingElement diagram = this.generateDiagram(moleculeSet);

        // paint it
        this.paint(drawVisitor, diagram);
    }

    /**
     * Paint a set of reactions.
     *
     * @param reaction the reaction to paint
     * @param drawVisitor the visitor that does the drawing
     * @param bounds the bounds on the screen
     * @param resetCenter
     *     if true, set the draw center to be the center of bounds
     */
    public void paintReactionSet(IReactionSet reactionSet,
            IDrawVisitor drawVisitor, Rectangle2D bounds, boolean resetCenter) {

        // total up the bounding boxes
        Rectangle2D totalBounds = null;
        for (IReaction reaction : reactionSet.reactions()) {
            Rectangle2D modelBounds = BoundsCalculator.calculateBounds(reaction);
            if (totalBounds == null) {
                totalBounds = modelBounds;
            } else {
                totalBounds = totalBounds.createUnion(modelBounds);
            }
        }

        this.setupTransformToFit(bounds, totalBounds,
                AverageBondLengthCalculator.calculateAverageBondLength(reactionSet), resetCenter);

        ElementGroup diagram = new ElementGroup();
        for (IReaction reaction : reactionSet.reactions()) {
            diagram.add(this.generateDiagram(reaction));
        }

        // paint them all
        this.paint(drawVisitor, diagram);
    }

    /**
	 * Paint a reaction.
	 *
	 * @param reaction the reaction to paint
	 * @param drawVisitor the visitor that does the drawing
	 * @param bounds the bounds on the screen
	 * @param resetCenter
	 *     if true, set the draw center to be the center of bounds
	 */
	public void paintReaction(IReaction reaction, IDrawVisitor drawVisitor,
            Rectangle2D bounds, boolean resetCenter) {

	    // calculate the bounds
        Rectangle2D modelBounds = BoundsCalculator.calculateBounds(reaction);

        this.setupTransformToFit(bounds, modelBounds,
                AverageBondLengthCalculator.calculateAverageBondLength(reaction), resetCenter);

        // generate the elements
        IRenderingElement diagram = this.generateDiagram(reaction);

        // paint it
        this.paint(drawVisitor, diagram);
    }

	/**
     * Paint a set of molecules.
     *
     * @param reaction the reaction to paint
     * @param drawVisitor the visitor that does the drawing
     * @param bounds the bounds on the screen
     * @param resetCenter
     *     if true, set the draw center to be the center of bounds
     */
    public void paintMoleculeSet(IMoleculeSet molecules,
            IDrawVisitor drawVisitor, Rectangle2D bounds, boolean resetCenter) {

        // total up the bounding boxes
        Rectangle2D totalBounds = null;
        for (IAtomContainer molecule : molecules.molecules()) {
            Rectangle2D modelBounds = BoundsCalculator.calculateBounds(molecule);
            if (totalBounds == null) {
                totalBounds = modelBounds;
            } else {
                totalBounds = totalBounds.createUnion(modelBounds);
            }
        }

        this.setupTransformToFit(bounds, totalBounds,
                AverageBondLengthCalculator.calculateAverageBondLength(molecules), resetCenter);

        ElementGroup diagram = new ElementGroup();
        for (IAtomContainer molecule : molecules.molecules()) {
            diagram.add(this.generateDiagram(molecule));
        }

        this.paint(drawVisitor, diagram);
    }

    /**
     * Repaint using the cached diagram
     *
     * @param drawVisitor the wrapper for the graphics object that draws
     */
    public void repaint(IDrawVisitor drawVisitor) {
        this.paint(drawVisitor, cachedDiagram);
    }

    /**
	 * Given a chem model, calculates the bounding rectangle in screen space.
	 *
	 * @param model the model to draw.
	 * @return a rectangle in screen space.
	 */
	public Rectangle calculateDiagramBounds(IChemModel model) {
        IMoleculeSet moleculeSet = model.getMoleculeSet();
        IReactionSet reactionSet = model.getReactionSet();
        if ((moleculeSet == null && reactionSet == null)) {
            return new Rectangle();
        }

        Rectangle2D moleculeBounds = null;
        Rectangle2D reactionBounds = null;
        if (moleculeSet != null) {
            moleculeBounds = BoundsCalculator.calculateBounds(moleculeSet);
        }
        if (reactionSet != null) {
            reactionBounds = BoundsCalculator.calculateBounds(reactionSet);
        }

        if (moleculeBounds == null) {
            return this.calculateScreenBounds(reactionBounds);
        } else if (reactionBounds == null) {
            return this.calculateScreenBounds(moleculeBounds);
        } else {
            Rectangle2D allbounds = new Rectangle2D.Double();
            Rectangle2D.union(moleculeBounds, reactionBounds, allbounds);
            return this.calculateScreenBounds(allbounds);
        }
    }

	/**
	 * Given a bond length for a model, calculate the scale that will transform
	 * this length to the on screen bond length in RendererModel.
	 *
	 * @param modelBondLength
	 * @param reset
	 * @return
	 */
	protected double calculateScaleForBondLength(double modelBondLength) {
	    if (Double.isNaN(modelBondLength) || modelBondLength == 0) {
            return rendererModel.getParameter(Scale.class).getDefault();
        } else {
            return this.rendererModel.getParameter(BondLength.class)
        		.getValue() / modelBondLength;
        }
	}

    /**
     * Calculate the bounds of the diagram on screen, given the current scale,
     * zoom, and margin.
     *
     * @param modelBounds
     *            the bounds in model space of the chem object
     * @return the bounds in screen space of the drawn diagram
     */
	private Rectangle convertToDiagramBounds(Rectangle2D modelBounds) {
	    double cx = modelBounds.getCenterX();
        double cy = modelBounds.getCenterY();
        double mw = modelBounds.getWidth();
        double mh = modelBounds.getHeight();

        double scale = rendererModel.getParameter(Scale.class).getValue();
        double zoom = rendererModel.getParameter(ZoomFactor.class).getValue();
        
        Point2d mc = this.toScreenCoordinates(cx, cy);

        // special case for 0 or 1 atoms
        if (mw == 0 && mh == 0) {
            return new Rectangle((int)mc.x, (int)mc.y, 0, 0);
        }

        double margin = this.rendererModel
            .getParameter(Margin.class).getValue();
        int w = (int) ((scale * zoom * mw) + (2 * margin));
        int h = (int) ((scale * zoom * mh) + (2 * margin));
        int x = (int) (mc.x - w / 2);
        int y = (int) (mc.y - h / 2);

        return new Rectangle(x, y, w, h);
	}

    private IRenderingElement generateDiagram(IReaction reaction) {
	    ElementGroup diagram = new ElementGroup();
	    
	    for (IGenerator<IReaction> generator : this.reactionGenerators) {
	        diagram.add(generator.generate(reaction, rendererModel));
	    }

	    diagram.add(generateDiagram(reaction.getReactants()));
	    diagram.add(generateDiagram(reaction.getProducts()));

	    return diagram;
	}

	private IRenderingElement generateDiagram(IMoleculeSet moleculeSet) {
	    ElementGroup diagram = new ElementGroup();
        for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
            IAtomContainer ac = moleculeSet.getAtomContainer(i);
            for (IGenerator<IAtomContainer> generator : this.generators) {
                diagram.add(generator.generate(ac, this.rendererModel));
            }
        }
        return diagram;
	}

	public List<IGenerator<IReaction>> getReactionGenerators(){
	    return new ArrayList<IGenerator<IReaction>>(reactionGenerators);
	}
}