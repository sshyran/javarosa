package org.javarosa.formmanager.view.chatterbox.widget;

import java.util.Date;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.DateField;
import de.enough.polish.ui.Item;

public class DateEntryWidget extends ExpandedWidget {
	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_ENTRY;
	}
	
	protected Item getEntryWidget (QuestionDef question) {
		//#style textBox
		return new DateField(null, DateField.DATE);
	}

	private DateField dateField () {
		return (DateField)entryWidget;    
	}

	protected void updateWidget (QuestionDef question) { /* do nothing */ }
	
	protected void setWidgetValue (Object o) {
		dateField().setDate((Date)o);
	}

	protected IAnswerData getWidgetValue () {
		Date d = dateField().getDate();
		return (d == null ? null : new DateData(d));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle#widgetType()
	 */
	public int widgetType() {
		return Constants.CONTROL_INPUT;
	}
}