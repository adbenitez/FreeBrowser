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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import adbenitez.notify.core.Notification;

import controller.PManager;
import controller.R;

public class SettingsForm extends JDialog {

    //	================= ATTRIBUTES ==============================

    private static final long serialVersionUID = 1L;
    private Main parent;
    private PManager pManager;

    private JPanel panel;
    private JTabbedPane tabbedPane;
    private JButton saveButt;

    private JPanel generalPanel;
    private JSpinner opacity_sp;
    private JSpinner timeout_sp;
    private JCheckBox debug_checkb;
    private JCheckBox alwaysOnTop_checkb;

    private JPanel browserPanel;
    private JTextField browseBotTF;
    private JTextField browseCmdTF;
    private JTextField searchBotTF;
    private JTextField searchCmdTF;
    private JCheckBox externalBrowser_checkb;
    private JTextField externalBrowserTF;

    private JPanel mailPanel;
    private JComboBox<String> sslComb;
    private JComboBox<String> protocolComb;
    private JTextField hostTF;
    private JTextField smtpPortTF;
    private JTextField receiverPortTF;
    private JTextField emailTF;
    private JPasswordField passPF;
    private JTextField inboxTF;
    private JComboBox<String> receiverAction_comb;


    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================

    public SettingsForm (Main parent) {
        super(parent, true);
        this.parent = parent;
        setup();
        setTitle(R.getString("sett.title"));
        pack();
        setLocationRelativeTo(null);
    }

    //	================= END CONSTRUCTORS =======================

    //	===================== METHODS ============================

    private void setup() {
        pManager = PManager.getInstance();
        Notification.setSoundsStatus(true);
        setContentPane(get_panel());
    }

    private JPanel get_panel() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = 0; c.weightx = 1; c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;
            panel.add(get_tabbedPane(), c);
            c.gridy = 1; c.weighty = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.ipady = 5;
            panel.add(get_saveButt(), c);
        }
        return panel;
    }

    private JTabbedPane get_tabbedPane() {
        if (tabbedPane == null) {
            tabbedPane = new JTabbedPane();
            tabbedPane.addTab(R.getString("sett.general"), null, get_generalPanel());
            tabbedPane.addTab(R.getString("sett.browser"), null, get_browserPanel());
            tabbedPane.addTab(R.getString("sett.mail"), null, get_mailPanel());
        }
        return tabbedPane;
    }

    private JPanel get_generalPanel() {
        if (generalPanel == null) {
            generalPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.anchor = GridBagConstraints.LINE_START; c.gridx = 0;
            c.gridy = 0; 
            c.insets = new Insets(10, 5, 5, 5);
            generalPanel.add(new JLabel(R.getString("sett.transparency")), c);
            c.insets = new Insets(0, 5, 5, 5);//top, left, bott, right
            c.gridy = 1;
            generalPanel.add(new JLabel(R.getString("sett.timeout")), c);

            c.gridy = 2;
            generalPanel.add(get_alwaysOnTop_checkb(), c);
            c.gridx = 1;
            generalPanel.add(get_debug_checkb(), c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1; c.gridy = 0; c.weightx = 1;
            c.insets = new Insets(10, 5, 5, 5);
            generalPanel.add(get_opacity_sp(), c);
            c.insets = new Insets(0, 5, 5, 5);
            c.gridy = 1;
            generalPanel.add(get_timeout_sp(), c);
            
            c.gridy = 4; c.weighty = 1; c.gridwidth = 2;
            c.fill = GridBagConstraints.VERTICAL;
            generalPanel.add(Box.createVerticalGlue(), c);
        }
        return generalPanel;
    }

    private JSpinner get_opacity_sp() {
        if (opacity_sp == null) {
            opacity_sp = new JSpinner();
            opacity_sp.setModel(new SpinnerNumberModel(1, 0.5, 1, 0.01));
            opacity_sp.setValue((double)pManager.getOpacity());
        }
        return opacity_sp;
    }
    
    private JSpinner get_timeout_sp() {
        if (timeout_sp == null) {
            timeout_sp = new JSpinner();
            timeout_sp.setModel(new SpinnerNumberModel(1, -1, null, 1));
            timeout_sp.setValue(pManager.getTimeout()/1000);
        }
        return timeout_sp;
    }

    private JCheckBox get_alwaysOnTop_checkb() {
        if (alwaysOnTop_checkb == null) {
            alwaysOnTop_checkb = new JCheckBox(R.getString("sett.always-on-top"), pManager.getAlwaysOnTop());
        }
        return alwaysOnTop_checkb;
    }

    private JCheckBox get_debug_checkb() {
        if (debug_checkb == null) {
            debug_checkb = new JCheckBox(R.getString("sett.errors-log"), pManager.getDebug());
        }
        return debug_checkb;
    }

    private JPanel get_browserPanel() {
        if (browserPanel == null) {
            browserPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.anchor = GridBagConstraints.LINE_START;
            c.gridy = 0; c.gridx = 0; c.weightx = 0;
            c.insets = new Insets(10, 5, 5, 5);
            browserPanel.add(new JLabel(R.getString("sett.browse-bot")), c);
            c.insets = new Insets(0, 5, 5, 5);
            c.gridy = 1;
            browserPanel.add(new JLabel(R.getString("sett.browse-cmd")), c);
            c.gridy = 2;
            browserPanel.add(new JLabel(R.getString("sett.search-bot")), c);
            c.gridy = 3;
            browserPanel.add(new JLabel(R.getString("sett.search-cmd")), c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1; c.gridx = 1; c.gridwidth = 2;
            c.gridy = 0;
            c.insets = new Insets(10, 5, 5, 5);
            browserPanel.add(get_browseBotTF(), c);
            c.insets = new Insets(0, 5, 5, 5);
            c.gridy = 1;
            browserPanel.add(get_browseCmdTF(), c);
            c.gridy = 2;
            browserPanel.add(get_searchBotTF(), c);
            c.gridy = 3;
            browserPanel.add(get_searchCmdTF(), c);

            c.anchor = GridBagConstraints.LINE_START;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.gridy = 4; c.gridx = 0;
            browserPanel.add(get_externalBrowser_checkb(), c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = 1;
            c.gridy = 4; c.gridx = 2;
            browserPanel.add(get_externalBrowserTF(), c);

            c.gridy = 6; c.weighty = 1;
            c.fill = GridBagConstraints.VERTICAL;
            browserPanel.add(Box.createVerticalGlue(), c);
        }
        return browserPanel;
    }

    private JTextField get_browseBotTF() {
        if (browseBotTF == null) {
            browseBotTF = new JTextField(pManager.getBrowseBot());
        }
        return browseBotTF;
    }

    private JTextField get_browseCmdTF() {
        if (browseCmdTF == null) {
            browseCmdTF = new JTextField(pManager.getBrowseCmd());
        }
        return browseCmdTF;
    }

    private JTextField get_searchBotTF() {
        if (searchBotTF == null) {
            searchBotTF = new JTextField(pManager.getSearchBot());
        }
        return searchBotTF;
    }

    private JTextField get_searchCmdTF() {
        if (searchCmdTF == null) {
            searchCmdTF = new JTextField(pManager.getSearchCmd());
        }
        return searchCmdTF;
    }

    private JCheckBox get_externalBrowser_checkb() {
        if (externalBrowser_checkb == null) {
            externalBrowser_checkb = new JCheckBox(R.getString("sett.use-ext-browser"));
            externalBrowser_checkb.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent evt) {
                        if(externalBrowser_checkb.isSelected()) {
                            get_externalBrowserTF().setEnabled(true);
                        } else {
                            get_externalBrowserTF().setEnabled(false);
                        }
                    }
                });
            externalBrowser_checkb.setSelected(pManager.useExternalBrowser());
        }
        return externalBrowser_checkb;
    }

    private JTextField get_externalBrowserTF() {
        if (externalBrowserTF == null) {
            externalBrowserTF = new JTextField(pManager.getExternalBrowser());
            externalBrowserTF.setEnabled(pManager.useExternalBrowser());
        }
        return externalBrowserTF;
    }

    private JPanel get_mailPanel() {
        if (mailPanel == null) {
            mailPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.anchor = GridBagConstraints.LINE_START;
            c.gridx = 0; c.gridy = 0;
            c.insets = new Insets(10, 5, 5, 5);
            mailPanel.add(new JLabel(R.getString("sett.ssl")), c);
            c.insets = new Insets(0, 5, 5, 5);
            c.gridy = 1;
            mailPanel.add(new JLabel(R.getString("sett.protocol")), c);
            c.gridy = 2;
            mailPanel.add(new JLabel(R.getString("sett.host")), c);
            c.gridy = 3;
            mailPanel.add(new JLabel(R.getString("sett.smtp-port")), c);
            c.gridy = 4;
            mailPanel.add(new JLabel(R.getString("sett.receiver-port")), c);
            c.gridy = 5;
            mailPanel.add(new JLabel(R.getString("sett.email")), c);
            c.gridy = 6;
            mailPanel.add(new JLabel(R.getString("sett.pass")), c);
            c.gridy = 7;
            mailPanel.add(new JLabel(R.getString("sett.folder")), c);
            c.gridy = 8;
            mailPanel.add(new JLabel(R.getString("sett.action")), c);

            c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
            c.gridx = 1; c.gridy = 0;
            c.insets = new Insets(10, 5, 5, 5);
            mailPanel.add(get_sslComb(), c);
            c.insets = new Insets(0, 5, 5, 5);
            c.gridy = 1;
            mailPanel.add(get_protocolComb(), c);
            c.gridy = 2;
            mailPanel.add(get_hostTF(), c);
            c.gridy = 3;
            mailPanel.add(get_smtpPortTF(), c);
            c.gridy = 4;
            mailPanel.add(get_receiverPortTF(), c);
            c.gridy = 5;
            mailPanel.add(get_emailTF(), c);
            c.gridy = 6;
            mailPanel.add(get_passPF(), c);
            c.gridy = 7;
            mailPanel.add(get_inboxTF(), c);
            c.gridy = 8;
            mailPanel.add(get_receiverAction_comb(), c);

            c.gridy = 9; c.weighty = 1;
            c.fill = GridBagConstraints.VERTICAL;
            mailPanel.add(Box.createVerticalGlue(), c);
        }
        return mailPanel;
    }

    private JComboBox<String> get_sslComb() {
        if (sslComb == null) {
            String[] options = {
                R.getString("sett.cbox.ssl"),
                R.getString("sett.cbox.tls"),
                R.getString("sett.cbox.none")
            };
            sslComb = new JComboBox<String>(options);
            if(pManager.getSSL()) {
                sslComb.setSelectedItem(R.getString("sett.cbox.ssl"));
            } else if(pManager.getStartTLS()) {
                sslComb.setSelectedItem(R.getString("sett.cbox.tls"));
            } else {
                sslComb.setSelectedItem(R.getString("sett.cbox.none"));
            }
        }
        return sslComb;
    }

    private JComboBox<String> get_protocolComb() {
        if (protocolComb == null) {
            String[] options = {R.getString("sett.cbox.imap"), R.getString("sett.cbox.pop3")};
            protocolComb = new JComboBox<String>(options);
            protocolComb.setSelectedItem(pManager.getReceiverProtocol().toUpperCase());
        }
        return protocolComb;
    }

    private JTextField get_hostTF() {
        if (hostTF == null) {
            hostTF = new JTextField(pManager.getHost());
        }
        return hostTF;
    }

    private JTextField get_smtpPortTF() {
        if (smtpPortTF == null) {
            smtpPortTF = new JTextField(pManager.getSenderPort());
        }
        return smtpPortTF;
    }

    private JTextField get_receiverPortTF() {
        if (receiverPortTF == null) {
            receiverPortTF = new JTextField(pManager.getReceiverPort());
        }
        return receiverPortTF;
    }

    private JTextField get_emailTF() {
        if (emailTF == null) {
            emailTF = new JTextField(pManager.getEmail());
        }
        return emailTF;
    }

    private JPasswordField get_passPF() {
        if (passPF == null) {
            passPF = new JPasswordField(pManager.getPassword());
        }
        return passPF;
    }

    private JTextField get_inboxTF() {
        if (inboxTF == null) {
            inboxTF = new JTextField(pManager.getReceiverINBOX());
        }
        return inboxTF;
    }

    private JComboBox<String> get_receiverAction_comb() {
        if(receiverAction_comb == null) {
            String[] options = {
                R.getString("sett.cbox.mark-as-read"),
                R.getString("sett.cbox.delete")
            };
            receiverAction_comb = new JComboBox<String>(options);
            int action = pManager.getReceiverAction();
            receiverAction_comb.setSelectedIndex(action);
        }
        return receiverAction_comb;
    }

    private JButton get_saveButt() {
        if(saveButt == null) {
            saveButt = new JButton(R.getString("sett.save"));
            saveButt.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ev) {
                        float op = ((Double)get_opacity_sp().getValue()).floatValue();
                        pManager.setOpacity(op);
                        Main.getInstance().updateOpacity();
                        int sec = (Integer)get_timeout_sp().getValue();
                        pManager.setTimeout(sec*1000);
                        boolean stat = get_debug_checkb().isSelected();
                        pManager.setDebug(stat);
                        stat = get_alwaysOnTop_checkb().isSelected();
                        pManager.setAlwaysOnTop(stat);
                        Main.getInstance().setAlwaysOnTop(stat);
                        
                        String browseBot = browseBotTF.getText().trim();
                        String browseCmd = browseCmdTF.getText();
                        String searchBot = searchBotTF.getText().trim();
                        String searchCmd = searchCmdTF.getText();
                        boolean useExtBrowser = externalBrowser_checkb.isSelected();
                        String extBrowser = externalBrowserTF.getText();
                        pManager.setBrowseBot(browseBot);
                        pManager.setBrowseCmd(browseCmd);
                        pManager.setSearchBot(searchBot);
                        pManager.setSearchCmd(searchCmd);
                        pManager.useExternalBrowser(useExtBrowser);
                        pManager.setExternalBrowser(extBrowser);

                        String sslStr = (String)sslComb.getSelectedItem();
                        boolean ssl = sslStr.equals(R.getString("sett.cbox.ssl"));
                        boolean starttls = sslStr.equals(R.getString("sett.cbox.tls"));
                        String protocol = ((String)protocolComb.getSelectedItem()).toLowerCase();
                        String host = hostTF.getText().trim();
                        String smtpPort = smtpPortTF.getText().trim();
                        String recPort = receiverPortTF.getText().trim();
                        String email = emailTF.getText().trim();
                        String pass = new String(passPF.getPassword());
                        String inbox = inboxTF.getText().trim();
                        int action = receiverAction_comb.getSelectedIndex();

                        pManager.setStartTLS(starttls);
                        pManager.setSSL(ssl);
                        pManager.setReceiverProtocol(protocol);
                        pManager.setHost(host);
                        pManager.setSenderPort(smtpPort);
                        pManager.setReceiverPort(recPort);
                        pManager.setEmail(email);
                        pManager.setPassword(pass);
                        pManager.setReceiverINBOX(inbox);
                        pManager.setReceiverAction(action);
                        
                        pManager.save_Config();
                        Notification.show(R.getString("sett.msg"),
                                          R.getString("sett.sett-saved"),
                                          3000);
                        SettingsForm.this.setVisible(false);
                    }
                });
        }
        return saveButt;
    }

    //	====================== END METHODS =======================

}
