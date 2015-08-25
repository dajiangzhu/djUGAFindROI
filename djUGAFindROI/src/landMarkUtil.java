/**
 * 
 */


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uga.liulab.djVtkBase.djVtkData;
import edu.uga.liulab.djVtkBase.djVtkPoint;

/**
 * @author djzhu
 * 
 */
public class landMarkUtil {
	static List<List<String>> loadData(String strFileName, String strSeperator) {
		List<List<String>> listResult = new ArrayList<List<String>>();
		try {
			FileInputStream fstream = new FileInputStream(strFileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String[] tmpStringArray;
			while ((strLine = br.readLine()) != null) {
				if (!strLine.startsWith("#") && (tmpStringArray = strLine.split(strSeperator)).length > 0) {
					List<String> currentItem = new ArrayList<String>();
					for (int i = 0; i < tmpStringArray.length; i++)
						currentItem.add(tmpStringArray[i]);
					listResult.add(currentItem);
				} // if not comment and more than one item in this line
			}// while
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		return listResult;
	}
	
	public static void writeToPointsVtkFile(String fileName, List<djVtkPoint> ptList, boolean attri_358) {
        System.out.println("Begin to write file:" + fileName + "...");
        List<String> roiAttriList = loadFileToArrayList("358_ROI_COLOR.txt");
        if(attri_358 && ptList.size()!=roiAttriList.size())
        {
        	System.out.println("the size of pts is not equal to the size of attributes...");
        	System.exit(0);
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(fileName);
            fw.write("# vtk DataFile Version 3.0\r\n");
            fw.write("vtk output\r\n");
            fw.write("ASCII\r\n");
            fw.write("DATASET POLYDATA\r\n");
            // print points info
            fw.write("POINTS " + ptList.size() + " float\r\n");
            for (int j = 0; j < ptList.size(); j++) {
                fw.write(ptList.get(j).x + " "
                        + ptList.get(j).y + " "
                        + ptList.get(j).z + "\r\n");
            }
            // print VERTICES info
            fw.write("VERTICES " + ptList.size() + " " + ptList.size() * 2 + " \r\n");
            for (int i = 0; i < ptList.size(); i++) {
                fw.write("1 " + i + "\r\n");
            }
            if(attri_358)
            {
                fw.write("POINT_DATA " + roiAttriList.size() + " \r\n"); 
                fw.write("COLOR_SCALARS PointColor  3 \r\n");
                for (int i = 0; i < roiAttriList.size(); i++) {
                    fw.write( roiAttriList.get(i)+" \r\n");
                }            	
            }
            
        } catch (IOException ex) {
            Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null,
                    ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
        }
        System.out.println("Write file done!");
        System.out.println("That is all!");
    }
	
    public static List<String> loadFileToArrayList(String fileName) {
        List<String> resultList = new ArrayList<String>();
        try {
            FileInputStream fstream = new FileInputStream(fileName);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                //System.out.println(strLine);
                resultList.add(strLine);
            }//while
            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return resultList;
    }

}
