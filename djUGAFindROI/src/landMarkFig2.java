

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import edu.uga.liulab.djVtkBase.djVtkData;
import edu.uga.liulab.djVtkBase.djVtkDataDictionary;
import edu.uga.liulab.djVtkBase.djVtkFiberData;
import edu.uga.liulab.djVtkBase.djVtkHybridData;
import edu.uga.liulab.djVtkBase.djVtkPoint;
import edu.uga.liulab.djVtkBase.djVtkSurData;
import edu.uga.liulab.djVtkBase.djVtkUtil;

public class landMarkFig2 {

	public djVtkSurData inputData;
	public djVtkSurData normalModelData;
	public djVtkSurData highlightModeData;

	public void drawROIWithDis() {
		this.inputData = new djVtkSurData("./fig2_v2/358_0.vtk");
		this.normalModelData = new djVtkSurData("./fig2_v2/sphere_radius2.vtk");
		this.highlightModeData = new djVtkSurData("./fig2_v2/sphere_radius2.vtk");

		String fileName = "./fig2_v2/roiDis_2.vtk";
		List<String> attriList_model1 = djVtkUtil.loadFileToArrayList("./fig2_v2/data/DisModel_2.txt");
		List<String> attriList_model2 = djVtkUtil.loadFileToArrayList("./fig2_v2/data/DisModel_2.txt");
		List<String> attriList_modelall = djVtkUtil.loadFileToArrayList("./fig2_v2/data/DisModelAll.txt");
		List<String> attriList_preLiu = djVtkUtil.loadFileToArrayList("./fig2_v2/data/DisPreLiu.txt");
		List<String> attriList_prePtsd = djVtkUtil.loadFileToArrayList("./fig2_v2/data/DisPrePtsd.txt");
		List<String> attriList_reg = djVtkUtil.loadFileToArrayList("./fig2_v2/data/DisImproRatio.txt");
		System.out.println("Begin to write file:" + fileName + "...");
		Map<Integer, String> highLightROI = new HashMap<Integer, String>();
		FileWriter fw = null;

		try {
			// fw = new FileWriter(roiGroupInfo + ".vtk");
			fw = new FileWriter(fileName);
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
			fw.write("POINTS " + ((roiNum - highROINum) * normalModelPtNum + (highROINum * highModelPtNum))
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
				} else {
					for (int modelPtIndex = 0; modelPtIndex < normalModelPtNum; modelPtIndex++) {
						float x = this.normalModelData.getPoint(modelPtIndex).x + currentROIPt.x;
						float y = this.normalModelData.getPoint(modelPtIndex).y + currentROIPt.y;
						float z = this.normalModelData.getPoint(modelPtIndex).z + currentROIPt.z;
						fw.write(x + " " + y + " " + z + "\r\n");
						offsetList.add(normalModelPtNum);
						countNormal++;
					} // for all points in the model vtk file
				}

			} // for all points in the input vtk file

			// print cells info
			int totalCellNum = (roiNum - highROINum) * normalModelCellNum + (highROINum * highModelCellNum);
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
				} else {
					for (int modelCellIndex = 0; modelCellIndex < normalModelCellNum; modelCellIndex++) {
						int ptId1 = this.normalModelData.getcell(modelCellIndex).pointsList.get(0).pointId + offset;
						int ptId2 = this.normalModelData.getcell(modelCellIndex).pointsList.get(1).pointId + offset;
						int ptId3 = this.normalModelData.getcell(modelCellIndex).pointsList.get(2).pointId + offset;
						fw.write("3 " + ptId1 + " " + ptId2 + " " + ptId3 + " \r\n");
					} // for all cells in the model vtk file
					offset = offset + normalModelPtNum;
				}

			} // for all points in the input vtk file

			fw.write("POINT_DATA " + ((roiNum - highROINum) * normalModelPtNum + (highROINum * highModelPtNum))
					+ "\r\n");
			fw.write("SCALARS model_1 float 1 \r\n");
			fw.write("LOOKUP_TABLE default \r\n");
			for (int roiIndex = 0; roiIndex < roiNum; roiIndex++)
				for (int modelPtIndex = 0; modelPtIndex < normalModelPtNum; modelPtIndex++)
					fw.write(attriList_model1.get(roiIndex) + " \r\n");

			fw.write("SCALARS model_2 float 1 \r\n");
			fw.write("LOOKUP_TABLE default \r\n");
			for (int roiIndex = 0; roiIndex < roiNum; roiIndex++)
				for (int modelPtIndex = 0; modelPtIndex < normalModelPtNum; modelPtIndex++)
					fw.write(attriList_model2.get(roiIndex) + " \r\n");

			fw.write("SCALARS model_all float 1 \r\n");
			fw.write("LOOKUP_TABLE default \r\n");
			for (int roiIndex = 0; roiIndex < roiNum; roiIndex++)
				for (int modelPtIndex = 0; modelPtIndex < normalModelPtNum; modelPtIndex++)
					fw.write(attriList_modelall.get(roiIndex) + " \r\n");

			fw.write("SCALARS PreLiu float 1 \r\n");
			fw.write("LOOKUP_TABLE default \r\n");
			for (int roiIndex = 0; roiIndex < roiNum; roiIndex++)
				for (int modelPtIndex = 0; modelPtIndex < normalModelPtNum; modelPtIndex++)
					fw.write(attriList_preLiu.get(roiIndex) + " \r\n");

			fw.write("SCALARS PrePtsd float 1 \r\n");
			fw.write("LOOKUP_TABLE default \r\n");
			for (int roiIndex = 0; roiIndex < roiNum; roiIndex++)
				for (int modelPtIndex = 0; modelPtIndex < normalModelPtNum; modelPtIndex++)
					fw.write(attriList_prePtsd.get(roiIndex) + " \r\n");

			fw.write("SCALARS improRatio float 1 \r\n");
			fw.write("LOOKUP_TABLE default \r\n");
			for (int roiIndex = 0; roiIndex < roiNum; roiIndex++)
				for (int modelPtIndex = 0; modelPtIndex < normalModelPtNum; modelPtIndex++)
					fw.write(attriList_reg.get(roiIndex) + " \r\n");

		} catch (IOException ex) {
			Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fw.close();
			} catch (IOException ex) {
				Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println("Write file:" + fileName + ".vtk done!");

	}

	public void geneAllROIBalls() throws IOException {
		this.inputData = new djVtkSurData("./fig1/358_0.vtk");
		this.highlightModeData = new djVtkSurData("./fig1/data/sphere_radius3_5.vtk");
		int highModelPtNum = this.highlightModeData.nPointNum;
		for (int roiID = 0; roiID < this.inputData.nPointNum; roiID++) {
			String fileName = "./fig2_v2/roiBalls/" + roiID + ".vtk";
			System.out.println("Begin to write file:" + fileName + "...");
			FileWriter fw = null;
			fw = new FileWriter(fileName);
			fw.write("# vtk DataFile Version 3.0\r\n");
			fw.write("vtk output\r\n");
			fw.write("ASCII\r\n");
			fw.write("DATASET POLYDATA\r\n");
			djVtkPoint currentROIPt = this.inputData.getPoint(roiID);
			fw.write("POINTS " + highModelPtNum + " float\r\n");
			for (int modelPtIndex = 0; modelPtIndex < highModelPtNum; modelPtIndex++) {
				float x = this.highlightModeData.getPoint(modelPtIndex).x + currentROIPt.x;
				float y = this.highlightModeData.getPoint(modelPtIndex).y + currentROIPt.y;
				float z = this.highlightModeData.getPoint(modelPtIndex).z + currentROIPt.z;
				fw.write(x + " " + y + " " + z + "\r\n");
			}
			fw.write("POLYGONS " + this.highlightModeData.nCellNum + " " + (this.highlightModeData.nCellNum * 4)
					+ " \r\n");
			for (int cellID = 0; cellID < this.highlightModeData.nCellNum; cellID++) {
				int ptId1 = this.highlightModeData.getcell(cellID).pointsList.get(0).pointId;
				int ptId2 = this.highlightModeData.getcell(cellID).pointsList.get(1).pointId;
				int ptId3 = this.highlightModeData.getcell(cellID).pointsList.get(2).pointId;
				fw.write("3 " + ptId1 + " " + ptId2 + " " + ptId3 + " \r\n");
			}
			fw.close();
		} // for roi
	}

	public void genegeneROIBall(djVtkPoint pt, String fileName) throws IOException {
		this.highlightModeData = new djVtkSurData("./fig1/data/sphere_radius7.vtk");
		int highModelPtNum = this.highlightModeData.nPointNum;
		System.out.println("Begin to write file:" + fileName + "...");
		FileWriter fw = null;
		fw = new FileWriter(fileName + ".ball.vtk");
		fw.write("# vtk DataFile Version 3.0\r\n");
		fw.write("vtk output\r\n");
		fw.write("ASCII\r\n");
		fw.write("DATASET POLYDATA\r\n");
		fw.write("POINTS " + highModelPtNum + " float\r\n");
		for (int modelPtIndex = 0; modelPtIndex < highModelPtNum; modelPtIndex++) {
			float x = this.highlightModeData.getPoint(modelPtIndex).x + pt.x;
			float y = this.highlightModeData.getPoint(modelPtIndex).y + pt.y;
			float z = this.highlightModeData.getPoint(modelPtIndex).z + pt.z;
			fw.write(x + " " + y + " " + z + "\r\n");
		}
		fw.write("POLYGONS " + this.highlightModeData.nCellNum + " " + (this.highlightModeData.nCellNum * 4) + " \r\n");
		for (int cellID = 0; cellID < this.highlightModeData.nCellNum; cellID++) {
			int ptId1 = this.highlightModeData.getcell(cellID).pointsList.get(0).pointId;
			int ptId2 = this.highlightModeData.getcell(cellID).pointsList.get(1).pointId;
			int ptId3 = this.highlightModeData.getcell(cellID).pointsList.get(2).pointId;
			fw.write("3 " + ptId1 + " " + ptId2 + " " + ptId3 + " \r\n");
		}
		fw.close();
	}

	public void geneFibers() throws IOException {
		AbstractMatrix roiM = Matrix.fromASCIIFile(new File("./fig2_v2/roiList.txt"));
		int roiNum = roiM.getRowDimension();
		djVtkSurData surData;
		djVtkFiberData fiberData;
		djVtkHybridData hybridData;
		djVtkFiberData newFiber;

		List<String> subDataList = djVtkUtil.loadFileToArrayList("./fig2_v2/DataDescriptor.txt");
		int subNum = subDataList.size();
		for (int subIndex = 25; subIndex < subNum; subIndex++) {
			String[] currentData = subDataList.get(subIndex).split("\\s+");
			System.out.println("Now dealing with subject:" + currentData[0]);
			AbstractMatrix currentPtM = Matrix.fromASCIIFile(new File(currentData[3]));
			int currentColIndex = Integer.valueOf(currentData[4]);
			surData = new djVtkSurData(currentData[1]);
			fiberData = new djVtkFiberData(currentData[2]);
			hybridData = new djVtkHybridData(surData, fiberData);
			hybridData.mapSurfaceToBox();
			hybridData.mapFiberToBox();
			System.out.println("finish mapping...");

			for (int roiIndex = 0; roiIndex < roiNum; roiIndex++) {
				int roiID = (int) roiM.get(roiIndex, 0);
				System.out.println("Now dealing with roi:" + roiID);
				int ptID = (int) currentPtM.get(roiID, currentColIndex);
				Set ptSet = surData.getNeighbourPoints(ptID, 2);
				newFiber = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet).getCompactData();
				newFiber.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
				String fileName = "./fig2_v2/fibers/" + currentData[0] + ".roi" + roiID + "." + ptID;
				this.genegeneROIBall(surData.getPoint(ptID), fileName);
				newFiber.writeToVtkFileCompact("./fig2_v2/fibers/" + currentData[0] + ".roi" + roiID + "." + ptID
						+ ".fiber.vtk");
			} // for all ROIs
			surData = null;
			fiberData = null;
			hybridData = null;
			newFiber = null;
		} // for all subjects
	}

	public void calROIFeatures() {
		djVtkSurData surData;
		djVtkFiberData fiberData;
		djVtkHybridData hybridData;
		djVtkFiberData newFiber;

		List<String> subDataList = djVtkUtil.loadFileToArrayList("./fig2_v2/DataDescriptor.txt");
		int subNum = subDataList.size();
		List<List<Float>> featureDataOfCurrentSub = new ArrayList<List<Float>>();
		for (int subIndex = 30; subIndex < 34; subIndex++) {
			featureDataOfCurrentSub.clear();
			String[] currentData = subDataList.get(subIndex).split("\\s+");
			System.out.println("Now dealing with subject:" + currentData[0]);
			AbstractMatrix currentPtM = Matrix.fromASCIIFile(new File(currentData[3]));
			int currentColIndex = Integer.valueOf(currentData[4]);
			surData = new djVtkSurData(currentData[1]);
			fiberData = new djVtkFiberData(currentData[2]);
			hybridData = new djVtkHybridData(surData, fiberData);
			hybridData.mapSurfaceToBox();
			hybridData.mapFiberToBox();
			System.out.println("finish mapping...");

			for (int roiIndex = 0; roiIndex < 358; roiIndex++) {
				System.out.println("Now dealing with roi:" + roiIndex);
				int ptID = (int) currentPtM.get(roiIndex, currentColIndex);
				// extract fibers
				Set ptSet = surData.getNeighbourPoints(ptID, 2);
				djVtkFiberData tmpFiberData = (djVtkFiberData) hybridData.getFibersConnectToPointsSet(ptSet)
						.getCompactData();
				tmpFiberData.cell_alias = djVtkDataDictionary.VTK_FIELDNAME_FIBER_CELL;
				// generate trace based on fiber file
				djVtkPoint tmpPoint = surData.getPoint(ptID);
				fiberBundleService fiberBundleDescriptor = new fiberBundleService();
				fiberBundleDescriptor.setFiberData(tmpFiberData);
				fiberBundleDescriptor.setSeedPnt(tmpPoint);
				fiberBundleDescriptor.createFibersTrace();
				List<djVtkPoint> allTracePointsList = fiberBundleDescriptor.getAllPoints();
				List<Float> tmpFeature = fiberBundleDescriptor.calFeatureOfTrace(allTracePointsList);
				if (tmpFeature.size() != 144) {
					System.out.println("ERROR:tmpFeature.size is not 144. roiIndex=" + roiIndex);
					System.exit(0);
				}
				tmpFeature.add((float) tmpFiberData.nCellNum);
				tmpFeature.add((float) allTracePointsList.size());
				featureDataOfCurrentSub.add(tmpFeature);
			} // for all ROIs
			djVtkUtil.writeArrayListToFile(featureDataOfCurrentSub, " ", 146, "./fig2_v2/data/" + currentData[0]
					+ "_358_featureList.txt");
			surData = null;
			fiberData = null;
			hybridData = null;
			newFiber = null;
		} // for all subjects
	}

	private double calFeatureDis(List<Double> f1, List<Double> f2) {
		double dis = 0.0;
		if (f1.size() == f2.size()) {
			for (int i = 0; i < f1.size(); i++) {
				dis += Math.abs(f1.get(i) - f2.get(i));
//				dis += Math.pow(Math.abs(f1.get(i) - f2.get(i)), 2);
			}
		} else {
			System.out.println("the size of f1 and f2 are not equal!!");
		}
		return dis;
	}

	private double calGroupDis(List<List<Double>> groupFeatureList) {
		double dis = 0.0;
		int itemNum = groupFeatureList.size();
		for (int i = 0; i < itemNum - 1; i++) {
			for (int j = i + 1; j < itemNum; j++) {
				double tmpDis = this.calFeatureDis(groupFeatureList.get(i), groupFeatureList.get(j));
				dis = dis + tmpDis;
			}
		}
		dis = dis * 2 / (itemNum * (itemNum - 1));
		return dis;
	}
	
	public void calOptiDis()
	{
		List<String> disList = new ArrayList<String>();
		List<Integer> gridMapping = new ArrayList<Integer>();
		Map<Integer, AbstractMatrix> featureDataMap = new HashMap<Integer, AbstractMatrix>();
		Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();
		System.out.println("Now begin to load feature file...");
		featureDataMap.put(0, Matrix.fromASCIIFile(new File("./fig2_v2/data/10_WorkingMem_3_featureList.txt")));
		featureDataMap.put(1, Matrix.fromASCIIFile(new File("./fig2_v2/data/12_WorkingMem_3_featureList.txt")));
		featureDataMap.put(2, Matrix.fromASCIIFile(new File("./fig2_v2/data/14_WorkingMem_3_featureList.txt")));
		featureDataMap.put(3, Matrix.fromASCIIFile(new File("./fig2_v2/data/16_WorkingMem_3_featureList.txt")));
		featureDataMap.put(4, Matrix.fromASCIIFile(new File("./fig2_v2/data/19_WorkingMem_3_featureList.txt")));
		System.out.println("Now begin to load surface data file...");
		surDataMap.put(0, new djVtkSurData("./fig2_v2/data/workingmem/10.sur.asc.to10.vtk"));
		AbstractMatrix finalMat = Matrix.fromASCIIFile(new File("./fig2_v2/data/workingmem/finalizedMat.txt"));
		AbstractMatrix initialMat = Matrix.fromASCIIFile(new File("./fig2_v2/data/GridPointMapping.txt"));
		
		int[] flag = new int [2056];
		for(int i=0;i<2056;i++)
			flag[i]=0;
		
		for (int roiIndex = 0; roiIndex < 358; roiIndex++) {
			int currentPtID = (int)finalMat.get(roiIndex, 0);
			float dis = 1000.0f;
			int initialGridID = -1;
			for(int i=0;i<2056;i++)
			{
				if(flag[i]==0)
				{
					int tmpPtID = (int)initialMat.get(i,0);
					float tmpDis = djVtkUtil.calDistanceOfPoints(surDataMap.get(0).getPoint(currentPtID), surDataMap.get(0).getPoint(tmpPtID));
					if(tmpDis<dis)
					{
						dis = tmpDis;
						initialGridID = i;
					}
				} //if flag
			} //for all 2056
			flag[initialGridID]=1;
			gridMapping.add(initialGridID);
		} //for all ROIs
		///////////////////////////////////////
		for (int i = 0; i < 358; i++) {
			System.out.println("Now dealing with roi: " + i);
			List<List<Double>> groupFeatureList = new ArrayList<List<Double>>();
			for (int j = 0; j < 5; j++) {
				int roiIndex = (int)initialMat.get( gridMapping.get(i) , j);
				List<Double> newFeature = new ArrayList<Double>();
				for (int k = 0; k < 144; k++)
					newFeature.add(featureDataMap.get(j).get(roiIndex, k));
				groupFeatureList.add(newFeature);
			}
			double dis = this.calGroupDis(groupFeatureList);
			disList.add(String.valueOf(dis));

		}
		djVtkUtil.writeArrayListToFile(disList, "./fig2_v2/data/DisBetweenOpti.txt");
		
	}

	public void calRegDis() {
		List<String> disList = new ArrayList<String>();
		Map<Integer, AbstractMatrix> featureDataMap = new HashMap<Integer, AbstractMatrix>();
		Map<Integer, djVtkSurData> surDataMap = new HashMap<Integer, djVtkSurData>();
		System.out.println("Now begin to load feature file...");
		featureDataMap.put(0, Matrix.fromASCIIFile(new File("./fig2_v2/data/10_WorkingMem_3_featureList.txt")));
		featureDataMap.put(1, Matrix.fromASCIIFile(new File("./fig2_v2/data/12_WorkingMem_3_featureList.txt")));
		featureDataMap.put(2, Matrix.fromASCIIFile(new File("./fig2_v2/data/14_WorkingMem_3_featureList.txt")));
		featureDataMap.put(3, Matrix.fromASCIIFile(new File("./fig2_v2/data/16_WorkingMem_3_featureList.txt")));
		featureDataMap.put(4, Matrix.fromASCIIFile(new File("./fig2_v2/data/19_WorkingMem_3_featureList.txt")));
		System.out.println("Now begin to load surface data file...");
		surDataMap.put(0, new djVtkSurData("./fig2_v2/data/workingmem/10.sur.asc.to10.vtk"));
		surDataMap.put(1, new djVtkSurData("./fig2_v2/data/workingmem/12.sur.asc.to10.vtk"));
		surDataMap.put(2, new djVtkSurData("./fig2_v2/data/workingmem/14.sur.asc.to10.vtk"));
		surDataMap.put(3, new djVtkSurData("./fig2_v2/data/workingmem/16.sur.asc.to10.vtk"));
		surDataMap.put(4, new djVtkSurData("./fig2_v2/data/workingmem/19.sur.asc.to10.vtk"));
		AbstractMatrix finalMat = Matrix.fromASCIIFile(new File("./fig2_v2/data/workingmem/finalizedMat.txt"));

		List<String> subDataList = djVtkUtil.loadFileToArrayList("./fig2_v2/DataDescriptor.txt");
		for (int roiIndex = 0; roiIndex < 358; roiIndex++) {
			System.out.println("Now begin to deal with roi:"+roiIndex);
			List<List<Double>> groupFeatureList = new ArrayList<List<Double>>();
			List<Double> modelFeature = new ArrayList<Double>();
			int modelPtId = (int)finalMat.get(roiIndex, 0);
			djVtkPoint modelPtData = surDataMap.get(0).getPoint(modelPtId);
			for (int i = 0; i < 144; i++)
				modelFeature.add(featureDataMap.get(0).get(modelPtId, i));
			groupFeatureList.add(modelFeature);
			//////////////////////////////////////////////////////////////////////
			for (int subIndex = 2; subIndex < 5; subIndex++) {
				int currentRegPtID = surDataMap.get(subIndex).findCloestPt(modelPtData);
				List<Double> newFeature = new ArrayList<Double>();
				for (int i = 0; i < 144; i++)
					newFeature.add(featureDataMap.get(subIndex).get(currentRegPtID, i));
				groupFeatureList.add(newFeature);

			} //for all subject excepte model subject 
			double dis = this.calGroupDis(groupFeatureList);
			disList.add(String.valueOf(dis));
			groupFeatureList.clear();
			groupFeatureList = null;
		} //for all ROIs
		djVtkUtil.writeArrayListToFile(disList, "./fig2_v2/data/DisRegDataF.txt");

		
	}

	public void calGroupTraceMapDis() {
		List<String> subDataList = djVtkUtil.loadFileToArrayList("./fig2_v2/DataDescriptor.txt");
		Map<String, AbstractMatrix> featureDataMap = new HashMap<String, AbstractMatrix>();
		List<String> disList = new ArrayList<String>();

		for (int roiIndex = 0; roiIndex < 358; roiIndex++) {
			System.out.println("Now dealing with roi: " + roiIndex);
			List<List<Double>> groupFeatureList = new ArrayList<List<Double>>();
			for (int subIndex = 20; subIndex < 25; subIndex++) {
				List<Double> newFeature = new ArrayList<Double>();
				String[] currentData = subDataList.get(subIndex).split("\\s+");
				String featureFileName = "./fig2_v2/data/" + currentData[0] + "_358_featureList.txt";
				AbstractMatrix featuresOfCurrentSubM;
				if (featureDataMap.containsKey(featureFileName))
					featuresOfCurrentSubM = featureDataMap.get(featureFileName);
				else {
					featuresOfCurrentSubM = Matrix.fromASCIIFile(new File(featureFileName));
					featureDataMap.put(featureFileName, featuresOfCurrentSubM);
				}

				for (int i = 0; i < 144; i++)
					newFeature.add(featuresOfCurrentSubM.get(roiIndex, i));
				groupFeatureList.add(newFeature);
			} // for all subjects in the group

			double dis = this.calGroupDis(groupFeatureList);
			disList.add(String.valueOf(dis));
			groupFeatureList.clear();
			groupFeatureList = null;
		} // for all ROIs
		djVtkUtil.writeArrayListToFile(disList, "./fig2_v2/data/DisPrePtsd_2.txt");
	}

	public void calRandomDis() {
		Map<Integer, AbstractMatrix> featureDataMap = new HashMap<Integer, AbstractMatrix>();
		featureDataMap.put(0, Matrix.fromASCIIFile(new File("./fig2_v2/data/10_WorkingMem_3_featureList.txt")));
		featureDataMap.put(1, Matrix.fromASCIIFile(new File("./fig2_v2/data/12_WorkingMem_3_featureList.txt")));
		featureDataMap.put(2, Matrix.fromASCIIFile(new File("./fig2_v2/data/14_WorkingMem_3_featureList.txt")));
		featureDataMap.put(3, Matrix.fromASCIIFile(new File("./fig2_v2/data/16_WorkingMem_3_featureList.txt")));
		featureDataMap.put(4, Matrix.fromASCIIFile(new File("./fig2_v2/data/19_WorkingMem_3_featureList.txt")));

		List<String> disList = new ArrayList<String>();
		Random generator = new Random();
		for (int i = 0; i < 358; i++) {
			System.out.println("Now dealing with roi: " + i);
			List<List<Double>> groupFeatureList = new ArrayList<List<Double>>();
			for (int j = 0; j < 5; j++) {
				int roiIndex = generator.nextInt(30000);
				List<Double> newFeature = new ArrayList<Double>();
				for (int k = 0; k < 144; k++)
					newFeature.add(featureDataMap.get(j).get(roiIndex, k));
				groupFeatureList.add(newFeature);
			}
			double dis = this.calGroupDis(groupFeatureList);
			disList.add(String.valueOf(dis));
			groupFeatureList.clear();
			groupFeatureList = null;
		}
		djVtkUtil.writeArrayListToFile(disList, "./fig2_v2/data/DisRandom.txt");

	}
	
	public void calDisPercent()
	{
		AbstractMatrix dataBefore = Matrix.fromASCIIFile(new File("./fig2_v2/data/DisRegDataF.txt"));
		AbstractMatrix dataAfter = Matrix.fromASCIIFile(new File("./fig2_v2/data/DisModel_1.txt"));
		List<String> disList = new ArrayList<String>();
		for(int i=0;i<358;i++)
		{
			double ratio = Math.abs( (dataBefore.get(i, 0)-dataAfter.get(i, 0)) )/dataBefore.get(i, 0);
			disList.add( String.valueOf(ratio) );
		}
		djVtkUtil.writeArrayListToFile(disList, "./fig2_v2/data/DisImproRatio.txt");
		
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		landMarkFig2 main = new landMarkFig2();
		main.drawROIWithDis();

	}

}
