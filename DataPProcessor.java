

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
/**
 *
 * @author Isys Johnson
 */
public class DataPProcessor {
    
    
    public DataPProcessor(String filename, boolean isjson) throws IOException, ParseException {
        if(isjson) jsonProcess(filename);
        else txtProcess(filename);
    }
    /***
     * Preprocess json files
     * @param filename
     * @throws IOException
     * @throws ParseException 
     */
    public void jsonProcess(String filename) throws IOException, ParseException {
       FileReader fr = new FileReader(filename);
       BufferedWriter writer = new BufferedWriter(new FileWriter(filename+".processed"));
       writer.write("");
       writer = new BufferedWriter(new FileWriter(filename+".processed",true));
       
       JSONParser parse = new JSONParser();
       Object obj = parse.parse(fr);      
       JSONObject arr = (JSONObject) obj;     
       JSONArray messages = (JSONArray)((JSONObject)arr.get("smsCorpus")).get("message");
      
       for(Object elt : messages) {
        
           String mes = (String)((JSONObject)((JSONObject)elt).get("text")).get("$").toString();
           String par = fix(mes);
           System.out.println(fix(mes));
           writer.append(fix(mes));
           writer.append("\n");
       }
       writer.close();
    }
    
    /**
     * Preprocess .txt files
     * @param filename
     * @throws IOException 
     */
    public void txtProcess(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename+".processed"));
        writer.write("");
        writer = new BufferedWriter(new FileWriter(filename+".processed",true));
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String tre;
        while((tre = br.readLine()) != null) {
            
            writer.append(fix(tre));
            writer.append("\n");
            
        }
        
         writer.close();
    }
    
    public String fix(String str) {
        str = str.toLowerCase();
        Pattern pattern = Pattern.compile("([A-Za-z0-9]+)([,.?!]+)");
        Matcher matcher = pattern.matcher(str);
        while(matcher.find()) {
           //add space between punctuation and alpha-numeric characters
           str = matcher.replaceFirst(matcher.group(1) + " " + matcher.group(2) + " ");
           matcher = pattern.matcher(str);
        }
        // remove punctuation at end of line
        Pattern pattern2 = Pattern.compile("([^A-Za-z0-9]+)\\z");
        Matcher matcher2 = pattern2.matcher(str);
        if(matcher2.find()) {
            str = matcher2.replaceFirst(" ");
        }
        return str;
    }
    
    public static void main(String args[]) throws IOException, ParseException {
     //   example of how to use
     //   DataPProcessor test = new DataPProcessor("C:\\Users\\JTwal\\Desktop\\sentences.training",false);
       
    }
    
    
}
