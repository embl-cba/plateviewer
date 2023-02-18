package de.embl.cba.plateviewer.mongodb;


import com.mongodb.client.MongoClient;

/**
 * Used to interact with the 'immuno-assay-analysis-results' collection which results of the analysis pipeline.
 */
public class AssayAnalysisResultRepository extends AbstractRepository {
    public AssayAnalysisResultRepository(MongoClient mongoClient, String database) {
        super(mongoClient, database, "immuno-assay-analysis-results");
    }
}
