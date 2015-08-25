

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import edu.uga.liulab.djVtkBase.*;

public class landMarkExpADNI {
	public List<String> IdList;

	public void forJournalPaper() {
		List<String> feature10 = djVtkUtil.loadFileToArrayList("./10_WorkingMem_3_featureList.txt");
		int size = feature10.size();
		List<String> strBeforeList = djVtkUtil.loadFileToArrayList("./tsbefore.txt");
		List<String> strAfterList = djVtkUtil.loadFileToArrayList("./tsafter.txt");
		double[][] tsSDMatrixBefore = new double[16][16];
		double[][] tsSDMatrixAfter = new double[16][16];
		for (int i = 0; i < 16; i++) {
			String[] currentBefore = strBeforeList.get(i).split("\\s+");
			String[] currentAfter = strAfterList.get(i).split("\\s+");
			for (int j = 0; j < 16; j++) {
				tsSDMatrixBefore[i][j] = Float.valueOf(currentBefore[j]);
				tsSDMatrixAfter[i][j] = Float.valueOf(currentAfter[j]);
			}
		}
		findModelUtil.writeVtkMatrix(tsSDMatrixBefore, 16, 16, "tsBefore.vtk");
		findModelUtil.writeVtkMatrix(tsSDMatrixAfter, 16, 16, "tsAfter.vtk");

	}

	public void statDisBetweenR() {
		List<List<String>> result = new ArrayList<List<String>>();
		for (int id = 0; id < IdList.size(); id++) {
			List<String> currentList = new ArrayList<String>();
			String subID = IdList.get(id);
			AbstractMatrix preResultR1 = Matrix
					.fromASCIIFile(new File("./PredictionResult/LandMarkPredictionResult_1_238_" + subID + "_ADNI_R1.txt"));
			AbstractMatrix preResultR2 = Matrix
					.fromASCIIFile(new File("./PredictionResult/LandMarkPredictionResult_1_238_" + subID + "_ADNI_R2.txt"));
			djVtkSurData surDataR1 = new djVtkSurData("./PredictionResult/" + subID + "To19ascsurfR1.vtk");
			djVtkSurData surDataR2 = new djVtkSurData("./PredictionResult/" + subID + "To19ascsurfR2.vtk");
			float dis = 0.0f;
			for (int i = 0; i < 238; i++) {
				int ptR1 = (int) preResultR1.get(i, 15);
				int ptR2 = (int) preResultR2.get(i, 15);
				float tmpDis = djVtkUtil.calDistanceOfPoints(surDataR1.getPoint(ptR1), surDataR2.getPoint(ptR2));
				currentList.add(String.valueOf(tmpDis));
				System.out.println("the th " + i + "dis is : " + tmpDis);
				dis += tmpDis;
			}
			result.add(currentList);
			dis /= 238.0;
			System.out.println("##########ave dis is : " + dis);
		} // for
		djVtkUtil.writeStringArrayListToFile(result, " ", 238, "./PredictionResult/ADNI_statResult.txt");
	}

	public void prepareRoiIndexForDegang() {
		for (int id = 0; id < IdList.size(); id++) {
			List<String> currentList = new ArrayList<String>();
			String subID = IdList.get(id);
			AbstractMatrix preResult = Matrix.fromASCIIFile(new File("./PredictionResult/LandMarkPredictionResult_1_238_" + subID + "_SZ.txt"));
			for (int i = 0; i < 238; i++) {
				int pt = (int) preResult.get(i, 15);
				currentList.add(String.valueOf(pt));
			}
			djVtkUtil.writeArrayListToFile(currentList, "SZ_" + subID + "_238.txt");
		}
	}

	public void prepareGridIndex() {
		List<String> gridFromKaiming = djVtkUtil.loadFileToArrayList("./para.p1.5P1.4.group12.result.txt");

		List<List<String>> result = new ArrayList<List<String>>();
		String currentStr;
		for (int i = 0; i < gridFromKaiming.size(); i++) {
			List<String> currentList = new ArrayList<String>();
			currentStr = gridFromKaiming.get(i);
			if (currentStr.startsWith("ROI")) {
				i++;
				currentStr = gridFromKaiming.get(i);
				String g1 = gridFromKaiming.get(i).split("\\.")[2];
				i = i + 9;
				String g2 = gridFromKaiming.get(i).split("\\.")[2];
				currentList.add(g1);
				currentList.add(g2);
				result.add(currentList);
			}
		}
		djVtkUtil.writeStringArrayListToFile(result, " ", 2, "./PredictionResult/FinalGrid2.txt");
	}

	public void preparepointIndex() {
		int rowNum = 573;
		AbstractMatrix gridM = Matrix.fromASCIIFile(new File("./FinalGrid2.txt"));
		AbstractMatrix optiG1 = Matrix.fromASCIIFile(new File("./landMarkResult_1_all.txt"));
		AbstractMatrix optiG2 = Matrix.fromASCIIFile(new File("./landMarkResult_2_all.txt"));

		List<List<String>> result = new ArrayList<List<String>>();
		String currentStr;
		for (int i = 0; i < rowNum; i++) {
			List<String> currentList = new ArrayList<String>();
			// note g1,g2 is from 0, that is why j=g1*5;j<(g1*5+5);j++
			int g1 = (int) gridM.get(i, 0);
			int g2 = (int) gridM.get(i, 1);
			for (int j = g1 * 5; j < (g1 * 5 + 5); j++) {
				String ptID = String.valueOf((int) optiG1.get(j, 3));
				currentList.add(ptID);
			}
			for (int j = g2 * 5; j < (g2 * 5 + 5); j++) {
				String ptID = String.valueOf((int) optiG2.get(j, 3));
				currentList.add(ptID);
			}
			result.add(currentList);
		}
		djVtkUtil.writeStringArrayListToFile(result, " ", 10, "./GridPoint_573.txt");
	}

	public void prepareGridIndex_back() {
		List<String> gridFromKaiming = djVtkUtil.loadFileToArrayList("./ROI_Recorded.txt");
		List<String> needRemove = djVtkUtil.loadFileToArrayList("./cereLines.txt");
		List<Integer> gridRemove = new ArrayList<Integer>();
		for (int i = 0; i < needRemove.size(); i++) {
			int gridID = Integer.valueOf(needRemove.get(i).split("_")[3].split("\\.")[0]);
			gridRemove.add(gridID);
		}
		List<List<String>> result = new ArrayList<List<String>>();
		for (int i = 0; i < gridFromKaiming.size(); i++) {
			List<String> currentList = new ArrayList<String>();
			if (!gridRemove.contains(i)) {
				String g1 = gridFromKaiming.get(i).split(":")[1].split("\\.")[2];
				String g2 = gridFromKaiming.get(i).split(":")[2].split("\\.")[2];
				String g3 = gridFromKaiming.get(i).split(":")[3].split("\\.")[2];
				currentList.add(g1);
				currentList.add(g2);
				currentList.add(g3);
				result.add(currentList);
			}
		}
		djVtkUtil.writeStringArrayListToFile(result, " ", 3, "./PredictionResult/FinalGrid2.txt");
	}

	public void selectFibers() throws IOException {
		List<String> gridInfo = djVtkUtil.loadFileToArrayList("manualPick-05-03_dj.txt");
		// AbstractMatrix gridM = Matrix.fromASCIIFile(new File("./roi_474.2.txt"));
		// List<List<String>> roiM = landMarkUtil.loadData("roi_474.3.txt", " ");
		// roiM.get(0).set(0, "-1");
		int numX = 0;
		int num0 = 0;
		int num1 = 0;
		int num6 = 0;
		int num = 0;
		Runtime.getRuntime().exec(" mkdir ./0504fibers ");
		for (int i = 0; i < gridInfo.size(); i++) {
			num++;
			if (gridInfo.get(i).startsWith("x"))
				numX++;
			if (gridInfo.get(i).startsWith("0") || gridInfo.get(i).startsWith("5"))
				num0++;
			if (gridInfo.get(i).startsWith("1"))
				num1++;
			if (gridInfo.get(i).startsWith("6")) {
				String[] currentLine = gridInfo.get(i).split(" ");
				for (int j = 1; j < currentLine.length; j++) {
					int roiID = i;
					int subID = Integer.valueOf(currentLine[j].split(":")[0]) - 1;
					String strFlag = currentLine[j].split(":")[1];
					int manPickPtID = -1;
					if (!strFlag.equalsIgnoreCase("good")) {
						manPickPtID = Integer.valueOf(strFlag);
						System.out.println("roiID = " + roiID + "   subID = " + subID + "   manPickPtID = " + manPickPtID);
						Runtime.getRuntime().exec(" cp ./roiFibers/roi_474.5/fiber.roi.sub." + subID + ".sid." + manPickPtID + ".vtk ./0504fibers/ ");

					}

					// ///////////////
					// int roiID = i;
					// int subID = Integer.valueOf(currentLine[j])-1;
					// int pointID = (int) gridM.get(roiID, subID);
					// // Runtime.getRuntime().exec(
					// // "cp ./roiFibers/fiber.roi.sub." + subID + ".sid." + pointID
					// // + ".vtk ./selectedFibers");
					//
				}
				num6++;
			}
		}

	}

	public void stat() throws IOException {
		List<String> gridInfo = djVtkUtil.loadFileToArrayList("manualPick-05-03_dj.txt");
		// AbstractMatrix gridM = Matrix.fromASCIIFile(new File("./roi_474.2.txt"));
		// List<List<String>> roiM = landMarkUtil.loadData("roi_474.3.txt", " ");
		// roiM.get(0).set(0, "-1");
		int numX = 0;
		int num0 = 0;
		int num1 = 0;
		int num6 = 0;
		int num = 0;
		for (int i = 0; i < gridInfo.size(); i++) {
			num++;
			if (gridInfo.get(i).startsWith("x"))
				numX++;
			if (gridInfo.get(i).startsWith("0") || gridInfo.get(i).startsWith("5"))
				num0++;
			if (gridInfo.get(i).startsWith("1"))
				num1++;
			if (gridInfo.get(i).startsWith("6")) {
				String[] currentLine = gridInfo.get(i).split(" ");
				for (int j = 1; j < currentLine.length; j++) {
					// int roiID = i;
					// int subID = Integer.valueOf(currentLine[j].split(":")[0]) - 1;
					// String strFlag = currentLine[j].split(":")[1];
					// int manPickPtID = -1;
					// if (!strFlag.equalsIgnoreCase("good")) {
					// manPickPtID = Integer.valueOf(strFlag);
					// System.out.println("roiID = " + roiID + "   subID = " + subID + "   manPickPtID = " + manPickPtID);
					// roiM.get(i).set(subID, strFlag);
					// }

					// ///////////////
					// int roiID = i;
					// int subID = Integer.valueOf(currentLine[j])-1;
					// int pointID = (int) gridM.get(roiID, subID);
					// // Runtime.getRuntime().exec(
					// // "cp ./roiFibers/fiber.roi.sub." + subID + ".sid." + pointID
					// // + ".vtk ./selectedFibers");
					//
				}
				num6++;
			}
		}
		System.out.println("numAll:" + num);
		System.out.println("numX:" + numX);
		System.out.println("num0,5:" + num0);
		System.out.println("num1:" + num1);
		System.out.println("num6:" + num6);
		// djVtkUtil.writeStringArrayListToFile(roiM, " ", 10, "roi_474.4.txt");

	}

	public void generateNewOptiGridDes() {
		List<String> gridInfo = djVtkUtil.loadFileToArrayList("manualPick-05-03_dj.txt");
		List<String> newDescriptor = new ArrayList<String>();
		for (int i = 0; i < gridInfo.size(); i++) {
			String newLine = "";
			if (gridInfo.get(i).startsWith("6")) {
				newLine = newLine + "6 ";
				String[] currentLine = gridInfo.get(i).split(" ");
				for (int j = 1; j < currentLine.length; j++) {
					int roiID = i;
					int subID = Integer.valueOf(currentLine[j].split(":")[0]);
					newLine = newLine + subID + " ";
				}
			} else
				newLine = newLine + "0";
			newDescriptor.add(newLine);
		}
		djVtkUtil.writeArrayListToFile(newDescriptor, "manualPick-05-03_djDataGridInfo.txt");
	}

	public void replaceNewRoi() {
		String fileNewRoi = "LandMarkResult_1_474_0504_1017.txt";
		List<String> newROIInfo = djVtkUtil.loadFileToArrayList(fileNewRoi);
		List<String> gridInfo = djVtkUtil.loadFileToArrayList("roi_474.4.txt");
		for (int i = 0; i < newROIInfo.size();) {
			int roiID = Integer.valueOf(newROIInfo.get(i).split(" ")[0]);
			String newLine = "";
			for (int j = 0; j < 10; j++) {
				String currentLine = newROIInfo.get(i++);
				if (Integer.valueOf(currentLine.split(" ")[0]) != roiID) {
					System.out.println("ERROR happened when i=" + i);
					System.exit(0);
				}
				newLine = newLine + currentLine.split(" ")[3] + " ";
			}
			gridInfo.set(roiID - 1, newLine);
		}
		djVtkUtil.writeArrayListToFile(gridInfo, "roi_474.5.txt");
	}

	public void generateROIfibers() throws IOException {
		List<List<String>> dataInfoList = landMarkUtil.loadData("DataDescriptor.txt", " ");
		AbstractMatrix roiM = Matrix.fromASCIIFile(new File("roi_474.5.txt"));
		int colNum = roiM.getColumnDimension();
		int rowNum = roiM.getRowDimension();
		for (int i = 0; i < colNum; i++) {
			System.out.println("Begin to output fibers of sub " + i + "...");
			djVtkSurData surData = new djVtkSurData(dataInfoList.get(i).get(1));
			djVtkFiberData fiberData = new djVtkFiberData(dataInfoList.get(i).get(2));
			djVtkHybridData hybridData = new djVtkHybridData(surData, fiberData);
			hybridData.mapSurfaceToBox();
			hybridData.mapFiberToBox();
			for (int j = 0; j < rowNum; j++) {
				Set ptSet = surData.getNeighbourPoints((int) roiM.get(j, i), 3);
				djVtkFiberData newFiber = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
				newFiber.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
				// newFiber.printInfo();
				Runtime.getRuntime().exec("mkdir ./roiFibers/roi_474.5");
				newFiber.writeToVtkFileCompact("./roiFibers/roi_474.5/fiber.roi.sub." + i + ".sid." + (int) roiM.get(j, i) + ".vtk");
			} // for j
		} // for i
	}

	public void generateNewGridMapping() {
		Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();
		AbstractMatrix roiM = Matrix.fromASCIIFile(new File("./data/GridPointMapping.txt"));
		List<List<String>> dataInfoList = landMarkUtil.loadData("DataDescriptor.txt", " ");
		List<String> gridList = new ArrayList<String>();

		for (int roiID = 0; roiID < 2056; roiID++) {
			System.out.println("Dealing with roi: " + roiID);
			String currentLine = "";
			djVtkSurData surData = null;
			if (surDataMap.containsKey(4)) // sub19
				surData = surDataMap.get(4);
			else {
				surData = new djVtkSurData(dataInfoList.get(4).get(1));
				surDataMap.put(4, surData);
			}
			int modelPtID = (int) roiM.get(roiID, 4);
			djVtkPoint modelPt = surData.getPoint(modelPtID);

			for (int subID = 0; subID < 15; subID++) {
				djVtkSurData currentSurData = null;
				if (surDataMap.containsKey(subID))
					currentSurData = surDataMap.get(subID);
				else {
					currentSurData = new djVtkSurData(dataInfoList.get(subID).get(1));
					surDataMap.put(subID, currentSurData);
				}

				float minDis = 1000.0f;
				int ptID = -1;
				for (int i = 0; i < currentSurData.nPointNum; i++) {
					float tmpDis = djVtkUtil.calDistanceOfPoints(modelPt, currentSurData.getPoint(i));
					if (tmpDis < minDis) {
						minDis = tmpDis;
						ptID = i;
					}
				}
				currentLine += ptID + " ";
			} // for all subjects
			gridList.add(currentLine);
		} // for all rois
		djVtkUtil.writeArrayListToFile(gridList, "NewGridMapping.txt");
	}

	public void generatePCEGridMapping() {
		Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();
		AbstractMatrix roiM = Matrix.fromASCIIFile(new File("finalizedMat.txt"));
		List<List<String>> dataInfoList = landMarkUtil.loadData("DataDescriptor.txt", " ");
		List<List<String>> pceDataInfoList = landMarkUtil.loadData("PCE_PredictionDataDescriptor.txt", " ");
		List<String> gridList = new ArrayList<String>();

		djVtkSurData surData = null;
		surData = new djVtkSurData(dataInfoList.get(0).get(1));
		for (int roiID = 0; roiID < 358; roiID++) {
			System.out.println("Dealing with roi: " + roiID);
			String currentLine = "";

			int modelPtID = (int) roiM.get(roiID, 0);
			djVtkPoint modelPt = surData.getPoint(modelPtID);

			for (int subID = 0; subID < 20; subID++) {
				djVtkSurData currentSurData = null;
				if (surDataMap.containsKey(subID))
					currentSurData = surDataMap.get(subID);
				else {
					currentSurData = new djVtkSurData(pceDataInfoList.get(subID).get(1));
					surDataMap.put(subID, currentSurData);
				}

				float minDis = 1000.0f;
				int ptID = -1;
				for (int i = 0; i < currentSurData.nPointNum; i++) {
					float tmpDis = djVtkUtil.calDistanceOfPoints(modelPt, currentSurData.getPoint(i));
					if (tmpDis < minDis) {
						minDis = tmpDis;
						ptID = i;
					}
				}
				currentLine += ptID + " ";
			} // for all subjects
			gridList.add(currentLine);
		} // for all rois
		djVtkUtil.writeArrayListToFile(gridList, "PCEGridMapping.txt");
	}

	public void calAveCoord() {
		Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();
		AbstractMatrix roiM = Matrix.fromASCIIFile(new File("finalizedMat.txt"));
		List<List<String>> dataInfoList = landMarkUtil.loadData("DataDescriptor.txt", " ");

		List<String> disList = new ArrayList<String>();
		List<String> aveDisList = new ArrayList<String>();

		for (int roiID = 0; roiID < 358; roiID++) {
			djVtkPoint avePt = new djVtkPoint();
			avePt.x = 0.0f;
			avePt.y = 0.0f;
			avePt.z = 0.0f;
			List<djVtkPoint> currentRoiPtList = new ArrayList<djVtkPoint>();
			djVtkSurData surData = null;
			for (int subID = 0; subID < 10; subID++) {
				if (surDataMap.containsKey(subID))
					surData = surDataMap.get(subID);
				else {
					surData = new djVtkSurData(dataInfoList.get(subID).get(1));
					surDataMap.put(subID, surData);
				}
				djVtkPoint tmpPt = surData.getPoint((int) roiM.get(roiID, subID));
				currentRoiPtList.add(tmpPt);
				avePt.x = avePt.x + tmpPt.x;
				avePt.y = avePt.y + tmpPt.y;
				avePt.z = avePt.z + tmpPt.z;
			}
			avePt.x = avePt.x / 10.0f;
			avePt.y = avePt.y / 10.0f;
			avePt.z = avePt.z / 10.0f;
			String strAveCoord = "" + avePt.x + " " + avePt.y + " " + avePt.z;
			aveDisList.add(strAveCoord);
			float aveDis = 0.0f;
			String currentDisStr = "";
			for (int i = 0; i < 10; i++) {
				float dis = djVtkUtil.calDistanceOfPoints(avePt, currentRoiPtList.get(i));
				currentDisStr = currentDisStr + dis + " ";
				aveDis = aveDis + dis;
			}
			aveDis = aveDis / 10.0f;
			currentDisStr = currentDisStr + aveDis + " ";
			disList.add(currentDisStr);
			System.out.println("roiID: " + roiID + "     " + aveDis);

		} // for all rois
		djVtkUtil.writeArrayListToFile(disList, "DisInfoOf358.txt");
		djVtkUtil.writeArrayListToFile(aveDisList, "InitialROILocation.txt");
	}

	public void bigPaper_gene_prediction() {
		String fileName = "./PredictionResult/LandMarkPredictionResult_1_358_2_WorkingMem.txt";
		List<String> preResult = djVtkUtil.loadFileToArrayList(fileName);
		String subID = fileName.split("_")[3];
		djVtkSurData surData = new djVtkSurData("./data/" + subID + ".sur.asc.to10.vtk");
		List<djVtkPoint> ptList = new ArrayList<djVtkPoint>();
		for (int i = 0; i < 358; i++) {
			int ptID = Integer.valueOf(preResult.get(i).split(" ")[10]);
			djVtkPoint tmpPt = surData.getPoint(ptID);
			ptList.add(tmpPt);
		}
		djVtkUtil.writeToPointsVtkFile("./bigPaperResult/358_14.vtk", ptList);

	}

	public void bigPaper_gene_358() {
//		AbstractMatrix roiM = Matrix.fromASCIIFile(new File("finalizedMat.txt"));
		List<List<String>> dataInfoList = landMarkUtil.loadData("DataDescriptor.txt", " ");

		for (int colIndex = 100; colIndex < 102; colIndex++) {
			djVtkSurData surData = new djVtkSurData(dataInfoList.get(colIndex).get(1));
			AbstractMatrix roiM = Matrix.fromASCIIFile(new File(dataInfoList.get(colIndex).get(3)));
			List<djVtkPoint> ptList = new ArrayList<djVtkPoint>();
			for (int rowIndex = 0; rowIndex < 358; rowIndex++) {
				int ptID = (int) roiM.get(rowIndex, Integer.valueOf( dataInfoList.get(colIndex).get(4)) );
				djVtkPoint tmpPt = surData.getPoint(ptID);
				ptList.add(tmpPt);
			} // for all rows
			djVtkUtil.writeToPointsVtkFile("./fig3/0612/358_" + dataInfoList.get(colIndex).get(0) + ".vtk", ptList);
		} // for all columns
	}

	public void bigPaper_gene_2056Pt() {
		List<String> optiResult = djVtkUtil.loadFileToArrayList("landMarkResult_1_all.txt");
		List<List<String>> dataInfoList = landMarkUtil.loadData("DataDescriptor.txt", " ");

		Map<Integer, List<djVtkPoint>> ptListMap = new HashMap<Integer, List<djVtkPoint>>();
		Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();
		djVtkSurData surData = null;

		for (int lineIndex = 0; lineIndex < optiResult.size(); lineIndex++) {
			String[] elesCurrentLine = optiResult.get(lineIndex).split(" ");
			int roiID = Integer.valueOf(elesCurrentLine[0]);
			int subID = Integer.valueOf(elesCurrentLine[1]);
			int ptID = Integer.valueOf(elesCurrentLine[2]);
			if (surDataMap.containsKey(subID))
				surData = surDataMap.get(subID);
			else {
				surData = new djVtkSurData(dataInfoList.get(subID).get(1));
				surDataMap.put(subID, surData);
			}
			djVtkPoint tmpPt = surData.getPoint(ptID);
			if (ptListMap.containsKey(subID))
				ptListMap.get(subID).add(tmpPt);
			else {
				List<djVtkPoint> newPtList = new ArrayList<djVtkPoint>();
				newPtList.add(tmpPt);
				ptListMap.put(subID, newPtList);
			}
		} // for all lines in the result

		for (int i = 0; i < 16; i++) {
			if (ptListMap.containsKey(i)) {
				djVtkUtil.writeToPointsVtkFile("./LiuFigRandom/2056_" + i + ".vtk", ptListMap.get(i));
			}
		}
	}

	public void bigPaper_readSurAttri() {
		List<List<String>> dataInfoList = landMarkUtil.loadData("DataDescriptor.txt", " ");
		for (int i = 0; i < 15; i++) {
			int subID = Integer.valueOf(dataInfoList.get(i).get(0).split("_")[0]);
			djVtkSurData surData = new djVtkSurData("./data/" + subID + ".sur.asc.to10.vtk");
			djVtkSurData surDataHammer = new djVtkSurData("./bigPaperResult/selWMHammer/" + subID + ".wm.hammer.vtk");
			surData.pointsScalarData = new HashMap<String, List<String>>();
			surData.pointsScalarData.put("LobeLabe", surDataHammer.pointsScalarData.get("Colors"));
			surData.cellsOutput = surData.cells;
			surData.writeToVtkFileCompact("./bigPaperResult/surWithLobe/" + subID + ".regTo10.lobe.vtk");

		}

	}

	public void bigPaper_gene_all358fibers() {
		List<List<String>> dataInfoList = landMarkUtil.loadData("DataDescriptor.txt", " ");
		AbstractMatrix roiM = Matrix.fromASCIIFile(new File("./PredictionResult/all358Result.txt"));
		int colNum = roiM.getColumnDimension();
		int rowNum = roiM.getRowDimension();
		for (int i = 0; i < colNum; i++) {
			System.out.println("Begin to output fibers of sub " + i + "...");
			djVtkSurData surData = new djVtkSurData(dataInfoList.get(i).get(1));
			djVtkFiberData fiberData = new djVtkFiberData(dataInfoList.get(i).get(2));
			djVtkHybridData hybridData = new djVtkHybridData(surData, fiberData);
			hybridData.mapSurfaceToBox();
			hybridData.mapFiberToBox();
			for (int j = 0; j < rowNum; j++) {
				Set ptSet = surData.getNeighbourPoints((int) roiM.get(j, i), 3);
				djVtkFiberData newFiber = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
				newFiber.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
				// newFiber.printInfo();
				newFiber.writeToVtkFileCompact("./bigPaperResult/allfibers/fiber.roi." + j + ".sub." + i + ".pid." + (int) roiM.get(j, i) + ".vtk");
			} // for j
		} // for i

	}

	public void bigPaper_gene_all358() {
		List<String> allResult = new ArrayList<String>();
		List<String> optiResult = djVtkUtil.loadFileToArrayList("finalizedMat.txt");
		List<String> preResult25 = djVtkUtil.loadFileToArrayList("./PredictionResult/LandMarkPredictionResult_1_358_25_WorkingMem.txt");
		List<String> preResult26 = djVtkUtil.loadFileToArrayList("./PredictionResult/LandMarkPredictionResult_1_358_26_WorkingMem.txt");
		List<String> preResult27 = djVtkUtil.loadFileToArrayList("./PredictionResult/LandMarkPredictionResult_1_358_27_WorkingMem.txt");
		List<String> preResult28 = djVtkUtil.loadFileToArrayList("./PredictionResult/LandMarkPredictionResult_1_358_28_WorkingMem.txt");
		List<String> preResult2 = djVtkUtil.loadFileToArrayList("./PredictionResult/LandMarkPredictionResult_1_358_2_WorkingMem.txt");
		for (int i = 0; i < 358; i++) {
			String currentLine = optiResult.get(i);
			currentLine = currentLine + " " + preResult25.get(i).split(" ")[10];
			currentLine = currentLine + " " + preResult26.get(i).split(" ")[10];
			currentLine = currentLine + " " + preResult27.get(i).split(" ")[10];
			currentLine = currentLine + " " + preResult28.get(i).split(" ")[10];
			currentLine = currentLine + " " + preResult2.get(i).split(" ")[10];
			allResult.add(currentLine);
		}
		djVtkUtil.writeArrayListToFile(allResult, "./PredictionResult/all358Result.txt");
	}

	public void generatefibers() throws IOException {

		djVtkSurData surData = new djVtkSurData("./data/10.sur.asc.to10.vtk");
		djVtkFiberData fiberData = new djVtkFiberData("./data/10.fiber.asc.to10.vtk");
		djVtkHybridData hybridData = new djVtkHybridData(surData, fiberData);
		hybridData.mapSurfaceToBox();
		hybridData.mapFiberToBox();

		Set ptSet = surData.getNeighbourPoints(21139, 2);
		djVtkFiberData newFiber = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
		newFiber.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
		newFiber.writeToVtkFileCompact("./fiber.sub0.21139.2.vtk");

		ptSet = surData.getNeighbourPoints(21139, 5);
		newFiber = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
		newFiber.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
		newFiber.writeToVtkFileCompact("./fiber.sub0.21139.5.vtk");

		ptSet = surData.getNeighbourPoints(24949, 2);
		newFiber = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
		newFiber.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
		newFiber.writeToVtkFileCompact("./fiber.sub0.24949.2.vtk");

	}

	public void geneSelectedROI() {
		List<Integer> roiList = new ArrayList<Integer>();
		roiList.add(227);
		roiList.add(278);
		roiList.add(100);
		djVtkSurData surData;

		List<djVtkPoint> ptList = new ArrayList<djVtkPoint>();

		for (int subID = 0; subID < 15; subID++) {

			String fileName = "./fig1/358_" + subID + ".vtk";
			surData = new djVtkSurData(fileName);
			for (int i = 0; i < roiList.size(); i++) {
				ptList.clear();
				ptList.add(surData.getPoint(roiList.get(i)));
				djVtkUtil.writeToPointsVtkFile("./fig1/pic/sub" + subID + "." + roiList.get(i) + ".vtk", ptList);
			}
		}
	}

	public void prepareForROIView() {
		int roiID = 278;
		djVtkSurData surData;
		djVtkFiberData fiberData;
		List<List<String>> dataList = landMarkUtil.loadData("./fig2/data_ptsd_c/Ptsd_c_fig2.txt", " ");
		List<String> strList1 = new ArrayList<String>();
		List<String> strList2 = new ArrayList<String>();
		strList1.add("SURFACES 15");
		for (int i = 1; i < 11; i++) {
			if (i != 2) {
				System.out.println("current dealing with " + i + "th subject...");
				AbstractMatrix roiM = Matrix.fromASCIIFile(new File("./fig2/data_liulab/"+i+".predictROI.allMat"));
				int ptID = (int) roiM.get(278, 10);
				surData = new djVtkSurData("./fig2/data_liulab/"+i+".sur.asc.to10.vtk");
				fiberData = new djVtkFiberData("./fig2/data_liulab/"+i+".fiber.asc.to10.vtk");
				djVtkHybridData hybridData = new djVtkHybridData(surData, fiberData);
				hybridData.mapSurfaceToBox();
				hybridData.mapFiberToBox();
				Set ptSet = surData.getNeighbourPoints(ptID, 3);
				djVtkFiberData newFiber = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
				newFiber.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
				newFiber.writeToVtkFileCompact("./fig2/data_liulab/fiber.roi.sub." + i + ".sid." + ptID + ".vtk");
				surData=null;
				fiberData=null;
			}
		}

		// for (int i = 0; i < 15; i++) {
		// strList1.add(dataList.get(i).get(1));
		// System.out.println("current dealing with " + i + "th subject...");
		// AbstractMatrix roiM = Matrix.fromASCIIFile(new File(dataList.get(i).get(3)));
		// int ptID = (int) roiM.get(278, 10);
		// surData = new djVtkSurData(dataList.get(i).get(1));
		// fiberData = new djVtkFiberData(dataList.get(i).get(2));
		// djVtkHybridData hybridData = new djVtkHybridData(surData, fiberData);
		// hybridData.mapSurfaceToBox();
		// hybridData.mapFiberToBox();
		// Set ptSet = surData.getNeighbourPoints(ptID, 2);
		// djVtkFiberData newFiber = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
		// newFiber.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
		// newFiber.writeToVtkFileCompact("./fig2/data_ptsd_c/fiber.roi.sub." + dataList.get(i).get(0) + ".sid." + ptID + ".vtk");
		// strList2.add("fiber.roi.sub." + dataList.get(i).get(0) + ".sid." + ptID + ".vtk");
		// }
		// strList1.add("ROI roi.278");
		// strList1.addAll(strList2);
		// djVtkUtil.writeArrayListToFile(strList1, "profile_ptsd_c_1-15");

	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		landMarkExpADNI mainHandler = new landMarkExpADNI();
		mainHandler.bigPaper_gene_358();
//		mainHandler.bigPaper_gene_2056Pt();
		//
//		mainHandler.prepareForROIView();
		// int ringNum = Integer.valueOf(args[0]);
		// findModelService main = new findModelService();
		//
		// main.virSubList.add(1);
		// main.virSubList.add(2);
		// main.virSubList.add(3);
		// main.virSubList.add(5);
		// main.virSubList.add(6);
		// main.virSubList.add(7);
		// main.virSubList.add(9);
		// main.virSubList.add(10);
		// main.virSubList.add(13);
		// main.do_validateUsingFMRI(ringNum);

	}

	// String fileNewRoi = "LandMarkResult_1_474_FinalOptiSpeci.1.txt";
	// List<String> newROIInfo = djVtkUtil.loadFileToArrayList(fileNewRoi);
	// List<String> gridInfo = djVtkUtil.loadFileToArrayList("roi_474.0.txt");
	// for (int i = 0; i < newROIInfo.size();) {
	// int roiID = Integer.valueOf(newROIInfo.get(i).split(" ")[0]);
	// String newLine = "";
	// for (int j = 0; j < 10; j++) {
	// String currentLine = newROIInfo.get(i++);
	// if (Integer.valueOf(currentLine.split(" ")[0]) != roiID) {
	// System.out.println("ERROR happened when i=" + i);
	// System.exit(0);
	// }
	// newLine = newLine + currentLine.split(" ")[3] + " ";
	// }
	// gridInfo.set(roiID - 1, newLine);
	// }
	// djVtkUtil.writeArrayListToFile(gridInfo, "roi_474.1.txt");

}
