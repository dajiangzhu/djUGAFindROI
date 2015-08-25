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
public class calculateConvergeNum {

    public static AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File("./data/subRIndex.txt")); //19*2

    public void calConvergeNum() {
        float radius = 10.0f;
        int density = 8;
        int[] flag = new int[2057];
        for (int i = 0; i < 2057; i++) {
            flag[i] = 0;
        }
        for (int subIndex = 0; subIndex < 5; subIndex++) {
            System.out.println("dealing with sub" + subIndex);
            for (int i = 0; i < 2057; i++) {
                flag[i] = 0;
            }
            int nRealSubID = (int) calculateConvergeNum.subRelationM.get(subIndex, 1);
            djVtkSurData currentSurData = new djVtkSurData("./OptiResult/" + nRealSubID + ".BeforeOrAfter5.grid.vtk");
            int currentClusterNum = 0;
            for (int ptID_a = 0; ptID_a < currentSurData.nPointNum; ptID_a++) {
                if (flag[ptID_a] == 0) {
                    djVtkPoint ptData_a = currentSurData.getPoint(ptID_a);
                    int count = 0;
                    List<djVtkPoint> currentCluster = new ArrayList<djVtkPoint>();
                    for (int ptID_b = 0; ptID_b < currentSurData.nPointNum; ptID_b++) {
                        djVtkPoint ptData_b = currentSurData.getPoint(ptID_b);
                        float dis = djVtkUtil.calDistanceOfPoints(ptData_a, ptData_b);
                        if (dis < radius) {
                            currentCluster.add(ptData_b);
                            count++;
                        }
                    } //for ptID_b

                    if (count > density) {
                        for (int j = 0; j < currentCluster.size(); j++) {
                            flag[currentCluster.get(j).pointId] = 1;
                        }
                        int initialNum;
                        do {
                            initialNum = currentCluster.size();
                            List<djVtkPoint> currentClusterCopy = new ArrayList<djVtkPoint>();
                            currentClusterCopy.addAll(currentCluster);
                            for (int ptID_c = 0; ptID_c < currentClusterCopy.size(); ptID_c++) {
                                if (flag[currentClusterCopy.get(ptID_c).pointId] == 0) {
                                    djVtkPoint ptData_c = currentClusterCopy.get(ptID_c);
                                    count = 0;
                                    for (int ptID_d = 0; ptID_d < currentSurData.nPointNum; ptID_d++) {
                                        djVtkPoint ptData_d = currentSurData.getPoint(ptID_d);
                                        float dis = djVtkUtil.calDistanceOfPoints(ptData_c, ptData_d);
                                        if (dis < radius) {
                                            currentCluster.add(ptData_d);
                                            count++;
                                            flag[ptID_d] = 1;
                                        }
                                    } //for ptID_d
                                } //if flag==0
                            } //for ptID_c

                        } while (currentCluster.size() > initialNum);
                        currentClusterNum++;
                    }
                } //if (flag[ptID_a] == 0) {
            } //for ptIID_a
            System.out.println("sub" + nRealSubID + " : clusterNum = " + currentClusterNum);
        } //for subIndex
    }

    private float calDis(djVtkSurData surData1, djVtkSurData surData2) {
        float minDis;
        float sumDis = 0.0f;
        for (int ptID_1 = 0; ptID_1 < surData1.nPointNum; ptID_1++) {
            minDis = 1000.0f;
            djVtkPoint ptData1 = surData1.getPoint(ptID_1);
            for (int ptID_2 = 0; ptID_2 < surData2.nPointNum; ptID_2++) {
                djVtkPoint ptData2 = surData2.getPoint(ptID_2);
                float tmpDis = djVtkUtil.calDistanceOfPoints(ptData1, ptData2);
                if (tmpDis < minDis) {
                    minDis = tmpDis;
                }
            } //for ptData2
            sumDis = sumDis + minDis;
        } //for ptData1
        return sumDis / surData1.nPointNum;
    }

    public void getLandMarksDis() {
        float dis;
        for (int subIndex = 0; subIndex < 5; subIndex++) {
            System.out.println("dealing with sub" + subIndex);
            int nRealSubID = (int) calculateConvergeNum.subRelationM.get(subIndex, 1);
            djVtkSurData surData_initial1 = new djVtkSurData("./OptiResult/" + nRealSubID + ".BeforeOrAfter3.grid.vtk");
            djVtkSurData surData_initial2 = new djVtkSurData("./OptiResult/" + nRealSubID + ".BeforeOrAfter3.chInitial.grid.vtk");
            dis = (this.calDis(surData_initial1, surData_initial2) + this.calDis(surData_initial2, surData_initial1)) / 2;
            System.out.println("before: " + dis);
            djVtkSurData surData_optimize1 = new djVtkSurData("./OptiResult/" + nRealSubID + ".BeforeOrAfter5.grid.vtk");
            djVtkSurData surData_optimize2 = new djVtkSurData("./OptiResult/" + nRealSubID + ".BeforeOrAfter5.chInitial.grid.vtk");
            dis = (this.calDis(surData_optimize1, surData_optimize2) + this.calDis(surData_optimize2, surData_optimize1)) / 2;
            System.out.println("after: " + dis);
        }
    }

    public static void main(String[] args) throws Exception {
        calculateConvergeNum mainFlow = new calculateConvergeNum();
        mainFlow.getLandMarksDis();;
        
    }
}
