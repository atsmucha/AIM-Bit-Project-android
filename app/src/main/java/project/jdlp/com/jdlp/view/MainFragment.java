package project.jdlp.com.jdlp.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import project.jdlp.com.jdlp.MainActivity;
import project.jdlp.com.jdlp.R;
import project.jdlp.com.jdlp.controller.JdlpGridAdapter;
import project.jdlp.com.jdlp.manager.DataHandler;
import project.jdlp.com.jdlp.model.JdlpItem;

/**
 * Created by atsmucha on 15. 7. 9.
 */
public class MainFragment extends Fragment {
    private View view;
    private MainActivity activity;
    private GridView grid;
    private JdlpGridAdapter jdlpAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);
        activity = (MainActivity)getActivity();
        grid = (GridView)view.findViewById(R.id.fragment_main_grid);
//        jdlpAdapter = new JdlpGridAdapter(activity);

        init();
        return view;
    }   //onCreateView

    public void init() {

        listener.onMainSyncAdapter(jdlpAdapter);

        DataHandler dataHandler = DataHandler.getInstance();
        jdlpAdapter.addAll(dataHandler.getItems());

        grid.setAdapter(jdlpAdapter);

        setMainFragmentListener();
    }   //init()

    public static MainFragment newInstance(JdlpGridAdapter adapter) {
        MainFragment mainFragment = new MainFragment();
        mainFragment.jdlpAdapter = adapter;
        return mainFragment;
    }

    public void setMainFragmentListener() {
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JdlpItem item = jdlpAdapter.getItemJdlpPosition(Integer.valueOf(String.valueOf(id)));
                listener.onMainClickListener(item);
            }
        });

        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            JdlpItem item = (JdlpItem)parent.getItemAtPosition(position);
            listener.onMainLongClickListener(item);

            return false;
            }
        });
    }   //setMainFragmentListener()

    /*
    *   interface
    */
    public interface OnMainFragmentListener {
        void onMainSyncAdapter(JdlpGridAdapter adapter);
        void onMainClickListener(JdlpItem item);
        void onMainLongClickListener(JdlpItem item);
    }
    OnMainFragmentListener listener;

    public void setOnMainFragmentClickListener(OnMainFragmentListener listener) {
        this.listener = listener;
    }

}   //MainFragment
