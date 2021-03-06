package org.aksw.provenance.wikipedia;

import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.helper.WikipediaSearchResult;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class WikipediaCrawler implements Callable<List<WikipediaSearchResult>> {

    private String topicIndicator;

    /**
     * 
     * @param topicIndicator
     */
    public WikipediaCrawler(String topicIndicator) {
        
        this.topicIndicator = topicIndicator;
    }
    
    @Override
    public List<WikipediaSearchResult> call() throws Exception {

        return WikipediaSearcher.queryWikipedia(this.topicIndicator);
    }

}
