import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.TreeMap;
import java.util.*;



public class ProcessorSarcasm{

   private static final String FILENAME = "training_text.txt"; 
   private static ArrayList<String[]> sentenceList = new ArrayList<String[]>();
   private static ArrayList<Integer> sarcasmFlagsList = new ArrayList<Integer>();
   private static ArrayList<Integer[]> vectorList = new ArrayList<Integer[]>();
   private static NaiveBayesSarcasm ns = new NaiveBayesSarcasm();
   public static void main(String[] args){
   
   
      BufferedReader br = null;
      FileReader fr = null;
      
      // All of the words that can be found in the sentence
      Hashtable<String,Integer> ht = new Hashtable<String,Integer>();
      
      // All of the words with at least 5 times
      TreeMap<String,Integer> vocabs = new TreeMap<String,Integer>();
      
      try{
      
         fr = new FileReader(FILENAME);
         br = new BufferedReader(fr);
      
         String currentString;
      
         br = new BufferedReader (new FileReader(FILENAME));
         
         // for each line
         while((currentString = br.readLine())!= null){
         
             //System.out.println(currentString);
            
            if (currentString.length()!= 0){
            
               //String[] words = currentString.replaceAll("[^a-zA-Z ]", "").split("\\s+");
            
               String[] currStringArray = currentString.split("");
               StringBuilder sb = new StringBuilder();
               int arrSize = currStringArray.length;
               for(int i = 0; i < arrSize; i ++)
               {   
                  if(i < arrSize-4 && i != 0 && i != 1){
                     sb.append(currStringArray[i]);
                  }
                  else if (i == arrSize-2){
                     sarcasmFlagsList.add(Integer.parseInt(currStringArray[i]));
                  }
               }
               //System.out.println(sb.toString());
               String sentence = sb.toString().replaceAll("[^a-zA-Z ]", "");
               String[] words = sentence.toLowerCase().split("\\s+");
               sentenceList.add(words);
               
               for (int i=0; i < words.length; i ++)
               {
                  String currentWord = words[i].toLowerCase();
                     
                  if (ht.get(currentWord) == null)
                  {
                     ht.put(currentWord, new Integer(1));         
                  }
                  else
                  {
                     int occurence = ht.get(currentWord);
                     ht.put(currentWord, occurence+1); 
                     if (occurence > 3)
                     {
                        vocabs.put(currentWord,occurence+1);        
                     }  
                  }      
               }
               
            }// if currentString is not empty
            
         } // end of while
         
      } // end of try
      catch(Exception e){
      
         e.printStackTrace();
      }
   
      
   
   // Form the vocabulary (if a word occur more than 5 times)
   
   // Get a set of the entries
      Set set = vocabs.entrySet();
   // Get an iterator
      Iterator i = set.iterator();
      String[] vocabWords = new String[vocabs.size()]; 
      int count = 0;
               
      while(i.hasNext()) {
      
         Map.Entry me = (Map.Entry)i.next();
         //System.out.print(me.getKey() + ": ");
         vocabWords[count] = me.getKey().toString();   
         count++;
      }
      
      for (int j = 0 ; j < sentenceList.size(); j++)
      {
         //Vector<Integer> v = new Vector<Integer>(vocabs.size()+1,0);
         ArrayList<Integer> vector = new ArrayList<Integer>();
         
         String[] wrds = sentenceList.get(j);
         ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(wrds));
         
         for (int l = 0; l < vocabWords.length; l++)
         {
            if(arrayList.contains(vocabWords[l]) == true)
            {
               vector.add(1);
            }
            else
            {
               vector.add(0);
            }
         
         }
         
         int flag = sarcasmFlagsList.get(j);
         vector.add(flag);
         if (flag==1)
         {
         //System.out.println(j+" "+sentenceList.get(j));
         }
         Integer[] vectorArr = vector.toArray(new Integer[0]);
         vectorList.add(vectorArr);
         
      }// after iterating each sentence in a list of sentences
      
      ns.process(vocabWords, vectorList);
      test(args[0]);
      writeToFile(vocabWords);
   } // end of main method

   private static void test(String fileName){
   
      BufferedReader br = null;
      FileReader fr = null;
      ArrayList<String> sarcasticList = new ArrayList<String>();
      ArrayList<String> normalList = new ArrayList<String>();
      
      try{
      
         fr = new FileReader(FILENAME);
         br = new BufferedReader(fr);
      
         String currentString;
      
         br = new BufferedReader (new FileReader(fileName));
         
         // for each TEST sentence 
         while((currentString = br.readLine())!= null){
            
            double probSarcastic = 1.0;
            double probNormal = 1.0;
            
            if (currentString.length()!= 0){
            
               String[] words = currentString.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
               
               //for each word in a TEST sentence 
               for (String word : words){
               
                  if (ns.isWordExist(word,true)){
                     probSarcastic = probSarcastic * ns.getProbability(word, true); 
                  }
                  if (ns.isWordExist(word,false)){
                     probNormal = probNormal * ns.getProbability(word,false);
                  }
               }
               probSarcastic = probSarcastic * ns.getMainProb(true);
               probNormal = probNormal * ns.getMainProb(false);
               
               if (probSarcastic > probNormal){
                  sarcasticList.add(currentString);
               }
               else
               {
                  normalList.add(currentString);
               }   
            }
         }
         System.out.println("Sarcastic result : " + sarcasticList.size());
         System.out.println("Normal result : " + normalList.size());
      } // end of try
      
      catch (Exception e){
      
      e.printStackTrace();
      
      }
   }

   private static void writeToFile(String[] vocabs)
   {
      
      try (BufferedWriter bw = new BufferedWriter(new FileWriter("preprocessed_train.txt"))) {
      
         for (int i = 0 ; i < vocabs.length; i ++)
         {
            bw.write(vocabs[i]); 
            if(i != vocabs.length-1)
            {
               bw.write(" ,");  
            }
            else
            {
               bw.write("\n");
            }
         }
         for (Integer[] features : vectorList){
         
            for (int i = 0; i < features.length; i++)
            {
               //System.out.println(features[i]);
               bw.write(features[i].toString());
               if(i != features.length-1)
               {
                  bw.write(" ,");  
               }
               else
               {
                  bw.write("\n");
               }
            
            }
         
         }
        
      }
      catch(Exception e){
         e.printStackTrace();
      }
   } // end of writeToFile method

}