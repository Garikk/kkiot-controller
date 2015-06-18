/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kkdev.kksystem.kkcontroller.main.systemmenu;

import kkdev.kksystem.base.classes.controls.PinControlData;
import static kkdev.kksystem.base.classes.controls.PinControlData.KK_CONTROL_DATA.CONTROL_LONGPRESS;
import kkdev.kksystem.base.classes.display.menumaker.MenuMaker;
import kkdev.kksystem.base.classes.display.menumaker.MenuMaker.IMenuMakerItemSelected;
import kkdev.kksystem.base.classes.plugins.FeatureConfiguration;
import kkdev.kksystem.base.classes.plugins.PluginMessage;
import static kkdev.kksystem.base.constants.PluginConsts.KK_PLUGIN_BASE_CONTROL_DATA;
import static kkdev.kksystem.base.constants.SystemConsts.KK_BASE_FEATURES_SYSTEM_MULTIFEATURE_UID;
import static kkdev.kksystem.base.constants.SystemConsts.KK_BASE_FEATURES_SYSTEM_UID;
import kkdev.kksystem.base.interfaces.IPluginBaseInterface;
import kkdev.kksystem.kkcontroller.main.SettingsManager;
import kkdev.kksystem.kkcontroller.pluginmanager.PluginLoader;

/**
 *
 * @author blinov_is
 */
public abstract class SystemMenu {

    private static MenuMaker SysMenu;
    private static final String MNU_CMD_CHANGE_FEATURE = "CHFTR";
    private static final String MNU_CMD_REBOOT = "REBOOT";
    private static final String MNU_CMD_POWEROFF = "POWEROFF";

    public static void InitSystemMenu(IPluginBaseInterface BaseConnector) {
        IMenuMakerItemSelected MenuCallBack = (String ItemCMD) -> {
            ExecMenuFunction(ItemCMD);
        };
        SysMenu = new MenuMaker(KK_BASE_FEATURES_SYSTEM_UID, BaseConnector, MenuCallBack, SettingsManager.MainConfiguration.SystemDisplay_UID);
        //
        String[][] ForMenuItems = new String[SettingsManager.MainConfiguration.Features.length][2];
        int f = 0;
        for (FeatureConfiguration FT : SettingsManager.MainConfiguration.Features) {
            ForMenuItems[f][0] = FT.FeatureName;
            ForMenuItems[f][1] = MNU_CMD_CHANGE_FEATURE + " " + FT.FeatureUUID;
            f++;
        }
        SysMenu.AddMenuItems(ForMenuItems);
        //
    }

    public static void ShowMenu() {
        SysMenu.ShowMenu();

    }

    private static void ExecMenuFunction(String Exec) {
        String[] CMD = Exec.split(" ");

        switch (CMD[0]) {
            case MNU_CMD_CHANGE_FEATURE:
                PluginLoader.PlEx.ChangeFeature(CMD[1]);
                break;
            case MNU_CMD_POWEROFF:
                break;
            case MNU_CMD_REBOOT:
                break;
        }

    }

    public static void ProcessCommands(PluginMessage PP) {
        switch (PP.PinName) {
            case (KK_PLUGIN_BASE_CONTROL_DATA):
                ProcessMenuManager(PP);
                break;

        }
    }

    private static void ProcessMenuManager(PluginMessage PP) {
        PinControlData PD = (PinControlData) PP.PinData;
        //
        switch (PD.DataType) {
            case CONTROL_LONGPRESS:
                if (PP.FeatureID.equals(KK_BASE_FEATURES_SYSTEM_MULTIFEATURE_UID)) {
                    if (PD.ControlID.equals(PinControlData.DEF_BTN_BACK)) {
                        ButtonsManager(PD, true);
                    }
                }
                break;
            case CONTROL_TRIGGERED:
                ButtonsManager(PD, false);
                break;
        }
    }

    private static void ButtonsManager(PinControlData PD, boolean GlobalCommand) {
        switch (PD.ControlID) {
            case PinControlData.DEF_BTN_UP:
                SysMenu.MenuSelectUp();
                break;
            case PinControlData.DEF_BTN_DOWN:
                SysMenu.MenuSelectDown();
                break;
            case PinControlData.DEF_BTN_ENTER:
                SysMenu.MenuExec();
                break;
            case PinControlData.DEF_BTN_BACK:
                break;

        }

    }
}
