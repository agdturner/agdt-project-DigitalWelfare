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
package uk.ac.leeds.ccg.andyt.projects.digitalwelfare.data.underoccupied;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_StaticIO;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.core.DW_Environment;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.core.DW_ID;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.core.DW_Object;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.core.DW_Strings;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.data.shbe.DW_SHBE_Data;
import uk.ac.leeds.ccg.andyt.projects.digitalwelfare.io.DW_Files;

/**
 *
 * @author geoagdt
 */
public class DW_UO_Handler extends DW_Object {

    /**
     * For convenience this is a reference to env.DW_Files.
     */
    protected DW_Strings DW_Strings;
    protected DW_Files DW_Files;
    private DW_SHBE_Data DW_SHBE_Data;
    private HashMap<DW_ID, String> ClaimIDToClaimRefLookup;
    private HashMap<String, DW_ID> ClaimRefToClaimIDLookup;

    private HashSet<String> RecordTypes;

    public DW_UO_Handler() {
    }

    public DW_UO_Handler(DW_Environment env) {
        super(env);
        this.DW_Files = env.getDW_Files();
        this.DW_Strings = env.getDW_Strings();
        DW_SHBE_Data = env.getDW_SHBE_Data();
        ClaimIDToClaimRefLookup = DW_SHBE_Data.getClaimIDToClaimRefLookup();
        ClaimRefToClaimIDLookup = DW_SHBE_Data.getClaimRefToClaimIDLookup();
    }

    public HashSet<String> getRecordTypes() {
        return RecordTypes;
    }

    /**
     * @param directory
     * @param filename
     * @return
     */
    public HashMap<DW_ID, DW_UO_Record> loadInputData(
            File directory,
            String filename) {
//        String type;
//        if (filename.contains("RSL")) {
//            type = "RSL";
//        } else {
//            type = "Council";
//        }
        HashMap<DW_ID, DW_UO_Record> result;
        result = new HashMap<DW_ID, DW_UO_Record>();
        File inputFile = new File(
                directory,
                filename);
        boolean addedNewClaimIDs;
        addedNewClaimIDs = false;
        try {
            BufferedReader br = Generic_StaticIO.getBufferedReader(inputFile);
            StreamTokenizer st
                    = new StreamTokenizer(br);
            Generic_StaticIO.setStreamTokenizerSyntax5(st);
            st.wordChars('`', '`');
            st.wordChars('*', '*');
            String line = "";
            //int duplicateEntriesCount = 0;
            int replacementEntriesCount = 0;
            long RecordID = 0;
            // Read firstline and check format
            int tokenType;
            tokenType = st.nextToken();
            line = st.sval;
            String[] fieldnames = line.split(",");
//            // Skip the first line
//            Generic_StaticIO.skipline(st);

            // Read data
            tokenType = st.nextToken();
            while (tokenType != StreamTokenizer.TT_EOF) {
                switch (tokenType) {
                    case StreamTokenizer.TT_EOL:
                        //System.out.println(line);
                        break;
                    case StreamTokenizer.TT_WORD:
                        line = st.sval;
                        try {
                            DW_UO_Record DW_UO_Record;
                            DW_UO_Record = new DW_UO_Record(
                                    RecordID, line, fieldnames);
                            //RecordID, line, type);
                            String ClaimRef;
                            ClaimRef = DW_UO_Record.getClaimRef();
                            DW_ID ClaimID;
                            ClaimID = ClaimRefToClaimIDLookup.get(ClaimRef);
                            if (ClaimID == null) {
                                ClaimID = new DW_ID(ClaimRefToClaimIDLookup.size());
                                ClaimRefToClaimIDLookup.put(ClaimRef, ClaimID);
                                ClaimIDToClaimRefLookup.put(ClaimID, ClaimRef);
                                addedNewClaimIDs = true;
                            }
                            Object o = result.put(
                                    ClaimID,
                                    DW_UO_Record);
                            if (o != null) {
                                DW_UO_Record existingUnderOccupiedReport_Record;
                                existingUnderOccupiedReport_Record = (DW_UO_Record) o;
                                if (!existingUnderOccupiedReport_Record.equals(DW_UO_Record)) {
                                    System.out.println("existingUnderOccupiedReport_DataRecord " + existingUnderOccupiedReport_Record);
                                    System.out.println("replacementUnderOccupiedReport_DataRecord " + DW_UO_Record);
                                    System.out.println("RecordID " + RecordID);
                                    replacementEntriesCount++;
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("DW_UnderOccupiedReport_Handler error processing from file " + inputFile);
                            System.err.println(line);
                            System.err.println("RecordID " + RecordID);
                            System.err.println(e.getLocalizedMessage());
                        }
                        RecordID++;
                        break;
                }
                tokenType = st.nextToken();
            }
            //System.out.println("replacementEntriesCount " + replacementEntriesCount);
            br.close();
            if (addedNewClaimIDs) {
                Generic_StaticIO.writeObject(
                        ClaimIDToClaimRefLookup,
                        DW_SHBE_Data.getClaimIDToClaimRefLookupFile());
                Generic_StaticIO.writeObject(
                        ClaimRefToClaimIDLookup,
                        DW_SHBE_Data.getClaimRefToClaimIDLookupFile());
            }
        } catch (IOException ex) {
            Logger.getLogger(DW_UO_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Loads the Under-Occupied report data for Leeds.
     *
     * @param reload If reload is true then the data is reloaded from source.
     * @return
     */
    public DW_UO_Data loadUnderOccupiedReportData(boolean reload) {
        String methodName;
        methodName = "loadUnderOccupiedReportData()";
        env.logO("<" + methodName + ">", true);
        DW_UO_Data result;
        TreeMap<String, DW_UO_Set> CouncilSets;
        CouncilSets = new TreeMap<String, DW_UO_Set>();
        TreeMap<String, DW_UO_Set> RSLSets;
        RSLSets = new TreeMap<String, DW_UO_Set>();

        // Look where the generated data should be stored.
        // Look where the input data are.
        // Are there new files to load? If so, load them from source. If not, 
        // then load and return the cached object.
        String type;
        Object[] filenames = getInputFilenames();
        TreeMap<String, String> CouncilFilenames;
        CouncilFilenames = (TreeMap<String, String>) filenames[0];
        TreeMap<String, String> RSLFilenames;
        RSLFilenames = (TreeMap<String, String>) filenames[1];
        String year_Month;
        String filename;
        Iterator<String> ite;
        ite = CouncilFilenames.keySet().iterator();
        type = DW_Strings.sCouncil;
        while (ite.hasNext()) {
            year_Month = ite.next();
            filename = CouncilFilenames.get(year_Month);
            DW_UO_Set set;
            set = new DW_UO_Set(
                    env,
                    type,
                    filename,
                    year_Month,
                    reload);
            CouncilSets.put(year_Month, set);
        }
        ite = RSLFilenames.keySet().iterator();
        type = DW_Strings.sRSL;
        while (ite.hasNext()) {
            year_Month = ite.next();
            filename = RSLFilenames.get(year_Month);
            DW_UO_Set set;
            set = new DW_UO_Set(
                    env,
                    type,
                    filename,
                    year_Month,
                    reload);
            RSLSets.put(year_Month, set);
        }
        result = new DW_UO_Data(env, RSLSets, CouncilSets);
        env.logO("</" + methodName + ">", true);
        return result;
    }

    /**
     * Returns the number of input files of UnderOccupied Data
     *
     * @return
     */
    public int getNumberOfInputFiles() {
        int result;
        File dirIn;
        dirIn = DW_Files.getInputUnderOccupiedDir();
        File[] files;
        files = dirIn.listFiles();
        result = files.length;
        return result;
    }

    /**
     * Returns the number of generated files of UnderOccupied Data
     *
     * @return
     */
    public int getNumberOfGeneratedFiles() {
        int result;
        File dirIn;
        dirIn = DW_Files.getGeneratedUnderOccupiedDir();
        result = dirIn.listFiles().length;
        return result;
    }

    /**
     * Do we really want to store these?
     *
     * @return Object[] result where: null null null null     {@code 
     * result[0] TreeMap<String, String> councilFilenames (year_Month, filename)
     * result[1] TreeMap<String, String> RSLFilenames (year_Month, filename)
     * }
     */
    private Object[] inputFilenames;

    /**
     * This needs modifying as more datasets are added currently....
     *
     * @return
     */
    public Object[] getInputFilenames() {
        if (inputFilenames == null) {
            inputFilenames = new Object[2];
            TreeMap<String, String> CouncilFilenames;
            TreeMap<String, String> RSLFilenames;
            CouncilFilenames = new TreeMap<String, String>();
            RSLFilenames = new TreeMap<String, String>();
            inputFilenames[0] = CouncilFilenames;
            inputFilenames[1] = RSLFilenames;
//            String[] list = DW_Files.getInputUnderOccupiedDir().list();
//            String s;
//            String ym;
//            TreeMap<String,String> yms;
//            yms = new TreeMap<String,String>();
//            for (String list1 : list) {
//                s = list1;
//                ym = getYearMonthNumber(s);
//                yms.put(ym, s);
//            }
//            Iterator<String> ite;
//            ite = yms.keySet().iterator();
//            int i = 0;
//            while (ite.hasNext()) {
//                ym = ite.next();
//                SHBEFilenamesAll[i] = yms.get(ym);
//                i ++;
//            }
            CouncilFilenames.put(
                    "2013_Mar",
                    "2013 14 Under Occupied Report For University Year Start Council Tenants.csv");
            RSLFilenames.put(
                    "2013_Mar",
                    "2013 14 Under Occupied Report For University Year Start RSLs.csv");
            String councilEndFilename = " Council Tenants.csv";
            String RSLEndFilename = " RSLs.csv";
            String RSLEndFilename2 = " RSL.csv";
            String underOccupiedReportForUniversityString = " Under Occupied Report For University ";
            String yearAll;
            yearAll = "2013 14";
            //councilFilenames.add(yearAll + underOccupiedReportForUniversityString + "Year Start" + councilEndFilename);
            //registeredSocialLandlordFilenames.add(yearAll + underOccupiedReportForUniversityString + "Year Start" + RSLEndFilename);
            putFilenames(
                    yearAll,
                    underOccupiedReportForUniversityString,
                    councilEndFilename,
                    RSLEndFilename,
                    CouncilFilenames,
                    RSLFilenames,
                    12);
            yearAll = "2014 15";
            putFilenames(
                    yearAll,
                    underOccupiedReportForUniversityString,
                    councilEndFilename,
                    RSLEndFilename2,
                    CouncilFilenames,
                    RSLFilenames,
                    12);
            yearAll = "2015 16";
            putFilenames(
                    yearAll,
                    underOccupiedReportForUniversityString,
                    councilEndFilename,
                    RSLEndFilename2,
                    CouncilFilenames,
                    RSLFilenames,
                    12);
            yearAll = "2016 17";
            putFilenames(
                    yearAll,
                    underOccupiedReportForUniversityString,
                    councilEndFilename,
                    RSLEndFilename2,
                    CouncilFilenames,
                    RSLFilenames,
                    12);
            yearAll = "2017 18";
            putFilenames(
                    yearAll,
                    underOccupiedReportForUniversityString,
                    councilEndFilename,
                    RSLEndFilename2,
                    CouncilFilenames,
                    RSLFilenames,
                    3);// This number needs increasing as there are more datasets....
        }
        return inputFilenames;
    }

    protected static void putFilenames(
            String yearAll,
            String underOccupiedReportForUniversityString,
            String councilEndFilename,
            String RSLEndFilename2,
            TreeMap<String, String> councilFilenames,
            TreeMap<String, String> registeredSocialLandlordFilenames,
            int maxMonth) {
        for (int i = 1; i <= maxMonth; i++) {
            String year = getYear(yearAll, i);
            String month = getMonth3(i);
            String year_Month = year + "_" + month;
            String s;
            s = yearAll + underOccupiedReportForUniversityString + "Month " + i;
//            String filename;
//            filename = s + councilEndFilename;
//            env.getDW_Files().getInputUnderOccupiedDir();
            councilFilenames.put(
                    year_Month,
                    s + councilEndFilename);
            registeredSocialLandlordFilenames.put(
                    year_Month,
                    s + RSLEndFilename2);
        }
    }

    protected static String getYear(String yearAll, int i) {
        String result;
        String[] split;
        split = yearAll.split(" ");
        if (i < 10) {
            result = split[0];
        } else {
            int year;
            year = Integer.valueOf(split[0]);
            year++;
            result = Integer.toString(year);
        }
        return result;
    }

    // The first month of this year sequence is April (financial year)
    protected static String getMonth(int i) {
        switch (i) {
            case 10:
                return "January";
            case 11:
                return "February";
            case 12:
                return "March";
            case 1:
                return "April";
            case 2:
                return "May";
            case 3:
                return "June";
            case 4:
                return "July";
            case 5:
                return "August";
            case 6:
                return "September";
            case 7:
                return "October";
            case 8:
                return "November";
            case 9:
                return "December";
        }
        return "";
    }

    // The first month of this year sequence is April (financial year)
    protected static String getMonth3(int i) {
        return getMonth(i).substring(0, 3);
    }

    /**
     * Returns a Set<DW_ID> of the ClaimIDs of those UnderOccupying at the start
     * of April2013.
     *
     * @param DW_UO_Data
     * @return
     */
    public Set<DW_ID> getUOStartApril2013ClaimIDs(
            DW_UO_Data DW_UO_Data) {
        return DW_UO_Data.getClaimIDsInUO().get("2013_Mar");
    }

    /**
     * This returns a Set of all ClaimIDs of all Claims that have at some time
     * been classed as Council Under Occupying.
     *
     * @param DW_UO_Data
     * @return
     */
    public Set<DW_ID> getAllCouncilUOClaimIDs(
            DW_UO_Data DW_UO_Data) {
        return DW_UO_Data.getClaimIDsInCouncilUO();
    }

    public static int getHouseholdSizeExcludingPartners(DW_UO_Record rec) {
        int result;
        result
                = 1 + rec.getTotalDependentChildren()
                //rec.getChildrenOver16()
                //+ rec.getFemaleChildren10to16() + rec.getFemaleChildrenUnder10()
                //+ rec.getMaleChildren10to16() + rec.getMaleChildrenUnder10()
                + rec.getNonDependents();
        return result;
    }

}
