/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kkdev.kksystem.kkcontroller.main;

import java.util.ArrayList;
import java.util.Arrays;
import kkdev.kksystem.base.classes.PluginInfo;

/**
 *
 * @author blinov_is
 */
public class KKSystemConfig  {
    public PluginInfo[] ConfPlugins;
    
    public ArrayList<PluginInfo> GetConfPlugins()
    {
        ArrayList<PluginInfo> Ret;
        Ret=new ArrayList<>();
        Ret.addAll(Arrays.asList(ConfPlugins));
       return (Ret);
    }
    
   
}
