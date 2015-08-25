/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 *
 * @author dajiang
 */

import org.jmat.data.*;
import org.jmat.data.matrixDecompositions.*;
import org.jmat.gui.*;

public class PCA {

    private AbstractMatrix covariance;
	private AbstractMatrix EigenVectors;
	private AbstractMatrix EigenValues;

	public PCA(AbstractMatrix X) {
		covariance = new RandomMatrix(X).covariance();
		EigenvalueDecomposition e = covariance.eig();
		EigenVectors = e.getV();
		EigenValues = e.getD();
	}

	public AbstractMatrix getVectors() {
		return EigenVectors;
	}

	public AbstractMatrix getValues() {
		return EigenValues;
	}
//
//	public static void main(String[] arg) {
//		//construct the matrix X
//		AbstractMatrix x1 = RandomMatrix.normal(100, 1, 0, 1);
//		AbstractMatrix x2 = RandomMatrix.normal(100, 1, 0, 1);
//		AbstractMatrix X = x1.plus(x2).mergeColumns(x2);
//
//		PCA pca = new PCA(X);
//
//		//display a Frame with data in a 2D-Plot and EigenValues and EigenVectors in the command line.
//		new FrameView(X.toPlot2DPanel("[x1+x2,x2]", PlotPanel.SCATTER));
//		pca.getValues().toCommandLine("EigenValues");
//		pca.getVectors().toCommandLine("EigenVectors");
//	}

}
