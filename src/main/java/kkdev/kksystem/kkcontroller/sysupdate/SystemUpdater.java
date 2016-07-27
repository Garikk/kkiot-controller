/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kkdev.kksystem.kkcontroller.sysupdate;

import kkdev.kksystem.base.classes.plugins.weblink.WM_Answer;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.String.join;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import kkdev.kksystem.base.classes.plugins.ControllerConfiguration;
import static kkdev.kksystem.base.classes.plugins.weblink.WM_KKMasterConsts.*;
import kkdev.kksystem.kkcontroller.main.ControllerSettingsManager;
import static kkdev.kksystem.kkcontroller.main.ControllerSettingsManager.SaveLastConfUID;
import static kkdev.kksystem.kkcontroller.pluginmanager.PluginLoader.GetRequiredPlugins;
import static kkdev.kksystem.kkcontroller.pluginmanager.PluginLoader.PreInitAllPlugins;
import static kkdev.kksystem.kkcontroller.sysupdate.downloader.Downloader.DownloadFiles;
import static kkdev.kksystem.kkcontroller.sysupdate.downloader.Downloader.SaveConfigFiles;
import kkdev.kksystem.kkcontroller.sysupdate.webmasterconnection.WM_Answer_Configuration_Data;
import kkdev.kksystem.kkcontroller.sysupdate.webmasterconnection.WM_Answer_Configuration_Info;
import kkdev.kksystem.kkcontroller.sysupdate.webmasterconnection.WM_Configuration_Data;
import kkdev.kksystem.kkcontroller.sysupdate.webmasterconnection.WM_File_Data;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import static org.apache.http.impl.client.HttpClientBuilder.create;
import org.apache.http.message.BasicNameValuePair;
import static kkdev.kksystem.kkcontroller.pluginmanager.PluginLoader.GetActivePluginUIDs;

/**
 *
 * @author blinov_is
 */
public abstract class SystemUpdater {

    final static String ___TEST_KKCAR_UUID_ = "2e2efd7b-ab83-42fa-9c00-2e45bb4b3ba1";
    final static String WEBMASTER_URL = "http://www.dingo-cloud.tk/";
    final static String WEBMASTER_URL_SERVICE = "weblink";
    final static int WEBMASTER_CLIENT_VERSION = 1;

    public static boolean CheckUpdate(String KKControllerVersion) {
        //Skip by now
   //    if (true) {
      //      return false;
      //  }

        boolean NeedReload = false;

        ControllerConfiguration UpdatedConfig = null;
        WM_Answer_Configuration_Data NewConfigurations = null;

        //Check configuration
        WM_Answer_Configuration_Info[] ConfInfo = GetConfigInfoFromWeb();
        //
        if (ConfInfo[0] != null && (!ControllerSettingsManager.mainConfiguration.configurationUID.equals(ConfInfo[0].confuuid) | !ControllerSettingsManager.mainConfiguration.configurationStamp.equals(ConfInfo[0].confstamp))) {
            out.println("Loading new Config");
            NeedReload = true;
            NewConfigurations = GetUpdatedConfigurations();
        } else {
            UpdatedConfig = ControllerSettingsManager.mainConfiguration;
        }
        //

        if (NewConfigurations != null) {
            String MainConfUID = "";
            for (WM_Configuration_Data DT : NewConfigurations.configurations) {
                if (DT.configurationtype == 1) {
                    Gson gson = new Gson();
                    UpdatedConfig = gson.fromJson(DT.data, ControllerConfiguration.class);
                    MainConfUID = UpdatedConfig.configurationUID;
                }
            }
            for (WM_Configuration_Data DT : NewConfigurations.configurations) {
                SaveConfigFiles(MainConfUID, DT);
            }
            //
            SaveLastConfUID(MainConfUID);
            //
        }

        //
        //Check plugins
        //
        //Pre init
        //
        PreInitAllPlugins();
        //
        //Get Available plugins list
        //
        Set<String> AvailPlugins = GetActivePluginUIDs();
        //
        // Get Required plugins
        //
        Set<String> ReqPlugins = GetRequiredPlugins(UpdatedConfig.features);
        //
        // Remove existed plugins from requierments list
        //

        AvailPlugins.stream().filter((RP) -> (ReqPlugins.contains(RP))).forEach((RP) -> {
            ReqPlugins.remove(RP);
        });

        DownloadFiles(UpdatedConfig.configurationUID, ReqPlugins);

        return NeedReload;

    }

    private static List<NameValuePair> getCheckServerInfo() {
        List<NameValuePair> nameValuePairs = new ArrayList<>(1);
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_ACT,
                WEBMASTER_REQUEST_CHECK));
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_MYUUID,
                ___TEST_KKCAR_UUID_));
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_CLIENTINFO,
                String.valueOf(WEBMASTER_CLIENT_VERSION)));

        return nameValuePairs;
    }

    private static List<NameValuePair> GetConfigurationInfoRequest() {
        List<NameValuePair> nameValuePairs = new ArrayList<>(1);
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_ACT,
                WEBMASTER_REQUEST_GET_MYCONF_INFO));
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_MYUUID,
                ___TEST_KKCAR_UUID_));

        return nameValuePairs;
    }

    private static List<NameValuePair> GetConfigurationDataRequest() {
        List<NameValuePair> nameValuePairs = new ArrayList<>(1);
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_ACT,
                WEBMASTER_REQUEST_GET_MYCONF_DATA));
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_MYUUID,
                ___TEST_KKCAR_UUID_));

        return nameValuePairs;
    }

    private static List<NameValuePair> GetFilesInfoRequestBin(String ReqFiles) {
        List<NameValuePair> nameValuePairs = new ArrayList<>(1);
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_ACT,
                WEBMASTER_REQUEST_GET_FILES_INFO_BIN));
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_MYUUID,
                ___TEST_KKCAR_UUID_));
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_REQFILESBIN,
                ReqFiles));

        return nameValuePairs;
    }

    private static List<NameValuePair> GetFilesInfoRequestExtConf(String MainConf) {
        List<NameValuePair> nameValuePairs = new ArrayList<>(1);
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_ACT,
                WEBMASTER_REQUEST_GET_FILES_INFO_EXTCONF));
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_MYUUID,
                ___TEST_KKCAR_UUID_));
        nameValuePairs.add(new BasicNameValuePair(WEBMASTER_REQUEST_CONFUUID,
                MainConf));

        return nameValuePairs;
    }

    public static WM_Answer_Configuration_Info[] GetConfigInfoFromWeb() {
        ControllerConfiguration Ret = null;
        WM_Answer Ans;
        Gson gson = new Gson();

        try {
            HttpClient client = create().build();
            HttpPost post = new HttpPost(WEBMASTER_URL + WEBMASTER_URL_SERVICE);

            post.setEntity(new UrlEncodedFormEntity(GetConfigurationInfoRequest()));

            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            Ans = gson.fromJson(rd, WM_Answer.class);

            // if (Ans!=null || Ans[0].answerState == 0) {
            return gson.fromJson(Ans.jsonData, WM_Answer_Configuration_Info[].class
            );
            //  } else {
            //       return null;
            //   }

        } catch (IOException e) {
            return null;
        }
    }

    public static WM_Answer_Configuration_Data GetUpdatedConfigurations() {
        ControllerConfiguration Ret = null;
        WM_Answer Ans;
        Gson gson = new Gson();

        try {
            HttpClient client = create().build();
            HttpPost post = new HttpPost(WEBMASTER_URL + WEBMASTER_URL_SERVICE);

            post.setEntity(new UrlEncodedFormEntity(GetConfigurationDataRequest()));

            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            Ans
                    = gson.fromJson(rd, WM_Answer.class
                    );

            if (Ans.answerState
                    == 0) {
                WM_Answer_Configuration_Data RC = gson.fromJson(Ans.jsonData, WM_Answer_Configuration_Data.class);
                return RC;
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static WM_File_Data[] GetPluginFilesInfo(Set<String> Plugins) {
        if (Plugins.isEmpty()) {
            return null;
        }

        ControllerConfiguration Ret = null;
        WM_Answer Ans;
        Gson gson = new Gson();

        try {
            HttpClient client = create().build();
            HttpPost post = new HttpPost(WEBMASTER_URL + WEBMASTER_URL_SERVICE);

            post.setEntity(new UrlEncodedFormEntity(GetFilesInfoRequestBin(join(",", Plugins))));

            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            Ans
                    = gson.fromJson(rd, WM_Answer.class
                    );

            if (Ans.answerState
                    == 0) {
                return gson.fromJson(Ans.jsonData, WM_File_Data[].class);
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static WM_File_Data[] GetExternalConfigurationsInfo(String MainConf) {
        ControllerConfiguration Ret = null;
        WM_Answer Ans;
        Gson gson = new Gson();

        try {
            HttpClient client = create().build();
            HttpPost post = new HttpPost(WEBMASTER_URL + WEBMASTER_URL_SERVICE);

            post.setEntity(new UrlEncodedFormEntity(GetFilesInfoRequestExtConf(MainConf)));

            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            Ans = gson.fromJson(rd, WM_Answer.class);

            if (Ans.answerState == 0) {
                return gson.fromJson(Ans.jsonData, WM_File_Data[].class);
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
