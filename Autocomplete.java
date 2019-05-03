import java.util.*;
import java.io.*;

public class Autocomplete {
	public static void main(String[] args) throws IOException { 
		LMModel languageModel = new DiscountLMModel(args[0], Double.parseDouble(args[2]));
		System.out.println("Perplexity: " + languageModel.getPerplexity(args[1]));
		languageModel.returnPredictions(args[1]);
	}
}