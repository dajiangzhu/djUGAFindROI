

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
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

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import edu.uga.liulab.djVtkBase.djVtkData;
import edu.uga.liulab.djVtkBase.djVtkPoint;
import edu.uga.liulab.djVtkBase.djVtkSurData;
import edu.uga.liulab.djVtkBase.djVtkUtil;

public class landMarkFigure1 {
	// public djVtkSurData modelData;
	public AbstractMatrix colorTableM;
	public djVtkSurData inputData;
	public String outputName;

	public djVtkSurData normalModelData;
	public djVtkSurData highlightModeData;

	// 0.568627 0.25098 0.494118 rf
	// 0.313726 0.341176 0.156863 rp
	// 0.752941 0.858824 0.894118 lf
	// 0.164706 0.298039 0.164706 rt
	// 0 0 0
	// 0.113725 0.376471 0.643137 ro
	// 0.760784 0.878431 0.498039 lt
	// 0.141176 0.00784314 0.882353 lo
	// 0.54902 0.788235 0.886275 lp
	public void tryLobeColor() {
		String dir = "./bigPaperResult/";
		djVtkSurData surData = new djVtkSurData(dir + "surWithLobe/10.regTo10.lobe.vtk");
		surData.cellsOutput = surData.cells;
		// surData.cellsScalarData.put("cell_lobe", new ArrayList<String>());
		// for(int i=0;i<surData.nCellNum;i++)
		// {
		// surData.cellsScalarData.get("cell_lobe").add("1 0 0");
		// }
		List<String> attriList = surData.pointsScalarData.get("LobeLabe");
		for (int i = 0; i < attriList.size(); i++) {
			if (attriList.get(i).trim().endsWith("0.752941 0.858824 0.894118")) // lf
			{
				float r = 255.0f;
				float g = 0.0f;
				float b = 0.0f;
				String colorStr = (r / 255.0) + " " + (g / 255.0) + " " + (b / 255.0);
				attriList.set(i, colorStr);
			}
			if (attriList.get(i).trim().endsWith("0.54902 0.788235 0.886275")) // lp
			{
				float r = 255.0f;
				float g = 255.0f;
				float b = 0.0f;
				String colorStr = (r / 255.0) + " " + (g / 255.0) + " " + (b / 255.0);
				attriList.set(i, colorStr);
			}
			if (attriList.get(i).trim().endsWith("0.141176 0.00784314 0.882353")) // lo
			{
				float r = 0.0f;
				float g = 255.0f;
				float b = 0.0f;
				String colorStr = (r / 255.0) + " " + (g / 255.0) + " " + (b / 255.0);
				attriList.set(i, colorStr);
			}
			if (attriList.get(i).trim().endsWith("0.760784 0.878431 0.498039")) // lt
			{
				float r = 0.0f;
				float g = 0.0f;
				float b = 255.0f;
				String colorStr = (r / 255.0) + " " + (g / 255.0) + " " + (b / 255.0);
				attriList.set(i, colorStr);
			}

			if (attriList.get(i).trim().endsWith("0.568627 0.25098 0.494118")) // rf
			{
				float r = 255.0f;
				float g = 0.0f;
				float b = 0.0f;
				String colorStr = (r / 255.0) + " " + (g / 255.0) + " " + (b / 255.0);
				attriList.set(i, colorStr);
			}
			if (attriList.get(i).trim().endsWith("0.313726 0.341176 0.156863")) // rp
			{
				float r = 255.0f;
				float g = 255.0f;
				float b = 0.0f;
				String colorStr = (r / 255.0) + " " + (g / 255.0) + " " + (b / 255.0);
				attriList.set(i, colorStr);
			}
			if (attriList.get(i).trim().endsWith("0.113725 0.376471 0.643137")) // ro
			{
				float r = 0.0f;
				float g = 255.0f;
				float b = 0.0f;
				String colorStr = (r / 255.0) + " " + (g / 255.0) + " " + (b / 255.0);
				attriList.set(i, colorStr);
			}
			if (attriList.get(i).trim().endsWith("0.164706 0.298039 0.164706")) // rt
			{
				float r = 0.0f;
				float g = 0.0f;
				float b = 255.0f;
				String colorStr = (r / 255.0) + " " + (g / 255.0) + " " + (b / 255.0);
				attriList.set(i, colorStr);
			}

			if (attriList.get(i).trim().endsWith("0 0 0")) {
				float r = 204.0f;
				float g = 204.0f;
				float b = 204.0f;
				String colorStr = (r / 255.0) + " " + (g / 255.0) + " " + (b / 255.0);
				attriList.set(i, colorStr);
			}
		}
		surData.writeToVtkFileCompact(dir + "test.vtk");
	}

	public void loadColorInfo(String strColorVersion) {
		String newColorTableInfo = "./fig1/dj_colortable_" + strColorVersion + ".txt";
		String colorTableInfo = "./fig1/dj_colortable_2.0.txt";
		String allColorInfo = "./fig1/allColors.txt";

		List<String> colorTableList = landMarkUtil.loadFileToArrayList(colorTableInfo);
		List<String> allColorList = landMarkUtil.loadFileToArrayList(allColorInfo);
		// ///////////////////////////
		for (int i = 0; i < colorTableList.size(); i++)
			colorTableList.set(i, "193 255 193");

		// //////////////////////////

		String roiGroupInfo = "./fig1/358_GyraSameColor.txt";
		try {
			FileInputStream fstream_roiGroupInfo = new FileInputStream(roiGroupInfo);
			DataInputStream in_roiGroupInfo = new DataInputStream(fstream_roiGroupInfo);
			BufferedReader br_roiGroupInfo = new BufferedReader(new InputStreamReader(in_roiGroupInfo));
			String strLine;
			String[] tmpStringArray;
			while ((strLine = br_roiGroupInfo.readLine()) != null) {
				if (!strLine.startsWith("#") && (tmpStringArray = strLine.split("\\s+")).length > 0) {
					int roiID = Integer.valueOf(tmpStringArray[0]);
					int colorIndex = Integer.valueOf(tmpStringArray[1]) - 1;
					colorTableList.set(roiID, allColorList.get(colorIndex));
				} //
			}// while
			br_roiGroupInfo.close();
			in_roiGroupInfo.close();
			fstream_roiGroupInfo.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		djVtkUtil.writeArrayListToFile(colorTableList, newColorTableInfo);
	}

	public void geneROIConnection() {
		String networkFile = "./fig1/data/network_aural.txt";

		String lineColor = "";
		List<Integer> nodeList = new ArrayList<Integer>();
		try {
			FileInputStream fstream_roiGroupInfo = new FileInputStream(networkFile);
			DataInputStream in_roiGroupInfo = new DataInputStream(fstream_roiGroupInfo);
			BufferedReader br_roiGroupInfo = new BufferedReader(new InputStreamReader(in_roiGroupInfo));
			String strLine;
			String[] tmpStringArray;
			while ((strLine = br_roiGroupInfo.readLine()) != null) {
				if (!strLine.startsWith("#") && (tmpStringArray = strLine.split("\\s+")).length > 0) {
					int roiID = Integer.valueOf(tmpStringArray[0]);
					nodeList.add(roiID);
				} //
				else if (strLine.startsWith("#backGround"))
					lineColor = br_roiGroupInfo.readLine();
			}// while
			br_roiGroupInfo.close();
			in_roiGroupInfo.close();
			fstream_roiGroupInfo.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		// write lines
		System.out.println("Begin to write file:" + this.outputName + "...");
		FileWriter fw = null;
		try {
			fw = new FileWriter(networkFile + ".lines.vtk");
			fw.write("# vtk DataFile Version 3.0\r\n");
			fw.write("vtk output\r\n");
			fw.write("ASCII\r\n");
			fw.write("DATASET POLYDATA\r\n");
			fw.write("POINTS " + nodeList.size() + " float\r\n");
			for (int i = 0; i < nodeList.size(); i++)
				fw.write(this.inputData.getPoint(nodeList.get(i)).x + " " + this.inputData.getPoint(nodeList.get(i)).y
						+ " " + this.inputData.getPoint(nodeList.get(i)).z + "\r\n");
			fw.write("LINES " + nodeList.size() * (nodeList.size() - 1) / 2 + " " + nodeList.size()
					* (nodeList.size() - 1) * 3 / 2 + "\r\n");
			for (int i = 0; i < nodeList.size() - 1; i++)
				for (int j = i + 1; j < nodeList.size(); j++)
					fw.write("2 " + i + " " + j + "\r\n");
			fw.write("CELL_DATA " + nodeList.size() * (nodeList.size() - 1) / 2 + "\r\n");
			fw.write("COLOR_SCALARS colors 3 \r\n");
			for (int i = 0; i < nodeList.size() - 1; i++)
				for (int j = i + 1; j < nodeList.size(); j++)
					fw.write(lineColor + "\r\n");
		} catch (IOException ex) {
			Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fw.close();
			} catch (IOException ex) {
				Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println("Write file:" + networkFile + ".lines.vtk done!");

	}

	public void geneSubNetwork(djVtkSurData modelData, String subDes) {
		String allColorInfo = "./fig1/data/allColors.txt";
		List<String> allColorList = landMarkUtil.loadFileToArrayList(allColorInfo);

		Map<String, List<Integer>> networksList = new HashMap<String, List<Integer>>();
		List<String> networkNameList = new ArrayList<String>();
		List<String> networkColorList = new ArrayList<String>();
		// this file assign the rois need to be enlarge
		//String roiGroupInfo = "./fig1_v2/NetWorkROIFinal.txt";
		String roiGroupInfo = "./0610/NetWorkROIFinal0610.txt";
		List<Integer> nodeList = null;
		String networkName = "";
		try {
			FileInputStream fstream_roiGroupInfo = new FileInputStream(roiGroupInfo);
			DataInputStream in_roiGroupInfo = new DataInputStream(fstream_roiGroupInfo);
			BufferedReader br_roiGroupInfo = new BufferedReader(new InputStreamReader(in_roiGroupInfo));
			String strLine;
			String[] tmpStringArray;
			while ((strLine = br_roiGroupInfo.readLine()) != null) {
				tmpStringArray = strLine.split("\\s+");
				if (strLine.startsWith("#backGround"))
					br_roiGroupInfo.readLine();
				else if (strLine.startsWith("#network")) {
					nodeList = new ArrayList<Integer>();
					networkName = tmpStringArray[1];
					networksList.put(networkName, nodeList);
					networkNameList.add(networkName);
					int colorIndex = Integer.valueOf(tmpStringArray[2]) - 1;
					networkColorList.add(allColorList.get(colorIndex));
				} // new network
				else {
					int roiID = Integer.valueOf(tmpStringArray[0]);
					networksList.get(networkNameList.get(networkNameList.size() - 1)).add(roiID);
				}
			}// while
			br_roiGroupInfo.close();
			in_roiGroupInfo.close();
			fstream_roiGroupInfo.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		// //////////////////////////////
		 int[] count = new int[358];
		 for (int i = 0; i < 358; i++)
		 count[i] = 0;
		 for (int i = 0; i < networkNameList.size(); i++) {
		 int ROINumofCurrentNetwork =
		 networksList.get(networkNameList.get(i)).size();
		 for (int j = 0; j < ROINumofCurrentNetwork; j++)
		 count[networksList.get(networkNameList.get(i)).get(j)]++;
		 System.out.println("ROI number of " + networkNameList.get(i) + " : "
		 + ROINumofCurrentNetwork);
		 }
		 System.out.println("Stat of ROI repeat times############");
		 for (int i = 358; i > 0; i--) {
		 for (int j = 0; j < 358; j++)
		 if (count[j] == i)
		 System.out.println("ROI " + j + " repeats " + i + " times...");
		 }

		// ////////////////////////////
//		for (int i = 0; i < networkNameList.size(); i++) {
//			System.out.println("Begin to write network:" + networkNameList.get(i) + "...");
//			String fileName = "./0610/network/NetWork.sub." + subDes + "." + networkNameList.get(i) + ".vtk";
//			nodeList = networksList.get(networkNameList.get(i));
//			FileWriter fw = null;
//			try {
//				fw = new FileWriter(fileName);
//				fw.write("# vtk DataFile Version 3.0\r\n");
//				fw.write("vtk output\r\n");
//				fw.write("ASCII\r\n");
//				fw.write("DATASET POLYDATA\r\n");
//
//				int roiNum = nodeList.size();
//				int normalModelPtNum = modelData.nPointNum;
//				int normalModelCellNum = modelData.nCellNum;
//
//				System.out.println("the number of points in the input vtk is : " + roiNum);
//				System.out.println("the number of points in the normalModel vtk is : " + normalModelPtNum);
//				// print points info
//				List<Integer> offsetList = new ArrayList<Integer>();
//				fw.write("POINTS " + roiNum * normalModelPtNum + " float\r\n");
//				for (int roiIndex = 0; roiIndex < roiNum; roiIndex++) {
//					int countNormal = 0;
//					int countHigh = 0;
//					djVtkPoint currentROIPt = this.inputData.getPoint(nodeList.get(roiIndex));
//					for (int modelPtIndex = 0; modelPtIndex < normalModelPtNum; modelPtIndex++) {
//						float x = modelData.getPoint(modelPtIndex).x + currentROIPt.x;
//						float y = modelData.getPoint(modelPtIndex).y + currentROIPt.y;
//						float z = modelData.getPoint(modelPtIndex).z + currentROIPt.z;
//						fw.write(x + " " + y + " " + z + "\r\n");
//						offsetList.add(normalModelPtNum);
//						countNormal++;
//					} // for all points in the model vtk file
//				} // for all points in the input vtk file
//
//				// print cells info
//				int totalCellNum = roiNum * normalModelCellNum;
//				fw.write("POLYGONS " + totalCellNum + " " + (totalCellNum * 4) + " \r\n");
//				int offset = 0;
//				for (int roiIndex = 0; roiIndex < roiNum; roiIndex++) {
//					for (int modelCellIndex = 0; modelCellIndex < normalModelCellNum; modelCellIndex++) {
//						int ptId1 = modelData.getcell(modelCellIndex).pointsList.get(0).pointId + offset;
//						int ptId2 = modelData.getcell(modelCellIndex).pointsList.get(1).pointId + offset;
//						int ptId3 = modelData.getcell(modelCellIndex).pointsList.get(2).pointId + offset;
//						fw.write("3 " + ptId1 + " " + ptId2 + " " + ptId3 + " \r\n");
//					} // for all cells in the model vtk file
//					offset = offset + normalModelPtNum;
//				} // for all points in the input vtk file
//
//				fw.write("POINT_DATA " + roiNum * normalModelPtNum + "\r\n");
//				fw.write("COLOR_SCALARS Colors 3\r\n");
//				for (int roiIndex = 0; roiIndex < roiNum; roiIndex++) {
//					String[] strRGB = networkColorList.get(i).split("\\s+");
//					float r = Float.valueOf(strRGB[0]) / 255.0f;
//					float g = Float.valueOf(strRGB[1]) / 255.0f;
//					float b = Float.valueOf(strRGB[2]) / 255.0f;
//					for (int modelPtIndex = 0; modelPtIndex < normalModelPtNum; modelPtIndex++)
//						fw.write(r + " " + g + " " + b + " \r\n");
//				} // for all points in the input vtk file
//			} catch (IOException ex) {
//				Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
//			} finally {
//				try {
//					fw.close();
//				} catch (IOException ex) {
//					Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
//				}
//			}
//			System.out.println("Write network:" + networkNameList.get(i) + " done!");
//		} // for all networks
	}

	public void geneColorROI(String subDes) {
		String allColorInfo = "./fig1/data/allColors.txt";
		List<String> allColorList = landMarkUtil.loadFileToArrayList(allColorInfo);

		Map<Integer, String> highLightROI = new HashMap<Integer, String>();
		// this file assign the rois need to be enlarge
		 String roiGroupInfo = "./fig3/0612/NetWorkVisual.txt";
//		String roiGroupInfo = "./fig3/supplement/wm/NetWorkWm.txt"; //for fig3
		String backColor = "";
		try {
			FileInputStream fstream_roiGroupInfo = new FileInputStream(roiGroupInfo);
			DataInputStream in_roiGroupInfo = new DataInputStream(fstream_roiGroupInfo);
			BufferedReader br_roiGroupInfo = new BufferedReader(new InputStreamReader(in_roiGroupInfo));
			String strLine;
			String[] tmpStringArray;
			while ((strLine = br_roiGroupInfo.readLine()) != null) {
				if (!strLine.startsWith("#") && (tmpStringArray = strLine.split("\\s+")).length > 0) {
					int roiID = Integer.valueOf(tmpStringArray[0]);
					int colorIndex = Integer.valueOf(tmpStringArray[1]) - 1;
					highLightROI.put(roiID, allColorList.get(colorIndex));
				} //
				else if (strLine.startsWith("#backGround"))
					backColor = br_roiGroupInfo.readLine();
			}// while
			br_roiGroupInfo.close();
			in_roiGroupInfo.close();
			fstream_roiGroupInfo.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		// ///////////////////////////////////////

		// ****************************************************************************************************
		// highLightROI.clear();
		// String fileName = "./fig1_v2/network/"+subDes+".defaultROI.vtk";
		// ****************************************************************************************************

		System.out.println("Begin to write file:" + roiGroupInfo + "." + subDes + ".vtk...");
		FileWriter fw = null;
		try {
			fw = new FileWriter(roiGroupInfo + "." + subDes + ".vtk.vtk");
			// fw = new FileWriter(fileName);
			fw.write("# vtk DataFile Version 3.0\r\n");
			fw.write("vtk output\r\n");
			fw.write("ASCII\r\n");
			fw.write("DATASET POLYDATA\r\n");

			int roiNum = this.inputData.nPointNum;
			int highROINum = highLightROI.size();
			int normalModelPtNum = this.normalModelData.nPointNum;
			int highModelPtNum = this.highlightModeData.nPointNum;
			int normalModelCellNum = this.normalModelData.nCellNum;
			int highModelCellNum = this.highlightModeData.nCellNum;

			System.out.println("the number of points in the input vtk is : " + roiNum);
			System.out.println("the number of points need to be highlighted is : " + highROINum);
			System.out.println("the number of points in the normalModel vtk is : " + normalModelPtNum);
			System.out.println("the number of points in the highModel vtk is : " + highModelPtNum);
			// print points info
			List<Integer> offsetList = new ArrayList<Integer>();
			fw.write("POINTS " + ( (highROINum * highModelPtNum))//fw.write("POINTS " + ((roiNum - highROINum) * normalModelPtNum + (highROINum * highModelPtNum))
					+ " float\r\n");
			for (int roiIndex = 0; roiIndex < roiNum; roiIndex++) {
				int countNormal = 0;
				int countHigh = 0;
				djVtkPoint currentROIPt = this.inputData.getPoint(roiIndex);
				if (highLightROI.containsKey(roiIndex)) {
					for (int modelPtIndex = 0; modelPtIndex < highModelPtNum; modelPtIndex++) {
						float x = this.highlightModeData.getPoint(modelPtIndex).x + currentROIPt.x;
						float y = this.highlightModeData.getPoint(modelPtIndex).y + currentROIPt.y;
						float z = this.highlightModeData.getPoint(modelPtIndex).z + currentROIPt.z;
						fw.write(x + " " + y + " " + z + "\r\n");
						offsetList.add(highModelPtNum);
						countHigh++;
					}
				} 
//				else {
//					for (int modelPtIndex = 0; modelPtIndex < normalModelPtNum; modelPtIndex++) {
//						float x = this.normalModelData.getPoint(modelPtIndex).x + currentROIPt.x;
//						float y = this.normalModelData.getPoint(modelPtIndex).y + currentROIPt.y;
//						float z = this.normalModelData.getPoint(modelPtIndex).z + currentROIPt.z;
//						fw.write(x + " " + y + " " + z + "\r\n");
//						offsetList.add(normalModelPtNum);
//						countNormal++;
//					} // for all points in the model vtk file
//				}

			} // for all points in the input vtk file

			// print cells info
			int totalCellNum = (highROINum * highModelCellNum);//int totalCellNum = (roiNum - highROINum) * normalModelCellNum + (highROINum * highModelCellNum);
			fw.write("POLYGONS " + totalCellNum + " " + (totalCellNum * 4) + " \r\n");
			int offset = 0;
			for (int roiIndex = 0; roiIndex < roiNum; roiIndex++) {
				if (highLightROI.containsKey(roiIndex)) {
					for (int modelCellIndex = 0; modelCellIndex < highModelCellNum; modelCellIndex++) {
						int ptId1 = this.highlightModeData.getcell(modelCellIndex).pointsList.get(0).pointId + offset;
						int ptId2 = this.highlightModeData.getcell(modelCellIndex).pointsList.get(1).pointId + offset;
						int ptId3 = this.highlightModeData.getcell(modelCellIndex).pointsList.get(2).pointId + offset;
						fw.write("3 " + ptId1 + " " + ptId2 + " " + ptId3 + " \r\n");
					} // for all cells in the model vtk file
					offset = offset + highModelPtNum;
				}
//				else {
//					for (int modelCellIndex = 0; modelCellIndex < normalModelCellNum; modelCellIndex++) {
//						int ptId1 = this.normalModelData.getcell(modelCellIndex).pointsList.get(0).pointId + offset;
//						int ptId2 = this.normalModelData.getcell(modelCellIndex).pointsList.get(1).pointId + offset;
//						int ptId3 = this.normalModelData.getcell(modelCellIndex).pointsList.get(2).pointId + offset;
//						fw.write("3 " + ptId1 + " " + ptId2 + " " + ptId3 + " \r\n");
//					} // for all cells in the model vtk file
//					offset = offset + normalModelPtNum;
//				}

			} // for all points in the input vtk file

			fw.write("POINT_DATA " + ((highROINum * highModelPtNum))+ "\r\n");//fw.write("POINT_DATA " + ((roiNum - highROINum) * normalModelPtNum + (highROINum * highModelPtNum))+ "\r\n");
			fw.write("COLOR_SCALARS Colors 3\r\n");
			for (int roiIndex = 0; roiIndex < roiNum; roiIndex++) {
				if (highLightROI.containsKey(roiIndex)) {
					String[] strRGB = highLightROI.get(roiIndex).split("\\s+");
					float r = Float.valueOf(strRGB[0]) / 255.0f;
					float g = Float.valueOf(strRGB[1]) / 255.0f;
					float b = Float.valueOf(strRGB[2]) / 255.0f;
					for (int modelPtIndex = 0; modelPtIndex < normalModelPtNum; modelPtIndex++)
						fw.write(r + " " + g + " " + b + " \r\n");
				} 
//				else {
//					String[] strRGB = backColor.split("\\s+");
//					float r = Float.valueOf(strRGB[0]) / 255.0f;
//					float g = Float.valueOf(strRGB[1]) / 255.0f;
//					float b = Float.valueOf(strRGB[2]) / 255.0f;
//					for (int modelPtIndex = 0; modelPtIndex < highModelPtNum; modelPtIndex++)
//						fw.write(r + " " + g + " " + b + " \r\n");
//				}
			} // for all points in the input vtk file
		} catch (IOException ex) {
			Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fw.close();
			} catch (IOException ex) {
				Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println("Write file:" + roiGroupInfo + ".vtk done!");

	}

	//
	// public void outPut() {
	// System.out.println("Begin to write file:" + this.outputName + "...");
	// FileWriter fw = null;
	// try {
	// fw = new FileWriter(this.outputName);
	// fw.write("# vtk DataFile Version 3.0\r\n");
	// fw.write("vtk output\r\n");
	// fw.write("ASCII\r\n");
	// fw.write("DATASET POLYDATA\r\n");
	// int roiNum = this.inputData.nPointNum;
	// int modelPtNum = this.modelData.nPointNum;
	// int modelCellNum = this.modelData.nCellNum;
	// System.out.println("the number of points in the input vtk is : " +
	// roiNum);
	// System.out.println("the number of points in the model vtk is : " +
	// modelPtNum);
	// // print points info
	// fw.write("POINTS " + (roiNum * modelPtNum) + " float\r\n");
	// for (int roiIndex = 0; roiIndex < roiNum; roiIndex++) {
	// djVtkPoint currentROIPt = this.inputData.getPoint(roiIndex);
	// for (int modelPtIndex = 0; modelPtIndex < modelPtNum; modelPtIndex++) {
	// float x = this.modelData.getPoint(modelPtIndex).x + currentROIPt.x;
	// float y = this.modelData.getPoint(modelPtIndex).y + currentROIPt.y;
	// float z = this.modelData.getPoint(modelPtIndex).z + currentROIPt.z;
	// fw.write(x + " " + y + " " + z + "\r\n");
	// } // for all points in the model vtk file
	// } // for all points in the input vtk file
	//
	// // print cells info
	// fw.write("POLYGONS " + (roiNum * modelCellNum) + " " + (roiNum *
	// modelCellNum * 4) + " \r\n");
	// for (int roiIndex = 0; roiIndex < roiNum; roiIndex++) {
	// for (int modelCellIndex = 0; modelCellIndex < modelCellNum;
	// modelCellIndex++) {
	// int ptId1 =
	// this.modelData.getcell(modelCellIndex).pointsList.get(0).pointId +
	// roiIndex * modelPtNum;
	// int ptId2 =
	// this.modelData.getcell(modelCellIndex).pointsList.get(1).pointId +
	// roiIndex * modelPtNum;
	// int ptId3 =
	// this.modelData.getcell(modelCellIndex).pointsList.get(2).pointId +
	// roiIndex * modelPtNum;
	// fw.write("3 " + ptId1 + " " + ptId2 + " " + ptId3 + " \r\n");
	// } // for all cells in the model vtk file
	// } // for all points in the input vtk file
	//
	// fw.write("POINT_DATA " + (roiNum * modelPtNum) + "\r\n");
	// fw.write("COLOR_SCALARS Colors 3\r\n");
	// for (int roiIndex = 0; roiIndex < roiNum; roiIndex++) {
	// float r = (float) this.colorTableM.get(roiIndex, 0) / 255.0f;
	// float g = (float) this.colorTableM.get(roiIndex, 1) / 255.0f;
	// float b = (float) this.colorTableM.get(roiIndex, 2) / 255.0f;
	// for (int modelPtIndex = 0; modelPtIndex < modelPtNum; modelPtIndex++)
	// fw.write(r + " " + g + " " + b + " \r\n");
	// } // for all points in the input vtk file
	// } catch (IOException ex) {
	// Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
	// } finally {
	// try {
	// fw.close();
	// } catch (IOException ex) {
	// Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
	// }
	// }
	// System.out.println("Write file:" + this.outputName + " done!");
	// }

	public float[][] geneNewArray() {
		float[][] newArray = new float[358][2];
		for (int i = 0; i < 358; i++)
			for (int j = 0; j < 2; j++)
				newArray[i][j] = 0.0f;
		return newArray;
	}

	public void analyzeNetworkROIs() {
		// input is kaiming's network file
		String roiGroupInfo = "./0610/allInOneFinal.final.0610.txt";
		List<String> outPutStringList = new ArrayList<String>();
		List<String> outPutDistanceList = new ArrayList<String>();
		Map<String, Set<Integer>> networksMap = new HashMap<String, Set<Integer>>();
		Set<Integer> nodeSet = null;
		List<String> networkNameList = new ArrayList<String>();
		String networkName = "";
		String colorName = "";
		float[][] allNetworkInfo = this.geneNewArray();
		float[][] currentNetworkInfo = this.geneNewArray();
		try {
			FileInputStream fstream_roiGroupInfo = new FileInputStream(roiGroupInfo);
			DataInputStream in_roiGroupInfo = new DataInputStream(fstream_roiGroupInfo);
			BufferedReader br_roiGroupInfo = new BufferedReader(new InputStreamReader(in_roiGroupInfo));
			String strLine;
			String[] tmpStringArray;
			while ((strLine = br_roiGroupInfo.readLine()) != null) {
				tmpStringArray = strLine.split("\\s+");
				if (strLine.startsWith("#network")) {
					int netWorkCount = networkNameList.size();
					if (netWorkCount != 0) {
						String currentOutPutString = "" + "#network " + networkNameList.get(netWorkCount - 1) + " "
								+ colorName;
						outPutStringList.add(currentOutPutString);
						Iterator itROIsOfCurrentNetwork = networksMap.get(networkNameList.get(netWorkCount - 1))
								.iterator();
						String strDis = "";
						while (itROIsOfCurrentNetwork.hasNext()) {
							int tmpDicccolID = (Integer) itROIsOfCurrentNetwork.next();
							float tmpDis = currentNetworkInfo[tmpDicccolID][0] / currentNetworkInfo[tmpDicccolID][1];
							strDis = strDis + tmpDis + " ";
							currentOutPutString = "" + tmpDicccolID + " " + colorName + " " + tmpDis;
							outPutStringList.add(currentOutPutString);
						}
						outPutDistanceList.clear();
						outPutDistanceList.add(strDis);
						djVtkUtil.writeArrayListToFile(outPutDistanceList,
								"./0610/Fig3_Dis_" + networkNameList.get(netWorkCount - 1) + ".txt");
					}
					// ///
					nodeSet = new HashSet<Integer>();
					networkName = tmpStringArray[1];
					colorName = tmpStringArray[2];
					networksMap.put(networkName, nodeSet);
					networkNameList.add(networkName);
					currentNetworkInfo = this.geneNewArray();
				} // new network
				else {
					int roiID = Integer.valueOf(tmpStringArray[1]);
					float dis = Float.valueOf(tmpStringArray[2]);
					currentNetworkInfo[roiID][0] += dis;
					currentNetworkInfo[roiID][1] += 1.0f;
					allNetworkInfo[roiID][0] += dis;
					allNetworkInfo[roiID][1] += 1.0f;
					networksMap.get(networkNameList.get(networkNameList.size() - 1)).add(roiID);
				}
			}// while
			br_roiGroupInfo.close();
			in_roiGroupInfo.close();
			fstream_roiGroupInfo.close();

			int netWorkCount = networkNameList.size();
			String currentOutPutString = "" + "#network " + networkNameList.get(netWorkCount - 1) + " " + colorName;
			outPutStringList.add(currentOutPutString);
			Iterator itROIsOfCurrentNetwork = networksMap.get(networkNameList.get(netWorkCount - 1)).iterator();
			String strDis = "";
			while (itROIsOfCurrentNetwork.hasNext()) {
				int tmpDicccolID = (Integer) itROIsOfCurrentNetwork.next();
				float tmpDis = currentNetworkInfo[tmpDicccolID][0] / currentNetworkInfo[tmpDicccolID][1];
				strDis = strDis + tmpDis + " ";
				currentOutPutString = "" + tmpDicccolID + " " + colorName + " " + tmpDis;
				outPutStringList.add(currentOutPutString);
			}
			outPutDistanceList.clear();
			outPutDistanceList.add(strDis);
			djVtkUtil.writeArrayListToFile(outPutDistanceList,
					"./0610/Fig3_Dis_" + networkNameList.get(netWorkCount - 1) + ".txt");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		djVtkUtil.writeArrayListToFile(outPutStringList, "./0610/NetWorkROIFinal0610.txt");

	}

	public void geneBalls() throws IOException {
		String subDes = "05051102";
		String surFile = "./fig3/data/empathy/"+subDes+".surf.vtk"; //for liulab data , all in empathy
		String ballFile = "./fig1/data/sphere_radius3_5.vtk";
		List<String> roiList = djVtkUtil.loadFileToArrayList("./fig3/0612/supplement/fear/ActivationFear."+subDes+".txt");
		String fileName = "./fig3/0612/supplement/fear/ActivationBall."+subDes+".vtk";
		
		
		djVtkSurData surData = new djVtkSurData(surFile);
		djVtkSurData ballData = new djVtkSurData(ballFile);

		System.out.println("Begin to write file:" + fileName + "...");
		FileWriter fw = null;
		fw = new FileWriter(fileName);
		fw.write("# vtk DataFile Version 3.0\r\n");
		fw.write("vtk output\r\n");
		fw.write("ASCII\r\n");
		fw.write("DATASET POLYDATA\r\n");
		fw.write("POINTS " + roiList.size() * ballData.nPointNum + " float\r\n");
		int roiNum = roiList.size();
		int normalModelPtNum = ballData.nPointNum;
		int normalModelCellNum = ballData.nCellNum;
		
		for (int roiIndex = 0; roiIndex < roiNum; roiIndex++) {
			int countNormal = 0;
			int countHigh = 0;
			int ptID = Integer.valueOf( roiList.get(roiIndex).split("\\s+")[0] );
			djVtkPoint currentROIPt = surData.getPoint(ptID);
			for (int modelPtIndex = 0; modelPtIndex < normalModelPtNum; modelPtIndex++) {
				float x = ballData.getPoint(modelPtIndex).x + currentROIPt.x;
				float y = ballData.getPoint(modelPtIndex).y + currentROIPt.y;
				float z = ballData.getPoint(modelPtIndex).z + currentROIPt.z;
				fw.write(x + " " + y + " " + z + "\r\n");
			} // for all points in the model vtk file
		} // for all points in the input vtk file

		// print cells info
		int totalCellNum = roiNum * normalModelCellNum;
		fw.write("POLYGONS " + totalCellNum + " " + (totalCellNum * 4) + " \r\n");
		int offset = 0;
		for (int roiIndex = 0; roiIndex < roiNum; roiIndex++) {
			for (int modelCellIndex = 0; modelCellIndex < normalModelCellNum; modelCellIndex++) {
				int ptId1 = ballData.getcell(modelCellIndex).pointsList.get(0).pointId + offset;
				int ptId2 = ballData.getcell(modelCellIndex).pointsList.get(1).pointId + offset;
				int ptId3 = ballData.getcell(modelCellIndex).pointsList.get(2).pointId + offset;
				fw.write("3 " + ptId1 + " " + ptId2 + " " + ptId3 + " \r\n");
			} // for all cells in the model vtk file
			offset = offset + normalModelPtNum;
		} // for all points in the input vtk file
		fw.close();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// if (args.length != 4) {
		// System.out.println("Usage: model.vtk input(point vtk) output(vtk)");
		// System.exit(0);
		// }
		landMarkFigure1 main = new landMarkFigure1();

		// //////////////////////////////////////////
		// String colorVersion = "2.1";
		// String newColorTableInfo = "./fig1/dj_colortable_" + colorVersion +
		// ".txt";
		// main.loadColorInfo(colorVersion);

		// main.inputData = new djVtkSurData(args[0]);
		main.normalModelData = new djVtkSurData("./fig1/data/sphere_radius2.vtk"); // 1
		main.highlightModeData = new djVtkSurData("./fig1/data/sphere_radius3_5.vtk");
		// main.outputName = args[3];

		// main.normalModelData = new
		// djVtkSurData("./fig1_v2/sphere_radius7.vtk");
		for (int i = 0; i < 1; i++) {
			String subID = "fig3_0613_visual_c001";
			String strInput = "./fig3/0612/358_" + subID + ".vtk";
			main.inputData = new djVtkSurData(strInput);
			// main.outputName = "./fig1/358_" + i + "_color_final.vtk";

			main.geneColorROI(subID);
//			 main.geneSubNetwork(new djVtkSurData("./fig1/data/sphere_radius3_5.vtk"),subID);
//			 main.analyzeNetworkROIs();
			
//			main.geneBalls();

			// main.geneROIConnection();
		}

		// //////////////////////////////////////
		// main.tryLobeColor();

	}

}
