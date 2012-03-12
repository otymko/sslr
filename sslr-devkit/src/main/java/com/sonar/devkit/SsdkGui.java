/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.devkit;

import com.sonar.sslr.api.*;
import com.sonar.sslr.impl.Parser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.colorizer.HtmlOptions;
import org.sonar.colorizer.HtmlRenderer;
import org.sonar.colorizer.Tokenizer;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

@SuppressWarnings("serial")
public class SsdkGui extends javax.swing.JFrame {

  private static final String CSS_PATH = "/com/sonar/sslr/devkit/codeEditor.css";
  private static final Logger LOG = LoggerFactory.getLogger("DevKit");

  private final JFileChooser fileChooser = new JFileChooser();
  private final JButton openButton = new JButton();
  private final JTree astTree = new JTree();
  private final JEditorPane codeEditor = new JEditorPane();
  private final JScrollPane scrollPane = new JScrollPane(codeEditor);
  private final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, astTree);

  private final Parser<? extends Grammar> parser;
  private final List<Tokenizer> colorizerTokenizers;
  private final HtmlRenderer htmlRenderer = new HtmlRenderer(new HtmlOptions(false, null, false));

  public SsdkGui(Parser<? extends Grammar> parser, List<Tokenizer> colorizerTokenizers) {
    this.parser = parser;
    this.colorizerTokenizers = colorizerTokenizers;

    setLayout(new BorderLayout(2, 2));
    setDefaultCloseOperation(SsdkGui.EXIT_ON_CLOSE);

    openButton.setText("Open source file");
    openButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int returnVal = fileChooser.showOpenDialog(SsdkGui.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

          File file = fileChooser.getSelectedFile();
          loadFromFile(file);
        }
      }
    });
    add(openButton, BorderLayout.PAGE_END);

    astTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    astTree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        TreePath[] selectedPaths = astTree.getSelectionPaths();
        if (selectedPaths != null) {
          for (TreePath selectedPath : selectedPaths) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
          }
        }
      }
    });

    codeEditor.setContentType("text/html");
    codeEditor.setEditable(false);

    splitPane.setDividerLocation(500);
    add(splitPane, BorderLayout.CENTER);

    loadFromString("");
  }

  private void loadFromFile(File file) {
    try {
      loadFromString(FileUtils.readFileToString(file));
    } catch (IOException e) {
      LOG.error("Unable to load the code file '" + file.getAbsolutePath() + "'", e);
    }
  }

  private void loadFromString(String code) {
    showCode(code);
    showAst(code);
  }

  private void showCode(String code) {
    StringBuffer sb = new StringBuffer();
    sb.append("<html><head><style type=\"text/css\">");
    sb.append(getCss());
    sb.append("</style></head><body><pre class=\"code\">");
    sb.append(htmlRenderer.render(new StringReader(code), colorizerTokenizers));
    sb.append("</pre></body></html>");

    codeEditor.setText(sb.toString());
  }

  private void showAst(String code) {
    astTree.setModel(new DefaultTreeModel(null));

    if (!code.isEmpty()) {
      try {
        AstNode astNode = parser.parse(code);
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(astNode);

        addChildNodes(treeNode, astNode);

        astTree.setModel(new DefaultTreeModel(treeNode));
      } catch (RecognitionException re) {
        LOG.error("Unable to parse the code.", re);
      }
    }
  }

  private void addChildNodes(DefaultMutableTreeNode treeNode, AstNode astNode) {
    if (astNode.hasChildren()) {
      for (AstNode astNodeChild : astNode.getChildren()) {
        DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(astNodeChild);
        treeNode.add(treeNodeChild);
        addChildNodes(treeNodeChild, astNodeChild);
      }
    } else if (astNode.hasToken() && astNode.getToken().hasTrivia()) {
      for (Trivia trivia : astNode.getToken().getTrivia()) {
        DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(trivia);
        treeNode.add(treeNodeChild);

        if (trivia.hasPreprocessingDirective()) {
          PreprocessingDirective directive = trivia.getPreprocessingDirective();
          DefaultMutableTreeNode treeNodeInnerChild = new DefaultMutableTreeNode(directive.getAst());
          treeNodeChild.add(treeNodeInnerChild);
          addChildNodes(treeNodeInnerChild, directive.getAst());
        }
      }
    }
  }

  private Object getCss() {
    try {
      return IOUtils.toString(SsdkGui.class.getResourceAsStream(CSS_PATH));
    } catch (IOException e) {
      LOG.error("Unable to load the CSS file '" + CSS_PATH + "'", e);
      throw new RuntimeException(e);
    }
  }

}
