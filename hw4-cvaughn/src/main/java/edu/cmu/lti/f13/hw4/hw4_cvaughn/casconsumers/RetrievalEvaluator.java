package edu.cmu.lti.f13.hw4.hw4_cvaughn.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f13.hw4.hw4_cvaughn.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_cvaughn.utils.*;
import edu.cmu.lti.f13.hw4.hw4_cvaughn.typesystems.Document;


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;

	private HashMap<String, ArrayList<Double>> dictionary;
	
	private int docIndex = -1;
	
	//private ArrayList<String> termList;
	
	//private ArrayList<ArrayList<Double>> freqArray;
		
	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();
		
		dictionary = new HashMap<String, ArrayList<Double>>();

	}

	/**
	 * Creates a global word dictionary to associate each word with an
	 * ArrayList of its frequencies (one entry in each ArrayList per
	 * document)
	 * 
	 * @param aCas - the JCas
	 * @throws ResourceProcessException
	 */
	public void processCas(CAS aCas) throws ResourceProcessException {

	  	  
		JCas jcas;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
		
		if (it.hasNext()) {
			Document doc = (Document) it.next();
			docIndex++;
			System.out.println("docIndex = "+docIndex);
			System.out.println(doc.getText());
			
			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);

			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			
			boolean b = false;
			for (int i = 0; i < tokenList.size(); i++) {
			  ArrayList<Double> value;
			  String tokStr = tokenList.get(i).getText();
			  if (dictionary.containsKey(tokenList.get(i).getText())) {
			    // The dictionary does have a key for this token already
			    value = dictionary.get(tokStr);
			    
			  } else {
			    // The dictionary doesn't have a key for this Token yet
			    // Create a new ArrayList for this Token's values:
			    value = new ArrayList<Double>();
			    dictionary.put(tokStr, value);
			  }
	      // Populate the value ArrayList with 0.0 until this document's index:
        for (int j=dictionary.get(tokStr).size(); j<docIndex; j++) {
          value.add(0.0);
        }
        // Add the Token's frequency to the ArrayList at this document's index:
        int toConvert = tokenList.get(i).getFrequency();
        Double doub = new Double(toConvert);
        value.add(doub);
        dictionary.put(tokenList.get(i).getText(), value);
			}
		}
	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);

		// TODO :: compute the cosine similarity measure
		
		
		
		// TODO :: compute the rank of retrieved sentences
		
		
		System.out.println(dictionary.toString());
		
		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity=0.0;

		// TODO :: compute cosine similarity between two sentences
		

		return cosine_similarity;
	}

	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr=0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		
		return metric_mrr;
	}

}
