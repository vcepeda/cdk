/*
 *  Copyright (C) 2004-2005  The Chemistry Development Kit (CDK) project
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
 */

package org.openscience.cdk.qsar.model.R;

/**
 * A class that wraps the return value from the R function, predict.lm.
 *
 * This is an internal class used by R to return the result of
 * the call to <a href="http://stat.ethz.ch/R-manual/R-patched/library/nnet/html/predict.nnet.html">predict.nnet</a>.
 * As a result it should not be instantiated by the user. The actual modeling
 * class, <code>CNNRegressionModel</code>, provides acess to the various
 * fields of this object.
 * 
 *
 * @author Rajarshi Guha
 * @cdk.require r-project
 * @cdk.module qsar
 */
public class CNNRegressionModelPredict {
    private int noutput;
    private double[][] predval;

    private double[][] vectorToMatrix(double[] v, int nrow, int ncol) {
        double[][] m = new double[nrow][ncol];
        for (int i = 0; i < ncol; i++) {
            for (int j = 0; j < nrow; j++) {
                m[j][i] = v[j + i*nrow];
            }
        }
        return(m);
    }

    public CNNRegressionModelPredict(int noutput, double[] values) { 
        this.noutput = noutput;
        int nrow = values.length / noutput;
        setPredicted(vectorToMatrix(values,nrow,noutput));
    }

    public double[][] getPredicted() { return(this.predval); }
    public void setPredicted(double[][] predicted) { 
        this.predval = new double[predicted.length][this.noutput];
        for (int i = 0; i < predicted.length; i++) {
            for (int j = 0; j < this.noutput; j++) {
                this.predval[i][j] = predicted[i][j];
            }
        }
    }
}


