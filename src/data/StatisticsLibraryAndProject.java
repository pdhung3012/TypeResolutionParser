package data;

import java.io.File;
import java.util.HashMap;

import utils.FileUtil;

public class StatisticsLibraryAndProject {

	
	public static void evaluateLibraryPerMethod(HashMap<String,Integer> mapMethod,String targetToken){
		String[] arrTargetTokens=targetToken.split("\\s+");
		for(int i=0;i<arrTargetTokens.length;i++){
			
			for(String itemKey:mapMethod.keySet()){
				if(arrTargetTokens[i].contains(itemKey)){
					mapMethod.put(itemKey,1);
				}
			}
		}
	}
	
	static String fop_data="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\5 libs live API\\type-sequences\\";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File folder=new File(fop_data);
		
		
		if(folder.isDirectory()){
			File[] listLibraries=folder.listFiles();
			int num1=0,num2=0,num3=0,num4=0,num5=0;
			for(int i=0;i<listLibraries.length;i++){
				File[] lstProjects=listLibraries[i].listFiles();
				int countNPaProjectOnOneLib=0;
				int indexProject=0;
				FileUtil.writeToFile(listLibraries[i].getName()+"_log_project.txt","");
				FileUtil.writeToFile("all_log_library.txt", "Library\tProjectName\tjoda-time\tandroid\tgwt\txstream\thibernate\n");
				
				
				for(int j=0;j<lstProjects.length;j++){
					int numPairs=0;
					if(lstProjects[j].getAbsolutePath().endsWith("alignment")){
						indexProject++;
						String[] arrContentAlign=FileUtil.getFileContent(lstProjects[j].getAbsolutePath()+"\\training.s-t.A3").trim().split("\n");
						
						numPairs=(arrContentAlign.length/3);
						if(numPairs>0){
							countNPaProjectOnOneLib+=numPairs;
							FileUtil.appendToFile(listLibraries[i].getName()+"_log_project.txt", lstProjects[j].getName()+"\t"+numPairs+"\n");
						
							//FileUtil.appendToFile(listLibraries[i].getName()+"_log_library.txt", projectItems+"\t"+isInRepo+"\n");
							HashMap<String,Integer> map5LibrariesPerProject=new HashMap<String,Integer>();
							map5LibrariesPerProject.put("org.joda.time", 0);
							map5LibrariesPerProject.put("android", 0);
							map5LibrariesPerProject.put("gwt",0);
							map5LibrariesPerProject.put("xstream", 0);
							map5LibrariesPerProject.put("hibernate", 0);
							for(int k=1;k<arrContentAlign.length;k+=3){
								evaluateLibraryPerMethod(map5LibrariesPerProject, arrContentAlign[k]);							
								
							}
							num1+=map5LibrariesPerProject.get("org.joda.time");
							num2+=map5LibrariesPerProject.get("android");
							num3+=map5LibrariesPerProject.get("gwt");
							num4+=map5LibrariesPerProject.get("xstream");
							num5+=map5LibrariesPerProject.get("hibernate");
							
							FileUtil.appendToFile("all_log_library.txt",listLibraries[i].getName()+"\t"+lstProjects[j].getName()+"\t"+ map5LibrariesPerProject.get("org.joda.time")+"\t"+map5LibrariesPerProject.get("android")+"\t"+map5LibrariesPerProject.get("gwt")+"\t"+map5LibrariesPerProject.get("xstream")+"\t"+map5LibrariesPerProject.get("hibernate")+"\n");
							
						}
						
						
						
					}
				}
				//FileUtil.appendToFile(listLibraries[i].getName()+"all_log_library.txt","Total\t"+"\t"+num1+"\t"+num2+"\t"+num3+"\t"+num4+"\t"+num5 +"\n");
				
				FileUtil.appendToFile(listLibraries[i].getName()+"_log_project.txt", "Count: "+countNPaProjectOnOneLib+"\n");
				
				
				
			}
		}
	}

}
