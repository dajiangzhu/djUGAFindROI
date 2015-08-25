

import java.util.*;

public class gridConvergeInfo {
	public int gridID=-1;
	public int convergeRoundNum=-1;
	public int[] convergePt = new int[15];
	public Map<Integer,List<Integer>> convergeTrace = new HashMap<Integer,List<Integer>>(); //<subID,ptList of trace>
	public gridConvergeInfo() {
		for(int i=0;i<15;i++)
		{
			List<Integer> newList = new ArrayList<Integer>();
			convergeTrace.put(i, newList);
		}
	}
	
}
