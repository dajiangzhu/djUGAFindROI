

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import edu.uga.liulab.djVtkBase.djVtkPoint;
import edu.uga.liulab.djVtkBase.djVtkSurData;
import edu.uga.liulab.djVtkBase.djVtkUtil;

public class landMarkFigure {
	
	AbstractMatrix coordWorkingMem = Matrix.fromASCIIFile(new File("./bigPaperResult/data/allGroundTruthCoords.txt"));
	AbstractMatrix roiM = Matrix.fromASCIIFile(new File("./bigPaperResult/data/all358Result.txt"));
	List<List<String>> dataInfoList = landMarkUtil.loadData("DataDescriptor.txt", " ");
	
	/**
	 * Note: here shoud use the surface before registration!!!
	 */
	public void geneFunctionalRoiPtIndex()
	{
		List<String> roiPtList = new ArrayList<String>();
		int roiNum = coordWorkingMem.getColumnDimension()/3;
		int subNum = coordWorkingMem.getRowDimension();
		for(int i=0;i<subNum;i++)
		{
			String subID=dataInfoList.get(i).get(0).split("_")[0];
			djVtkSurData surData = new djVtkSurData("./data/"+subID+".surf.vtk");
			for(int j=0;j<roiNum;j++)
			{
				djVtkPoint tmpPt = new djVtkPoint();
				tmpPt.x = (float)coordWorkingMem.get(i, j*3) ;
				tmpPt.y = (float)coordWorkingMem.get(i, j*3+1) ;
				tmpPt.z = (float)coordWorkingMem.get(i, j*3+2) ;
				int cloestPtIndex = surData.findCloestPt(tmpPt);
				
				String tmpStr;
				if(roiPtList.size()>j)
				{
					tmpStr = roiPtList.get(j);
					tmpStr += cloestPtIndex+" ";
					roiPtList.set(j, tmpStr);
				}
				else
				{
					tmpStr = cloestPtIndex+" ";
					roiPtList.add(tmpStr);
				}					
			} //for all rois
		} //for all subjects
		djVtkUtil.writeArrayListToFile(roiPtList, "./bigPaperResult/data/workingMemGroundTruthPt.txt");
	}
	
	public void geneWorkingMemVtk() {
		AbstractMatrix roiM = Matrix.fromASCIIFile(new File("./bigPaperResult/data/workingMemGroundTruthPt.txt"));
		List<List<String>> dataInfoList = landMarkUtil.loadData("DataDescriptor.txt", " ");

		for (int colIndex = 0; colIndex < 15; colIndex++) {
			djVtkSurData surData = new djVtkSurData(dataInfoList.get(colIndex).get(1));
			List<djVtkPoint> ptList = new ArrayList<djVtkPoint>();
			for (int rowIndex = 0; rowIndex < 16; rowIndex++) {
				int ptID = (int) roiM.get(rowIndex, colIndex);
				djVtkPoint tmpPt = surData.getPoint(ptID);
				ptList.add(tmpPt);
			} // for all rows
			djVtkUtil.writeToPointsVtkFile("./bigPaperResult/WorkingMem_" + colIndex + ".vtk", ptList);
		} // for all columns
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		landMarkFigure main = new landMarkFigure();
		main.geneWorkingMemVtk();

	}

}
