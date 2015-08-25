

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import edu.uga.liulab.djVtkBase.djVtkUtil;

public class Miccai2012_test {

	public void testDMN() {
		List<Integer> idList = new ArrayList<Integer>();
		idList.add(45);
		idList.add(72);
		idList.add(76);
		idList.add(79);
		idList.add(144);
		idList.add(155);
		idList.add(298);
		idList.add(326);
		double[][] conn = new double[8][8];

		int count = 0;
		String folderDir = "./connectome/ptsd/adolescent_normal_controls/";
		File dir = new File(folderDir);
		String[] files = dir.list();
		for (int f = 0; f < files.length; f++) {
			AbstractMatrix tmpMatrix = Matrix.fromASCIIFile(new File(folderDir + files[f]));
			count++;
			for (int i = 0; i < idList.size(); i++) {
				for (int j = 0; j < idList.size(); j++)
					conn[i][j] = conn[i][j] + tmpMatrix.get(idList.get(i), idList.get(j));
			} // for i
		} // for f
		for (int i = 0; i < idList.size(); i++)
			for (int j = 0; j < idList.size(); j++)
				conn[i][j] = conn[i][j] / count;

		findModelUtil.writeVtkMatrix(conn, 8, 8, "./2012MICCAI/dmn_conn_ptsd_adolescent_normals.vtk");
	}

	public void printStructuralInfor() {
		AbstractMatrix tmpMatrix = Matrix.fromASCIIFile(new File("./2012MICCAI/All_StructuralConn.txt"));
		for (int i = 0; i < 7; i++)
			for (int j = i + 1; j < 8; j++)
				System.out.println(tmpMatrix.get(i, j));

	}

	public void generateRandom() {
		int numbersNeeded = 10;
		int max = 10;
		Random rng = new Random(); // Ideally just create one instance globally
		List<Integer> generated = new ArrayList<Integer>();
		for (int i = 0; i < numbersNeeded; i++) {
			while (true) {
				Integer next = rng.nextInt(max) + 1;
				if (!generated.contains(next)) {
					// Done for this iteration
					generated.add(next);
					System.out.println(next);
					break;
				}
			}
		}
	}
	
	public void generateCorrData()
	{
		List<String> outputDataList = new ArrayList<String>();
		String folderDir = "./2012MICCAI/mci/MCI_signals/patient/";
		File dir = new File(folderDir);
		String[] files = dir.list();
		for (int f = 0; f < files.length; f++) {
			if(files[f].endsWith("corr.txt"))
			{
				System.out.println("Dealing with file: "+files[f]);
				String addLineData = "";
				List<String> currentDataList = djVtkUtil.loadFileToArrayList( folderDir+files[f]);
				for(int l=0;l<currentDataList.size()-1;l++)
				{
					String[] currentLine = currentDataList.get(l).split(",");
					for(int k=l+1;k<currentLine.length;k++)
						addLineData = addLineData+currentLine[k]+",";
				}
				outputDataList.add(addLineData);
			} //if
		} //for f
		djVtkUtil.writeArrayListToFile(outputDataList, "./2012MICCAI/patient_corre.txt");
		
	}

	public static void main(String[] args) {
		Miccai2012_test mainHandler = new Miccai2012_test();
		// mainHandler.printStructuralInfor();
		mainHandler.generateCorrData();

	}

}
