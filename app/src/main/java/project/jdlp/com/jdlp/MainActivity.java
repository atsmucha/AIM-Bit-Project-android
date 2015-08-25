package project.jdlp.com.jdlp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


import project.jdlp.com.jdlp.controller.JdlpGridAdapter;
import project.jdlp.com.jdlp.controller.ViewPagerAdapter;
import project.jdlp.com.jdlp.manager.Constants;
import project.jdlp.com.jdlp.manager.DataHandler;
import project.jdlp.com.jdlp.manager.JdlpSocketChannel;
import project.jdlp.com.jdlp.model.JdlpItem;
import project.jdlp.com.jdlp.model.LogicStatus;
import project.jdlp.com.jdlp.view.AddFragment;
import project.jdlp.com.jdlp.view.AddTypeDialog;
import project.jdlp.com.jdlp.view.GetItemDialog;
import project.jdlp.com.jdlp.view.MainFragment;
import project.jdlp.com.jdlp.view.animation.ReaderViewPagerTransformer;
import project.jdlp.com.jdlp.view.animation.TransformType;

public class MainActivity extends FragmentActivity {
    private AddFragment addFragment;
    private MainFragment mainFragment;
    public ViewPager jdlpPager;

    private Context context;
    private GetItemDialog getItemDialog;
    private JdlpGridAdapter mainFragAdapter;
    private EditText addFragTitle;
    private EditText addFragPass;
    private ImageView addFragItemImage;

    private List<Fragment> jdlpFragmentList = new ArrayList();

    private Bitmap itemImageBitmap;
    private DataHandler dataHandler;

    private boolean pressFirstBackKey = false;
    private Timer time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        setMainFragmentListener();
        setAddFragmentListener();

        initSocket();

    }   //onCrate

    @Override
    public void onBackPressed() {
        if(!pressFirstBackKey) {
            Toast.makeText(getApplicationContext(), "한번더 누르시면 종료합니다.", Toast.LENGTH_SHORT).show();
            pressFirstBackKey = true;

            TimerTask second = new TimerTask() {
                @Override
                public void run() {
                    time.cancel();
                    time = null;
                    pressFirstBackKey = false;
                }
            };
            if(time!=null) {
                time.cancel();
                time = null;
            }
            time = new Timer();
            time.schedule(second, 2000);
        }
        else {
            super.onBackPressed();
        }
    }   //onBackPressed

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("MainActivity::", "onStart()");
        dataHandler.threadStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("destory::", "destorydestorydestorydestorydestorydestory");
        dataHandler.disConnect();
    }

    public void init() {
        this.context = getApplicationContext();
        mainFragAdapter = new JdlpGridAdapter(context);

        if(mainFragment == null) {
             mainFragment = mainFragment.newInstance(mainFragAdapter);
        }

        if(addFragment == null) {
            addFragment = new AddFragment();
        }

        pagerSetting();
        setJdlpPagerListener();

    }

    public void pagerSetting() {
        jdlpPager = (ViewPager)findViewById(R.id.jdlp_pager);
        ViewPagerAdapter vpa = new ViewPagerAdapter(getSupportFragmentManager(), jdlpFragmentList);
        jdlpPager.setPageTransformer(false, new ReaderViewPagerTransformer(TransformType.DEPTH));

        jdlpFragmentList.add(mainFragment);
        jdlpFragmentList.add(addFragment);
        jdlpPager.setAdapter(vpa);
        jdlpPager.setCurrentItem(0, true);
        jdlpPager.getAdapter().notifyDataSetChanged();

    }

    public void setJdlpPagerListener() {
        jdlpPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.e("JdlpPager::", "View Position : " + position);
                //addFragment View 초기화
                if (addFragTitle != null) {
                    addFragTitle.setText("");//            key.attach(null);

                    addFragPass.setText("");
                    addFragItemImage.setImageBitmap(null);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }   //setJdlpPagerListener();

    public void setMainFragmentListener() {
//        onJdlpAdapterListener.OnJdlpAdapter(mainFragItemAdapter);

        mainFragment.setOnMainFragmentClickListener(new MainFragment.OnMainFragmentListener() {
            @Override
            public void onMainSyncAdapter(JdlpGridAdapter adapter) {
                mainFragAdapter = adapter;
            }

            @Override
            public void onMainClickListener(JdlpItem item) {
                if( item.getItemPosition() == 0) {
                    Toast.makeText(getApplicationContext(), "사용할 수 없는 슬롯 입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                jdlpPager.setCurrentItem(1, true);
                jdlpPager.getAdapter().notifyDataSetChanged();
                onMainActivityItemTranseListener.OnMainActivityItemTranseListener(item);
            }

            @Override
            public void onMainLongClickListener(final JdlpItem item) {
                //JDLP Item Get Logic
                getItemDialog = new GetItemDialog(MainActivity.this);
                getItemDialog.setOnGetItemDialogListener(new GetItemDialog.OnGetItemDialogListener() {
                    @Override
                    public void OnOKClickListener() {
                        if (item.getItemExist() != Constants.ITEM_EXIST) {
                            Toast.makeText(getApplicationContext(), "아이템이 들어있지 않습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            JdlpItem jdlpItem = mainFragAdapter.getItem(item.getItemPosition());
                            dataHandler.sendData(Constants.JOB_POP_ITEM, jdlpItem);
                        }
                        jdlpPager.setCurrentItem(0);
                        jdlpPager.getAdapter().notifyDataSetChanged();

                        getItemDialog.dismiss();
                    }

                    @Override
                    public void OnNoClickListener() {
                        getItemDialog.dismiss();
                    }
                });
                getItemDialog.show();
            }
        });
    }

    public void setAddFragmentListener() {

        addFragment.setOnAddFragmentClickListener(new AddFragment.onAddFragmentClickListener() {
            @Override
            public void OnAddFragmentNoClickListener() {
                jdlpPager.setCurrentItem(0, true);
                jdlpPager.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void OnAddFragmentOkClickListener(final JdlpItem jdlpItem, LogicStatus addFragLogicStatus) {
                JdlpItem item = jdlpItem;
                Log.v("Main::", item.getItemPosition()+"");
//                        mainFragAdapter.getItem(jdlpItem.getItemPosition());
                switch (addFragLogicStatus) {
                    case JOB_PUSH_ITEM:

                        try {
                            if (item.getItemExist() == Constants.ITEM_EMPTY && item.getItemExist() == Constants.ITEM_BUSY) {
                                Toast.makeText(getApplicationContext(), "이미 아이템이 들어있거나, 다른사용자가 기기를 사용중입니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            dataHandler.sendData(Constants.JOB_PUSH_ITEM, item);

                        } finally {
//                            Toast.makeText(getApplicationContext(), "PUSH_ITEM_SUCCESS", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case JOB_PUSH_ITEM_RAND:

                        break;
                    case JOB_MOD_ITEM:
                        try {
                            if (item.getItemExist() != Constants.ITEM_EXIST) {
                                Toast.makeText(getApplicationContext(), "수정하려는 아이템이 없거나, 다른사용자가 기기를 사용중입니다.", Toast.LENGTH_SHORT).show();
                            }
                            dataHandler.sendData(Constants.JOB_MOD_ITEM, item);
                        } finally {
//                            Toast.makeText(getApplicationContext(), "MOD_ITEM_SUCCESS", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }

//                mainFragAdapter.updateItem(jdlpItem);

                jdlpPager.setCurrentItem(0, true);
                jdlpPager.getAdapter().notifyDataSetChanged();

//                Log.e("MainActivity::AddFragOK", addFragLogicStatus.name() + "");
//                if (jdlpItem != null) {
//                    Log.e("MainActivity::AddFragOK", jdlpItem.getItemPosition() + "");
//                    Log.e("MainActivity::AddFragOK", jdlpItem.getItemTitle() + "");
//                    Log.e("MainActivity::AddFragOK", jdlpItem.getItemBitmap() + "");
//                }

            }

            @Override
            public void OnAddFragmentTackPicClickListener(AddTypeDialog addTypeDialog, JdlpItem jdlpItem) {
                addTypeDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(intent, 1);
            }

            @Override
            public void OnAddFragmentGetPicClickListener(AddTypeDialog addTypeDialog, JdlpItem jdlpItem) {
                addTypeDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2);
            }

            @Override
            public void OnAddFragmentPickPicClickListener(AddTypeDialog addTypeDialog, JdlpItem jdlpItem) {

            }

            @Override
            public void OnAddFragmentSyncView(EditText title, EditText pass, ImageView itemImage) {
                addFragTitle = title;
                addFragPass = pass;
                addFragItemImage = itemImage;
            }
        });
    }//setAddFragmentListener()

    public void initSocket() {
        dataHandler = DataHandler.getInstance();
        dataHandler.sendData(Constants.JOB_CONFIRM, null);

        //MainLooper MainThread Handler 연결하기! 지금 에러남
        dataHandler.setDataHandlerInterrupt(new DataHandler.DataHandlerInterrupt() {
            @Override
            public void onSuccess(final int type, String message, final List<JdlpItem> items) {
                final String resultMessage = message;
                final android.os.Handler dataHandlerMainThread = new android.os.Handler(Looper.getMainLooper());
                dataHandlerMainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        Iterator iter = items.iterator();
                        Log.e("MainActivity::", resultMessage);

                        switch (type) {
                            case Constants.JOB_POP_ITEM:
                                while (iter.hasNext()) {
                                    JdlpItem item = (JdlpItem) iter.next();
                                    if (item.getItemExist() == Constants.ITEM_EMPTY) {
                                        item.setItemTitle("");
                                        item.setItemBitmapSize(0);
                                        item.setItemBitmap(null);
                                        item.setItemPassword("");
                                    }
                                    updateItems(items);
                                }
                                break;
                            case Constants.JOB_CONFIRM:
                                updateItems(items);
                                dataHandler.sendData(Constants.JOB_UPDATE, null);
                                break;
                            case Constants.JOB_UPDATE:
                                while (iter.hasNext()) {
                                    JdlpItem item = (JdlpItem) iter.next();

                                    if (item.getItemExist() == Constants.ITEM_EXIST && item.getItemBitmap() == null) {
                                        dataHandler.sendData(Constants.JOB_IMG_POP, item);
                                        try {
                                            Thread.sleep(200);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                updateItems(items);
                                break;
                            case Constants.JOB_IMG_POP:
                                break;
                            case Constants.JOB_IMG_POP_RECV:
                                updateItems(items);
                                break;
                            case Constants.JOB_PUSH_ITEM:
//                                dataHandler.sendData(Constants.JOB_CONFIRM, null);
                                break;
                            case Constants.JOB_IMG_PUSH_SEND:
                                while (iter.hasNext()) {
                                    JdlpItem item = (JdlpItem) iter.next();
                                    if (item.getItemBitmap() != null) {
                                        item.setItemExist(Constants.ITEM_EXIST);
                                        Log.e("MainActivity::haha", item.toString());
                                    }
                                }
                                updateItems(items);
                                break;
                            case Constants.JOB_MOD_ITEM:
                                Log.e("MainActivity::", "JOB_MOD_ITEM success!!!!!!!!!!!");
                                break;

                        }

//                        Log.e("JOBCODE", "          " + resultMessage);
//                        for (int i = 0; i < items.size(); i++) {
//                            Log.e("pos", mainFragAdapter.getItem(items.get(i).getItemPosition()).getItemPosition() + "");
//                            Log.e("exist", mainFragAdapter.getItem(items.get(i).getItemPosition()).getItemExist() + "");
//                            Log.e("pass", mainFragAdapter.getItem(items.get(i).getItemPosition()).getItemPassword() + "");
//                            Log.e("title", mainFragAdapter.getItem(items.get(i).getItemPosition()).getItemTitle() + "");
//                        }

                    }
                });
                message = "";
            }

            @Override
            public void onFail(int type, String message) {
                Log.e("Interrupt FAIL", message);
                message = "";
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 1:
                if(itemImageBitmap == null) {
                    itemImageBitmap = byteArrayToBitmap(data.getByteArrayExtra("data"));
                }
                addFragItemImage.setImageBitmap(itemImageBitmap);
                break;
            case 2:
                try {
                    Uri selectImageURI = Uri.parse(data.getDataString());
                    Log.e("test", selectImageURI + "");
                    if (itemImageBitmap == null) {
                        itemImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectImageURI);
                        addFragItemImage.setImageBitmap(itemImageBitmap);

                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        itemImageBitmap = null;

    }   //onActivityResult()

    private Bitmap byteArrayToBitmap(byte[] byteArray) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return bitmap;
    }

    private void updateItems(List<JdlpItem> items) {
        if( mainFragAdapter != null )    mainFragAdapter.updateItems(items);
        else return;
    }

    /*
    *   interface
    */
    public interface OnMainActivityItemTranseListener {
        void OnMainActivityItemTranseListener(JdlpItem item);
    }

    OnMainActivityItemTranseListener onMainActivityItemTranseListener;

    public void setOnMainActivityItemTranseListener(OnMainActivityItemTranseListener listener) {
        this.onMainActivityItemTranseListener = listener;
    }
}   //MainActivity
