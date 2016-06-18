package com.android.gscaparrotti.bendermobile.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.gscaparrotti.bendermobile.R;
import com.android.gscaparrotti.bendermobile.network.ServerInteractor;

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
    private String ip;

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
        new TableAmountDownloader().execute();
        view.findViewById(R.id.mainUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TableAmountDownloader().execute();
            }
        });
        view.findViewById(R.id.allPending).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onTablePressedEventFired(0);
            }
        });
        return view;
    }

    public void tableAdded(final int tableNumber) {
        int current = ta.getCount();
        for (int i = 0; i < tableNumber - current; i++) {
            ta.addElement(i + 1);
        }
        ta.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnMainFragmentInteractionListener) {
            mListener = (OnMainFragmentInteractionListener) context;
            ip = getActivity().getSharedPreferences("BenderIP", 0).getString("BenderIP", "Absent");
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
        void onTablePressedEventFired(int tableNumber);
    }

    private class TableAdapter extends BaseAdapter {

        private int n = 0;
        private LayoutInflater inflater;

        TableAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return n;
        }

        public void addElement(final Integer i) {
            n++;
        }

        @Override
        public Integer getItem(int position) {
            return position + 1;
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
            tableView.setText(getString(R.string.itemTableText) + table);
            convertView.setLongClickable(true);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onTablePressedEventFired(table);
                }
            });
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.ResetConfirmDialogTitle))
                            .setMessage(R.string.ResetConfirmDialogQuestion)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new TableResetRequestUploader().execute(table);
                                }})
                            .setNegativeButton(android.R.string.no, null).show();
                    return true;
                }
            });
            return convertView;
        }
    }

    private class TableResetRequestUploader extends AsyncTask<Integer, Void, Boolean> {

        private String errorMessage;

        @Override
        protected Boolean doInBackground(final Integer... params) {
            final ServerInteractor serverInteractor = ServerInteractor.getInstance();
            final String command = "RESET TABLE " + params[0];
            boolean success = false;
            final Object input = serverInteractor.sendCommandAndGetResult(ip, 6789, command);
            if (input instanceof Exception) {
                final Exception e = (Exception) input;
                errorMessage = e.toString();
            } else if (input instanceof String) {
                final String stringInput = (String) input;
                if (stringInput.equals("TABLE RESET CORRECTLY")) {
                    success = true;
                } else {
                    errorMessage = stringInput;
                }
            }
            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            if (getActivity() != null) {
                if (success) {
                    Toast.makeText(MainFragment.this.context, getString(R.string.ResetSuccess), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainFragment.this.context, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class TableAmountDownloader extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(final Void... params) {
            final ServerInteractor serverInteractor = ServerInteractor.getInstance();
            final String command = "GET AMOUNT";
            Integer amount = 0;
            final Object input = serverInteractor.sendCommandAndGetResult(ip, 6789, command);
            if (input instanceof Exception) {
                amount = -1;
            } else if (input instanceof Integer){
                amount = (Integer) input;
            }
            return amount;
        }

        @Override
        protected void onPostExecute(final Integer integer) {
            super.onPostExecute(integer);
            if (getActivity() != null) {
                if (integer < 0) {
                    Toast.makeText(MainFragment.this.context, getString(R.string.ServerError), Toast.LENGTH_LONG).show();
                } else {
                    if (MainFragment.this.isVisible()) {
                        MainFragment.this.tableAdded(integer);
                    }
                }
            }
        }
    }
}
