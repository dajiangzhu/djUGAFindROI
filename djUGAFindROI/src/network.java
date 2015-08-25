

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import edu.uga.liulab.djVtkBase.djVtkFiberData;
import edu.uga.liulab.djVtkBase.djVtkHybridData;
import edu.uga.liulab.djVtkBase.djVtkSurData;
import edu.uga.liulab.djVtkBase.djVtkUtil;
import jxl.*;
import jxl.read.biff.BiffException;

public class network {

	public void generateMatrix() {
		AbstractMatrix inputM = Matrix.fromASCIIFile(new File("finalizedMat.txt"));
		int rowDim = inputM.getRowDimension();
		int columnDim = 1; // inputM.getColumnDimension();

		String surFile = "./fig2_v2/data/workingmem/10.sur.asc.to10.vtk";
		String fiberFile = "./fig2_v2/data/workingmem/10.fiber.asc.to10.vtk";

		djVtkSurData currentSurData = new djVtkSurData(surFile);
		djVtkFiberData currentFiberData = new djVtkFiberData(fiberFile);
		djVtkHybridData hybridData = new djVtkHybridData(currentSurData, currentFiberData);
		hybridData.mapSurfaceToBox();
		hybridData.mapFiberToBox();

		Set<Integer> netfibers = new HashSet<Integer>();
		Map<Integer, Set<Integer>> fiberMap = new HashMap<Integer, Set<Integer>>();

		for (int r = 0; r < rowDim; r++)
			for (int c = 0; c < columnDim; c++) {
				int ptID = (int) inputM.get(r, c);
				hybridData.getFiberData().cellsOutput.clear();
				System.out.println("ptID : " + ptID);
				Set ptSet = currentSurData.getNeighbourPoints(ptID, findModelDictionary.EXTRACTING_FIBER_RINGNUM);
				hybridData.getFibersConnectToPointsSet(ptSet);
				int cellSize = hybridData.getFiberData().cellsOutput.size();
				System.out.println("size : " + cellSize);
				for (int i = 0; i < cellSize; i++) {
					int cellID = hybridData.getFiberData().cellsOutput.get(i).cellId;
					netfibers.add(cellID);
					if (fiberMap.containsKey(cellID)) {
						fiberMap.get(cellID).add(r);
					} else {
						Set<Integer> dicccolsConnThisfiber = new HashSet<Integer>();
						dicccolsConnThisfiber.add(r);
						fiberMap.put(cellID, dicccolsConnThisfiber);
					}
					// System.out.println("cell : "+cellID);
				} // for
			} // for c

		double[][] connMatrix = new double[358][358];
		for (int i = 0; i < 358; i++)
			for (int j = 0; j < 358; j++)
				connMatrix[i][j] = 0.0;

		Iterator itNetFibers = netfibers.iterator();
		while (itNetFibers.hasNext()) {
			int fiberID = (Integer) itNetFibers.next();
			List<Integer> dicccols = new ArrayList<Integer>(fiberMap.get(fiberID));
			for (int i = 0; i < dicccols.size() - 1; i++)
				for (int j = i + 1; j < dicccols.size(); j++) {
					connMatrix[dicccols.get(i)][dicccols.get(j)] += 1.0;
					connMatrix[dicccols.get(j)][dicccols.get(i)] += 1.0;
				}
		} // wihile

		for (int i = 0; i < 358; i++)
			for (int j = 0; j < 358; j++)
				if (i == j)
					connMatrix[i][j] = 1.0;

		findModelUtil.writeArrayListToFile(connMatrix, 358, 358, "./connectome/sub10.txt");
		System.out.println("finish!");
	}

	public void checkMatrixAnd() {
		List<AbstractMatrix> MList = new ArrayList<AbstractMatrix>();
		AbstractMatrix inputM0 = Matrix.fromASCIIFile(new File("./connectome/sub10.txt"));
		AbstractMatrix inputM1 = Matrix.fromASCIIFile(new File("./connectome/sub12.txt"));
		AbstractMatrix inputM2 = Matrix.fromASCIIFile(new File("./connectome/sub14.txt"));
		AbstractMatrix inputM3 = Matrix.fromASCIIFile(new File("./connectome/sub16.txt"));
		AbstractMatrix inputM4 = Matrix.fromASCIIFile(new File("./connectome/sub19.txt"));
		AbstractMatrix inputM5 = Matrix.fromASCIIFile(new File("./connectome/sub20.txt"));
		MList.add(inputM0);
		MList.add(inputM1);
		MList.add(inputM2);
		MList.add(inputM3);
		MList.add(inputM4);
		MList.add(inputM5);

		double[][] connMatrix = new double[358][358];
		for (int i = 0; i < 358; i++)
			for (int j = 0; j < 358; j++)
				if (inputM0.get(i, j) > 0.0)
					connMatrix[i][j] = 1.0;
				else
					connMatrix[i][j] = 0.0;
		for (int k = 1; k < 6; k++) {
			AbstractMatrix inputM = MList.get(k);
			for (int i = 0; i < 358; i++)
				for (int j = 0; j < 358; j++)
					if ((inputM.get(i, j) > 0.0) && (connMatrix[i][j] > 0.0))
						connMatrix[i][j] = 1.0;
					else
						connMatrix[i][j] = 0.0;
		}
		findModelUtil.writeArrayListToFile(connMatrix, 358, 358, "./connectome/subAnd0_5.txt");
	}

	public void findSubNetWorks() {
		System.out.println("bigin findSubNetWorks...");
		AbstractMatrix inputM = Matrix.fromASCIIFile(new File("./connectome/sub20.txt"));
		Set<Integer> nodeCovered = new HashSet<Integer>();
		Stack<Integer> stack = new Stack<Integer>();
		for (int i = 0; i < 358; i++) {
			if (!nodeCovered.contains(i)) {
				System.out.println("######deal with dicccol " + i);
				nodeCovered.add(i);
				stack.push(i);
				// System.out.println("+push "+i);
				while (!stack.empty()) {
					int currentRoot = stack.pop();
					// System.out.println("-pop "+currentRoot);
					for (int j = 0; j < 358; j++) {
						int node = (int) inputM.get(currentRoot, j);
						if ((node > 0) && (!nodeCovered.contains(j))) {
							nodeCovered.add(j);
							stack.push(j);
							System.out.println(j);
						} // if
					} // for j
				} // while
			} // if
		} // for

		if (nodeCovered.size() != 358)
			System.out.println("Error happened!");
		else
			System.out.println("fihished with succeed!");

	}

	public void genePTSDDecisionFile() {
		double[][] A = new double[358][358];
		AbstractMatrix flagM1 = Matrix.fromASCIIFile(new File("./connectome/ptsd/child.ttest.finalDecision"));
		AbstractMatrix flagM2 = Matrix.fromASCIIFile(new File("./connectome/ptsd/adult.ttest.finalDecision"));
		AbstractMatrix flagM3 = Matrix.fromASCIIFile(new File("./connectome/ptsd/elder.ttest.finalDecision"));
		int flagCount = 0;
		for (int m = 0; m < 357; m++)
			for (int n = m + 1; n < 358; n++) {
				int flag1 = (int) flagM1.get(m, n);
				int flag2 = (int) flagM2.get(m, n);
				int flag3 = (int) flagM3.get(m, n);
				if ((flag1 != 0) || (flag2 != 0) || (flag3 != 0)) {
					A[m][n] = 1.0;
					flagCount++;
				}
			} // for
		findModelUtil.writeArrayListToFile(A, 358, 358, "./connectome/ptsd/allOR.ttest.finalDecision");
		System.out.println("Total select " + flagCount + " pairs!");
	}

	public void genePTSDClusterInputFile() throws IOException {

		// the result of ttest
		AbstractMatrix flagM = Matrix.fromASCIIFile(new File("./connectome/ptsd/allOR.ttest.finalDecision"));
		int flagCount = 0;
		for (int m = 0; m < 357; m++)
			for (int n = m + 1; n < 358; n++) {
				int flag = (int) flagM.get(m, n);
				if (flag != 0)
					flagCount++;
			}
		System.out.println("Total select " + flagCount + " pairs!");

		// double[][] A = new double[357 * 356 / 2 + 357][28];
		double[][] A = new double[flagCount][196]; // Modify**************
		// subject infor
		File inputWorkbook = new File("./connectome/ptsd/info.xls");
		Workbook w;
		List<String> classList = new ArrayList<String>();
		List<String> dicccolRList = new ArrayList<String>();
		int count1 = -1;
		try {
			w = Workbook.getWorkbook(inputWorkbook);
			Sheet sheet = w.getSheet(0);

			// *****************************************deal with patient
			for (int i = 5; i < 30; i++) {

				Cell cell1 = sheet.getCell(9, i);
				Cell cell2 = sheet.getCell(10, i);
				if (cell2.getContents().trim().equalsIgnoreCase("1")) {
					count1++;
					classList.add("PTSD-CHILD");
					System.out.println("File-ID: " + cell1.getContents());
					AbstractMatrix inputM = Matrix.fromASCIIFile(new File("./connectome/ptsd/connectivity/funcC."
							+ cell1.getContents()));
					int count2 = 0;
					for (int m = 0; m < 357; m++)
						for (int n = m + 1; n < 358; n++) {
							int flag = (int) flagM.get(m, n);
							if (flag != 0) {
								A[count2++][count1] = inputM.get(m, n);
								if (count1 == 0)
									dicccolRList.add(m + "-" + n);
							}

						}
				}
			} // for i

			for (int i = 5; i < 51; i++) {

				Cell cell1 = sheet.getCell(5, i);
				Cell cell2 = sheet.getCell(6, i);
				if (cell2.getContents().trim().equalsIgnoreCase("1")) {
					count1++;
					classList.add("PTSD-ADULT");
					System.out.println("File-ID: " + cell1.getContents());
					AbstractMatrix inputM = Matrix.fromASCIIFile(new File("./connectome/ptsd/connectivity/funcC."
							+ cell1.getContents()));
					int count2 = 0;
					for (int m = 0; m < 357; m++)
						for (int n = m + 1; n < 358; n++) {
							int flag = (int) flagM.get(m, n);
							if (flag != 0) {
								A[count2++][count1] = inputM.get(m, n);
								if (count1 == 0)
									dicccolRList.add(m + "-" + n);
							}

						}
				}
			} // for i

			for (int i = 5; i < 28; i++) {

				Cell cell1 = sheet.getCell(1, i);
				Cell cell2 = sheet.getCell(2, i);
				if (cell2.getContents().trim().equalsIgnoreCase("1")) {
					count1++;
					classList.add("PTSD-EDLER");
					System.out.println("File-ID: " + cell1.getContents());
					AbstractMatrix inputM = Matrix.fromASCIIFile(new File("./connectome/ptsd/connectivity/funcC."
							+ cell1.getContents()));
					int count2 = 0;
					for (int m = 0; m < 357; m++)
						for (int n = m + 1; n < 358; n++) {
							int flag = (int) flagM.get(m, n);
							if (flag != 0) {
								A[count2++][count1] = inputM.get(m, n);
								if (count1 == 0)
									dicccolRList.add(m + "-" + n);
							}

						}
				}
			} // for i

			// *****************************************deal with controls
			for (int i = 5; i < 34; i++) {

				Cell cell1 = sheet.getCell(11, i);
				Cell cell2 = sheet.getCell(12, i);
				if (cell2.getContents().trim().equalsIgnoreCase("1")) {
					count1++;
					classList.add("PTSD-CHILD-Ctrl");
					System.out.println("File-ID: " + cell1.getContents());
					AbstractMatrix inputM = Matrix.fromASCIIFile(new File("./connectome/ptsd/connectivity/funcC."
							+ cell1.getContents()));
					int count2 = 0;
					for (int m = 0; m < 357; m++)
						for (int n = m + 1; n < 358; n++) {
							int flag = (int) flagM.get(m, n);
							if (flag != 0) {
								A[count2++][count1] = inputM.get(m, n);
							}

						}
				}

			} // for i

			for (int i = 5; i < 58; i++) {

				Cell cell1 = sheet.getCell(7, i);
				Cell cell2 = sheet.getCell(8, i);
				if (cell2.getContents().trim().equalsIgnoreCase("1")) {
					count1++;
					classList.add("PTSD-ADULT-Ctrl");
					System.out.println("File-ID: " + cell1.getContents());
					AbstractMatrix inputM = Matrix.fromASCIIFile(new File("./connectome/ptsd/connectivity/funcC."
							+ cell1.getContents()));
					int count2 = 0;
					for (int m = 0; m < 357; m++)
						for (int n = m + 1; n < 358; n++) {
							int flag = (int) flagM.get(m, n);
							if (flag != 0) {
								A[count2++][count1] = inputM.get(m, n);
							}

						}
				}

			} // for i

			for (int i = 5; i < 28; i++) {

				Cell cell1 = sheet.getCell(3, i);
				Cell cell2 = sheet.getCell(4, i);
				if (cell2.getContents().trim().equalsIgnoreCase("1")) {
					count1++;
					classList.add("PTSD-ELDER-Ctrl");
					System.out.println("File-ID: " + cell1.getContents());
					AbstractMatrix inputM = Matrix.fromASCIIFile(new File("./connectome/ptsd/connectivity/funcC."
							+ cell1.getContents()));
					int count2 = 0;
					for (int m = 0; m < 357; m++)
						for (int n = m + 1; n < 358; n++) {
							int flag = (int) flagM.get(m, n);
							if (flag != 0) {
								A[count2++][count1] = inputM.get(m, n);
							}

						}
				}

			} // for i

		} catch (BiffException e) {
			e.printStackTrace();
		}
		this.writeArrayListToFile(A, flagCount, ++count1, classList, dicccolRList,
				"./connectome/ptsd/clusterInput_filtered_ptsd_all_OR.txt");

	}

	public void statisticDicccolRatio_network() throws BiffException, IOException {
		List<String> statisticResult = new ArrayList<String>();
		List<String> dicccolNetworkList = djVtkUtil.loadFileToArrayList("./connectome/networklist.txt");
		String[] dataSet = { "mci", "sz", "ptsd-child", "ptsd-adult", "ptsd-elder" };
		File inputWorkbook = new File("./connectome/analyze.xls");
		Workbook w;
		w = Workbook.getWorkbook(inputWorkbook);
		Sheet sheet;

		Cell cell1, cell2;
		boolean flag = false;
		for (int n = 0; n < dicccolNetworkList.size(); n++) { // for each
																// network
			String networkName = dicccolNetworkList.get(n).split("\\s+")[0];
			System.out.println("begin to analyze network: " + networkName);
			String resultLine = "";
			for (int d = 0; d < dataSet.length; d++) { // for each data set
				System.out.println("begin to analyze data set : " + dataSet[d]);
				sheet = w.getSheet(dataSet[d]);

				flag = false;
				for (int r = 4; r < 46; r++) {
					System.out.println("the fist cluster and r : " + r);
					cell1 = sheet.getCell(1, r);
					if (cell1.getContents().trim().equals(networkName)) {
						flag = true;
						cell2 = sheet.getCell(3, r);
						resultLine += cell2.getContents().trim() + " ";
						break;
					} // if
				} // for r
				if (flag == false)
					resultLine += "0.0 ";

				flag = false;
				for (int r = 4; r < 46; r++) {
					System.out.println("the second cluster and r : " + r);
					cell1 = sheet.getCell(5, r);
					if (cell1.getContents().trim().equals(networkName)) {
						flag = true;
						cell2 = sheet.getCell(7, r);
						resultLine += cell2.getContents().trim() + " ";
						break;
					} // if
				} // for r
				if (flag == false)
					resultLine += "0.0 ";
			} // for d
			statisticResult.add(resultLine);
		} // for n
		djVtkUtil.writeArrayListToFile(statisticResult, "./connectome/statisticResult.txt");
	}

	public void geneClusterInputFile() throws IOException {

		// the result of ttest
		AbstractMatrix flagM = Matrix.fromASCIIFile(new File("./connectome/pce/pce.func.p001.finalDecision"));
		int flagCount = 0;
		for (int m = 0; m < 357; m++)
			for (int n = m + 1; n < 358; n++) {
				int flag = (int) flagM.get(m, n);
				if (flag != 0)
					flagCount++;
			}
		System.out.println("Total select " + flagCount + " pairs!");

		for (int i = 0; i < 358; i++) {
			flagM.set(14, i, 0.0);
			flagM.set(i, 14, 0.0);

			flagM.set(15, i, 0.0);
			flagM.set(i, 15, 0.0);

			flagM.set(24, i, 0.0);
			flagM.set(i, 24, 0.0);

			flagM.set(42, i, 0.0);
			flagM.set(i, 42, 0.0);

			flagM.set(64, i, 0.0);
			flagM.set(i, 64, 0.0);

			flagM.set(70, i, 0.0);
			flagM.set(i, 70, 0.0);

			flagM.set(166, i, 0.0);
			flagM.set(i, 166, 0.0);

			flagM.set(167, i, 0.0);
			flagM.set(i, 167, 0.0);

			flagM.set(170, i, 0.0);
			flagM.set(i, 170, 0.0);

			flagM.set(352, i, 0.0);
			flagM.set(i, 352, 0.0);
		}

		flagCount = 0;
		for (int m = 0; m < 357; m++)
			for (int n = m + 1; n < 358; n++) {
				int flag = (int) flagM.get(m, n);
				if (flag != 0)
					flagCount++;
			}
		System.out.println("Total select " + flagCount + " pairs!");

		// double[][] A = new double[357 * 356 / 2 + 357][28];
		double[][] A = new double[flagCount][55]; // Modify**************
		// subject infor
		File inputWorkbook = new File("./connectome/pce/infor.xls");
		Workbook w;
		List<String> classList = new ArrayList<String>();
		List<String> dicccolRList = new ArrayList<String>();
		int count1 = -1;
		try {
			w = Workbook.getWorkbook(inputWorkbook);
			Sheet sheet = w.getSheet(0);

			// deal with patient
			for (int i = 3; i < 45; i++) {

				Cell cell1 = sheet.getCell(0, i);
				Cell cell2 = sheet.getCell(2, i);
				if (cell2.getContents().trim().equalsIgnoreCase("1")) {
					count1++;
					classList.add("PCE");
					System.out.println("File-ID: " + cell1.getContents());

					AbstractMatrix inputM = null;
					File dir = new File("./connectome/pce/connectivity/func/");
					String[] children = dir.list();

					for (int l = 0; l < children.length; l++) {
						// Get filename of file or directory
						String filename = children[l];
						if (filename.contains(".TIS" + cell1.getContents()))
							inputM = Matrix.fromASCIIFile(new File("./connectome/pce/connectivity/func/" + filename));
					}

//					 AbstractMatrix inputM = Matrix.fromASCIIFile(new File(
//					 "./connectome/mci/connectivity/funcC." +
//					 cell1.getContents()));
					int count2 = 0;
					for (int m = 0; m < 357; m++)
						for (int n = m + 1; n < 358; n++) {
							int flag = (int) flagM.get(m, n);
							if (flag != 0) {
								A[count2++][count1] = inputM.get(m, n);
								if (count1 == 0)
									dicccolRList.add(m + "-" + n);
							}

						}
				}
			} // for i

			// deal with controls
			for (int i = 45; i < 79; i++) {

				Cell cell1 = sheet.getCell(0, i);
				Cell cell2 = sheet.getCell(2, i);
				if (cell2.getContents().trim().equalsIgnoreCase("1")) {
					count1++;
					classList.add("PCE-Ctrl");
					System.out.println("File-ID: " + cell1.getContents());
					
					AbstractMatrix inputM = null;
					File dir = new File("./connectome/pce/connectivity/func/");
					String[] children = dir.list();

					for (int l = 0; l < children.length; l++) {
						// Get filename of file or directory
						String filename = children[l];
//						if(cell1.getContents().equals("31031"))
//							System.out.println("ddd");
						if (filename.contains(".TIS" + cell1.getContents()))
							inputM = Matrix.fromASCIIFile(new File("./connectome/pce/connectivity/func/" + filename));
					}

//					 AbstractMatrix inputM = Matrix.fromASCIIFile(new
//					 File("./connectome/mci/connectivity/funcC."
//					 + cell1.getContents()));
					int count2 = 0;
					for (int m = 0; m < 357; m++)
						for (int n = m + 1; n < 358; n++) {
							int flag = (int) flagM.get(m, n);
							if (flag != 0) {
								A[count2++][count1] = inputM.get(m, n);
							}

						}
				}

			} // for i

		} catch (BiffException e) {
			e.printStackTrace();
		}
		this.writeArrayListToFile(A, flagCount, ++count1, classList, dicccolRList,
				"./connectome/pce/clusterInput_filtered_pce_1017_func_1.txt");

	}

	public void writeArrayListToFile(double[][] dataArray, int dimRow, int dimColumn, List<String> classList,
			List<String> dicccolRList, String fileName) {
		try {
			System.out.println("Begin to write file:" + fileName + "...");
			FileWriter fw = null;
			fw = new FileWriter(fileName);

			fw.write("YORF	");
			for (int j = 0; j < dimColumn; j++)
				fw.write(classList.get(j) + "	");
			fw.write("\r\n");

			for (int i = 0; i < dimRow; i++) {
				fw.write(dicccolRList.get(i) + "	");
				for (int j = 0; j < dimColumn; j++)
					fw.write(dataArray[i][j] + "	");
				fw.write("\r\n");
			}
			fw.close();
			System.out.println("Write file " + fileName + " done!");
		} catch (IOException ex) {
			Logger.getLogger(djVtkUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws BiffException
	 */
	public static void main(String[] args) throws IOException, BiffException {
		// TODO Auto-generated method stub
		network mainHandler = new network();
//		mainHandler.geneClusterInputFile();
		 mainHandler.genePTSDDecisionFile();
		// mainHandler.statisticDicccolRatio_network();

	}

}
