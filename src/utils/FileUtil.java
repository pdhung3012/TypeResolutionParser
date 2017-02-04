package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileUtil {
	public static String getFileContent(String fp){
        String strResult="";
        try{
             strResult = new String(Files.readAllBytes(Paths.get(fp)));
        }catch(Exception ex){
          //  ex.printStackTrace();
        }
        return strResult;
    
	}
	
	public static ArrayList<String> getFileStringArray(String fp){
        ArrayList<String> lstResults=new ArrayList<String>();
        try{
        	try (BufferedReader br = new BufferedReader(new FileReader(fp))) {
        	    String line;
        	    while ((line = br.readLine()) != null) {
        	       // process the line.
        	    	//strResult+=line+"\n";
        	    	if(!line.trim().isEmpty()){
        	    		lstResults.add(line.trim());
        	    	}
        	    }
        	}
        }catch(Exception ex){
          //  ex.printStackTrace();
        }
        return lstResults;
    
	}
	
	public static int countNumberOfLines(String fp) {
		int count = 0;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fp));
    	    String line;
    	    while ((line = br.readLine()) != null) {
    	    	if(!line.trim().isEmpty())
    	    		count++;
    	    }
    	    return count;
    	} catch (IOException e) {
    		return -1;
    	} finally {
    		if (br != null)
				try {
					br.close();
				} catch (IOException e) {}
    	}
	}
    
	 public static void appendToFile(String fp,String line){
         BufferedWriter bf=null;
             try {
                 bf = new BufferedWriter(new FileWriter(new File(fp), true), 32768);
                 bf.write(line);
             } catch (IOException ex) {
                 ex.printStackTrace();
             } finally {
                 try {
                     if (bf != null)
                    	 bf.close();
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
             }
}

	 public static void appendLineToFile(String fp,String line){
         BufferedWriter bf=null;
             try {
                 bf = new BufferedWriter(new FileWriter(new File(fp), true));
                 bf.write(line+"\n");

             } catch (IOException ex) {
                 ex.printStackTrace();
             } finally {
                 try {
                     if (bf != null)
                    	 bf.close();
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
             }
	 }

    public static void deleteFile(String fp){
        try {
            File f=new File(fp);
            if(f.exists()){
            	f.delete();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static void writeToFile(String fp,String line){
                BufferedWriter bf=null;
                    try {
                        bf = new BufferedWriter(new FileWriter(new File(fp), false));
                        bf.write(line);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } finally {
                        try {
                            if (bf != null)
                           	 bf.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
    }

	public static void writeToFile(String path, ArrayList<String> lines) {
		StringBuilder sb = new StringBuilder();
		for (String l : lines)
			sb.append(l + "\n");
		writeToFile(path, sb.toString());
	}
}
