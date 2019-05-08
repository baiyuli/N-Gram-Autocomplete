import java.util.*;
import java.io.*;

/**
 * @authors Baiyu Li, Carlos Salas Ortega
 * An absolute discounting trigram language model
 */
public class Autocomplete {
	public static void main(String[] args) throws IOException {
		// double bestPerplexity = Double.MAX_VALUE, bestDiscount = 0.0;
		// for (double i = Double.parseDouble(args[2]); i < 1 ; i+=0.01){
		// 	LMModel languageModel = new DiscountLMModel("data/" + args[0], i);
		// 	double curPerplexity = languageModel.getPerplexity("data/" + args[1]);
		// 	if (curPerplexity < bestPerplexity){
		// 		bestPerplexity = curPerplexity;
		// 		bestDiscount = i;
		// 	}
		// }
		// LMModel languageModel = new DiscountLMModel("data/" + args[0], bestDiscount);
		LMModel languageModel = new DiscountLMModel("data/" + args[0], Double.parseDouble(args[2]));
		languageModel.returnPredictions("data/" + args[1]);
		// System.out.println("optimal discount: " + bestDiscount);
		// System.out.println("optimal perplexity: " + bestPerplexity);
		System.out.println("perplexity: " + languageModel.getPerplexity("data/" + args[1]));
	}
}