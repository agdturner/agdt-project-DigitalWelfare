package uk.ac.leeds.ccg.projects.dw.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import uk.ac.leeds.ccg.data.core.Data_Environment;
import uk.ac.leeds.ccg.agdt.data.census.core.Census_Environment;
import uk.ac.leeds.ccg.generic.core.Generic_Environment;
import uk.ac.leeds.ccg.generic.core.Generic_ErrorAndExceptionHandler;
import uk.ac.leeds.ccg.grids.core.Grids_Environment;
import uk.ac.leeds.ccg.agdt.data.ukp.data.UKP_Data;
import uk.ac.leeds.ccg.agdt.data.ukp.util.UKP_YM3;
import uk.ac.leeds.ccg.generic.data.shbe.core.SHBE_Environment;
import uk.ac.leeds.ccg.generic.data.shbe.data.SHBE_Handler;
import uk.ac.leeds.ccg.generic.data.shbe.data.SHBE_TenancyType_Handler;
import uk.ac.leeds.ccg.generic.memory.Generic_MemoryManager;
import uk.ac.leeds.ccg.projects.digitalwelfare.data.underoccupied.DW_UO_Data;
import uk.ac.leeds.ccg.projects.digitalwelfare.data.underoccupied.DW_UO_Handler;
import uk.ac.leeds.ccg.projects.digitalwelfare.data.underoccupied.DW_UO_Set;
import uk.ac.leeds.ccg.projects.digitalwelfare.io.DW_Files;
import uk.ac.leeds.ccg.projects.digitalwelfare.visualisation.mapping.DW_Geotools;
import uk.ac.leeds.ccg.projects.digitalwelfare.visualisation.mapping.DW_Maps;
import uk.ac.leeds.ccg.vector.core.Vector_Environment;

/**
 * This class is for a shared object, common to many objects in a Digital
 * Welfare program. It contains holders for commonly referred to objects that
 * might otherwise be constructed multiple times. It is also for handling memory
 * although for the time being, there has not been a need for convoluted
 * cacheping of data from memory to disk.
 */
public class DW_Environment extends Generic_MemoryManager
        implements Serializable {

    public String Directory;
    //public String Directory = "/scratch02/DigitalWelfare";
    //public String Directory = "C:/Users/geoagdt/projects/DigitalWelfare";

    // For convenience
    public final DW_Files files;
    public final transient Data_Environment de;
    public final transient Generic_Environment ge;
    public final transient SHBE_Environment SHBE_Env;
    public final transient Grids_Environment Grids_Env;
    public final transient Vector_Environment Vector_Env;
    public final transient Census_Environment censusEnv;
    public transient DW_Geotools Geotools;
    public transient UKP_Data ONSPD_Handler;
    public DW_UO_Handler UO_Handler;
    public DW_UO_Data UO_Data;
    public SHBE_TenancyType_Handler SHBE_TenancyType_Handler;
    public DW_Maps Maps;
    public SHBE_Handler SHBE_Handler;

    public DW_Environment(Data_Environment de) throws IOException {
        this.de = de;
        this.ge = de.env;
        this.files = new DW_Files(ge.files.getDir());
        SHBE_Env = new SHBE_Environment(de);
        Grids_Env = new Grids_Environment(ge, files.getGeneratedGridsDir());
        Vector_Env = new Vector_Environment(Grids_Env);
        File censusDir = null;
        censusEnv = new Census_Environment(de, censusDir);
    }

    public int getDefaultMaximumNumberOfObjectsPerDirectory() {
        return 100;
    }

//    /**
//     * A method to ensure there is enough memory to continue.
//     *
//     * @param handleOutOfMemoryError
//     * @return
//     */
//    @Override
//    public boolean checkAndMaybeFreeMemory(
//            boolean handleOutOfMemoryError) {
//        try {
//            if (checkAndMaybeFreeMemory()) {
//                return true;
//            } else {
//                if (Level < LOGGING_LEVEL_NORMAL) {
//                    String message
//                            = "Warning! No data to cache or clear in "
//                            + this.getClass().getName()
//                            + ".tryToEnsureThereIsEnoughMemoryToContinue(boolean)";
//                    System.out.println(message);
//                }
//                // Set to exit method with OutOfMemoryError
//                handleOutOfMemoryError = false;
//                throw new OutOfMemoryError();
//            }
//        } catch (OutOfMemoryError e) {
//            if (handleOutOfMemoryError) {
//                clearMemoryReserve();
//                boolean createdRoom = false;
//                while (!createdRoom) {
//                    if (!cacheDataAny()) {
//                        if (Level < LOGGING_LEVEL_NORMAL) {
//                            String message = "Warning! No data to cache or clear in "
//                                    + this.getClass().getName()
//                                    + ".tryToEnsureThereIsEnoughMemoryToContinue(boolean)";
//                            System.out.println(message);
//                        }
//                        throw e;
//                    }
//                    try {
//                        initMemoryReserve();
//                        createdRoom = true;
//                    } catch (OutOfMemoryError e2) {
//                        if (Level < LOGGING_LEVEL_NORMAL) {
//                            String message
//                                    = "Struggling to ensure there is enough memory in "
//                                    + this.getClass().getName()
//                                    + ".tryToEnsureThereIsEnoughMemoryToContinue(boolean)";
//                            System.out.println(message);
//                        }
//                    }
//                }
//                return checkAndMaybeFreeMemory(
//                        handleOutOfMemoryError);
//            } else {
//                throw e;
//            }
//        }
//    }
    /**
     * A method to try to ensure there is enough memory to continue.
     *
     * @return
     * @throws java.io.IOException
     */
    @Override
    public boolean checkAndMaybeFreeMemory() throws IOException {
        while (getTotalFreeMemory() < Memory_Threshold) {
            if (!cacheDataAny()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean cacheDataAny(boolean handleOutOfMemoryError)
            throws IOException {
        try {
            boolean result = cacheDataAny();
            checkAndMaybeFreeMemory();
            return result;
        } catch (OutOfMemoryError e) {
            if (handleOutOfMemoryError) {
                clearMemoryReserve();
                boolean result = cacheDataAny(HOOMEF);
                initMemoryReserve();
                return result;
            } else {
                throw e;
            }
        }
    }

    /**
     * Currently this just tries to cache a SHBE collection.
     *
     * @return
     */
    @Override
    public boolean cacheDataAny() throws IOException {
        boolean c = clearSomeSHBECache();
        if (!c) {
            ge.log("No SHBE data to clear. Do some coding to try "
                    + "to arrange to clear something else if needs be!!!");
        }
        return c;
    }

    /**
     * Clears the cache of all SHBE data.
     *
     * @return The number of sets of records cleared.
     */
    public int clearAllSHBECache() throws IOException {
        return getSHBE_Handler().clearAll();
    }

    /**
     * Swaps to file a collection.
     *
     * @return True iff a collection is cacheped.
     */
    public boolean clearSomeSHBECache() throws IOException {
        return getSHBE_Handler().clearSome();
    }

    /**
     * Swaps to file a collection.
     *
     * @param YM3
     * @return True iff a collection is cacheped.
     */
    public boolean clearSomeSHBECacheExcept(UKP_YM3 YM3) throws IOException {
        return getSHBE_Handler().clearSomeExcept(YM3);
    }

    public DW_Geotools getGeotools() throws IOException {
        if (Geotools == null) {
            Geotools = new DW_Geotools(this);
            //Geotools = new DW_Geotools(df.getGeneratedGeotoolsDir());
        }
        return Geotools;
    }

    /**
     * For returning an instance of UO_Handler for convenience.
     *
     * @return
     */
    public DW_UO_Handler getUO_Handler() throws IOException {
        if (UO_Handler == null) {
            UO_Handler = new DW_UO_Handler(this);
        }
        return UO_Handler;
    }

    /**
     * For returning an instance of UO_Data for convenience.
     *
     * @return
     */
    public DW_UO_Data getUO_Data() {
        if (UO_Data == null) {
            try {
                UO_Data = getUO_Data(false);
            } catch (IOException | ClassNotFoundException e) {
                try {
                    UO_Data = getUO_Data(true);
                } catch (IOException e1) {
                    System.err.print(e1.getMessage());
                    e.printStackTrace(System.err);
                    System.exit(Generic_ErrorAndExceptionHandler.IOException);
                } catch (ClassNotFoundException e1) {
                    System.err.print(e1.getMessage());
                    e.printStackTrace(System.err);
                    System.exit(Generic_ErrorAndExceptionHandler.ClassNotFoundException);
                }
            }
        }
        return UO_Data;
        // return getUO_Data(false);
    }

    /**
     * For returning an instance of UO_Data for convenience. If loadFromSource
     * is true then the data is reloaded from source. Otherwise if the formatted
     * version exists this is loaded. If the formatted version does not exist,
     * the data is loaded from source.
     *
     * @param loadFromSource
     * @return
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public DW_UO_Data getUO_Data(boolean loadFromSource) throws IOException, ClassNotFoundException {
        if (UO_Data == null || loadFromSource) {
            UO_Handler = getUO_Handler();
            File f;
            f = new File(files.getGeneratedUnderOccupiedDir(),
                    DW_Strings.sDW_UO_Data + DW_Strings.sBinaryFileExtension);
            if (loadFromSource) {
                UO_Data = UO_Handler.loadUnderOccupiedReportData(loadFromSource);
                ge.io.writeObject(UO_Data, f);
            } else if (f.exists()) {
                UO_Data = (DW_UO_Data) ge.io.readObject(f);
                // For debugging/testing load
                TreeMap<UKP_YM3, DW_UO_Set> CouncilUOSets;
                CouncilUOSets = UO_Data.getCouncilUOSets();
                TreeMap<UKP_YM3, DW_UO_Set> RSLUOSets;
                RSLUOSets = UO_Data.getRSLUOSets();
                int n;
                n = CouncilUOSets.size() + RSLUOSets.size();
                //logO("Number of UnderOccupancy data sets loaded " + n);
                //logO("Number of Input files " + numberOfInputFiles);
                if (n != UO_Handler.getNumberOfInputFiles()) {
                    ge.log("Warning, there are some UnderOccupancy Data that "
                            + "have not been loaded.", true);
                }
            } else {
                UO_Data = UO_Handler.loadUnderOccupiedReportData(true);
                ge.io.writeObject(UO_Data, f);
            }
        }
        return UO_Data;
    }

    /**
     * For returning an instance of UKP_Data for convenience.
     *
     * @return
     */
    public UKP_Data getONSPD_Handler() {
        if (ONSPD_Handler == null) {
            ONSPD_Handler = SHBE_Env.oe.getHandler();
        }
        return ONSPD_Handler;
    }

    /**
     * For returning an instance of SHBE_Handler for convenience.
     *
     * @return
     */
    public SHBE_Handler getSHBE_Handler() {
        if (SHBE_Env.handler == null) {
            try {
            SHBE_Env.handler = new SHBE_Handler(SHBE_Env);
            } catch (IOException ex) {
                ex.printStackTrace();
                ge.log(ex.getMessage());
            }
        }
        return SHBE_Env.handler;
    }

    /**
     * {@code this.handler = handler;}
     *
     * @param SHBE_Handler The handler to set.
     */
    public void setSHBE_Handler(SHBE_Handler SHBE_Handler) {
        SHBE_Env.handler = SHBE_Handler;
    }

    /**
     * For returning an instance of SHBE_TenancyType_Handler for convenience.
     *
     * @return
     */
    public SHBE_TenancyType_Handler getSHBE_TenancyType_Handler() {
        if (SHBE_TenancyType_Handler == null) {
            SHBE_TenancyType_Handler = new SHBE_TenancyType_Handler(SHBE_Env);
        }
        return SHBE_TenancyType_Handler;
    }

    /**
     * For returning an instance of DW_Maps for convenience.
     *
     * @return
     */
    public DW_Maps getMaps() throws IOException {
        if (Maps == null) {
            Maps = new DW_Maps(this);
        }
        return Maps;
    }
}