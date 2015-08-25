/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import edu.uga.liulab.djVtkBase.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.tools.data.FileHandler;

/**
 * 
 * @author dajiang
 */
public class forTest {

	private float calFeatureDis(List<Float> fiberFeature1, List<Float> fiberFeature2) {
		if (fiberFeature1.size() == fiberFeature2.size()) {
			float dis = 0.0f;
			for (int i = 0; i < fiberFeature1.size(); i++) {
				if (fiberFeature1.get(i) < fiberFeature2.get(i)) {
					dis = dis + fiberFeature1.get(i);
				} else {
					dis = dis + fiberFeature2.get(i);
				}
			}
			return dis;
		} else {
			System.out.println("ERROR!!!!!!!fiberFeature1.size is not equal to fiberFeature2.size!!");
			return -1.0f;
		}
	}

	public List<Float> getNewFeature() {
		List<Float> featureList = new ArrayList<Float>();
		for (int i = 0; i < 150; i++) {
			float tmpFeature = (float) Math.random();
			featureList.add(tmpFeature);
		}
		return featureList;
	}


	public void prepareDescriptorFileForLandMarkOptimization() {
		try {
			FileWriter fw = null;
			fw = new FileWriter("Ptsd_PredictionDataDescriptor.txt");
			// AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File("./data/subRIndex.txt")); // N*2

			for (int i = 1; i <= 73; i++) {
				// int subID = (int)subRelationM.get(i,1);
				int subID = i;
				fw.write(subID + "_Ptsd" + " ../Data_Ptsd/data/pd00" + subID + ".surf.topcorrect.vtk" + " ../Data_Ptsd/data/pd00" + subID
						+ "ascfibers.vtk" + " ../Data_Ptsd/data/" + subID + "_Ptsd_3_featureList.txt ../Data_Ptsd/data/preVol/" + "pd00"+subID+".b0.mhd\r\n");
			}

			fw.close();
			System.out.println("Write file done!");
			System.out.println("That is all!");
		} catch (IOException ex) {
			Logger.getLogger(djVtkUtil.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public static void writeArrayListToFile(List<List<String>> dataList, int secondDim, String fileName) {
		try {
			System.out.println("begin to write to file:" + fileName);
			System.out.println("Begin to write file:" + fileName + "...");
			FileWriter fw = null;
			fw = new FileWriter(fileName);
			for (int i = 0; i < dataList.size(); i++) {
				for (int j = 0; j < secondDim; j++) {
					fw.write(dataList.get(i).get(j) + " ");
				}
				fw.write("\r\n");

			}
			fw.close();
			System.out.println("Write file done!");
			System.out.println("That is all!");
		} catch (IOException ex) {
			Logger.getLogger(djVtkUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static List<List<String>> loadFileToArrayList(String fileName) {
		List<List<String>> resultList = new ArrayList<List<String>>();
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				// System.out.println(strLine);
				if (strLine.trim().length() != 0) {
					List<String> currentLineList = new ArrayList<String>();
					String[] strArrayOfCurrentLine = strLine.split(",");
					for (int i = 0; i < strArrayOfCurrentLine.length; i++) {
						String strTmp = strArrayOfCurrentLine[i];
						if (strTmp.trim().length() != 0) {
							currentLineList.add(strTmp.trim());
						}
					}
					resultList.add(currentLineList);
				}
			}// while
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		return resultList;
	}

	public void writeROIToVtk() {
		AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File("./data/subRIndex.txt")); // N*2
		AbstractMatrix seedBeforeM = Matrix.fromASCIIFile(new File("./data/seedIndex.txt")); // N*2
		AbstractMatrix seedAfterM = Matrix.fromASCIIFile(new File("./data/seedAfterOptimized.txt")); // N*2
		for (int subIndex = 0; subIndex < 1; subIndex++) {
			int subID = (int) subRelationM.get(subIndex, 1);
			List<djVtkPoint> ptListBefore = new ArrayList<djVtkPoint>();
			List<djVtkPoint> ptListAfter = new ArrayList<djVtkPoint>();
			System.out.println("dealing with sub: " + subIndex);
			djVtkSurData currentSurData = new djVtkSurData("./data/" + subID + ".surf.vtk");
			for (int roiID = 0; roiID < 16; roiID++) {
				ptListBefore.add(currentSurData.getPoint((int) seedBeforeM.get(subIndex, roiID)));
				ptListAfter.add(currentSurData.getPoint((int) seedAfterM.get(subIndex, roiID)));
			}
			djVtkUtil.writeToPointsVtkFile("./roiPointVtk/sub." + subID + "_Before.vtk", ptListBefore);
			djVtkUtil.writeToPointsVtkFile("./roiPointVtk/sub." + subID + "_After.vtk", ptListAfter);
		}
	}

	public void getFibersOfSubcortical() {
		AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File("./data/subRIndex.txt")); // N*2
		File dirSur = new File("./subCorticalValidation/subcorticalSur/");
		String[] strSurFileList = dirSur.list();

		for (int roiID = 0; roiID < 16; roiID++) {
			File dirFiber = new File("./subCorticalValidation/roi" + roiID + "/after/");
			String[] strFiberFileList = dirFiber.list();
			for (int subIndex = 0; subIndex < 15; subIndex++) {
				int subID = (int) subRelationM.get(subIndex, 1);
				System.out.println("dealing with sub: " + subID);
				djVtkFiberData fiberBefore = new djVtkFiberData("./subCorticalValidation/roi" + roiID + "/before/12EndRoi" + roiID + "Sub" + subIndex
						+ ".vtk");
				djVtkFiberData fiberAfter = null;
				for (int i = 0; i < strFiberFileList.length; i++) {
					String currentFiberFileName = strFiberFileList[i];
					if (currentFiberFileName.startsWith("sub" + subIndex) && currentFiberFileName.endsWith("fibers.vtk")) {
						System.out.println("reading " + currentFiberFileName);
						fiberAfter = new djVtkFiberData("./subCorticalValidation/roi" + roiID + "/after/" + currentFiberFileName);
					}
				} // for all files
				djVtkSurData scSurData;
				for (int i = 0; i < strSurFileList.length; i++) {
					String currentSurFileName = strSurFileList[i];
					if (currentSurFileName.startsWith(subID + "-R")) {
						System.out.println("reading " + currentSurFileName);
						scSurData = new djVtkSurData("./subCorticalValidation/subcorticalSur/" + currentSurFileName);
						fiberBefore.cellsOutput.clear();
						fiberAfter.cellsOutput.clear();

						djVtkHybridData hybridData = new djVtkHybridData(scSurData, fiberBefore);
						hybridData.mapSurfaceToBox();
						hybridData.mapFiberToBox();
						hybridData.getFibersConnectToSurface().writeToVtkFileCompact(
								"./subCorticalValidation/roi" + roiID + "/roi" + roiID + "_" + currentSurFileName.split("\\.")[0]
										+ "_fibersBefore.vtk");

						hybridData = new djVtkHybridData(scSurData, fiberAfter);
						hybridData.mapSurfaceToBox();
						hybridData.mapFiberToBox();
						hybridData.getFibersConnectToSurface().writeToVtkFileCompact(
								"./subCorticalValidation/roi" + roiID + "/roi" + roiID + "_" + currentSurFileName.split("\\.")[0]
										+ "_fibersAfter.vtk");
					}
				} // for all files

			} // for all subjects
		} // for all rois

	}

	public void translateVtk() throws IOException {
		AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File("./data/subRIndex.txt")); // N*2
		File dir = new File("./subCorticalData/");
		String[] strFileList = dir.list();
		for (int i = 0; i < strFileList.length; i++) {
			String currentFileName = strFileList[i];
			System.out.println("dealing with " + currentFileName);
			int subID = Integer.valueOf(currentFileName.split("-")[0]);
			String niftiName = "./data/" + subID + ".3d.brain";
			djNiftiData niftiData = new djNiftiData(niftiName);
			System.out.println("reading " + niftiName + "...");
			// FileInputStream fstream = new FileInputStream(mhdFileName);
			// DataInputStream in = new DataInputStream(fstream);
			// BufferedReader br = new BufferedReader(new InputStreamReader(in));
			// String strLine;
			// String[] tmpStringArray;
			// float[] offset = new float[3];
			// while ((strLine = br.readLine()) != null) {
			// if (strLine.startsWith("Offset")) {
			// tmpStringArray = strLine.split(" ");
			// offset[0] = Float.valueOf(tmpStringArray[tmpStringArray.length - 3]);
			// offset[1] = Float.valueOf(tmpStringArray[tmpStringArray.length - 2]);
			// offset[2] = Float.valueOf(tmpStringArray[tmpStringArray.length - 1]);
			// }
			// }
			// br.close();
			// in.close();
			// fstream.close();
			float[] offset = niftiData.rawNiftiData.getImagePositionPatient();
			djVtkSurData currentSurData = new djVtkSurData("./subCorticalData/" + currentFileName);
			for (int ptID = 0; ptID < currentSurData.nPointNum; ptID++) {
				currentSurData.getPoint(ptID).x = currentSurData.getPoint(ptID).x + offset[0];
				currentSurData.getPoint(ptID).y = currentSurData.getPoint(ptID).y + offset[1];
				currentSurData.getPoint(ptID).z = currentSurData.getPoint(ptID).z + offset[2];
			}
			currentSurData.cellsOutput.addAll(currentSurData.cells);
			currentSurData.writeToVtkFileCompact("./subCorticalData/" + currentFileName + "T.vtk");
		}

	}

	public void testDecimateSurData() {
		djVtkSurData surData = new djVtkSurData("19.surf.vtk");
		int seedID = 24641;
		Set<djVtkPoint> newPatchPoints = surData.decimatePatch(seedID, 2, 4);
		List<djVtkPoint> ptsList = new ArrayList<djVtkPoint>();
		ptsList.addAll(newPatchPoints);
		djVtkUtil.writeToPointsVtkFile("decimate_19_" + seedID + ".vtk", ptsList);

	}
	
	public void testDis()
	{
		djVtkSurData surData = new djVtkSurData("./data/10.surf.vtk");
		int pt1 = 32069;
		int pt2 = 32068;
		float dis = djVtkUtil.calDistanceOfPoints(surData.getPoint(pt1), surData.getPoint(pt2));
		System.out.println("dis is : "+dis);
		
	}

	public static void main(String[] args) throws Exception {

		// AbstractMatrix featuresOfCurrentSubM = Matrix.fromASCIIFile(new File("./10_ConstrainList.txt"));
		// int i = featuresOfCurrentSubM.getColumnDimension();
		// int j = featuresOfCurrentSubM.getRowDimension();
		// int ss=0;

		// String fileName = "fmri_pro_1a" ;
		// djNiftiData nifData = new djNiftiData(fileName,"rw");
		//
//		forTest mainFlow = new forTest();
//		mainFlow.testDis();
		djVtkSurData surData = new djVtkSurData("fig1_v2/waveSurface/surf16to10.wavelet.asc.5.vtk");
		surData.calSurDataBox();
		int iii=0;
		iii=1;
		// mainFlow.getFibersOfSubcortical();
		// mainFlow.translateVtk();
		// mainFlow.testDecimateSurData();

		// List<List<String>> resultList = forTest.loadFileToArrayList("iris.data");
		// // djVtkUtil.writeArrayListToFile(resultList, 1, "iris.test.data");
		// // forTest.writeArrayListToFile(resultList, 4, "iris.pure.data");
		// // String s = resultList.get(2).get(1);
		//
		// /* Load a dataset */
		// Dataset data = FileHandler.loadDataset(new File("iris.pure.data"),"   ");
		// /*
		// * Create a new instance of the KMeans algorithm, with no options
		// * specified. By default this will generate 4 clusters.
		// */
		// Clusterer km = new KMeans(10,500);
		// /*
		// * Cluster the data, it will be returned as an array of data sets, with
		// * each dataset representing a cluster
		// */
		// Dataset[] clusters = km.cluster(data);
		// System.out.println("Cluster count: " + clusters.length);
		//
		//
		// int a = 0;

		// *****************************************************************
		// djVtkSurData surData = new djVtkSurData("./data/19.surf.vtk");
		// djVtkFiberData fiberData = new djVtkFiberData("./data/19.asc.fiber.vtk");
		// djVtkHybridData hybridData = new djVtkHybridData(surData, fiberData);
		// hybridData.mapSurfaceToBox();
		// hybridData.mapFiberToBox();
		// for (int i = 0; i < surData.nPointNum; i++) {
		// if (i % 100 == 0) {
		// System.out.println("finished i:" + i);
		// }
		// Set ptSet = surData.getNeighbourPoints(i, 3);
		// hybridData.getFibersConnectToPointsSet(ptSet).writeToVtkFileCompact("./19/" + i + "_19_fiber.vtk");
		// }

		//
		// Set ptset = surData.getNeighbourPoints(33389, 3);
		// int ptNum = ptset.size();
		// System.out.println("there are " + ptNum + " points..");

		// *****************************************************************
		// float f = 0.0f;
		// List<Float> f1 = mainFlow.getNewFeature();
		// List<Float> f2 = mainFlow.getNewFeature();
		// for (int i = 0; i < 1000000; i++) {
		// for (int j = 0; j < 1000000; j++) {
		// if (j % 100 == 0) {
		// System.out.println("j=" + j);
		// }
		// for (int k = 0; k < 1000000; k++) {
		// float tmp = mainFlow.calFeatureDis(f1, f2);
		// if (f < 10000.0) {
		// f = f + tmp;
		// }
		// }
		// }
		// }

		// ****************************************************************************
		// String strDir = "./testTrace" ;
		// File dirOfTraceFiles = new File( strDir );
		// String[] fileListOfCurrentDir = dirOfTraceFiles.list();
		// for(int i=0;i<fileListOfCurrentDir.length;i++)
		// {
		// String currentFileName = strDir+"/" + fileListOfCurrentDir[i];
		// djVtkSurData currentTraceData = new djVtkSurData( currentFileName );
		// fiberBundleService fiberBundle = new fiberBundleService();
		// List<Float> feature = fiberBundle.calFeatureOfTrace(currentTraceData.points);
		// System.out.println("------------------"+currentFileName+"--------------------------");
		// for(int j=0;j<feature.size();j++)
		// {
		// System.out.println(feature.get(j));
		// }
		// }
	}
}
