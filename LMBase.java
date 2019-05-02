import java.io.*;
import java.util.*;

/**
 * @authors Baiyu Li, Dylan Keezell
 * A base class for counting unigrams and bigrams, and calculating logprob and perplexity
 */
abstract class LMBase {
    public int wordCount=0;
    public HashMap<String, HashMap<String,Integer>> bigramTable =  new HashMap<String, HashMap<String,Integer>>();
    public HashMap<String, HashMap<String,Integer>> trigramTable =  new HashMap<String, HashMap<String,Integer>>();
    public HashMap<String,Integer> bigramTotal =  new HashMap<String,Integer>();//stores total occurences of bigrams X _
    public HashMap<String, Integer> unigramTable = new HashMap<String,Integer>();
    public HashMap<String, Integer> trigramTotal = new HashMap<String, Integer>();
    public HashSet<String> vocab = new HashSet<String>();

    public LMBase(){
    }

    /**
     *Given textfile, count bigrams and unigrams, storing their respective counts in HashMaps
     *
     * @param filename
     */
    public void getCount(String filename){
        bigramTable.put("<s>", new HashMap<>());
        bigramTable.put("</s>", new HashMap<>());
        bigramTable.put("<UNK>", new HashMap<>());

        unigramTable.put("<s>",0);
        unigramTable.put("</s>",0);
        unigramTable.put("<UNK>",0);

        try{
            BufferedReader reader =  new BufferedReader(new FileReader(filename));
            String line= reader.readLine();
            while(line!=null){
                line = " <s> "+line+" </s> ";
                int begin;
                int end =0;
                //add in <UNK> for the first occurence of unknown words
                while(true){
                    begin = line.indexOf(" ", end);
                    end = line.indexOf(" ", begin+1);
                    if(end<0| begin<0){
                        break;
                    }
                    String word = line.substring(begin+1, end);
                    if (!unigramTable.containsKey(word)) {
                        line = line.substring(0, begin) + " <UNK> " + line.substring(end + 1);
                        end = begin+6;// 6 is the length of " <UNK> " that replaced the entry
                        unigramTable.put(word, 0);
                    }
                    wordCount++;
                }

                // //count bigrams and unigrams
                // begin = line.indexOf(" ", 0);
                // end = line.indexOf(" ", begin+1);
                // String X = line.substring(begin+1, end);

                // //increment unigram
                // unigramTable.put(X, unigramTable.get(X)+1);

                // while(true){
                //     begin = line.indexOf(" ", end);
                //     end = line.indexOf(" ", begin+1);
                //     if(end<0| begin<0){
                //         break;
                //     }
                //     String Y = line.substring(begin+1, end);

                //     //increment brigram count
                //     if(!bigramTable.get(X).containsKey(Y)){
                //         bigramTable.get(X).put(Y,1);
                //     }else{
                //         bigramTable.get(X).put(Y, bigramTable.get(X).get(Y)+1);
                //     }
                //     //increment unigram
                //     unigramTable.put(Y, unigramTable.get(Y)+1);

                //     X=Y;
                // }
                
                String[] sentence = line.split("\\s");
                String fstWord = sentence[0], sndWord = sentence[1], trdWord = sentence[2];
                addUnigram(fstWord);
                addUnigram(sndWord);
                addUnigram(trdWord);
                addBigram(fstWord, sndWord);
                addBigram(sndWord, trdWord);
                addTrigram(fstWord, sndWord, trdWord);

                for (int i = 1; i < sentence.length-2; i++){
                    addUnigram(sentence[i+2]);
                    addBigram(sentence[i+1], sentence[i+2]);
                    addTrigram(sentence[i], sentence[i+1], sentence[i+2]);
                }
                line = reader.readLine();
            }

        }
        catch(IOException e){}

        //calculate and store totals for each bigram that start with X
        Iterator<String> it = bigramTable.keySet().iterator();
        while(it.hasNext()){
            String X = it.next();
            Iterator<String> it2 = bigramTable.get(X).keySet().iterator();
            int count = 0;//total count of bigrams of form X _
            while(it2.hasNext()){
                String Y = it2.next();
                count += bigramTable.get(X).get(Y);
            }
            bigramTotal.put(X, count);
        }

        for (Map.Entry<String, HashMap<String, Integer>> bigram : trigramTable.entrySet()){
            int count = 0;
            for (Map.Entry<String, Integer> trdWord : bigram.getValue().entrySet()){
                count += trdWord.getValue();
            }
            trigramTotal.put(bigram.getKey(), count);
        }

        //vocabulary is every string in unigram table with count >0
        Iterator<String> it3 = unigramTable.keySet().iterator();
        while(it3.hasNext()){
            String word =it3.next();
            if(unigramTable.get(word)>0){
                vocab.add(word);
            }
        }
    }

    public void addUnigram(String word){
        if (unigramTable.get(word) == null){
                unigramTable.put(word, 1);
        }
        else unigramTable.put(word, unigramTable.get(word)+1);
    }

    public void addBigram(String fstWord, String sndWord){
        if (bigramTable.get(fstWord) == null){
            bigramTable.put(fstWord, new HashMap<String, Integer>());
        }
        HashMap<String, Integer> map = bigramTable.get(fstWord);
        if (map.get(sndWord) == null){
            map.put(sndWord, 1);
        }
        else {
            map.put(sndWord, map.get(sndWord) + 1);
        }
    }

    public void addTrigram(String fstWord, String sndWord, String trdWord){
        String bigram = fstWord + "-" + sndWord;

        if (trigramTable.get(bigram) == null){
            trigramTable.put(bigram, new HashMap<String, Integer>());
        }
        HashMap<String, Integer> map = trigramTable.get(bigram);
        if (map.get(trdWord) == null){
            map.put(trdWord, 1);
        }
        else {
            map.put(trdWord, map.get(trdWord) + 1);
        }
    }
    /**
     * Given a sentence, return the log of the probability of the sentence based on the LM.
     *
     * @param sentWords the words in the sentence.  sentWords should NOT contain <s> or </s>.
     * @return the log probability
     */
    public double logProb(ArrayList<String> sentWords){
        double prob = 0;
        int size = sentWords.size();
        //account for start and end token log probabilities
        if (size < 2){
            prob += Math.log10(this.getBigramProb("<s>", sentWords.get(0)));
            return prob;
        }
        prob += Math.log10(this.getTrigramProb("<s>", sentWords.get(0), sentWords.get(1)));
        prob += Math.log10(this.getTrigramProb(sentWords.get(size-2), sentWords.get(size-1), "</s>"));

        for(int i=0; i<size-2; i++) {
            // System.out.println(sentWords.get(i) +" "+ sentWords.get(i+1) +" "+ sentWords.get(i+2));
            // System.out.println(this.getTrigramProb(sentWords.get(i), sentWords.get(i+1), sentWords.get(i+2)));
            // Double prob1 = this.getTrigramProb(sentWords.get(i), sentWords.get(i+1), sentWords.get(i+2));
            // Double prob2 = Math.log10(prob1);
            // System.out.println("prob: " + prob1);
            // System.out.println("log_prob: " + prob2);
            // if (prob1.isInfinite() || prob2.isNaN() || prob1.isNaN() || prob2.isInfinite()) {
            //     System.out.println(sentWords.get(i) +" "+ sentWords.get(i+1)+" "+ sentWords.get(i+2));
            //     continue;
            // }
            prob += Math.log10(this.getTrigramProb(sentWords.get(i), sentWords.get(i+1), sentWords.get(i+2)));
            // System.out.println("REALPROB: " + prob);
        }

        // System.out.println(sentWords);
        
        return prob;
    }

    public double logProb2(ArrayList<String> sentWords){
        double prob = 0;

        //account for start and end token log probabilities
        prob += Math.log10(this.getBigramProb("<s>", sentWords.get(0)));
        prob += Math.log10(this.getBigramProb(sentWords.get(sentWords.size()-1), "</s>"));

        for(int i=1;i<sentWords.size();i++) {
            prob += Math.log10(this.getBigramProb(sentWords.get(i - 1), sentWords.get(i)));
        }
        return prob;
    }

    /**
     * Given a text file, calculate the perplexity of the text file, that is the negative average per word log
     * probability
     *
     * @param filename a text file.  The file will contain sentences WITHOUT <s> or </s>.
     * @return the perplexity of the text in file based on the LM
     */
    public double getPerplexity(String filename){
        // System.out.println("prob s unk b: " + getTrigramProb("<s>", "<UNK>", "b"));
        // System.out.println("prob b b b: " + getTrigramProb("b", "b", "b"));
        // System.out.println("prob unk a b: " + getTrigramProb("<UNK>", "a", "b"));
        // System.out.println("prob a a b: " + getTrigramProb("a", "a", "b"));
        double numerator=0;//log prob summation
        int n =0;//total word count
        try{
            BufferedReader reader =  new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while(line!=null){
                line = " "+line+" ";
                int begin;
                int end =0;
                ArrayList<String> sentence =new ArrayList<String>();
                //build sentence
                while(true) {
                    begin = line.indexOf(" ", end);
                    end = line.indexOf(" ", begin+1);
                    if(end<0| begin<0){
                        break;
                    }
                    sentence.add(line.substring(begin+1, end));
                    n++;
                }
                n+=2; //account for the start and end tokens added in the log prob method
                numerator += this.logProb(sentence);
                line = reader.readLine();
            }
        }catch(IOException e){
        }

        System.out.println("num, n: " + numerator + "    " + n);
        // System.out.println(trigramTable.get("the-rebec"));
        // int num_types = trigramTable.get("the-rebec").size();
        //         double reserved_mass = (num_types * 0.01)/(trigramTotal.get("the-rebec"));
        //         System.out.println("reserved mass: " +reserved_mass);
        //         double bigram_sum_prob = 0.0;
        //         for (Map.Entry<String, Integer> trigramEntry : trigramTable.get("the-rebec").entrySet()){
        //             if (bigramTable.get("rebec").get(trigramEntry.getKey()) != null){
        //             bigram_sum_prob += (double)bigramTable.get("rebec").get(trigramEntry.getKey())/
        //                                 (double)bigramTotal.get("rebec");
        //             }
        //         }
        // System.out.println(bigram_sum_prob);

        return (Math.pow(10.0, -1*(numerator)/((double)n)));
    }
    /**
     * Returns p(second | first)
     *
     * @param first
     * @param second
     * @return the probability of the second word given the first word (as a probability)
     */
    abstract double getBigramProb(String first, String second);

    abstract double getTrigramProb(String first, String second, String third);
}
