package it.nm.sparkplugha.monitor;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;

public class SparkPlugMonitorApplication {

    private JFrame frame;
    private JTree tree;
    private JTextArea textArea;

    /**
     * Create the application.
     */
    public SparkPlugMonitorApplication() {

	initialize();

    }

    public JTree getTree() {

	return tree;

    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {

	frame = new JFrame();
	frame.setSize(new Dimension(600, 400));
	frame.setPreferredSize(new Dimension(800, 600));
	frame.setBounds(100, 100, 610, 485);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	frame.setTitle("Primary Application Demo");

	frame.setVisible(true);

	JSplitPane splitPane = new JSplitPane();
	frame.getContentPane().add(splitPane, BorderLayout.CENTER);

	JScrollPane scrollPane_2 = new JScrollPane();
	splitPane.setRightComponent(scrollPane_2);

	tree = new JTree();
	tree.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
	splitPane.setLeftComponent(tree);
	splitPane.setDividerLocation(400);

	 textArea = new JTextArea();
	textArea.setBorder(new TitledBorder(null, "Event Log", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	textArea.setPreferredSize(new Dimension(1, 100));
	frame.getContentPane().add(textArea, BorderLayout.SOUTH);

    }

    public void log(String string) {

	textArea.append(string + "\n");

    }

}
