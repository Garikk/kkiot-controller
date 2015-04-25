/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kkdev.kksystem.kkcontroller.pluginmanager;

import java.io.BufferedReader;
import kkdev.kksystem.kkcontroller.main.KKSystemConfig;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import kkdev.kksystem.base.classes.PluginConnection;
import kkdev.kksystem.base.classes.PluginConnectionsConfig;
import kkdev.kksystem.base.classes.PluginInfo;
import kkdev.kksystem.base.constants.SystemConsts;
import static kkdev.kksystem.base.constants.SystemConsts.KK_BASE_PLUGINS_MANIFEST_CONNECTOR_ATTR;
import kkdev.kksystem.base.interfaces.IPluginKKConnector;

/**
 *
 * @author blinov_is
 */
public abstract class PluginManager {

    static KKSystemConfig MainConfiguraion;
    static HashMap<String,IPluginKKConnector> ActivePlugins;
    static ArrayList<PluginConnection> ActiveConnections;
    static PluginExecute PlEx;
    
    public static void InitPlugins(ArrayList<PluginInfo> Plugins, ArrayList<PluginConnectionsConfig> ConnectionsConfiguration) {
        ArrayList<PluginInfo>  ToLoad;
        //Prepare config
        ToLoad = PrepareConnections(ConnectionsConfiguration, Plugins);
        //Load plugins
        ActivePlugins = ConnectPlugins(ToLoad);
        //
        if (ActivePlugins == null) {
            return;
        }
        //
        PlEx=new PluginExecute(ActivePlugins,ActiveConnections);
        PlEx.InitPlugins();

    }

    public static void StartPlugins()
    {
                PlEx.StartPlugins();
    }
    
    private static ArrayList<PluginInfo> PrepareConnections(ArrayList<PluginConnectionsConfig> ConfConfig, ArrayList<PluginInfo> Plugins) {
        //Create Needed plugins list
        ArrayList<PluginConnection> ConnectionsLoad;
        ArrayList<String> PluginUsing;
        ConnectionsLoad = new ArrayList<>();
        PluginUsing = new ArrayList<>();
        //Organize connections to list
        for (PluginConnectionsConfig PCC : ConfConfig) {
            ConnectionsLoad.addAll(Arrays.asList(PCC.Connections));
        }
        //Create active plugins list
        for (PluginConnection PC : ConnectionsLoad) {
            if (!PluginUsing.contains(PC.SourcePluginUID)) {
                PluginUsing.add(PC.SourcePluginUID);
            }
            if (!PluginUsing.contains(PC.TargetPluginUID)) {
                PluginUsing.add(PC.TargetPluginUID);
            }
        }
        //set not active plugins to disabled
        for (PluginInfo PI : Plugins) {
            if (PI.Enabled) {
                PI.Enabled = PluginUsing.contains(PI.PluginUUID);
            }
        }
        //
        ActiveConnections=ConnectionsLoad;
        //
        return Plugins;
    }

    private static boolean CheckPlugin(ArrayList<PluginInfo> Plugins, PluginInfo CheckPlugin) {
        for (PluginInfo Pl : Plugins) {
            if (Pl.PluginUUID.equals(CheckPlugin.PluginUUID)) {
                return Pl.Enabled;
            }
        }
        return false;
    }

    private static String GetPluginConnectorClass(File FileToCheck) {
        String Ret = null;

        JarFile jarFile = null;
        JarEntry entry = null;
        InputStream IS = null;
        try {
            jarFile = new JarFile(FileToCheck);
            //
            Manifest MF=jarFile.getManifest();
            Ret=MF.getMainAttributes().getValue(KK_BASE_PLUGINS_MANIFEST_CONNECTOR_ATTR);
            //
            if (Ret==null) {
                System.out.println("Plugin read error (kkconnector file not found)");
                return null;
            }
            //
            jarFile.close();
        } catch (IOException ex) {
            System.out.println("Plugin info read error: " + ex.getMessage());
            try {
                jarFile.close();
            } catch (Exception Ex) {
            }
            return null;
        }
        //
        return Ret;
    }

    private static HashMap<String, IPluginKKConnector> ConnectPlugins(ArrayList<PluginInfo> Plugins) {
        System.out.println("Required plugins count: " + Plugins.size());
        //
        int Counter = 0;
        HashMap<String, IPluginKKConnector> Ret = new HashMap<>();
        //
        //
        File folder = new File(SystemConsts.KK_BASE_PLUGINPATH);
        File[] PluginFiles = folder.listFiles();
        //
        if (PluginFiles == null) {
            System.out.println("No plugins found...exitting");
            System.exit(0);
        }
        //
        System.out.println("Plugin files count: " + PluginFiles.length);
        //

        for (File loadFile : PluginFiles) {
            boolean Err = false;
            //Check load only Jar file
            if (!loadFile.getName().endsWith(".jar") | loadFile.isDirectory()) {
                continue;
            }
            System.out.println("--------------------");
            System.out.println("File: " + loadFile.getName());

            //
            try {
                //
                String ConnectorClass;
                IPluginKKConnector PluginConnection;
                ConnectorClass = GetPluginConnectorClass(loadFile);
                //
                URLClassLoader CLoader = new URLClassLoader(new URL[]{loadFile.toURI().toURL()});
                //
                PluginConnection = (IPluginKKConnector) CLoader.loadClass(ConnectorClass).newInstance();
                //
                if (CheckPlugin(Plugins, PluginConnection.GetPluginInfo()) == false) {
                    System.out.println("Config: not in config, disabled or incorrect version");
                    System.out.println("Skip");
                    continue;
                }
                Ret.put(PluginConnection.GetPluginInfo().PluginUUID,PluginConnection);
                //
                System.out.println("Load: ok");
                //
                Counter++;
            } catch (MalformedURLException | InstantiationException | ClassNotFoundException | IllegalAccessException e) {
                System.out.println("Load Error: " + loadFile.getName() + " " + e.toString());
            }
            //

        }
        //
        for (PluginInfo Pl : Plugins) {
            if (!Pl.Enabled) {
                System.out.println("Disabled plugin: " + Pl.PluginName);
            }
        }
        //
        return Ret;
    }

}
