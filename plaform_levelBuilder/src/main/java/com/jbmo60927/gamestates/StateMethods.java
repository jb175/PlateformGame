package com.jbmo60927.gamestates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public interface StateMethods {
    public void update();
    public void draw(Graphics g);

    public void mouseClicked(MouseEvent e);
    public void mousePressed(MouseEvent e);
    public void mouseReleased(MouseEvent e);
    public void mouseMoved(MouseEvent e);

    public void keyPressed(KeyEvent e);
    public void keyReleased(KeyEvent e);

    public void mouseWheelMoved(MouseWheelEvent e);
}