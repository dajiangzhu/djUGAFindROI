/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import edu.uga.liulab.djVtkBase.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author dajiang
 */
public class prepareFeature {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("format:java -Xmx3000m -jar *.jar ptsdSubID ringNum(3)");
        } else {
            int nRealSubID = Integer.valueOf(args[0]);
            int nRingNum = Integer.valueOf(args[1]);
            List<List<Float>> featureDataOfCurrentSub = new ArrayList<List<Float>>();

            System.out.println("Extracting fibers+traces with " + nRingNum + " rings from sub-" + nRealSubID + " for all points...");
            String subName = "";
			if (nRealSubID < 10)
				subName = "pd000" + nRealSubID;
			else
				subName = "pd00" + nRealSubID;
            djVtkSurData surData = new djVtkSurData("./PDSD_c_DTI/" + subName + ".surf.topcorrect.vtk");
            djVtkFiberData fiberData = new djVtkFiberData("./PDSD_c_DTI/" + subName + "ascfibers.vtk");
            djVtkHybridData hybridData = new djVtkHybridData(surData, fiberData);
            hybridData.mapSurfaceToBox();
            hybridData.mapFiberToBox();
            int nPtNum = surData.nPointNum;
            for (int i = 0; i < surData.nPointNum; i++) {
                if (i % 100 == 0) {
                    System.out.println("finished " + i + "/"+nPtNum+".");
                }
                //extract fibers
                Set ptSet = surData.getNeighbourPoints(i, nRingNum);
                djVtkFiberData tmpFiberData = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
				tmpFiberData.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
				// generate trace based on fiber file
				djVtkPoint tmpPoint = surData.getPoint(i);
				fiberBundleService fiberBundleDescriptor = new fiberBundleService();
				fiberBundleDescriptor.setFiberData(tmpFiberData);
				fiberBundleDescriptor.setSeedPnt(tmpPoint);
				fiberBundleDescriptor.createFibersTrace();
				List<djVtkPoint> allTracePointsList = fiberBundleDescriptor.getAllPoints();
                List<Float> tmpFeature = fiberBundleDescriptor.calFeatureOfTrace(allTracePointsList);
                if(tmpFeature.size()!=144)
                {
                    System.out.println("ERROR:tmpFeature.size is not 144. i="+i);
                    System.exit(0);
                }
                featureDataOfCurrentSub.add(tmpFeature);
            }
            djVtkUtil.writeArrayListToFile(featureDataOfCurrentSub," ", 144, "./PDSD_c_DTI/"+subName+"_3_featureList.txt");
        }
    }
}
