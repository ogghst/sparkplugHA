package it.nm.sparkplugha.example.simple;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.tahu.message.model.Metric;

import it.nm.sparkplugha.events.SPHADeviceCommandEvent;
import it.nm.sparkplugha.events.SPHADeviceDataEvent;
import it.nm.sparkplugha.events.SPHAEvent;
import it.nm.sparkplugha.events.SPHAEventListener;
import it.nm.sparkplugha.events.SPHAEventManager;
import it.nm.sparkplugha.events.SPHAMQTTConnectEvent;
import it.nm.sparkplugha.events.SPHAMQTTConnectLossEvent;
import it.nm.sparkplugha.events.SPHANodeBirthEvent;
import it.nm.sparkplugha.events.SPHANodeCommandEvent;
import it.nm.sparkplugha.events.SPHANodeDataEvent;
import it.nm.sparkplugha.events.SPHANodeDeathEvent;
import it.nm.sparkplugha.events.SPHANodeEvent;
import it.nm.sparkplugha.events.SPHANodeOutOfSequenceEvent;
import it.nm.sparkplugha.model.SPHAFeature;
import it.nm.sparkplugha.model.SPHANode;
import it.nm.sparkplugha.model.SPHANode.SPHANodeState;

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

	evtMgr.subscribe(SPHAMQTTConnectLossEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		SPHAMQTTConnectLossEvent me = (SPHAMQTTConnectLossEvent) event;
		if (me.getClient() == null)
		    return;

		try {

		    buildTreeStructure(new MQTTClientTreeNode(me.getClient()),
			    (DefaultMutableTreeNode) model.getRoot());

		} catch (Exception e) {

		    LOGGER.log(Level.SEVERE, e.getMessage(), e);

		}

	    }

	});

	evtMgr.subscribe(SPHAMQTTConnectEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		SPHAMQTTConnectEvent me = (SPHAMQTTConnectEvent) event;

		try {

		    buildTreeStructure(new MQTTClientTreeNode(me.getClient()),
			    (DefaultMutableTreeNode) model.getRoot());

		} catch (Exception e) {

		    LOGGER.log(Level.SEVERE, e.getMessage(), e);

		}

	    }

	});

	evtMgr.subscribe(SPHANodeBirthEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		buildTreeFromEvent(event);

	    }

	});

	evtMgr.subscribe(SPHANodeDataEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		buildTreeFromEvent(event);

	    }

	});

	evtMgr.subscribe(SPHANodeCommandEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		buildTreeFromEvent(event);

	    }

	});

	evtMgr.subscribe(SPHADeviceDataEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		buildTreeFromEvent(event);

	    }

	});

	evtMgr.subscribe(SPHADeviceCommandEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		buildTreeFromEvent(event);

	    }

	});

	evtMgr.subscribe(SPHANodeDeathEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		buildTreeFromEvent(event);

	    }

	});

	evtMgr.subscribe(SPHANodeOutOfSequenceEvent.class, new SPHAEventListener() {

	    @Override
	    public void trigger(SPHAEvent event) {

		buildTreeFromEvent(event);

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

		    // LOGGER.fine("*** NODE ONLINE: " + node.getNode().getDescriptorString());
		    setIcon(cloud);

		} else if (node.getNode().getState().equals(SPHANodeState.OFFLINE)) {

		    // LOGGER.fine("*** NODE OFFLINE: " + node.getNode().getDescriptorString());
		    setIcon(cloud_off);

		}

	    } else if (value instanceof MQTTClientTreeNode) {

		MQTTClientTreeNode node = (MQTTClientTreeNode) value;

		if (node.getClient().isConnected()) {

		    setIcon(cloud);

		} else {

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

    SpBGroupIDTreeNode buildSpBGroupIDTreeNode(SPHANode node, DefaultMutableTreeNode parent) {

	// LOGGER.fine(" findSpBGroupIDTreeNode - node='"+node+"', parent =
	// '"+parent+"'");

	int childs = parent.getChildCount();

	for (int i = 0; i < childs; i++) {

	    DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);

	    SpBGroupIDTreeNode deepSearch = buildSpBGroupIDTreeNode(node, child);
	    if (deepSearch != null)
		return deepSearch;

	    if (child instanceof SpBGroupIDTreeNode) {

		if (((SpBGroupIDTreeNode) child).getNode().getGroupId().equals(node.getGroupId())) {

		    // LOGGER.fine(" findSpBGroupIDTreeNode - found: '"+child+"'");
		    return ((SpBGroupIDTreeNode) child);

		}

	    }

	}

	return new SpBGroupIDTreeNode(node);

    }

    SpBEONTreeNode buildSpBEONTreeNode(SPHANode node, DefaultMutableTreeNode parent) {

	// LOGGER.fine(" findSpBEONTreeNode - node='"+node+"', parent = '"+parent+"'");

	int childs = parent.getChildCount();

	for (int i = 0; i < childs; i++) {

	    DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);

	    SpBEONTreeNode deepSearch = buildSpBEONTreeNode(node, child);
	    if (deepSearch != null)
		return deepSearch;

	    if (child instanceof SpBEONTreeNode) {

		if (((SpBEONTreeNode) child).getNode().getDescriptorString().equals(node.getDescriptorString())) {

		    // LOGGER.fine(" findSpBEONTreeNode - found: '"+child+"'");
		    return ((SpBEONTreeNode) child);

		}

	    }

	}

	return new SpBEONTreeNode(node);

    }

    SpBMetricTreeNode buildSpBMetricTreeNode(Metric metric, DefaultMutableTreeNode parent) {

	//LOGGER.fine(" buildSpBMetricTreeNode - metric='" + metric + "', parent = '" + parent + "'");

	int childs = parent.getChildCount();

	for (int i = 0; i < childs; i++) {

	    DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);

	    SpBMetricTreeNode deepSearch = buildSpBMetricTreeNode(metric, child);
	    if (deepSearch != null)
		return deepSearch;

	    if (child instanceof SpBMetricTreeNode) {

		if (((SpBMetricTreeNode) child).getMetric().getValue().equals(metric.getValue())) {

		    //LOGGER.fine(" findSpBMetricTreeNode - found: '" + child + "'");
		    return ((SpBMetricTreeNode) child);

		}

	    }

	}

	return new SpBMetricTreeNode(metric);

    }

    SpBDeviceTreeNode buildSpBDeviceTreeNode(SPHAFeature feature, DefaultMutableTreeNode parent) {

	//LOGGER.fine(" buildSpBDeviceTreeNode - metric='" + feature + "', parent = '" + parent + "'");

	int childs = parent.getChildCount();

	for (int i = 0; i < childs; i++) {

	    DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);

	    SpBDeviceTreeNode deepSearch = buildSpBDeviceTreeNode(feature, child);
	    if (deepSearch != null)
		return deepSearch;

	    if (child instanceof SpBDeviceTreeNode) {

		if (((SpBDeviceTreeNode) child).getDevice().getName().equals(feature.getName())) {

		    //LOGGER.fine(" buildSpBDeviceTreeNode - found: '" + child + "'");
		    return ((SpBDeviceTreeNode) child);

		}

	    }

	}

	return new SpBDeviceTreeNode(feature);

    }

    private DefaultMutableTreeNode buildTreeStructure(DefaultMutableTreeNode node, DefaultMutableTreeNode parent)
	    throws Exception {

	//LOGGER.fine(" *** buildTreeStructure(" + node + "," + parent + ") - parent childs: " + parent.getChildCount());

	if (parent == null || node == null)
	    return null;

	int childIdx = -1;

	for (int i = 0; i < parent.getChildCount(); i++) {

	    //LOGGER.fine(" *** comparing " + parent.getChildAt(i) + " with " + node + " : "
		//    + parent.getChildAt(i).equals(node));

	    if (parent.getChildAt(i).equals(node)) {

		childIdx = i;
		break;

	    }

	}

	if (childIdx >= 0) {

	    DefaultMutableTreeNode child = ((DefaultMutableTreeNode) parent.getChildAt(childIdx));
	    // LOGGER.fine(" *** UPDATING " + node + " below " + parent + " *** " +
	    // node.getClass().getSimpleName());
	    child.setUserObject(node.getUserObject());
	    // node.setParent(parent);
	    // model.removeNodeFromParent(node);
	    // model.insertNodeInto(node, parent, childIdx);
	    model.nodeChanged(parent);
	    model.nodeChanged(child);
	    tree.scrollPathToVisible(new TreePath(child.getPath()));
	    return child;

	} else {

	    // LOGGER.fine(" *** ADDING " + node + " below " + parent + ", childCount:
	    // "+parent.getChildCount()+", class = " + node.getClass().getSimpleName());
	    model.insertNodeInto(node, parent, parent.getChildCount());
	    // LOGGER.fine(" *** childCount: "+parent.getChildCount());
	    // node.setParent(parent);
	    model.nodeChanged(parent);
	    model.nodeChanged(node);
	    tree.scrollPathToVisible(new TreePath(node.getPath()));
	    return node;

	}

    }

    private DefaultMutableTreeNode buildLabelTree(StringTokenizer tok, DefaultMutableTreeNode parent) {

	if (tok.hasMoreTokens()) {

	    // LOGGER.fine("buildLabelTree(" + parent + ")");
	    String label = tok.nextToken();
	    // if (!tok.hasMoreTokens())
	    // return parent;
	    // LOGGER.fine(" nextToken = " + label);
	    SpBLabelTreeNode labelNode = new SpBLabelTreeNode(label);

	    try {

		labelNode = (SpBLabelTreeNode) buildTreeStructure(labelNode, parent);
		return buildLabelTree(tok, labelNode);

	    } catch (Exception e) {

		LOGGER.log(Level.SEVERE, e.getMessage(), e);
		return parent;

	    }

	} else {

	    return parent;

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

    private void buildTreeFromEvent(SPHAEvent event) {

	try {

	    if (event instanceof SPHANodeEvent) {

		SPHANode node = ((SPHANodeEvent) event).getNode();

		SpBGroupIDTreeNode gnode = buildSpBGroupIDTreeNode(node, (DefaultMutableTreeNode) model.getRoot());
		gnode.setUserObject(node);

		// LOGGER.fine("*** buildTreeFromEvent - node: " + node + ", STATUS " +
		// node.getState());

		// LOGGER.fine("*** buildTreeFromEvent - gnode " + gnode);
		gnode = (SpBGroupIDTreeNode) buildTreeStructure(gnode,
			(DefaultMutableTreeNode) ((DefaultMutableTreeNode) model.getRoot()).getChildAt(0));

		SpBEONTreeNode nnode = buildSpBEONTreeNode(node, gnode);
		nnode.setUserObject(node);

		// LOGGER.fine("*** buildTreeFromEvent - nnode " + nnode);
		nnode = (SpBEONTreeNode) buildTreeStructure(nnode, gnode);

		// LOGGER.fine("*** buildTreeFromEvent - metrics count: " +
		// node.getPayload().getMetrics().size());

		// makes sure there is a metric label parent
		DefaultMutableTreeNode mt = new SpBEONMetricsTreeNode(node);
		mt = buildTreeStructure(mt, nnode);

		// update metrics
		for (Metric metric : node.getPayload().getMetrics()) {

		    // build tree
		    StringTokenizer tok = new StringTokenizer(metric.getName(), "/");
		    DefaultMutableTreeNode ml = buildLabelTree(tok, mt);

		    SpBMetricTreeNode menode = buildSpBMetricTreeNode(metric, ml);
		    menode.setUserObject(metric);

		    // LOGGER.fine("*** buildTreeFromEvent - menode " + menode);

		    buildTreeStructure(menode, ml);

		}

		// makes sure there is a features label parent
		DefaultMutableTreeNode fl = new SpBEONFeaturesTreeNode(node);
		fl = buildTreeStructure(fl, nnode);

		// LOGGER.fine("*** buildTreeFromEvent - features count: " +
		// node.getFeatures().size());

		// update features
		for (SPHAFeature feature : node.getFeatures()) {

		    // build tree
		    StringTokenizer tok = new StringTokenizer(feature.getName(), "/");
		    DefaultMutableTreeNode ml = buildLabelTree(tok, fl);

		    SpBDeviceTreeNode denode = buildSpBDeviceTreeNode(feature, fl);
		    denode.setUserObject(feature);

		    // LOGGER.fine("*** buildTreeFromEvent - denode " + denode);

		    buildTreeStructure(denode, fl);

		}

	    }

	} catch (Exception e) {

	    LOGGER.log(Level.SEVERE, e.getMessage(), e);
	    e.printStackTrace();

	}

    }

    class MQTTClientTreeNode extends DefaultMutableTreeNode {

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

    class SpBGroupIDTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -2951570176657774463L;

	public SpBGroupIDTreeNode(SPHANode node) {

	    super(node);

	}

	public SPHANode getNode() {

	    return (SPHANode) userObject;

	}

	@Override
	public String toString() {

	    if (userObject == null)
		return "(no group ID)";
	    SPHANode node = (SPHANode) userObject;
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

	    return ((SpBGroupIDTreeNode) obj).getNode().getGroupId().equals(((SPHANode) userObject).getGroupId());

	}

	@Override
	public int hashCode() {

	    if (userObject == null)
		return 0;
	    return userObject.hashCode();

	}

    }

    class SpBEONTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -5384014600837735706L;

	public SpBEONTreeNode(SPHANode node) {

	    super(node);

	}

	public SPHANode getNode() {

	    return (SPHANode) userObject;

	}

	@Override
	public String toString() {

	    if (userObject == null)
		return "(no edge node ID)";

	    SPHANode node = (SPHANode) userObject;
	    return node.getEdgeNodeId();

	}

	@Override
	public boolean equals(Object obj) {

	    if (obj == this)
		return true;
	    if (!(obj instanceof SpBEONTreeNode))
		return false;

	    // LOGGER.fine("EON equals: "+ getNode()+ "(" + getNode().getClass()+ ") with
	    // "+getNode()+" ("+((SpBEONTreeNode)obj).getNode().getClass()+")");

	    if (((SpBEONTreeNode) obj).getNode() == null || userObject == null)
		return false;

	    return ((SpBEONTreeNode) obj).getNode().getDescriptorString()
		    .equals(((SPHANode) userObject).getDescriptorString());

	}

	@Override
	public int hashCode() {

	    // LOGGER.fine("EON hashCode: "+ userObject+" - "+userObject.hashCode());

	    if (userObject == null)
		return 0;
	    return userObject.hashCode();

	}

    }

    class SpBEONMetricsTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -5384014650837735706L;

	public SpBEONMetricsTreeNode(SPHANode node) {

	    super(node);

	}

	public SPHANode getNode() {

	    return (SPHANode) userObject;

	}

	@Override
	public String toString() {

	    return "Metrics";

	}

	@Override
	public boolean equals(Object obj) {

	    if (obj == this)
		return true;
	    if (!(obj instanceof SpBEONMetricsTreeNode))
		return false;

	    // LOGGER.fine("EON equals: "+ getNode()+ "(" + getNode().getClass()+ ") with
	    // "+getNode()+" ("+((SpBEONTreeNode)obj).getNode().getClass()+")");

	    if (((SpBEONMetricsTreeNode) obj).getNode() == null || userObject == null)
		return false;

	    return ((SpBEONMetricsTreeNode) obj).getNode().getDescriptorString()
		    .equals(((SPHANode) userObject).getDescriptorString());

	}

	@Override
	public int hashCode() {

	    // LOGGER.fine("EON hashCode: "+ userObject+" - "+userObject.hashCode());

	    if (userObject == null)
		return 0;
	    return userObject.hashCode();

	}

    }

    class SpBEONFeaturesTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -5384014600817735706L;

	public SpBEONFeaturesTreeNode(SPHANode node) {

	    super(node);

	}

	public SPHANode getNode() {

	    return (SPHANode) userObject;

	}

	@Override
	public String toString() {

	    return "Features";

	}

	@Override
	public boolean equals(Object obj) {

	    if (obj == this)
		return true;
	    if (!(obj instanceof SpBEONFeaturesTreeNode))
		return false;

	    // LOGGER.fine("EON equals: "+ getNode()+ "(" + getNode().getClass()+ ") with
	    // "+getNode()+" ("+((SpBEONTreeNode)obj).getNode().getClass()+")");

	    if (((SpBEONFeaturesTreeNode) obj).getNode() == null || userObject == null)
		return false;

	    return ((SpBEONFeaturesTreeNode) obj).getNode().getDescriptorString()
		    .equals(((SPHANode) userObject).getDescriptorString());

	}

	@Override
	public int hashCode() {

	    // LOGGER.fine("EON hashCode: "+ userObject+" - "+userObject.hashCode());

	    if (userObject == null)
		return 0;
	    return userObject.hashCode();

	}

    }

    class SpBDeviceTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -5384014600824335706L;

	public SpBDeviceTreeNode(SPHAFeature feature) {

	    super(feature);

	}

	public SPHAFeature getDevice() {

	    return (SPHAFeature) userObject;

	}

	@Override
	public String toString() {

	    if (userObject == null)
		return "(no edge node ID)";

	    SPHAFeature node = (SPHAFeature) userObject;
	    return node.getName();

	}

	@Override
	public boolean equals(Object obj) {

	    if (obj == this)
		return true;
	    if (!(obj instanceof SpBEONTreeNode))
		return false;

	    if (((SpBEONTreeNode) obj).getNode() == null || userObject == null)
		return false;

	    return ((SpBEONTreeNode) obj).getNode().getEdgeNodeId().equals(((SPHANode) userObject).getEdgeNodeId());

	}

    }

    class SpBMetricTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -5505771358879020540L;

	public SpBMetricTreeNode(Metric metric) {

	    super(metric);

	}

	public Metric getMetric() {

	    return (Metric) userObject;

	}

	@Override
	public String toString() {

	    Metric metric = (Metric) userObject;
	    if (metric == null)
		return "<no metric>";
	    return metric.getName() + " (" + metric.getValue() + ")";

	}

	@Override
	public boolean equals(Object obj) {

	    if (obj == this)
		return true;
	    if (!(obj instanceof SpBMetricTreeNode))
		return false;
	    if (((SpBMetricTreeNode) obj).getMetric() == null || userObject == null)
		return false;

	    return ((SpBMetricTreeNode) obj).getMetric().getName().equals(((Metric) userObject).getName());

	}

	@Override
	public int hashCode() {

	    if (userObject == null)
		return 0;
	    return ((Metric) userObject).getName().hashCode();

	}

    }

    class SpBLabelTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -5505771382342390200L;

	public SpBLabelTreeNode(String label) {

	    super(label);

	}

	public String getLabel() {

	    return (String) userObject;

	}

	@Override
	public String toString() {

	    String label = (String) userObject;
	    if (label == null)
		return "<no label>";
	    return label;

	}

	@Override
	public boolean equals(Object obj) {

	    if (obj == this)
		return true;
	    if (!(obj instanceof SpBLabelTreeNode))
		return false;
	    if (((SpBLabelTreeNode) obj).getLabel() == null || userObject == null)
		return false;

	    return ((SpBLabelTreeNode) obj).getLabel().equals(getLabel());

	}

	@Override
	public int hashCode() {

	    if (userObject == null)
		return 0;
	    return userObject.hashCode();

	}

    }

}
