package org.aksw.provenance.boa;

import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.provenance.Constants;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class BoaPatternSearcher {

    private static CommonsHttpSolrServer server;
    private static final String SOLR_INDEX = "boa_fact_detail";

    public BoaPatternSearcher(){

        try {

            server = new CommonsHttpSolrServer("http://dbpedia.aksw.org:8080/solr/" + SOLR_INDEX);
            server.setRequestWriter(new BinaryRequestWriter());
        }
        catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Queries the configured BOA index with the configured number of returned 
     * BOA Patterns (see Constants.NUMBER_OF_BOA_PATTERNS) and a pattern score
     * threshold of 0.5.
     * 
     * @param propertyUri
     * @return
     */
    public List<Pattern> getNaturalLanguageRepresentations(String propertyUri){

        return querySolrIndex(propertyUri, Constants.NUMBER_OF_BOA_PATTERNS, 0.5D);
    }
    
    /**
     * Queries the configured BOA index with the configured number of returned 
     * BOA Patterns (see. Constants.NUMBER_OF_BOA_PATTERNS).
     * 
     * @param propertyUri 
     * @param numberOfBoaPatterns
     * @return
     */
    public List<Pattern> getNaturalLanguageRepresentations(String propertyUri, int numberOfBoaPatterns){

        return querySolrIndex(propertyUri, numberOfBoaPatterns, 0.5D);
    }
    
    /**
     * 
     * 
     * @param propertyUri
     * @param numberOfBoaPatterns
     * @param patternThreshold
     * @return
     */
    public List<Pattern> getNaturalLanguageRepresentations(String propertyUri, int numberOfBoaPatterns, double patternThreshold){

        return querySolrIndex(propertyUri, numberOfBoaPatterns, patternThreshold);
    }
    
    /**
     * Returns all patterns from the index and their factFeatures for reverb and the
     * wordnet distance and the overall boa-boaScore.
     * 
     * @param propertyUri
     * @return a list of patterns
     */
    private List<Pattern> querySolrIndex(String propertyUri, int numberOfBoaPatterns, double scoreThreshold) {

        List<Pattern> patterns = new ArrayList<Pattern>();

        try {

            SolrQuery query = new SolrQuery("uri:\"" + propertyUri + "\"");
            query.addField("boa-score");
            query.addField("nlr-var");
            query.addField("nlr-no-var");
            query.addSortField("boa-score", ORDER.desc);
            if ( numberOfBoaPatterns > 0 ) query.setRows(numberOfBoaPatterns);
            QueryResponse response = server.query(query);
            SolrDocumentList docList = response.getResults();
            
            // return the first list of types
            for (SolrDocument d : docList) {
                
                Pattern pattern = new Pattern();
                pattern.naturalLanguageRepresentation = (String) d.get("nlr-var");
                pattern.naturalLanguageRepresentationWithoutVariables = (String) d.get("nlr-no-var");
                pattern.posTags = (String) d.get("pos");
                pattern.boaScore = (Double) d.get("boa-score");
                
                if ( pattern.boaScore > scoreThreshold ) patterns.add(pattern);
            }
        }
        catch (SolrServerException e) {

            System.out.println("Could not execute query: " + e);
            e.printStackTrace();
        }
        
        // we need to sort this list because we always want to have the same results in the eval
        Collections.sort(patterns, new Comparator<Pattern>() {

            @Override // -1 if first is smaller
            public int compare(Pattern pattern1, Pattern pattern2) {

                double difference = pattern1.boaScore - pattern2.boaScore;
                if ( difference > 0 ) return -1;
                if ( difference < 0 ) return 1;
                
                return pattern1.naturalLanguageRepresentation.compareTo(pattern2.naturalLanguageRepresentation);
            }
        });
        
        return patterns;
    }
    
    public static void main(String[] args) {

        BoaPatternSearcher bps = new BoaPatternSearcher();
        for (Pattern p : bps.getNaturalLanguageRepresentations("http://dbpedia.org/ontology/birthPlace", 500, 0.0)) {
            
            System.out.println(new DecimalFormat("0.000").format(p.boaScore) + ": " + p.naturalLanguageRepresentation);
        }
    }
}
