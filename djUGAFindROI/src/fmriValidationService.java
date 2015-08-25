

import java.io.File;
import java.util.*;

import edu.uga.liulab.djVtkBase.djVtkDataDictionary;
import edu.uga.liulab.djVtkBase.djVtkFiberData;
import edu.uga.liulab.djVtkBase.djVtkHybridData;
import edu.uga.liulab.djVtkBase.djVtkPoint;
import edu.uga.liulab.djVtkBase.djVtkSurData;
import edu.uga.liulab.djVtkBase.djVtkUtil;

public class fmriValidationService {

	public void do_trustConsistent()
	{
		List<List<Float>> dataList = new ArrayList<List<Float>>();
		djVtkSurData currentSurData;
		for(int subIndex=0;subIndex<15;subIndex++)
		{
			System.out.println("current dealling with sub"+subIndex);
			List<Float> currentSubInfo = new ArrayList<Float>();
//			int realSubID = (int) findModelDictionary.subRelationM.get(subIndex, 1);
//			djVtkSurData currentSurData = new djVtkSurData("./data/" + realSubID + ".surf.vtk");
//			djVtkFiberData currentFiberData = new djVtkFiberData("./data/" + realSubID + ".asc.fiber.vtk");
//			djVtkHybridData hybridData = new djVtkHybridData(currentSurData, currentFiberData);
//			hybridData.mapSurfaceToBox();
//			hybridData.mapFiberToBox();
			for(int roiIndex=0;roiIndex<16;roiIndex++)
			{
//				int ptIDBefore = (int)findModelDictionary.workMemBefore.get(subIndex, roiIndex);
//				int ptIDAfter = (int)findModelDictionary.workMemAfter.get(subIndex, roiIndex);
//				//calculate the trace-map feature of before optimization
//				Set ptSetBefore = currentSurData.getNeighbourPoints(ptIDBefore, findModelDictionary.EXTRACTING_FIBER_RINGNUM);
//				djVtkFiberData tmpFiberDataBefore = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSetBefore).getCompactData();
//				tmpFiberDataBefore.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
//				// generate trace based on fiber file
//				djVtkPoint tmpPointBefore = currentSurData.getPoint(ptIDBefore);
				fiberBundleService fiberBundleDescriptor = new fiberBundleService();
//				fiberBundleDescriptor.setFiberData(tmpFiberDataBefore);
//				fiberBundleDescriptor.setSeedPnt(tmpPointBefore);
//				fiberBundleDescriptor.createFibersTrace();
//				List<djVtkPoint> allTracePointsList = fiberBundleDescriptor.getAllPoints();
				// calculate the feature
				currentSurData = new djVtkSurData("./data/optiData/roi"+ roiIndex + "/before/12EndRoi"+roiIndex+"Sub"+subIndex+".vtk.Trace.vtk" );
				List<Float> tmpFeatureBefore = fiberBundleDescriptor.calFeatureOfTrace(currentSurData.points);
				if (tmpFeatureBefore.size() != 144) {
					System.out.println("ERROR:tmpFeatureBefore.size is not 144. roiIndex=" + roiIndex);
					System.exit(0);
				}
				//calculate the trace-map feature of after optimization
//				Set ptSetAfter = currentSurData.getNeighbourPoints(ptIDAfter, findModelDictionary.EXTRACTING_FIBER_RINGNUM);
//				djVtkFiberData tmpFiberDataAfter = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSetAfter).getCompactData();
//				tmpFiberDataAfter.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
//				// generate trace based on fiber file
//				djVtkPoint tmpPointAfter = currentSurData.getPoint(ptIDAfter);
//				fiberBundleDescriptor = new fiberBundleService();
//				fiberBundleDescriptor.setFiberData(tmpFiberDataAfter);
//				fiberBundleDescriptor.setSeedPnt(tmpPointAfter);
//				fiberBundleDescriptor.createFibersTrace();
//				allTracePointsList = fiberBundleDescriptor.getAllPoints();
				// calculate the feature
				String dir = "./data/optiData/roi"+ roiIndex + "/after/";
				File filesAfterOpti = new File(dir);
				String[] fileListOfCurrentDir = filesAfterOpti.list();
				List<Float> tmpFeatureAfter = new ArrayList<Float>();
				for(int i=0;i<fileListOfCurrentDir.length;i++)
				{
					if(fileListOfCurrentDir[i].contains("sub"+subIndex) && fileListOfCurrentDir[i].contains(".Trace.vtk") )
					{
						currentSurData = new djVtkSurData(dir+fileListOfCurrentDir[i] );
						tmpFeatureAfter = fiberBundleDescriptor.calFeatureOfTrace(currentSurData.points);
					}
				}
				if (tmpFeatureAfter.size() != 144) {
					System.out.println("ERROR:tmpFeatureAfter.size is not 144. roiIndex=" + roiIndex);
					System.exit(0);
				}
				//calculate the distace
				float dis = fiberBundleDescriptor.calFeatureDis_SD(tmpFeatureBefore, tmpFeatureAfter);
				dis = (float)((int)(dis*10000))/10000;
				currentSubInfo.add(dis);				
			} //for all rois
			dataList.add( currentSubInfo );
//			currentSurData=null;
//			currentFiberData=null;
		} //for all subjects
		djVtkUtil.writeArrayListToFile(dataList, " ", 16, "allDisBetweenOpti.txt");		
	}

}
