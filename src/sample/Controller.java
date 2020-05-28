package sample;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class Controller {

    public TextArea chatLog;
    public TextField myMsg;

    public void mySend(){
        chatLog.appendText(myMsg.getText());
        chatLog.appendText("\n");
        myMsg.setText("");
        myMsg.requestFocus();
    }

    public void buttonPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode().toString().equals("ENTER"))
        {
            mySend();
        }
    }

    public void onClickSend(ActionEvent actionEvent) {
        mySend();
    }
}