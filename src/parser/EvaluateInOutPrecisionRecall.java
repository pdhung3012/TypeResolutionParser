package parser;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import utils.FileUtil;

public class EvaluateInOutPrecisionRecall {

	static String fop_input="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_stackoverflow\\";
	
	public static boolean checkAPIsInLibrary(HashSet<String> setLib,String token){
		boolean check=false;
		for(String str:setLib){
			if(token.startsWith(str)){
				//System.out.println(token);
				check=true;
				break;
			}
		}
		return check;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fn_trainSource="train.s";
		String fn_trainTarget="train.t";
		String fn_testSource="test.s";
		String fn_testTarget="test.t";
		String fn_testTranslation="test.tune.baseline.trans";
		String fn_result="result.txt";
		String fn_log_incorrect="log_incorrect.txt";
		
		ArrayList<String> arrTrainSource=FileUtil.getFileStringArray(fop_input+fn_trainSource);
		ArrayList<String> arrTestSource=FileUtil.getFileStringArray(fop_input+fn_testSource);
		ArrayList<String> arrTestTarget=FileUtil.getFileStringArray(fop_input+fn_testTarget);
		ArrayList<String> arrTestTranslation=FileUtil.getFileStringArray(fop_input+fn_testTranslation);
		ArrayList<String> arrEvaluatedTypes=FileUtil.getFileStringArray(fop_input+"evaluatedResults.txt");
		
		HashSet<String> setVocabTrainSource=new HashSet<String>();
		HashSet<String> setVocabTrainTarget=new HashSet<String>();
		HashSet<String> setVocabMap=new HashSet<String>();
		
		HashSet<String> set5Libraries=new HashSet<String>();
		set5Libraries.add("android.");
		set5Libraries.add("com.google.gwt.");
		set5Libraries.add("com.thoughtworks.xstream.");
		set5Libraries.add("org.hibernate.");
		set5Libraries.add("org.joda.time.");
		
		set5Libraries.add("org.apache.");
		set5Libraries.add("java.");
		
		
		
		
		HashSet<Integer> lstNotReorderedLine=new HashSet<Integer>();
		
		for(int i=0;i<arrEvaluatedTypes.size();i++){
			String[] arrItems=arrEvaluatedTypes.get(i).split("\t");
			//||arrTestSource.get(i).split("\\s+").length<=7
			if(arrItems.length>=2&&(arrItems[1].equals("true"))){
				lstNotReorderedLine.add(i+1);
			}
		}
		
		
		
		
		for(int i=0;i<arrTrainSource.size();i++){
			String[] itemSource=arrTrainSource.get(i).trim().split("\\s+");
			for(int j=0;j<itemSource.length;j++){
				if(!setVocabTrainSource.contains(itemSource[j])){
					setVocabTrainSource.add(itemSource[j]);
				}				
												
			}
									
		}
		
		arrTrainSource.clear();
		ArrayList<String> arrTrainTarget=FileUtil.getFileStringArray(fop_input+fn_trainTarget);
		
		
		for(int i=0;i<arrTrainTarget.size();i++){
			String[] itemTarget=arrTrainTarget.get(i).trim().split("\\s+");
			for(int j=0;j<itemTarget.length;j++){
								
				if(!setVocabTrainTarget.contains(itemTarget[j])){
					setVocabTrainTarget.add(itemTarget[j]);
				}
												
			}
									
		}
		
		arrTrainTarget.clear();
		
		
		
		int countOutOfSource=0,countOutOfTarget=0,countAllOutOfVocab=0,countIncorrect=0,countCorrect=0;
		FileUtil.writeToFile(fop_input+fn_result, "Correct"+"\t"+"Incorrect"+"\t"+"Out_of_source"+"\t"+"Out_of_target"+"\t"+"Out_of_vocab"+"\n");
		FileUtil.writeToFile(fop_input+fn_log_incorrect, "");
		
		PrintStream ptResult=null,ptIncorrect=null;
		try{
			ptResult=new PrintStream(new FileOutputStream(fop_input+fn_result));
			ptIncorrect=new PrintStream(new FileOutputStream(fop_input+fn_log_incorrect));
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		for(int i=0;i<arrTestSource.size();i++){
			if(!lstNotReorderedLine.contains(i+1)){
				continue;
			}
			HashSet<String> setIncorrect=new HashSet<String>();
			String[] itemSource=arrTestSource.get(i).trim().split("\\s+");
			String[] itemTarget=arrTestTarget.get(i).trim().split("\\s+");
			String[] itemTrans=arrTestTranslation.get(i).trim().split("\\s+");
			String strIncorrectLog="";
			
			int numCSourceLine=0,numCTargetLine=0,numIncorrect=0,numCorrect=0;
			System.out.println("Line "+i);
			for(int j=0;j<itemSource.length;j++){
				
				//if(itemTarget[j].contains(".")){
				if(checkAPIsInLibrary(set5Libraries, itemTarget[j])){
				if(!setVocabTrainSource.contains(itemSource[j])){
						numCSourceLine++;
					}
					else if(!setVocabTrainTarget.contains(itemTarget[j])){
						numCTargetLine++;
					}else if(itemTarget[j].equals(itemTrans[j])){
						numCorrect++;
					} else{
						numIncorrect++;
						if(!setIncorrect.contains(itemTrans[j]+"(Correct: "+itemTarget[j]+") ")){
							strIncorrectLog+=itemTrans[j]+"(Correct: "+itemTarget[j]+") ";
							setIncorrect.add(itemTrans[j]+"(Correct: "+itemTarget[j]+") ");
						}
						
					}
				}
												
			}
			countCorrect+=numCorrect;
			countIncorrect+=numIncorrect;
			countOutOfSource+=numCSourceLine;
			countOutOfTarget+=numCTargetLine;
			countAllOutOfVocab+=numCSourceLine+numCTargetLine;
			ptResult.print("Line "+(i+1)+" (correct/incorrect/OOS/OOT/OOV): "+numCorrect+"\t"+numIncorrect+"\t"+numCSourceLine+"\t"+numCTargetLine+"\t"+(numCSourceLine+numCTargetLine)+"\n");
			ptIncorrect.print("Line "+(i+1)+" (correct/incorrect/OOS/OOT/OOV): "+strIncorrectLog+"\n");
			//FileUtil.appendToFile(fop_input+fn_result, numCorrect+"\t"+numIncorrect+"\t"+numCSourceLine+"\t"+numCTargetLine+"\t"+(numCSourceLine+numCTargetLine)+"\n");
	//		FileUtil.appendToFile(fop_input+fn_log_incorrect, strIncorrectLog+"\n");
			
		}
		
		try{
			ptResult.close();
			ptIncorrect.close();
		}catch(Exception ex){
			
		}
		
		FileUtil.appendToFile(fop_input+fn_result, countCorrect+"\t"+countIncorrect+"\t"+countOutOfSource+"\t"+countOutOfTarget+"\t"+countAllOutOfVocab+"\n");
//		FileUtil.appendToFile(fop_input+fn_result, "Precision in-vocab: "+countCorrect*1.0/(countCorrect+countIncorrect)+"\n");
//		FileUtil.appendToFile(fop_input+fn_result, "Recall out-vocab: "+countCorrect*1.0/(countCorrect+countIncorrect+countAllOutOfVocab)+"\n");
		
		
	}

}
