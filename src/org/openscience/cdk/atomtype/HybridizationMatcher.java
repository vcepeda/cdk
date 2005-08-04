/*  $RCSfile$
 *  $Author$
 *  $Date$
 *  $Revision$
 *
 *  Copyright (C) 2005  The Chemistry Development Kit (CDK) project
 *
 *  Contact: cdk-devel@lists.sourceforge.net
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
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package org.openscience.cdk.atomtype;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomType;
import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.tools.LoggingTool;

/**
 * AtomTypeMatcher that finds an AtomType by matching the Atom's element symbol,
 * formal charge and hybridization state.
 *
 * @author         egonw
 * @cdk.created    2005-04-15
 * @cdk.module     core
 */
public class HybridizationMatcher implements AtomTypeMatcher {

	private static AtomTypeFactory factory = null;
    private LoggingTool logger;
    
	/**
	 * Constructor for the HybridizationMatcher object.
	 */
	public HybridizationMatcher() {
		logger = new LoggingTool(this);
        if (factory == null) {
            try {
                factory = AtomTypeFactory.getInstance("org/openscience/cdk/config/data/hybridization_atomtypes.xml");
            } catch (Exception ex1) {
                logger.error(ex1.getMessage());
                logger.debug(ex1);
            }
        }
	}


	/**
	 * Finds the AtomType matching the Atom's element symbol, formal charge and 
     * hybridization state.
	 *
	 * @param  atomContainer  AtomContainer
	 * @param  atom            the target atom
	 * @exception CDKException Exception thrown if something goed wrong
	 * @return                 the matching AtomType
	 */
	public AtomType findMatchingAtomType(AtomContainer atomContainer, Atom atom) throws CDKException {
        AtomType[] types = factory.getAtomTypes(atom.getSymbol());
        for (int i=0; i<types.length; i++) {
            AtomType type = types[i];
            logger.debug("   ... matching atom ", atom, " vs ", type);
            int charge = atom.getFormalCharge();
            if (charge == type.getFormalCharge()) {
                logger.debug("     formal charge matches...");
                if (atom.getHybridization() == type.getHybridization()) {
                    logger.debug("     hybridization is OK... We have a match!");
                    return type;
                }
            } else {
                logger.debug("     formal charge does NOT match...");
            }
        }
        logger.debug("    No Match");
        
        return null;
	}
}

