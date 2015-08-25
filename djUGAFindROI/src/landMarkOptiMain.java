/**
 * 
 */


import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.*;

import edu.uga.liulab.djVtkBase.djVtkData;

/**
 * @author djzhu
 * 
 */
public class landMarkOptiMain {
	private CommandLineParser cmdParser;
	private Options options;
	private CommandLine cmdLine;
	private HelpFormatter formatter;

	public landMarkOptiMain() {
		cmdParser = new GnuParser();
		formatter = new HelpFormatter();
	}

	private void createOptions() {
		options = new Options();

		// **************Help info********************************
		Option ohelp = new Option("help", "print this message");
		// ***********Optimization

		// *************Input list of files and grid********************************
		Option oDataInfo = OptionBuilder.withArgName("file name").hasArg().isRequired(false)
				.withDescription("List of files which are need to be optimized or models( e.g. surface, fiber)").create("if");
		Option oGridInfo = OptionBuilder.withArgName("file name").hasArg().isRequired(false)
				.withDescription("List of grids. Each row represents one subject").create("ig");

		// *************Assign part of input when needed********************************
		Option oSubNum = OptionBuilder.withArgName("Integer").hasArg().isRequired(false).withDescription("number of subjects").create("sn");
		Option oSubList = OptionBuilder.withArgName("Integer,Integer,...,Integer").hasArg().isRequired(false)
				.withDescription("subjects list using comma(eg. 0,1,2,3,4,5,6) which are from -if").create("sl");
		Option oGridIDStart = OptionBuilder.withArgName("Integer").hasArg().isRequired(false)
				.withDescription("grid index start(from 1) which are from -ig").create("gs");
		Option oGridIDEnd = OptionBuilder.withArgName("Integer").hasArg().isRequired(false).withDescription("grid index end which are from -ig")
				.create("ge");

		// **************Assign the range********************************
		Option oRange = OptionBuilder.withArgName("Integer").hasArg().isRequired(false)
				.withDescription("number of rings for extracting fibers or search depend on -f or -o (default 3)").create("rn");

		// ************Compute Feature********************************
		Option oDoComputeFeature = new Option("f", "compute the feature vectors of the subjects. Need assign -if -ig -sn -sl -rn");

		// ***********Optimization********************************
		Option oDoOptimization = new Option("o", "optimization");
		Option oNeedSampleNeighbors = new Option("sampleNeighbors", "decimate the neighborhood. Actual search range will be rn*2");

		// ***********Prediction********************************
		Option oPredictionDataInfo = OptionBuilder.withArgName("file name").hasArg().isRequired(false)
				.withDescription("List of files for prediction( e.g. surface, fiber, volumes...)").create("ifPre");
		Option oDoPrediction = new Option("p", "prediction");

		// **************Optimization for a specific sub and gridIndex********************************
		Option oDoOptimizationS = new Option("os", "Optimization for a specific sub and gridIndex");
		Option oGridIDForOptiSpec = OptionBuilder.withArgName("Integer").hasArg().isRequired(false)
				.withDescription("grid index of optimization for a specific gridIndex").create("grid");

		// ***********Generate fibers of result********************************
		Option oDoGenerateFibers = new Option("fibers", "Generate fibers of result. ( use gridInfo from -ig , dataInfo from -ifPre, -sn and -sl)");
		Option oFolderForFibers = OptionBuilder.withArgName("String").hasArg().isRequired(false).withDescription("folder for fibers").create("fof");

		// ***************Add these options
		options.addOption(ohelp);
		options.addOption(oDataInfo);
		options.addOption(oGridInfo);
		options.addOption(oSubNum);
		options.addOption(oSubList);
		options.addOption(oGridIDStart);
		options.addOption(oGridIDEnd);
		options.addOption(oRange);
		options.addOption(oDoComputeFeature);
		options.addOption(oDoOptimization);
		options.addOption(oNeedSampleNeighbors);
		options.addOption(oPredictionDataInfo);
		options.addOption(oDoPrediction);
		options.addOption(oDoOptimizationS);
		options.addOption(oDoGenerateFibers);
		options.addOption(oFolderForFibers);
	}

	private void parseArgs(String[] strInputs) {
		if (strInputs.length == 0)
			this.printErrMsg("Need Input!");
		try {
			cmdLine = this.cmdParser.parse(this.options, strInputs);
		} catch (ParseException e) {
			formatter.printHelp("landMarkOpti input error!", this.options);
			System.exit(0);
			e.printStackTrace();
		}
		if (cmdLine == null)
			this.printErrMsg("cmdLine is null!");
	}

	private void printErrMsg(String strMsg) {
		formatter.printHelp(strMsg, this.options);
	}

	private void dispatch(String[] strInputs) throws Exception {
		this.createOptions();
		this.parseArgs(strInputs);
		if (cmdLine.hasOption("help")) {
			this.printErrMsg("Help is useful! ");
			return;
		}
		// ********************************Parse the input********************************
		landMarkOptiService landMarkOptiServiceHandler = new landMarkOptiService();

		if (cmdLine.hasOption("if")) {
			landMarkOptiServiceHandler.dataFileName = cmdLine.getOptionValue("if");
			landMarkOptiServiceHandler.loadDataInfo();
		}
		if (cmdLine.hasOption("ig")) {
			landMarkOptiServiceHandler.gridFileNme = cmdLine.getOptionValue("ig");
			landMarkOptiServiceHandler.loadGridInfo();
		}
		if (cmdLine.hasOption("sn") && cmdLine.hasOption("sl")) {
			int subNum = Integer.valueOf(cmdLine.getOptionValue("sn"));
			String strSubList = cmdLine.getOptionValue("sl");
			String[] subList = strSubList.split(",");
			if (subList.length == subNum) {
				landMarkOptiServiceHandler.virSubList.clear();
				for (int i = 0; i < subNum; i++)
					landMarkOptiServiceHandler.virSubList.add(Integer.valueOf(subList[i]));
			} else {
				this.printErrMsg("subjects number is not match!");
				return;
			}
		}
		if (cmdLine.hasOption("gs"))
			landMarkOptiServiceHandler.gridStart = Integer.valueOf(cmdLine.getOptionValue("gs"));
		if (cmdLine.hasOption("ge"))
			landMarkOptiServiceHandler.gridEnd = Integer.valueOf(cmdLine.getOptionValue("ge"));
		if (cmdLine.hasOption("rn"))
			landMarkOptiServiceHandler.ringNum = Integer.valueOf(cmdLine.getOptionValue("rn"));
		if (cmdLine.hasOption("sampleNeighbors"))
			landMarkOptiServiceHandler.needDecimateNeighbors = true;
		if (cmdLine.hasOption("ifPre")) {
			landMarkOptiServiceHandler.predictionFileNme = cmdLine.getOptionValue("ifPre");
			landMarkOptiServiceHandler.loadPredictionInfo();
		}

		// need to compute the feature vectors of the assigned subjects
		if (cmdLine.hasOption("f")) {
			landMarkOptiServiceHandler.prepareFeture();
		} // if computer feature

		// need to optimize
		if (cmdLine.hasOption("o")) {
			landMarkOptiServiceHandler.do_optimize();
		}

		if (cmdLine.hasOption("os")) {
			landMarkOptiServiceHandler.do_optimizeSpec();
		}

		// need to predict
		if (cmdLine.hasOption("p")) {
			if (landMarkOptiServiceHandler.predictionInfoList == null || landMarkOptiServiceHandler.predictionInfoList.size() == 0) {
				this.printErrMsg("Need input predictionInfo!");
				return;
			} else
				landMarkOptiServiceHandler.do_prediction();
		}

		// need to generate fibers
		if (cmdLine.hasOption("fibers")) {
			landMarkOptiServiceHandler.folderForFibers = cmdLine.getOptionValue("fof");
			landMarkOptiServiceHandler.do_generateROIfibers();
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		landMarkOptiMain mainHandler = new landMarkOptiMain();
		// **************The first operation***********************
		mainHandler.dispatch(args);
	}

}
