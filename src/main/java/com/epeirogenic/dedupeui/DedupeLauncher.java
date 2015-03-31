package com.epeirogenic.dedupeui;

import com.epeirogenic.dedupe.Checksum;
import com.epeirogenic.dedupe.FileRecurse;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class DedupeLauncher extends JDialog {
    
    private File startDirectory;
    private FileRecurse fileRecurse;
    private Properties properties;
    private Map<String, Set<File>> checksumMap;
    private DedupeWorker worker;

    private void onBrowse() {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose start directory");
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home", "/")));

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            System.out.println("getCurrentDirectory(): " + fileChooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : " + fileChooser.getSelectedFile());

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
            e.printStackTrace();
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
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.setEnabled(false);
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onBrowse();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        mainDialog.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        readProperties("dupree.properties");

        checksumMap = new HashMap<String, Set<File>>();
        worker = new DedupeWorker();
    }

    private void readProperties(final String filename) {

        properties = new Properties();

        Resource resource = new ClassPathResource(filename);

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
            fileRecurse = new FileRecurse(Checksum.SHA256, new DedupeUICallback(worker, pathField));
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

    class DirectoryFilter extends FileFilter {

        @Override
        public boolean accept(File file) {
            return file != null && file.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Directory filter";
        }
    }

    class DedupeUICallback implements FileRecurse.Callback {

        private final DedupeWorker dw;

        public DedupeUICallback(final DedupeWorker dedupeWorker, final JTextField currentFileField) {
            this.dw = dedupeWorker;
        }

        @Override
        public void currentFile(final File file) {

            try {
                final String canonicalPath = file.getCanonicalPath();
                final String abbreviatedPath = StringUtils.abbreviateMiddle(canonicalPath, "...", 80);
                dw.publish(abbreviatedPath);

            } catch(final IOException ioe) {
                // swallow?
                System.err.println(ioe);
            }
        }

        @Override
        public void currentDirectory(final File directory) {
            System.out.println(directory.getAbsolutePath());
        }
    }

    public static void main(final String[] args) {
        final DedupeLauncher dialog = new DedupeLauncher();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private JPanel mainDialog;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton chooseButton;
    private JScrollPane infoPanel;
    private JTextField pathField;
    private JTextField startDirectoryField;
}
