

import java.io.File;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

public class findModelDictionary {
	public static final int EXTRACTING_FIBER_RINGNUM = 3;
	public static final int FINE_OPTI_RINGNUM = 2;
	public static final int ITERATIVE_MAX = 5;
	public static final int ITERATIVE_THRESHOLD = 3;
	public static final int MOVE_RANGE = 3;
	public static final int FIBERNUM_THRESHOLD = 30;
	public static AbstractMatrix subRelationM = Matrix.fromASCIIFile(new File("./data/subRIndex.txt")); // N*2
	public static AbstractMatrix gridPointM = Matrix.fromASCIIFile(new File("./data/GridPointMapping.txt")); // 2056*15
	public static AbstractMatrix gridPoint_chInitial_1_M = Matrix.fromASCIIFile(new File("./data/GridPointMapping_CHInitial_1.txt")); // 2056*15
	//******************************************Working Memory Data****************************************************************
	public static AbstractMatrix workMemBefore = Matrix.fromASCIIFile(new File("./data/seedIndex.txt")); // 15*16
	public static AbstractMatrix workMemAfter = Matrix.fromASCIIFile(new File("./data/seedAfterOptimized.txt")); // 15*16

}
