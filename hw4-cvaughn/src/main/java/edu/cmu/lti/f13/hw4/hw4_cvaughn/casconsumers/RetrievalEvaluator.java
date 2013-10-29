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
	
	private HashMap<String, Integer> numDocs;
	
	private ArrayList<Integer[]> metaDict;
	
	private int docIndex = -1;
	
	private int Dsize;
	
	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();
		
		dictionary = new HashMap<String, ArrayList<Double>>();
		
		metaDict = new ArrayList<Integer[]>();
		
		numDocs = new HashMap<String, Integer>();

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
		
		int sentID = 1;
		int prevQID = -5;
		
		if (it.hasNext()) {
		  Document doc = (Document) it.next();
		  docIndex++;
			System.out.println("docIndex = "+docIndex);
			System.out.println(doc.getText());
			System.out.println();
			
			Integer[] things = new Integer[3];
			things[0] = doc.getRelevanceValue();
			things[1] = doc.getQueryID();
			if (doc.getQueryID()==prevQID) {
			  sentID++;
			}
			things[2] = sentID;
			metaDict.add(things);
			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);

			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());

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
		
		Dsize = docIndex;
		
		for (String str : dictionary.keySet()) {
		  ArrayList<Double> alDoub = dictionary.get(str);
		  for (int x = alDoub.size(); x <= Dsize; x++) {
		    alDoub.add(0.0);
		  }
		  for (int y = 0; y <= Dsize; y++) {
		    if (alDoub.get(y) > 0.0) {
		      if (numDocs.containsKey(str)) {
		        numDocs.put(str, numDocs.get(str) + 1);
		      } else {
		        numDocs.put(str, 1);
		      }
		    }
		  }/*
		  for (int z = 0; z <= Dsize; z++) {
		    double tfidf = alDoub.get(z)*(Math.log((Dsize+1)/numDocs));
		    alDoub.set(z, tfidf);
		  }*/
		  dictionary.put(str, alDoub);
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
		
		int Qsize = 0;
		ArrayList<Integer> ranks = new ArrayList<Integer>();
		
		for (int d=0; d<Dsize; d+=4) {
		  ArrayList<Double> Q = new ArrayList<Double>();
		  ArrayList<Double> A = new ArrayList<Double>();
		  ArrayList<Double> B = new ArrayList<Double>();
		  ArrayList<Double> C = new ArrayList<Double>();
		  
		  for (String str : dictionary.keySet()) {
		    Q.add(dictionary.get(str).get(d));
		    A.add(dictionary.get(str).get(d+1));
		    B.add(dictionary.get(str).get(d+2));
		    C.add(dictionary.get(str).get(d+3));
		  }
		  
		  for (String str : numDocs.keySet()) {
		    for (int z = 0; z <Dsize; z+=4) {
		      double tfidfQ = Q.get(z)*(Math.log((Dsize+1)/numDocs.get(str)));
		      double tfidfA = A.get(z+1)*(Math.log((Dsize+1)/numDocs.get(str)));
		      double tfidfB = B.get(z+2)*(Math.log((Dsize+1)/numDocs.get(str)));
		      double tfidfC = C.get(z+3)*(Math.log((Dsize+1)/numDocs.get(str)));
		    }
      }
		  
		  /*
      int numDocs = 0;
      for (int y = 0; y <= Dsize; y++) {
        if (alDoub.get(y) > 0.0) {
          numDocs++;
        }
      }
      for (int z = 0; z <= Dsize; z++) {
        double tfidf = alDoub.get(z)*(Math.log((Dsize+1)/numDocs));
        alDoub.set(z, tfidf);
      }*/
		  
		  // Compute the cosine similarity measure:
		  double numerA = 0.0;
		  double denom1A = 0.0;
		  double denom2A = 0.0;
		  
		  double numerB = 0.0;
      double denom1B = 0.0;
      double denom2B = 0.0;
      
      double numerC = 0.0;
      double denom1C = 0.0;
      double denom2C = 0.0;
		  for (int i=0; i<Q.size(); i++) {
		    numerA += (Q.get(i)*A.get(i));
		    denom1A += (Q.get(i)*Q.get(i));
		    denom2A += (A.get(i)*A.get(i));
		    
		    numerB += (Q.get(i)*B.get(i));
        denom1B += (Q.get(i)*Q.get(i));
        denom2B += (B.get(i)*B.get(i));
        
        numerC += (Q.get(i)*C.get(i));
        denom1C += (Q.get(i)*Q.get(i));
        denom2C += (C.get(i)*C.get(i));
		  }
		  
		  denom1A = Math.sqrt(denom1A);
		  denom2A = Math.sqrt(denom2A);
		  double cosSimA = numerA/(denom1A + denom2A);
		  
		  denom1B = Math.sqrt(denom1B);
      denom2B = Math.sqrt(denom2B);
      double cosSimB = numerB/(denom1B + denom2B);
      
      denom1C = Math.sqrt(denom1C);
      denom2C = Math.sqrt(denom2C);
      double cosSimC = numerC/(denom1C + denom2C);
      
      
      // Compute the rank of retrieved sentences:
      
      int rankA = 0;
      int rankB = 0;
      int rankC = 0;
      
      if (cosSimA > cosSimB && cosSimA > cosSimC) {
        rankA = 1;
        if (cosSimB > cosSimC) {
          rankB = 2;
          rankC = 3;
        } else {
          rankC = 2;
          rankB = 3;
        }
      }
      if (cosSimB > cosSimA && cosSimB > cosSimC) {
        rankB = 1;
        if (cosSimA > cosSimC) {
          rankA = 2;
          rankC = 3;
        } else {
          rankC = 2;
          rankA = 3;
        }
      }
      if (cosSimC > cosSimB && cosSimC > cosSimA) {
        rankC = 1;
        if (cosSimB > cosSimA) {
          rankB = 2;
          rankA = 3;
        } else {
          rankA = 2;
          rankB = 3;
        }
      }
      
      Qsize += 1;
      if (metaDict.get(d+1)[0]==1) {
        ranks.add(rankA);
        System.out.println("Score: " + cosSimA + " rank=" + rankA + " rel=" + metaDict.get(d+1)[0] + " qid=" + metaDict.get(d+1)[1] + " sent1");
        System.out.println();
      } else if (metaDict.get(d+2)[0]==1) {
        ranks.add(rankB);
        System.out.println("Score: " + cosSimB + " rank=" + rankB + " rel=" + metaDict.get(d+2)[0] + " qid=" + metaDict.get(d+2)[1] + " sent2");
        System.out.println();
      } else if (metaDict.get(d+3)[0]==1) {
        ranks.add(rankC);
        System.out.println("Score: " + cosSimC + " rank=" + rankC + " rel=" + metaDict.get(d+3)[0] + " qid=" + metaDict.get(d+3)[1] + " sent3");
        System.out.println();
      } else {
        System.out.println("ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR");
      }
		}
		
		// Compute the metric: mean reciprocal rank
    double metric_mrr = 0.0;
    for (int i=0; i<ranks.size(); i++) {
      metric_mrr += 1.0/(double)ranks.get(i);
    }
    metric_mrr = metric_mrr*(1.0/(double)Qsize);
    System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
		
    /*
		//Troubleshooting: print metaDict:
		for (int a=0; a<metaDict.size(); a++) {
		  for (int b=0; b<3; b++) {
		    System.out.print(" " + metaDict.get(a)[b] + " ");
		  }
		  System.out.println();
 		}*/
		
	}
}
