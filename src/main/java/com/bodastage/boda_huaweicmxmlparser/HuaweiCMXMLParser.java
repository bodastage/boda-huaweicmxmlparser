/*
 * Parses Hauwei XML Configuration management files to csv.
 *
 * @see http://github.com/bodastage/boda-huaweicmxmlparser
 */
package com.bodastage.boda_huaweicmxmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @since 1.0.0
 * @author Bodastage<info@bodastage.com>
 */
public class HuaweiCMXMLParser {
    
    /**
     * Current release version 
     * 
     * Since 1.1.0
     */
    final static String VERSION = "1.1.0";
    
    /**
     * The base file name of the file being parsed.
     * 
     * @since 1.0.0
     */
    private String baseFileName = "";
    
    /**The value of the xsi:Type attribute of the NE tag.
     * 
     * @since 1.0.0
     */
    private String xsiType = "";
    
    /**
     * The value of the neType XML attribute of the NE tag.
     * 
     * @since 1.0.0
     */
    private String neType = "";
    
    /**
     * The value of the neversion XML attribute of the NE tag.
     * 
     * @since 1.0.0
     */
    private String neVersion = "";
    
    /**
     * The value of the neid XML attribute of the NE tag.
     * 
     * @since 1.0.0
     */
    private String neid = "";
    
    /**
     * This marks whether an moi start tag has been encountered during 
     * the XML tree traversal.
     * 
     * @since 1.0.0
     */
    private boolean inMoi = false;
    
    /**
     * The  attribute value of the xsi:type attribute for the moi tag.
     * 
     * @since 1.0.0
     */
    private String moiXSIType = "";
    
    /**
     * The  attribute value of the neid attribute for the moi tag.
     * 
     * @since 1.0.0
     */
    private String neId = "";
    

    /**
     * The  attribute value of the xsi:type attribute for the NE tag.
     * 
     * @since 1.0.0
     */
    private String neXSIType = "";    
    
    /**
     * The  attribute value of the xsi:type attribute for the module tag.
     * 
     * @since 1.0.0
     */
    private String moduleXSIType = "";
    
    /**
     * Module tag productversion attribute
     * 
     * @since 1.1.0
     */
    private String moduleProductVersion = "";
    
    /**
     * Module tag remark attribute
     * 
     * @since 1.1.0
     */
    private String moduleRemark = "";
    
    /**
     * The holds the parameters and corresponding values for the moi tag  
     * currently being processed.
     * 
     * @since 1.0.0
     */
    private Map<String,String> moiParameterValueMap 
            = new LinkedHashMap<String, String>();
    
    /**
     * This holds a map of the Managed Object Instances (MOIs) to the respective
     * csv print writers.
     * 
     * @since 1.0.0
     */
    private Map<String, PrintWriter> moiPrintWriters 
            = new LinkedHashMap<String, PrintWriter>();
    

    /**
     * Output directory.
     *
     * @since 1.0.0
     */
    private String outputDirectory = "/tmp";
    
    /**
     * Tag data.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    private String tagData = "";
    
        /**
     * Tracks Managed Object attributes to write to file. This is dictated by 
     * the first instance of the MO found. 
     * @TODO: Handle this better.
     *
     * @since 1.0.0
     */
    private Map<String, Stack> moColumns = new LinkedHashMap<String, Stack>();
    
    /**
     * Parser start time. 
     * 
     * @since 1.0.4
     * @version 1.0.0
     */
    final long startTime = System.currentTimeMillis();
    
    /**
     * The file to be parsed.
     * 
     * @since 1.0.0
     */
    private String dataFile;
    
   /**
     * The file/directory to be parsed.
     * 
     * @since 1.1.0
     */
    private String dataSource;
            
    /**
     * Parser states. Currently there are only 2: extraction and parsing
     * 
     * @since 1.1.0
     */
    static int parserState = ParserStates.EXTRACTING_PARAMETERS;

    /**
     * Extraction date time.
     * 
     * @since 1.1.0
     */
    private String varDateTime;
    
    /**
     * Extract managed objects and their parameters
     */
    private Boolean extractParametersOnly = false;
    
    /**
     * Add meta fields to each MO. FILENAME, DATETIME
     */
    private Boolean extractMetaFields = false;
    
    /**
     * File with a list of managed objects and parameters to extract.
     * 
     */
    private String parameterFile = null;
   
    
    /**
     * Extract parameter list from  parameter file
     * 
     * @param filename 
     */
    public static void getParametersToExtract(String filename) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new FileReader(filename));
        for(String line; (line = br.readLine()) != null; ) {
           String [] moAndParameters =  line.split(":");
           String mo = moAndParameters[0];
           String [] parameters = moAndParameters[1].split(",");
           
           Stack parameterStack = new Stack();
           for(int i =0; i < parameters.length; i++){
               parameterStack.push(parameters[i]);
           }
        }
        
        parserState = ParserStates.EXTRACTING_VALUES;
    }
    
    /**
     * Set parameter file 
     * 
     * @param filename 
     */
    public void setParameterFile(String filename){
        parameterFile = filename;
    }
    
    public void setExtractParametersOnly(Boolean bool){
        extractParametersOnly = bool;
    }
    
    public void setExtractMetaFields(Boolean bool){
        extractMetaFields = bool;
    }

    public static void main( String[] args )
    {
        
       Options options = new Options();
       CommandLine cmd = null;
       String outputDirectory = null;   
       String inputFile = null;
       String parameterConfigFile = null;
       Boolean onlyExtractParameters = false;
       Boolean showHelpMessage = false;
       Boolean showVersion = false;
       Boolean attachMetaFields = false; //Attach mattachMetaFields FILENAME,DATETIME,NE_TECHNOLOGY,NE_VENDOR,NE_VERSION,NE_TYPE
       
       try{ 
            options.addOption( "p", "extract-parameters", false, "extract only the managed objects and parameters" );
            options.addOption( "v", "version", false, "display version" );
//            options.addOption( "m", "meta-fields", false, "add meta fields to extracted parameters. FILENAME,DATETIME" );
            options.addOption( Option.builder("i")
                    .longOpt( "input-file" )
                    .desc( "input file or directory name")
                    .hasArg()
                    .argName( "INPUT_FILE" ).build());
            options.addOption(Option.builder("o")
                    .longOpt( "output-directory" )
                    .desc( "output directory name")
                    .hasArg()
                    .argName( "OUTPUT_DIRECTORY" ).build());
            options.addOption(Option.builder("c")
                    .longOpt( "parameter-config" )
                    .desc( "parameter configuration file")
                    .hasArg()
                    .argName( "PARAMETER_CONFIG" ).build() );
            options.addOption( "h", "help", false, "show help" );
            
            //Parse command line arguments
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse( options, args);

            if( cmd.hasOption("h")){
                showHelpMessage = true;
            }

            if( cmd.hasOption("v")){
                showVersion = true;
            }
            
            if(cmd.hasOption('o')){
                outputDirectory = cmd.getOptionValue("o"); 
            }
            
            if(cmd.hasOption('i')){
                inputFile = cmd.getOptionValue("i"); 
            }
            
            if(cmd.hasOption('c')){
                parameterConfigFile = cmd.getOptionValue("c"); 
            }
            
            if(cmd.hasOption('p')){
                onlyExtractParameters  = true;
            }
            
            if(cmd.hasOption('m')){
                attachMetaFields  = true;
            }
            
       }catch(IllegalArgumentException e){
           
       } catch (ParseException ex) {
//            java.util.logging.Logger.getLogger(HuaweiCMObjectParser.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       
        try{
            
            if(showVersion == true ){
                System.out.println(VERSION);
                System.out.println("Copyright (c) 2019 Bodastage Solutions(http://www.bodastage.com)");
                System.exit(0);
            }
            
            

            //show help
            if( showHelpMessage == true || 
                inputFile == null || 
                ( outputDirectory == null && onlyExtractParameters == false) ){
                     HelpFormatter formatter = new HelpFormatter();
                     String header = "Parses Huawei NBI XML files to csv\n\n";
                     String footer = "\n";
                     footer += "Examples: \n";
                     footer += "java -jar boda-huaweicmxmlparser.jar -i nbi_dump.xml -o out_folder\n";
                     footer += "java -jar boda-huaweicmxmlparser.jar -i input_folder -o out_folder\n";
                     footer += "\nCopyright (c) 2019 Bodastage Solutions(http://www.bodastage.com)";
                     formatter.printHelp( "java -jar boda-huaweicmxmlparser.jar", header, options, footer );
                     System.exit(0);
            }
        
            //Confirm that the output directory is a directory and has write 
            //privileges
            if(outputDirectory != null ){
                File fOutputDir = new File(outputDirectory);
                if (!fOutputDir.isDirectory()) {
                    System.err.println("ERROR: The specified output directory is not a directory!.");
                    System.exit(1);
                }

                if (!fOutputDir.canWrite()) {
                    System.err.println("ERROR: Cannot write to output directory!");
                    System.exit(1);
                }
            }
            
            //Confirm that the input directory or file can be read
            File fInputFile = new File(inputFile);
            if(!fInputFile.isFile() && !fInputFile.isDirectory()){
                    System.err.println("ERROR: The specified input file/directory can not be accessed!.");
                    System.exit(1);
            }
            
            //Get parser instance
            HuaweiCMXMLParser cmParser = new HuaweiCMXMLParser();

            if(onlyExtractParameters == true ){
                cmParser.setExtractParametersOnly(true);
            }
            
            if( attachMetaFields == true ){
                cmParser.setExtractMetaFields(true);
            }
            
            if(  parameterConfigFile != null ){
                File f = new File(parameterConfigFile);
                if(f.isFile()){
                    cmParser.setParameterFile(parameterConfigFile);
                    cmParser.getParametersToExtract(parameterConfigFile);
                    cmParser.parserState = ParserStates.EXTRACTING_VALUES;
                }
            }
            
            
            cmParser.setDataSource(inputFile);
            if(outputDirectory != null ) cmParser.setOutputDirectory(outputDirectory);

            cmParser.setOutputDirectory(outputDirectory);
            cmParser.parse();
            cmParser.printExecutionTime();
            
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }


    }

    
    HuaweiCMXMLParser(){}
    
    /**
     * Parser entry point 
     * 
     * @since 1.0.0
     * @version 1.1.0
     * 
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    public void parse() throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException {
        //Extract parameters
        if (parserState == ParserStates.EXTRACTING_PARAMETERS) {
            processFileOrDirectory();

            parserState = ParserStates.EXTRACTING_VALUES;
        }

        //Extracting values
        if (parserState == ParserStates.EXTRACTING_VALUES) {
            processFileOrDirectory();
            parserState = ParserStates.EXTRACTING_DONE;
        }
        
        closeMOPWMap();
    }
    
    /**
     * The parser's entry point.
     * 
     * @param filename 
     */
    public void parseFile(String filename) 
    throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException
    {
            XMLInputFactory factory = XMLInputFactory.newInstance();

            XMLEventReader eventReader = factory.createXMLEventReader(
                    new FileReader(filename));
            baseFileName = getFileBasename(filename);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        startElementEvent(event);
                        break;
                    case XMLStreamConstants.SPACE:
                    case XMLStreamConstants.CHARACTERS:
                        characterEvent(event);
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        endELementEvent(event);
                        break;
                }
            }

            
    }
    
    /**
     * Determines if the source data file is a regular file or a directory and 
     * parses it accordingly
     * 
     * @since 1.1.0
     * @version 1.0.0
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public void processFileOrDirectory()
            throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException {
        //this.dataFILe;
        Path file = Paths.get(this.dataSource);
        boolean isRegularExecutableFile = Files.isRegularFile(file)
                & Files.isReadable(file);

        boolean isReadableDirectory = Files.isDirectory(file)
                & Files.isReadable(file);

        if (isRegularExecutableFile) {
            this.setFileName(this.dataSource);
            baseFileName =  getFileBasename(this.dataFile);
            if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                System.out.print("Extracting parameters from " + this.baseFileName + "...");
            }else{
                System.out.print("Parsing " + this.baseFileName + "...");
            }
            this.parseFile(this.dataSource);
            
            if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                 System.out.println("Done.");
            }else{
                System.out.println("Done.");
                //System.out.println(this.baseFileName + " successfully parsed.\n");
            }
        }

        if (isReadableDirectory) {

            File directory = new File(this.dataSource);

            //get all the files from a directory
            File[] fList = directory.listFiles();

            for (File f : fList) {
                this.setFileName(f.getAbsolutePath());
                try {
                    
                    //@TODO: Duplicate call in parseFile. Remove!
                    baseFileName =  getFileBasename(this.dataFile);
                    if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                        System.out.print("Extracting parameters from " + this.baseFileName + "...");
                    }else{
                        System.out.print("Parsing " + this.baseFileName + "...");
                    }
                    
                    //Parse
                    this.parseFile(f.getAbsolutePath());
                    if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                         System.out.println("Done.");
                    }else{
                        System.out.println("Done.");
                        //System.out.println(this.baseFileName + " successfully parsed.\n");
                    }
                   
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Skipping file: " + this.baseFileName + "\n");
                }
            }
        }

    }
    
    /**
     * Handle start element event.
     *
     * @param xmlEvent
     *
     * @since 1.0.0
     * @version 1.0.0
     *
     */
    public void startElementEvent(XMLEvent xmlEvent) throws FileNotFoundException {
        StartElement startElement = xmlEvent.asStartElement();
        String qName = startElement.getName().getLocalPart();
        String prefix = startElement.getName().getPrefix();
        
        Iterator<Attribute> attributes = startElement.getAttributes();
        

        //Handle start of <footer ...>
        if(qName.equals("filefooter") && parserState == ParserStates.EXTRACTING_PARAMETERS){
            String datetime = "";
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().getLocalPart().equals("datetime")) {
                    datetime = attribute.getValue();
                    varDateTime = datetime;
                }
            }            
            
            String f = outputDirectory + File.separatorChar + "filefooter.csv";
            PrintWriter pw = new PrintWriter(f);
            pw.println("FileName,datetime");
            pw.println(baseFileName+","+datetime);
            pw.close();
            return;
        }
        
        //Handle start of <moi ...>
        if(qName.equals("moi")){
            inMoi = true;

            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().getLocalPart().equals("type")) {
                    this.moiXSIType = attribute.getValue();
                }
            }
            return;
        }        
       
        //Skip <attributes>
        if(qName.equals("attributes")){
            return;
        }

        
        //Handle start of <module ...>
        if(qName.equals("module")){
            
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().getLocalPart().equals("type")) {
                    this.moduleXSIType = attribute.getValue();
                }
                
                if (attribute.getName().getLocalPart().equals("productversion")) {
                    this.moduleProductVersion = attribute.getValue();
                }
                
                if (attribute.getName().getLocalPart().equals("remark")) {
                    this.moduleRemark = attribute.getValue();
                }
            }
            
            return;
        }
        
        //Handle start of 
        //<NE xsi:type="SRAN" netype="NodeB" neversion="XXX" neid="XXX">
        if(qName.equals("NE")){
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().getLocalPart().equals("type")) {
                    this.neXSIType = attribute.getValue();
                }
                
                if (attribute.getName().getLocalPart().equals("netype")) {
                    this.neType = attribute.getValue();
                }
                
                if (attribute.getName().getLocalPart().equals("neversion")) {
                    this.neVersion = attribute.getValue();
                }

                if (attribute.getName().getLocalPart().equals("neid")) {
                    this.neId = attribute.getValue();
                }
                
            }
            
            return;            
        }
       
    }
    

    public void endELementEvent(XMLEvent xmlEvent)
            throws FileNotFoundException, UnsupportedEncodingException {
        EndElement endElement = xmlEvent.asEndElement();
        String prefix = endElement.getName().getPrefix();
        String qName = endElement.getName().getLocalPart();
        
        String paramNames = "FileName,varDateTime,ne_xsitype,netype,neversion,neid,"
                + "module_type,module_remark, module_productversion";
        String paramValues = baseFileName + "," + varDateTime +","+neXSIType+","+neType+","+neVersion
                +","+neId + "," + moduleXSIType + "," + moduleRemark + "," + moduleProductVersion;
        
        //Handle </NE>
        if(qName.equals("NE")){
            if(!moiPrintWriters.containsKey("SUBSESSION_NE")){
                String moiFile = outputDirectory + File.separatorChar + "SUBSESSION_NE.csv";
                 moiPrintWriters.put("SUBSESSION_NE", new PrintWriter(moiFile));
                 moiPrintWriters.get("SUBSESSION_NE").println(paramNames);
            }
            
            PrintWriter pw = moiPrintWriters.get("SUBSESSION_NE");
            pw.println(paramValues);
            return;
        }
        
        //Handle </module>
        if(qName.equals("filefooter")){
            return;
        }
        

        
        //Handle </moi>
        if(qName.equals("moi")){
                        
            
            //Extract parameters
            if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                Stack columns = new Stack();
                if( ! moColumns.containsKey(moiXSIType) ){
                    moColumns.put(moiXSIType, columns);
                }
                columns = moColumns.get(moiXSIType);
                Iterator<Map.Entry<String, String>> iter 
                            = moiParameterValueMap.entrySet().iterator();

                while (iter.hasNext()) {
                    Map.Entry<String, String> me = iter.next();
                    if( ! columns.contains(me.getKey())){
                        columns.push(me.getKey());
                    }       
                }
            }
        
            if(parserState == ParserStates.EXTRACTING_VALUES){
                //check if print writer doesn't exists and create it
                if(!moiPrintWriters.containsKey(moiXSIType)){
                    String moiFile = outputDirectory + File.separatorChar + moiXSIType +  ".csv";
                     moiPrintWriters.put(moiXSIType, new PrintWriter(moiFile));

                    String pName = paramNames;
                    Stack columns = moColumns.get(moiXSIType);
                    for(int i =0; i < columns.size(); i++){
                        pName += "," + columns.get(i);
                    }
                    
                    moiPrintWriters.get(moiXSIType).println(pName);

                }

                Stack moiAttributes = moColumns.get(moiXSIType);
                for(int i = 0; i< moiAttributes.size(); i++){
                    String moiName = moiAttributes.get(i).toString();

                    if( moiParameterValueMap.containsKey(moiName) ){
                        paramValues += "," + moiParameterValueMap.get(moiName);
                    }else{
                        paramValues += ",";
                    }   
                }

                PrintWriter pw = moiPrintWriters.get(moiXSIType);
                pw.println(paramValues);

                
            }

            moiParameterValueMap.clear();
            inMoi = false;
            return;

        }
        
        //Handle </attributes>
        if(qName.equals("attributes")){
            return;
        }
        
        //Handle </param>
        if(inMoi == true){
            moiParameterValueMap.put(qName, toCSVFormat(tagData));
        }
        
    }
    
    /**
     * Handle character events.
     *
     * @param xmlEvent
     * @version 1.0.0
     * @since 1.0.0
     */
    public void characterEvent(XMLEvent xmlEvent) {
        Characters characters = xmlEvent.asCharacters();
        if(!characters.isWhiteSpace()){
            tagData = characters.getData(); 
        }
    }    
    
    /**
     * Get file base name.
     * 
     * @since 1.0.0
     */
     public String getFileBasename(String filename){
        try{
            return new File(filename).getName();
        }catch(Exception e ){
            return filename;
        }
    }
     
    /**
     * Print program's execution time.
     * 
     * @since 1.0.0
     */
    public void printExecutionTime(){
        float runningTime = System.currentTimeMillis() - startTime;
        
        String s = "Parsing completed. ";
        s = s + "Total time:";
        
        //Get hours
        if( runningTime > 1000*60*60 ){
            int hrs = (int) Math.floor(runningTime/(1000*60*60));
            s = s + hrs + " hours ";
            runningTime = runningTime - (hrs*1000*60*60);
        }
        
        //Get minutes
        if(runningTime > 1000*60){
            int mins = (int) Math.floor(runningTime/(1000*60));
            s = s + mins + " minutes ";
            runningTime = runningTime - (mins*1000*60);
        }
        
        //Get seconds
        if(runningTime > 1000){
            int secs = (int) Math.floor(runningTime/(1000));
            s = s + secs + " seconds ";
            runningTime = runningTime - (secs/1000);
        }
        
        //Get milliseconds
        if(runningTime > 0 ){
            int msecs = (int) Math.floor(runningTime/(1000));
            s = s + msecs + " milliseconds ";
            runningTime = runningTime - (msecs/1000);
        }

        
        System.out.println(s);
    }
    
    /**
     * Close file print writers.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    public void closeMOPWMap() {
        Iterator<Map.Entry<String, PrintWriter>> iter
                = moiPrintWriters.entrySet().iterator();
        while (iter.hasNext()) {
            iter.next().getValue().close();
        }
        moiPrintWriters.clear();
    }
    
    /**
     * Process given string into a format acceptable for CSV format.
     *
     * @since 1.0.0
     * @param s String
     * @return String Formated version of input string
     */
    public String toCSVFormat(String s) {
        String csvValue = s;

        //Check if value contains comma
        if (s.contains(",")) {
            csvValue = "\"" + s + "\"";
        }

        if (s.contains("\"")) {
            csvValue = "\"" + s.replace("\"", "\"\"") + "\"";
        }

        return csvValue;
    }
    
    /**
     * Set the output directory.
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @param directoryName 
     */
    public void setOutputDirectory(String directoryName ){
        this.outputDirectory = directoryName;
    }
     
    /**
     * Set name of file to parser.
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @param directoryName 
     */
    public void setFileName(String filename ){
        this.dataFile = filename;
    }
    
    /**
     * Set name of file to parser.
     * 
     * @since 1.0.1
     * @version 1.0.0
     * @param dataSource 
     */
    public void setDataSource(String dataSource ){
        this.dataSource = dataSource;
    }
}
