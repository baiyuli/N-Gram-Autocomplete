import java.util.*;
import java.io.*;

public class Autocomplete {
	public static void main(String[] args) throws IOException { 
		LMModel languageModel = new DiscountLMModel("data/" + args[0], Double.parseDouble(args[2]));
		System.out.println("Perplexity: " + languageModel.getPerplexity("data/" + args[1]));
		languageModel.returnPredictions("data/" + args[1]);
	}
}