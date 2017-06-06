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

package view;

import java.awt.Dimension;
import java.awt.Desktop;
import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.text.MessageFormat;

import javax.mail.MessagingException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.JFileChooser;

import adbenitez.notify.core.Notification;
import controller.MailManager;
import controller.PManager;
import controller.R;
import controller.event.DraggerListener;
import controller.event.MailEvent;
import controller.event.MailListener;

public class Main extends JFrame {

    //	================= ATTRIBUTES ==============================

    private static final long serialVersionUID = 1L;

    private static Main main;

    private PManager pManager;
    private MailManager mManager;
    private SettingsForm settingsForm;

    private GraphicsDevice gDevice;
    private JMenuBar menuBar;
    private JFileChooser fileChooser;
    private JPanel panel;
    private JTextField browseTF;
    private JTextField searchTF;

    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================

    private Main () {
        super();
        Notification.setDefaultTheme(Notification.ThemeType.NONE);
        setUndecorated(true);
        setup();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(pManager.getAlwaysOnTop());
        DraggerListener dragger = new DraggerListener(this);
        addMouseListener(dragger);
        addMouseMotionListener(dragger);
        updateOpacity();
    }

    //	================= END CONSTRUCTORS =======================

    //	===================== METHODS ============================

    public void updateOpacity() {
        if (gDevice.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT)) {
            setOpacity(pManager.getOpacity());
        } else {
            pManager.writeLog("main.opacity-not-supported");
        }
    }

    private void setup() {
        pManager = PManager.getInstance();
        mManager = MailManager.getInstance();
        settingsForm = new SettingsForm(this);
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        gDevice = ge.getDefaultScreenDevice();
        setContentPane(get_panel());
        mManager.addMailListener(new MailListener() {
                public void newMailReceived(MailEvent evt) {
                    File attach = evt.getAttachment();
                    if (attach != null) {
                        String url;
                        if (attach.isDirectory()) {
                            url = createIndex(attach);
                        } else {
                             url = attach.toURI().toString();
                        }
                        if (pManager.useExternalBrowser()) {
                            addExternTab(url);
                        } else {
                            pManager.writeLog(R.getString("main.browser-disabled")); //$NON-NLS-1$
                        }
                    }
                }
                public void errorOccurred(MailEvent evt) {
                    Exception e = evt.getError();
                    pManager.writeLog(e);
                    Notification.show(R.getString("main.error"), //$NON-NLS-1$
                                      e.getMessage(),
                                      Notification.ERROR_MESSAGE,
                                      4000);
                }
            });
        mManager.startReceiver();
    }

    private String createIndex(File attach) {
        File[] webPages = attach.listFiles(new FilenameFilter() {
                public boolean accept(File f, String path) {
                    path = path.toLowerCase();
                    if (path.endsWith(".html") || path.endsWith(".htm")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        int len = webPages.length;
        if (len == 0) {
            return attach.toURI().toString();
        } else if (len == 1) {
            return webPages[0].toURI().toString();
        }
        String attName = attach.getName();
        String content = "<html><head>"
            +"<title>" + attName + "</title>"
            +"<style>"
            +"html { padding-top:25px; background: #0074C9; }"
            +"body { width:90%; padding: 20px; margin: 0px auto; background-color:#E1EDEB; box-shadow:10px 10px 10px rgba(0,0,0,.5); }"
            +".button { background: #0074C9; text-decoration: none; color: white; padding: 10px; box-shadow:3px 3px 5px rgba(0,0,0,.5); }"
            +".button:active { box-shadow:none; }"
            +"</style>"
            +"</head><body>"
            +"<h2>" + MessageFormat.format(R.getString("main.index-banner"), attName) + "</h2>"
            +"<ul>";
        String name;
        for (int i = 0; i < len; i++) {
            name = webPages[i].getName();
            content += "<li><a href=\"./" + name + "\">"+ name +"</a></li><br/>";
        }
        content += "</ul><br/><a class=\"button\" href=\"./\">"+ R.getString("main.show-all") +"</a></body></html>";

        File indexFile = new File(attach, "__index__.html");
        try {
            PrintWriter pw = new PrintWriter(indexFile);
            pw.print(content);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
        return indexFile.toURI().toString();
    }

    private JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser(pManager.getDownloadsPath());
        }
        return fileChooser;
    }

    private JMenuBar get_menuBar() {
        if (menuBar == null) {
            // ---- FILE -----------------
            JMenuItem openMI = new JMenuItem(R.getString("main.open")); //$NON-NLS-1$
            openMI.setMnemonic(R.getChar("main.open-nemonic"));
            openMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        int retVal = getFileChooser().showOpenDialog(Main.this);
                        if (retVal == JFileChooser.APPROVE_OPTION) {
                            File f = getFileChooser().getSelectedFile();
                            addExternTab(f.toURI().toString());
                        }
                    }
                });
            JMenuItem minMI = new JMenuItem(R.getString("main.minimize")); //$NON-NLS-1$
            minMI.setMnemonic(R.getChar("main.min-nemonic"));
            minMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        Main.this.setExtendedState(JFrame.ICONIFIED);
                    }
                });
            JMenuItem quitMI = new JMenuItem(R.getString("main.quit")); //$NON-NLS-1$
            quitMI.setMnemonic(R.getChar("main.quit-nemonic"));
            quitMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        Main.this.setVisible(false);
                        System.exit(0);
                    }
                });
            JMenu fileM = new JMenu(R.getString("main.file")); //$NON-NLS-1$
            fileM.setMnemonic(R.getChar("main.file-nemonic"));
            fileM.add(openMI);
            fileM.addSeparator();
            fileM.add(minMI);
            fileM.add(quitMI);
            // ---- FILE -----------------
            // ---- EDIT -----------------
            JMenuItem prefsMI = new JMenuItem(R.getString("main.prefs")); //$NON-NLS-1$
            prefsMI.setMnemonic(R.getChar("main.prefs-nemonic"));
            prefsMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        settingsForm.setVisible(true);
                    }
                });
            JMenu editM = new JMenu(R.getString("main.edit")); //$NON-NLS-1$
            editM.setMnemonic(R.getChar("main.edit-nemonic"));
            editM.add(prefsMI);
            editM.addSeparator();
            // ---- EDIT -----------------
            // ---- TOOLS -----------------
            JMenuItem gamesMI = new JMenuItem(R.getString("main.games")); //$NON-NLS-1$
            gamesMI.setMnemonic(R.getChar("main.games-nemonic"));
            gamesMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        addExternTab(pManager.getGamesPage());
                    }
                });
            JMenu toolsM = new JMenu(R.getString("main.tools")); //$NON-NLS-1$
            toolsM.setMnemonic(R.getChar("main.tools-nemonic"));
            toolsM.add(gamesMI);
            toolsM.addSeparator();
            // ---- TOOLS -----------------
            // ---- HELP -----------------
            JMenuItem helpMI = new JMenuItem(R.getString("main.help")); //$NON-NLS-1$
            helpMI.setMnemonic(R.getChar("main.help-nemonic"));
            helpMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        addExternTab(pManager.getHelpPage());
                    }
                });
            JMenuItem aboutMI = new JMenuItem(R.getString("main.about")); //$NON-NLS-1$
            aboutMI.setMnemonic(R.getChar("main.about-nemonic"));
            aboutMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        addExternTab(pManager.getAboutPage());
                    }
                });
            JMenu helpM = new JMenu(R.getString("main.help")); //$NON-NLS-1$
            helpM.setMnemonic(R.getChar("main.help-nemonic"));
            helpM.add(helpMI);
            helpM.add(aboutMI);
            // ---- HELP -----------------

            menuBar = new JMenuBar();
            menuBar.add(fileM);
            menuBar.add(editM);
            menuBar.add(toolsM);
            menuBar.add(helpM);
        }
        return menuBar;
    }

    private JPanel get_panel() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridy = 0; c.gridx = 0;
            panel.add(get_menuBar(), c);
            c.gridy = 1; c.gridx = 0; c.weightx = 1; c.gridwidth = 2;
            c.insets = new Insets(0, 0, 0, 10);//top, left, bott, right
            panel.add(getBrowseTF(), c);

            c.gridx = 2; c.weightx = 0; c.gridwidth = 1;
            c.insets = new Insets(0, 0, 0, 0);
            panel.add(getSearchTF(), c);

            panel.setBorder(BorderFactory.createRaisedBevelBorder());
        }
        return panel;
    }

    private JTextField getBrowseTF() {
        if (browseTF == null) {
            browseTF = new JTextField(R.getString("main.browse-banner"));
            Dimension d = browseTF.getPreferredSize();
            browseTF.setPreferredSize(new Dimension(400, d.height));
            browseTF.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        browse();
                    }
                });
            browseTF.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        browseTF.select(0, browseTF.getText().length());
                    }
                    @Override
                    public void focusLost(FocusEvent e) {
                        browseTF.select(0, 0);
                    }
                });
        }
        return browseTF;
    }

    private JTextField getSearchTF() {
        if (searchTF == null) {
            searchTF = new JTextField(R.getString("main.search-banner"));
            Dimension d = searchTF.getPreferredSize();
            searchTF.setMinimumSize(new Dimension(150, d.height));
            searchTF.setPreferredSize(new Dimension(250, d.height));
            searchTF.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        search();
                    }
                });
            searchTF.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        searchTF.select(0, searchTF.getText().length());
                    }
                    @Override
                    public void focusLost(FocusEvent e) {
                        searchTF.select(0, 0);
                    }
                });
        }
        return searchTF;
    }

    private void browse() {
        String text = browseTF.getText().trim();
        if (text.equals("")) { //$NON-NLS-1$
            Toolkit.getDefaultToolkit().beep();
        } else {
            if (!(text.indexOf("://") >= 0
                  || text.indexOf("%3A%2F%2F") >= 0)) { //$NON-NLS-1$
                text = "http://" + text;    //$NON-NLS-1$
            }
            int end = text.indexOf("&sa=");
            if (text.startsWith("http://www.google.com/url?q=") && end > 0) {
                text = text.replace("http://www.google.com/url?q=", "");
                end = text.indexOf("&sa=");
                text = text.substring(0, end);
                browseTF.setText(text);
            }

            String cmd = pManager.getBrowseCmd();
            String to = pManager.getBrowseBot();
            text = (cmd + text).trim(); //$NON-NLS-1$
            sendMail(to, text, text);
        }
    }

    @SuppressWarnings("deprecation")
    private void search() {
        String text = searchTF.getText().trim();
        if (text.equals("")) { //$NON-NLS-1$
            Toolkit.getDefaultToolkit().beep();
        } else {
            try {
                text = URLEncoder.encode(text, "UTF-8");  //$NON-NLS-1$
            } catch (UnsupportedEncodingException e) {
                pManager.writeLog(e);
                text = URLEncoder.encode(text);
            }
            String to = pManager.getSearchBot();
            String cmd = pManager.getSearchCmd();
            text = (cmd + text).trim();
            sendMail(to, text, text);
        }
    }

    private void sendMail(final String to, final String subj,
                          final String body) {
        new Thread() {
            public void run() {
                boolean browFocus = browseTF.isFocusOwner();
                boolean searFocus = searchTF.isFocusOwner();
                browseTF.setEnabled(false);
                searchTF.setEnabled(false);

                try {
                    mManager.sendMail(to, subj, body);
                    Notification.show(R.getString("main.success"), //$NON-NLS-1$
                                      R.getString("main.petition-sent"), //$NON-NLS-1$
                                      Notification.SUCCESS_MESSAGE,
                                      2000);
                } catch (MessagingException e) {
                    pManager.writeLog(e);
                    Notification.show(R.getString("main.error"), //$NON-NLS-1$
                                      e.getMessage(),
                                      Notification.ERROR_MESSAGE,
                                      6000);
                    try {
                        mManager.senderConnect();
                    } catch (MessagingException ex) {
                        pManager.writeLog(ex);
                    }
                }
                browseTF.setEnabled(true);
                searchTF.setEnabled(true);
                if (browFocus) {
                    browseTF.requestFocus();
                } else if (searFocus) {
                    searchTF.requestFocus();
                }
            }
        }.start();
    }

    private void addExternTab(String url) {
        String browser = pManager.getExternalBrowser();
        if (browser.equals("<DEFAULT>")) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                    return;
                } catch (URISyntaxException ex) {
                    pManager.writeLog(ex);
                } catch (IOException ex) {
                    pManager.writeLog(ex);
                }
            }
            Notification.show(R.getString("main.error"), R.getString("main.desktop-not-supp"), Notification.ERROR_MESSAGE);
            return;
        }

        ProcessBuilder builder = new ProcessBuilder(Arrays.asList((browser+" "+url).split(" "))); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            builder.start();
        } catch (IOException e) {
            pManager.writeLog(e);
        }
    }

    public static Main getInstance() {
        if (main == null) {
            main = new Main();
        }
        return main;
    }

    //	====================== END METHODS =======================

    //	========================= MAIN ===========================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    Main.getInstance().setVisible(true);
                }
            });
    }

    //	======================= END MAIN =========================
}
