package org.javarosa.core.model;

import org.javarosa.formmanager.model.temp.QuestionData;

import org.javarosa.core.model.utils.ITreeVisitor;

/**
 * QuestionDataElement is a TreeElement of a DataModelTree that is a leaf
 * which contains the answer to a QuestionDef. 
 * 
 * In an XML Analogy, this represents a terminal element in the XML tree.
 * 
 * @author Clayton Sims
 *
 */
public class QuestionDataElement extends TreeElement{

	/** The actual question data value */
	private QuestionData value;
	
	/** A Binding for the Question Definition */
	private IDataReference reference;
	
	/**
	 * Creates a new QuestionDataElement for the question defined
	 * by the name and reference provided
	 * 
	 * @param name The name of this TreeElement
	 * @param reference The reference for Question Definitions
	 */
	public QuestionDataElement(String name, IDataReference reference) {
		this.name = name;
		this.reference = reference;
	}
	
	/**
	 * Creates a new QuestionDataElement for the question defined
	 * by the name and reference provided, and sets its value to that 
	 * provided.
	 *  
	 * @param name The name of this TreeElement
	 * @param reference The reference for Question Definitions
	 * @param value The value for this Question Definition
	 */
	public QuestionDataElement(String name, IDataReference reference, QuestionData value) {
		this(name, reference);
		this.value = value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.TreeElement#getName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return The value for the question defined by IBinding
	 */
	public QuestionData getValue(){ 
		return value;
	}
	
	/**
	 * Sets the value for the question defined by IBinding
	 * @param value The question's answer value
	 */
	public void setValue(QuestionData value) {
		this.value = value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.TreeElement#isLeaf()
	 */
	public boolean isLeaf() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.TreeElement#setRoot(org.javarosa.core.model.TreeElement)
	 */
	protected void setRoot(TreeElement root) {
		this.root = root;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.TreeElement#contains(org.javarosa.core.model.TreeElement)
	 */
	public boolean contains(TreeElement element) {
		if(this == element) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean matchesReference(IDataReference reference) {
		if(this.reference == null) {
			return false;
		}
		else {
			return this.reference.referenceMatches(reference);
		}
	}
	
	/**
	 * @return a string representing the value of this question's answer
	 */
	public String createStringValue() {
		return value.toString();
	}
	/**
	 * Visitor pattern acceptance method.
	 * @param visitor The visitor traveling this tree
	 */
	public void accept(ITreeVisitor visitor) {
		visitor.visit(this);
	}
}
