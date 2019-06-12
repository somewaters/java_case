package xuji;

public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name){
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hello"+name;
    }
}
