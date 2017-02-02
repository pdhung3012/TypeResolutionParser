package parser;

import java.util.HashMap;

import utils.FileUtil;

public class CountCorrectStructure {

	static String fop_dirTranslate="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_simpleCodeSequence\\";
	static String fn_eval="evaluatedResults.txt";
	static String fn_testSource="test_filter.s";
	static String fn_testTarget="test_filter.t";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[] arrEvals=FileUtil.getFileContent(fop_dirTranslate+fn_eval).trim().split("\n");
		String[] arrSource=FileUtil.getFileContent(fop_dirTranslate+fn_testSource).trim().split("\n");
		String[] arrTarget=FileUtil.getFileContent(fop_dirTranslate+fn_testTarget).trim().split("\n");
		int countLessThan30=0,countGreater30=0;
		String strLessThan30="",strGreater30="";
		HashMap<Integer,Integer> mapCount=new HashMap<Integer, Integer>();
		HashMap<Integer,Integer> mapTotal=new HashMap<Integer, Integer>();
		
		for(int i=0;i<arrEvals.length;i++){
			String[] arrItems=arrEvals[i].trim().split("\t");
			if(arrItems.length>=4){
				int length= Integer.parseInt(arrItems[2]);
				if(!mapTotal.containsKey(length)){
					
					mapTotal.put(length, 1);
				}else{
					mapTotal.put(length, mapTotal.get(length)+1);
				}
				if(arrItems[1].equals("false")){
					if(!mapCount.containsKey(length)){
						
						mapCount.put(length, 1);
					}else{
						mapCount.put(length, mapCount.get(length)+1);
					}
					if(length<=30){
						countLessThan30++;
						strLessThan30+=(i+1)+" ";
					} else{
						countGreater30++;
						strGreater30+=(i+1)+" ";
					}
				}
				
			}
		}
		for(Integer key:mapCount.keySet()){
			System.out.println("Number of reordered case have length "+key+": "+mapCount.get(key)+" / "+mapTotal.get(key));
		}
		System.out.println(countLessThan30+" cases <= 30: "+strLessThan30);
		System.out.println(countGreater30+" cases > 30: "+strGreater30);

	}

}
