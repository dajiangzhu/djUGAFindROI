

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import com.ojn.gexf4j.*;
import com.ojn.gexf4j.core.*;
import com.ojn.gexf4j.core.data.*;

import com.ojn.gexf4j.core.impl.*;
import com.ojn.gexf4j.core.impl.data.*;
import com.ojn.gexf4j.core.GexfWriter;

import edu.uga.DICCCOL.DicccolUtilIO;
import edu.uga.liulab.djVtkBase.djVtkSurData;

public class connectomePrepareData {

	// ///////////////
	public int[] dicccolLocation = new int[358];
	AbstractMatrix DICCCOL_M = Matrix.fromASCIIFile(new File("./connectome/visualization/finalizedMat.txt"));

	public void prepareDicccolNetworkRelation() throws BiffException, IOException {
		File inputWorkbook = new File("./connectome/DICCCOL_labels.xls");
		Workbook w;
		List<List<String>> dicccolList = new ArrayList<List<String>>();
		w = Workbook.getWorkbook(inputWorkbook);
		Sheet sheet = w.getSheet("Sheet1");

		// System.out.println("row-0 col-1"+sheet.getCell(1, 0).getContents());
		// System.out.println("row-0 col-64"+sheet.getCell(64,
		// 0).getContents());

		System.out.println("begin to read excel...");
		for (int row = 1; row < 359; row++) {
			List<String> networkList = new ArrayList<String>();
			for (int col = 1; col < 65; col++) {
				Cell cell = sheet.getCell(col, row);
				if (cell.getContents().trim().length() != 0)
					networkList.add(sheet.getCell(col, 0).getContents());
			} // for col
			dicccolList.add(networkList);
		} // for row

		// write to a txt file
		System.out.println("begin to write txt...");
		String fileName = "./connectome/DICCCOL_Networks_1120.txt";
		FileWriter fw = null;
		fw = new FileWriter(fileName);
		for (int i = 0; i < dicccolList.size(); i++) {
			System.out.println("begin to write diccclo:" + (i + 1));
			fw.write((i + 1) + "   " + dicccolList.get(i).size() + "   ");
			for (int j = 0; j < dicccolList.get(i).size(); j++)
				fw.write(dicccolList.get(i).get(j) + "::");
			fw.write("\r\n");
		}
		fw.close();
	}

	public void writeGEXF() throws IOException {
		String saveFileName = "./connectome/mci/cluster1.gexf";
		String clusterFileName = "./connectome/mci/analysis_cluster1.txt";
		int pairNum = 200; // need to know in advance

		int[][] pairInfor = new int[pairNum][2];

		// analyze cluster file which contains pair information
		int pairCount = -1;
		FileInputStream fstream_clusterInfo = new FileInputStream(clusterFileName);
		DataInputStream in_clusterInfo = new DataInputStream(fstream_clusterInfo);
		BufferedReader br_clusterInfo = new BufferedReader(new InputStreamReader(in_clusterInfo));
		String strLine;
		String[] tmpStringArray;
		br_clusterInfo.readLine();// ignore the first line
		while ((strLine = br_clusterInfo.readLine()) != null) {
			if ((tmpStringArray = strLine.split("\\s+")).length > 0) {
				pairCount++;
				String strPair = tmpStringArray[1];
				System.out.println("analyzing pair:" + strPair);
				int dicccol_ID1 = Integer.valueOf(strPair.split("-")[0]);
				int dicccol_ID2 = Integer.valueOf(strPair.split("-")[1]);
				pairInfor[pairCount][0] = dicccol_ID1;
				pairInfor[pairCount][1] = dicccol_ID2;
			} //
		}// while
		br_clusterInfo.close();
		in_clusterInfo.close();
		fstream_clusterInfo.close();
		System.out.println("pair count is : " + (++pairCount));

		// begin to write GEXF file
		FileWriter fw = null;
		fw = new FileWriter(saveFileName);
		fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		fw.write("<gexf xmlns=\"http://www.gephi.org/gexf\" xmlns:viz=\"http://www.gephi.org/gexf/viz\">\r\n");
		fw.write("<graph type=\"static\">\r\n");
		fw.write("<attributes class=\"node\" type=\"static\">\r\n");
		fw.write("<attribute id=\"label\" title=\"label\" type=\"string\"/>\r\n");
		fw.write("</attributes>\r\n");

		// write 358 nodes
		fw.write("<nodes>\r\n");
		double r = 2000.0;
		for (int i = 0; i < 358; i++) {
			double x = r * Math.cos(i * Math.PI / 179.0);
			double y = r * Math.sin(i * Math.PI / 179.0);
			fw.write("<node id=\"" + i + "\" label=\"" + i + "\" >\r\n");
			fw.write("<viz:position x=\"" + x + "\" y=\"" + y + "\" z=\"0.0\"/>\r\n");
			fw.write("<viz:color b=\"0\" g=\"255\" r=\"0\"/>\r\n");
			fw.write("</node>\r\n");
		}
		fw.write("</nodes>\r\n");

		// write edges
		fw.write("<edges>\r\n");
		for (int i = 0; i < pairCount; i++) {
			fw.write("<edge id=\"" + i + "\" source=\"" + pairInfor[i][0] + "\" target=\"" + pairInfor[i][1]
					+ "\" >\r\n");
			fw.write("</edge>\r\n");
		}
		fw.write("</edges>\r\n");

		fw.write("</graph>\r\n");
		fw.write("</gexf>\r\n");

		fw.close();
		System.out.println("Write file done!");
	}

	private int findMaxWithinList(List<Integer> dicccolList, djVtkSurData surData) {
		int result = -1;
		float tmpMax = -10000.0f;
		for (int i = 0; i < dicccolList.size(); i++) {
			float y = surData.getPoint( (int) DICCCOL_M.get(dicccolList.get(i), 0) ).y;
			if (y > tmpMax) {
				result = i;
				tmpMax = y;
			}
		}
		return result;
	}

	private int findMinWithinList(List<Integer> dicccolList, djVtkSurData surData) {
		int result = -1;
		float tmpMax = 10000.0f;
		for (int i = 0; i < dicccolList.size(); i++) {
			float y = surData.getPoint((int) DICCCOL_M.get(dicccolList.get(i), 0)).y;
			if (y < tmpMax) {
				result = i;
				tmpMax = y;
			}
		}
		return result;
	}

	public void analyzeOneLobe(List<Integer> dicccolList, int locationStart, int locationEnd, djVtkSurData surData,
			int flag) // flag:1 find max; 0 find min
	{
		if (dicccolList.size() != (locationEnd - locationStart + 1))
			System.out.println("Error with list num!!");
		else {
			for (int i = locationStart; i <= locationEnd; i++) {
				if (i == locationEnd)
					if (dicccolList.size() != 1)
						System.out.println("Error with list num!!");
				int p = -1;
				if (flag == 1)
					p = this.findMaxWithinList(dicccolList, surData);
				else
					p = this.findMinWithinList(dicccolList, surData);
				dicccolLocation[i] = dicccolList.get(p);
				dicccolList.remove(p);
			} // for
		} // else
	}
	
	
	public void outputLobe()
	{
		Map<Integer, List<Integer>> anaResult = new HashMap<Integer, List<Integer>>();
		anaResult.put(30, new ArrayList<Integer>());
		anaResult.put(57, new ArrayList<Integer>());
		anaResult.put(73, new ArrayList<Integer>());
		anaResult.put(83, new ArrayList<Integer>());
		anaResult.put(17, new ArrayList<Integer>());
		anaResult.put(105, new ArrayList<Integer>());
		anaResult.put(45, new ArrayList<Integer>());
		anaResult.put(59, new ArrayList<Integer>());
		anaResult.put(0, new ArrayList<Integer>());

		//AbstractMatrix DICCCOL_M = Matrix.fromASCIIFile(new File("./connectome/visualization/finalizedMat.txt"));
		djVtkSurData surData = new djVtkSurData("./connectome/10.wm.lobe.vtk");
		for (int i = 0; i < 358; i++) {
			int ptID = (int) DICCCOL_M.get(i, 0);
			int label = Integer.valueOf(surData.pointsScalarData.get("Labels").get(ptID));
			anaResult.get(label).add(i);
			System.out.println( i + " " + label);
		}
		// deal with 185
		anaResult.get(30).add(185);
		DicccolUtilIO.writeIntegerListToFile(anaResult.get(17), "DicccolID_RF.txt");
		DicccolUtilIO.writeIntegerListToFile(anaResult.get(105), "DicccolID_RP.txt");
		DicccolUtilIO.writeIntegerListToFile(anaResult.get(59), "DicccolID_RT.txt");
		DicccolUtilIO.writeIntegerListToFile(anaResult.get(45), "DicccolID_RO.txt");
		DicccolUtilIO.writeIntegerListToFile(anaResult.get(73), "DicccolID_LO.txt");
		DicccolUtilIO.writeIntegerListToFile(anaResult.get(83), "DicccolID_LT.txt");
		DicccolUtilIO.writeIntegerListToFile(anaResult.get(57), "DicccolID_LP.txt");
		DicccolUtilIO.writeIntegerListToFile(anaResult.get(30), "DicccolID_LF.txt");
	}

	public void analyze358() {
		Map<Integer, List<Integer>> anaResult = new HashMap<Integer, List<Integer>>();
		anaResult.put(30, new ArrayList<Integer>());
		anaResult.put(57, new ArrayList<Integer>());
		anaResult.put(73, new ArrayList<Integer>());
		anaResult.put(83, new ArrayList<Integer>());
		anaResult.put(17, new ArrayList<Integer>());
		anaResult.put(105, new ArrayList<Integer>());
		anaResult.put(45, new ArrayList<Integer>());
		anaResult.put(59, new ArrayList<Integer>());
		anaResult.put(0, new ArrayList<Integer>());

		//AbstractMatrix DICCCOL_M = Matrix.fromASCIIFile(new File("./connectome/visualization/finalizedMat.txt"));
		djVtkSurData surData = new djVtkSurData("./connectome/10.wm.lobe.vtk");
		for (int i = 0; i < 358; i++) {
			int ptID = (int) DICCCOL_M.get(i, 0);
			int label = Integer.valueOf(surData.pointsScalarData.get("Labels").get(ptID));
			anaResult.get(label).add(i);
			System.out.println( i + " " + label);
		}
		// deal with 185
		anaResult.get(30).add(185);

		System.out.println("begin to analyze one lobe...");
		//before for gephy
//		this.analyzeOneLobe(anaResult.get(30), 0, 84, surData,1);//left-f
//		this.analyzeOneLobe(anaResult.get(57), 85, 124, surData,1);//left-p
//		this.analyzeOneLobe(anaResult.get(73), 125, 138, surData,1);//left-o
//		this.analyzeOneLobe(anaResult.get(83), 139, 180, surData,1);//left-t
//		this.analyzeOneLobe(anaResult.get(17), 272, 357, surData,0);//right-f
//		this.analyzeOneLobe(anaResult.get(105), 251, 271, surData,0);//right-p
//		this.analyzeOneLobe(anaResult.get(45), 226, 250, surData,0);//right-o
//		this.analyzeOneLobe(anaResult.get(59), 181, 225, surData,0);//right-t
		////////////////////////////
		//for circos
		this.analyzeOneLobe(anaResult.get(17), 0, 85, surData,1);//right-f
		this.analyzeOneLobe(anaResult.get(105), 86, 106, surData,1);//right-p
		this.analyzeOneLobe(anaResult.get(59), 107, 151, surData,1);//right-t
		this.analyzeOneLobe(anaResult.get(45), 152, 176, surData,1);//right-o
		this.analyzeOneLobe(anaResult.get(73), 177, 190, surData,0);//left-o
		this.analyzeOneLobe(anaResult.get(83), 191, 232, surData,0);//left-t
		this.analyzeOneLobe(anaResult.get(57), 233, 272, surData,0);//left-p
		this.analyzeOneLobe(anaResult.get(30), 273, 357, surData,0);//left-f
		
		
		
		
		
		
		

		Set tmpSet = new HashSet();
		for (int i = 0; i < 358; i++) {
			System.out.println(i + " " + dicccolLocation[i]);
			tmpSet.add(dicccolLocation[i]);
		}
		System.out.println("The size of dicccolLocation is : " + tmpSet.size());

		for (int i = 0; i < 358; i++)
			for (int j = 0; j < 358; j++) {
				if (dicccolLocation[j] == i)
					System.out.println(i + " " + j);
			}

		// System.out.println("the size of 30 : "+anaResult.get(30).size());
		// System.out.println("the size of 57 : "+anaResult.get(57).size());
		// System.out.println("the size of 73 : "+anaResult.get(73).size());
		// System.out.println("the size of 83 : "+anaResult.get(83).size());
		// System.out.println("the size of 17 : "+anaResult.get(17).size());
		// System.out.println("the size of 105 : "+anaResult.get(105).size());
		// System.out.println("the size of 45 : "+anaResult.get(45).size());
		// System.out.println("the size of 59 : "+anaResult.get(59).size());
		// System.out.println("the size of 0 : "+anaResult.get(0).size());

	}

	public void testLobeLabel() {
		djVtkSurData surData = new djVtkSurData("./connectome/10.wm.lobe.vtk");
		String[] ss = new String[18];
		ss[0] = surData.pointsScalarData.get("Labels").get(39864);
		ss[1] = surData.pointsScalarData.get("Labels").get(39888);
		ss[2] = surData.pointsScalarData.get("Labels").get(18421);
		ss[3] = surData.pointsScalarData.get("Labels").get(20549);
		ss[4] = surData.pointsScalarData.get("Labels").get(1617);
		ss[5] = surData.pointsScalarData.get("Labels").get(1623);
		ss[6] = surData.pointsScalarData.get("Labels").get(27231);
		ss[7] = surData.pointsScalarData.get("Labels").get(15752);
		ss[8] = surData.pointsScalarData.get("Labels").get(42400);
		ss[9] = surData.pointsScalarData.get("Labels").get(48784);
		ss[10] = surData.pointsScalarData.get("Labels").get(21115);
		ss[11] = surData.pointsScalarData.get("Labels").get(26926);
		ss[12] = surData.pointsScalarData.get("Labels").get(12272);
		ss[13] = surData.pointsScalarData.get("Labels").get(14905);
		ss[14] = surData.pointsScalarData.get("Labels").get(1501);
		ss[15] = surData.pointsScalarData.get("Labels").get(2606);
		ss[16] = surData.pointsScalarData.get("Labels").get(2157);
		ss[17] = surData.pointsScalarData.get("Labels").get(2729);
		for (int i = 0; i < 18; i++)
			System.out.println("the " + i + "th element: " + ss[i]);

	}

	public void testGEXFWriter() throws IOException {
		Gexf gexf = new GexfImpl();

		// gexf.getMetadata()
		// .setLastModified(new Date())
		// .setCreator("Gephi.org")
		// .setDescription("A Web network");
		//
		// AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		// gexf.getGraph().getAttributeLists().add(attrList);
		//
		// Attribute attUrl = attrList.createAttribute("0",
		// AttributeType.STRING, "url");
		// Attribute attIndegree = attrList.createAttribute("1",
		// AttributeType.FLOAT, "indegree");
		// Attribute attFrog = attrList.createAttribute("2",
		// AttributeType.BOOLEAN, "frog")
		// .setDefaultValue("true");

		Node gephi = gexf.getGraph().createNode("0");
		gephi.setLabel("Gephi");

		Node webatlas = gexf.getGraph().createNode("1");
		webatlas.setLabel("Webatlas");

		Node rtgi = gexf.getGraph().createNode("2");
		rtgi.setLabel("RTGI");

		Node blab = gexf.getGraph().createNode("3");
		blab.setLabel("BarabasiLab");

		gephi.connectTo("0", webatlas);
		gephi.connectTo("1", rtgi);
		webatlas.connectTo("2", gephi);
		rtgi.connectTo("3", webatlas);
		gephi.connectTo("4", blab);

		GexfWriter gw = new StaxGraphWriter();
		String fileName = "./connectome/testWriter.gexf";
		File f = new File(fileName);
		FileOutputStream fos = new FileOutputStream(f);

		gw.writeToStream(gexf, fos);

	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws BiffException
	 */
	public static void main(String[] args) throws BiffException, IOException {
		// TODO Auto-generated method stub
		connectomePrepareData mainHandler = new connectomePrepareData();
//		 mainHandler.prepareDicccolNetworkRelation();
		// mainHandler.writeGEXF();
//		mainHandler.analyze358();
		mainHandler.outputLobe();

	}

}
