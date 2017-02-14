package parser;

public class GenerateSequenceForSOV {

	public static void main(String[] args){
		String fop_githubProject="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_sov3\\TypeResolution_Oracle-master\\";
		String fop_sequence="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_sov3\\";
		
		ProjectSequencesGenerator generator=new ProjectSequencesGenerator(fop_githubProject);
		generator.generateSequences(fop_sequence);
		
		
		
	}
}
