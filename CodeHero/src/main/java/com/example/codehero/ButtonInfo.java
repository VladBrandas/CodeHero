package com.example.codehero;

import java.io.Serializable;

public class ButtonInfo implements Serializable {
    private String functionName;
    private String buttonText;

    public ButtonInfo(String functionName) {
        this.functionName = functionName;
        this.buttonText = functionName.substring(0, 1).toUpperCase() + functionName.substring(1);
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getButtonText() {
        return buttonText;
    }
}
