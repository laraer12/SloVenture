package si.um.feri.sloventure.data.crowd;

import com.badlogic.gdx.Gdx;

import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class DesktopSslFactory {
    public static SSLSocketFactory fromP12(String p12Path, String password)
        throws Exception {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(
            Gdx.files.internal(p12Path).read(),
            password.toCharArray()
        );

        KeyManagerFactory kmf =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password.toCharArray());

        TrustManagerFactory tmf =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);

        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }
}
