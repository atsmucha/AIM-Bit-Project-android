package project.jdlp.com.jdlp.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import project.jdlp.com.jdlp.model.JdlpItem;
import project.jdlp.com.jdlp.model.LogicStatus;

/**
 * Created by atsmucha on 15. 7. 28.
 * @author atsmucha
 * @since 20150728
 */
public class DataHandler{
    private static DataHandler dataHandler;
    private JdlpSocketChannel jdlpSocketChannel;
    private List<JdlpItem> items = new ArrayList<>();
    private LinkedBlockingQueue<byte[]> dataQueue;

    private boolean isImgRecvReady = false;
    private static boolean RECVIMG_READY = true;
    private static boolean RECVIMG_NOT_READY = false;

    private ByteBuffer recvQueueImgBuffer;
    private JdlpItem imageItem;
    private int recvImghasRemainig;

    /**
     *
     * @return Instance of DataHandler Class
     */
    public static DataHandler getInstance() {
        if(dataHandler == null) {
            dataHandler = new DataHandler();
        }
        return dataHandler;
    }

    private DataHandler() {
        jdlpSocketChannel  = new JdlpSocketChannel();
        dataQueue = (LinkedBlockingQueue)jdlpSocketChannel.getDataQueue();
        dataQueue.clear();
        init();
    }

    public void init() {
        itemInstantaion();
    }

    public void itemInstantaion() {
        JdlpItem item0 = new JdlpItem(0);
        JdlpItem item1 = new JdlpItem(1);
        JdlpItem item2 = new JdlpItem(2);
        JdlpItem item3 = new JdlpItem(3);
        JdlpItem item4 = new JdlpItem(4);
        JdlpItem item5 = new JdlpItem(5);
        JdlpItem item6 = new JdlpItem(6);
        JdlpItem item7 = new JdlpItem(7);
        JdlpItem item8 = new JdlpItem(8);
        items.add(item0);
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        items.add(item5);
        items.add(item6);
        items.add(item7);
        items.add(item8);
    }

    /*****************************
            SetGet
     ****************************/
    public List<JdlpItem> getItems() {
        return items;
    }

    public void setItems(List<JdlpItem> items) {
        this.items = items;
    }

    public Queue getDataQueue() {
        return dataQueue;
    }

    public void disConnect() {
        this.jdlpSocketChannel.disConnect();
    }

    public void threadStart() {
        jdlpSocketChannel.socketStart();
    }

    /**
     *
     * @param buffer
     * Slice할 buffer
     * @param bufferPosition
     * slice할 시작점
     * @param byteLength
     * slice할 끝점
     * @return
     */
    public byte[] bufferToByte(ByteBuffer buffer, int bufferPosition, int byteLength) {
        buffer.position(bufferPosition);
        byte[] array = buffer.array();
        int arrayOffset = buffer.arrayOffset();

        return Arrays.copyOfRange(array, arrayOffset + buffer.position(), arrayOffset + byteLength);
    }

    /**
     * @param value
     * casting할 int값
     * @return
     * casting되어서 reture하는 byte[]
     */
    private byte[] integerToByte(int value) {
        ByteBuffer castBuffer = ByteBuffer.allocate(4);
        castBuffer.order(ByteOrder.LITTLE_ENDIAN);
        castBuffer.putInt(value);
        byte[] result = castBuffer.array();
        return result;
    }
    /**
     * @param buffer
     * This param is equal recvData(buffer)
     * @return bodyBuffer
     * BODY만 들어있는 buffer을 return
     */
    public ByteBuffer getBody(ByteBuffer buffer) {
        int bodySize = buffer.limit()-Constants.HEADER_SIZE;
        byte[] body = bufferToByte(buffer, Constants.HEADER_SIZE, buffer.limit());

        return ByteBuffer.allocate(bodySize).put(body).order(ByteOrder.LITTLE_ENDIAN);
    }

    private String typeToString(int type) {
        switch(type) {
            case Constants.JOB_POP_ITEM:
                return "JOB_POP_ITEM";
            case Constants.JOB_PUSH_ITEM:
                return "JOB_PUSH_IMG_ITEM";
            case Constants.JOB_PUSH_ITEM_RAND:
                return "JOB_PUSH_IMG_ITEM_RAND";
            case Constants.JOB_MOD_ITEM:
                return "JOB_MOD_IMG_ITEM";
            case Constants.JOB_UPDATE:
                return "JOB_UPDATE";
            case Constants.JOB_IMG_POP:
                return "JOB_IMG_POP";
            case Constants.JOB_CONFIRM:
                return "JOB_CONFIRM";
            case Constants.JOB_IMG_POP_RECV:
                return "JOB_IMG_POP_RECV";
            case Constants.JOB_IMG_PUSH_SEND:
                return "JOB_IMG_PUSH_SEND";
        }
        return null;
    }

    /*****************************
        Send function
    *****************************/
    public void sendData(int sendType, JdlpItem item) {
        byte[] sendByteArray = null;
        String sendTypeToString = typeToString(sendType);
//        Log.v("DataHandler::", item.getItemPosition()+"sendData_JOB_IMG_POP");

        switch(sendType) {
            case Constants.JOB_POP_ITEM:
                sendByteArray = sendCreateByteArray(sendType, item);
                if(sendByteArray == null) {
                    interrupt.onFail(sendType, "ITERRUPT FAIL 아이템이 EMPTY라서 가져올 item 없음");
                    return;
                }
                jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_READ);
                try {
                    dataQueue.add(sendByteArray);
                } finally {
                    items.get(item.getItemPosition()).setItemExist(Constants.ITEM_BUSY);
                }
                break;
            case Constants.JOB_PUSH_ITEM:case Constants.JOB_PUSH_ITEM_RAND:case Constants.JOB_MOD_ITEM:
                sendByteArray = sendCreateByteArray(sendType, item);
                if(sendByteArray == null) {
                    if(sendType == Constants.JOB_PUSH_ITEM) {
                        interrupt.onFail(sendType, "INTERRUPT FAIL::" + sendTypeToString);
                    } else if(sendType == Constants.JOB_PUSH_ITEM_RAND) {
                        interrupt.onFail(sendType, "INTERRUPT FAIL::" + sendTypeToString);
                    } else if(sendType == Constants.JOB_MOD_ITEM) {
                        interrupt.onFail(sendType, "INTERRUPT FAIL::" + sendTypeToString);
                    }
                    return;
                }

                try {
                    jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_READ);
                    dataQueue.add(sendByteArray);
                } finally {
                    jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_WRITE);
                    item = items.get(item.getItemPosition());
//                    byte[] resizingBitmapByteArray = item.bitmapToByteArray(item.getItemBitmap());
//                    Bitmap resizingBitmap = item.byteArrayToBitmap(resizingBitmapByteArray);
//                    byte[] resizingResultByteArray = item.bitmapToByteArray(resizingBitmap);
//                    sendImg(resizingResultByteArray, item, sendType);
                    sendImg(item.bitmapToByteArray(item.getItemBitmap()), item, sendType);
                }

                break;
            case Constants.JOB_UPDATE:case Constants.JOB_CONFIRM:
                if(sendType == Constants.JOB_UPDATE) {
                    jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_READ);
                    dataQueue.add(sendCreateByteArray(sendType, null));
                    return;
                }
                    jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_READ);
                    dataQueue.add(sendCreateByteArray(Constants.JOB_CONFIRM, null));
                break;
            case Constants.JOB_IMG_POP:

                jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_READ);
                dataQueue.add(sendCreateByteArray(Constants.JOB_IMG_POP, item));
                break;
        }

    }   //sendData

    /**
     *
     * @param sendType
     * SendType는 Constants의 로직 번호
     * @param item
     *  Jdlp item
     * @return
     * JdlpSocketChannel의 LinkedBlocking queue에 들어갈 byte array
     */
    private byte[] sendCreateByteArray(int sendType, JdlpItem item) {
        ByteBuffer sendBuffer = null;
        if(item != null) {
//            item = items.get(item.getItemPosition());
            Log.v("DataHandler;;", item.getItemPosition()+"sendCreate");
        }

        switch(sendType) {
            //JOB_POP_ITEM
            case Constants.JOB_POP_ITEM:

                if(item.getItemExist() == Constants.ITEM_EMPTY) {
                    return null;
                }
                    sendBuffer = ByteBuffer.allocate(Constants.HEADER_SIZE + Constants.JOB_POP_ITEM_BODY_SIZE).order(ByteOrder.LITTLE_ENDIAN);
                    sendBuffer.putInt(sendType);
                    sendBuffer.putInt(Constants.ITEM_INT_SIZE);

                    sendBuffer.position(Constants.HEADER_SIZE);
                    sendBuffer.putInt(item.getItemPosition());
                return sendBuffer.array();
            //JOB_PUSH_ITEM, JOB_PUSH_RAND, JOB_MOD_ITEM
            case Constants.JOB_PUSH_ITEM:case Constants.JOB_PUSH_ITEM_RAND:case Constants.JOB_MOD_ITEM:
                if(sendType == Constants.JOB_MOD_ITEM
                        && item.getItemExist() != Constants.ITEM_EXIST
                        && item.getItemTitle() == null
                        && item.getItemPassword() == null
                        && item.getItemPosition() == '\0') {
                    return null;
                } else if(sendType == Constants.JOB_PUSH_ITEM_RAND
                        && item.getItemExist() != Constants.ITEM_EMPTY
                        && item.getItemPosition() == '\0') {
                    return null;
                }
                if(sendType == Constants.JOB_PUSH_ITEM
                        && item.getItemExist() != Constants.ITEM_EMPTY
                        && item.getItemTitle() == null
                        && item.getItemPassword() == null
//                        && item.getItemPosition() == '\0'
                        ) {
                    interrupt.onFail(sendType, "INTERRUPT FAIL::     PUSH_ITEM");
                    return null;
                }

                sendBuffer = ByteBuffer.allocate(Constants.JOB_PUSH_ITEM_SIZE).order(ByteOrder.LITTLE_ENDIAN);
                sendBuffer.order(ByteOrder.LITTLE_ENDIAN);
                sendBuffer.putInt(sendType);
                sendBuffer.putInt((int) (136 & 0xffffffffL));

                sendBuffer.position(Constants.HEADER_SIZE);
                byte[] bodyByte = item.getItemPassword().getBytes(Charset.forName("UTF-8"));
                sendBuffer.put(bodyByte, 0, bodyByte.length);

                sendBuffer.position(Constants.HEADER_SIZE + Constants.ITEM_BODY_PASS_SIZE);
                bodyByte = item.getItemTitle().getBytes(Charset.forName("UTF-8"));
                sendBuffer.put(bodyByte, 0, bodyByte.length);
                sendBuffer.position(Constants.HEADER_SIZE + (Constants.ITEM_BODY_PASS_SIZE * 2));
                sendBuffer.putInt(item.getItemPosition());

                sendBuffer.position(Constants.HEADER_SIZE + (Constants.ITEM_BODY_PASS_SIZE * 2) + Constants.ITEM_INT_SIZE);
                sendBuffer.putInt(item.getItemBitmapSize());
                sendBuffer.flip();

                return sendBuffer.array();
            case Constants.JOB_IMG_POP:
                sendBuffer = ByteBuffer.allocate(Constants.JOB_IMG_POP_SIZE).order(ByteOrder.LITTLE_ENDIAN);
                sendBuffer.putInt(sendType);    //uiJobCode
                sendBuffer.putInt(Constants.ITEM_INT_SIZE*2);   //uiSize
                sendBuffer.putInt(0);   //uiParam
                sendBuffer.putInt(item.getItemPosition());  //UiItemPos
                sendBuffer.putInt(0);    //UiItemSize

                return sendBuffer.array();
            case Constants.JOB_CONFIRM:case Constants.JOB_UPDATE:
                sendBuffer = ByteBuffer.allocate(Constants.HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
                sendBuffer.putInt(sendType);
                return sendBuffer.array();
        }
        return null;
    }       //sendCreateByteArray

    /**
     * @param sendImgByteArray
     * push, mod, push_rand일경우에 이미지를 push.mod.push_rand하고 난뒤에
     * 서버에 img Byte를 1024사이즈 만큼 보낸다.
     *
     */
    private void sendImg(byte[] sendImgByteArray, JdlpItem item, int sendType) {
        byte[] itemImgByteArray = sendImgByteArray;
        byte[] resizingByteArray;
        ByteBuffer imgSendBuffer;
        int pointer = 0;
        int hasRemaining = itemImgByteArray.length;
        int count = 0;

        String sendTypeToString = typeToString(sendType);

        JdlpItem updateItem = items.get(item.getItemPosition());
        while(true) {
            if(hasRemaining <= Constants.ITEM_IMG_BUF_SIZE) {
                resizingByteArray = new byte[hasRemaining];
                for(int i = 0; i < hasRemaining; i++) {
                    itemImgByteArray[pointer+i] = resizingByteArray[i];
                }

                imgSendBuffer = ByteBuffer.allocate(hasRemaining).order(ByteOrder.LITTLE_ENDIAN);
                imgSendBuffer.put(resizingByteArray);

                Bitmap bitmap = item.byteArrayToBitmap(itemImgByteArray);
                item.setItemBitmap(bitmap);

                try {
                    dataQueue.add(imgSendBuffer.array());
                } finally {
                    updateItem.setItemBitmap(item.getItemBitmap());

                    interrupt.onSuccess(Constants.JOB_IMG_PUSH_SEND, "INTERRUPT SUCCESS::" + sendTypeToString, items);
                    jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_READ);
                    imgSendBuffer.clear();
                }
                return;
            } else {
                resizingByteArray = new byte[Constants.ITEM_IMG_BUF_SIZE];
                for(int i = 0; i < Constants.ITEM_IMG_BUF_SIZE; i++) {
                    resizingByteArray[i] = itemImgByteArray[pointer+i];
                }
                imgSendBuffer = ByteBuffer.allocate(Constants.ITEM_IMG_BUF_SIZE).order(ByteOrder.LITTLE_ENDIAN);
                imgSendBuffer.put(resizingByteArray);

                try {
                    jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_WRITE);
                    dataQueue.add(imgSendBuffer.array());
                } finally {
                    imgSendBuffer.clear();
                }
            }
            pointer += Constants.ITEM_IMG_BUF_SIZE;
            hasRemaining -= 1024;
            count++;
        }

    }       //sendImg

    /*****************************
        Recv function
    *****************************/
    /**
     * @param buffer
     * ReciveData buffer from server(SocketChannel readBuffer)
     */
    public void recvData(ByteBuffer buffer) {
        if(isImgRecvReady) {
            recvImg(imageItem, Constants.JOB_IMG_POP_RECV);
            return;
        }

        int recvType = buffer.get(0);

        switch(recvType) {
            case Constants.JOB_POP_ITEM:case Constants.JOB_PUSH_ITEM:case Constants.JOB_PUSH_ITEM_RAND:case Constants.JOB_MOD_ITEM:
                jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_WRITE);
                recvHead(recvType);
//                if(recvType == Constants.JOB_POP_ITEM) recvHead(Constants.JOB_POP_ITEM);
//                else if(recvType == Constants.JOB_PUSH_ITEM) recvHead(Constants.JOB_PUSH_ITEM);
//                else if(recvType == Constants.JOB_PUSH_ITEM_RAND) recvHead(Constants.JOB_PUSH_ITEM_RAND);
//                else if(recvType == Constants.JOB_MOD_ITEM) recvHead(Constants.JOB_MOD_ITEM);
                break;
            case Constants.JOB_UPDATE:
                jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_WRITE);
                recvUpdateItem(recvType);
                break;
            case Constants.JOB_IMG_POP:
                jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_READ);
                recvImgPopItem(recvType);
                break;
            case Constants.JOB_CONFIRM:
                jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_WRITE);
                recvConfirmItem(recvType);
                break;
            default:    //JOB_IMG_POP

                break;

        }

    }   //recvData

    /**
     * @param recvType
     * POP,PUSH,PUSH_RAND,MOD의 recv되는 Header는 똑같아서 한 함수로 묶는다
     */
    private void recvHead(final int recvType) {
        final String recvTypeToString = typeToString(recvType);

        jdlpSocketChannel.setResponseHandler(new JdlpSocketChannel.ResponseHandler() {
            @Override
            public void onSuccess(ByteBuffer buffer) {
                Iterator iter = items.iterator();
                while(iter.hasNext()) {
                    JdlpItem item = (JdlpItem)iter.next();
                    if(item.getItemExist() == Constants.ITEM_BUSY)      item.setItemExist(Constants.ITEM_EMPTY);
                }
                interrupt.onSuccess(recvType, "INTERRUPT SUCCESS::" + recvTypeToString, items);
            }

            @Override
            public void onFail(Exception e) {
                interrupt.onFail(recvType, "INTERRUT FAIL::" + recvTypeToString);
            }
        });
    }   //recvHead

    public void recvImgPopItem(final int recvType) {
        final String recvTypeToString = typeToString(recvType);

        jdlpSocketChannel.setResponseHandler(new JdlpSocketChannel.ResponseHandler() {
            @Override
            public void onSuccess(ByteBuffer buffer) {
                ByteBuffer bodyBuffer = getBody(buffer);
                bodyBuffer.flip();
                imageItem = new JdlpItem(bodyBuffer.getInt());

                imageItem.setItemBitmapSize(bodyBuffer.getInt());

                recvImghasRemainig = imageItem.getItemBitmapSize();

                byte[] sliceByteArray = bufferToByte(bodyBuffer, bodyBuffer.position(), bodyBuffer.capacity());

//                recvQueueImgBuffer = ByteBuffer.allocateDirect(imageItem.getItemBitmapSize()).order(ByteOrder.LITTLE_ENDIAN);
                recvQueueImgBuffer = ByteBuffer.allocate(imageItem.getItemBitmapSize()).order(ByteOrder.LITTLE_ENDIAN);
                recvQueueImgBuffer.put(sliceByteArray);

                recvImghasRemainig -= sliceByteArray.length;
                isImgRecvReady = RECVIMG_READY;
            }

            @Override
            public void onFail(Exception e) {
                interrupt.onFail(recvType, "INTERRUT FAIL::"+recvTypeToString);
            }
        });
    }       //recvImgPopItem

    public JdlpItem recvImg(final JdlpItem item, final int recvType) {
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        final String recvTypeToString = typeToString(recvType);

        jdlpSocketChannel.setResponseHandler(new JdlpSocketChannel.ResponseHandler() {
            @Override
            public void onSuccess(ByteBuffer buffer) {
                buffer.flip();
                if( recvImghasRemainig <= 4096 ) {

                    recvQueueImgBuffer.put(buffer.array(), 0, recvImghasRemainig);
                    recvQueueImgBuffer.rewind();

                    item.setItemBitmap(item.byteArrayToBitmap(recvQueueImgBuffer.array()));
                    items.get(item.getItemPosition()).setItemBitmapSize(item.getItemBitmapSize());
                    items.get(item.getItemPosition()).setItemBitmap(item.getItemBitmap());

                    Log.e("recvImg::", items.get(item.getItemPosition()).toString());
                    if(items.get(item.getItemPosition()).getItemBitmap() != null)   interrupt.onSuccess(Constants.JOB_IMG_POP_RECV, "INTERRUPT SUCCESS::"+recvTypeToString, items);
                    else interrupt.onFail(Constants.JOB_IMG_POP_RECV, "INTERRUPT FAIL::" + recvTypeToString);

                    imageItem = null;
                    recvQueueImgBuffer.clear();

                    recvImghasRemainig = 0;
                    jdlpSocketChannel.setKeyMode(Constants.SELECT_KEYMODE_WRITE);
                    isImgRecvReady = RECVIMG_NOT_READY;

                    return;
                } else {
                    recvQueueImgBuffer.put(buffer.array());
                    recvImghasRemainig -= buffer.capacity();
                    buffer.clear();
                    isImgRecvReady = RECVIMG_READY;
                }
            }

            @Override
            public void onFail(Exception e) {
                isImgRecvReady = RECVIMG_NOT_READY;
                interrupt.onFail(Constants.JOB_IMG_POP_RECV, "INTERRUPT::FAIL"+recvTypeToString);
            }
        });
        return null;
    }


    /*
        여러 사용자와 Serialization를 맞추기 위해 사용된다.
        uiItemStatus[9] : 0 -> empty,   1 -> Exist,     2 -> busy(사용불가능)
     */
    public void recvConfirmItem(final int recvType) {
        final String recvTypeToString = typeToString(recvType);

        jdlpSocketChannel.setResponseHandler(new JdlpSocketChannel.ResponseHandler() {
            @Override
            public void onSuccess(ByteBuffer buffer) {
                ByteBuffer bodyBuffer = getBody(buffer);
                Log.e("remaining", bodyBuffer.remaining() + "");
                bodyBuffer.flip();
                while(bodyBuffer.hasRemaining()) {
                    switch(bodyBuffer.getInt()) {
                        case Constants.ITEM_EMPTY:
                            items.get( positioning( (bodyBuffer.position() / 4)-1) ).setItemExist(Constants.ITEM_EMPTY);
                            break;
                        case Constants.ITEM_EXIST:
                            items.get( positioning( (bodyBuffer.position() / 4)-1) ).setItemExist(Constants.ITEM_EXIST);
                            break;
                        case Constants.ITEM_BUSY:
                            items.get( positioning( (bodyBuffer.position() / 4)-1) ).setItemExist(Constants.ITEM_BUSY);
                            break;
                    }
                }
                    interrupt.onSuccess(recvType, "INTERRUPT SUCCESS::" + recvTypeToString, items);
            }

            @Override
            public void onFail(Exception e) {
                interrupt.onFail(recvType, "INTERRUPT FAIL::" + recvTypeToString);
            }
        });
    }   //setConfirmItem

    private Integer positioning(int position) {
        switch(position) {
            case 0:
                return 6;
            case 1:
                return 7;
            case 2:
                return 8;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 0;
            case 7:
                return 1;
            case 8:
                return 2;
        }
        return null;
    }
    /*
        기기에 상태를 가져온다.
        uiItemPos[9] : 0 -> empty,   1 -> Exist
    */
    public void recvUpdateItem(final int recvType) {
        final String recvTypeToString = typeToString(recvType);

        jdlpSocketChannel.setResponseHandler(new JdlpSocketChannel.ResponseHandler() {
            @Override
            public void onSuccess(ByteBuffer buffer) {
                ByteBuffer updateBuffer = getBody(buffer);
                updateBuffer.flip();

                byte[] itemPosByte = bufferToByte(updateBuffer, updateBuffer.position(), Constants.ITEM_BODY_POS_SIZE);

                byte[] itemPasswdByte = bufferToByte(updateBuffer, Constants.ITEM_BODY_POS_SIZE, Constants.ITEM_BODY_POS_SIZE + Constants.JOB_UPDATE_BODY_PASS_TITLE_SIZE);

                byte[] itemTitleByte = bufferToByte(updateBuffer, updateBuffer.position()+itemPasswdByte.length, updateBuffer.limit());

                Log.e("posByte.length", itemPosByte.length+"");
                Log.e("passByte.length", itemPasswdByte.length + "");
                Log.e("titleBYte.length", itemTitleByte.length + "");

                setUpdateItemPassTitle(recvType, itemPasswdByte, Constants.JOB_UPDATE_BODY_PASS);
                setUpdateItemPassTitle(recvType, itemTitleByte, Constants.JOB_UPDATE_BODY_TITLE);

            }

            @Override
            public void onFail(Exception e) {
                interrupt.onFail(recvType, "INTERRUPT FAIL::"+recvTypeToString);
            }
        });

    }   //setUpdateItem

    /**
     *
     * @param passTitle
     * updateBuffer(getBody)에서 Slice한 pass, title byte[]
     * @param bodyName
     * update bodyName -> title? passwd?
     */
    private void setUpdateItemPassTitle(int recvType, byte[] passTitle, int bodyName) {
        String recvTypeToString = typeToString(recvType);

        Iterator iter = items.iterator();
        int pointer = 0;
        StringBuffer result = new StringBuffer("");
        result.setLength(Constants.ITEM_BODY_PASS_SIZE);

        while(iter.hasNext()) {
            JdlpItem item = (JdlpItem)iter.next();
            result.delete(0, result.length());

            if(item.getItemExist() == Constants.ITEM_EXIST) {
                for(int i = pointer; i < pointer + Constants.ITEM_BODY_TITLE_SIZE; i++) {
                    if(passTitle[i] != 0) {
                        result.append(String.valueOf((char)passTitle[i]));
                    }
                }

                if(bodyName == Constants.JOB_UPDATE_BODY_PASS)        item.setItemPassword(result.toString());
                else if(bodyName == Constants.JOB_UPDATE_BODY_TITLE)    item.setItemTitle(result.toString());

            }
                if( item.getItemTitle() != null
                        && item.getItemPassword() != null) {
//                    item.getItemPosition() != '\0'
//                    Log.e("dataHandler::", "    recvUpdate" + "itemPosition ->" + item.getItemPosition() + "");
//                    Log.e("dataHandler::", "    recvUpdate" + "itemTitle ->" + item.getItemTitle() + "");
//                    Log.e("dataHandler::", "    recvUpdate" + "itemPass ->" + item.getItemPassword() + "");
                    interrupt.onSuccess(recvType, "INTERRUPT SUCCESS::"+recvTypeToString, items);
                }
                    pointer += Constants.ITEM_BODY_TITLE_SIZE;
        }
    }   //setUpdateItemPassTitle

    public interface DataHandlerInterrupt {
        void onSuccess(int type, String message, List<JdlpItem> items);
        void onFail(int type, String message);
    }

    DataHandlerInterrupt interrupt;

    public void setDataHandlerInterrupt(DataHandlerInterrupt interrupt) {
        this.interrupt = interrupt;
    }


}   //ByteHandler
