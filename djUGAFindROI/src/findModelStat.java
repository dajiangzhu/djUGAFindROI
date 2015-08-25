

import java.io.File;
import java.util.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import edu.uga.liulab.djVtkBase.djVtkPoint;
import edu.uga.liulab.djVtkBase.djVtkSurData;
import edu.uga.liulab.djVtkBase.djVtkUtil;

public class findModelStat {
	private CommandLineParser cmdParser;
	private Options options;
	private CommandLine cmdLine;
	private HelpFormatter formatter;

	public findModelStat() {
		cmdParser = new GnuParser();
		formatter = new HelpFormatter();
	}

	private void createOptions() {
		options = new Options();
		Option oSubID = OptionBuilder.withArgName("Integer").hasArg().isRequired(false)
				.withDescription("the subID which you want to generate vtk files of grid points").create("sub");
		Option ohelp = new Option("help", "print this message");
		Option oDoAnaConvergeNum = new Option("a", "analyze converge grid nums");
		Option oDoGenerateVtk = new Option("g", "generate vtk files of converge points for assigned subject");
		options.addOption(oSubID);
		options.addOption(oDoAnaConvergeNum);
		options.addOption(oDoGenerateVtk);
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

	public void doAnalyzeConvergeNum() {
		File fileDir = new File("./convergeInfo");
		String[] fileList = fileDir.list();
		int fileNum = fileList.length;
		Set<Integer> set1 = new HashSet<Integer>();
		Set<Integer> set2 = new HashSet<Integer>();
		Set<Integer> set3 = new HashSet<Integer>();
		for (int n = 5; n < 10; n++) {
			System.out.println("****************" + (n - 2) + "subs optimization**************************");
			set2.clear();
			for (int i = 0; i < fileNum; i++) {
				String currentFileName = fileList[i];
				String[] tmpFileNameEle = currentFileName.split("\\.");
				if (tmpFileNameEle.length == n) {
					set3.clear();
					System.out.println(currentFileName + "###");
					AbstractMatrix inputM = Matrix.fromASCIIFile(new File("./convergeInfo/" + currentFileName));
					int rowDim = inputM.getRowDimension();
					for (int r = 0; r < rowDim; r++) {
						int roundNum = (int) inputM.get(r, 1);
						if (roundNum <= 3) {
							int gridID = (int) inputM.get(r, 0);
							set3.add(gridID);
						} // if roundNum<=3
					} // for r
					System.out.println("the converge grid num in" + currentFileName + " is " + set3.size());
					set2.addAll(set3);
				} // if
			} // for all files in the dir
			System.out.println("the converge grid num for " + (n - 2) + " subs is " + set2.size());
			set1.addAll(set2);
		} // for all num of opti
		System.out.println("the all files converge grid num is " + set1.size());
	}

	public void doGenerateVtkFiles(int subVirID) {
		int realSubID = (int) findModelDictionary.subRelationM.get(subVirID, 1);
		djVtkSurData surData = new djVtkSurData("./data/" + realSubID + ".surf.vtk");
		File fileDir = new File("./convergeInfo");
		String[] fileList = fileDir.list();
		int fileNum = fileList.length;
		Set<String> setFileNameParts = new HashSet<String>();
		for (int n = 5; n < 10; n++) {
			System.out.println("****************generate sub" + subVirID + " in " + (n - 2) + "subs optimization**************************");
			for (int i = 0; i < fileNum; i++) {
				String currentFileName = fileList[i];
				String[] tmpFileNameEle = currentFileName.split("\\.");
				boolean isContainSub = false;
				int nLoc = -1;
				for (int j = 0; j < tmpFileNameEle.length; j++) {
					if (tmpFileNameEle[j].trim().equals(String.valueOf(subVirID))) {
						System.out.println("file:" + currentFileName + " contains subject-" + subVirID);
						isContainSub = true;
						nLoc = j;
						break;
					}
				} // for

				List<djVtkPoint> ptList = new ArrayList<djVtkPoint>();
				if ((tmpFileNameEle.length == n) && (isContainSub)) {
					AbstractMatrix inputM = Matrix.fromASCIIFile(new File("./convergeInfo/" + currentFileName));
					int rowDim = inputM.getRowDimension();
					for (int r = 0; r < rowDim; r++) {
						int roundNum = (int) inputM.get(r, 1);
						if (roundNum <= findModelDictionary.ITERATIVE_THRESHOLD) {
							int ptID = (int) inputM.get(r, nLoc + 1);
							ptList.add(surData.getPoint(ptID));
						} // if roundNum<=3
					} // for r
					System.out.println("the converge grid num in" + currentFileName + " is " + ptList.size());
					djVtkUtil.writeToPointsVtkFile("./convergeInfo/sub" + subVirID + "_" + currentFileName + "_convergePts.vtk", ptList);
				} // if
			} // for all files in the dir
		} // for all num of opti
	}

	private void dispatch(String[] strInputs) throws Exception {
		this.createOptions();
		this.parseArgs(strInputs);
		if (cmdLine == null || cmdLine.hasOption("help")) {
			this.printErrMsg("Find Model");
			return;
		}
		// Parse the input
		if (cmdLine.hasOption("a")) // need to compute the feature vectors of the assigned subjects
		{
			this.doAnalyzeConvergeNum();
		}

		if (cmdLine.hasOption("g")) // need to optimize subjects list and output the consistency score/converge points
		{
			int subVirID = Integer.valueOf(cmdLine.getOptionValue("sub"));
			this.doGenerateVtkFiles(subVirID);
		}

	}

	public static void main(String[] args) throws Exception {
		findModelStat mainHandler = new findModelStat();
		mainHandler.dispatch(args);

	}
}
