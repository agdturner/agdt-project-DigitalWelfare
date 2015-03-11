/*
 * Copyright (C) 2014 geoagdt.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package uk.ac.leeds.ccg.andyt.projects.digitalwelfare.mapping;

import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.mapping.DW_Shapefile;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.data.collection.TreeSetFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_StaticIO;
import uk.ac.leeds.ccg.andyt.agdtcensus.AGDT_Census;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.io.DW_Files;
import static uk.ac.leeds.ccg.andyt.projects.digitalwelfare.mapping.DW_Maps.getCensusBoundaryShapefile;

/**
 *
 * @author geoagdt
 */
public class DW_AreaCodesAndShapefiles {

    private final TreeSet<String> _LeedsAreaCodes;
    private final TreeSet<String> _LeedsAndNeighbouringLADAreaCodes;
    private final TreeSet<String> _LeedsAndNearNeighbouringLADAreaCodes;
    private final DW_Shapefile _LevelDW_Shapefile;
//    private final FeatureCollection levelFC;
//    private final SimpleFeatureType levelSFT;
    private final DW_Shapefile _LeedsLevelDW_Shapefile;
//    private final FeatureCollection leedsLADFC;
//    private final SimpleFeatureType leedsLADSFT;
    private final DW_Shapefile _LeedsLADDW_Shapefile;
    private final DW_Shapefile _LeedsAndNeighbouringLADDW_Shapefile;
    private final DW_Shapefile _LeedsAndNearNeighbouringLADDW_Shapefile;

    /**
     *
     * @param level
     * @param targetPropertyName
     * @param sdsf
     */
    public DW_AreaCodesAndShapefiles(
            String level,
            String targetPropertyName,
            ShapefileDataStoreFactory sdsf) {
        String tLeedsString = "Leeds";
        String tLeedsAndNeighbouringLADsString;
        tLeedsAndNeighbouringLADsString = "LeedsAndNeighbouringLADs";
        String tLeedsAndNearNeighbouringLADsString;
        tLeedsAndNearNeighbouringLADsString = "LeedsAndNearNeighbouringLADs";
        // Set LAD DW_Shapefiles
        // ---------------------
        // Get Leeds LAD Shapefile
        TreeSet<String> tLeedsLADCensusAreaCode;
        tLeedsLADCensusAreaCode = new TreeSet<String>();
        // Code for Leeds 00DA = E08000035
        tLeedsLADCensusAreaCode.add("E08000035");
        _LeedsLADDW_Shapefile = getLADShapefile(
                tLeedsString,
                tLeedsLADCensusAreaCode,
                sdsf);
        // Get Leeds and Neighbouring LAD Shapefile
        TreeSet<String> tLeedsAndNeighbouringLADCodes;
        tLeedsAndNeighbouringLADCodes = new TreeSet<String>();
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
        _LeedsAndNeighbouringLADDW_Shapefile = getLADShapefile(
                tLeedsAndNeighbouringLADsString,
                tLeedsAndNeighbouringLADCodes,
                sdsf);
        // Get Leeds and Near Neighbouring LAD Shapefile
        TreeSet<String> tLeedsAndNearNeighbouringLADCodes;
        tLeedsAndNearNeighbouringLADCodes = new TreeSet<String>();
        tLeedsAndNearNeighbouringLADCodes.addAll(
                tLeedsAndNeighbouringLADCodes);
        // Code for Calderdale 00CY = E08000033
        // Code for Craven 36UB = E07000163
        // Code for York 00FF = E06000014
        tLeedsAndNearNeighbouringLADCodes.add("E08000033");
        tLeedsAndNearNeighbouringLADCodes.add("E07000163");
        tLeedsAndNearNeighbouringLADCodes.add("E06000014");
        _LeedsAndNearNeighbouringLADDW_Shapefile = getLADShapefile(
                tLeedsAndNearNeighbouringLADsString,
                tLeedsAndNearNeighbouringLADCodes,
                sdsf);
        File censusDataDirectory = new File(
                DW_Files.getInputCensus2011AttributeDataDir(level),
                tLeedsString);
        if (level.equalsIgnoreCase("OA")
                || level.equalsIgnoreCase("LSOA")
                || level.equalsIgnoreCase("MSOA")) {
            // Read Census Boundary Data of level
            File levelShapefile = DW_Maps.getAreaBoundaryShapefile(level);
            _LevelDW_Shapefile = new DW_Shapefile(levelShapefile);

            // Read area level Census Codes
            _LeedsAreaCodes = AGDT_Census.getCensusCodes(
                    tLeedsString,
                    level,
                    censusDataDirectory);
            // Read Leeds and neighbouring District LADs Census Codes for level
            _LeedsAndNeighbouringLADAreaCodes = AGDT_Census.getCensusCodes(
                    tLeedsAndNeighbouringLADsString,
                    level,
                    censusDataDirectory);
            // Read Leeds and neighbouring District LADs and Craven And York Census Codes
            _LeedsAndNearNeighbouringLADAreaCodes = AGDT_Census.getCensusCodes(
                    tLeedsAndNearNeighbouringLADsString,
                    level,
                    censusDataDirectory);

            FeatureCollection levelFC;
            SimpleFeatureType levelSFT;
            levelFC = _LevelDW_Shapefile.getFeatureCollection();
            levelSFT = _LevelDW_Shapefile.getSimpleFeatureType();

            /*
             * Filter Leeds LAD LSOAs and write new Shapefile if it does not exist
             * and set this Leeds LAD LSOA Boundary Data Shapefile as the
             * backgroundShapefile
             */
            File leedsLevelShapefile = getCensusBoundaryShapefile(
                    tLeedsString,
                    level);
            _LeedsLevelDW_Shapefile = new DW_Shapefile(leedsLevelShapefile);
            if (!leedsLevelShapefile.exists()) {
                DW_Maps.selectAndCreateNewShapefile(sdsf,
                        levelFC,
                        levelSFT,
                        _LeedsAreaCodes,
                        targetPropertyName,
                        leedsLevelShapefile);
            }
        } else {
            File levelShapefile = DW_Maps.getAreaBoundaryShapefile(level);
            _LevelDW_Shapefile = new DW_Shapefile(levelShapefile);
            _LeedsAreaCodes = getAreaCodesAndShapefile(
                    tLeedsString,
                    level,
                    targetPropertyName,
                    _LeedsLADDW_Shapefile,
                    _LevelDW_Shapefile);
            _LeedsAndNeighbouringLADAreaCodes = getAreaCodesAndShapefile(
                    tLeedsAndNeighbouringLADsString,
                    level,
                    targetPropertyName,
                    _LeedsLADDW_Shapefile,
                    _LevelDW_Shapefile);
            _LeedsAndNearNeighbouringLADAreaCodes = getAreaCodesAndShapefile(
                    tLeedsAndNearNeighbouringLADsString,
                    level,
                    targetPropertyName,
                    _LeedsLADDW_Shapefile,
                    _LevelDW_Shapefile);
            _LeedsLevelDW_Shapefile = new DW_Shapefile(
                    getPostcodeShapefile(
                            tLeedsString,
                            level));
        }
    }

    public static TreeSet<String> getAreaCodesAndShapefile(
            String area,
            String level,
            String targetPropertyName,
            DW_Shapefile tLeedsLADDW_Shapefile,
            DW_Shapefile tLevelDW_Shapefile) {
        TreeSet<String> result;
        File censusDataDirectory = new File(
                DW_Files.getInputCensus2011AttributeDataDir(level),
                area);
        if (level.equalsIgnoreCase("OA")
                || level.equalsIgnoreCase("LSOA")
                || level.equalsIgnoreCase("MSOA")) {
            result = AGDT_Census.getCensusCodes(
                    area,
                    level,
                    censusDataDirectory);
        } else {
            File tPostcodeShapefile;
            tPostcodeShapefile = getPostcodeShapefile(
                    area,
                    level);
            result = new TreeSet<String>();
            File postcodeAreaCodesFile = getPostcodeDataAreaCodesFile(
                    area,
                    level);
            if (tPostcodeShapefile.exists()
                    && postcodeAreaCodesFile.exists()) {
                //Read files
                result = getAreaCodes(
                        area,
                        level);
            } else {
                tPostcodeShapefile.getParentFile().mkdirs();
                ShapefileDataStoreFactory sdsf;
                sdsf = new ShapefileDataStoreFactory();
                SimpleFeatureType sft = null;
                TreeSetFeatureCollection tsfc;
                tsfc = new TreeSetFeatureCollection();
                // Intersect and return
                FeatureCollection fc;
                fc = tLeedsLADDW_Shapefile.getFeatureCollection();
                FeatureCollection fc2;
                fc2 = tLevelDW_Shapefile.getFeatureCollection();
                FeatureIterator fi2;
                fi2 = fc2.features();
                while (fi2.hasNext()) {
                    SimpleFeature sf2;
                    sf2 = (SimpleFeature) fi2.next();
                    if (sft == null) {
                        sft = sf2.getFeatureType();
                    }
                    Geometry g2 = (Geometry) sf2.getDefaultGeometry();
//                        Polygon p2;
//                        p2 = (Polygon) sf2.getDefaultGeometry();
                    FeatureIterator fi;
                    fi = fc.features();
                    while (fi.hasNext()) {
                        SimpleFeature sf;
                        sf = (SimpleFeature) fi.next();
                        Geometry g = (Geometry) sf.getDefaultGeometry();
//                            Polygon p;
//                            p = (Polygon) sf.getDefaultGeometry();
                        Geometry intersection = g.intersection(g2);
                        if (intersection != null
                                && !intersection.isEmpty()) {
                            tsfc.add(sf2);
                            String postcode;
                            postcode = sf2.getAttribute(targetPropertyName).toString();
                            result.add(postcode);
                        }
                    }
                    fi.close();
                }
                fi2.close();
                DW_Shapefile.transact(tPostcodeShapefile, sft, tsfc, sdsf);
                writeAreaCodes(
                        postcodeAreaCodesFile,
                        result,
                        area,
                        level);
            }
        }
        return result;
    }

    public static File getPostcodeShapefile(
            String area,
            String level) {
        File result;
        result = new File(
                DW_Files.getGeneratedPostcodeDir(),
                area + level + "PolyShapefile.shp");
        result = new File(
                result,
                area + level + "PolyShapefile.shp");
        return result;
    }

    public static DW_Shapefile getLADShapefile(
            String name,
            TreeSet<String> tLADCensusCodes,
            ShapefileDataStoreFactory sdsf) {
        DW_Shapefile result;
        // Get LAD shapefiles
        String levelLAD = "LAD";
        // Read LAD Census Boundary Data
        File tLADShapefile = DW_Maps.getAreaBoundaryShapefile(levelLAD);
        DW_Shapefile tLAD_DW_Shapefile;
        tLAD_DW_Shapefile = new DW_Shapefile(tLADShapefile);
        result = getLADShapefile(
                name,
                tLADCensusCodes,
                tLAD_DW_Shapefile,
                sdsf);
        return result;
    }

    public static DW_Shapefile getLADShapefile(
            String name,
            TreeSet<String> tLADCensusCodes,
            DW_Shapefile tLAD_DW_Shapefile,
            ShapefileDataStoreFactory sdsf) {
        DW_Shapefile result;
        FeatureCollection tLAD_FC = tLAD_DW_Shapefile.getFeatureCollection();
        SimpleFeatureType tLAD_SFT = tLAD_DW_Shapefile.getSimpleFeatureType();
        // Select tLADCensusCodes from LAD Census Boundary Data
        File tLADShapefile = getCensusBoundaryShapefile(
                name,
                "LAD");
        result = new DW_Shapefile(tLADShapefile);
        if (!tLADShapefile.exists()) {
            DW_Maps.selectAndCreateNewShapefile(
                    sdsf,
                    tLAD_FC,
                    tLAD_SFT,
                    tLADCensusCodes,
                    "CODE",
                    tLADShapefile);
        }
        return result;
    }

    public static File getPostcodeDataAreaCodesFile(
            String area,
            String level) {
        File result;
        File postcodeDataDirectory = new File(
                DW_Files.getGeneratedPostcodeDir(),
                area);
        postcodeDataDirectory = new File(
                postcodeDataDirectory,
                level);
        postcodeDataDirectory.mkdirs();
        result = new File(
                postcodeDataDirectory,
                "AreaCodes.csv");
        return result;
    }

    public static void writeAreaCodes(
            File postcodeDataAreaCodesFile,
            TreeSet<String> result,
            String area,
            String level) {
        postcodeDataAreaCodesFile.getParentFile().mkdirs();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(postcodeDataAreaCodesFile);
            Iterator<String> ite;
            ite = result.iterator();
            while (ite.hasNext()) {
                pw.println(ite.next());
            }
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DW_AreaCodesAndShapefiles.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param area
     * @param level "OA" or "LSOA" or "MSOA" currently...
     * @return <code>TreeSet&ltString&gt</code> of LSOA codes for the Leeds
     * Local Authority District loaded from a specific file within
     * digitalWelfareDir.
     */
    public static TreeSet<String> getAreaCodes(
            String area,
            String level) {
        TreeSet<String> result = null;
        File postcodeDataDirectory = new File(
                DW_Files.getGeneratedPostcodeDir(),
                area);
        postcodeDataDirectory = new File(
                postcodeDataDirectory,
                level);
        File file = new File(
                postcodeDataDirectory,
                "AreaCodes.csv");
        if (file.exists()) {
            try {
                BufferedReader br;
                StreamTokenizer st;
                br = Generic_StaticIO.getBufferedReader(file);
                if (br != null) {
                    result = new TreeSet<String>();
                    st = new StreamTokenizer(br);
                    Generic_StaticIO.setStreamTokenizerSyntax1(st);
                    int token = st.nextToken();
//                    long RecordID = 0;
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
                    br.close();
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        return result;
    }

    /**
     * @return the tLeedsAreaCodes
     */
    public TreeSet<String> getLeedsAreaCodes() {
        return _LeedsAreaCodes;
    }

    /**
     * @return the _LeedsAndNeighbouringLADAreaCodes
     */
    public TreeSet<String> getLeedsAndNeighbouringLADAreaCodes() {
        return _LeedsAndNeighbouringLADAreaCodes;
    }

    /**
     * @return the _LeedsAndNearNeighbouringLADAreaCodes
     */
    public TreeSet<String> getLeedsAndNearNeighbouringLADAreaCodes() {
        return _LeedsAndNearNeighbouringLADAreaCodes;
    }

    /**
     * @return the levelShapefile
     */
    public DW_Shapefile getLevelDW_Shapefile() {
        return _LevelDW_Shapefile;
    }

    /**
     * @return getLevelDW_Shapefile().getFeatureCollection();
     */
    public FeatureCollection getLevelFC() {
        return getLevelDW_Shapefile().getFeatureCollection();
    }

    /**
     * @return getLevelDW_Shapefile().getSimpleFeatureType();
     */
    public SimpleFeatureType getLevelSFT() {
        return getLevelDW_Shapefile().getSimpleFeatureType();
    }

    /**
     * @return the _LeedsLevelDW_Shapefile
     */
    public DW_Shapefile getLeedsLevelDW_Shapefile() {
        return _LeedsLevelDW_Shapefile;
    }

    /**
     * @return getLeedsLADDW_Shapefile().getFeatureCollection();
     */
    public FeatureCollection getLeedsLADFC() {
        return getLeedsLADDW_Shapefile().getFeatureCollection();
    }

    /**
     * @return return getLeedsLADDW_Shapefile().getSimpleFeatureType();
     */
    public SimpleFeatureType getLeedsLADSFT() {
        return getLeedsLADDW_Shapefile().getSimpleFeatureType();
    }

    /**
     * @return the _LeedsLADDW_Shapefile
     */
    public DW_Shapefile getLeedsLADDW_Shapefile() {
        return _LeedsLADDW_Shapefile;
    }

    /**
     * @return the _LeedsAndNeighbouringLADDW_Shapefile
     */
    public DW_Shapefile getLeedsAndNeighbouringLADDW_Shapefile() {
        return _LeedsAndNeighbouringLADDW_Shapefile;
    }

    /**
     * @return the _LeedsAndNearNeighbouringLADDW_Shapefile
     */
    public DW_Shapefile getLeedsAndNearNeighbouringLADDW_Shapefile() {
        return _LeedsAndNearNeighbouringLADDW_Shapefile;
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
