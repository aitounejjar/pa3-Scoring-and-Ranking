package edu.stanford.cs276;

import edu.stanford.cs276.util.Pair;
import edu.stanford.cs276.util.StemmingUtils;

import java.util.HashMap;
import java.util.HashSet;
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
    protected double getBoostScore(Document d, Query q) {
        // same logic as the the overridden method, with the exception that here we make use of the number of times
        // the smallest window was found

        // number of unique words in the query
        int querySize = new HashSet<>(q.queryWords).size();

        if (querySize == 1) {
            return 1.0;
        }

        Pair<Integer, Integer> pair = getWindow(d, q);
        int smallestWindow = pair.getFirst();
        int smallestWindowCount = pair.getSecond();

        double boostScore;

        if (smallestWindow == Integer.MAX_VALUE) {
            boostScore = 1;
        } else if (smallestWindow == querySize) {
            boostScore = BOOST_FACTOR * smallestWindow * smallestWindowCount;
        } else {
            int delta = smallestWindow - querySize;
            boostScore = getBoostScore_helper(delta);
        }

        return boostScore;

    }
}
