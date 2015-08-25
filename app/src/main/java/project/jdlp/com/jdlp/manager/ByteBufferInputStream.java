package project.jdlp.com.jdlp.manager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by atsmucha on 15. 8. 20.
 */
public class ByteBufferInputStream extends InputStream{
    private int bbisInitPos;
    private int bbisLimit;
    private ByteBuffer bbisBuffer;

    public ByteBufferInputStream(ByteBuffer buffer) {
        this(buffer, buffer.limit() - buffer.position());
    }

    public ByteBufferInputStream(ByteBuffer buffer, int bbisLimit) {
        this.bbisBuffer = bbisBuffer;
        this.bbisLimit = bbisLimit;
        this.bbisInitPos = bbisBuffer.position();
    }

    @Override
    public int read() throws IOException {
        if(bbisBuffer.position() - bbisInitPos > bbisLimit) return -1;
        return bbisBuffer.get();
    }
}
