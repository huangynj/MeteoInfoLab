/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.util.FontUtil;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.laboratory.gui.FrmMain;
import org.meteoinfo.laboratory.gui.MyPythonInterpreter;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.InteractiveConsole;

/**
 *
 * @author wyq
 */
public class MeteoInfoLab {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here        
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("-r")) {
                String fontPath = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "fonts";
                //fontPath = "D:\\MyProgram\\java\\MeteoInfoDev\\MeteoInfo\\fonts";
                List<String> fontFns = GlobalUtil.getFiles(fontPath, ".ttc");
                for (String fontFn : fontFns) {
                    System.out.println("Register: " + fontFn);
                    FontUtil.registerFont(fontFn);
                }
                args = (String[]) DataConvert.resizeArray(args, args.length - 1);
            }
        }
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("-i")) {
                runInteractive();
            } else if (args[0].equalsIgnoreCase("-b")) {
                if (args.length == 1) {
                    System.out.println("Script file name is needed!");
                    System.exit(0);
                } else {
                    String fn = args[1];
                    if (new File(fn).isFile()) {
                        System.setProperty("java.awt.headless", "true");
                        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                        System.out.println("Headless mode: " + ge.isHeadless());
                        runScript(args, fn, 1);
                    } else {
                        System.out.println("The script file does not exist!");
                        System.exit(0);
                    }
                }
            } else if (args[0].equalsIgnoreCase("-eng")) {
                runApplication(true);
            } else {
                String fn = args[0];
                if (new File(fn).isFile()) {
                    runScript(args, fn, 0);
                } else {
                    System.out.println("The script file does not exist!");
                    System.exit(0);
                }
            }
        } else {
            runApplication();
        }
    }

    private static void runScript_back(String args[], String fn, int idx) {
        String ext = GlobalUtil.getFileExtension(fn);
        System.out.println("Running Jython script...");
        PySystemState state = new PySystemState();
        if (args.length > idx + 1) {
            for (int i = idx + 1; i < args.length; i++) {
                state.argv.append(new PyString(args[i]));
            }
        }
        //state.setdefaultencoding("utf-8");
        //PythonInterpreter interp = new PythonInterpreter(null, state);
        MyPythonInterpreter interp = new MyPythonInterpreter(null, state);

        //String pluginPath = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "plugins";
        //List<String> jarfns = GlobalUtil.getFiles(pluginPath, ".jar");
        String path = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "pylib";
        String toolboxPath = GlobalUtil.getAppPath(FrmMain.class) + "/toolbox";
        interp.exec("import sys");
        interp.exec("import os");
        interp.exec("import datetime");
        interp.exec("sys.path.append('" + path + "')");
        interp.exec("from milab import *");
        interp.exec("sys.path.append('" + toolboxPath + "')");
        interp.exec("from toolbox import *");
        interp.exec("mipylib.plotlib.miplot.batchmode = True");
        interp.exec("mipylib.plotlib.miplot.isinteractive = False");
        System.out.println("mipylib is loaded...");
        try {
//            File file = new File(fn);    
//            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
//            StringBuilder sb = new StringBuilder();
//            String line;
//            while((line = r.readLine()) != null){
//                sb.append(line);
//                sb.append("\n");
//            }
//            String code = sb.toString();
//            System.out.print(code);
//            ByteArrayInputStream bis = new ByteArrayInputStream(code.getBytes("utf-8"));
            interp.execfile(new FileInputStream(new File(fn)), "utf-8");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MeteoInfoLab.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(MeteoInfoLab.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.exit(0);
        }
    }

    private static void runScript(String args[], String fn, int idx) {
        //String ext = GlobalUtil.getFileExtension(fn);
        //registerFonts();
        org.meteoinfo.global.util.FontUtil.registerWeatherFont();

        System.out.println("Running Jython script...");
        PySystemState state = new PySystemState();
        if (args.length > idx + 1) {
            for (int i = idx + 1; i < args.length; i++) {
                state.argv.append(new PyString(args[i]));
            }
        }
        //state.setdefaultencoding("utf-8");
        //PythonInterpreter interp = new PythonInterpreter(null, state);
        MyPythonInterpreter interp = new MyPythonInterpreter(null, state);

        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
        String path, toolboxPath, mapPath;
        if (isDebug) {
            path = "D:/MyProgram/java/MeteoInfoDev/MeteoInfoLab/pylib";
            toolboxPath = "D:/MyProgram/java/MeteoInfoDev/MeteoInfoLab/toolbox";
            mapPath = "D:/MyProgram/Distribution/Java/MeteoInfo/MeteoInfo/map";
        } else {
            //String pluginPath = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "plugins";
            //List<String> jarfns = GlobalUtil.getFiles(pluginPath, ".jar");
            String basePath = GlobalUtil.getAppPath(FrmMain.class);
            path = basePath + File.separator + "pylib";
            toolboxPath = basePath + "/toolbox";
            mapPath = basePath + "/map";
            if (mapPath.substring(0, 1).equals("/"))
                mapPath = mapPath.substring(1);
        }

        try {
            interp.exec("import sys");
            interp.exec("import os");
            interp.exec("import datetime");
            interp.exec("sys.path.append('" + path + "')");
            interp.exec("from milab import *");
            if (!isDebug) {
                interp.exec("sys.path.append('" + toolboxPath + "')");
                interp.exec("from toolbox import *");
            }
            interp.exec("mipylib.plotlib.miplot.batchmode = True");
            interp.exec("mipylib.plotlib.miplot.isinteractive = False");
            interp.exec("mipylib.plotlib.miplot.mappath = '" + mapPath + "'");
            System.out.println("mipylib is loaded...");

            //String encoding = "utf-8";
            //File file = new File(fn);            
            //byte[] bytes = Files.readAllBytes(file.toPath());
            //String code = new String(bytes, encoding);
            //ByteArrayInputStream bis = new ByteArrayInputStream(code.getBytes(encoding));
            //ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            //interp.execfile(bis);
            //interp.exec(code);
            interp.execfile(fn);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private static void runInteractive() {
//        PlotForm plotForm = new PlotForm();
//        plotForm.setSize(800, 600);
//        plotForm.setVisible(true);
//        MeteoInfoScript mis = new MeteoInfoScript(plotForm);
        String basePath = GlobalUtil.getAppPath(FrmMain.class);
        String path = basePath + File.separator + "pylib";
        String toolboxPath = basePath + "/toolbox";
        String mapPath = basePath + "/map";
        if (mapPath.substring(0, 1).equals("/"))
            mapPath = mapPath.substring(1);
        //MeteoInfoScript mis = new MeteoInfoScript(path);
        InteractiveConsole console = new InteractiveConsole();
        try {
            //console.set("mis", mis);
            console.exec("import sys");
            console.exec("import os");
            console.exec("import datetime");
            console.exec("sys.path.append('" + path + "')");
            console.exec("from milab import *");
            console.exec("sys.path.append('" + toolboxPath + "')");
            console.exec("from toolbox import *");
            //console.exec("import mipylib");
            //console.exec("from mipylib.miscript import *");
            console.exec("mipylib.plotlib.miplot.isinteractive = True");
            console.exec("mipylib.plotlib.miplot.mappath = '" + mapPath + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
        console.interact();
    }

    private static void runApplication() {
        runApplication(false);
        //runApplication(true);
    }

    private static void runApplication(final boolean isEng) {
        try {
            /* Set look and feel */
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
            * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
             */
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MeteoInfoLab.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //System.setProperty("-Dsun.java2d.dpiaware", "false");

        /* Create and display the form */
        SwingUtilities.invokeLater(new Runnable() {
        //java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        try {
//                            final SplashScreen splash = SplashScreen.getSplashScreen();
//                            if (splash == null){
//                                System.out.println("SplashScreen.getSplashScreen() returned null");
//                                return;
//                            }
//                            Graphics2D g = splash.createGraphics();
//                            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                            g.setFont(new Font("Arial", Font.BOLD, 60));
//                            g.setColor(Color.red);
//                            g.drawString("MeteoInfo", 100, 200);
//                            splash.update();
//                            Thread.sleep(1000);
//                            //splash.setImageURL(Program.class.getResource("/meteoinfo/resources/logo.png"));
//                            //splash.update();
//                        } catch (Exception e) {
//                        }
//                    }
//                }.start();

                boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                        getInputArguments().toString().contains("jdwp");
//                if (isDebug) {
//                    Locale.setDefault(Locale.ENGLISH);
//                }

                if (isEng) {
                    Locale.setDefault(Locale.ENGLISH);
                }

                StackWindow sw = null;
                if (!isDebug) {
                    sw = new StackWindow("Show Exception Stack", 600, 400);
                    Thread.UncaughtExceptionHandler handler = sw;
                    Thread.setDefaultUncaughtExceptionHandler(handler);
                    System.setOut(sw.printStream);
                    System.setErr(sw.printStream);
                }

                //registerFonts();
                org.meteoinfo.global.util.FontUtil.registerWeatherFont();
                FrmMain frame = new FrmMain();
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                //frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                if (sw != null) {
                    sw.setLocationRelativeTo(frame);
                }
            }
        });
    }
}
