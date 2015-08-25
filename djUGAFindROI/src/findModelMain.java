

import java.io.IOException;

import org.apache.commons.cli.*;

public class findModelMain {
	private CommandLineParser cmdParser;
	private Options options;
	private CommandLine cmdLine;
	private HelpFormatter formatter;

	public findModelMain() {
		cmdParser = new GnuParser();
		formatter = new HelpFormatter();
	}

	private void createOptions() {
		options = new Options();
		Option oSubNum = OptionBuilder.withArgName("Integer").hasArg().isRequired(false).withDescription("number of subjects").create("sn");
		Option oSubList = OptionBuilder.withArgName("Integer,Integer,...,Integer").hasArg().isRequired(false)
				.withDescription("subjects list using space(eg. 1,2,3)").create("sl");
		Option oGridIDStart = OptionBuilder.withArgName("Integer").hasArg().isRequired(false).withDescription("grid index start").create("gs");
		Option oGridIDEnd = OptionBuilder.withArgName("Integer").hasArg().isRequired(false).withDescription("grid index end").create("ge");
		Option ohelp = new Option("help", "print this message");
		Option oDoComputeFeature = new Option("f", "compute the feature vectors of the assigned subjects");
		Option oDoOptimze = new Option("o", "optimize subjects list and output the consistency score/converge points");
		Option oDoValidationUsingFMRI = new Option("vf", "validation using fMRI, see the correlation");
		Option oDoValidationUsingSubCortical = new Option("vs", "validation using subcortical, see the connectivity patterns");
		Option oDoAnalyzeConverInfo = new Option("a", "analyze the consistant points among the input file and the model file");
		Option oAnaInput = OptionBuilder.withArgName("file name").hasArg().isRequired(false).withDescription("input file(converge info)")
				.create("ai");
		Option oAnaModel = OptionBuilder.withArgName("file name").hasArg().isRequired(false).withDescription("model file(more converge info)")
		.create("am");
		options.addOption(oSubNum);
		options.addOption(oSubList);
		options.addOption(oGridIDStart);
		options.addOption(oGridIDEnd);
		options.addOption(oDoComputeFeature);
		options.addOption(oDoOptimze);
		options.addOption(oDoValidationUsingFMRI);
		options.addOption(oDoValidationUsingSubCortical);
		options.addOption(oDoAnalyzeConverInfo);
		options.addOption(oAnaInput);
		options.addOption(oAnaModel);
		options.addOption(ohelp);
	}

	private void parseArgs(String[] strInputs) {
		try {
			cmdLine = this.cmdParser.parse(this.options, strInputs);
		} catch (ParseException e) {
			formatter.printHelp("Find Model input error!", this.options);
			System.exit(0);
			e.printStackTrace();
		}
	}

	private void printErrMsg(String strMsg) {
		formatter.printHelp(strMsg, this.options);
	}

	private void dispatch(String[] strInputs) throws Exception {
		this.createOptions();
		this.parseArgs(strInputs);
		if (cmdLine == null || cmdLine.hasOption("help")) {
			this.printErrMsg("Find Model");
			return;
		}
		// Parse the input
		findModelService optiService = new findModelService();
		if (cmdLine.hasOption("f")) // need to compute the feature vectors of the assigned subjects
		{
			int subNum = Integer.valueOf(cmdLine.getOptionValue("sn"));
			String strSubList = cmdLine.getOptionValue("sl");
			String[] subList = strSubList.split(",");
			if (subList.length == subNum) {
				for (int i = 0; i < subNum; i++)
					optiService.virSubList.add(Integer.valueOf(subList[i]));
			} else {
				this.printErrMsg("subjects number is not match!");
				return;
			}
			optiService.do_computeFeatures();
		}

		if (cmdLine.hasOption("o")) // need to optimize subjects list and output the consistency score/converge points
		{
			int subNum = Integer.valueOf(cmdLine.getOptionValue("sn"));
			String strSubList = cmdLine.getOptionValue("sl");
			String[] subList = strSubList.split(",");
			if (subList.length == subNum) {
				for (int i = 0; i < subNum; i++)
					optiService.virSubList.add(Integer.valueOf(subList[i]));
			} else {
				this.printErrMsg("subjects number is not match!");
				return;
			}
			optiService.gridStart = Integer.valueOf(cmdLine.getOptionValue("gs"));
			optiService.gridEnd = Integer.valueOf(cmdLine.getOptionValue("ge"));
			optiService.do_optimize();
		}
		if (cmdLine.hasOption("a")) // need to analyze the consistant points among the input file and the model file
		{
			optiService.anaInputFile = cmdLine.getOptionValue("ai");
			optiService.anaModelFile = cmdLine.getOptionValue("am");
			optiService.do_analyzeConsistancy();
		}
		if (cmdLine.hasOption("vf")) //
		{
			int subNum = Integer.valueOf(cmdLine.getOptionValue("sn"));
			String strSubList = cmdLine.getOptionValue("sl");
			String[] subList = strSubList.split(",");
			if (subList.length == subNum) {
				for (int i = 0; i < subNum; i++)
					optiService.virSubList.add(Integer.valueOf(subList[i]));
			} else {
				this.printErrMsg("subjects number is not match!");
				return;
			}
			optiService.do_validateUsingFMRI( 3 );
			//**********************************
//			fmriValidationService validationService = new fmriValidationService();
//			validationService.do_trustConsistent();
		}
		if(cmdLine.hasOption("vs"))
		{
			optiService.do_validateUsingSubCortical();
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		findModelMain mainHandler = new findModelMain();
		// **************The first operation***********************
		mainHandler.dispatch(args);

	}

}
