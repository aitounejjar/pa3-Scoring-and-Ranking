package edu.stanford.cs276;

import edu.stanford.cs276.util.StemmingUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Skeleton code for the implementation of an extra
 * credit scorer as a part of Extra Credit.
 */
public class ExtraCreditScorer extends SmallestWindowScorer {

    /**
     * Constructs a scorer for Extra Credit.
     *
     * @param idfs the map of idf values
     */
    public ExtraCreditScorer(Map<String, Double> idfs, Map<Query, Map<String, Document>> queryDict) {
        super(idfs, queryDict);
        Map<String, Double> idfs2 = new HashMap<>();
        for(String word: idfs.keySet()){
            double CurrentCount = idfs.get(word);
            double StemmerCount = 0;
            String StemmedWord = StemmingUtils.getStemmedWord(word);
            if (idfs2.containsKey(StemmedWord)){
                StemmerCount = idfs2.get(StemmedWord);
            }
            idfs2.put(StemmedWord, CurrentCount + StemmerCount);
        }

        idfs=null;
        this.idfs=idfs2;
    }

    @Override
    /**
     * Get the awesome similarity score using the ranking
     * algorithm you have derived incorporating other
     * signals indicating relevance of a doc to a query.
     * @param d the Document
     * @param q the Query
     * @return the similarity score
     */
    public double getSimScore(Document d, Query q) {
    /*
     * TODO : Your code here
     */

        return super.getSimScore(d,q);
    }

}
