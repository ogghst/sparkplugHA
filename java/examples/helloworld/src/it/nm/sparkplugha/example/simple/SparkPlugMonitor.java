package it.nm.sparkplugha.example.simple;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.tree.TreePath;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.tahu.message.model.Metric;

import it.nm.sparkplugha.events.SPHAEvent;
import it.nm.sparkplugha.events.SPHAEventListener;
import it.nm.sparkplugha.events.SPHAEventManager;
import it.nm.sparkplugha.events.SPHAMQTTConnectEvent;
import it.nm.sparkplugha.events.SPHANodeBirthEvent;
import it.nm.sparkplugha.events.SPHANodeDataEvent;
import it.nm.sparkplugha.events.SPHANodeDeathEvent;
import it.nm.sparkplugha.events.SPHANodeEvent;
import it.nm.sparkplugha.events.SPHANodeOutOfSequenceEvent;
import it.nm.sparkplugha.model.SPHAEdgeNode;
import it.nm.sparkplugha.model.SPHAEdgeNode.SPHANodeState;

public class SparkPlugMonitor extends JFrame {

    private static final long serialVersionUID = -4623442886076179985L;

    private final static Logger LOGGER = Logger.getLogger(SparkPlugMonitor.class.getName());

    private JTree tree;
    private JLabel selectedLabel;
    SPHAEventManager evtMgr;

    private DefaultTreeModel model;

    public SparkPlugMonitor(SPHAEventManager evtMgr) {

	this.evtMgr = evtMgr;

	// create the root node
	DefaultMutableTreeNode root = new DefaultMutableTreeNode("SpHA Network");

	// create the tree by passing in the root node
	tree = new JTree(root);

	model = (DefaultTreeModel) tree.getModel();

	SpTreeCellRendered renderer = new SpTreeCellRendered();

	tree.setRootVisible(true);
	tree.setCellRenderer(renderer);
	tree.setShowsRootHandles(true);

	add(new JScrollPane(tree));

	selectedLabel = new JLabel();
	add(selectedLabel, BorderLayout.SOUTH);
	tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

	    @Override
	    public void valueChanged(TreeSelectionEvent e) {

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (selectedNode != null)
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
		updateTree(new MQTTClientTreeNode(me.getClient()), (DefaultMutableTreeNode) model.getRoot());

	    }

	});

	evtMgr.subscribe(SPHANodeBirthEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		SPHANodeEvent me = (SPHANodeEvent) event;
		updateTree(me.getNode());

	    }

	});

	evtMgr.subscribe(SPHANodeDataEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		SPHANodeDataEvent me = (SPHANodeDataEvent) event;
		updateTree(me.getNode());

	    }

	});

	evtMgr.subscribe(SPHANodeDeathEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		SPHANodeDataEvent me = (SPHANodeDataEvent) event;
		updateTree(me.getNode());

	    }

	});

	evtMgr.subscribe(SPHANodeOutOfSequenceEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		SPHANodeOutOfSequenceEvent me = (SPHANodeOutOfSequenceEvent) event;
		updateTree(me.getNode());

	    }

	});

    }

    private class SpTreeCellRendered extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -7148725668293483029L;

	ImageIcon hub = new ImageIcon(SparkPlugMonitor.class.getResource("hub.png"));
	ImageIcon cloud = new ImageIcon(SparkPlugMonitor.class.getResource("cloud.png"));
	ImageIcon cloud_off = new ImageIcon(SparkPlugMonitor.class.getResource("cloud_off.png"));

	public SpTreeCellRendered() {

	    super();

	    setOpenIcon(hub);
	    setClosedIcon(hub);
	    setLeafIcon(cloud);
	    setDisabledIcon(cloud_off);

	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
		boolean leaf, int row, boolean hasFocus) {

	    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

	    if (value instanceof SpBEONTreeNode) {

		SpBEONTreeNode node = (SpBEONTreeNode) value;

		if (node.getNode().getState().equals(SPHANodeState.ONLINE)) {

		    //LOGGER.fine("*** NODE ONLINE: " + node.getNode().getDescriptorString());
		    setIcon(cloud);

		} else if (node.getNode().getState().equals(SPHANodeState.OFFLINE)) {

		    //LOGGER.fine("*** NODE OFFLINE: " + node.getNode().getDescriptorString());
		    setIcon(cloud_off);

		}

	    }

	    return this;

	}

    }

    MQTTClientTreeNode findMQTTClientTreeNode(MqttClient client) {

	DefaultMutableTreeNode root = ((DefaultMutableTreeNode) model.getRoot());
	int rootChild = root.getChildCount();

	for (int i = 0; i < rootChild; i++) {

	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);

	    if (node instanceof MQTTClientTreeNode) {

		if (((MQTTClientTreeNode) node).getClient().equals(client)) {

		    return ((MQTTClientTreeNode) node);

		}

	    }

	}

	return null;

    }

    SpBGroupIDTreeNode findSpBGroupIDTreeNode(SPHAEdgeNode descriptor, DefaultMutableTreeNode parent) {

	int childs = parent.getChildCount();

	for (int i = 0; i < childs; i++) {

	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(i);

	    SpBGroupIDTreeNode deepSearch = findSpBGroupIDTreeNode(descriptor, node);
	    if (deepSearch != null)
		return deepSearch;

	    if (node instanceof SpBGroupIDTreeNode) {

		if (((SpBGroupIDTreeNode) node).getNode().equals(descriptor)) {

		    return ((SpBGroupIDTreeNode) node);

		}

	    }

	}

	return null;

    }

    SpBEONTreeNode findSpBEONTreeNode(SPHAEdgeNode descriptor, DefaultMutableTreeNode parent) {

	int childs = parent.getChildCount();

	for (int i = 0; i < childs; i++) {

	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(i);

	    SpBEONTreeNode deepSearch = findSpBEONTreeNode(descriptor, node);
	    if (deepSearch != null)
		return deepSearch;

	    if (node instanceof SpBEONTreeNode) {

		if (((SpBEONTreeNode) node).getNode().equals(descriptor)) {

		    return ((SpBEONTreeNode) node);

		}

	    }

	}

	return null;

    }

    private void updateTree(DefaultMutableTreeNode node, DefaultMutableTreeNode parent) {

	// LOGGER.fine(" *** updateTree(" + node + "," + parent + ") - parent childs: "
	// + parent.getChildCount());

	if (parent == null || node == null)
	    return;

	int present = -1;

	for (int i = 0; i < parent.getChildCount(); i++) {

	    // LOGGER.fine(" *** comparing " + parent.getChildAt(i) + " with " + node + " :
	    // "
	    // + parent.getChildAt(i).equals(node));

	    if (parent.getChildAt(i).equals(node)) {

		present = i;
		break;

	    }

	}

	if (present >= 0) {

	    DefaultMutableTreeNode child = ((DefaultMutableTreeNode) parent.getChildAt(present));
	    //LOGGER.fine(" *** UPDATING " + node + " in " + child + " *** " + child.getClass().getCanonicalName());
	    child.setUserObject(node.getUserObject());
	    model.nodeChanged(parent);
	    model.nodeChanged(child);
	    tree.scrollPathToVisible(new TreePath(child.getPath()));

	} else {

	    //LOGGER.fine(" *** ADDING " + node + " below " + parent + " *** " + node.getClass().getCanonicalName());
	    model.insertNodeInto(node, parent, parent.getChildCount());
	    //model.nodeChanged(parent);
	    //model.nodeChanged(node);
	    tree.scrollPathToVisible(new TreePath(node.getPath()));
	}

	

    }

    /*
     * private void updateTree(SpBEONTreeNode node) {
     * 
     * if (node == null) return; SpBEONTreeNode _n =
     * findSpBEONTreeNode(node.getNode(), (DefaultMutableTreeNode) model.getRoot());
     * if (_n == null) return; _n.setUserObject(node.getUserObject()); //
     * DefaultMutableTreeNode parent = (DefaultMutableTreeNode) _n.getParent(); //
     * model.removeNodeFromParent(_n); model.nodeChanged(_n); //
     * tree.scrollPathToVisible(new TreePath(parent.getPath())); model.reload();
     * 
     * }
     */

    private void updateTree(SPHAEdgeNode node) {

	SpBGroupIDTreeNode gnode = findSpBGroupIDTreeNode(node, (DefaultMutableTreeNode) model.getRoot());
	if (gnode == null)
	    gnode = new SpBGroupIDTreeNode(node);
	gnode.setUserObject(node);

	LOGGER.fine("*** updateTree - node: "+node+", STATUS " + node.getState());

	// LOGGER.fine("*** updateTree gnode " + gnode);
	updateTree(gnode, (DefaultMutableTreeNode) ((DefaultMutableTreeNode) model.getRoot()).getChildAt(0));

	SpBEONTreeNode nnode = findSpBEONTreeNode(node, (DefaultMutableTreeNode) model.getRoot());
	if (nnode == null)
	    nnode = new SpBEONTreeNode(node);
	nnode.setUserObject(node);

	// LOGGER.fine("*** updateTree nnode " + nnode);
	updateTree(nnode, gnode);

	if (node.getPayload() != null) {

	    for (Metric m : node.getPayload().getMetrics()) {

		MetricTreeNode mnode = new MetricTreeNode(m);
		LOGGER.fine("	*** updateTree metric " + m.getName() + " - "+m.getValue());
		updateTree(mnode, nnode);

	    }

	} else {

	    nnode.removeAllChildren();
	    updateTree(nnode, gnode);

	}

    }

    private class MQTTClientTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 3077743171196515780L;

	public MQTTClientTreeNode(MqttClient client) {

	    super(client);

	}

	public MqttClient getClient() {

	    return (MqttClient) userObject;

	}

	@Override
	public String toString() {

	    if (userObject == null)
		return "(no mqtt)";
	    return ((MqttClient) userObject).getClientId();

	}

	@Override
	public boolean equals(Object obj) {

	    if (obj == this)
		return true;
	    if (!(obj instanceof MQTTClientTreeNode))
		return false;

	    if (((MQTTClientTreeNode) obj).getClient() == null || userObject == null)
		return false;

	    return ((MQTTClientTreeNode) obj).getClient().equals(userObject);

	}

    }

    private class SpBGroupIDTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -2951570176657774463L;

	public SpBGroupIDTreeNode(SPHAEdgeNode node) {

	    super(node);

	}

	public SPHAEdgeNode getNode() {

	    return (SPHAEdgeNode) userObject;

	}

	@Override
	public String toString() {

	    if (userObject == null)
		return "(no group ID)";
	    SPHAEdgeNode node = (SPHAEdgeNode) userObject;
	    return node.getGroupId();

	}

	@Override
	public boolean equals(Object obj) {

	    if (obj == this)
		return true;
	    if (!(obj instanceof SpBGroupIDTreeNode))
		return false;

	    if (((SpBGroupIDTreeNode) obj).getNode() == null || userObject == null)
		return false;

	    return ((SpBGroupIDTreeNode) obj).getNode().getGroupId().equals(((SPHAEdgeNode) userObject).getGroupId());

	}

	@Override
	public int hashCode() {

	    if (userObject == null)
		return 0;
	    return ((SPHAEdgeNode) userObject).getGroupId().hashCode();

	}

    }

    private class SpBEONTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -5384014600837735706L;

	public SpBEONTreeNode(SPHAEdgeNode node) {

	    super(node);

	}

	public SPHAEdgeNode getNode() {

	    return (SPHAEdgeNode) userObject;

	}

	@Override
	public String toString() {

	    if (userObject == null)
		return "(no edge node ID)";

	    SPHAEdgeNode node = (SPHAEdgeNode) userObject;
	    return node.getEdgeNodeId();

	}

	@Override
	public boolean equals(Object obj) {

	    if (obj == this)
		return true;
	    if (!(obj instanceof SpBEONTreeNode))
		return false;

	    if (((SpBEONTreeNode) obj).getNode() == null || userObject == null)
		return false;

	    return ((SpBEONTreeNode) obj).getNode().getEdgeNodeId().equals(((SPHAEdgeNode) userObject).getEdgeNodeId());

	}

	@Override
	public int hashCode() {

	    if (userObject == null)
		return 0;
	    return ((SPHAEdgeNode) userObject).getEdgeNodeId().hashCode();

	}

    }

    private class MetricTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -5505771358879020540L;

	public MetricTreeNode(Metric metric) {

	    super(metric);

	}

	public Metric getMetric() {

	    return (Metric) userObject;

	}

	@Override
	public String toString() {

	    Metric metric = (Metric) userObject;
	    if (metric == null)
		return "(no metric)";
	    return metric.getName() + " - " + metric.getValue();

	}

	@Override
	public boolean equals(Object obj) {

	    if (obj == this)
		return true;
	    if (!(obj instanceof MetricTreeNode))
		return false;
	    if (((MetricTreeNode) obj).getMetric() == null || userObject == null)
		return false;

	    return ((MetricTreeNode) obj).getMetric().getName().equals(((Metric) userObject).getName());

	}

    }

}
