package com.sywyar.reuseourworld;

import com.jcraft.jsch.*;
import com.sywyar.reuseourworld.event.BorderListener;
import com.sywyar.reuseourworld.event.HintListener;
import com.sywyar.reuseourworld.event.StringListener;
import com.sywyar.reuseourworld.event.StringUpdate;
import com.sywyar.reuseourworld.utils.Language;
import com.sywyar.reuseourworld.utils.MainWindows;
import com.sywyar.reuseourworld.utils.SftpMonitor;
import com.sywyar.reuseourworld.utils.ZipUtils;
import com.sywyar.superjsonobject.SuperJsonObject;
import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class ReUseOurWorld {
    public static String language="en-US";
    public static StringUpdate outputText = new StringUpdate();
    static boolean isClick =false;
    static int maxZipNum = 10;
    public static void main(String[] args) {
        try {
            BeautyEyeLNFHelper.launchBeautyEyeLNF();
            BeautyEyeLNFHelper.translucencyAtFrameInactive = false;
            UIManager.put("RootPane.setupButtonVisible",false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,Language.getString("useourworld_swing_init_error"),Language.getString("useourworld_error_title"),JOptionPane.WARNING_MESSAGE);
            saveError(e);
            throw new RuntimeException(e);
        }

        System.setProperty("file.encoding","utf-8");

        MainWindows selectLanguage = new MainWindows(400,200);
        selectLanguage.setTitle(Language.getString("select_language_windows_title"));
        JComboBox<String> switchType = new JComboBox<>(new String[]{"English","简体中文"});
        switchType.setSelectedItem("English");
        JButton confirm = new JButton(Language.getString("select_language_button_confirm"));
        switchType.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                language = SelectLanguage((String) switchType.getSelectedItem());
                confirm.setText(Language.getString("select_language_button_confirm"));
                selectLanguage.setTitle(Language.getString("select_language_windows_title"));
            }
        });
        switchType.setBounds(10,35,150,30);
        confirm.setBounds(170,35,100,30);
        selectLanguage.background.add(switchType);
        selectLanguage.background.add(confirm);
        selectLanguage.setVisible(true);

        confirm.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                selectLanguage.setVisible(false);

                MainWindows mainWindows = new MainWindows(800,600);
                mainWindows.setTitle(Language.getString("useourworld_title"));
                JComboBox<String> serverType = new JComboBox<>(new String[]{"Forge Server","Bukkit Server","Minecraft Server"});
                JComboBox<String> workMode = new JComboBox<>(new String[]{Language.getString("useourworld_upload"),Language.getString("useourworld_download"),Language.getString("useourworld_download_replace")});
                JTextField host = new JTextField();
                JTextField port = new JTextField();
                JTextField username = new JTextField();
                JPasswordField password = new JPasswordField();
                JLabel output = new JLabel(){
                    @Override
                    public void setText(String text) {
                        super.setText("<html><body>"+text+"</body></html>");
                    }
                };
                JScrollPane outputPane = new JScrollPane(output);
                JButton start = new JButton(Language.getString("useourworld_start"));

                outputPane.getVerticalScrollBar().addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        isClick=true;
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        isClick=false;
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {

                    }

                    @Override
                    public void mouseExited(MouseEvent e) {

                    }
                });
                outputPane.getVerticalScrollBar().addAdjustmentListener(e1 -> {
                    if (!isClick){
                        e1.getAdjustable().setValue(e1.getAdjustable().getMaximum());
                    }
                });

                host.addFocusListener(new HintListener(host,Language.getString("useourworld_host_hint")));
                port.addFocusListener(new HintListener(port,Language.getString("useourworld_port_hint")));
                username.addFocusListener(new HintListener(username,Language.getString("useourworld_username_hint")));

                host.addFocusListener(new BorderListener(host));
                port.addFocusListener(new BorderListener(port));
                username.addFocusListener(new BorderListener(username));
                password.addFocusListener(new BorderListener(password));

                serverType.setBounds(10,10,150,30);
                workMode.setBounds(170,10,110,30);
                host.setBounds(290,10,100,30);
                port.setBounds(400,10,50,30);
                username.setBounds(460,10,100,30);
                password.setBounds(570,10,100,30);
                start.setBounds(680,10,50,30);
                outputPane.setBounds(10,50,730,460);

                outputPane.setBorder(new LineBorder(new Color(5, 103, 157), 2,true));

                output.setHorizontalAlignment(SwingConstants.LEFT);
                output.setVerticalAlignment(SwingConstants.TOP);

                mainWindows.background.add(serverType);
                mainWindows.background.add(workMode);
                mainWindows.background.add(host);
                mainWindows.background.add(port);
                mainWindows.background.add(username);
                mainWindows.background.add(password);
                mainWindows.background.add(start);
                mainWindows.background.add(outputPane);

                mainWindows.setVisible(true);

                StringListener listener = output::setText;
                outputText.setPercentListener(listener);

                start.addActionListener(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (host.getText().equals("") || host.getText().equals(Language.getString("useourworld_host_hint")) || username.getText().equals("") || username.getText().equals(Language.getString("useourworld_username_hint"))){
                            JOptionPane.showMessageDialog(null,Language.getString("useourworld_incomplete_information"),Language.getString("useourworld_error_title"),JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if (port.getText().equals("") || port.getText().equals(Language.getString("useourworld_port_hint")))port.setText("22");
                        if (new String(password.getPassword()).equals("") && (JOptionPane.showConfirmDialog(null,Language.getString("useourworld_confirm_message"),Language.getString("useourworld_confirm_title"),JOptionPane.YES_NO_OPTION))==1)return;

                        serverType.setEnabled(false);
                        workMode.setEnabled(false);
                        host.setEditable(false);
                        port.setEditable(false);
                        username.setEditable(false);
                        password.setEditable(false);

                        new Thread(() -> {
                            try {
                                outputText.updateString(Language.getString("useourworld_sftp_conning"));
                                ChannelSftp sftp = getChannelSftp(host.getText(), Integer.parseInt(port.getText()),username.getText(), new String(password.getPassword()));
                                if (!isDirExist("UseOurWorld",sftp))sftp.mkdir("UseOurWorld");
                                sftp.cd("UseOurWorld");
                                try {
                                    sftp.ls("UseOurWorld-List.json");
                                } catch (Exception ex) {
                                    File listJson = new File(System.getProperty("user.dir")+"/UseOurWorld/UseOurWorld-List.json");
                                    if (!listJson.exists()){
                                        if (!listJson.getParentFile().exists()){
                                            listJson.getParentFile().mkdirs();
                                        }
                                        try {
                                            listJson.createNewFile();
                                        } catch (IOException exc) {
                                            saveError(exc);
                                            throw new RuntimeException(exc);
                                        }
                                    }
                                    Writer(new SuperJsonObject().toString(),listJson);
                                    sftp.put(listJson.getPath(),"UseOurWorld-List.json");
                                }
                                File dir = new File(System.getProperty("user.dir")+"/UseOurWorld");
                                if (!dir.exists())dir.mkdirs();
                                sftp.get("UseOurWorld-List.json",dir.getPath());
                                File listJson = new File(System.getProperty("user.dir")+"/UseOurWorld/UseOurWorld-List.json");
                                SuperJsonObject list = new SuperJsonObject(listJson);

                                outputText.updateString(Language.getString("useourworld_output_connect_success"));

                                if (workMode.getSelectedItem().equals(Language.getString("useourworld_upload"))){
                                    upload(list,sftp, (String) serverType.getSelectedItem());
                                } else if (workMode.getSelectedItem().equals(Language.getString("useourworld_download"))) {
                                    download(list,sftp);
                                } else if (workMode.getSelectedItem().equals(Language.getString("useourworld_download_replace"))) {
                                    replace((String) serverType.getSelectedItem(),download(list,sftp));
                                }

                                Writer(list.toString(),listJson);
                                sftp.put(listJson.getPath(),"UseOurWorld-List.json");
                                listJson.delete();

                                serverType.setEnabled(true);
                                workMode.setEnabled(true);
                                host.setEditable(true);
                                port.setEditable(true);
                                username.setEditable(true);
                                password.setEditable(true);

                                outputText.updateString(Language.getString("useourworld_re_end"));
                            } catch (ParseException | SftpException | FileNotFoundException ex) {
                                saveError(ex);
                                throw new RuntimeException(ex);
                            }catch (JSchException ex){

                                serverType.setEnabled(true);
                                workMode.setEnabled(true);
                                host.setEditable(true);
                                port.setEditable(true);
                                username.setEditable(true);
                                password.setEditable(true);

                                JOptionPane.showMessageDialog(null,Language.getString("useourworld_sftp_connect_error"),Language.getString("useourworld_error_title"),JOptionPane.WARNING_MESSAGE);
                                outputText.updateString(Language.getString("useourworld_sftp_connect_error"));
                            }
                        }).start();
                    }
                });
            }
        });
    }

    public static void saveError(Exception e) {
        File log = new File(System.getProperty("user.dir")+"/UseOurWorld.log");
        ReUseOurWorld.Writer("Type:"+ getExceptionType(e) + "\n"+
                "Message:"+ getExceptionMessage(e) +"\n"+
                "ExceptionSprintStackTrace:"+ getExceptionSprintStackTrace(e),log);
        showErrorDialog(Language.getString("useourworld_error_title"),Language.getString("useourworld_error_message"));
    }

    public static String SelectLanguage(String str){
        switch (str){
            case "English":return "en-US";
            case "简体中文":return "zh-CN";
            default:return "unknown";
        }
    }

    public static void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
        System.exit(-1);
    }

    public static boolean isDirExist(String directory, ChannelSftp sftp)
    {
        boolean isDirExistFlag = false;
        try
        {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        }
        catch (Exception e)
        {
            if (e.getMessage().toLowerCase().equals("no such file"))
            {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }

    public static void upload(SuperJsonObject list,ChannelSftp sftp,String serverType) throws SftpException, ParseException {
        outputText.updateString(Language.getString("useourworld_sftp_work_mode_upload"));

        if (list.size()>=maxZipNum){
            outputText.updateString(Language.getString("useourworld_sftp_exceed_max"));
            sftp.rm(list.getAsString(String.valueOf(0)));
            list.remove(String.valueOf(0));
            for (int i = 1; i < maxZipNum; i++) {
                list.addProperty(String.valueOf(i-1),list.getAsString(String.valueOf(i)));
                list.remove(String.valueOf(i));
            }
        }

        switch (serverType){
            case "Forge Server": zipForgeServer();break;
            case "Bukkit Server":zipBukkitServer();break;
            case "Minecraft Server":zipMinecraftServer();break;
            default:showErrorDialog(Language.getString("useourworld_error_title"),Language.getString("useourworld_unknown_server_type"));
                return;
        }
        File zip = new File(System.getProperty("user.dir") + "/UseOurWorld.zip");
        Date date = new Date();
        SimpleDateFormat bjSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        bjSdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date BeiJinDate = bjSdf.parse(bjSdf.format(date));
        String time = BeiJinDate.toString().replace(" ","_").replace(":",".");
        File newZip = new File(zip.getParentFile().getPath()+"/UseOurWorld-"+time+".zip");
        zip.renameTo(newZip);
        SftpMonitor monitor = new SftpMonitor(newZip.length(),outputText);
        sftp.put(newZip.getPath(),newZip.getName(),monitor);
        list.addProperty(String.valueOf(list.size()),newZip.getName());
        newZip.delete();
    }

    public static File download(SuperJsonObject list,ChannelSftp sftp) throws SftpException {
        outputText.updateString(Language.getString("useourworld_sftp_work_mode_download"));
        outputText.updateString(Language.getString("useourworld_sftp_save_latest"));

        if (list.size()==0){
            outputText.updateString(Language.getString("useourworld_download_server_no_exceed"));
            return null;
        }

        String zipName = list.getAsString(String.valueOf(list.size()-1));
        SftpATTRS zip = sftp.lstat(zipName);
        SftpMonitor monitor = new SftpMonitor(zip.getSize(),outputText);
        File localZip = new File(System.getProperty("user.dir")+"/UseOurWorld/"+zipName);
        sftp.get(zipName,localZip.getPath(),monitor);

        outputText.updateString(Language.getString("useourworld_sftp_end_saved")+":"+localZip.getPath());

        return localZip;
    }

    public static void replace(String serverType,File localZip){
        if (localZip==null){
            outputText.updateString(Language.getString("useourworld_download_server_no_file"));
            return;
        }
        switch (serverType){
            case "Forge Server": deleteForgeServer();break;
            case "Bukkit Server":deleteBukkitServer();break;
            case "Minecraft Server":deleteMinecraftServer();break;
            default:
                showErrorDialog(Language.getString("useourworld_error_title"),Language.getString("useourworld_unknown_server_type"));
                return;
        }
        ZipUtils.unzip(localZip.getPath(), System.getProperty("user.dir"), "UTF-8", evt -> {
            if (evt.getOldValue()!=evt.getNewValue())outputText.updateString(Language.getString("useourworld_zip_decompression")+"("+evt.getNewValue()+"%)");
        });
        localZip.delete();
    }

    public static void zipForgeServer(){
        zipServer(new String[]{System.getProperty("user.dir")+"/config",System.getProperty("user.dir")+"/mods",System.getProperty("user.dir")+"/world"});
    }

    public static void zipBukkitServer(){
        zipServer(new String[]{System.getProperty("user.dir")+"/plugins",System.getProperty("user.dir")+"/world",System.getProperty("user.dir")+"/world_nether",System.getProperty("user.dir")+"/world_the_end"});
    }

    public static void zipMinecraftServer(){
        zipServer(new String[]{System.getProperty("user.dir")+"/world"});
    }

    public static void deleteForgeServer(){
        deleteFile(new File(System.getProperty("user.dir")+"/config"),new File(System.getProperty("user.dir")+"/mods"),new File(System.getProperty("user.dir")+"/world"));
    }

    public static void deleteBukkitServer(){
        deleteFile(new File(System.getProperty("user.dir")+"/plugins"),new File(System.getProperty("user.dir")+"/world"),new File(System.getProperty("user.dir")+"/world_nether"),new File(System.getProperty("user.dir")+"/world_the_end"));
    }

    public static void deleteMinecraftServer(){
        deleteFile(new File(System.getProperty("user.dir")+"/world"));
    }

    public static void zipServer(String[] files){
        int total = files.length;
        AtomicInteger num= new AtomicInteger(0);
        ZipUtils.zip(System.getProperty("user.dir") + "/UseOurWorld.zip", "UTF-8", evt -> {
            if (evt.getOldValue()!=evt.getNewValue()){
                if ((int)evt.getOldValue()==0) num.getAndAdd(1);
                outputText.updateString(Language.getString("useourworld_zip_compressing")+"("+evt.getNewValue()+"%)("+num.get()+"/"+total+")");
            }
        },files);
    }

    public static void deleteFile(File... files){
        if (files!=null){
            for (File file : files) {
                if (file.exists()){
                    if (file.isFile()) {
                        outputText.updateString(Language.getString("useourworld_delete_file")+"("+file.getName()+"):"+(file.delete()?Language.getString("useourworld_success"):Language.getString("useourworld_fail")));
                    } else {
                        File[] var = file.listFiles();
                        if (var != null){
                            for (File var1 : var){
                                deleteFile(var1);
                            }
                        }
                        outputText.updateString(Language.getString("useourworld_delete_dir")+"("+file.getName()+"):"+(file.delete()?Language.getString("useourworld_success"):Language.getString("useourworld_fail")));
                    }
                }
            }
        }
    }

    public static ChannelSftp getChannelSftp(String host,int port,String username,String password) throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(username,host,port);
        if (password!=null){
            session.setPassword(password);
        }
        session.setConfig("StrictHostKeyChecking","no");
        session.connect();
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        return channel;
    }

    public static void Writer(String str, File file){
        try {
            Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
            writer.write(str);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Throwable getExceptionType(Exception e) {
        return e;
    }
    public static String getExceptionMessage(Exception e) {
        return e.getMessage();
    }
    public static String getExceptionSprintStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
