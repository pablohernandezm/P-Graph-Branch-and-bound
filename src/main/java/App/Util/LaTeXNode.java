package App.Util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * LaTeXNode class is used to create LaTeX images.
 *
 * @author Pablo Hernández
 * @author Juan Camilo Narváez
 */
public class LaTeXNode {
    public static ImageView createLaTeXImage(String latex, int size, Color color) {
        if (color == null) color = Color.BLACK;

        TeXFormula formula = new TeXFormula(latex);
        TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, size);
        icon.setForeground(color);
        icon.setInsets(new Insets(5, 5, 5, 5));

        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        icon.paintIcon(new javax.swing.JLabel(), image.getGraphics(), 0, 0);

        return new ImageView(SwingFXUtils.toFXImage(image, null));
    }
}
