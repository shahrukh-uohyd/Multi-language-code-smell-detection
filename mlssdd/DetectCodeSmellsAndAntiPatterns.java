/* (c) Copyright 2019 and following years, MounaA and PalmyreB.
 *
 * Use and copying of this software and preparation of derivative works
 * based upon this software are permitted. Any copy of this software or
 * of any derivative work must include the above copyright notice of
 * the author, this paragraph and the one after it.
 *
 * This software is made available AS IS, and THE AUTHOR DISCLAIMS
 * ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE, AND NOT WITHSTANDING ANY OTHER PROVISION CONTAINED HEREIN,
 * ANY LIABILITY FOR DAMAGES RESULTING FROM THE SOFTWARE OR ITS USE IS
 * EXPRESSLY DISCLAIMED, WHETHER ARISING IN CONTRACT, TORT (INCLUDING
 * NEGLIGENCE) OR STRICT LIABILITY, EVEN IF THE AUTHOR IS ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * All Rights Reserved.
 */
package mlssdd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.Document;

import mlssdd.codesmells.detection.repository.UnusedImplementationDetectionModified;

import mlssdd.antipatterns.detection.IAntiPatternDetection;
import mlssdd.antipatterns.detection.repository.ExcessiveInterLanguageCommunicationDetection;
import mlssdd.antipatterns.detection.repository.EILCModifiedForRocksdb;
import mlssdd.antipatterns.detection.repository.TooMuchClusteringDetectionModified;
import mlssdd.antipatterns.detection.repository.TooMuchScatteringDetectionModified;
import mlssdd.codesmells.detection.ICodeSmellDetection;
import mlssdd.codesmells.detection.repository.AssumingSafeMultiLanguageReturnValuesDetectionModified;
import mlssdd.codesmells.detection.repository.HardCodingLibrariesDetection;
import mlssdd.codesmells.detection.repository.LocalReferencesAbuseDetection;
import mlssdd.codesmells.detection.repository.MemoryManagementMismatchDetection;
import mlssdd.codesmells.detection.repository.NotCachingObjectsElementsDetection;
import mlssdd.codesmells.detection.repository.NotHandlingExceptionsDetection;
import mlssdd.codesmells.detection.repository.NotSecuringLibrariesDetection;
import mlssdd.codesmells.detection.repository.NotUsingRelativePathDetection;
import mlssdd.codesmells.detection.repository.PassingExcessiveObjectsDetection;
import mlssdd.codesmells.detection.repository.UnusedDeclarationDetectionModified;
import mlssdd.codesmells.detection.repository.UnusedParametersDetectionModified;

import mlssdd.codesmells.detection.repository.NotUsingSafePoints;
//import mlssdd.github.git.CloneRepository;
import mlssdd.utils.CreateXml;

public class DetectCodeSmellsAndAntiPatterns {

    /**
     * Outputs a CSV listing the code smells and anti-patterns detected in the
     * input project.
     *
     * @param args	Path to the input project (can be a file or a directory)
     */
    

    public static void main(String[] argumnets) {
         File folder = new File("/home/shahrukh/smellDetection/Detection/cloned projects");
     File[] files1 = folder.listFiles();
    // // // //System.out.println(files1.length);
    for(int f=10; f<files1.length;f++)
    {
      String Fname =  files1[f].getName();
     //String Fname = "rocksdb-8.3.2";
      System.out.println(Fname);
    Document xml = CreateXml.parseSingleDocument("/home/shahrukh/smellDetection/Detection/cloned projects/"+Fname);
                
                final long start = System.currentTimeMillis();
                final Set<ICodeSmellDetection> codeSmellDetectors = new HashSet<>();
                final Set<IAntiPatternDetection> antiPatternDetectors = new HashSet<>();

               // codeSmellDetectors
               //         .add(new AssumingSafeMultiLanguageReturnValuesDetectionModified());
               //   codeSmellDetectors.add(new HardCodingLibrariesDetection());
               //   codeSmellDetectors.add(new LocalReferencesAbuseDetection());
               //  codeSmellDetectors.add(new MemoryManagementMismatchDetection());
               //  codeSmellDetectors.add(new NotHandlingExceptionsDetection());
               //  codeSmellDetectors.add(new NotSecuringLibrariesDetection());
               //  codeSmellDetectors.add(new NotUsingRelativePathDetection());
               //  codeSmellDetectors.add(new PassingExcessiveObjectsDetection());
               //  codeSmellDetectors.add(new UnusedParametersDetectionModified());

               //  // Detectors that need to analyse both languages
               //  // Uncomment when giving both Java and native code as an argument
               //  codeSmellDetectors.add(new NotCachingObjectsElementsDetection());
               //  codeSmellDetectors.add(new UnusedDeclarationDetectionModified());
               //  //codeSmellDetectors.add(new UnusedImplementationDetectionModified());

                codeSmellDetectors.add(new NotUsingSafePoints());

               //  //antiPatternDetectors
               //    //      .add(new ExcessiveInterLanguageCommunicationDetection());
               // antiPatternDetectors.add(new EILCModifiedForRocksdb());
               //  antiPatternDetectors.add(new TooMuchClusteringDetectionModified());
               //  // antiPatternDetectors.add(new TooMuchScatteringDetectionModified());

                //System.err.println(a+" : "+project);
                //final Document xml = CodeToXml.parse(project);
                System.out
                        .println(
                                "The creation of the XML took "
                                + (System.currentTimeMillis() - start) + " ms.\n");

                try {
                    int id = 0;
                    String bareName = Fname;
                    // if (bareName.equals("")) {
                    //     final String[] parts = project.split("[\\/\\\\]");
                    //     bareName = parts[parts.length - 1];
                    // }
                    final String dir = "results/NotUsingSafePoints_new";
                    final String fullPath = dir + "/" + bareName + ".csv";

                    if (new File(dir).mkdirs()) {
                        System.out.println("Directory " + dir + " created");
                    }

                    // System.out.println(bareName);
                    // System.out.println(project);
                    // System.out.println();

                    // FileWriter(..., false): no auto-append, write at the beginning of the file
                    // PrintWriter(..., false): no autoflush for performance reason
                    final PrintWriter outputWriter = new PrintWriter(
                            new BufferedWriter(new FileWriter(fullPath, false)),
                            false);
                    outputWriter.println("ID,Name,Variable,Method,Class,Package,File,File Name");

                    for (final ICodeSmellDetection detector : codeSmellDetectors) {
                        detector.detect(xml);
                        detector.output(outputWriter, id);
                        final int nbCodeSmells = detector.getCodeSmells().size();
                        id += nbCodeSmells;
                        System.out
                                .println(detector.getCodeSmellName() + ": " + nbCodeSmells);
                    }

                    for (final IAntiPatternDetection detector : antiPatternDetectors) {
                        detector.detect(xml);
                        detector.output(outputWriter, id);
                        final int nbAntiPatterns = detector.getAntiPatterns().size();
                        id += nbAntiPatterns;
                        System.out
                                .println(
                                        detector.getAntiPatternName() + ": " + nbAntiPatterns);
                    }
                    outputWriter.flush();
                    outputWriter.close();
                    System.out
                            .println(
                                    "\nThe detection took "
                                    + (System.currentTimeMillis() - start) + " ms.");
                } catch (final IOException e) {
                    System.out.println("Cannot create output file");
                    e.printStackTrace();
                }
            }
        }
    }
//}
