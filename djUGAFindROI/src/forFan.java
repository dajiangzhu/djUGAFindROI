

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uga.liulab.djVtkBase.djVtkFiberData;
import edu.uga.liulab.djVtkBase.djVtkHybridData;
import edu.uga.liulab.djVtkBase.djVtkPoint;
import edu.uga.liulab.djVtkBase.djVtkSurData;
import edu.uga.liulab.djVtkBase.djVtkUtil;

public class forFan {

	public int getIndex(List<List<List<Integer>>> fiberIndexMap, int r, int c) {
		int result = 0;
		if (r == 0)
			result = c;
		else
			for (int i = 0; i <= r; i++) {
				if (i < r)
					result = result + fiberIndexMap.get(i).size();
				else
					result = result + c;
			}
		return result;
	}

	public void calStruCon() {
		String fileLandmark = "./forFan/subHARDI_LR.txt";
		String surFile = "./forFan/surf_006.vtk";
		String fiberFile = "./forFan/fiber_006.vtk";

		List<String> landmarkInfo = djVtkUtil.loadFileToArrayList(fileLandmark);

		djVtkSurData currentSurData = new djVtkSurData(surFile);
		djVtkFiberData currentFiberData = new djVtkFiberData(fiberFile);
		djVtkHybridData hybridData = new djVtkHybridData(currentSurData, currentFiberData);
		hybridData.mapSurfaceToBox();
		hybridData.mapFiberToBox();

		List<List<List<Integer>>> fiberIndexMap = new ArrayList<List<List<Integer>>>();
		List<djVtkPoint> landmarkList = new ArrayList<djVtkPoint>();

		for (int i = 0; i < 2; i++) {
			List<List<Integer>> currentGyriMap = new ArrayList<List<Integer>>();
			String[] tmpLine = landmarkInfo.get(i).split("\\s+");
			for (int j = 1; j < tmpLine.length; j++) {
				System.out.println(tmpLine[0] + " : ");
				List<Integer> currentLandmarkSet = new ArrayList<Integer>();
				int ptID = Integer.valueOf(tmpLine[j]);
				System.out.println("ptID=" + ptID);
				landmarkList.add(currentSurData.getPoint(ptID));
				hybridData.getFibersConnectToPointsSet(currentSurData.getNeighbourPoints(ptID, 3));
						//.writeToVtkFileCompact("./forFan/" + ptID + "fiber_hardi.vtk");
				System.out.println("fiber size=" + currentFiberData.cellsOutput.size());

				for (int k = 0; k < currentFiberData.cellsOutput.size(); k++)
					currentLandmarkSet.add(currentFiberData.cellsOutput.get(k).cellId);
				currentGyriMap.add(currentLandmarkSet);
				currentFiberData.cellsOutput.clear();
			}
			fiberIndexMap.add(currentGyriMap);
		} // for all gyri

		List<String> lineList = new ArrayList<String>();
		List<String> attriList = new ArrayList<String>();
		for (int i = 0; i < fiberIndexMap.size() - 1; i++) {
			for (int j = 0; j < fiberIndexMap.get(i).size(); j++) {
				for (int m = i + 1; m < fiberIndexMap.size(); m++) {
					for (int n = 0; n < fiberIndexMap.get(m).size(); n++) {
						int count = 0;
						for (int k = 0; k < fiberIndexMap.get(i).get(j).size(); k++)
							if (fiberIndexMap.get(m).get(n).contains(fiberIndexMap.get(i).get(j).get(k)))
								count++;
						if (count > 0) {
							lineList.add("2 " + this.getIndex(fiberIndexMap, i, j) + " "
									+ this.getIndex(fiberIndexMap, m, n));
							attriList.add(String.valueOf(count));
						}
						// System.out.println("i="+i+"  j="+j+"m="+m+"  n="+n+"  count="+count);
					} // for n
				} // for m
			} // for j
		} // for i

		djVtkUtil.writeToPointsVtkFile("./forFan/strucon_R_POG-CS_hardi.vtk", landmarkList);
		djVtkUtil.writeArrayListToFile(lineList, "./forFan/lines_R_POG-CS_hardi.txt");
		djVtkUtil.writeArrayListToFile(attriList, "./forFan/attri_R_POG-CS_hardi.txt");
	}

	public void generateLandmarks() {
		String fileLandmark = "./forFan/subDSI.txt";
		String surFile = "./forFan/surf.smooth.flip.vtk.ply.vtk";

		List<String> landmarkInfo = djVtkUtil.loadFileToArrayList(fileLandmark);
		djVtkSurData currentSurData = new djVtkSurData(surFile);

		List<djVtkPoint> landmarkList = new ArrayList<djVtkPoint>();
		
		String[] tmpLine = landmarkInfo.get(5).split("\\s+");
		for (int j = 1; j < tmpLine.length; j++) {
			System.out.println(tmpLine[0] + " : ");
			int ptID = Integer.valueOf(tmpLine[j]);
			System.out.println("ptID=" + ptID);
			landmarkList.add(currentSurData.getPoint(ptID));
		}
		tmpLine = landmarkInfo.get(7).split("\\s+");
		for (int j = 1; j < tmpLine.length; j++) {
			System.out.println(tmpLine[0] + " : ");
			int ptID = Integer.valueOf(tmpLine[j]);
			System.out.println("ptID=" + ptID);
			landmarkList.add(currentSurData.getPoint(ptID));
		}
		
		djVtkUtil.writeToPointsVtkFile("./forFan/landmark_POS_dsi.vtk", landmarkList);
	}
	
	public Set<djVtkPoint> getPtsOfTmp(djVtkSurData currentSurData)
	{
		Set<djVtkPoint> ptSet = new HashSet<djVtkPoint>();
		List<String> landmarkInfo = djVtkUtil.loadFileToArrayList("./forFan/hardi_tmp_pt_R.txt");
		
		for(int i=0;i<2;i++)
		{
			String[] tmpLine = landmarkInfo.get(i).split("\\s+");
			for (int j = 0; j < tmpLine.length; j++) {
				System.out.println(tmpLine[j] + " : ");
				int ptID = Integer.valueOf(tmpLine[j]);
				System.out.println("ptID=" + ptID);
				ptSet.addAll(currentSurData.getNeighbourPoints(ptID, 3));
			} //for j
		} //for i
		return ptSet;
	}
	
	public void getFibers()
	{
		String surFile = "./forFan/surf_006.vtk";
		String fiberFile = "./forFan/fiber_006.vtk";
		djVtkSurData currentSurData = new djVtkSurData(surFile);
		Set<djVtkPoint> ptSet=this.getPtsOfTmp(currentSurData);
		djVtkUtil.writeToPointsVtkFile("./forFan/RSTG_hardi.vtk", new ArrayList<djVtkPoint>(ptSet));
		
		djVtkFiberData currentFiberData = new djVtkFiberData(fiberFile);
		djVtkHybridData hybridData = new djVtkHybridData(currentSurData, currentFiberData);
		hybridData.mapSurfaceToBox();
		hybridData.mapFiberToBox();
//		Set<djVtkPoint> ptSet = new HashSet<djVtkPoint>(currentSurData.points);
		
		hybridData.getFibersConnectToPointsSet(ptSet).writeToVtkFileCompact("./forFan/RSTG_fiber_hardi.vtk");
	}
	
	public void getNeighbourPts()
	{
		String surFile = "./forFan/RS.sub1.surf.vtk";
		djVtkSurData currentSurData = new djVtkSurData(surFile);
		List<djVtkPoint> ptList = new ArrayList<djVtkPoint>( currentSurData.getNeighbourPoints(16205, 3) );
		for(int i=0;i<ptList.size();i++)
		{
			if(ptList.get(i).pointId==16205)
				ptList.remove(i);
		}
		djVtkUtil.writeToPointsVtkFile("./forFan/dti.sub1.16205.ring3.pts.vtk",ptList );
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		forFan mainHandler = new forFan();
//		mainHandler.calStruCon();
//		mainHandler.generateLandmarks();
//		mainHandler.getFibers();
		mainHandler.getNeighbourPts();

	}

}
