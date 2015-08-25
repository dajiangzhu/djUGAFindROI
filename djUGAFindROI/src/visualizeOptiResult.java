/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.IOException;
import org.jmat.data.*;
import edu.uga.liulab.djVtkBase.*;
import java.io.FileWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;

/**
 *
 * @author dajiang
 */
public class visualizeOptiResult {

    public static AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File("./data/subRIndex.txt")); //19*2
    public static AbstractMatrix gridPointM = Matrix.fromASCIIFile(new File("./data/GridPointMapping.txt")); //2056*15
    public static int nGridNum = 2056;
    public static int nFeatureDim = 144;
    public List<Integer> subList = new ArrayList<Integer>();
    public AbstractMatrix optiResultM;
    public Map<Integer, AbstractMatrix> featureMap = new HashMap<Integer, AbstractMatrix>();
    public Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();
    public Map<Integer, djVtkFiberData> fiberDataMap = new HashMap<Integer, djVtkFiberData>();
    public int nGroupID = -1;
    public double[] disBefore = new double[nGridNum + 1];
    public double[] disAfter = new double[nGridNum + 1];
    public double[] disProvement = new double[nGridNum + 1];
    ///
    public List<Integer> resultGridAfter = new ArrayList<Integer>();
    public List<Integer> resultGridProvement = new ArrayList<Integer>();

    public void printMesh(int virSubID, int BeforeOrAfter) { //before-3 after-5
        FileWriter fw = null;
        try {
            int rowID = virSubID - (this.nGroupID - 1) * 5;
            int nRealSubID = (int) visualizeOptiResult.subRelationM.get(virSubID, 1);
            String resultDir = "./OptiResult/";
            fw = new FileWriter(resultDir + nRealSubID + ".BeforeOrAfter" + BeforeOrAfter + ".grid.vtk");
            fw.write("# vtk DataFile Version 3.0\r\n");
            fw.write("vtk output\r\n");
            fw.write("ASCII\r\n");
            fw.write("DATASET POLYDATA\r\n");
            fw.write("POINTS 2056 float\r\n");

            System.out.println("Beging to printMesh with virSubID=" + virSubID);

            djVtkSurData currentSurData = surDataMap.get(nRealSubID);
            if (currentSurData == null) {
                currentSurData = new djVtkSurData("./data/" + nRealSubID + ".surf.vtk");
                surDataMap.put(nRealSubID, currentSurData);
            }
            for (int gridIndex = 1; gridIndex <= visualizeOptiResult.nGridNum; gridIndex++) {
                System.out.println("Now dealing with grid=" + gridIndex);
                int ptIDOfGrid = (int) optiResultM.get((gridIndex - 1) * 5 + rowID, BeforeOrAfter);
                djVtkPoint tmpPoint = currentSurData.getPoint(ptIDOfGrid);
                fw.write(tmpPoint.x + " "
                        + tmpPoint.y + " "
                        + tmpPoint.z + "\r\n");
            }
            // print VERTICES info
            fw.write("VERTICES 2056 " + 2056 * 2 + " \r\n");
            for (int i = 0; i < 2056; i++) {
                fw.write("1 " + i + "\r\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null,
                    ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
        }
        System.out.println("Write file done!");
        System.out.println("That is all!");

    }

    //output fibers and trace of specific sub
    public void outPutFibers(int virSubID) {
        System.out.println("Beging to outPutFibers with virSubID=" + virSubID);
        String resultDir = "./OptiResult/";
        int rowID = virSubID - (this.nGroupID - 1) * 5;
        int nRealSubID = (int) visualizeOptiResult.subRelationM.get(virSubID, 1);
        djVtkSurData currentSurData = surDataMap.get(nRealSubID);
        djVtkFiberData currentFiberData = fiberDataMap.get(nRealSubID);
        if (currentSurData == null) {
            currentSurData = new djVtkSurData("./data/" + nRealSubID + ".surf.vtk");
            surDataMap.put(nRealSubID, currentSurData);
        }
        if (currentFiberData == null) {
            currentFiberData = new djVtkFiberData("./data/" + nRealSubID + ".asc.fiber.vtk");
            fiberDataMap.put(nRealSubID, currentFiberData);
        }
        djVtkHybridData currentHybridData = new djVtkHybridData(currentSurData, currentFiberData);
        currentHybridData.mapSurfaceToBox();
        currentHybridData.mapFiberToBox();
        for (int gridIndex = 1; gridIndex <= visualizeOptiResult.nGridNum; gridIndex++) {
            System.out.println("Now dealing with grid=" + gridIndex);
            int ptIDBefore = (int) optiResultM.get((gridIndex - 1) * 5 + rowID, 3);
            int ptIDAfter = (int) optiResultM.get((gridIndex - 1) * 5 + rowID, 5);
            int gridIndexAfter = (int) optiResultM.get((gridIndex - 1) * 5 + rowID, 1);
            String fiberNameBefore = resultDir + gridIndex + "/" + gridIndex + "." + nRealSubID + ".Before." + ptIDBefore + ".vtk";
            String fiberNameAfter = resultDir + gridIndex + "/" + gridIndex + "." + nRealSubID + ".After." + ptIDAfter + ".vtk";
            currentHybridData.getFibersConnectToPointsSet(currentSurData.getNeighbourPoints(ptIDBefore, 3)).writeToVtkFileCompact(fiberNameBefore);
            currentHybridData.getFibersConnectToPointsSet(currentSurData.getNeighbourPoints(ptIDAfter, 3)).writeToVtkFileCompact(fiberNameAfter);
        }

    }

    public double calTraceDis(List<Double> f1, List<Double> f2) {
        double dis = 0.0;
        if (f1.size() == f2.size()) {
            for (int i = 0; i < f1.size(); i++) {
                dis += Math.pow(Math.abs(f1.get(i) - f2.get(i)), 2);
            }
        } else {
            System.out.println("the size of f1 and f2 are not equal!!");
        }
        return dis;
    }

    public List<Double> getFeature(int realSubID, int ptID) {
        List<Double> newFeature = new ArrayList<Double>();
        AbstractMatrix newFeatureM = this.featureMap.get(realSubID);
        for (int i = 0; i < visualizeOptiResult.nFeatureDim; i++) {
            newFeature.add(newFeatureM.get(ptID, i));
        }
        return newFeature;
    }

    public double disAcrossSubList(List<Integer> ptIDList) {
        double dis = 0.0;
        for (int i = 0; i < ptIDList.size() - 1; i++) {
            int virSubID1 = this.subList.get(i);
            int nRealSubID1 = (int) visualizeOptiResult.subRelationM.get(virSubID1, 1);
            List<Double> f1 = this.getFeature(nRealSubID1, ptIDList.get(i));
            for (int j = i + 1; j < ptIDList.size(); j++) {
                int virSubID2 = this.subList.get(j);
                int nRealSubID2 = (int) visualizeOptiResult.subRelationM.get(virSubID2, 1);
                List<Double> f2 = this.getFeature(nRealSubID2, ptIDList.get(j));
                dis = dis + this.calTraceDis(f1, f2);
            }
        }
        return dis;
    }

    public int[] getNewArray(int dim, int initialValue) {
        int[] newArray = new int[dim];
        for (int i = 0; i < dim; i++) {
            newArray[i] = initialValue;
        }
        return newArray;
    }

    public void findGridWithMoreSimilarity(int topNum) {
        System.out.println("Beging to findGridWithMoreSimilarity ...");
        List<List<Float>> outputToExcel = new ArrayList<List<Float>>(); //just for out put to excel.
        for (int gridIndex = 1; gridIndex <= visualizeOptiResult.nGridNum; gridIndex++) {
            if (gridIndex % 200 == 0) {
                System.out.println("Finished " + gridIndex + " grids...");
            }
            List<Integer> ptListBefore = new ArrayList<Integer>();
            List<Integer> ptListAfter = new ArrayList<Integer>();
            for (int i = 0; i < this.subList.size(); i++) {
                int virSubID = this.subList.get(i);
                int rowID = virSubID - (this.nGroupID - 1) * 5;
                int ptIDBefore = (int) optiResultM.get((gridIndex - 1) * 5 + rowID, 3);
                int ptIDAfter = (int) optiResultM.get((gridIndex - 1) * 5 + rowID, 5);
                ptListBefore.add(ptIDBefore);
                ptListAfter.add(ptIDAfter);
            }
            double dBefore = this.disAcrossSubList(ptListBefore);
            double dAfter = this.disAcrossSubList(ptListAfter);
            this.disBefore[gridIndex] = dBefore;
            this.disAfter[gridIndex] = dAfter;
            this.disProvement[gridIndex] = dBefore - dAfter;
            List<Float> newItem = new ArrayList<Float>();
            newItem.add((float) dBefore);
            newItem.add((float) dAfter);
            outputToExcel.add(newItem);
        } //for all grids

        djVtkUtil.writeArrayListToFile(outputToExcel, " ", 2, this.nGroupID + "_EnergyDifference.txt");


        //find the top grid with minimal disAfter or disProvement
        int[] flagA = this.getNewArray(visualizeOptiResult.nGridNum + 1, 0);
        int[] flagP = this.getNewArray(visualizeOptiResult.nGridNum + 1, 0);
        for (int i = 0; i < topNum; i++) {
            double tmpAfter = 1000.0;
            double tmpProvement = 0.0;
            int gridAfter = -1;
            int gridProvement = -1;

            for (int j = 1; j <= visualizeOptiResult.nGridNum; j++) {
                if (flagA[j] == 0) {
                    if (disAfter[j] < tmpAfter) {
                        tmpAfter = disAfter[j];
                        gridAfter = j;
                    }
                } //if After
                if (flagP[j] == 0) {
                    if (disProvement[j] > tmpProvement) {
                        tmpProvement = disProvement[j];
                        gridProvement = j;
                    }
                } //if After
            } //for
            flagA[gridAfter] = 1;
            flagP[gridProvement] = 1;
            resultGridAfter.add(gridAfter);
            resultGridProvement.add(gridProvement);
        } //for topNum

        System.out.println("Now checking resultGridAfter ...");
        for (int j = 0; j < this.resultGridAfter.size(); j++) {
            if (resultGridAfter.get(j) == -1) {
                System.out.println("ERROR!!!!!!!!resultGridAfter and index=" + j);
            }
        }
        System.out.println("Now checking resultGridProvement ...");
        for (int j = 0; j < this.resultGridProvement.size(); j++) {
            if (resultGridProvement.get(j) == -1) {
                System.out.println("ERROR!!!!!!!!resultGridProvement and index=" + j);
            }
        }

    }

    public void printResultToVtk(int topNum) {
        System.out.println("Beging to printResultToVtk ...");
        String resultDir = "./OptiResult/";
        List<List<Integer>> attriList = new ArrayList<List<Integer>>();
        List<String> attriNameList = new ArrayList<String>();
        for (int i = 0; i < this.subList.size(); i++) {
            List<djVtkPoint> ptsListToPrint = new ArrayList<djVtkPoint>();
            int virSubID = this.subList.get(i);
            int nRealSubID = (int) visualizeOptiResult.subRelationM.get(virSubID, 1);
            djVtkSurData currentSurData = surDataMap.get(nRealSubID);
            if (currentSurData == null) {
                currentSurData = new djVtkSurData("./data/" + nRealSubID + ".surf.vtk");
                surDataMap.put(nRealSubID, currentSurData);
            }

            ptsListToPrint.clear();
            attriList.clear();
            attriNameList.clear();
            List<Integer> monoAttriList = new ArrayList<Integer>();//for allInOne attri
            List<String> monoAttriNameList = new ArrayList<String>();//for allInOne attri
            for (int j = 0; j < this.resultGridAfter.size(); j++) {
                int gridIndex = resultGridAfter.get(j);
//                System.out.println("j=" + j + " and gridIndex=" + gridIndex);
                int rowID = virSubID - (this.nGroupID - 1) * 5;
                int ptIDAfter = (int) optiResultM.get((gridIndex - 1) * 5 + rowID, 5);
                int gridFrom = (int) optiResultM.get((gridIndex - 1) * 5 + rowID, 0);
                djVtkPoint tmpPoint = currentSurData.getPoint(ptIDAfter);
                ptsListToPrint.add(tmpPoint);
                monoAttriList.add(gridFrom);//for allInOne attri
//                List<Integer> newList = this.getNewList(topNum, 0);
//                newList.set(j, gridFrom);
//                attriNameList.add("Grid" + gridFrom);
//                attriList.add(newList);
            }
            attriList.add(monoAttriList);
            monoAttriNameList.add("GridFrom");
            this.writeToPointsVtkFileWithAttribute(resultDir + nRealSubID + "_After_result_top" + topNum + "_oneAttri.vtk", ptsListToPrint, monoAttriNameList, attriList);
//            this.writeToPointsVtkFileWithAttribute(resultDir + nRealSubID + "_After_result_top" + topNum + ".vtk", ptsListToPrint, attriNameList, attriList);

            try {
                ptsListToPrint.clear();
                monoAttriList.clear();
                monoAttriNameList.clear();
                attriList.clear();
                attriNameList.clear();
                for (int j = 0; j < this.resultGridProvement.size(); j++) {
                    int gridIndex = resultGridProvement.get(j);
//                    System.out.println("j=" + j + " and gridIndex=" + gridIndex);
                    int rowID = virSubID - (this.nGroupID - 1) * 5;
//                    System.out.println("j="+j+" , rowID="+rowID+" , gridIndex="+gridIndex);
                    int ptIDAfter = (int) optiResultM.get((gridIndex - 1) * 5 + rowID, 5);
                    int gridFrom = (int) optiResultM.get((gridIndex - 1) * 5 + rowID, 0);
                    djVtkPoint tmpPoint = currentSurData.getPoint(ptIDAfter);
                    ptsListToPrint.add(tmpPoint);
                    monoAttriList.add(gridFrom);//for allInOne attri
//                    List<Integer> newList = this.getNewList(topNum, 0);
//                    newList.set(j, gridFrom);
//                    attriNameList.add("Grid" + gridFrom);
//                    attriList.add(newList);
                }
                attriList.add(monoAttriList);
                monoAttriNameList.add("GridFrom");
                this.writeToPointsVtkFileWithAttribute(resultDir + nRealSubID + "_Provement_result_top" + topNum + "_oneAttri.vtk", ptsListToPrint, monoAttriNameList, attriList);
//                this.writeToPointsVtkFileWithAttribute(resultDir + nRealSubID + "_Provement_result_top" + topNum + ".vtk", ptsListToPrint, attriNameList, attriList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } //for all subs in the subList
    }

    public void tryRoiInTheOriGrids(float searchRadius) {
        System.out.println("Search Radius is : " + searchRadius);
        AbstractMatrix groundTruthM = Matrix.fromASCIIFile(new File("./data/allGroundTruthCoords.txt")); //15*48
        FileWriter fw = null;
        try {
            fw = new FileWriter("Result_tryRoiInTheGrids_" + searchRadius + "_OriDis.txt");
            for (int roiID = 0; roiID < 16; roiID++) {
                fw.write("roiID = " + roiID + " -------------------------------------------------------------------------\r\n");
                System.out.println("roiID=" + roiID + "----------------------------------------------------------------------");
                Map<Integer, Integer> candidateMap = new HashMap<Integer, Integer>();
                Map<Integer, Float> candidateDisMap = new HashMap<Integer, Float>();
                for (int subID = 0; subID < 15; subID++) {
//                System.out.println("subID=" + subID + "************");
                    int nRealSubID = (int) visualizeOptiResult.subRelationM.get(subID, 1);
                    djVtkPoint truthPoint = new djVtkPoint();
                    truthPoint.x = (float) groundTruthM.get(subID, roiID * 3);
                    truthPoint.y = (float) groundTruthM.get(subID, roiID * 3 + 1);
                    truthPoint.z = (float) groundTruthM.get(subID, roiID * 3 + 2);
                    djVtkSurData currentSurData = this.surDataMap.get(nRealSubID);
                    if (currentSurData == null) {
                        currentSurData = new djVtkSurData("./data/" + nRealSubID + ".surf.vtk");
                        this.surDataMap.put(nRealSubID, currentSurData);
                    }
                    for (int gridIndex = 0; gridIndex < visualizeOptiResult.nGridNum; gridIndex++) {
                        int gridPointID = (int) visualizeOptiResult.gridPointM.get(gridIndex, subID);
                        djVtkPoint gridPoint = currentSurData.getPoint(gridPointID);

                        float dis = djVtkUtil.calDistanceOfPoints(truthPoint, gridPoint);
                        if (dis <= searchRadius) {
                            int grid = gridIndex + 1;
//                        System.out.println("GridID=" + grid);
                            if (candidateMap.containsKey(grid)) {
                                int count = candidateMap.get(grid);
                                float preDis = candidateDisMap.get(grid);
                                candidateMap.remove(grid);
                                candidateDisMap.remove(grid);
                                candidateMap.put(grid, count + 1);
                                candidateDisMap.put(grid, dis + preDis);
                            } else {
                                candidateMap.put(grid, 1);
                                candidateDisMap.put(grid, 0.0f);
                            }
                        }
                    } //for all points in the grid file
                } //for all subs
                //output the map result


                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
                Iterator itCandidates = candidateMap.keySet().iterator();
                while (itCandidates.hasNext()) {
                    int grid = Integer.valueOf(itCandidates.next().toString());
                    int convergeCount = candidateMap.get(grid);
                    System.out.println(candidateMap.get(grid) + " subs have " + "GRID : " + grid);
                    if (convergeCount >= 10) {
                        fw.write(convergeCount + " subs have " + "GRID : " + grid + " with distance : " + candidateDisMap.get(grid) / convergeCount + "\r\n");
                    }

                }
                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");

            } //for all rois
        } catch (IOException ex) {
            Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null,
                    ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
        }

    }

    public void tryRoiInTheGrids(float searchRadius) {
        System.out.println("Search Radius is : " + searchRadius);
        AbstractMatrix groundTruthM = Matrix.fromASCIIFile(new File("./data/allGroundTruthCoords.txt")); //15*48
        FileWriter fw = null;
        try {
            fw = new FileWriter("Result_tryRoiInTheGrids_" + searchRadius + "_dis.txt");
            for (int roiID = 0; roiID < 16; roiID++) {
                fw.write("roiID = " + roiID + " -------------------------------------------------------------------------\r\n");
                System.out.println("roiID=" + roiID + "----------------------------------------------------------------------");
                Map<Integer, Integer> candidateMap = new HashMap<Integer, Integer>();
                Map<Integer, Float> candidateDisMap = new HashMap<Integer, Float>();
                for (int subID = 0; subID < 15; subID++) {
//                System.out.println("subID=" + subID + "************");
                    int nRealSubID = (int) visualizeOptiResult.subRelationM.get(subID, 1);
                    djVtkPoint truthPoint = new djVtkPoint();
                    truthPoint.x = (float) groundTruthM.get(subID, roiID * 3);
                    truthPoint.y = (float) groundTruthM.get(subID, roiID * 3 + 1);
                    truthPoint.z = (float) groundTruthM.get(subID, roiID * 3 + 2);
                    String roiCandidateFileName = "./OptiResult/" + nRealSubID + "_After_result_top1000_oneAttri.vtk";
                    djVtkSurData roiCandidateData = new djVtkSurData(roiCandidateFileName);
                    for (int ptID = 0; ptID < roiCandidateData.nPointNum; ptID++) {
                        float dis = djVtkUtil.calDistanceOfPoints(truthPoint, roiCandidateData.getPoint(ptID));
                        if (dis <= searchRadius) {
                            int grid = Integer.valueOf(roiCandidateData.pointsScalarData.get("GridFrom").get(ptID));
//                        System.out.println("GridID=" + grid);
                            if (candidateMap.containsKey(grid)) {
                                int count = candidateMap.get(grid);
                                float preDis = candidateDisMap.get(grid);
                                candidateMap.remove(grid);
                                candidateDisMap.remove(grid);
                                candidateMap.put(grid, count + 1);
                                candidateDisMap.put(grid, dis + preDis);
                            } else {
                                candidateMap.put(grid, 1);
                                candidateDisMap.put(grid, 0.0f);
                            }
                        }
                    } //for all points in the grid file                        
                } //for all subs    
                //output the map result


                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
                Iterator itCandidates = candidateMap.keySet().iterator();
                while (itCandidates.hasNext()) {
                    int grid = Integer.valueOf(itCandidates.next().toString());
                    int convergeCount = candidateMap.get(grid);
                    System.out.println(candidateMap.get(grid) + " subs converge to " + "GRID : " + grid);
                    if (convergeCount >= 10) {
                        fw.write(convergeCount + " subs converge from " + "GRID : " + grid + " with distance : " + candidateDisMap.get(grid) / convergeCount + "\r\n");
                    }

                }
                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");

            } //for all rois 
        } catch (IOException ex) {
            Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null,
                    ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
        }
    }

//    public static void main(String[] args) throws IOException {
//        if (args.length < 1) {
//            System.out.println("format:java -Xmx3000m -jar *.jar searchRadius");
//
//        } else {
//            visualizeOptiResult mainHandler = new visualizeOptiResult();
////            mainHandler.tryRoiInTheGrids(Float.valueOf(args[0]));
//            mainHandler.tryRoiInTheOriGrids(Float.valueOf(args[0]));
//
//        } //else, means valid inputs
//    } //main
    public void calAllMoveDis() throws IOException {

        FileWriter fw = new FileWriter("allSeedMoveDis.txt");

        AbstractMatrix seedBeforeM = Matrix.fromASCIIFile(new File("./data/seedIndex.txt"));
        AbstractMatrix seedAfterM = Matrix.fromASCIIFile(new File("./data/seedAfterOptimized.txt"));
        for (int subID = 0; subID < 15; subID++) {
            int nRealSubID = (int) visualizeOptiResult.subRelationM.get(subID, 1);
            String printStr = "sub" + subID + ": ";
            djVtkSurData currentSurData = new djVtkSurData("./data/" + nRealSubID + ".surf.vtk");
            for (int roiID = 0; roiID < 16; roiID++) {
                int seedBefore = (int) seedBeforeM.get(subID, roiID);
                int seedAfter = (int) seedAfterM.get(subID, roiID);
                djVtkPoint ptBefore = currentSurData.getPoint(seedBefore);
                djVtkPoint ptAfter = currentSurData.getPoint(seedAfter);
                float dis = djVtkUtil.calDistanceOfPoints(ptBefore, ptAfter);
                fw.write(dis + " ");
                printStr = printStr + " (roiID-" + roiID + ")" + dis + "  ";
            }
            System.out.println(printStr);
            fw.write("\r\n");
        }
        fw.close();
    }

    public static void main(String[] args) throws IOException {
//        visualizeOptiResult mainHandler = new visualizeOptiResult();
//        mainHandler.calAllMoveDis();
        if (args.length < 3) {
//            System.out.println("format:java -Xmx3000m -jar *.jar groupNum(1,2,3) topNum");
            System.out.println("format:java -Xmx3000m -jar *.jar groupID virSubID beforeOrAfter(3-before 5-after)");

        } else {
            visualizeOptiResult mainHandler = new visualizeOptiResult();
            mainHandler.nGroupID = Integer.valueOf(args[0]);
//            for (int i = (mainHandler.nGroupID - 1) * 5; i < mainHandler.nGroupID * 5; i++) {
//                mainHandler.subList.add(i);
//            }
//            //initial optiResultM,feature
//            System.out.println("Initializing...");
            mainHandler.optiResultM = Matrix.fromASCIIFile(new File("./data/optimizationResult_" + mainHandler.nGroupID + "_all.txt"));
//            mainHandler.optiResultM = Matrix.fromASCIIFile(new File("./data/optimizationResult_" + mainHandler.nGroupID + "_all_chInitial.txt"));

//            for (int i = 0; i < mainHandler.subList.size(); i++) {
//                int nRealSubID = (int) visualizeOptiResult.subRelationM.get(mainHandler.subList.get(i), 1);
//                djVtkSurData currentSurData = new djVtkSurData("./data/" + nRealSubID + ".surf.vtk");
//                mainHandler.surDataMap.put(nRealSubID, currentSurData);
//                AbstractMatrix newFeatureM = Matrix.fromASCIIFile(new File("./data/" + nRealSubID + "_3_featureList.txt"));
//                mainHandler.featureMap.put(nRealSubID, newFeatureM);
//            }
//            System.out.println("Initializing done");
//
//            //do sth
//            int topNum = Integer.valueOf(args[1]);
//            mainHandler.findGridWithMoreSimilarity(topNum);
//            mainHandler.printResultToVtk(topNum);

//            mainHandler.outPutFibers(Integer.valueOf(args[1]));

            mainHandler.printMesh(Integer.valueOf(args[1]), Integer.valueOf(args[2]));

        } //else, means valid inputs
    } //main

    public void writeToPointsVtkFileWithAttribute(String fileName, List<djVtkPoint> ptList, List<String> attriNameList, List<List<Integer>> attriList) {
        System.out.println("Begin to write file:" + fileName + "...");
        FileWriter fw = null;
        try {
            fw = new FileWriter(fileName);
            fw.write("# vtk DataFile Version 3.0\r\n");
            fw.write("vtk output\r\n");
            fw.write("ASCII\r\n");
            fw.write("DATASET POLYDATA\r\n");
            // print points info
            fw.write("POINTS " + ptList.size() + " float\r\n");
            for (int j = 0; j < ptList.size(); j++) {
                fw.write(ptList.get(j).x + " "
                        + ptList.get(j).y + " "
                        + ptList.get(j).z + "\r\n");
            }
            // print VERTICES info
            fw.write("VERTICES " + ptList.size() + " " + ptList.size() * 2 + " \r\n");
            for (int i = 0; i < ptList.size(); i++) {
                fw.write("1 " + i + "\r\n");
            }

            if (attriNameList.size() != 0) {
                fw.write("POINT_DATA " + ptList.size() + " \r\n");
                for (int i = 0; i < attriList.size(); i++) {
                    fw.write("SCALARS " + attriNameList.get(i) + " float  \r\n");
                    fw.write(" LOOKUP_TABLE default  \r\n");
                    for (int j = 0; j < attriList.get(i).size(); j++) {
                        fw.write(attriList.get(i).get(j) + "\r\n");
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null,
                    ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
        }
        System.out.println("Write file done!");
        System.out.println("That is all!");
    }

    public List<Integer> getNewList(int dim, int value) {
        List<Integer> newList = new ArrayList<Integer>();
        for (int i = 0; i < dim; i++) {
            newList.add(value);
        }
        return newList;
    }
}
