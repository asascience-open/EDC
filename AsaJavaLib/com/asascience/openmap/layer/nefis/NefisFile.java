/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NefisFile.java
 *
 * Created on Jun 10, 2008, 10:42:15 AM
 *
 */
package com.asascience.openmap.layer.nefis;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author cmueller_mac
 */
public class NefisFile {

	private String fileType = "NEFIS";
	private String subType = "";
	private String filename = "";
	private String datExt = ".dat";
	private String defExt = ".def";
	private String format = "b";
	private List<GroupData> grpDat;
	private List<GroupDef> grpDef;
	private List<CellDef> celDef;
	private List<ElementDef> elmDef;

	/** Creates a new instance of NefisFile */
	public NefisFile() {

		grpDat = new ArrayList<GroupData>();
		grpDef = new ArrayList<GroupDef>();
		celDef = new ArrayList<CellDef>();
		elmDef = new ArrayList<ElementDef>();
	}

	public GroupData getGroupDataByName(String grpDatName) {
		for (GroupData g : this.grpDat) {
			if (g.getName().equals(grpDatName)) {
				return g;
			}
		}
		return null;
	}

	public List<String> getGroupDefNames() {
		List<String> ret = new ArrayList<String>();
		for (GroupDef g : grpDef) {
			ret.add(g.getName());
		}
		return ret;
	}

	public GroupDef getGroupDefByName(String groupName) {
		for (GroupDef g : grpDef) {
			if (g.getName().equals(groupName)) {
				return g;
			}
		}
		return null;
	}

	public String getGroupNameForElementName(String elementName) {
		for (GroupDef g : grpDef) {
			for (String s : getElementDefNamesForGroupName(g.getName())) {
				if (s.equals(elementName)) {
					return g.getName();
				}
			}
		}
		return null;
	}

	public List<String> getElementDefNamesForGroupName(String groupName) {
		List<String> ret = new ArrayList<String>();
		GroupDef gd = getGroupDefByName(groupName);
		if (gd != null) {
			List<Integer> edi = celDef.get(gd.getCelIndex()).getElm();
			ElementDef ed;
			for (int e : edi) {
				ed = elmDef.get(e);
				ret.add(ed.getName());
			}
		}

		return ret;
	}

	public List<String> getAllElementDefNames() {
		List<String> ret = new ArrayList<String>();
		for (ElementDef e : elmDef) {
			ret.add(e.getName());
		}
		return ret;
	}

	public String getElementNameByDescription(String elementDesc) {
		for (ElementDef e : elmDef) {
			if (e.getDescription().equals(elementDesc)) {
				return e.getName();
			}
		}
		return null;
	}

	public ElementDef getElementDefByName(String elementName) {
		for (ElementDef e : elmDef) {
			if (e.getName().equals(elementName)) {
				return e;
			}
		}
		return null;
	}

	public void printDetailsToSystem() {
		System.out.println(printDetails());
	}

	public String printDetails() {
		StringBuilder masterString = new StringBuilder();
		String lineEnd = "\n\r";

		masterString.append(lineEnd);
		masterString.append(lineEnd);
		masterString.append("---------------------------");
		masterString.append(lineEnd);
		masterString.append(lineEnd);

		masterString.append("Nefis File: ");
		masterString.append(filename);
		masterString.append(lineEnd);
		masterString.append("File Type: ");
		masterString.append(fileType);
		masterString.append(lineEnd);
		masterString.append("Sub Type: ");
		masterString.append(subType);
		masterString.append(lineEnd);
		masterString.append("Data Ext: ");
		masterString.append(datExt);
		masterString.append(lineEnd);
		masterString.append("Def Ext: ");
		masterString.append(defExt);
		masterString.append(lineEnd);
		masterString.append("Format: ");
		masterString.append(format);
		masterString.append(lineEnd);

		// TODO: fix the rest of the masterString.append() so that there aren't
		// "+" strings...
		masterString.append("Group Data:" + lineEnd);
		StringBuilder sb;
		for (GroupData g : grpDat) {
			masterString.append("  Offset: " + g.getOffset() + lineEnd);
			masterString.append("  Var Dim: " + g.getVarDim() + lineEnd);
			masterString.append("  Name: " + g.getName() + lineEnd);
			masterString.append("  Def Name: " + g.getDefName() + lineEnd);
			masterString.append("  Def Index: " + g.getDefIndex() + lineEnd);
			masterString.append("  IANames: " + g.getIANames() + lineEnd);
			sb = new StringBuilder();
			for (int i : g.getIAValues()) {
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append(i);
			}
			masterString.append("  IAValues: " + sb.toString() + lineEnd);
			masterString.append("  RANames: " + g.getRANames() + lineEnd);
			sb = new StringBuilder();
			for (float i : g.getRAValues()) {
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append(i);
			}
			masterString.append("  RAValues: " + sb.toString() + lineEnd);
			masterString.append("  SANames: " + g.getSANames() + lineEnd);
			masterString.append("  SAValues: " + g.getSAValues() + lineEnd);
			sb = new StringBuilder();
			for (int i : g.getSizeDim()) {
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append(i);
			}
			masterString.append("  Size Dim: " + sb.toString() + lineEnd);
			masterString.append("  Order Dim: " + g.getOrderDim() + lineEnd);

			masterString.append(lineEnd);
		}

		masterString.append(lineEnd);
		masterString.append("Group Def:" + lineEnd);
		for (GroupDef g : grpDef) {
			masterString.append("  Offset: " + g.getOffset() + lineEnd);
			masterString.append("  Name: " + g.getName() + lineEnd);
			masterString.append("  Cell Name: " + g.getCelName() + lineEnd);
			masterString.append("  Cell Index: " + g.getCelIndex() + lineEnd);
			sb = new StringBuilder();
			for (int i : g.getSizeDim()) {
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append(i);
			}
			masterString.append("  Size Dim: " + sb.toString() + lineEnd);
			masterString.append("  Order Dim: " + g.getOrderDim() + lineEnd);

			masterString.append(lineEnd);
		}

		masterString.append(lineEnd);
		masterString.append("Cell Def:" + lineEnd);
		for (CellDef g : celDef) {
			masterString.append("  Offset: " + g.getOffset() + lineEnd);
			masterString.append("  Name: " + g.getName() + lineEnd);
			sb = new StringBuilder();
			for (int i : g.getElm()) {
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append(i);
			}
			masterString.append("  Elm: " + sb.toString() + lineEnd);

			masterString.append(lineEnd);
		}

		masterString.append(lineEnd);
		masterString.append("Elm Def:" + lineEnd);
		for (ElementDef g : elmDef) {
			masterString.append("  Offset: " + g.getOffset() + lineEnd);
			masterString.append("  Name: " + g.getName() + lineEnd);
			masterString.append("  Type: " + g.getType() + lineEnd);
			masterString.append("  Size Val: " + g.getSizeVal() + lineEnd);
			masterString.append("  Size Elm: " + g.getSizeElm() + lineEnd);
			masterString.append("  Quantity: " + g.getQuantity() + lineEnd);
			masterString.append("  Units: " + g.getUnits() + lineEnd);
			masterString.append("  Descitption: " + g.getDescription() + lineEnd);
			sb = new StringBuilder();
			for (int i : g.getSize()) {
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append(i);
			}
			masterString.append("  Size: " + sb.toString() + lineEnd);

			masterString.append(lineEnd);
		}

		masterString.append(lineEnd);
		masterString.append("---------------------------" + lineEnd);
		masterString.append(lineEnd);

		return masterString.toString();
	}

	public void printLinkagesToSystem() {
		System.out.print(printLinkages());
	}

	public String printLinkages() {
		StringBuilder masterString = new StringBuilder();
		String lineEnd = "\n\r";

		masterString.append(lineEnd);
		masterString.append(lineEnd);
		masterString.append("---------------------------");
		masterString.append(lineEnd);
		masterString.append(lineEnd);

		masterString.append("Nefis File: ");
		masterString.append(filename);
		masterString.append(lineEnd);
		masterString.append("File Type: ");
		masterString.append(fileType);
		masterString.append(lineEnd);
		masterString.append("Sub Type: ");
		masterString.append(subType);
		masterString.append(lineEnd);
		masterString.append("Data Ext: ");
		masterString.append(datExt);
		masterString.append(lineEnd);
		masterString.append("Def Ext: ");
		masterString.append(defExt);
		masterString.append(lineEnd);
		masterString.append("Format: ");
		masterString.append(format);
		masterString.append(lineEnd);

		masterString.append("Linkages:");
		masterString.append(lineEnd);
		for (GroupDef gd : grpDef) {
			masterString.append("  Group: ");
			masterString.append(gd.getName());
			masterString.append(lineEnd);
			int cdi = gd.getCelIndex();
			CellDef cd = celDef.get(cdi);
			masterString.append("    Cell: ");
			masterString.append(cd.getName());
			masterString.append(lineEnd);
			List<Integer> edi = cd.getElm();
			ElementDef ed;
			for (int e : edi) {
				ed = elmDef.get(e);
				masterString.append("      Element: [");
				masterString.append(e);
				masterString.append("]");
				masterString.append(ed.getName());
				masterString.append("(");
				masterString.append(ed.getType());
				masterString.append(") - ");
				masterString.append(ed.getDescription());
				masterString.append(lineEnd);
			}
		}

		masterString.append(lineEnd);
		masterString.append("---------------------------");
		masterString.append(lineEnd);
		masterString.append(lineEnd);

		return masterString.toString();
	}

	// <editor-fold defaultstate="collapsed" desc=" Gets & Sets ">
	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDatExt() {
		return datExt;
	}

	public void setDatExt(String datExt) {
		this.datExt = datExt;
	}

	public String getDefExt() {
		return defExt;
	}

	public void setDefExt(String defExt) {
		this.defExt = defExt;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void addGroupData(GroupData grpData) {
		this.grpDat.add(grpData);
	}

	public int getGroupDataIndex(String grpDatName) {
		for (int i = 0; i < this.grpDat.size(); i++) {
			if (grpDatName.equals(this.grpDat.get(i).getName())) {
				return i;
			}
		}
		return -1;
	}

	public List<GroupData> getGrpDat() {
		return grpDat;
	}

	public void setGrpDat(List<GroupData> grpDat) {
		this.grpDat = grpDat;
	}

	public void addGroupDef(GroupDef grpDef) {
		this.grpDef.add(grpDef);
	}

	public List<GroupDef> getGrpDef() {
		return grpDef;
	}

	public void setGrpDef(List<GroupDef> grpDef) {
		this.grpDef = grpDef;
	}

	public void addCellDef(CellDef celDef) {
		this.celDef.add(celDef);
	}

	public List<CellDef> getCelDef() {
		return celDef;
	}

	public void setCelDef(List<CellDef> celDef) {
		this.celDef = celDef;
	}

	public void addElementDef(ElementDef elmDef) {
		this.elmDef.add(elmDef);
	}

	public List<ElementDef> getElmDef() {
		return elmDef;
	}

	public void setElmDef(List<ElementDef> elmDef) {
		this.elmDef = elmDef;
	}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" Sub-Classes ">
	public static class GroupData {

		private int offset;
		private int varDim;
		private String name;
		private String defName;
		private int defIndex;
		private String iANames;
		private int[] iAValues;
		private String rANames;
		private float[] rAValues;
		private String sANames;
		private String sAValues;
		private List<Integer> sizeDim;
		private int orderDim;

		public GroupData() {
			sizeDim = new ArrayList<Integer>();
		}

		// <editor-fold defaultstate="collapsed" desc=" Gets & Sets ">
		public int getOffset() {
			return offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

		public int getVarDim() {
			return varDim;
		}

		public void setVarDim(int varDim) {
			this.varDim = varDim;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDefName() {
			return defName;
		}

		public void setDefName(String defName) {
			this.defName = defName;
		}

		public int getDefIndex() {
			return defIndex;
		}

		public void setDefIndex(int defIndex) {
			this.defIndex = defIndex;
		}

		public String getIANames() {
			return iANames;
		}

		public void setIANames(String iANames) {
			this.iANames = iANames;
		}

		public int[] getIAValues() {
			return iAValues;
		}

		public void setIAValues(int[] iAValues) {
			this.iAValues = iAValues;
		}

		public String getRANames() {
			return rANames;
		}

		public void setRANames(String rANames) {
			this.rANames = rANames;
		}

		public float[] getRAValues() {
			return rAValues;
		}

		public void setRAValues(float[] rAValues) {
			this.rAValues = rAValues;
		}

		public String getSANames() {
			return sANames;
		}

		public void setSANames(String sANames) {
			this.sANames = sANames;
		}

		public String getSAValues() {
			return sAValues;
		}

		public void setSAValues(String sAValues) {
			this.sAValues = sAValues;
		}

		public List<Integer> getSizeDim() {
			return sizeDim;
		}

		public void addSizeDim(int size) {
			this.sizeDim.add(size);
		}

		public void setSizeDim(int index, int size) {
			this.sizeDim.set(index, size);
		}

		public void setSizeDim(List<Integer> sizeDim) {
			this.sizeDim = sizeDim;
		}

		public int getOrderDim() {
			return orderDim;
		}

		public void setOrderDim(int orderDim) {
			this.orderDim = orderDim;
		} // </editor-fold>
	}

	public static class GroupDef {

		private int offset;
		private String name;
		private String celName;
		private int celIndex;
		private List<Integer> sizeDim;
		private int orderDim;

		public GroupDef() {
			sizeDim = new ArrayList<Integer>();
		}

		// <editor-fold defaultstate="collapsed" desc=" Gets & Sets ">
		public int getOffset() {
			return offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCelName() {
			return celName;
		}

		public void setCelName(String celName) {
			this.celName = celName;
		}

		public int getCelIndex() {
			return celIndex;
		}

		public void setCelIndex(int celIndex) {
			this.celIndex = celIndex;
		}

		public List<Integer> getSizeDim() {
			return sizeDim;
		}

		public void addSizeDim(int size) {
			this.sizeDim.add(size);
		}

		public void setSizeDim(int index, int size) {
			this.sizeDim.set(index, size);
		}

		public void setSizeDim(List<Integer> sizeDim) {
			this.sizeDim = sizeDim;
		}

		public int getOrderDim() {
			return orderDim;
		}

		public void setOrderDim(int orderDim) {
			this.orderDim = orderDim;
		} // </editor-fold>
	}

	public static class CellDef {

		private int offset;
		private String name;
		private List<Integer> elm;

		public CellDef() {
			elm = new ArrayList<Integer>();
		}

		// <editor-fold defaultstate="collapsed" desc=" Gets & Sets ">
		public int getOffset() {
			return offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void addElement(int elmIndex) {
			this.elm.add(elmIndex);
		}

		public List<Integer> getElm() {
			return elm;
		}

		public void setElm(int index, int elm) {
			this.elm.set(index, elm);
		}

		public void setElm(List<Integer> elm) {
			this.elm = elm;
		} // </editor-fold>
	}

	public static class ElementDef {

		private int offset;
		private String name;
		private int type;
		private int sizeVal;
		private int sizeElm;
		private String quantity;
		private String units;
		private String description;
		private List<Integer> size;

		public ElementDef() {
		}

		// <editor-fold defaultstate="collapsed" desc=" Gets & Sets ">
		public int getOffset() {
			return offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int getSizeVal() {
			return sizeVal;
		}

		public void setSizeVal(int sizeVal) {
			this.sizeVal = sizeVal;
		}

		public int getSizeElm() {
			return sizeElm;
		}

		public void setSizeElm(int sizeElm) {
			this.sizeElm = sizeElm;
		}

		public String getQuantity() {
			return quantity;
		}

		public void setQuantity(String quantity) {
			this.quantity = quantity;
		}

		public String getUnits() {
			return units;
		}

		public void setUnits(String units) {
			this.units = units;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public List<Integer> getSize() {
			return size;
		}

		public void setSize(List<Integer> size) {
			this.size = size;
		} // </editor-fold>
	} // </editor-fold>
}
