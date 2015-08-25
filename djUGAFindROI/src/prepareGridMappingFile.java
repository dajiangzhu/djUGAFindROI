/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import org.jmat.data.*;

import edu.uga.liulab.djVtkBase.*;
import java.util.*;

/**
 *
 * @author dajiang
 */
public class prepareGridMappingFile {

    public static void main(String[] args) throws Exception {
        System.out.println("This is prepareGridMappingFile...");
        AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File("./data/subRIndex.txt")); //19*2
        AbstractMatrix pdsdSubRelationM = Matrix.fromASCIIFile(new File("./PDSD_c_DTI/pdsdSubRIndex.txt")); //45*1
        int nTempSub = 19;
        int nGridNum = 2056;
        int nSubNum = 15;

        List<List<String>> allGridOfSubs = new ArrayList<List<String>>();

        for (int gridIndex = 1; gridIndex <= nGridNum; gridIndex++) { //for all grid     
            if(gridIndex%100==0)
            {
                System.out.println("finished "+gridIndex+" grids...");
            }
            List<String> gridOfCurrentSub = new ArrayList<String>();            
            for (int subIndex = 0; subIndex < 5; subIndex++) { //for all subs, one line
                int nRealSubID = (int) pdsdSubRelationM.get(subIndex, 0);
                String strSql = "select * from test.t_allmapping_ptsd where sub_id=" + nRealSubID + " and grid_id=" + gridIndex + " and template_subid=" + nTempSub;
                List result = DatabaseTool.executeQuery(strSql);
                if (result.size() != 1) {
                    System.out.println("ERROR:the num of records is not 1-- grid=" + gridIndex + ", sub=" + nRealSubID);
                    System.exit(0);
                }
                HashMap dataMap = (HashMap) result.get(0);
                String pointID = String.valueOf(dataMap.get("pt_id"));
                gridOfCurrentSub.add(pointID);
            } //for all subs
            allGridOfSubs.add(gridOfCurrentSub);
        } // for all grid
        djVtkUtil.writeStringArrayListToFile(allGridOfSubs," ", 5, "./PDSD_c_DTI/GridPointMappingPTSD_1_5.txt");


    }
}
