/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import org.jmat.data.*;
import edu.uga.liulab.djVtkBase.*;
import java.util.*;

/**
 *
 * @author dajiang
 */
public class changeInitialGridPoints {

    public static AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File("./fig2_v2/data/subRIndex.txt")); //19*2
    public static AbstractMatrix gridPointM = Matrix.fromASCIIFile(new File("./fig2_v2/data/GridPointMapping.txt")); //2056*15
    public Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();

    private void initialSurDataMap() {
        for (int subID = 0; subID < 5; subID++) {
            int nRealSubID = (int) changeInitialGridPoints.subRelationM.get(subID, 1);
            djVtkSurData currentSurData = new djVtkSurData("./fig2_v2/data/workingmem/" + nRealSubID + ".sur.asc.to10.vtk");
            this.surDataMap.put(nRealSubID, currentSurData);
        }
    }

//    public static void main(String[] args) throws Exception {
//        System.out.println("This is changeInitialGridPoints...");
//
//        int nDOF = 3;
//        int nGridNum = 2056;
//
//        changeInitialGridPoints mainFlow = new changeInitialGridPoints();
//        mainFlow.initialSurDataMap();
//
//        for (int subIndex = 0; subIndex < 15; subIndex++) { //for all subs, one line
//            System.out.println("dealing with " + subIndex + " sub...");
//            int nRealSubID = (int) subRelationM.get(subIndex, 1);
//            List<djVtkPoint> currentPointList = new ArrayList<djVtkPoint>();
//            for (int gridIndex = 1; gridIndex <= nGridNum; gridIndex++) { //for all grid   
////                System.out.println("dealing with " + gridIndex + " gridIndex...");
//                int oriGridPtID = (int) gridPointM.get(gridIndex - 1, subIndex);
//                if (mainFlow.surDataMap.get(nRealSubID).getNeighbourPtIDsOnSpecificRing(oriGridPtID, nDOF).iterator().hasNext()) {
//                    int newGridPtID = mainFlow.surDataMap.get(nRealSubID).getNeighbourPtIDsOnSpecificRing(oriGridPtID, nDOF).iterator().next();
//                    currentPointList.add(mainFlow.surDataMap.get(nRealSubID).getPoint(newGridPtID));
//                }
//
//            } // for all grid
//            String fileName = nRealSubID + ".BeforeOrAfter3.chInitial.grid.vtk";
//            djVtkUtil.writeToPointsVtkFile(fileName, currentPointList);
//        } //for all subs
//
//    }
    public static void main(String[] args) throws Exception {
        System.out.println("This is changeInitialGridPoints...");

        int nDOF = 3;
        int nGridNum = 2056;

        List<List<String>> allGridOfSubs = new ArrayList<List<String>>();
        changeInitialGridPoints mainFlow = new changeInitialGridPoints();
        mainFlow.initialSurDataMap();

        for (int gridIndex = 1; gridIndex <= nGridNum; gridIndex++) { //for all grid     
            if (gridIndex % 100 == 0) {
                System.out.println("finished " + gridIndex + " grids...");
            }
            List<String> gridOfCurrentSub = new ArrayList<String>();
            for (int subIndex = 0; subIndex < 5; subIndex++) { //for all subs, one line
                int nRealSubID = (int) subRelationM.get(subIndex, 1);
                int oriGridPtID = (int) gridPointM.get(gridIndex - 1, subIndex);
                int newGridPtID;
                if( mainFlow.surDataMap.get(nRealSubID).getNeighbourPtIDsOnSpecificRing(oriGridPtID, nDOF).iterator().hasNext() )
                 newGridPtID = mainFlow.surDataMap.get(nRealSubID).getNeighbourPtIDsOnSpecificRing(oriGridPtID, nDOF).iterator().next();
                else
                	newGridPtID = oriGridPtID;
                gridOfCurrentSub.add(String.valueOf( newGridPtID ));
            } //for all subs
            allGridOfSubs.add(gridOfCurrentSub);
        } // for all grid
        djVtkUtil.writeStringArrayListToFile(allGridOfSubs, " ", 5, "GridPointMapping_CHInitial_2.txt");

    }
}
