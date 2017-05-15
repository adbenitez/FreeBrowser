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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.text.MessageFormat;
import java.util.LinkedList;

import javax.activation.DataHandler;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.net.ssl.internal.ssl.Provider;

import controller.event.MailEvent;
import controller.event.MailListener;
import controller.exception.InvalidFolderException;

public class MailManager {
    
    //	================= ATTRIBUTES ==============================
    
    private static MailManager mManager;

    private PManager pManager;
    private SMTPTransport transport;
    private Session senderSession;

    private Session receiverSession;
    private Store store;
    private Folder folder;
    private LinkedList<MailListener> mailListeners;

    private Thread receiverThr;
    
    //	================= END ATTRIBUTES ==========================
    
    //	================= CONSTRUCTORS ===========================
    
    private MailManager () {
        pManager = PManager.getInstance();
        mailListeners = new LinkedList<MailListener>();
        Security.addProvider(new Provider());
    }
    
    //	================= END CONSTRUCTORS =======================
    
    //	===================== METHODS ============================

    public void addMailListener(MailListener ml) {
        mailListeners.add(ml);
    }

    public void notifyMailListeners(String attachment) {
        MailEvent evt = new MailEvent(this, attachment);
        for (MailListener ml : mailListeners) {
            ml.newMailReceived(evt);
        }
    }

    public void notifyErrorListeners(Exception e) {
        MailEvent evt = new MailEvent(this, e);
        for (MailListener ml : mailListeners) {
            ml.errorOccurred(evt);
        }
    }
    
    public void startReceiver() {
        stopReceiver();
        receiverThr = new Thread() {
                public void run() {
                    while (true) {
                        try {
                            receiveMails();
                        } catch (MessagingException e) {
                            pManager.writeLog(e);
                            try {
                                receiverConnect();
                            } catch (MessagingException ex) {
                                pManager.writeLog(ex);
                            }
                        } catch (IOException e) {
                            notifyErrorListeners(e);
                        } catch (InvalidFolderException e) {
                            notifyErrorListeners(e);
                        }
                        
                        try {
                            sleep(10000);
                        } catch (InterruptedException e) {/* ignore */}
                    }
                }
            };
        receiverThr.start();
    }

    @SuppressWarnings("deprecation")
    public void stopReceiver() {
        if (receiverThr != null) {
            receiverThr.stop();
            receiverThr = null;
        }
    }
    
    public void sendMail(String to, String subj, String body) throws MessagingException {
        if(transport == null || !transport.isConnected()) {
            senderConnect();
        }
        MimeMessage message = new MimeMessage(senderSession);
        message.setFrom(new InternetAddress(pManager.getSenderFrom()));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subj);
        message.setText(body);
        transport.sendMessage(message, message.getAllRecipients());
    }
    
    public void senderConnect() throws NoSuchProviderException, MessagingException {
        String msg = MessageFormat.format(R.getString("mm.connecting"), "SMTP");
        pManager.writeLog(msg);
        if (transport != null) {
            try {
                transport.close();
            } catch (MessagingException e) { /* ignore */ }
        }
        senderSession = Session.getInstance(pManager.getSenderProps());
        transport = (SMTPTransport)(senderSession.getTransport("smtp"));
        transport.connect(pManager.getSenderUser(), pManager.getSenderPass());
        msg = MessageFormat.format(R.getString("mm.connected"), "SMTP");
        pManager.writeLog(msg);
    }

    public void receiverConnect() throws NoSuchProviderException, MessagingException {
        String prot = pManager.getReceiverProtocol().toUpperCase();
        String msg = MessageFormat.format(R.getString("mm.connecting"), prot);
        pManager.writeLog(msg);
        
        if (store != null) {
            try {
                store.close();
            } catch (MessagingException e) { /* ignore */ }
        }
        receiverSession = Session.getInstance(pManager.getReceiverProps());
        store = receiverSession.getStore(pManager.getReceiverProtocol());
        store.connect(pManager.getReceiverUser(), pManager.getReceiverPass());
        msg = MessageFormat.format(R.getString("mm.connected"), prot);
        pManager.writeLog(msg);
    }
    
    public synchronized void receiveMails() throws MessagingException, IOException, InvalidFolderException {
        if (store == null || !store.isConnected()) {
            receiverConnect();
        }
        String folderPath = pManager.getReceiverINBOX();
        Folder folder = store.getFolder(folderPath);
        if (!folder.exists()) {
            String msg = MessageFormat.format(R.getString("mm.inv-folder"), folderPath);
            throw new InvalidFolderException(msg);
        }
        folder.open(Folder.READ_WRITE);
        SearchTerm term = new FromStringTerm(pManager.getBrowseBot());
        FromStringTerm sBot = new FromStringTerm(pManager.getSearchBot());
        FlagTerm flagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        term = new OrTerm(term, sBot);
        term = new AndTerm(flagTerm, term);
        Message[] msgs = folder.search(term);
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.CONTENT_INFO);
        folder.fetch(msgs, fp);
        if (msgs.length > 0) {
        
        } else {
            pManager.writeLog(R.getString("mm.no-new-msg"));
        }
        int i = 1;
        int len = msgs.length;
        for(Message m : msgs) {
            String msg = MessageFormat.format(R.getString("mm.receiving"), i++, len);
            pManager.writeLog(msg);
            String attachPath = receiveMultipartMessage(m, pManager.getDownloadsPath());
            if (attachPath != null) {
                notifyMailListeners(attachPath);
            } else {
                pManager.writeLog(R.getString("mm.null-attach"));
            }
        }
        try {
            folder.close(true);
        } catch(IllegalStateException ex) {
            pManager.writeLog(ex);
        } catch (MessagingException ex) {
            pManager.writeLog(ex);
        }
    }
    
    private String receiveMultipartMessage(Message m, String dir)
        throws IOException, MessagingException {
        File directory = new File(dir);
        File attach = null;
        String subj = m.getSubject();
        if (subj.startsWith("Re: webpage ")) {
            subj = subj.substring(12, subj.length());
        }
        subj = subj.replace("/", "_").replace(":", "_").replace("?", "_").replace("*", "_").replace("\\", "_");
        int len = subj.length();
        if (len > 100) {
            len = 100;
        }
        subj = subj.substring(0, len);
        
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (folder == null || !folder.equals(m.getFolder())) {
            folder = m.getFolder();
        }
        if (!folder.isOpen()) {
            folder.open(Folder.READ_WRITE);
        }
        
        Object o = m.getContent();
        if (o instanceof Multipart) {
            Multipart mp = (Multipart) o;
            int numPart = mp.getCount();
            boolean isZip = m.getContentType().indexOf("application/zip") >= 0;
            for (int i = 0; i < numPart; i++) {
                Part part = mp.getBodyPart(i);
                String disposition = part.getDisposition();
                if (disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
                    attach = download(part, dir, subj, isZip);
                }
            }
        } else if (o instanceof String) {
            attach = new File(dir, subj+".html");
            PrintStream ps = new PrintStream(attach);    
            String msg = MessageFormat.format(R.getString("mm.downloading"), subj);
            pManager.writeLog(msg);
        
            if (m.getContentType().indexOf("text/html") >= 0) {
                DataHandler dh = m.getDataHandler();
                dh.writeTo(ps);
            } else {
                ps.println("<head><title>" + subj + "</title></head>");
                ps.println("<body>");
                ps.print(((String)o).replace("\n", "<br/>"));
                ps.println("</body>");
            }
            ps.close();
            
            pManager.writeLog(R.getString("mm.downloaded"));
        } else if (o instanceof BASE64DecoderStream) {
            boolean isZip = m.getFileName().endsWith(".zip");
            attach = download(o, dir, subj, isZip);
        } else {
            String msg = MessageFormat.format(R.getString("mm.unknow-type"), o.getClass().toString());
            pManager.writeLog(msg);
        }
        
        switch (pManager.getReceiverAction()) {
        case 0: //"mark as read"
            m.setFlag(Flags.Flag.SEEN, true);
            break;
        default: // 1: "delete it"
            m.setFlag(Flags.Flag.DELETED, true);
            break;
        }
        return attach != null? attach.getAbsolutePath() : null;
    }

    private File download(Object attObj, String dir, String attName, boolean isZip) throws IOException, MessagingException {
        String attachPath = Paths.get(dir, "attach").toString();
        File attach = File.createTempFile("att", ".tmp");
        String msg = MessageFormat.format(R.getString("mm.downloading"), attName);
        pManager.writeLog(msg);
        
        if(attObj instanceof MimeBodyPart) {
            ((MimeBodyPart) attObj).saveFile(attach);  // save in the temp file
        } else if (attObj instanceof BASE64DecoderStream) {
            InputStream base64_is = (InputStream) attObj;
            PrintStream ps = new PrintStream(attach);    
            int b;
            while ((b = base64_is.read()) >= 0) {
                ps.write(b);
            }
            base64_is.close();
            ps.close();
        }
        pManager.writeLog(R.getString("mm.downloaded"));
        
        if (isZip) {
            try {
                pManager.writeLog(R.getString("mm.uncompressing"));
            
                ZipFile zipFile = new ZipFile(attach);
                zipFile.extractAll(attachPath);
                attach.delete();
                attach = new File(attachPath);
            
                pManager.writeLog(R.getString("mm.uncompressed"));
            } catch (ZipException e) {
                pManager.writeLog(e);
            }
        }
    
        if (new File(dir, attName).exists()) { // if the folder exists
            for (int i = 2; i < Integer.MAX_VALUE; i++) {
                String name = attName + "_" + i;
                if (!new File(dir, name).exists()) {
                    attName = name;
                    break;
                }
            }
        }
        attach = new File(Files.move(attach.toPath(), Paths.get(dir, attName)).toString());
        if (attach.isDirectory()) {
            File f = new File(attach.getAbsolutePath(), "index.html");
            if (f.exists()) {
                attach = f;
            }
        }

        return attach;
    }
    
    public static MailManager getInstance() {
        if (mManager == null) {
            mManager = new MailManager();
        }
        return mManager;
    }
    
    //	====================== END METHODS =======================
    
}
