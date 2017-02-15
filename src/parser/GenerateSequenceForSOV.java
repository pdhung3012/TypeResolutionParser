package parser;

import java.io.File;

public class GenerateSequenceForSOV {

	public static void main(String[] args){
		String fop_githubProject="C:\\Users\\pdhung\\Documents\\OracleProjects\\";
		String fop_sequence="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_sov5\\";
		
		File[] arrProj=new File(fop_githubProject).listFiles();
		
		for(File f:arrProj){
			System.out.println(f.getAbsolutePath());
			ProjectSequencesGenerator generator=new ProjectSequencesGenerator(f.getAbsolutePath());
			generator.generateSequences(fop_sequence+"\\"+f.getName()+"\\");			
		}
		
		
		
		
	}
}
