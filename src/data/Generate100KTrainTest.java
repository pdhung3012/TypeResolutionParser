package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import utils.FileUtil;



public class Generate100KTrainTest {
	
	public static void evaluateLibraryPerMethod(HashMap<String,Integer> mapMethod,String targetToken){
		String[] arrTargetTokens=targetToken.split("\\s+");
		for(int i=0;i<arrTargetTokens.length;i++){
			
			for(String itemKey:mapMethod.keySet()){
				if(arrTargetTokens[i].contains(itemKey)){
					mapMethod.put(itemKey, mapMethod.get(itemKey)+1);
				}
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_logProject="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\log_extractData\\";
		String fop_logProgram="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\log_program\\";
		String fop_sequenceInput="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\5 libs live API\\type-sequences\\";
		String fop_output="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output\\";
		HashMap<String,Integer> map5LibrariesCount=new HashMap<String,Integer>();
//		map5LibrariesCount.put("org.joda.time", 0);
//		map5LibrariesCount.put("android", 0);
//		map5LibrariesCount.put("gwt",0);
//		map5LibrariesCount.put("xstream", 0);
//		map5LibrariesCount.put("hibernate", 0);
		
		File dirRepo=new File(fop_sequenceInput);
		
		File[] arrLstFiles=dirRepo.listFiles();
		int indexLstFile=0;
//		FileUtil.writeToFile(fop_output+"all.source.txt", "");
//		FileUtil.writeToFile(fop_output+"all.target.txt", "");
//		FileUtil.writeToFile(fop_output+"all.location.txt", "");
//		FileUtil.writeToFile(fop_output+"all.training.s-t.A3", "");
//		FileUtil.writeToFile(fop_output+"all.training.t-s.A3", "");
		
		PrintStream stSource=null,stTarget=null,stLocation=null,stTrainSt=null,stTrainTs=null;
		
		
		
		HashSet<String> setMethodInfo=new HashSet<String>();
		int numberOfMethods=0;

		for(int i=0;i<arrLstFiles.length;i++){
			
			try {
				stSource = new PrintStream(new FileOutputStream(fop_output+arrLstFiles[i].getName()+".source.txt"));
				stTarget = new PrintStream(new FileOutputStream(fop_output+arrLstFiles[i].getName()+".target.txt"));
				stLocation = new PrintStream(new FileOutputStream(fop_output+arrLstFiles[i].getName()+".location.txt"));
				stTrainSt = new PrintStream(new FileOutputStream(fop_output+arrLstFiles[i].getName()+".training.s-t.A3"));
				stTrainTs = new PrintStream(new FileOutputStream(fop_output+arrLstFiles[i].getName()+".training.t-s.A3"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			System.out.println(arrLstFiles[i].getAbsolutePath());
			//int numberMethodsRequired=20000;
			
			//int numberL1PerProject=0,numberL2PerProject=0,numberL3PerProject=0,numberL4PerProject=0,numberL5PerProject=0;
			
			String fn_libName=arrLstFiles[i].getName();
			File[] arrProjects=arrLstFiles[i].listFiles();
			//String[] arrLibName=fn_libName.split("\\.");
			
			for(int j=0;j<arrProjects.length;j++){
				//System.out.println(arrProjects[j].getAbsolutePath());
				
				String projName=arrProjects[j].getName();
				if(projName.endsWith("alignment")){
					continue;
				}
				//System.out.println(arrProjects[j]);
				//String projName=arrProjects[j].split("\t")[0].replace("/", "_");
//				if(existProjects.contains(projName)){
//					continue;
//				} else{
//					existProjects.add(projName);
//					
//				}
				
				
				
				String fp_locations=arrProjects[j].getAbsolutePath()+"\\locations.txt";
				String fp_source=arrProjects[j].getAbsolutePath()+"\\source.txt";
				String fp_target=arrProjects[j].getAbsolutePath()+"\\target.txt";
				String fp_alignmentST=arrProjects[j].getAbsolutePath()+"-alignment\\training.s-t.A3";
				String fp_alignmentTS=arrProjects[j].getAbsolutePath()+"-alignment\\training.t-s.A3";
				
				String[] arrLocation=utils.FileUtil.getFileContent(fp_locations).split("\n");
				String[] arrSource=utils.FileUtil.getFileContent(fp_source).split("\n");
				String[] arrTarget=utils.FileUtil.getFileContent(fp_target).split("\n");
				String[] arrAlignST=utils.FileUtil.getFileContent(fp_alignmentST).split("\n");
				String[] arrAlignTS=utils.FileUtil.getFileContent(fp_alignmentTS).split("\n");
				
				if(arrAlignST.length==0){
					continue;
				}
				
				
				
				for(int k=0;k<arrTarget.length;k++){
					String[] arrLocationInfo=arrLocation[k].split("\t");
					String signaturePerMethod="";
					for(int q=0;q<arrLocationInfo.length-3;q++){
						signaturePerMethod+=arrLocationInfo[q]+" ";
					}
					
					
					String percentResolve=arrLocationInfo[arrLocationInfo.length-1];
					String[] arrTokenTarget=arrTarget[k].trim().split("\\s+");
				//	System.out.println(percentResolve);
//					boolean checkStartWithTokens=false;
//					for(int q=0;q<arrTokenTarget.length;q++){
//						if(arrTokenTarget[q].contains(fn_libName)){
//							checkStartWithTokens=true;
//							break;
//						}
//					}
					if(percentResolve.equals("100%")&&!setMethodInfo.contains(signaturePerMethod)){
						//add to corpus
						setMethodInfo.add(signaturePerMethod);
						//if(k*3<arrAlignST.length){
							setMethodInfo.add(arrLocation[k]);
							numberOfMethods++;
							stSource.print(arrSource[k]+"\n");
							stTarget.print(arrTarget[k]+"\n");
							stLocation.print( arrLocation[k]+"\n");
							stTrainSt.print(  arrAlignST[k*3]+"\n");
							stTrainSt.print( arrAlignST[k*3+1]+"\n");
							stTrainSt.print( arrAlignST[k*3+2]+"\n");						
							stTrainTs.print(  arrAlignTS[k*3]+"\n");
							stTrainTs.print( arrAlignTS[k*3+1]+"\n");
							stTrainTs.print( arrAlignTS[k*3+2]+"\n");
							
						
						
					}
				}
				//FileUtil.appendToFile(fop_logProgram+"log_projects.txt",  fn_libName+"\t"+projName+"\t"+numberOfMethods+"\n");
//				if(indexMethodRequired>numberMethodsRequired){
//					break;
//				}
			
			}
			
		}
		
		System.out.println("Finish combine corpus! "+numberOfMethods+" in corpus");
		System.out.println("Start created 10 fold");
	//	System.out.println("Start created 10 fold");
		
		//create folder to store 10 fold
		for(int i=1;i<=10;i++){
			String fn_fold="fold-"+i;
			File fFold=new File(fop_output+"\\"+fn_fold+"\\");
			if(!fFold.isDirectory()){
				fFold.mkdir();
			}
		}
		
		PrintStream[] arrPrtTestSource=new PrintStream[10];
		PrintStream[] arrPrtTestTarget=new PrintStream[10];
		PrintStream[] arrPrtTrainSource=new PrintStream[10];
		PrintStream[] arrPrtTrainTarget=new PrintStream[10];
		PrintStream[] arrPrtTrainAlignS2T=new PrintStream[10];
		PrintStream[] arrPrtTrainAlignT2S=new PrintStream[10];
		PrintStream[] arrPrtTuneSource=new PrintStream[10];
		PrintStream[] arrPrtTuneTarget=new PrintStream[10];
		PrintStream[] arrPrtTuneLocation=new PrintStream[10];
		PrintStream[] arrPrtTestLocation=new PrintStream[10];
		PrintStream[] arrPrtTrainLocation=new PrintStream[10];		
		PrintStream[] arrPrtTuneLine=new PrintStream[10];
		PrintStream[] arrPrtTestLine=new PrintStream[10];
		PrintStream[] arrPrtTrainLine=new PrintStream[10];
		
		for(int j=0;j<10;j++){

			try {
				
				arrPrtTestTarget[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\test.t",true));
				arrPrtTestSource[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\test.s",true));
				arrPrtTrainSource[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\train.s",true));
				arrPrtTrainTarget[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\train.t",true));
				arrPrtTuneSource[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\tune.s",true));
				arrPrtTuneTarget[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\tune.t",true));
				arrPrtTuneLocation[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\tune.locations.txt",true));
				arrPrtTestLocation[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\test.locations.txt",true));
				arrPrtTrainLocation[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\train.locations.txt",true));
				arrPrtTuneLine[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\tune.lines.txt",true));
				arrPrtTestLine[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\test.lines.txt",true));
				arrPrtTrainLine[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\train.lines.txt",true));
				arrPrtTrainAlignS2T[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\training.s-t.A3",true));
				arrPrtTrainAlignT2S[j]=new PrintStream(new FileOutputStream(fop_output+"\\fold-"+(j+1)+"\\training.t-s.A3",true));
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		for(int i=0;i<1;i++){
			ArrayList<String> arrSource = FileUtil.getFileStringArray(fop_output+arrLstFiles[i].getName()+".source.txt");
			ArrayList<String> arrTarget = FileUtil.getFileStringArray(fop_output+arrLstFiles[i].getName()+".target.txt");
			ArrayList<String> arrLocation = FileUtil.getFileStringArray(fop_output+arrLstFiles[i].getName()+".location.txt");
			
			int lengthOfPairs=arrSource.size();
			
			HashMap<Integer,HashSet<Integer>> mapTestOver10Fold=new HashMap<Integer,HashSet<Integer>>();
			HashMap<Integer,HashSet<Integer>> mapTuneOver10Fold=new HashMap<Integer,HashSet<Integer>>();
			
			int numberForTestInLib=lengthOfPairs/10;
			int numberForTuneInLib=lengthOfPairs/10;
			System.out.println("begin lib "+arrLstFiles[i]);
			
			//create hash set to store all possible test line after each fold;
			//from fold 1 to fold 10, the list will be reduced
			ArrayList<Integer> listPossibleTestPerEachFold=new ArrayList<Integer>();
			for(int j=0;j<arrSource.size();j++){
				listPossibleTestPerEachFold.add(j);
			}
			
			for(int indexFold=1;indexFold<=10;indexFold++){
				int indexForTest=0;
				HashSet<Integer> setTestPerFold=new HashSet<Integer>();
				HashSet<Integer> setTunePerFold=new HashSet<Integer>();
				
				if(indexFold<10){
					while ( indexForTest<=numberForTestInLib ){
						indexForTest++;
						int randomIndexForTest=randInt(0, listPossibleTestPerEachFold.size()-1);
						if(!setTestPerFold.contains(randomIndexForTest)){
							setTestPerFold.add(listPossibleTestPerEachFold.get(randomIndexForTest));
							listPossibleTestPerEachFold.remove(randomIndexForTest);
						}				
					}
				}else{
					while(listPossibleTestPerEachFold.size()>0){
						setTestPerFold.add(listPossibleTestPerEachFold.get(0));
						listPossibleTestPerEachFold.remove(0);
					}
				}
				
				int indexForTune=0;
				while ( indexForTune<numberForTuneInLib ){				
					if(!setTestPerFold.contains(indexForTune)){
						setTunePerFold.add(indexForTune);
						
					}
					indexForTune++;
				}
				
				mapTestOver10Fold.put(indexFold, setTestPerFold);
				mapTuneOver10Fold.put(indexFold, setTunePerFold);
								
			}
			
			for(int indexFold=1;indexFold<=10;indexFold++){
				HashSet<Integer> setTest=mapTestOver10Fold.get(indexFold);
				HashSet<Integer> setTune=mapTuneOver10Fold.get(indexFold);
				
				//source & target & location
				for(int j=0;j<arrSource.size();j++){
					
					if(setTest.contains(j)){
						arrPrtTestSource[indexFold-1].print(arrSource.get(j)+"\n");
						arrPrtTestTarget[indexFold-1].print(arrTarget.get(j)+"\n");
						arrPrtTestLocation[indexFold-1].print(arrLocation.get(j)+"\n");
						arrPrtTestLine[indexFold-1].print((j+1)+"\t"+arrLstFiles[i].getName()+"\n");
						
					} else{
						arrPrtTrainSource[indexFold-1].print(arrSource.get(j)+"\n");
						arrPrtTrainTarget[indexFold-1].print(arrTarget.get(j)+"\n");
						arrPrtTrainLocation[indexFold-1].print(arrLocation.get(j)+"\n");
						arrPrtTrainLine[indexFold-1].print((j+1)+"\t"+arrLstFiles[i].getName()+"\n");
						
						if(setTune.contains(j)){
							arrPrtTuneSource[indexFold-1].print(arrSource.get(j)+"\n");
							arrPrtTuneTarget[indexFold-1].print(arrTarget.get(j)+"\n");
							arrPrtTuneLocation[indexFold-1].print(arrLocation.get(j)+"\n");
							arrPrtTuneLine[indexFold-1].print((j+1)+"\t"+arrLstFiles[i].getName()+"\n");
						}
						
					}
				}
				System.out.println("fold "+indexFold);

			}
			
			
			
			arrSource.clear();
			arrTarget.clear();
			arrLocation.clear();
			
			//append for align s2t
			ArrayList<String> arrTrainSt = FileUtil.getFileStringArray(fop_output+arrLstFiles[i].getName()+".training.s-t.A3");			
			for(int indexFold=1;indexFold<=10;indexFold++){
				HashSet<Integer> setTest=mapTestOver10Fold.get(indexFold);
				
				//source & target & location
				for(int j=0;j<lengthOfPairs;j++){
					
					if(setTest.contains(j)){
						
					} else{
						arrPrtTrainAlignS2T[indexFold-1].print( arrTrainSt.get(j*3)+"\n");
						arrPrtTrainAlignS2T[indexFold-1].print( arrTrainSt.get(j*3+1)+"\n");
						arrPrtTrainAlignS2T[indexFold-1].print( arrTrainSt.get(j*3+2)+"\n");						
						
						
					}
				}
				System.out.println("fold "+indexFold);

			}
			arrTrainSt.clear();
			//append for align t2s
			ArrayList<String> arrTrainTs = FileUtil.getFileStringArray(fop_output+arrLstFiles[i].getName()+".training.t-s.A3");			
			for(int indexFold=1;indexFold<=10;indexFold++){
				HashSet<Integer> setTest=mapTestOver10Fold.get(indexFold);
				
				//source & target & location
				for(int j=0;j<lengthOfPairs;j++){
					
					if(setTest.contains(j)){
						
					} else{
						arrPrtTrainAlignT2S[indexFold-1].print(  arrTrainTs.get(j*3)+"\n");
						arrPrtTrainAlignT2S[indexFold-1].print( arrTrainTs.get(j*3+1)+"\n");
						arrPrtTrainAlignT2S[indexFold-1].print( arrTrainTs.get(j*3+2)+"\n");
						
						
					}
				}
				System.out.println("fold "+indexFold);

			}
			arrTrainTs.clear();
			
			
			
		}
		
		for(int j=0;j<10;j++){

			try {
				
				arrPrtTestTarget[j].close();;
				arrPrtTestSource[j].close();
				arrPrtTrainSource[j].close();
				arrPrtTrainTarget[j].close();;
				arrPrtTuneSource[j].close();
				arrPrtTuneTarget[j].close();
				arrPrtTuneLocation[j].close();
				arrPrtTestLocation[j].close();
				arrPrtTrainLocation[j].close();
				arrPrtTuneLine[j].close();
				arrPrtTestLine[j].close();
				arrPrtTrainLine[j].close();
				arrPrtTrainAlignS2T[j].close();
				arrPrtTrainAlignT2S[j].close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
	}
	
	public static int randInt(int min, int max) {

	    // NOTE: This will (intentionally) not run as written so that folks
	    // copy-pasting have to think about how to initialize their
	    // Random instance.  Initialization of the Random instance is outside
	    // the main scope of the question, but some decent options are to have
	    // a field that is initialized once and then re-used as needed or to
	    // use ThreadLocalRandom (if using at least Java 1.7).
	    Random rand;

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum =  ThreadLocalRandom.current().nextInt(min, max + 1);

	    return randomNum;
	}

}
