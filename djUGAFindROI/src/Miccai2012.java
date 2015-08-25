

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import com.ojn.gexf4j.core.Gexf;
import com.ojn.gexf4j.core.GexfWriter;
import com.ojn.gexf4j.core.Node;
import com.ojn.gexf4j.core.impl.GexfImpl;
import com.ojn.gexf4j.core.impl.StaxGraphWriter;
import com.ojn.gexf4j.core.impl.viz.ColorImpl;
import com.ojn.gexf4j.core.impl.viz.PositionImpl;

import edu.uga.liulab.djVtkBase.djVtkUtil;

public class Miccai2012 {

	private List<Integer> idList;

	private void initialDMN() {
		idList = new ArrayList<Integer>();
//		idList.add(45);
//		idList.add(72);
//		idList.add(76);
//		idList.add(79);
//		idList.add(144);
//		idList.add(155);
//		idList.add(298);
//		idList.add(326);
		for(int i=0;i<358;i++)
			idList.add(i);
	}

	public void writeArrayToFile(double[][] dataArray, int row, int column, String fileName) {
		List<String> dataList = new ArrayList<String>();
		for (int i = 0; i < row; i++) {
			String line = "";
			for (int j = 0; j < column; j++)
				line += dataArray[i][j] + " ";
			dataList.add(line);
		}
		djVtkUtil.writeArrayListToFile(dataList, fileName);
	}

	public void calculateStructuralMatrix() {
		int roiNum=358;
		double[][] connNormal = new double[roiNum][roiNum];
		double[][] connPatient = new double[roiNum][roiNum];
		double[][] connAll = new double[roiNum][roiNum];

		String folderDir;
		File dir;
		String[] files;
		
		int subNum=0;

		folderDir = "./connectome/ptsd/adult_normal/";
		dir = new File(folderDir);
		files = dir.list();
		for (int f = 0; f < files.length; f++) {
			if (files[f].startsWith("strucC")) {
				subNum++;
				AbstractMatrix tmpMatrix = Matrix.fromASCIIFile(new File(folderDir + files[f]));
				System.out.println(files[f]);
				for (int i = 0; i < idList.size(); i++) {
					for (int j = 0; j < idList.size(); j++)
						connNormal[i][j] = connNormal[i][j] + tmpMatrix.get(idList.get(i), idList.get(j));
				} // for i
			} // if
		} // for f
		
		for (int i = 0; i < idList.size(); i++)
			for (int j = 0; j < idList.size(); j++)
				connNormal[i][j]=connNormal[i][j]/subNum;
		this.writeArrayToFile(connNormal, roiNum, roiNum, "./2012MICCAI/fiberNumt_"+roiNum+"_ptsd_adult_normals.txt");
		findModelUtil.writeVtkMatrix(connNormal, roiNum, roiNum, "./2012MICCAI/fiberNum_"+roiNum+"_ptsd_adult_normals.vtk");
		
		for (int i = 0; i < idList.size(); i++) {
			double sum = 0.0;
			for (int j = 0; j < idList.size(); j++) {
				connAll[i][j] = connNormal[i][j];
				sum += connNormal[i][j];
			}
			for (int j = 0; j < idList.size(); j++)
				if (sum > 1)
					connNormal[i][j] /= sum;
				else
					connNormal[i][j] = 1.0;
		}
		this.writeArrayToFile(connNormal, roiNum, roiNum, "./2012MICCAI/weight_"+roiNum+"_ptsd_adult_normals.txt");
		findModelUtil.writeVtkMatrix(connNormal, roiNum, roiNum, "./2012MICCAI/weight_"+roiNum+"_ptsd_adult_normals.vtk");
		for (int i = 0; i < idList.size(); i++)
			for (int j = 0; j < idList.size(); j++)
				connNormal[i][j] = 1 - connNormal[i][j] / 2.0;
		this.writeArrayToFile(connNormal, roiNum, roiNum, "./2012MICCAI/normal_w_"+roiNum+".txt");
		findModelUtil.writeVtkMatrix(connNormal, roiNum, roiNum, "./2012MICCAI/normal_w_"+roiNum+".vtk");

//		folderDir = "./connectome/ptsd/adult_patient/";
//		dir = new File(folderDir);
//		files = dir.list();
//		for (int f = 0; f < files.length; f++) {
//			if (files[f].startsWith("strucC")) {
//				AbstractMatrix tmpMatrix = Matrix.fromASCIIFile(new File(folderDir + files[f]));
//				System.out.println(files[f]);
//				for (int i = 0; i < idList.size(); i++) {
//					for (int j = 0; j < idList.size(); j++)
//						connPatient[i][j] = connPatient[i][j] + tmpMatrix.get(idList.get(i), idList.get(j));
//				} // for i
//			} // if
//		} // for f
//		for (int i = 0; i < idList.size(); i++) {
//			double sum = 0.0;
//			for (int j = 0; j < idList.size(); j++) {
//				connAll[i][j] += connPatient[i][j];
//				sum += connPatient[i][j];
//			}
//			for (int j = 0; j < idList.size(); j++)
//				if (sum > 1)
//					connPatient[i][j] /= sum;
//				else
//					connPatient[i][j] = 1.0;
//		}
//		this.writeArrayToFile(connAll, 8, 8, "./2012MICCAI/All_StructuralConn.txt");
//		findModelUtil.writeVtkMatrix(connAll, 8, 8, "./2012MICCAI/All_StructuralConn.vtk");
//		this.writeArrayToFile(connPatient, 8, 8, "./2012MICCAI/weight_dmn_ptsd_adult_patients.txt");
//		findModelUtil.writeVtkMatrix(connPatient, 8, 8, "./2012MICCAI/weight_dmn_ptsd_adult_patients.vtk");
//		for (int i = 0; i < idList.size(); i++)
//			for (int j = 0; j < idList.size(); j++)
//				connPatient[i][j] = 1 - connPatient[i][j] / 2.0;
//		this.writeArrayToFile(connPatient, 8, 8, "./2012MICCAI/patient_w.txt");
//		findModelUtil.writeVtkMatrix(connPatient, 8, 8, "./2012MICCAI/patient_w.vtk");
//
//		for (int i = 0; i < idList.size(); i++) {
//			double sum = 0.0;
//			for (int j = 0; j < idList.size(); j++)
//				sum += connAll[i][j];
//			for (int j = 0; j < idList.size(); j++)
//				if (sum > 1)
//					connAll[i][j] /= sum;
//				else
//					connAll[i][j] = 1.0;
//		}
//		this.writeArrayToFile(connAll, 8, 8, "./2012MICCAI/weight_dmn_ptsd_adult_all.txt");
//		findModelUtil.writeVtkMatrix(connAll, 8, 8, "./2012MICCAI/weight_dmn_ptsd_adult_all.vtk");
//		for (int i = 0; i < idList.size(); i++)
//			for (int j = 0; j < idList.size(); j++)
//				connAll[i][j] = 1 - connAll[i][j] / 2.0;
//		this.writeArrayToFile(connAll, 8, 8, "./2012MICCAI/all_w.txt");
//		findModelUtil.writeVtkMatrix(connAll, 8, 8, "./2012MICCAI/all_w.vtk");
	}

	public void extractBoldSig() {
		String folderDir;
		File dir;
		String[] files;

		int nSigSampleNum = 150;
		double[][] sigNormal = new double[nSigSampleNum][8];
		double[][] sigPatient = new double[nSigSampleNum][8];

		folderDir = "./connectome/mci/connectivity/normal/";
		dir = new File(folderDir);
		files = dir.list();
		for (int f = 0; f < files.length; f++) {
			if (files[f].endsWith("singal.txt")) {
				AbstractMatrix tmpMatrix = Matrix.fromASCIIFile(new File(folderDir + files[f]));
				System.out.println(files[f]);
				System.out.println("row:" + tmpMatrix.getRowDimension());
				System.out.println("column:" + tmpMatrix.getColumnDimension());

				if (tmpMatrix.getRowDimension() != 358 || tmpMatrix.getColumnDimension() != nSigSampleNum) {
					System.out.println("ERROR with Bold Signal dim!!");
					System.exit(0);
				} else
					for (int i = 0; i < idList.size(); i++) {
						for (int j = 0; j < nSigSampleNum; j++)
							sigNormal[j][i] = tmpMatrix.get(idList.get(i), j);
					} // for i
				this.writeArrayToFile(sigNormal, nSigSampleNum, 8, "./2012MICCAI/mci/normalSig/n_dmn_" + files[f]);
			} // if
		} // for f

		folderDir = "./connectome/mci/connectivity/patient/";
		dir = new File(folderDir);
		files = dir.list();
		for (int f = 0; f < files.length; f++) {
			if (files[f].endsWith("singal.txt")) {
				AbstractMatrix tmpMatrix = Matrix.fromASCIIFile(new File(folderDir + files[f]));
				System.out.println(files[f]);
				System.out.println("row:" + tmpMatrix.getRowDimension());
				System.out.println("column:" + tmpMatrix.getColumnDimension());

				if (tmpMatrix.getRowDimension() != 358 || tmpMatrix.getColumnDimension() != nSigSampleNum) {
					System.out.println("ERROR with Bold Signal dim!!");
					System.exit(0);
				} else
					for (int i = 0; i < idList.size(); i++) {
						for (int j = 0; j < nSigSampleNum; j++)
							sigPatient[j][i] = tmpMatrix.get(idList.get(i), j);
					} // for i
				this.writeArrayToFile(sigPatient, nSigSampleNum, 8, "./2012MICCAI/mci/patientSig/p_dmn_" + files[f]);
			} // if
		} // for f
	}

	public void prepareDataForSVM() {
		List<String> dataList = djVtkUtil.loadFileToArrayList("./2012MICCAI/testInstance_3_3.txt");
		List<String> labelList = djVtkUtil.loadFileToArrayList("./2012MICCAI/testLabel_3_3.txt");
		List<String> outPutList = new ArrayList<String>();
		for (int i = 0; i < dataList.size(); i++) {
			String tmpString = "";
			String[] currentLine = dataList.get(i).split(",");
			tmpString = tmpString + labelList.get(i) + " ";
			for (int j = 0; j < currentLine.length; j++)
				tmpString = tmpString + j + ":" + currentLine[j] + " ";
			outPutList.add(tmpString);
		}
		djVtkUtil.writeArrayListToFile(outPutList, "./2012MICCAI/SVM_testInstance_3_3.txt");
	}

	private List<Integer> geneRandom(int numOfNeed, int maxValue) {
		Random rng = new Random(); // Ideally just create one instance globally
		List<Integer> generated = new ArrayList<Integer>();
		for (int i = 0; i < numOfNeed; i++) {
			while (true) {
				Integer next = rng.nextInt(maxValue) + 1;
				if (!generated.contains(next)) {
					// Done for this iteration
					generated.add(next);
					// System.out.println(next);
					break;
				} // if
			} // while
		} // for
		return generated;
	}

	public void calEntropy() {
		List<String> dataList = djVtkUtil.loadFileToArrayList("./2012MICCAI/allCoeff_7_7.txt");
		List<String> outputList_EdgeNum = new ArrayList<String>();
		List<String> outputList_Entropy = new ArrayList<String>();
		for (int i = 0; i < dataList.size(); i++) {
			String[] currentLine = dataList.get(i).split(",");
			String currentOutPut_EdgeNum = "";
			String currentOutPut_Entropy = "";
			for (int m = 0; m < 8; m++) {
				int currentEdgeNum = 0;
				double currentEntropy = 0.0;
				for (int n = m * 7 + 0; n < m * 7 + 7; n++) {
					double pi = Math.abs(Double.valueOf(currentLine[n]));
					if (pi != 0.0) {
						currentEdgeNum++;
						currentEntropy += Math.abs(pi * Math.log(pi));
					} // if
				} // for n
				currentOutPut_EdgeNum = currentOutPut_EdgeNum + currentEdgeNum + " ";
				currentOutPut_Entropy = currentOutPut_Entropy + currentEntropy + " ";
			} // for m
			outputList_EdgeNum.add(currentOutPut_EdgeNum);
			outputList_Entropy.add(currentOutPut_Entropy);
		} // for i
		djVtkUtil.writeArrayListToFile(outputList_EdgeNum, "./2012MICCAI/outputList_EdgeNum.txt");
		djVtkUtil.writeArrayListToFile(outputList_Entropy, "./2012MICCAI/outputList_Entropy.txt");

	}

	public void generateFilesForSVM() {
		int reproduceNum = 28;
		List<String> dataList = djVtkUtil.loadFileToArrayList("./2012MICCAI/allCoeff_7_3.txt");
		List<String> labelList = djVtkUtil.loadFileToArrayList("./2012MICCAI/allLabel.txt");

		List<String> dataListC = djVtkUtil.loadFileToArrayList("./2012MICCAI/all_correE.txt");

		for (int r = 0; r < reproduceNum; r++) {
			// ////////////Original two fold////////////////////////////
			// List<Integer> normalIDs = this.geneRandom(9, 18);
			// List<Integer> patientIDs = this.geneRandom(5, 10);
			//
			// ////////////////////
			List<String> outPutTrainList = new ArrayList<String>();
			List<String> outPutTestList = new ArrayList<String>();
			List<String> outPutTrainListC = new ArrayList<String>();
			List<String> outPutTestListC = new ArrayList<String>();
			//
			// // Deal with training cases
			// for (int i = 0; i < normalIDs.size(); i++) {
			// System.out.println("train-normal:"+normalIDs.get(i));
			// String tmpString = "";
			// String tmpStringC = "";
			// String[] currentLine = dataList.get(normalIDs.get(i) -
			// 1).split(",");
			// String[] currentLineC = dataListC.get(normalIDs.get(i) -
			// 1).split(",");
			// tmpString = tmpString + labelList.get(normalIDs.get(i) - 1) +
			// " ";
			// tmpStringC = tmpStringC + labelList.get(normalIDs.get(i) - 1) +
			// " ";
			// for (int j = 0; j < currentLine.length; j++)
			// {
			// tmpString = tmpString + j + ":" + currentLine[j] + " ";
			// tmpStringC = tmpStringC + j + ":" + currentLineC[j] + " ";
			// }
			// outPutTrainList.add(tmpString);
			// outPutTrainListC.add(tmpStringC);
			// }
			// for (int i = 0; i < patientIDs.size(); i++) {
			// System.out.println("train-patient:"+patientIDs.get(i));
			// String tmpString = "";
			// String tmpStringC = "";
			// String[] currentLine = dataList.get(17 +
			// patientIDs.get(i)).split(",");
			// String[] currentLineC = dataListC.get(17 +
			// patientIDs.get(i)).split(",");
			// tmpString = tmpString + labelList.get(17 + patientIDs.get(i)) +
			// " ";
			// tmpStringC = tmpStringC + labelList.get(17 + patientIDs.get(i)) +
			// " ";
			// for (int j = 0; j < currentLine.length; j++)
			// {
			// tmpString = tmpString + j + ":" + currentLine[j] + " ";
			// tmpStringC = tmpStringC + j + ":" + currentLineC[j] + " ";
			// }
			// outPutTrainList.add(tmpString);
			// outPutTrainListC.add(tmpStringC);
			// }
			//
			// // Deal with testing cases
			// for (int i = 0; i < 18; i++) {
			// if (!normalIDs.contains(i + 1)) {
			// System.out.println("test-normal:"+(i+1));
			// String tmpString = "";
			// String tmpStringC = "";
			// String[] currentLine = dataList.get(i).split(",");
			// String[] currentLineC = dataListC.get(i).split(",");
			// tmpString = tmpString + labelList.get(i) + " ";
			// tmpStringC = tmpStringC + labelList.get(i) + " ";
			// for (int j = 0; j < currentLine.length; j++)
			// {
			// tmpString = tmpString + j + ":" + currentLine[j] + " ";
			// tmpStringC = tmpStringC + j + ":" + currentLineC[j] + " ";
			// }
			// outPutTestList.add(tmpString);
			// outPutTestListC.add(tmpStringC);
			// }
			//
			// }
			// for (int i = 0; i < 10; i++) {
			// if (!patientIDs.contains(i + 1)) {
			// System.out.println("test-patient:"+(i+1));
			// String tmpString = "";
			// String tmpStringC = "";
			// String[] currentLine = dataList.get(18+i).split(",");
			// String[] currentLineC = dataListC.get(18+i).split(",");
			// tmpString = tmpString + labelList.get(18+i) + " ";
			// tmpStringC = tmpStringC + labelList.get(18+i) + " ";
			// for (int j = 0; j < currentLine.length; j++)
			// {
			// tmpString = tmpString + j + ":" + currentLine[j] + " ";
			// tmpStringC = tmpStringC + j + ":" + currentLineC[j] + " ";
			// }
			// outPutTestList.add(tmpString);
			// outPutTestListC.add(tmpStringC);
			// }
			// }
			// //////////////////////////////////////////////////////////////
			// /////////Leave one out///////////////////////////

			System.out.println("test-data:" + (r + 1));
			String tmpString = "";
			String tmpStringC = "";
			String[] currentLine = dataList.get(r).split(",");
			String[] currentLineC = dataListC.get(r).split(",");
			tmpString = tmpString + labelList.get(r) + " ";
			tmpStringC = tmpStringC + labelList.get(r) + " ";
			for (int j = 0; j < currentLine.length; j++)
				tmpString = tmpString + j + ":" + currentLine[j] + " ";
			for (int j = 0; j < currentLineC.length; j++)
				tmpStringC = tmpStringC + j + ":" + currentLineC[j] + " ";

			outPutTestList.add(tmpString);
			outPutTestListC.add(tmpStringC);

			for (int i = 0; i < reproduceNum; i++) {
				if (r != i) {
					System.out.println("train-data:" + (i + 1));
					tmpString = "";
					tmpStringC = "";
					currentLine = dataList.get(i).split(",");
					currentLineC = dataListC.get(i).split(",");
					tmpString = tmpString + labelList.get(i) + " ";
					tmpStringC = tmpStringC + labelList.get(i) + " ";
					for (int j = 0; j < currentLine.length; j++)
						tmpString = tmpString + j + ":" + currentLine[j] + " ";
					for (int j = 0; j < currentLineC.length; j++)
						tmpStringC = tmpStringC + j + ":" + currentLineC[j] + " ";
					outPutTrainList.add(tmpString);
					outPutTrainListC.add(tmpStringC);
				} // if
			} // for i

			// //////////////////////////////////////////////////////////
			djVtkUtil.writeArrayListToFile(outPutTrainList, "./2012MICCAI/leaveoneout_data/" + r
					+ "_SVM_trainInstance.txt");
			djVtkUtil.writeArrayListToFile(outPutTestList, "./2012MICCAI/leaveoneout_data/" + r
					+ "_SVM_testInstance.txt");
			djVtkUtil.writeArrayListToFile(outPutTrainListC, "./2012MICCAI/leaveoneout_data/" + r
					+ "_SVM_trainInstanceC.txt");
			djVtkUtil.writeArrayListToFile(outPutTestListC, "./2012MICCAI/leaveoneout_data/" + r
					+ "_SVM_testInstanceC.txt");
		} // for r

	}

	public void drawRegression() throws IOException {
		String fileName = "patientAveM";
		List<String> dataList = djVtkUtil.loadFileToArrayList("./2012MICCAI/fig/regression_fig/" + fileName + ".txt");
		double[][] currentCoffM = new double[7][8];
		if (dataList.size() == 7)
			for (int i = 0; i < dataList.size(); i++) {
				String[] currentLine = dataList.get(i).split(",");
				if (currentLine.length == 8)
					for (int j = 0; j < currentLine.length; j++)
						currentCoffM[i][j] = Math.abs(Double.valueOf(currentLine[j]));
				else {
					System.out.println("Data matrix is not 7*8 !!");
					System.exit(0);
				} // else
			} // for i
		else {
			System.out.println("Data matrix is not 7*8 !!");
			System.exit(0);
		} // else
		this.generateGephiFile(currentCoffM, fileName);
	}

	private void generateGephiFile(double[][] conn, String filePrex) throws IOException {
		String folderName = "./2012MICCAI/fig/regression_fig/";
		List<String> NodesLabes = new ArrayList<String>();
		String strPrefix = "DMN-";
		NodesLabes.add(strPrefix + "1"); // 45
		NodesLabes.add(strPrefix + "2"); // 72
		NodesLabes.add(strPrefix + "3"); // 76
		NodesLabes.add(strPrefix + "4"); // 79
		NodesLabes.add(strPrefix + "5"); // 144
		NodesLabes.add(strPrefix + "6"); // 155
		NodesLabes.add(strPrefix + "7"); // 298
		NodesLabes.add(strPrefix + "8"); // 326
		int[][] colormap = new int[8][3];
		colormap[0][0] = 0;
		colormap[0][1] = 255;
		colormap[0][2] = 0;
		colormap[1][0] = 255;
		colormap[1][1] = 255;
		colormap[1][2] = 0;
		colormap[2][0] = 170;
		colormap[2][1] = 85;
		colormap[2][2] = 0;
		colormap[3][0] = 104;
		colormap[3][1] = 34;
		colormap[3][2] = 139;
		colormap[4][0] = 255;
		colormap[4][1] = 0;
		colormap[4][2] = 255;
		colormap[5][0] = 0;
		colormap[5][1] = 0;
		colormap[5][2] = 255;
		colormap[6][0] = 255;
		colormap[6][1] = 0;
		colormap[6][2] = 0;
		colormap[7][0] = 0;
		colormap[7][1] = 255;
		colormap[7][2] = 255;

		for (int d = 0; d < 8; d++) {
			List<Node> nodeList = new ArrayList<Node>();
			double r = 220.0;
			Gexf gexf = new GexfImpl();
			gexf.setVisualization(true);
			// Generate the center node
			Node tmpNode = gexf.getGraph().createNode(NodesLabes.get(d));
			tmpNode.setPosition(new PositionImpl(0.0f, 0.0f, 0.0f));
			tmpNode.setLabel(NodesLabes.get(d));
			tmpNode.setColor(new ColorImpl(colormap[d][0], colormap[d][1], colormap[d][2]));
			nodeList.add(tmpNode);
			// Generate the perpheral nodes
			int count = 0;
			for (int i = 0; i < NodesLabes.size(); i++) {
				if (i != d) {
					tmpNode = gexf.getGraph().createNode(NodesLabes.get(i));
					tmpNode.setPosition(new PositionImpl((float) (r * Math.cos(Math.PI / 7.0 + count * Math.PI / 3.5)),
							(float) (r * Math.sin(Math.PI / 7.0 + count * Math.PI / 3.5)), 0.0f));
					tmpNode.setLabel(NodesLabes.get(i));
					tmpNode.setColor(new ColorImpl(colormap[i][0], colormap[i][1], colormap[i][2]));
					nodeList.add(tmpNode);
					count++;
				} // if
			} // for i
				// Construct the edges
			for (int i = 0; i < 7; i++)
				nodeList.get(i + 1).connectTo(nodeList.get(0)).setColor(new ColorImpl(255, 0, 0))
						.setWeight((float) conn[i][d]);
			// Write file
			GexfWriter gw = new StaxGraphWriter();
			File fileSave = new File(folderName + filePrex + "_" + d + "_" + NodesLabes.get(d) + ".gexf");
			FileOutputStream fos = new FileOutputStream(fileSave);
			gw.writeToStream(gexf, fos);
			System.out.println("Write file done!");
		} // for d
	}

	public void connectome_prepareForWeka() {
		List<String> dataList = djVtkUtil
				.loadFileToArrayList("./2012MICCAI/connectome/clusterInput_filtered_sz_1009.txt");
		String[] currentLine;
		List<String> labelList = new ArrayList<String>();
		List<String> featurelList = new ArrayList<String>();

		currentLine = dataList.get(0).split("\\s+");
		for (int l = 1; l < currentLine.length; l++)
			labelList.add(currentLine[l]);
		String[][] featureData = new String[labelList.size()][dataList.size()];

		for (int i = 1; i < dataList.size(); i++) {
			currentLine = dataList.get(i).split("\\s+");
			featurelList.add(currentLine[0]);
			for (int j = 1; j < currentLine.length; j++)
				featureData[j - 1][i - 1] = currentLine[j];
		} // for i

		for (int i = 0; i < labelList.size(); i++)
			featureData[i][dataList.size() - 1] = labelList.get(i);

		// /////////////////////////////////////////////////////////
		// out put arff file
		List<String> outList = new ArrayList<String>();
		outList.add("@relation mci");
		for (int i = 0; i < featurelList.size(); i++)
			outList.add("@attribute " + featurelList.get(i) + " numeric");
		outList.add("@ATTRIBUTE class        {SZ,SZ-Ctrl}");
		outList.add("@DATA");
		for (int i = 0; i < labelList.size(); i++) {
			String tmpLine = "";
			for (int j = 0; j < dataList.size(); j++)
				tmpLine += featureData[i][j] + " ";
			outList.add(tmpLine);
		}
		djVtkUtil.writeArrayListToFile(outList, "./2012MICCAI/connectome/sz.arff");
	}

	public void connectome_geneSelectedFeatureFile() {
		List<String> dataList = djVtkUtil
				.loadFileToArrayList("./2012MICCAI/connectome/clusterInput_filtered_sz_1009.txt");
		List<String> featureList = djVtkUtil.loadFileToArrayList("./2012MICCAI/connectome/sz_featureList.txt");
		String[] features = featureList.get(0).split(",");
		List<String> outList = new ArrayList<String>();
		for (int i = 0; i < features.length; i++) {
			int featureIndex = Integer.valueOf(features[i]);
			outList.add(dataList.get(featureIndex));
		}
		djVtkUtil.writeArrayListToFile(outList, "./2012MICCAI/connectome/szSelectedFeature.txt");
	}

	public void connectome_geneFilesForSVM() {
		List<String> dataList = djVtkUtil.loadFileToArrayList("./2012MICCAI/connectome/clusterInput_filtered_sz_1009.txt");
		int flag = 8;

		String[] currentLine;
		currentLine = dataList.get(0).split("\\s+");
		int subNum = currentLine.length - 1;
		int featureNum = dataList.size() - 1;
		String[][] featureData = new String[subNum][featureNum];
		for (int i = 1; i < dataList.size(); i++) {
			currentLine = dataList.get(i).split("\\s+");
			for (int j = 1; j < currentLine.length; j++)
				featureData[j - 1][i - 1] = currentLine[j];
		} // for i

		for (int r = 0; r < subNum; r++) {
			List<String> outPutTrainList = new ArrayList<String>();
			List<String> outPutTestList = new ArrayList<String>();
			
			for (int i = 0; i < subNum; i++) {
				if (i != r) // put it to the training samples
				{
					String tmpStringTrain = "";
					if (i < flag)
						tmpStringTrain += "1 ";
					else
						tmpStringTrain += "0 ";
					for (int j = 0; j < featureNum; j++)
						tmpStringTrain += j + ":" +featureData[i][j] + " ";
					outPutTrainList.add(tmpStringTrain);
				} // if
				else // put the only one case to testing samples
				{
					String tmpStringTest = "";
					if (i < flag)
						tmpStringTest += "1 ";
					else
						tmpStringTest += "0 ";
					for (int j = 0; j < featureNum; j++)
						tmpStringTest += j + ":" + featureData[r][j] + " ";
					outPutTestList.add(tmpStringTest);
				} //else
			} // for i
			djVtkUtil.writeArrayListToFile(outPutTrainList, "./2012MICCAI/connectome/svm/" + r
					+ "_SVM_trainInstance_757.txt");
			djVtkUtil.writeArrayListToFile(outPutTestList, "./2012MICCAI/connectome/svm/" + r + "_SVM_testInstance_757.txt");
		} // for r
	}
		
	public void connectome_visuaMatrix() throws IOException
	{
		String clusterFile = "./2012MICCAI/connectome/mci_selected_cluter1.txt";

		double[][] ma = new double[358][358];
		for(int i=0;i<358;i++)
			for(int j=0;j<358;j++)
				ma[i][j]=0.0;
		
		FileInputStream fstream_clusterInfo = new FileInputStream(clusterFile);
		DataInputStream in_clusterInfo = new DataInputStream(fstream_clusterInfo);
		BufferedReader br_clusterInfo = new BufferedReader(new InputStreamReader(in_clusterInfo));
		String strLine;
		String[] tmpStringArray;
		br_clusterInfo.readLine();// ignore the first line
		int pairCount=0;
		while ((strLine = br_clusterInfo.readLine()) != null) {
			if ((tmpStringArray = strLine.split("\\s+")).length > 0) {
				pairCount++;
				String strPair = tmpStringArray[1];//****************Need to modify sometimes
				System.out.println("analyzing pair:" + strPair);
				int dicccol_ID1 = Integer.valueOf(strPair.split("-")[0]); //need -1 if dicccol is from 1 to 358
				int dicccol_ID2 = Integer.valueOf(strPair.split("-")[1]); //need -1 if dicccol is from 1 to 358
				ma[dicccol_ID1][dicccol_ID2]=1.0;
				ma[dicccol_ID2][dicccol_ID1]=1.0;
			} //
		}// while
		br_clusterInfo.close();
		in_clusterInfo.close();
		fstream_clusterInfo.close();
		System.out.println("There are "+pairCount+" pairs.  ");
		findModelUtil.writeArrayListToFile(ma, 358, 358, "./2012MICCAI/connectome/mci_cluster1_matrix.txt");
		
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Miccai2012 mainHandler = new Miccai2012();
		mainHandler.initialDMN();

		// calculate the structural constraint (weights)
		 mainHandler.calculateStructuralMatrix();

		// extract the B
		// mainHandler.extractBoldSig();

		// mainHandler.prepareDataForSVM();

		// mainHandler.drawRegression();

		// mainHandler.generateFilesForSVM();

		// mainHandler.calEntropy();

		// mainHandler.connectome_prepareForWeka();
		// mainHandler.connectome_geneSelectedFeatureFile();
//		mainHandler.connectome_geneFilesForSVM();
		
//		mainHandler.connectome_visuaMatrix();

	}

}
