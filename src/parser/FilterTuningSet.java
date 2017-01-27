package parser;

import java.util.HashSet;

import utils.FileUtil;

public class FilterTuningSet {
	
	static int countNumberTarger(String strTarget){
		int numCount=0;
		String[] arrItems=strTarget.trim().split("\\s");
		for(int j=0;j<arrItems.length;j++){
			if(!arrItems[j].trim().isEmpty()&&arrItems[j].trim().contains(".")&&!arrItems[j].trim().startsWith(".")){
				//setVocab.add(arrItems[j]);
				numCount++;
			}
		}
		return numCount;
	}
	
	public static void main(String[] args){
		String[] arrTrainSource=FileUtil.getFileContent("C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output20170125\\train.s").trim().split("\n");
		String[] arrTrainTarget=FileUtil.getFileContent("C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output20170125\\train.t").trim().split("\n");
		String[] arrLocationTarget=FileUtil.getFileContent("C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output20170125\\train_location.t").trim().split("\n");
		String fp_tuneSource="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output20170125\\tune.s";
		String fp_tuneTarget="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output20170125\\tune.t";
		String fp_tuneLocation="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output20170125\\tune_location.t";
		
		int countVocabulary=0;
		HashSet<String> setVocab=new HashSet<String>();
		int numTuneSet=500;
		
		for(int i=0;i<arrTrainTarget.length;i++){
			String[] arrItems=arrTrainTarget[i].trim().split("\\s+");
			for(int j=0;j<arrItems.length;j++){
				if(!arrItems[j].trim().isEmpty()&&!setVocab.contains(arrItems[j].trim())){
					setVocab.add(arrItems[j]);
				}
			}
		}
		
//		  for(int i=0; i < arrTrainTarget.length; i++){  
//              for(int j=1; j < (arrTrainTarget.length-i); j++){  
//                       if(countNumberTarger(arrTrainTarget[j-1]) < countNumberTarger(arrTrainTarget[j])){  
//                              //swap elements  
//                    	   		System.out.println(i+"---"+j);
//                              String temp = arrTrainTarget[j-1];  
//                              arrTrainTarget[j-1] = arrTrainTarget[j];  
//                              arrTrainTarget[j] = temp;
//                              
//                              temp = arrTrainSource[j-1];  
//                              arrTrainSource[j-1] = arrTrainSource[j];  
//                              arrTrainSource[j] = temp;
//                      }  
//                       
//              }  
//              
//              
//      }
		
		quickSort(arrTrainTarget, arrTrainSource, 0, arrTrainTarget.length-1);
		
	  FileUtil.writeToFile(fp_tuneSource, "");
	  FileUtil.writeToFile(fp_tuneTarget, "");
	  int index=0,i=0;
	  while(index<numTuneSet){
		  if(arrTrainTarget[i].trim().split("\\s+").length>10&&arrTrainTarget[i].trim().split("\\s+").length<=50){
			  FileUtil.appendToFile(fp_tuneSource, arrTrainSource[i].trim()+"\n");
			  FileUtil.appendToFile(fp_tuneTarget, arrTrainTarget[i].trim()+"\n");
			  index++;

		  }
		  i++;
  	  }
		
		System.out.println(setVocab.size());
		
	}
	
	public static void quickSort(String[] arr,String[] arr2, int low, int high) {
		if (arr == null || arr.length == 0)
			return;
 
		if (low >= high)
			return;
 
		// pick the pivot
		int middle = low + (high - low) / 2;
		int pivot = countNumberTarger(arr[middle]);
 
		// make left < pivot and right > pivot
		int i = low, j = high;
		while (i <= j) {
			while (countNumberTarger(arr[i]) > pivot) {
				i++;
			}
 
			while (countNumberTarger(arr[j]) < pivot) {
				j--;
			}
 
			if (i <= j) {
    	   		System.out.println(i+"---"+j);
				String temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
				
				temp = arr2[i];
				arr2[i] = arr2[j];
				arr2[j] = temp;
				i++;
				j--;
			}
		}
 
		// recursively sort two sub parts
		if (low < j)
			quickSort(arr,arr2, low, j);
 
		if (high > i)
			quickSort(arr,arr2, i, high);
	}

}
