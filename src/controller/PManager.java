/*
 * Copyright (c) 2017 Asiel Díaz Benítez.
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * You should have received a copy of the GNU General Public License
 * along with this file.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Properties;
import java.text.MessageFormat;

public class PManager {

    //	================= ATTRIBUTES ==============================

    private static PManager pManager;

    public String program_name;
    private String user_name;

    private Properties prefs;
    private String program_path;
    private PrintStream log_stream;

    private final String SEPARATOR;

    private final String FIRST_TIME = "prefs.fisttime";
    private final String OPACITY = "prefs.opacity";
    private final String ALWAYS_ON_TOP = "prefs.alwaysontop";
    private final String DEBUG = "mail.debug";
    private final String BROWSE_BOT = "prefs.browse-bot";
    private final String BROWSE_CMD = "prefs.browse-cmd";
    private final String SEARCH_BOT = "prefs.search-bot";
    private final String SEARCH_CMD = "prefs.search-cmd";
    private final String USE_EXT_BROWSER = "prefs.use-external-browser";
    private final String EXTERNAL_BROWSER = "prefs.external-browser";
    // SENDER
    private final String SENDER_PROTOCOL = "prefs.sender.protocol";
    //mail
    public final String SENDER_STARTTLS = "mail.smtp.starttls.enable";
    public final String SENDER_SSL = "mail.smtp.ssl.enable";
    public final String SENDER_HOST = "mail.smtp.host";
    public final String SENDER_PORT = "mail.smtp.port";
    public final String SENDER_FROM = "mail.smtp.from";
    public final String SENDER_AUTH = "mail.smtp.auth";
    public final String SENDER_USER = "mail.smtp.user";
    public final String SENDER_PASS = "mail.smtp.pass";
    private final String SENDER_TIMEOUT ="mail.smtp.connectiontimeout";
    // RECEIVER
    private final String RECEIVER_PROTOCOL = "prefs.receiver.protocol";
    private final String RECEIVE_FROM = "prefs.receiver.receivefrom";
    private final String RECEIVE_FOLDER = "prefs.receiver.folder";
    private final String RECEIVER_INBOX = "prefs.receiver.inbox";
    private final String RECEIVER_ACTION = "prefs.receiver.action";

    private String helpPage;
    private String aboutPage;
    private String gamesPage;
    private boolean debug;

    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================

    private PManager () {
        debug = false;
        SEPARATOR = File.separator;
        program_name = R.getString("app.name");
        user_name = System.getProperty("user.name");
        prefs = get_Program_Properties();

    }

    //	================= END CONSTRUCTORS =======================

    //	===================== METHODS ============================

    public void writeLog(String log) {
        if (getDebug()) {
            System.out.println(log);
            if (log_stream != null) {
                log_stream.println(log);
            }
        }
    }

    public void writeLog(Exception ex) {
        if (getDebug()) {
            System.out.println(ex.getMessage());
            if (log_stream != null) {
                ex.printStackTrace(log_stream);
            }
        }
    }

    public String get_Log_File_Path() {
        return Paths.get(getAssetsPath(), program_name + ".log").toString();
    }

    private void open_Log_Stream() {
        try {
            File file = new File(get_Log_File_Path());
            double size = file.length() / 1024; // KB
            FileOutputStream os = new FileOutputStream(file, (size <= 1024));
            log_stream = new PrintStream(os, true);
            if (getDebug()) {
                log_stream.println("============== "
                                   + R.getString("pm.prog-started")
                                   + " ==============");
            }
        } catch (Exception ex) {
            log_stream = System.out;
            ex.printStackTrace();
        }
    }

    public void close_Log_Stream() {
        if (log_stream != null) {
            if (getDebug()) {
                log_stream.println("============== "
                                   + R.getString("pm.prog-closed")
                                   + " ==============");
            }
            log_stream.close();
        }
    }

    private Properties get_Program_Properties() {
        if (prefs == null) {
            try {
                URI uri = PManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                program_path = Paths.get(uri).getParent().toString();

                prefs = new Properties();
                File f = Paths.get(getAssetsPath(), program_name
                                   +R.getString("pm.config-ext")).toFile();
                if (!f.exists()) {
                    f.createNewFile();
                } else {
                    FileInputStream in = new FileInputStream(f);
                    prefs.load(in);
                    in.close();
                }
                open_Log_Stream();
            } catch (Exception ex) {
                writeLog(ex);
            }
        }
        return prefs;
    }

    public String getAssetsPath() {
        return Paths.get(program_path, R.getString("pm.assets")).toString();
    }

    public String getDownloadsPath() {
        return Paths.get(getAssetsPath(), R.getString("pm.downloads")).toString();
    }

    public String getHelpPage() {
        if (helpPage == null) {
            String helpDir = MessageFormat.format(R.getString("pm.help-dir"), SEPARATOR);
            helpPage = R.getString("pm.help-page");
            helpPage = Paths.get(getAssetsPath(), helpDir, helpPage).toUri().toString();
        }
        return helpPage;
    }

    public String getGamesPage() {
        if (gamesPage == null) {
            String gamesDir = MessageFormat.format(R.getString("pm.games-dir"), SEPARATOR);
            gamesPage = R.getString("pm.games-page");
            gamesPage = Paths.get(getAssetsPath(), gamesDir, gamesPage).toUri().toString();
        }
        return gamesPage;
    }

    public String getAboutPage() {
        if (aboutPage == null) {
            String aboutDir = MessageFormat.format(R.getString("pm.about-dir"), SEPARATOR);
            aboutPage = R.getString("pm.about-page");
            aboutPage = Paths.get(getAssetsPath(), aboutDir, aboutPage).toUri().toString();
        }
        return aboutPage;
    }

    public void save_Config() {
        try {
            File f = Paths.get(getAssetsPath(), program_name+R.getString("pm.config-ext")).toFile();
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            prefs.store(out, R.getString("pm.config-banner"));
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String get_Program_Path() {
        return program_path;
    }

    public boolean isFirstTime() {
        boolean ft = Boolean.parseBoolean(prefs.getProperty(FIRST_TIME, "true"));
        if (ft) {
            prefs.setProperty(FIRST_TIME, "false");
            save_Config();
        }
        return ft;
    }

    // ------------------------

    public boolean useExternalBrowser() {
        return Boolean.parseBoolean(prefs.getProperty(USE_EXT_BROWSER, "true"));
    }

    public float getOpacity() {
        return Float.parseFloat(prefs.getProperty(OPACITY, R.getString("pm.opacity")));
    }

    public void setOpacity(float op) {
        prefs.setProperty(OPACITY, Float.toString(op));
    }

    public void useExternalBrowser(boolean status) {
        prefs.setProperty(USE_EXT_BROWSER, Boolean.toString(status));
    }

    public void setExternalBrowser(String browser) {
        prefs.setProperty(EXTERNAL_BROWSER, browser);
    }

    public String getExternalBrowser() {
        return prefs.getProperty(EXTERNAL_BROWSER, R.getString("pm.browser"));
    }

    public String getUser() {
        return getSenderUser();
    }

    public void setUser(String user) {
        setSenderUser(user);
        setReceiverUser(user);
    }

    public String getPassword() {
        return getSenderPass();
    }

    public void setPassword(String password) {
        setSenderPass(password);
        setReceiverPass(password);
    }

    public String getEmail() {
        return getSenderFrom();
    }

    public void setEmail(String email) {
        setSenderFrom(email);
        setReceiverFrom(email);
    }

    public boolean getStartTLS() {
        return getSenderStartTLS();
    }

    public void setStartTLS(boolean enabled) {
        setSenderStartTLS(enabled);
        setReceiverStartTLS(enabled);
    }

    public void setSSL(boolean enabled) {
        setSenderSSL(enabled);
        setReceiverSSL(enabled);
    }

    public boolean getSSL() {
        return getSenderSSL();
    }

    public void setBrowseCmd(String cmd) {
        prefs.setProperty(BROWSE_CMD, cmd);
    }

    public String getBrowseCmd() {
        return prefs.getProperty(BROWSE_CMD, R.getString("pm.browse-cmd"));
    }

    public void setBrowseBot(String bot) {
        prefs.setProperty(BROWSE_BOT, bot);
    }

    public String getBrowseBot() {
        return prefs.getProperty(BROWSE_BOT, R.getString("pm.browse-bot"));
    }

    public void setSearchCmd(String cmd) {
        prefs.setProperty(SEARCH_CMD, cmd);
    }

    public String getSearchCmd() {
        return prefs.getProperty(SEARCH_CMD, R.getString("pm.search-cmd"));
    }

    public void setSearchBot(String bot) {
        prefs.setProperty(SEARCH_BOT, bot);
    }

    public String getSearchBot() {
        return prefs.getProperty(SEARCH_BOT, R.getString("pm.search-bot"));
    }

    public boolean getAlwaysOnTop() {
        return Boolean.parseBoolean(prefs.getProperty(ALWAYS_ON_TOP, "true"));
    }

    public void setAlwaysOnTop(boolean enabled) {
        prefs.setProperty(ALWAYS_ON_TOP, Boolean.toString(enabled));
    }

    public boolean getDebug() {
        return Boolean.parseBoolean(prefs.getProperty(DEBUG, "true"));
    }

    public void setDebug(boolean enabled) {
        prefs.setProperty(DEBUG, Boolean.toString(enabled));
    }

    public Properties getSenderProps() {
        Properties p = new Properties();
        p.setProperty(SENDER_STARTTLS, Boolean.toString(getSenderStartTLS()));
        p.setProperty(SENDER_SSL, Boolean.toString(getSSL()));
        p.setProperty("mail.smtp.ssl.trust", "*");
        p.setProperty(SENDER_HOST, getSenderHost());
        p.setProperty(SENDER_PORT, getSenderPort());
        p.setProperty(SENDER_FROM, getSenderFrom());
        p.setProperty(SENDER_AUTH, "true");
        p.setProperty(SENDER_USER, getUser());
        p.setProperty(SENDER_PASS, getPassword());
        p.setProperty(SENDER_TIMEOUT, Integer.toString(getSenderTimeout()));
        p.setProperty(DEBUG, Boolean.toString(getDebug()&& debug));

        return p;
    }

    public Properties getReceiverProps() {
        Properties p = new Properties();
        String RECEIVER_STARTTLS = "mail."+getReceiverProtocol()+".starttls.enable";
        String RECEIVER_SSL = "mail."+getReceiverProtocol()+".ssl.enable";
        String RECEIVER_HOST = "mail."+getReceiverProtocol()+".host";
        String RECEIVER_PORT = "mail."+getReceiverProtocol()+".port";
        String RECEIVER_AUTH = "mail."+getReceiverProtocol()+".auth";
        String RECEIVER_USER = "mail."+getReceiverProtocol()+".user";
        String RECEIVER_PASS = "mail."+getReceiverProtocol()+".pass";
        String RECEIVER_TIMEOUT = "mail."+getReceiverProtocol()+".connectiontimeout";

        p.setProperty(RECEIVER_STARTTLS, Boolean.toString(getReceiverStartTLS()));
        p.setProperty(RECEIVER_SSL, Boolean.toString(getSSL()));
        p.setProperty("mail."+getReceiverProtocol()+".ssl.trust", "*");
        p.setProperty(RECEIVER_HOST, getReceiverHost());
        p.setProperty(RECEIVER_PORT, getReceiverPort());
        p.setProperty(RECEIVER_AUTH, "true");
        p.setProperty(RECEIVER_USER, getUser());
        p.setProperty(RECEIVER_PASS, getPassword());
        p.setProperty(RECEIVER_TIMEOUT, Integer.toString(getTimeout()));
        p.setProperty(DEBUG, Boolean.toString(getDebug() && debug));
        return p;
    }

    public String getSenderProtocol() {
        return prefs.getProperty(SENDER_PROTOCOL, "smtp");
    }

    public void setSenderProtocol(String protocol) {
        prefs.setProperty(SENDER_PROTOCOL, protocol);
    }

    public boolean getSenderStartTLS() {
        return Boolean.parseBoolean(prefs.getProperty(SENDER_STARTTLS, "false"));
    }

    public void setSenderStartTLS(boolean enabled) {
        prefs.setProperty(SENDER_STARTTLS, Boolean.toString(enabled));
    }

    public void setSenderSSL(boolean enabled) {
        prefs.setProperty(SENDER_SSL, Boolean.toString(enabled));
    }

    public boolean getSenderSSL() {
        return Boolean.parseBoolean(prefs.getProperty(SENDER_SSL, "true"));
    }

    public String getSenderHost() {
        return prefs.getProperty(SENDER_HOST, R.getString("pm.smtp-host"));
    }

    public void setSenderHost(String host) {
        prefs.setProperty(SENDER_HOST, host);
    }

    public String getSenderPort() {
        return prefs.getProperty(SENDER_PORT, R.getString("pm.smtp-port"));
    }

    public void setSenderPort(String port) {
        prefs.setProperty(SENDER_PORT, port);
    }

    public String getSenderFrom() {
        return prefs.getProperty(SENDER_FROM, user_name+R.getString("pm.dom"));
    }

    public void setSenderFrom(String email) {
        prefs.setProperty(SENDER_FROM, email);
        prefs.setProperty(SENDER_USER, getUser(email));
    }

    public boolean getSenderAuth() {
        return Boolean.parseBoolean(prefs.getProperty(SENDER_AUTH, "true"));
    }

    public void setSenderAuth(boolean enabled) {
        prefs.setProperty(SENDER_AUTH, Boolean.toString(enabled));
    }

    public String getSenderUser() {
        return prefs.getProperty(SENDER_USER, user_name);
    }

    public void setSenderUser(String user) {
        prefs.setProperty(SENDER_USER, user);
    }

    public String getSenderPass() {
        return prefs.getProperty(SENDER_PASS, "");
    }

    public void setSenderPass(String pass) {
        prefs.setProperty(SENDER_PASS, pass);
    }

    public int getTimeout() {
        return getSenderTimeout();
    }

    public void setTimeout(int timeout) {
        setSenderTimeout(timeout);
        setReceiverTimeout(timeout);
    }

    private int getSenderTimeout() {
        return Integer.parseInt(prefs.getProperty(SENDER_TIMEOUT, "-1"));
    }

    private void setSenderTimeout(int timeout) {
        prefs.setProperty(SENDER_TIMEOUT, Integer.toString(timeout));
    }

    public String getReceiveFrom() {
        return prefs.getProperty(RECEIVE_FROM, R.getString("pm.browse-bot"));
    }

    public void setReceiveFrom(String email) {
        prefs.setProperty(RECEIVE_FROM, email);
    }

    public String getReceiveFolder() {
        return prefs.getProperty(RECEIVE_FOLDER, getDownloadsPath());
    }

    public void setReceiveFolder(String folder) {
        prefs.setProperty(RECEIVE_FOLDER, folder);
    }

    public String getReceiverINBOX() {
        return prefs.getProperty(RECEIVER_INBOX, R.getString("pm.inbox"));
    }

    public void setReceiverINBOX(String folder) {
        prefs.setProperty(RECEIVER_INBOX, folder);
    }

    public int getReceiverAction() {
        return Integer.parseInt(prefs.getProperty(RECEIVER_ACTION, "1"));
    }

    public void setReceiverAction(int action) {
        prefs.setProperty(RECEIVER_ACTION, Integer.toString(action));
    }

    //---------------------------------
    public String getReceiverProtocol() {
        return prefs.getProperty(RECEIVER_PROTOCOL, "imap");
    }

    public void setReceiverProtocol(String type) {
        prefs.setProperty(RECEIVER_PROTOCOL, type);
    }

    public boolean getReceiverStartTLS() {
        String RECEIVER_STARTTLS = "mail."+getReceiverProtocol()+".starttls.enable";
        return Boolean.parseBoolean(prefs.getProperty(RECEIVER_STARTTLS, "false"));
    }

    public void setReceiverStartTLS(boolean enabled) {
        String RECEIVER_STARTTLS = "mail."+getReceiverProtocol()+".starttls.enable";
        prefs.setProperty(RECEIVER_STARTTLS, Boolean.toString(enabled));
    }

    public boolean getReceiverSSL() {
        String RECEIVER_SSL = "mail."+getReceiverProtocol()+".ssl.enable";
        return Boolean.parseBoolean(prefs.getProperty(RECEIVER_SSL, "true"));
    }

    public void setReceiverSSL(boolean enabled) {
        String RECEIVER_SSL = "mail."+getReceiverProtocol()+".ssl.enable";
        prefs.setProperty(RECEIVER_SSL, Boolean.toString(enabled));
    }

    public String getReceiverHost() {
        String RECEIVER_HOST = "mail."+getReceiverProtocol()+".host";
        return prefs.getProperty(RECEIVER_HOST, R.getString("pm.imap-host"));
    }

    public void setReceiverHost(String host) {
        String RECEIVER_HOST = "mail."+getReceiverProtocol()+".host";
        prefs.setProperty(RECEIVER_HOST, host);
    }

    public String getReceiverPort() {
        String RECEIVER_PORT = "mail."+getReceiverProtocol()+".port";
        return prefs.getProperty(RECEIVER_PORT, R.getString("pm.imap-port"));
    }

    public void setReceiverPort(String port) {
        String RECEIVER_PORT = "mail."+getReceiverProtocol()+".port";
        prefs.setProperty(RECEIVER_PORT, port);
    }

    public String getReceiverFrom() {
        String RECEIVER_FROM = "mail."+getReceiverProtocol()+".from";
        return prefs.getProperty(RECEIVER_FROM, user_name+R.getString("pm.dom"));
    }

    public void setReceiverFrom(String email) {
        String RECEIVER_FROM = "mail."+getReceiverProtocol()+".from";
        String RECEIVER_USER = "mail."+getReceiverProtocol()+".user";
        prefs.setProperty(RECEIVER_FROM, email);
        prefs.setProperty(RECEIVER_USER, getUser(email));
    }

    public String getReceiverUser() {
        String RECEIVER_USER = "mail."+getReceiverProtocol()+".user";
        return prefs.getProperty(RECEIVER_USER, user_name);
    }

    public void setReceiverUser(String user) {
        String RECEIVER_USER = "mail."+getReceiverProtocol()+".user";
        prefs.setProperty(RECEIVER_USER, user);
    }

    public String getReceiverPass() {
        String RECEIVER_PASS = "mail."+getReceiverProtocol()+".pass";
        return prefs.getProperty(RECEIVER_PASS, "");
    }

    public void setReceiverPass(String pass) {
        String RECEIVER_PASS = "mail."+getReceiverProtocol()+".pass";
        prefs.setProperty(RECEIVER_PASS, pass);
    }

    private void setReceiverTimeout(int timeout) {
        String RECEIVER_TIMEOUT = "mail."+getReceiverProtocol()+".connectiontimeout";
        prefs.setProperty(RECEIVER_TIMEOUT, Integer.toString(timeout));
    }

    private String getUser(String email) {
        int i = 0;
        int j = 0;
        while (i < email.length()) {
            if (email.charAt(i) == '<') {
                j = i + 1;
            }
            if (email.charAt(i) == '@') {
                return email.substring(j, i);
            }
            i++;
        }
        return email;
    }

    public static PManager getInstance() {
        if (pManager == null) {
            pManager = new PManager();
        }
        return pManager;
    }

    //	====================== END METHODS =======================

}
