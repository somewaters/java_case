import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.sun.tools.javac.util.ServiceLoader;
import org.junit.Test;
import spi.test.Robot;

import javax.swing.plaf.basic.BasicBorders;
import java.util.Iterator;

public class JavaSpiTest {
    @Test
    public void testJavaSPI(){
        ServiceLoader<Robot> serviceLoader = ServiceLoader.load(Robot.class);
        Iterator<Robot> it =  serviceLoader.iterator();
        while(it.hasNext()){
            Robot robot = it.next();
            robot.sayHello();
        }
    }

    @Test
    public void testDubboSPI(){
        ExtensionLoader<Robot> extensionLoader = ExtensionLoader.getExtensionLoader(Robot.class);
        Robot robot = extensionLoader.getExtension("bumblebee");
        robot.sayHello();

    }

}
