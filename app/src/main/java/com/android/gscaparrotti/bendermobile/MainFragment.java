package com.android.gscaparrotti.bendermobile;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMainFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    private GridView gv;
    private TableAdapter ta;
    private Context context;
    private int tableNumber;

    private OnMainFragmentInteractionListener mListener;

    public MainFragment() {
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        gv = (GridView) view.findViewById(R.id.tablesContainer);
        ta = new TableAdapter(context);
        gv.setAdapter(ta);
        new TableNumberGetter().execute();
        return view;
    }

    public void tableAdded(final int tableNumber) {
        int current = ta.getCount();
        for (int i = 0; i < tableNumber - current; i++) {
            ta.addElement(i + 1);
        }
        gv.setAdapter(ta);
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnMainFragmentInteractionListener) {
            mListener = (OnMainFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMainFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnMainFragmentInteractionListener {
        void onTablePressed(int tableNumber);
    }

    private class TableAdapter extends BaseAdapter {

        private List<Integer> list = new LinkedList<>();
        private LayoutInflater inflater;

        TableAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        public void addElement(final Integer i) {
            list.add(i);
        }

        @Override
        public Integer getItem(int position) {
            return Integer.valueOf(list.get(position));
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_table, parent, false);
            }
            final Integer table = getItem(position);
            final TextView tableView = (TextView) convertView.findViewById(R.id.table);
            tableView.setText(getString(R.string.itemTableText) + " " + table);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onTablePressed(table);
                }
            });
            return convertView;
        }
    }

    private class TableNumberGetter extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(final Void... params) {
            final ServerInteractor serverInteractor = ServerInteractor.getInstance();
            final String command = "GET AMOUNT";
            Integer amount = 0;
            try {
                amount = (Integer) serverInteractor.sendCommandAndGetResult("10.0.2.2", 6789, command);
            } catch (Exception e) {
                Log.e("exception", e.getMessage());
                amount = -1;
            }
            return amount;
        }

        @Override
        protected void onPostExecute(final Integer integer) {
            super.onPostExecute(integer);
            if (integer < 0) {
                Toast.makeText(MainFragment.this.context, "Impossibile comunicare col server", Toast.LENGTH_LONG).show();
            } else {
                MainFragment.this.tableAdded(integer);
            }
        }
    }
}
