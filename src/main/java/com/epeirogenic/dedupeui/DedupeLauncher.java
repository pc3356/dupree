package com.epeirogenic.dedupeui;

import com.epeirogenic.dedupe.Checksum;
import com.epeirogenic.dedupe.FileRecurse;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class DedupeLauncher extends JDialog {
    
    private File startDirectory;
    private FileRecurse fileRecurse;
    private Properties properties;
    private Map<String, Set<File>> checksumMap;

    private void onBrowse() {
        JFileChooser fileChooser = new JFileChooser();
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
        fileRecurse = new FileRecurse(Checksum.SHA256, new DedupeUICallback(pathField));
        fileRecurse.iterate(startDirectory, checksumMap);
        //dispose();
    }

    private void onCancel() {
        dispose();
    }

    public DedupeLauncher() {
        setContentPane(mainDialog);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Dupree");

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

        //readProperties(getClass().getResourceAsStream("dupree.properties"));

        checksumMap = new HashMap<String, Set<File>>();
    }

    private void readProperties(InputStream propertiesStream) {

        properties = new Properties();
        try {
            properties.load(propertiesStream);
            properties.list(System.out);
        } catch(IOException ioe) {
            System.err.println("Unable to load properties");
            System.exit(-1);
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

        private final JTextField currentFileField;

        public DedupeUICallback(JTextField currentFileField) {
            this.currentFileField = currentFileField;
        }

        @Override
        public void currentFile(File file) {

            try {
                String canonicalPath = file.getCanonicalPath();
                String abbreviatedPath = StringUtils.abbreviateMiddle(canonicalPath, "...", 30);

                currentFileField.setText(abbreviatedPath);

            } catch(IOException ioe) {
                // swallow?
                System.err.println(ioe);
            }
        }

        @Override
        public void currentDirectory(File directory) {
            System.out.println(directory.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        DedupeLauncher dialog = new DedupeLauncher();
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
