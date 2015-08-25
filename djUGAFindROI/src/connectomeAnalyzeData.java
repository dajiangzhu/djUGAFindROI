
import java.io.BufferedReader;
import java.io.DataInputStream;
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

public class connectomeAnalyzeData {
	public Set<Integer> dicccolSet = new HashSet<Integer>();

	public void anaNetworkComposition() throws IOException {
		String clusterFile = "./2012MICCAI/connectome/mci_selected_cluter2.txt";
		List<String> dicccolNetworkDictionary = landMarkUtil.loadFileToArrayList("./connectome/DICCCOL_Networks_1120.txt"); //from 1 to 358
		List<String> dicccolLobeDictionary = landMarkUtil.loadFileToArrayList("./connectome/dicccol_lobe.txt"); //from 1 to 358

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
				String strPair = tmpStringArray[1];
				System.out.println("analyzing pair:" + strPair);
				int dicccol_ID1 = Integer.valueOf(strPair.split("-")[0]); //need -1 if dicccol is from 1 to 358
				int dicccol_ID2 = Integer.valueOf(strPair.split("-")[1]); //need -1 if dicccol is from 1 to 358
				dicccolSet.add(dicccol_ID1);
				dicccolSet.add(dicccol_ID2);

			} //
		}// while
		br_clusterInfo.close();
		in_clusterInfo.close();
		fstream_clusterInfo.close();
		System.out.println("There are "+pairCount+" pairs.  The size of this cluster is : "+dicccolSet.size());

		Map<String, List<Integer>> netWorkInvolve = new HashMap<String, List<Integer>>();
		Map<Integer, List<Integer>> lobeInvolve = new HashMap<Integer, List<Integer>>();
		Iterator it = dicccolSet.iterator();
		while (it.hasNext()) {
			int dicccol = (Integer) it.next();
			String str = dicccolNetworkDictionary.get(dicccol);
			String str1 = dicccolLobeDictionary.get(dicccol);
			String[] arStr = str.split("\\s+");
			String[] arStr1 = str1.split("\\s+");
			//analyze network
			if ( (arStr.length>2)&&(Integer.valueOf(arStr[0].trim()) == (dicccol+1)) ) {
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
						} //else
				} //for i
			} //if
			//analyze lobe
			int lobeID = Integer.valueOf(arStr1[1].trim());
			if( Integer.valueOf(arStr1[0].trim()) == dicccol )
			{
				if (lobeInvolve.containsKey(lobeID))
					lobeInvolve.get(lobeID).add(dicccol);
				else {
					List<Integer> tmpList = new ArrayList<Integer>();
					tmpList.add(dicccol);
					lobeInvolve.put(lobeID, tmpList);
				} //else
			} //if
		} //while
		
		System.out.println("RESULT**********************");
		List<String> listToWrite = new ArrayList<String>();
		String strCount = "";
		String strNetwork = "";
		it = netWorkInvolve.keySet().iterator();
		while(it.hasNext())
		{
			String networkName = (String)it.next();
			strNetwork = strNetwork + networkName + " ";
			int dicccolCount = netWorkInvolve.get(networkName).size();
			strCount = strCount + dicccolCount + " ";
			System.out.println(networkName+" : "+dicccolCount);
		}
		listToWrite.add( strCount );
		listToWrite.add( strNetwork );
		djVtkUtil.writeArrayListToFile(listToWrite, clusterFile+".networkInvolve.txt");
		
		listToWrite.clear();
		String strLineToWrite = "";
		
		strLineToWrite ="Left Frontal : "+lobeInvolve.get(30).size();
		listToWrite.add(strLineToWrite);
		strLineToWrite ="Right Frontal : "+lobeInvolve.get(17).size();
		listToWrite.add(strLineToWrite);
		strLineToWrite ="Left Parietal : "+lobeInvolve.get(57).size();
		listToWrite.add(strLineToWrite);
		strLineToWrite ="Right Parietal : "+lobeInvolve.get(105).size();
		listToWrite.add(strLineToWrite);
		strLineToWrite ="Left Occipital : "+lobeInvolve.get(73).size();
		listToWrite.add(strLineToWrite);
		strLineToWrite ="Right Occipital : "+lobeInvolve.get(45).size();
		listToWrite.add(strLineToWrite);
		strLineToWrite ="Left Temporal : "+lobeInvolve.get(83).size();
		listToWrite.add(strLineToWrite);
		strLineToWrite ="Right Temporal : "+lobeInvolve.get(59).size();
		listToWrite.add(strLineToWrite);
		djVtkUtil.writeArrayListToFile(listToWrite, clusterFile+".lobeInvolve.txt");

	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		connectomeAnalyzeData mainHandler = new connectomeAnalyzeData();
		mainHandler.anaNetworkComposition();

	}

}
