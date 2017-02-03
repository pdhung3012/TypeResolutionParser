package parser;

import java.util.HashSet;

import utils.FileUtil;

public class EvaluateInOutPrecisionRecall {

	static String fop_input="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output20170130\\sovProjects\\";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fn_trainSource="train.s";
		String fn_trainTarget="train.t";
		String fn_testSource="test.s";
		String fn_testTarget="test.t";
		String fn_testTranslation="test.tune.baseline.trans";
		String fn_result="result.txt";
		String fn_log_incorrect="log_incorrect.txt";
		
		String[] arrTrainSource=FileUtil.getFileContent(fop_input+fn_trainSource).trim().split("\n");
		String[] arrTrainTarget=FileUtil.getFileContent(fop_input+fn_trainTarget).trim().split("\n");
		String[] arrTestSource=FileUtil.getFileContent(fop_input+fn_testSource).trim().split("\n");
		String[] arrTestTarget=FileUtil.getFileContent(fop_input+fn_testTarget).trim().split("\n");
		String[] arrTestTranslation=FileUtil.getFileContent(fop_input+fn_testTranslation).trim().split("\n");
		String[] arrEvaluatedTypes=FileUtil.getFileContent(fop_input+"evaluatedResults.txt").trim().split("\n");
		
		HashSet<String> setVocabTrainSource=new HashSet<String>();
		HashSet<String> setVocabTrainTarget=new HashSet<String>();
		HashSet<String> setVocabMap=new HashSet<String>();
		
		
		
		HashSet<Integer> lstNotReorderedLine=new HashSet<Integer>();
		
//		for(int i=0;i<arrEvaluatedTypes.length;i++){
//			String[] arrItems=arrEvaluatedTypes[i].split("\t");
//			if(arrItems[1].equals("true")){
//				lstNotReorderedLine.add(i+1);
//			}
//		}
		
		
		for(int i=0;i<arrTrainSource.length;i++){
			String[] itemSource=arrTrainSource[i].trim().split("\\s+");
			String[] itemTarget=arrTrainTarget[i].trim().split("\\s+");
			for(int j=0;j<itemSource.length;j++){
				if(!setVocabTrainSource.contains(itemSource[j])){
					setVocabTrainSource.add(itemSource[j]);
				}				
				if(!setVocabTrainTarget.contains(itemTarget[j])){
					setVocabTrainTarget.add(itemTarget[j]);
				}
				
				if(!setVocabMap.contains(itemSource[j]+"--"+itemTarget[j])){
					setVocabMap.add(itemSource[j]+"--"+itemTarget[j]);
				}								
			}
									
		}
		
		int countOutOfSource=0,countOutOfTarget=0,countAllOutOfVocab=0,countIncorrect=0,countCorrect=0;
		FileUtil.writeToFile(fop_input+fn_result, "Correct"+"\t"+"Incorrect"+"\t"+"Out_of_source"+"\t"+"Out_of_target"+"\t"+"Out_of_vocab"+"\n");
		FileUtil.writeToFile(fop_input+fn_log_incorrect, "");
		
		for(int i=0;i<arrTestSource.length;i++){
//			if(!lstNotReorderedLine.contains(i+1)){
//				continue;
//			}
			String[] itemSource=arrTestSource[i].trim().split("\\s+");
			String[] itemTarget=arrTestTarget[i].trim().split("\\s+");
			String[] itemTrans=arrTestTranslation[i].trim().split("\\s+");
			String strIncorrectLog="";
			
			int numCSourceLine=0,numCTargetLine=0,numIncorrect=0,numCorrect=0;
			System.out.println("Line "+i);
			for(int j=0;j<itemSource.length;j++){
				
				if(itemTarget[j].contains(".")){
					if(!setVocabTrainSource.contains(itemSource[j])){
						numCSourceLine++;
					}
					else if(!setVocabTrainTarget.contains(itemTarget[j])){
						numCTargetLine++;
					}else if(itemTarget[j].equals(itemTrans[j])){
						numCorrect++;
					} else{
						numIncorrect++;
						strIncorrectLog+=itemTrans[j]+"(Correct: "+itemTarget[j]+") ";
					}
				}
												
			}
			countCorrect+=numCorrect;
			countIncorrect+=numIncorrect;
			countOutOfSource+=numCSourceLine;
			countOutOfTarget+=numCTargetLine;
			countAllOutOfVocab+=numCSourceLine+numCTargetLine;
			FileUtil.appendToFile(fop_input+fn_result, numCorrect+"\t"+numIncorrect+"\t"+numCSourceLine+"\t"+numCTargetLine+"\t"+(numCSourceLine+numCTargetLine)+"\n");
			FileUtil.appendToFile(fop_input+fn_log_incorrect, strIncorrectLog+"\n");
			
		}
		FileUtil.appendToFile(fop_input+fn_result, countCorrect+"\t"+countIncorrect+"\t"+countOutOfSource+"\t"+countOutOfTarget+"\t"+countAllOutOfVocab+"\n");
		FileUtil.appendToFile(fop_input+fn_result, "Precision in-vocab: "+countCorrect*1.0/(countCorrect+countIncorrect));
		FileUtil.appendToFile(fop_input+fn_result, "Recall out-vocab: "+countCorrect*1.0/(countCorrect+countIncorrect+countAllOutOfVocab));
		
		
	}

}
