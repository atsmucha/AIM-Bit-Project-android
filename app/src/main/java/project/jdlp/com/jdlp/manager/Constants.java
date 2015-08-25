package project.jdlp.com.jdlp.manager;

/**
 * Created by atsmucha on 15. 7. 28.
 */
public class Constants {
    //HEADER
    public static final int HEADER_SIZE = 12;
    //BODY
    public static final int ITEM_BODY_POS_SIZE = 36;
    public static final int ITEM_BODY_PASS_SIZE = 64;
    public static final int ITEM_BODY_TITLE_SIZE = 64;

    //JOB_ITEM
    public static final byte JOB_POP_ITEM = 0;
    public static final byte JOB_POP_ITEM_BODY_SIZE = 4;

    public static final byte JOB_PUSH_ITEM = 1;
    public static final int JOB_PUSH_ITEM_SIZE = 148;
    public static final byte JOB_PUSH_ITEM_RAND = 2;

    public static final byte JOB_IMG_PUSH_SEND = 8;

    public static final byte JOB_MOD_ITEM = 3;

    public static final byte JOB_UPDATE = 4;
    public static final int JOB_UPDATE_BODY_PASS_TITLE_SIZE = 9 * ITEM_BODY_TITLE_SIZE;
    public static final byte JOB_UPDATE_BODY_PASS = 0;
    public static final byte JOB_UPDATE_BODY_TITLE = 1;

    public static final byte JOB_IMG_POP = 5;
    public static final int JOB_IMG_POP_SIZE = 20;

    public static final byte JOB_IMG_POP_RECV = 7;


    public static final byte JOB_CONFIRM = 6;

//    public static final byte JOB_IMG_PUSH = 7;

    //DATAHANDLER USEED CONSTANTS
    public static final int ITEM_INT_SIZE = 4;
    public static final int ITEM_CHAR_SIZE = 9;

    public static final byte ITEM_EMPTY = 0;
    public static final byte ITEM_EXIST = 1;
    public static final byte ITEM_BUSY = 2;

    public static final int ITEM_IMG_BUF_SIZE = 1024;

    //JdlpSocketChannel config
    public static boolean SELECT_KEYMODE_WRITE = true;
    public static boolean SELECT_KEYMODE_READ = false;
}
