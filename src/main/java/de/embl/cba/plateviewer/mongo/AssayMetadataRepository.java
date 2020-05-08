package de.embl.cba.plateviewer.mongo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.embl.cba.plateviewer.table.AnnotatedIntervalCreatorAndAdder;
import org.bson.Document;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

/**
 * Used to interact with the 'immuno-assay-metadata' collection which keeps information about the plates.
 */
public class AssayMetadataRepository extends AbstractRepository {

    public static String outlier = "outlier";
    public static String patientType = "patient_type";
    public static String cohortId = "cohort_id";

    public static String[] attributes = new String[]{ outlier, patientType, cohortId };

    private String plateName;
    private AnnotatedIntervalCreatorAndAdder.IntervalType intervalType;
    private HashMap< String, Document > wellNameToDocument = new HashMap<>();
    private List< Document > wells;
    private HashMap< String, Document > plateNameToDocument = new HashMap<>();
    private HashMap< String, Document > siteNameToDocument = new HashMap<>();

    public AssayMetadataRepository(MongoClient mongoClient, String database) {
        super(mongoClient, database, "immuno-assay-metadata");
    }

    public String getPlateName()
    {
        return plateName;
    }

    public void setPlateName( String plateName )
    {
        this.plateName = plateName;
    }

    public AnnotatedIntervalCreatorAndAdder.IntervalType getIntervalType()
    {
        return intervalType;
    }

    public void setIntervalType( AnnotatedIntervalCreatorAndAdder.IntervalType defaultIntervalType )
    {
        this.intervalType = defaultIntervalType;
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
        String plateName = this.plateName;
        switch ( getIntervalType() )
        {
            case Wells:
                if ( attribute.equals( outlier ) )
                    return getWellAttribute( plateName, siteOrWellName, attribute, Integer.class ).toString();
                else
                    return getWellAttribute( plateName, siteOrWellName, attribute, String.class );
            case Sites:
                if ( attribute.equals( outlier ) )
                    return getImageAttribute( plateName, siteOrWellName, attribute, Integer.class ).toString();
                else
                {
                    // only the outlier attribute is available for images, the others only from well
                    final String wellName = getWellName( siteOrWellName );
                    return getWellAttribute( plateName, wellName, attribute, String.class );
                }
            default:
                throw new UnsupportedOperationException( "Interval type not supported: " + getIntervalType() );
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
        switch ( getIntervalType() )
        {
            case Wells:
                if ( attribute.equals( outlier ) )
                    return getWellAttribute( plateName, siteOrWellName, attribute, Integer.class ).toString();
                else
                    return getWellAttribute( plateName, siteOrWellName, attribute, String.class );
            case Sites:
                if ( attribute.equals( outlier ) )
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
        final Document plate = getPlateDocument( plateName );
        Document well = getWellDocument( wellName, plate );
        return well.get( attribute, clazz );
    }

    public < T > T getImageAttribute( String plateName, String siteName, String attribute, Class< T > clazz )
    {
        final String wellName = getWellName( siteName );
        final Document plate = getPlateDocument( plateName );
        Document well = getWellDocument( wellName, plate );
        final Document image = getImageDocument( siteName, well );
        return image.get( attribute, clazz );
    }

    public Document getImageDocument( String siteName, Document well )
    {
        if ( ! siteNameToDocument.containsKey( siteName ) )
        {
            List< Document > images = ( List< Document > ) well.get( "images" );
            Document image = images.stream()
                    .filter( img -> img.get( "site_name", String.class ).equals( siteName ) )
                    .findFirst()
                    .orElse( null );
            siteNameToDocument.put( siteName, image );
        }
        return siteNameToDocument.get( siteName );
    }

    public Document getPlateDocument( String plateName )
    {
        if ( ! plateNameToDocument.containsKey( plateName ) )
        {
            final Document plate = getCollection().find( eq( "name", plateName ) ).first();
            plateNameToDocument.put( plateName, plate );
        }

        return plateNameToDocument.get( plateName );
    }

    public String getWellName( String siteName )
    {
        return siteName.split( "-" )[ 0 ];
    }

    public Document getWellDocument( String wellName, Document plate )
    {
        if ( ! wellNameToDocument.containsKey (wellName  ) )
        {
            if ( wells == null )
                wells = ( List< Document > ) plate.get( "wells" );

            final Document well = wells.stream()
                    .filter( w -> w.get( "name", String.class ).equals( wellName ) )
                    .findFirst()
                    .orElse( null );

            if ( well == null )
            {
                throw new UnsupportedOperationException( "Well not found in database: " + wellName );
            }

            wellNameToDocument.put( wellName, well );
        }

        return wellNameToDocument.get( wellName );
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
