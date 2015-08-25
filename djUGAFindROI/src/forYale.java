

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

public class forYale {
	private List<String> NodesLabes;

	// ********************************************************************
	List<int[][]> dataNormal = new ArrayList<int[][]>();
	List<String> dataNormalLabel = new ArrayList<String>();
	List<int[][]> dataPatient = new ArrayList<int[][]>();
	List<String> dataPatientLabel = new ArrayList<String>();
	String groupDes = "elderly";
	String folderDir = "./forYale/matrix/";

	// ********************************************************************

	public void initialNodeLabes() {
		NodesLabes = new ArrayList<String>();
		String strPrefix = "";
		NodesLabes.add(strPrefix + "326");
		NodesLabes.add(strPrefix + "76");
		NodesLabes.add(strPrefix + "144");
		NodesLabes.add(strPrefix + "45");
		NodesLabes.add(strPrefix + "298");
		NodesLabes.add(strPrefix + "79");
		NodesLabes.add(strPrefix + "155");
		NodesLabes.add(strPrefix + "72");
	}

	public void generateGEXF() throws IOException {
		String folderDir = "./for_DJ/";
		File dir = new File(folderDir);
		String[] files = dir.list();
		for (int f = 0; f < files.length; f++) {
			String filename = files[f];
			if (filename.startsWith("0")) {
				System.out.println("Now deal with:"+filename);
				List<Node> nodeList = new ArrayList<Node>();
				double r = 300.0;
				Gexf gexf = new GexfImpl();
				gexf.setVisualization(true);
				for (int i = 0; i < this.NodesLabes.size(); i++) {
					Node tmpNode = gexf.getGraph().createNode(NodesLabes.get(i));
					tmpNode.setPosition(new PositionImpl((float) (r * Math.cos(Math.PI / 8.0 + i * Math.PI / 4.0)),
							(float) (r * Math.sin(Math.PI / 8.0 + i * Math.PI / 4.0)), 0.0f));
					tmpNode.setLabel(this.NodesLabes.get(i));
					tmpNode.setColor(new ColorImpl(0, 255, 0));
					tmpNode.setSize(25.0f);
					nodeList.add(tmpNode);
				} // for i

				AbstractMatrix currentDataM = Matrix.fromASCIIFile(new File(folderDir + filename));
				int dimRow = currentDataM.getRowDimension();
				int dimColumn = currentDataM.getColumnDimension();
				if ((dimRow == dimColumn) && (dimRow == this.NodesLabes.size())) {
					for (int row = 0; row < dimRow; row++)
						for (int column = 0; column < dimColumn; column++) {
							double currentEle = currentDataM.get(row, column);
							double symEle = currentDataM.get(column, row);
							if (currentEle > 0.0)
								if (symEle > 0.0)
									nodeList.get(row).connectTo(nodeList.get(column))
											.setColor(new ColorImpl(255, 0, 0)).setThickness(3.0f);
								else
									nodeList.get(row).connectTo(nodeList.get(column))
											.setColor(new ColorImpl(0, 0, 255)).setThickness(3.0f);
						} // for column
					GexfWriter gw = new StaxGraphWriter();
					File fileSave = new File(folderDir + "visualization_" + filename + ".gexf");
					FileOutputStream fos = new FileOutputStream(fileSave);
					gw.writeToStream(gexf, fos);
					System.out.println("Write file done!");
				} // if
				else
					System.out.println("Error!--there is something wrong with the dimention of the Matrix");

			} // if the current file is start with sp_
		} // for f
	}

	public float calSimDis(int[][] m1, int[][] m2, int dim) {
		float result = 0.0f;
		for (int i = 0; i < dim; i++)
			for (int j = 0; j < dim; j++)
				if (m1[i][j] != m2[i][j])
					result++;
		return result / (dim * dim);
	}

	public float[][] calSimMatrix(List<int[][]> dataMatrix, int dim) {
		int dimSimMatrix = dataMatrix.size();
		float[][] simMatrix = new float[dimSimMatrix][dimSimMatrix];
		for (int i = 0; i < dimSimMatrix - 1; i++)
			for (int j = i + 1; j < dimSimMatrix; j++)
				simMatrix[i][j] = simMatrix[j][i] = this.calSimDis(dataMatrix.get(i), dataMatrix.get(j), dim);
		return simMatrix;
	}

	public void writeMatix(int[][] data, int dimRow, int dimColumn, String fileName) {
		List<String> outList = new ArrayList<String>();
		for (int i = 0; i < dimRow; i++) {
			String str = "";
			for (int j = 0; j < dimColumn; j++)
				str = str + data[i][j] + " ";
			outList.add(str);
		}
		djVtkUtil.writeArrayListToFile(outList, fileName);
	}

	public void writeMatix(double[][] data, int dim, String fileName) {
		List<String> outList = new ArrayList<String>();
		for (int i = 0; i < dim; i++) {
			String str = "";
			for (int j = 0; j < dim; j++)
				str = str + data[i][j] + " ";
			outList.add(str);
		}
		djVtkUtil.writeArrayListToFile(outList, fileName);
	}

	public void initialDataMatrix(String networkName, int dim) throws IOException {
		File dir = new File(folderDir);
		String[] files = dir.list();
		for (int f = 0; f < files.length; f++) {
			String filename = files[f];
			if (filename.startsWith(networkName + "_ptsd_" + groupDes)) {
				System.out.println("Starte to read file:" + filename);
				String[] fileNameComponent = filename.split("_");
				String strLabel = networkName + "_" + fileNameComponent[2] + "_" + fileNameComponent[3] + "_"
						+ fileNameComponent[5];
				// read the current file
				FileInputStream fstream_currentSub = new FileInputStream(folderDir + filename);
				DataInputStream in_currentSub = new DataInputStream(fstream_currentSub);
				BufferedReader br_currentSub = new BufferedReader(new InputStreamReader(in_currentSub));
				String strLine;
				String[] tmpStringArray;
				while ((strLine = br_currentSub.readLine()) != null) {
					if (strLine.startsWith("startpoint")) {
						int[][] tmpArray = new int[dim][dim];
						String tmpLabel = strLabel + "_" + strLine.split("\\s+")[1];
						int tmpCount = 0;
						while (tmpCount < dim) {
							strLine = br_currentSub.readLine();
							if ((tmpStringArray = strLine.split("\\s+")).length > 0) {
								String[] eleLine = strLine.split("\\s+");
								if (eleLine.length == dim) {
									for (int i = 0; i < dim; i++)
										tmpArray[tmpCount][i] = Integer.valueOf(eleLine[i]);
									tmpCount++;
								}
							} // if
						} // while tmpCount<dim
						if (fileNameComponent[3].equalsIgnoreCase("normal")) {
							dataNormalLabel.add(tmpLabel);
							dataNormal.add(tmpArray);
						} else {
							dataPatientLabel.add(tmpLabel);
							dataPatient.add(tmpArray);
						}
					} // if a new matrix with different start point
				}// while
				br_currentSub.close();
				in_currentSub.close();
				fstream_currentSub.close();
				System.out.println("The size of normal is:" + dataNormalLabel.size());
				System.out.println("The size of patient is:" + dataPatient.size());
			} // if end of reading the current file
		} // for f
		System.out.println("Matrix initilazation finished...");
	}

	public double[][] calAveMatrix(List<int[][]> dataMatrix, List<String> normalAssignedLabels, String center, int dim) {
		int dataSize = dataMatrix.size();
		int labelSize = normalAssignedLabels.size();
		double[][] result = new double[dim][dim];
		int count = 0;
		for (int i = 0; i < dataSize; i++) {
			if (normalAssignedLabels.get(i).equalsIgnoreCase(center)) {
				for (int m = 0; m < dim; m++)
					for (int n = 0; n < dim; n++)
						result[m][n] += dataMatrix.get(i)[m][n];
				count++;
			} // if
		} // for i

		for (int m = 0; m < dim; m++)
			for (int n = 0; n < dim; n++)
				result[m][n] /= count;
		return result;
	}

	public void findPatternMatrix(String networkName, int dim) throws IOException {
		this.initialDataMatrix(networkName, dim);
		List<String> normalCenters = djVtkUtil.loadFileToArrayList(folderDir + networkName + "_" + groupDes
				+ "_Normal_Result.txt.center.txt");
		List<String> normalAssignedLabels = djVtkUtil.loadFileToArrayList(folderDir + networkName + "_" + groupDes
				+ "_Normal_Result.txt.assignedLabel.txt");
		List<String> patientCenters = djVtkUtil.loadFileToArrayList(folderDir + networkName + "_" + groupDes
				+ "_Patient_Result.txt.center.txt");
		List<String> patientAssignedLabels = djVtkUtil.loadFileToArrayList(folderDir + networkName + "_" + groupDes
				+ "_Patient_Result.txt.assignedLabel.txt");
		for (int c = 0; c < normalCenters.size(); c++) {
			String strCenter = normalCenters.get(c).trim();
			if (strCenter.length() != 0) {
				System.out.println("Normal Center: " + strCenter);
				double[][] aveMatrix = this.calAveMatrix(dataNormal, normalAssignedLabels, strCenter, dim);
				this.writeMatix(aveMatrix, dim, folderDir + groupDes + "/" + networkName + "_" + groupDes
						+ "_normal_pattern_" + strCenter + ".txt");
			} // if
		} // for c

		for (int c = 0; c < patientCenters.size(); c++) {
			String strCenter = patientCenters.get(c).trim();
			if (strCenter.length() != 0) {
				System.out.println("Normal Center: " + strCenter);
				double[][] aveMatrix = this.calAveMatrix(dataPatient, patientAssignedLabels, strCenter, dim);
				this.writeMatix(aveMatrix, dim, folderDir + groupDes + "/" + networkName + "_" + groupDes
						+ "_patient_pattern_" + strCenter + ".txt");
			} // if
		} // for c
	}

	public void prepareSimMatrix(String networkName, int dim) throws IOException {
		this.initialDataMatrix(networkName, dim);
		// **************************output center
		// matrix*********************************************
		// List<String> centerNormalList =
		// djVtkUtil.loadFileToArrayList(folderDir+networkName+"_"+groupDes+"_Normal_Result.txt.center.txt");
		// List<String> centerPatientList =
		// djVtkUtil.loadFileToArrayList(folderDir+networkName+"_"+groupDes+"_Patient_Result.txt.center.txt");
		// for(int i=0;i<centerNormalList.size();i++)
		// {
		// String strCenter = centerNormalList.get(i).trim();
		// if(strCenter.length()>0)
		// {
		// int matrixIndex = Integer.valueOf(strCenter)-1;
		// this.writeMatix(dataNormal.get(matrixIndex), dim,
		// folderDir+networkName+"/"+networkName+"_normal_"+matrixIndex+".txt");
		// }
		// }
		// for(int i=0;i<centerPatientList.size();i++)
		// {
		// String strCenter = centerPatientList.get(i).trim();
		// if(strCenter.length()>0)
		// {
		// int matrixIndex = Integer.valueOf(strCenter)-1;
		// this.writeMatix(dataPatient.get(matrixIndex), dim,
		// folderDir+networkName+"/"+networkName+"_patient_"+matrixIndex+".txt");
		// }
		// }

		// **************************calculate the
		// simMatrix********************************************
		float[][] resultNormal = this.calSimMatrix(dataNormal, dim);
		float[][] resultPatient = this.calSimMatrix(dataPatient, dim);
		System.out.println("Calculate the simMatrix finished...");

		// **************************Write the matrix
		List<String> outList = new ArrayList<String>();
		for (int i = 0; i < dataNormalLabel.size(); i++) {
			String str = "";
			for (int j = 0; j < dataNormalLabel.size(); j++)
				str = str + resultNormal[i][j] + " ";
			outList.add(str);
		}
		djVtkUtil.writeArrayListToFile(outList, folderDir + networkName + "_" + groupDes + "_Normal_Result.txt");

		outList.clear();
		for (int i = 0; i < dataPatientLabel.size(); i++) {
			String str = "";
			for (int j = 0; j < dataPatientLabel.size(); j++)
				str = str + resultPatient[i][j] + " ";
			outList.add(str);
		}
		djVtkUtil.writeArrayListToFile(outList, folderDir + networkName + "_" + groupDes + "_Patient_Result.txt");
		djVtkUtil.writeArrayListToFile(dataNormalLabel, folderDir + networkName + "_" + groupDes + "_NormalLabel.txt");
		djVtkUtil
				.writeArrayListToFile(dataPatientLabel, folderDir + networkName + "_" + groupDes + "_PatientLabel.txt");
	}

	public void calAveSimMatrix(String networkName, int dim) {
		List<AbstractMatrix> dataMatrix = new ArrayList<AbstractMatrix>();
		List<String> allPatersLabel = new ArrayList<String>();
		List<String> adolscentNormalCenters = djVtkUtil.loadFileToArrayList(folderDir + networkName
				+ "_adolscent_Normal_Result.txt.center.txt");
		List<String> adolscentPatientCenters = djVtkUtil.loadFileToArrayList(folderDir + networkName
				+ "_adolscent_Patient_Result.txt.center.txt");
		List<String> adultNormalCenters = djVtkUtil.loadFileToArrayList(folderDir + networkName
				+ "_adult_Normal_Result.txt.center.txt");
		List<String> adultPatientCenters = djVtkUtil.loadFileToArrayList(folderDir + networkName
				+ "_adult_Patient_Result.txt.center.txt");
		List<String> elderlyNormalCenters = djVtkUtil.loadFileToArrayList(folderDir + networkName
				+ "_elderly_Normal_Result.txt.center.txt");
		List<String> elderlyPatientCenters = djVtkUtil.loadFileToArrayList(folderDir + networkName
				+ "_elderly_Patient_Result.txt.center.txt");

		for (int i = 0; i < adolscentNormalCenters.size(); i++) {
			AbstractMatrix tmpMatrix = Matrix.fromASCIIFile(new File(folderDir + "adolscent/" + networkName
					+ "_adolscent_normal_pattern_" + adolscentNormalCenters.get(i).trim() + ".txt"));
			dataMatrix.add(tmpMatrix);
			allPatersLabel.add(networkName + "_adolscent_normal_pattern_" + adolscentNormalCenters.get(i).trim()
					+ ".txt");
		}
		for (int i = 0; i < adolscentPatientCenters.size(); i++) {
			AbstractMatrix tmpMatrix = Matrix.fromASCIIFile(new File(folderDir + "adolscent/" + networkName
					+ "_adolscent_patient_pattern_" + adolscentPatientCenters.get(i).trim() + ".txt"));
			dataMatrix.add(tmpMatrix);
			allPatersLabel.add(networkName + "_adolscent_patient_pattern_" + adolscentPatientCenters.get(i).trim()
					+ ".txt");
		}

		for (int i = 0; i < adultNormalCenters.size(); i++) {
			AbstractMatrix tmpMatrix = Matrix.fromASCIIFile(new File(folderDir + "adult/" + networkName
					+ "_adult_normal_pattern_" + adultNormalCenters.get(i).trim() + ".txt"));
			dataMatrix.add(tmpMatrix);
			allPatersLabel.add(networkName + "_adult_normal_pattern_" + adultNormalCenters.get(i).trim() + ".txt");
		}
		for (int i = 0; i < adultPatientCenters.size(); i++) {
			AbstractMatrix tmpMatrix = Matrix.fromASCIIFile(new File(folderDir + "adult/" + networkName
					+ "_adult_patient_pattern_" + adultPatientCenters.get(i).trim() + ".txt"));
			dataMatrix.add(tmpMatrix);
			allPatersLabel.add(networkName + "_adult_patient_pattern_" + adultPatientCenters.get(i).trim() + ".txt");
		}

		for (int i = 0; i < elderlyNormalCenters.size(); i++) {
			AbstractMatrix tmpMatrix = Matrix.fromASCIIFile(new File(folderDir + "elderly/" + networkName
					+ "_elderly_normal_pattern_" + elderlyNormalCenters.get(i).trim() + ".txt"));
			dataMatrix.add(tmpMatrix);
			allPatersLabel.add(networkName + "_elderly_normal_pattern_" + elderlyNormalCenters.get(i).trim() + ".txt");
		}
		for (int i = 0; i < elderlyPatientCenters.size(); i++) {
			AbstractMatrix tmpMatrix = Matrix.fromASCIIFile(new File(folderDir + "elderly/" + networkName
					+ "_elderly_patient_pattern_" + elderlyPatientCenters.get(i).trim() + ".txt"));
			dataMatrix.add(tmpMatrix);
			allPatersLabel
					.add(networkName + "_elderly_patient_pattern_" + elderlyPatientCenters.get(i).trim() + ".txt");
		}

		// **************************output center
		// matrix*********************************************
		List<String> centerList = djVtkUtil.loadFileToArrayList(folderDir + networkName + "_ave_sim.txt.center.txt");
		for (int i = 0; i < centerList.size(); i++) {
			String strCenter = centerList.get(i).trim();
			if (strCenter.length() > 0) {
				int matrixIndex = Integer.valueOf(strCenter) - 1;
				this.writeMatix(dataMatrix.get(matrixIndex).toDouble2DArray(), dim, folderDir + networkName + "/ave"
						+ "/" + networkName + "_ave_pattern_" + (i + 1) + "_" + (matrixIndex + 1) + ".txt");
			}
		}

		// **************************calculate the sim
		// matrix*********************************************
		// int dimSimMatrix = dataMatrix.size();
		// double[][]aveSimMatrix = new double[dimSimMatrix][dimSimMatrix];
		// for(int i=0;i<dimSimMatrix-1;i++)
		// for(int j=i+1;j<dimSimMatrix;j++)
		// {
		// double[][] m1 = dataMatrix.get(i).toDouble2DArray();
		// double[][] m2 = dataMatrix.get(j).toDouble2DArray();
		// double diff = 0.0;
		// for(int m=0;m<dim;m++)
		// for(int n=0;n<dim;n++)
		// diff += Math.abs(m1[m][n]-m2[m][n]);
		// aveSimMatrix[i][j] = aveSimMatrix[j][i] = diff/(dim*dim);
		// }
		// this.writeMatix(aveSimMatrix, dataMatrix.size(),
		// folderDir+networkName+"_ave_sim.txt");
		// djVtkUtil.writeArrayListToFile(allPatersLabel,
		// folderDir+networkName+"_ave_label.txt");
	}

	public void doStat(String networkName) {
		List<String> centerList = djVtkUtil.loadFileToArrayList(folderDir + networkName + "_ave_sim.txt.center.txt");
		List<String> labelList = djVtkUtil.loadFileToArrayList(folderDir + networkName
				+ "_ave_sim.txt.assignedLabel.txt");
		int centerNum = centerList.size();
		int[][] dataArray = new int[centerNum][8];
		for (int i = 0; i < 12; i++)
			// adolscent normal
			for (int j = 0; j < centerNum; j++)
				if (labelList.get(i).trim().equalsIgnoreCase(centerList.get(j).trim()))
					dataArray[j][0]++;
		for (int i = 12; i < 23; i++)
			// adolscent patient
			for (int j = 0; j < centerNum; j++)
				if (labelList.get(i).trim().equalsIgnoreCase(centerList.get(j).trim()))
					dataArray[j][1]++;
		for (int i = 23; i < 35; i++)
			// adult normal
			for (int j = 0; j < centerNum; j++)
				if (labelList.get(i).trim().equalsIgnoreCase(centerList.get(j).trim()))
					dataArray[j][2]++;
		for (int i = 35; i < 45; i++)
			// adult patient
			for (int j = 0; j < centerNum; j++)
				if (labelList.get(i).trim().equalsIgnoreCase(centerList.get(j).trim()))
					dataArray[j][3]++;
		for (int i = 45; i < 56; i++)
			// elderly normal
			for (int j = 0; j < centerNum; j++)
				if (labelList.get(i).trim().equalsIgnoreCase(centerList.get(j).trim()))
					dataArray[j][4]++;
		for (int i = 56; i < 67; i++)
			// elderly patient
			for (int j = 0; j < centerNum; j++)
				if (labelList.get(i).trim().equalsIgnoreCase(centerList.get(j).trim()))
					dataArray[j][5]++;
		for (int j = 0; j < centerNum; j++)
			// normal
			dataArray[j][6] = dataArray[j][0] + dataArray[j][2] + dataArray[j][4];
		for (int j = 0; j < centerNum; j++)
			// patient
			dataArray[j][7] = dataArray[j][1] + dataArray[j][3] + dataArray[j][5];

		this.writeMatix(dataArray, centerNum, 8, folderDir + networkName + "_ave_stat.txt");
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		forYale mainHandler = new forYale();

		// **********************************
		mainHandler.initialNodeLabes();
		mainHandler.generateGEXF();
		// **********12/16******************
		// mainHandler.prepareSimMatrix("dmn", 8);
		// mainHandler.findPatternMatrix("dmn",8);
		// mainHandler.calAveSimMatrix("dmn", 8);
		// mainHandler.doStat("dmn");

	}

}
