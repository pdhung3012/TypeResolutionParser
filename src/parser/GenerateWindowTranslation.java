package parser;

import java.util.ArrayList;
import java.util.HashSet;

import utils.FileUtil;

public class GenerateWindowTranslation {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_input="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output100K\\reordered\\";
		String[] arrFilesSource=FileUtil.getFileContent(fop_input+"test.s").trim().split("\n");
		String[] arrFilesTarget=FileUtil.getFileContent(fop_input+"test.t").trim().split("\n");
		int windowSize=6;
		
		String fn_testFilterSource="test_reorder.s",fn_testFilterTarget="test_reorder.t",fn_line="test_order_line.txt";
		
		HashSet<Integer> lstReorderedLine=new HashSet<Integer>();
		
			
		
		FileUtil.writeToFile(fop_input+fn_testFilterSource, "");
		FileUtil.writeToFile(fop_input+fn_testFilterTarget, "");
		FileUtil.writeToFile(fop_input+fn_line, "");
		
		for(Integer index:lstReorderedLine){
			String[] arrItemSource=arrFilesSource[index-1].trim().split("\\s+");
			String[] arrItemTarget=arrFilesTarget[index-1].trim().split("\\s+");
			if(arrItemSource.length<windowSize+1){
			//	FileUtil.appendToFile(fop_dir+"test_filter.s", a);
				break;
			} else{
				
			}
			for(int i=0;i<arrItemSource.length;i++){
				String strTranslateSource="",strTranslateTarget="";
				int realPosInSmallString=0;
				if(i<(windowSize/2)){
					for(int j=0;j<windowSize+1;j++){
						strTranslateSource+=arrItemSource[j]+" ";
						strTranslateTarget+=arrItemTarget[j]+" ";
						
					}
					realPosInSmallString=i+1;
	
				}else if(i>(arrItemSource.length-windowSize/2-1)){
					for(int j=arrItemSource.length-windowSize-1;j<arrItemSource.length;j++){
						strTranslateSource+=arrItemSource[j]+" ";
						strTranslateTarget+=arrItemTarget[j]+" ";
					}
					realPosInSmallString= windowSize-(arrItemSource.length-i-2);
				} else{
					for(int j=i-windowSize/2;j<=i+windowSize/2;j++){
						System.out.println(j+" -- "+i+" -- "+" -- "+arrItemSource.length+" -- "+index);
						strTranslateSource+=arrItemSource[j]+" ";
						strTranslateTarget+=arrItemTarget[j]+" ";
					}
					realPosInSmallString=windowSize/2+1;
				}
				FileUtil.appendToFile(fop_input+fn_testFilterSource, strTranslateSource.trim()+"\n");
				FileUtil.appendToFile(fop_input+fn_testFilterTarget, strTranslateTarget.trim()+"\n");
				FileUtil.appendToFile(fop_input+fn_line, (i+1)+"\t"+realPosInSmallString+"\t"+index+"\n");
			}
		}
		
		

	}

}
