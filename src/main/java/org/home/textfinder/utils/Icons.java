package org.home.textfinder.utils;

import javafx.scene.image.Image;
import lombok.Getter;

/**
 * Stores image view icons.
 *
 * @author Sergei Viacheslaev
 */
@Getter
public enum Icons {
    DIRECTORY_EXPANDED(
            new Image(FileTreeUtils.class.getResourceAsStream("/static/images/folder-expanded.png")));

    private Image image;

    Icons(Image image) {
        this.image = image;
    }
}
