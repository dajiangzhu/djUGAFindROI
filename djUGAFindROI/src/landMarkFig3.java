

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.uga.liulab.djVtkBase.djVtkUtil;

public class landMarkFig3 {

	public void anaFinalAllInOneFile() {
//		String networkName = "auditory";
		int roiNum = 14;
		
		String subDes = "05051102";
		int subIndex =5 ;

		List<String> activationPtID = new ArrayList<String>();
		
		try {
			FileInputStream fstream_roiGroupInfo = new FileInputStream("./fig3/0612/allInOneFinal.final.0610.txt");
			DataInputStream in_roiGroupInfo = new DataInputStream(fstream_roiGroupInfo);
			BufferedReader br_roiGroupInfo = new BufferedReader(new InputStreamReader(in_roiGroupInfo));
			String strLine;
			String[] tmpStringArray;
			while ((strLine = br_roiGroupInfo.readLine()) != null) {
				tmpStringArray = strLine.split("\\s+");
				if (strLine.startsWith("#network fear")) {
					for(int i=0;i<roiNum;i++)
					{
						strLine = br_roiGroupInfo.readLine();
						tmpStringArray = strLine.split("\\s+");
						String labelID = tmpStringArray[0];
						List<String> tmpInfoList = djVtkUtil.loadFileToArrayList("./fig3/0612/disFiles/fear/disOnSurf.fear.roi."+labelID);
						activationPtID.add( tmpInfoList.get(subIndex).split("\\s+")[0] );
					}
					djVtkUtil.writeArrayListToFile(activationPtID, "./fig3/0612/supplement/fear/ActivationFear."+subDes+".txt");
				} // new network
			}// while
			br_roiGroupInfo.close();
			in_roiGroupInfo.close();
			fstream_roiGroupInfo.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public void geneDisDistribution()
	{
		int roiNum = 9;
		int subNum = 10;
		List<String> disDistributionList = new ArrayList<String>();
		try {
			FileInputStream fstream_roiGroupInfo = new FileInputStream("./fig3/0612/allInOneFinal.final.0610.txt"); //need modify
			DataInputStream in_roiGroupInfo = new DataInputStream(fstream_roiGroupInfo);
			BufferedReader br_roiGroupInfo = new BufferedReader(new InputStreamReader(in_roiGroupInfo));
			String strLine;
			String[] tmpStringArray;
			while ((strLine = br_roiGroupInfo.readLine()) != null) {
				tmpStringArray = strLine.split("\\s+");
				if (strLine.startsWith("#network visual")) {
					for(int i=0;i<roiNum;i++)
					{
						strLine = br_roiGroupInfo.readLine();
						tmpStringArray = strLine.split("\\s+");
						String labelID = tmpStringArray[0];
						List<String> tmpInfoList = djVtkUtil.loadFileToArrayList("./fig3/0612/disFiles/visual/disOnSurf.child.visual.roi."+labelID);
						String disOfCurrentRoi="";
						for(int j=0;j<subNum;j++)
							disOfCurrentRoi = disOfCurrentRoi + tmpInfoList.get(j).split("\\s+")[1] + " ";
						disDistributionList.add( disOfCurrentRoi );
					}
					djVtkUtil.writeArrayListToFile(disDistributionList, "./fig3/0612/DisDistributionVisual.txt");
				} // new network
			}// while
			br_roiGroupInfo.close();
			in_roiGroupInfo.close();
			fstream_roiGroupInfo.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		landMarkFig3 mainHandler =  new landMarkFig3();
		mainHandler.anaFinalAllInOneFile();
//		mainHandler.geneDisDistribution();

	}

}
