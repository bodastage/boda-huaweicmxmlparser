package com.bodastage.boda_huaweicmxmlparser;

import java.io.File;

/**
 * Parses Huawei Bulk Configuration XML Data to csv.
 * 
 * @author Bodastage<info@bodastage.com>
 * @version 1.0.0
 * @see http://github.com/bodastage/boda-huaweicmxmlparser
 */
public class App
{
    public static void main( String[] args )
    {
        try{
            //show help
            if(args.length != 2 || (args.length == 1 && args[0] == "-h")){
                showHelp();
                System.exit(1);
            }
            //Get bulk CM XML file to parse.
            String filename = args[0];
            String outputDirectory = args[1];
            
            //Confirm that the output directory is a directory and has write 
            //privileges
            File fOutputDir = new File(outputDirectory);
            if(!fOutputDir.isDirectory()) {
                System.err.println("ERROR: The specified output directory is not a directory!.");
                System.exit(1);
            }
            
            if(!fOutputDir.canWrite()){
                System.err.println("ERROR: Cannot write to output directory!");
                System.exit(1);            
            }

            HuaweiCMXMLParser cmXMLparser = new HuaweiCMXMLParser();
            cmXMLparser.setDataSource(filename);
            cmXMLparser.setOutputDirectory(outputDirectory);
            cmXMLparser.parse();
            cmXMLparser.printExecutionTime();
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Show parser help.
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    static public void showHelp(){
        System.out.println("boda-huaweinbixmlparser 1.0.0. Copyright (c) 2016 Bodastage(http://www.bodastage.com)");
        System.out.println("Parses HuaweI Bulk Configuration Data XML to csv.");
        System.out.println("Usage: java -jar boda-huaweinbixmlparser.jar <fileToParse.xml|Directory> <outputDirectory>");
    }
}
