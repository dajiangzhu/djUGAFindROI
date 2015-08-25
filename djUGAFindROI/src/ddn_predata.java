

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.uga.liulab.djVtkBase.djVtkUtil;

public class ddn_predata {
	public Set<Integer> dicccolSet = new HashSet<Integer>();

	public void anaNetworkComposition() throws IOException {

		List<String> dicccolNetworkDictionary = landMarkUtil
				.loadFileToArrayList("./connectome/DICCCOL_Networks_1120.txt"); // from
																				// 1
																				// to
																				// 358
		Map<String, List<Integer>> netWorkInvolve = new HashMap<String, List<Integer>>();
		for (int dicccol = 1; dicccol < 359; dicccol++) {
			String str = dicccolNetworkDictionary.get(dicccol - 1);
			String[] arStr = str.split("\\s+");
			// analyze network
			if ((arStr.length > 2) && (Integer.valueOf(arStr[0].trim()) == dicccol)) {
				String[] arStrSub = arStr[2].split("::");
				for (int i = 0; i < arStrSub.length; i++) {
					String netWorkName = arStrSub[i].trim();
					if (netWorkName.length() > 0)
						if (netWorkInvolve.containsKey(netWorkName))
							netWorkInvolve.get(netWorkName).add(dicccol);
						else {
							List<Integer> tmpList = new ArrayList<Integer>();
							tmpList.add(dicccol);
							netWorkInvolve.put(netWorkName, tmpList);
						} // else
				} // for i
			} // if
		} // for

		System.out.println("RESULT**********************");
		String strNetwork = "";
		Iterator it = netWorkInvolve.keySet().iterator();
		while (it.hasNext()) {
			String networkName = (String) it.next();
			strNetwork = strNetwork + networkName + " ";
			int dicccolCount = netWorkInvolve.get(networkName).size();
			System.out.println(networkName + " : " + dicccolCount + "...");
			// for(int i=0;i<dicccolCount;i++)
			// System.out.println("dicccol:"+netWorkInvolve.get(networkName).get(i));
			// create the directory of the current network
			new File("./connectome/networkSignature/mci/" + networkName).mkdir();
			// deal with all files in this directory
			String folderDir = "./connectome/networkSignature/mci/";
			File dir = new File(folderDir);
			String[] files = dir.list();
			for (int f = 0; f < files.length; f++) {
				String filename = files[f];
				if (filename.endsWith("singal.txt")) {
					System.out.println(filename);
					List<String> orginalSig = djVtkUtil.loadFileToArrayList(folderDir + filename);
					List<String> extractedSigData = new ArrayList<String>();
					List<String> extractedSigName = new ArrayList<String>();
					for (int i = 0; i < netWorkInvolve.get(networkName).size(); i++) {
						int tmpDicccolID = netWorkInvolve.get(networkName).get(i);
						extractedSigName.add(String.valueOf("Dicccol-" + tmpDicccolID));
						extractedSigData.add(orginalSig.get(tmpDicccolID - 1));
					} // for i
					djVtkUtil.writeArrayListToFile(extractedSigName, "./connectome/networkSignature/mci/" + networkName
							+ "/" + filename + ".signame.txt");
					djVtkUtil.writeArrayListToFile(this.changeFormat(extractedSigData),
							"./connectome/networkSignature/mci/" + networkName + "/" + filename + ".sigdata.txt");
				} // if
			} // for f
		}
	}

	public void checkPairInNetwork() throws IOException {
		// analyze the composition of networks
		List<String> dicccolNetworkDictionary = landMarkUtil
				.loadFileToArrayList("./connectome/DICCCOL_Networks_1120.txt"); // from
																				// 1
																				// to
																				// 358
		Map<String, List<Integer>> netWorkInvolve = new HashMap<String, List<Integer>>();
		for (int dicccol = 1; dicccol < 359; dicccol++) {
			String str = dicccolNetworkDictionary.get(dicccol - 1);
			String[] arStr = str.split("\\s+");
			// analyze network
			if ((arStr.length > 2) && (Integer.valueOf(arStr[0].trim()) == dicccol)) {
				String[] arStrSub = arStr[2].split("::");
				for (int i = 0; i < arStrSub.length; i++) {
					String netWorkName = arStrSub[i].trim();
					if (netWorkName.length() > 0)
						if (netWorkInvolve.containsKey(netWorkName))
							netWorkInvolve.get(netWorkName).add(dicccol);
						else {
							List<Integer> tmpList = new ArrayList<Integer>();
							tmpList.add(dicccol);
							netWorkInvolve.put(netWorkName, tmpList);
						} // else
				} // for i
			} // if
		} // for

		// analyze cluster file which contains pair information
		int[][] pairInfor = new int[900][2];
		int pairCount = -1;
		String clusterName = "cluster2.txt";
		String clusterFileName = "./connectome/mci/analysis_" + clusterName;
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
				// need -1 if dicccol is from 1 to 358
				int dicccol_ID1 = Integer.valueOf(strPair.split("-")[0]);
				int dicccol_ID2 = Integer.valueOf(strPair.split("-")[1]);
				pairInfor[pairCount][0] = dicccol_ID1 + 1;
				pairInfor[pairCount][1] = dicccol_ID2 + 1;
			} //
		}// while
		br_clusterInfo.close();
		in_clusterInfo.close();
		fstream_clusterInfo.close();
		System.out.println("pair count is : " + (++pairCount));

		String strNetwork = "";
		Iterator it = netWorkInvolve.keySet().iterator();
		while (it.hasNext()) {
			String networkName = (String) it.next();
			Set<String> dicccolInvolved = new HashSet<String>();
			int dicccolCount = netWorkInvolve.get(networkName).size();
			System.out.println(networkName + " : " + dicccolCount + "...");
			for (int i = 0; i < pairCount; i++) {
				if ((netWorkInvolve.get(networkName).contains(pairInfor[i][0]))
						&& (netWorkInvolve.get(networkName).contains(pairInfor[i][1]))) {
					dicccolInvolved.add(String.valueOf(pairInfor[i][0]));
					dicccolInvolved.add(String.valueOf(pairInfor[i][1]));
					System.out.println(pairInfor[i][0] + "-" + pairInfor[i][1]);
				}
			}
			List<String> dataList = new ArrayList<String>(dicccolInvolved);
			djVtkUtil.writeArrayListToFile(dataList, "./connectome/networkSignature/mci/" + networkName + "/"
					+ clusterName);

		} // while

	}

	public List<String> changeFormat(List<String> dataList) {
		List<String> resultList = new ArrayList<String>();
		for (int i = 0; i < dataList.size(); i++) {
			String currentLine = dataList.get(i);
			String changedLine = "";
			String[] items = currentLine.split("\\s+");
			for (int j = 0; j < items.length; j++)
				changedLine = changedLine + items[j].trim() + ",";
			changedLine = changedLine.substring(0, changedLine.length() - 1);
			resultList.add(changedLine);
		}
		return resultList;
	}

	public void analyzeDDNNetworks() {
		int[][] redEdges = new int[358][358]; // only exist in control
		int[][] greenEdges = new int[358][358]; // only exist in patient
		String folderDir = "./connectome/networkSignature/mci_networks/attention/";
		// ///////////////////////////////////////////////////////////////
//		List<String> cluster1List = djVtkUtil
//				.loadFileToArrayList("./connectome/networkSignature/mci/attention/cluster1.txt");
//		List<String> cluster2List = djVtkUtil
//				.loadFileToArrayList("./connectome/networkSignature/mci/attention/cluster2.txt");
//		Set<String> clusterSet = new HashSet<String>(cluster1List);
//		clusterSet.addAll(cluster2List);
		// ///////////////////////////////////////////////////////////////
		File dir = new File(folderDir);
		String[] files = dir.list();
		for (int f = 0; f < files.length; f++) {
			String filename = files[f];
			if (filename.endsWith(".sif")) {
				System.out.println(filename);
				List<String> currentSif = djVtkUtil.loadFileToArrayList(folderDir + filename);
				for (int i = 0; i < currentSif.size(); i++) {
					String[] strItems = currentSif.get(i).split("\\s+");
					int diccol1 = Integer.valueOf(strItems[0].split("-")[1]); // dicccol
																				// index
																				// is
																				// from
																				// 1
																				// to
																				// 358
					int diccol2 = Integer.valueOf(strItems[2].split("-")[1]);// dicccol
																				// index
																				// is
																				// from
																				// 1
																				// to
																				// 358
					// dicccol1 always less than dicccol2
					// add constrain for network
//					if ((clusterSet.contains(String.valueOf(diccol1)))
//							&& (clusterSet.contains(String.valueOf(diccol2)))) {
						int edgeType = Integer.valueOf(strItems[1]);
						if (edgeType == 1) // red
							redEdges[diccol1 - 1][diccol2 - 1]++;
						else
							// green
							greenEdges[diccol1 - 1][diccol2 - 1]++;
//					}
				} // for i
			} // if
		} // for f

		// /////////////////////////////////////////////
		// int thresholdRed = 50;
		// int thresholdGreen = 50;
		// System.out.println("redEdges:##########################################");
		// for(int i=0;i<357;i++)
		// for(int j=i+1;j<358;j++)
		// if(redEdges[i][j]>=thresholdRed)
		// System.out.println("pair: "+i+" - "+j);
		// System.out.println("greenEdges:##########################################");
		// for(int i=0;i<357;i++)
		// for(int j=i+1;j<358;j++)
		// if(greenEdges[i][j]>=thresholdGreen)
		// System.out.println("pair: "+i+" - "+j);

		// ////////////////////////////////////////////////
		ddn_visualization visualizationHandler;
		int[][] visuaMatrix = new int[358][358];
		Set<String> networkSet = new HashSet<String>();
		int topRed = 10;
		int topGreen = 10;
		System.out.println("redEdges:##########################################");
		for (int n = 0; n < topRed; n++) {
			int tmpMax = 0;
			int tmpI = -1;
			int tmpJ = -1;
			for (int i = 0; i < 357; i++)
				for (int j = i + 1; j < 358; j++)
					if (redEdges[i][j] >= tmpMax) {
						tmpMax = redEdges[i][j];
						tmpI = i;
						tmpJ = j;
					}
			System.out.println("pair: " + tmpI + " - " + tmpJ + " : " + tmpMax);
			if (tmpMax == 0)
				break;
			networkSet.add(String.valueOf(tmpI));
			networkSet.add(String.valueOf(tmpJ));
			redEdges[tmpI][tmpJ] = -1;
			visuaMatrix[tmpI][tmpJ] = tmpMax;
		}
		visualizationHandler = new ddn_visualization(visuaMatrix);
		int[] edgeColorRed = { 255, 0, 0 };
		visualizationHandler.setEdgeColor(edgeColorRed);
		visualizationHandler.matrixVisualization("./connectome/networkSignature/mci_networks/redNetworks_Control_10.gexf");

		visuaMatrix = new int[358][358];
		System.out.println("greenEdges:##########################################");
		for (int n = 0; n < topGreen; n++) {
			int tmpMax = 0;
			int tmpI = -1;
			int tmpJ = -1;
			for (int i = 0; i < 357; i++)
				for (int j = i + 1; j < 358; j++)
					if (greenEdges[i][j] >= tmpMax) {
						tmpMax = greenEdges[i][j];
						tmpI = i;
						tmpJ = j;
					}
			System.out.println("pair: " + tmpI + " - " + tmpJ + " : " + tmpMax);
			if (tmpMax == 0)
				break;
			networkSet.add(String.valueOf(tmpI));
			networkSet.add(String.valueOf(tmpJ));
			greenEdges[tmpI][tmpJ] = -1;
			visuaMatrix[tmpI][tmpJ] = tmpMax;
		}
		visualizationHandler = new ddn_visualization(visuaMatrix);
		int[] edgeColorGreen = { 0, 255, 0 };
		visualizationHandler.setEdgeColor(edgeColorGreen);
		visualizationHandler
				.matrixVisualization("./connectome/networkSignature/mci_networks/greenNetworks_Patient_10.gexf");
		List<String> dataList = new ArrayList<String>(networkSet);
		djVtkUtil.writeArrayListToFile(dataList, "./connectome/networkSignature/mci_networks/attention/top10.txt");

	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ddn_predata mainHandler = new ddn_predata();
		// mainHandler.anaNetworkComposition();
		 mainHandler.analyzeDDNNetworks();
//		mainHandler.checkPairInNetwork();

	}

}
