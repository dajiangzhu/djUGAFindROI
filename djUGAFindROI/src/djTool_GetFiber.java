

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import edu.uga.liulab.djVtkBase.djVtkDataDictionary;
import edu.uga.liulab.djVtkBase.djVtkFiberData;
import edu.uga.liulab.djVtkBase.djVtkHybridData;
import edu.uga.liulab.djVtkBase.djVtkSurData;

public class djTool_GetFiber {
	String stroutputPrefix = "";
	djVtkSurData currentSurData = null;
	djVtkFiberData currentFiberData = null;
	djVtkHybridData hybridData = null;
	int ptNum = -1;
	int ringNum = 3;

	public void geneFibers(String fileName) {
		try {
			FileInputStream fstream_roiGroupInfo = new FileInputStream(fileName);
			DataInputStream in_roiGroupInfo = new DataInputStream(fstream_roiGroupInfo);
			BufferedReader br_roiGroupInfo = new BufferedReader(new InputStreamReader(in_roiGroupInfo));
			String strLine;
			String[] tmpStringArray;
			while ((strLine = br_roiGroupInfo.readLine()) != null && strLine.trim().length() != 0) {
				tmpStringArray = strLine.split("\\s+");
				if (strLine.startsWith("#surface")) {
					currentSurData = new djVtkSurData(tmpStringArray[1]);
				}
				if (strLine.startsWith("#fiber")) {
					currentFiberData = new djVtkFiberData(tmpStringArray[1]);
				}
				if (strLine.startsWith("#outputPrefix")) {
					stroutputPrefix = tmpStringArray[1];
				}
				if (strLine.startsWith("#pointID")) {
					hybridData = new djVtkHybridData(currentSurData, currentFiberData);
					hybridData.mapSurfaceToBox();
					hybridData.mapFiberToBox();
					ptNum = Integer.valueOf(tmpStringArray[1]);
					ringNum = Integer.valueOf(tmpStringArray[2]);

					for (int i = 0; i < ptNum;) {
						if ((strLine = br_roiGroupInfo.readLine()) != null && strLine.trim().length() != 0) {
							tmpStringArray = strLine.split("\\s+");
							int seedPtID = Integer.valueOf(tmpStringArray[0]);
							System.out.println("Begin to extract fibers of pt:"+seedPtID);
							Set ptSet = currentSurData.getNeighbourPoints(seedPtID, ringNum);
							djVtkFiberData newFiber = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
							newFiber.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
							newFiber.writeToVtkFileCompact(stroutputPrefix + "_" + seedPtID + ".fibers.vtk");
							i++;
						} // if
					} // for
				} // if
			}// while
			br_roiGroupInfo.close();
			in_roiGroupInfo.close();
			fstream_roiGroupInfo.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Need one para: Name of file which induces surface, fiber and pointIDs info...");
			System.exit(0);
		}
		djTool_GetFiber mainHandler = new djTool_GetFiber();
		mainHandler.geneFibers(args[0]);
	}
}
