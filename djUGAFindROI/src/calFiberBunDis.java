

import java.util.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.uga.liulab.djVtkBase.*;

public class calFiberBunDis {
	private CommandLineParser cmdParser;
	private Options options;
	private CommandLine cmdLine;
	private HelpFormatter formatter;
	// *******************************
	public String fiberBundleListFile;
	public String outputDisMatrixFile;

	public calFiberBunDis() {
		cmdParser = new GnuParser();
		formatter = new HelpFormatter();
	}

	private void createOptions() {
		options = new Options();
		Option oInputFibersFile = OptionBuilder.withArgName("file name").hasArg().isRequired(true)
				.withDescription("input file of fiber bundle list(*.txt)").create("fblist");
		Option oOutputDisMatrix = OptionBuilder.withArgName("file name").hasArg().isRequired(true)
				.withDescription("output distance matrix file(*.txt)").create("om");
		Option ohelp = new Option("help", "print this message");
		options.addOption(oInputFibersFile);
		options.addOption(oOutputDisMatrix);
		options.addOption(ohelp);
	}

	private void parseArgs(String[] strInputs) {
		try {
			cmdLine = this.cmdParser.parse(this.options, strInputs);
		} catch (ParseException e) {
			formatter.printHelp("calculate fiber bundle distance: input error!", this.options);
			System.exit(0);
			e.printStackTrace();
		}
	}

	private void printErrMsg(String strMsg) {
		formatter.printHelp(strMsg, this.options);
	}
	
	public djVtkPoint getAvePoint(String patchFile)
	{
		djVtkPoint pt = new djVtkPoint();
		djVtkSurData surData = new djVtkSurData(patchFile);
		float xSum = 0.0f;
		float ySum = 0.0f;
		float zSum = 0.0f;
		for(int i=0;i<surData.nPointNum;i++)
		{
			xSum += surData.getPoint(i).x;
			ySum += surData.getPoint(i).y;
			zSum += surData.getPoint(i).z;
		}
		pt.x = xSum/surData.nPointNum;
		pt.y = ySum/surData.nPointNum;
		pt.z = zSum/surData.nPointNum;
		return pt;
	}

	public void calFiberBundleDisMatrix(String[] strInputs) {
		this.createOptions();
		this.parseArgs(strInputs);
		if (cmdLine == null || cmdLine.hasOption("help")) {
			this.printErrMsg("calculate fiber bundle distance: input error!");
			return;
		}
		this.fiberBundleListFile = cmdLine.getOptionValue("fblist");
		this.outputDisMatrixFile = cmdLine.getOptionValue("om");
		List<String> fiberBundleList = djVtkUtil.loadFileToArrayList(this.fiberBundleListFile);
		if (fiberBundleList == null) {
			System.out.println("DJ said : Load file failed...");
			System.exit(0);
		} else {
			List<List<Float>> allTraceMap = new ArrayList<List<Float>>();
			for (int i = 0; i < fiberBundleList.size(); i++) {
				String[] tmpAnaArray = fiberBundleList.get(i).split("_");
				String patchFile=tmpAnaArray[0]+"_"+tmpAnaArray[1].split("=")[1]+"ROIsurface_stable.vtk";
				// generate trace based on fiber file
				djVtkFiberData tmpFiberData = new djVtkFiberData(fiberBundleList.get(i));
				djVtkPoint tmpPoint = this.getAvePoint(patchFile);
				fiberBundleService fiberBundleDescriptor = new fiberBundleService();
				fiberBundleDescriptor.setFiberData(tmpFiberData);
				fiberBundleDescriptor.setSeedPnt(tmpPoint);
				fiberBundleDescriptor.createFibersTrace();
				List<djVtkPoint> allTracePointsList = fiberBundleDescriptor.getAllPoints();
				// calculate the feature
				List<Float> tmpFeature = fiberBundleDescriptor.calFeatureOfTrace(allTracePointsList);
				allTraceMap.add(tmpFeature);
			} // for all fiber bundles

			System.out.println("DJ said : calculating distance among the fiber bundles... ");
			List<List<Float>> allDisList = new ArrayList<List<Float>>();
			int dim = allTraceMap.size();
			float[][] allDisArray = new float[dim][dim];
			for (int i = 0; i < dim - 1; i++) {
				for (int j = i + 1; j < dim; j++) {
					float tmpDis = this.calFeatureDis(allTraceMap.get(i), allTraceMap.get(j));
					allDisArray[i][j] = tmpDis;
					allDisArray[j][i] = tmpDis;
				} // for j
			} // for i

			for (int i = 0; i < dim; i++) {
				List<Float> newDisRow = new ArrayList<Float>();
				for (int j = 0; j < dim; j++)
					newDisRow.add(allDisArray[i][j]);
				allDisList.add(newDisRow);
			} //for i
			djVtkUtil.writeArrayListToFile(allDisList, " ", dim, this.outputDisMatrixFile);
		} // else

	}

	private float calFeatureDis(List<Float> f1, List<Float> f2) {
		float dis = 0.0f;
		if (f1.size() == f2.size()) {
			for (int i = 0; i < f1.size(); i++) {
				dis += Math.pow(Math.abs(f1.get(i) - f2.get(i)), 2);
			}
		} else {
			System.out.println("the size of f1 and f2 are not equal!!");
		}
		return dis;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		calFiberBunDis mainHandler = new calFiberBunDis();
		mainHandler.calFiberBundleDisMatrix(args);

	}

}
