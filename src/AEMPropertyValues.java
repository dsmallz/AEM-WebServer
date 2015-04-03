import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Dennis on 3/29/2015.
 */
public class AEMPropertyValues {
    public Properties getPropValues() throws IOException {
        String result = "";
        Properties prop = new Properties();
        String propFileName = "server.properties";

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("Property file '" + propFileName + "' not found in the classpath");
        }
        return prop;
    }
}
