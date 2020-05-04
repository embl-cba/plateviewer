package de.embl.cba.plateviewer.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

/**
 * Used to interact with the 'immuno-assay-metadata' collection which keeps information about the plates.
 */
public class AssayMetadataRepository extends AbstractRepository {
    public AssayMetadataRepository(MongoClient mongoClient, String database) {
        super(mongoClient, database, "immuno-assay-metadata");
    }

    public Document getPlate(String plateName) {
        return getCollection().find(eq("name", plateName)).first();
    }

    /**
     * Update outlier status of the entire plate
     *
     * @param plateName     plate to be updated
     * @param outlierStatus outlier flag see {@link OutlierStatus}
     * @param outlierType   additional information about the outlier
     * @return number of plates modified by the update (0 or 1);
     * bear in mind that even if the plate is matched by the name, but the outlierStatus and outlierType values
     * are the same as stored in DB, the modified count will be 0
     */
    public long updatePlateQC(String plateName, OutlierStatus outlierStatus, String outlierType) {
        return getCollection().updateOne(eq("name", plateName),
                combine(
                        set("outlier", outlierStatus.getStatus()),
                        set("outlier_type", outlierType)
                )
        ).getModifiedCount();
    }

    /**
     * Update outlier status of a given well
     *
     * @param plateName     plate name
     * @param wellName      well to be updated
     * @param outlierStatus outlier flag
     * @param outlierType   additional info
     * @return number of wells modified by the update (0 or 1)
     */
    public long updateWellQC(String plateName, String wellName, OutlierStatus outlierStatus, String outlierType) {
        return getCollection().updateOne(
                new Document()
                        .append("name", plateName)
                        .append("wells.name", wellName),
                combine(
                        set("wells.$.outlier", outlierStatus.getStatus()),
                        set("wells.$.outlier_type", outlierType)
                )
        ).getModifiedCount();
    }

    /**
     * Update outlier status of a given image
     *
     * @param plateName
     * @param wellName
     * @param siteName
     * @param outlierType
     * @return
     */
    public long updateImageQC(String plateName, String wellName, String siteName, OutlierStatus outlierStatus, String outlierType) {
        Document plate = getCollection().find(
                eq("name", plateName)
        ).first();

        final List<Document> wells = (List<Document>) plate.get("wells");

        Document well = wells.stream()
                .filter(w -> w.get("name", String.class).equals(wellName))
                .findFirst()
                .orElse(null);

        if (well != null) {
            List<Document> images = (List<Document>) well.get("images");
            Document image = images.stream()
                    .filter(img -> img.get("site_name", String.class).equals(siteName))
                    .findFirst()
                    .orElse(null);

            if (image != null) {
                image.put("outlier", outlierStatus.getStatus());
                image.put("outlier_type", outlierType);
                getCollection().replaceOne(eq("name", plateName), plate);
                return 1;
            }
        }

        return 0;
    }

    /**
     * Gets manual assessment values for a given well stored in the DB
     *
     * @param plateName plate name
     * @param wellName  well name
     * @return manual assessment label or null if the well cannot be found
     */
    public String getManualAssessment(String plateName, String wellName) {
        Document plate = getCollection().find(eq("name", plateName)).first();

        List<Document> wells = (List<Document>) plate.get("wells");
        Document well = wells.stream()
                .filter(w -> w.get("name", String.class).equals(wellName))
                .findFirst()
                .orElse(null);

        if (well != null) {
            return well.get("manual_assessment", String.class);
        }
        return null;
    }

    /**
     * Updates manual assessment for a given well
     *
     * @param plateName
     * @param wellName
     * @param assessment
     * @return number of wells modified by the update (0 or 1)
     */
    public long updateManualAssessment(String plateName, String wellName, String assessment) {
        return getCollection().updateOne(
                new Document()
                        .append("name", plateName)
                        .append("wells.name", wellName),
                set("wells.$.manual_assessment", assessment)
        ).getModifiedCount();
    }

    public long addIssueToImage(String plateName, String wellName, String siteName, String issueUrl) {
        Document plate = getCollection().find(
                eq("name", plateName)
        ).first();

        final List<Document> wells = (List<Document>) plate.get("wells");

        Document well = wells.stream()
                .filter(w -> w.get("name", String.class).equals(wellName))
                .findFirst()
                .orElse(null);

        if (well != null) {
            List<Document> images = (List<Document>) well.get("images");
            Document image = images.stream()
                    .filter(img -> img.get("site_name", String.class).equals(siteName))
                    .findFirst()
                    .orElse(null);

            if (image != null) {
                List<String> issues = (List<String>) image.get("issue_urls");
                if (issues != null) {
                    issues.add(issueUrl);
                } else {
                    issues = Arrays.asList(issueUrl);
                    image.put("issue_urls", issues);
                }

                getCollection().replaceOne(eq("name", plateName), plate);
                return 1;
            }
        }

        return 0;
    }


    public static AssayMetadataRepository getCovid19AssayMetadataRepository( String password )
    {
        String host = "vm-kreshuk08.embl.de";
        int port = 27017;
        String user = "covid19";
        String dbName = "covid";

        String connectionString = String.format("mongodb://%s:%s@%s:%d/?authSource=%s", user, password, host, port, dbName);
        MongoClient client = MongoClients.create(connectionString);

        return new AssayMetadataRepository(client, dbName);
    }
}
