

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import com.ojn.gexf4j.core.Gexf;
import com.ojn.gexf4j.core.GexfWriter;
import com.ojn.gexf4j.core.Node;
import com.ojn.gexf4j.core.impl.GexfImpl;
import com.ojn.gexf4j.core.impl.StaxGraphWriter;
import com.ojn.gexf4j.core.impl.viz.ColorImpl;

import edu.uga.liulab.djVtkBase.djVtkFiberData;
import edu.uga.liulab.djVtkBase.djVtkHybridData;
import edu.uga.liulab.djVtkBase.djVtkPoint;
import edu.uga.liulab.djVtkBase.djVtkSurData;
import edu.uga.liulab.djVtkBase.djVtkUtil;

public class bigPaper {

	private djVtkSurData modelSurData;
	private djVtkSurData currentSurData;
	private djVtkSurData currentOriSurData; //not registered
	private djVtkFiberData currentFiberData;
	private djVtkHybridData hybridData;
	private List<Integer> hammerLabelList;

	private int hammerLabelNum;
	private int dicccolNum;
	
	private List<int[]> hammerColor = new ArrayList<int[]>();
	private List<int[]> dicccolColor = new ArrayList<int[]>();

	List<Integer> findDicccolsfHammerLabels() {
		AbstractMatrix inputM = Matrix.fromASCIIFile(new File("finalizedMat.txt"));
		List<Integer> resultList = new ArrayList<Integer>();
		for (int h = 0; h < this.hammerLabelNum; h++) {
			String hammerLabel = String.valueOf(this.hammerLabelList.get(h));
			System.out.println("current hammer label is : " + hammerLabel);
			for (int pm = 0; pm < this.modelSurData.nPointNum; pm++) {
				String tmpLabel = this.modelSurData.pointsScalarData.get("label").get(pm);
				if (tmpLabel.trim().equals(hammerLabel)) // this point belong to
															// the current
															// hammer label
				{
					for (int d = 0; d < 358; d++) {
						int dicccolPtID = (int) inputM.get(d, 0);
						if (dicccolPtID == pm) // this point is a dicccol
						{
							System.out.println(d);
							resultList.add(d); // dicccol index is from 0
							dicccolColor.add( hammerColor.get(h) );
						}
					} // for d
				} // if
			} // for pm
		} // for h
		this.dicccolNum = resultList.size();
		return resultList;
	}

	Map<Integer, Set<djVtkPoint>> findPtsOfHammerLabels() {
		Map<Integer, Set<djVtkPoint>> resultList = new HashMap<Integer, Set<djVtkPoint>>();
		for (int h = 0; h < this.hammerLabelNum; h++) {
			Set<djVtkPoint> ptsOfCurrentLabel = new HashSet<djVtkPoint>();
			String hammerLabel = String.valueOf(this.hammerLabelList.get(h));
			for (int pm = 0; pm < this.modelSurData.nPointNum; pm++) {
				String tmpLabel = this.modelSurData.pointsScalarData.get("label").get(pm);
				if (tmpLabel.trim().equals(hammerLabel)) // this point belong to
															// the current
															// hammer label
				{
					int tmpPtID = this.currentSurData.findCloestPt(this.modelSurData.getPoint(pm));
					ptsOfCurrentLabel.add(this.currentSurData.getPoint(tmpPtID));
				} // if
			} // for pm
			resultList.put(this.hammerLabelList.get(h), ptsOfCurrentLabel);
		} // for h
		return resultList;
	}

	public double[][] calStructuralConnectivity_Hammer(Map<Integer, Set<djVtkPoint>> resultList) {
		hybridData = new djVtkHybridData(currentSurData, currentFiberData);
		hybridData.mapSurfaceToBox();
		hybridData.mapFiberToBox();

		Set<Integer> setfibers = new HashSet<Integer>();
		Map<Integer, Set<Integer>> fiberMap = new HashMap<Integer, Set<Integer>>();
		double[][] connMatrix = new double[260][260];
		Iterator itResultList = resultList.keySet().iterator();
		while (itResultList.hasNext()) {
			int hammerLabel = (Integer) itResultList.next();
			System.out.println("current hammer label is : " + hammerLabel);
			hybridData.getFibersConnectToPointsSet(resultList.get(hammerLabel));
			int cellSize = hybridData.getFiberData().cellsOutput.size();
			System.out.println("size : " + cellSize);
			for (int i = 0; i < cellSize; i++) {
				int cellID = hybridData.getFiberData().cellsOutput.get(i).cellId;
				setfibers.add(cellID);
				if (fiberMap.containsKey(cellID)) {
					fiberMap.get(cellID).add(hammerLabel);
				} else {
					Set<Integer> labelsConnThisfiber = new HashSet<Integer>();
					labelsConnThisfiber.add(hammerLabel);
					fiberMap.put(cellID, labelsConnThisfiber);
				}
				// System.out.println("cell : "+cellID);
			} // for i
		} // while

		Iterator itNetFibers = setfibers.iterator();
		while (itNetFibers.hasNext()) {
			int fiberID = (Integer) itNetFibers.next();
			List<Integer> labels = new ArrayList<Integer>(fiberMap.get(fiberID));
			for (int i = 0; i < labels.size() - 1; i++)
				for (int j = i + 1; j < labels.size(); j++) {
					connMatrix[labels.get(i)][labels.get(j)] += 1.0;
					connMatrix[labels.get(j)][labels.get(i)] += 1.0;
				}
		} // wihile
		return connMatrix;
	}

	public void doStructuralAnalysis() {
		String filePrefix = "c010";
		this.currentSurData = new djVtkSurData("./data/ptsd/" + filePrefix + ".sur.asc.to10.vtk");
		this.currentFiberData = new djVtkFiberData("./data/ptsd/" + filePrefix + ".fiber.asc.to10.vtk");
		// /////////////////////////
		double[][] hammerConn = this.calStructuralConnectivity_Hammer(this.findPtsOfHammerLabels());
		this.writeArrayToFile(hammerConn, 260, 260, "./experiment1128/hammer." + filePrefix + ".txt");
	}
	
	public void calFunctionalConnectivity_Hammer(Map<Integer, Set<djVtkPoint>> resultList)
	{
		Iterator itResultList = resultList.keySet().iterator();
		while (itResultList.hasNext()) { //for each hammer label
			int hammerLabel = (Integer) itResultList.next();
			System.out.println("current hammer label is : " + hammerLabel);
			Iterator itPtSet = resultList.get(hammerLabel).iterator();
		}
		
	}

	private void writeVoidArray() {
		double[][] voidArray = new double[358][358];
		this.writeArrayToFile(voidArray, 358, 358, "./experiment1128/dicccol.void.txt");
	}

	public void writeArrayToFile(double[][] dataArray, int row, int column, String fileName) {
		List<String> outPutList = new ArrayList<String>();
		for (int r = 0; r < row; r++) {
			String strLine = "";
			for (int c = 0; c < column; c++)
				strLine = strLine + String.valueOf(dataArray[r][c]) + " ";
			outPutList.add(strLine);
		}
		djVtkUtil.writeArrayListToFile(outPutList, fileName);
	}

	public void calAveMatrix() {
		String folderDir = "./experiment1128/";
		AbstractMatrix allDataM_Hammer = Matrix.fromASCIIFile(new File(folderDir + "hammer.void.txt"));
		AbstractMatrix allDataM_Dicccol = Matrix.fromASCIIFile(new File(folderDir + "strucC.dicccol.void.txt"));

		int hammerCount=0;
		int dicccolCount=0;
		File dir = new File(folderDir);
		String[] files = dir.list();
		for (int f = 0; f < files.length; f++) {
			String filename = files[f];
			if (filename.startsWith("hammer")) {
				AbstractMatrix dataM = Matrix.fromASCIIFile(new File(folderDir + filename));
				allDataM_Hammer = allDataM_Hammer.plus(dataM);
				hammerCount++;
			} // if
			if (filename.startsWith("strucC.")) {
				AbstractMatrix dataM = Matrix.fromASCIIFile(new File(folderDir + filename));
				allDataM_Dicccol = allDataM_Dicccol.plus(dataM);
				dicccolCount++;
			} // if
		} // for
		System.out.println("hammerCount:"+hammerCount);
		System.out.println("dicccolCount:"+dicccolCount);
		allDataM_Hammer = allDataM_Hammer.divide(hammerCount);
		allDataM_Dicccol = allDataM_Dicccol.divide(dicccolCount);

		////////////////////////////////////////////////////////////////////////////
		int[][] hammerMatrix = new int[this.hammerLabelList.size()][this.hammerLabelList.size()];
		for (int h1 = 0; h1 < this.hammerLabelList.size() - 1; h1++)
			for (int h2 = h1 + 1; h2 < this.hammerLabelList.size(); h2++)
				hammerMatrix[h1][h2] = (int) allDataM_Hammer.get(this.hammerLabelList.get(h1), this.hammerLabelList.get(h2));
		this.geneGEXF(hammerMatrix, hammerLabelList, "Hammer", folderDir+"visual_hammer_structure_ave.gexf", this.hammerColor, true);
		
		//////////////////////////////////////////////////////////////////////////
		List<Integer> dicccolLabelList = this.findDicccolsfHammerLabels();
		int[][] dicccolMatrix = new int[dicccolLabelList.size()][dicccolLabelList.size()];
		for (int h1 = 0; h1 < dicccolLabelList.size() - 1; h1++)
			for (int h2 = h1 + 1; h2 < dicccolLabelList.size(); h2++)
				dicccolMatrix[h1][h2] = (int) allDataM_Dicccol.get(dicccolLabelList.get(h1), dicccolLabelList.get(h2));
		this.geneGEXF(dicccolMatrix, dicccolLabelList, "Dicccol", folderDir+"visual_dicccol_structure_ave.gexf", this.dicccolColor, false);

	}

	public void geneGEXF(int[][] dataM, List<Integer> labelList, String strPrefix, String fileName, List<int[]> colorList, boolean isHammer) {
		Gexf gexf = new GexfImpl();
		gexf.setVisualization(true);

		Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
		List<Node> nodeList = new ArrayList<Node>();
		for (int i = 0; i < labelList.size(); i++) {
			Node tmpNode = gexf.getGraph().createNode(String.valueOf(labelList.get(i)));
			if(isHammer)
			{
				String strHammerLabel = "";
				if(labelList.get(i)==5)
					strHammerLabel = "RightPrecentralGyrus";
				if(labelList.get(i)==74)
					strHammerLabel = "LeftPostcentralGyrus";
				if(labelList.get(i)==80)
					strHammerLabel = "LeftPrecentralGyrus";
				if(labelList.get(i)==110)
					strHammerLabel = "RightPostcentralGyrus";
				tmpNode.setLabel(strHammerLabel);
			}
			else
			tmpNode.setLabel(strPrefix+"-" + String.valueOf(labelList.get(i)));
			tmpNode.setColor(new ColorImpl(colorList.get(i)[0], colorList.get(i)[1], colorList.get(i)[2]));
			nodeList.add(tmpNode);
		} // for

		for (int h1 = 0; h1 < labelList.size() - 1; h1++)
			for (int h2 = h1 + 1; h2 < labelList.size(); h2++)
				if (dataM[h1][h2] > 0)
					nodeList.get(h1).connectTo(nodeList.get(h2)).setWeight(dataM[h1][h2]);

		GexfWriter gw = new StaxGraphWriter();
		File ff = new File(fileName);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(ff);
			gw.writeToStream(gexf, fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Write file : " + fileName + " done!");

	}

	public bigPaper() {
		this.hammerLabelList = new ArrayList<Integer>();
		//////////////////////////////////////////////////////
		this.hammerLabelList.add(5); // 5, "precentral gyrus right",
//		this.hammerLabelList.add(110); //110, "postcentral gyrus right",
		this.hammerLabelList.add(74); // 74, "postcentral gyrus left"
		this.hammerLabelList.add(80); // 80, "precentral gyrus left",
		this.hammerLabelList.add(110); // 110, "postcentral gyrus right",
		int[] color1 = {255,0,0};
		this.hammerColor.add(color1);
		int[] color2 = {255,255,0};
		this.hammerColor.add(color2);
		int[] color3 = {0,0,255};
		this.hammerColor.add(color3);
		int[] color4 = {0,255,0};
		this.hammerColor.add(color4);
		//////////////////////////////////////////////////////
		
		this.hammerLabelNum = this.hammerLabelList.size();
		this.modelSurData = new djVtkSurData("./experiment1128/10.label.dj.vtk");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		bigPaper mainHandler = new bigPaper();
//		 mainHandler.doStructuralAnalysis();
		mainHandler.calAveMatrix();
		
		
		
//		 mainHandler.writeVoidArray();

	}

}
