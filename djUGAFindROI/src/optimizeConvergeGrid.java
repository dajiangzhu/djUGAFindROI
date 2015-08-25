

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import edu.uga.liulab.djVtkBase.djVtkData;
import edu.uga.liulab.djVtkBase.djVtkDataDictionary;
import edu.uga.liulab.djVtkBase.djVtkPoint;
import edu.uga.liulab.djVtkBase.djVtkSurData;
import edu.uga.liulab.djVtkBase.djVtkUtil;

public class optimizeConvergeGrid {

	public static AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File(
			"./data/subRIndex.txt")); // 19*2
	public static AbstractMatrix gridPointM = Matrix.fromASCIIFile(new File(
			"./data/GridPointMapping.txt")); // 2056*15
	public static AbstractMatrix gridPoint_chInitial_1_M = Matrix
			.fromASCIIFile(new File("./data/GridPointMapping_CHInitial_1.txt")); // 2056*15
	public static int nTempSub = 19;
	public static int nGridNum = 2056;
	public static int ringScale = 3;
	public static int nFeatureDim = 144;
	public static int nClusterNum = 5;
	public static int nIteration = 100;
	// ///////////////////////////////////
	public List<Integer> subList = new ArrayList<Integer>();
	public int nSubNum;
	public int nGrindMin;
	public int nGrindMax;
	public int nMoveRange;
	public int nMaxCandidateNum = 80;
	public int nAnathreshold = 3;
	public int subNumWithinGroup = 5;
	public Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();
	// ////////////////////////////////
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
		disDictionary = new double[allCandidateList.size()][this.nMaxCandidateNum][allCandidateList
				.size()][this.nMaxCandidateNum];
		for (int i = 0; i < allCandidateList.size(); i++)// 5
		{
			System.out.println("calculating the distance of  sub" + i
					+ " to other subs... ");
			for (int j = 0; j < allCandidateList.get(i).size(); j++)// mostly
																	// 0~4
			{
				for (int m = i + 1; m < allCandidateList.size(); m++) {
					for (int n = 0; n < allCandidateList.get(m).size(); n++) {
						gridCandidate c1 = allCandidateList.get(i).get(j);
						gridCandidate c2 = allCandidateList.get(m).get(n);
						disDictionary[i][j][m][n] = this.calFeatureDis(
								c1.feature, c2.feature);
					} // for n
				} // for m
			} // for j
		} // for i

	}

	private double calDisOfGroup(List<Integer> subCombination) {
		double sumDis = 0.0;
		for (int i = 0; i < subCombination.size() - 1; i++) {
			for (int j = i + 1; j < subCombination.size(); j++) {
				sumDis += disDictionary[i][subCombination.get(i)][j][subCombination
						.get(j)];
			}
		}
		return sumDis;
	}

	public List<gridCandidate> optimizeExamplers(
			List<List<gridCandidate>> allCandidateList) {
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
			gridCandidate optiGridOfCurrentSub = allCandidateList.get(i).get(
					subCombinationWithMinDis.get(i));
			optiGridOfCurrentSub.findOptiGridBasedOnOptiPt();
			optiResultOfCurrentGrid.add(optiGridOfCurrentSub);
		}
		return optiResultOfCurrentGrid;
	}

	public void chooseExampler() throws IOException {
		System.out.println("Entering chooseExampler...");
		FileWriter fw = null;
		fw = new FileWriter(this.nGrindMin + "_" + this.nGrindMax
				+ "_ConvergeInfo.txt");
		int convergeCound = 0;

		for (int gridIndex = this.nGrindMin; gridIndex <= this.nGrindMax; gridIndex++) {
			System.out
					.println("dealing with the "
							+ gridIndex
							+ "th grid----------------------------------------------------------------------------");

			List<gridCandidate> optiResultOfCurrentGrid = new ArrayList<gridCandidate>();
			for (int subIndex = 0; subIndex < this.subList.size(); subIndex++) {
				int nVirSubID = this.subList.get(subIndex);
				int ptID = (int) gridPoint_chInitial_1_M.get(gridIndex - 1,
						nVirSubID);
				gridCandidate newCandidate = new gridCandidate();
				newCandidate.virSubID = nVirSubID;
				newCandidate.realSubID = (int) optimizeGrids.subRelationM.get(
						nVirSubID, 1);
				newCandidate.optiPtID = ptID;
				optiResultOfCurrentGrid.add(newCandidate);
			}
			for (int round = 0; round < 10; round++) {
				System.out
						.println("doning the "
								+ round
								+ "th round of optimization..................................................................");

				List<List<gridCandidate>> allCandidateList = new ArrayList<List<gridCandidate>>();
				for (int subIndex = 0; subIndex < this.subList.size(); subIndex++) // for
																					// subs
				{
					System.out.println("dealing with gird=" + gridIndex
							+ " and sub=" + subIndex);
					List<gridCandidate> candiListOfCurrentSub = new ArrayList<gridCandidate>();
					List<List<Double>> neighboursFeatureList = new ArrayList<List<Double>>();
					int nVirSubID = this.subList.get(subIndex);
					int nRealSubID = (int) subRelationM.get(nVirSubID, 1);
					// djVtkSurData currentSurData = new djVtkSurData("./data/"
					// + nRealSubID + ".surf.vtk");
					djVtkSurData currentSurData;
					if (this.surDataMap.containsKey(nRealSubID)) {
						currentSurData = surDataMap.get(nRealSubID);
					} else {
						currentSurData = new djVtkSurData("./data/"
								+ nRealSubID + ".surf.vtk");
						surDataMap.put(nRealSubID, currentSurData);
					}
					AbstractMatrix featuresOfCurrentSubM = Matrix
							.fromASCIIFile(new File("./data/" + nRealSubID
									+ "_3_featureList.txt")); // 2056*15
					// int ptID = (int) gridPointM.get(gridIndex - 1,
					// nVirSubID); //***************modify if chang initial
					// grids**********************
					// int ptID = (int) gridPoint_chInitial_1_M.get(gridIndex -
					// 1, nVirSubID);
					int ptID = optiResultOfCurrentGrid.get(subIndex).optiPtID;

					// prepare neighboursFeatureList
					for (int ptIndex = 0; ptIndex < currentSurData.nPointNum; ptIndex++) {
						List<Double> tmpPtFeature = new ArrayList<Double>();
						for (int i = 0; i < optimizeGrids.nFeatureDim; i++) {
							tmpPtFeature.add(featuresOfCurrentSubM.get(ptIndex,
									i));
						}
						neighboursFeatureList.add(tmpPtFeature);
					}

					// get neighbours based on seedPt=ptID and
					// nMoveRange(default step is 2, the total range is
					// 2*nMoveRange(3 or 4 in general))
					Set<djVtkPoint> candidatePoints = currentSurData
							.decimatePatch(ptID, 2, this.nMoveRange);
					Iterator itCandidatePoints = candidatePoints.iterator();
					while (itCandidatePoints.hasNext()) {
						djVtkPoint tmpPoint = (djVtkPoint) itCandidatePoints
								.next();
						gridCandidate newCandidate = new gridCandidate();
						newCandidate.virSubID = nVirSubID;
						newCandidate.realSubID = (int) optimizeGrids.subRelationM
								.get(nVirSubID, 1);
						newCandidate.surData = this.surDataMap
								.get(newCandidate.realSubID);
						newCandidate.oriGridID = gridIndex;
						newCandidate.oriPtID = ptID;
						newCandidate.optiPtID = tmpPoint.pointId;
						newCandidate.feature = neighboursFeatureList
								.get(tmpPoint.pointId);
						candiListOfCurrentSub.add(newCandidate);
					}
					allCandidateList.add(candiListOfCurrentSub);
				} // for all subs

				// begin to optimize
				optiResultOfCurrentGrid = this
						.optimizeExamplers(allCandidateList);
				if (round > 0) {
					boolean isConverge = this.checkConverge(
							optiResultOfCurrentGrid,
							allOptiResult.get(allOptiResult.size() - 1));
					if (isConverge) {
						System.out
								.println("converge on round:"
										+ round
										+ "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
						String tmpStr = gridIndex + " " + round + " ";
						for (int i = 0; i < this.subList.size(); i++)
							tmpStr = tmpStr + " "
									+ optiResultOfCurrentGrid.get(i).optiPtID;
						tmpStr = tmpStr + "\r\n";
						fw.write(tmpStr);
						convergeCound++;
						break;
					}
				}
				allOptiResult.add(optiResultOfCurrentGrid);
				this.printOptiResultInfoToConsole(optiResultOfCurrentGrid);
			} // for all round

			// allOptiResult.add(optiResultOfCurrentGrid);
		} // for all grids
		fw.close();

		// print
		this.printOptiResult(allOptiResult, "convergeDetail");
		System.out.println("Total " + convergeCound + " grid converge....");
		// this.printOptiResult(this.filterResult(this.nAnathreshold),
		// "threshold" + this.nAnathreshold);
		// int tmpThreshold = this.nAnathreshold + 1;
		// this.printOptiResult(this.filterResult(tmpThreshold), "threshold" +
		// tmpThreshold);

	}

	public boolean checkConverge(List<gridCandidate> currentResult,
			List<gridCandidate> preResult) {
		boolean isConverge = true;
		if (currentResult.size() != preResult.size()) {
			System.out
					.println("Error in checkConverge: the size of list are not equal!!");
		}
		for (int i = 0; i < currentResult.size(); i++) {
			if (currentResult.get(i).optiPtID != preResult.get(i).optiPtID)
				isConverge = false;
		}

		return isConverge;
	}

	public void printOptiResultInfoToConsole(List<gridCandidate> result) {
		for (int i = 0; i < result.size(); i++) {
			System.out.println("subID=" + result.get(i).realSubID
					+ " optimizedPtID=" + result.get(i).optiPtID);
		}
	}

	public void printOptiResult(List<List<gridCandidate>> resultToPrint,
			String descriptor) {
		System.out.println("Beging to output the optimiresult...");
		String fileName = "optimizationResult_" + this.nGrindMin + "_"
				+ this.nGrindMax + "_" + descriptor + ".txt";
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName);
			for (int grid = 0; grid < resultToPrint.size(); grid++) {
				for (int sub = 0; sub < resultToPrint.get(grid).size(); sub++) {
					gridCandidate currentCandidate = resultToPrint.get(grid)
							.get(sub);
					// oriGridID+optiGridID+subID+oriGridPointID+optiGridPointID+optiPointID
					String strInfo = currentCandidate.oriGridID + " "
							+ currentCandidate.optiGridID + " "
							+ currentCandidate.virSubID + " "
							+ currentCandidate.oriPtID + " "
							+ currentCandidate.optiGridPtID + " "
							+ currentCandidate.optiPtID;
					fw.write(strInfo + "\r\n");
				} // for sub
			} // for grid
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

	public void analyzeConvergeFiles() throws IOException {
		int groupNum = 1;

		List<gridConvergeInfo> gridConvergeInfoList = new ArrayList<gridConvergeInfo>();

		String fileName = "./data/convergeInfo_g"+groupNum+".txt";
		String fileName1 = "./data/convergeInfoDetail_g"+groupNum+".txt";
		String outputFileName = "convergeInfo_g1_analyze.txt";

		FileInputStream fstream = new FileInputStream(fileName);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		String[] tmpStringArray;

		int count = 0;

		while ((strLine = br.readLine()) != null) {
			// System.out.println(strLine);
			tmpStringArray = strLine.split(" ");
			if (tmpStringArray.length > 0) {
				System.out.println("grid:" + tmpStringArray[1]);
				System.out.println("round:" + tmpStringArray[5]);
				int gridIndex = Integer.valueOf(tmpStringArray[1]);
				int roundNum = Integer.valueOf(tmpStringArray[5]);
				if (roundNum <= 3) {
					gridConvergeInfo newGridConvergeInfo = new gridConvergeInfo();
					newGridConvergeInfo.gridID = gridIndex;
					newGridConvergeInfo.convergeRoundNum = roundNum;

					FileInputStream fstream1 = new FileInputStream(fileName1);
					DataInputStream in1 = new DataInputStream(fstream1);
					BufferedReader br1 = new BufferedReader(
							new InputStreamReader(in1));
					String strLine1;
					String[] tmpStringArray1;
					while ((strLine1 = br1.readLine()) != null) {
						tmpStringArray1 = strLine1.split(" ");
						if (tmpStringArray1.length > 0) {
							if (tmpStringArray1[0]
									.equalsIgnoreCase(tmpStringArray[1])) {
								for (int roundIndex = 0; roundIndex < roundNum; roundIndex++) {
									for (int subIndex = 0; subIndex < this.subNumWithinGroup; subIndex++) {
										newGridConvergeInfo.convergeTrace
												.get(subIndex)
												.add(Integer
														.valueOf(tmpStringArray1[5]));
//										strLine1 = br1.readLine();
										if( (strLine1 = br1.readLine()) != null )
										tmpStringArray1 = strLine1.split(" ");
										else
											System.out.println("The end of the file.");
									}
								}
							} // if
						} // if
					} // while
					count++;
					gridConvergeInfoList.add(newGridConvergeInfo);
				} // if
			} // if
		} // while
		br.close();
		in.close();
		fstream.close();
		System.out.println("the total grid num which converg in 3 round is : "
				+ count);

		// ############################################output the converge
		// points####################################
		for (int s = 0; s < this.subNumWithinGroup; s++) {
			List<djVtkPoint> tmpPtList = new ArrayList<djVtkPoint>();
			Set<Integer> tmpPtSet = new HashSet<Integer>();
			int nVirtualSubID = (groupNum-1)*this.subNumWithinGroup+s;
			int nRealSubID = (int) subRelationM.get(nVirtualSubID, 1);
			djVtkSurData currentSurData;
			if (this.surDataMap.containsKey(nRealSubID)) {
				currentSurData = surDataMap.get(nRealSubID);
			} else {
				currentSurData = new djVtkSurData("./data/" + nRealSubID
						+ ".surf.vtk");
				surDataMap.put(nRealSubID, currentSurData);
			}
			for (int i = 0; i < gridConvergeInfoList.size(); i++) {
				int tmpPtID = gridConvergeInfoList.get(i).convergeTrace.get(s)
						.get(gridConvergeInfoList.get(i).convergeTrace.get(s)
								.size() - 1);
				tmpPtSet.add(tmpPtID);
			}
			Iterator iteratorTmpPtSet = tmpPtSet.iterator();
			while (iteratorTmpPtSet.hasNext()) {
				int ptID = (Integer) iteratorTmpPtSet.next();
				djVtkPoint tmpPt = currentSurData.getPoint(ptID);
				tmpPtList.add(tmpPt);
			}
			djVtkUtil.writeToPointsVtkFile("sub" + nVirtualSubID + "_convergePoints.vtk",
					tmpPtList);

		}

		// ############################################output the converge
		// points End! ####################################

		// ############################################output the analyze
		// file####################################
		// FileWriter fw = null;
		// fw = new FileWriter(outputFileName);
		// for(int i=0;i<gridConvergeInfoList.size();i++)
		// {
		// fw.write("Grid "+gridConvergeInfoList.get(i).gridID+" ####################################\r\n");
		// for(int j=0;j<this.subNumWithinGroup;j++)
		// {
		// fw.write("sub"+j+" : ");
		// for(int
		// k=0;k<gridConvergeInfoList.get(i).convergeTrace.get(j).size();k++)
		// fw.write(gridConvergeInfoList.get(i).convergeTrace.get(j).get(k)+" ");
		// fw.write("\r\n");
		// }
		// }
		// fw.close();
		// ############################################output the analyze file
		// End! ####################################

	}

	public static void main(String[] args) throws IOException {
		// if (args.length < 4) {
		// System.out
		// .println("format:java -Xmx3000m -jar *.jar subNum subList(eg. 0 1 2) gridMin(>=1) gridMax(<=2056) move-range");
		// } else {
		// // initialization
		// int i = 0;
		// optimizeConvergeGrid optimizeHandler = new optimizeConvergeGrid();
		// optimizeHandler.nSubNum = Integer.valueOf(args[i]);
		//
		// for (i = 1; i < optimizeHandler.nSubNum + 1; i++) {
		// optimizeHandler.subList.add(Integer.valueOf(args[i]));
		// }
		// optimizeHandler.nGrindMin = Integer.valueOf(args[i++]);
		// optimizeHandler.nGrindMax = Integer.valueOf(args[i++]);
		// optimizeHandler.nMoveRange = Integer.valueOf(args[i++]);
		//
		// // Begin to work..
		// optimizeHandler.chooseExampler();
		//
		// } // else, means valid inputs
		// ***************************************************************************
		optimizeConvergeGrid optimizeHandler = new optimizeConvergeGrid();
		optimizeHandler.analyzeConvergeFiles();
	}

}
