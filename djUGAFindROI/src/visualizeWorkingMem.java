/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import org.jmat.data.*;
import edu.uga.liulab.djVtkBase.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author dajiang
 */
public class visualizeWorkingMem {

	public static AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File(
			"./data/subRIndex.txt")); // 19*2
	public static AbstractMatrix gridPointM = Matrix.fromASCIIFile(new File(
			"./data/GridPointMapping.txt")); // 2056*15
	public AbstractMatrix groundTruthM = Matrix.fromASCIIFile(new File(
			"./data/allGroundTruthCoords.txt")); // 15*48
	public AbstractMatrix optiResultM;// = Matrix.fromASCIIFile(new
										// File("./data/optimizationResult_1_all.txt"));
	public int nGroupID = -1;
	public int roiID = -1;
	public List<Integer> subList = new ArrayList<Integer>();
	public List<Integer> girdList = new ArrayList<Integer>();
	public Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();

	private void initialSurDataMap() {
		for (int subID = 0; subID < 15; subID++) {
			int nRealSubID = (int) changeInitialGridPoints.subRelationM.get(
					subID, 1);
			djVtkSurData currentSurData = new djVtkSurData("./data/"
					+ nRealSubID + ".surf.vtk");
			this.surDataMap.put(nRealSubID, currentSurData);
		}
	}

	public void printMesh(int virSubID) { // before-3 after-5
		FileWriter fw = null;
		try {
			int rowID = virSubID - (this.nGroupID - 1) * 5;
			int nRealSubID = (int) visualizeOptiResult.subRelationM.get(
					virSubID, 1);
			String resultDir = "./OptiResult/";
			fw = new FileWriter(resultDir + nRealSubID + ".GridTrace.vtk");
			fw.write("# vtk DataFile Version 3.0\r\n");
			fw.write("vtk output\r\n");
			fw.write("ASCII\r\n");
			fw.write("DATASET POLYDATA\r\n");
			fw.write("POINTS 4112 float\r\n");

			System.out.println("Beging to printMesh with virSubID=" + virSubID);

			djVtkSurData currentSurData = surDataMap.get(nRealSubID);
			if (currentSurData == null) {
				currentSurData = new djVtkSurData("./data/" + nRealSubID
						+ ".surf.vtk");
				surDataMap.put(nRealSubID, currentSurData);
			}
			for (int gridIndex = 1; gridIndex <= visualizeOptiResult.nGridNum; gridIndex++) {
				System.out.println("Now dealing with grid=" + gridIndex);
				int ptIDOfGridBefore = (int) optiResultM.get((gridIndex - 1)
						* 5 + rowID, 3);
				int ptIDOfGridAfter = (int) optiResultM.get((gridIndex - 1) * 5
						+ rowID, 5);
				djVtkPoint ptBefore = currentSurData.getPoint(ptIDOfGridBefore);
				djVtkPoint ptAfter = currentSurData.getPoint(ptIDOfGridAfter);
				fw.write(ptBefore.x + " " + ptBefore.y + " " + ptBefore.z
						+ "\r\n");
				fw.write(ptAfter.x + " " + ptAfter.y + " " + ptAfter.z + "\r\n");

			}
			// print LINES info
			fw.write("LINES 2056 6168 \r\n");
			for (int i = 0; i < 2056; i++) {
				fw.write("2 " + (i * 2) + " " + (i * 2 + 1) + "\r\n");
			}

			fw.write("POINT_DATA 4112 \r\n");
			fw.write("SCALARS trace float  \r\n");
			fw.write(" LOOKUP_TABLE default  \r\n");
			for (int i = 0; i < 4112; i++) {
				if (i % 2 == 0) {
					fw.write("0\r\n");
				} else {
					fw.write("1\r\n");
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

	public void prepreVtkFiles(int roiID, int groupID) {
		for (int i = 0; i < this.subList.size(); i++) {
			int virSubID = this.subList.get(i);
			int nRealSubID = (int) visualizeWorkingMem.subRelationM.get(
					virSubID, 1);
			djVtkSurData currentSurData = new djVtkSurData("./data/"
					+ nRealSubID + ".surf.vtk");
			int rowID = virSubID - (this.nGroupID - 1) * 5;

			for (int j = 0; j < this.girdList.size(); j++) {
				List<djVtkPoint> tmpPtList = new ArrayList<djVtkPoint>();
				int gridID = this.girdList.get(j);
				String consistentGridFileName = "./taskValidation/sub"
						+ nRealSubID + "_" + roiID + "_" + gridID + ".vtk";
				int gridPtID = (int) optiResultM.get((gridID - 1) * 5 + rowID,
						5);
				tmpPtList.add(currentSurData.getPoint(gridPtID));
				djVtkUtil.writeToPointsVtkFile(consistentGridFileName,
						tmpPtList);
			}

			String currentTruthFileName = "./taskValidation/sub" + nRealSubID
					+ "_" + roiID + "_truth.vtk";
			List<djVtkPoint> truthPnList = new ArrayList<djVtkPoint>();
			String currentDistributionFileName = "./taskValidation/sub"
					+ nRealSubID + "_" + roiID + "_distribution.vtk";
			List<djVtkPoint> distributionList = new ArrayList<djVtkPoint>();

			djVtkPoint truthPoint = new djVtkPoint();
			truthPoint.x = (float) groundTruthM.get(virSubID, roiID * 3);
			truthPoint.y = (float) groundTruthM.get(virSubID, roiID * 3 + 1);
			truthPoint.z = (float) groundTruthM.get(virSubID, roiID * 3 + 2);
			truthPnList.add(truthPoint);
			String roiCandidateFileName = "./OptiResult/" + nRealSubID
					+ "_After_result_top1000_oneAttri.vtk";
			djVtkSurData roiCandidateData = new djVtkSurData(
					roiCandidateFileName);
			for (int ptID = 0; ptID < roiCandidateData.nPointNum; ptID++) {
				float dis = djVtkUtil.calDistanceOfPoints(truthPoint,
						roiCandidateData.getPoint(ptID));
				if (dis <= 10) {
					distributionList.add(roiCandidateData.getPoint(ptID));
				}
			} // for all points in the grid file

			djVtkUtil.writeToPointsVtkFile(currentTruthFileName, truthPnList);
			djVtkUtil.writeToPointsVtkFile(currentDistributionFileName,
					distributionList);
		}
	}

	public void prepareSpecificFibers() {
		for (int i = 0; i < this.subList.size(); i++) {
			int virSubID = this.subList.get(i);
			int nRealSubID = (int) visualizeWorkingMem.subRelationM.get(
					virSubID, 1);
			djVtkSurData currentSurData = new djVtkSurData("./data/"
					+ nRealSubID + ".surf.vtk");
			djVtkFiberData currentFiberData = new djVtkFiberData("./data/"
					+ nRealSubID + ".asc.fiber.vtk");
			djVtkHybridData hybridData = new djVtkHybridData(currentSurData,
					currentFiberData);
			hybridData.mapSurfaceToBox();
			hybridData.mapFiberToBox();

			int rowID = virSubID - (this.nGroupID - 1) * 5;
			for (int j = 0; j < this.girdList.size(); j++) {
				int gridID = this.girdList.get(j);
				int ptIDOfGridBefore = (int) optiResultM.get((gridID - 1) * 5
						+ rowID, 3);
				int ptIDOfGridAfter = (int) optiResultM.get((gridID - 1) * 5
						+ rowID, 5);
				String fiberBeforeFileName = "./taskValidationFibers/"
						+ nRealSubID + "_" + this.roiID + "_" + gridID
						+ "_fiberBefore.vtk";
				String fiberAfterFileName = "./taskValidationFibers/"
						+ nRealSubID + "_" + this.roiID + "_" + gridID
						+ "_fiberAfter.vtk";
				// extract fibers
				Set ptSetBefore = currentSurData.getNeighbourPoints(
						ptIDOfGridBefore, 3);
				Set ptSetAfter = currentSurData.getNeighbourPoints(
						ptIDOfGridAfter, 3);
				hybridData.getFibersConnectToPointsSet(ptSetBefore)
						.writeToVtkFileCompact(fiberBeforeFileName);
				hybridData.getFibersConnectToPointsSet(ptSetAfter)
						.writeToVtkFileCompact(fiberAfterFileName);
			}
		}

	}

	public static void main(String[] args) throws Exception {
		System.out.println("This is visualizeWorkingMem...");

		if (args.length < 1) {
			System.out
					.println("format:java -Xmx3000m -jar *.jar groupID roiID gridNum gridList(eg. 1 2 3)");
		} else {
			// initialization

			visualizeWorkingMem mainHandler = new visualizeWorkingMem();
			mainHandler.nGroupID = Integer.valueOf(args[0]);
			// mainHandler.roiID = Integer.valueOf(args[1]);
			// int gridNum = Integer.valueOf(args[2]);
			// int count = 3;
			// for (int i = 0; i < gridNum; i++) {
			// mainHandler.girdList.add(Integer.valueOf(args[count++]));
			// }
			// for (int i = (mainHandler.nGroupID - 1) * 5; i <
			// mainHandler.nGroupID * 5; i++) {
			// mainHandler.subList.add(i);
			// }
			mainHandler.optiResultM = Matrix.fromASCIIFile(new File(
					"./data/optimizationResult_" + mainHandler.nGroupID
							+ "_all.txt"));
			// mainHandler.prepareSpecificFibers();

			// mainHandler.prepreVtkFiles(roiID, mainHandler.nGroupID);

			// ///////////////////////////////////////
			mainHandler.printMesh(1);

		} // else, means valid inputs

	}
}
