package parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import utils.FileUtil;

public class CorpusGenerator {
	private String repoPath;
	
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		String[] libs = new String[]{"org.apache.commons.", "android.", "com.google.gwt.", "org.hibernate.", "org.joda.time.", "com.thoughtworks.xstream."};
		CorpusGenerator cg = new CorpusGenerator("G:/github/repos-5stars-50commits");
		cg.generateSequences("T:/github", false, libs, "T:/type-sequences");
		long end = System.currentTimeMillis();
		System.out.println("Finish parsing corpus in " + (end - start) / 1000);
	}

	public CorpusGenerator(String repoPath) {
		this.repoPath = repoPath;
	}
	
	public void generateSequences(final String repoListsPath, final boolean keepUnresolvables, final String[] libs, final String outPath) {
		for (final String lib : libs) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					String content = FileUtil.getFileContent(repoListsPath + "/repos-5stars-50commits-lib-" + lib + ".csv");
					Scanner sc = new Scanner(content);
					int count = 0, numOfSequences = 0;
					while (sc.hasNextLine()) {
						count++;
						String line = sc.nextLine();
						int index = line.indexOf(',');
						if (index == -1)
							index = line.length();
						String name = line.substring(0, index);
							File outDir = new File(outPath + "/" + lib + "/" + name.replace('/', '_'));
							System.out.println("Start " + lib + " project " + count + " " + name);
							int n = 0;
							if (new File(outDir.getAbsolutePath() + "-alignment/training.t-s.A3").exists()) {
								String locations = FileUtil.getFileContent(outDir.getAbsolutePath() + "/locations.txt");
								for (int i = 0; i < locations.length(); i++)
									if (locations.charAt(i) == '\n')
										n++;
							} else {
								ProjectSequencesGenerator psg = new ProjectSequencesGenerator(repoPath + "/" + name, false);
								if (!outDir.exists())
									outDir.mkdirs();
								try {
									n = psg.generateSequences(keepUnresolvables, lib, outDir.getAbsolutePath());
								} catch (Throwable t) {
									System.err.println("Error in parsing " + lib + " project " + count + " " + name);
									t.printStackTrace();
								}
//								psg.generateAlignment();
							}
							numOfSequences += n;
							System.out.println("Done " + lib + " project " + count + " " + name + " sequences " + n + " " + numOfSequences);
						if (count >= 200)
							break;
					}
					sc.close();
				}
			}).start();
		}
	}

	/**
	 * 
	 * @param inPath
	 * @param doVerify
	 * @return 	numbers[0]: number of project with different numbers of sequences;
	 * 			numbers[1]: number of sequences with different lengths;
	 * 			numbers[2]: number of sequences with non-aligned tokens;
	 * 			numbers[3]: number of non-aligned tokens 
	 */
	public static int[] concatSequences(String inPath, String outPath, boolean keepNonAlignment) {
		int[] numbers = new int[]{0, 0, 0, 0};
		PrintStream sources = null, targets = null;
		new File(outPath).mkdirs();
		try {
			sources = new PrintStream(new FileOutputStream(outPath + "/source.txt"));
			targets = new PrintStream(new FileOutputStream(outPath + "/target.txt"));
		} catch (IOException e) {
			return null;
		}
		File dir = new File(inPath);
		for (File sublib : dir.listFiles()) {
			for (File subp : sublib.listFiles()) {
				ArrayList<String> sourceSequences = FileUtil.getFileStringArray(subp.getAbsolutePath() + "/source.txt"), 
						targetSequences = FileUtil.getFileStringArray(subp.getAbsolutePath() + "/target.txt");
				if (sourceSequences.size() != targetSequences.size()) {
					numbers[0]++;
					continue;
				}
				for (int i = 0; i < sourceSequences.size(); i++) {
					String source = sourceSequences.get(i), target = targetSequences.get(i);
					String[] sTokens = source.trim().split(" "), tTokens = target.trim().split(" ");
					if (sTokens.length != tTokens.length) {
						numbers[1]++;
						if (!keepNonAlignment)
							continue;
					}
					if (!keepNonAlignment) {
						boolean aligned = true;
						for (int j = 0; j < sTokens.length; j++) {
							String s = sTokens[j], t = tTokens[j];
							if ((t.contains(".") && !t.endsWith(s)) || (!t.contains(".") && !t.equals(s))) {
								aligned = false;
								numbers[3]++;
							}
						}
						if (!aligned) {
							numbers[2]++;
							if (!keepNonAlignment)
								continue;
						}
					}
					sources.println(source);
					targets.println(target);
				}
			}
		}
		sources.flush();
		targets.flush();
		sources.close();
		targets.close();
		return numbers;
	}
}
