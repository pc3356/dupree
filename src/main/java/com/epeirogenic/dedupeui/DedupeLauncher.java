package com.epeirogenic.dedupeui;

import com.epeirogenic.dedupe.Checksum;
import com.epeirogenic.dedupe.FileRecurse;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import javax.swing.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@Setter
public class DedupeLauncher extends JDialog {

    public final static String DEFAULT_PROPERTIES_FILESNAME = "dupree.properties";

    private File startDirectory;
    private final Map<String, Set<File>> checksumMap;
    private final DedupeWorker worker;
    private String propertiesFilename;

    private void onBrowse() {
        var fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose start directory");
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home", "/")));

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

//            log.info("getCurrentDirectory(): {}", fileChooser.getCurrentDirectory());
//            log.info("getSelectedFile() : {}", fileChooser.getSelectedFile());

            startDirectory = fileChooser.getSelectedFile();
            buttonOK.setEnabled(true);
            buttonCancel.setEnabled(true);

            startDirectoryField.setText(startDirectory.getAbsolutePath());

        } else {

            System.out.println("No Selection ");

        }
    }

    private void onOK() {

        // start progress with callbacks
        try {
            worker.doInBackground();
        } catch (Exception e) {
//            log.error("Exception encountered: {}", e.getMessage());
//            if (log.isDebugEnabled()) {
//                for ( var m : e.getStackTrace()) {
//                    logStackTraceElement(m);
//                }
//            }
        }
        //dispose();
    }

    private void onCancel() {

        // terminate any running processes
        worker.cancel(true);
        dispose();
    }

    public DedupeLauncher() {
        setContentPane(mainDialog);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Dupree");
        setResizable(false);

        buttonOK.setEnabled(false);
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.setEnabled(false);
        buttonCancel.addActionListener(e -> onCancel());

        chooseButton.addActionListener(actionEvent -> onBrowse());

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        mainDialog.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        readProperties(getPropertiesFilename());

        checksumMap = new HashMap<>();
        worker = new DedupeWorker();
    }

    private void readProperties(final String filename) {

        var properties = new Properties();

        var resource = new ClassPathResource(filename);

        try {
            properties.load(resource.getInputStream());
            properties.list(System.out);
        } catch(IOException ioe) {
            System.err.println("Unable to load properties");
            System.exit(-1);
        }
    }

    class DedupeWorker extends SwingWorker<Void, String> {

        @Override
        protected Void doInBackground() throws Exception {
            var fileRecurse = new FileRecurse(Checksum.SHA256, new DedupeUICallback(worker, pathField));
            fileRecurse.iterate(startDirectory, checksumMap);
            return null;
        }

        @Override
        protected void process(final List<String> messages) {
            pathField.setText(messages.get(messages.size() - 1));
        }

        public void publish(final String message) {
            super.publish(message);
        }

        @Override
        protected void done() {
            super.done();
        }
    }

//    static class DirectoryFilter extends FileFilter {
//
//        @Override
//        public boolean accept(final File file) {
//            return file != null && file.isDirectory();
//        }
//
//        @Override
//        public String getDescription() {
//            return "Directory filter";
//        }
//    }

    class DedupeUICallback implements FileRecurse.Callback {

        private final DedupeWorker dw;

        public DedupeUICallback(final DedupeWorker dedupeWorker, final JTextField currentFileField) {
            this.dw = dedupeWorker;
//            log.info("Created callback");
        }

        @Override
        public void currentFile(final File file) {

            try {
                final String canonicalPath = file.getCanonicalPath();
                final String abbreviatedPath = StringUtils.abbreviateMiddle(canonicalPath, "...", 80);
                dw.publish(abbreviatedPath);

            } catch(final IOException ioe) {
                // swallow?
//                log.error("Error reading file: {}", ioe.getMessage());
//                if (log.isDebugEnabled()) {
//                    for (var m : ioe.getStackTrace()) {
//                        logStackTraceElement(m);
//                    }
//                }
            }
        }

        @Override
        public void currentDirectory(final File directory) {
            System.out.println(directory.getAbsolutePath());
        }
    }

    private String getPropertiesFilename() {
        return Objects.requireNonNullElse(propertiesFilename, DEFAULT_PROPERTIES_FILESNAME);
    }

//    public static void main(final String[] args) {
//        final DedupeLauncher dialog = new DedupeLauncher();
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
//    }

//    private void logStackTraceElement(final StackTraceElement e) {
//        log.debug("{} : {} : {} : {}", e.getClassName(), e.getMethodName(), e.getFileName(), e.getLineNumber());
//    }

    private JPanel mainDialog;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton chooseButton;
//    private JScrollPane infoPanel;
    private JTextField pathField;
    private JTextField startDirectoryField;
}
