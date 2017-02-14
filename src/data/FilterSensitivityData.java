package data;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.osgi.container.Module.Settings;

import parser.ProjectSequencesGenerator;
import utils.FileUtil;

public class FilterSensitivityData {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_input="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_5libs_newApp\\fold-1\\";
		String fop_output="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_5libs_newApp\\fold-1\\sensitivity_7fold\\";
		ArrayList<String> lstTrainLine=FileUtil.getFileStringArray(fop_input+"train.lines.txt");
		ArrayList<String> lstTuneLine=FileUtil.getFileStringArray(fop_input+"tune.lines.txt");
		ArrayList<String> lstOffset=FileUtil.getFileStringArray(fop_input+"offfset.txt");
		
		HashMap<String,ArrayList<String>> mapTrain=new HashMap<String,ArrayList<String>>();
		HashMap<String,ArrayList<String>> mapTune=new HashMap<String,ArrayList<String>>();
		double ratio=7.0/9;
		
		String[] arrLibraries={"android","com.google.gwt","com.thoughtworks.xstream","org.joda.time","org.hibernate"};
		
		
		PrintStream ptTrainSource=null,ptTrainTarget=null,ptTrainLocation=null,ptTrainLine=null, ptTuneSource=null,ptTuneTarget=null, ptTuneLocation=null,ptTuneLine=null,ptAlignST=null,ptAlignTS=null;
		
		try{
			ptTrainSource=new PrintStream(new FileOutputStream(fop_output+"source.txt"));
			ptTrainTarget=new PrintStream(new FileOutputStream(fop_output+"target.txt"));
			ptTrainLine=new PrintStream(new FileOutputStream(fop_output+"train.lines.txt"));
			ptTrainLocation=new PrintStream(new FileOutputStream(fop_output+"train.locations.txt"));
			ptAlignST=new PrintStream(new FileOutputStream(fop_output+"training.s-t.A3"));
			ptAlignTS=new PrintStream(new FileOutputStream(fop_output+"training.t-s.A3"));
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		//ArrayList<String> lstTrainLines=FileUtil.getFileStringArray(fop_input+"train.lines.txt");
		ArrayList<String> lstTrainLocations=FileUtil.getFileStringArray(fop_input+"train.locations.txt");		
		ArrayList<String> lstTrainSource=FileUtil.getFileStringArray(fop_input+"train.s");
		ArrayList<String> lstTrainTarget=FileUtil.getFileStringArray(fop_input+"train.t");
		
		
		for(int i=0;i<arrLibraries.length;i++){
			ArrayList<String> lstLinePerLib=new ArrayList<String>();
			mapTrain.put(arrLibraries[i], lstLinePerLib);
			int startIndex=Integer.parseInt(lstOffset.get(i).split("\t")[1])-1;
			int endIndex=Integer.parseInt(lstOffset.get(i).split("\t")[1])+(int)(Integer.parseInt(lstOffset.get(i).split("\t")[2])*ratio);
			
//			ArrayList<String> lstLinePerLib2=new ArrayList<String>();
//			mapTune.put(arrLibraries[i], lstLinePerLib2);
			
			for(int j=startIndex;j<endIndex;j++){
				ptTrainSource.print(lstTrainSource.get(j)+"\n");
				ptTrainTarget.print(lstTrainTarget.get(j)+"\n");
				ptTrainLine.print(lstTrainLine.get(j)+"\n");
				ptTrainLocation.print((j+1)+"\t"+lstTrainLocations.get(i)+"\n");
				
			}
		}
		lstTrainSource.clear();
		lstTrainTarget.clear();
		
		ProjectSequencesGenerator psg=new ProjectSequencesGenerator("C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_5libs_newApp\\fold-1\\sensitivity_7fold\\");
		psg.generateAlignment("C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_5libs_newApp\\fold-1\\sensitivity_7fold\\",true);
//		
//		ArrayList<String> arrTrainSt=FileUtil.getFileStringArray(fop_input+"\\align\\training.s-t.A3");
//		
//		for(int i=0;i<arrLibraries.length;i++){
//			ArrayList<String> lstLinePerLib=new ArrayList<String>();
//			mapTrain.put(arrLibraries[i], lstLinePerLib);
//			int startIndex=Integer.parseInt(lstOffset.get(i).split("\t")[1])-1;
//			int endIndex=Integer.parseInt(lstOffset.get(i).split("\t")[1])+(int)(Integer.parseInt(lstOffset.get(i).split("\t")[2])*ratio);
//			
////			ArrayList<String> lstLinePerLib2=new ArrayList<String>();
////			mapTune.put(arrLibraries[i], lstLinePerLib2);
//			
//			for(int j=startIndex;j<endIndex;j++){
//			//	ptAlignST.print((j+1)+"\t"+lstTrainLocations.get(i)+"\n");
//				ptAlignST.print( arrTrainSt.get(j*3)+"\n");
//				ptAlignST.print( arrTrainSt.get(j*3+1)+"\n");
//				ptAlignST.print( arrTrainSt.get(j*3+2)+"\n");						
//				
//			}
//		}
//		
//		arrTrainSt.clear();
//		ArrayList<String> arrTrainTs=FileUtil.getFileStringArray(fop_input+"\\align\\training.s-t.A3");
//		
//		for(int i=0;i<arrLibraries.length;i++){
//			ArrayList<String> lstLinePerLib=new ArrayList<String>();
//			mapTrain.put(arrLibraries[i], lstLinePerLib);
//			int startIndex=Integer.parseInt(lstOffset.get(i).split("\t")[1])-1;
//			int endIndex=Integer.parseInt(lstOffset.get(i).split("\t")[1])+(int)(Integer.parseInt(lstOffset.get(i).split("\t")[2])*ratio);
//			
////			ArrayList<String> lstLinePerLib2=new ArrayList<String>();
////			mapTune.put(arrLibraries[i], lstLinePerLib2);
//			
//			for(int j=startIndex;j<endIndex;j++){
//			//	ptAlignTS.print((j+1)+"\t"+lstTrainLocations.get(i)+"\n");
//				ptAlignTS.print( arrTrainTs.get(j*3)+"\n");
//				ptAlignTS.print( arrTrainTs.get(j*3+1)+"\n");
//				ptAlignTS.print( arrTrainTs.get(j*3+2)+"\n");						
//				
//			}
//		}
//		
//		
////		for(int i=0;i<lstTrainLine.size();i++){
////			String libName=lstTrainLine.get(i).split("\\s+")[1];
////			mapTrain.get(libName).add(lstTrainLine.get(i));
////		}
////		
////		HashSet<Integer> setTrain=new HashSet<Integer>();
////		
////		for(int i=0;i<lstTuneLine.size();i++){
////			String libName=lstTrainLine.get(i).split("\\s+")[1];
////			mapTune.get(libName).add(lstTuneLine.get(i));
////			//setTrain.add(i+1);
////		}
////		
//		
//		
//		int numberNeedFor5Folds=lstTrainLine.size()*5/9;
//		int indexTrain=0;
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		for(int i=0;i<arrLibraries.length;i++){
//			
//		}
//		
		
		
	}

}
