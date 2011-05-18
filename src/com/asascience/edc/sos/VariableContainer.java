/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * VariableContainer.java
 *
 * Created on Aug 27, 2009 @ 10:42:27 PM
 */
package com.asascience.edc.sos;

/**
 * 
 * @author DAS <dstuebe@asascience.com>
 */
public class VariableContainer implements Comparable {

  private String procedure;
  private String name;
  private String property;
  private boolean selected = false;
  private String sosRequest;

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public VariableContainer() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getProcedure() {
    return procedure;
  }

  public void setProcedure(String procedure) {
    this.procedure = procedure;
  }

  public void printVariable() {

    System.out.println("Variable Name: " + name);
    System.out.println("Variable Property: " + property);
    System.out.println("Variable Procedure: " + procedure);

  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSosRequest(String sosRequest) {
    this.sosRequest = sosRequest;
  }

  public String getSosRequest() {
    return sosRequest;
  }

  public int compareTo(Object o) {
    return this.name.compareTo(((VariableContainer)o).name);
  }
}
