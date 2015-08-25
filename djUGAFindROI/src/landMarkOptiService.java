/**
 * 
 */


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import edu.uga.liulab.djVtkBase.djNiftiData;
import edu.uga.liulab.djVtkBase.djVtkData;
import edu.uga.liulab.djVtkBase.djVtkDataDictionary;
import edu.uga.liulab.djVtkBase.djVtkFiberData;
import edu.uga.liulab.djVtkBase.djVtkHybridData;
import edu.uga.liulab.djVtkBase.djVtkPoint;
import edu.uga.liulab.djVtkBase.djVtkSurData;
import edu.uga.liulab.djVtkBase.djVtkUtil;

/**
 * @author djzhu
 * 
 */
public class landMarkOptiService {

	// **********************Input parameter***************************************
	public String dataFileName; // -if
	public String gridFileNme; // -ig
	public String predictionFileNme; // -ifPre
	public List<Integer> virSubList = new ArrayList<Integer>(); // -sl
	public int gridStart; // -gs
	public int gridEnd; // -ge
	public int ringNum = 3; // -rn default:3
	public boolean needDecimateNeighbors = false;
	public String folderForFibers = "";

	public List<List<String>> dataInfoList;
	public List<List<String>> gridInfoList;
	public List<List<String>> predictionInfoList;

	// *************************Local parameter************************************
	public Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();
	public Map<Integer, djVtkFiberData> fiberDataMap = new HashMap<Integer, djVtkFiberData>();
	public Map<Integer, AbstractMatrix> featureDataMap = new HashMap<Integer, AbstractMatrix>();
	public double[][][][] disDictionary;
	public int nMaxCandidateNum = 10;
	public List<List<landMarkOptiCandidate>> allOptiResult = new ArrayList<List<landMarkOptiCandidate>>();

	// *************************load data info************************************
	public void loadDataInfo() {
		dataInfoList = landMarkUtil.loadData(dataFileName, " ");
		for (int i = 0; i < dataInfoList.size(); i++)
			virSubList.add(i);
	}

	// *************************load grid info************************************
	public void loadGridInfo() {
		gridInfoList = landMarkUtil.loadData(gridFileNme, " ");
		gridStart = 1;
		gridEnd = gridInfoList.size();
	}

	// *************************load prediction info************************************
	public void loadPredictionInfo() {
		predictionInfoList = landMarkUtil.loadData(predictionFileNme, " ");
	}

	// *************************prepare fiber features of all points ??? Need modify************************************
	public void prepareFeture() {
		int nRingNum = 3;
		List<List<Float>> featureDataOfCurrentSub = new ArrayList<List<Float>>();
		for (int subIndex = 0; subIndex < this.virSubList.size(); subIndex++) {
			int fileIndex = this.virSubList.get(subIndex);
			featureDataOfCurrentSub.clear();
			System.out.println("###########################Begin to computer feature of " + fileIndex + " fileDescriptor:"
					+ dataInfoList.get(fileIndex).get(0) + "######");
			djVtkSurData surData = new djVtkSurData(dataInfoList.get(fileIndex).get(1));
			djVtkFiberData fiberData = new djVtkFiberData(dataInfoList.get(fileIndex).get(2));
			djVtkHybridData hybridData = new djVtkHybridData(surData, fiberData);
			hybridData.mapSurfaceToBox();
			hybridData.mapFiberToBox();
			int nPtNum = surData.nPointNum;
			for (int i = 0; i < surData.nPointNum; i++) {
				if (i % 100 == 0) {
					System.out.println("finished " + i + "/" + nPtNum + ".");
				}
				// extract fibers
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
				if (tmpFeature.size() != 144) {
					System.out.println("ERROR:tmpFeature.size is not 144. i=" + i);
					System.exit(0);
				}
				tmpFeature.add((float) tmpFiberData.nCellNum);
				tmpFeature.add((float) allTracePointsList.size());
				featureDataOfCurrentSub.add(tmpFeature);
			} // for all points
			djVtkUtil.writeArrayListToFile(featureDataOfCurrentSub, " ", 146, dataInfoList.get(fileIndex).get(0) + "_3_featureList.txt");
		} // for all data
	} // End of Function:prepareFeature

	// *************************Prediction************************************
	public List<Integer> initialPredictionLocation(int subIndex, djVtkSurData predictSurData) {
		System.out.println("Entering initialPredictionLocation...");
		List<Integer> initialPtIDList = new ArrayList<Integer>();
		List<djVtkPoint> currentSubGridPoints = new ArrayList<djVtkPoint>();
		//String subID = predictionInfoList.get(subIndex).get(0).split("_")[0];

		List<String> modelCoordList = landMarkUtil.loadFileToArrayList("InitialROILocation.txt");
		if(modelCoordList.size()!=358)
		{
			System.out.println("error happened in reading InitialROILocation.txt!!!");
			System.exit(0);
		}
		for (int gridIndex = gridStart; gridIndex <= gridEnd; gridIndex++) {
			System.out.println("dealing with grid: " + gridIndex);
			String[] currentCoords = modelCoordList.get(gridIndex-1).split(" ");
			djVtkPoint modelPt = new djVtkPoint();
			modelPt.x = Float.valueOf(currentCoords[0]);
			modelPt.y = Float.valueOf(currentCoords[1]);
			modelPt.z = Float.valueOf(currentCoords[2]);
			// djVtkPoint tmpPtP = new djVtkPoint();
			// tmpPtP.x = 0.0f;
			// tmpPtP.y = 0.0f;
			// tmpPtP.z = 0.0f;
			// for (int i = 0; i < gridInfoList.get(gridIndex - 1).size(); i++) {
			// int modelPtID = Integer.valueOf(gridInfoList.get(gridIndex - 1).get(i));
			// djVtkSurData currentSurData;
			// if (this.surDataMap.containsKey(i)) {
			// currentSurData = surDataMap.get(i);
			// } else {
			// currentSurData = new djVtkSurData(dataInfoList.get(i).get(1));
			// surDataMap.put(i, currentSurData);
			// }
			// djVtkPoint tmpPtM = currentSurData.getPoint(modelPtID);
			// tmpPtP.x += tmpPtM.x;
			// tmpPtP.y += tmpPtM.y;
			// tmpPtP.z += tmpPtM.z;
			// } // for all subjects in the model
			// tmpPtP.x /= (float) gridInfoList.get(gridIndex - 1).size();
			// tmpPtP.y /= (float) gridInfoList.get(gridIndex - 1).size();
			// tmpPtP.z /= (float) gridInfoList.get(gridIndex - 1).size();

//			djVtkSurData modelSurData;
//			if (this.surDataMap.containsKey(0)) {
//				modelSurData = surDataMap.get(0);
//			} else {
//				modelSurData = new djVtkSurData(dataInfoList.get(0).get(1));
//				surDataMap.put(0, modelSurData);
//			}
//			int modelPtID = Integer.valueOf(gridInfoList.get(gridIndex - 1).get(0));
//			djVtkPoint modelPt = modelSurData.getPoint(modelPtID);

			// find the nearest point on the predict surface
			float tmpMinDis = 1000.0f;
			int tmpSelectedPtID = -1;
			for (int ptIndex = 0; ptIndex < predictSurData.nPointNum; ptIndex++) {
				float tmpDis = djVtkUtil.calDistanceOfPoints(modelPt, predictSurData.getPoint(ptIndex));
				if (tmpDis < tmpMinDis) {
					tmpMinDis = tmpDis;
					tmpSelectedPtID = ptIndex;
				}
			} // for all pt on the surface, find the nearest point
			initialPtIDList.add(tmpSelectedPtID);
			currentSubGridPoints.add(predictSurData.getPoint(tmpSelectedPtID));
		} // for all rois
		landMarkUtil.writeToPointsVtkFile(predictionInfoList.get(subIndex).get(4) + predictionInfoList.get(subIndex).get(0) + "_initial.vtk",
				currentSubGridPoints, true);
		System.out.println("Finished initialPredictionLocation...");
		return initialPtIDList;
	}

	public void initialModel(int gridIndex, List<List<landMarkOptiCandidate>> allCandidateList) {
		System.out.println("Entering initialModel: gridIndex = " + gridIndex);
		for (int i = 0; i < gridInfoList.get(gridIndex - 1).size(); i++) {
			List<landMarkOptiCandidate> candiListOfCurrentSub = new ArrayList<landMarkOptiCandidate>();
			AbstractMatrix featuresOfCurrentSubM;
			if (featureDataMap.containsKey(i))
				featuresOfCurrentSubM = featureDataMap.get(i);
			else {
				featuresOfCurrentSubM = Matrix.fromASCIIFile(new File(this.dataInfoList.get(i).get(3)));
				featureDataMap.put(i, featuresOfCurrentSubM);
			}
			landMarkOptiCandidate newCandidate = new landMarkOptiCandidate();
			newCandidate.oriGridID = gridIndex;
			int modelPtID = Integer.valueOf(gridInfoList.get(gridIndex - 1).get(i));
			newCandidate.optiPtID = modelPtID;
			for (int j = 0; j < landMarkOptiDiction.nFeatureDim; j++) {
				newCandidate.feature.add(featuresOfCurrentSubM.get(modelPtID, j));
			}
			newCandidate.numOfFiber = (int) featuresOfCurrentSubM.get(modelPtID, 144);
			newCandidate.numOfTracePoints = (int) featuresOfCurrentSubM.get(modelPtID, 145);
			candiListOfCurrentSub.add(newCandidate);
			allCandidateList.add(candiListOfCurrentSub);
		} // for all subjects in the model
	}

	public void do_prediction() throws Exception {
		List<List<landMarkOptiCandidate>> allCandidateList = new ArrayList<List<landMarkOptiCandidate>>();
		for (int subIndex = 0; subIndex < this.virSubList.size(); subIndex++) {
			allOptiResult.clear();
			int nVirSubID = this.virSubList.get(subIndex);
			System.out.println("Now is sub" + predictionInfoList.get(nVirSubID).get(0)
					+ "------------------------------------------------------------");

			djVtkSurData currentSurData = new djVtkSurData(predictionInfoList.get(nVirSubID).get(1));
			// List<Integer> initialPtIDList = this.mapVolGridToSur(nVirSubID, currentSurData);
			List<Integer> initialPtIDList = this.initialPredictionLocation(nVirSubID, currentSurData);

			for (int gridIndex = gridStart; gridIndex <= gridEnd; gridIndex++) {
				System.out.println("Now gridID is : " + gridIndex);
				allCandidateList.clear();
				// add the candidates of model to the list for optimization
				this.initialModel(gridIndex, allCandidateList);

				List<landMarkOptiCandidate> candiListOfCurrentSub = new ArrayList<landMarkOptiCandidate>();

				// get neighbors based on seedPt=ptID and nMoveRange(default step is 2, the total range is 2*nMoveRange(3 or 4 in general))
				AbstractMatrix featuresOfCurrentSubM = Matrix.fromASCIIFile(new File(this.predictionInfoList.get(nVirSubID).get(3)));
				Set<djVtkPoint> candidatePoints;
				candidatePoints = currentSurData.getNeighbourPoints(initialPtIDList.get(gridIndex - 1), this.ringNum);
				if (candidatePoints.size() > this.nMaxCandidateNum)
					this.nMaxCandidateNum = candidatePoints.size();
				Iterator itCandidatePoints = candidatePoints.iterator();
				System.out.println(predictionInfoList.get(nVirSubID).get(0) + " has " + candidatePoints.size() + " candidates.");
				while (itCandidatePoints.hasNext()) {
					djVtkPoint tmpPoint = (djVtkPoint) itCandidatePoints.next();
					landMarkOptiCandidate newCandidate = new landMarkOptiCandidate();
					// newCandidate.virSubID = nVirSubID;
					// newCandidate.surData = this.surDataMap.get(newCandidate.virSubID);
					newCandidate.oriGridID = gridIndex;
					// newCandidate.oriPtID = ptID;
					newCandidate.optiPtID = tmpPoint.pointId;
					for (int i = 0; i < landMarkOptiDiction.nFeatureDim; i++) {
						newCandidate.feature.add(featuresOfCurrentSubM.get(tmpPoint.pointId, i));
					}
					newCandidate.numOfFiber = (int) featuresOfCurrentSubM.get(tmpPoint.pointId, 144);
					newCandidate.numOfTracePoints = (int) featuresOfCurrentSubM.get(tmpPoint.pointId, 145);
					candiListOfCurrentSub.add(newCandidate);
				}
				allCandidateList.add(candiListOfCurrentSub);
				List<landMarkOptiCandidate> optiResultOfCurrentGrid = this.optimizeExamplers_full(allCandidateList, true);
				allOptiResult.add(optiResultOfCurrentGrid);

			} // for all grids
				// print
			this.printPredictResult(allOptiResult, predictionInfoList.get(nVirSubID).get(0));
		} // for all subjects which need to be predicted
	}

	// *************************Optimization************************************
	public void do_optimizeSpec() {
		System.out.println("Entering do_optimizeSpec...");
		System.out.println("GridStart:" + this.gridStart + " and GridEnd:" + this.gridEnd);
		System.out.println("Need Sample neighbors:" + this.needDecimateNeighbors);
		List<String> gridInfo = djVtkUtil.loadFileToArrayList("manualPick-05-03_djOptiList.txt");
		for (int gridIndex = this.gridStart; gridIndex <= this.gridEnd; gridIndex++) {
			System.out.println("Grid " + gridIndex + " ---------------------------------------");
			List<List<landMarkOptiCandidate>> allCandidateList = new ArrayList<List<landMarkOptiCandidate>>();
			List<landMarkOptiCandidate> optiFinalResult = new ArrayList<landMarkOptiCandidate>();
			List<Integer> subModelList = new ArrayList<Integer>();
			List<Integer> subNeedOpti = new ArrayList<Integer>();

			String[] flag = gridInfo.get(gridIndex - 1).split(" ");
			if (flag[0].equalsIgnoreCase("x") || flag[0].equalsIgnoreCase("0")) // if cancel this roi or this roi is perfect
			{
				System.out.println("This roi pass!!!!!!!!!!");
				continue;
			} else if (flag[0].equalsIgnoreCase("3")) { // do the optimization for all subjects
				System.out.println("This roi need optimize all!!!!!!!!!!!");
				for (int i = 0; i < 10; i++) {
					List<landMarkOptiCandidate> candiListOfCurrentSub = new ArrayList<landMarkOptiCandidate>();
					int nVirSubID = i;
					djVtkSurData currentSurData;
					if (this.surDataMap.containsKey(nVirSubID)) {
						currentSurData = surDataMap.get(nVirSubID);
					} else {
						currentSurData = new djVtkSurData(dataInfoList.get(nVirSubID).get(1));
						surDataMap.put(nVirSubID, currentSurData);
					}
					AbstractMatrix featuresOfCurrentSubM = Matrix.fromASCIIFile(new File(dataInfoList.get(nVirSubID).get(3)));
					int ptID = Integer.valueOf(gridInfoList.get(gridIndex - 1).get(nVirSubID));

					// get neighbors based on seedPt=ptID and nMoveRange(default step is 2, the total range is 2*nMoveRange(3 or 4 in general))
					Set<djVtkPoint> candidatePoints;
					if (this.needDecimateNeighbors)
						candidatePoints = currentSurData.decimatePatch(ptID, 2, 3);
					else
						candidatePoints = currentSurData.getNeighbourPoints(ptID, this.ringNum);
					if (candidatePoints.size() > this.nMaxCandidateNum)
						this.nMaxCandidateNum = candidatePoints.size();
					Iterator itCandidatePoints = candidatePoints.iterator();
					System.out.println(nVirSubID + " has " + candidatePoints.size() + " candidates.");
					while (itCandidatePoints.hasNext()) {
						djVtkPoint tmpPoint = (djVtkPoint) itCandidatePoints.next();
						landMarkOptiCandidate newCandidate = new landMarkOptiCandidate();
						newCandidate.virSubID = nVirSubID;
						newCandidate.surData = this.surDataMap.get(newCandidate.virSubID);
						newCandidate.oriGridID = gridIndex;
						newCandidate.oriPtID = ptID;
						newCandidate.optiPtID = tmpPoint.pointId;
						for (int j = 0; j < landMarkOptiDiction.nFeatureDim; j++) {
							newCandidate.feature.add(featuresOfCurrentSubM.get(tmpPoint.pointId, j));
						}
						newCandidate.numOfFiber = (int) featuresOfCurrentSubM.get(tmpPoint.pointId, 144);
						newCandidate.numOfTracePoints = (int) featuresOfCurrentSubM.get(tmpPoint.pointId, 145);
						candiListOfCurrentSub.add(newCandidate);
					} // while
					allCandidateList.add(candiListOfCurrentSub);
				} // add all the subjects
				optiFinalResult = this.optimizeExamplers_fast(allCandidateList, true);
				allOptiResult.add(optiFinalResult);
				continue;
			} else if (flag[0].equalsIgnoreCase("6") && flag.length > 1) { // optimize some cases
				System.out.println("This roi need optimize some of them!!!!!!!!!!");
				for (int i = 1; i < flag.length; i++) {
					int subID = Integer.valueOf(flag[i]) - 1;
					subNeedOpti.add(subID);
				}
			} else {
				System.out.println("No match info. Pass!!");
				continue;
			}
			for (int i = 0; i < 10; i++) {
				if (!subNeedOpti.contains(i))
					subModelList.add(i);
				// initial the output result of this round optimization. some of them will be replaced later
				landMarkOptiCandidate newCandidate = new landMarkOptiCandidate();
				optiFinalResult.add(newCandidate);
			}
			System.out.println("Model subList:" + subModelList.toString());
			System.out.println("optimized subList:" + subNeedOpti.toString());

			// initial model
			for (int i = 0; i < subModelList.size(); i++) {
				System.out.println("Now dealing with the " + gridIndex
						+ "th grid for initial model ########################################################");
				List<landMarkOptiCandidate> candiListOfCurrentSub = new ArrayList<landMarkOptiCandidate>();
				int nVirSubID = subModelList.get(i);
				djVtkSurData currentSurData;
				if (this.surDataMap.containsKey(nVirSubID)) {
					currentSurData = surDataMap.get(nVirSubID);
				} else {
					currentSurData = new djVtkSurData(dataInfoList.get(nVirSubID).get(1));
					surDataMap.put(nVirSubID, currentSurData);
				}
				AbstractMatrix featuresOfCurrentSubM = Matrix.fromASCIIFile(new File(dataInfoList.get(nVirSubID).get(3)));
				int ptID = Integer.valueOf(gridInfoList.get(gridIndex - 1).get(nVirSubID));

				djVtkPoint tmpPoint = currentSurData.getPoint(ptID);
				landMarkOptiCandidate newCandidate = new landMarkOptiCandidate();
				newCandidate.virSubID = nVirSubID;
				newCandidate.surData = currentSurData;
				newCandidate.oriGridID = gridIndex;
				newCandidate.oriPtID = ptID;
				newCandidate.optiPtID = ptID;
				for (int j = 0; j < landMarkOptiDiction.nFeatureDim; j++) {
					newCandidate.feature.add(featuresOfCurrentSubM.get(ptID, j));
				}
				newCandidate.numOfFiber = (int) featuresOfCurrentSubM.get(ptID, 144);
				newCandidate.numOfTracePoints = (int) featuresOfCurrentSubM.get(ptID, 145);
				optiFinalResult.set(nVirSubID, newCandidate);
				candiListOfCurrentSub.add(newCandidate);
				allCandidateList.add(candiListOfCurrentSub);
			}

			// initial subsNeedToOpti
			for (int i = 0; i < subNeedOpti.size(); i++) {
				System.out.println("Now dealing with the " + gridIndex
						+ "th grid for subs need to be optimized########################################################");
				List<landMarkOptiCandidate> candiListOfCurrentSub = new ArrayList<landMarkOptiCandidate>();
				int nVirSubID = subNeedOpti.get(i);
				djVtkSurData currentSurData;
				if (this.surDataMap.containsKey(nVirSubID)) {
					currentSurData = surDataMap.get(nVirSubID);
				} else {
					currentSurData = new djVtkSurData(dataInfoList.get(nVirSubID).get(1));
					surDataMap.put(nVirSubID, currentSurData);
				}
				AbstractMatrix featuresOfCurrentSubM = Matrix.fromASCIIFile(new File(dataInfoList.get(nVirSubID).get(3)));
				int ptID = Integer.valueOf(gridInfoList.get(gridIndex - 1).get(nVirSubID));

				// get neighbors based on seedPt=ptID and nMoveRange(default step is 2, the total range is 2*nMoveRange(3 or 4 in general))
				Set<djVtkPoint> candidatePoints;
				candidatePoints = currentSurData.getNeighbourPoints(ptID, this.ringNum);
				if (candidatePoints.size() > this.nMaxCandidateNum)
					this.nMaxCandidateNum = candidatePoints.size();
				Iterator itCandidatePoints = candidatePoints.iterator();
				System.out.println(nVirSubID + " has " + candidatePoints.size() + " candidates.");
				while (itCandidatePoints.hasNext()) {
					djVtkPoint tmpPoint = (djVtkPoint) itCandidatePoints.next();
					landMarkOptiCandidate newCandidate = new landMarkOptiCandidate();
					newCandidate.virSubID = nVirSubID;
					newCandidate.surData = this.surDataMap.get(newCandidate.virSubID);
					newCandidate.oriGridID = gridIndex;
					newCandidate.oriPtID = ptID;
					newCandidate.optiPtID = tmpPoint.pointId;
					for (int j = 0; j < landMarkOptiDiction.nFeatureDim; j++) {
						newCandidate.feature.add(featuresOfCurrentSubM.get(tmpPoint.pointId, j));
					}
					newCandidate.numOfFiber = (int) featuresOfCurrentSubM.get(tmpPoint.pointId, 144);
					newCandidate.numOfTracePoints = (int) featuresOfCurrentSubM.get(tmpPoint.pointId, 145);
					candiListOfCurrentSub.add(newCandidate);
				} // while
				allCandidateList.add(candiListOfCurrentSub);
				List<landMarkOptiCandidate> optiResultOfCurrentGrid = this.optimizeExamplers_full(allCandidateList, true);
				optiFinalResult.set(nVirSubID, optiResultOfCurrentGrid.get(optiResultOfCurrentGrid.size() - 1));
				int idRemove = allCandidateList.size() - 1;
				allCandidateList.remove(idRemove);// remove the last element: the optimized result just got. prepare for the next optimization
			} // for subs need to be optimized
				// begin to optimize
			allOptiResult.add(optiFinalResult);
		} // for all grids
			// print
		this.printOptiResult(allOptiResult, "0504_1017");
	}

	public void do_optimize() {
		System.out.println("Entering do_optimize...");
		System.out.println("subList:" + this.virSubList.toString());
		System.out.println("GridStart:" + this.gridStart + " and GridEnd:" + this.gridEnd);
		System.out.println("Need Sample neighbors:" + this.needDecimateNeighbors);

		for (int gridIndex = this.gridStart; gridIndex <= this.gridEnd; gridIndex++) {
			List<List<landMarkOptiCandidate>> allCandidateList = new ArrayList<List<landMarkOptiCandidate>>();
			for (int subIndex = 0; subIndex < this.virSubList.size(); subIndex++) // for subs
			{
				System.out.println("Now dealing with the " + gridIndex + "th grid########################################################");
				List<landMarkOptiCandidate> candiListOfCurrentSub = new ArrayList<landMarkOptiCandidate>();
				int nVirSubID = this.virSubList.get(subIndex);
				djVtkSurData currentSurData;
				if (this.surDataMap.containsKey(nVirSubID)) {
					currentSurData = surDataMap.get(nVirSubID);
				} else {
					currentSurData = new djVtkSurData(dataInfoList.get(nVirSubID).get(1));
					surDataMap.put(nVirSubID, currentSurData);
				}
				AbstractMatrix featuresOfCurrentSubM = Matrix.fromASCIIFile(new File(dataInfoList.get(nVirSubID).get(3)));
				int ptID = Integer.valueOf(gridInfoList.get(gridIndex - 1).get(nVirSubID));

				// get neighbors based on seedPt=ptID and nMoveRange(default step is 2, the total range is 2*nMoveRange(3 or 4 in general))
				Set<djVtkPoint> candidatePoints;
				if (this.needDecimateNeighbors)
					candidatePoints = currentSurData.decimatePatch(ptID, 2, 3);
				else
					candidatePoints = currentSurData.getNeighbourPoints(ptID, this.ringNum);
				if (candidatePoints.size() > this.nMaxCandidateNum)
					this.nMaxCandidateNum = candidatePoints.size();
				Iterator itCandidatePoints = candidatePoints.iterator();
				System.out.println(nVirSubID + " has " + candidatePoints.size() + " candidates.");
				while (itCandidatePoints.hasNext()) {
					djVtkPoint tmpPoint = (djVtkPoint) itCandidatePoints.next();
					landMarkOptiCandidate newCandidate = new landMarkOptiCandidate();
					newCandidate.virSubID = nVirSubID;
					newCandidate.surData = this.surDataMap.get(newCandidate.virSubID);
					newCandidate.oriGridID = gridIndex;
					newCandidate.oriPtID = ptID;
					newCandidate.optiPtID = tmpPoint.pointId;
					for (int i = 0; i < landMarkOptiDiction.nFeatureDim; i++) {
						newCandidate.feature.add(featuresOfCurrentSubM.get(tmpPoint.pointId, i));
					}
					newCandidate.numOfFiber = (int) featuresOfCurrentSubM.get(tmpPoint.pointId, 144);
					newCandidate.numOfTracePoints = (int) featuresOfCurrentSubM.get(tmpPoint.pointId, 145);
					candiListOfCurrentSub.add(newCandidate);
				} // while
				allCandidateList.add(candiListOfCurrentSub);
			} // for all subs
				// begin to optimize
			List<landMarkOptiCandidate> optiResultOfCurrentGrid = this.optimizeExamplers_full(allCandidateList, true);
			allOptiResult.add(optiResultOfCurrentGrid);
		} // for all grids
			// print
		this.printOptiResult(allOptiResult, this.virSubList.toString());
	}

	public boolean checkConstrain(List<Integer> subCombination, List<List<landMarkOptiCandidate>> allCandidateList) {
		boolean bValid = true;
		int sumNumOfFiber = 0;
		int sumNumOfTracePoints = 0;
		for (int i = 0; i < subCombination.size(); i++) {
			sumNumOfFiber += allCandidateList.get(i).get(subCombination.get(i)).numOfFiber;
			sumNumOfTracePoints += allCandidateList.get(i).get(subCombination.get(i)).numOfTracePoints;
		}
		int lowBoundFiber = sumNumOfFiber / (3 * subCombination.size());
		int highBoundFiber = (5 * sumNumOfFiber) / (3 * subCombination.size());
		for (int i = 0; i < subCombination.size(); i++) {
			if ((allCandidateList.get(i).get(subCombination.get(i)).numOfFiber < lowBoundFiber)
					|| (allCandidateList.get(i).get(subCombination.get(i)).numOfFiber > highBoundFiber))
				bValid = false;
			// System.out.println("$$$$$$$$$$$$$$$$$$$checkConstrain:" + subCombination + " has "
			// + allCandidateList.get(i).get(subCombination.get(i)).numOfFiber + " fibers, check result:"+bValid);
		}
		return bValid;
	}

	public List<landMarkOptiCandidate> optimizeExamplers_fast(List<List<landMarkOptiCandidate>> allCandidateList, boolean needConstrainCheck) {
		System.out.println("Entering optimizeExamplers fast!...");
		List<landMarkOptiCandidate> optiResultOfCurrentGrid = new ArrayList<landMarkOptiCandidate>();

		// find the average of all candidates
		System.out.println("Calculating the average of all candidates...");
		List<Double> aveFeatureList = new ArrayList<Double>();
		for (int i = 0; i < landMarkOptiDiction.nFeatureDim; i++)
			aveFeatureList.add(0.0);
		int count = 0;
		for (int i = 0; i < allCandidateList.size(); i++) {
			for (int j = 0; j < allCandidateList.get(i).size(); j++) {
				for (int k = 0; k < landMarkOptiDiction.nFeatureDim; k++) {
					double tmpSub = aveFeatureList.get(k) + allCandidateList.get(i).get(j).feature.get(k);
					aveFeatureList.set(k, tmpSub);
					count++;
				} // for k
			} // for j
		} // for i
		System.out.println("There are total " + count + " candidates ...");
		for (int k = 0; k < landMarkOptiDiction.nFeatureDim; k++) {
			double tmpAve = aveFeatureList.get(k) / count;
			aveFeatureList.set(k, tmpAve);
		}
		// System.out.println("The average value is : " + aveFeatureList.toString());

		// find the candidate which has the least distance with the average
		System.out.println("Finding the candidate which has the least distance with the average...");
		for (int i = 0; i < allCandidateList.size(); i++) {
			double minDis = 1000.0;
			int currentID = -1;
			for (int j = 0; j < allCandidateList.get(i).size(); j++) {
				double tmpDis = this.calFeatureDis(aveFeatureList, allCandidateList.get(i).get(j).feature);
				if (tmpDis < minDis) {
					minDis = tmpDis;
					currentID = j;
				}
			} // for j
			optiResultOfCurrentGrid.add(allCandidateList.get(i).get(currentID));
		} // for i
		return optiResultOfCurrentGrid;
	}

	public List<landMarkOptiCandidate> optimizeExamplers_full(List<List<landMarkOptiCandidate>> allCandidateList, boolean needConstrainCheck) {
		System.out.println("Entering optimizeExamplers full!...");
		List<landMarkOptiCandidate> optiResultOfCurrentGrid = new ArrayList<landMarkOptiCandidate>();
		this.fillDisDictionary(allCandidateList);
		System.out.println("fillDisDictionary over!");
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
				// if(subCombination.get(size/3)==(limit[size/3]-1))
				// System.out.println(subCombination);
				if (!needConstrainCheck || (needConstrainCheck && this.checkConstrain(subCombination, allCandidateList))) {
					tmpDis = this.calDisOfGroup(subCombination);
					if (tmpDis < disMin) {
						disMin = tmpDis;
						subCombinationWithMinDis.clear();
						subCombinationWithMinDis.addAll(subCombination);
					}
				} // if the result of constrain checking is true
			} // if
			subCombination.set(size - 1, subCombination.get(size - 1) + 1);
			for (int i = size - 1; i > 0; i--) {
				// if(i==(size/3))
				// System.out.println(subCombination+" / "+subCombination);
				if (subCombination.get(i) < limit[i]) {
					break;
				} else {
					subCombination.set(i, 0);
					subCombination.set(i - 1, subCombination.get(i - 1) + 1);
					if ((i - 1) == 0) {// if ((i == (size / 3)) || (i == (size / 2)) || (i == (2 * size / 3))) {// if ((i - 1) == 0) {
						System.out.println(subCombination);
					}
				}
			} // for
		} // while

		if (subCombinationWithMinDis.size() == 0) {
			System.out.println("++++++Need to be compute again due to the constrain check!");
			optiResultOfCurrentGrid = this.optimizeExamplers_full(allCandidateList, false);
		} else
			for (int i = 0; i < size; i++) {
				landMarkOptiCandidate optiGridOfCurrentSub = allCandidateList.get(i).get(subCombinationWithMinDis.get(i));
				optiResultOfCurrentGrid.add(optiGridOfCurrentSub);
			}
		return optiResultOfCurrentGrid;
	}

	private void fillDisDictionary(List<List<landMarkOptiCandidate>> allCandidateList) {
		disDictionary = new double[allCandidateList.size()][this.nMaxCandidateNum][allCandidateList.size()][this.nMaxCandidateNum];
		for (int i = 0; i < allCandidateList.size(); i++)// 5
		{
			System.out.println("calculating the distance of  sub" + i + " to other subs... ");
			for (int j = 0; j < allCandidateList.get(i).size(); j++)// mostly 0~4
			{
				for (int m = i + 1; m < allCandidateList.size(); m++) {
					for (int n = 0; n < allCandidateList.get(m).size(); n++) {
						landMarkOptiCandidate c1 = allCandidateList.get(i).get(j);
						landMarkOptiCandidate c2 = allCandidateList.get(m).get(n);
						disDictionary[i][j][m][n] = this.calFeatureDis(c1.feature, c2.feature);
						// System.out.println("disDictionary["+i+"]["+j+"]["+m+"]["+n+"]="+disDictionary[i][j][m][n]);
					} // for n
				} // for m
			} // for j
		} // for i
	}

	private double calFeatureDis(List<Double> f1, List<Double> f2) {
		double dis = 0.0;
		if (f1.size() == f2.size()) {
			for (int i = 0; i < f1.size(); i++) {
				dis += Math.abs(f1.get(i) - f2.get(i));
			}
		} else {
			System.out.println("the size of f1 and f2 are not equal!!");
		}
		return dis;
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

	public void printPredictResult(List<List<landMarkOptiCandidate>> resultToPrint, String descriptor) {
		System.out.println("Beging to output the Predict Result...");
		String fileName = "LandMarkPredictionResult_" + this.gridStart + "_" + this.gridEnd + "_" + descriptor + ".txt";
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName);
			for (int grid = 0; grid < resultToPrint.size(); grid++) {
				for (int sub = 0; sub < resultToPrint.get(grid).size(); sub++) {
					landMarkOptiCandidate currentCandidate = resultToPrint.get(grid).get(sub);
					String strInfo = currentCandidate.optiPtID + " ";
					fw.write(strInfo);
				} // for sub
				fw.write("\r\n");
			} // for grid
		} catch (IOException ex) {
			Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fw.close();
			} catch (IOException ex) {
				Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println("Output done!!!");
	}

	public void printOptiResult(List<List<landMarkOptiCandidate>> resultToPrint, String descriptor) {
		boolean outPutFiber = false;
		System.out.println("Beging to output the optimizatized result...");
		String fileName = "LandMarkResult_" + this.gridStart + "_" + this.gridEnd + "_" + descriptor + ".txt";
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName);
			for (int grid = 0; grid < resultToPrint.size(); grid++) {
				for (int sub = 0; sub < resultToPrint.get(grid).size(); sub++) {
					landMarkOptiCandidate currentCandidate = resultToPrint.get(grid).get(sub);
					String strInfo = currentCandidate.oriGridID + " " + currentCandidate.virSubID + " " + currentCandidate.oriPtID + " "
							+ currentCandidate.optiPtID;
					fw.write(strInfo + "\r\n");

					if (outPutFiber) {
						System.out.println("Begin to output fibers...");
						djVtkSurData surData = surDataMap.get(currentCandidate.virSubID);
						djVtkFiberData fiberData = fiberDataMap.get(currentCandidate.virSubID);
						if (fiberData == null)
							fiberData = new djVtkFiberData(dataInfoList.get(currentCandidate.virSubID).get(2));
						djVtkHybridData hybridData = new djVtkHybridData(surData, fiberData);
						hybridData.mapSurfaceToBox();
						hybridData.mapFiberToBox();
						Set ptSet = surData.getNeighbourPoints(currentCandidate.optiPtID, 3);
						djVtkFiberData tmpFiberData = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
						tmpFiberData.writeToVtkFile("sub." + currentCandidate.virSubID + "." + currentCandidate.optiPtID + ".fibers.vtk");
					} // if output fiber
				} // for sub
			} // for grid
		} catch (IOException ex) {
			Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fw.close();
			} catch (IOException ex) {
				Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println("Output done!!!");
	}

	public void do_generateROIfibers() throws IOException {
		System.out.println("Entering do_generateROIfibers...");
		int resultColNum = 10; // / current 10 models and the last column is the optimized result
		List<djVtkPoint> resultPtList = new ArrayList<djVtkPoint>();
		for (int subIndex = 0; subIndex < this.virSubList.size(); subIndex++) {
			int nVirSubID = this.virSubList.get(subIndex);
			System.out.println("generating fibers for " + predictionInfoList.get(nVirSubID).get(0));
			djVtkSurData surData = new djVtkSurData(predictionInfoList.get(nVirSubID).get(1));
			djVtkFiberData fiberData = new djVtkFiberData(predictionInfoList.get(nVirSubID).get(2));
			djVtkHybridData hybridData = new djVtkHybridData(surData, fiberData);
			hybridData.mapSurfaceToBox();
			hybridData.mapFiberToBox();
			for (int rowID = 0; rowID < this.gridInfoList.size(); rowID++) {
				int opPtID = Integer.valueOf(this.gridInfoList.get(rowID).get(resultColNum));
				resultPtList.add(surData.getPoint(opPtID));
				Set ptSet = surData.getNeighbourPoints(opPtID, 3);
				djVtkFiberData newFiber = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
				newFiber.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
				newFiber.writeToVtkFileCompact(predictionInfoList.get(subIndex).get(4) + this.folderForFibers + "/fiber.roi.sub." + nVirSubID
						+ ".sid." + opPtID + ".vtk");
			} // for all rows
			landMarkUtil.writeToPointsVtkFile(predictionInfoList.get(subIndex).get(4) + predictionInfoList.get(subIndex).get(0) + nVirSubID
					+ "_predictionResult.vtk", resultPtList, true);
		} // for all subjects
		System.out.println("The fibers have been written to : " + this.folderForFibers);
	}

}
