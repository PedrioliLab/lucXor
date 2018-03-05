/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lucxor;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.LCMSDataSubset;
import umich.ms.datatypes.scan.IScan;
import umich.ms.datatypes.scancollection.IScanCollection;
import umich.ms.datatypes.scancollection.ScanIndex;
import umich.ms.fileio.exceptions.FileParsingException;
import umich.ms.fileio.filetypes.mzml.MZMLFile;
import umich.ms.fileio.filetypes.mzxml.MZXMLFile;

/**
 * @author dfermin
 */
class Globals {

  private static File spectrumPath = null;
  static String spectrumSuffix = null;
  static String matchedPkFile = null;
  static File inputFile = null;
  static String outputFile = null;
  static String timeStamp = null;
  static String dateStamp = null;
  static int ms2tol_units;
  static int inputType;
  static int debugMode;
  static int reduceNL;
  static int peptideRepresentation;
  static int scoringMethod;
  static int scoringAlgorithm;
  static int maxChargeState;
  static int minNumPSMsForModeling;
  static int numThreads;
  static int runMode;
  static int tsvHdr;
  static int maxPepLen;
  static double ms2tol;
  static double modelTH;
  static double scoreTH;
  private static double decoyMass;
  static double minMZ;
  static double max_num_permutations;
  static double precursorNLmass;
  static double ntermMassProt;
  static double ntermMass;
  static double ctermMassProt;
  static double ctermMass;
  static boolean writeMatchedPeaks;

  static ArrayList<PSM> PSM_list = null;

  /** Mods user wants to search for. */
  static THashMap<String, Double> targetModMap = null;
  /** Fixed mods observed in data. */
  static THashMap<String, Double> fixedModMap = null;
  /** Variable mods observed in data. */
  static THashMap<String, Double> varModMap = null;
  /** Holds all neutral loss masses, k= list of amino acids v= NL mass. */
  static THashMap<String, Double> nlMap = null;
  static THashMap<String, Double> decoyNLmap = null;
  private static THashMap<Double, double[]> FLRestimateMap = null; // ary[0] = globalFLR, ary[1] = localFLR

  // TODO: ACHTUNG: XXX: Delete this monstrosity (replace with Char->Stirng or an array?)
  static THashMap<String, Double> AAmassMap = null;

  // TODO: ACHTUNG: XXX: Delete this monstrosity (replace with Char->Stirng or an array?)
  private static THashMap<Character, String> decoyAAMap = null;

  static THashMap<Integer, ModelData_CID> modelingMap_CID = null;
  static THashMap<Integer, ModelData_HCD> modelingMap_HCD = null;


  static void parse_input_file(String str) throws IOException {

    File inF = new File(str);
    if (!inF.exists()) {
      System.err.print("\nERROR! Unable to open " + str + "\n\n");
      System.exit(0);
    }

    debugMode = 0; // 0 means no debugging output
    runMode = 0; // calculate FLR and rescore PSMs without decoys (2 iterations)
    minNumPSMsForModeling = 50;
    maxPepLen = 40;
    reduceNL = 0;
    numThreads = Runtime.getRuntime().availableProcessors();
    tsvHdr = 0; // default is no header
    writeMatchedPeaks = false; // true means you will generate the matched peaks

    BufferedReader br = new BufferedReader(new FileReader(inF));
    String line;
    while ((line = br.readLine()) != null) {
      if (line.startsWith("#")) {
        continue;
      }
      if (line.length() < 2) {
        continue;
      }

      if (line.startsWith("SPECTRUM_PATH")) {
        String s = parse_input_line(line);
        spectrumPath = new File(s).getCanonicalFile();
      }

      if (line.startsWith("SPECTRUM_SUFFIX")) {
        String s = parse_input_line(line);
        spectrumSuffix = s.toLowerCase();
      }

      if (line.startsWith("INPUT_DATA")) {
        String s = parse_input_line(line);
        inputFile = new File(s);
      }

      if (line.startsWith("OUTPUT_FILE")) {
        outputFile = parse_input_line(line);
      }

      if (line.startsWith("INPUT_TYPE")) {
        String s = parse_input_line(line);
        inputType = Integer.valueOf(s);
      }

      if (line.startsWith("MAX_PEP_LEN")) {
        String s = parse_input_line(line);
        maxPepLen = Integer.valueOf(s);
      }

      if (line.startsWith("MAX_NUM_PERM")) {
        String s = parse_input_line(line);
        max_num_permutations = Double.valueOf(s);
      }

      if (line.startsWith("MIN_NUM_PSMS_MODEL")) {
        String s = parse_input_line(line);
        minNumPSMsForModeling = Integer.valueOf(s);
      }

      if (line.startsWith("MS2_TOL") && !line.contains("_UNITS")) {
        String s = parse_input_line(line);
        ms2tol = Double.valueOf(s);
      }

      if (line.startsWith("MS2_TOL_UNITS")) {
        String s = parse_input_line(line);
        ms2tol_units = Integer.valueOf(s);
      }

      if (line.startsWith("ALGORITHM")) {
        String s = parse_input_line(line);
        scoringAlgorithm = Integer.valueOf(s);
      }

      if (line.startsWith("TSV_HEADER")) {
        String s = parse_input_line(line);
        tsvHdr = Integer.valueOf(s);
      }

      if (line.startsWith("REDUCE_PRECURSOR_NL")) {
        String s = parse_input_line(line);
        reduceNL = Integer.valueOf(s);
      }

      if (line.startsWith("PRECURSOR_NL_MASS_DIFF")) {
        String s = parse_input_line(line);
        precursorNLmass = Double.valueOf(s);
      }

      if (line.startsWith("SELECTION_METHOD")) {
        String s = parse_input_line(line);
        scoringMethod = Integer.valueOf(s);
      }

      if (line.startsWith("MODELING_SCORE_THRESHOLD")) {
        String s = parse_input_line(line);
        modelTH = Double.valueOf(s);
      }

      if (line.startsWith("MAX_CHARGE_STATE")) {
        String s = parse_input_line(line);
        maxChargeState = Integer.valueOf(s);
      }

      if (line.startsWith("NUM_THREADS")) {
        String s = parse_input_line(line);
        int x = Integer.valueOf(s);
        // We do minus 1 because 1 thread already goes to
        // running the whole program
        if (x < 0) {
          numThreads = 1;
        } else if (x > 1) {
          numThreads = (x - 1);
        } else if (x == 0) {
          numThreads = Runtime.getRuntime().availableProcessors();
        } else {
          numThreads = x;
        }
      }

      if (line.startsWith("DEBUG_MODE")) {
        String s = parse_input_line(line);
        debugMode = Integer.valueOf(s);
      }

      if (line.startsWith("WRITE_MATCHED_PEAKS_FILE")) {
        String s = parse_input_line(line);
        if (s.equals("1")) {
          writeMatchedPeaks = true;
        }
      }

      if (line.startsWith("RUN_MODE")) {
        String s = parse_input_line(line);
        runMode = Integer.valueOf(s);
        if (runMode > 1) {
          runMode = 0;
        }
      }

      if (line.startsWith("SCORING_THRESHOLD")) {
        String s = parse_input_line(line);
        scoreTH = Double.valueOf(s);
      }

      if (line.startsWith("DECOY_MASS")) {
        String s = parse_input_line(line);
        decoyMass = Double.valueOf(s);
      }

      if (line.startsWith("DECOY_NL")) {
        String[] ary = parse_NL_line(line);
        String k = ary[0].substring(1);
        double m = Double.valueOf(ary[1]);
        decoyNLmap.put(k, m);
      }

      if (line.startsWith("NL")) {
        String[] ary = parse_NL_line(line);
        double m = Double.valueOf(ary[1]);
        nlMap.put(ary[0], m);
      }

      if (line.startsWith("MIN_MZ")) {
        String s = parse_input_line(line);
        minMZ = Double.valueOf(s);
      }

      if (line.startsWith("MOD_PEP_REP")) {
        String s = parse_input_line(line);
        peptideRepresentation = Integer.valueOf(s);
      }

      if (line.startsWith("TARGET_MOD")) {
        String[] ary = parse_input_mod_line(line);
        double m = Double.valueOf(ary[1]);
        targetModMap.put(ary[0].toUpperCase(), m);
      }

      // You only need to extract the VAR_MOD and FIXED_MOD values
      // from the input file if you are using TSV files
      if (Globals.inputType == Constants.TSV) {
        if (line.startsWith("VAR_MOD")) {
          String[] ary = parse_input_mod_line(line);
          double m = Double.valueOf(ary[1]);
          varModMap.put(ary[0].toLowerCase(), m); // variable mods are lower case
        }

        if (line.startsWith("FIXED_MOD")) {
          String[] ary = parse_input_mod_line(line);
          double m = Double.valueOf(ary[1]);
          fixedModMap.put(ary[0].toUpperCase(), m);
        }
      }
    }
    br.close();

    if ((null == outputFile) || (outputFile.isEmpty())) {
      outputFile = "luciphor_results.tsv";
    }

    String classStr = "";
    switch (scoringMethod) {
      case Constants.PEPPROPHET:
        classStr = "Peptide Prophet Prob.";
        break;
      case Constants.MASCOTIONSCORE:
        classStr = "Mascot Ion Score";
        break;
      case Constants.NEGLOGEXPECT:
        classStr = "-log(Expect Value) (X!Tandem or Comet)";
        break;
      case Constants.XTDHYPERSCORE:
        classStr = "X!Tandem Hyperscore";
        break;
      case Constants.XCORR:
        classStr = "Sequest XCorr";
        break;
      default:
        System.err.print("\nERROR! Unknown scoring method: " + scoringMethod + "\n\n");
        System.exit(0);
        break;
    }

    int NCPU = numThreads;
    if ((numThreads > 1) && (numThreads < Runtime.getRuntime().availableProcessors())) {
      NCPU = (numThreads + 1);
    }

    System.err.println("Spectrum Path:           " + spectrumPath.getAbsolutePath());
    System.err.println("Spectrum Suffix:         " + spectrumSuffix);
    System.err.println("Input file:              " + inputFile);
    System.err
        .println("Input type:              " + (inputType == Constants.PEPXML ? "pepXML" : "tsv"));
    System.err.println(
        "MS2 tolerance:           " + ms2tol + (ms2tol_units == Constants.DALTONS ? " Da"
            : " ppm"));
    System.err
        .println("Luciphor Algorithm:      " + (scoringAlgorithm == Constants.CID ? "CID" : "HCD"));
    System.err.println("Classifying on:          " + classStr);
    System.err.println("Run Mode:                " + (runMode == 0 ? "Default" : "Report Decoys"));
    System.err.println("Num of Threads:          " + NCPU);
    System.err.println("Modeling Threshold:      " + modelTH);
    System.err.println("Scoring Threshold:       " + scoreTH);
    System.err.println("Permutation Limit:       " + max_num_permutations);
    System.err.println("Max peptide length:      " + maxPepLen);
    System.err.println("Min num PSMs for model:  " + minNumPSMsForModeling);
    System.err.println("Decoy Mass Adduct:       " + decoyMass);
    System.err.println("Max Charge State:        " + maxChargeState);
    System.err.println("Reduce NL:               " + (reduceNL == 0 ? "no" : "yes"));
    System.err.println("Output File:             " + outputFile);
    System.err.println("Write matched Peaks:     " + (writeMatchedPeaks ? "yes" : "no"));
    System.err.print("\n");

    if (debugMode != 0) {
      System.err.println("Debug mode:              " + debugMode + "  (Limiting to 1 CPU)\n");
      Globals.numThreads = 1;
    }

    System.err.println("Mods to score:");
    for (String s : targetModMap.keySet()) {
      System.err.println(s + "\t" + targetModMap.get(s));
    }

    if (!nlMap.isEmpty()) {
      System.err.println("\nAllowed Neutral Losses:");
      for (String s : nlMap.keySet()) {
        System.err.println(s + "\t" + nlMap.get(s));
      }
      for (String s : decoyNLmap.keySet()) {
        System.err.println("<X>" + s + "\t" + decoyNLmap.get(s) + "  (Decoy NL)");
      }
    }

  }


  private static String parse_input_line(String line) {
    StringBuilder sb = new StringBuilder();
    int N = line.length();

    int b = line.indexOf("=") + 1;

    for (int i = b; i < N; i++) {
      char c = line.charAt(i);
      if (c == '#') {
        break;
      }
      if (c == ' ') {
        continue;
      }
      sb.append(c);
    }

    return sb.toString();
  }


  private static String[] parse_input_mod_line(String line) {
    String[] ret = new String[2];

    char aa = 0;
    StringBuilder sb = new StringBuilder();
    double mass;
    int N = line.length();

    int b = line.indexOf("=") + 1;

    for (int i = b; i < N; i++) {
      char c = line.charAt(i);

      if (c == '#') {
        break;
      }
      if (c == ' ') {
        continue;
      }

      if (Character.isAlphabetic(c)) {
        aa = c;
      } else if ((c == '[') || (c == ']')) {
        aa = c;
      } else {
        sb.append(c);
      }
    }

    mass = Double.valueOf(sb.toString());

    ret[0] = Character.toString(aa);
    ret[1] = String.valueOf(mass);

    return ret;
  }


  private static String[] parse_NL_line(String line) {
    String[] ret = new String[2];

    line = line.replaceAll("#", "");

    String[] tmp = line.split("\\s+");

    ret[0] = tmp[2] + tmp[3];
    ret[1] = tmp[4];

    return ret;
  }


  static void initialize() {
    PSM_list = new ArrayList<>();

    decoyAAMap = new THashMap<>();
    AAmassMap = new THashMap<>();
    targetModMap = new THashMap<>();
    fixedModMap = new THashMap<>();
    varModMap = new THashMap<>();
    nlMap = new THashMap<>();
    decoyNLmap = new THashMap<>();

    ntermMass = 0d;
    ntermMassProt = 0d;
    ctermMass = 0d;
    ctermMassProt = 0d;

    // in case we need it, initialize the timestamp signature variable
    java.util.Date date = new java.util.Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMdd-hh_mm_ss");
    timeStamp = sdf.format(date);
    sdf = new SimpleDateFormat("yyyMMMdd");
    dateStamp = sdf.format(date);

    AAmassMap.put("A", 71.03711);
    AAmassMap.put("R", 156.10111);
    AAmassMap.put("N", 114.04293);
    AAmassMap.put("D", 115.02694);
    AAmassMap.put("C", 103.00919);
    AAmassMap.put("E", 129.04259);
    AAmassMap.put("Q", 128.05858);
    AAmassMap.put("G", 57.02146);
    AAmassMap.put("H", 137.05891);
    AAmassMap.put("I", 113.08406);
    AAmassMap.put("L", 113.08406);
    AAmassMap.put("K", 128.09496);
    AAmassMap.put("M", 131.04049);
    AAmassMap.put("F", 147.06841);
    AAmassMap.put("P", 97.05276);
    AAmassMap.put("S", 87.03203);
    AAmassMap.put("T", 101.04768);
    AAmassMap.put("W", 186.07931);
    AAmassMap.put("Y", 163.06333);
    AAmassMap.put("V", 99.06841);

    decoyAAMap.put('2', "A");
    decoyAAMap.put('3', "R");
    decoyAAMap.put('4', "N");
    decoyAAMap.put('5', "D");
    decoyAAMap.put('6', "C");
    decoyAAMap.put('7', "E");
    decoyAAMap.put('8', "Q");
    decoyAAMap.put('9', "G");
    decoyAAMap.put('0', "H");
    decoyAAMap.put('@', "I");
    decoyAAMap.put('#', "L");
    decoyAAMap.put('$', "K");
    decoyAAMap.put('%', "M");
    decoyAAMap.put('&', "F");
    decoyAAMap.put(';', "P");
    decoyAAMap.put('?', "W");
    decoyAAMap.put('~', "V");
    decoyAAMap.put('^', "S");
    decoyAAMap.put('*', "T");
    decoyAAMap.put('=', "Y");
  }


  // Function to incorporate information read in from input file into the
  // AAmassMap object
  public static void loadUserMods() {

    // Assign the information you got from the input file to the
    // relevant map. If none were given, then this should just proceed
    // without changing anything.
    for (String c : targetModMap.keySet()) {
      double mass = AAmassMap.get(c) + targetModMap.get(c);
      String symbol = c.toLowerCase();
      AAmassMap.put(symbol, mass);
    }

    for (String c : fixedModMap.keySet()) {
      double mass = AAmassMap.get(c) + fixedModMap.get(c);
      String symbol = c.toUpperCase();
      AAmassMap.put(symbol, mass);
    }

    for (String c : varModMap.keySet()) { // this will be a lower case key

      if (c.equalsIgnoreCase("[")) {
        Globals.ntermMass = varModMap.get(c);
      } else if (c.equalsIgnoreCase("]")) {
        Globals.ctermMass = varModMap.get(c);
      } else {
        double mass = AAmassMap.get(c.toUpperCase()) + varModMap.get(c);
        AAmassMap.put(c, mass);
      }
    }

    // Now add the decoy masses to the AAmassMap
    for (Character c : decoyAAMap.keySet()) {
      String trueAA = decoyAAMap.get(c);

      if (varModMap.containsKey(trueAA)) {
        continue;
      }
      if (targetModMap.containsKey(trueAA)) {
        continue;
      }

      double mass = AAmassMap.get(trueAA) + Globals.decoyMass;
      AAmassMap.put(c.toString(), mass);
    }

  }


  /**
   * This function is only called if we are getting our modifications from a pepXML file.
   */
  static void recordModsFromPepXML() {

    String alphabet = PepXML.AA_CHARS_UPPER;

    for (String c : fixedModMap.keySet()) {

      if (!alphabet.contains(c)) {
        continue; // skip non-amino acid characters
      }

      double mass = AAmassMap.get(c) + fixedModMap.get(c);
      String symbol = c.toUpperCase();
      AAmassMap.put(symbol, mass);
    }

    // First filter the data in varModMap.
    // We want to remove non-standard amino acid characters and remove
    // the amino acids that are in our 'targetModMap' variable.
    HashMap<String, Double> tmp = new HashMap<>(varModMap);
    varModMap.clear();

    for (String c : tmp.keySet()) {
      String C = c.toUpperCase();
      double mass = tmp.get(c);
      if (!alphabet.contains(C)) {
        continue; // skip non-amino acid characters
      }
      if (targetModMap.containsKey(C)) {
        continue; // skip target residues
      }
      varModMap.put(c, mass);
    }

    for (String c : varModMap.keySet()) {
      String C = c.toUpperCase();
      double mass = AAmassMap.get(C) + varModMap.get(c);
      AAmassMap.put(c, mass);
    }
  }


  public static double round_dbl(double value, int numPlaces) {
    double ret;
    double N = Math.pow(10, numPlaces);

    ret = (double) Math.round((value * N)) / N;

    return ret;
  }


  static void read_in_spectra()
      throws IOException, IllegalStateException, FileParsingException {

    System.err.println("\nReading spectra from " + Globals.spectrumPath.getCanonicalPath() + "  ("
        + Globals.spectrumSuffix.toUpperCase() + " format)");
    System.err.println("This can take a while so please be patient.");

    final List<String> rawFileNames = PSM_list.stream()
        .map(psm -> psm.srcFile).distinct().sorted()
        .collect(Collectors.toList());
    System.err.printf("Input PSMs originated from %d spectral data files:%n", rawFileNames.size());
    rawFileNames.forEach(file -> System.out.printf("\t%s%n", file));
    // test file paths
    final Set<String> rawPathNonExistent = rawFileNames.stream()
        .filter(file -> !Files.exists(Paths.get(Globals.spectrumPath.toString(), file)))
        .collect(Collectors.toSet());
    if (!rawPathNonExistent.isEmpty()) {
      System.err.printf("Files don't exist, PSMs from them will be skipped:%n");
      rawPathNonExistent.forEach(path -> System.out.printf("\t%s%n", path));
    }

    // map whatever paths are in PSMs list to absolute paths
    final Map<String, Path> mapPaths = rawFileNames.stream()
        .filter(file -> !rawPathNonExistent.contains(file))
        .collect(Collectors.toMap(f -> f, f -> Paths.get(Globals.spectrumPath.toString(), f).toAbsolutePath()));

    TreeMap<String, TreeMap<Integer, PSM>> mapFilesToPsms = new TreeMap<>();
    final ArrayList<PSM> psmsKept = new ArrayList<>(PSM_list.size());
    PSM_list.stream()
        .filter(psm -> !rawPathNonExistent.contains(psm.srcFile))
        .forEach(psm -> {
          psmsKept.add(psm);
          TreeMap<Integer, PSM> map = mapFilesToPsms.get(psm.srcFile);
          if (map == null) map = new TreeMap<>();
          map.put(psm.scanNum, psm);
          mapFilesToPsms.put(mapPaths.get(psm.srcFile).toString(), map);
        });
    PSM_list = psmsKept;

    if (Globals.spectrumSuffix.equalsIgnoreCase("mgf")) {

      for (String specFile : mapFilesToPsms.keySet()) {
        TIntObjectHashMap<SpectrumClass> curSpectra = read_mgf(specFile);
        String fileName = new File(specFile).getName(); // get just the file name of specFile
        int assignedSpectraCtr = 0;

        // Assign the spectra to their respective PSMs
        for (PSM p : PSM_list) {
          if (p.srcFile.equalsIgnoreCase(fileName)) {
            if (curSpectra.containsKey(p.scanNum)) {
              p.recordSpectra(curSpectra.get(p.scanNum));
              assignedSpectraCtr++;
            }
          }
        }
        System.err.println(fileName + ": " + assignedSpectraCtr + " spectra read in.");
      }
    } else if (Globals.spectrumSuffix.equalsIgnoreCase("mzXML")) {
      read_mzXML(mapFilesToPsms);
    } else if (Globals.spectrumSuffix.equalsIgnoreCase("mzML")) {
      read_mzML(mapFilesToPsms);
    }
  }


  /****************
   * Function reads in spectral data from mzML files.
   */
  private static void read_mzML(TreeMap<String, TreeMap<Integer, PSM>> mapFilesToPsms) throws FileParsingException {

    // Iterate over the file names
    for (String fn : mapFilesToPsms.keySet()) {
      String fileName = new File(fn).getName();
      System.err.printf("\n%s: ...", fileName); // beginning of info line

      final TreeMap<Integer, PSM> psmsOfInterest = mapFilesToPsms.get(fn);
      Integer threads = numThreads >= 0 ? numThreads : null;

      // read in the mzML file
      LCMSData lcmsData = null;
      try (MZMLFile mzml = new MZMLFile(fn)) {
        mzml.setNumThreadsForParsing(threads);
        mzml.setParsingTimeout(60L); // 1 minute before it times out trying to read a file
        lcmsData = new LCMSData(mzml);

        lcmsData.load(LCMSDataSubset.MS2_WITH_SPECTRA);
        IScanCollection scans = lcmsData.getScans();
        ScanIndex ms2ScanIndex = scans.getMapMsLevel2index().get(2);

        for (Entry<Integer, PSM> psmEntry : psmsOfInterest.entrySet()) {
          final Integer scanNum = psmEntry.getKey();
          final PSM psm = psmEntry.getValue();
          final IScan scan = ms2ScanIndex.getNum2scan().get(scanNum);
          if (scan == null) {

            continue;
          }
          SpectrumClass x = new SpectrumClass(scan.getSpectrum().getMZs(),
              scan.getSpectrum().getIntensities());
          psm.recordSpectra(x);
        }
      } finally {
        if (lcmsData != null) lcmsData.releaseMemory();
      }
      System.err.printf("\r%s: Done\n", fileName); // beginning of info line
    }

  }


  static double getFragmentIonMass(String x, double z, double addl_mass) {
    double ret = 1.00728 * z;

    int start = x.indexOf(":") + 1;
    int stop = x.length();

    if (x.contains("-")) {
      stop = x.indexOf("-");
    }

    for (int i = start; i < stop; i++) {
      String c = Character.toString(x.charAt(i));

      if (AAmassMap.containsKey(c)) {
        double mass = AAmassMap.get(c);
        ret += mass;
      }
    }

    ret += addl_mass; // y-ions have a water molecule extra

    return ret;
  }


  // Function reads in an MGF file and returns it as a HashMap
  private static TIntObjectHashMap<SpectrumClass> read_mgf(String specFile)
      throws IOException {
    TIntObjectHashMap<SpectrumClass> ret = new TIntObjectHashMap<>();

    File mgf = new File(specFile);
    BufferedReader br = new BufferedReader(new FileReader(mgf));
    String line;
    int scanNum = 0;
    SpectrumClass S;
    ArrayList<Double> mzAL = null, intensityAL = null;

    while ((line = br.readLine()) != null) {

      if (line.length() < 2) {
        continue;
      }

      if (line.startsWith("END IONS")) {
        if ((null != mzAL) && (!mzAL.isEmpty())) {

          int N = mzAL.size();
          double[] mz = new double[N];
          double[] I = new double[N];
          for (short i = 0; i < N; i++) {
            mz[i] = mzAL.get(i);
            I[i] = intensityAL.get(i);
          }
          S = new SpectrumClass(mz, I);
          ret.put(scanNum, S);

          mzAL = null;
          intensityAL = null;
        }
        scanNum = 0;
      }

      if (line.startsWith("BEGIN IONS")) {
        mzAL = new ArrayList<>();
        intensityAL = new ArrayList<>();
      }

      if (line.startsWith("TITLE=")) {
        int i = line.indexOf(".") + 1;
        int j = line.indexOf(".", i);
        String s = line.substring(i, j);
        scanNum = Integer.valueOf(s);
      }

      if (line.startsWith("CHARGE") || line.startsWith("PEPMASS")) {
        continue;
      }

      if (Character.isDigit(line.charAt(0))) {
        String[] ary = line.split("\\s+");
        double mz = round_dbl(Double.valueOf(ary[0]), 8);
        double I = round_dbl(Double.valueOf(ary[1]), 8);
        mzAL.add(mz);
        intensityAL.add(I);
      }
    }

    return ret;
  }


  // Function to read spectra from mzXML file
  private static void read_mzXML(TreeMap<String, TreeMap<Integer, PSM>> mapFilesToPsms) throws FileParsingException {

    // Iterate over the file names
    for (String fn : mapFilesToPsms.keySet()) {
      String fileName = new File(fn).getName();
      System.err.printf("\n%s: ...", fileName); // beginning of info line

      final TreeMap<Integer, PSM> psmsOfInterest = mapFilesToPsms.get(fn);
      Integer threads = numThreads >= 0 ? numThreads : null;

      // read in the mzML file
      LCMSData lcmsData = null;
      try (MZXMLFile mzxml = new MZXMLFile(fn)) {
        mzxml.setNumThreadsForParsing(threads);
        mzxml.setParsingTimeout(60L); // 1 minute before it times out trying to read a file
        lcmsData = new LCMSData(mzxml);


        lcmsData.load(LCMSDataSubset.MS2_WITH_SPECTRA);
        IScanCollection scans = lcmsData.getScans();
        ScanIndex ms2ScanIndex = scans.getMapMsLevel2index().get(2);

        for (Entry<Integer, PSM> psmEntry : psmsOfInterest.entrySet()) {
          final Integer scanNum = psmEntry.getKey();
          final PSM psm = psmEntry.getValue();
          final IScan scan = ms2ScanIndex.getNum2scan().get(scanNum);
          if (scan == null) {

            continue;
          }
          SpectrumClass x = new SpectrumClass(scan.getSpectrum().getMZs(),
              scan.getSpectrum().getIntensities());
          psm.recordSpectra(x);
        }
      } finally {
        if (lcmsData != null) lcmsData.releaseMemory();
      }
      System.err.printf("\r%s: Done\n", fileName); // beginning of info line
    }
  }


  // Function returns the decoy 'key' from the decoy map for the given residue
  static String getDecoySymbol(char c) {
    String srcChar = Character.toString(c);

    // TODO: ACHTUNG: Continue here, change the underlying map and this whole method.

    for (Character k : decoyAAMap.keySet()) {
      String v = decoyAAMap.get(k);

      if (v.equalsIgnoreCase(srcChar)) {
        return k.toString();
        //break;
      }
    }

    return "";
  }


  // Function returns the TPP-formatted representation of the given single
  // character modification
  static String getTPPresidue(String c) {
    String ret = "";
    String orig;

    if (c.equalsIgnoreCase("[")) {
      int d = (int) Math.round(Globals.ntermMass) + 1; // adds a proton
      ret = "n[" + String.valueOf(d) + "]";
    } else if (c.equals("]")) {
      int d = (int) Math.round(Globals.ctermMass);
      ret += "c[" + String.valueOf(d) + "]";
    } else {
      int i = (int) Math.round(AAmassMap.get(c));

      if (isDecoyResidue(c)) {
        orig = decoyAAMap.get(c);
      } else {
        orig = c.toUpperCase();
      }

      ret = orig + "[" + String.valueOf(i) + "]";
    }

    return ret;
  }


  // Function returns true if the given sequence contains decoy characters
  static boolean isDecoySeq(String seq) {

    for (int i = 0; i < seq.length(); i++) {
      char c = seq.charAt(i);
      if (Character.compare('[', c) == 0) {
        continue; // n-term mod symbol
      }
      if (Character.compare(']', c) == 0) {
        continue; // c-term mod symbol
      }

      if (isDecoyResidue(Character.toString(c))) {
        return true;
      }
    }

    return false;
  }


  // Function returns true if the given residue is from the decoy list
  static boolean isDecoyResidue(String AA) {
    return decoyAAMap.containsKey(AA);
  }


  // Function writes a sample input file for LucXor to disk
  static void writeTemplateInputFile() throws IOException {
    File outF = new File("luciphor2_input_template.txt");

    FileWriter fw = new FileWriter(outF.getAbsoluteFile());
    BufferedWriter bw = new BufferedWriter(fw);

    bw.write(
        "## Input file for Luciphor2 (aka: LucXor).\n## Anything after a hash '#' is ignored\n");
    bw.write(
        "## By default, these initial parameters are for performing a phosphorylation search\n\n");
    bw.write("SPECTRUM_PATH = <fill_in> ## specify the path to the spectra here\n");
    bw.write("SPECTRUM_SUFFIX = MGF     ## available options are MGF, mzML, or mzXML\n\n");

    bw.write("## Specify your input PSM results format\n" +
        "INPUT_DATA = <fill_in> ## specify the path to the pepXML or tab-delimited file here\n" +
        "INPUT_TYPE = 0   ## 0 = TPP pepXML, 1 = tab-delimited file\n");
    bw.write("ALGORITHM = 0    ## 0 = CID method, 1 = HCD method\n\n");

    bw.write("TSV_HEADER = 0 ## This pertains ONLY to tab-delimited (TSV) input data\n" +
        "               ## 0 = the input file does NOT contain column names as the first row\n" +
        "               ## 1 = the first row of the input file is the column names\n\n");

    bw.write("MS2_TOL = 0.5      ## MS/MS fragment ion tolerance\n" +
        "MS2_TOL_UNITS = 0  ## 0 = Daltons, 1 = PPM\n\n");

    bw.write("MIN_MZ = 150.0 ## do not consider peaks below this value " +
        "for matching fragment ions\n\n");

    bw.write("OUTPUT_FILE =  ## Specify the path to your desired output filename here\n" +
        "               ## A default value will be used if nothing is specified here.\n\n");

    bw.write(
        "WRITE_MATCHED_PEAKS_FILE = 0 ## Generate a tab-delimited file of all the matched peaks\n" +
            "                             ## for the top 2 predictions of each spectra\n" +
            "                             ## Useful for plotting spectra\n" +
            "                             ## 0 = no, 1 = yes\n\n");

    bw.write("## Place here any FIXED modifications that were used in your search\n" +
        "## This field is ONLY used for tab-delimited input\n" +
        "## Syntax: FIXED_MOD = <RESIDUE> <MODIFICATION_MASS>\n" +
        "## For N-terminal modifications use '[' for the residue character\n" +
        "## For C-terminal modifications use ']' for the residue character\n" +
        "FIXED_MOD = C 57.021464\n\n");

    bw.write("## Place here any VARIABLE modifications that might be encountered that you don't\n" +
        "## want luciphor to score.\n" +
        "## This field is ONLY used for tab-delimited input\n" +
        "## Syntax: VAR_MOD = <RESIDUE> <MODIFICATION_MASS>\n" +
        "## For N-terminal modifications use '[' for the residue character\n" +
        "## For C-terminal modifications use ']' for the residue character\n" +
        "VAR_MOD = M 15.994915\n\n"
    );

    bw.write("## List the amino acids to be searched for and their mass modifications\n" +
        "## Syntax: TARGET_MOD = <RESIDUE> <MODIFICATION_MASS>\n" +
        "TARGET_MOD = S 79.966331\n" +
        "TARGET_MOD = T 79.966331\n" +
        "TARGET_MOD = Y 79.966331\n\n");

    bw.write("## List the types of neutral losses that you want to consider\n" +
        "## The residue field is case sensitive. For example: lower case 'sty' implies\n" +
        "## that the neutral loss can only occur if the specified modification is present\n" +
        "## Syntax: NL = <RESDIUES> -<NEUTRAL_LOSS_MOLECULAR_FORMULA> <MASS_LOST>\n" +
        "#NL = STE -H2O -18.01056    ## a correctly formatted example, (not actually recommended for phospho-searches)\n"
        +
        "#NL = RKQN -NH3 -17.026548  ## another correctly formatted example, (again not recommended for phospho-searches)\n"
        +
        "NL = sty -H3PO4 -97.97690\n\n");

    bw.write("DECOY_MASS = 79.966331  ## how much to add to an amino acid to make it a decoy\n\n");
    bw.write("## For handling the neutral loss from a decoy sequence.\n" +
        "## The syntax for this is identical to that of the normal neutral losses given\n" +
        "## above except that the residue is always 'X'\n" +
        "## Syntax: DECOY_NL = X -<NEUTRAL_LOSS_MOLECULAR_FORMULA> <MASS_LOST>\n" +
        "DECOY_NL = X -H3PO4 -97.97690\n\n");

    bw.write("MAX_CHARGE_STATE = 5 ## do not consider PSMs with a charge state above this value\n");
    bw.write(
        "MAX_PEP_LEN = 40 ## restrict scoring to peptides with a length shorter than this value\n");
    bw.write("MAX_NUM_PERM = 16384 ## the maximum number of permutations a sequence can have\n\n");

    bw.write("SELECTION_METHOD = 0   ## 0 = Peptide Prophet probability (default)\n" +
        "                       ## 1 = Mascot Ion Score\n" +
        "                       ## 2 = -log(E-value)\n" +
        "                       ## 3 = X!Tandem Hyperscore\n" +
        "                       ## 4 = Sequest Xcorr\n\n");

    bw.write(
        "MODELING_SCORE_THRESHOLD = 0.95 ## minimum score a PSM needs to be considered for modeling\n");
    bw.write("SCORING_THRESHOLD = 0    ## PSMs below this value will be discarded\n");
    bw.write(
        "MIN_NUM_PSMS_MODEL = 50  ## The minimum number of PSMs you need for any charge state in order to build a model for it\n\n");

    bw.write(
        "MOD_PEP_REP = 0 ## 0 = show single character modifications, 1 = show TPP-formatted modifications\n\n");

    bw.write("NUM_THREADS = 0 ## For multi-threading, zero = use all CPU found by JAVA\n\n");

    bw.write("RUN_MODE = 0 ## Determines how Luciphor will run.\n" +
        "             ## 0 = Default: calculate FLR then rerun scoring without decoys (two iterations)\n"
        +
        "             ## 1 = Report Decoys: calculate FLR but don't rescore PSMs, all decoy hits will be reported\n\n");

    bw.write(
        "## This option can be used to help diagnose problems with Luciphor. Multi-threading is disabled in debug mode.\n"
            +
            "DEBUG_MODE = 0 ## 0 = default: turn off debugging\n" +
            "               ## 1 = write peaks selected for modeling to disk\n" +
            "               ## 2 = write the scores of all permutations for each PSM to disk\n" +
            "               ## 3 = write the matched peaks for the top scoring permutation to disk\n"
            +
            "               ## 4 = write HCD non-parametric models to disk (HCD-mode only option)\n\n");

    bw.close();
    System.err.print(
        "\nPlease edit the input file: " + outF.getPath() + " with your favorite text editor\n\n");
    System.exit(0);
  }


  // Record the global and local FLR values estimated for all of the delta scores
  public static void recordFLRestimates() {
    FLRestimateMap = new THashMap<>();

    for (PSM p : Globals.PSM_list) {
      if (p.isDecoy) {
        continue; // skip FLR data from decoys
      }
      double[] d = new double[2];
      d[0] = p.globalFDR;
      d[1] = p.localFDR;
      FLRestimateMap.put(p.deltaScore, d);
    }
  }


  // Function assigns the global and local FLR for the current PSM from the FLRestimateMap
  public static void assignFLR() {

    ArrayList<Double> obsDeltaScores = new ArrayList<>(FLRestimateMap.keySet());
    Collections.sort(obsDeltaScores);  // sort them from low to high
    int N = obsDeltaScores.size();
    boolean assigned;
    for (PSM p : Globals.PSM_list) {
      double obs_ds = p.deltaScore;
      assigned = false;

      // iterate over the delta scores until you find the value closest to this one
      for (int i = 1; i < N; i++) {
        double curDS = obsDeltaScores.get(i);
        if (curDS > obs_ds) { // hit the limit, get the *previous* delta score
          double[] d = FLRestimateMap.get(obsDeltaScores.get((i - 1)));
          p.globalFDR = d[0];
          p.localFDR = d[1];
          assigned = true;
          break;
        }
      }

      if (!assigned) { // very high scoring PSM
        double[] d = FLRestimateMap.get(obsDeltaScores.get((N - 1)));
        p.globalFDR = d[0];
        p.localFDR = d[1];
      }
    }
  }


  // This function prepares each PSM for the second iteration (the one after the FLR has been estimated)
  public static void clearPSMs() {
    for (PSM p : PSM_list) {
      p.clearScores();
    }
  }
}
