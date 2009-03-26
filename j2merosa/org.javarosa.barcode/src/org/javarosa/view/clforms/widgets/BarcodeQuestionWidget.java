package org.javarosa.view.clforms.widgets;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.TextField;

import org.javarosa.barcode.acquire.BarcodeCaptureScreen;
import org.javarosa.barcode.process.IBarcodeProcessingService;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.clforms.acquire.AcquireScreen;
import org.javarosa.formmanager.view.clforms.acquire.AcquiringQuestionScreen;

/**
 * @author mel
 * 
 *         A SingleQuestionScreen-syle widget that adds a scan command for
 *         scaning a barcode
 * 
 */
public class BarcodeQuestionWidget extends AcquiringQuestionScreen {

	protected TextField tf;
	protected IBarcodeProcessingService barcodeProcessor;

	public BarcodeQuestionWidget(FormElementBinding question) {
		super(question);
	}

	public BarcodeQuestionWidget(FormElementBinding prompt, int num) {
		super(prompt, num);
	}

	public BarcodeQuestionWidget(FormElementBinding prompt, String str) {
		super(prompt, str);
	}

	public BarcodeQuestionWidget(FormElementBinding prompt, char c) {
		super(prompt, c);
	}

	public void creatView() {

		setHint("Type in your answer");
		//#style textBox
		 tf = new TextField("", "", 200, TextField.ANY);
		 if(qDef.instanceNode.required)
				tf.setLabel("*"+((QuestionDef)qDef.element).getLongText()); //visual symbol for required
				else
					tf.setLabel(((QuestionDef)qDef.element).getLongText());
		this.append(tf);
		this.addNavigationButtons();
		if (((QuestionDef)qDef.element).getHelpText()!=null){
			setHint(((QuestionDef)qDef.element).getHelpText());
		}

	}

	public Command getAcquireCommand() {
		return new Command("Scan", Command.SCREEN, 3);
	}

	public AcquireScreen getAcquireScreen(CommandListener callingListener) {
		BarcodeCaptureScreen bcScreen = new BarcodeCaptureScreen(
				"Scan barcode", this, callingListener);
		bcScreen.setBarcodeProcessor(barcodeProcessor);
		return bcScreen;
	}

	public IAnswerData getWidgetValue() {
		String s = tf.getString();
		return (s == null || s.equals("") ? null : new StringData(s));
	}

	protected void updateDisplay() {
		tf.setString(((StringData) acquiredData).getDisplayText());
	}

	public void setBarcodeProcessor(IBarcodeProcessingService barcodeProcessor) {
		this.barcodeProcessor = barcodeProcessor;

	}

}