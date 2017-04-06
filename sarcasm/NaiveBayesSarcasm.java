import java.util.ArrayList;
import java.util.Hashtable;

public class NaiveBayesSarcasm{

   private int noOfSarcastic = 0;
   private int noOfNormal = 0;
   private Hashtable<String, Integer> sarcasticWords = new Hashtable<String, Integer>();
   private Hashtable<String, Integer> normalWords = new Hashtable<String, Integer>();
   

   public void process(String[] vocabs, ArrayList<Integer[]> vectorList){
   
      for (Integer[] features : vectorList){
      // for each sentence with size of vocab (105 size) 
         int sarcasticFlag = 0;
         
         // for each word in the feature vector
         for (int i = features.length-1 ; i >= 0 ; i --){
         
            if (i == features.length -1){
            
               sarcasticFlag = features[i];
               if (sarcasticFlag == 1){noOfSarcastic = noOfSarcastic + 1;}
               else {noOfNormal = noOfNormal + 1;}
            }
            else // pointing to a feature
            {
               if(sarcasticFlag == 1){
               
                  if(sarcasticWords.get(vocabs[i]) == null){
                  
                     sarcasticWords.put(vocabs[i],features[i]);   
                  }
                  else{
                     int freq = sarcasticWords.get(vocabs[i])+features[i];
                     sarcasticWords.put(vocabs[i],freq);    
                  }    
               }
               
               else{
                  if(normalWords.get(vocabs[i]) == null){
                  
                     normalWords.put(vocabs[i],features[i]);
                  }
                  else{
                     int freq = normalWords.get(vocabs[i])+features[i];
                     normalWords.put(vocabs[i],freq); 
                  }                 
               }
            }
         
         
         }
         
      }
   
   } //end of process method
   
   public boolean isWordExist(String word, boolean isSarcastic){
      
      boolean flag = false;
      
      if (isSarcastic){
         if (sarcasticWords.get(word)!= null){
         
            flag = true;
         }
      }
      else
      {
         if (normalWords.get(word)!= null){
         
            flag = true;
         }
      
      }
      return flag;
   }
   
   public double getMainProb(boolean isSarcastic){
    
      int total = noOfSarcastic + noOfNormal;
    
      if (isSarcastic){
         double ret = noOfSarcastic / (double) total;
         return ret;
      }
      else {
         
         double in = noOfNormal / (double) total;
         return in;
      }
   }
   public double getProbability(String word, boolean isSarcastic){
      
      
      double results = 0.0;
   
      if (isSarcastic){
         if (sarcasticWords.get(word)!= null)
         {
            double count = sarcasticWords.get(word);
            results = count / noOfSarcastic;
         }    
      }
      else{
         if (normalWords.get(word)!= null){
         
            double count = normalWords.get(word);
            results = count / noOfNormal;
         } 
      }
   
      return results;
   
   }
   


}