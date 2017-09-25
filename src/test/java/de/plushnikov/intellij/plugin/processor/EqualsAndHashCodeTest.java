package de.plushnikov.intellij.plugin.processor;

import de.plushnikov.intellij.plugin.AbstractLombokParsingTestCase;

/**
 * Unit tests for IntelliJPlugin for Lombok, based on lombok test classes
 */
public class EqualsAndHashCodeTest extends AbstractLombokParsingTestCase {

  public void testEqualsandhashcode$EqualsAndHashCode() throws Exception {
    doTest(true);
  }

  public void testEqualsandhashcode$EqualsAndHashCodeWithExistingMethods() throws Exception {
    doTest(true);
  }

  public void testEqualsandhashcode$EqualsAndHashCodeWithSomeExistingMethods() throws Exception {
    doTest(true);
  }

  public void testEqualsandhashcode$EqualsAndHashCodeExplicitEmptyOf() throws Exception {
    doTest(true);
  }

  public void testEqualsandhashcode$EqualsAndHashCodeExplicitOfAndExclude() throws Exception {
    doTest(true);
  }
}
