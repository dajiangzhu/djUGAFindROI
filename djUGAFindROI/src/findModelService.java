

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.apache.commons.math.stat.correlation.*;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import edu.uga.liulab.djVtkBase.*;

public class findModelService {
	public List<Integer> virSubList = new ArrayList<Integer>();
	public int gridStart;
	public int gridEnd;
	public String anaInputFile;
	public String anaModelFile;
	// dictionary data
	public Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();
	public Map<Integer, djVtkFiberData> fiberDataMap = new HashMap<Integer, djVtkFiberData>();
	public double[][][][] disDictionary;
	public int nMaxCandidateNum = 80;
	public List<List<gridCandidate>> allOptiResult = new ArrayList<List<gridCandidate>>();

	public void do_analyzeConsistancy() {
		AbstractMatrix inputM = Matrix.fromASCIIFile(new File("./convergeInfo/" + anaInputFile));
		AbstractMatrix modelM = Matrix.fromASCIIFile(new File("./convergeInfo/" + anaModelFile));
		String[] subIDListInput = this.anaInputFile.split("\\.");
		String[] subIDListModel = this.anaModelFile.split("\\.");
		for (int i = 1; i < (subIDListInput.length - 1); i++) {
			for (int j = 1; j < (subIDListModel.length - 1); j++) {
				if (subIDListInput[i].trim().equals(subIDListModel[j].trim())) {
					int virSubID = Integer.valueOf(subIDListInput[i]);
					int realSubID = (int) findModelDictionary.subRelationM.get(virSubID, 1);
					djVtkSurData surData = new djVtkSurData("./data/" + realSubID + ".surf.vtk");
					int countConsistent = 0;
					int rowDim = inputM.getRowDimension();
					int columnDim = inputM.getColumnDimension();
					int countConverge = 0;
					for (int r = 0; r < rowDim; r++) {
						int roundNum = (int) inputM.get(r, 1);
						if (roundNum <= findModelDictionary.ITERATIVE_THRESHOLD) // smaller than 3
						{
							countConverge++;
							int tmpRowDim = modelM.getRowDimension();
							for (int rTmp = 0; rTmp < tmpRowDim; rTmp++) {
								int tmpRoundNum = (int) modelM.get(rTmp, 1);
								if (tmpRoundNum <= findModelDictionary.ITERATIVE_THRESHOLD) // smaller than 3
								{
									int ptID = (int) inputM.get(r, i + 1);
									int tmpPtID = (int) modelM.get(rTmp, j + 1);
									float dis = djVtkUtil.calDistanceOfPoints(surData.getPoint(ptID), surData.getPoint(tmpPtID));
									if (dis < 5) {
										countConsistent++;
										break;
									} // if distance<thershold
								} // if roundNum<3 in modelM
							} // for rTmp
						}
					} // for all rows
					System.out.println("sub" + virSubID + " :countConverge " + countConverge + "    countConsistent" + countConsistent);
				} // if subID equal
			} // for j
		} // for i
	}

	public void do_computeFeatures() {
		// computer features and constrain: fiber number, trace points number
		for (int s = 0; s < this.virSubList.size(); s++) {
			List<List<Float>> featureDataOfCurrentSub = new ArrayList<List<Float>>();
			List<String> constrainOfCurrentSub = new ArrayList<String>();
			int virSubID = this.virSubList.get(s);
			int realSubID = (int) findModelDictionary.subRelationM.get(virSubID, 1);
			djVtkSurData currentSurData;
			djVtkFiberData currentFiberData;
			if (this.surDataMap.containsKey(realSubID)) {
				currentSurData = surDataMap.get(realSubID);
			} else {
				currentSurData = new djVtkSurData("./data/" + realSubID + ".surf.vtk");
				surDataMap.put(realSubID, currentSurData);
			}
			if (this.fiberDataMap.containsKey(realSubID)) {
				currentFiberData = fiberDataMap.get(realSubID);
			} else {
				currentFiberData = new djVtkFiberData("./data/" + realSubID + ".asc.fiber.vtk");
				fiberDataMap.put(realSubID, currentFiberData);
			}
			djVtkHybridData hybridData = new djVtkHybridData(currentSurData, currentFiberData);
			hybridData.mapSurfaceToBox();
			hybridData.mapFiberToBox();

			int nPtNum = currentSurData.nPointNum;
			for (int i = 0; i < nPtNum; i++) {
				if (i % 1000 == 0) {
					System.out.println("finished " + i + "/" + nPtNum + ".");
				}
				// extract fibers
				Set ptSet = currentSurData.getNeighbourPoints(i, findModelDictionary.EXTRACTING_FIBER_RINGNUM);
				djVtkFiberData tmpFiberData = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
				tmpFiberData.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
				String constrainInfo = String.valueOf(tmpFiberData.nCellNum);
				// generate trace based on fiber file
				djVtkPoint tmpPoint = currentSurData.getPoint(i);
				fiberBundleService fiberBundleDescriptor = new fiberBundleService();
				fiberBundleDescriptor.setFiberData(tmpFiberData);
				fiberBundleDescriptor.setSeedPnt(tmpPoint);
				fiberBundleDescriptor.createFibersTrace();
				List<djVtkPoint> allTracePointsList = fiberBundleDescriptor.getAllPoints();
				constrainInfo = constrainInfo + " " + allTracePointsList.size();
				// calculate the feature
				List<Float> tmpFeature = fiberBundleDescriptor.calFeatureOfTrace(allTracePointsList);
				if (tmpFeature.size() != 144) {
					System.out.println("ERROR:tmpFeature.size is not 144. i=" + i);
					System.exit(0);
				}
				featureDataOfCurrentSub.add(tmpFeature);
				constrainOfCurrentSub.add(constrainInfo);
			} // for all points in the current surface
			System.out.println("featureDataOfCurrentSub size:" + featureDataOfCurrentSub.size());
			System.out.println("constrainOfCurrentSub size:" + constrainOfCurrentSub.size());
			djVtkUtil.writeArrayListToFile(featureDataOfCurrentSub, " ", 144, realSubID + "_FeatureList.txt");
			djVtkUtil.writeArrayListToFile(constrainOfCurrentSub, realSubID + "_ConstrainList.txt");
		} // for all subjects in the subList
	}

	public void do_optimize() throws Exception {
		System.out.println("Begin to optimize...");
		String strSubList = "";
		FileWriter fw = null;
		String convergeFileName = this.gridStart + "_" + this.gridEnd + "_ConvergeInfo";
		for (int subIndex = 0; subIndex < this.virSubList.size(); subIndex++) {
			int nVirSubID = this.virSubList.get(subIndex);
			convergeFileName = convergeFileName + "." + nVirSubID;
			strSubList = strSubList + nVirSubID + ",";
		}
		// check if this combination has been done before
		String sql = "select * from test.t_modelInfo where subList='" + strSubList + "' and gridInfo='" + this.gridStart + "-" + this.gridEnd + "'";
		if (DatabaseTool.executeQuery(sql).size() != 0) {
			System.out.println("this combination has been done before!!!");
			return;
		} else {
			sql = "insert into test.t_modelInfo (subNum,subList)" + " values (" + this.virSubList.size() + ",'" + strSubList + "')";
			DatabaseTool.executeUpdate(sql);

		}

		convergeFileName = convergeFileName + ".txt";
		fw = new FileWriter(convergeFileName);
		int convergeCount = 0;
		double totalInnerDis = 0.0;

		for (int gridIndex = this.gridStart; gridIndex <= this.gridEnd; gridIndex++) {
			System.out.println("dealing with the " + gridIndex + "th grid--------------------------------");
			List<gridCandidate> optiResultOfCurrentGrid = new ArrayList<gridCandidate>();

			// ***initial optiResultOfCurrentGrid
			for (int subIndex = 0; subIndex < this.virSubList.size(); subIndex++) {
				int nVirSubID = this.virSubList.get(subIndex);
				int ptID = (int) findModelDictionary.gridPoint_chInitial_1_M.get(gridIndex - 1, nVirSubID);
				gridCandidate newCandidate = new gridCandidate();
				newCandidate.virSubID = nVirSubID;
				newCandidate.realSubID = (int) optimizeGrids.subRelationM.get(nVirSubID, 1);
				newCandidate.optiPtID = ptID;
				optiResultOfCurrentGrid.add(newCandidate);
			}

			// ***begin to optimize to find the converge points
			for (int round = 0; round < findModelDictionary.ITERATIVE_MAX; round++) {
				boolean needQuitRound = false;
				System.out.println("doning the " + round + "th round of optimization........................");
				List<List<gridCandidate>> allCandidateList = new ArrayList<List<gridCandidate>>();
				for (int subIndex = 0; subIndex < this.virSubList.size(); subIndex++) // for all subs in the subList
				{
					List<gridCandidate> candiListOfCurrentSub = new ArrayList<gridCandidate>();
					List<List<Double>> neighboursFeatureList = new ArrayList<List<Double>>();
					int nVirSubID = this.virSubList.get(subIndex);
					int nRealSubID = (int) findModelDictionary.subRelationM.get(nVirSubID, 1);
					System.out.println("dealing with gird=" + gridIndex + " and sub=" + nVirSubID);
					djVtkSurData currentSurData;
					if (this.surDataMap.containsKey(nRealSubID)) {
						currentSurData = surDataMap.get(nRealSubID);
					} else {
						currentSurData = new djVtkSurData("./data/" + nRealSubID + ".surf.vtk");
						surDataMap.put(nRealSubID, currentSurData);
					}
					AbstractMatrix featuresOfCurrentSubM = Matrix.fromASCIIFile(new File("./data/" + nRealSubID + "_FeatureList.txt"));
					AbstractMatrix constrainOfCurrentSubM = Matrix.fromASCIIFile(new File("./data/" + nRealSubID + "_ConstrainList.txt"));
					int ptID = optiResultOfCurrentGrid.get(subIndex).optiPtID;
					// prepare neighboursFeatureList
					for (int ptIndex = 0; ptIndex < currentSurData.nPointNum; ptIndex++) {
						List<Double> tmpPtFeature = new ArrayList<Double>();
						for (int i = 0; i < optimizeGrids.nFeatureDim; i++)
							tmpPtFeature.add(featuresOfCurrentSubM.get(ptIndex, i));
						neighboursFeatureList.add(tmpPtFeature);
					}

					// ***get neighbors based on seedPt=ptID and nMoveRange(default step is 2, the total range is 2*nMoveRange(3 or 4 in general))
					Set<djVtkPoint> candidatePoints = currentSurData.decimatePatch(ptID, 2, findModelDictionary.MOVE_RANGE);
					Iterator itCandidatePoints = candidatePoints.iterator();
					while (itCandidatePoints.hasNext()) {
						djVtkPoint tmpPoint = (djVtkPoint) itCandidatePoints.next();
						// fiber number constrain, make sure fiber number larger than the threshold
						if (constrainOfCurrentSubM.get(tmpPoint.pointId, 0) > findModelDictionary.FIBERNUM_THRESHOLD) {
							gridCandidate newCandidate = new gridCandidate();
							newCandidate.virSubID = nVirSubID;
							newCandidate.realSubID = (int) optimizeGrids.subRelationM.get(nVirSubID, 1);
							newCandidate.surData = this.surDataMap.get(newCandidate.realSubID);
							newCandidate.oriGridID = gridIndex;
							newCandidate.oriPtID = ptID;
							newCandidate.optiPtID = tmpPoint.pointId;
							newCandidate.feature = neighboursFeatureList.get(tmpPoint.pointId);
							candiListOfCurrentSub.add(newCandidate);
						} // if satisfy the fiber number constrain
					} // while
					if (candiListOfCurrentSub.size() == 0) {
						System.out.println("!!!!!!!!ERROR: sub" + subIndex + " has no optimization candidate!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						needQuitRound = true;
						break;
					}
					allCandidateList.add(candiListOfCurrentSub);
				} // for all subs

				if (needQuitRound) {
					System.out.println("go to next grid!!!");
					break;
				}

				// ***begin to optimize
				optimizeResult optiResult = this.optimizeExamplers(allCandidateList);
				optiResultOfCurrentGrid = optiResult.optiResult;
				if (round > 0) {
					boolean isConverge = this.checkConverge(optiResultOfCurrentGrid, allOptiResult.get(allOptiResult.size() - 1));
					if (isConverge) {
						System.out.println("converge on round:" + round + "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
						System.out.println("begin to fine optimization..."); // ***Begin to fine optimization
						List<List<gridCandidate>> allCandidateListForFineOpti = new ArrayList<List<gridCandidate>>();
						for (int i = 0; i < this.virSubList.size(); i++) {
							int nVirSubID = this.virSubList.get(i);
							int nRealSubID = (int) findModelDictionary.subRelationM.get(nVirSubID, 1);
							AbstractMatrix featuresOfCurrentSubM = Matrix.fromASCIIFile(new File("./data/" + nRealSubID + "_FeatureList.txt"));
							AbstractMatrix constrainOfCurrentSubM = Matrix.fromASCIIFile(new File("./data/" + nRealSubID + "_ConstrainList.txt"));
							List<gridCandidate> candiListOfCurrentSubForFineOpti = new ArrayList<gridCandidate>();
							// prepare neighboursFeatureList
							List<List<Double>> neighboursFeatureList = new ArrayList<List<Double>>();
							for (int ptIndex = 0; ptIndex < this.surDataMap.get(nRealSubID).nPointNum; ptIndex++) {
								List<Double> tmpPtFeature = new ArrayList<Double>();
								for (int j = 0; j < optimizeGrids.nFeatureDim; j++)
									tmpPtFeature.add(featuresOfCurrentSubM.get(ptIndex, j));
								neighboursFeatureList.add(tmpPtFeature);
							}
							Set<djVtkPoint> ptFineOptiSet = this.surDataMap.get(nRealSubID).getNeighbourPoints(
									optiResultOfCurrentGrid.get(i).optiPtID, findModelDictionary.FINE_OPTI_RINGNUM);
							Iterator itCandidatePoints = ptFineOptiSet.iterator();
							while (itCandidatePoints.hasNext()) {
								djVtkPoint tmpPoint = (djVtkPoint) itCandidatePoints.next();
								// fiber number constrain, make sure fiber number larger than the threshold
								if (constrainOfCurrentSubM.get(tmpPoint.pointId, 0) > findModelDictionary.FIBERNUM_THRESHOLD) {
									gridCandidate newCandidate = new gridCandidate();
									newCandidate.virSubID = nVirSubID;
									newCandidate.realSubID = (int) optimizeGrids.subRelationM.get(nVirSubID, 1);
									newCandidate.surData = this.surDataMap.get(newCandidate.realSubID);
									newCandidate.oriGridID = gridIndex;
									newCandidate.oriPtID = optiResultOfCurrentGrid.get(i).optiPtID;
									newCandidate.optiPtID = tmpPoint.pointId;
									newCandidate.feature = neighboursFeatureList.get(tmpPoint.pointId);
									candiListOfCurrentSubForFineOpti.add(newCandidate);
								} // if satisfy the fiber number constrain
							} // while
							if (candiListOfCurrentSubForFineOpti.size() == 0)
								System.out.println("!!!!!!!!ERROR: sub" + i + " has no optimization candidate!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
							allCandidateListForFineOpti.add(candiListOfCurrentSubForFineOpti);
						} // for all subjects in fine optimization
						optimizeResult optiFineResult = this.optimizeExamplers(allCandidateListForFineOpti);
						optiResultOfCurrentGrid = optiFineResult.optiResult;
						// ***end of fine optimization
						totalInnerDis = totalInnerDis + optiFineResult.innerDistance;
						String tmpStr = gridIndex + " " + round;
						for (int i = 0; i < this.virSubList.size(); i++)
							tmpStr = tmpStr + " " + optiResultOfCurrentGrid.get(i).optiPtID;
						tmpStr = tmpStr + "\r\n";
						fw.write(tmpStr);
						System.out.println("write:" + tmpStr);
						convergeCount++;
						break;
					} // if converge
				} // if round > 0
				allOptiResult.add(optiResultOfCurrentGrid);
			} // for all round
		} // for all grids
		if (convergeCount != 0)
			totalInnerDis = totalInnerDis / convergeCount;
		else
			totalInnerDis = 0.0;
		fw.close();
		sql = "update test.t_modelInfo set innerDistance=" + totalInnerDis + ",convergeNum=" + convergeCount + " where subList='" + strSubList + "'";
		DatabaseTool.executeUpdate(sql);
		System.out.println("exc sql: " + sql + "...");
		System.out.println("Total " + convergeCount + " grid converge....");
	}

	public optimizeResult optimizeExamplers(List<List<gridCandidate>> allCandidateList) {
		optimizeResult optiResult = new optimizeResult();
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
				// System.out.println(subCombination);
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
			} // for
		} // while

		for (int i = 0; i < size; i++) {
			gridCandidate optiGridOfCurrentSub = allCandidateList.get(i).get(subCombinationWithMinDis.get(i));
			optiGridOfCurrentSub.findOptiGridBasedOnOptiPt();
			optiResultOfCurrentGrid.add(optiGridOfCurrentSub);
		}
		optiResult.optiResult = optiResultOfCurrentGrid;
		optiResult.innerDistance = disMin;
		return optiResult;
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

	private void fillDisDictionary(List<List<gridCandidate>> allCandidateList) {
		disDictionary = new double[allCandidateList.size()][this.nMaxCandidateNum][allCandidateList.size()][this.nMaxCandidateNum];
		for (int i = 0; i < allCandidateList.size(); i++)// 5
		{
			System.out.println("calculating the distance of the " + i + "th  sub to other subs... ");
			for (int j = 0; j < allCandidateList.get(i).size(); j++)// mostly
																	// 0~4
			{
				for (int m = i + 1; m < allCandidateList.size(); m++) {
					for (int n = 0; n < allCandidateList.get(m).size(); n++) {
						gridCandidate c1 = allCandidateList.get(i).get(j);
						gridCandidate c2 = allCandidateList.get(m).get(n);
						disDictionary[i][j][m][n] = this.calFeatureDis(c1.feature, c2.feature);
					} // for n
				} // for m
			} // for j
		} // for i
	}

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

	public boolean checkConverge(List<gridCandidate> currentResult, List<gridCandidate> preResult) {
		boolean isConverge = true;
		if (currentResult.size() != preResult.size()) {
			System.out.println("Error in checkConverge: the size of list are not equal!!");
		}
		for (int i = 0; i < currentResult.size(); i++) {
			if (currentResult.get(i).optiPtID != preResult.get(i).optiPtID)
				isConverge = false;
		}
		return isConverge;
	}

	public void do_validateUsingSubCortical() {
		float allImprove = 0.0f;
		int improveCount = 0;
		for (int roiID = 0; roiID < 16; roiID++) {
			System.out.println("Start to deal with roi" + roiID + "****************************");
			double[][] currentPatternBefore = new double[15][6];
			double[][] currentPatternAfter = new double[15][6];
			int offSet=0;
			if(roiID>7)
				offSet=1;
			for (int subIndex = 0; subIndex < 15; subIndex++) // for all subs in the subList
			{
				int nVirSubID = subIndex;// this.virSubList.get(subIndex);
				int nRealSubID = (int) findModelDictionary.subRelationM.get(nVirSubID, 1);
				AbstractMatrix currentSubPatternDic = Matrix.fromASCIIFile(new File("./vertexPattern/" + nRealSubID + "_VertexCSPattern.txt"));

				int ptIDBefore = (int) findModelDictionary.workMemBefore.get(nVirSubID, roiID);
				int ptIDAfter = (int) findModelDictionary.workMemAfter.get(nVirSubID, roiID);
				for (int i = offSet,j=0; i < 12; i=i+2,j++) {
					currentPatternBefore[subIndex][j] = currentSubPatternDic.get(ptIDBefore, i);
					currentPatternAfter[subIndex][j] = currentSubPatternDic.get(ptIDAfter, i);
				} // for i
				currentSubPatternDic = null;
			} // for sub
			
			int disBefore=0;
			int disAfter=0;
			for(int i=0;i<14;i++)
			{
				for(int j=i+1;j<15;j++)
				{
					for(int k=0;k<6;k++)
					{
						disBefore = disBefore+ (int)Math.abs( currentPatternBefore[i][k]-currentPatternBefore[j][k] );
						disAfter = disAfter+ (int)Math.abs( currentPatternAfter[i][k]-currentPatternAfter[j][k] );
					}
				}
			}
			System.out.println("disBefore is : "+disBefore);
			System.out.println("disAfter is : "+disAfter);
			if(disAfter<disBefore)
			{
				float improve = 0.0f;
				improve = (float)(disBefore-disAfter)/disBefore;
				allImprove += improve;
				improveCount++;
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$improve is : "+improve);
			}			
//			findModelUtil.writeArrayListToFile(currentPatternBefore, 15, 6, "./vertexPattern/roi" + roiID + "_SubCorticalPatternBefore.txt");
//			findModelUtil.writeArrayListToFile(currentPatternAfter, 15, 6, "./vertexPattern/roi" + roiID + "_SubCorticalPatternAfter.txt");

		} //for all roi
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$Evergy improve is : "+allImprove/improveCount);
	}

	public void do_validateUsingFMRI(int ringNum) {
		System.out.println("Begin to do_validateUsingFMRI...");
		List<double[][]> tsOfAllSubBefore = new ArrayList<double[][]>();
		List<double[][]> tsOfAllSubAfter = new ArrayList<double[][]>();
		double[][] meanOfAllSubBefore = new double[16][16];
		double[][] meanOfAllSubAfter = new double[16][16];
		double[] zChange = new double[16];
		List<String> zChangeList = new ArrayList<String>();
		List<String> tsChange = new ArrayList<String>();
		for (int subIndex = 0; subIndex < this.virSubList.size(); subIndex++) // for all subs in the subList
		{
			String currentZChange = "";
			String currentTsChange = "";
			System.out.println("Start to deal with sub" + subIndex + "****************************");
			PearsonsCorrelation correlate = new PearsonsCorrelation();
			int nVirSubID = this.virSubList.get(subIndex);
			int nRealSubID = (int) findModelDictionary.subRelationM.get(nVirSubID, 1);
			djVtkSurData currentSurData = new djVtkSurData("./data/" + nRealSubID + ".surf.vtk");
			djNiftiData currentFMRIData = new djNiftiData("./data/fmri/sub" + nRealSubID + ".zmap", "./data/" + nRealSubID + ".b0.mhd");
			djNiftiData currentGMData = new djNiftiData("./data/fmri/" + nRealSubID + ".gm", "./data/" + nRealSubID + ".b0.mhd");
			List<double[]> tsOfAllROIBefore = new ArrayList<double[]>();
			List<double[]> tsOfAllROIAfter = new ArrayList<double[]>();
			for (int roiID = 0; roiID < 16; roiID++) {
				int ptIDBefore = (int) findModelDictionary.workMemBefore.get(nVirSubID, roiID);
				int ptIDAfter = (int) findModelDictionary.workMemAfter.get(nVirSubID, roiID);
				Set<djVtkPoint> ptsBefore = currentSurData.getNeighbourPoints(ptIDBefore, ringNum);
				Set<djVtkPoint> ptsAfter = currentSurData.getNeighbourPoints(ptIDAfter, ringNum);
				int tDim = currentFMRIData.tSize;
				double[] tsBefore = new double[tDim];
				double[] tsAfter = new double[tDim];
				// calculate the ts before optimization
				Iterator itBefore = ptsBefore.iterator();
				while (itBefore.hasNext()) {
					List<Float> tsOfCurrentPt = new ArrayList<Float>();
					djVtkPoint tmpPoint = (djVtkPoint) itBefore.next();
					for (int t = 0; t < tDim; t++) {
//						float tmpValue = currentFMRIData.getValueBasedOnPhysicalCoordinate(tmpPoint.x, tmpPoint.y, tmpPoint.z, t);
						float tmpValue = currentFMRIData.getValueBasedOnPhysicalCoordinateRange(tmpPoint.x, tmpPoint.y, tmpPoint.z, t);
						tsBefore[t] += tmpValue;
					}
				}
				// calculate the ts after optimization
				Iterator itAfter = ptsAfter.iterator();
				while (itAfter.hasNext()) {
					List<Float> tsOfCurrentPt = new ArrayList<Float>();
					djVtkPoint tmpPoint = (djVtkPoint) itAfter.next();
					for (int t = 0; t < tDim; t++) {
						float tmpValue = currentFMRIData.getValueBasedOnPhysicalCoordinate(tmpPoint.x, tmpPoint.y, tmpPoint.z, t);
						tsAfter[t] += tmpValue;
					}
				}
				// calculate the mean
				for (int t = 0; t < tDim; t++) {
					tsBefore[t] /= ptsBefore.size();
					tsAfter[t] /= ptsAfter.size();
				}
				tsOfAllROIBefore.add(tsBefore);
				tsOfAllROIAfter.add(tsAfter);
//				double tmpChange = Math.abs(correlate.correlation(tsBefore, tsAfter)); 
				double tmpChange=0.0;
				if( Math.abs(tsBefore[0])<0.00001 && Math.abs(tsAfter[0])<0.00001)
					tmpChange=0.0;
				else if(Math.abs(tsBefore[0])<0.00001)
					tmpChange=1.0;
				else
					tmpChange= Math.abs(tsBefore[0] - tsAfter[0])/tsBefore[0];
				currentTsChange = currentTsChange+tmpChange+" ";
//				System.out.println(tsBefore[0]+" "+tsAfter[0]);
				zChange[roiID] = zChange[roiID] + tmpChange;
				currentZChange = currentZChange+tmpChange+" ";
			} // for all rois
			tsChange.add(currentTsChange);
			
			zChangeList.add(currentZChange);

//			double[][] tsCorreMatrixBefore = new double[16][16];
//			double[][] tsCorreMatrixAfter = new double[16][16];
//			for (int i = 0; i < 16; i++) {
//				for (int j = 0; j < 16; j++) {
//					double[] tmp1Before = tsOfAllROIBefore.get(i);
//					double[] tmp2Before = tsOfAllROIBefore.get(j);
//					tsCorreMatrixBefore[i][j] = Math.abs(correlate.correlation(tmp1Before, tmp2Before));
//					double[] tmp1After = tsOfAllROIAfter.get(i);
//					double[] tmp2After = tsOfAllROIAfter.get(j);
//					tsCorreMatrixAfter[i][j] = Math.abs(correlate.correlation(tmp1After, tmp2After));
//				} // for j
//			} // for i
//			findModelUtil.writeArrayListToFile(tsCorreMatrixBefore, 16, 16, nVirSubID + ".tsRelationMatrixBefore.txt");
//			findModelUtil.writeArrayListToFile(tsCorreMatrixAfter, 16, 16, nVirSubID + ".tsRelationMatrixAfter.txt");
//			findModelUtil.writeVtkMatrix(tsCorreMatrixBefore, 16, 16, nVirSubID + ".tsRelationMatrixBefore.vtk");
//			findModelUtil.writeVtkMatrix(tsCorreMatrixAfter, 16, 16, nVirSubID + ".tsRelationMatrixAfter.vtk");
//			tsOfAllSubBefore.add(tsCorreMatrixBefore);
//			tsOfAllSubAfter.add(tsCorreMatrixAfter);
//			for (int i = 0; i < 16; i++) {
//				for (int j = 0; j < 16; j++) {
//					meanOfAllSubBefore[i][j] += tsCorreMatrixBefore[i][j];
//					meanOfAllSubAfter[i][j] += tsCorreMatrixAfter[i][j];
//				}
//			}

			// calculate the change of distance
			// double[][] tsSDMatrixBefore = new double[16][16];
			// double[][] tsSDMatrixAfter = new double[16][16];
			// double meanBefore = 0.0;
			// double meanAfter = 0.0;
			// for (int i = 0; i < 16; i++)
			// for (int j = 0; j < 16; j++) {
			// meanBefore += tsCorreMatrixBefore[i][j];
			// meanAfter += tsCorreMatrixAfter[i][j];
			// }
			// meanBefore /= (16 * 16);
			// meanAfter /= (16 * 16);
			// for (int i = 0; i < 16; i++)
			// for (int j = 0; j < 16; j++) {
			// tsSDMatrixBefore[i][j] = Math.abs(tsCorreMatrixBefore[i][j] - meanBefore);
			// tsSDMatrixAfter[i][j] = Math.abs(tsCorreMatrixAfter[i][j] - meanAfter);
			// }
			// findModelUtil.writeArrayListToFile(tsSDMatrixBefore, 16, 16, nVirSubID + ".tsSDMatrixBefore.txt");
			// findModelUtil.writeArrayListToFile(tsSDMatrixAfter, 16, 16, nVirSubID + ".tsSDMatrixAfter.txt");
			// ******************************
			tsOfAllROIBefore = null;
			tsOfAllROIAfter = null;
			currentSurData = null;
			currentFMRIData = null;
		} // for all subjects
		////////////////////////////////
		djVtkUtil.writeArrayListToFile(zChangeList, "0619ZChange_"+ringNum+".txt");
		for(int z=0;z<16;z++)
		{
			zChange[z] = zChange[z]/this.virSubList.size();
			System.out.println(zChange[z]);
		}
		//////////////////////////////
		
			// calculate the mean
//		for (int i = 0; i < 16; i++) {
//			for (int j = 0; j < 16; j++) {
//				meanOfAllSubBefore[i][j] /= this.virSubList.size();
//				meanOfAllSubAfter[i][j] /= this.virSubList.size();
//			} // for j
//		} // for i
//
//		double[][] tsSDMatrixBefore = new double[16][16];
//		double[][] tsSDMatrixAfter = new double[16][16];
//		
//		Map<Integer,List<String>> roi0ListBefore = new HashMap<Integer,List<String>>();
//		Map<Integer,List<String>> roi0ListAfter = new HashMap<Integer,List<String>>();
//		for(int i=0;i<16;i++)
//		{
//			List<String> newList1 = new ArrayList<String>();
//			roi0ListBefore.put(i, newList1);
//			List<String> newList2 = new ArrayList<String>();
//			roi0ListAfter.put(i, newList2);
//		}
//		
//		for (int subIndex = 0; subIndex < this.virSubList.size(); subIndex++) // for all subs in the subList
//		{
//
//			System.out.println("Start to compute the sd with sub" + subIndex + "****************************");
//			int nVirSubID = this.virSubList.get(subIndex);
//			double[][] tmpMatrixBefore = tsOfAllSubBefore.get(subIndex);
//			double[][] tmpMatrixAfter = tsOfAllSubAfter.get(subIndex);
//			for (int i = 0; i < 16; i++) {
//				roi0ListBefore.get(i).add( String.valueOf(tmpMatrixBefore[0][i]) );
//				roi0ListAfter.get(i).add( String.valueOf(tmpMatrixAfter[0][i]) );
//				////////////////////////////////
//				for (int j = 0; j < 16; j++) {
//					tsSDMatrixBefore[i][j] += (tmpMatrixBefore[i][j] - meanOfAllSubBefore[i][j]) * (tmpMatrixBefore[i][j] - meanOfAllSubBefore[i][j]);
//					tsSDMatrixAfter[i][j] += (tmpMatrixAfter[i][j] - meanOfAllSubAfter[i][j]) * (tmpMatrixAfter[i][j] - meanOfAllSubAfter[i][j]);
//				} // for j
//			} // for i
//			///////////////
//		} // for all subs
////		findModelUtil.writeArrayListToFile(tsSDMatrixBefore, 16, 16, "tsSDMatrixBefore.txt");
////		findModelUtil.writeArrayListToFile(tsSDMatrixAfter, 16, 16, "tsSDMatrixAfter.txt");
////		findModelUtil.writeVtkMatrix(tsSDMatrixBefore, 16, 16, "tsSDMatrixBefore.vtk");
////		findModelUtil.writeVtkMatrix(tsSDMatrixAfter, 16, 16, "tsSDMatrixAfter.vtk");
//		///////////////////////////////
////		for(int i=1;i<16;i++)
////		{
////			djVtkUtil.writeArrayListToFile(roi0ListBefore.get(i), "Journal_roi0_roi"+i+"_Before.txt");
////			djVtkUtil.writeArrayListToFile(roi0ListAfter.get(i), "Journal_roi0_roi"+i+"_After.txt");
////		}
//		
//		djVtkUtil.writeArrayListToFile(tsChange, "allCorre_"+ringNum+".txt");

	}

}
