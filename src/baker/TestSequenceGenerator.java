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
//		BakerSequencesGenerator psg = new BakerSequencesGenerator("resources/testInput", "", true);
		BakerSequencesGenerator psg = new BakerSequencesGenerator("F:/Study\\Research\\Re-implement LiveAPI\\TypeResolution_Oracle\\01-Android\\src\\androidExamples\\","F:\\Study\\Research\\Re-implement LiveAPI\\TypeResolution_Oracle\\01-Android\\lib\\", true);
//		BakerSequencesGenerator psg = new BakerSequencesGenerator("F:\\Study\\Research\\Re-implement LiveAPI\\TypeResolution_Oracle\\02-GWT\\src\\gwt\\","F:\\Study\\Research\\Re-implement LiveAPI\\TypeResolution_Oracle\\02-GWT\\lib\\", true);
//		BakerSequencesGenerator psg = new BakerSequencesGenerator("F:/Study\\Research\\Re-implement LiveAPI\\TypeResolution_Oracle\\03-Hibernate\\src\\hibernate\\","F:/Study\\Research\\Re-implement LiveAPI\\TypeResolution_Oracle\\03-Hibernate\\lib\\", true);
//		BakerSequencesGenerator psg = new BakerSequencesGenerator("F:\\Study\\Research\\Re-implement LiveAPI\\TypeResolution_Oracle\\04-Jodatime\\src\\jodatime","F:\\Study\\Research\\Re-implement LiveAPI\\TypeResolution_Oracle\\04-Jodatime\\lib\\", true);
//		BakerSequencesGenerator psg = new BakerSequencesGenerator("F:\\Study\\Research\\Re-implement LiveAPI\\TypeResolution_Oracle\\05-Xstream\\src\\xstream\\","F:\\Study\\Research\\Re-implement LiveAPI\\TypeResolution_Oracle\\05-Xstream\\lib\\", true);
		psg.generateSequences("F:/Study/Research/Re-implement LiveAPI/sequences");
//		psg.updateTokens();
		int[] numbers = psg.generateAlignment(true);
		for (int n : numbers)
			System.out.println(n);
	}
	
}
