package com.epeirogenic.dedupeui;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.*;
import java.io.File;

public class DedupeLauncher extends JDialog {
    
    private File startDirectory;

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

        } else {

            System.out.println("No Selection ");

        }

    }

    private void onOK() {



        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public DedupeLauncher() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Duplicity");

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
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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

    public static void main(String[] args) {
        DedupeLauncher dialog = new DedupeLauncher();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JButton chooseButton;
    private JProgressBar progressBar1;
    private JScrollPane infoPanel;
}
