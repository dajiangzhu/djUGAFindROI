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
import net.sf.javaml.distance.NormDistance;

/**
 *
 * @author dajiang
 */
public class optimizeGrids {

    public static AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File("./data/subRIndex.txt")); //19*2
    public static AbstractMatrix pdsdSubRelationM = Matrix.fromASCIIFile(new File("./PDSD_c_DTI/pdsdSubRIndex.txt")); //45*1
    public static AbstractMatrix gridPointM = Matrix.fromASCIIFile(new File("./data/GridPointMapping.txt")); //2056*15
    public static AbstractMatrix gridPoint_chInitial_1_M = Matrix.fromASCIIFile(new File("./data/GridPointMapping_CHInitial_1.txt")); //2056*15
    public static AbstractMatrix gridPoint_PTSD_1_5 = Matrix.fromASCIIFile(new File("./PDSD_c_DTI/GridPointMappingPTSD_1_5.txt")); //2056*5
    public static int nTempSub = 19;
    public static int nGridNum = 2056;
    public static int ringScale = 3;
    public static int nFeatureDim = 144;
    public static int nClusterNum = 5;
    public static int nIteration = 100;
    /////////////////////////////////////
    public List<Integer> subList = new ArrayList<Integer>();
    public int nSubNum;
    public int nGrindMin;
    public int nGrindMax;
    public int nMoveRange;
    public int nMaxCandidateNum = 80;
    public int nAnathreshold = 3;
    public Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();
    //////////////////////////////////
    public double[][][][] disDictionary;
    public List<List<gridCandidate>> allOptiResult = new ArrayList<List<gridCandidate>>();

    private double calFeatureDis(List<Double> f1, List<Double> f2) {
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

    private void fillDisDictionary(List<List<gridCandidate>> allCandidateList) {
        disDictionary = new double[allCandidateList.size()][this.nMaxCandidateNum][allCandidateList.size()][this.nMaxCandidateNum];
        for (int i = 0; i < allCandidateList.size(); i++)//5
        {
            System.out.println("calculating the distance of  sub" + i + " to other subs... ");
            for (int j = 0; j < allCandidateList.get(i).size(); j++)//mostly 0~4
            {
                for (int m = i + 1; m < allCandidateList.size(); m++) {
                    for (int n = 0; n < allCandidateList.get(m).size(); n++) {
                        gridCandidate c1 = allCandidateList.get(i).get(j);
                        gridCandidate c2 = allCandidateList.get(m).get(n);
                        disDictionary[i][j][m][n] = this.calFeatureDis(c1.feature, c2.feature);
                    } //for n
                } //for m
            } //for j
        } //for i

    }

    private double calDisOfGroup(List<Integer> subCombination) {
        double sumDis = 0.0;
        for (int i = 0; i < subCombination.size() - 1; i++) {
            for (int j = i + 1; j < subCombination.size(); j++) {
                sumDis += disDictionary[i][subCombination.get(i)][j][subCombination.get(j)];
            }
        }
        return sumDis;
    }

    public List<gridCandidate> optimizeExamplers(List<List<gridCandidate>> allCandidateList) {
        List<gridCandidate> optiResultOfCurrentGrid = new ArrayList<gridCandidate>();
        this.fillDisDictionary(allCandidateList);
        int size = allCandidateList.size();
        List<Integer> subCombination = new ArrayList<Integer>();
        List<Integer> subCombinationWithMinDis = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            subCombination.add(0);
        }
        int[] limit = new int[size];
        for (int i = 0; i < size; i++) {
            limit[i] = allCandidateList.get(i).size();
        }

        double disMin = 1000.0;
        double tmpDis;
        while (subCombination.get(0) < limit[0]) {
            if (subCombination.get(0) < limit[0]) {
                //System.out.println(subCombination);
                tmpDis = this.calDisOfGroup(subCombination);
                if (tmpDis < disMin) {
                    disMin = tmpDis;
                    subCombinationWithMinDis.clear();
                    subCombinationWithMinDis.addAll(subCombination);
                }
            }
            subCombination.set(size - 1, subCombination.get(size - 1) + 1);
            for (int i = size - 1; i > 0; i--) {
                if (subCombination.get(i) < limit[i]) {
                    break;
                } else {
                    subCombination.set(i, 0);
                    subCombination.set(i - 1, subCombination.get(i - 1) + 1);
                    if ((i - 1) == 0) {
                        System.out.println(subCombination);
                    }
                }
            } //for
        } //while

        for (int i = 0; i < size; i++) {
            gridCandidate optiGridOfCurrentSub = allCandidateList.get(i).get(subCombinationWithMinDis.get(i));
            optiGridOfCurrentSub.findOptiGridBasedOnOptiPt();
            optiResultOfCurrentGrid.add(optiGridOfCurrentSub);
        }
        return optiResultOfCurrentGrid;
    }

    public List<gridCandidate> findCandidates(String wekaFile, List<List<Double>> neighboursFeatureList, List<Integer> neighboursPtIDList, int subID, int gridID, int ptID) throws IOException {
        List<gridCandidate> candidateList = new ArrayList<gridCandidate>();
        Set<Integer> candidateIDSet = new HashSet<Integer>();
        Dataset data = FileHandler.loadDataset(new File(wekaFile), ",");

        // if data size larger than the num of cluster
        if (neighboursPtIDList.size() > optimizeGrids.nClusterNum) {
            Clusterer km = new KMeans(optimizeGrids.nClusterNum, optimizeGrids.nIteration);
            System.out.println("begin to cluster...");
            Dataset[] clusters = km.cluster(data);
            System.out.println("cluster done!!!");

            //find the candidate pointID for all clusters
            for (int i = 0; i < clusters.length; i++) {
                int nItemNum = clusters[i].size();
                Instance sumInstance = clusters[i].get(0);
                for (int j = 1; j < clusters[i].size(); j++) {
                    sumInstance = sumInstance.add(clusters[i].get(j));
                }
                sumInstance = sumInstance.divide(nItemNum); //calculate the centroid

                //find the nearest feature(point) to the centroid from all features
                double featureDisMin = 1000.0;
                int featureIndexMin = -1;
                for (int m = 0; m < neighboursFeatureList.size(); m++) {
                    double featureDis = 0.0;
                    for (int n = 0; n < neighboursFeatureList.get(m).size(); n++) {
                        featureDis += Math.pow(Math.abs(sumInstance.value(n) - neighboursFeatureList.get(m).get(n)), 2);
                    }
                    if (featureDis < featureDisMin) {
                        featureDisMin = featureDis;
                        featureIndexMin = m;
                    }
                } // for all items
                if (candidateIDSet.contains(neighboursPtIDList.get(featureIndexMin))) {
                    System.out.println("This candidate is add before:" + neighboursPtIDList.get(featureIndexMin));
                } else { //new candidate
                    candidateIDSet.add(neighboursPtIDList.get(featureIndexMin));
                    gridCandidate newCandidate = new gridCandidate();
                    newCandidate.virSubID = subID;
                    newCandidate.realSubID = (int) optimizeGrids.subRelationM.get(subID, 1);
                    newCandidate.surData = this.surDataMap.get(newCandidate.realSubID);
                    newCandidate.oriGridID = gridID;
                    newCandidate.oriPtID = ptID;
                    newCandidate.optiPtID = neighboursPtIDList.get(featureIndexMin);
                    newCandidate.feature = neighboursFeatureList.get(featureIndexMin);
                    candidateList.add(newCandidate);
                }
            } //for all clusters
        } //if
        else {
            System.out.println("items are less than cluster number!!!");
        }
        return candidateList;
    }

    public void chooseExampler() throws IOException {
        System.out.println("Entering chooseExampler...");
        String OutPutDescriptor = "PTSD"+this.subList.toString()+".txt";

        for (int gridIndex = this.nGrindMin; gridIndex <= this.nGrindMax; gridIndex++) {
            System.out.println("dealing with the " + gridIndex + "th grid----------------------------------------------------------------------------");
            List<List<gridCandidate>> allCandidateList = new ArrayList<List<gridCandidate>>();
            for (int subIndex = 0; subIndex < this.subList.size(); subIndex++) //for subs
            {
                System.out.println("dealing with gird=" + gridIndex + " and sub=" + subIndex);
                List<gridCandidate> candiListOfCurrentSub = new ArrayList<gridCandidate>();
                List<List<Double>> neighboursFeatureList = new ArrayList<List<Double>>();
                int nVirSubID = this.subList.get(subIndex);
                int nRealSubID = (int) pdsdSubRelationM.get(nVirSubID-1, 0);
    			String subName = "";
    			if (nRealSubID < 10)
    				subName = "pd000" + nRealSubID;
    			else
    				subName = "pd00" + nRealSubID;
//                djVtkSurData currentSurData = new djVtkSurData("./data/" + nRealSubID + ".surf.vtk");
                djVtkSurData currentSurData;
                if (this.surDataMap.containsKey(nRealSubID)) {
                    currentSurData = surDataMap.get(nRealSubID);
                } else {
                    currentSurData = new djVtkSurData("./PDSD_c_DTI/" + subName + ".surf.topcorrect.vtk");
                    surDataMap.put(nRealSubID, currentSurData);
                }
                AbstractMatrix featuresOfCurrentSubM = Matrix.fromASCIIFile(new File("./PDSD_c_DTI/" + subName + "_3_featureList.txt")); //2056*15
//                int ptID = (int) gridPointM.get(gridIndex - 1, nVirSubID); //***************modify if chang initial grids**********************
                int ptID = (int) gridPoint_PTSD_1_5.get(gridIndex - 1, nVirSubID-1);

                //prepare neighboursFeatureList
                for (int ptIndex = 0; ptIndex < currentSurData.nPointNum; ptIndex++) {
                    List<Double> tmpPtFeature = new ArrayList<Double>();
                    for (int i = 0; i < optimizeGrids.nFeatureDim; i++) {
                        tmpPtFeature.add(featuresOfCurrentSubM.get(ptIndex, i));
                    }
                    neighboursFeatureList.add(tmpPtFeature);
                }

                //get neighbours based on seedPt=ptID and nMoveRange(default step is 2, the total range is 2*nMoveRange(3 or 4 in general))
                Set<djVtkPoint> candidatePoints = currentSurData.decimatePatch(ptID, 2, this.nMoveRange);
                Iterator itCandidatePoints = candidatePoints.iterator();
                while (itCandidatePoints.hasNext()) {
                    djVtkPoint tmpPoint = (djVtkPoint) itCandidatePoints.next();
                    gridCandidate newCandidate = new gridCandidate();
                    newCandidate.virSubID = nVirSubID;
                    newCandidate.realSubID = (int) pdsdSubRelationM.get(nVirSubID-1, 0);
                    newCandidate.surData = this.surDataMap.get(newCandidate.realSubID);
                    newCandidate.oriGridID = gridIndex;
                    newCandidate.oriPtID = ptID;
                    newCandidate.optiPtID = tmpPoint.pointId;
                    newCandidate.feature = neighboursFeatureList.get(tmpPoint.pointId);
                    candiListOfCurrentSub.add(newCandidate);
                }
                allCandidateList.add(candiListOfCurrentSub);
            } //for all subs

            //begin to optimize
            List<gridCandidate> optiResultOfCurrentGrid = this.optimizeExamplers(allCandidateList);
            allOptiResult.add(optiResultOfCurrentGrid);
        } //for all grids

        //print
        this.printOptiResult(allOptiResult, OutPutDescriptor);
//        this.printOptiResult(this.filterResult(this.nAnathreshold), "threshold" + this.nAnathreshold);
//        int tmpThreshold = this.nAnathreshold + 1;
//        this.printOptiResult(this.filterResult(tmpThreshold), "threshold" + tmpThreshold);

    }

    public List<List<gridCandidate>> filterResult(int threshold) {
        List<List<gridCandidate>> filteredResult = new ArrayList<List<gridCandidate>>();
        for (int i = 0; i < allOptiResult.size(); i++) {
            for (int j = 0; j < allOptiResult.get(i).size(); j++) {
                int count = 1;
                for (int k = j + 1; k < allOptiResult.get(i).size(); k++) {
                    if (allOptiResult.get(i).get(j).optiGridID == allOptiResult.get(i).get(k).optiGridID) {
                        count++;
                    }
                } //for k
                if (count >= threshold) {
                    filteredResult.add(allOptiResult.get(i));
                    break;
                }
            } //for j        
        } //for i    
        return filteredResult;
    }

    public void printOptiResult(List<List<gridCandidate>> resultToPrint, String descriptor) {
        System.out.println("Beging to output the optimiresult...");
        String fileName = "optimizationResult_" + this.nGrindMin + "_" + this.nGrindMax + "_" + descriptor + ".txt";
        FileWriter fw = null;
        try {
            fw = new FileWriter(fileName);
            for (int grid = 0; grid < resultToPrint.size(); grid++) {
                for (int sub = 0; sub < resultToPrint.get(grid).size(); sub++) {
                    gridCandidate currentCandidate = resultToPrint.get(grid).get(sub);
                    // oriGridID+optiGridID+subID+oriGridPointID+optiGridPointID+optiPointID
                    String strInfo = currentCandidate.oriGridID + " " + currentCandidate.optiGridID + " " + currentCandidate.virSubID
                            + " " + currentCandidate.oriPtID + " " + currentCandidate.optiGridPtID + " " + currentCandidate.optiPtID;
                    fw.write(strInfo + "\r\n");
                } //for sub
            } //for grid
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
        System.out.println("Output done!!!");


    }

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.out.println("format:java -Xmx3000m -jar *.jar subNum subList(eg. 0 1 2) gridMin(>=1) gridMax(<=2056) move-range");
        } else {
            //initialization
            int i = 0;
            optimizeGrids optimizeHandler = new optimizeGrids();
            optimizeHandler.nSubNum = Integer.valueOf(args[i]);


            for (i = 1; i
                    < optimizeHandler.nSubNum + 1; i++) {
                optimizeHandler.subList.add(Integer.valueOf(args[i]));
            }
            optimizeHandler.nGrindMin = Integer.valueOf(args[i++]);
            optimizeHandler.nGrindMax = Integer.valueOf(args[i++]);
            optimizeHandler.nMoveRange = Integer.valueOf(args[i++]);

            //Begin to work..
            optimizeHandler.chooseExampler();

        } //else, means valid inputs
    }

    public void chooseExampler_back() throws IOException {
        System.out.println("Entering chooseExampler...");


        for (int gridIndex = this.nGrindMin; gridIndex
                <= this.nGrindMax; gridIndex++) {
            System.out.println("dealing with the " + gridIndex + "th grid...");
            List<List<gridCandidate>> allCandidateList = new ArrayList<List<gridCandidate>>();


            for (int subIndex = 0; subIndex
                    < this.subList.size(); subIndex++) //for subs
            {
                List<List<Double>> neighboursFeatureList = new ArrayList<List<Double>>();
                List<Integer> neighboursPtIDList = new ArrayList<Integer>();


                int nVirSubID = this.subList.get(subIndex);


                int nRealSubID = (int) subRelationM.get(nVirSubID, 1);
                djVtkSurData currentSurData = new djVtkSurData("./data/" + nRealSubID + ".surf.vtk");
                AbstractMatrix featuresOfCurrentSubM = Matrix.fromASCIIFile(new File("./data/" + nRealSubID + "_3_featureList.txt")); //2056*15


                int ptID = (int) gridPointM.get(gridIndex - 1, nVirSubID);

                //get neighbours based on seedPt=ptID and ringNum=this.nMoveRange
                Set<djVtkPoint> allNeighbours = currentSurData.getNeighbourPoints(ptID, this.nMoveRange);
                System.out.println("neighbour size:" + allNeighbours.size());
                Iterator itAllNeighbours = allNeighbours.iterator();


                while (itAllNeighbours.hasNext()) {
                    List<Double> tmpPtFeature = new ArrayList<Double>();
                    djVtkPoint tmpPoint = (djVtkPoint) itAllNeighbours.next();
//                    System.out.println("neighbour:" + tmpPoint.pointId);


                    for (int i = 0; i
                            < optimizeGrids.nFeatureDim; i++) {
                        tmpPtFeature.add(featuresOfCurrentSubM.get(tmpPoint.pointId, i));


                    }
                    neighboursFeatureList.add(tmpPtFeature);
                    neighboursPtIDList.add(tmpPoint.pointId);


                }

                //write to weka file format
                String strWekaFile = nRealSubID + "_wekaformat.data";
                djVtkUtil.writeDoubleArrayListToFile(neighboursFeatureList, ",", optimizeGrids.nFeatureDim, strWekaFile);
                List<gridCandidate> candiListOfCurrentSub = this.findCandidates(strWekaFile, neighboursFeatureList, neighboursPtIDList, nVirSubID, gridIndex, ptID);
                allCandidateList.add(candiListOfCurrentSub);


            } //for all subs

            //begin to optimize
            List<gridCandidate> optiResultOfCurrentGrid = this.optimizeExamplers(allCandidateList);

            //print
            System.out.println("gridID = " + gridIndex);


            for (int i = 0; i
                    < optiResultOfCurrentGrid.size(); i++) {
                System.out.println(optiResultOfCurrentGrid.get(i).virSubID + " : " + optiResultOfCurrentGrid.get(i).optiGridID);

            }

        } //for all grids

    }
}
