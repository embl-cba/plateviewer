package de.embl.cba.plateviewer.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class Demo {
    public static void main(String[] args) {

        AssayMetadataRepository amr = getAssayMetadataRepository();

        MongoCollection<Document> collection = amr.getCollection();

        System.out.println(collection.countDocuments());

        final String plateName = "titration_plate_20200403_154849";

        System.out.println(amr.getPlate( plateName ).toJson());

        String manualAssessment = amr.getSiteOrWellAttribute( plateName, "C01", "patient_type" );
        System.out.println(manualAssessment);

//        System.out.println(amr.updatePlateQC( plateName, OutlierStatus.OUTLIER, "plateViewer:image-saturated"));
//
//        long result = amr.updateImageQC( plateName, "C01", "C01-0007", OutlierStatus.OUTLIER, "lol!");
//        System.out.println(result);
//
//        String manualAssessment = amr.getManualAssessment( plateName, "C01");
//        System.out.println(manualAssessment);
//
//        long count = amr.updateManualAssessment( plateName, "C01", "positive");
//        System.out.println(count);
//
//        amr.addIssueToImage( plateName, "C01", "C01-0007", "https://github.com/hci-unihd/antibodies-analysis-issues/issues/666");
    }

    private static AssayMetadataRepository getAssayMetadataRepository()
    {
        String host = "vm-kreshuk08.embl.de";
        int port = 27017;
        String user = "covid19";
        String password = "covid2581";
        String dbName = "covid";

        String connectionString = String.format("mongodb://%s:%s@%s:%d/?authSource=%s", user, password, host, port, dbName);
        MongoClient client = MongoClients.create(connectionString);

        return new AssayMetadataRepository(client, dbName);
    }
}
