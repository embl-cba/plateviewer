package de.embl.cba.plateviewer.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class Demo {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 27017;
        String user = "covid19";
        String password = "secret";
        String dbName = "covid";

        String connectionString = String.format("mongodb://%s:%s@%s:%d/?authSource=%s", user, password, host, port, dbName);
        MongoClient client = MongoClients.create(connectionString);

        AssayMetadataRepository amr = new AssayMetadataRepository(client, dbName);
        MongoCollection<Document> collection = amr.getCollection();

        System.out.println(collection.countDocuments());

        System.out.println(amr.getPlate("test-cell-analysis").toJson());

        System.out.println(amr.updatePlateQC("test-cell-analysis", OutlierStatus.OUTLIER, "plateViewer:image-saturated"));

        long result = amr.updateImageQC("test-cell-analysis", "C01", "C01-0007", OutlierStatus.OUTLIER, "lol!");
        System.out.println(result);

        String manualAssessment = amr.getManualAssessment("test-cell-analysis", "C01");
        System.out.println(manualAssessment);

        long count = amr.updateManualAssessment("test-cell-analysis", "C01", "positive");
        System.out.println(count);

        amr.addIssueToImage("test-cell-analysis", "C01", "C01-0007", "https://github.com/hci-unihd/antibodies-analysis-issues/issues/666");
    }
}
