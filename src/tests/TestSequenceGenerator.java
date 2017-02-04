package tests;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import parser.CorpusGenerator;
import parser.ProjectSequencesGenerator;

public class TestSequenceGenerator {
    @Rule
    public TestName name = new TestName();

	public static void main(String[] args) throws Exception {
		new TestSequenceGenerator().test();
	}
	
	@Test
	public void testGenAlignments() throws Exception {
		int[] numbers = ProjectSequencesGenerator.generateAlignment("T:/type-sequences-concat", true);
		for (int n : numbers)
			System.out.println(n);
	}
	
	@Test
	public void testCorpus() throws Exception {
		int[] numbers = CorpusGenerator.concatSequences("T:/type-sequences", "T:/type-sequences-concat", true);
		for (int n : numbers)
			System.out.println(n);
	}
	
	@Test
	public void testLengthAndContainment() throws Exception {
//		ProjectSequencesGenerator psg = new ProjectSequencesGenerator(".", true);
//		ProjectSequencesGenerator psg = new ProjectSequencesGenerator("resources", true);
//		ProjectSequencesGenerator psg = new ProjectSequencesGenerator("T:/repos/lucene-solr", true);
		ProjectSequencesGenerator psg = new ProjectSequencesGenerator("F:/github/repos-IntelliJ/JetBrains/intellij-community", true);
		psg.generateSequences("T:/temp/statType/test");
//		for (int i = 0; i < psg.getLocations().size(); i++) {
//			System.out.println(psg.getLocations().get(i));
//			System.out.println(psg.getSourceSequences().get(i));
//			System.out.println(psg.getTargetSequences().get(i));
//			String[] ss = psg.getSourceSequenceTokens().get(i), ts = psg.getTargetSequenceTokens().get(i);
//			assertThat(ss.length, is(ts.length));
//			for (int j = 0; j < ss.length; j++) {
//				String s = ss[j], t = ts[j];
//				assertThat(s.equals(t) || t.endsWith(s), is(true));
//			}
//		}
	}
	
	void test() {
//		ProjectSequencesGenerator psg = new ProjectSequencesGenerator(".", true);
//		ProjectSequencesGenerator psg = new ProjectSequencesGenerator("resources", true);
		ProjectSequencesGenerator psg = new ProjectSequencesGenerator("T:/repos/lucene-solr", true);
//		ProjectSequencesGenerator psg = new ProjectSequencesGenerator("F:/github/repos-IntelliJ/JetBrains/intellij-community", true);
//		ProjectSequencesGenerator psg = new ProjectSequencesGenerator("G:/github/repos-5stars-50commits/apache/camel", true);
		psg.generateSequences("T:/temp/statType/test");
//		psg.updateTokens();
		int[] numbers = psg.generateAlignment(true);
		for (int n : numbers)
			System.out.println(n);
	}
	
}
