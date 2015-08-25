

import java.util.ArrayList;

import edu.uga.liulab.djVtkBase.djVtkSurData;
import edu.uga.liulab.djVtkBase.djVtkUtil;

public class bigPaperUtil {

	public void manuLabel() {
		djVtkSurData patch_Pre_L = new djVtkSurData("./experiment1128/10.Pre.L.vtk");
		djVtkSurData patch_Pre_R = new djVtkSurData("./experiment1128/10.Pre.R.vtk");
		djVtkSurData patch_Post_L = new djVtkSurData("./experiment1128/10.Post.L.vtk");
		djVtkSurData patch_Post_R = new djVtkSurData("./experiment1128/10.Post.R.vtk");

		// patch_Pre_L.cellsOutput.addAll(patch_Pre_L.cells);
		// patch_Pre_R.cellsOutput.addAll(patch_Pre_R.cells);
		// patch_Post_L.cellsOutput.addAll(patch_Post_L.cells);
		// patch_Post_R.cellsOutput.addAll(patch_Post_R.cells);
		// patch_Pre_L.writeToVtkFileCompact("./experiment1128/10.Pre.L.vtk");
		// patch_Pre_R.writeToVtkFileCompact("./experiment1128/10.Pre.R.vtk");
		// patch_Post_L.writeToVtkFileCompact("./experiment1128/10.Post.L.vtk");
		// patch_Post_R.writeToVtkFileCompact("./experiment1128/10.Post.R.vtk");

		djVtkSurData surData = new djVtkSurData("./connectome/10.gm.hammer.vtk");
		surData.pointsScalarData.clear();
		surData.pointsScalarData.put("label", new ArrayList<String>());
		boolean flag = false;
		for (int i = 0; i < surData.nPointNum; i++) {
			if (i % 100 == 0)
				System.out.println("pt: " + i);
			flag = false;
			if (flag==false)
				for (int i1 = 0; i1 < patch_Pre_L.nPointNum; i1++) {
					float dis = djVtkUtil.calDistanceOfPoints(surData.getPoint(i), patch_Pre_L.getPoint(i1));
					if (dis < 0.00001) {
						surData.pointsScalarData.get("label").add("80");
						flag = true;
						break;
					}
				}
			if (flag==false)
				for (int i1 = 0; i1 < patch_Pre_R.nPointNum; i1++) {
					float dis = djVtkUtil.calDistanceOfPoints(surData.getPoint(i), patch_Pre_R.getPoint(i1));
					if (dis < 0.00001) {
						surData.pointsScalarData.get("label").add("5");
						flag = true;
						break;
					}
				}
			if (flag==false)
				for (int i1 = 0; i1 < patch_Post_L.nPointNum; i1++) {
					float dis = djVtkUtil.calDistanceOfPoints(surData.getPoint(i), patch_Post_L.getPoint(i1));
					if (dis < 0.00001) {
						surData.pointsScalarData.get("label").add("74");
						flag = true;
						break;
					}
				}
			if (flag==false)
				for (int i1 = 0; i1 < patch_Post_R.nPointNum; i1++) {
					float dis = djVtkUtil.calDistanceOfPoints(surData.getPoint(i), patch_Post_R.getPoint(i1));
					if (dis < 0.00001) {
						surData.pointsScalarData.get("label").add("110");
						flag = true;
						break;
					}
				}
			if (flag == false)
				surData.pointsScalarData.get("label").add("0");
		} // for i
		surData.cellsOutput.addAll(surData.cells);
		surData.writeToVtkFile("./experiment1128/10.label.dj.vtk");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		bigPaperUtil mainHandler = new bigPaperUtil();
		mainHandler.manuLabel();

	}

}
