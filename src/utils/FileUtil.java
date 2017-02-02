package utils;

import java.io.BufferedWriter;
import java.io.File;
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
    
	 public static void appendToFile(String fp,String line){
         BufferedWriter bf=null;
             try {
                 bf = new BufferedWriter(new FileWriter(new File(fp), true), 32768);
                 bf.write(line);

             } catch (IOException ex) {
                 ex.printStackTrace();
             } finally {
                 try {
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
