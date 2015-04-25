package kkdev.kksystem.kkcontroller.main;

import kkdev.kksystem.base.classes.PluginInfo;
import kkdev.kksystem.base.constants.PluginConsts;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author sayma_000
 *
 * by default we found 3 1 - ODB connector 2 - TextLogger 3 - LED Display
 */
public final class kk_defaultconfig {

    public static KKSystemConfig GetDefaultSystemConfig() {
        KKSystemConfig Ret = new KKSystemConfig();
        Ret.ConfPlugins = new PluginInfo[3];
        Ret.ConfPlugins[0] = GetODBPlugin();
        Ret.ConfPlugins[1] = GetLEDDisplayPlugin();
        Ret.ConfPlugins[2] = GetDataDisplayPluginInfo();
        return Ret;
    }

    private static PluginInfo GetODBPlugin() {
        PluginInfo Ret = new PluginInfo();
        Ret.PluginUUID = PluginConsts.KK_PLUGIN_BASE_PLUGIN_ODB2_UUID;
        Ret.PluginName = PluginConsts.KK_PLUGIN_BASE_PLUGIN_ODB2;
        Ret.PluginDescription = "Basic ELM327 ODB2 Reader plugin";
        Ret.PluginType = PluginConsts.KK_PLUGIN_TYPE.PLUGIN_INPUT;
        Ret.ConnectorClass = "kkdev.kksystem.plugins.odb2.elm372.KKPlugin";
        Ret.PluginVersion = 1;
        Ret.Enabled = true;
        return Ret;

    }

    
    private static PluginInfo GetLEDDisplayPlugin() {
        PluginInfo Ret = new PluginInfo();
        Ret.PluginUUID = PluginConsts.KK_PLUGIN_BASE_PLUGIN_LEDDISPLAY_UUID;
        Ret.PluginName = PluginConsts.KK_PLUGIN_BASE_PLUGIN_LEDDISPLAY;
        Ret.PluginDescription = "Basic LED Display plugin";
        Ret.PluginType = PluginConsts.KK_PLUGIN_TYPE.PLUGIN_OUTPUT;
        Ret.ConnectorClass = "kkdev.kksystem.plugins.leddisplay.KKPlugin";
        Ret.PluginVersion = 1;
        Ret.Enabled = true;

        return Ret;

    }

    private static PluginInfo GetDataDisplayPluginInfo() {
        PluginInfo Ret = new PluginInfo();
        Ret.PluginUUID = PluginConsts.KK_PLUGIN_BASE_PLUGIN_DATADISPLAY_UUID;
        Ret.PluginName = PluginConsts.KK_PLUGIN_BASE_PLUGIN_DATADISPLAY;
        Ret.PluginDescription = "Data Display Processor";
        Ret.PluginType = PluginConsts.KK_PLUGIN_TYPE.PLUGIN_PROCESSOR;
        Ret.ConnectorClass = "kkdev.kksystem.plugins.datadisplay.KKPlugin";
        Ret.PluginVersion = 1;
        Ret.Enabled = true;

        return Ret;

    }
}
