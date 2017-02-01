package eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import utils.FileUtil;

public class Eval {

	public static void main(String[] args) {
		String dir = "T:/temp/statType/SOF-40";
		ArrayList<String> trainSourceSequences = readSource(dir + "/train.s"), trainTargetSequences = readSource(dir + "/train.t");
		HashSet<String> sourceVocabulary = new HashSet<>(), targetVocabulary = new HashSet<>();
		for (int i = 0; i < trainSourceSequences.size(); i++) {
			String source = trainSourceSequences.get(i), target = trainTargetSequences.get(i);
			String[] sTokens = source.trim().split(" "), tTokens = target.trim().split(" ");
			for (int j = 0; j < sTokens.length; j++) {
				String s = sTokens[j], t = tTokens[j];
				sourceVocabulary.add(s);
				targetVocabulary.add(t);
			}
		}
		ArrayList<String> testSourceSequences = readSource(dir + "/test.s"), testTargetSequences = readSource(dir + "/test.t"),
				translationSequences = readSource(dir + "/test.tune.baseline.trans");
		int numOfAllTokens = 0, numOfAllCorrect = 0, numOfAllOut = 0, numOfCorrectSquences = 0, numOfOutSequences = 0;
		HashMap<String, int[]> numOverPackages = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < testSourceSequences.size(); i++) {
			String source = testSourceSequences.get(i), target = testTargetSequences.get(i), tran = translationSequences.get(i);
			String[] sTokens = source.split(" "), tTokens = target.split(" "), tranTokens = tran.split(" ");
			int numOfTokens = 0, numOfCorrect = 0, numOfOut = 0;
			int j = 0;
			while ( j < sTokens.length) {
				String s = sTokens[j], t = tTokens[j], tr = tranTokens[j];
				if (t.contains(".")) {
					String p = getPackage(t);
					int[] nums = numOverPackages.get(p);
					if (nums == null) {
						nums = new int[3];
						numOverPackages.put(p, nums);
					}
					numOfTokens++;
					nums[0]++;
					if (sourceVocabulary.contains(s) && targetVocabulary.contains(t)) {
						if (tr.equals(t)) {
							numOfCorrect++;
							nums[1]++;
						} else
							sb.append(tr + " (" + t + ") ");
					} else {
						numOfOut++;
						nums[2]++;
						while (j + 1 < sTokens.length) {
							if (sTokens[j+1].charAt(0) == '.') {
								numOfTokens++;
								numOfOut++;
								nums[0]++;
								nums[2]++;
								j++;
							} else break;
						}
					}
				}
				j++;
			}
			if (numOfOut > 0)
				numOfOutSequences++;
			else if (numOfCorrect == numOfTokens)
				numOfCorrectSquences++;
			numOfAllTokens += numOfTokens;
			numOfAllCorrect += numOfCorrect;
			numOfAllOut += numOfOut;
			sb.append("\n");
		}
		FileUtil.writeToFile(dir + "/typeIncorrectTranslate.txt", sb.toString());
		System.out.println("Total: " + numOfAllTokens);
		System.out.println("Correct: " + numOfAllCorrect);
		System.out.println("OOV: " + numOfAllOut);
		System.out.println("Accuracy: " + numOfAllCorrect*1.0 / numOfAllTokens);
		System.out.println("In-vocabulary accuracy: " + numOfAllCorrect*1.0 / (numOfAllTokens - numOfAllOut));
		System.out.println("Sequences: " + testSourceSequences.size());
		System.out.println("Correct sequences: " + numOfCorrectSquences);
		System.out.println("OOV sequences: " + numOfOutSequences);
		System.out.println();
		ArrayList<String> l = new ArrayList<>(numOverPackages.keySet());
		Collections.sort(l);
		for (String p : l) {
			int[] nums = numOverPackages.get(p);
			System.out.print(p);
			for (int num : nums)
				System.out.print("\t" + num);
			System.out.println();
		}
	}

	private static String getPackage(String t) {
		if (t.charAt(0) == '.')
			return t;
		String[] names = t.split("\\.");
		String name = names[0] + "." + names[1];
		if (names.length > 2 && !names[0].equals("java") && !names[0].equals("javax"))
			name += "." + names[2];
		return name;
	}

	private static ArrayList<String> readSource(String path) {
		ArrayList<String> sequences = new ArrayList<>();
		String sources = FileUtil.getFileContent(path);
		Scanner sc = new Scanner(sources);
		while (sc.hasNextLine()) {
			sequences.add(sc.nextLine());
		}
		sc.close();
		return sequences;
	}

}
