package parser;

import java.io.File;
import java.util.Scanner;

import utils.FileUtil;

public class CorpusGenerator {
	private String repoPath;
	
	public static void main(String[] args) {
		String[] libs = new String[]{"android.", "com.google.gwt.", "org.hibernate.", "org.joda.time.", "com.thoughtworks.xstream."};
		CorpusGenerator cg = new CorpusGenerator("G:/github/repos-5stars-50commits");
		cg.generateSequences("T:/github", false, libs, "T:/type-sequences");
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
						if (count > 200 || numOfSequences > 1000000)
							break;
					}
					sc.close();
				}
			}).start();
		}
	}
}
