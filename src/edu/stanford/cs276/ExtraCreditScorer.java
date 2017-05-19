package edu.stanford.cs276;

import edu.stanford.cs276.util.Pair;
import edu.stanford.cs276.util.StemmingUtils;

import java.util.HashSet;
import java.util.Map;

/**
 * Skeleton code for the implementation of an extra
 * credit scorer as a part of Extra Credit.
 */
public class ExtraCreditScorer extends SmallestWindowScorer {

    private static final double PageRankW = .000001;

    /**
     * Constructs a scorer for Extra Credit.
     *
     * @param idfs the map of idf values
     */
    public ExtraCreditScorer(Map<String, Double> idfs, Map<Query, Map<String, Document>> queryDict) {
        super(idfs, queryDict);

        /*

        Map<String, Double> idfs2 = new HashMap<>();
        Map<String, Integer> idfsCount = new HashMap<>();

        for(String word: idfs.keySet()){
            double CurrentCount = idfs.get(word);
            double StemmerCount = 0;
            String StemmedWord = StemmingUtils.getStemmedWord(word);
            int UniqueWords = 0;
            if (idfs2.containsKey(StemmedWord)){
                StemmerCount = idfs2.get(StemmedWord);
                UniqueWords = idfsCount.get(StemmedWord); //How many other words point to this stemmed word

            }
            idfs2.put(StemmedWord, CurrentCount + StemmerCount);
            idfsCount.put(StemmedWord, UniqueWords+1);
        }

        for(String word: idfs2.keySet()){
            double totalCount = idfs2.get(word);
            int wordCount = idfsCount.get(word);
            idfs2.put(word, (totalCount/wordCount));
        }

        idfs=null;
        this.idfs=idfs2;

        */
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
    @Override
    public double getSimScore(Document d, Query q) {
        Map<String, Map<String, Double>> tfs = this.getDocTermFreqs(d, q);
        this.normalizeTFs(tfs, d, q);
        Map<String, Double> tfQuery = getQueryFreqs(q);
        double boost = getBoostScore(d, q);
        double rawScore = this.getNetScore(tfs, q, tfQuery, d);

        double goodness = Math.log(d.page_rank)*PageRankW;

        return ( (boost * rawScore) + goodness );
    }

    @Override
    public int countOccurrences(String pattern, String string) {
        pattern = StemmingUtils.getStemmedWord(pattern);

        String[] words = string.split("\\W+");
        int count = 0;
        for(String word:words)
        {
            String ws = StemmingUtils.getStemmedWord(word);
            if(ws.contains(pattern)) {
                ++count;
            }
        }

        return count;
    }
}

