/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.*;
import edu.uga.liulab.djVtkBase.*;

/**
 *
 * @author dajiang
 */
public class gridCandidate {

    public int virSubID = -1;
    public int realSubID = -1;
    public int oriGridID = -1;
    public int oriPtID = -1;
    public int optiPtID = -1;
    public int optiGridPtID = -1;
    public int optiGridID = -1;
    public List<Double> feature = new ArrayList<Double>();
    public djVtkSurData surData;

    public void findOptiGridBasedOnOptiPt() {
//        this.realSubID = (int) optimizeGrids.subRelationM.get(virSubID, 1);
//        surData = new djVtkSurData("./data/" + realSubID + ".surf.vtk");
        djVtkPoint optiPoint = surData.getPoint(optiPtID);
        float disMin = 1000.0f;
        int gridFind = -1;
        float tmpDis;
        for(int i=0;i<optimizeGrids.nGridNum;i++)
        {
            djVtkPoint tmpPoint = surData.getPoint( (int)optimizeGrids.gridPoint_PTSD_1_5.get(i, virSubID-1) );
            tmpDis = djVtkUtil.calDistanceOfPoints(optiPoint, tmpPoint);
            if(tmpDis<disMin)
            {
                disMin = tmpDis;
                gridFind = i;
            }
        }
        this.optiGridID = gridFind+1;
        this.optiGridPtID = (int)optimizeGrids.gridPoint_PTSD_1_5.get(gridFind, virSubID-1);
    }
}
