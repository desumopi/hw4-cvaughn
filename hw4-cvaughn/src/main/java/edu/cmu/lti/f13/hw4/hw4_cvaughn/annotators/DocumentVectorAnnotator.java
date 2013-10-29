package edu.cmu.lti.f13.hw4.hw4_cvaughn.annotators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f13.hw4.hw4_cvaughn.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_cvaughn.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_cvaughn.utils.Utils;

import java.util.*;

/**
 * 
 * @author Callie Vaughn (cvaughn)
 *
 */

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

  private Pattern tokenPattern = Pattern.compile("[a-zA-Z0-9']+");

	/**
	 * Populates the FSList of Tokens in each document in jcas.
	 * 
	 * @param jcas - The JCas that stores information about the documents
	 */
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
			//System.out.println();
			//System.out.println(doc.getText());
			//System.out.println();
			FSList fsOut = doc.getTokenList();
			//ArrayList<Token> alOut = Utils.fromFSListToCollection(fsOut, Token.class);
			//for (int j=0; j<alOut.size(); j++) {
			  //System.out.println(((Token) alOut.get(j)).getText());
			  //System.out.println(j);
			//}
		}

	}
	
	
	/**
	 * Constructs a vector of tokens (text and frequencies) from the document
	 * doc and stores that vector as an FSList of Tokens in the CAS jcas
	 * 
	 * @param jcas - the JCas that we are storing this in
	 * @param doc - the current document
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText().toLowerCase();
		
		//Construct a vector of tokens and update the tokenList in CAS
		Matcher matcher = tokenPattern.matcher(docText);
    int pos = 0;
    ArrayList<Token> alTok = new ArrayList<Token>();
    while (matcher.find(pos)) {
      // found one - create annotation
      Token tok = new Token(jcas);
      tok.setText(docText.substring(matcher.start(), matcher.end()));
      
      boolean b = false;
      
      for (int i=0; i<alTok.size(); i++) {
        if (alTok.get(i).getText().equals(tok.getText())) {
          alTok.get(i).setFrequency(alTok.get(i).getFrequency() + 1);
          b = true;
        } 
      }
      if (!b) {
        tok.setFrequency(1);
        alTok.add(tok);
      }
      
      pos = matcher.end();
    }
    
    //for (int j=0; j<alTok.size(); j++) {
      //System.out.println(alTok.get(j).toString());
    //}
    
    FSList fslTok = Utils.fromCollectionToFSList(jcas, alTok);
    doc.setTokenList(fslTok);
    fslTok.addToIndexes();

	}

}
