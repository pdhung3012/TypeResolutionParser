package baker;

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
	public void testCorpus() throws Exception {
		int[] numbers = CorpusGenerator.concatSequences("T:/type-sequences", "T:/type-sequences-concat", true);
		for (int n : numbers)
			System.out.println(n);
	}
	

	void test() {
//		BakerSequencesGenerator psg = new BakerSequencesGenerator("resources/testInput", true);
		BakerSequencesGenerator psg = new BakerSequencesGenerator("F:/Study\\Research\\Re-implement LiveAPI\\TypeResolution_Oracle\\src\\androidExamples\\Android39.java", true);
		psg.generateSequences("F:/Study/Research/Re-implement LiveAPI/sequences");
//		psg.updateTokens();
		int[] numbers = psg.generateAlignment(true);
		for (int n : numbers)
			System.out.println(n);
	}
	
}
