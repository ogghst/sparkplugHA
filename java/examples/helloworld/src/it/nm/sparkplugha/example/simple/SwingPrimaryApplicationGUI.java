package it.nm.sparkplugha.example.simple;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import it.nm.sparkplugha.events.SPHAEvent;
import it.nm.sparkplugha.events.SPHAEventListener;
import it.nm.sparkplugha.events.SPHAEventManager;
import it.nm.sparkplugha.events.SPHAMQTTConnectEvent;
import it.nm.sparkplugha.events.SPHANodeBirthEvent;
import it.nm.sparkplugha.mqtt.MQTTSPHAPrimaryApplication;

public class SwingPrimaryApplicationGUI extends JFrame {

    private static final long serialVersionUID = -4623442886076179985L;

    private final static Logger LOGGER = Logger.getLogger(SwingPrimaryApplicationGUI.class.getName());

    private JTree tree;
    private JLabel selectedLabel;
    SPHAEventManager evtMgr;

    public SwingPrimaryApplicationGUI(SPHAEventManager evtMgr) {

	this.evtMgr = evtMgr;

	// create the root node
	DefaultMutableTreeNode root = new DefaultMutableTreeNode("SpHA Network");

	// create the tree by passing in the root node
	tree = new JTree(root);
	ImageIcon imageIcon = new ImageIcon(SwingPrimaryApplicationGUI.class.getResource("cloud.png"));
	DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
	renderer.setLeafIcon(imageIcon);

	tree.setCellRenderer(renderer);
	tree.setShowsRootHandles(true);
	tree.setRootVisible(false);
	add(new JScrollPane(tree));

	selectedLabel = new JLabel();
	add(selectedLabel, BorderLayout.SOUTH);
	tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

	    @Override
	    public void valueChanged(TreeSelectionEvent e) {

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		selectedLabel.setText(selectedNode.getUserObject().toString());

	    }

	});

	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.setTitle("Primary Application Demo");
	this.setSize(600, 600);
	this.setVisible(true);

	evtMgr.subscribe(SPHAMQTTConnectEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		SPHAMQTTConnectEvent me = (SPHAMQTTConnectEvent) event;
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(me.getClient().getClientId());

		addRootNode(node);

	    }

	});

	evtMgr.subscribe(SPHANodeBirthEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		SPHANodeBirthEvent me = (SPHANodeBirthEvent) event;
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(
			me.getNodeDesc().getGroupId() + "\\" + me.getNodeDesc().getGroupId());

		addNode(node);

	    }

	});

    }

    private void addRootNode(DefaultMutableTreeNode node) {

	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {

		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		root.add(node);
		model.reload(root);

	    }

	});

    }

    private void addNode(DefaultMutableTreeNode node) {

	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {

		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(0);
		if (child == null)
		    return;
		child.add(node);
		model.reload(root);

	    }

	});

    }

}
