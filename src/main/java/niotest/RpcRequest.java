package niotest;

import java.util.concurrent.atomic.AtomicLong;

public class RpcRequest {
    private static AtomicLong atomicLong = new AtomicLong();

    private long requestId;
    private String interfaceName;

    private String methodName;

    private Class<?>[] paramTypes;

    private Object[] args;

    public RpcRequest(){
        requestId=atomicLong.getAndIncrement();
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
