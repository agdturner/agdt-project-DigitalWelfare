package uk.ac.leeds.ccg.projects.dw.visualisation.mapping;

import com.vividsolutions.jts.geom.Geometry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.TreeSet;
import org.geotools.data.collection.TreeSetFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import uk.ac.leeds.ccg.generic.io.Generic_IO;
import uk.ac.leeds.ccg.projects.dw.core.DW_Environment;
import uk.ac.leeds.ccg.projects.dw.core.DW_Object;

/**
 * @author Andy Turner
 * @version 1.0.0
 */
public class DW_AreaCodesAndShapefiles extends DW_Object {

    protected DW_Maps Maps;

    private final TreeSet<String> LeedsAreaCodes;
    private final TreeSet<String> LeedsAndNeighbouringLADAreaCodes;
    private final TreeSet<String> LeedsAndNearNeighbouringLADAreaCodes;
    private final DW_Shapefile LevelDW_Shapefile;
//    private final FeatureCollection levelFC;
//    private final SimpleFeatureType levelSFT;
    private final DW_Shapefile LeedsLevelDW_Shapefile;
//    private final FeatureCollection leedsLADFC;
//    private final SimpleFeatureType leedsLADSFT;
    private final DW_Shapefile LeedsLADDW_Shapefile;
    private final DW_Shapefile LeedsAndNeighbouringLADDW_Shapefile;
    private final DW_Shapefile LeedsAndNearNeighbouringLADDW_Shapefile;

    public DW_AreaCodesAndShapefiles(DW_Environment e) {
        super(e);
        LeedsAreaCodes = null;
        LeedsAndNeighbouringLADAreaCodes = null;
        LeedsAndNearNeighbouringLADAreaCodes = null;
        LevelDW_Shapefile = null;
        LeedsLevelDW_Shapefile = null;
        LeedsLADDW_Shapefile = null;
        LeedsAndNeighbouringLADDW_Shapefile = null;
        LeedsAndNearNeighbouringLADDW_Shapefile = null;
    }

    /**
     *
     * @param env
     * @param level
     * @param targetPropertyName
     * @param sdsf
     */
    public DW_AreaCodesAndShapefiles(DW_Environment env, String level,
            String targetPropertyName, ShapefileDataStoreFactory sdsf) 
            throws IOException {
        super(env);
        this.Maps = env.getMaps();
        String tLeedsString = "Leeds";
        String tLeedsAndNeighbouringLADsString;
        tLeedsAndNeighbouringLADsString = "LeedsAndNeighbouringLADs";
        String tLeedsAndNearNeighbouringLADsString;
        tLeedsAndNearNeighbouringLADsString = "LeedsAndNearNeighbouringLADs";
        // Set LAD DW_Shapefiles
        // ---------------------
        // Get Leeds LAD Shapefile
        TreeSet<String> tLeedsLADCensusAreaCode;
        tLeedsLADCensusAreaCode = new TreeSet<>();
        // Code for Leeds 00DA = E08000035
        tLeedsLADCensusAreaCode.add("E08000035");
        LeedsLADDW_Shapefile = getLADShapefile(tLeedsString,
                tLeedsLADCensusAreaCode, sdsf);
        // Get Leeds and Neighbouring LAD Shapefile
        TreeSet<String> tLeedsAndNeighbouringLADCodes;
        tLeedsAndNeighbouringLADCodes = new TreeSet<>();
        tLeedsAndNeighbouringLADCodes.addAll(
                tLeedsLADCensusAreaCode);
        // Code for Selby 36UH = E07000169
        // Code for Bradford 00CX = E08000032
        // Code for Kirklees 00CZ = E08000034
        // Code for Wakefield 00DB = E08000036
        // Code for Harrogate 36UD = E07000165
        tLeedsAndNeighbouringLADCodes.add("E07000169");
        tLeedsAndNeighbouringLADCodes.add("E08000032");
        tLeedsAndNeighbouringLADCodes.add("E08000034");
        tLeedsAndNeighbouringLADCodes.add("E08000036");
        tLeedsAndNeighbouringLADCodes.add("E07000165");
        LeedsAndNeighbouringLADDW_Shapefile = getLADShapefile(
                tLeedsAndNeighbouringLADsString,
                tLeedsAndNeighbouringLADCodes,
                sdsf);
        // Get Leeds and Near Neighbouring LAD Shapefile
        TreeSet<String> tLeedsAndNearNeighbouringLADCodes;
        tLeedsAndNearNeighbouringLADCodes = new TreeSet<>();
        tLeedsAndNearNeighbouringLADCodes.addAll(
                tLeedsAndNeighbouringLADCodes);
        // Code for Calderdale 00CY = E08000033
        // Code for Craven 36UB = E07000163
        // Code for York 00FF = E06000014
        tLeedsAndNearNeighbouringLADCodes.add("E08000033");
        tLeedsAndNearNeighbouringLADCodes.add("E07000163");
        tLeedsAndNearNeighbouringLADCodes.add("E06000014");
        LeedsAndNearNeighbouringLADDW_Shapefile = getLADShapefile(
                tLeedsAndNearNeighbouringLADsString,
                tLeedsAndNearNeighbouringLADCodes,
                sdsf);
        Path censusDataDirectory = Paths.get(
                env.files.getInputCensus2011AttributeDataDir(level).toString(),
                tLeedsString);
        if (level.equalsIgnoreCase("OA")
                || level.equalsIgnoreCase("LSOA")
                || level.equalsIgnoreCase("MSOA")) {
            // Read Census Boundary Data of level
            Path levelShapefile = Maps.getAreaBoundaryShapefile(level);
            LevelDW_Shapefile = new DW_Shapefile(levelShapefile);

            // Read area level Census Codes
            LeedsAreaCodes = env.censusEnv.getCensusCodes(
                    tLeedsString, level);
            // Read Leeds and neighbouring District LADs Census Codes for level
            LeedsAndNeighbouringLADAreaCodes = env.censusEnv.getCensusCodes(
                    tLeedsAndNeighbouringLADsString, level);
            // Read Leeds and neighbouring District LADs and Craven And York Census Codes
            LeedsAndNearNeighbouringLADAreaCodes = env.censusEnv.getCensusCodes(
                    tLeedsAndNearNeighbouringLADsString, level);

            FeatureCollection levelFC;
            SimpleFeatureType levelSFT;
            levelFC = LevelDW_Shapefile.getFeatureCollection();
            levelSFT = LevelDW_Shapefile.getSimpleFeatureType();

            /*
             * Filter Leeds LAD LSOAs and write new Shapefile if it does not exist
             * and set this Leeds LAD LSOA Boundary Data Shapefile as the
             * backgroundShapefile
             */
            Path leedsLevelShapefile = Maps.getCensusBoundaryShapefile(
                    tLeedsString, level);
            LeedsLevelDW_Shapefile = new DW_Shapefile(leedsLevelShapefile);
            if (!Files.exists(leedsLevelShapefile)) {
                Maps.selectAndCreateNewShapefile(sdsf, levelFC, levelSFT,
                        LeedsAreaCodes, targetPropertyName, leedsLevelShapefile);
            }
        } else {
            Path levelShapefile = Maps.getAreaBoundaryShapefile(level);
            if (levelShapefile == null) {
                int debug = 1;
            }
            LevelDW_Shapefile = new DW_Shapefile(levelShapefile);
            LeedsAreaCodes = getAreaCodesAndShapefile(tLeedsString, level,
                    targetPropertyName, LeedsLADDW_Shapefile, LevelDW_Shapefile);
            LeedsAndNeighbouringLADAreaCodes = getAreaCodesAndShapefile(
                    tLeedsAndNeighbouringLADsString, level, targetPropertyName,
                    LeedsLADDW_Shapefile, LevelDW_Shapefile);
            LeedsAndNearNeighbouringLADAreaCodes = getAreaCodesAndShapefile(
                    tLeedsAndNearNeighbouringLADsString, level,
                    targetPropertyName, LeedsLADDW_Shapefile, LevelDW_Shapefile);
            LeedsLevelDW_Shapefile = new DW_Shapefile(
                    getPostcodeShapefile(tLeedsString, level));
        }
    }

    /**
     *
     * @param area
     * @param level
     * @param targetPropertyName
     * @param llsf tLeedsLADDW_Shapefile
     * @param lsf tLevelDW_Shapefile
     * @return
     * @throws IOException
     */
    public final TreeSet<String> getAreaCodesAndShapefile(String area,
            String level, String targetPropertyName, DW_Shapefile llsf,
            DW_Shapefile lsf) throws IOException {
        TreeSet<String> r;
        Path censusDataDirectory = Paths.get(
                env.files.getInputCensus2011AttributeDataDir(level).toString(), area);
        if (level.equalsIgnoreCase("OA")
                || level.equalsIgnoreCase("LSOA")
                || level.equalsIgnoreCase("MSOA")) {
            r = env.censusEnv.getCensusCodes(area, level);
        } else {
            Path psf = getPostcodeShapefile(area, level);
            r = new TreeSet<>();
            Path pacf = getPostcodeDataAreaCodesFile(area, level);
            if (Files.exists(psf) && Files.exists(pacf)) {
                //Read files
                r = getAreaCodes(area, level);
            } else {
                Files.createDirectories(psf.getParent());
                ShapefileDataStoreFactory sdsf = new ShapefileDataStoreFactory();
                SimpleFeatureType sft = null;
                TreeSetFeatureCollection tsfc = new TreeSetFeatureCollection();
                // Intersect and return
                FeatureCollection fc = llsf.getFeatureCollection();
                FeatureCollection fc2 = lsf.getFeatureCollection();
                FeatureIterator fi2 = fc2.features();
                while (fi2.hasNext()) {
                    SimpleFeature sf2;
                    sf2 = (SimpleFeature) fi2.next();
                    if (sft == null) {
                        sft = sf2.getFeatureType();
                    }
                    Geometry g2 = (Geometry) sf2.getDefaultGeometry();
//                        Polygon p2;
//                        p2 = (Polygon) sf2.getDefaultGeometry();
                    FeatureIterator fi = fc.features();
                    while (fi.hasNext()) {
                        SimpleFeature sf = (SimpleFeature) fi.next();
                        Geometry g = (Geometry) sf.getDefaultGeometry();
//                            Polygon p;
//                            p = (Polygon) sf.getDefaultGeometry();
                        Geometry intersection = g.intersection(g2);
                        if (intersection != null && !intersection.isEmpty()) {
                            tsfc.add(sf2);
                            r.add(sf2.getAttribute(targetPropertyName).toString());
                        }
                    }
                    fi.close();
                }
                fi2.close();
                DW_Shapefile.transact(psf, sft, tsfc, sdsf);
                writeAreaCodes(pacf, r, area, level);
            }
        }
        return r;
    }

    public final Path getPostcodeShapefile(String area, String level)
            throws IOException {
        String n = area + level + "PolyShapefile.shp";
        return Paths.get(env.files.getGeneratedPostcodeDir().toString(), n, n);
    }

    /**
     *
     * @param name
     * @param tLADCensusCodes The Local Authority District Census Area Codes.
     * @param sdsf
     * @return
     */
    public final DW_Shapefile getLADShapefile(String name,
            TreeSet<String> tLADCensusCodes, ShapefileDataStoreFactory sdsf)
            throws IOException {
        // Read LAD Census Boundary Data
        Path tLADShapefile = Maps.getAreaBoundaryShapefile("LAD");
        DW_Shapefile sf = new DW_Shapefile(tLADShapefile);
        return getLADShapefile(name, tLADCensusCodes, sf, sdsf);
    }

    public DW_Shapefile getLADShapefile(String name,
            TreeSet<String> tLADCensusCodes, DW_Shapefile sf,
            ShapefileDataStoreFactory sdsf) throws IOException {
        DW_Shapefile r;
        FeatureCollection fc = sf.getFeatureCollection();
        SimpleFeatureType sft = sf.getSimpleFeatureType();
        // Select tLADCensusCodes from LAD Census Boundary Data
        Path tLADShapefile = Maps.getCensusBoundaryShapefile(name, "LAD");
        r = new DW_Shapefile(tLADShapefile);
        if (!Files.exists(tLADShapefile)) {
            Maps.selectAndCreateNewShapefile(sdsf, fc, sft,
                    tLADCensusCodes, "CODE", tLADShapefile);
        }
        return r;
    }

    /**
     *
     * @param a area
     * @param l level
     * @return
     * @throws IOException
     */
    public Path getPostcodeDataAreaCodesFile(String a, String l)
            throws IOException {
        Path dir = Paths.get(env.files.getGeneratedPostcodeDir().toString(),
                a, l);
        Files.createDirectories(dir);
        return Paths.get(dir.toString(), "AreaCodes.csv");
    }

    /**
     *
     * @param pacf postcodeDataAreaCodesFile
     * @param result
     * @param area
     * @param level
     * @throws IOException
     */
    public static void writeAreaCodes(Path pacf, TreeSet<String> result,
            String area, String level) throws IOException {
        Files.createDirectories(pacf.getParent());
        try (PrintWriter pw = Generic_IO.getPrintWriter(pacf, false)) {
            Iterator<String> ite = result.iterator();
            while (ite.hasNext()) {
                pw.println(ite.next());
            }
            pw.flush();
        }
    }

    /**
     * @param area
     * @param level "OA" or "LSOA" or "MSOA" currently...
     * @return <code>TreeSet&ltString&gt</code> of LSOA codes for the Leeds
     * Local Authority District loaded from a specific file within
     * digitalWelfareDir.
     */
    public TreeSet<String> getAreaCodes(String area, String level) throws IOException {
        TreeSet<String> result = null;
        Path f = Paths.get(env.files.getGeneratedPostcodeDir().toString(),
                area, level, "AreaCodes.csv");
        if (Files.exists(f)) {
            try (BufferedReader br = Generic_IO.getBufferedReader(f)) {
                result = new TreeSet<>();
                StreamTokenizer st = new StreamTokenizer(br);
                Generic_IO.setStreamTokenizerSyntax1(st);
                int token = st.nextToken();
//                long RecordID = 0;
                String line = "";
                while (!(token == StreamTokenizer.TT_EOF)) {
                    switch (token) {
                        case StreamTokenizer.TT_EOL:
//                                if (RecordID % 100 == 0) {
//                                    System.out.println(line);
//                                }
//                                RecordID++;
                            break;
                        case StreamTokenizer.TT_WORD:
                            line = st.sval;
                            result.add(line);
                            break;
                    }
                    token = st.nextToken();
                }
            }
        }
        return result;
    }

    /**
     * @return the tLeedsAreaCodes
     */
    public TreeSet<String> getLeedsAreaCodes() {
        return LeedsAreaCodes;
    }

    /**
     * @return the LeedsAndNeighbouringLADAreaCodes
     */
    public TreeSet<String> getLeedsAndNeighbouringLADAreaCodes() {
        return LeedsAndNeighbouringLADAreaCodes;
    }

    /**
     * @return the LeedsAndNearNeighbouringLADAreaCodes
     */
    public TreeSet<String> getLeedsAndNearNeighbouringLADAreaCodes() {
        return LeedsAndNearNeighbouringLADAreaCodes;
    }

    /**
     * @return the levelShapefile
     */
    public DW_Shapefile getLevelDW_Shapefile() {
        return LevelDW_Shapefile;
    }

    /**
     * @return getLevelDW_Shapefile().getFeatureCollection();
     */
    public FeatureCollection getLevelFC() throws IOException {
        return getLevelDW_Shapefile().getFeatureCollection();
    }

    /**
     * @return getLevelDW_Shapefile().getSimpleFeatureType();
     */
    public SimpleFeatureType getLevelSFT() {
        return getLevelDW_Shapefile().getSimpleFeatureType();
    }

    /**
     * @return the LeedsLevelDW_Shapefile
     */
    public DW_Shapefile getLeedsLevelDW_Shapefile() {
        return LeedsLevelDW_Shapefile;
    }

    /**
     * @return getLeedsLADDW_Shapefile().getFeatureCollection();
     */
    public FeatureCollection getLeedsLADFC() throws IOException {
        return getLeedsLADDW_Shapefile().getFeatureCollection();
    }

    /**
     * @return return getLeedsLADDW_Shapefile().getSimpleFeatureType();
     */
    public SimpleFeatureType getLeedsLADSFT() {
        return getLeedsLADDW_Shapefile().getSimpleFeatureType();
    }

    /**
     * @return the LeedsLADDW_Shapefile
     */
    public DW_Shapefile getLeedsLADDW_Shapefile() {
        return LeedsLADDW_Shapefile;
    }

    /**
     * @return the LeedsAndNeighbouringLADDW_Shapefile
     */
    public DW_Shapefile getLeedsAndNeighbouringLADDW_Shapefile() {
        return LeedsAndNeighbouringLADDW_Shapefile;
    }

    /**
     * @return the LeedsAndNearNeighbouringLADDW_Shapefile
     */
    public DW_Shapefile getLeedsAndNearNeighbouringLADDW_Shapefile() {
        return LeedsAndNearNeighbouringLADDW_Shapefile;
    }

    /**
     * For disposing of resources once used.
     */
    public void dispose() {
        DW_Shapefile tDW_Shapefile;
        tDW_Shapefile = getLeedsLevelDW_Shapefile();
        if (tDW_Shapefile != null) {
            tDW_Shapefile.dispose();
        }
        tDW_Shapefile = getLeedsAndNearNeighbouringLADDW_Shapefile();
        if (tDW_Shapefile != null) {
            tDW_Shapefile.dispose();
        }
        tDW_Shapefile = getLeedsAndNeighbouringLADDW_Shapefile();
        if (tDW_Shapefile != null) {
            tDW_Shapefile.dispose();
        }
        tDW_Shapefile = getLeedsLADDW_Shapefile();
        if (tDW_Shapefile != null) {
            tDW_Shapefile.dispose();
        }
        tDW_Shapefile = getLevelDW_Shapefile();
        if (tDW_Shapefile != null) {
            tDW_Shapefile.dispose();
        }
    }
}
