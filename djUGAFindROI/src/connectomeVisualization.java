

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

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import edu.uga.liulab.djVtkBase.*;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import com.ojn.gexf4j.*;
import com.ojn.gexf4j.core.*;
import com.ojn.gexf4j.core.data.*;
import com.ojn.gexf4j.core.impl.*;
import com.ojn.gexf4j.core.impl.data.*;
import com.ojn.gexf4j.core.impl.viz.ColorImpl;
import com.ojn.gexf4j.core.impl.viz.PositionImpl;
import com.ojn.gexf4j.core.viz.NodeShape;
import com.ojn.gexf4j.core.viz.Position;

public class connectomeVisualization {

	public int pairCount = 0;

	public Set<Integer> dicccolSet = new HashSet<Integer>();
	public Map<Integer, List> dicccolIndexMap = new HashMap<Integer, List>();
	public List<Integer> clusterDICCCOLList = new ArrayList<Integer>();
	public Map<Integer, Integer> clusterDICCCOLIndex = new HashMap<Integer, Integer>();
	public Map<String, List<Integer>> networkDicccolMap = new HashMap<String, List<Integer>>();

	public void analyzeCluster(String clusterFileName) {
		try {
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
					int dicccol_ID1 = Integer.valueOf(strPair.split("-")[0]); // need
																				// -1
																				// if
																				// dicccol
																				// is
																				// from
																				// 1
																				// to
																				// 358
					int dicccol_ID2 = Integer.valueOf(strPair.split("-")[1]); // need
																				// -1
																				// if
																				// dicccol
																				// is
																				// from
																				// 1
																				// to
																				// 358
					dicccolSet.add(dicccol_ID1);
					dicccolSet.add(dicccol_ID2);
					if (dicccolIndexMap.containsKey(dicccol_ID1))
						dicccolIndexMap.get(dicccol_ID1).add(dicccol_ID2);
					else {
						List<Integer> neighborList = new ArrayList<Integer>();
						neighborList.add(dicccol_ID2);
						dicccolIndexMap.put(dicccol_ID1, neighborList);
					}
				} //
			}// while
			br_clusterInfo.close();
			in_clusterInfo.close();
			fstream_clusterInfo.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		System.out.println("analyzing pair finished... total: " + pairCount);

		System.out.println("begin to build new index...");
		int count = 0;
		Iterator it = dicccolSet.iterator();
		while (it.hasNext()) {
			int currentDicccol = (Integer) it.next();
			System.out.println("currentDicccol:" + currentDicccol);
			clusterDICCCOLList.add(currentDicccol);
			clusterDICCCOLIndex.put(currentDicccol, count++);
		}
		System.out.println("analyzeCluster finished...");
	}

	public void writeNetwork(String saveFileName) throws IOException {
		System.out.println("begin to writeNetwork...");
		AbstractMatrix DICCCOL_M = Matrix.fromASCIIFile(new File("./connectome/visualization/finalizedMat.txt"));
		djVtkSurData surData = new djVtkSurData("./connectome/visualization/surf10to10.wavelet.5.vtk");
		List<djVtkPoint> ptList = new ArrayList<djVtkPoint>();
		List<String> lineList = new ArrayList<String>();
		for (int i = 0; i < clusterDICCCOLList.size(); i++)
			ptList.add(surData.getPoint((int) DICCCOL_M.get(clusterDICCCOLList.get(i), 0)));

		djVtkUtil.writeToPointsVtkFile(saveFileName + ".points.vtk", ptList);

		Iterator it = dicccolIndexMap.keySet().iterator();
		while (it.hasNext()) {
			int keyDicccol = (Integer) it.next();
			for (int i = 0; i < dicccolIndexMap.get(keyDicccol).size(); i++) {
				String strLine = "2 " + clusterDICCCOLIndex.get(keyDicccol) + " "
						+ clusterDICCCOLIndex.get(dicccolIndexMap.get(keyDicccol).get(i)) + " \r\n";
				lineList.add(strLine);
			}
		}

		System.out.println("begin to writefile...");
		FileWriter fw = null;
		fw = new FileWriter(saveFileName);
		fw.write("# vtk DataFile Version 3.0\r\n");
		fw.write("vtk output\r\n");
		fw.write("ASCII\r\n");
		fw.write("DATASET POLYDATA\r\n");
		fw.write("POINTS " + ptList.size() + " float\r\n");
		for (int i = 0; i < ptList.size(); i++)
			fw.write(ptList.get(i).x + " " + ptList.get(i).y + " " + ptList.get(i).z + "\r\n");
		fw.write("LINES " + pairCount + " " + (pairCount * 3) + " \r\n");
		for (int i = 0; i < lineList.size(); i++)
			fw.write(lineList.get(i));

		fw.close();
		System.out.println("Write file done!");
	}

	public void generateDiccolNetworkMap() {
		// List<String> networkList =
		// djVtkUtil.loadFileToArrayList("./connectome/networklist.txt");
		List<String> dicccolNetworkList = djVtkUtil.loadFileToArrayList("./connectome/DICCCOL_Networks.txt");
		String[] tmpStringArray1;
		String[] tmpStringArray2;
		for (int i = 0; i < dicccolNetworkList.size(); i++) {
			tmpStringArray1 = dicccolNetworkList.get(i).split("\\s+");
			if (!tmpStringArray1[1].trim().equalsIgnoreCase("0")) {
				tmpStringArray2 = tmpStringArray1[2].split("::");
				for (int j = 0; j < tmpStringArray2.length; j++) {
					if (networkDicccolMap.containsKey(tmpStringArray2[j]))
						networkDicccolMap.get(tmpStringArray2[j]).add(i);
					else {
						List<Integer> tmpList = new ArrayList<Integer>();
						tmpList.add(i);
						networkDicccolMap.put(tmpStringArray2[j], tmpList);
					}
				} // for j
			} // if
		} // for i

	}

	public void generateGEXF() throws IOException {

		String saveFileName = "./2012MICCAI/connectome/szSelected_cluster.gexf";
		String clusterFileName = "./2012MICCAI/connectome/szSelected_cluster.txt";
		int pairNum = 28; // need to know in advance
		double r = 900.0;

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
				int dicccol_ID1 = Integer.valueOf(strPair.split("-")[0]); // need
																			// -1
																			// if
																			// dicccol
																			// is
																			// from
																			// 1
																			// to
																			// 358
				int dicccol_ID2 = Integer.valueOf(strPair.split("-")[1]); // need
																			// -1
																			// if
																			// dicccol
																			// is
																			// from
																			// 1
																			// to
																			// 358
				pairInfor[pairCount][0] = dicccol_ID1;
				pairInfor[pairCount][1] = dicccol_ID2;
			} //
		}// while
		br_clusterInfo.close();
		in_clusterInfo.close();
		fstream_clusterInfo.close();
		System.out.println("pair count is : " + (++pairCount));

		// begin to write GEXF file
		AbstractMatrix DICCCOL_LOCATION_M = Matrix.fromASCIIFile(new File("./connectome/dicccol_location.txt"));
		List<Node> nodeList = new ArrayList<Node>();
		Gexf gexf = new GexfImpl();
		gexf.setVisualization(true);
		// draw circle
		int nodeCount = 0;
		for (int i = 0; i < 358; i++) {
			Node tmpNode = gexf.getGraph().createNode(String.valueOf(nodeCount++));
			tmpNode.setPosition(new PositionImpl((float) (r * Math.cos((Math.PI / 2) + (i * Math.PI / 179.0))),
					(float) (r * Math.sin((Math.PI / 2) + (i * Math.PI / 179.0))), 0.0f));
			if ((i >= 0) && (i <= 84))
				tmpNode.setColor(new ColorImpl(255, 0, 0));
			if ((i >= 85) && (i <= 124))
				tmpNode.setColor(new ColorImpl(255, 255, 0));
			if ((i >= 125) && (i <= 138))
				tmpNode.setColor(new ColorImpl(0, 0, 255));
			if ((i >= 139) && (i <= 180))
				tmpNode.setColor(new ColorImpl(0, 255, 0));

			if ((i >= 181) && (i <= 225))
				tmpNode.setColor(new ColorImpl(0, 128, 0));
			if ((i >= 226) && (i <= 250))
				tmpNode.setColor(new ColorImpl(30, 144, 255));
			if ((i >= 251) && (i <= 271))
				tmpNode.setColor(new ColorImpl(255, 165, 0));
			if ((i >= 272) && (i <= 357))
				tmpNode.setColor(new ColorImpl(199, 21, 133));
			nodeList.add(tmpNode);
		}
		// draw link
		for (int i = 0; i < pairCount; i++) {
			int loc1 = (int) DICCCOL_LOCATION_M.get(pairInfor[i][0], 1);
			int loc2 = (int) DICCCOL_LOCATION_M.get(pairInfor[i][1], 1);
			nodeList.get(loc1).connectTo(nodeList.get(loc2));
		}

		// draw networks
		this.generateDiccolNetworkMap();
		List<String> networkList = djVtkUtil.loadFileToArrayList("./connectome/networklist.txt");

		int[] color = { 0, 0, 0 };
		for (int i = 0; i < networkList.size(); i++) {
			tmpStringArray = networkList.get(i).split("\\s+");
			String currentNetwork = tmpStringArray[0];
			int c1 = Integer.valueOf(tmpStringArray[1].split(",")[0]);
			int c2 = Integer.valueOf(tmpStringArray[1].split(",")[1]);
			int c3 = Integer.valueOf(tmpStringArray[1].split(",")[2]);
			System.out.println("******Current network is : " + currentNetwork);
			for (int j = 0; j < 358; j++) {
				Node tmpNode = gexf.getGraph().createNode(String.valueOf(nodeCount++));
				tmpNode.setPosition(new PositionImpl((float) ((i * 20 + 20 + r) * Math.cos((Math.PI / 2)
						+ (j * Math.PI / 179.0))), (float) ((i * 20 + 20 + r) * Math.sin((Math.PI / 2)
						+ (j * Math.PI / 179.0))), 0.0f));
				tmpNode.setColor(new ColorImpl(237, 237, 237));
				if (networkDicccolMap.get(currentNetwork).contains(j))
					tmpNode.setColor(new ColorImpl(c1, c2, c3));
			}

			// for(int j=0;j<networkDicccolMap.get(currentNetwork).size();j++)
			// {
			// int dicccolID = networkDicccolMap.get(currentNetwork).get(j);
			// Node tmpNode =
			// gexf.getGraph().createNode(String.valueOf(nodeCount++));
			// tmpNode.setPosition(new PositionImpl((float) ((i*20+20+r) *
			// Math.cos((Math.PI / 2) + (dicccolID * Math.PI / 179.0))),
			// (float) ((i*20+20+r) * Math.sin((Math.PI / 2) + (dicccolID *
			// Math.PI / 179.0))), 0.0f));
			// tmpNode.setColor(new ColorImpl(color[0], color[1], color[2]));
			// }
			// color = this.pickColor(color);
		}

		GexfWriter gw = new StaxGraphWriter();
		File f = new File(saveFileName);
		FileOutputStream fos = new FileOutputStream(f);
		gw.writeToStream(gexf, fos);
		System.out.println("Write file done!");

	}

	public void generateGEXF_forXintao() throws IOException {

		double r = 1000.0;
		String[] tmpStringArray;

		// ////////////////////////////////////////////////////
		String folderDir = "./connectome/xintao/interactom_results_all/SZ/p001/";
		File dir = new File(folderDir);
		String[] files = dir.list();
		for (int f = 0; f < files.length; f++) {
			String filename = files[f];
			if (filename.endsWith("tive.txt")) {
				String cliqueNum = filename.split("_")[2];
				Map<Integer, Set<Integer>> mapLink = new HashMap<Integer, Set<Integer>>();
				List<String> dicccolListOfClique = djVtkUtil.loadFileToArrayList(folderDir + "sz_" + cliqueNum + "_gons.txt");
				List<String> listInCluster = djVtkUtil.loadFileToArrayList(folderDir + filename);

				// begin to write GEXF file
				AbstractMatrix DICCCOL_LOCATION_M = Matrix.fromASCIIFile(new File("./connectome/dicccol_location.txt"));
				List<Node> nodeList = new ArrayList<Node>();
				Gexf gexf = new GexfImpl();
				gexf.setVisualization(true);
				// draw circle
				int nodeCount = 0;
				for (int i = 0; i < 358; i++) {
					Node tmpNode = gexf.getGraph().createNode(String.valueOf(nodeCount++));
					tmpNode.setPosition(new PositionImpl((float) (r * Math.cos((Math.PI / 2) + (i * Math.PI / 179.0))),
							(float) (r * Math.sin((Math.PI / 2) + (i * Math.PI / 179.0))), 0.0f));
					if ((i >= 0) && (i <= 84))
						tmpNode.setColor(new ColorImpl(255, 0, 0));
					if ((i >= 85) && (i <= 124))
						tmpNode.setColor(new ColorImpl(255, 255, 0));
					if ((i >= 125) && (i <= 138))
						tmpNode.setColor(new ColorImpl(0, 0, 255));
					if ((i >= 139) && (i <= 180))
						tmpNode.setColor(new ColorImpl(0, 255, 0));

					if ((i >= 181) && (i <= 225))
						tmpNode.setColor(new ColorImpl(0, 128, 0));
					if ((i >= 226) && (i <= 250))
						tmpNode.setColor(new ColorImpl(30, 144, 255));
					if ((i >= 251) && (i <= 271))
						tmpNode.setColor(new ColorImpl(255, 165, 0));
					if ((i >= 272) && (i <= 357))
						tmpNode.setColor(new ColorImpl(199, 21, 133));
					nodeList.add(tmpNode);
				}
				
				//record all the dicccols
				Set<String> setInvolveDicccols = new HashSet<String>();
				// draw link
				for (int l = 1; l < listInCluster.size(); l++) {
					String sIndex = listInCluster.get(l).split("\\s+")[1].substring(3);
					int nIndex = Integer.valueOf(sIndex);
					String dicccolGroup = dicccolListOfClique.get(nIndex - 1);
					String[] strdicccols = dicccolGroup.split(",");
					int[] ndicccols = new int[strdicccols.length];
					int[] currentColor = new int[3];
					int numOfItems = strdicccols.length;
					
					for (int i = 0; i < numOfItems; i++) {
						ndicccols[i] = Integer.valueOf(strdicccols[i]);
						setInvolveDicccols.add ( String.valueOf(ndicccols[i]) );
						currentColor[0] += nodeList.get(ndicccols[i]-1).getColor().getR();
						currentColor[1] += nodeList.get(ndicccols[i]-1).getColor().getG();
						currentColor[2] += nodeList.get(ndicccols[i]-1).getColor().getB();
					}
					for (int i = 0; i < 3; i++)
						currentColor[i] = currentColor[i] / numOfItems;
					for (int d1 = 0; d1 < numOfItems - 1; d1++)
						for (int d2 = d1 + 1; d2 < numOfItems; d2++) {
							if (mapLink.containsKey(ndicccols[d1] - 1)) {
								if( !mapLink.get(ndicccols[d1] - 1).contains(ndicccols[d2] - 1) )
								{
									mapLink.get(ndicccols[d1] - 1).add(ndicccols[d2] - 1);
									int loc1 = (int) DICCCOL_LOCATION_M.get(ndicccols[d1] - 1, 1);
									int loc2 = (int) DICCCOL_LOCATION_M.get(ndicccols[d2] - 1, 1);
									nodeList.get(loc1).connectTo(nodeList.get(loc2)).setColor(new ColorImpl(currentColor[0], currentColor[1], currentColor[2]));
//									nodeList.get(ndicccols[d1] - 1).connectTo(nodeList.get(ndicccols[d2] - 1))
//									.setColor(new ColorImpl(currentColor[0], currentColor[1], currentColor[2]));
								}
								
							} else {
								Set<Integer> newSet = new HashSet<Integer>();
								newSet.add(ndicccols[d2] - 1);
								mapLink.put(ndicccols[d1] - 1, newSet);
								int loc1 = (int) DICCCOL_LOCATION_M.get(ndicccols[d1] - 1, 1);
								int loc2 = (int) DICCCOL_LOCATION_M.get(ndicccols[d2] - 1, 1);
								nodeList.get(loc1).connectTo(nodeList.get(loc2)).setColor(new ColorImpl(currentColor[0], currentColor[1], currentColor[2]));
//								nodeList.get(ndicccols[d1] - 1).connectTo(nodeList.get(ndicccols[d2] - 1))
//										.setColor(new ColorImpl(currentColor[0], currentColor[1], currentColor[2]));
							}
						} // for d2
				} // for l
//				List<String> listInvolveDicccols = new ArrayList<String>();
//				listInvolveDicccols.addAll(setInvolveDicccols);
//				djVtkUtil.writeArrayListToFile(listInvolveDicccols, folderDir+filename+"_dicccols.txt");

				// draw networks
				this.generateDiccolNetworkMap();
				List<String> networkList = djVtkUtil.loadFileToArrayList("./connectome/networklist.txt");

				int[] color = { 0, 0, 0 };
				for (int i = 0; i < networkList.size(); i++) {
					tmpStringArray = networkList.get(i).split("\\s+");
					String currentNetwork = tmpStringArray[0];
					int c1 = Integer.valueOf(tmpStringArray[1].split(",")[0]);
					int c2 = Integer.valueOf(tmpStringArray[1].split(",")[1]);
					int c3 = Integer.valueOf(tmpStringArray[1].split(",")[2]);
					System.out.println("******Current network is : " + currentNetwork);
					for (int j = 0; j < 358; j++) {
						Node tmpNode = gexf.getGraph().createNode(String.valueOf(nodeCount++));
						tmpNode.setPosition(new PositionImpl((float) ((i * 20 + 20 + r) * Math.cos((Math.PI / 2)
								+ (j * Math.PI / 179.0))), (float) ((i * 20 + 20 + r) * Math.sin((Math.PI / 2)
								+ (j * Math.PI / 179.0))), 0.0f));
						tmpNode.setColor(new ColorImpl(237, 237, 237));
						if (networkDicccolMap.get(currentNetwork).contains(j))
							tmpNode.setColor(new ColorImpl(c1, c2, c3));
					}
				}

				String saveFileName = folderDir + filename + ".gexf";
				GexfWriter gw = new StaxGraphWriter();
				File ff = new File(saveFileName);
				FileOutputStream fos = new FileOutputStream(ff);
				gw.writeToStream(gexf, fos);
				System.out.println("Write file done!");

			} // if
		} // for f

	}

	public int[] pickColor(int[] color) {
		if (color[2] == 0) {
			if ((color[0] == 255) && (color[1] == 255))
				color[2] = color[1] = color[0] = 0;
			else
				color[2] = 255;
		} else {
			color[2] = 0;
			if (color[1] == 0)
				color[1] = 255;
			else {
				color[1] = 0;
				if (color[0] == 0)
					color[0] = 255;
				else
					color[0] = 0;
			}
		}
		return color;
	}

	public void test() {
		// this.generateDiccolNetworkMap();

		System.out.println("this is a test.");
		String ss = "00013456";
		int n = Integer.valueOf(ss);
		System.out.println("ss is : " + ss);
		System.out.println("n is : " + n);

	}
	
	public void visualDICCCOLs()
	{
		String fileName = "./connectome/xintao/results_interactome_MCI/results_interactome/11_gons_positive_cluster.txt_dicccols.txt";
		List<String> dicccolList = djVtkUtil.loadFileToArrayList(fileName);
		List<djVtkPoint> ptList = new ArrayList<djVtkPoint>();
		
		AbstractMatrix DICCCOL_M = Matrix.fromASCIIFile(new File("./connectome/visualization/finalizedMat.txt"));
		djVtkSurData surData = new djVtkSurData("./connectome/10.wm.lobe.vtk");
		for (int i = 0; i < dicccolList.size(); i++) {
			int ptID = (int) DICCCOL_M.get(Integer.valueOf(dicccolList.get(i).trim()), 0);
			ptList.add(surData.getPoint(ptID));
		}
		djVtkUtil.writeToPointsVtkFile(fileName+".vtk", ptList);
		
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		connectomeVisualization mainHandler = new connectomeVisualization();
//		mainHandler.generateGEXF();
		// /////////////////////////////////////////////////////////////////////////
		 String clusterFileName =
		 "./2012MICCAI/connectome/mci_selected_cluter2.txt";
		 mainHandler.analyzeCluster(clusterFileName);
		 String saveFileName = "./2012MICCAI/connectome/mci_selected_cluter2.vtk";
		 mainHandler.writeNetwork(saveFileName);
		// ////////////////////////////////////////////////////////////////////////////
//		mainHandler.generateGEXF_forXintao();
//		mainHandler.visualDICCCOLs();
		// mainHandler.test();

	}

}
