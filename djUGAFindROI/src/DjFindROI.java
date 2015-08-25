/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import org.jmat.data.*;
import edu.uga.liulab.djVtkBase.*;

/**
 *
 * @author dajiang
 */
public class DjFindROI {
    
    public static final String rootDataDir = "./data/";
    public static final String pdsdDataDir = "./PDSD_c_DTI/";
    public AbstractMatrix subRelationM; //19*2
    public AbstractMatrix pdsdSubRelationM; //45*1

    public DjFindROI() {
        subRelationM = Matrix.fromASCIIFile(new File(rootDataDir + "subRIndex.txt")); //19*2
        pdsdSubRelationM = Matrix.fromASCIIFile(new File( pdsdDataDir+"pdsdSubRIndex.txt")); //45*1
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {        
        if( args.length!=1 )
        {
            System.out.println("format111: java -Xmx2000m -jar *.jar seedSubID(0~14) pdsd using -1");
            return;
        }
        DjFindROI mainClass = new DjFindROI();
        int input = Integer.valueOf(args[0]);
        int seedSubID;
        if(input==-1)
        	seedSubID=19; //use #19sub of working memory dataset
        else
         seedSubID = (int) mainClass.subRelationM.get(input, 1);
        // TODO code application logic here
        MainFlow mainFlow = new MainFlow();
        mainFlow.setSeedSubID(seedSubID);
        
        //run first
//        mainFlow.setSeedSurData( new djVtkSurData(DjFindROI.rootDataDir+seedSubID+".surf.vtk") );
////        mainFlow.geneSeedSurMeshPts();
////
//        mainFlow.setSeedVolData( new djNiftiData(DjFindROI.rootDataDir+seedSubID+".b0",DjFindROI.rootDataDir+seedSubID+".b0.mhd") );
//        mainFlow.geneSeedVolMeshVoxel();
        //ddaabb
        
        //*************
        // call flirt
        //*************
        
        //run second
        for(int i=10;i<46;i++)
        {
            if(i!=Integer.valueOf(args[0]))
            {
                mainFlow.getNonSeedSubIDList().add( (int) mainClass.pdsdSubRelationM.get(i, 0) );
            }
        }
        mainFlow.mapVolGridToSur();
        
//        
//        mainFlow.analyzeAllValidGrid();
//        mainFlow.calculateGridDis();
//
//        //output result
//        mainFlow.displayResult();
//
        
        //for test and info...
        //analyze DB
//        mainFlow.ayalyzeDB();
        
        
//        mainFlow.findCommonGridAcrossSubs();
//        mainFlow.printTraceSamplePoints();
    }

}
