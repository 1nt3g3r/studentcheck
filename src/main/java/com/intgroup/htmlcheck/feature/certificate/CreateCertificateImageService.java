package com.intgroup.htmlcheck.feature.certificate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CreateCertificateImageService {
    public static final String CERTIFICATE_PATH = "./data/certificate.png";

    public static BufferedImage makeCertificate(String text) throws IOException {

        final BufferedImage image = ImageIO.read(new File(CERTIFICATE_PATH));

        int fontSize = 56;
        int textY = 320;
        int imageWidth = image.getWidth();

        Graphics2D g2=(Graphics2D) image.getGraphics().create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
        g2.setFont(font);
        g2.setColor(Color.BLACK);

        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
        int textWidth = (int)(font.getStringBounds(text, frc).getWidth());

        int textX = imageWidth/2 - textWidth/2;

        g2.drawString(text, textX, textY);
        g2.dispose();

        return image;
    }

    public static void main(String[] args) throws IOException {
        BufferedImage certificate = makeCertificate("Ivan Melnichuk");
        ImageIO.write(certificate, "png", new File("./data/result.png"));

    }


}
