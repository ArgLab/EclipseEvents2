package com.arglab.eclipsedatacollector.jenkins.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JenkinsView extends ViewPart {

	public static final String ID = "com.arglab.eclipsedatacollector.jenkins.ui.JenkinsView";

	private TreeViewer viewer;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		viewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

		viewer.setContentProvider(new JSONContentProvider());
		viewer.setLabelProvider(new JSONLabelProvider());

		Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);

		TreeColumn column = new TreeColumn(tree, SWT.LEFT);
		column.setText("JSON file");
		column.setWidth(400);

		viewer.addTreeListener(new ITreeViewerListener() {
			
			@Override
			public void treeExpanded(org.eclipse.jface.viewers.TreeExpansionEvent event) {
				Object element = event.getElement();
				if (element instanceof JSONTreeNode) {
					JSONTreeNode node = (JSONTreeNode) element;
					System.out.println("Expanded: " + node.getKey());
				}
			}

			@Override
			public void treeCollapsed(org.eclipse.jface.viewers.TreeExpansionEvent event) {
				Object element = event.getElement();
				if (element instanceof JSONTreeNode) {
					JSONTreeNode node = (JSONTreeNode) element;
					System.out.println("Collapsed: " + node.getKey());
				}
			}
		});

		fetchDataAndDisplay();
	}

	private void fetchDataAndDisplay() {
		InputStream is = getClass().getResourceAsStream("sample.json");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder jsonContent = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				jsonContent.append(line);
			}

			JsonElement rootElement = JsonParser.parseString(jsonContent.toString());
			
			JsonObject transformedData = transformDataByClassName(rootElement.getAsJsonObject());
			
			viewer.setInput(transformedData);
			viewer.expandToLevel(2);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JsonObject transformDataByClassName(JsonObject originalData) {
		JsonObject result = new JsonObject();
		
		Map<String, JsonObject> classBuckets = new TreeMap<>();
		

		String[] classArrays = {"checkstyleNotifications", "pmdNotifications", "coverageData", 
								"studentUnitTests", "tsUnitTests", "countsData"};
		
		for (String arrayName : classArrays) {
			if (originalData.has(arrayName) && originalData.get(arrayName).isJsonArray()) {
				JsonArray array = originalData.getAsJsonArray(arrayName);
				
				for (JsonElement element : array) {
					if (element.isJsonObject()) {
						JsonObject obj = element.getAsJsonObject();
						String className = extractClassName(obj, arrayName);
						
						if (className != null && !className.trim().isEmpty()) {
							if (!classBuckets.containsKey(className)) {
								classBuckets.put(className, new JsonObject());
							}
							
							JsonObject classBucket = classBuckets.get(className);

							if (arrayName.equals("studentUnitTests") || arrayName.equals("tsUnitTests")) {
								if (!classBucket.has(arrayName)) {
									classBucket.add(arrayName, new JsonObject());
								}
								
								JsonObject testContainer = classBucket.getAsJsonObject(arrayName);
								addTestMethod(testContainer, obj);
							} else {
								// Regular handling for other arrays
								if (!classBucket.has(arrayName)) {
									classBucket.add(arrayName, new JsonArray());
								}
								classBucket.getAsJsonArray(arrayName).add(element);
							}
						}
					}
				}
			}
		}
		
		for (Map.Entry<String, JsonObject> entry : classBuckets.entrySet()) {
			result.add(entry.getKey(), entry.getValue());
		}
		
		JsonObject otherDetails = new JsonObject();
		for (Map.Entry<String, JsonElement> entry : originalData.entrySet()) {
			String key = entry.getKey();
			boolean isClassArray = false;
			
			for (String arrayName : classArrays) {
				if (key.equals(arrayName)) {
					isClassArray = true;
					break;
				}
			}
			
			if (!isClassArray) {
				otherDetails.add(key, entry.getValue());
			}
		}
		
		if (otherDetails.size() > 0) {
			result.add("Other Details", otherDetails);
		}
		
		return result;
	}
	
	private void addTestMethod(JsonObject testContainer, JsonObject testObj) {
		String methodName = testObj.has("methodName") ? testObj.get("methodName").getAsString() : "unknown";
		boolean failed = testObj.has("fail") ? testObj.get("fail").getAsBoolean() : false;
		String failMsg = testObj.has("failMsg") ? testObj.get("failMsg").getAsString() : "";
		
		String status = failed ? "fail" : "pass";
		if (failed && !failMsg.trim().isEmpty()) {
			status += " - " + failMsg;
		}
		
		testContainer.addProperty(methodName, status);
	}
	
	private String extractClassName(JsonObject obj, String arrayType) {
		String className = null;

		switch (arrayType) {
			case "checkstyleNotifications":
			case "pmdNotifications":
				className = obj.has("className") ? obj.get("className").getAsString() : null;
				break;
			case "coverageData":
				className = obj.has("classname") ? obj.get("classname").getAsString() : null;
				break;
			case "studentUnitTests":
			case "tsUnitTests":
				className = obj.has("className") ? obj.get("className").getAsString() : null;
				break;
			case "countsData":
				className = obj.has("classname") ? obj.get("classname").getAsString() : null;
				break;
			default:
				return null;
		}
		

		return normalizeClassName(className);
	}
	
	private String normalizeClassName(String className) {
		if (className == null || className.trim().isEmpty()) {
			return className;
		}
		
		if (className.toLowerCase().startsWith("ts") && className.length() > 2) {
			if (Character.isUpperCase(className.charAt(2))) {
				return className.substring(2);
			}
		}

		
		return className;
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	class JSONContentProvider implements ITreeContentProvider {
		
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) inputElement;
				List<JSONTreeNode> elements = new ArrayList<>();

				for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					elements.add(new JSONTreeNode(entry.getKey(), entry.getValue()));
				}

				return elements.toArray();
			} else if (inputElement instanceof JsonArray) {
				JsonArray jsonArray = (JsonArray) inputElement;
				List<JSONTreeNode> elements = new ArrayList<>();

				for (int i = 0; i < jsonArray.size(); i++) {
					elements.add(new JSONTreeNode("[" + i + "]", jsonArray.get(i)));
				}

				return elements.toArray();
			}
			return new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof JSONTreeNode) {
				JSONTreeNode node = (JSONTreeNode) parentElement;
				JsonElement value = node.getValue();

				if (value.isJsonObject()) {
					JsonObject jsonObject = value.getAsJsonObject();
					List<JSONTreeNode> children = new ArrayList<>();

					for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
						children.add(new JSONTreeNode(entry.getKey(), entry.getValue()));
					}

					return children.toArray();
				} else if (value.isJsonArray()) {
					JsonArray jsonArray = value.getAsJsonArray();
					List<JSONTreeNode> children = new ArrayList<>();

					for (int i = 0; i < jsonArray.size(); i++) {
						children.add(new JSONTreeNode("[" + i + "]", jsonArray.get(i)));
					}

					return children.toArray();
				}
			}
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof JSONTreeNode) {
				JSONTreeNode node = (JSONTreeNode) element;
				JsonElement value = node.getValue();
				return value.isJsonObject() || value.isJsonArray();
			}
			return false;
		}
	}

	class JSONLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof JSONTreeNode) {
				JSONTreeNode node = (JSONTreeNode) element;
				String key = node.getKey();
				JsonElement value = node.getValue();

				if (value.isJsonPrimitive()) {
					JsonPrimitive primitive = value.getAsJsonPrimitive();
					if (primitive.isString()) {
						return key + ": \"" + primitive.getAsString() + "\"";
					} else {
						return key + ": " + primitive.toString();
					}
				} else if (value.isJsonObject()) {
					return key;
				} else if (value.isJsonArray()) {
					JsonArray array = value.getAsJsonArray();
					return key + " (" + array.size() + " items)";
				} else if (value.isJsonNull()) {
					return key + ": null";
				}
			}
			return super.getText(element);
		}
	}

	class JSONTreeNode {
		private String key;       
		private JsonElement value; 

		public JSONTreeNode(String key, JsonElement value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public JsonElement getValue() {
			return value;
		}
	}
}