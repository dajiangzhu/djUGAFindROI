

import java.util.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.uga.liulab.djVtkBase.djVtkFiberData;
import edu.uga.liulab.djVtkBase.djVtkHybridData;
import edu.uga.liulab.djVtkBase.djVtkPoint;
import edu.uga.liulab.djVtkBase.djVtkSurData;
import edu.uga.liulab.djVtkBase.djVtkUtil;

public class testForKaiming {
	private CommandLineParser cmdParser;
	private Options options;
	private CommandLine cmdLine;
	private HelpFormatter formatter;
	// *******************************
	public String surFileName;
	public String fiberFileName;
	public int seedPtID;
	public int ringNum;
	
	public testForKaiming() {
		cmdParser = new GnuParser();
		formatter = new HelpFormatter();
	}

	private void createOptions() {
		options = new Options();
		Option oInputSurFile = OptionBuilder.withArgName("file name").hasArg().isRequired(true).withDescription("input file of surface(*.vtk)")
				.create("sur");
		Option oInputFibersFile = OptionBuilder.withArgName("file name").hasArg().isRequired(true).withDescription("input file of fiber(*.vtk)")
				.create("fiber");
		Option oInputPtID = OptionBuilder.withArgName("Integer").hasArg().isRequired(true).withDescription("input point ID").create("pt");
		Option oInputRingNum = OptionBuilder.withArgName("Integer").hasArg().isRequired(true).withDescription("input ROI size").create("rn");
		Option ohelp = new Option("help", "print this message");
		options.addOption(oInputSurFile);
		options.addOption(oInputFibersFile);
		options.addOption(oInputPtID);
		options.addOption(oInputRingNum);
		options.addOption(ohelp);
	}

	private void parseArgs(String[] strInputs) {
		try {
			cmdLine = this.cmdParser.parse(this.options, strInputs);
		} catch (ParseException e) {
			formatter.printHelp("test for Degang: input error!", this.options);
			System.exit(0);
			e.printStackTrace();
		}
	}

	private void printErrMsg(String strMsg) {
		formatter.printHelp(strMsg, this.options);
	}

	public void testTracemaps(String[] strInputs) {
		this.createOptions();
		this.parseArgs(strInputs);
		if (cmdLine == null || cmdLine.hasOption("help")) {
			this.printErrMsg("test for Degang: input error!");
			return;
		}
		this.surFileName = cmdLine.getOptionValue("sur");
		this.fiberFileName = cmdLine.getOptionValue("fiber");
		this.seedPtID = Integer.valueOf(cmdLine.getOptionValue("pt"));
		this.ringNum = Integer.valueOf(cmdLine.getOptionValue("rn"));
		// ************************************************************
		djVtkSurData currentSurData = new djVtkSurData(this.surFileName);
		djVtkFiberData currentFiberData = new djVtkFiberData(this.fiberFileName);
		djVtkHybridData hybridData = new djVtkHybridData(currentSurData, currentFiberData);
		hybridData.mapSurfaceToBox();
		hybridData.mapFiberToBox();
		Set ptSet = currentSurData.getNeighbourPoints(this.seedPtID, this.ringNum);
		hybridData.getFibersConnectToPointsSet(ptSet).writeToVtkFileCompact(this.surFileName + "_" + this.seedPtID + ".fibers.vtk");
		djVtkPoint tmpPoint = currentSurData.getPoint(this.seedPtID);
		djVtkFiberData tmpFiberData = new djVtkFiberData(this.surFileName + "_" + this.seedPtID + ".fibers.vtk");
		fiberBundleService fiberBundleDescriptor = new fiberBundleService();
		fiberBundleDescriptor.setFiberData(tmpFiberData);
		fiberBundleDescriptor.setSeedPnt(tmpPoint);
		fiberBundleDescriptor.createFibersTrace();
		fiberBundleDescriptor.writeToVtkFile(this.surFileName + "_" + this.seedPtID + ".trace.vtk");
		List<djVtkPoint> allTracePointsList = fiberBundleDescriptor.getAllPoints();
		List<Float> tmpFeature = fiberBundleDescriptor.calFeatureOfTrace(allTracePointsList);
		List<String> outPutFeatures = new ArrayList<String>();
		for(int i=0;i<tmpFeature.size();i++)
			outPutFeatures.add(tmpFeature.get(i).toString());
		djVtkUtil.writeArrayListToFile(outPutFeatures, this.surFileName + "_" + this.seedPtID + ".feature.txt");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testForKaiming mainHandler = new testForKaiming();
		mainHandler.testTracemaps(args);

	}

}
