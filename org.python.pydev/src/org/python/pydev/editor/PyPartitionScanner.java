/*
 * Author: atotic
 * Created: July 10, 2003
 * License: Common Public License v1.0
 */

package org.python.pydev.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

/**
 * Rule-based partition scanner
 * 
 * Simple, fast parsing of the document into partitions.<p>
 * This is like a rough 1st pass at parsing. We only parse
 * out for comments, single-line strings, and multiline strings<p>
 * The results are parsed again inside {@link org.python.pydev.editor.PyEditConfiguration#getPresentationReconciler}
 * and colored there.<p>
 * 
 * "An IPartitionTokenScanner can also start in the middle of a partition,
 * if it knows the type of the partition."
 */
public class PyPartitionScanner extends RuleBasedPartitionScanner {
	public final static String PY_COMMENT = "__python_comment";
	public final static String PY_SINGLELINE_STRING = "__python_singleline_string";
	public final static String PY_MULTILINE_STRING = "__python_multiline_string";
    
    public final static String[] types = {PY_COMMENT, PY_SINGLELINE_STRING, PY_MULTILINE_STRING};
	public PyPartitionScanner() {
		super();
		List rules = new ArrayList();

		addCommentRule(rules);
		addMultilineStringRule(rules);
		addSinglelineStringRule(rules);
		
		IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}

	private void addSinglelineStringRule(List rules) {
		IToken singleLineString = new Token(PY_SINGLELINE_STRING);
		// deal with "" and '' strings
		rules.add(new SingleLineRule("\"", "\"", singleLineString, '\\'));
		rules.add(new SingleLineRule("'", "'", singleLineString, '\\'));
	}

	private void addMultilineStringRule(List rules) {
		IToken multiLineString = new Token(PY_MULTILINE_STRING);
		// deal with ''' and """ strings
		rules.add(new MultiLineRule("'''", "'''", multiLineString, '\\'));
		rules.add(new MultiLineRule("\"\"\"", "\"\"\"", multiLineString,'\\'));
	}

	private void addCommentRule(List rules) {
		IToken comment = new Token(PY_COMMENT);
		rules.add(new EndOfLineRule("#", comment));
	}
	
	/**
	 * @return all types recognized by this scanner (used by doc partitioner)
	 */
	static public String[] getTypes() {
		return types;
	}

    /**
     * @param element
     * @param document
     */
    public static void addPartitionScanner(IDocument document) {
        if (document != null) {
            IDocumentPartitioner partitioner2 = document.getDocumentPartitioner();
            if(partitioner2 == null){
    		    DefaultPartitioner partitioner = new DefaultPartitioner(new PyPartitionScanner(), getTypes());
                partitioner.connect(document);
                document.setDocumentPartitioner(partitioner);
            }
        }
    }
}
