/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zygon.rl.ui;

/**
 *
 * @author zygon
 */
public class RowComponent extends RenderableBase {

    private final Renderable top;
    private final Renderable bottom;
    private final double bottomPct;

    /**
     *
     * @param top
     * @param bottom
     * @param bottomPct
     */
    public RowComponent(Renderable top, Renderable bottom, double bottomPct) {
        this.top = top;
        this.bottom = bottom;
        if (bottomPct <= 0.0 || bottomPct > 1.0) {
            throw new IllegalArgumentException("bottomPct must between 0 and 1.0");
        }
        this.bottomPct = bottomPct;
    }

    @Override
    public void render() {
        top.render();
        bottom.render();
    }

//    @Override
//    public void render(int x, int y, int width, int height) {
//
//        int totalWidth = width;
//        int totalHeight = height;
//
//        double yMargin = totalHeight * bottomPct;
//
//        bottom.render(x, y, totalWidth, (int) yMargin);
//        top.render(x, y + (int) yMargin, totalWidth, totalHeight - (int) yMargin);
//    }
}
