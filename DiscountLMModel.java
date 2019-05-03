import java.io.IOException;
import java.util.*;
import java.io.*;

/**
 * @authors Baiyu Li, Carlos Salas Ortega
 * An absolute discounting trigram language model
 */
public class DiscountLMModel extends LMBase implements LMModel {
    private double discount;
    private HashMap<String, Double> alpha = new HashMap<>();

    public DiscountLMModel(String trainingFile, double discount) throws IOException{
        this.discount=discount;
        this.getCount(trainingFile);
    }

    public void returnPredictions(String file) throws IOException{
        BufferedReader testReader = new BufferedReader(new FileReader(file));
        String inputlines;

        TreeMap<String,Double> probabilities = new TreeMap<>(Collections.reverseOrder());
        int correctPredictions = 0, totalPredictions = 0;
        while((inputlines = testReader.readLine()) != null){
            totalPredictions++;
            double prob = 0.0;
            String currProbWord = "";
            String fString = "";
            probabilities.clear();
            String[] wordsInLine = inputlines.split("\\s");
            String penultimateWord = wordsInLine[wordsInLine.length-3];
            String finalWord = wordsInLine[wordsInLine.length-2];
            String actualWord = wordsInLine[wordsInLine.length-1];
            String concatFinalWords = penultimateWord + "-" + finalWord;

            // If the trigram exists
            if(trigramTable.containsKey(concatFinalWords)){
                Iterator<String> trigramIterator = trigramTable.get(concatFinalWords).keySet().iterator();
                while(trigramIterator.hasNext()){
                    // Create a HashMap with words and probs for the last two words
                    currProbWord = trigramIterator.next();
                    prob = getTrigramProb(penultimateWord,finalWord,currProbWord);
                    probabilities.put(currProbWord,prob);
                }
                Iterator<String> keySetAscending = probabilities.descendingKeySet().descendingIterator();
                fString = penultimateWord + " " + finalWord + " ";
                System.out.println("Top Three predictions for: " + fString);
                for(int i = 0; i < 3; i++){
                    if(keySetAscending.hasNext()){
                        String prediction = keySetAscending.next();
                        System.out.println(prediction);
                        if (prediction.equals(actualWord)){
                            correctPredictions++;
                        }
                    }
                }
                System.out.println(probabilities);
            }
            // If the trigram doesn't exist use the bigram instead
            else if(bigramTable.containsKey(finalWord) && !trigramTable.containsKey(concatFinalWords)){
                Iterator<String> bigramIterator = bigramTable.get(finalWord).keySet().iterator();
                while(bigramIterator.hasNext()){
                    currProbWord = bigramIterator.next();
                    prob = getBigramProb(finalWord,currProbWord);
                    probabilities.put(currProbWord,prob);
                }
                System.out.println(probabilities);
                Iterator<String> keySetAscending = probabilities.descendingKeySet().descendingIterator();
                fString = finalWord + " ";
                System.out.println("Top Three predictions for: " + fString);
                for(int i = 0; i < 3; i++){
                    if(keySetAscending.hasNext()){
                        String nextPrediction = keySetAscending.next();
                        System.out.println(nextPrediction);
                        if (nextPrediction.equals(actualWord)){
                            correctPredictions++;
                        }
                    }
                }
                System.out.println(keySetAscending);
            }
        }
        System.out.println("% Lines predicted correctly: " + (double)correctPredictions/(double)totalPredictions);
    }


    /**
     * Returns p(second | first)
     *
     * @param first
     * @param second
     * @return the probability of the second word given the first word (as a probability)
     */
    public double getBigramProb(String first, String second){
        if(!vocab.contains(first)){//first is not in vocab
            first = "<UNK>";
        }
        if(!vocab.contains(second)){//second is not in vocab
            second = "<UNK>";
        }

        if(!bigramTable.get(first).containsKey(second)){
            //check if alpha has already been computed for first
            if(alpha.containsKey(first)){
                return alpha.get(first) * (unigramTable.get(second)/((double)wordCount));
            }
            else {
                int num_types = bigramTable.get(first).keySet().size();
                double reserved_mass = (num_types * discount)/(bigramTotal.get(first));
                double unigram_sum_prob = 0.0;

                //calculate unigram probability summation
                Iterator<String> it = bigramTable.get(first).keySet().iterator();
                while(it.hasNext()){
                    String word = it.next();
                    unigram_sum_prob += (double)unigramTable.get(word)/(double)wordCount;
                }
                alpha.put(first,reserved_mass/(1 - unigram_sum_prob));

                return alpha.get(first) * (unigramTable.get(second)/((double)wordCount));
            }
        }
        else {
            return (bigramTable.get(first).get(second) - discount)/bigramTotal.get(first);
        }
    }

    public double getTrigramProb(String first, String second, String third){
        if (!vocab.contains(first)){
            first = "<UNK>";
        }
        if (!vocab.contains(second)){
            second = "<UNK>";
        }
        if (!vocab.contains(third)){
            third = "<UNK>";
        }

        String bigram = first + "-" + second;
        // System.out.println(bigram);
        if (trigramTable.get(bigram) == null){
            return getBigramProb(second, third);
        }
        if (trigramTable.get(bigram).get(third) == null){
            if (alpha.get(bigram) != null){
                return alpha.get(bigram) * getBigramProb(second, third);
            }
            else {
                int num_types = trigramTable.get(bigram).size();
                double reserved_mass = (num_types * discount)/(trigramTotal.get(bigram));
                // System.out.println("reserved mass: " +reserved_mass);
                double bigram_sum_prob = 0.0;
                for (Map.Entry<String, Integer> trigramEntry : trigramTable.get(bigram).entrySet()){
                    if (bigramTable.get(second).get(trigramEntry.getKey()) != null){
                        bigram_sum_prob += getBigramProb(second, trigramEntry.getKey());
                    }
                }
                alpha.put(bigram, reserved_mass/(1-bigram_sum_prob));


                return alpha.get(bigram) * getBigramProb(second, third);
            }
        }
        else {
            return (double)(trigramTable.get(bigram).get(third) - discount)/(double)trigramTotal.get(bigram);
        }

    }
}
