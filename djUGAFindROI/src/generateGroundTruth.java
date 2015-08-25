/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import edu.uga.liulab.djVtkBase.*;

/**
 *
 * @author dajiang
 */
public class generateGroundTruth {
    
    public static void main(String[] args)
    {
        System.out.println("begin to load file...");
        djVtkFiberData fiber = new djVtkFiberData("098_S_2047_dj.vtk");
        int nCellNum = fiber.nCellNum;
        System.out.println("load file done! cellnum is : "+ nCellNum);
    
    }

}
