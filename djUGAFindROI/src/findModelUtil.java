

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uga.liulab.djVtkBase.djVtkUtil;

public class findModelUtil {
	public static void writeArrayListToFile(double[][] dataArray, int dimRow, int dimColumn, String fileName) {
		try {
			System.out.println("Begin to write file:" + fileName + "...");
			FileWriter fw = null;
			fw = new FileWriter(fileName);
			for (int i = 0; i < dimRow; i++) {
				for (int j = 0; j < dimColumn; j++)
					fw.write(dataArray[i][j] + " ");
				fw.write("\r\n");
			}
			fw.close();
			System.out.println("Write file " + fileName + " done!");
		} catch (IOException ex) {
			Logger.getLogger(djVtkUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static void writeVtkMatrix(double[][] mat, int dimRow, int dimColumn, String fileName)
	{
		double blockwidth=4.0;
		double blockheight=4.0;
		double interval=0.0;
		int numpoint=4*dimRow*dimColumn;
		int numcell = dimRow*dimColumn;
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName);
			fw.write("# vtk DataFile Version 3.0 \r\n");
			fw.write("vtk output \r\n");
			fw.write("ASCII \r\n");
			fw.write("DATASET POLYDATA \r\n");
			fw.write("POINTS "+numpoint+" float \r\n");
			
			for (int x = 0; x < dimRow; x++) {
				for (int y = 0; y < dimColumn; y++) {
					double rx, ry;
					rx = (blockwidth+interval)*dimColumn/2 + (blockwidth+interval)*y + interval;
					ry = (blockheight+interval)*dimRow/2 - (blockheight+interval)*x + interval;
					fw.write(rx+" "+ry+" 0.0 \r\n" );
					fw.write((rx+blockwidth)+" "+ry+" 0.0\r\n");
					fw.write(rx+" "+(ry+blockheight)+" 0.0\r\n");
					fw.write((rx+blockwidth)+" "+(ry+blockheight)+" 0.0\r\n");
				}
			} //for x
			fw.write("POLYGONS "+numcell+" "+5*numcell+"\r\n");
			for (int icell = 0; icell < numcell; icell++) {
				fw.write("4 "+(4*icell)+" "+(4*icell+1)+" "+(4*icell+3)+" "+(4*icell+2)+"\r\n");
			}
			fw.write("POINT_DATA "+numpoint+"\r\n");
			fw.write("SCALARS color float \r\n");
			fw.write("LOOKUP_TABLE default \r\n");
			
			for (int x = 0; x < dimRow; x++) {
				for (int y = 0; y < dimColumn; y++) {
					float val = (float)mat[x][y];
					fw.write(val+" "+val+" "+val+" "+val+"\r\n");
				}
			}
			fw.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
