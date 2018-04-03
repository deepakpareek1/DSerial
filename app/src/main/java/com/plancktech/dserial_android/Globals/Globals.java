package com.plancktech.dserial_android.Globals;

import android.app.Application;

import java.util.ArrayList;

public class Globals extends Application {

    private String _message;
    private ArrayList _serverMessageQue;
    private ArrayList _clientMessageQue;

    public String get_message() {
        return _message;
    }

    public void set_message(String _message) {
        this._message = _message;
    }

    public ArrayList get__serverMessageQue() {
        return _serverMessageQue;
    }

    public void set__serverMessageQue(ArrayList _serverMessageQue) {
        this._serverMessageQue = _serverMessageQue;
    }

    public ArrayList get_clientMessageQue() {
        return _clientMessageQue;
    }

    public void set_clientMessageQue(ArrayList _clientMessageQue) {
        this._clientMessageQue = _clientMessageQue;
    }
}
