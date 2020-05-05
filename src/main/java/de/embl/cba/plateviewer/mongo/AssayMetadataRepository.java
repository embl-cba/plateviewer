package de.embl.cba.plateviewer.mongo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

/**
 * Used to interact with the 'immuno-assay-metadata' collection which keeps information about the plates.
 */
public class AssayMetadataRepository extends AbstractRepository {

    public static String dbOutlier = "DB_outlier";
    public static String dbPatientType = "DB_patient_type";
    public static String dbCohortId = "DB_cohort_id";

    public static String[] attributes = new String[]{ dbOutlier, dbPatientType, dbCohortId };

    private String defaultPlateName;
    private TableType defaultTableType;

    public enum TableType
    {
        Plate,
        Well,
        Image
    }

    public AssayMetadataRepository(MongoClient mongoClient, String database) {
        super(mongoClient, database, "immuno-assay-metadata");
    }

    public String getDefaultPlateName()
    {
        return defaultPlateName;
    }

    public void setDefaultPlateName( String defaultPlateName )
    {
        this.defaultPlateName = defaultPlateName;
    }

    public TableType getDefaultTableType()
    {
        return defaultTableType;
    }

    public void setDefaultTableType( TableType defaultTableType )
    {
        this.defaultTableType = defaultTableType;
    }

    public Document getPlate( String plateName ) {
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
    public long updateImageQC( String plateName, String siteName, OutlierStatus outlierStatus, String outlierType) {

        final String wellName = getWellName( siteName );

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

    public String getSiteOrWellAttribute( String siteOrWellName, String attribute )
    {
        String plateName = defaultPlateName;
        attribute = attribute.replace( "DB_", "");
        switch ( getDefaultTableType() )
        {
            case Well:
                if ( attribute.equals( dbOutlier ) )
                    return getWellAttribute( plateName, siteOrWellName, attribute, Integer.class ).toString();
                else
                    return getWellAttribute( plateName, siteOrWellName, attribute, String.class );
            case Image:
                if ( attribute.equals( dbOutlier ) )
                    return getImageAttribute( plateName, siteOrWellName, attribute, Integer.class ).toString();
                else
                    return getImageAttribute( plateName, siteOrWellName, attribute, String.class );
            default:
                return null;
        }
    }

    /**
     * Gets patient type values for a given well stored in the DB
     *
     * @param attribute
     * @param plateName plate name
     * @param siteOrWellName site or well name
     * @return attribute string
     */
    public String getSiteOrWellAttribute( String plateName, String siteOrWellName, String attribute )
    {
        switch ( getDefaultTableType() )
        {
            case Well:
                if ( attribute.equals( dbOutlier ) )
                    return getWellAttribute( plateName, siteOrWellName, attribute, Integer.class ).toString();
                else
                    return getWellAttribute( plateName, siteOrWellName, attribute, String.class );
            case Image:
                if ( attribute.equals( dbOutlier ) )
                    return getImageAttribute( plateName, siteOrWellName, attribute, Integer.class ).toString();
                else
                    return getImageAttribute( plateName, siteOrWellName, attribute, String.class );
            default:
                return null;
        }
    }

    public < T > T getPlateAttribute( String plateName, String attribute, Class< T > clazz )
    {
        Document plate = getCollection().find(eq("name", plateName)).first();

        if (plate != null) {
            return plate.get( attribute, clazz );
        }

        return null;
    }


    public < T > T getWellAttribute( String plateName, String wellName, String attribute, Class< T > clazz )
    {
        Document plate = getCollection().find(eq("name", plateName)).first();

        List<Document> wells = (List<Document>) plate.get("wells");
        Document well = wells.stream()
                .filter(w -> w.get("name", String.class).equals(wellName))
                .findFirst()
                .orElse(null);

        if (well != null) {
            return well.get( attribute, clazz );
        }
        return null;
    }

    public < T > T getImageAttribute( String plateName, String siteName, String attribute, Class< T > clazz )
    {
        Document plate = getCollection().find( eq( "name", plateName ) ).first();

        Document well = getWellDocument( getWellName( siteName ), plate );

        if ( well != null )
        {
            List< Document > images = ( List< Document > ) well.get( "images" );
            Document image = images.stream()
                    .filter( img -> img.get( "site_name", String.class ).equals( siteName ) )
                    .findFirst()
                    .orElse( null );
            if ( image != null )
                return image.get( attribute, clazz );
        }

        return null;
    }

    public String getWellName( String siteName )
    {
        return siteName.split( "-" )[ 0 ];
    }

    public Document getWellDocument( String wellName, Document plate )
    {
        List<Document> wells = (List<Document>) plate.get("wells");
        return wells.stream()
                .filter(w -> w.get("name", String.class).equals(wellName))
                .findFirst()
                .orElse(null);
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
        final Logger logger = ( Logger ) LoggerFactory.getLogger( "org.mongodb");
        logger.setLevel( Level.WARN );

        String host = "vm-kreshuk08.embl.de";
        int port = 27017;
        String user = "covid19";
        String dbName = "covid";

        String connectionString = String.format("mongodb://%s:%s@%s:%d/?authSource=%s", user, password, host, port, dbName);
        MongoClient client = MongoClients.create(connectionString);
        final AssayMetadataRepository repository = new AssayMetadataRepository( client, dbName );

        return repository;
    }
}
