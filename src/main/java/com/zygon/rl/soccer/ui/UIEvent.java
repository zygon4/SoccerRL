package com.zygon.rl.soccer.ui;

import com.zygon.rl.soccer.ui.UIEvent;
import org.hexworks.zircon.api.uievent.KeyboardEvent;
import org.hexworks.zircon.api.uievent.MouseEvent;

/**
 * For marshaling from the UI to a key/click processor.
 *
 * @author zygon
 */
public record UIEvent(MouseEvent mouseEvent, KeyboardEvent keyboardEvent) {

    public static UIEvent create(MouseEvent mouseEvent) {
        return new UIEvent(mouseEvent, null);
    }

    public static UIEvent create(KeyboardEvent keyboardEvent) {
        return new UIEvent(null, keyboardEvent);
    }
}
