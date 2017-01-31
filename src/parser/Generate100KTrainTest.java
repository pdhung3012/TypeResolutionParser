package parser;

import java.io.File;
import java.util.HashSet;

import utils.FileUtil;


public class Generate100KTrainTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_repository="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\5Repositories\\";
		String fop_sequenceInput="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output20170130\\";
		String fop_output="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output100K\\";
		
		File dirRepo=new File(fop_repository);
		File[] arrLstFiles=dirRepo.listFiles();
		
		for(int i=0;i<arrLstFiles.length;i++){
			int numberMethodsRequired=20000;
			int indexMethodRequired=0;
			String[] arrProjects=utils.FileUtil.getFileContent(arrLstFiles[i].getAbsolutePath()).trim().split("\n");
			String fn_libName=arrLstFiles[i].getName().replace("repos-5stars-50commits-lib-", "").replace("..csv", "");
			HashSet<String> existProjects=new HashSet<String>(); 
			
			FileUtil.writeToFile(fop_output+fn_libName+".source.txt", "");
			FileUtil.writeToFile(fop_output+fn_libName+".target.txt", "");
			FileUtil.writeToFile(fop_output+fn_libName+".location.txt", "");
			FileUtil.writeToFile(fop_output+fn_libName+".training-s-t.txt", "");
			FileUtil.writeToFile(fop_output+fn_libName+".training-t-s.txt", "");
			
			for(int j=0;j<arrProjects.length;j++){
				
				System.out.println(arrProjects[j]);
				String projName=arrProjects[j].split(",")[0].replace("/", "_");
				if(existProjects.contains(projName)){
					continue;
				} else{
					existProjects.add(projName);
					
				}
				String fp_locations=fop_sequenceInput+"\\"+projName+"\\locations.txt";
				String fp_source=fop_sequenceInput+"\\"+projName+"\\source.txt";
				String fp_target=fop_sequenceInput+"\\"+projName+"\\target.txt";
				String fp_alignmentST=fop_sequenceInput+"\\"+projName+"\\-alignment\\training.s-t.A3";
				String fp_alignmentTS=fop_sequenceInput+"\\"+projName+"\\-alignment\\training.t-s.A3";
				
				String[] arrLocation=utils.FileUtil.getFileContent(fp_locations).split("\n");
				String[] arrSource=utils.FileUtil.getFileContent(fp_source).split("\n");
				String[] arrTarget=utils.FileUtil.getFileContent(fp_target).split("\n");
				String[] arrAlignST=utils.FileUtil.getFileContent(fp_alignmentST).split("\n");
				String[] arrAlignTS=utils.FileUtil.getFileContent(fp_alignmentTS).split("\n");
				
				for(int k=0;k<arrTarget.length;k++){
					String[] arrLocationInfo=arrLocation[k].split("\\t");
					String percentResolve=arrLocationInfo[arrLocationInfo.length-1];
					String[] arrTokenTarget=arrTarget[k].trim().split("\\s+");
					System.out.println(percentResolve);
					if(percentResolve.equals("100%")&&arrTokenTarget.length>=3&&arrTokenTarget.length<25&&arrTarget[k].contains(fn_libName)){
						//add to corpus
						
						FileUtil.appendToFile(fop_output+fn_libName+".source.txt", arrSource[k]+"\n");
						FileUtil.appendToFile(fop_output+fn_libName+".target.txt", arrTarget[k]+"\n");
						FileUtil.appendToFile(fop_output+fn_libName+".location.txt", arrLocation[k]+"\n");
						FileUtil.appendToFile(fop_output+fn_libName+".training-s-t.txt", arrAlignST[k*3]+"\n");
						FileUtil.appendToFile(fop_output+fn_libName+".training-s-t.txt", arrAlignST[k*3+1]+"\n");
						FileUtil.appendToFile(fop_output+fn_libName+".training-s-t.txt", arrAlignST[k*3+2]+"\n");
						
						FileUtil.appendToFile(fop_output+fn_libName+".training-t-s.txt", arrAlignTS[k*3]+"\n");
						FileUtil.appendToFile(fop_output+fn_libName+".training-t-s.txt", arrAlignTS[k*3+1]+"\n");
						FileUtil.appendToFile(fop_output+fn_libName+".training-t-s.txt", arrAlignTS[k*3+2]+"\n");
						indexMethodRequired++;
					}
				}
				
				if(indexMethodRequired>=numberMethodsRequired){
					break;
				}
			}
			
		}
		
	}

}
