/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zygon.rl.soccer.ui;

import com.zygon.rl.soccer.game.GameImpl;

/**
 *
 * @author zygon
 */
public class Runner {

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        UserInterface ui = new UserInterface(new GameImpl());
        ui.start();
    }
}
