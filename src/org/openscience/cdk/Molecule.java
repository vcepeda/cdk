/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 1997-2002  The Chemistry Development Kit (CDK) project
 * 
 * Contact: steinbeck@ice.mpg.de, gezelter@maul.chem.nd.edu, egonw@sci.kun.nl
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 
 */
package org.openscience.cdk;

import java.util.Vector;

/**
 * Represents the concept of a chemical molecule, an object composed of 
 * atoms connected by bonds.
 *
 * @author     steinbeck
 * @created    October 2, 2000
 *
 * @keyword    molecule
 */
public class Molecule extends AtomContainer {

	/**
	 *  Creates an Molecule without Atoms and Bonds.
	 */
	public Molecule() {
		super();
	}

	/**
	 *  Constructor for the Molecule object. The parameters define the
     *  initial capacity of the arrays.
	 *
	 * @param  atomCount  init capacity of Atom array
	 * @param  bondCount  init capacity of Bond array
	 */
	public Molecule(int atomCount, int bondCount)
	{
		super(atomCount, bondCount);
	}

	/**
	 * Constructs a Molecule with
	 * a shallow copy of the atoms and bonds of an AtomContainer.
	 *
	 * @param   ac  An Molecule to copy the atoms and bonds from
	 */
	public Molecule(AtomContainer ac)
	{
		super(ac);
	}

       /**
         * Clones this molecule object.
         *
         * @return  The cloned molecule object
         */
        public Object clone()
        {
                Object o = null;
                try
                {
                        o = super.clone();
                }
                catch (Exception e)
                {
                        e.printStackTrace(System.err);
                }
                return o;
        }
}


