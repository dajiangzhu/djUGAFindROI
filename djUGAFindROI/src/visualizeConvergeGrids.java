

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import edu.uga.liulab.djVtkBase.*;

public class visualizeConvergeGrids {

	public static AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File("./data/subRIndex.txt")); // 19*2
	public static AbstractMatrix gridPointM = Matrix.fromASCIIFile(new File("./data/GridPointMapping.txt")); // 2056*15
	public static AbstractMatrix gridPoint_chInitial_1_M = Matrix.fromASCIIFile(new File("./data/GridPointMapping_CHInitial_1.txt")); // 2056*15
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
					String strInfo = currentCandidate.oriGridID + " " + currentCandidate.optiGridID + " " + currentCandidate.virSubID + " "
							+ currentCandidate.oriPtID + " " + currentCandidate.optiGridPtID + " " + currentCandidate.optiPtID;
					fw.write(strInfo + "\r\n");
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

	public void analyzeConvergeFiles() throws IOException {
		int groupIDStart = 1;
		int groupIDEnd = 2;
		List<List<String>> listForKaiming = new ArrayList<List<String>>();
		List<String> gridIndexList = new ArrayList<String>();
		boolean switchWriteOutputConvergePt = false;
		boolean switchWriteOutputConvergePtFiber = false;
		int convergeRoudT = 3;
		List<Set<Integer>> convergeGridIDInfo = new ArrayList<Set<Integer>>();
		int convergeNum = 0;
		for (int groupID = groupIDStart; groupID < groupIDEnd; groupID++) {
			System.out.println("group:" + groupID);
			Set<Integer> newconvergeGridIDSet = new HashSet<Integer>();
			String fileName = "./data/convergeInfo_g" + groupID + ".txt";
			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String[] tmpStringArray;
			while ((strLine = br.readLine()) != null) {
				tmpStringArray = strLine.split(" ");
				if (tmpStringArray.length > 0) {
					System.out.println("grid:" + tmpStringArray[1]);
					System.out.println("round:" + tmpStringArray[5]);
					int gridIndex = Integer.valueOf(tmpStringArray[1]);
					int roundNum = Integer.valueOf(tmpStringArray[5]);
					if (roundNum <= convergeRoudT) {
						newconvergeGridIDSet.add(gridIndex);
					} // if
				} // if
			} // while
			convergeGridIDInfo.add(newconvergeGridIDSet);
			br.close();
			in.close();
			fstream.close();
		} // for

		// find common converge grid
		Iterator iterSet = convergeGridIDInfo.get(0).iterator();
		Set<Integer> convergeGridCommon = new HashSet<Integer>();
		while (iterSet.hasNext()) {
			int currentGridID = (Integer) iterSet.next();
			boolean flag = true;
			for (int i = 1; i < convergeGridIDInfo.size(); i++) {
				if (!convergeGridIDInfo.get(i).contains(currentGridID))
					flag = false;
			} // for
			if (flag)
				convergeGridCommon.add(currentGridID);
		} // while
		System.out.println("there are " + convergeGridCommon.size() + " grids converge in all the groups..");

		// generate the color map
		List<String> attriList = new ArrayList<String>();
		iterSet = convergeGridCommon.iterator();
		Random generator = new Random(System.currentTimeMillis());
		while (iterSet.hasNext()) {
			iterSet.next();
			int c1 = generator.nextInt(256);
			int c2 = generator.nextInt(256);
			int c3 = generator.nextInt(256);
			attriList.add(c1 + " " + c2 + " " + c3 + "\r\n");
		}
		System.out.println("generate color map done...");

		// second round scan
		for (int groupID = groupIDStart; groupID < groupIDEnd; groupID++) {
			System.out.println("group:" + groupID);
			List<gridConvergeInfo> gridConvergeInfoList = new ArrayList<gridConvergeInfo>();
			String fileName = "./data/convergeInfo_g" + groupID + ".txt";
			String fileName1 = "./data/convergeInfoDetail_g" + groupID + ".txt";
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
					// System.out.println("grid:" + tmpStringArray[1]);
					// System.out.println("round:" + tmpStringArray[5]);
					int gridIndex = Integer.valueOf(tmpStringArray[1]);
					int roundNum = Integer.valueOf(tmpStringArray[5]);
					if (convergeGridCommon.contains(gridIndex)) {
						gridConvergeInfo newGridConvergeInfo = new gridConvergeInfo();
						newGridConvergeInfo.gridID = gridIndex;
						newGridConvergeInfo.convergeRoundNum = roundNum;

						FileInputStream fstream1 = new FileInputStream(fileName1);
						DataInputStream in1 = new DataInputStream(fstream1);
						BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
						String strLine1;
						String[] tmpStringArray1;
						while ((strLine1 = br1.readLine()) != null) {
							tmpStringArray1 = strLine1.split(" ");
							if (tmpStringArray1.length > 0) {
								if (tmpStringArray1[0].equalsIgnoreCase(tmpStringArray[1])) {
									for (int roundIndex = 0; roundIndex < roundNum; roundIndex++) {
										for (int subIndex = 0; subIndex < this.subNumWithinGroup; subIndex++) {
											newGridConvergeInfo.convergeTrace.get(subIndex).add(Integer.valueOf(tmpStringArray1[5]));
											if ((strLine1 = br1.readLine()) != null)
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
			System.out.println("the total grid num which converg in 3 round is : " + count);
			convergeNum = count;

			// ############################################output the converge
			// points####################################
			for (int s = 0; s < this.subNumWithinGroup; s++) {
				List<djVtkPoint> tmpPtList = new ArrayList<djVtkPoint>();
				Set<Integer> tmpPtSet = new HashSet<Integer>();
				int nVirtualSubID = (groupID - 1) * this.subNumWithinGroup + s;
				int nRealSubID = (int) subRelationM.get(nVirtualSubID, 1);
				djVtkSurData currentSurData;
				djVtkFiberData currentFiberData;
				djVtkHybridData hybridData;
//				currentSurData = new djVtkSurData("./data/" + nRealSubID + ".surf.vtk");
//				currentFiberData = new djVtkFiberData("./data/" + nRealSubID + ".asc.fiber.vtk");
//				hybridData = new djVtkHybridData(currentSurData, currentFiberData);
//				hybridData.mapSurfaceToBox();
//				hybridData.mapFiberToBox();

				String outFileName = "sub" + nVirtualSubID + "_convergePointsWithColors.vtk";
				System.out.println("Begin to write file:" + outFileName + "...");
				List<String> ptsList = new ArrayList<String>();

				int tmpPtIndex = 0;
				List<String> newPtIndexList = new ArrayList<String>();
				for (int i = 1; i < 2056; i++) {
					if (convergeGridCommon.contains(i)) {
						for (int j = 0; j < gridConvergeInfoList.size(); j++) {
							if (i == gridConvergeInfoList.get(j).gridID) {
								int tmpPtID = gridConvergeInfoList.get(j).convergeTrace.get(s).get(
										gridConvergeInfoList.get(j).convergeTrace.get(s).size() - 1);
								// for kaiming
								newPtIndexList.add(String.valueOf(tmpPtID));
								if (listForKaiming.size() == 0)
									gridIndexList.add(String.valueOf(i));
								// for kaiming end
//								if (switchWriteOutputConvergePtFiber) {
//									Set ptSet = currentSurData.getNeighbourPoints(tmpPtID, 3);
//									String fiberFileName = "./fibersOfConverPoints/grid" + i + ".sub" + nVirtualSubID + ".fiber.vtk";
//									hybridData.getFibersConnectToPointsSet(ptSet).writeToVtkFileCompact(fiberFileName);
//								}
//								djVtkPoint tmpPt = currentSurData.getPoint(tmpPtID);
//								ptsList.add(tmpPt.x + " " + tmpPt.y + " " + tmpPt.z + "\r\n");
								System.out.println("ConvergePointID:" + tmpPtIndex + " GridID:" + i);
								tmpPtIndex++;
							} // for
						} // for
					} // if
				} // for
				listForKaiming.add(newPtIndexList);
				// write the output converge points file with color attribute
				if (switchWriteOutputConvergePt) {
					FileWriter fw = null;
					fw = new FileWriter(outFileName);
					fw.write("# vtk DataFile Version 3.0\r\n");
					fw.write("vtk output\r\n");
					fw.write("ASCII\r\n");
					fw.write("DATASET POLYDATA\r\n");
					// print points info
					fw.write("POINTS " + convergeGridCommon.size() + " float\r\n");
					for (int i = 0; i < ptsList.size(); i++) {
						fw.write(ptsList.get(i));
					}
					fw.write("VERTICES " + ptsList.size() + " " + ptsList.size() * 2 + " \r\n");
					for (int i = 0; i < ptsList.size(); i++)
						fw.write("1 " + i + "\r\n");
					fw.write("POINT_DATA " + ptsList.size() + "\r\n");
					fw.write("VECTORS convergeGrid unsigned_char \r\n");
					for (int i = 0; i < ptsList.size(); i++)
						fw.write(attriList.get(i));
					fw.close();
				}

			} // for s
		} // for groupID
		djVtkUtil.writeStringArrayListToFile(listForKaiming, " ", convergeNum, "convergePts_g1.txt");
		djVtkUtil.writeArrayListToFile(gridIndexList, "convergeGridIndex_g1.txt");

	}

	public static void main(String[] args) throws IOException {

		visualizeConvergeGrids optimizeHandler = new visualizeConvergeGrids();
		optimizeHandler.analyzeConvergeFiles();
	}

}
