package network.pkgswitch.management.system;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

public class SplashScreen
{
    private final static JWindow window = new JWindow();
    private long startTime;
    private int minimumMilliseconds;

    public SplashScreen()
    {
        try
        {
            final ImageIcon image = new ImageIcon ( ImageIO.read ( Utils.GetResourceURL ( "splash.png" ) ) );

            window.getContentPane().add ( new JLabel ( "", image, SwingConstants.CENTER ) );

            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            window.setBounds ( ( int ) ( ( screenSize.getWidth() - image.getIconWidth() ) / 2 ),
                ( int ) ( ( screenSize.getHeight() - image.getIconHeight() ) / 2 ),
                image.getIconWidth(), image.getIconHeight() );
        }
        catch ( final IOException e )
        {
            e.printStackTrace();
        }
    }

    public void show ( final int minimumMilliseconds )
    {
        this.minimumMilliseconds = minimumMilliseconds;

        window.setVisible ( true );
        startTime = System.currentTimeMillis();
    }

    public void hide()
    {
        final long elapsedTime = System.currentTimeMillis() - startTime;

        try
        {
            Thread.sleep ( Math.max ( minimumMilliseconds - elapsedTime, 0 ) );
        }
        catch ( final InterruptedException e )
        {
            e.printStackTrace();
        }

        window.setVisible ( false );
    }
}
