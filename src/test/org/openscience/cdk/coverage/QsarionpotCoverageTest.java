/* $Revision$ $Author$ $Date$
 * 
 * Copyright (C) 2007-2008  Egon Willighagen <egonw@users.sf.net>
 * 
 * Contact: cdk-devel@lists.sourceforge.net
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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA. 
 */
package org.openscience.cdk.coverage;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TestSuite that tests whether all public methods in the core
 * module are tested. Unlike Emma, it does not test that all code is
 * tested, just all methods.
 *
 * @cdk.module test-qsarionpot
 */
public class QsarionpotCoverageTest extends CoverageAnnotationTest {

    private final static String CLASS_LIST = "qsarionpot.javafiles";
    
    @BeforeClass public static void setUp() throws Exception {
        loadClassList(CLASS_LIST, QsarionpotCoverageTest.class.getClassLoader());
    }

    @Test public void testCoverage() {
        super.runCoverageTest();
    }

    @Test public void testForMissingTestClassAnnotation() {
        Assert.assertTrue(super.runTestClassMissingTest());
    }

}
