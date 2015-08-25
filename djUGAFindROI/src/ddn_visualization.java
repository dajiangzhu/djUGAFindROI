

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ojn.gexf4j.core.Gexf;
import com.ojn.gexf4j.core.GexfWriter;
import com.ojn.gexf4j.core.Node;
import com.ojn.gexf4j.core.impl.GexfImpl;
import com.ojn.gexf4j.core.impl.StaxGraphWriter;
import com.ojn.gexf4j.core.impl.viz.ColorImpl;

public class ddn_visualization {
	private int[][] theMatrix;
	private int row = 358;
	private int column = 358;
	private int[] nodeColor;
	private int[] edgeColor;

	public int[] getNodeColor() {
		return nodeColor;
	}

	public void setNodeColor(int[] nodeColor) {
		this.nodeColor = nodeColor;
	}

	public int[] getEdgeColor() {
		return edgeColor;
	}

	public void setEdgeColor(int[] edgeColor) {
		this.edgeColor = edgeColor;
	}

	public ddn_visualization(int[][] theMatrix) {
		this.theMatrix = theMatrix;
		this.nodeColor = new int[3];
		this.edgeColor = new int[3];
	}

	public void matrixVisualization(String fileName) {

		Gexf gexf = new GexfImpl();
		gexf.setVisualization(true);

		Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
		for (int i = 0; i < row - 1; i++)
			for (int j = i + 1; j < column; j++) {
				if (this.theMatrix[i][j] > 0) // this edge is a candidate
				{
					// add the two nodes connected by this edge to the node set
					if (!nodeMap.containsKey(i)) {
						Node tmpNode = gexf.getGraph().createNode(String.valueOf(i));
						tmpNode.setLabel("DICCCOL-" + (i + 1));
						tmpNode.setColor(new ColorImpl(nodeColor[0], nodeColor[1], nodeColor[2]));
						nodeMap.put(i, tmpNode);
					}
					if (!nodeMap.containsKey(j)) {
						Node tmpNode = gexf.getGraph().createNode(String.valueOf(j));
						tmpNode.setLabel("DICCCOL-" + (j + 1));
						tmpNode.setColor(new ColorImpl(nodeColor[0], nodeColor[1], nodeColor[1]));
						nodeMap.put(j, tmpNode);
					}
					nodeMap.get(i).connectTo(nodeMap.get(j)).setWeight(this.theMatrix[i][j])
							.setColor(new ColorImpl(edgeColor[0], edgeColor[1], edgeColor[2]));
				}
			}

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

}
