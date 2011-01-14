package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.awt.Color;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.util.FreeplaneIconUtils;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.filter.condition.ICondition;
import org.freeplane.features.common.link.ArrowType;
import org.freeplane.features.common.styles.IStyle;
import org.freeplane.plugin.script.ExecuteScriptException;

/**
 * This interface alone defines the api for accessing the internal state of the Freeplane. All read-write methods
 * and properties (with rare, documented exceptions in {@link Controller} and {@link Map}) support undo and
 * rollback on exceptions.
 * <p>
 * Every Proxy subinterface comes in two variants:
 * <ul>
 * <li>A read-only interface, like {@link NodeRO}. This collects only the methods that don't change the
 *     underlying object (in case of <code>NodeRO</code> this would be <code>NodeModel</code>.
 * <li>A read-write interface, like {@link Node}. This inherits from the respective read-only interface all its
 *     methods and properties and adds write access to the underlying object.
 * </ul>
 * The main point of this distinction are formulas: <em>Only the methods defined in the read-only interfaces are
 * supported in Formulas!</em>. Changing values in a Formula are against the Formula concept and lead to corruption
 * of the caching mechanism for Formulas.
 */
public interface Proxy {
	/** Node's attribute table: <code>node.attributes</code> - read-only.
	 * <p>
	 * Attributes are name - value pairs assigned to a node. A node may have multiple attributes
	 * with the same name. */
	interface AttributesRO {
		/** alias for {@link #getFirst(String)}.
		 * @deprecated before 1.1 - use {@link #get(int)}, {@link #getFirst(String)} or {@link #getAll(String)} instead. */
		@Deprecated
		String get(final String name);

		/** returns the <em>first</em> value of an attribute with the given name or null otherwise.
		 * @since 1.2 */
		String getFirst(final String name);

		/** returns all values for the attribute name. */
		List<String> getAll(final String name);

		/** returns all attribute names in the proper sequence. The number of names returned
		 * is equal to the number of attributes.
		 * <pre>
		 *   // rename attribute
		 *   int i = 0;
		 *   for (String name : attributes.getAttributeNames()) {
		 *       if (name.equals("xy"))
		 *           attributes.set(i, "xyz", attributes.get(i));
		 *       ++i;
		 *   }
		 * </pre> */
		List<String> getAttributeNames();

		/** returns the attribute value at the given index.
		 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
		 *         &lt; 0 || index &gt;= size())</tt>.*/
		String get(final int index);

		/** @deprecated since 1.2 - use {@link #findFirst(String)} instead. */
		int findAttribute(final String name);

		/** returns the index of the first attribute with the given name if one exists or -1 otherwise.
		 * For searches for <em>all</em> attributes with a given name <code>getAttributeNames()</code>
		 * must be used.
		 * @since 1.2*/
		int findFirst(final String name);

		/** the number of attributes. It is <code>size() == getAttributeNames().size()</code>. */
		int size();
	}

	/** Node's attribute table: <code>node.attributes</code> - read-write. */
	interface Attributes extends AttributesRO {
		/** sets the value of the attribute at an index. This method will not create new attributes.
		 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
		 *         &lt; 0 || index &gt;= size())</tt>. */
		void set(final int index, final String value);

		/** sets name and value of the attribute at the given index. This method will not create new attributes.
		 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
		 *         &lt; 0 || index &gt;= size())</tt>. */
		void set(final int index, final String name, final String value);

		/** removes the <em>first</em> attribute with this name.
		 * @return true on removal of an existing attribute and false otherwise.
		 * @deprecated before 1.1 - use {@link #remove(int)} or {@link #removeAll(String)} instead. */
		@Deprecated
		boolean remove(final String name);

		/** removes <em>all</em> attributes with this name.
		 * @return true on removal of an existing attribute and false otherwise. */
		boolean removeAll(final String name);

		/** removes the attribute at the given index.
		 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
		 *         &lt; 0 || index &gt;= size())</tt>. */
		void remove(final int index);

		/** adds an attribute if there is no attribute with the given name or changes
		 * the value <em>of the first</em> attribute with the given name. */
		void set(final String name, final String value);

		/** adds an attribute no matter if an attribute with the given name already exists. */
		void add(final String name, final String value);

		/** removes all attributes.
		 * @since 1.2 */
		void clear();
	}

	/** Graphical connector between nodes:<code>node.connectorsIn</code> / <code>node.connectorsOut</code>
	 * - read-only. */
	interface ConnectorRO {
		Color getColor();

		String getColorCode();

		ArrowType getEndArrow();

		String getMiddleLabel();

		Node getSource();

		String getSourceLabel();

		ArrowType getStartArrow();

		Node getTarget();

		String getTargetLabel();

		boolean simulatesEdge();
	}

	/** Graphical connector between nodes:<code>node.connectorsIn</code> / <code>node.connectorsOut</code>
	 * - read-write. */
	interface Connector extends ConnectorRO {
		void setColor(Color color);

		/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
		 *  @since 1.2 */
		void setColorCode(String rgbString);

		void setEndArrow(ArrowType arrowType);

		void setMiddleLabel(String label);

		void setSimulatesEdge(boolean simulatesEdge);

		void setSourceLabel(String label);

		void setStartArrow(ArrowType arrowType);

		void setTargetLabel(String label);
	}

	/** Access to global state: <code>c</code> - read-only. */
	interface ControllerRO {
		/** if multiple nodes are selected returns one (arbitrarily chosen)
		 * selected node or the selected node for a single node selection. */
		Node getSelected();

		List<Node> getSelecteds();

		/** returns List<Node> of Node objects sorted on Y
		 *
		 * @param differentSubtrees if true
		 *   children/grandchildren/grandgrandchildren/... nodes of selected
		 *   parent nodes are excluded from the result. */
		List<Node> getSortedSelection(boolean differentSubtrees);

		/**
		 * returns Freeplane version.
		 * Use it like this:
		 * <pre>
		 *   import org.freeplane.core.util.FreeplaneVersion
		 *   import org.freeplane.core.ui.components.UITools
		 * 
		 *   def required = FreeplaneVersion.getVersion("1.1.2");
		 *   if (c.freeplaneVersion < required)
		 *       UITools.errorMessage("Freeplane version " + c.freeplaneVersion
		 *           + " not supported - update to at least " + required);
		 * </pre>
		 */
		FreeplaneVersion getFreeplaneVersion();

		/** Starting from the root node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true.
		 * @see Node#find(ICondition) for searches on subtrees
		 * @deprecated since 1.2 use {@link #find(Closure)} instead. */
		List<Node> find(ICondition condition);

		/**
		 * Starting from the root node, recursively searches for nodes (in breadth-first sequence) for which
		 * <code>closure.call(node)</code> returns true.
		 * <p>
		 * A find method that uses a Groovy closure ("block") for simple custom searches. As this closure
		 * will be called with a node as an argument (to be referenced by <code>it</code>) the search can
		 * evaluate every node property, like attributes, icons, node text or notes.
		 * <p>
		 * Examples:
		 * <pre>
		 *    def nodesWithNotes = c.find{ it.noteText != null }
		 *    
		 *    def matchingNodes = c.find{ it.text.matches(".*\\d.*") }
		 *    def texts = matchingNodes.collect{ it.text }
		 *    print "node texts containing numbers:\n " + texts.join("\n ")
		 * </pre>
		 * @param closure a Groovy closure that returns a boolean value. The closure will receive
		 *        a NodeModel as an argument which can be tested for a match.
		 * @return all nodes for which <code>closure.call(NodeModel)</code> returns true.
		 * @see Node#find(Closure) for searches on subtrees
		 */
		List<Node> find(Closure closure);

		/**
		 * Returns all nodes of the map in breadth-first order, that is, for the following map,
		 * <pre>
		 *  1
		 *    1.1
		 *      1.1.1
		 *      1.1.2
		 *    1.2
		 *  2
		 * </pre>
		 * [1, 1.1, 1.1.1, 1.1.2, 1.2, 2] is returned.
		 * @see Node#find(Closure) for searches on subtrees
		 * @since 1.2 */
		List<Node> findAll();

		/**
		 * Returns all nodes of the map in depth-first order, that is, for the following map,
		 * <pre>
		 *  1
		 *    1.1
		 *      1.1.1
		 *      1.1.2
		 *    1.2
		 *  2
		 * </pre>
		 * [1.1.1, 1.1.2, 1.1, 1.2, 1, 2] is returned.
		 * @see Node#findAllDepthFirst() for subtrees
		 * @since 1.2 */
		List<Node> findAllDepthFirst();
	}

	/** Access to global state: <code>c</code> - read-write. */
	interface Controller extends ControllerRO {
		void centerOnNode(Node center);

		/** Starts editing node, normally in the inline editor. Does not block until edit has finished.
		 * @since 1.2.2 */
		void edit(Node node);

		/** opens the appropriate popup text editor. Does not block until edit has finished.
		 * @since 1.2.2 */
		void editInPopup(Node node);

		void select(Node toSelect);

		/** selects branchRoot and all children */
		void selectBranch(Node branchRoot);

		/** toSelect is a List<Node> of Node objects */
		void selectMultipleNodes(List<Node> toSelect);

		/** reset undo / redo lists and deactivate Undo for current script */
		void deactivateUndo();

		/** invokes undo once - for testing purposes mainly.
		 * @since 1.2 */
		void undo();

		/** invokes redo once - for testing purposes mainly.
		 * @since 1.2 */
		void redo();

		/** The main info for the status line with key="standard", use null to remove. Removes icon if there is one. */
		void setStatusInfo(String info);

		/** Info for status line, null to remove. Removes icon if there is one.
		 * @see #setStatusInfo(String, String, String) */
		void setStatusInfo(String infoPanelKey, String info);

		/** Info for status line - text and icon - null stands for "remove" (text or icon)
		 * @param infoPanelKey "standard" is the left most standard info panel. If a panel with
		 *        this name doesn't exist it will be created.
		 * @param info Info text
		 * @param iconKey key as those that are used for nodes (see {@link Icons#addIcon(String)}).
		 * <pre>
		 *   println("all available icon keys: " + FreeplaneIconUtils.listStandardIconKeys())
		 *   c.setStatusInfo("standard", "hi there!", "button_ok");
		 * </pre>
		 * @see FreeplaneIconUtils
		 * @since 1.2 */
		void setStatusInfo(String infoPanelKey, String info, String iconKey);

		/** @deprecated since 1.2 - use {@link #setStatusInfo(String, String, String)} */
		void setStatusInfo(String infoPanelKey, Icon icon);

		/** opens a new map with a default name in the foreground.
		 * @since 1.2 */
		Map newMap();

		/** opens a new map for url in the foreground if it isn't opened already.
		 * @since 1.2 */
		Map newMap(URL url);
	}

	/** Edge to parent node: <code>node.style.edge</code> - read-only. */
	interface EdgeRO {
		Color getColor();

		String getColorCode();

		EdgeStyle getType();

		int getWidth();
	}

	/** Edge to parent node: <code>node.style.edge</code> - read-write. */
	interface Edge extends EdgeRO {
		void setColor(Color color);

		/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
		 *  @since 1.2 */
		void setColorCode(String rgbString);

		void setType(EdgeStyle type);

		/** can be -1 for default, 0 for thin, >0 */
		void setWidth(int width);
	}

	/** External object: <code>node.externalObject</code> - read-only. */
	interface ExternalObjectRO {
		/** returns the object's uri if set or null otherwise.
		 * @since 1.2 */
		String getUri();

		/** returns the current zoom level as ratio, i.e. 1.0 is returned for 100%.
		 * If there is no external object 1.0 is returned. */
		float getZoom();
		
		/** @deprecated since 1.2 - use {@link #getUri()} instead. */
		String getURI();
	}

	/** External object: <code>node.externalObject</code> - read-write. */
	interface ExternalObject extends ExternalObjectRO {
		/** setting null uri means remove external object. */
		void setUri(String uri);
		
		/** set to 1.0 to set it to 100%. If the node has no object assigned this method does nothing. */
		void setZoom(float zoom);
		
		/** @deprecated since 1.2 - use {@link #setUri(String)} instead. */
		void setURI(String uri);
	}

	/** Node's font: <code>node.style.font</code> - read-only. */
	interface FontRO {
		String getName();

		int getSize();

		boolean isBold();

		boolean isBoldSet();

		boolean isItalic();

		boolean isItalicSet();

		boolean isNameSet();

		boolean isSizeSet();
	}

	/** Node's font: <code>node.style.font</code> - read-write. */
	interface Font extends FontRO {
		void resetBold();

		void resetItalic();

		void resetName();

		void resetSize();

		void setBold(boolean bold);

		void setItalic(boolean italic);

		void setName(String name);

		void setSize(int size);
	}

	/** Node's icons: <code>node.icons</code> - read-only. */
	interface IconsRO {
		/** returns List<Node> of Strings (corresponding to iconID above);
		 * iconID is one of "Idea","Question","Important", etc. */
		List<String> getIcons();
	}

	/** Node's icons: <code>node.icons</code> - read-write. */
	interface Icons extends IconsRO {
		/**
		 * adds an icon to a node if an icon for the given key can be found. The same icon can be added multiple
		 * times.
		 * <pre>
		 *   println("all available icon keys: " + FreeplaneIconUtils.listStandardIconKeys())
		 *   node.icons.addIcon("button_ok")
		 * </pre>
		 * @see FreeplaneIconUtils */
		void add(String name);

		/** @deprecated since 1.2 - use {@link #add(String)} instead. */
		void addIcon(String name);

		/** deletes first occurence of icon with the given name, returns true if
		 * success (icon existed); */
		boolean remove(String name);

		/** @deprecated since 1.2 - use {@link #remove(String)} instead. */
		boolean removeIcon(String name);
	}

	/** Node's link: <code>node.link</code> - read-only.
	 * <p>
	 * None of the getters will throw an exception, even if you call, e.g. getNode() on a File link.
	 * Instead they will return null. To check the link type evaluate getUri().getScheme() or the result
	 * of the special getters.*/
	interface LinkRO {
		/** returns the link text, a stringified URI, if a link is defined and null otherwise.
		 * @since 1.2 */
		String getText();

		/** returns the link as URI if defined and null otherwise. Won't throw an exception.
		 * @since 1.2 */
		URI getUri();

		/** returns the link as File if defined and if the link target is a valid File URI and null otherwise.
		 * @see File#File(URI).
		 * @since 1.2 */
		File getFile();

		/** returns the link as Node if defined and if the link target is a valid local link to a node and null otherwise.
		 * @since 1.2 */
		Node getNode();

		/** @deprecated since 1.2 - use {@link #getText()} instead. */
		String get();
	}

	/** Node's link: <code>node.link</code> - read-write. */
	interface Link extends LinkRO {
		/** target is a stringified URI. Removes any link if uri is null.
		 * To get a local link (i.e. to another node) target should be: "#" + nodeId or better use setNode(Node).
		 * @throws IllegalArgumentException if target is not convertible into a {@link URI}.
		 * @since 1.2 */
		void setText(String target);

		/** sets target to uri. Removes any link if uri is null.
		 * @since 1.2 */
		void setUri(URI uri);

		/** sets target to file. Removes any link if file is null.
		 * @since 1.2 */
		void setFile(File file);

		/** target is a node of the same map. Shortcut for setTarget("#" + node.nodeId)
		 * Removes any link if node is null.
		 * @throws IllegalArgumentException if node belongs to another map.
		 * @since 1.2 */
		void setNode(Node node);

		/** @deprecated since 1.2 - use {@link #setText(String)} instead.
		 * @return true if target could be converted to an URI and false otherwise. */
		boolean set(String target);
	}

	/** The map a node belongs to: <code>node.map</code> - read-only. */
	interface MapRO {
		/** @since 1.2 */
		Node getRoot();

		/** @deprecated since 1.2 - use {@link #getRoot()} instead. */
		Node getRootNode();

		/** returns the node if the map contains it or null otherwise. */
		Node node(String id);

		/** returns the physical location of the map if available or null otherwise. */
		File getFile();

		/** returns the title of the MapView.
		 * @since 1.2 */
		String getName();
	}

	/** The map a node belongs to: <code>node.map</code> - read-write. */
	interface Map extends MapRO {
		/**
		 * closes a map. Note that there is <em>no undo</em> for this method!
		 * @param force close map even if there are unsaved changes.
		 * @param allowInteraction if (allowInteraction && ! force) a saveAs dialog will be opened if there are
		 *        unsaved changes.
		 * @return false if the saveAs was cancelled by the user and true otherwise.
		 * @throws RuntimeException if the map contains changes and parameter force is false.
		 * @since 1.2
		 */
		boolean close(boolean force, boolean allowInteraction);

		/**
		 * saves the map to disk. Note that there is <em>no undo</em> for this method.
		 * @param allowInteraction if a saveAs dialog should be opened if the map has no assigned URL so far.
		 * @return false if the saveAs was cancelled by the user and true otherwise.
		 * @throws RuntimeException if the map has no assigned URL and parameter allowInteraction is false.
		 * @since 1.2
		 */
		boolean save(boolean allowInteraction);
	}

	/** The currently selected node: <code>node</code> - read-only. */
	interface NodeRO {
		Attributes getAttributes();

		/** allows to access attribute values like array elements. Note that the returned type is a
		 * {@link Convertible}, not a String. Nevertheless it behaves like a String in almost all respects,
		 * that is, in Groovy scripts it understands all String methods like lenght(), matches() etc.
		 * <pre>
		 *   // standard way
		 *   node.attributes.set("attribute name", "12")
		 *   // implicitely use getAt()
		 *   def val = node["attribute name"]
		 *   // use all conversions that Convertible provides (num, date, string, ...)
		 *   assert val.num == new Long(12)
		 *   // or use it just like a string
		 *   assert val.startsWith("1")
		 * </pre>
		 * @throws ExecuteScriptException 
		 * @since 1.2
		 */
		Convertible getAt(String attributeName);

		/** returns the index (0..) of this node in the (by Y coordinate sorted)
		 * list of this node's children. Returns -1 if childNode is not a child
		 * of this node. */
		int getChildPosition(Node childNode);

		/** returns the children of this node ordered by Y coordinate. */
		List<Node> getChildren();

		Collection<Connector> getConnectorsIn();

		Collection<Connector> getConnectorsOut();

		/** returns the raw HTML text of the details if there is any or null otherwise.
		 * @since 1.2 */
		String getDetailsText();

		/** returns the text of the details as a Convertible like {@link #getNote()} for notes.
		 * @since 1.2 */
		Convertible getDetails();

		/** returns true if node details are hidden.
		 * @since 1.2 */
		boolean getHideDetails();

		ExternalObject getExternalObject();

		Icons getIcons();

		Link getLink();

		/** the map this node belongs to. */
		Map getMap();

		/** @deprecated since 1.2 - use Node.getId() instead. */
		String getNodeID();

		/** @since 1.2 */
		String getId();

		/** if countHidden is false then only nodes that are matched by the
		 * current filter are counted. */
		int getNodeLevel(boolean countHidden);

		/**
		 * Returns a Convertible object for the plain not text. Convertibles behave like Strings in most respects.
		 * Additionally String methods are overridden to handle Convertible arguments as if the argument were the
		 * result of Convertible.getText().
		 * @return Convertible getString(), getText() and toString() will return plain text instead of the HTML.
		 *         Use {@link #getNoteText()} to get the HTML text.
		 * @throws ExecuteScriptException 
		 * @since 1.2
		 */
		Convertible getNote();

		/** Returns the HTML text of the node. (Notes always contain HTML text.) 
		 * @throws ExecuteScriptException */
		String getNoteText();

		/** @since 1.2 */
		Node getParent();

		/** @deprecated since 1.2 - use {@link #getParent()} instead. */
		Node getParentNode();

		NodeStyle getStyle();

		/** use this method to remove all tags from an HTML node. Formulas are not evaluated.
		 * @since 1.2 */
		String getPlainText();

		/** use this method to remove all tags from an HTML node.
		 * @deprecated since 1.2 - use getPlainText() or getTo().getPlain() instead. */
		String getPlainTextContent();

		/** The raw text of this node. Use {@link #getPlainText()} to remove HTML.
		 * @since 1.2 */
		String getText();

		/**
		 * returns an object that performs conversions (method name is choosen to give descriptive code):
		 * <dl>
		 * <dt>node.to.num <dd>Long or Double, see {@link Convertible#getDate()}.
		 * <dt>node.to.date <dd>Date, see {@link Convertible#getDate()}.
		 * <dt>node.to.string <dd>Text, see {@link Convertible#getString()}.
		 * <dt>node.to.text <dd>an alias for getString(), see {@link Convertible#getText()}.
		 * <dt>node.to.object <dd>returns what fits best, see {@link Convertible#getObject()}.
		 * </dl>
		 * @return ConvertibleObject
		 * @throws ExecuteScriptException on formula evaluation errors
		 * @throws ConversionException on parse errors, e.g. if to.date is invoked on "0.25"
		 * @since 1.2
		 */
		Convertible getTo();

		/** an alias for {@link #getTo()}.
		 * @throws ExecuteScriptException
		 * @since 1.2 */
		Convertible getValue();

		/** returns true if p is a parent, or grandparent, ... of this node, or if it <em>is equal<em>
		 * to this node; returns false otherwise. */
		boolean isDescendantOf(Node p);

		boolean isFolded();

		boolean isLeaf();

		boolean isLeft();

		boolean isRoot();

		boolean isVisible();

		/** Starting from this node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true.
		 * @deprecated since 1.2 use {@link #find(Closure)} instead. */
		List<Node> find(ICondition condition);

		/** Starting from this node, recursively searches for nodes for which <code>closure.call(node)</code>
		 * returns true. See {@link Controller#find(Closure)} for details. */
		List<Node> find(Closure closure);

		/** Returns all nodes of the map in breadth-first order.
		 * @see Controller#findAll() for subtrees
		 * @since 1.2 */
		List<Node> findAll();
		
		/** Returns all nodes of the map in depth-first order.
		 * @see Controller#findAllDepthFirst() for subtrees.
		 * @since 1.2 */
		List<Node> findAllDepthFirst();

		Date getLastModifiedAt();

		Date getCreatedAt();
	}

	/** The currently selected node: <code>node</code> - read-write. */
	interface Node extends NodeRO {
		Connector addConnectorTo(Node target);

		/** adds a new Connector object to List<Node> connectors and returns
		 * reference for optional further editing (style); also enlists the
		 * Connector on the target Node object. */
		Connector addConnectorTo(String targetNodeId);

		/** inserts *new* node as child, takes care of all construction work and
		 * internal stuff inserts as last child. */
		Node createChild();

		/** same as {@link #createChild()} but sets the node text to the given text.
		 * @since 1.2 */
		Node createChild(Object value);

		/** inserts *new* node as child, takes care of all construction work and
		 * internal stuff */
		Node createChild(int position);

		void delete();

		/** removes connector from List<Node> connectors; does the corresponding
		 * on the target Node object referenced by connectorToBeRemoved */
		void moveTo(Node parentNode);

		void moveTo(Node parentNode, int position);

		/** as above, using String nodeId instead of Node object to establish the connector*/
		void removeConnector(Connector connectorToBeRemoved);

		/**
		 * A node's text is String valued. This methods provides automatic conversion to String in the same way as
		 * for {@link #setText(Object)}, that is special conversion is provided for dates and calendars, other
		 * types are converted via value.toString().
		 * 
		 * If the conversion result is not valid HTML it will be automatically converted to HTML.
		 * 
		 * @param details An object for conversion to String. Use null to unset the details. Works well for all types
		 *        that {@link Convertible} handles, particularly {@link Convertible}s itself.
		 * @since 1.2
		 */
		void setDetails(Object details);

		/** use node.hideDetails = true/false to control visibility of details.
		 * @since 1.2 */
		void setHideDetails(boolean hide);

		void setFolded(boolean folded);

		/**
		 * Set the note text:
		 * <ul>
		 * <li>This methods provides automatic conversion to String in a way that node.getNote().getXyz()
		 *     methods will be able to convert the string properly to the wanted type.
		 * <li>Special conversion is provided for dates and calendars: They will be converted in a way that
		 *     node.note.date and node.note.calendar will work. All other types are converted via value.toString().
		 * <li>If the conversion result is not valid HTML it will be automatically converted to HTML.
		 * </ul>
		 * <p>
		 * <pre>
		 *   // converts numbers and other stuff with toString()
		 *   node.note = 1.2
		 *   assert node.note.text == "<html><body><p>1.2"
		 *   assert node.note.plain == "1.2"
		 *   assert node.note.num == 1.2d
		 *   // == dates
		 *   // a date in some non-UTC time zone
		 *   def date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").
		 *       parse("1970-01-01 00:00:00.000-0200")
		 *   // converts to "1970-01-01T02:00:00.000+0000" (GMT)
		 *   // - note the shift due to the different time zone
		 *   // - the missing end tags don't matter for rendering
		 *   node.note = date
		 *   assert node.note == "<html><body><p>1970-01-01T02:00:00.000+0000"
		 *   assert node.note.plain == "1970-01-01T02:00:00.000+0000"
		 *   assert node.note.date == date
		 *   // == remove note
		 *   node.note = null
		 *   assert node.note.text == null
		 * </pre>
		 * @param value An object for conversion to String. Works well for all types that {@link Convertible}
		 *        handles, particularly {@link Convertible}s itself.
		 * @since 1.2 (note that the old setNoteText() did not support non-String arguments.
		 */
		void setNote(Object value);

		/** @deprecated since 1.2 - use {@link #setNote(Object)} instead. */
		void setNoteText(String text);

		/**
		 * A node's text is String valued. This methods provides automatic conversion to String in a way that
		 * node.to.getXyz() methods will be able to convert the string properly to the wanted type.
		 * Special conversion is provided for dates and calendars: They will be converted in a way that
		 * node.to.date and node.to.calendar will work. All other types are converted via value.toString():
		 * <pre>
		 *   // converts non-Dates with toString()
		 *   node.text = 1.2
		 *   assert node.to.text == "1.2"
		 *   assert node.to.num == 1.2d
		 *   // == dates
		 *   // a date in some non-GMT time zone
		 *   def date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").
		 *       parse("1970-01-01 00:00:00.000-0200")
		 *   // converts to "1970-01-01T02:00:00.000+0000" (GMT)
		 *   // - note the shift due to the different time zone
		 *   node.text = date
		 *   assert node.to.text == "1970-01-01T02:00:00.000+0000"
		 *   assert node.to.date == date
		 * </pre>
		 * @param value A not-null object for conversion to String. Works well for all types that {@link Convertible}
		 *        handles, particularly {@link Convertible}s itself.
		 */
		void setText(Object value);

		void setLastModifiedAt(Date date);

		void setCreatedAt(Date date);

		// Attributes
		/**
		 * Allows to set and to change attribute like array elements.
		 * <p>
		 * Note that attributes are String valued. This methods provides automatic conversion to String in a way that
		 * node["a name"].getXyz() methods will be able to convert the string properly to the wanted type.
		 * Special conversion is provided for dates and calendars: They will be converted in a way that
		 * node["a name"].date and node["a name"].calendar will work. All other types are converted via
		 * value.toString():
		 * <pre>
		 *   // == text
		 *   node["attribute name"] = "a value"
		 *   assert node["attribute name"] == "a value"
		 *   assert node.attributes.getFirst("attribute name") == "a value" // the same
		 *   // == numbers and others
		 *   // converts numbers and other stuff with toString()
		 *   node["a number"] = 1.2
		 *   assert node["a number"].text == "1.2"
		 *   assert node["a number"].num == 1.2d
		 *   // == dates
		 *   // a date in some non-GMT time zone
		 *   def date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").
		 *       parse("1970-01-01 00:00:00.000-0200")
		 *   // converts to "1970-01-01T02:00:00.000+0000" (GMT)
		 *   // - note the shift due to the different time zone
		 *   node["a date"] = date
		 *   assert node["a date"].text == "1970-01-01T02:00:00.000+0000"
		 *   assert node["a date"].date == date
		 *   // == remove an attribute
		 *   node["removed attribute"] = "to be removed"
		 *   assert node["removed attribute"] == "to be removed"
		 *   node["removed attribute"] = null
		 *   assert node.attributes.find("removed attribute") == -1
		 * </pre>
		 * @param value An object for conversion to String. Works well for all types that {@link Convertible}
		 *        handles, particularly {@link Convertible}s itself. Use null to unset an attribute.
		 * @return the new value
		 */
		String putAt(String attributeName, Object value);

		/** allows to set all attributes at once:
		 * <pre>
		 *   node.attributes = [:] // clear the attributes
		 *   assert node.attributes.size() == 0
		 *   node.attributes = ["1st" : "a value", "2nd" : "another value"] // create 2 attributes 
		 *   assert node.attributes.size() == 2
		 *   node.attributes = ["one attrib" : new Double(1.22)] // replace all attributes
		 *   assert node.attributes.size() == 1
		 *   assert node.attributes.getFirst("one attrib") == "1.22" // note the type conversion
		 *   assert node["one attrib"] == "1.22" // here we compare Convertible with String
		 * </pre>
		 */
		void setAttributes(java.util.Map<String, Object> attributes);
	}

	/** Node's style: <code>node.style</code> - read-only. */
	interface NodeStyleRO {
		IStyle getStyle();

		/** Returns the name of the node's style if set or null otherwise. For styles with translated names the
		 * translation key is returned to make the process robust against language setting changes.
		 * It's guaranteed that <code>node.style.name = node.style.name</code> does not change the style.
		 * @since 1.2.2 */
		String getName();
		
		Node getStyleNode();

		Color getBackgroundColor();

		/** returns HTML color spec like #ff0000 (red) or #222222 (darkgray).
		 *  @since 1.2 */
		String getBackgroundColorCode();

		Edge getEdge();

		Font getFont();

		/** @deprecated since 1.2 - use {@link #getTextColor()} instead. */
		Color getNodeTextColor();

		/** @since 1.2 */
		Color getTextColor();

		String getTextColorCode();
	}

	/** Node's style: <code>node.style</code> - read-write. */
	interface NodeStyle extends NodeStyleRO {
		void setStyle(IStyle style);

		/** Selects a style by name, see menu Styles -> Pre/Userdefined styles for valid style names or use
		 * {@link #getName()} to display the name of a node's style.
		 * It's guaranteed that <code>node.style.name = node.style.name</code> does not change the style.
		 * @param styleName can be the name visible in the style menu or its translation key as returned by
		 *        {@link #getName()}. (Names of predefined styles are subject to translation.)
		 *        Only translation keys will continue to work if the language setting is changed.
		 * @throws IllegalArgumentException if the style does not exist.
		 * @since 1.2.2 */
		void setName(String styleName);

		void setBackgroundColor(Color color);

		/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
		 *  @since 1.2 */
		void setBackgroundColorCode(String rgbString);

		/** @deprecated since 1.2 - use {@link #setTextColor(Color)} instead. */
		void setNodeTextColor(Color color);

		/** @since 1.2 */
		void setTextColor(Color color);

		/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
		 *  @since 1.2 */
		void setTextColorCode(String rgbString);
	}
}
